package com.example.springappresttemplateservice.controller;


import com.example.springappresttemplateservice.model.FileInfo;
import com.example.springappresttemplateservice.model.Product;
import com.example.springappresttemplateservice.model.ResponseMessage;
import com.example.springappresttemplateservice.service.FilesStorageService;
import com.example.springappresttemplateservice.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ProductController {

    @Autowired
    ProductRepo productRepo;
    @Autowired
    FilesStorageService storageService;

    @GetMapping("/products")
    private List<Product> findAllProducts() {

        return productRepo.findAll();
    }

    @PostMapping("/products")
    private Product createProduct(@RequestBody Product product) {

        return productRepo.save(product);
    }

    @PostMapping("/upload")
    public ResponseEntity<ResponseMessage> uploadFile(@RequestParam("file") MultipartFile file) {
        String message = " ";

        try {
            storageService.save(file);

            message = "Uploaded the file successfully: " + file.getOriginalFilename();
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Could not upload the file: " + file.getOriginalFilename() + ". Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileInfo>> getListFiles() {
        List<FileInfo> fileInfos = storageService.loadAll().map(path -> {
            String filename = path.getFileName().toString();
            String url = MvcUriComponentsBuilder
                    .fromMethodName(ProductController.class, "getFile", path.getFileName().toString()).build().toString();

            return new FileInfo(filename, url);
        }).collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Resource file = storageService.load(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }

    @PutMapping("/products/{id}")
    public Product changeProduct(@RequestBody Product product, @PathVariable int id) {

        productRepo.findById(id).get();

        product.setProductName(product.getProductName());
        product.setDescription(product.getDescription());

        return productRepo.save(product);
    }

    @DeleteMapping("/products/{id}")
    public String removeProduct(@PathVariable int id) {

        productRepo.deleteById(id);
        return "Remove product with id: " + id;
    }

}

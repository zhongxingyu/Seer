 package com.irondish.mvc;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 public class ProductController {
 
 	@Autowired
 	private ProductCatalog catalog;
 	
	@RequestMapping("/products")
 	public String list(Model model) {
 		
 		model.addAttribute("products", catalog.getProducts());
 		return "listProducts";
 	}
 	
	@RequestMapping("/newProduct")
 	public String newProduct() {
 		return "newProduct";
 	}
 }

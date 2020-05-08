 package com.irondish.mvc;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 public class ProductController {
 
 	@Autowired
 	private ProductCatalog catalog;
 	
	@RequestMapping("/product/list")
 	public String list(Model model) {
 		
 		model.addAttribute("products", catalog.getProducts());
 		return "listProducts";
 	}
 	
	@RequestMapping("/product/form")
 	public String newProduct() {
 		return "newProduct";
 	}
	
	@RequestMapping("/product/add")
	public String add() {
		return "xxx";
	}

 }

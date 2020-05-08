 package com.irondish.mvc;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.FieldError;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller
 @RequestMapping("/product/*")
 public class ProductController {
 
 	@Autowired
 	private ProductCatalog catalog;
 	
 	@RequestMapping("list")
 	public String list(Model model) {
 		
 		model.addAttribute("products", catalog.getProducts());
 		return "listProducts";
 	}
 	
 	@RequestMapping("form")
	public String newProduct(Model model) {
		model.addAttribute("product", new Product());
 		return "newProduct";
 	}
 	
 	@RequestMapping("add")
 	public String add(@ModelAttribute Product product, BindingResult bindingResult, Model model) {
 		bindingResult.addError(new FieldError("product", "name", "nem tetszik"));
 		if (bindingResult.hasErrors()) {
 			model.addAttribute("errorMsg", "baj van ...");
 			return "newProduct";
 		}
 		
 		catalog.addProduct(product);
 		
 		return "redirect:/product/list";
 	}
 	
 	@ExceptionHandler(Exception.class)
 	public String bajVan() {
 		
 		return "newProduct";
 	}
 
 }

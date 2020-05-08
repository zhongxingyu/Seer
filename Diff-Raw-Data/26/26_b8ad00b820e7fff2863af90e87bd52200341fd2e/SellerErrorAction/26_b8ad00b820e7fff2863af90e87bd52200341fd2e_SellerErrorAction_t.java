 package com.omartech.tdg.action.seller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 @RequestMapping("/seller/error")
 public class SellerErrorAction {
 
	@RequestMapping("/productHasNoChildren")
 	public String productNoChildren(){
 		return "/seller/error/productNoChildren";
 	}
 
 }

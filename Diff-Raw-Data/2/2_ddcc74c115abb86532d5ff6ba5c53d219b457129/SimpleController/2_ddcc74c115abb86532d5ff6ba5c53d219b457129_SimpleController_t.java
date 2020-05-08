 package com.ronenn.springmvc.ldap.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 public class SimpleController {
 
 	@RequestMapping("/simple")
 	public @ResponseBody String simple() {
 		return "Hello world!";
 	}
 
     @RequestMapping("/model")
     public String prepare(Model model) {
         model.addAttribute("foo", "bar");
         model.addAttribute("fruit", "apple");
        return "redirect:/mymodel";
     }
 
 }

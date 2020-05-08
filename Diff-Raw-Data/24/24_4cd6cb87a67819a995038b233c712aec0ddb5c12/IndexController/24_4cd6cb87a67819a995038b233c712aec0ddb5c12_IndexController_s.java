 package com.cqlybest.admin.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.cqlybest.common.service.OptionService;
 
 @Controller
 public class IndexController {
 
   @Autowired
   private OptionService optionService;
 
   @RequestMapping("/index.do")
   public String index() {
     return "login";
   }
 
   @RequestMapping("/home.do")
   public String home(Model model) {
     model.addAttribute("Options", optionService.getOptions());
     return "index";
   }
 
 }

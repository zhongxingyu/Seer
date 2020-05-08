 package com.potatorental.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 @RequestMapping("/")
 public class HomeController {
 
     @RequestMapping(method = RequestMethod.GET)
     public String printWelcome(ModelMap model) {
        return "home";
     }
 
     /*@RequestMapping(value = "signup", method = RequestMethod.POST)
     public String signUp(@RequestParam(value = "hello")) {
         return "hello";
     }*/
 }

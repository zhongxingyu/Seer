 package com.gurumayoo.jaffna;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 
 @Controller
 @RequestMapping("/movie")
 public class MovieController {
 
     public String getMovie(@PathVariable String name, ModelMap model){
         model.addAttribute("movie",name);
         return "list";
 
     }
 
 }

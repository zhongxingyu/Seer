 package mspace.controller;
 
 import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 
 @Controller
 public class HelloController {
 
     @RequestMapping("/hello.html")
    public String handleRequest(Model model) {
 
        model.addAttribute("message", "HELLO!!!");
        return "hello";
     }
 }

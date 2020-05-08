 package mspace.controller;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
 
 
 @Controller
 public class HelloController {
 
     @RequestMapping("/hello.html")
    public ModelAndView handleRequest() {
 
        ModelAndView modelAndView = new ModelAndView("hello");
        modelAndView.addObject("message", "HELLO!!!");
        return modelAndView;
     }
 }

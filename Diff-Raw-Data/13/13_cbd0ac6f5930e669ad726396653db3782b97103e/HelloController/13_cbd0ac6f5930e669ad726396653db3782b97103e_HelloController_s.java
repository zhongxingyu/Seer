 package com.github.mazak.controllers;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 class HelloController {
     @RequestMapping(value="/help")
     Map help() {
         Map attrs = new HashMap();
         attrs.put("abc", "hereyea");
         return attrs;
     }
     @RequestMapping(value="/SpringPage")
    ModelAndView SpringPage() {
        Map attrs = new HashMap();
        attrs.put("abc", "babye");
        ModelAndView mav = new ModelAndView("com.github.mazak.SpringPage");
		mav.addObject("attrs", attrs);
        return mav;
     }
 }

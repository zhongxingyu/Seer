 package com.github.mazak.controllers;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;

import com.github.mazak.util.WicketInternalResourceView;
 
 @Controller
 class HelloController {
     @RequestMapping(value="/help")
     Map help() {
         Map attrs = new HashMap();
         attrs.put("abc", "hereyea");
         return attrs;
     }
     @RequestMapping(value="/SpringPage")
    View SpringPage() {
        return new WicketInternalResourceView("/wicket/bookmarkable/com.github.mazak.SpringPage");
     }
 }

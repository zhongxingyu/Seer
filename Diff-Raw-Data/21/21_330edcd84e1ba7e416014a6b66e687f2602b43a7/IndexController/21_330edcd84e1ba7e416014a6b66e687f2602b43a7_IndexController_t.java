 package com.googlecode.coss.web.home;
 
 import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 
@Controller
 public class IndexController {
 
    @RequestMapping("/index.htm")
    public ModelMap get() {
        ModelMap m = new ModelMap();
        m.addAttribute("msg", "Access Success");
        return m;
     }
 }

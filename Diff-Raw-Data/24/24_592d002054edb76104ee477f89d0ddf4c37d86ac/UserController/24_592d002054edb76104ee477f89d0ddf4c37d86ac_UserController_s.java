 package org.kiwi.springmvc;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import java.util.List;
 
 import static java.util.Arrays.asList;
 
 @Controller
 @RequestMapping("/user")
 public class UserController {
     @RequestMapping(value = "/{name}", method = RequestMethod.GET)
     public ModelAndView helloInPath(@PathVariable String name) {
         return new ModelAndView("user/show", "name", name);
     }
 
     @RequestMapping(method = RequestMethod.GET)
     public ModelAndView helloInRequest(@RequestParam("name") String name) {
         return new ModelAndView("user/show", "name", name);
     }
 
     @RequestMapping(value = "/index", method = RequestMethod.GET)
     public ModelMap getUsers() {
         return new ModelMap("users", asList("lucy", "jack", "kiwi"));
     }
 }

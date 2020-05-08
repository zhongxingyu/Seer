 package com.jsoft.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import com.jsoft.domain.entities.User;
 import com.jsoft.services.UserService;
 
 import java.util.Iterator;
 
 /**
  * Made by aurbrsz / 12/27/11 - 20:05
  */
 @Controller
 public class UserController {
 
   @Autowired
   private UserService userService;
 
  @RequestMapping(method = RequestMethod.GET, value = "/user/{pseudo}", headers="Accept=application/xml, application/json")
   public @ResponseBody User getUserByPseudo(@PathVariable String pseudo) {
     return userService.findUserByPseudo(pseudo);
   }
 
   @RequestMapping(method = RequestMethod.GET, value = "/welcome", headers = "Accept=application/xml, application/json")
   public String printWelcome(ModelMap model) {
     model.addAttribute("message", "Spring 3 MVC Hello World");
     return "hello";
   }
 
 }

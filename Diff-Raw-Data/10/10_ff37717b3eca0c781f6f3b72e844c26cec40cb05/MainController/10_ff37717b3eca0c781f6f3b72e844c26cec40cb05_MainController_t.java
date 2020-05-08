 package org.eduproject.web.controller;
 
 import org.eduproject.web.enumdata.Role;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.*;
 import org.eduproject.web.pojo.UserEntity;
 import org.eduproject.web.service.LoginService;
 import org.eduproject.web.service.UserDataTools;
 
 import java.security.Principal;
 
 @Controller
 public class MainController {
    @Autowired
     private UserDataTools userDataTools;
 
     @RequestMapping(value = "/", method = RequestMethod.GET)
     public String printWelcome(ModelMap model, Principal principal) {
         if (principal != null) {
             return new LoginService().login(userDataTools.findUserByName(principal.getName()), model);
         } else {
             return "login";
         }
     }
 
     @RequestMapping(value = "/login", method = RequestMethod.GET)
     public String login(ModelMap model) {
         return "login";
     }
 
     @RequestMapping(value = "/loginfailed", method = RequestMethod.GET)
     public String loginerror(ModelMap model) {
         model.addAttribute("error", "You are not registered");
         return "login";
     }
 
     @RequestMapping(value = "/logout", method = RequestMethod.GET)
     public String logout(ModelMap model) {
         return "login";
     }
 
     @RequestMapping(value = "/accessDenied", method = RequestMethod.GET)
     public String accessDenied(ModelMap model) {
         return "error_403";
     }
 
     @RequestMapping(value = "/register", method = RequestMethod.GET)
     public String register(ModelMap model) {
         model.addAttribute("personAttribute", new UserEntity());
         model.addAttribute("roles", Role.values());
         return "register";
     }
 
     @RequestMapping(value = "/register_new", method = RequestMethod.POST)
     public String Add(@ModelAttribute("personAttribute") UserEntity userEntity) {
         userDataTools.addUser(userEntity);
         return "redirect:/";
     }
 }

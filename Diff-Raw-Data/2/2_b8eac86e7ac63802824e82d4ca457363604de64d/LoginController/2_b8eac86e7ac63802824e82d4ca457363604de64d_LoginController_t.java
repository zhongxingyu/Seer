 package com.thoughtworks.controllers;
 
 import com.thoughtworks.models.Admin;
 import com.thoughtworks.services.LoginService;
 import lombok.NoArgsConstructor;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.SessionAttributes;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.Map;
 
 @NoArgsConstructor
 @Controller
 @SessionAttributes(types = Admin.class)
 public class LoginController {
 
   @Autowired
   LoginService service;
 
   public LoginController(LoginService service) {
     this.service = service;
   }
 
   @RequestMapping(value = "/login", method = RequestMethod.GET)
   public String login_request(Map<String, Object> model) {
     Admin login = new Admin();
     model.put("login", login);
     return "admin/login";
   }
 
   @RequestMapping(value = "/login", method = RequestMethod.POST)
   public String login(@RequestParam("employeeId") int employeeId, HttpServletRequest request) {
     boolean isAdmin = (service.login(employeeId));
     request.getSession().setAttribute("isAdmin", isAdmin);
     return "redirect:/";
   }
 
   @RequestMapping(value = "/logout", method = RequestMethod.GET)
   public String logout(HttpServletRequest request) {
    request.getSession().setAttribute("isAdmin", null);
     return "redirect:/";
   }
 }
 
 

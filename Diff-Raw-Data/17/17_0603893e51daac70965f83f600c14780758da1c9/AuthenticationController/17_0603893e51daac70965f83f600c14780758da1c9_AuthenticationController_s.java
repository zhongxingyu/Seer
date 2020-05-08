 package org.sukrupa.app.authentication;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 
 import javax.servlet.http.HttpServletRequest;
 
 
 @Controller
 @RequestMapping("/authentication")
 public class AuthenticationController {
 
     @RequestMapping(value = "login")
    public String create() {

         return "/authentication/login";
     }
 
     @RequestMapping(value = "logout")
     public String logout(HttpServletRequest request) {
         request.getSession().invalidate();
         return "/authentication/logout";
     }
 
 }

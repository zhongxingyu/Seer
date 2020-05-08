 package org.sukrupa.app.authentication;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
 
 
 import javax.servlet.http.HttpServletRequest;
import java.util.Map;
 
 
 @Controller
 @RequestMapping("/authentication")
 public class AuthenticationController {
 
     @RequestMapping(value = "login")
    public String create(@RequestParam("success") boolean success, Map<String, Object> model) {
        model.put("success", success);
         return "/authentication/login";
     }
 
     @RequestMapping(value = "logout")
     public String logout(HttpServletRequest request) {
         request.getSession().invalidate();
         return "/authentication/logout";
     }
 
 }

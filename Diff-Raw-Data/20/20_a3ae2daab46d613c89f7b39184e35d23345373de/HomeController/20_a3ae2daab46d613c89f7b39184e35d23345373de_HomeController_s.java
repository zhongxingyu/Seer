 package org.knetwork.webapp;
 
 import java.net.MalformedURLException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 @Controller
 @RequestMapping("/")
 public class HomeController {
 
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     @RequestMapping(method = RequestMethod.GET)
     public String display(final HttpSession session, final HttpServletRequest request, final Model model) throws MalformedURLException {
        return "logged-in";
     }
     
     @RequestMapping("logout")
     public String logout(HttpSession session) {
         session.removeAttribute("accessToken");
         return "redirect:/";
     }
 
 }

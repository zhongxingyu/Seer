 package web.controller;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.security.authentication.AuthenticationManager;
 import org.springframework.security.authentication.BadCredentialsException;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.web.context.SecurityContextRepository;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import web.service.UserService;
 
 /**
  * @author Romain Foncier <ro.foncier at gmail.com>
  */
 @Controller
 public class AuthAjaxController extends HttpServlet {
 
     @Autowired
     @Qualifier("authenticationManager")
     AuthenticationManager authenticationManager;
     @Autowired
     SecurityContextRepository repository;
     @Autowired
     private UserService userService;
 
    /*
     @RequestMapping(method = RequestMethod.GET)
     public String ajaxLogin() {
         return "ajax-login";
     }
    */
 
    @RequestMapping(value = "/ajax-login", method = RequestMethod.POST)
     public @ResponseBody
     String ajaxLoginProcess(@RequestParam("auth_username") String username,
             @RequestParam("auth_pwd") String password,
             HttpServletRequest request, HttpServletResponse response, Model model) {

         UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(username, password);
         try {
            //Authentication auth = authenticationManager.authenticate(token);
            //SecurityContextHolder.getContext().setAuthentication(auth);
            //repository.saveContext(SecurityContextHolder.getContext(), request, response);
 
             // Update last connection
             userService.connected(userService.findByUsername(username));
             return "{\"status\": \"success\"}";
         } catch (BadCredentialsException bce) {
             return "{\"status\": \"success\"}";
         }
     }
 }

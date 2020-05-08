 package org.alessiodm.ringer.web.api.v1.controller;
 
 import java.io.IOException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.alessiodm.ringer.domain.User;
 import org.alessiodm.ringer.util.RingerAPIException;
 import org.alessiodm.ringer.web.api.v1.auth.AuthService;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.http.HttpHeaders;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.context.request.WebRequest;
 
 public abstract class BaseController {
     
     private final static Logger log = Logger.getLogger(BaseController.class);
     
     @Autowired
     private AuthService authService;
     
     @ExceptionHandler({DataAccessException.class, IOException.class})
     @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Error in accessing data")
     public void dataAccessExceptionHandler(RuntimeException ex, HttpServletResponse response) {
         log.warn("Got a " + ex.getClass().getSimpleName() + " : " + ex.getLocalizedMessage());
     }
     
     @ExceptionHandler(RingerAPIException.class)
     public ResponseEntity<Object> handleConflict(RuntimeException ex, WebRequest request) {
        return new ResponseEntity(ex.getLocalizedMessage(), new HttpHeaders(), HttpStatus.CONFLICT);
     }
  
     /**
      * Get the ID of the user the token belongs to.
      * 
      * @param request   HTTP request
      * @param model     Current model
      */
     @ModelAttribute    
     public void getUser(HttpServletRequest request, Model model){
         String token = request.getParameter("token");
         User user = authService.validateToken(token);
         model.addAttribute("token", token);
         model.addAttribute("user", user); 
     }
 }

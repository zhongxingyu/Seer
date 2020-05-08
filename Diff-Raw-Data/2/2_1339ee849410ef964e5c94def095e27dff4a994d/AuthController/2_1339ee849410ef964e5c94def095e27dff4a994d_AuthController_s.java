 package org.alessiodm.ringer.web.api.v1.controller;
 
 import org.alessiodm.ringer.web.api.v1.auth.AuthService;
 import org.alessiodm.ringer.web.api.v1.dto.AuthToken;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 /**
  * Controller for getting valid token. OAuth inspired.
  * 
  * @author alessio
  */
 @Controller
 public class AuthController extends BaseController {
     
     @Autowired
     private AuthService authService;
     
     /**
      * We do not verify here user credentials because this operation is 
      * demanded to the authorization service.
      * 
      * @param username User username
      * @param password User password
      * @return Authorization token
      */
    @RequestMapping(value = "/api/v1/auth/token", produces = {"application/json", "application/xml"})
     public @ResponseBody AuthToken getToken(@RequestParam(value = "username", required = true) String username,
                                             @RequestParam(value = "password", required = true) String password){
         String token = authService.createTokenForUser(username, password);
         return new AuthToken(token);
     }
     
     @RequestMapping(value = "/api/v1/secure/auth/invalidateToken", method = RequestMethod.GET)
     public @ResponseBody void invalidateToken(@ModelAttribute("token") String token){
         authService.retireToken(token);
     }
     
 }

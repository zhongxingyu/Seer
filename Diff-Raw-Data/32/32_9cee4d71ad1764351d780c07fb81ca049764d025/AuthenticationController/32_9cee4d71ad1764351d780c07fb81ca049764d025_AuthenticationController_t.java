 package wad.tukki.controllers;
 
import javax.validation.Valid;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.authentication.BadCredentialsException;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import wad.tukki.models.JSONMessage;
 import wad.tukki.models.JSONMessageCode;
 import wad.tukki.models.User;
 import wad.tukki.services.AuthenticationService;
 
 @Controller
 @RequestMapping("api")
 public class AuthenticationController extends JSONBaseController {
     
     @Autowired
     AuthenticationService authenticationService;
     
     @RequestMapping(method = RequestMethod.POST, value = "login", consumes = "application/json")
     @ResponseBody
    public JSONMessage login(@Valid @RequestBody User user) {
         
         authenticationService.authenticate(user);
         
         return new JSONMessage(JSONMessageCode.AUTHENTICATED, "Authenticated.");
     }
     
     @RequestMapping("logout")
     @ResponseBody
     public JSONMessage logout() {
         
         authenticationService.invalidate();
         
         return new JSONMessage(JSONMessageCode.OK, "Logged out.");
     }
     
     @ExceptionHandler(BadCredentialsException.class)
     @ResponseBody
     public JSONMessage handleBadCredentialsException(BadCredentialsException exception) {
         return new JSONMessage(JSONMessageCode.BAD_CREDENTIALS, "Wrong username or password.");
     }
 }

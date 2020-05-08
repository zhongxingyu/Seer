 package org.alessiodm.ringer.web.api.v1.controller;
 
import org.alessiodm.ringer.domain.User;
 import org.alessiodm.ringer.service.UserService;
 import org.alessiodm.ringer.web.api.v1.auth.AuthService;
 import org.alessiodm.ringer.web.api.v1.dto.AuthToken;
 import org.alessiodm.ringer.web.api.v1.dto.SimpleResult;
 import org.alessiodm.ringer.web.api.v1.dto.SimpleResult.ResultType;
 import org.alessiodm.ringer.web.api.v1.dto.UserCredentials;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 public class UserController extends BaseController {
     
     @Autowired
     private AuthService authService;
     
     @Autowired
     private UserService userService;
     
     @RequestMapping(value = "/api/v1/user/register", method = RequestMethod.POST, produces = {"application/json", "application/xml"})
     public @ResponseBody AuthToken register(@RequestBody UserCredentials u){
         userService.registerUser(u.getUsername(), u.getPassword());
         String token = authService.createTokenForUser(u.getUsername(), u.getPassword());
         return new AuthToken(token);
     }
     
    @RequestMapping(value = "/api/v1/secure/user/delete", method = RequestMethod.DELETE, produces = {"application/json", "application/xml"})
    public @ResponseBody SimpleResult destroy(@ModelAttribute("user") User user){
        int result = userService.deleteUser(user.getId());
         if (result != 1){
             return new SimpleResult(ResultType.ERROR, "Result code: " + result);
         }
         return new SimpleResult(ResultType.OKEY, "User deleted");
     }
 
 }

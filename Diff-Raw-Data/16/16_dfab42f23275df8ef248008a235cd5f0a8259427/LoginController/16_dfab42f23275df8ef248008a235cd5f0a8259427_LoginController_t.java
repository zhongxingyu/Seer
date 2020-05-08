 package com.bouncingdata.plfdemo.controller;
 
 import java.security.Principal;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.bouncingdata.plfdemo.datastore.pojo.dto.RegisterResult;
 import com.bouncingdata.plfdemo.datastore.pojo.model.User;
 import com.bouncingdata.plfdemo.datastore.pojo.model.UserActionLog;
 import com.bouncingdata.plfdemo.service.DatastoreService;
 import com.bouncingdata.plfdemo.util.Utils;
 
 @Controller
 public class LoginController {
   
   private Logger logger = LoggerFactory.getLogger(LoginController.class);
   
   @Autowired
   private DatastoreService datastoreService;
         
   @RequestMapping(value={"/new", "/test", "/profile"}, method = RequestMethod.GET)
   public String newVersion(ModelMap model, Principal principal) {
     return "test";
   }
   
   @RequestMapping(value="/create", method = RequestMethod.GET)
   public String openCreate() {
     return "create";
   }
   
   @RequestMapping(value="/help/python", method = RequestMethod.GET)
   public String openPyHelp() {
     return "help-python";
   }
   
   @RequestMapping(value="/help/r", method = RequestMethod.GET)
   public String openRHelp() {
     return "help-r";
   }
     
   @RequestMapping(value="/auth", method=RequestMethod.GET)
   public String gologin(ModelMap model) {
     model.addAttribute("mode", "login");
     return "redirect:/auth/login";
   }
   
   @RequestMapping(value="/auth/login", method=RequestMethod.GET)
   public String login(ModelMap model) {
     model.addAttribute("mode", "login");
 
     return "login";
   }
   
   @RequestMapping(value="/auth/failed", method=RequestMethod.GET)
   public String failed(ModelMap model) {
     model.addAttribute("mode", "login");
     model.addAttribute("error", "true");
     return "login";
   }
   
   @RequestMapping(value="/auth/logout", method = RequestMethod.GET)
   public String logout(ModelMap model) {
     model.addAttribute("mode", "login");
     return "login";
   }
   
   @RequestMapping(value="/auth/register", method = RequestMethod.GET)
   public String getRegisterPage(ModelMap model) {
     model.addAttribute("mode", "register");
     model.addAttribute("regResult", null);
     return "login";
   }
   
   @RequestMapping(value="/auth/register", method = RequestMethod.POST)
   public String register(@RequestParam(value="username", required=true) String username, 
       @RequestParam(value="password", required=true) String password, 
       @RequestParam(value="email", required=true) String email, 
       @RequestParam(value="firstName", required=false) String firstName, 
       @RequestParam(value="lastName", required=false) String lastName, ModelMap model) {
     
     RegisterResult result = new RegisterResult();
     result.setUsername(username);
     result.setEmail(email);
     model.addAttribute("mode", "register");
     
     // validate inputs
     boolean isValid = true;
     StringBuilder errMsg = new StringBuilder();
     if (username == null || username.length() < 4) {
       errMsg.append("Username must be at least 4 characters.");
       isValid = false;
     }
     
     if (password == null || password.length() < 4) {
       if (errMsg.length() > 0) errMsg.append("\n");
       errMsg.append("Password must be at least 4 characters.");
       isValid = false;
     }
     
     if (email == null || !Utils.validate(email)) {
       if (errMsg.length() > 0) errMsg.append("\n");
       errMsg.append("Your email address is invalid.");
       isValid = false;
     }
     
     if (datastoreService.findUserByUsername(username) != null) {
       if (errMsg.length() > 0) errMsg.append("\n");
       errMsg.append("Username already exists.");
       isValid = false;
     }
     
     if (datastoreService.findUserByEmail(email) != null) {
       if (errMsg.length() > 0) errMsg.append("\n");
       errMsg.append("Email address already exists.");
       isValid = false;
     }
     
     if (!isValid) {
       result.setMessage(errMsg.toString());
       result.setStatusCode(-1);     
       model.addAttribute("regResult", result);
       return "login";
     }
     
     // business logic to create account
     User user = new User();
     user.setUsername(username);
     user.setEmail(email);
     user.setPassword(password);
     user.setFirstName(firstName);
     user.setLastName(lastName);
     
     try {
    	 /*ObjectMapper logmapper = new ObjectMapper();
          String data = logmapper.writeValueAsString(new String[] {"5", username, email,password,firstName,lastName});		   	 
         datastoreService.logUserAction(user.getId(),UserActionLog.ActionCode.REGISTER,data);      */
        
       datastoreService.createUser(user);
       result.setStatusCode(0);
       result.setMessage("Successfully create user " + user.getUsername());
       model.addAttribute("regResult", result);
     } catch (Exception e) {
       if (logger.isDebugEnabled()) logger.debug("Failed to create new user " + username, e);
       result.setStatusCode(-2);
       result.setMessage("Failed to register new user. Internal server error.");
       model.addAttribute("regResult", result);
     }
     
     return "login";
   }
   
   @RequestMapping(value="/auth/resetpasswd", method = RequestMethod.POST)
   public String resetPassword(@RequestParam(value="user-email", required=true) String email, ModelMap model) {
     model.addAttribute("mode", "resetpasswd");
     
     // validate email
     
     // find email to determine user
     
     // send new password to email address
     
     
     return "login";
   }
 }

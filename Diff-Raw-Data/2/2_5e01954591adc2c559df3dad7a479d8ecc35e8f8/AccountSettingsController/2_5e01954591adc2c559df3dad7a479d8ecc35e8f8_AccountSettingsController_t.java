 package org.netvogue.server.webmvc.controllers;
 
 import org.netvogue.server.mandrill.util.EmailUtil;
 import org.netvogue.server.neo4japi.common.ResultStatus;
 import org.netvogue.server.neo4japi.common.USER_TYPE;
 import org.netvogue.server.neo4japi.domain.User;
 import org.netvogue.server.neo4japi.service.BoutiqueService;
 import org.netvogue.server.neo4japi.service.UserService;
 import org.netvogue.server.webmvc.common.PasswordGenerator;
 import org.netvogue.server.webmvc.converters.ImageURLsConverter;
 import org.netvogue.server.webmvc.domain.AccountInfo;
 import org.netvogue.server.webmvc.domain.EmailNotifications;
 import org.netvogue.server.webmvc.domain.JsonResponse;
 import org.netvogue.server.webmvc.security.NetvogueUserDetailsService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 @Controller
 public class AccountSettingsController {
 
   @Autowired
   NetvogueUserDetailsService userDetailsService;
 
   @Autowired
   ImageURLsConverter imageURLsConverter;
 
   @Autowired
   BoutiqueService boutiqueService;
 
   @Autowired
   UserService userService;
 
   @RequestMapping(value = "/getaccountdetails", method = RequestMethod.GET)
   public @ResponseBody
   AccountInfo getAccountDetails() throws Exception {
     User user = userDetailsService.getUserFromSession();
     System.out.println("get account settings: " + user.getUsername());
     AccountInfo response = new AccountInfo();
 
     response.setName(user.getName());
     response.setIsbrand(USER_TYPE.BRAND == user.getUserType() ? true : false);
     response.setProfilepic(imageURLsConverter.convert(user.getProfilePicLink(), user.getUsername()));
     response.setEmail(user.getEmail());
 
     EmailNotifications emailnotifications = new EmailNotifications();
     userService.getEmailNotifications(user);
 
     org.netvogue.server.neo4japi.domain.EmailNotifications notification = user.getEmailnotifications();
     if (null != notification) {
       emailnotifications.setNetworkrequest(notification.isNetworkRequestFlag());
       emailnotifications.setNewsletter(notification.isNewsletterFlag());
     }
 
     response.setEmailnotifications(emailnotifications);
 
     return response;
   }
 
   @RequestMapping(value = "/account/emailnotifications", method = RequestMethod.POST)
   public @ResponseBody
   JsonResponse updateEmailNotifications(@RequestBody
   EmailNotifications notifications) throws Exception {
     System.out.println("Update email notifications: ");
     JsonResponse response = new JsonResponse();
     User user = userDetailsService.getUserFromSession();
     boolean isUserChanged = false;
 
     org.netvogue.server.neo4japi.domain.EmailNotifications emailNotifications = user.getEmailnotifications();
     if (null == emailNotifications) {
       emailNotifications = new org.netvogue.server.neo4japi.domain.EmailNotifications();
       user.setEmailnotifications(emailNotifications);
       isUserChanged = true;
     }
 
     emailNotifications.setNetworkRequestFlag(notifications.isNetworkrequest());
 
     StringBuffer error = new StringBuffer();
     if (isUserChanged) {
       if (ResultStatus.SUCCESS == userService.SaveUser(user, error)) {
         response.setStatus(true);
       }
     } else {
       if (ResultStatus.SUCCESS == userService.SaveEmailNotifications(emailNotifications, error)) {
         response.setStatus(true);
       }
     }
     response.setError(error.toString());
     return response;
   }
 
   @RequestMapping(value = "/{userId}/unsubscribe", method = RequestMethod.GET)
   public String unsubscribeNotifications(@PathVariable
   String userId, @RequestParam
   String nid) throws Exception {
     String redirectPage = null;
     StringBuffer error = null;
     boolean isUserChanged = false;
 
     System.out.println("User Id in AccountSettingsController: " + userId + " nid: " + nid);
 
     User user = userDetailsService.getUserFromSession();
     if (null != user && userId.equals(user.getUserId())) {
       redirectPage = "Account_Settings";
     } else {
       user = userService.getUserByUserId(userId);
       // This page needs to be designed - Pavan
       if (null != user) {
         redirectPage = "Notification";
       } else {
         return "Error";
       }
     }
 
     userService.getEmailNotifications(user);
     org.netvogue.server.neo4japi.domain.EmailNotifications emailNotifications = user.getEmailnotifications();
     if (null == emailNotifications) {
       emailNotifications = new org.netvogue.server.neo4japi.domain.EmailNotifications();
       user.setEmailnotifications(emailNotifications);
       isUserChanged = true;
     }
     // this code has to be changed to use pid request parameter.
     // left it this way as there is only letter type which can be unsubscribed.
     emailNotifications.setNetworkRequestFlag(false);
     error = new StringBuffer();
 
     if (isUserChanged) {
       if (ResultStatus.FAILURE == userService.SaveUser(user, error)) {
         redirectPage = "Error";
       }
     } else {
       if (ResultStatus.FAILURE == userService.SaveEmailNotifications(emailNotifications, error)) {
         redirectPage = "Error";
       }
     }
     return redirectPage;
   }
 
   @RequestMapping(value = "/resetPassword", method = RequestMethod.POST)
  public String forgotPassowrd(@RequestParam("uEmail")
   String userName) {
     String password = null;
     String redirectPage = null;
     StringBuffer error = null;
     if (null != userName) {
       System.out.println("Entered");
       User user = boutiqueService.GetUser(userName);
       System.out.println(user);
       if (null == user) {
         redirectPage = "Error";
       } else {
         password = PasswordGenerator.generatePassword();
         System.out.println(password);
         user.setTemporaryPassword(password);
         error = new StringBuffer();
         if (ResultStatus.SUCCESS == userService.SaveUser(user, error)) {
           EmailUtil.sendPasswordEmail(user, password);
           redirectPage = "redirect:/Netvogue.html";
         } else {
           redirectPage = "Error";
         }
       }
     }
 
     return redirectPage;
   }
 
 }

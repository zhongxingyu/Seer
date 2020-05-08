 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.digt.web;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.rave.model.User;
 import org.apache.rave.portal.model.impl.UserImpl;
 import org.apache.rave.portal.service.PageService;
 import org.apache.rave.portal.service.UserService;
 import org.apache.rave.portal.web.controller.ProfileController;
import org.apache.rave.portal.web.model.UserForm;
 import org.apache.rave.portal.web.util.ModelKeys;
 import org.apache.rave.portal.web.util.ViewNames;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 /**
  *
  * @author wasa
  */
 @Controller
 public class ExtProfileController extends ProfileController {
 
     private final UserService userService;
     
     private static final Logger LOG = Logger.getLogger(ExtProfileController.class.getName());
     
     @Autowired
     public ExtProfileController(UserService userService, PageService pageService) {
         super(userService, pageService);
         this.userService = userService;
     }
     
    @RequestMapping(value="/person", method = RequestMethod.POST)
     public String updateProfile(ModelMap model,
                                 @RequestParam(required = false) String referringPageId,
                                 @ModelAttribute("updatedUser") UserImpl updatedUser) {
         
         User user = userService.getAuthenticatedUser();
         LOG.log(Level.FINE, "Updating {0} profile information", user.getUsername());
 
         user.setGivenName(updatedUser.getGivenName());
         user.setFamilyName(updatedUser.getFamilyName());
         user.setDisplayName(updatedUser.getDisplayName());
         user.setAboutMe(updatedUser.getAboutMe());
         user.setStatus(updatedUser.getStatus());
         user.setEmail(updatedUser.getEmail());
         user.setProperties(updatedUser.getProperties());
 
         userService.updateUserProfile(user);
 
         model.addAttribute(ModelKeys.USER_PROFILE, user);
     	model.addAttribute(ModelKeys.REFERRING_PAGE_ID, referringPageId);
 
         return ViewNames.REDIRECT + "app/person/" + user.getUsername();
     }
     
    @RequestMapping(value = {"/person/{username:.*}"}, method = RequestMethod.GET)
     @Override
     public String viewProfileByUsername(@PathVariable String username, ModelMap model, 
                 @RequestParam(required = false) String referringPageId, HttpServletResponse response) {
         
         return super.viewProfileByUsername(username, model, referringPageId, response);
     }
 
    @RequestMapping(value = {"/person/id/{userid:.*}"}, method = RequestMethod.GET)
     @Override
     public String viewProfile(@PathVariable String userid, ModelMap model, 
                  @RequestParam(required = false) String referringPageId, HttpServletResponse response) {
         
         return super.viewProfile(userid, model, referringPageId, response);
     }
 }

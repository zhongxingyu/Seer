 package com.exadel.borsch.web.controllers;
 
 import com.exadel.borsch.dao.AccessRight;
 import com.exadel.borsch.dao.User;
 import com.exadel.borsch.managers.ManagerFactory;
 import com.exadel.borsch.managers.UserManager;
 import com.exadel.borsch.web.users.UserUtils;
 import java.security.Principal;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 
 import java.util.*;
 
 import org.springframework.ui.Model;
 
 import javax.validation.Valid;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Size;
 
 /**
  *
  * @author Vlad
  */
 @Controller
 public class UsersController {
 
     private static UserCommand userCommand = new UserCommand();
     @Autowired
     private ManagerFactory managerFactory;
 
     @RequestMapping("/users")
     public String processPageRequest(Principal principal, Model model) {
         UserManager userManager = managerFactory.getUserManager();
         model.addAttribute("users", userManager.getAllUsers());
         return ViewURLs.USERS_PAGE;
     }
 
     @RequestMapping("/edit/user/{userId}")
     public String processEditPageRequest(@PathVariable String userId, ModelMap model, Principal principal) {
         UserManager userManager = managerFactory.getUserManager();
         User user = userManager.getUserById(UUID.fromString(userId));
        UserUtils.checkEditor(principal, user.getId());
 
         userCommand.mapUserToUserCommand(user);
         model.addAttribute("userCommand", userCommand);
         model.addAttribute("allRights", AccessRight.getAllRightsToString().toArray(new String[]{""}));
         return ViewURLs.USER_EDIT_PAGE;
     }
 
     @ResponseBody
     @RequestMapping("/edit/user/{userId}/edit")
     public EditStatus processUpdateUserRequest(@PathVariable String userId,
             @Valid UserCommand userCommand, BindingResult result, Principal principal) {
         EditStatus response = new EditStatus();
         UserManager userManager = managerFactory.getUserManager();
         User user = userManager.getUserById(UUID.fromString(userId));
        UserUtils.checkEditor(principal, user.getId());
 
 
         boolean hasError = result.hasFieldErrors("name");
         response.setAlertName(hasError);
 
         if (!hasError) {
             user.setName(userCommand.getName());
         }
 
         hasError = result.hasFieldErrors("locale");
 
         response.setAlertLocale(hasError);
 
         if (!hasError) {
             user.setLocale(new Locale(userCommand.getLocale()));
         }
 
         hasError = result.hasFieldErrors("rights");
 
         response.setAlertRights(hasError && !userCommand.isRightsNull());
 
         if (!hasError && !userCommand.isRightsNull()) {
             user.setStringAccessRights(Arrays.asList(userCommand.getRights()));
         }
 
         user.setNeedEmailNotification(userCommand.getNeedEmailNotification());
         if (!result.hasErrors()) {
             userManager.updateUser(user);
         }
 
         return response;
     }
 
     @RequestMapping(value = "/edit/user/{userId}/remove", method = RequestMethod.POST)
     public String processRemoveUserRequest(@PathVariable String userId, Principal principal) {
         UserUtils.hasRole(principal, AccessRight.ROLE_EDIT_PROFILE);
         UserManager userManager = managerFactory.getUserManager();
         userManager.deleteUserById(UUID.fromString(userId));
         return ViewURLs.USERS_PAGE;
     }
 
     public static class EditStatus {
 
         private boolean alertName = false;
         private boolean alertLocale = false;
         private boolean alertRights = false;
 
         public boolean getAlertName() {
             return alertName;
         }
 
         public void setAlertName(boolean alertName) {
             this.alertName = alertName;
         }
 
         public boolean getAlertLocale() {
             return alertLocale;
         }
 
         public void setAlertLocale(boolean alertLocale) {
             this.alertLocale = alertLocale;
         }
 
         public boolean getAlertRights() {
             return alertRights;
         }
 
         public void setAlertRights(boolean alertRights) {
             this.alertRights = alertRights;
         }
     }
 
     public static class UserCommand {
 
         @Size(min = 1, max = 20)
         private String name;
         @NotEmpty
         private String locale;
         @NotNull
         private String id;
         @NotEmpty
         private String[] rights;
         private boolean needEmailNotification;
 
         public boolean getNeedEmailNotification() {
             return needEmailNotification;
         }
 
         public boolean isRightsNull() {
             return rights == null;
         }
 
         public void setNeedEmailNotification(boolean needEmailNotification) {
             this.needEmailNotification = needEmailNotification;
         }
 
         public String getId() {
             return id;
         }
 
         public void setId(String id) {
             this.id = id;
         }
 
         public String getName() {
             return name;
         }
 
         public void setName(String name) {
             this.name = name;
         }
 
         public String getLocale() {
             return locale;
         }
 
         public void setLocale(String locale) {
             this.locale = locale;
         }
 
         public void setRights(String[] newRights) {
             rights = Arrays.copyOf(newRights, newRights.length);
         }
 
         public String[] getRights() {
             return Arrays.copyOf(rights, rights.length);
         }
 
         public void mapUserToUserCommand(User user) {
             this.name = user.getName();
             this.id = user.getId().toString();
             this.locale = user.getLocale().getLanguage();
             this.needEmailNotification = user.getNeedEmailNotification();
             this.rights = user.getStringAccessRights().toArray(new String[]{""});
         }
     }
 }

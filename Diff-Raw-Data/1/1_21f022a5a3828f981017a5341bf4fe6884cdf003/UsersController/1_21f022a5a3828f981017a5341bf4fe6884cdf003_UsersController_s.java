 package com.exadel.borsch.web.controllers;
 
 import com.exadel.borsch.dao.AccessRight;
 import com.exadel.borsch.dao.User;
 import com.exadel.borsch.managers.ManagerFactory;
 import com.exadel.borsch.managers.UserManager;
 import com.exadel.borsch.web.users.UserUtils;
 import java.security.Principal;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
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
  * @author Vlad
  */
 @Controller
 public class UsersController {
 
     private static UserCommand userCommand = new UserCommand();
     @Autowired
     private ManagerFactory managerFactory;
 
     @Secured("ROLE_EDIT_PROFILE")
     @RequestMapping("/users")
     public String processPageRequest(Principal principal, Model model) {
         UserManager userManager = managerFactory.getUserManager();
         model.addAttribute("users", userManager.getAllUsers());
         return ViewURLs.USERS_PAGE;
     }
 
     @Secured("ROLE_EDIT_MENU_SELF")
     @RequestMapping("/edit/user/{userId}")
     public String processEditPageRequest(@PathVariable String userId, ModelMap model, Principal principal) {
         UUID uuidUserId = UUID.fromString(userId);
         UserUtils.checkEditor(principal, uuidUserId);
         UserManager userManager = managerFactory.getUserManager();
         User user = userManager.getUserById(uuidUserId);
 
         userCommand.mapUserToUserCommand(user);
         model.addAttribute("userCommand", userCommand);
         model.addAttribute("allRights", AccessRight.getAllRightsToString().toArray(new String[]{""}));
         return ViewURLs.USER_EDIT_PAGE;
     }
 
     @Secured("ROLE_EDIT_MENU_SELF")
     @RequestMapping("/edit/user/{userId}/edit")
     public String processUpdateUserRequest(@PathVariable String userId, ModelMap model,
             @Valid UserCommand userCommand, BindingResult result, Principal principal) {
         UUID uuidUserId = UUID.fromString(userId);
         UserUtils.checkEditor(principal, uuidUserId);
         UserManager userManager = managerFactory.getUserManager();
         User user = userManager.getUserById(uuidUserId);
 
         boolean invalidForm = true;
 
         invalidForm &= result.hasFieldErrors("rights") && user.hasAccessRight(AccessRight.ROLE_EDIT_PROFILE);
         if (!result.hasFieldErrors("rights")
                 && !user.hasAccessRight(AccessRight.ROLE_EDIT_PROFILE)) {
             user.setStringAccessRights(Arrays.asList(userCommand.getRights()));
         }
 
         invalidForm &= result.hasFieldErrors("name");
         if (!result.hasFieldErrors("name")) {
             user.setName(userCommand.getName());
         }
 
         invalidForm &= result.hasFieldErrors("locale");
         if (!result.hasFieldErrors("locale")) {
             user.setLocale(new Locale(userCommand.getLocale()));
         }
 
         user.setNeedEmailNotification(userCommand.getNeedEmailNotification());
 
         if (!invalidForm) {
             userManager.updateUser(user);
         }
 
         userCommand.setId(userId);
         model.addAttribute(userCommand);
         return ViewURLs.USER_EDIT_PAGE;
     }
 
     @Secured("ROLE_EDIT_PROFILE")
     @RequestMapping(value = "/edit/user/{userId}/remove", method = RequestMethod.POST)
     public String processRemoveUserRequest(@PathVariable String userId, Principal principal) {
         UserUtils.hasRole(principal, AccessRight.ROLE_EDIT_PROFILE);
         UserManager userManager = managerFactory.getUserManager();
         userManager.deleteUserById(UUID.fromString(userId));
         return ViewURLs.USERS_PAGE;
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

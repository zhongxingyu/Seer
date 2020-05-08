 package com.bde.portal.gui;
 
 import java.util.*;
 
 import java.io.File;
 import java.io.Serializable;
 
 // import of the needed entities
 import com.bde.portal.domain.usermanagement.User;
 import com.bde.portal.domain.usermanagement.Mandant;
 import com.bde.portal.domain.accounting.HolidayCard;
 import com.bde.portal.domain.usermanagement.UserProfile;
 import com.bde.portal.domain.usermanagement.Role;
 import com.bde.portal.domain.usermanagement.UserSettings;
 
 import org.primefaces.context.RequestContext;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 
 // import of the services
 import com.bde.portal.service.usermanagement.UserService;
 import com.bde.portal.service.accounting.HolidayCardService;
 import com.bde.portal.service.usermanagement.UserProfileService;
 
 import org.primefaces.event.SelectEvent;
 
 // Spring specific Classes
 import org.springframework.context.annotation.Scope;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.log4j.Logger;
 
 import com.bde.portal.exceptions.*;
 import com.bde.portal.gui.util.BDEGuiUtil;
 import javax.annotation.PreDestroy;
 
 @Named("usersBean")
 @Scope("access")
 public class UsersBean implements Serializable {
 
     private static final Logger LOG = Logger.getLogger(UsersBean.class.getName());
     @Inject
     private UserService userService;
     @Inject
     private UserSessionBean userSessionBean;
     @Inject
     private HolidayCardService holidayCardService;
     @Inject
     private UserProfileService userProfileService;
     private Mandant mandant;
     private List<User> filteredUserList;
     private List<User> userList;
     private User selectedUser;
     private boolean isCreated = false;
     private String requiredSymbol;
     private String newPassword;
     private String repeatedPassword;
     private List<Role> roleSelectionItems;
     private List<Role> selectedRoles = new ArrayList<Role>();
 
     public void initView() {
 
         if (userList == null) {
             // zuerst den Mandanten aus der globalen ConversationBean auslesen
             mandant = userSessionBean.getMandant();
             // alle Benutzer zu dem aktuellen Mandanten holen
             userList = userService.getUsers(mandant);
             // die gefilterte Liste alle Benutzer zuweisen
             filteredUserList = userList;
             // als initiale Auswahl den ersten Benutzer in der Tabelle markieren
             selectedUser = filteredUserList.get(0);
         }
     }
     
     @PreDestroy
     public void isDestroyed() {
         LOG.info("The Bean for the users is destroyed......");
     }
 
     // reset of the passwords from the bean
     public void resetPasswords() {
 
         // load the user roles to the conversation 
         this.selectedRoles = userService.getRoles(selectedUser);
         // reset the password and the repeated Password
         newPassword = null;
         repeatedPassword = null;
     }
 
     public void createNewUser() {
         // create a new user 
         this.selectedUser = userService.createNew();
         // clear the displayed user roles
         selectedRoles.clear();
         // and mark the user as created
         this.isCreated = true;
     }
 
     public String save() {
         try {
             RequestContext reqContext = RequestContext.getCurrentInstance();
             // if one or more user already exist and the user is new created
             // display a faces message and do not perform any operations
             if ((isCreated == true) && (userService.getUser(selectedUser.getLogin()) != null)) {
                 FacesContext context = FacesContext.getCurrentInstance();
                 context.addMessage(null, BDEGuiUtil.getFacesMessage(context, FacesMessage.SEVERITY_ERROR, "UserExist_Error", "UserExist_Error_detail"));
                 reqContext.addCallbackParam("userExist", true);
                 LOG.error("user already exists...");
             } else {
                 reqContext.addCallbackParam("userExist", false);
                 if (isCreated == false) {
 
                     // update the password if both fields have same Password entered
                    if (newPassword != null && !newPassword.equals("") && repeatedPassword != null && !repeatedPassword.equals("")) {
                         if (newPassword.equals(repeatedPassword)) {
                             userService.updatePassword(selectedUser.getLogin(), newPassword);
                         }
                     }
                 } else {
                     selectedUser.setMandant(mandant);
                     UserSettings userSettings = new UserSettings();
                     userSettings.setPreferredTheme("bde");
                     userSettings.setLanguageAbbreviation("de");
                     userSettings.setAccountingCalendarRows(5);
                     userSettings.setColor("blue");
                     userSettings.setUser(selectedUser);
                     selectedUser.setUserSettings(userSettings);
                     selectedUser.setPassword(userService.encrypt(newPassword));
                 }
 
                 // assign the seleted roles to the user
                 selectedUser.setRoles(selectedRoles);
 
                 // save the user		
                 userService.save(selectedUser);
 
                 // check whether the user is created
                 if (isCreated == true) {
                     // password has to be updated for the new user
                     userService.updatePassword(selectedUser.getLogin(), newPassword);
                     // read the new user from the database
                     User user = userService.getUser(selectedUser.getLogin());
                     // create a default userProfile for the new user
                     UserProfile userProfile = userProfileService.createDefaultUserProfile(user);
                     // and save the new created userProfile
                     userService.saveUserProfile(userProfile);
                     // create a new HolidayCard for the new user
                     HolidayCard holidayCard = holidayCardService.createUserHolidayCard(user);
                     holidayCardService.save(holidayCard);
                     // add the new user to the list for the view
                     userList.add(user);
                     // the set the filtered-Userlist to the userList
                     filteredUserList = userList;
                     // reset the selected user to the first user of the filtered user-list
                     selectedUser = filteredUserList.get(0);
                     // create a user directory on the server
                     String relativeNewFileName = File.separator + "home" + File.separator + "jwiesner" + File.separator + "BDEPortal-webapp" + File.separator + "BDEPortalServerTestData" + File.separator + user.getLogin();
                     userService.createUserDirectory(relativeNewFileName);
                 }
 
                 // reset the indication for a new user creation
                 isCreated = false;
 
             }
         } catch (MultipleUserFoundException mef) {
             LOG.error("Failed to save the user, because multipleUser where found for the selected user...");
         }
         // no navigation to perform
         return null;
     }
 
     public List<Role> getRolesSelectionItems() {
 
         roleSelectionItems = userService.getRoles();
         // return all the posible roleSelection items
         return roleSelectionItems;
     }
 
     public String cancel() {
 
         // if a new user was created and cancel is pressed set the selected user to the first user of the list
         if (isCreated == true) {
             selectedUser = filteredUserList.get(0);
         }
         // reset the indication flag for the user creation
         isCreated = false;
         // clear all the selected roles
         selectedRoles.clear();
         // reset the password and the repeated Password
         newPassword = null;
         repeatedPassword = null;
 
         // no navigation to perform
         return null;
     }
 
     public String deleteUser() {
         // delete the selected user from the database
         userService.delete(selectedUser);
         // remove the user from the general user list of the view
         userList.remove(selectedUser);
         // assign the user list to the filtered user list
         filteredUserList = userList;
         // and set the current user to the first user of the filtered list
         selectedUser = filteredUserList.get(0);
         // no navigation to perform
         return null;
     }
 
     public String getRequiredSymbol() {
         if (isCreated == true) {
             requiredSymbol = "*";
             return requiredSymbol;
         } else {
             requiredSymbol = "";
             return requiredSymbol;
         }
     }
 
     public boolean getIsCreated() {
         return this.isCreated;
     }
 
     public List<User> getUserList() {
         return userList;
     }
 
     public void setUserList(List<User> userList) {
         this.userList = userList;
     }
 
     public String getNewPassword() {
         return this.newPassword;
     }
 
     public String getRepeatedPassword() {
         return this.repeatedPassword;
     }
 
     public void setNewPassword(String password) {
         this.newPassword = password;
     }
 
     public void setRepeatedPassword(String password) {
         this.repeatedPassword = password;
     }
 
     public Mandant getMandant() {
         return this.mandant;
     }
 
     public void setMandant(Mandant mandant) {
         this.mandant = mandant;
     }
 
     public List<User> getFilteredUserList() {
         return this.filteredUserList;
     }
 
     public void setFilteredUserList(List<User> filteredUserList) {
         this.filteredUserList = filteredUserList;
     }
 
     public void setSelectedUser(User selectedUser) {
         this.selectedUser = selectedUser;
     }
 
     public User getSelectedUser() {
         return this.selectedUser;
     }
 
     public void onRowSelect(SelectEvent event) {
         this.selectedUser = (User) event.getObject();
     }
 
     public List<Role> getSelectedRoles() {
         return selectedRoles;
     }
 
     public void setSelectedRoles(List<Role> selectedRoles) {
         this.selectedRoles = selectedRoles;
     }
 }

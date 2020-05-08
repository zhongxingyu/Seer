 package com.bde.portal.gui;
 
 import java.io.Serializable;
 
 // importieren der Entities
 import com.bde.portal.domain.usermanagement.User;
 import com.bde.portal.domain.usermanagement.UserSettings;
 
 // importieren der Service-Klassen
 import com.bde.portal.service.usermanagement.UserService;
 
 // Spring specific Classes
 import org.springframework.context.annotation.Scope;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.apache.log4j.Logger;
 
 import com.bde.portal.exceptions.*;
 import java.util.ArrayList;
 import java.util.List;
 import javax.faces.model.SelectItem;
 
 @Named("userSettingsBean")
 @Scope("access")
 public class UserSettingsBean implements Serializable {
 
     private static final Logger LOG = Logger.getLogger(UserSettingsBean.class.getName());
     @Inject
     private UserService userService;
     @Inject
     private UserSessionBean userSessionBean;
    
    private String newPassword;
    private String repeatedPassword;
     private String userColor;
     private int calendarRows;
     private int customerItems;
     private User user;
     // selection of the user color
     private List<SelectItem> userColorSelectionItems;
 
     public String loadUserSettings() {
 
         user = userSessionBean.getSelectedUser();
         newPassword = user.getPassword();
         repeatedPassword = user.getPassword();
         calendarRows = user.getUserSettings().getAccountingCalendarRows();
         userColor = user.getUserSettings().getColor();
         customerItems = user.getUserSettings().getCustomerCarouselRows();
         
         // no navigation to perform
         return null;
     }
 
         // SelectOneMenu for the project color selection
     public List<SelectItem> getUserColorSelectionItems() {
 
         // only create if not yet exist
         if (userColorSelectionItems == null) {
             // create the array list
             userColorSelectionItems = new ArrayList<SelectItem>();
             userColorSelectionItems.add(new SelectItem("green", "green"));
             userColorSelectionItems.add(new SelectItem("black", "black"));
             userColorSelectionItems.add(new SelectItem("blue", "blue"));
             userColorSelectionItems.add(new SelectItem("brown", "brown"));
             userColorSelectionItems.add(new SelectItem("red", "red"));
         }
 
         // return the project color selection items
         return userColorSelectionItems;
     }
     
     
     public String save() {
 
         // update the password of the user
         try {
             
             // update the password if it is not null
            if (newPassword != null && repeatedPassword != null){
                 userService.updatePassword(user.getLogin(), newPassword);
             }
             // update the userSettings
             user = userService.getUser(user.getLogin());
             UserSettings userSettings = user.getUserSettings();
             userSettings.setAccountingCalendarRows(calendarRows);
             userSettings.setColor(userColor);
             // save the user settings and update the session data
             userService.saveUserSettings(userSettings);
             userSessionBean.getSelectedUser().getUserSettings().setAccountingCalendarRows(calendarRows);
             userSessionBean.getSelectedUser().getUserSettings().setCustomerCarouselRows(customerItems);
             userSessionBean.getSelectedUser().getUserSettings().setColor(userColor);
         
         } catch (MultipleUserFoundException e) {
             LOG.error("The update of the user password not successfull...");
         }
 
         // no navigation to perform
         return null;
     }
 
     public String cancel(){
         // reset all the values
         newPassword = null;
         repeatedPassword = null;
         calendarRows = userSessionBean.getSelectedUser().getUserSettings().getAccountingCalendarRows();
         customerItems = userSessionBean.getSelectedUser().getUserSettings().getCustomerCarouselRows();
         userColor = userSessionBean.getSelectedUser().getUserSettings().getColor();;
         // and perform no navigation
         return null;
     }
     
     public String getNewPassword() {
         return newPassword;
     }
 
     public void setNewPassword(String newPassword) {
         this.newPassword = newPassword;
     }
 
     public String getRepeatedPassword() {
         return repeatedPassword;
     }
 
     public void setRepeatedPassword(String repeatedPassword) {
         this.repeatedPassword = repeatedPassword;
     }
 
     public int getCalendarRows() {
         return calendarRows;
     }
 
     public void setCalendarRows(int calendarRows) {
         this.calendarRows = calendarRows;
     }
 
     public String getUserColor() {
         return userColor;
     }
 
     public void setUserColor(String userColor) {
         this.userColor = userColor;
     }
 
     public int getCustomerItems() {
         return customerItems;
     }
 
     public void setCustomerItems(int customerItems) {
         this.customerItems = customerItems;
     }
 }

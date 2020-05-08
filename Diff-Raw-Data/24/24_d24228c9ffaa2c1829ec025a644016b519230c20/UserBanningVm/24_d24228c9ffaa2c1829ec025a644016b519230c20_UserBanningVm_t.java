 package org.jtalks.poulpe.web.controller.userbanning;
 
import java.util.Collections;
 import java.util.List;
 
 import javax.annotation.Nonnull;
 
 import org.jtalks.poulpe.model.entity.User;
 import org.jtalks.poulpe.service.UserService;
 import org.zkoss.bind.annotation.BindingParam;
 import org.zkoss.bind.annotation.Command;
 import org.zkoss.bind.annotation.NotifyChange;
 
 public class UserBanningVm {
 
     // Injected
     private UserService userService;
 
     /**
      * User selected in list of available users.
      */
     private User selectedUser;
 
     /**
      * Flag variable which indicates that window to edit ban properties should be shown.
      */
     private boolean editBanWindowOpened = false;
 
     public UserBanningVm(@Nonnull UserService userService) {
         this.userService = userService;
     }
 
     //-- Accessors ------------------------------
 
     public List<User> getAvailableUsers() {
         List<User> users = userService.getAll();
         users.removeAll(userService.getAllBannedUsers());
        return Collections.unmodifiableList(users);
     }
 
     public List<User> getBannedUsers() {
        return Collections.unmodifiableList(userService.getAllBannedUsers());
     }
 
     public User getSelectedUser() {
         return selectedUser;
     }
 
     public void setSelectedUser(User selectedUser) {
         this.selectedUser = selectedUser;
     }
 
     /**
      * @return the editBanWindowOpened
      */
     public boolean isEditBanWindowOpened() {
         return editBanWindowOpened;
     }
 
     /**
      * @param editBanWindowOpened the editBanWindowOpened to set
      */
     public void setEditBanWindowOpened(boolean editBanWindowOpened) {
         this.editBanWindowOpened = editBanWindowOpened;
     }
 
     //-- ZK bindings ----------------------------
 
     /**
      * Set banned state to selected user.
      */
     @Command
     @NotifyChange({ "editBanWindowOpened" })
    public void addBanToSelectedUser() {
         editBan(selectedUser);
     }
 
     /**
      * Revoke ban for specified user.
      * 
      * @param user the user to revoke for
      */
     @Command
    @NotifyChange({ "availableUsers", "bannedUsers" })
     public void revokeBan(@Nonnull @BindingParam("user") User user) {
        user.setBanReason(null);
        userService.updateUser(user);
     }
 
     /**
      * Edit ban properties for specified user.
      * 
      * @param user the user to edit for
      */
     @Command
    @NotifyChange({ "availableUsers", "bannedUsers" })
     public void editBan(@Nonnull @BindingParam("user") User user) {
         // now not implemented
 
         // here must be initialization actions, setting internal state, etc.
 
         // after all
         // editBanWindowOpened = true;
     }
 
     //-- Utility methods -------------------------
 
 }

 /*
 *	http://www.jrecruiter.org
 *
 *	Disclaimer of Warranty.
 *
 *	Unless required by applicable law or agreed to in writing, Licensor provides
 *	the Work (and each Contributor provides its Contributions) on an "AS IS" BASIS,
 *	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied,
 *	including, without limitation, any warranties or conditions of TITLE,
 *	NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE. You are
 *	solely responsible for determining the appropriateness of using or
 *	redistributing the Work and assume any risks associated with Your exercise of
 *	permissions under this License.
 *
 */
 package org.jrecruiter.web.actions.admin;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.struts2.convention.annotation.Result;
 import org.jasypt.digest.StringDigester;
 import org.jrecruiter.model.User;
 import org.jrecruiter.web.actions.BaseAction;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.context.SecurityContextHolder;
 
 /**
  * @author Gunnar Hillert
  * @version $Id:UserService.java 128 2007-07-27 03:55:54Z ghillert $
  */
 @Result(name="success", location="show-users", type="redirectAction")
 public class EditUserAction extends BaseAction {
 
     private User user;
     private String password;
     private String password2;
     private boolean changePassword;
     private @Autowired StringDigester stringDigester;
 
     private Long userId;
 
     /** serialVersionUID. */
     private static final long serialVersionUID = -3422780336408883930L;
     private final static Logger LOGGER = LoggerFactory.getLogger(EditUserAction.class);
 
     public void validateSave() {
 
         if (changePassword) {
            if (StringUtils.isBlank(this.password)) {
                super.addFieldError("password", "Please enter a password.");
             }
         }
     }
     /**
      * Save the user.
      * @return
      */
     public String save() {
 
         final User userFromDb = userService.getUser(this.user.getId());
 
         if (userFromDb == null) {
             throw new IllegalStateException(
                     "User with id " + user.getId()
                   + " not found but was expected to exist");
         }
 
         //Let's check whether a username with for a different user id
         //already exists in the system.
         final User duplicateUserFromDb = userService.getUser(user.getUsername());
 
         if (duplicateUserFromDb != null && !userFromDb.getId().equals(duplicateUserFromDb.getId())) {
             LOGGER.warn("Duplicate user name ("+ user.getUsername() +") for id "
                     + userFromDb.getId());
             addFieldError("username", getText("error.duplicateUsername"));
             return INPUT;
         }
 
         if (!StringUtils.isBlank(this.password)) {
             userFromDb.setPassword(this.stringDigester.digest(this.password));
         } else {
             addActionMessage("Your password has not been changed.");
         }
 
         userFromDb.setCompany(this.user.getCompany());
         userFromDb.setEmail(this.user.getEmail());
         userFromDb.setFax(this.user.getFax());
         userFromDb.setFirstName(this.user.getFirstName());
         userFromDb.setLastName(this.user.getLastName());
         userFromDb.setPhone(this.user.getPhone());
 
         userService.updateUser(userFromDb);
 
         addActionMessage(getText("class.EditUserAcion.success"));
 
         User securityContextUser =
             (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
 
         securityContextUser.setCompany(this.user.getCompany());
         securityContextUser.setEmail(this.user.getEmail());
         securityContextUser.setFax(this.user.getFax());
         securityContextUser.setFirstName(this.user.getFirstName());
         securityContextUser.setLastName(this.user.getLastName());
         securityContextUser.setPassword(this.user.getPassword());
         securityContextUser.setPhone(this.user.getPhone());
         securityContextUser.setUsername(this.user.getUsername());
 
         return SUCCESS;
     }
 
     /**
      * Initialize the edit user form with the registration information of the
      * currently logged in user.
      */
     public String execute() {
 
         if (this.userId != null) {
             this.user = userService.getUser(this.userId);
         } else {
             this.user = super.getLoggedInUser();
         }
         return INPUT;
     }
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     public Long getUserId() {
         return userId;
     }
 
     public void setUserId(Long userId) {
         this.userId = userId;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getPassword2() {
         return password2;
     }
 
     public void setPassword2(String password2) {
         this.password2 = password2;
     }
 
     public boolean isChangePassword() {
         return changePassword;
     }
 
     public void setChangePassword(boolean changePassword) {
         this.changePassword = changePassword;
     }
 
 
 }

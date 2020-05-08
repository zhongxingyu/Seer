 package org.synyx.minos.umt.web;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.util.Assert;
 import org.synyx.minos.core.domain.Role;
 import org.synyx.minos.core.domain.User;
 
 
 /**
  * Like {@link AccountUserForm} but with mapping for all {@link User} properties.
  * 
  * @author Oliver Gierke - gierke@synyx.de
  */
 public class UserForm {
 
     private Long id;
     private String username;
     private String firstname;
     private String lastname;
     private String emailAddress;
     private List<Role> roles;
     private boolean active = true;
 
     private String newPassword;
     private String repeatedPassword;
 
 
     /**
      * Creates an empty form for users.
      */
     protected UserForm() {
 
         this.roles = new ArrayList<Role>();
     }
 
 
     /**
      * Creates a new form and prepopulates it with the data from the given user.
      */
     public UserForm(User user) {
 
         Assert.notNull(user);
 
         id = user.getId();
         username = user.getUsername();
         firstname = user.getFirstname();
         lastname = user.getLastname();
         emailAddress = user.getEmailAddress();
         roles = user.getRoles();
        active = user.isActive();
     }
 
 
     /**
      * Returns, if it represents a new user or an already existing one.
      * 
      * @return
      */
     public boolean isNew() {
 
         return null == id;
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see com.synyx.minos.core.web.support.DomainObjectForm#getDomainObject()
      */
     public User getDomainObject() {
 
         return mapProperties(null);
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see com.synyx.minos.core.web.DomainObjectForm#getDomainObject(java.lang.Object )
      */
     public User mapProperties(User user) {
 
         if (null == user) {
             user = new User(username, emailAddress, null);
         }
 
         user.setId(id);
         user.setFirstname(firstname);
         user.setLastname(lastname);
 
         // only set the newPasswort if its not null or empty string
         // this is because the ui does cannot resend the password
         // and it should only be changed it the user gives a new password
         if (!StringUtils.isBlank(newPassword)) {
             user.setPassword(newPassword);
         }
 
         user.setEmailAddress(emailAddress);
         user.setUsername(username);
 
         user.getRoles().clear();
         for (Role role : roles) {
             user.addRole(role);
         }
 
         user.setActive(active);
 
         return user;
     }
 
 
     /**
      * @return the id
      */
     public Long getId() {
 
         return id;
     }
 
 
     /**
      * @param id the id to set
      */
     public void setId(Long id) {
 
         this.id = id;
     }
 
 
     /**
      * @return the username
      */
     public String getUsername() {
 
         return username;
     }
 
 
     /**
      * @param username the username to set
      */
     public void setUsername(String username) {
 
         this.username = username;
     }
 
 
     /**
      * @return the firstname
      */
     public String getFirstname() {
 
         return firstname;
     }
 
 
     /**
      * @param firstname the firstname to set
      */
     public void setFirstname(String firstname) {
 
         this.firstname = firstname;
     }
 
 
     /**
      * @return the lastname
      */
     public String getLastname() {
 
         return lastname;
     }
 
 
     /**
      * @param lastname the lastname to set
      */
     public void setLastname(String lastname) {
 
         this.lastname = lastname;
     }
 
 
     /**
      * @return the emailAddress
      */
     public String getEmailAddress() {
 
         return emailAddress;
     }
 
 
     /**
      * @param emailAddress the emailAddress to set
      */
     public void setEmailAddress(String emailAddress) {
 
         this.emailAddress = emailAddress;
     }
 
 
     /**
      * @return the roles
      */
     public List<Role> getRoles() {
 
         return roles;
     }
 
 
     /**
      * @param roles the roles to set
      */
     public void setRoles(List<Role> roles) {
 
         this.roles = null == roles ? new ArrayList<Role>() : roles;
     }
 
 
     /**
      * @return the password
      */
     public String getNewPassword() {
 
         return newPassword;
     }
 
 
     /**
      * @param password the password to set
      */
     public void setNewPassword(String password) {
 
         this.newPassword = password;
     }
 
 
     /**
      * @return the repeatedPassword
      */
     public String getRepeatedPassword() {
 
         return repeatedPassword;
     }
 
 
     /**
      * @param repeatedPassword the repeatedPassword to set
      */
     public void setRepeatedPassword(String repeatedPassword) {
 
         this.repeatedPassword = repeatedPassword;
     }
 
 
     /**
      * @return the active
      */
     public boolean isActive() {
 
         return active;
     }
 
 
     /**
      * @param active the active to set
      */
     public void setActive(boolean active) {
 
         this.active = active;
     }
 
 }

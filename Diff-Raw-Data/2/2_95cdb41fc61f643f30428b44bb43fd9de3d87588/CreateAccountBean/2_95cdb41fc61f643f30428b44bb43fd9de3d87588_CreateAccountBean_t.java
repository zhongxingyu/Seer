 package org.esgf.accounts;
 
 import esg.node.security.UserInfo;
 
 /**
  * Class used to input and validate the user information from the web interface.
  * This class setter/getter methods delegate to the corresponding methods of the embedded {@link UserInfo} object.
  * 
  * @author Luca Cinquini
  *
  */
 public class CreateAccountBean {
     
     private String password1;
     private String password2;
     private UserInfo user;
     
     /**
      * Honeypot field, must be left blank for form to be accepted.
      */
    private String blank = "";
     
     /**
      * Verification token, set by Javascript to a value stored on the server.
      */
     private String uuid = java.util.UUID.randomUUID().toString();;
     
     public CreateAccountBean() {}
     
     public CreateAccountBean(final UserInfo user) {
         this.user = user;
     }
     
     public UserInfo getUser() {
         return user;
     }
 
     public String getPassword1() {
         return password1;
     }
 
     public void setPassword1(String password1) {
         this.password1 = password1;
     }
 
     public String getPassword2() {
         return password2;
     }
 
     public void setPassword2(String password2) {
         this.password2 = password2;
     }
 
     public String getFirstName() { 
         return user.getFirstName(); 
     }
     public void setFirstName(final String firstName) {
        user.setFirstName(firstName);
     }
 
     public String getMiddleName() { 
         return user.getMiddleName(); 
     }
     public void setMiddleName(final String middleName) {
         user.setMiddleName(middleName);
     }
     
     public String getLastName() { 
         return user.getLastName(); 
     }
     public void setLastName(final String lastName) {
        user.setLastName(lastName);
     }
 
     public String getUserName() { 
         return user.getUserName();
     }
     public void setUserName(final String userName) {
         user.setUserName(userName);
     }
         
     public final String getEmail() { 
         return user.getEmail();
     }
     public final void setEmail(final String email) {
         user.setEmail(email);
     }
     
     public String getOrganization() { 
         return user.getOrganization(); 
     }
     public void setOrganization(final String organization) {
         user.setOrganization(organization);
     }
     
     public String getCity() { 
         return user.getCity(); 
     }
     public void setCity(final String city) {
         user.setCity(city);
     } 
 
     public String getState() { 
         return user.getState(); 
     }
     public void setState(final String state) {
         user.setState(state);
     } 
     
     public String getCountry() { 
         return user.getCountry(); 
     }
     public void setCountry(final String country) {
         user.setCountry(country);
     } 
     
     public String getOpenid() {
         return user.getOpenid();
     }
     
     public String getBlank() {
         return blank;
     }
 
     public void setBlank(String blank) {
         this.blank = blank;
     }
 
     public String getUuid() {
         return uuid;
     }
 
     public void setUuid(String uuid) {
         this.uuid = uuid;
     }
    
 }

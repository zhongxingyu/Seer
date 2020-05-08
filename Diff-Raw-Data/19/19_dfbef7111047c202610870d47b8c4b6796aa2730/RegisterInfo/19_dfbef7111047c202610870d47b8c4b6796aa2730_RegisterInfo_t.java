 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package monsterMashGroupProject;
 
 import databaseManagement.MyUser;
 import databaseManagement.PersistManager;
 import java.util.List;
 import java.util.regex.*;
 
 /**
  * Class contains methods relating to registering
  * new users.
  * @author jamesslater
  */
 public class RegisterInfo {
     
     private String regUsername;
     
     private String regPassword;
     
     private String regEmail;
     /**
      * Gets current username
      * @return String username.
      */
     public String getRegUsername() {
         return regUsername;
     }
     /**
      * sets current username.
      * @param regUsername 
      */
     public void setRegUsername(String regUsername) {
         this.regUsername = regUsername;
     }
     /**
      * gets current password.
      * @return String password
      */
     public String getRegPassword() {
         return regPassword;
     }
    /**
      * sets a new current password.
      * @param regPassword 
      */
     public void setRegPassword(String regPassword) {
         this.regPassword = regPassword;
     }
 
     /**
      * Gets current email.
      * @return String email.
      */
     public String getRegEmail() {
         return regEmail;
     }
     /**
      * sets current email.
      * @param regEmail 
      */
     public void setRegEmail(String regEmail) {
         this.regEmail = regEmail;
     }
         /**
      * adds current user to the system.
      * @return String page to redirect to.
      */
     public String addUser(){
         Login log = new Login();
         boolean success = false;
         String output = "login.jsp";
         
         
         //UNCOMMENT WHEN YOU WANT USER VALIDATION
         //slows things down and i don't know why.
         
         PersistManager pert = new PersistManager();
         pert.init();
         List<MyUser> curUsers = pert.searchUsers();
         
        for (int i = 0; i < curUsers.size(); i++){
            if (curUsers.get(i).getUsername().toUpperCase()
                    .equals(regUsername.toUpperCase())){
                success = false;
            } else {
                success = true;
            }
        }
        
         if (
         success && // IMPORTANT when above uncommented.
         Pattern.matches("^[\\w]{3,20}$", regUsername) &&
         Pattern.matches("^[\\w]{3,20}$", regPassword))
         //Pattern.matches("\\b[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,4}\\b", regEmail.toUpperCase())){
 //        Pattern.matches("^[\\w+]+@[\\w+]\\.\\w{2,4}$", regEmail.toUpperCase()))
                 {
             
             log.Register(regUsername, regPassword, regEmail);
             success = true;
        } else {
            success = false;
         }
         
         // This breaks stuff!!
         //UserEmail email = new UserEmail();
         //email.send("felix.farquharson@googlemail.com", "monster-mash-n06@teame4t.co.uk",
         //        "User Registration", "Sucessfully registered");
         
         if ( success == false ){
             output = "failedLogin.jsp";
         }
         return output;
     }
     
 }

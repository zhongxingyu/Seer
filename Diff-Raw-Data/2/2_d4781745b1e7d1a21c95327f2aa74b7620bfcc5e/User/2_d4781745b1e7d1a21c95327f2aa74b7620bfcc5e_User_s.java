 package BackEnd.UserSystem;
 
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.InvalidKeyException;
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 /**
  *
  * @author David Tersoff
  */
 public class User extends Participant {
     // private int UID;
 
     private String password;
     private boolean adminPrivilege;
     private boolean eventCreationPrivilege;
     final private char[] ILLEGAL_CHARACTERS = {'@', '/', '\\', ' '};
     
 
     public User() {
         super();
         password = new String();
     }
 
     /**
      * Constructor, creates a User object
      *
      * @param pword the desired password
      * @param pwordMatch the password entered a second time to verify it
      */
     public User(String firstName, String lastName, String emailAddress, String pword, String pwordMatch)
             throws PasswordMismatchError, IllegalCharacterException {
         super(firstName, lastName, emailAddress);
         setPassword(pword, pwordMatch);
         
 
     }
 
     /*
      public User(int uid, String firstName, String lastName, String emailAddress, String pword, String pwordMatch) throws PasswordMismatchError, IllegalCharacterException
      {
      super(uid, firstName, lastName, emailAddress);
      setPassword(pword, pwordMatch);
             
      }*/
     public User(int userID, User user) {
         super(userID, (Participant) user);
 
         password = user.getPassword();
 
         adminPrivilege = user.getAdminPrivilege();
         eventCreationPrivilege = user.getEventCreationPrivilege();
     }
 
     public User(int uid, String firstName, String lastName, String emailAddress, String pword) {
         super(uid, firstName, lastName, emailAddress);
         password = pword;
     }
 
     /**
      *
      * @param pword The new password
      * @param pwordMatch repeated password, for verification
      * @throws IllegalCharacterException throws exception if the password
      * contains illegal characters
      * @throws PasswordMismatchError throws exception if the passwords don't
      * match.
      */
     public void setPassword(String pword, String pwordMatch) throws
             IllegalCharacterException, PasswordMismatchError{
         if (checkCharacters(pword)) {
             if (verifyPassword(pword, pwordMatch)) {
                
             } else {
                 throw new PasswordMismatchError();
             }
         } else {
             throw new IllegalCharacterException("Password contains illegal characters");
         }
     }
 
     private boolean verifyPassword(String pword, String pwordMatch) {
         if (pword.equals(pwordMatch)) {
             return true;
         } else {
             return false;
         }
     }
 
     /**
      *
      * @return username
      */
     /**
      *
      * @return password
      */
     public String getPassword() {
         return password;
     }
 
     /**
      * Checks a String object such as a username or password to see whether or
      * not it contains any illegal characters.
      *
      * @param s The String to be checked
      * @return Returns false if the string contains illegal characters,
      * otherwise returns true
      */
     public boolean checkCharacters(String s) {
         boolean b = true;
         for (char ic : ILLEGAL_CHARACTERS) {
             for (int x = 0; x < s.length(); x++) {
                 if (ic == s.charAt(x)) {
                     b = false;
                 }
             }
         }
         return b;
     }
 
     /**
      *
      * @param b boolean value determining if the user has admin privileges
      */
     public void setAdminPrivilege(boolean b) {
         adminPrivilege = b;
     }
 
     /**
      *
      * @return the user's admin privileges.
      */
     public boolean getAdminPrivilege() {
         return adminPrivilege;
     }
 
     /**
      *
      * @param b boolean value determining if the user has event creation
      * privileges
      */
     public void setEventCreationPrivilege(boolean b) {
         eventCreationPrivilege = b;
     }
 
     /**
      *
      * @return the user's event creation privileges
      */
     public boolean getEventCreationPrivilege() {
         return eventCreationPrivilege;
     }
 
     public boolean equals(User user) {
         if (user == null) {
             return false;
         }
         String s = this.getEmailAddress();
         if (s.equals(user.getEmailAddress())) {
             return true;
         } else {
             return false;
         }
     }
 
     public String toString() {
 //        String output = "\n" + super.toString() +
 //                "\nPassword: " + password +
 //                "\nAdmin Privileges: " + adminPrivilege +
 //                "\nEvent Creation Privileges: " + eventCreationPrivilege;
 //        return output;
         return super.getFirstName() + " " + super.getLastName();
     }
 }

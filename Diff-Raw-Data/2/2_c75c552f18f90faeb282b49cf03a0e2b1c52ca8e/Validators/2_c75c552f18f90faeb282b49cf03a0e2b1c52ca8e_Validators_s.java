 
 package cz.vutbr.fit.gja.gjaddr.gui.util;
 
 /**
  * Class for user input validation.
  *
  * @author Bc. Radek Gajdusek <xgajdu07@stud.fit,vutbr.cz>
  */
 public class Validators {
 
   /**
    * Email adress regex pattern.
    */
   private static String emailPattern = "^([A-Za-z0-9_\\-\\.])+\\@([A-Za-z0-9_\\-\\.])+\\.([A-Za-z]{2,4})";
   
   /**
    * Url adress regex pattern.
    */  
   private static String urlPattern = "(^http(s{0,1})://)?[a-zA-Z0-9_/\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";
   
     /**
    * Phone number regex pattern.
    */  
  private static String phonePattern = "\\+?[0-9]+";
 
   /**
    * Checks whether the given email address is valid.
    *
    * @param email represents the email address.
    * @return true if the email is valid, false otherwise.
    */
   public static boolean isEmailValid(String email) {
     if (email == null) {
       return false;
     }
     
     if (email.isEmpty()) {
       return true;
     }
 
     return email.matches(emailPattern);
   }
 
   /**
    * Checks whether the given URL (website address) is valid.
    *
    * @param url represents the website address.
    * @return true if the email is valid, false otherwise.
    */
   public static boolean isUrlValid(String url) {
     if (url == null) {
       return false;
     }
     
     if (url.isEmpty()) {
       return true;
     }    
     
     return url.matches(urlPattern);
   }
   
   /**
    * Checks whether the given phone phone is valid.
    *
    * @param url represents the website address.
    * @return true if the email is valid, false otherwise.
    */
   public static boolean isPhoneNumberValid(String phone) {
     if (phone == null) {
       return false;
     }
     
     if (phone.isEmpty()) {
       return true;
     }      
     
     return phone.matches(phonePattern);
   }
 }

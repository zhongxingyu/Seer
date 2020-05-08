 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author Vincent
  */
 public class StringCheck {
 
     public final static String patternMail = "^([\\w-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([\\w-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
    public final static String patternUsername = "^[A-Za-z0-9]{4,12}$";
     public final static String[] patternUsernamePrefix = {"sp_", "up9cloud_"};
 
 //    public static void main(String[] args) {
 //        System.out.println(patternMail);
 //        System.out.println(patternUsername);
 //        System.out.println(patternUsernamePrefix);
 //    }
     public static boolean mailCheck(String mail) {
         boolean flag;
         if (mail == null) {
             flag = false;
         } else {
             Pattern pattern = Pattern.compile(patternMail);
             Matcher matcher = pattern.matcher(mail);
             flag = matcher.matches();
         }
         return flag;
     }
 
     public static boolean usernameCheck(String username) {
         boolean flag;
         if (username == null) {
             flag = false;
         } else {
             Pattern pattern = Pattern.compile(patternUsername);
             Matcher matcher = pattern.matcher(username);
             flag = matcher.matches();
         }
         return flag;
     }
 
     public static boolean usernamePrefixCheck(String prefix) {
         boolean flag;
         if (prefix == null) {
             flag = false;
         } else {
             flag = false;
             for (int i = 0; i < patternUsernamePrefix.length; i++) {
                 if (prefix.equals(patternUsernamePrefix[i])) {
                     flag = true;
                 }
             }
         }
         return flag;
     }
 
     public static boolean passwordCheck(String password) {
         boolean flag;
         if (password == null) {
             flag = false;
         } else if (4 > password.length()) {
             int length;
             flag = false;
        } else if (12 < password.length()) {
             flag = false;
         } else {
             flag = true;
         }
         return flag;
     }
 }

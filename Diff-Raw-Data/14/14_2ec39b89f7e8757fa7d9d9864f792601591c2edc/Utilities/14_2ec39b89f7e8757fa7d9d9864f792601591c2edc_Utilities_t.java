 package web.utils;
 
 import java.util.ArrayList;
 import java.util.List;
 import web.model.User;
 
 /**
  * @author Romain Foncier <ro.foncier at gmail.com>
  */
 
 public class Utilities {
 
     public static List<String> checkForm(User userData) {
         List<String> res = new ArrayList<String>();
        if ("".equals(userData.getUsername())) {
             res.add("username_error");
         }
        if ("".equals(userData.getName())) {
             res.add("name_error");
         }
        if ("".equals(userData.getFirstname())) {
             res.add("fname_error");
         }
        if ("".equals(userData.getEmail())) {
             res.add("email_error");
         }
        if ("".equals(userData.getPassword())) {
             res.add("pwd_error");
         }
         return res;
     }
 }

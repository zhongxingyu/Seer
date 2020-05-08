 package controllers;
 import models.*;
 import play.data.validation.*;
 import java.util.*;
 import play.mvc.*;
 
 public class AdminUsers extends SecureAdmin {
     
     public static void index() {
         List inactive = User.getInactiveUsers();
         render(inactive);
     }
     
     public static void activate(Long id) {
         notFoundIfNull(id);
         User user = User.findById(id);
         notFoundIfNull(user);
         user.activate();
         user.save();
        informSuccess();
         index();
     }
     
     public static void delete(Long id) {
         notFoundIfNull(id);
         User user = User.findById(id);
         notFoundIfNull(user);
         user.delete();
        informSuccess();
         index();
     }
 }

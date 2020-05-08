 package controllers;
 
 import models.User;
 
 /**
  *
  * @author Maxim Kolchin
  */
 public class Security extends Secure.Security {
 
     public static boolean authenticate(String username, String password) {
         return User.connect(username, password) != null;
     }
 
     static void onAuthenticated() {
        redirect("Application.dashboard");
     }
 
     static void onDisconnected(){
         redirect("Index.index");
     }
 
 }

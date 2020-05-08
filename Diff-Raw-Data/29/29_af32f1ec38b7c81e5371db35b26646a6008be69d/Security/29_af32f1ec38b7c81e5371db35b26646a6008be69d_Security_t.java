 package controllers;
 import models.*;
  
 public class Security extends Secure.Security {
     
     static boolean authenticate(String username, String password) {
        Users user = Users.find("byEmail", username).first();
         return user != null && user.password.equals(password);
     }    
     
 }

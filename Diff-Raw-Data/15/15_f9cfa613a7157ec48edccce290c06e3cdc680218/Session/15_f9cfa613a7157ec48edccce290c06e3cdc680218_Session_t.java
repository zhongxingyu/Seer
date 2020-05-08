 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.ece.superkids.ui.events;
 
 import com.ece.superkids.users.entities.User;
 
 /**
  *
  * @author baris
  */
 public class Session{
 
     User currentUser = null;
 
     private Session() {
         System.out.println("Session created");
     }
     
     private static class SessionHolder {
         public static final Session aSession = new Session();
     }
 
     /**
      * Get a session.
      */
     public static Session aSession() {
         return Session.SessionHolder.aSession;
     }
 
     /**
      * Login user.
      */
     public boolean login(User user) {
         if (currentUser == null) {
             currentUser = user;
             System.out.println("User logged in: " + user.getName());
             return true;
         } else {
             System.out.println("There is a user already logged in.");
             return false;
         }
     }
 
     /**
      * Logout user.
      */
     public boolean logout() {
         if (currentUser != null) {
             String tmpUser = currentUser.getName();
             currentUser = null;
             System.out.println("User logged out: " + tmpUser);
             return true;
         } else {
            System.out.println("No user logged in yet.");
             return false;
         }
     }
 
     /**
      * Is any user logged in?.
      */
     public boolean isUserLoggedIn() {
         if (currentUser == null) {
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Is specific user logged in?.
      */
     public boolean isUserLoggedIn(User user) {
         if (currentUser!=null&&currentUser.getName().equals(user.getName())){
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Get the user logged in.
      */
     public User getLoggedInUser() {
         return currentUser;
     }
 }

 package com.soebes.subversion.sapm;
 
 import java.util.ArrayList;
 
 /**
  * Convenience class for the grammar to handle a list of {@link User}s.
  *
  * @author Karl Heinz Marbaise
  *
  */
 public class Users {
 
     private ArrayList<User> usersList;
 
     public Users() {
         setUsersList(new ArrayList<User>());
     }
 
     public void setUsersList(ArrayList<User> usersList) {
         this.usersList = usersList;
     }
 
     public ArrayList<User> getUsersList() {
         return usersList;
     }
 
     /**
      * Add a single user to the list of users.
      * @param user The user which will be added.
      */
     public void add(User user) {
         getUsersList().add(user);
     }
 
     /**
      * Check to see if a given username is within
      * the list of users.
      * Checking will be done case sensitive.
      * @param userName The name of the user.
      * @return true if the user has been found false otherwise.
      */
     public boolean hasUser(String userName) {
         boolean result = false;
         for (User item : getUsersList()) {
             if (item.getName().equals(userName)) {
                 result = true;
             }
         }
         return result;
     }
 
     public User getUser(String userName) {
         User result = null;
         for (User item : getUsersList()) {
             if (item.getName().equals(userName)) {
                 result = item;
             }
         }
         return result;
     }
 }

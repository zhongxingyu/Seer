 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.gravical.bwell.models;
 
 import java.io.Serializable;
 
 /**
  *
  * @author User
  */
 public class User implements Serializable{
     private String username;
     private String first_name;
     private String last_name;
     private int user_id;
     private int role_id; //  1 == ADMIN  2 == Supervisor  3 == Intern  4 == Client   5 == Guest
     private String passwordAfterHash;
     
     public User(int userId, String userName, String firstName, String lastName, String passwordBeforeHash, int roleId) {
         this.user_id = userId;
         this.username = userName;
         this.first_name = firstName;
         this.last_name = lastName;
         // TODO: Implement password hashing
         this.passwordAfterHash = User.hashPassword(passwordBeforeHash);
         this.role_id = roleId;
     } ;
     
     public static String hashPassword(String passwordToHash) {
         
         return passwordToHash;
     }

     /**
      * @return the username
      */
     public String getUsername() {
         return username;
     }
 
     /**
      * @param username the username to set
      */
     public void setUsername(String username) {
         this.username = username;
     }
 
     /**
      * @return the first_name
      */
     public String getFirst_name() {
         return first_name;
     }
 
     /**
      * @param first_name the first_name to set
      */
     public void setFirst_name(String first_name) {
         this.first_name = first_name;
     }
 
     /**
      * @return the last_name
      */
     public String getLast_name() {
         return last_name;
     }
 
     /**
      * @param last_name the last_name to set
      */
     public void setLast_name(String last_name) {
         this.last_name = last_name;
     }
 
     /**
      * @return the user_id
      */
     public int getUser_id() {
         return user_id;
     }
 
     /**
      * @param user_id the user_id to set
      */
     public void setUser_id(int user_id) {
         this.user_id = user_id;
     }
     
     public String toString() {
         String returnValue;
         returnValue = "[" + getUser_id() + ";" + getUsername() + ";" + getFirst_name() + ";" + getLast_name();
         returnValue +=  ";" + this.getPasswordAfterHash() + ";" + this.getRole_id() +";]";
         return returnValue;
     }
 
     /**
      * @return the passwordAfterHash
      */
     public String getPasswordAfterHash() {
         return passwordAfterHash;
     }
 
     /**
      * @param passwordAfterHash the passwordAfterHash to set
      */
     public void setPasswordAfterHash(String passwordAfterHash) {
         this.passwordAfterHash = passwordAfterHash;
     }
 
     /**
      * @return the role_id
      */
     public int getRole_id() {
         return role_id;
     }
 
     /**
      * @param role_id the role_id to set
      */
     public void setRole_id(int role_id) {
         this.role_id = role_id;
     }
     
 }

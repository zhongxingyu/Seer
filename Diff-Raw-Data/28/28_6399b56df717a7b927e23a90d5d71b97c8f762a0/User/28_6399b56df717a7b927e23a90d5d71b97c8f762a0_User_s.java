 package org.walkingarchive.backend.model.security;
 
 public class User {
     private Integer id;
     private String name;
     private String password;
     private String email;
     
     public User(String name, String password, String email) {
         //TODO - check if this user already exists, encrypt password
         this.name = name;
         this.password = password;
         this.email = email;
     }
     
     public User() {}
     
     public String getName() {
         return name;
     }
     
     public String getEmail() {
         return email;
     }
     
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public boolean isPassword(String password) {
         boolean result = false;
         
         if (password == this.password) {
             result = true;
         }
         
         return result;
     }
 }

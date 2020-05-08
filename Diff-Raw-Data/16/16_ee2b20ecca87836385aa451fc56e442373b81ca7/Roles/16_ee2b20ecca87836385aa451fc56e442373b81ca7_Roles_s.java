 package com.socialcomputing.wps.server.persistence.hibernate;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "user_roles")
 public class Roles {
     
     int id;
 
     String username;
     
     String role;
 
     public Roles() {
         
     }
     
     @Id
     @Column(name = "id")
    @GeneratedValue(strategy=GenerationType.IDENTITY)
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     @Column(name = "username", nullable = false, length=255)
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
     
     @Column(name = "role", nullable = false, length=255)
     public String getRole() {
         return role;
     }
 
     public void setRole(String role) {
         this.role = role;
     }
 
 }

 package com.edify.model;
 
 import org.hibernate.validator.constraints.NotBlank;
 
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 
 /**
  * @author: <a href="https://github.com/jarias">jarias</a>
  */
 @Entity
 @Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = {"username"})})
 public class User {
     @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
     @NotBlank
     @NotNull
     private String username;
     @NotNull
     @NotBlank
     private String password;
     @NotNull
     @NotBlank
     private String firstName;
     @NotNull
     @NotBlank
     private String lastName;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 }

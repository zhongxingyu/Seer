 package com.lavida.service.entity;
 
 import javax.persistence.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Admin
  * Date: 01.08.13
  * Time: 17:04
  * To change this template use File | Settings | File Templates.
  */
 @Entity
 public class AuthorityJdo {
 
     @Id
     @GeneratedValue
     private int id;
 
     private String role;
 
     @ManyToOne
     @JoinColumn(name="user_id")
     private UserJdo user;
 
     public AuthorityJdo() {
     }
 
     public AuthorityJdo(String role) {
         this.role = role;
     }
 
     public AuthorityJdo(String role, UserJdo user) {
         this.role = role;
         this.user = user;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public String getRole() {
         return role;
     }
 
     public void setRole(String role) {
         role = role;
     }
 
     public UserJdo getUser() {
         return user;
     }
 
     public void setUser(UserJdo user) {
         this.user = user;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         AuthorityJdo that = (AuthorityJdo) o;
 
         if (id != that.id) return false;
         if (!role.equals(that.role)) return false;
         if (user != null ? !user.equals(that.user) : that.user != null) return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = id;
         result = 31 * result + role.hashCode();
         return result;
     }
 
     @Override
     public String toString() {
         return "AuthorityJdo{" +
                 "id=" + id +
                 ", Role='" + role + '\'' +
                 ", user=" + user.getId() +
                 '}';
     }
 }

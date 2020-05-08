 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.entity.user;
 
 import com.fasterxml.jackson.annotation.JsonView;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 import org.springframework.security.core.GrantedAuthority;
 
 /**
  *
  * @author katsu
  */
 @Entity
 @Table(name = "role1")
 public class Role implements GrantedAuthority{
     @Id
     @JsonView(Views.Public.class)
     private String authority;
    @JsonView(Views.Level1.class)
     @ManyToMany(mappedBy = "roles")
     private List<User> users;
 
     public List<User> getUsers() {
         return users;
     }
 
     public void setUsers(List<User> users) {
         this.users = users;
     }
 
     public String getAuthority() {
         return authority;
     }
 
     public void setAuthority(String authority) {
         this.authority = authority;
     }
     
     public void addUser(User user) {
         if (this.users == null) {
             this.setUsers(new ArrayList<User>());
         }
         if (!this.users.contains(user)) {
             this.users.add(user);
         }
         if (user.getRoles() == null) {
             user.setRoles(new ArrayList<Role>());
         }
         if (!user.getRoles().contains(this)) {
             user.getRoles().add(this);
         }
     }
 
 }

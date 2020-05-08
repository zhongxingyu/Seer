 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.entity.user;
 
 import com.fasterxml.jackson.annotation.JsonView;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 
 /**
  *
  * @author katsu
  */
 @Entity
 @Table(name = "groups1")
 public class Group implements Serializable {
     @Id
     @JsonView(Views.Public.class)
     @Column(name = "value1")
     private String value;
    @JsonView(Views.Level1.class)
     @ManyToMany(mappedBy = "groups")
     private List<User> users;
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
     public List<User> getUsers() {
         return users;
     }
 
     public void setUsers(List<User> users) {
         this.users = users;
     }
     
      public void addUser(User user) {
         if (this.users == null) {
             this.setUsers(new ArrayList<User>());
         }
         if (!this.users.contains(user)) {
             this.users.add(user);
         }
         if (user.getGroups() == null) {
             user.setGroups(new ArrayList<Group>());
         }
         if (!user.getGroups().contains(this)) {
             user.getGroups().add(this);
         }
 
     }
 }

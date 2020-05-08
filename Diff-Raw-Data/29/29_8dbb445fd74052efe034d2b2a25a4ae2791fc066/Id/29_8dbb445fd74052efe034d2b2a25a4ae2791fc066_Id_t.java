 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.cleia.entity.user;
 
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.fasterxml.jackson.annotation.JsonView;
 import java.io.Serializable;
 import java.util.ArrayList;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 
 /**
  *
  * @author katsu
  */
 @Entity
 public class Id implements Serializable {
     @javax.persistence.Id
     @JsonView(Views.Public.class)
     @GeneratedValue(strategy = GenerationType.AUTO)
     private long id;
     @JsonView(Views.Public.class)
     @Column(nullable = false,length = 1024,name = "value1")
     private String value;
     @JsonView(Views.Public.class)
     @ManyToOne(fetch = FetchType.EAGER,optional = false)
     private IdType type;
    @JsonView(Views.Level2.class)
     @ManyToOne(fetch = FetchType.LAZY)
     @JoinColumn(name = "user_id")
     private User user;
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public String getValue() {
         return value;
     }
 
     public void setValue(String value) {
         this.value = value;
     }
 
     public IdType getType() {
         return type;
     }
 
     public void setType(IdType type) {
         this.type = type;
     }
       public void addUser(User user) {
         if (this.user == null) {
             this.setUser(user);
         }
     }
 }

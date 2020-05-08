 package com.socialcomputing.wps.server.persistence.hibernate;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "accounts")
 public class Accounts {
 
     int id;
 
     String name;
 
     public Accounts() {
         
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
 
     @Column(name = "name", nullable = false, length=255)
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 }

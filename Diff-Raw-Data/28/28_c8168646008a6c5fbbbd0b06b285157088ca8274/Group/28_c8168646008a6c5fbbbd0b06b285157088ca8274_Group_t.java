 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.rwth.travelTogether.model;
 
 import java.lang.reflect.Array;
 import java.util.Date;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 /**
  *
  * @author marco
  */
 @Entity
 public class Group {
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     private Long id;
 
     private String name;
 
     @Temporal(TemporalType.DATE)
     private Date creationDate;
 
     private String founder;
 
     private Array[] members;
 
     private Array[] forumEntries;
 
     public Date getCreationDate() {
         return creationDate;
     }
 
     public void setCreationDate(Date creationDate) {
         this.creationDate = creationDate;
     }
 
     public Array[] getForumEntries() {
         return forumEntries;
     }
 
     public void setForumEntries(Array[] forumEntries) {
         this.forumEntries = forumEntries;
     }
 
     public String getFounder() {
         return founder;
     }
 
     public void setFounder(String founder) {
         this.founder = founder;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public Array[] getMembers() {
         return members;
     }
 
     public void setMembers(Array[] members) {
         this.members = members;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     
 
 }

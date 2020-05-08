 package com.thoughtworks.twu.persistence;
 
 import org.hibernate.annotations.GenericGenerator;
 
 import javax.persistence.*;
 
 @Entity
@Table( name = "favoritetimesheet" )
 public class FavoriteTimesheet {
 
     @Id
     @GeneratedValue(generator="id")
     @GenericGenerator(name="id", strategy = "increment")
     private int id;
 
     private String name;
     private String userId;
 
     public FavoriteTimesheet() {
     }
 
     public FavoriteTimesheet(String name) {
         this.name = name.trim();
         this.userId = "1234";
     }
 
     public String getName() {
         return name;
     }
 }

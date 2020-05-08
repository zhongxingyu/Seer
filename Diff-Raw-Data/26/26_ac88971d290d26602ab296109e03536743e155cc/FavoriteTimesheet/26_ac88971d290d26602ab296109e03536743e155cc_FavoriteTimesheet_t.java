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

    private String name = "Default";
     private String userId;
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setUserId(String userId) {
         this.userId = userId;
     }
 
    public String getName() {
        return name;
    }
 }

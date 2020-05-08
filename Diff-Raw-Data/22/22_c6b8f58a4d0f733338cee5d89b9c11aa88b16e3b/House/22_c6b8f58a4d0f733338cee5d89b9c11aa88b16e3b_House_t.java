 package com.house.model;
 
 import java.util.List;
 
 public class House {
     private String id;
 
     public House(String id, String name) {
         this.id = id;
         this.name = name;
     }
 
     private String name;
 
     private List<Door> door;
 
     public House() {
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public List<Door> getDoor() {
         return door;
     }
 
     public String getId() {
         return id;
     }
 
     public String getName() {
         return name;
     }
 }

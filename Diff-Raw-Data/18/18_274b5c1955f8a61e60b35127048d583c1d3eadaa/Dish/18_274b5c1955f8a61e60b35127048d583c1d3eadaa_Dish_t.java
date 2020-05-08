 package com.exadel.borsch.dao;
 
 import com.exadel.borsch.util.Encoder;
 import java.util.UUID;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 public class Dish {
     private UUID id = UUID.randomUUID();
     private String name;
     private String photoUrl;
     private Integer price;
     private String description;
     private String hashId;
     private Course course;
 
     public Dish() {
     }
     public Dish(String name, Integer price, String description) {
         this.name = name;
         this.price = price;
         this.description = description;
         this.updateHash();
     }
 
     public UUID getId() {
         return id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getPhotoUrl() {
         return photoUrl;
     }
 
     public void setPhotoUrl(String photoUrl) {
         this.photoUrl = photoUrl;
     }
 
     public int getPrice() {
         return price;
     }
 
     public void setPrice(int price) {
         this.price = price;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     private void updateHash() {
         hashId = Encoder.encodeWithMD5(name + description, price.toString());
     }
 
     public String getHashId() {
        updateHash();
         return hashId;
     }
 
     public void setCourse(Course course) {
         this.course = course;
     }
     public Course getCourse() {
         return this.course;
     }
 
     @Override
     public boolean equals(Object toCompare) {
         if (toCompare instanceof Dish) {
             return this.getHashId().equals(((Dish) toCompare).getHashId());
         } else {
             return false;
         }
     }
 
     @Override
     public int hashCode() {
         return new HashCodeBuilder(42, 7)
                 .append(name)
                 .append(price)
                 .append(description)
                 .toHashCode();
     }
 }

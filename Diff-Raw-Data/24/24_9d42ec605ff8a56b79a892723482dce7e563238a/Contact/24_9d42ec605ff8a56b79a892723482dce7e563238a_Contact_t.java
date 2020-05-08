 package edu.rit.taskers.model;
 
 import java.net.URL;
 
 import javax.persistence.*;
 
 @Entity
 @Table(name = "Contact")
 public class Contact {
 
 
     private int id;
     protected String name;
     protected String phone;
     protected String email;
     protected String role;
     protected String bio;
     protected String pictureURL;
 
     protected Integer spaceId;
    protected Integer userId;
 
     public Contact() {
 
     }
 
     public Contact(String name, String phone, String email, String role, String bio, String pictureURL) {
         this.name = name;
         this.phone = phone;
         this.email = email;
         this.role = role;
         this.bio = bio;
         this.pictureURL = pictureURL;
     }
     @Id
     @GeneratedValue
     @Column(name = "ContactID")
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     @Column(name = "Name")
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @Column(name = "Phone")
     public String getPhone() {
         return phone;
     }
 
     public void setPhone(String phone) {
         this.phone = phone;
     }
 
      @Column(name = "Email")
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
      @Column(name = "Role")
     public String getRole() {
         return role;
     }
 
     public void setRole(String role) {
         this.role = role;
     }
 
     @Column(name = "Bio")
     public String getBio() {
         return bio;
     }
 
     public void setBio(String bio) {
         this.bio = bio;
     }
 
     @Column(name = "PictureURL", nullable = true)
     public String getPictureURL() {
         return pictureURL;
     }
 
     public void setPictureURL(String pictureURL) {
         this.pictureURL = pictureURL;
     }
 
     @Column(name = "SpaceID", nullable = true)
     public Integer getSpaceId() {
         return spaceId;
     }
 
     public void setSpaceId(Integer spaceId) {
         this.spaceId = spaceId;
     }
 
    @Column(name = "UserID", nullable = true)
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
 
 }

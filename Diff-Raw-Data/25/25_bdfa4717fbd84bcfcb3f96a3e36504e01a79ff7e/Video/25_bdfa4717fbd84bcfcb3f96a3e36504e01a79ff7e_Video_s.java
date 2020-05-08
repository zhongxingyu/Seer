 package com.ilopezluna.entities;
 
 import org.apache.tapestry5.beaneditor.NonVisual;
 import org.apache.tapestry5.beaneditor.Validate;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import java.util.Date;
 
 @Entity
 public class Video {
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     @NonVisual
     private Long id;
 
     @Validate("required")
     private String url;
 
     private Date date;
 
     private String city;
 
     private String street;
 
     private String email;
 
     @NonVisual
     private String hash;
 
     @NonVisual
     private boolean validated;
 
     @NonVisual
     private Date timestamp;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getUrl() {
         return url;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
 
     public Date getDate() {
         return date;
     }
 
     public void setDate(Date date) {
         this.date = date;
     }
 
     public String getCity() {
         return city;
     }
 
     public void setCity(String city) {
         this.city = city;
     }
 
     public String getStreet() {
         return street;
     }
 
     public void setStreet(String street) {
         this.street = street;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getHash() {
         return hash;
     }
 
     public void setHash(String hash) {
         this.hash = hash;
     }
 
     public boolean isValidated() {
         return validated;
     }
 
     public void setValidated(boolean validated) {
         this.validated = validated;
     }
 
     public Date getTimestamp() {
         return timestamp;
     }
 
     public void setTimestamp(Date timestamp) {
         this.timestamp = timestamp;
     }
 
     @Override
     public String toString() {
        return "id: "
                + id + " url: "
                + url + " date: "
                + date + " city: "
                + city + " street: "
                + street + " email: "
                + email + " timestamp: "
                + timestamp
                + "Validation URL: http://localhost:8080/youtube-evidence/rest/validate/video/" + hash;
     }
 }

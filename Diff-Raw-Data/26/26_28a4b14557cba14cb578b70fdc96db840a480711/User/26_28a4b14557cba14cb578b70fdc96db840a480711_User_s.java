 package com.yrek.rideapp.facebook;
 
 import java.io.Serializable;
 
 import org.codehaus.jackson.annotate.JsonIgnoreProperties;
 
 @JsonIgnoreProperties(ignoreUnknown=true)
 public class User implements Serializable {
     private static final long serialVersionUID = 0L;
 
     private String id;
     private String name;
     private String firstName;
     private String lastName;
     private String link;
     private String gender;
     private Integer timezone;
     private String locale;
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getFirstName() {
         return firstName;
     }
 
     public void setFirstName(String firstName) {
         this.firstName = firstName;
     }
 
     public String getLastName() {
         return lastName;
     }
 
     public void setLastName(String lastName) {
         this.lastName = lastName;
     }
 
     public String getLink() {
         return link;
     }
 
     public void setLink(String link) {
         this.link = link;
     }
 
     public String getGender() {
         return gender;
     }
 
     public void setGender(String gender) {
         this.gender = gender;
     }
 
     public Integer getTimezone() {
         return timezone;
     }
 
     public void setTimezone(Integer timezone) {
         this.timezone = timezone;
     }
 
     public String getLocale() {
         return locale;
     }
 
     public void setLocale(String locale) {
         this.locale = locale;
     }
 }

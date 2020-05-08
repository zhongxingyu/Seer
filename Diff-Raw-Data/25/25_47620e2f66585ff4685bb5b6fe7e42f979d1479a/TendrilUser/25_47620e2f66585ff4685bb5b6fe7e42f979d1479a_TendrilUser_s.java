 package com.tendril.cleanweb.domain.tendril;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 /**
  */
 @XmlRootElement
 public class TendrilUser {
 
     private String id;
     private String authorId;
     private String emailAddress;
     private boolean expert;
     private String firstName;
     private String lastName;
    private String username;
 
 
 
     public TendrilUser() {
     }
 
    public TendrilUser(String id, String authorId, String emailAddress, boolean expert, String firstName, String lastName, String username) {
         this.id = id;
         this.authorId = authorId;
         this.emailAddress = emailAddress;
         this.expert = expert;
         this.firstName = firstName;
         this.lastName = lastName;
        this.username = username;
     }
 
     public String getId() {
         return id;
     }
 
     public void setId(String id) {
         this.id = id;
     }
 
     public String getAuthorId() {
         return authorId;
     }
 
     public void setAuthorId(String authorId) {
         this.authorId = authorId;
     }
 
     public String getEmailAddress() {
         return emailAddress;
     }
 
     public void setEmailAddress(String emailAddress) {
         this.emailAddress = emailAddress;
     }
 
     public boolean isExpert() {
         return expert;
     }
 
     public void setExpert(boolean expert) {
         this.expert = expert;
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
 
    public String getUsername() {
        return username;
     }
 
    public void setUsername(String username) {
        this.username = username;
     }
 }

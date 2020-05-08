 /* Copyright 2004, Aetrion LLC.  All Rights Reserved. */
 
 package com.aetrion.flickr.uploader;
 
 import java.util.Collection;
 
 /**
  * @author Anthony Eden
  */
 public class UploadMetaData {
 
     private String email;
     private String password;
     private String title;
     private String description;
     private Collection tags;
     private boolean publicFlag;
    private boolean friendFlag;
     private boolean familyFlag;
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public Collection getTags() {
         return tags;
     }
 
     public void setTags(Collection tags) {
         this.tags = tags;
     }
 
     public boolean isPublicFlag() {
         return publicFlag;
     }
 
     public void setPublicFlag(boolean publicFlag) {
         this.publicFlag = publicFlag;
     }
 
    public boolean isFriendFlag() {
        return friendFlag;
     }
 
    public void setFriendFlag(boolean friendFlag) {
        this.friendFlag = friendFlag;
     }
 
     public boolean isFamilyFlag() {
         return familyFlag;
     }
 
     public void setFamilyFlag(boolean familyFlag) {
         this.familyFlag = familyFlag;
     }
 
 }

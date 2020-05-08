 package com.csg.warrior.domain;
 
 import org.hibernate.annotations.Type;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Transient;
 
 @Entity
 public class MobileKey {
     private static final long KEY_EXPIRE_SECONDS = 60;
 
     @Id
     @GeneratedValue
     private Long id;
    private String key;
     @Type(type="org.joda.time.contrib.hibernate.PersistentDateTime")
     private DateTime uploadTime;
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
    public String getKey() {
        return key;
     }
 
    public void setKey(String key) {
        this.key = key;
     }
 
     public DateTime getUploadTime() {
         return uploadTime;
     }
 
     public void setUploadTime(DateTime uploadTime) {
         this.uploadTime = uploadTime;
     }
 
     @Transient
     public boolean isValid() {
         DateTime currentTime = DateTime.now();
         Duration keyUploadDuration = new Duration(uploadTime, currentTime);
         return keyUploadDuration.getStandardSeconds() < KEY_EXPIRE_SECONDS ;
     }
 }

 package com.eldridge.twitsync.rest.endpoints.payload;
 
 import java.io.Serializable;
 
 /**
  * Created by ryaneldridge on 8/9/13.
  */
 public class RegistrationPayload implements Serializable {
 
     private String registrationId;
     private String deviceId;
    private String twitterId;
 
     public RegistrationPayload() {
 
     }
 
    public RegistrationPayload(String registrationId, String deviceId, String twitterId) {
         this.registrationId = registrationId;
         this.deviceId = deviceId;
        this.twitterId = twitterId;
     }
 
     public String getRegistrationId() {
         return registrationId;
     }
 
     public void setRegistrationId(String registrationId) {
         this.registrationId = registrationId;
     }
 
     public String getDeviceId() {
         return deviceId;
     }
 
     public void setDeviceId(String deviceId) {
         this.deviceId = deviceId;
     }
 
    public String getTwitterId() {
        return twitterId;
     }
 
    public void setTwitterId(String twitterId) {
        this.twitterId = twitterId;
     }

 }

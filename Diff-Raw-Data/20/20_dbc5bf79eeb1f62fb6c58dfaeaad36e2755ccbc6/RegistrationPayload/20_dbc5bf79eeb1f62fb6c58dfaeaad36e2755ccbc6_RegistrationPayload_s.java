 package com.eldridge.twitsync.rest.endpoints.payload;
 
 import java.io.Serializable;
 
 /**
  * Created by ryaneldridge on 8/9/13.
  */
 public class RegistrationPayload implements Serializable {
 
     private String registrationId;
     private String deviceId;
    private String userId;
 
     public RegistrationPayload() {
 
     }
 
    public RegistrationPayload(String registrationId, String deviceId, String userId) {
         this.registrationId = registrationId;
         this.deviceId = deviceId;
        this.userId = userId;
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
 
    public String getUserId() {
        return userId;
     }
 
    public void setUserId(String userId) {
        this.userId = userId;
     }
 }

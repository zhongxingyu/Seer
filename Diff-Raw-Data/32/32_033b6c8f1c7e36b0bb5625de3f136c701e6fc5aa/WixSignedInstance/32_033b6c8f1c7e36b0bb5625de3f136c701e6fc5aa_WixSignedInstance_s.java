 package com.wixpress.testapp.domain;
 
 import org.joda.time.DateTime;
 
 import java.util.UUID;
 
 /**
  * Created by : doron
  * Since: 7/1/12
  */
 
 public class WixSignedInstance {
 
     private UUID instanceId;
     private DateTime signDate;
     private UUID uid;
     private String permissions;
 
     //Empty cont' for the ObjectMapper - don't delete
    public WixSignedInstance(){}
 
     public WixSignedInstance(UUID instanceId, DateTime signDate, UUID uid, String permissions) {
         this.instanceId = instanceId;
         this.signDate = signDate;
         this.uid = uid;
         this.permissions = permissions;
     }
 
     public UUID getInstanceId() {
         return instanceId;
     }
 
     public void setInstanceId(UUID instanceId) {
         this.instanceId = instanceId;
     }
 
     public DateTime getSignDate() {
         return signDate;
     }
 
     public void setSignDate(DateTime signDate) {
         this.signDate = signDate;
     }
 
     public UUID getUid() {
         return uid;
     }
 
     public void setUid(UUID uid) {
         this.uid = uid;
     }
 
     public String getPermissions() {
         return permissions;
     }
 
     public void setPermissions(String permissions) {
         this.permissions = permissions;
     }
 }

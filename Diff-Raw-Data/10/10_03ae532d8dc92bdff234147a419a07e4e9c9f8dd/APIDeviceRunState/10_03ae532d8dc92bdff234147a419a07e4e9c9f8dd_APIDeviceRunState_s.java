 package com.testdroid.api.model;
 
 import com.testdroid.api.APIEntity;
 import java.util.Date;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 
 /**
  *
  * @author kajdus
  */
 @XmlRootElement
 public class APIDeviceRunState extends APIEntity {
     @XmlType public static enum Status { STARTED, EXCLUDED, ERROR, FAILED, SUCCEEDED, NOT_AVAILABLE }
    @XmlType public static enum Type {
         PREPARING, WAITING, DEVICE_WAITING, DEVICE_DOWNLOAD_TESTSESSION, DEVICE_UNINSTALLING_ALL,
         DEVICE_REBOOTING, DEVICE_WIFI_CHECKING, DEVICE_ADD_PERMISSIONS,
         DEVICE_REPACKAGING, DEVICE_TARGET_INSTALLING, DEVICE_TEST_INSTALLING,DEVICE_REMOTECONTROL_RUNNING,
         DEVICE_RUNNING, DEVICE_SDCARD_COPYING, DEVICE_TARGET_UNINSTALLING, DEVICE_UIAUTOMATOR_RUNNING,
         DEVICE_TEST_UNINSTALLING, RESULTS_WAITING, RESULTS_PROCESSING, PARSE_LOGCAT
     }
     
     private Long startTimeMS;
     private Long finishTimeMS;
     private Date retryTime;
     private String failReason;
     private Status status;
     private Type type;
 
     public APIDeviceRunState() {}
     public APIDeviceRunState(Long id, Long startTimeMS, Long finishTimeMS, Date retryTime, String failReason, Status status, Type type) {
         super(id);
         this.startTimeMS = startTimeMS;
         this.finishTimeMS = finishTimeMS;
         this.retryTime = retryTime;
         this.failReason = failReason;
         this.status = status;
         this.type = type;
     }
 
     public Long getStartTimeMS() {
         return startTimeMS;
     }
 
     public void setStartTimeMS(Long startTimeMS) {
         this.startTimeMS = startTimeMS;
     }
 
     public Long getFinishTimeMS() {
         return finishTimeMS;
     }
 
     public void setFinishTimeMS(Long finishTimeMS) {
         this.finishTimeMS = finishTimeMS;
     }
 
     public Date getRetryTime() {
         return retryTime;
     }
 
     public void setRetryTime(Date retryTime) {
         this.retryTime = retryTime;
     }
 
     public String getFailReason() {
         return failReason;
     }
 
     public void setFailReason(String failReason) {
         this.failReason = failReason;
     }
 
     public Status getStatus() {
         return status;
     }
 
     public void setStatus(Status status) {
         this.status = status;
     }
 
     public Type getType() {
         return type;
     }
 
     public void setType(Type type) {
         this.type = type;
     }
     
 }

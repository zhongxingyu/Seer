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
     
     private Long deviceRunId;
     private DeviceRunStateType type;
     private Long startTimeMS;
     private Long finishTimeMS;
     private Date retryTime;
     private String failReason;
     private Status status;
    private 
 
     public APIDeviceRunState() {}
     public APIDeviceRunState(Long id, Long deviceRunId, Long startTimeMS, Long finishTimeMS, Date retryTime, String failReason, Status status, DeviceRunStateType type) {
         super(id);
         this.deviceRunId = deviceRunId;
         this.startTimeMS = startTimeMS;
         this.finishTimeMS = finishTimeMS;
         this.retryTime = retryTime;
         this.failReason = failReason;
         this.status = status;
         this.type = type;
     }
     
     public Long getDeviceRunId() {
          return deviceRunId;
     }
  
     public void setDeviceRunId(Long deviceRunId) {
         this.deviceRunId = deviceRunId;
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
 
     public DeviceRunStateType getDeviceRunStateType() {
         return type;
     }
 
     public void setDeviceRunStateType(DeviceRunStateType type) {
         this.type = type;
     }
 
     
     
     
 }

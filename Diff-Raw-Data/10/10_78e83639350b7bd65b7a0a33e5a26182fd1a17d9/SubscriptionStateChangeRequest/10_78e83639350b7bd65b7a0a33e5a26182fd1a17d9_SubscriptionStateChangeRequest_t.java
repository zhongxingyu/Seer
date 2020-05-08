 package org.motechproject.ananya.kilkari.internal;
 
 import org.joda.time.DateTime;
 
 import java.util.Date;
 
 public class SubscriptionStateChangeRequest {
 
     private String subscriptionId;
 
     private String subscriptionStatus;
 
     private String reason;
 
     private DateTime createdAt;
 
     private String operator;
 
    private int graceCount;

    public int getGraceCount() {
        return graceCount;
    }

    public void setGraceCount(int graceCount) {
        this.graceCount = graceCount;
    }

     public String getSubscriptionId() {
         return subscriptionId;
     }
 
     public void setSubscriptionId(String subscriptionId) {
         this.subscriptionId = subscriptionId;
     }
 
     public String getSubscriptionStatus() {
         return subscriptionStatus;
     }
 
     public DateTime getCreatedAt() {
         return createdAt;
     }
 
     public void setCreatedAt(DateTime createdAt) {
         this.createdAt = createdAt;
     }
 
     public void setSubscriptionStatus(String subscriptionStatus) {
         this.subscriptionStatus = subscriptionStatus;
     }
 
     public String getReason() {
         return reason;
     }
 
     public void setReason(String reason) {
         this.reason = reason;
     }
 
     public String getOperator() {
         return operator;
     }
 
     public void setOperator(String operator) {
         this.operator = operator;
     }
 }

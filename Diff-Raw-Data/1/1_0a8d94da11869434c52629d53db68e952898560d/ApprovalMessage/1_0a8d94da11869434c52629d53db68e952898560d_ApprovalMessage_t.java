 package org.mule.galaxy.policy;
 
 import org.apache.commons.lang.builder.ReflectionToStringBuilder;
 import org.apache.commons.lang.builder.ToStringStyle;
 
 public class ApprovalMessage {
     private boolean warning;
     private String message;
     
     public ApprovalMessage(String message) {
         this.message = message;
     }
 
     public ApprovalMessage(String message, boolean warning) {
         this.message = message;
         this.warning = warning;
     }
     
     public boolean isWarning() {
         return warning;
     }
     public void setWarning(boolean warning) {
         this.warning = warning;
     }
     public String getMessage() {
         return message;
     }
     public void setMessage(String message) {
         this.message = message;
     }
 
     @Override
     public String toString()
     {
         return ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE);
     }
 }

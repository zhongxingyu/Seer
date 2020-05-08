 package com.ssm.llp.biz.validation;
 
 /**
  * @author rafizan.baharum
  * @since 9/19/13
  */
 public class ValidatorContext {
    private boolean poisoned = false;
    private boolean existed = false;
    private String error = "ERROR";
    private boolean waived = false;
 
     public boolean isValid() {
         return !(poisoned | existed);
     }
 
     public boolean isPoisoned() {
         return poisoned;
     }
 
     public void setPoisoned(boolean poisoned) {
         this.poisoned = poisoned;
     }
 
     public boolean isExisted() {
         return existed;
     }
 
     public void setExisted(boolean existed) {
         this.existed = existed;
     }
 
     public String getError() {
         return error;
     }
 
     public void setError(String error) {
         this.error = error;
     }
 
     public boolean isWaived() {
         return waived;
     }
 
     public void setWaived(boolean waived) {
         this.waived = waived;
     }
 }

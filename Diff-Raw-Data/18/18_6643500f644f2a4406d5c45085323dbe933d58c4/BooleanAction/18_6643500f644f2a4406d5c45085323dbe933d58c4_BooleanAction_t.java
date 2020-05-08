 package com.sun.identity.admin.model;
 
 import com.sun.identity.admin.DeepCloneable;
 import com.sun.identity.admin.handler.BooleanActionHandler;
 import java.io.Serializable;
 
 public class BooleanAction
         extends BaseAction
         implements Action, Serializable {
 
     private boolean allow = false;
     private BooleanActionHandler booleanActionHandler;
 
     public BooleanAction() {
         booleanActionHandler = new BooleanActionHandler();
         booleanActionHandler.setBooleanAction(this);
     }
 
    public Boolean getValue() {
         return Boolean.valueOf(allow);
     }
 
     public DeepCloneable deepClone() {
         BooleanAction clone = new BooleanAction();
         clone.setName(getName());
         clone.setAllow(allow);
 
         return clone;
     }
 
     public BooleanActionHandler getBooleanActionHandler() {
         return booleanActionHandler;
     }
 
     public void setBooleanActionHandler(BooleanActionHandler booleanActionHandler) {
         this.booleanActionHandler = booleanActionHandler;
     }
 
     public boolean isAllow() {
         return allow;
     }
 
     public void setAllow(boolean allow) {
         this.allow = allow;
     }
 }

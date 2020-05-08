 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.controller;
 
 import java.util.List;
 import net.wgr.lang.I18N;
 
 /**
  * 
  * @created Oct 5, 2011
  * @author double-u
  */
 public class BadAPICallException extends Exception {
 
     protected String methodName;
     protected List args;
     protected String errorName, errorDescription;
     protected List<String> info;
 
     public BadAPICallException(String methodName, List args) {
         this(methodName, args, "Call " + methodName + " failed", "");
     }
 
     public BadAPICallException(String methodName, List params, String errorName, List<String> info) {
         this.methodName = methodName;
         this.args = params;
         this.errorName = errorName;
        this.info = info;
        this.errorDescription = getErrorDescription();
        String betterDescription = I18N.get().getText(errorName);
        if (betterDescription != null) {
            this.errorDescription = betterDescription;
        }
     }
 
     public BadAPICallException(String methodName, List params, String errorName, String errorDescription) {
         this.methodName = methodName;
         this.args = params;
         this.errorName = errorName;
         String betterDescription = I18N.get().getText(errorName);
         this.errorDescription = (betterDescription == null ? errorDescription : betterDescription);
     }
 
     public List getArgs() {
         return args;
     }
 
     public String getMethodName() {
         return methodName;
     }
 
     public String getErrorName() {
         return errorName;
     }
 
    public final String getErrorDescription() {
         if (this.errorDescription != null && !this.errorDescription.isEmpty()) {
             return errorDescription;
         } else if (info != null) {
             String decription = null;
             if (info.size() > 2) {
                 decription = info.get(2);
             } else if (info.size() == 2) {
                 decription = info.get(1);
            } else {
                decription = errorName;
             }
 
             return decription;
         }
         return "No description available";
     }
 
     public void setErrorDescription(String errorDescription) {
         this.errorDescription = errorDescription;
     }
 
     public List<String> getInfo() {
         return info;
     }
 
     public void setInfo(List<String> info) {
         this.info = info;
     }
 
     public String toString() {
         return "The method " + methodName + " returned following error " + errorName + " : " + getErrorDescription();
     }
 
     @Override
     public String getMessage() {
        if (errorDescription == null) errorDescription = getErrorDescription();
       return errorDescription;
     }
 }

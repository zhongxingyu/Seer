 package com.ctp.javaone.archiver.plugin;
 
 public class Result {
 
     private final String reason;
     private final Status status;
     
     public Result(String reason, Status status) {
         this.reason = reason;
         this.status = status;
     }
 
     public String getReason() {
         return reason;
     }
 
     public Status getStatus() {
         return status;
     }
     
 }

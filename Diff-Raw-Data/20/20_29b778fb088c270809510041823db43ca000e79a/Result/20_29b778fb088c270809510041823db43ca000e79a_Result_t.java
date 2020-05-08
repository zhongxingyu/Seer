 package com.ctp.javaone.archiver.plugin;
 
 public class Result {
    
    public static final Result SUCCESS = new Result("", Status.SUCCESS);
 
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

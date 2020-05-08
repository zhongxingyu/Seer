 package no.helsenorge.nipen.model;
 
 public class DataType {
     private long id = -1;
     private long userId;
     private String timestamp;
 
     protected DataType() {
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public long getId() {
         return id;
     }
 
     public long getUserId() {
         return userId;
     }
 
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
     }
 
     public String getTimestamp() {
         return timestamp;
     }
 }

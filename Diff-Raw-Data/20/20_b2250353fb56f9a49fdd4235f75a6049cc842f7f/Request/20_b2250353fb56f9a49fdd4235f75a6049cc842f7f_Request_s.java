 package com.sample.model;
 
 import javax.persistence.*;
 
 @Entity
 @Table(name = "requests")
 public class Request {
 
     @Id
     @Column(name = "id")
     @GeneratedValue
     private long id;
 
     @ManyToOne
     @JoinColumn(name = "till_request_id")
     private TillRequest tillRequest;
 
     @Column(name = "till_number")
     private int tillNumber;
 
     @Column(name = "time_stamp")
    private int time_stamp;
 
     @Column(name = "done")
     private boolean done;
 
     public void setTillNumber(int tillNumber) {
         this.tillNumber = tillNumber;
     }
 
     public int getTillNumber() {
         return tillNumber;
     }
 
    public int getTime_stamp() {
        return time_stamp;
     }
 
    public void setTime_stamp(int time_stamp) {
        this.time_stamp = time_stamp;
     }
 
     public TillRequest getTillRequest() {
         return tillRequest;
     }
 
     public void setTillRequest(TillRequest tillRequest) {
         this.tillRequest = tillRequest;
     }
 
     public boolean isDone() {
         return done;
     }
 
     public void setDone(boolean done) {
         this.done = done;
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 }

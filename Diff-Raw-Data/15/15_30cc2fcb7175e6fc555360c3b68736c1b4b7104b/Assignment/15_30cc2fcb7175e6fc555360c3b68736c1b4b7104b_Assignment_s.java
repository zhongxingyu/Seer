 package com.ismobile.blaagent;
 
 /**
  * Created by pbm on 2013-07-03.
  */
 public class Assignment {
     private float longi;
     private float lati;
     private String title;
     private String uid;
     private boolean booked;
     private String start;
     private String stop;
 
    public Assignment() {
     }

     public void setLongi(float longitude) {
         this.longi = longitude;
     }
     public void setLati(float latitude) {
         this.lati = latitude;
     }
     public void setTitle(String titleAss) {
         this.title = titleAss;
     }
     public void setUid(String uId) {
         this.uid = uId;
     }
     public void setBooked(boolean bookedAss) {
         this.booked = bookedAss;
     }
     public void setStart(String startTime) {
         this.start = startTime;
     }
     public void setStop(String stopTime) {
         this.stop = stopTime;
     }

     public String getTitle() {
         //return this.title;
         return "Byte av glödlampa.";
     }
 
     public String getUid() {
         return this.uid;
     }
 
     public String getStart() {
         return this.start;
     }
 
     public String getStop() {
         //return this.stop;
         return "16:00";
     }
 
     public boolean getBooked() {
         return this.booked;
     }
 
     public float getLongitude() {
         return this.longi;
     }
 
     public float getLatitude() {
         return this.lati;
     }
 }

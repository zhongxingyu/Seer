 package org.vikenpedia.fellesprosjekt.shared.models;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 
 public class Meeting extends Model {
 
     private int id, chairmanId;
     private Timestamp startTime, endTime; // is sql.Date the correct type?
     private String description, place;
     private boolean isChairman;
 
    public Meeting(int id, Timestamp startTime, Timestamp endTime, String description,
             String place, int chairmanId, int userId) {
         this.id = id;
         this.startTime = startTime;
         this.endTime = endTime;
         this.description = description;
         this.place = place;
         this.chairmanId = chairmanId;
         if (chairmanId == userId) {
         	this.isChairman = true;
         }
         else
         	this.isChairman = false;
     }
 
    public Meeting(int id, Timestamp startTime, Timestamp endTime, String description,
             String place, int chairmanId) {
         this.id = id;
         this.startTime = startTime;
         this.endTime = endTime;
         this.description = description;
         this.place = place;
         this.chairmanId = chairmanId;
     }
     
     public Meeting(Timestamp startTime, Timestamp endTime, String description, String place,
             int chairmanId) {
         this.startTime = startTime;
         this.endTime = endTime;
         this.description = description;
         this.place = place;
         this.chairmanId = chairmanId;
     }
 
     public Meeting(Timestamp startTime, Timestamp endTime, String description, String place) {
         this.startTime = startTime;
         this.endTime = endTime;
         this.description = description;
         this.place = place;
     }
 
     @Override
     public void saveModel() {
         // TODO Auto-generated method stub
 
     }
 
     public ArrayList<User> getMeetingParticipants() {
         return null;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public Timestamp getStartTime() {
         return startTime;
     }
 
     public void setStartTime(Timestamp startTime) {
         this.startTime = startTime;
     }
 
     public Timestamp getEndTime() {
         return endTime;
     }
 
     public void setEndTime(Timestamp endTime) {
         this.endTime = endTime;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public String getPlace() {
         return place;
     }
 
     public void setPlace(String place) {
         this.place = place;
     }
 
     public int getChairmanId() {
         return chairmanId;
     }
 
     public void setChairmanId(int chairmanId) {
         this.chairmanId = chairmanId;
     }
 
 }

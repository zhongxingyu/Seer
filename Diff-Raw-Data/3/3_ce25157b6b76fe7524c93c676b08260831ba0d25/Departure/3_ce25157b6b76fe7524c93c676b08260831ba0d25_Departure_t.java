 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.mycompany.booking.core;
 
 import java.io.Serializable;
 import java.sql.Time;
 import java.util.Date;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Temporal;
 
 /**
  *
  * @author Philip och Johan
  */
 @Entity
 public class Departure implements Serializable {
 
     @Id
     private Long id;
     private String departureLocation;
     private String destination;
     
     private Time departureTime;
     @Temporal(javax.persistence.TemporalType.DATE)
     private Date departureDate;
     
     private Time travelTime;
 
    public Departure() {
    }

     public Departure(String departureLocation, String destination, Time depTime,Date depDate,Time travelTime){
         this.departureLocation = departureLocation;
         this.destination = destination;
         this.departureTime = depTime;
         this.departureDate = depDate;
         this.travelTime = travelTime;
     }
     
     public String getDepartureLocation() {
         return departureLocation;
     }
 
     public String getDestination() {
         return destination;
     }
 
     public Time getDepartureTime() {
         return departureTime;
     }
 
     public Date getDepartureDate() {
         return departureDate;
     }
 }

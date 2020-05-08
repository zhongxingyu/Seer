 package com.lauchenauer.nextbusperth.dao;
 
 import java.util.Date;
 
 public class Service {
     private String stopNumber;
     private String stopName;
     private String routeNumber;
     private String routeName;
     private String headsign;
     private Date departureTime;
 
     public Service(String stopNumber, String stopName, String routeNumber, String routeName, String headsign, Date departureTime) {
         this.stopNumber = stopNumber;
         this.stopName = stopName;
         this.routeNumber = routeNumber;
         this.routeName = routeName;
         this.headsign = headsign;
         this.departureTime = departureTime;
     }
 
     public String getStopNumber() {
         return stopNumber;
     }
 
     public String getStopName() {
         return stopName;
     }
 
     public String getRouteNumber() {
         return routeNumber;
     }
 
     public String getRouteName() {
         return routeName;
     }
 
     public String getHeadsign() {
         return headsign;
     }
 
     public Date getDepartureTime() {
         return departureTime;
     }
 
     public String getTimeDelta() {
         if (departureTime == null) {
             return "\u221e";
         }
 
         long minutes = timDiffInMinutes(departureTime);
 
         if (minutes == 0) {
             return "Now";
         } else {
            return "" + minutes;
         }
     }
 
     private long timDiffInMinutes(Date time) {
         if (time == null) return 0;
 
         Date now = new Date();
         long millis = time.getTime() - now.getTime();
 
         return millis / 1000 / 60;
     }
 }

 package org.primefaces.model;
 
 import java.util.Date;
 
 /**
  * Author: Piotr Turek
  */
 public interface EnrollScheduleEvent extends ScheduleEvent {
 
     int getImportance();    //0-100 (%), gets translated into opacity
 
     int getPoints();        //number of points assigned to the given event.
 
     boolean isPossible();
 
     String getTeacher();
 
     String getPlace();
 
     String getColor();
 
    String getActivityType();
 
     Boolean isInteractive();
 
     Boolean isShowPoints();
 }

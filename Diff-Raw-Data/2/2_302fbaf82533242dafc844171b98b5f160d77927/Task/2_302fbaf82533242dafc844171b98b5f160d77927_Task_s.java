 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package core.task;
 
 import core.utils.Hour;
 
 /**
  *
  * @author a1
  */
 public class Task implements Comparable<Task> {
 
     private Hour initHour;
     private Hour endHour;
     private String description;
 
     public Task( Hour initHour, Hour endHour, String description ) {
         this.initHour = initHour;
         this.endHour = endHour;
         this.description = description;
     }
 
     public Hour getInitHour() {
         return initHour;
     }
 
     public Hour getEndHour() {
         return endHour;
     }
 
     public String getDescription() {
         return description;
     }
     
     @Override
     public int compareTo( Task task ) {
         return this.initHour.compareTo( task.initHour );
     }
 
     public boolean isInTime( Hour hour ) {
        return this.initHour.compareTo( hour ) <= 0
                && this.endHour.compareTo( hour ) > 0;
     }
 
     public boolean isAfter( Hour hour ) {
         return this.endHour.compareTo( hour ) <= 0;
     }
 
     @Override
     public String toString() {
         return "Task{" + "initHour=" + initHour + ", endHour=" + endHour + ", description=" + description + '}';
     }
         
 }

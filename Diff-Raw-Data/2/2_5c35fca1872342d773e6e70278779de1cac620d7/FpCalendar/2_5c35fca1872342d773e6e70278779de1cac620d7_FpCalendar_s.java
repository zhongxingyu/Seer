 package org.vikenpedia.fellesprosjekt.shared.models;
 
 public class FpCalendar extends Model {
 
     private int id;
     private String calendarName, owner;
     
 
     public FpCalendar(int id, String calendarName, String owner) {
         this.id = id;
         this.calendarName = calendarName;
         this.owner = owner;
     }
     
     public FpCalendar(int id) {
     	this.id = id;
     }
 
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     @Override
     public String toString() {
        return Integer.toString(getId());
     }
 
     @Override
     public void saveModel() {
         // TODO Auto-generated method stub
 
     }
 
 }

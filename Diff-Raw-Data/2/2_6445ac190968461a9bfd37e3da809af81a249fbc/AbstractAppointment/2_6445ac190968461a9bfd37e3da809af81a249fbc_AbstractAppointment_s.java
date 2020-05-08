 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package FP;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 /**
  *
  * @author lycantrophe
  */
 public abstract class AbstractAppointment implements Appointment {
 
     protected Date start;
     protected Date end;
     protected Person owner;
     protected String description;
     protected Location location;
     protected ArrayList<String> participants;
 
     public AbstractAppointment(Person owner, Date start, Date end, String description, ArrayList<String> participants) {
         this.owner = owner;
         this.start = start;
         this.end = end;
         this.description = description;
         this.participants = participants;
     }
 
     /**
      * Updates the appointment according to differences (and not-null fields) in
      * newAppointment
      *
      * @param newAppointment Appointment object holding fields to modify
      */
     public void updateAppointment(Appointment newAppointment) {
         if (newAppointment.getStart() != null) {
             start = newAppointment.getStart();
         }
         if (newAppointment.getEnd() != null) {
             end = newAppointment.getEnd();
         }
         if (newAppointment.getOwner() != null) {
             owner = newAppointment.getOwner();
         }
         if (newAppointment.getDescription() != null) {
             description = newAppointment.getDescription();
         }
         if (newAppointment.getLocation() != null) {
             location = newAppointment.getLocation();
         }
         if (newAppointment.getParticipants() != null) {
             participants = newAppointment.getParticipants();
         }
     }
 
     /**
      * @return the start
      */
     @Override
     public Date getStart() {
         return start;
     }
 
     /**
      * @return the end
      */
     @Override
     public Date getEnd() {
         return end;
     }
 
     /**
      * @return the owner
      */
     @Override
     public Person getOwner() {
         return owner;
     }
 
     /**
      * @return the description
      */
     @Override
     public String getDescription() {
         return description;
     }
 
     /**
      * @return the location
      */
     @Override
    public AbstractLocation getLocation() {
         return location;
     }
 
     /**
      * @return the participants
      */
     @Override
     public ArrayList<String> getParticipants() {
         return participants;
     }
 }

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
 public interface Appointment {
 
     public ArrayList<Person> getInvited();
 
     public Date getStart();
 
     public Date getEnd();
 
     public String getDescription();
 
     public Person getOwner();
 
    public Location getLocation();
     
     public ArrayList<String> getParticipants();
 }

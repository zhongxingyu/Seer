 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package FP;
 
 import java.util.LinkedList;
 import java.util.Queue;
 
 /**
  *
  * @author lycantrophe
  */
 public class Person {
     
     private String username;
     private String firstname;
     private String surname;
     private String email;
     private String phoneNumber;
     
     private Queue<Object> notifications;
     /*
      * Structure options are:
      *  Hash table
      *  List
      */
     private Structure appointments;
     
     
     public Person( String username, String firstname, String surname, String email, String phoneNumber ){
         
         this.username = username;
         this.firstname = firstname;
         this.surname = surname;
         this.email = email;
         this.phoneNumber = phoneNumber;
         
         notifications = new LinkedList<Object>();
     }
     
     /**
      * Adds an appointment to this objects list and adds a notification to the notification queue
      * 
      * @param appointment Appointment to add
      */
     public void addAppointment( Appointment appointment ){
         appointments.add( appointment );
         notifications.add(Object);
     }
     
     /**
      * Removes the given appointment from this objects appointments list
      * 
      * @param appointment Appointment to remove
      */
     
     public void removeAppointment( Appointment appointment ){
         appointments.remove( appointment );
     }
     
     public void declined( Appointment appointment, Person person ){
         appointments( appointment ).remove( person );
         notifyAll();
     }
    public String getUsername() {
         return username;
     }
 }

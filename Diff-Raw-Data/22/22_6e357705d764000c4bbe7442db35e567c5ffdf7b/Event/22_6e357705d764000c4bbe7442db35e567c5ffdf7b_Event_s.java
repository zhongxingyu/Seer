 /**
  * Name:    Amuthan Narthana and Nicholas Dyszel
  * Section: 2
  * Program: Scheduler Project
  * Date:    10/8/12
  */
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 /**
  * This class represents an event in a user's schedule. The event 
  * might be a one-time event, or it might be recurring (daily, weekly, 
  * etc.).
  * 
  * @author Nick
  */
 public abstract class Event {
     protected int eventID;      // the ID of the event in the SQL database
     protected String name;      // the name of the event
     protected String location;  // the location of the event
     protected User[] attendees; // the users attending the event
     protected User creator;     // the user that created this event
     
     /**
      * Init constructor
      * @param name          name of the event
      * @param location      location of the event
      * @param attendees     users attending event
      * @param creator       creator of the event
      */
     public Event(String name, String location, User[] attendees, User creator){
         this.name = name;
         this.location = location;
         this.attendees = attendees;
         this.creator = creator;
         eventID = -1;
     }
     
     public void setEventID(int eventID) {
         this.eventID = eventID;
     }
     
     public int getEventID() {
         return eventID;
     }
     
     /**
      * This gets all events within a certain time frame.
      * Example: If this is an event that occurs every Monday at 9:00, 
      *          and if the time frame is from Mon. Oct. 1 to Wed. Oct. 31, 
      *          then this function would return events at 9:00 on 
      *          Oct. 1, Oct. 8. Oct. 15, Oct. 22, and Oct. 29.
      * 
      * @return  an ArrayList of OneTimeEvent objects representing
      *          the events in the given time interval
      */
     public abstract ArrayList<OneTimeEvent> getEvents(Date min, Date max);
     
     /**
      * This is a getter for the 'name' data member.
      * @return  the name of the event
      */
     public String getName(){
         return name;
     }
     
     /**
      * This is a getter for the 'location' data member.
      * @return  the location of the event
      */
     public String getLocation(){
         return location;
     }
     
     public User[] getAttendees(){
         return attendees;
     }
     
     /**
      * Takes an array of usernames and sets it as the list of attendees
      * @param attendeeIDs
      */
     public void setAttendees(ArrayList<String> attendeeIDs) {
         User[] attendees = new User[attendeeIDs.size()];
         int i=0;
         for (String attendee : attendeeIDs) {
             attendees[i] = SchedulerMain.application.searchForUser(attendee);
             i++;
         }
         this.attendees = attendees;
     }
     
     public abstract Calendar getStartEventCalendar();
     
     public abstract TimeBlock getTimes();
     
     public abstract int getRecurrence();
     
     public Calendar getEndEventCalendar(){
         return null;
     }
 }

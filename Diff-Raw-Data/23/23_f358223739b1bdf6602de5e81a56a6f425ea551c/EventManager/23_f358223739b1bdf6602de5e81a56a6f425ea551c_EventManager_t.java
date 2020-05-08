 package BackEnd.ManagerSystem;
 
 import BackEnd.EventSystem.*;
 import BackEnd.UserSystem.*;
 
 import java.util.ArrayList;
import java.util.Calendar;
 
 /**
  * This class serves as a liaison between the GUI and the back end and the data.
  * It checks to see whether a user has the proper privileges to change
  * something, and if the user does, then edits the database accordingly. It also
  * provides ready-to-use methods for the GUI to call.
  *
  * @author Julian Kuk
  */
 public class EventManager {
 
     private ArrayList<Event> eventList;
     private Event selectedEvent;
 
     public EventManager() {
         // not used in current version. may be utilized in future version
         eventList = new ArrayList<Event>();
         // get information from database
     }
 
     // method may be finished in future version
     public ArrayList<Event> getEventList() {
         return null;
     }
 
     public void setSelectedEvent(Event selectedEvent) {
         this.selectedEvent = selectedEvent;
     }
 
     public Event getSelectedEvent() {
         return selectedEvent;
     }
 
     public void createEvent(Event newEvent, User loggedInUser) {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent = newEvent;
             // write to database
         }
     }
 
     public void removeEvent(User loggedInUser) {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent = null;
         }
     }
 
     public void addOrganizer(User organizer, User loggedInUser) {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.getOrganizerList().add(organizer);
             // write to database
         }
     }
 
     public void removeOrganizer(User organizer, User loggedInUser) {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.getOrganizerList().remove(organizer);
             // write to database
             // remove all related database entries
         }
     }
 
     public void addSubEvent(SubEvent subEvent, User loggedInUser) {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.getSubEventList().add(subEvent);
             // write to database
         }
     }
 
     public void removeSubEvent(SubEvent subEvent, User loggedInUser) {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.getSubEventList().remove(subEvent);
             // write to database
             // remove all related database entries
         }
     }
 
     public void addCommittee(Committee committee, User loggedInUser) {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.getCommitteeList().add(committee);
             // write to database
         }
     }
 
     public void removeCommittee(Committee committee, User loggedInUser) {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.getCommitteeList().remove(committee);
             // write to database
             // remove all related database entries
         }
     }
 
     public void editDescription(String description, User loggedInUser) {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.setDescription(description);
             // write to database
         }
     }
 
     public void editLocation(String location, User loggedInUser) {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.setLocation(location);
             // write to database
         }
     }
 
     public void editStartDate(Calendar startDate, User loggedInUser) {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.setStartDate(startDate);
             // write to database
         }
     }
 
     public void editEndDate(Calendar endDate, User loggedInUser) {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.setEndDate(endDate);
             // write to database
         }
     }
 
     public void editStartTime(Calendar startTime, User loggedInUser) {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.setStartTime(startTime);
             // write to database
         }
     }
 
     public void editEndTime(Calendar startTime, User loggedInUser) {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.setEndTime(startTime);
             // write to database
         }
     }
 }

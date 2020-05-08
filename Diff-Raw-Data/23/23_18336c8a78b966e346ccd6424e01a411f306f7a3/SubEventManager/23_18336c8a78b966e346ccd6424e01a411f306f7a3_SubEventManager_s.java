 package BackEnd.ManagerSystem;
 
 import BackEnd.EventSystem.*;
 import BackEnd.UserSystem.*;
 
 import java.util.ArrayList;
 
 /**
  * This class serves as a liaison between the GUI and the back end and the data.
  * It checks to see whether a user has the proper privileges to change
  * something, and if the user does, then edits the database accordingly. It also
  * provides ready-to-use methods for the GUI to call.
  *
  * @author Julian Kuk
  */
 public class SubEventManager {
     private ArrayList<SubEvent> subEventList;
     private SubEvent selectedSubEvent;
 
     public SubEventManager() {
         subEventList = new ArrayList<SubEvent>();
         // build sub event list
     }
 
     public void setSelectedSubEvent(SubEvent selectedSubEvent) {
         this.selectedSubEvent = selectedSubEvent;
     }
 
     public SubEvent getSelectedSubEvent() {
         return selectedSubEvent;
     }
     
     public void addParticipant(){
     }
     
     public void removeParticipant(){
     }
 
     public void editDescription(String description, User loggedInUser, Event selectedEvent, Committee selectedCommittee) {
         if (PrivilegeManager.hasSubEventPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedSubEvent.setDescription(description);
             // write to database
         }
     }
 
     public void editLocation(String location, User loggedInUser, Event selectedEvent, Committee selectedCommittee) {
         if (PrivilegeManager.hasSubEventPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedSubEvent.setLocation(location);
             // write to database
         }
     }
 
     public void editStartDate(Calendar startDate, User loggedInUser, Event selectedEvent, Committee selectedCommittee) {
         if (PrivilegeManager.hasSubEventPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedSubEvent.setStartDate(startDate);
             // write to database
         }
     }
 
     public void editEndDate(Calendar endDate, User loggedInUser, Event selectedEvent, Committee selectedCommittee) {
         if (PrivilegeManager.hasSubEventPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedSubEvent.setEndDate(endDate);
             // write to database
         }
     }
 
     public void editStartTime(Calendar startTime, User loggedInUser, Event selectedEvent, Committee selectedCommittee) {
         if (PrivilegeManager.hasSubEventPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedSubEvent.setStartTime(startTime);
             // write to database
         }
     }
 
     public void editEndTime(Calendar startTime, User loggedInUser, Event selectedEvent, Committee selectedCommittee) {
         if (PrivilegeManager.hasSubEventPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedSubEvent.setEndTime(startTime);
             // write to database
         }
     }
 }

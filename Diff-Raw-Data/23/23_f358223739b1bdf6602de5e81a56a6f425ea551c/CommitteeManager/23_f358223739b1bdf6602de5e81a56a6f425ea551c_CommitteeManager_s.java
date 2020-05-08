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
 public class CommitteeManager {
 
     private ArrayList<Committee> committeeList;
     private Committee selectedCommittee;
 
     public CommitteeManager() {
         committeeList = new ArrayList<Committee>();
         // get information from database
     }
 
     public ArrayList<Committee> getCommitteeList() {
         return committeeList;
     }
 
     public void setSelectedCommittee(Committee selectedCommittee) {
         this.selectedCommittee = selectedCommittee;
     }
 
     public Committee getSelectedCommittee() {
         return selectedCommittee;
     }
 
     public void editTitle(String title, User loggedInUser, Event selectedEvent) {
         if (PrivilegeManager.hasCommitteePrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedCommittee.setTitle(title);
             // write to database
         }
     }
     
     public void editChair(User chair, User loggedInUser, Event selectedEvent) {
         if (PrivilegeManager.hasCommitteePrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedCommittee.setChair(chair);
             // write to database
         }
     }
     
     public void addBudgetAccess(User budgetAccess, User loggedInUser, Event selectedEvent) {
         if (PrivilegeManager.hasCommitteePrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
            selectedCommittee.getBudgetAccessList.add(budgetAccess);
             // write to database
         }
     }
     
     public void removeBudgetAccess(User budgetAccess, User loggedInUser, Event selectedEvent) {
         if (PrivilegeManager.hasCommitteePrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
            selectedCommittee.getBudgetAccessList.remove(budgetAccess);
             // write to database
             // remove all related database entries
         }
     }
 
     public void addMember(User member, User loggedInUser, Event selectedEvent) {
         if (PrivilegeManager.hasCommitteePrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedCommittee.getMemberList().add(member);
             // write to database
         }
     }
 
     public void removeMember(User member, User loggedInUser, Event selectedEvent) {
         if (PrivilegeManager.hasCommitteePrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedCommittee.getMemberList().remove(member);
             // write to database
             // remove all related database entries
         }
     }
 
     public void addTask(Task task, User loggedInUser, Event selectedEvent) {
         if (PrivilegeManager.hasCommitteePrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
            selectedCommittee.getTaskList().addTask(task);
             // write to database
         }
     }
 
     public void removeTask(Task task, User loggedInUser, Event selectedEvent) {
         if (PrivilegeManager.hasCommitteePrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
            selectedCommittee.getTaskList().removeTask(task);
             // write to database
             // remove all related database entries
         }
     }
 }

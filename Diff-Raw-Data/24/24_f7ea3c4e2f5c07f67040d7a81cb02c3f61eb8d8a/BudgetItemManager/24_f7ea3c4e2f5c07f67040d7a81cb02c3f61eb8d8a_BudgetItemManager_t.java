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
 public class BudgetItemManager {
 
     private ArrayList<BudgetItem> budgetItemList;
     private BudgetItem selectedBudgetItem;
 
     public BudgetItemManager() {
         budgetItemList = new ArrayList<BudgetItem>();
         // build list from database
     }
 
     public void setSelectedBudgetItem(BudgetItem selectedBudgetItem) {
         this.selectedBudgetItem = selectedBudgetItem;
     }
 
     public BudgetItem getSelectedBudgetItem() {
         return selectedBudgetItem;
     }
 
    public void editValue(int value, User loggedInUser, Event selectedEvent, Committee selectedCommittee) {
         if (PrivilegeManager.hasBudgetPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedBudgetItem.setValue(value);
             // write to database
         }
     }
 
    public void editDescription(String description, User loggedInUser, Event selectedEvent, Committee selectedCommittee) {
         if (PrivilegeManager.hasBudgetPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedBudgetItem.setValue(description);
             // write to database
         }
     }
 
    public void editDate(Calendar date, User loggedInUser, Event selectedEvent, Committee selectedCommittee) {
         if (PrivilegeManager.hasBudgetPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedBudgetItem.setValue(date);
             // write to database
         }
     }
 }

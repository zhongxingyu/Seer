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
 public class BudgetManager {
 
     private ArrayList<Budget> budgetList;
     private Budget selectedBudget;
 
     /**
      * Default, no arg constructor. Builds the budget list.
      */
     public BudgetManager() {
         budgetList = new ArrayList<Budget>();
         // get information from database
     }
 
     /**
      * Get the budget list.
      *
      * @return the budget list
      */
     public ArrayList<Budget> getBudgetList() {
         return budgetList;
     }
 
     /**
      * Store the budget selected by the user.
      *
      * @param budget the budget that is selected by the user
      */
     public void setSelectedBudget(Budget selectedBudget) {
         this.selectedBudget = selectedBudget;
     }
 
     /**
      * Retrieve the budget selected by the user
      *
      * @return the budget selected by the user
      */
     public Budget getSelectedBudget() {
         return selectedBudget;
     }
 
     /**
      * Add an income item to the selected budget. Verifies the user's privilege
      * level first, then edits the database if privileged.
      *
      * @param income
      * @param loggedInUser
      * @param selectedEvent
      * @param selectedCommittee
      */
    public void addIncome(Income income, User loggedInUser, Event selectedEvent, Committee selectedCommittee) {
         if (PrivilegeManager.hasBudgetPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedBudget.getIncomeList().add(income);
             // write to database
         }
     }
 
     /**
      * Remove an income item from the selected budget. Verifies the user's
      * privilege level first, then edits the database if privileged.
      *
      * @param income
      * @param loggedInUser
      * @param selectedEvent
      * @param selectedCommittee
      */
    public void removeIncome(Income income, User loggedInUser, Event selectedEvent, Committee selectedCommittee) {
         if (PrivilegeManager.hasBudgetPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedBudget.getIncomeList().remove(income);
             // write to database
             // remove all related database entries
         }
     }
 
     /**
      * Add an expense to the selected budget. Verifies the user's privilege
      * level first, then edits the database if privileged.
      *
      * @param expense the expense item to add
      * @param loggedInUser the currently logged in user
      * @param selectedEvent the selected event
      * @param selectedCommittee the selected committee
      */
    public void addExpense(Expense expense, User loggedInUser, Event selectedEvent, Committee selectedCommittee) {
         if (PrivilegeManager.hasBudgetPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedBudget.getExpenseList().add(expense);
             // write to database
         }
     }
 
     /**
      * Remove an expense item from the selected budget. Verifies the user's
      * privilege level first, then edits the database if privileged.
      *
      * @param expense
      * @param loggedInUser
      * @param selectedEvent
      * @param selectedCommittee
      */
    public void removeExpense(Expense expense, User loggedInUser, Event selectedEvent, Committee selectedCommittee) {
         if (PrivilegeManager.hasBudgetPrivilege(loggedInUser, selectedEvent, selectedCommittee)) {
             selectedBudget.getExpenseList().remove(expense);
             // write to database
             // remove all related database entries
         }
     }
 }

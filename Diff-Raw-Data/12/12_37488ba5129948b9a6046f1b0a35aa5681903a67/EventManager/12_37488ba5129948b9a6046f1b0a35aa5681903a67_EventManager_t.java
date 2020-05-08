 package BackEnd.ManagerSystem;
 
 import BackEnd.EventSystem.*;
 import BackEnd.UserSystem.*;
 import EMS_Database.*;
 import EMS_Database.impl.*;
 
 import java.util.ArrayList;
 import java.sql.Timestamp;
 
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
     private Events_Table eventsTable;
     private UserData_Table usersTable;
     private SubEvent_Table subEventsTable;
     private Committees_Table committeesTable;
     private Tasks_Table tasksTable;
     private Income_Table incomeTable;
     private Expense_Table expenseTable;
 
     public EventManager(ArrayList<Participant> userList, UserData_Table usersTable, Tasks_Table tasksTable,
             SubEvent_Table subEventsTable, Committees_Table committeesTable,
             Income_Table incomeTable, Expense_Table expenseTable) // FIGURE OUT HOW TO HANDLE EXCEPTION
             throws DoesNotExistException {
         eventList = new ArrayList<Event>();
        
         eventsTable = new Events_Table();
         this.usersTable = usersTable;
         this.subEventsTable = subEventsTable;
         this.tasksTable = tasksTable;
         this.committeesTable = committeesTable;
         this.incomeTable = incomeTable;
         this.expenseTable = expenseTable;
        rebuildEventList(userList);
 
         setSelectedEvent(eventList.get(0));
     }
 
     private void rebuildEventList(ArrayList<Participant> userList) // FIGURE OUT HOW TO HANDLE EXCEPTION
             throws DoesNotExistException {
         ArrayList<Integer> eventIDList = eventsTable.currentUIDList();
         for (Integer eventID : eventIDList) {
             eventList.add(rebuildEvent(eventID, userList));
         }
     }
 
     private Event rebuildEvent(int eventID, ArrayList<Participant> userList) // FIGURE OUT HOW TO HANDLE EXCEPTION
             throws DoesNotExistException {
         Event event = new Event(eventID, eventsTable.getDescription(eventID));
 
         event.setSubEventList(rebuildSubEventList(eventID));
         event.setOrganizerList(rebuildOrganizerList(eventID, userList));
         event.setParticipantList(rebuildParticipantList(eventID, userList));
         event.setCommitteeList(rebuildCommitteeList(eventID, userList));
 
         event.setLocation(new Location(eventsTable.getStreet(eventID), eventsTable.getCity(eventID),
                 eventsTable.getState(eventID), eventsTable.getZipcode(eventID), eventsTable.getCountry(eventID),
                 eventsTable.getDetails(eventID)
                 ));
         event.getTimeSchedule().setStartDateTime(eventsTable.getStartDate(eventID));
         event.getTimeSchedule().setEndDateTime(eventsTable.getEndDate(eventID));
 
         return event;
     }
 
     private ArrayList<SubEvent> rebuildSubEventList(int eventID)
             throws DoesNotExistException {
         ArrayList<SubEvent> subEventList = new ArrayList<SubEvent>();
         for (Integer subEventID : eventsTable.getSubEventList(eventID)) {
             subEventList.add(rebuildSubEvent(subEventID));
         }
         return subEventList;
     }
 
     private SubEvent rebuildSubEvent(int subEventID)
             throws DoesNotExistException {
         SubEvent subEvent = new SubEvent(subEventID, subEventsTable.getDescription(subEventID));
         subEvent.setLocation(new Location(subEventsTable.getStreet(subEventID), subEventsTable.getCity(subEventID),
                 subEventsTable.getState(subEventID), subEventsTable.getZipcode(subEventID), subEventsTable.getCountry(subEventID),
                 subEventsTable.getDetails(subEventID)
                 ));
         subEvent.getTimeSchedule().setStartDateTime(subEventsTable.getStartDate(subEventID));
         subEvent.getTimeSchedule().setEndDateTime(subEventsTable.getEndDate(subEventID));
 
         return subEvent;
     }
 
     private ArrayList<User> rebuildOrganizerList(int eventID, ArrayList<Participant> userList)
             throws DoesNotExistException {
         ArrayList<User> organizerList = new ArrayList<User>();
         ArrayList<Integer> organizerIDList = eventsTable.getOrganizerList(eventID);
 
         for (Participant user : userList) {
             if (organizerIDList.contains(user.getUserId())) {
                 organizerList.add((User) user);
             }
         }
         return organizerList;
     }
 
     private ArrayList<Participant> rebuildParticipantList(int eventID, ArrayList<Participant> userList)
             throws DoesNotExistException {
         ArrayList<Participant> participantList = new ArrayList<Participant>();
         ArrayList<Integer> participantIDList = eventsTable.getParticipantList(eventID);
         for (Participant participant : userList) {
             if (participantIDList.contains(participant.getUserId())) {
                 participantList.add(participant);
             }
         }
         return participantList;
     }
 
     private ArrayList<Committee> rebuildCommitteeList(int eventID, ArrayList<Participant> userList)
             throws DoesNotExistException {
         ArrayList<Committee> committeeList = new ArrayList<Committee>();
         for (Integer committeeID : eventsTable.getCommittee(eventID)) {
             committeeList.add(rebuildCommittee(committeeID, userList));
         }
         return committeeList;
     }
 
     private Committee rebuildCommittee(int committeeID, ArrayList<Participant> userList)
             throws DoesNotExistException {
         Committee committee = new Committee(committeeID, committeesTable.getTitle(committeeID));
 
         committee.setMemberList(rebuildCommitteeMemberList(committeeID, userList));
         committee.setBudgetAccessList(rebuildBudgetAccessList(committeeID, userList));
         committee.setChair(usersTable.getUser(committeesTable.getChairman(committeeID)));
         committee.setTaskList(rebuildTaskList(committeeID, userList));
 
         Budget newBudget = new Budget();
         newBudget.setIncomeList(rebuildIncomeList(committeeID));
         newBudget.setExpenseList(rebuildExpenseList(committeeID));
         committee.setBudget(newBudget);
         return committee;
     }
 
     private ArrayList<User> rebuildCommitteeMemberList(int committeeID, ArrayList<Participant> userList)
             throws DoesNotExistException {
         ArrayList<User> memberList = new ArrayList<User>();
         ArrayList<Integer> memberIDList = committeesTable.getCommitteeMembers(committeeID);
 
         for (Participant user : userList) {
             if (memberIDList.contains(user.getUserId())) {
                 memberList.add((User) user);
             }
         }
         return memberList;
     }
 
     private ArrayList<User> rebuildBudgetAccessList(int committeeID, ArrayList<Participant> userList)
             throws DoesNotExistException {
         ArrayList<User> budgetAccessList = new ArrayList<User>();
         ArrayList<Integer> budgetAccessIDList = committeesTable.getBudgetAccessList(committeeID);
 
         for (Participant user : userList) {
             if (budgetAccessIDList.contains(user.getUserId())) {
                 budgetAccessList.add((User) user);
             }
         }
         return budgetAccessList;
     }
 
     private ArrayList<Task> rebuildTaskList(int committeeID, ArrayList<Participant> userList)
             throws DoesNotExistException {
         ArrayList<Task> taskList = new ArrayList<Task>();
         for (Integer taskID : committeesTable.getTaskList(committeeID)) {
             taskList.add(rebuildTask(taskID, userList));
         }
         return taskList;
 
     }
 
     private Task rebuildTask(int taskID, ArrayList<Participant> userList)
             throws DoesNotExistException {
         Task task = new Task(taskID, tasksTable.getDescription(taskID));
         task.setLocation(new Location(tasksTable.getStreet(taskID), tasksTable.getCity(taskID),
                 tasksTable.getState(taskID), tasksTable.getZipcode(taskID), tasksTable.getCountry(taskID),
                 tasksTable.getDetails(taskID)
                 ));
         task.getTimeSchedule().setStartDateTime(tasksTable.getStartDate(taskID));
         task.getTimeSchedule().setEndDateTime(tasksTable.getEndDate(taskID));
 
         task.setResponsibleList(rebuildResponsibleList(taskID, userList));
         task.setCompleted(tasksTable.getComplete(taskID) == 1 ? true : false);
 
         return task;
     }
 
     private ArrayList<User> rebuildResponsibleList(int taskID, ArrayList<Participant> userList)
             throws DoesNotExistException {
         ArrayList<User> responsibleList = new ArrayList<User>();
         ArrayList<Integer> responsibleIDList = tasksTable.getAuthority(taskID);
 
         for (Participant user : userList) {
             if (responsibleIDList.contains(user.getUserId())) {
                 responsibleList.add((User) user);
             }
         }
         return responsibleList;
     }
     
     private ArrayList<Income> rebuildIncomeList(int committeeID) throws DoesNotExistException{
         ArrayList<Income> incomeList = new ArrayList<Income>();
         ArrayList<Integer> incomeIDList = committeesTable.getIncome(committeeID);
         
         for (Integer incomeID : incomeIDList){
             incomeList.add(rebuildIncome(incomeID));
         }
         return incomeList;
     }
 
     private Income rebuildIncome(int incomeID) throws DoesNotExistException{
         Income income = new Income(incomeID, incomeTable.getValue(incomeID), incomeTable.getDescription(incomeID));
         return income;
     }
     
     private ArrayList<Expense> rebuildExpenseList(int committeeID) throws DoesNotExistException{
         
         ArrayList<Expense> expenseList = new ArrayList<Expense>();
         ArrayList<Integer> expenseIDList = committeesTable.getExpense(committeeID);
         
         for (Integer expenseID : expenseIDList){
             expenseList.add(rebuildExpense(expenseID));
         }
         return expenseList;
         
     }
     
     private Expense rebuildExpense(int expenseID) throws DoesNotExistException{
         Expense expense = new Expense(expenseID, expenseTable.getValue(expenseID), expenseTable.getDescription(expenseID));
         return expense;
     }
 
     // method may be finished in future version
     public ArrayList<Event> getEventList() {
         return eventList;
     }
 
     public void setSelectedEvent(Event selectedEvent) {
         this.selectedEvent = selectedEvent;
     }
 
     public Event getSelectedEvent() {
         return selectedEvent;
     }
 
     public void createEvent(Event newEvent, User loggedInUser)
             throws PrivilegeInsufficientException, DuplicateInsertionException {
         if (PrivilegeManager.hasEventCreationPrivilege(loggedInUser)) {
             // write to database
 
             ArrayList<Integer> organizerIDList, subEventIDList, participantIDList, committeeIDList;
             organizerIDList = new ArrayList<Integer>();
             organizerIDList.add(loggedInUser.getUserId());
             subEventIDList = new ArrayList<Integer>();
             participantIDList = new ArrayList<Integer>();
             committeeIDList = new ArrayList<Integer>();
             
             Event event = new Event(eventsTable.createEvent(new InputEventData(newEvent.getDescription(), newEvent.getLocation().getDetails(),
                     "test", newEvent.getTimeSchedule().getStartDateTimeTimestamp(), newEvent.getTimeSchedule().getEndDateTimeTimestamp(),
                     0, committeeIDList, organizerIDList, subEventIDList, participantIDList,
                     newEvent.getLocation().getStreet(), newEvent.getLocation().getCity(),
                     newEvent.getLocation().getState(), newEvent.getLocation().getZipCode(),
                     newEvent.getLocation().getCountry()
                     )), newEvent);
             eventList.add(event);
             selectedEvent = event;
         }
     }
 
     public void clearEvent(User loggedInUser)
             throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
 
             // remove the event from the database
             for (int i = 0; i < selectedEvent.getCommitteeList().size(); i++) {
                 for (int j = 0; j < selectedEvent.getCommitteeList().get(i).getTaskList().size(); j++) {
                     tasksTable.removeTask(selectedEvent.getCommitteeList().get(i).getTaskList().get(j).getTASK_ID());
                     selectedEvent.getCommitteeList().get(i).getTaskList().remove(j);
                 }
                 for (int k = 0; k < selectedEvent.getCommitteeList().get(i).getBudget().getIncomeList().size(); k++) {
                     incomeTable.removeBudgetItem(selectedEvent.getCommitteeList().get(i).getBudget().getIncomeList().get(k).getBUDGET_ITEM_ID());
                     selectedEvent.getCommitteeList().get(i).getBudget().getIncomeList().remove(k);
                 }
                 for (int l = 0; l < selectedEvent.getCommitteeList().get(i).getBudget().getExpenseList().size(); l++) {
                     expenseTable.removeBudgetItem(selectedEvent.getCommitteeList().get(i).getBudget().getExpenseList().get(l).getBUDGET_ITEM_ID());
                     selectedEvent.getCommitteeList().get(i).getBudget().getExpenseList().remove(l);
                 }
                 selectedEvent.getCommitteeList().get(i).setBudget(null);
                 committeesTable.removeCommittee(selectedEvent.getCommitteeList().get(i).getCOMMITTEE_ID());
                 selectedEvent.getCommitteeList().remove(i);
             }
             for (int m = 0; m < selectedEvent.getSubEventList().size(); m++) {
                 subEventsTable.removeSubEvent(selectedEvent.getSubEventList().get(m).getSUB_EVENT_ID());
                 selectedEvent.getSubEventList().remove(m);
             }
 
             for (int n = 0; n < selectedEvent.getParticipantList().size(); n++) {
                 if (!(selectedEvent.getParticipantList().get(n) instanceof User)) {
                     usersTable.removeUser(selectedEvent.getParticipantList().get(n).getUserId());
                 }
                 selectedEvent.getParticipantList().remove(n);
             }
             eventsTable.removeEvent(selectedEvent.getEVENT_ID());
             eventList.remove(selectedEvent.getEVENT_ID());
             selectedEvent = null;
         }
     }
 
     public void addOrganizer(User organizer, User loggedInUser) throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.getOrganizerList().add(organizer);
             // write to database
 
             Integer organizerUserID = new Integer(usersTable.getUIDByEmail(organizer.getEmailAddress()));
             ArrayList<Integer> newOrganizerList = eventsTable.getOrganizerList(selectedEvent.getEVENT_ID());
             newOrganizerList.add(organizerUserID);
             eventsTable.setOrganizerList(selectedEvent.getEVENT_ID(), newOrganizerList);
         }
     }
 
     public void removeOrganizer(User organizer, User loggedInUser) throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.getOrganizerList().remove(organizer);
             // write to database
 
             Integer organizerUserID = new Integer(usersTable.getUIDByEmail(organizer.getEmailAddress()));
             ArrayList<Integer> newOrganizerList =
                     eventsTable.getOrganizerList(selectedEvent.getEVENT_ID());
             newOrganizerList.remove(organizerUserID);
             eventsTable.setOrganizerList(selectedEvent.getEVENT_ID(),
                     newOrganizerList);
 
         }
     }
 
     public void addSubEvent(SubEvent subEvent, User loggedInUser) throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.getSubEventList().add(subEvent);
             // write to database
 
             Integer subEventID = new Integer(subEvent.getSUB_EVENT_ID());
             ArrayList<Integer> newSubEventList = eventsTable.getSubEventList(selectedEvent.getEVENT_ID());
             newSubEventList.add(subEventID);
             eventsTable.setSubEventList(selectedEvent.getEVENT_ID(), newSubEventList);
         }
     }
 
     public void removeSubEvent(SubEvent subEvent, User loggedInUser) throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.getSubEventList().remove(subEvent);
             // write to database
 
             Integer subEventID = new Integer(subEvent.getSUB_EVENT_ID());
             ArrayList<Integer> newSubEventList = eventsTable.getSubEventList(selectedEvent.getEVENT_ID());
             newSubEventList.remove(subEventID);
             eventsTable.setSubEventList(selectedEvent.getEVENT_ID(), newSubEventList);
 
             // remove all related database entries
 
         }
     }
 
     public void addCommittee(Committee committee, User loggedInUser) throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.getCommitteeList().add(committee);
 
             // write to database
 
 
             Integer committeeID = new Integer(committee.getCOMMITTEE_ID());
             ArrayList<Integer> newCommitteeList = eventsTable.getCommittee(selectedEvent.getEVENT_ID());
             newCommitteeList.add(committeeID);
             eventsTable.setCommittee(selectedEvent.getEVENT_ID(), newCommitteeList);
 
         }
     }
 
     public void removeCommittee(Committee committee, User loggedInUser) throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.getCommitteeList().remove(committee);
             // write to database
 
 
             Integer committeeID = new Integer(committee.getCOMMITTEE_ID());
             ArrayList<Integer> newCommitteeList = eventsTable.getCommittee(selectedEvent.getEVENT_ID());
             newCommitteeList.remove(committeeID);
             eventsTable.setCommittee(selectedEvent.getEVENT_ID(), newCommitteeList);
 
             // remove all related database entries
 
 
         }
     }
 
     public void addParticipant(Participant participant) throws DoesNotExistException {
         selectedEvent.getParticipantList().add(participant);
         // write to database
 
 
         Integer participantID = new Integer(participant.getUserId());
         ArrayList<Integer> newParticipantList = eventsTable.getParticipantList(selectedEvent.getEVENT_ID());
         newParticipantList.add(participantID);
         eventsTable.setParticipantList(selectedEvent.getEVENT_ID(), newParticipantList);
 
     }
 
     public void removeParticipant(Participant participant) throws DoesNotExistException {
         selectedEvent.getParticipantList().remove(participant);
         // write to database
 
 
         Integer participantID = new Integer(participant.getUserId());
         ArrayList<Integer> newParticipantList = eventsTable.getParticipantList(selectedEvent.getEVENT_ID());
         newParticipantList.remove(participantID);
         eventsTable.setParticipantList(selectedEvent.getEVENT_ID(), newParticipantList);
 
     }
 
     public void editDescription(String description, User loggedInUser) throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.setDescription(description);
             // write to database
 
 
             eventsTable.setDescription(selectedEvent.getEVENT_ID(), description);
 
         }
     }
 
     public void editLocation(Location location, User loggedInUser) throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.setLocation(location);
             // write to database
 
 
             eventsTable.setDetails(selectedEvent.getEVENT_ID(), location.getDetails());
             eventsTable.setStreet(selectedEvent.getEVENT_ID(), location.getStreet());
             eventsTable.setCity(selectedEvent.getEVENT_ID(), location.getCity());
             eventsTable.setState(selectedEvent.getEVENT_ID(), location.getState());
             eventsTable.setZipcode(selectedEvent.getEVENT_ID(), location.getZipCode());
             eventsTable.setCountry(selectedEvent.getEVENT_ID(), location.getCountry());
 
         }
     }
 
     public void editStartDateTime(int year, int month, int day, int hour, int minute, User loggedInUser)
             throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.getTimeSchedule().setStartDateTime(year, month, day, hour, minute);
             // write to database
 
 
             eventsTable.setStartDate(selectedEvent.getEVENT_ID(), selectedEvent.getTimeSchedule().getStartDateTimeTimestamp());
 
         }
     }
 
     public void editEndDateTime(int year, int month, int day, int hour, int minute, User loggedInUser)
             throws PrivilegeInsufficientException, DoesNotExistException {
         if (PrivilegeManager.hasEventPrivilege(loggedInUser, selectedEvent)) {
             selectedEvent.getTimeSchedule().setEndDateTime(year, month, day, hour, minute);
             // write to database
 
 
             eventsTable.setEndDate(selectedEvent.getEVENT_ID(), selectedEvent.getTimeSchedule().getEndDateTimeTimestamp());
 
         }
     }
 }

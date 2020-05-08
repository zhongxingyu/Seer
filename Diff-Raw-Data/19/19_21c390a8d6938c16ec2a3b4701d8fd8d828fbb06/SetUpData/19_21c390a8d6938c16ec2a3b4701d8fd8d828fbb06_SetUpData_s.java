 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package GUI;
 
 import BackEnd.EventSystem.Budget;
 import BackEnd.EventSystem.Committee;
 import BackEnd.EventSystem.Event;
 import BackEnd.EventSystem.Income;
 import BackEnd.EventSystem.SubEvent;
 import BackEnd.EventSystem.Task;
 import BackEnd.EventSystem.TimeSchedule;
 import BackEnd.ManagerSystem.MainManager;
 import BackEnd.UserSystem.Address;
 import BackEnd.UserSystem.IllegalCharacterException;
 import BackEnd.UserSystem.PasswordMismatchError;
 import BackEnd.UserSystem.PhoneNumber;
 import BackEnd.UserSystem.User;
 import EMS_Database.impl.Committees_Table;
 import EMS_Database.impl.Events_Table;
 import EMS_Database.impl.SubEvent_Table;
 import EMS_Database.impl.Tasks_Table;
 import EMS_Database.impl.UserData_Table;
 import java.util.ArrayList;
 
 /**
  *
  * @author Sid
  */
 public class SetUpData {
     public static void main(String[] args) throws PasswordMismatchError, IllegalCharacterException
     {
         System.out.println("DELETING EVENTS");
         Events_Table edt = new Events_Table();
         edt.removeAll("EVENTS");
         
         System.out.println("DELETING USERS");
         UserData_Table udt = new UserData_Table();
         udt.removeAll("USERS");
         
         System.out.println("DELETING COMMITTEES");
         Committees_Table ct = new Committees_Table();
         ct.removeAll("COMMITTEE");
         
         System.out.println("DELETINGSUBEVENTS");
         SubEvent_Table set = new SubEvent_Table();
         set.removeAll("SUBEVENTS");
         
         System.out.println("DELETING TASKS");
         Tasks_Table tt = new Tasks_Table();
         tt.removeAll("TASKS");
         
         //Create Event
         Event e = new Event(0,"SetupTest1");
         e.setDescription("I'm an event.");
         //e.setLocation(new Location());
         //Create admin user
         User u = new User("Alpha","Bravo","AB@AB.com","ab","ab");
         Address addr = new Address("Street","City", "STATE","00000","COUNTRY");
         PhoneNumber num = new PhoneNumber("5555555555");
         u.setAddress(addr);
         u.setPhoneNumber(num);
         u.setAdminPrivilege(true); 
         u.setEventCreationPrivilege(true);
         
         //Start Instance
         MainManager manager = MainManager.getInstance();
         
         //Try to create Event
         try
         {
             manager.getEventManager().createEvent(e,u);
             manager.getUserManager().setSelectedUser(manager.getUserManager().createUser(u));
             manager.getLogInManager().setLoggedInUser("AB@AB.com", "ab");
             manager.getEventManager().addOrganizer(manager.getUserManager().getSelectedUser(), manager.getLogInManager().getLoggedInUser());
             System.out.println("Event Created.");
             System.out.println("Admin Created: " + u);
         }
         catch (Exception ex1)
         {
             System.out.println("Can't create event.");
             ex1.printStackTrace();
         }
         
         //Create users
         ArrayList<User> uList = new ArrayList<User>();
         User u2 = new User("Bravo","Charlie","BC@BC.com","bc","bc");
         uList.add(u2);
         User u3 = new User("Charlie","Delta","CD@CD.com","cd","cd");
         uList.add(u3);
         User u4 = new User("Delta","Echo","DE@DE.com","de","de");
         uList.add(u4);
         uList.add(new User("Echo","Foxtrot","EF@EF.com","ef","ef"));
         uList.add(new User("Foxtrot","Golf","FG@FG.com","fg","fg"));
         uList.add(new User("Golf","Hotel","GH@GH.com","gh","gh"));
         uList.add(new User("Hotel","India","HI@HI.com","hi","hi"));
         uList.add(new User("India","Juliett","IJ@IJ.com","ij","ij"));
         uList.add(new User("Juliett","Kilo","JK@JK.com","jk","jk"));
         
         //add users
         try
         {
             for(User tu : uList)
             {
                 manager.getUserManager().setSelectedUser((manager.getUserManager().createUser(tu)));
                 manager.getEventManager().addOrganizer(manager.getUserManager().getSelectedUser(), manager.getLogInManager().getLoggedInUser());
                 System.out.println(manager.getLogInManager().getLoggedInUser());
             }
             System.out.println("Users created.");
         }
         catch (Exception ex2)
         {
             System.out.println("Can't create Users.");
             ex2.printStackTrace();
         }
         
         
         //create committees
         ArrayList<Committee> list = new ArrayList<Committee>();
         Committee c = new Committee();
         c.setTitle("Committee #1");
         list.add(c);
         Committee c2 = new Committee();
         c2.setTitle("Committee #2");
         list.add(c2);
         Committee c3 = new Committee();
         c3.setTitle("Committee #3");
         list.add(c3);
         //add committees
         try
         {
             for(Committee tc : list)
                 {
                     manager.getCommitteeManager().setSelectedCommittee(manager.getEventManager().createCommittee(tc, manager.getLogInManager().getLoggedInUser()));
                     manager.getCommitteeManager().addMember(manager.getUserManager().getSelectedUser(), manager.getUserManager().getSelectedUser(), manager.getEventManager().getSelectedEvent());
                     //manager.getCommitteeManager().editChair(manager.g, manager.getUserManager().getSelectedUser(), manager.getEventManager().getSelectedEvent());
                     for(int i = 1; i < 4; i++)
                     {
                         Task t = new Task();
                         manager.getTaskManager().setSelectedTask(manager.getCommitteeManager().createTask(t, manager.getLogInManager().getLoggedInUser(), manager.getEventManager().getSelectedEvent()));
                        manager.getTaskManager().editDescription("Task " + i, manager.getLogInManager().getLoggedInUser(), manager.getEventManager().getSelectedEvent(), manager.getCommitteeManager().getSelectedCommittee());
                     }
                     for(int i = 1; i < 4; i++)
                     {
                         Budget b = manager.getCommitteeManager().getSelectedCommittee().getBudget();
                         manager.getBudgetManager().setSelectedBudget(b);
                         manager.getBudgetItemManager().setSelectedBudgetItem(manager.getBudgetManager().createIncome(new Income(), manager.getLogInManager().getLoggedInUser(), manager.getEventManager().getSelectedEvent(), manager.getCommitteeManager().getSelectedCommittee()));
                         manager.getBudgetItemManager().editDescription("test"+i, manager.getLogInManager().getLoggedInUser(), manager.getEventManager().getSelectedEvent(), manager.getCommitteeManager().getSelectedCommittee());
                     }
                 }
             System.out.println("Committees created.");
         }
         catch (Exception ex3)
         {
             System.out.println("Can't add committees.");
             ex3.printStackTrace();
         }
         
         //create subEvents
         ArrayList<SubEvent> sList = new ArrayList<SubEvent>();
         SubEvent se1 = new SubEvent("SE - Test 1");
         TimeSchedule ts1 = new TimeSchedule();
         ts1.setStartDateTime(2013,5,7,5,30);
         ts1.setStartDateTime(2013,5,7,8,00);
         se1.setTimeSchedule(ts1);
         sList.add(se1);
         SubEvent se2 = new SubEvent("SE - Test 2");
         TimeSchedule ts2 = new TimeSchedule();
         ts1.setStartDateTime(2013,5,8,4,30);
         ts1.setStartDateTime(2013,5,8,9,30);
         se2.setTimeSchedule(ts2);
         sList.add(se2);
         SubEvent se3 = new SubEvent("SE - Test 3");
         TimeSchedule ts3 = new TimeSchedule();
         ts3.setStartDateTime(2013,5,10,8,00);
         ts3.setStartDateTime(2013,5,10,11,00);
         se3.setTimeSchedule(ts3);
         sList.add(se3);
         SubEvent se4 = new SubEvent("SE - Test 4");
         TimeSchedule ts4 = new TimeSchedule();
         ts4.setStartDateTime(2013,5,10,4,30);
         ts4.setStartDateTime(2013,5,10,8,30);
         se4.setTimeSchedule(ts4);
         sList.add(se4);
         SubEvent se5 = new SubEvent("SE - Test 5");
         TimeSchedule ts5 = new TimeSchedule();
         ts5.setStartDateTime(2013,5,12,16,30);
         ts5.setStartDateTime(2013,5,12,18,30);
         se5.setTimeSchedule(ts5);
         sList.add(se5);
         SubEvent se6 = new SubEvent("SE - Test 6");
         TimeSchedule ts6 = new TimeSchedule();
         ts6.setStartDateTime(2013,5,21,8,00);
         ts6.setStartDateTime(2013,5,21,11,00);
         se6.setTimeSchedule(ts6);
         sList.add(se6);  
         try
         {
             for(SubEvent ts : sList)
             {
                 manager.getSubEventManager().setSelectedSubEvent(manager.getEventManager().createSubEvent(ts, u));
             }
             System.out.println("SubEvents created.");
         }
         catch (Exception ex4)
         {
             System.out.println("Can't create SubEvents.");
             ex4.printStackTrace();
         }
         
     }
     
 }

 package controllers;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import play.*;
 import play.jobs.*;
 import play.test.*;
  
 import models.*;
  
 @OnApplicationStart
 public class Bootstrap extends Job {
  
 	public Database db;
 	
     public void doJob() {
 
     	db = Database.getInstance();
         if(db.getUsers().isEmpty()) {
         	User testUser = new User("Johnny", "J");
         	User testUser2 = new User("Jack", "Ja");
         	Calendar testCalendar = testUser.createCalendar("Hausaufgaben");
         	Calendar testCal2 = testUser.createCalendar("Stuff");
        	Calendar testCalJack = testUser2.createCalendar("Jack's stuff");
         	Event event1, event2, event3, event4;
     		event1 = new Event("event1 private", "1990-01-17 10:00", "1990-01-17 13:00", true);
 			event2 = new Event("event2 public", "2012-01-17 00:00", "2012-01-18 00:00", false);
 			testCalendar.addEvent(event1);
 			testCalendar.addEvent(event2);
 			event3 = new Event("event3 private", "2011-03-17 10:00", "2011-03-17 14:00", true);
 			event4 = new Event("event4 public", "2012-06-17 00:00", "2012-06-18 12:00", false);
			testCalJack.addEvent(event3);
			testCalJack.addEvent(event4);
             db.addUser(testUser);
             db.addUser(testUser2);
         }
 
     }
  
 }

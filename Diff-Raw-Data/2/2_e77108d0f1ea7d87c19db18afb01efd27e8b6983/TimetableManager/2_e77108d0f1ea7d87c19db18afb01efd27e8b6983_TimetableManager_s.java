 package managers;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import javafiles.Event;
 
 public class TimetableManager {
 
     public TimetableManager()
     {
 
     }
     
     /**
      * Outputs a timetable for a week for a given user
      * Times will be stored in the db as integers eg period 1,2,3 etc
      * @param user
      * @throws FileNotFoundException
      * @throws IOException 
      */
     public String outputTimetable(String user, Calendar cal) throws FileNotFoundException, IOException
     {
         DBManager db = new DBManager();
         ArrayList<Event> events = db.getEventsForUser(user);
         
         String output = "";
         
         output += ("<div id=\"calendar\">");					
         output += ("    <div id=\"calcontainer\">");
         output += ("        <div id=\"calheader\">");
         output += ("            <h2>" + cal.MONTH + " " + cal.YEAR + "</h2>");
         output += ("        </div>		");
         output += ("        <div id=\"daysweek\">");
         output += ("            <div class=\"dayweek\"><p>Monday</p></div>");
         output += ("            <div class=\"dayweek\"><p>Tuesday</p></div>");
         output += ("            <div class=\"dayweek\"><p>Wednesday</p></div>");
         output += ("            <div class=\"dayweek\"><p>Thursday</p></div>");
         output += ("            <div class=\"dayweek\"><p>Friday</p></div>");
         output += ("            <div class=\"dayweek\"><p>Saturday</p></div>");
         output += ("            <div class=\"dayweek brn\"><p>Sunday</p></div>");
         output += ("        </div>");
         output += ("        <div id=\"daysmonth\">");
         
         int currentDay = 1;//first monday
         
         for(int i=0; i<5; i++) {//for 5 weeks
             output += "<div class= \"week\">";
             for(int j = 0; j < 7; j++) { //7 days
                ArrayList<Event> todaysEvents = null;
                 for(Event e : events)
                 {
                     if(e.getStartDateDay() == currentDay && e.getStartDateMonth() == cal.MONTH && e.getStartDateYear() ==  cal.YEAR)
                     {
                         todaysEvents.add(e);
                     }
                 }
                 
                 if(currentDay % 7 == 0)
                 {
                     output += outputDayBrn(currentDay, todaysEvents);
                 } else {
                     output += outputDay(currentDay, todaysEvents);
                 }
                 currentDay++;
             }
             output += ("    </div>");
         }
         
         output += ("                </div>");
         output += ("    </div>");			
         output += ("    <div id=\"calcat\">");
         output += ("        <div class=\"caldot blue\"></div><p>Lecture</p>");
         output += ("        <div class=\"caldot yellow\"></div><p>Tutorial</p>");
         output += ("        <div class=\"caldot green\"></div><p>Student Meeting</p>");
         output += ("        <div class=\"caldot red\"></div><p>Lecturer Meeting</p>");
         output += ("    </div>");
         output += ("</div>");
         output += ("</div>");
         
         return output;
         
     }
     
     private String outputDay(int dayNumber, ArrayList<Event> eventsForToday ) {
                         String output = "";
                         output += ("<div class=\"day\">");
                         output += ("    <div class=\"daybar\"><p>" + dayNumber + "</p></div>");
                         output += ("    <div class=\"dots\">");
                         output += ("        <ul>");
                         //******************************************************************
                         
                         //output += ("            <li class=\"yellow\"></li>"); TODO: need to add entry to 
                         //output += ("            <li class=\"green\"></li>");        db for types of events
                         
                         //***************************************************************88
                         output += ("        </ul>");
                         output += ("    </div>	");
                         output += ("    <!-- slide open -->");
                         output += ("    <div class=\"open\">");
                         output += ("        <ul>");
                         int currEventStart = 0;
                         
                         for(Event e: eventsForToday) {
                             int duration = e.getEndTime() - e.getStartTime();
                             int startTime = e.getStartTime() - currEventStart - duration;
                             output += ("            <li class=\"yellow l" + duration + " a" + startTime + " \"><p>" 
                                                                 + e.getStartTime() + ":00 " + e.getDescription() + "</p></li>");
                             currEventStart = e.getStartTime();
                             
                         }				
                         output += ("        </ul>");
                         output += ("    </div>	");
                         output += ("    <!-- slide closed -->");
                         output += ("</div>		");
                         
                  return output;
     }
     
     private String outputDayBrn(int dayNumber, ArrayList<Event> eventsForToday) {
                         String output = "";
                         output += ("<div class=\"day brn\">");
                         output += ("    <div class=\"daybar\"><p>" + dayNumber + "</p></div>");
                         output += ("    <div class=\"dots\">");
                         output += ("        <ul>");
                         //******************************************************************
                         
                         //output += ("            <li class=\"yellow\"></li>"); TODO: need to add entry to 
                         //output += ("            <li class=\"green\"></li>");        db for types of events
                         
                         //***************************************************************88
                         output += ("        </ul>");
                         output += ("    </div>	");
                         output += ("    <!-- slide open -->");
                         output += ("    <div class=\"open\">");
                         output += ("        <ul>");
                         int currEventStart = 0;
                         
                         for(Event e: eventsForToday) {
                             int duration = e.getEndTime() - e.getStartTime();
                             int startTime = e.getStartTime() - currEventStart - duration;
                             output += ("            <li class=\"yellow l" + duration + " a" + startTime + " \"><p>"
                                                     + e.getStartTime() + ":00 " + e.getName() + " - " + e.getDescription() + "</p></li>");
                             currEventStart = e.getStartTime();
                             
                         }				
                         output += ("        </ul>");
                         output += ("    </div>	");
                         output += ("    <!-- slide closed -->");
                         output += ("</div>		");
                         return output;
     }
 }

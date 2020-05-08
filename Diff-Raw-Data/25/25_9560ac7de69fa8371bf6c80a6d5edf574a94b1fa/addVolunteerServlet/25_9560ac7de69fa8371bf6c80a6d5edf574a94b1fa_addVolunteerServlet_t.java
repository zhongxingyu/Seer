 package com.VolunteerCoordinatorApp;
 
 import java.io.*;
 import java.net.URL;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 import javax.jdo.PersistenceManager;
 import javax.servlet.http.*;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.gdata.client.calendar.CalendarQuery;
 import com.google.gdata.client.calendar.CalendarService;
 import com.google.gdata.data.DateTime;
 import com.google.gdata.data.PlainTextConstruct;
 import com.google.gdata.data.calendar.*;
 import com.google.gdata.data.extensions.*;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 
 @SuppressWarnings("serial")
 public class addVolunteerServlet extends HttpServlet {
     public void doPost(HttpServletRequest req, HttpServletResponse resp)
     throws IOException {
         doGet(req, resp); 
     }
     
     public void doGet(HttpServletRequest req, HttpServletResponse resp)
             throws IOException {
         String date = req.getParameter("date");
         String title = req.getParameter("title");
         String name = req.getParameter("name"); 
         
         //If no user in query string, prompt to log in.
         if (name == null || name.equalsIgnoreCase("null") || name.equals(""))
         {
             String newURL = "/index.jsp?name=none";
             resp.sendRedirect( newURL );
         }
         
         URL feedUrl = new URL("https://www.google.com/calendar/feeds/default/private/full"); //&max-results=10");
 
           
         CalendarQuery myQuery = new CalendarQuery(feedUrl);
         myQuery.setStringCustomParameter("futureevents", "true"); 
            
         CalendarService myService = new CalendarService("Volunteer-Coordinator-Calendar");
         try {
             myService.setUserCredentials("rockcreekvolunteercoordinator@gmail.com", "G0covenant");
         } catch (AuthenticationException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         // Send the request and receive the response:
         CalendarEventFeed resultFeed = null;
         try {
             resultFeed = myService.query(myQuery, CalendarEventFeed.class);
         } catch (ServiceException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
           
         List<CalendarEventEntry> results = (List<CalendarEventEntry>)resultFeed.getEntries();
         for (CalendarEventEntry entry : results) {
             // Get the start time for the event 
             When time = entry.getTimes().get(0); 
             DateTime start = time.getStartTime(); 
            TimeZone estTZ =  TimeZone.getTimeZone("GMT-5");
            Date startDate = new Date(start.getValue());
            //Determine timezone offset in minutes, depending on whether or not
            //Daylight Savings Time is in effect
            if (estTZ.inDaylightTime(startDate)) { 
                start.setTzShift(-240); 
            } else {
                start.setTzShift(-300); 
            }
             
             // Concert to milliseconds to get a date object, which can be formatted easier. 
             Date entryDate = new Date(start.getValue() + 1000 * (start.getTzShift() * 60)); 
             
             String datePattern = "MM-dd-yyyy"; 
             SimpleDateFormat dateFormat = new SimpleDateFormat(datePattern);
             String startDay = dateFormat.format(entryDate);
             
             String eventTitle = new String(entry.getTitle().getPlainText());
             
             if (startDay.equals(date) && eventTitle.equals(title)) {
                 String content = entry.getPlainTextContent(); 
                
                 if (content.contains("<volunteers>")) {
                     String contentArray[] = content.split("<volunteers>");
                     StringBuffer volList = new StringBuffer(contentArray[1]);
                     //make sure the user isn't already in the list
                     if (!contentArray[1].contains(name.trim())) {
                         int end = volList.indexOf("</volunteers>");
                         volList.insert(end, name.trim() + " ; ");
                        content = contentArray[0] + "<volunteers>" + volList;
                     }
                 }
                 else {
                     content += " <volunteers> " + name + " ; </volunteers>";
                 }
                 entry.setContent(new PlainTextConstruct(content));
                 URL editUrl = new URL(entry.getEditLink().getHref());
                 try {
                     myService.update(editUrl, entry);
                 } catch (ServiceException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
                 
                 //Search for existing calendars under this user's name
                PersistenceManager pm = PMF.get().getPersistenceManager(); 

                 Key k = KeyFactory.createKey(Volunteer.class.getSimpleName(), name);
                 Volunteer v = pm.getObjectById(Volunteer.class, k);
                 
                 String usrCalUrl = v.getCalendarId();
                 URL newUrl = new URL("http://www.google.com/calendar/feeds/" 
                         + usrCalUrl + "/private/full");
                 try
                 {
                     myService.insert(newUrl, entry);
                 }
                 catch ( ServiceException e )
                 {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }
         }        
         resp.sendRedirect("/calendar.jsp?name=" + name);
     }
 }
     

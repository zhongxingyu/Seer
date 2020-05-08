 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tools.googleTools;
 
 import agenda.Agenda;
 import com.google.gdata.client.calendar.CalendarService;
 import com.google.gdata.data.Link;
 import com.google.gdata.data.PlainTextConstruct;
 import com.google.gdata.data.calendar.CalendarEntry;
 import com.google.gdata.data.calendar.CalendarFeed;
 import com.google.gdata.util.AuthenticationException;
 import com.google.gdata.util.ServiceException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author mael
  */
 public class Tools {
     
     // The base URL for a user's calendar metafeed (needs a username appended).
     private static String METAFEED_URL_BASE = 
             "https://www.google.com/calendar/feeds/";
 
     // The string to add to the user's metafeedUrl to access the allcalendars
     // feed.
     private static String ALLCALENDARS_FEED_URL_SUFFIX = 
         "/allcalendars/full";
 
     // The string to add to the user's metafeedUrl to access the owncalendars
     // feed.
     private static String OWNCALENDARS_FEED_URL_SUFFIX = 
         "/owncalendars/full";
 
     // The string to add to the user's metafeedUrl to access the owncalendars
     // feed.
     private static String ICS_URL_SUFFIX = 
         "/public/basic.ics";
     
     // The URL for the metafeed of the specified user.
     // (e.g. http://www.google.com/feeds/calendar/jdoe@gmail.com)
     private static URL metafeedUrl = null;
 
     // The URL for the allcalendars feed of the specified user.
     // (e.g. http://www.googe.com/feeds/calendar/jdoe@gmail.com/allcalendars/full)
     private static URL allcalendarsFeedUrl = null;
 
     // The URL for the owncalendars feed of the specified user.
     // (e.g. http://www.googe.com/feeds/calendar/jdoe@gmail.com/owncalendars/full)
     private static URL owncalendarsFeedUrl = null;
     
     // The URL for the owncalendars feed of the specified user.
     // (e.g. http://www.googe.com/calendar/ical/idimport.calendar.google.com/public/basic.ics)
     private static URL icsUrl = null;
     
     private static CalendarService calServ = null;
     
     /**
    * Prints the titles of calendars in the feed specified by the given URL.
    * 
    * @param service An authenticated CalendarService object.
    * @param feedUrl The URL of a calendar feed to retrieve.
    * @throws IOException If there is a problem communicating with the server.
    * @throws ServiceException If the service is unable to handle the request.
    */
   public static void printUserCalendars(URL feedUrl)
       throws IOException, ServiceException {
     // Send the request and receive the response:
     CalendarFeed resultFeed = calServ.getFeed(feedUrl, CalendarFeed.class);
     
     // Print the title of each calendar
     for (int i = 0; i < resultFeed.getEntries().size(); i++) {
       CalendarEntry entry = resultFeed.getEntries().get(i);
 
       System.out.println("\t" + entry.getTitle().getPlainText());
       
       try{
           for(Link l : entry.getLinks()){
               System.out.println(l.getHref());
           }
       }catch(Exception e){
           
       } 
     }
   }
   
  /**
    * Creates a new secondary calendar using the owncalendars feed.
    * Make sure that you are authentified before executing this method.
    * @return The newly created calendar entry.
    * @throws IOException If there is a problem communicating with the server.
    * @throws ServiceException If the service is unable to handle the request.
    */
   public static CalendarEntry createCalendar(Agenda agenda)
       throws IOException, ServiceException {
     System.out.println("Creating " + agenda.getTitle() + " calendar into the current account");
 
     // Create the calendar
     CalendarEntry calendar = new CalendarEntry();
     calendar.setTitle(new PlainTextConstruct(agenda.getTitle()));
     calendar.setSummary(new PlainTextConstruct(agenda.getSummary()));
 
     // Insert the calendar
     return calServ.insert(owncalendarsFeedUrl, calendar);
   }
     
   /**
    * Return the URL to get the list of all calendar on the google account.
    * @param login
    * @param passwd
    * @throws IOException
    * @throws ServiceException 
    * @return owncalendarsFeedUrl 
    */
   public static URL authTo(String login, String passwd) throws IOException, ServiceException{
 
         // Create necessary URL objects
         try {
           owncalendarsFeedUrl = new URL(METAFEED_URL_BASE + login + 
               OWNCALENDARS_FEED_URL_SUFFIX);
           
         } catch (MalformedURLException e) {
             // Bad URL
             System.err.println("Uh oh - you've got an invalid URL.");
             e.printStackTrace();
             return null;
         }
 
         // Create CalendarService and authenticate using ClientLogin
         calServ = new CalendarService("kissFairyCorporation-agix-1.0");
 
         try {
           calServ.setUserCredentials(login, passwd);
           return owncalendarsFeedUrl;
         } catch (AuthenticationException e) {
           // Invalid credentials
           e.printStackTrace();
           return null;
         }
         
       }
   
     /**
     * Deletes the given calendar entry.
     * Make sure that you have used authTo before this method
     * 
     * @param calendar The calendar entry to delete.
     * @throws IOException If there is a problem communicating with the server.
     * @throws ServiceException If the service is unable to handle the request.
     */
     public static void removeCalendar(Agenda agenda) throws IOException, ServiceException {
       
         System.out.println("Deleting " + agenda.getTitle() + " from the current account");
             
         CalendarFeed resultFeed = calServ.getFeed(owncalendarsFeedUrl, CalendarFeed.class);
         List<CalendarEntry> entries = resultFeed.getEntries();
         
         int i = 0;
         boolean ok = false;
         CalendarEntry entry = null;
         while(!ok && i < entries.size()){
             
             entry = entries.get(i);
             if(entry.getTitle().toString().equals(agenda.toString())){
                 ok = true;
             }
             i++;
         }
         
         if(ok)
             entry.delete();
         
     }
     
 
   
 }

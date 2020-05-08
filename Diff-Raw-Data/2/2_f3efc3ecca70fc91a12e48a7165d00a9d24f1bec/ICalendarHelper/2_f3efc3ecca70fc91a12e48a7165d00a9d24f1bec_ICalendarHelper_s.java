 package de.flower.rmt.service.mail;
 
 import de.flower.common.util.IO;
 import de.flower.rmt.model.db.entity.Venue;
 import de.flower.rmt.model.db.entity.event.Event;
 import org.joda.time.DateTime;
 
 import java.io.UnsupportedEncodingException;
 import java.text.SimpleDateFormat;
 
 /**
  * @author flowerrrr
  */
 public class ICalendarHelper {
 
     public static final String CHARSET = IO.CharacterEncoding.UTF8.toString();
 
     /**
      * 'text/calendar' has problems with non-ascii characters when sending mail attachments.
      */
     public static final String CONTENT_TYPE_MAIL = "application/octet-stream";
 
     public static final String CONTENT_TYPE_HTTP = "text/calendar; charset=" + CHARSET;
 
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
 
     private Event event;
 
     public ICalendarHelper(final Event event) {
         this.event = event;
     }
 
     public String getUid() {
         return event.getId() + ".das-tool.flower.de";
     }
 
     public String getDtstart() {
         return sdf.format(event.getDateTimeAsDate());
     }
 
     public String getDtend() {
         DateTime end = event.getDateTimeEnd();
         return sdf.format(end.toDate());
     }
 
     public String getDtstamp() {
         return sdf.format(new DateTime().toDate());
     }
 
     public String getSummary() {
         return event.getTeam().getName() + " - " + event.getSummary();
     }
 
     public String getDescription(final String eventDetails) {
         return sanitize(eventDetails);
     }
 
     public String getLocation() {
         Venue venue = event.getVenue();
         return venue == null ? "" : venue.getName();
     }
 
     public String sanitize(String in) {
         // replace newlines with literal \n
         return in.replace("\r", "").replace("\n", "\\n");
     }
 
     public static byte[] getBytes(final String iCalendar) {
         try {
             return iCalendar.getBytes(CHARSET);
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 }

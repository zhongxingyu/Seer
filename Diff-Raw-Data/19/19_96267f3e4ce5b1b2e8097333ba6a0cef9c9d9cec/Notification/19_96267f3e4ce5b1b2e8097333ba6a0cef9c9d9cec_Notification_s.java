 package uk.ac.cam.signups.models;
 
 import com.google.common.collect.ImmutableMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Map;
 
 public class Notification implements Mappable, Comparable<Notification> {
 	private String message;
 	private Calendar timestamp;
 	private int id;
 
 	private Logger logger = LoggerFactory.getLogger(Notification.class);
 	
 	public Notification(int id, String message, String timestamp) {
 		this.message = message;
 		this.id = id;
 		
 		SimpleDateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
 		Calendar dummyCal = new GregorianCalendar();
 		try {
 	    dummyCal.setTime(formatter.parse(timestamp));
     } catch (ParseException e) {
     	logger.error("Could not properly parse your notification time.");
     	logger.error(e.getMessage());
     }
 		this.timestamp = dummyCal;
 	}
 	
 	@Override
   public int compareTo(Notification not) {
 		int result = timestamp.compareTo(not.timestamp);
 		if (result == 0) {
 			return this.equals(not) ? 0 : -1;
 		} else {
 			return result * -1;
 		}
   }
 
 	@Override
   public int getId() {
 	  throw new UnsupportedOperationException();
   }
 	
 	@Override
 	public boolean equals(Object not) {
 		return this.id == ((Notification) not).id;
 	}
 
 	@Override
   public Map<String, ?> toMap() {
		SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MMMM d kk:mm");
 
 		return ImmutableMap.of(
 				"message", this.message,
 				"timestamp", formatter.format(this.timestamp.getTime()));
   }
 }

 package meshlabs.pushq;
 
import java.util.Calendar;
import java.util.GregorianCalendar;
 
 /**
  * Utility methods for transforming stored time values into other formats
  * 
  * @author matt
  *
  */
 public class TimeHelper {
 	
 	public static String asString(final int time) {
 		return ""+ TimeHelper.getHours(time) + ":" + TimeHelper.getMinutes(time);
 	}
 	
 	public static int toTime(final int hours, final int minutes) {
 		return (hours * 60) + minutes;
 	}
 	
 	public static int getHours(final int time) {
 		return time / 60;
 	}
 	
 	public static int getMinutes(final int time) {
 		return time % 60;
 	}
 	
 	/**
 	 * Returns last datetime that occurred with the given time. In unix timestamp format. 
 	 */
 	public static long lastTimestamp(final int time) {
		GregorianCalendar datetime = new GregorianCalendar();
		datetime.set(Calendar.HOUR_OF_DAY, TimeHelper.getHours(time));
		datetime.set(Calendar.MINUTE, TimeHelper.getMinutes(time));
 		
 		// If the set hour/minute puts us ahead of now, roll back a day.
		if (datetime.after(new GregorianCalendar())) {
			datetime.roll(Calendar.DAY_OF_MONTH, false);
 		}
 		
		return datetime.getTimeInMillis();
 	}
 
 }

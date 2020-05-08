 package meshlabs.pushq;
 
import org.joda.time.DateTime;
import org.joda.time.DateTimeComparator;
 
 /**
  * Utility methods for transforming stored time values into other formats
  * 
  * @author matt
  *
  */
 public class TimeHelper {
	private final static String TAG = "TimeHelper";
 	
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
		DateTime now = new DateTime();
		DateTime last = now.minuteOfDay().setCopy(time);

		DateTimeComparator comparator = DateTimeComparator.getInstance();
 		
 		// If the set hour/minute puts us ahead of now, roll back a day.
		if (comparator.compare(now, last) < 0) { 
			last = last.minusDays(1);
 		}
 		
		return last.getMillis();
 	}
 
 }

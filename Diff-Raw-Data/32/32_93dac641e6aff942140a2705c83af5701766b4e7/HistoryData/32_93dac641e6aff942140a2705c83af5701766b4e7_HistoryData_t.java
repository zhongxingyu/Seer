 /**
  * A class to save the history for a certain time with constant speed and
  * inclination.
  *
  * @version 0.2
  */
 public class HistoryData {
 	private int speed; // tenths of a mph
 	private int incline; // slope %
 	private long startTime; // ms (unix time)
 	private long endTime; // ms (unix time)
 
 
 	/**
 	 * Creates a HistoryData object with a start time of the time of the
 	 * call.
 	 *
 	 * @param sp    speed in tenths of a mile per hour
 	 * @param inc   inclination in percent
 	 */
 	public HistoryData(int sp, int inc) {
 		this(sp, inc, System.currentTimeMillis(), 0);
 	}
 	/**
 	 * Creates a HistoryData object with a start time.
 	 *
 	 * @param sp    speed in tenths of a mile per hour
 	 * @param inc   inclination in percent
 	 * @param start time in milliseconds
 	 */
 	public HistoryData(int sp, int inc, long start) {
 		this(sp, inc, start, 0);
 	}
 	/**
 	 * Creates a HistoryData object with a time spanned.
 	 *
 	 * @param sp    speed in tenths of a mile per hour
 	 * @param inc   inclination in percent
 	 * @param start start time in ms
 	 * @param end   end time in ms
 	 */
 	public HistoryData(int sp, int inc, long start, long end) {
 		speed = sp;
 		incline = inc;
 		startTime = start;
 		endTime = end;
 	}
 
 
 	/**
 	 * Sets the end time to now. If the end time is already set, doesn't
 	 * update it. If you really want to update the end time, call {@link
 	 * #setEndTime(long)}.
 	 */
 	public void setEndTime() {
		if (endTime == 0) {
 			setEndTime(System.currentTimeMillis());
 		}
 	}
 	/**
 	 * Sets the end time.
 	 *
 	 * @param ts time in milliseconds
 	 */
 	public void setEndTime(long ts) { endTime = ts; }
 
 	/**
 	 * Returns the total distance run during this object's lifetime.
 	 *
 	 * @return distance run in miles
 	 */
 	public double getDistance() {
		long end = (endTime == 0) ? endTime : System.currentTimeMillis();
 		// miles = miles/hr * ms * (s/ms * hr/s)
 		return (double)speed/10.0 * (end - startTime) / 3600000.0;
 	}
 
 	/**
 	 * Returns the lifespan of the object.
 	 *
 	 * @return time the object was alive in milliseconds
 	 */
 	public long getTime() {
		long end = (endTime == 0) ? endTime : System.currentTimeMillis();
 		return end - startTime;
 	}
 
 	/**
 	 * Returns the speed.
 	 *
 	 * @return speed running in tenths of a mile per hour
 	 */
 	public int getSpeed() { return speed; }
 
 	/**
 	 * Returns the incline of the treadmill.
 	 *
 	 * @return inclination in percent
 	 */
 	public int getIncline() { return incline; }
 
 	/**
 	 * Returns the number of calories burnt during the lifetime of the
 	 * object.
 	 *
 	 * @param runner Current user of the treadmill
 	 * @return       calories burnt
 	 */
 	// XXX: This calculation is not very good, it should be changed later
 	public int calculateCalories(User runner) {
		long end = (endTime == 0) ? endTime : System.currentTimeMillis();
 		return (int) (0.75 * runner.getWeight() * (end - startTime));
 	}
 }

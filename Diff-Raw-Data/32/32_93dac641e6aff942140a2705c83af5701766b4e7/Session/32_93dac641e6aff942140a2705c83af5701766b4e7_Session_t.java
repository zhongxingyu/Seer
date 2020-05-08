 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Stores the main variables and session data. This class is in charge of all
  * the visible "stuff". It knows what the treadmill is doing, what the
  * statistics are, and so forth.
  *
  * @version 0.3
  */
 public class Session {
 	private List<HistoryData> history;
 	private HistoryData currentValues;
 	private State state;
 	/* These are contained in currentValues
 	private int speedSetting;
 	private int inclineSetting;
 	*/
 
 	/** Doesn't do anything yet */
 	private int timeRatio;
 	/* TODO: not implemented yet
 	private Belt belt;
 	*/
 
 	/* Cache variables: these should be the sums of all the values in
 	 * this.history; to get usable data while running, add in the values in
 	 * currentValues. */
 	private long timeElapsedCache;
 	private double totalDistanceCache;
 	/* Now that I think about, caloriesBurned actually depends on the
 	 * weight, which the user might change, so let's not keep track of it.
 	private int caloriesBurnedCache;
 	*/
 
 	private User runner;
 
 	/**
 	 * State of the treadmill.
 	 */
 	public enum State {
 		RUNNING, PAUSED, STOPPED;
 	}
 
 
 
 	/**
 	 * Sets up the treadmill and puts it in a stopped state. To actually
 	 * start the treadmill, you must call {@link #start(int, int)}.
 	 *
 	 * @param user Current runner on the treadmill
 	 */
 	public Session(User user) {
 		// Pre-allocate memory for 50. Nice!
 		history = new ArrayList<HistoryData>(50);
 		timeElapsedCache = 0;
 		totalDistanceCache = 0.0;
 		timeRatio = 1;
 		state = State.STOPPED;
 		runner = user;
 	}
 
 	/**
 	 * Sets a new speed for the treadmill.
 	 *
 	 * @param sp new speed in tenths of a mile per hour
 	 */
 	public void setSpeed(int sp) {
 		int inclineSetting = currentValues.getIncline();
 		saveHistoryData();
 		currentValues = new HistoryData(sp, inclineSetting);
 	}
 	/**
 	 * Sets a new inclination for the treadmill.
 	 *
 	 * @param inc new inclination in percent
 	 */
 	public void setIncline(int inc) {
 		int speedSetting = currentValues.getSpeed();
 		saveHistoryData();
 		currentValues = new HistoryData(speedSetting, inc);
 	}
 
 
 	/**
 	 * Inclination of the treadmill.
 	 *
 	 * @return current inclination in percent
 	 */
 	public int getIncline() { return currentValues.getIncline(); }
 	/**
 	 * Speed of the treadmill.
 	 *
 	 * @return current speed in tenths of a mile per hour
 	 */
	public double getSpeed() throws UserFellOffException {
 		try {
 			return runner.getSpeed();
 		} catch (UserFellOffException e) {
 			stop();
 			throw e;
 		}
 	}
 	/**
 	 * Total distance run.
 	 *
 	 * @return distance run in miles
 	 */
 	public double getDistance() throws UserFellOffException {
 		try {
			return totalDistanceCache + currentValues.getDistance() + runner.getPosition()/5280.0;
 		} catch (UserFellOffException e) {
 			stop();
 			throw e;
 		}
 	}
 	/**
 	 * Total time spent running
 	 *
 	 * @return time run in milliseconds
 	 */
 	public long getTimeElapsed() {
 		return timeElapsedCache + currentValues.getTime();
 	}
 
 	/**
 	 * Calories burnt.
 	 *
 	 * @return       calories burnt while running
 	 */
	public int calculateCalories() {
 		int calories = currentValues.calculateCalories(runner);
 		for (HistoryData curHist : history) {
 			calories += curHist.calculateCalories(runner);
 		}
 		return calories;
 	}
 
 	/**
 	 * Stops the treadmill. Forgets all of the session data including time
 	 * elapsed, distance run, etc.
 	 */
 	public void stop() {
 		timeElapsedCache = 0;
 		totalDistanceCache = 0;
 		currentValues = null;
 		
 		if (state != State.STOPPED) {
 			// If the state is already stopped, no need to
 			// reallocate 50 new HistoryData objects
 			history = new ArrayList<HistoryData>(50);
 		}
 		state = State.STOPPED;
 	}
 	/**
 	 * Starts the treadmill. If the treadmill is currently running or
 	 * paused, this method has no effect.
 	 *
 	 * @param speed   speed in tenths of a mile per hour
 	 * @param incline inclination in percent
 	 */
 	public void start(int speed, int incline) {
 		if (state != State.STOPPED) return;
 		currentValues = new HistoryData(speed, incline);
 		state = State.RUNNING;
 	}
 	/**
 	 * Pauses the treadmill. All data are retained. If the treadmill is
 	 * already paused or stopped, this method has no effect.
 	 */
 	public void pause() {
 		if (state != State.RUNNING) return;
 		currentValues.setEndTime();
 		state = State.PAUSED;
 	}
 	/**
 	 * Resumes from a paused state. If the treadmill is already going, or
 	 * has been stopped completely, this method does nothing.
 	 */
 	public void resume() {
 		if (state != State.PAUSED) return;
 		int sp = currentValues.getSpeed();
 		int inc = currentValues.getIncline();
 		saveHistoryData();
 		currentValues = new HistoryData(sp, inc);
 	}
 
 
 	/**
 	 * Saves the history data. Call this when you want to put
 	 * <tt>currentValues</tt> into the <tt>history</tt> data store.
 	 */
 	private void saveHistoryData() {
 		currentValues.setEndTime();
 		updateCaches(currentValues.getTime(), currentValues.getDistance());
 		history.add(currentValues);
 	}
 	/**
 	 * Updates the time and distance caches. Should be called every time a
 	 * {@link HistoryData} object is added to the <tt>history</tt> data
 	 * store. If you forget to call this method, {@link
	 * #calculateCalories()} will be off.
 	 *
	 * <p>Note that {@link #saveHistoryData()} calls this method, so if you
 	 * operate through it, you will be A-OK.
 	 *
 	 * @param timeToAdd additional time run in milliseconds
 	 * @param distToAdd additional distance in miles
 	 */
 	private void updateCaches(long timeToAdd, double distToAdd) {
 		timeElapsedCache += timeToAdd;
 		totalDistanceCache += distToAdd;
 	}
 }

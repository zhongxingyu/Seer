 package edu.gatech.cs2340.sequencing;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import edu.gatech.cs2340.test.DebugPrinter;
 
 
 /**
  * 
  * @author Stephen Conway
  * 		Function group:		Controller: Sequencing
  * 		Created for:		M6		10/8/13
  * 		Assigned to:		Stephen
  * 		Modifications:		M6		10/11/13
  * 									Added method to end thread.	
  * 							M7		10/21/13
  * 									Changed to run asynchronously (and optionally synchronously)
  * 
  * 		Purpose: Blocks for a set amount of time.
  */
 public class MULETimer implements WaitedOn, Serializable {
 	private static ArrayList<MULETimer> activeTimers;
 	
 	private final long duration_ms;
 	private boolean stopped;
 	private long startTime_tick;
 	
 	public static ArrayList<MULETimer> getActiveTimers() {
 		if (activeTimers == null) {
 			activeTimers = new ArrayList<MULETimer>();
 		}
 		return activeTimers;
 	}
 	
 	
 	/**
 	 * Constructor to
 	 * 
 	 * @param duration_ms
 	 */
 	public MULETimer(long duration_ms) {
 		this.duration_ms = duration_ms;
 		stopped = false;
 	}
 
 	@Override
 	public boolean isFinished() {
 		boolean result = stopped || startTime_tick + duration_ms/GameClock.TICK_LENGTH <= GameClock.getTick();
 		if (result) {
 			activeTimers.remove(this);
 		}
 		return result;
 	}
 
 	/**
 	 * Method to start the timer
 	 */
 	public void start() {
 		if (activeTimers == null) {
 			activeTimers = new ArrayList<MULETimer>();
 		}
 		activeTimers.add(this);
 		startTime_tick = GameClock.getTick();
 	}
 	
 	/**
 	 * Method to stop the timer. Cannot be restarted.
 	 */
 	public void stop() {
 		stopped = true;
 	}
 	
 	/**
 	 * Method to get remaining time on the timer
 	 * @return
 	 */
 	public long getTimeRemaining() {
		return (startTime_tick + duration_ms/GameClock.TICK_LENGTH - GameClock.getTick())* GameClock.TICK_LENGTH;
 	}
 	
 	/**
 	 * Method to get the original duration of the timer
 	 * @return
 	 */
 	public long getTimerDuration() {
 		return duration_ms;
 	}
 	
 	/**
 	 * Method to get starting time in System.current time
 	 * @return
 	 */
 	public long getStartTime() {
 		return startTime_tick;
 	}
 	
 
 
 
 }

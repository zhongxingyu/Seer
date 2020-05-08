 package se.chalmers.dat255.sleepfighter.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Manages all the existing alarms.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Sep 18, 2013
  */
 public class AlarmsManager {
 	/**
 	 * Provides information about the earliest alarm.<br/>
 	 * This information contains:
 	 * <ul>
 	 * 	<li>occurrence in milliseconds since unix epoch</li>
 	 * 	<li>index of alarm in list.</li>
 	 * </ul>
 	 *
 	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 	 * @version 1.0
 	 * @since Sep 18, 2013
 	 */
 	public static class EarliestInfo {
 		private long millis;
 		private int index;
 
 		private EarliestInfo( long millis, int index ) {
 			this.millis = millis;
 			this.index = index;
 		}
 
 		/**
 		 * Returns true if the earliest info is real.<br/>
 		 * This occurs when {@link Alarm#canHappen()} returns true for some alarm.
 		 *
 		 * @return true if earliest info is real.
 		 */
 		public boolean isReal() {
 			return this.millis != Alarm.NEXT_NON_REAL;
 		}
 
 		/**
 		 * The earliest alarm in milliseconds.
 		 *
 		 * @return the earliest alarm in milliseconds.
 		 */
 		public long getMillis() {
 			return this.millis;
 		}
 
 		/**
 		 * The earliest alarm in index.
 		 *
 		 * @return the earliest alarm in index.
 		 */
 		public int getIndex() {
 			return this.index;
 		}
 	}
 
 	/** Holds the list of alarms. */
 	private List<Alarm> list;
 
 	/**
 	 * Constructs the manager with no initial alarms.
 	 */
 	public AlarmsManager() {
 		this.list = new ArrayList<Alarm>();
 	}
 
 	/**
 	 * Sets the list of alarms.
 	 *
 	 * @param list the list of alarms to set.
 	 */
 	public void set( List<Alarm> list ) {
 		this.list = list;
 	}
 
 	/**
 	 * Returns the list of alarms.
 	 *
 	 * @return the list of alarms.
 	 */
 	public List<Alarm> get() {
 		return this.list;
 	}
 
 	/**
 	 * Returns info about the earliest alarm.<br/>
 	 * The info contains info about milliseconds and index of alarm.
 	 *
 	 * @return info about the earliest alarm. 
 	 */
	public EarliestInfo getEariliestInfo() {
 		long millis = -1;
 		int earliestIndex = -1;
 
 		for ( int i = 0; i < this.list.size(); i++ ) {
 			long currMillis = this.list.get( i ).getNextMillis();
 			if ( currMillis > -1 && millis > currMillis ) {
 				earliestIndex = i;
 			}
 		}
 
 		return new EarliestInfo( millis, earliestIndex );
 	}
 }

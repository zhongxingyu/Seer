 package se.chalmers.dat255.sleepfighter.model;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import se.chalmers.dat255.sleepfighter.utils.DateTimeUtils;
 import se.chalmers.dat255.sleepfighter.utils.collect.ObservableList;
 import se.chalmers.dat255.sleepfighter.utils.message.Message;
 import se.chalmers.dat255.sleepfighter.utils.message.MessageBus;
 
 /**
  * Manages all the existing alarms.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Sep 18, 2013
  */
 public class AlarmsManager extends ObservableList<Alarm> {
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
 		private Alarm alarm;
 		private int index;
 
 		/**
 		 * Constructs an EarliestInfo.
 		 *
 		 * @param millis milliseconds to earliest alarm.
 		 * @param alarm the alarm object that is earliest.
 		 * @param index the index of the alarm in the alarm list.
 		 * 
 		 */
 		private EarliestInfo( long millis, Alarm alarm, int index) {
 			this.millis = millis;
 			this.alarm = alarm;
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
 		 * Returns the difference of the time of earliest alarm and now as a calendar.
 		 *
 		 * @param now the current time.
 		 * @return the difference.
 		 */
 		public Calendar getDiff( Calendar now ) {
 			return DateTimeUtils.getDateDiff( now, this.getMillis() );
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
 		 * The earliest alarm.
 		 *
 		 * @return the earliest alarm.
 		 */
 		public Alarm getAlarm() {
 			return this.alarm;
 		}
 		
 		public int getIndex() {
 			return this.index;
 		}
 
 	}
 
 	/**
 	 * Constructs the manager with no initial alarms.
 	 */
 	public AlarmsManager() {
 		this( new ArrayList<Alarm>() );
 	}
 
 	/**
 	 * Constructs the manager starting with given alarms.
 	 *
 	 * @param alarms list of given alarms. Don't modify this list directly.
 	 */
 	public AlarmsManager( List<Alarm> alarms ) {
 		this.setDelegate( alarms );
 	}
 
 	/**
 	 * Sets the list of alarms.
 	 *
 	 * @param delegate list of given alarms. Don't modify this list directly.
 	 */
 	@Override
 	public void setDelegate( List<Alarm> delegate ) {
 		super.setDelegate( delegate );
 	}
 
 	@Override
 	protected void fireEvent( Event e ) {
 		// Intercept add/update events and inject message bus.
 		if ( e.operation() == Operation.ADD || e.operation() == Operation.UPDATE ) {
 			for ( Object obj : e.elements() ) {
 				((Alarm) obj).setMessageBus( this.getMessageBus() );
 			}
 		}
 
 		super.fireEvent( e );
 	}
 
 	/**
 	 * Sets the message bus, if not set, no events will be received.
 	 *
 	 * @param bus the buss that receives events.
 	 */
 	public void setMessageBus( MessageBus<Message> bus ) {
 		super.setMessageBus( bus );
 
 		for ( Alarm alarm : this ) {
 			alarm.setMessageBus( bus );
 		}
 	}
 
 	/**
 	 * Returns info about the earliest alarm.<br/>
 	 * The info contains info about milliseconds and the alarm.
 	 *
 	 * @return info about the earliest alarm. 
 	 */
 	public EarliestInfo getEarliestInfo( Calendar now ) {
 		long millis = Alarm.NEXT_NON_REAL;
 		int earliestIndex = -1;
 
 		for ( int i = 0; i < this.size(); i++ ) {
 			long currMillis = this.get( i ).getNextMillis( now );
 			if ( currMillis > Alarm.NEXT_NON_REAL && (millis == Alarm.NEXT_NON_REAL || millis > currMillis) ) {
 				earliestIndex = i;
 				millis = currMillis;
 			}
 		}
 
 		if ( millis == Alarm.NEXT_NON_REAL ) {
 			earliestIndex = -1;
 		}
 
		Alarm alarm = millis == Alarm.NEXT_NON_REAL ? null : this.get( earliestIndex );

 		return new EarliestInfo( millis,alarm, earliestIndex );
 	}
 
 	/**
 	 * Returns info about the earliest alarm.<br/>
 	 * The info contains info about milliseconds and the alarm.
 	 *
 	 * @return info about the earliest alarm. 
 	 */
 	public EarliestInfo getEarliestInfo() {
 		return this.getEarliestInfo( GregorianCalendar.getInstance() );
 	}
 }

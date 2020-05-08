 package se.chalmers.dat255.sleepfighter.model;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeConstants;
 import org.joda.time.MutableDateTime;
 import org.joda.time.ReadableDateTime;
 
 import se.chalmers.dat255.sleepfighter.utils.DateTextUtils;
 import se.chalmers.dat255.sleepfighter.utils.message.Message;
 import se.chalmers.dat255.sleepfighter.utils.message.MessageBus;
 import android.util.Log;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 import com.j256.ormlite.field.DatabaseField;
 import com.j256.ormlite.table.DatabaseTable;
 
 /**
  * Alarm models the alarm settings and business logic for an alarm.
  *
  * Actual model fields are described in {@link Field}
  *
  * @version 1.0
  * @since Sep 16, 2013
  */
 @DatabaseTable(tableName = "alarm")
 public class Alarm implements Cloneable {
 	/**
 	 * Enumeration of fields in an Alarm.
 	 *
 	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 	 * @version 1.0
 	 * @since Sep 19, 2013
 	 */
 	public static enum Field {
 		TIME, ACTIVATED, ENABLED_DAYS, NAME, ID
 	}
 
 	/**
 	 * All events fired by {@link Alarm} extends this interface.
 	 *
 	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 	 * @version 1.0
 	 * @since Sep 19, 2013
 	 */
 	public static interface AlarmEvent extends Message {
 		/**
 		 * Returns the alarm that triggered the event.
 		 *
 		 * @return the alarm.
 		 */
 		public Alarm getAlarm();
 
 		public Field getModifiedField();
 	}
 
 	/**
 	 * Base implementation of AlarmEvent
 	 *
 	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 	 * @version 1.0
 	 * @since Sep 19, 2013
 	 */
 	public static abstract class BaseAlarmEvent implements AlarmEvent {
 		private Field field;
 		private Alarm alarm;
 
 		private BaseAlarmEvent(Alarm alarm, Field field) {
 			this.alarm = alarm;
 			this.field = field;
 		}
 
 		public Alarm getAlarm() {
 			return this.alarm;
 		}
 
 		/**
 		 * Returns the modified field in Alarm.
 		 *
 		 * @return the modified field.
 		 */
 		public Field getModifiedField() {
 			return this.field;
 		}
 	}
 
 	/**
 	 * DateChangeEvent occurs when a date-time related constraint is modified, these are:<br/>
 	 * <ul>
 	 * 	<li>{@link Field#TIME}</li>
 	 * 	<li>{@link Field#ACTIVATED}</li>
 	 * 	<li>{@link Field#ENABLED_DAYS}</li>
 	 * </ul>
 	 *
 	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 	 * @version 1.0
 	 * @since Sep 19, 2013
 	 */
 	public static class DateChangeEvent extends BaseAlarmEvent {
 		private DateChangeEvent(Alarm alarm, Field field) {
 			super(alarm, field);
 		}
 	}
 
 	/**
 	 * DateChangeEvent occurs when a date-time related constraint is modified, these are:<br/>
 	 * <ul>
 	 * 	<li>{@link Field#NAME}</li>
 	 * 	<li>{@link Field#ID}</li>
 	 * </ul>
 	 *
 	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 	 * @version 1.0
 	 * @since Sep 19, 2013
 	 */
 	public static class MetaChangeEvent extends BaseAlarmEvent {
 		private MetaChangeEvent(Alarm alarm, Field field) {
 			super(alarm, field);
 		}
 	}
 
 	@DatabaseField(generatedId = true)
 	private int id;
 
 	/** IDs for non-committed Alarms. */
 	public static final int NOT_COMMITTED_ID = -1;
 
 	@DatabaseField
 	private boolean isActivated = true;
 
 	@DatabaseField
 	private String name;
 
 	/** The value for unnamed strings is {@value #UNNAMED} */
 	public static final String UNNAMED = null;
 
 	@DatabaseField
 	private int hour;
 
 	@DatabaseField
 	private int minute;
 
 	@DatabaseField
 	private int second;
 
 	/** The weekdays that this alarm can ring. */
 	@DatabaseField(width = 7)
 	private boolean[] enabledDays = { true, true, true, true, true, true, true };
 	private static final int MAX_WEEK_LENGTH = DateTimeConstants.DAYS_PER_WEEK;
 	private static final int MAX_WEEK_INDEX = MAX_WEEK_LENGTH - 1;
 
 	@DatabaseField
 	private int unnamedPlacement;
 
 	/** The value {@link #getNextMillis()} returns when Alarm can't happen. */
 	public static final Long NEXT_NON_REAL = null;
 
 	private MessageBus<Message> bus;
 
 	/**
 	 * Sets the message bus, if not set, no events will be received.
 	 *
 	 * @param bus the buss that receives events.
 	 */
 	public void setMessageBus( MessageBus<Message> bus ) {
 		this.bus = bus;
 	}
 
 	/**
 	 * Returns the message bus, or null if not set.
 	 *
 	 * @return the message bus.
 	 */
 	public MessageBus<Message> getMessageBus() {
 		return this.bus;
 	}
 
 	/**
 	 * Constructs an alarm to 00:00.<br/>
 	 * This is the equivalent of calling {@link #Alarm(int, int)} with (0, 0).
 	 */
 	public Alarm() {
 		this(0, 0, 0, "");
 		this.setId(NOT_COMMITTED_ID);
 	}
 
 	/**
 	 * Copy constructor
 	 *
 	 * @param rhs the alarm to copy from.
 	 */
 	public Alarm( Alarm rhs ) {
 		// Reset id.
 		this.setId( NOT_COMMITTED_ID );
 
 		// Copy data.
 		this.hour = rhs.hour;
 		this.minute = rhs.minute;
 		this.second = rhs.second;
 		this.isActivated = rhs.isActivated;
 		this.enabledDays = rhs.enabledDays;
 		this.name = rhs.name;
 
 		// Copy dependencies.
 		this.bus = rhs.bus;
 	}
 
 	/**
 	 * Constructs an alarm given an hour and minute.
 	 *
 	 * @param 	hour the hour the alarm should occur.
 	 * @param minute the minute the alarm should occur.
 	 */
 	public Alarm(int hour, int minute) {
 		this(hour, minute, 0, "");
 	}
 
 	/**
 	 *  Constructs an alarm given an hour, minute and name.
 	 * 
 	 * @param hour the hour the alarm should occur.
 	 * @param minute the minute the alarm should occur.
 	 * @param name the name the alarm should have.
 	 */
 	public Alarm(int hour, int minute, String name) {
 		this(hour, minute, 0, name);
 	}
 	
 	/**
 	 * Constructs an alarm given an hour, minute and second.
 	 *
 	 * @param hour the hour the alarm should occur.
 	 * @param minute the minute the alarm should occur.
 	 * @param second the second the alarm should occur.
 	 */
 	public Alarm(int hour, int minute, int second) {
 		this(hour, minute, second, "");
 	}
 
 	/**
 	 *  Constructs an alarm given an hour, minute, second and name.
 	 * 
 	 * @param hour the hour the alarm should occur.
 	 * @param minute the minute the alarm should occur.
 	 * @param second the second the alarm should occur.
 	 * @param name the name the alarm should have.
 	 */
 	public Alarm(int hour, int minute, int second, String name) {
 		this.setTime(hour, minute, second);
 		this.setName(name);
 	}
 	
 	/**
 	 * Constructs an alarm with values derived from a unix epoch timestamp.
 	 *
 	 * @param time time in unix epoch timestamp.
 	 */
 	public Alarm(long time) {
 		this(time, "");
 	}
 	
 	/**
 	 * Constructs an alarm with values derived from a unix epoch timestamp and a name.
 	 *
 	 * @param time time in unix epoch timestamp.
 	 * @param name the name the alarm should have.
 	 */
 	public Alarm(long time, String name) {
 		this.setTime(time);
 		this.setName(name);
 	}
 
 	/**
 	 * Sets the hour, minute and second of this alarm derived from a {@link ReadableDateTime} object.
 	 * 
 	 * @param time a {@link ReadableDateTime} object. 
 	 */
 	public Alarm(ReadableDateTime time) {
 		this(time, "");
 	}
 	
 	/**
 	 * Sets the hour, minute and second of this alarm derived from a {@link ReadableDateTime} object as well as a name.
 	 * 
 	 * @param time a {@link ReadableDateTime} object. 
 	 * @param name the name the alarm should have.
 	 */
 	public Alarm(ReadableDateTime time, String name) {
 		this.setTime(time);
 		this.setName(name);
 	}
 
 	/**
 	 * Returns the ID of the alarm.
 	 *
 	 * @return the ID of the alarm.
 	 */
 	public int getId() {
 		return this.id;
 	}
 
 	/**
 	 * Sets the ID of the alarm.<br/>
 	 * Should only be used for testing.
 	 *
 	 * @param id the ID of the alarm.
 	 */
 	public void setId( int id ) {
 		if ( this.id == id ) {
 			return;
 		}
 
 		this.id = id;
 		this.publish( new MetaChangeEvent( this, Field.ID ) );
 	}
 
 	/**
 	 * Returns the name of the Alarm.
 	 *
 	 * @return the name of the Alarm.
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Sets the name of the Alarm.
 	 *
 	 * @param name the name of the Alarm to set.
 	 */
 	public void setName( String name ) {
 		if ( this.name == name ) {
 			return;
 		}
 
 		if ( name == null ) {
 			throw new IllegalArgumentException( "A named Alarm can not be unnamed." );
 		}
 
 		this.name = name;
 		this.unnamedPlacement = 0;
 		this.publish( new MetaChangeEvent( this, Field.NAME ) );
 	}
 
 	/**
 	 * Sets the hour and minute of this alarm.<br/>
 	 * This is the equivalent of {@link #setTime(int, int, int)} with <code>(hour, minute, 0)</code>.
 	 *
 	 * @param hour the hour the alarm should occur.
 	 * @param minute the minute the alarm should occur.
 	 */
 	public synchronized void setTime(int hour, int minute) {
 		this.setTime( hour, minute, 0 );
 	}
 
 	/**
 	 * Sets the hour, minute and second of this alarm.
 	 *
 	 * @param hour the hour the alarm should occur.
 	 * @param minute the minute the alarm should occur.
 	 * @param second the second the alarm should occur.
 	 */
 	public synchronized void setTime( int hour, int minute, int second ) {
 		if ( this.hour == hour && this.minute == minute && this.second == second ) {
 			return;
 		}
 
 		if (hour < 0 || hour > 23 || minute < 0 || minute > 59 || second < 0 || second > 59 ) {
 			throw new IllegalArgumentException();
 		}
 		this.hour = hour;
 		this.minute = minute;
 		this.second = second;
 
 		this.publish( new DateChangeEvent( this, Field.TIME ) );
 	}
 
 	/**
 	 * Sets the hour, minute and second of this alarm derived from a unix epoch timestamp.
 	 *
 	 * @param time time in unix epoch timestamp.
 	 */
 	public synchronized void setTime( long time ) {
 		this.setTime( new DateTime(time) );
 	}
 
 	/**
 	 * Sets the hour, minute and second of this alarm derived from a {@link ReadableDateTime} object.
 	 *
 	 * @param time a {@link ReadableDateTime} object.
 	 */
 	public synchronized void setTime( ReadableDateTime time ) {
 		this.setTime( time.getHourOfDay(), time.getMinuteOfHour(), time.getSecondOfMinute() );
 	}
 
 	/**
 	 * Returns the weekdays days this alarm is enabled for.<br/>
 	 * For performance, a direct reference is returned.
 	 *
 	 * @return the weekdays alarm is enabled for.
 	 */
 	public synchronized boolean[] getEnabledDays() {
 		return this.enabledDays.clone();
 	}
 
 	/**
 	 * Sets the weekdays this alarm is enabled for.<br/>
 	 * For performance, the passed value is not cloned.
 	 *
 	 * @param enabledDays the weekdays alarm should be enabled for.
 	 */
 	public synchronized void setEnabledDays( boolean[] enabledDays ) {
 		Preconditions.checkNotNull( enabledDays );
 
 		if ( Objects.equal( this.enabledDays, enabledDays ) ) {
 			return;
 		}
 
 		if ( enabledDays.length != MAX_WEEK_LENGTH ) {
 			throw new IllegalArgumentException( "A week has 7 days, but an array with: " + enabledDays.length + " was passed" );
 		}
 
 		this.enabledDays = enabledDays.clone();
 		this.publish( new DateChangeEvent( this, Field.ENABLED_DAYS ) );
 	}
 
 	/**
 	 * Returns when this alarm will ring.<br/>
 	 * If {@link #canHappen()} returns false, -1 will be returned.
 	 *
 	 * @param now the current time in unix epoch timestamp.
 	 * @return the time in unix epoch timestamp when alarm will next ring.
 	 */
 	public synchronized Long getNextMillis(long now) {
 		if ( !this.canHappen() ) {
 			return NEXT_NON_REAL;
 		}
 
 		MutableDateTime next = new MutableDateTime(now);
 		next.setHourOfDay( this.hour );
 		next.setMinuteOfHour( this.minute );
 		next.setSecondOfMinute( this.second );
 
 		// Houston, we've a problem, alarm is before now, adjust 1 day.
 		if ( next.isBefore( now ) ) {
 			next.addDays( 1 );
 		}
 
 		// Offset for weekdays. Checking canHappen() is important for this part, unless you want infinite loops.
 		int offset = 0;
 		for ( int currDay = next.getDayOfWeek() - 1; !this.enabledDays[currDay]; currDay++ ) {
 			Log.d( "Alarm", Integer.toString( currDay ) );
 			if ( currDay > MAX_WEEK_INDEX ) {
 				currDay = 0;
 			}
 
 			offset++;
 		}
 
 		if ( offset > 0 ) {
 			next.addDays( offset );
 		}
 
 		return next.getMillis();
 	}
 
 	/**
 	 * Returns true if the alarm can ring in the future,<br/>
 	 * that is: if {@link #isActivated()} and some weekday is enabled.
 	 *
 	 * @return true if the alarm can ring in the future.
 	 */
 	public boolean canHappen() {
 		if ( !this.isActivated() ) {
 			return false;
 		}
 
 		for ( boolean day : this.enabledDays ) {
 			if ( day == true ) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Returns the hour the alarm occurs.
 	 *
 	 * @return the hour the alarm occurs.
 	 */
 	public int getHour() {
 		return this.hour;
 	}
 
 	/**
 	 * Returns the minute the alarm occurs.
 	 *
 	 * @return the minute the alarm occurs.
 	 */
 	public int getMinute() {
 		return this.minute;
 	}
 
 	/**
 	 * Returns the second the alarm occurs.
 	 *
 	 * @return the second the alarm occurs.
 	 */
 	public int getSecond() {
 		return this.second;
 	}
 
 	/**
 	 * Sets whether or not the alarm should be active.
 	 *
 	 * @param isActivated whether or not the alarm should be active.
 	 */
 	public void setActivated(boolean isActivated) {
 		if ( this.isActivated == isActivated ) {
 			return;
 		}
 
 		this.isActivated = isActivated;
 		this.publish( new DateChangeEvent( this, Field.ACTIVATED ) );
 	}
 
 	/**
 	 * Returns true if the alarm is active.
 	 *
 	 * @return true if the alarm is active.
 	 */
 	public boolean isActivated() {
 		return this.isActivated;
 	}
 
 	/**
 	 * Returns true if the alarm is unnamed = ({@link #getName()} == {@link #UNNAMED}.
 	 *
 	 * @return true if the alarm is unnamed.
 	 */
 	public boolean isUnnamed() {
 		return this.name == UNNAMED;
 	}
 
 	/**
 	 * Returns the unnamed placement.<br/>
 	 * The unnamed placement is a number that is set for Alarms that honor {@link #isUnnamed()}.<br/>
 	 * It is a leaping number that is set upon addition to {@link AlarmList#add(int, Alarm)}.
 	 *
 	 * Let us assume that we have 3 alarms which all start out as unnamed.
 	 * Their placements will be 1,2,3,4.
 	 *
 	 * When the second alarm is removed, or renamed to "Holidays",
 	 * the 3rd alarm will not change its placement to 2.
 	 *
 	 * If then a 4th alarm is added, it will usurp the place that the 2nd alarm had,
 	 * and its unnamed placement will be 2.
 	 */
 	public int getUnnamedPlacement() {
 		return this.unnamedPlacement;
 	}
 
 	/**
 	 * Sets the unnamed placement of alarm, see {@link #getUnnamedPlacement()}.<br/>
 	 * This should be set directly after constructor, otherwise provided for testing.
 	 *
 	 * @param placement the unnamed placement of alarm.
 	 * @throws IllegalArgumentException if {@link #isUnnamed()} returns false.
 	 */
 	public void setUnnamedPlacement( int placement ) {
 		if ( !this.isUnnamed() ) {
 			throw new IllegalArgumentException("Can't set numeric placement when alarm is already named.");
 		}
 		this.unnamedPlacement = placement;
 	}
 
 	@Override
 	public String toString() {
 		return DateTextUtils.joinTime( this.hour, this.minute, this.second ) + " is" + (this.isActivated ? " " : " NOT ") + "activated.";
 	}
 
 	/**
 	 * Returns the Alarm:s time in the format: hh:mm.
 	 *
 	 * @return the formatted time.
 	 */
 	public String getTimeString() {
 		return String.format("%02d", this.getHour()) + ":" + String.format("%02d", this.getMinute());
 	}
 
 	@Override
 	public int hashCode() {
 		return this.id;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 
 		if (obj == null || getClass() != obj.getClass()) {
 			return false;
 		}
 
 		// TODO uncomment when working unique ID
 		Alarm rhs = (Alarm) obj;
 		return this.id == rhs.id;
 	}
 
	public Object clone() throws CloneNotSupportedException {
 		return new Alarm( this );
 	}
 
 	/**
 	 * Publishes an event to event bus.
 	 *
 	 * @param event the event to publish.
 	 */
 	private void publish( AlarmEvent event ) {
 		if ( this.bus == null ) {
 			return;
 		}
 
 		this.bus.publish( event );
 	}
 }

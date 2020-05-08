 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.toxbee.sleepfighter.model;
 
 import java.util.Arrays;
 import java.util.Map;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeConstants;
 import org.joda.time.MutableDateTime;
 
 import se.toxbee.sleepfighter.model.audio.AudioConfig;
 import se.toxbee.sleepfighter.model.audio.AudioSource;
 import se.toxbee.sleepfighter.model.audio.AudioSourceType;
 import se.toxbee.sleepfighter.model.challenge.ChallengeConfigSet;
 import se.toxbee.sleepfighter.utils.collect.PrimitiveArrays;
 import se.toxbee.sleepfighter.utils.message.Message;
 import se.toxbee.sleepfighter.utils.message.MessageBus;
 import se.toxbee.sleepfighter.utils.message.MessageBusHolder;
 import se.toxbee.sleepfighter.utils.model.IdProvider;
 import se.toxbee.sleepfighter.utils.string.StringUtils;
 import android.provider.Settings;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Maps;
 import com.google.common.primitives.Booleans;
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
 public class Alarm implements IdProvider, MessageBusHolder {
 	/**
 	 * Enumeration of fields in an Alarm.
 	 *
 	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 	 * @version 1.0
 	 * @since Sep 19, 2013
 	 */
 	public static enum Field {
 		ID, NAME,
 		TIME, MODE, ACTIVATED, ENABLED_DAYS,
 		AUDIO_SOURCE, AUDIO_CONFIG, SPEECH, FLASH
 	}
 
 	/* --------------------------------
 	 * Defined Events.
 	 * --------------------------------
 	 */
 
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
 
 		/**
 		 * Returns the Field that was modified in the alarm.
 		 *
 		 * @return the modified field.
 		 */
 		public Field getModifiedField();
 
 		/**
 		 * Returns the old value.
 		 *
 		 * @return the old value.
 		 */
 		public Object getOldValue();
 	}
 
 	/**
 	 * Base implementation of AlarmEvent
 	 *
 	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 	 * @version 1.0
 	 * @since Sep 19, 2013
 	 */
 	public static class BaseAlarmEvent implements AlarmEvent {
 		private Field field;
 		private Alarm alarm;
 		private Object oldValue;
 
 		private BaseAlarmEvent(Alarm alarm, Field field, Object oldValue ) {
 			this.alarm = alarm;
 			this.field = field;
 			this.oldValue = oldValue;
 		}
 
 		public Alarm getAlarm() {
 			return this.alarm;
 		}
 
 		public Field getModifiedField() {
 			return this.field;
 		}
 
 		@Override
 		public Object getOldValue() {
 			return this.oldValue;
 		}
 		
 	}
 
 	/**
 	 * ScheduleChangeEvent occurs when a scheduling related constraint is modified, these are:<br/>
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
 	public static class ScheduleChangeEvent extends BaseAlarmEvent {
 		private ScheduleChangeEvent(Alarm alarm, Field field, Object oldValue ) {
 			super(alarm, field, oldValue);
 		}
 	}
 
 	/**
 	 * MetaChangeEvent occurs when a name related constraint is modified, these are:<br/>
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
 		private MetaChangeEvent(Alarm alarm, Field field, Object oldValue ) {
 			super(alarm, field, oldValue);
 		}
 	}
 
 	/**
 	 * AudioChangeEvent occurs when a audio related constraint is modified, these are:<br/>
 	 * <ul>
 	 * 	<li>{@link Field#AUDIO_SOURCE}</li>
 	 * 	<li>{@link Field#AUDIO_CONFIG}</li>
 	 * </ul>
 	 * 
 	 *
 	 * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
 	 * @version 1.0
 	 * @since Sep 27, 2013
 	 */
 	public static class AudioChangeEvent extends BaseAlarmEvent {
 		private AudioChangeEvent(Alarm alarm, Field field, Object oldValue ) {
 			super(alarm, field, oldValue);
 		}
 	}
 
 	/* --------------------------------
 	 * Fields.
 	 * --------------------------------
 	 */
 
 	@DatabaseField(generatedId = true)
 	private int id = NOT_COMMITTED_ID;
 
 	/** IDs for non-committed Alarms. */
 	public static final int NOT_COMMITTED_ID = -1;
 
 	@DatabaseField
 	private boolean isActivated;
 
 	@DatabaseField
 	private String name = UNNAMED;
 
 	/** The value for unnamed strings is {@value #UNNAMED} */
 	public static final String UNNAMED = null;
 
 	@DatabaseField
 	private AlarmTime time;
 
 	@DatabaseField
 	private AlarmMode mode;
 
 	/** The weekdays that this alarm can ring. */
 	@DatabaseField(width = 7)
 	private boolean[] enabledDays = PrimitiveArrays.filled( true, 7 );
 	private static final int MAX_WEEK_LENGTH = DateTimeConstants.DAYS_PER_WEEK;
 	private static final int MAX_WEEK_INDEX = MAX_WEEK_LENGTH - 1;
 
 	@DatabaseField
 	private int unnamedPlacement;
 
 	@DatabaseField
 	private boolean isFlash = false;
 
 	// whether this alarm is the preset alarm(the default alarm)
 	@DatabaseField
 	private boolean isPresetAlarm = false;
 
 	@DatabaseField(foreign = true, canBeNull = true)
 	private AudioSource audioSource = new AudioSource( AudioSourceType.RINGTONE,
 			Settings.System.DEFAULT_ALARM_ALERT_URI.toString() );
 
 	@DatabaseField(foreign = true, canBeNull = false)
 	private AudioConfig audioConfig = new AudioConfig( 100, true );
 
 	@DatabaseField(foreign = true, canBeNull = false)
 	private SnoozeConfig snoozeConfig = new SnoozeConfig( true, 9 );
 
 	// the time and weather will be read out when the alarm goes off. 
 	@DatabaseField
 	private boolean isSpeech = false;
 
 	/** The value {@link #getNextMillis()} returns when Alarm can't happen. */
 	public static final Long NEXT_NON_REAL = null;
 
 	@DatabaseField(foreign = true, canBeNull = false)
 	private ChallengeConfigSet challenges = new ChallengeConfigSet( true );
 
 	private MessageBus<Message> bus;
 
 	/* --------------------------------
 	 * Constructors.
 	 * --------------------------------
 	 */
 
 	/**
 	 * Constructs an alarm to current time.
 	 */
 	public Alarm() {
 		this( new AlarmTime( new DateTime() ) );
 	}
 
 	/**
 	 * Constructs an alarm for given time.
 	 *
 	 * @param time the time.
 	 */
 	public Alarm( AlarmTime time ) {
 		this.time = time;
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
 		this.time = new AlarmTime( rhs.time );
 		this.isActivated = rhs.isActivated;
 		this.enabledDays = rhs.enabledDays.clone();
 		this.name = rhs.name;
 		this.mode = rhs.mode;
 
 		this.unnamedPlacement = 0;
 
 		// Copy dependencies.
 		this.bus = rhs.bus;
 		
 		this.audioSource = new AudioSource( rhs.audioSource );
 		this.audioConfig = new AudioConfig( rhs.audioConfig );
 		this.snoozeConfig = new SnoozeConfig( rhs.snoozeConfig );
 
 		this.challenges = new ChallengeConfigSet( rhs.challenges );
 		
 		this.isSpeech = rhs.isSpeech;
 		this.isFlash = rhs.isFlash;
 	}
 
 	/* --------------------------------
 	 * Public methods.
 	 * --------------------------------
 	 */
 
 	/**
 	 * Should be called when an alarm has been issued.<br/>
 	 * This forces out a {@link ScheduleChangeEvent} no matter what.
 	 */
 	public void issued() {
 		// Temporarily remove bus, don't send out excess events.
 		MessageBus<Message> bus = this.bus;
 		this.bus = null;
 
 		if ( this.mode != AlarmMode.REPEATING ) {
			if ( this.mode == AlarmMode.REPEATING ) {
 				this.mode = AlarmMode.NORMAL;
 			}
 
 			this.isActivated = false;
 		}
 
 		// Pretend like the alarms scheduling was altered even tho it might not have been.
 		this.bus = bus;
 		this.publish( new ScheduleChangeEvent( this, Field.ACTIVATED, !this.isActivated() ) );
 	}
 
 	/**
 	 * Sets the message bus, if not set, no events will be received.
 	 *
 	 * @param bus the buss that receives events.
 	 */
 	public void setMessageBus( MessageBus<Message> bus ) {
 		this.bus = bus;
 
 		// Pass it on!
 		this.challenges.setMessageBus( bus );
 		this.audioConfig.setMessageBus( bus );
 		this.snoozeConfig.setMessageBus( bus );
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
 
 		int old = this.id;
 		this.id = id;
 		this.publish( new MetaChangeEvent( this, Field.ID, old ) );
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
 		if (this.name != null && this.name.equals(name)) {
 			return;
 		}
 
 		if ( name == null ) {
 			if ( this.name == null ) {
 				return;
 			}
 
 			throw new IllegalArgumentException( "A named Alarm can not be unnamed." );
 		}
 
 		String old = this.name;
 		this.name = name;
 		this.unnamedPlacement = 0;
 		this.publish( new MetaChangeEvent( this, Field.NAME, old ) );
 	}
 
 	/**
 	 * Returns the time of the alarm (not timestamp, see {@link #getNextMillis(long)} for that...).
 	 *
 	 * @return the time.
 	 */
 	public AlarmTime getTime() {
 		return this.time;
 	}
 
 	/**
 	 * Sets the time to the given time.<br/>
 	 * For performance, the passed value is not cloned.
 	 *
 	 * @param time the time to set alarm to.
 	 */
 	public synchronized void setTime( AlarmTime time ) {
 		if ( Objects.equal( this.time, time ) ) {
 			return;
 		}
 
 		AlarmTime old = this.time;
 		this.time = time;
 		this.publish( new ScheduleChangeEvent( this, Field.TIME, old ) );
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
 
 		if ( enabledDays.length != MAX_WEEK_LENGTH ) {
 			throw new IllegalArgumentException( "A week has 7 days, but an array with: " + enabledDays.length + " was passed" );
 		}
 
 		boolean[] old = this.enabledDays;
 		this.enabledDays = enabledDays.clone();
 		this.publish( new ScheduleChangeEvent( this, Field.ENABLED_DAYS, old ) );
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
 
 		MutableDateTime next = this.getTime().afterNow( now );
 
 		// Offset if not countdown alarm.
 		// Countdown alarms always happen exactly at set time.
 		if ( !this.isCountdown() ) {
 			this.offsetWeekdays( next );
 		}
 
 		return next.getMillis();
 	}
 
 	/**
 	 * Offsets next to the first enabled weekday.
 	 *
 	 * @param next the time to offset.
 	 */
 	private void offsetWeekdays( MutableDateTime next ) {
 		// Offset for weekdays
 		int offset = 0;
 		
 		// First weekday to check (0-6), getDayOfWeek returns (1-7)
 		int weekday = next.getDayOfWeek() - 1;
 
 		// Find the weekday the alarm should run, should at most run seven times
 		for ( int i = 0; i < 7; ++i ) {
 			// Wrap to first weekday
 			if ( weekday > MAX_WEEK_INDEX ) {
 				weekday = 0;
 			}
 
 			if ( this.enabledDays[weekday] ) {
 				// We've found the closest day the alarm is enabled for
 				offset = i;
 				break;
 			}
 
 			++weekday;
 			++offset;
 		}
 
 		if ( offset > 0 ) {
 			next.addDays( offset );
 		}
 	}
 
 	/**
 	 * Returns true if the alarm can ring in the future,<br/>
 	 * that is: if {@link #isActivated()} and some weekday is enabled.
 	 *
 	 * @return true if the alarm can ring in the future.
 	 */
 	public synchronized boolean canHappen() {
 		if ( !this.isActivated() ) {
 			return false;
 		}
 
 		return this.isCountdown() || Booleans.contains( this.enabledDays, true );
 	}
 
 	/**
 	 * Sets whether or not the alarm should be active.
 	 *
 	 * @param isActivated whether or not the alarm should be active.
 	 */
 	public void setActivated( boolean isActivated ) {
 		if ( this.isActivated == isActivated ) {
 			return;
 		}
 
 		boolean old = this.isActivated;
 		this.isActivated = isActivated;
 		this.publish( new ScheduleChangeEvent( this, Field.ACTIVATED, old ) );
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
 		final Map<String, String> prop = Maps.newHashMap();
 		prop.put( "id", Integer.toString( this.getId() ) );
 		prop.put( "name", this.getName() );
 		prop.put( "time", this.getTime().getTimeString( false ) );
 		prop.put( "weekdays", Arrays.toString( this.enabledDays ) );
 		prop.put( "activated", Boolean.toString( this.isActivated() ) );
 		prop.put( "mode", this.getMode().toString() );
 		prop.put( "audio_source", this.getAudioSource() == null ? null : this.getAudioSource().toString() );
 		prop.put( "audio_config", this.getAudioConfig().toString() );
 
 		return "Alarm[" + StringUtils.PROPERTY_MAP_JOINER.join( prop ) + "]";
 	}
 
 	/**
 	 * <p><code>{@link Alarm#hashCode()} == {@link Alarm#getId()}</code></p>
 	 * {@inheritDoc}
 	 */
 	@Override
 	public int hashCode() {
 		return this.id;
 	}
 
 	/**
 	 * <p>Two alarms are considered equal iff <code>{@link Alarm#hashCode()} == {@link Alarm#getId()}</code></p>
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		return this == obj || obj != null && this.getClass() == obj.getClass() && this.id == ((Alarm) obj).id;
 	}
 
 	/**
 	 * Sets the mode of the alarm, this specifies how {@link #getTime()} is understood.
 	 *
 	 * @param mode the mode to set.
 	 */
 	public void setMode( AlarmMode mode ) {
 		if ( this.mode == Preconditions.checkNotNull( mode ) ) {
 			return;
 		}
 
 		AlarmMode old = this.mode;
 		this.mode = mode;
 		this.publish( new ScheduleChangeEvent( this, Field.MODE, old ) );
 	}
 
 	/**
 	 * Returns the mode of the alarm.
 	 *
 	 * @return the mode.
 	 */
 	public AlarmMode getMode() {
 		return this.mode;
 	}
 
 	/**
 	 * Sets if the alarm is repeating or not.
 	 *
 	 * @param isRepeating true if it is repeating.
 	 */
 	public void setRepeat( boolean isRepeating ) {
 		AlarmMode mode = isRepeating ? AlarmMode.REPEATING : AlarmMode.NORMAL;
 		this.setMode( mode );
 	}
 
 	/**
 	 * Sets if the alarm is counting down or not.
 	 *
 	 * @param isCountdown true if it is counting down.
 	 */
 	public void setCountdown( boolean isCountdown ) {
 		AlarmMode mode = isCountdown ? AlarmMode.COUNTDOWN : AlarmMode.NORMAL;
 		this.setMode( mode );
 	}
 
 	/**
 	 * Returns whether or not this alarm is repeating or not.
 	 *
 	 * @return true if it is repeating.
 	 */
 	public boolean isRepeating() {
 		return this.mode == AlarmMode.REPEATING;
 	}
 
 	/**
 	 * Returns whether or not this alarm is counting down or not.
 	 *
 	 * @return true if it is counting down.
 	 */
 	public boolean isCountdown() {
 		return this.mode == AlarmMode.COUNTDOWN;
 	}
 
 	/**
 	 * Sets if the alarm is flashing or not.
 	 *
 	 * @param isFlash true if it is flashing.
 	 */
 	public void setFlash( boolean isFlash ) {
 		if ( this.isFlash == isFlash ) {
 			return;
 		}
 		
 		boolean old = this.isFlash;
 		this.isFlash = isFlash;
 		this.publish( new BaseAlarmEvent( this, Field.FLASH, old ) );	
 	}
 	
 	/**
 	 * Returns whether or not this alarm is flashing or not.
 	 *
 	 * @return true if it is flashing.
 	 */
 	public boolean isFlashEnabled() {
 		return this.isFlash;
 	}
 
 	/**
 	 * Returns whether or not speech is enabled.<br/>
 	 * If true, then the time and weather will be read out when the alarm goes off.
 	 *
 	 * @return true if enabled.
 	 */
 	public boolean isSpeech() {
 		return this.isSpeech;
 	}
 
 	/**
 	 * Sets whether or not speech is enabled.
 	 *
 	 * @param isSpeech true if enabled.
 	 */
 	public void setSpeech( boolean isSpeech ) {
 		if ( this.isSpeech == isSpeech ) {
 			return;
 		}
 	
 		boolean old = this.isSpeech;
 		this.isSpeech = isSpeech;
 		this.publish( new BaseAlarmEvent( this, Field.SPEECH, old ) );	
 	}
 
 	/**
 	 * Sets the audio source for this alarm.
 	 *
 	 * @param source the audio source to set.
 	 */
 	public void setAudioSource( AudioSource source ) {
 		if ( this.audioSource == source ) {
 			return;
 		}
 
 		AudioSource old = this.audioSource;
 		this.audioSource = source;
 		this.publish( new AudioChangeEvent( this, Field.AUDIO_SOURCE, old ) );
 	}
 
 	/**
 	 * Returns the audio source of this Alarm.
 	 *
 	 * @return the audio source.
 	 */
 	public AudioSource getAudioSource() {
 		return this.audioSource;
 	}
 
 	/**
 	 * Returns the audio configuration for this alarm.
 	 *
 	 * @return the audio configuration.
 	 */
 	public AudioConfig getAudioConfig() {
 		return this.audioConfig;
 	}
 
 	/**
 	 * Returns the snooze configuration for the alarm.
 	 * 
 	 * @return the snooze configuration
 	 */
 	public SnoozeConfig getSnoozeConfig() {
 		return this.snoozeConfig;
 	}
 
 	/**
 	 * Returns the ChallengeConfigSet for this alarm.<br/>
 	 * Modifications may be done directly to the returned set<br/>
 	 * as it is a well isolated module/unit.
 	 *
 	 * @return the ChallengeConfigSet object.
 	 */
 	public ChallengeConfigSet getChallengeSet() {
 		return this.challenges;
 	}
 
 	/* --------------------------------
 	 * PERSISTENCE ONLY METHODS.
 	 * --------------------------------
 	 */
 
 	/**
 	 * <p><strong>NOTE:</strong> this method is only intended for persistence purposes.<br/>
 	 * This method is motivated and needed due to OrmLite not supporting results from joins.<br/>
 	 * This is also a better method than reflection which is particularly expensive on android.</p>
 	 *
 	 * <p>Sets the {@link AudioConfig}, bypassing any and all checks, and does not send any event to bus.</p>
 	 *
 	 * @param config the {@link AudioConfig} to set.
 	 */
 	public void setFetched( AudioConfig config ) {
 		this.audioConfig = config;
 	}
 
 	/**
 	 * <p><strong>NOTE:</strong> this method is only intended for persistence purposes.<br/>
 	 * This method is motivated and needed due to OrmLite not supporting results from joins.<br/>
 	 * This is also a better method than reflection which is particularly expensive on android.</p>
 	 *
 	 * <p>Sets the {@link AudioSource}, bypassing any and all checks, and does not send any event to bus.</p>
 	 *
 	 * @param source the {@link AudioSource} to set.
 	 */
 	public void setFetched( AudioSource source ) {
 		this.audioSource = source;
 	}
 
 	/**
 	 * <p><strong>NOTE:</strong> this method is only intended for persistence purposes.<br/>
 	 * This method is motivated and needed due to OrmLite not supporting results from joins.<br/>
 	 * This is also a better method than reflection which is particularly expensive on android.</p>
 	 *
 	 * <p>Sets the {@link SnoozeConfig}, bypassing any and all checks, and does not send any event to bus.</p>
 	 *
 	 * @param source the {@link SnoozeConfig} to set.
 	 */
 	public void setFetched(SnoozeConfig config) {
 		this.snoozeConfig = config;
 	}
 
 	/**
 	 * <p><strong>NOTE:</strong> this method is only intended for persistence purposes and factorization.<br/>
 	 * This method is motivated and needed due to OrmLite not supporting results from joins.<br/>
 	 * This is also a better method than reflection which is particularly expensive on android.</p>
 	 *
 	 * <p>Sets the {@link ChallengeConfigSet}, bypassing any and all checks, and does not send any event to bus.</p>
 	 *
 	 * @param challenges the {@link ChallengeConfigSet} to set.
 	 */
 	public void setChallenges( ChallengeConfigSet challenges ) {
 		this.challenges = challenges;
 		this.challenges.setMessageBus( this.getMessageBus() );
 	}
 
 	/**
 	 * Sets whether or not this is a preset alarm.
 	 *
 	 * @param isPresetAlarm true if it is a preset alarm, otherwise false.
 	 */
 	public void setIsPresetAlarm( boolean isPresetAlarm ) {
 		this.isPresetAlarm = isPresetAlarm;
 	}
 
 	/**
 	 * Returns whether or not this is a preset alarm.
 	 *
 	 * @return true if it is a preset alarm, otherwise false.
 	 */
 	public boolean isPresetAlarm() {
 		return this.isPresetAlarm;
 	}
 
 	/* --------------------------------
 	 * Private Methods.
 	 * --------------------------------
 	 */
 
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

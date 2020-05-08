 package com.alexrnl.commons.time;
 
 import java.sql.Date;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.alexrnl.commons.utils.object.AutoCompare;
 import com.alexrnl.commons.utils.object.AutoHashCode;
 import com.alexrnl.commons.utils.object.Field;
 
 /**
  * Extension of {@link Time} which handles seconds.<br />
  * 
  * @author Alex
  */
 public class TimeSec extends Time implements Cloneable {
 	/** Logger */
 	private static Logger		lg					= Logger.getLogger(TimeSec.class.getName());
 	
 	/** Serial version UID */
 	private static final long	serialVersionUID	= -5220683648807102121L;
 	
 	/** Number of seconds per minutes */
 	public static final int		SECONDS_PER_MINUTES	= 60;
 	
 	/** The number of seconds */
 	private final int			seconds;
 	
 	/**
 	 * Constructor #1.<br />
 	 * Default constructor, set time to midnight.
 	 */
 	public TimeSec () {
 		this(0);
 	}
 	
 	/**
 	 * Constructor #2.<br />
 	 * The number of minutes and seconds will be set to 0.
 	 * @param hours
 	 *        the number of hours.
 	 */
 	public TimeSec (final int hours) {
 		this(hours, 0);
 	}
 	
 	/**
 	 * Constructor #3.<br />
 	 * The number of seconds will be set to 0.
 	 * @param hours
 	 *        the number of hours.
 	 * @param minutes
 	 *        the number of minutes.
 	 */
 	public TimeSec (final int hours, final int minutes) {
 		this(hours, minutes, 0);
 	}
 	
 	/**
 	 * Constructor #4.<br />
 	 * @param hours
 	 *        the number of hours.
 	 * @param minutes
 	 *        the number of minutes.
 	 * @param seconds
 	 *        the number of seconds.
 	 */
 	public TimeSec (final int hours, final int minutes, final int seconds) {
 		super(hours, minutes + seconds / SECONDS_PER_MINUTES);
 		this.seconds = seconds % SECONDS_PER_MINUTES;
 	}
 	
 	/**
 	 * Constructor #5.<br />
 	 * @param timeStamp
	 *        the number of milliseconds since January 1st, 1970.
 	 */
 	public TimeSec (final long timeStamp) {
 		this(new Date(timeStamp));
 	}
 	
 	/**
 	 * Constructor #6.<br />
 	 * Build the time from the date.
 	 * @param date
 	 *        the date to use.
 	 */
 	public TimeSec (final Date date) {
 		super(date);
 		final Calendar cal = Calendar.getInstance(Locale.getDefault());
 		cal.setTime(date);
 		seconds = cal.get(Calendar.SECOND);
 	}
 	
 	/**
 	 * Constructor #7.<br />
 	 * Build the object from the {@link Time} given.
 	 * @param time
 	 *        the time.
 	 */
 	public TimeSec (final Time time) {
 		this(time, 0);
 	}
 	
 	/**
 	 * Constructor #8.<br />
 	 * Build the object from the {@link Time} and seconds given.
 	 * @param time
 	 *        the time.
 	 * @param seconds
 	 *        the number of seconds.
 	 */
 	public TimeSec (final Time time, final int seconds) {
 		this(time.getHours(), time.getMinutes(), seconds);
 	}
 	
 	/**
 	 * Constructor #9.<br />
 	 * Build the object from the {@link Time} and seconds given.
 	 * @param time
 	 *        the time.
 	 */
 	public TimeSec (final TimeSec time) {
 		this(time.getHours(), time.getMinutes(), time.getSeconds());
 	}
 	
 	/**
 	 * Build a time based on a string.<br />
 	 * The time format must be hours minutes (in that order) separated using any
 	 * non-numerical character.<br />
 	 * @param time
 	 *        the time set.
 	 * @return the time matching the string.
 	 */
 	public static TimeSec get (final String time) {
 		if (lg.isLoggable(Level.FINE)) {
 			lg.fine("Parsing time " + time);
 		}
 		final String[] hm = time.split(NON_DECIMAL_CHARACTER);
 		Integer hours = null;
 		Integer minutes = null;
 		Integer seconds = null;
 		for (final String s : hm) {
 			if (s.isEmpty()) {
 				continue;
 			}
 			if (hours == null) {
 				hours = Integer.parseInt(s);
 				continue;
 			}
 			if (minutes == null) {
 				minutes = Integer.parseInt(s);
 				continue;
 			}
 			seconds = Integer.parseInt(s);
 			break;
 		}
 		return new TimeSec(hours == null ? 0 : hours,
 				minutes == null ? 0 : minutes,
 						seconds == null ? 0 : seconds);
 	}
 	
 	/**
 	 * Return the current time.
 	 * @return a {@link Time} object matching the current time.
 	 */
 	public static TimeSec getCurrent () {
 		return new TimeSec(System.currentTimeMillis());
 	}
 	
 	/**
 	 * Return the attribute seconds.
 	 * @return the attribute seconds.
 	 */
 	@Field
 	public int getSeconds () {
 		return seconds;
 	}
 	
 	/**
 	 * Return a Time object build from this TimeSec properties.
 	 * @return the new Time object.
 	 */
 	public Time getTime () {
 		return new Time(getHours(), getMinutes());
 	}
 	
 	/**
 	 * Add the amount of time specified to the current time.<br />
 	 * There is no maximum, so you may reach 25:48:02.
 	 * @param time
 	 *        the time to add.
 	 * @return the new time.
 	 */
 	public TimeSec add (final TimeSec time) {
 		return new TimeSec(getHours() + time.getHours(), getMinutes() + time.getMinutes(),
 				getSeconds() + time.getSeconds());
 	}
 	
 	/**
 	 * Subtract the amount of time specified to the current time.<br />
 	 * There is no minimum, so you may reach -2:48:28.
 	 * @param time
 	 *        the time to subtract.
 	 * @return the new time.
 	 */
 	public TimeSec sub (final TimeSec time) {
 		return new TimeSec(getHours() - time.getHours(), getMinutes() - time.getMinutes(),
 				getSeconds() - time.getSeconds());
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Comparable#compareTo(java.lang.Object)
 	 */
 	@Override
 	public int compareTo (final Time o) {
 		if (o == null) {
 			return 1;
 		}
 		final int parentComparison = super.compareTo(o);
 		if (parentComparison != 0) {
 			return parentComparison;
 		}
 		final TimeSec timeSec = (o instanceof TimeSec) ? (TimeSec) o : new TimeSec(o);
 		if (seconds > timeSec.seconds) {
 			return 1;
 		} else if (seconds < timeSec.seconds) {
 			return -1;
 		}
 		assert equals(timeSec);
 		return 0;
 	}
 	
 	/**
 	 * Check if the current time is after the specified time.<br />
 	 * @param time
 	 *        the time used for reference.
 	 * @return <code>true</code> if this time is after the reference time provided.
 	 * @see #compareTo(Time)
 	 */
 	@Override
 	public boolean after (final Time time) {
 		return compareTo(time) > 0;
 	}
 	
 	/**
 	 * Check if the current time is before the specified time.<br />
 	 * @param time
 	 *        the time used for reference.
 	 * @return <code>true</code> if this time is before the reference time provided.
 	 * @see #compareTo(Time)
 	 */
 	@Override
 	public boolean before (final Time time) {
 		return compareTo(time) < 0;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString () {
 		String s = Integer.toString(seconds);
 		if (s.length() < 2) {
 			s = Integer.valueOf(0) + s;
 		}
 		return super.toString() + TIME_SEPARATOR + s;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode () {
 		return AutoHashCode.getInstance().hashCode(this);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals (final Object obj) {
 		if (!(obj instanceof Time)) {
 			return false;
 		}
 		return AutoCompare.getInstance().compare(this, (Time) obj);
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * @see java.lang.Object#clone()
 	 */
 	@Override
 	public TimeSec clone () throws CloneNotSupportedException {
 		return new TimeSec(this);
 	}
 }

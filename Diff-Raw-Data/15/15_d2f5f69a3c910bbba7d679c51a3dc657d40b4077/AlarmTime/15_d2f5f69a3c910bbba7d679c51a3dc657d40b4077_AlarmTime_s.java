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
 package se.toxbee.sleepfighter.model.time;
 
 import se.toxbee.sleepfighter.utils.model.Refreshable;
 import se.toxbee.sleepfighter.utils.string.StringUtils;
 
 import com.google.common.collect.ComparisonChain;
 
 /**
  * AlarmTime holds the time of an alarm.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Nov 11, 2013
  */
 public abstract class AlarmTime implements Comparable<AlarmTime>, Refreshable {
 	protected int second;
 	protected int minute;
 	protected int hour;
 
 	/**
 	 * Copy constructor.
 	 *
 	 * @param rhs the time to copy from.
 	 */
 	public AlarmTime( AlarmTime rhs ) {
 		this( rhs.hour, rhs.minute, rhs.second );
 	}
 
 	/**
 	 * Constructs time with h (hour), m (minute), s (second).
 	 * Times are wrapped.
 	 *
 	 * @param h hour.
 	 * @param m minute.
 	 * @param s second.
 	 */
 	public AlarmTime( int h, int m, int s ) {
 		this.hour = h % 24;
 		this.minute = m % 60;
 		this.second = s % 60;
 	}
 
 	/**
 	 * Does nothing.
 	 */
 	protected AlarmTime() {
 	}
 
 	/**
 	 * Returns the second.
 	 *
 	 * @return the second
 	 */
 	public int getSecond() {
 		return this.second;
 	}
 
 	/**
 	 * Returns the minute.
 	 *
 	 * @return the minute
 	 */
 	public int getMinute() {
 		return this.minute;
 	}
 
 	/**
 	 * Returns the hour.
 	 *
 	 * @return the hour.
 	 */
 	public int getHour() {
 		return this.hour;
 	}
 
 	/**
 	 * Returns the time as an array of integers: [h, m (, s)].
 	 *
 	 * @param omitSecond omit the second?
 	 * @return the array.
 	 */
 	public int[] asArray( boolean omitSecond ) {
 		return	omitSecond
 			?	new int[] { this.hour, this.minute }
 			:	new int[] { this.hour, this.minute, this.second };
 	}
 
 	/**
 	 * Returns the AlarmTime as a string with format: HH:MM::SS.
 	 *
 	 * @return the string.
 	 */
 	public String toString() {
 		return this.getTimeString( false );
 	}
 
 	/**
 	 * Returns the AlarmTime as a string with format: HH:MM(::SS).
 	 *
 	 * @param omitSecond omit the second?
 	 * @return the string.
 	 */
 	public String getTimeString( boolean omitSecond ) {
 		return StringUtils.joinTime( this.asArray( omitSecond ) );
 	}
 
 	/**
 	 * Returns the AlarmTime as a string with format: HH:MM.
 	 *
 	 * @return the string.
 	 */
 	public String getTimeString() {
 		return this.getTimeString( true );
 	}
 
 	/**
 	 * <p>
 	 *
 	 * {@inheritDoc}
 	 */
 	@Override
 	public final boolean equals( Object o ) {
 		return this == o || (o != null && o.getClass() == this.getClass()) && this.equalsImpl( (AlarmTime) o );
 	}
 
 	/**
	 * <p>Compares with following order: {@link #getSecond()}, {@link #getMinute()}, {@link #getHour()}.</p>
 	 * <p>Issues a call to {@link #refresh()}.</p>
 	 */
 	@Override
 	public int compareTo( AlarmTime r ) {
 		this.refresh();
 
 		return ComparisonChain.start()
			.compare( this.second, r.second )
			.compare( this.minute, r.minute )
 			.compare( this.hour, r.hour )
 			.result();
 	}
 
 	/**
 	 * Refreshes the time: {@link #getHour()}, {@link #getMinute()}, {@link #getSecond()}.<br/>
 	 * It is guaranteed not to alter a call to {@link #scheduledTimestamp(Object)}.
 	 */
 	public abstract void refresh();
 
 	/**
 	 * Returns the UNIX epoch timestamp when the time will next occur.
 	 *
 	 * @param now current time in UNIX epoch timestamp.
 	 * @param inject any extra info needed for computation.
 	 * @return the timestamp.
 	 */
 	public abstract long scheduledTimestamp( long now, Object... inject );
 
 	/**
 	 * Returns whether or not the time can occur.
 	 *
 	 * @param inject any extra info needed for computation.
 	 * @return true if it can.
 	 */
 	public abstract boolean canHappen( Object... inject );
 
 	protected abstract boolean equalsImpl( AlarmTime t );
 
 	public abstract int hashCode();
 
 	/**
 	 * Returns a {@link #AlarmTime()} that functions like an {@link ExactTime}.
 	 *
 	 * @return the time.
 	 */
 	public abstract AlarmTime exact();
 }

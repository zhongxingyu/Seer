 /*
  * Copyright 2011 Ritz, Bruno <bruno.ritz@gmail.com>
  *
  * This file is part of S-Plan.
  *
  * S-Plan is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation, either version 3 of the License, or any later version.
  *
  * S-Plan is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
  * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with S-Plan. If not, see
  * http://www.gnu.org/licenses/.
  */
 package org.splan.model.units;
 
 import org.netbeans.api.annotations.common.NonNull;
 
 /**
  * Represents a duration in seconds. <code>Time</code> objects do not represent a specific point in time, but rather a
  * duration (a delta to be precise).
  *
  * @author Ritz, Bruno &lt;bruno.ritz@gmail.com&gt;
  */
 public class Time
 	implements Comparable<Time>
 {
 	// <editor-fold defaultstate="collapsed" desc="Instantiation">
 	/**
 	 * Creates a new <code>Time</code> instance that represents the specified number of milliseconds.
 	 *
 	 * @param value
 	 *     The number of milliseconds to represent
 	 *
 	 * @return
 	 *     The instance
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>value</code> is negative
 	 */
 	@NonNull
 	public static Time createFromMilliSeconds(int value)
 		throws IllegalArgumentException
 	{
 		return new Time(value);
 	}
 
 	/**
 	 * Creates a new <code>Time</code> instance that represents the specified number of seconds.
 	 *
 	 * @param value
 	 *     The number of seconds to represent
 	 *
 	 * @return
 	 *     The instance
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>value</code> is negative
 	 */
 	@NonNull
 	public static Time createFromSeconds(int value)
 		throws IllegalArgumentException
 	{
 		return new Time(value * 1000);
 	}
 
 	/**
 	 * Creates a new <code>Time</code> instance.
 	 *
 	 * @param milliseconds
 	 *     The number of milliseconds to represent
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>milliseconds</code> is negative
 	 */
 	private Time(int milliseconds)
 		throws IllegalArgumentException
 	{
 		if (milliseconds < 0)
 		{
 			throw new IllegalArgumentException("milliseconds cannot be negative");
 		}
 
 		this.milliseconds = milliseconds;
 	}
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Value Object">
 	private int milliseconds;
 
 	/**
 	 * Returns the time (difference) in milliseconds.
 	 *
 	 * @return
 	 *     The time
 	 */
 	public int getMilliSeconds()
 	{
 		return this.milliseconds;
 	}
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Comparison">
 	/**
 	 * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
 	 * as this object is less than, equal to, or greater than the specified object.
 	 * <p>
 	 *
 	 * Order is determined from the number of milliseconds represented by either object. The one with the greater number
 	 * of milliseconds is considered greater, two objects representing the same number of milliseconds are considered
 	 * equal.
 	 * <p>
 	 *
 	 * If this object is compared against <code>null</code> this object is considered greater.
 	 *
 	 * @param o
 	 *     The object to compare
 	 *
 	 * @return
 	 *     An integer number as described above
 	 */
 	@Override
 	public int compareTo(Time o)
 	{
 		int retval;
 
 		if (o == null)
 		{
 			retval = 1;
 		}
 		else
 		{
 			retval = this.milliseconds - o.milliseconds;
 		}
 
 		return retval;
 	}
 
 	/**
 	 * Indicates if this object is equal to the specified object. <code>obj</code> is considered <i>equal</i> if the
 	 * following conditions apply:
 	 * <ul>
 	 *     <li><code>obj</code> is not null</li>
	 *     <li><code>obj</code> is of the same class as this object</code>
 	 *     <li><code>obj</code> represents the same number of milliseconds as this object</li>
 	 * </ul>
 	 *
 	 * @param obj
 	 *     The object to check for equality
 	 *
 	 * @return
 	 *     <code>true</code> if <code>obj</code> is equal to this object
 	 */
 	@Override
 	public boolean equals(Object obj)
 	{
 		boolean retval;
 
 		if (obj == null)
 		{
 			retval = false;
 		}
 		else if (this.getClass() != obj.getClass())
 		{
 			retval = false;
 		}
 		else
 		{
 			retval = (this.milliseconds == ((Time) obj).milliseconds);
 		}
 
 		return retval;
 	}
 
 	/**
 	 * Returns a hash code value for this object. The hash code returned by this method is simply the number of
 	 * milliseconds this object represents.
 	 *
 	 * @return
 	 *     The hash code
 	 */
 	@Override
 	public int hashCode()
 	{
 		return this.milliseconds;
 	}
 	// </editor-fold>
 }

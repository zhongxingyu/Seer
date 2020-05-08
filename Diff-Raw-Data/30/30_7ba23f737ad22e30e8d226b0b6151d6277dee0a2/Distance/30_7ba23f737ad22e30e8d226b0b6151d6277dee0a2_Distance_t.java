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
  * Represents a distance in millimeters.
  *
  * @author Ritz, Bruno &lt;bruno.ritz@gmail.com&gt;
  */
 public class Distance
 	implements Comparable<Distance>
 {
 	// <editor-fold defaultstate="collapsed" desc="Instantiation">
 	/**
 	 * Creates a new <code>Distance</code> instance that represents the specified number of millimeters.
 	 *
 	 * @param value
 	 *     The number of millimeters to represent
 	 *
 	 * @return
 	 *     The instance
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>value</code> is negative
 	 */
 	@NonNull
 	public static Distance createFromMillimeters(int value)
 		throws IllegalArgumentException
 	{
 		return new Distance(value);
 	}
 
 	/**
 	 * Creates a new <code>Distance</code> instance that represents the specified number of meters.
 	 *
 	 * @param value
 	 *     The number of meters to represent
 	 *
 	 * @return
 	 *     The instance
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>value</code> is negative
 	 */
 	@NonNull
 	public static Distance createFromMeters(int value)
 		throws IllegalArgumentException
 	{
 		return new Distance(value * 1000);
 	}
 
 	/**
 	 * Creates a new <code>Distance</code> instance.
 	 *
 	 * @param millimeters
 	 *     The number of millimeters to represent
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>millimeters</code> is negative
 	 */
 	private Distance(int millimeters)
 		throws IllegalArgumentException
 	{
 		if (millimeters < 0)
 		{
 			throw new IllegalArgumentException("millimeters cannot be negative");
 		}
 
 		this.millimeters = millimeters;
 	}
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Value Object">
 	private int millimeters;
 
 	/**
 	 * Returns the distance in millimeters.
 	 *
 	 * @return
 	 *     The distance
 	 */
 	public int getMillimeters()
 	{
 		return this.millimeters;
 	}
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Comparison">
 	/**
 	 * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
 	 * as this object is less than, equal to, or greater than the specified object.
 	 * <p>
 	 *
 	 * Order is determined from the number of millimeters represented by either object. The one with the greater number
 	 * of millimeters is considered greater, two objects representing the same number of millimeters are considered
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
 	public int compareTo(Distance o)
 	{
 		int retval;
 
 		if (o == null)
 		{
 			retval = 1;
 		}
 		else
 		{
 			retval = this.millimeters - o.millimeters;
 		}
 
 		return retval;
 	}
 
 	/**
 	 * Indicates if this object is equal to the specified object. <code>obj</code> is considered <i>equal</i> if the
 	 * following conditions apply:
 	 * <ul>
 	 *     <li><code>obj</code> is not null</li>
	 *     <li><code>obj</code> is of the same class as this object</li>
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
 			retval = (this.millimeters == ((Distance) obj).millimeters);
 		}
 
 		return retval;
 	}
 
 	/**
 	 * Returns a hash code value for this object. The hash code returned by this method is simply the number of
 	 * millimeters this object represents.
 	 *
 	 * @return
 	 *     The hash code
 	 */
 	@Override
 	public int hashCode()
 	{
 		return this.millimeters;
 	}
 	// </editor-fold>
 }

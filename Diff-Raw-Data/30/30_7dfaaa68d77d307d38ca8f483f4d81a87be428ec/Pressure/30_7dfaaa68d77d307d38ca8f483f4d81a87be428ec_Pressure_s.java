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
  * Represents a pressure in millibar. <code>Pressure</code> objects do not represent an absolute pressure, but rather a
  * pressure difference.
  *
  * @author Ritz, Bruno &lt;bruno.ritz@gmail.com&gt;
  */
 public class Pressure
 	implements Comparable<Pressure>
 {
 	// <editor-fold defaultstate="collapsed" desc="Instantiation">
 	/**
 	 * Creates a new <code>Temperature</code> instance that represents the specified number of <i>millibar</i>.
 	 *
 	 * @param value
 	 *     The number of millibar to represent
 	 *
 	 * @return
 	 *     The instance
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>value</code> is negative
 	 */
 	@NonNull
 	public static Pressure createFromMillibar(int value)
 		throws IllegalArgumentException
 	{
 		return new Pressure(value);
 	}
 
 	/**
 	 * Creates a new <code>Temperature</code> instance that represents the specified number of <i>bar</i>.
 	 *
 	 * @param value
 	 *     The number of bar to represent
 	 *
 	 * @return
 	 *     The instance
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>value</code> is negative
 	 */
 	@NonNull
 	public static Pressure createFromBar(int value)
 		throws IllegalArgumentException
 	{
 		return new Pressure(value * 1000);
 	}
 
 	/**
 	 * Creates a new <code>Pressure</code> instance.
 	 *
 	 * @param millibar
 	 *     The number of millibar to represent
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>millibar</code> is negative
 	 */
 	private Pressure(int millibar)
 		throws IllegalArgumentException
 	{
 		if (millibar < 0)
 		{
 			throw new IllegalArgumentException("millibar cannot be negative");
 		}
 
 		this.millibar = millibar;
 	}
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Value Object">
 	private int millibar;
 
 	/**
 	 * Returns the pressure (difference) in millibar.
 	 *
 	 * @return
 	 *     The pressure
 	 */
 	public int getMillibar()
 	{
 		return this.millibar;
 	}
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Comparison">
 	/**
 	 * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
 	 * as this object is less than, equal to, or greater than the specified object.
 	 * <p>
 	 *
 	 * Order is determined from the number of millibar represented by either object. The one with the greater number
 	 * of millibar is considered greater, two objects representing the same number of millibar are considered equal.
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
 	public int compareTo(Pressure o)
 	{
 		int retval;
 
 		if (o == null)
 		{
 			retval = 1;
 		}
 		else
 		{
 			retval = this.millibar - o.millibar;
 		}
 
 		return retval;
 	}
 
 	/**
 	 * Indicates if this object is equal to the specified object. <code>obj</code> is considered <i>equal</i> if the
 	 * following conditions apply:
 	 * <ul>
 	 *     <li><code>obj</code> is not null</li>
	 *     <li><code>obj</code> is of the same class as this object</code>
 	 *     <li><code>obj</code> represents the same number of millibar as this object</li>
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
 			retval = (this.millibar == ((Pressure) obj).millibar);
 		}
 
 		return retval;
 	}
 
 	/**
 	 * Returns a hash code value for this object. The hash code returned by this method is simply the number of millibar
 	 * this object represents.
 	 *
 	 * @return
 	 *     The hash code
 	 */
 	@Override
 	public int hashCode()
 	{
 		return this.millibar;
 	}
 	// </editor-fold>
 }

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
 
 /**
  * Represents a temperature. <code>Temperature</code> objects do not represent an absolute temperature, but rather a
  * temperature difference.
  *
  * @author Ritz, Bruno &lt;bruno.ritz@gmail.com&gt;
  */
 public class Temperature
 	implements Comparable<Temperature>
 {
 	// <editor-fold defaultstate="collapsed" desc="Instantiation">
 	/**
 	 * Creates a new <code>Temperature</code> instance that represents the specified number of milli Kelvin.
 	 *
 	 * @param value
 	 *     The number of milli Kelvin to represent
 	 *
 	 * @return
 	 *     The instance
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>value</code> is negative
 	 */
	public static Temperature createFromMilliKelvin(int value)
 		throws IllegalArgumentException
 	{
 		return new Temperature(value);
 	}
 
 	/**
 	 * Creates a new <code>Temperature</code> instance that represents the specified number of Kelvin.
 	 *
 	 * @param value
 	 *     The number of Kelvin to represent
 	 *
 	 * @return
 	 *     The instance
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>value</code> is negative
 	 */
 	public static Temperature createFromKelvin(int value)
 		throws IllegalArgumentException
 	{
 		return new Temperature(value * 1000);
 	}
 
 	/**
 	 * Creates a new <code>Temperature</code> instance.
 	 *
 	 * @param milliKelvin
 	 *     The number of milli Kelvin to represent
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>milliKelvin</code> is negative
 	 */
 	private Temperature(int milliKelvin)
 		throws IllegalArgumentException
 	{
 		if (milliKelvin < 0)
 		{
 			throw new IllegalArgumentException("milliKelvin cannot be negative");
 		}
 
 		this.milliKelvin = milliKelvin;
 	}
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Value Object">
 	private int milliKelvin;
 
 	/**
 	 * Returns the temperature (difference) in milli Kelvin.
 	 *
 	 * @return
 	 *     The temperature
 	 */
 	public int getMilliKelvin()
 	{
 		return this.milliKelvin;
 	}
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Comparison">
 	/**
 	 * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
 	 * as this object is less than, equal to, or greater than the specified object.
 	 * <p>
 	 *
 	 * Order is determined from the number of milli Kelvin represented by either object. The one with the greater number
 	 * of milli Kelvin is considered greater, two objects representing the same number of milli Kelvin are considered
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
 	public int compareTo(Temperature o)
 	{
 		int retval;
 
 		if (o == null)
 		{
 			retval = 1;
 		}
 		else
 		{
 			retval = this.milliKelvin - o.milliKelvin;
 		}
 
 
 		return retval;
 	}
 
 	/**
 	 * Indicates if this object is equal to the specified object. <code>obj</code> is considered <i>equal</i> if the
 	 * following conditions apply:
 	 * <ul>
 	 *     <li><code>obj</code> is not null</li>
 	 *     <li><code>obj</code> is of the same class as this object</li>
 	 *     <li><code>obj</code> represents the same number of milli Kelvin as this object</li>
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
 			retval = (this.milliKelvin == ((Temperature) obj).milliKelvin);
 		}
 
 		return retval;
 	}
 
 	/**
 	 * Returns a hash code value for this object. The hash code returned by this method is simply the number of milli
 	 * Kelvin this object represents.
 	 *
 	 * @return
 	 *     The hash code
 	 */
 	@Override
 	public int hashCode()
 	{
 		return this.milliKelvin;
 	}
 	// </editor-fold>
 }

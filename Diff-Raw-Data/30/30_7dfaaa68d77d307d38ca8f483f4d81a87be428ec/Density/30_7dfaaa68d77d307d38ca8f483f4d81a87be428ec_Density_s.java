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
  * Represents the density of a material in <i>grams per cubic meter</i>.
  *
  * @author Ritz, Bruno &lt;bruno.ritz@gmail.com&gt;
  */
 public class Density
 	implements Comparable<Density>
 {
 	// <editor-fold defaultstate="collapsed" desc="Instantiation">
 	/**
 	 * Creates a new <code>Density</code> instance that represents the specified number of <i>grams per cubic meter</i>.
 	 *
 	 * @param value
 	 *     The number of grams per cubic meter to represent
 	 *
 	 * @return
 	 *     The instance
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>value</code> is negative
 	 */
 	@NonNull
 	public static Density createFromGramsPerCubicMeter(int value)
 		throws IllegalArgumentException
 	{
 		return new Density(value);
 	}
 
 	/**
 	 * Creates a new <code>Density</code> instance.
 	 *
 	 * @param gmsPerCubMtr
 	 *     The number of grams per cubic meter to represent
 	 *
 	 * @throws IllegalArgumentException
 	 *     If <code>gmsPerCubMtr</code> is negative
 	 */
 	private Density(int gmsPerCubMtr)
 		throws IllegalArgumentException
 	{
 		if (gmsPerCubMtr < 0)
 		{
 			throw new IllegalArgumentException("gmsPerCubMtr cannot be negative");
 		}
 
 		this.gmsPerCubMtr = gmsPerCubMtr;
 	}
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Value Object">
 	private int gmsPerCubMtr;
 
 	/**
 	 * Returns the density in grams per cubic meter.
 	 *
 	 * @return
 	 *     The density
 	 */
 	public int getGramsPerCubicMeter()
 	{
 		return this.gmsPerCubMtr;
 	}
 	// </editor-fold>
 
 	// <editor-fold defaultstate="collapsed" desc="Comparison">
 	/**
 	 * Compares this object with the specified object for order. Returns a negative integer, zero, or a positive integer
 	 * as this object is less than, equal to, or greater than the specified object.
 	 * <p>
 	 *
 	 * Order is determined from the number of grams per cubic meter represented by either object. The one with the
 	 * greater number of grams per cubic meter is considered greater, two objects representing the same number of grams
 	 * per cubic meter are considered equal.
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
 	public int compareTo(Density o)
 	{
 		int retval;
 
 		if (o == null)
 		{
 			retval = 1;
 		}
 		else
 		{
 			retval = this.gmsPerCubMtr - o.gmsPerCubMtr;
 		}
 
 		return retval;
 	}
 
 	/**
 	 * Indicates if this object is equal to the specified object. <code>obj</code> is considered <i>equal</i> if the
 	 * following conditions apply:
 	 * <ul>
 	 *     <li><code>obj</code> is not null</li>
	 *     <li><code>obj</code> is of the same class as this object</code>
 	 *     <li><code>obj</code> represents the same number of grams per cubic meter as this object</li>
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
 			retval = (this.gmsPerCubMtr == ((Density) obj).gmsPerCubMtr);
 		}
 
 		return retval;
 	}
 
 	/**
 	 * Returns a hash code value for this object. The hash code returned by this method is simply the number of grams
 	 * per cubic meter this object represents.
 	 *
 	 * @return
 	 *     The hash code
 	 */
 	@Override
 	public int hashCode()
 	{
 		return this.gmsPerCubMtr;
 	}
 	// </editor-fold>
 }

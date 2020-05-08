 /*
 
 This file is part of JSysNet.
 
 JSysNet is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 JSysNet is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with JSysNet.  If not, see <http://www.gnu.org/licenses/>.
 
 */
 
 package edu.purdue.bbc.util;
 
 /**
  * A class for finding whether a particular value falls within a Range.
  */
 public class Range implements Cloneable {
 	private double min;
 	private double max;
 
 	/**
 	 * Creates a new Range from 0 to 1;
 	 */
 	public Range( ) {
 		this( 0, 1 );
 	}
 
 	/**
 	 * Creates a new Range with the given values.
 	 * 
 	 * @param min The minimum value for this Range
 	 * @param max The maximum value for this Range
 	 */
 	public Range( int min, int max ) {
 		this.min = min;
 		this.max = max;
 	}
 
 	/**
 	 * Creates a new Range with the given values.
 	 * 
 	 * @param min The minimum value for this Range
 	 * @param max The maximum value for this Range
 	 */
 	public Range( float min, float max ) {
 		this.min = min;
 		this.max = max;
 	}
 
 	/**
 	 * Creates a new Range with the given values.
 	 * 
 	 * @param min The minimum value for this Range
 	 * @param max The maximum value for this Range
 	 */
 	public Range( double min, double max ) {
 		this.min = min;
 		this.max = max;
 	}
 
 	/**
 	 * Sets the minimum value for this Range
 	 * 
 	 * @param min The new min value for this Range.
 	 */
 	public void setMin( int min ) {
 		this.setMin( (double)min );
 	}
 
 	/**
 	 * Sets the minimum value for this Range
 	 * 
 	 * @param min The new min value for this Range.
 	 */
 	public void setMin( float min ) {
 		this.setMin( (double)min );
 	}
 
 	/**
 	 * Sets the minimum value for this Range
 	 * 
 	 * @param min The new min value for this Range.
 	 */
 	public void setMin( double min ) {
 		this.min = min;
 	}
 
 	/**
 	 * Sets the maximum value for this Range
 	 * 
 	 * @param max The new min value for this Range.
 	 */
 	public void setMax( int max ) {
 		this.setMax( (double)max );
 	}
 
 	/**
 	 * Sets the maximum value for this Range
 	 * 
 	 * @param max The new min value for this Range.
 	 */
 	public void setMax( float max ) {
 		this.setMax( (double)min );
 	}
 
 	/**
 	 * Sets the maximum value for this Range
 	 * 
 	 * @param max The new minimum value for this Range.
 	 */
 	public void setMax( double max ) {
 		this.max = max;
 	}
 
 	/**
 	 * Sets a new minimum and maximum value for this Range.
 	 * 
 	 * @param min The new minimum value for this Range.
 	 * @param max The new maximum value for this Range.
 	 */
 	public void setRange( int min, int max ) {
 		this.setRange( (double)min, (double)max );
 	}
 
 	/**
 	 * Sets a new minimum and maximum value for this Range.
 	 * 
 	 * @param min The new minimum value for this Range.
 	 * @param max The new maximum value for this Range.
 	 */
 	public void setRange( float min, float max ) {
 		this.setRange( (double)min, (double)max );
 	}
 
 	/**
 	 * Sets a new minimum and maximum value for this Range.
 	 * 
 	 * @param min The new minimum value for this Range.
 	 * @param max The new maximum value for this Range.
 	 */
 	public void setRange( double min, double max ) {
 		this.min = min;
 		this.max = max;
 	}
 
 	/**
 	 * Expands this Range by the given amount on both upper and lower bounds.
 	 * 
	 * @param amount The amount to expand this range by.
 	 * @return This range after making the expansion.
 	 */
 	public Range expand( float amount ) {
 		return this.expand( (double)amount );
 	}
 
 	/**
 	 * Expands this Range by the given amount on both upper and lower bounds.
 	 * 
	 * @param amount The amount to expand this range by.
 	 * @return This range after making the expansion.
 	 */
 	public Range expand( double amount ) {
 		this.setRange( min - amount, max + amount );
 		return this;
 	}
 
 	/**
 	 * Scales this Range by the given amount on both upper and lower bounds,
 	 * based on the size of this Range.
 	 * 
 	 * @param amount The amount to expand this range by.
 	 * @return This range after making the expansion.
 	 */
 	public Range scale( float amount ) {
 		return this.scale( (double)amount );
 	}
 
 	/**
 	 * Scales this Range by the given amount on both upper and lower bounds,
 	 * based on the size of this Range.
 	 * 
 	 * @param amount The amount to expand this range by.
 	 * @return This range after making the expansion.
 	 */
 	public Range scale( double amount ) {
 		this.expand( this.size( ) * ( amount - 1 ));
 		return this;
 	}
 
 	/**
 	 * Returns the size of this Range, a.k.a max - min.
 	 * 
 	 * @return The size of this Range.
 	 */
 	public double size( ) {
 		return this.max - this.min;
 	}
 	
 	/**
 	 * Returns the minimum value of this Range.
 	 * 
 	 * @return The minimum value of this Range.
 	 */
 	public double getMin( ) {
 		return this.min;
 	}
 
 	/**
 	 * Returns the maximum value of this Range.
 	 * 
 	 * @return The maximum value of this Range.
 	 */
 	public double getMax( ) {
 		return this.max;
 	}
 
 	/**
 	 * Returns the size of this Range.
 	 * 
 	 * @return the size of this Range.
 	 */
 	public double getSize( ) {
 		return this.max - this.min;
 	}
 
 	/**
 	 * Determines whether the specified value is within the Range.
 	 * 
 	 * @param value The value to check.
 	 * @return true if value is within the range, false otherwise.
 	 */
 	public boolean contains( int value ) {
 		return ( value >= this.min && value <= this.max );
 	}
 	
 	/**
 	 * Determines whether the specified value is within the Range.
 	 * 
 	 * @param value The value to check.
 	 * @return true if value is within the range, false otherwise.
 	 */
 	public boolean contains( float value ) {
 		return ( value >= this.min && value <= this.max );
 	}
 	
 	/**
 	 * Determines whether the specified value is within the Range.
 	 * 
 	 * @param value The value to check.
 	 * @return true if value is within the range, false otherwise.
 	 */
 	public boolean contains( double value ) {
 		return ( value >= this.min && value <= this.max );
 	}
 
 	/**
 	 * Determines whether the specified value is between min and max
 	 *	(not inclusive).
 	 * 
 	 * @param value The value to check
 	 * @return true if value is between min and max, false otherwise.
 	 */
 	public boolean isInside( int value ) {
 		return ( value > this.min && value < this.max );
 	}
 
 	/**
 	 * Determines whether the specified value is between min and max
 	 *	(not inclusive).
 	 * 
 	 * @param value The value to check
 	 * @return true if value is between min and max, false otherwise.
 	 */
 	public boolean isInside( float value ) {
 		return ( value > this.min && value < this.max );
 	}
 
 	/**
 	 * Determines whether the specified value is between min and max
 	 *	(not inclusive).
 	 * 
 	 * @param value The value to check
 	 * @return true if value is between min and max, false otherwise.
 	 */
 	public boolean isInside( double value ) {
 		return ( value > this.min && value < this.max );
 	}
 
 	/**
 	 * Compares the passed in value to the minimum of this Range.
 	 * 
 	 * @param value the value to be compared to the minimum.
 	 * @return true if the passed in value is numerically equal to the minimum.
 	 */
 	public boolean isMin( int value ) {
 		return Double.compare( this.min, (double)value ) == 0;
 	}
 
 	/**
 	 * Compares the passed in value to the minimum of this Range.
 	 * 
 	 * @param value the value to be compared to the minimum.
 	 * @return true if the passed in value is numerically equal to the minimum.
 	 */
 	public boolean isMin( float value ) {
 		return Double.compare( this.min, (double)value ) == 0;
 	}
 
 	/**
 	 * Compares the passed in value to the minimum of this Range.
 	 * 
 	 * @param value the value to be compared to the minimum.
 	 * @return true if the passed in value is numerically equal to the minimum.
 	 */
 	public boolean isMin( double value ) {
 		return Double.compare( this.min, value ) == 0;
 	}
 
 	/**
 	 * Compares the passed in value to the maximum of this Range.
 	 * 
 	 * @param value the value to be compared to the maximum.
 	 * @return true if the passed in value is numerically equal to the maximum.
 	 */
 	public boolean isMax( int value ) {
 		return Double.compare( this.max, (double)value ) == 0;
 	}
 
 	/**
 	 * Compares the passed in value to the maximum of this Range.
 	 * 
 	 * @param value the value to be compared to the maximum.
 	 * @return true if the passed in value is numerically equal to the maximum.
 	 */
 	public boolean isMax( float value ) {
 		return Double.compare( this.max, (double)value ) == 0;
 	}
 
 	/**
 	 * Compares the passed in value to the maximum of this Range.
 	 * 
 	 * @param value the value to be compared to the maximum.
 	 * @return true if the passed in value is numerically equal to the maximum.
 	 */
 	public boolean isMax( double value ) {
 		return Double.compare( this.max, value ) == 0;
 	}
 
 	/**
 	 * Creates a copy of this range
 	 * 
 	 * @return a A new Range that is a copy of this range.
 	 */
 	public Range clone( ) {
 		return new Range( this.min, this.max );
 	}
 
 	/**
 	 * Determines if the two ranges are equal.
 	 * 
 	 * @param r The range to compare this range to.
 	 * @return True if the 2 ranges are equal, false otherwise.
 	 */
 	public boolean equals( Range r ) {
 		return ( this.min == r.getMin( ) && this.max == r.getMax( ));
 	}
 
 	/**
 	 * Returns a sequence within the range separated by increment.
 	 * 
 	 * @param increment the difference between each element in the sequence.
 	 * @return a double array containing a sequence of numbers contained in this
 	 *	Range.
 	 */
 	public double[] getSequence( double increment ) {
 		int count = (int)(( max - min ) / increment ) + 1;
 		return getSequence( count, increment );
 	}
 
 	/**
 	 * Returns a sequence within the range containing the requested number of
 	 * values.
 	 * 
 	 * @param count The number of elements for the sequence to contain.
 	 * @return a double array containing a sequence of numbers contained in this
 	 *	Range.
 	 */
 	public double[] getSequence( int count ) {
 		double increment = ( max - min ) / ( count - 1 );
 		return this.getSequence( count, increment );
 	}
 
 	private double[] getSequence( int count, double increment ) {
 		if ( sequence != null && sequence.length == count )
 			return sequence;
 		sequence = new double[ count ];
 		for( int i=0; i < count; i++ ) {
 			sequence[ i ] = min + i * increment;
 		}
 		return sequence;
 
 	}
 	private double[] sequence;
 
 	/**
 	 * Returns a string representation of this range.
 	 * 
 	 * @return A string representation of this range.
 	 */
 	public String toString( ) {
 		return String.format( "%f - %f", this.min, this.max );
 	}
 }
 
 

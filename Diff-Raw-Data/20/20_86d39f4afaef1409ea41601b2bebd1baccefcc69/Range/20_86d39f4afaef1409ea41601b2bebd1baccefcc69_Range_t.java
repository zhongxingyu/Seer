 /*
  *  ExperienceMod - Bukkit server plugin for modifying the experience system in Minecraft.
  *  Copyright (C) 2012 Kristian S. Stangeland
  *
  *  This program is free software; you can redistribute it and/or modify it under the terms of the 
  *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along with this program; 
  *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  *  02111-1307 USA
  */
 
 package com.comphenix.xp;
 
 import java.util.Random;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 public class Range {
 	private double start;
 	private double end;
 	
 	public static Range DEFAULT = new Range(0);
 	
 	/**
 	 * Constructs a range of the form [value, value].
 	 * @param value The end value.
 	 */
 	public Range(double value) {
 		this.start = value;
 		this.end = value;
 	}
 	
 	public Range(double start, double end) {
 		if (end < start)
 			throw new IllegalArgumentException("Illegal range. The first value must be less than the last.");
 		
 		this.start = start;
 		this.end = end;
 	}
 	
 	public double getStart() {
 		return start;
 	}
 	
 	public double getEnd() {
 		return end;
 	}
 	
 	public Range multiply(double multiply) {
 		// Consider the average value of a uniform distribution
 		return new Range(start * multiply, end * multiply);
 	}
 	
 	public double sampleDouble(Random rnd) {
 		if (start == end)
 			return start;
 		else
 			return start + (end - start) * rnd.nextDouble();
 	}
 	
 	public int getMinimum() {
 		return (int) Math.floor(start);
 	}
 	
 	public int getMaximum() {
 		return (int) Math.ceil(end);
 	}
 	
 	public int sampleInt(Random rnd) {
 
 		/*
 		 * Imagine our range is 0.7 - 5.3:
 		 *
 		 * 0.7  1          2          3          4          5  5.3
 	     *	
 		 *  |---|----------|----------|----------|----------|---|
 		 *  |   |          |          |          |          |   |
 		 *  |   |          |          |          |          |   |
 		 *  |---|----------|----------|----------|----------|---|
 		 *
 		 * The integer part, 1 - 5, is easy. To get a random number between and
 		 * including 1 and 5, we simply get a random number between 0 and 4 
 		 * and add one.
 	 	 * 
 		 * The beginning, 0.7 - 1.0, covers 30% of an integer. One interpretation is
 		 * that this indicates the probability of getting that integer. 
 		 *
 		 * So, we end up with a 30% probability of getting 0 and 5.3 - 5 = 30% 
 		 * probability of getting 4.
 		 */
 
 		int value = 0;
 		
 		// Convert the range to an integer equivalent. 
 		// Notice that we round to shrink the range.
 		int a = (int) Math.ceil(start); 
 		int b = (int) Math.floor(end);
 		
 		// Special case
 		if ((int)start == (int)end) {
 			return sampleIntReduced(rnd);
 		}
 		
 		// The decimal leftover
 		double dA = a - start;
 		double dB = end - b;
 		
 		// Sample an integer from the range [a, b] (inclusive)
 		if (b > a) {
 			value = a + rnd.nextInt(b - a + 1); // Add one since nextInt is exclusive
 		}
 		
 		// The remainder is the probability of choosing the previous value
 		if (dA > 0 && rnd.nextDouble() < dA)
 			value--;
 		
 		// And here it is the probability of choosing the next value
 		if (dB > 0 && rnd.nextDouble() < dB)
 			value++;
 		
 		return value;
 	}
 	
 	private int sampleIntReduced(Random rnd) {
 		double value = sampleDouble(rnd);
 		
 		// Probability of adding the fraction
 		double fraction = value - Math.floor(value);
 		double toAdd = rnd.nextDouble() < fraction ? Math.signum(value) : 0;
 		
 		return (int)value + (int)toAdd;
 	}
 
 	@Override
 	public int hashCode() {
 		return new HashCodeBuilder(17, 31).
 	            append(start).
 	            append(end).
 	            toHashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null)
             return false;
         if (obj == this)
             return true;
         if (obj.getClass() != getClass())
             return false;
 
         Range other = (Range) obj;
         return new EqualsBuilder().
             append(start, other.start).
             append(end, other.end).
             isEquals();
 	}
 
 	@Override
 	public String toString() {
 		if (start == end)
 			return "{" + start + "}";
 		else
			return "{start: " + start + ", end: " + end + "}";
 	}
 }

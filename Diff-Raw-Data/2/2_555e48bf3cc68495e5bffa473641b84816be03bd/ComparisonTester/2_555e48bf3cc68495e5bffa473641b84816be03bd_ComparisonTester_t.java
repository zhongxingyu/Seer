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
 package org.splan.testing;
 
 /**
  * A helper class that assists in testing the correct implementation of the <code>equals()</code>,
  * <code>hashCode()</code> and <code>compareTo()</code> methods.
  * <p>
  *
  * This is a utility class whose purpose is simply to relieve the programmer from having to write such tests manually
  * over and over again.
  *
  * @author Ritz, Bruno &lt;bruno.ritz@gmail.com&gt;
  */
 public class ComparisonTester
 {
 	/**
 	 * Private constructor as this is a utility class.
 	 */
 	private ComparisonTester()
 	{
 	}
 
 	/**
 	 * Validates the <code>compareTo()</code> method for correctness. This method requires that <code>compareTo()</code>
 	 * be consistent with <code>equals()</code>.
 	 *
 	 * @param <T>
 	 *     The type of objects under test
 	 * @param equal1
 	 *     An object that must be equal to <code>equal2</code>
 	 * @param equal2
 	 *     An object that must be equal to <code>equal1</code>
 	 * @param greater
 	 *     An object that must be greater than <code>equal1</code>
 	 * @param less
 	 *     An object that must be greater than <code>equal1</code>
 	 *
 	 * @throws ValidationException
 	 *     If <code>compareTo()</code> is not behaving properly
 	 */
 	private static <T extends Comparable> void testCompareTo(T equal1, T equal2, T greater, T less)
 		throws ValidationException
 	{
 		// Equal objects
 		if ((equal1.compareTo(equal2) != 0) || (equal2.compareTo(equal1) != 0))
 		{
 			throw new ValidationException("compareTo() not consistent with equals()");
 		}
 
 		// Greater other
 		if ((equal1.compareTo(greater) >= 0) || (greater.compareTo(equal1) <= 0))
 		{
 			throw new ValidationException("Greater object not considered greater");
 		}
 
 		// Less other
 		if ((equal1.compareTo(less) <= 0) || (less.compareTo(equal1) >= 0))
 		{
 			throw new ValidationException("Less object not considered less");
 		}
 
 		// Transitive
 		if ((less.compareTo(greater) >= 0) || (greater.compareTo(less) <= 0))
 		{
 			throw new ValidationException("compareTo() is not transitive");
 		}
 	}
 
 	/**
	 * Validates the <code>equals()</code> and <code>hashCode()</code> methods for correctness.
 	 *
 	 * @param <T>
 	 *     The type of objects under test
 	 * @param equal1
 	 *     An object that must be equal to <code>equal2</code>
 	 * @param equal2
 	 *     An object that must be equal to <code>equal1</code>
 	 * @param different
 	 *     An object that must be different from <code>equal1</code>
 	 *
 	 * @throws ValidationException
 	 *     If <code>hashCode()</code> is not behaving properly
 	 */
 	public static <T> void testEqualsHashCode(T equal1, T equal2, T different)
 		throws ValidationException
 	{
 		// equals()
 		if (!(equal1.equals(equal2) && equal2.equals(equal1)))
 		{
 			throw new ValidationException("Equal objects considered different");
 		}
 
 		if (equal1.equals(different) || different.equals(equal1))
 		{
 			throw new ValidationException("Different object considered equal");
 		}
 
 		if (!equal1.equals(equal1))
 		{
 			throw new ValidationException("Same object considered different");
 		}
 
 		if (equal1.equals(null))
 		{
 			throw new ValidationException("null object considered equal");
 		}
 
 		// hashCode()
 		if (equal1.hashCode() != equal2.hashCode())
 		{
 			throw new ValidationException("Equal objects produced different hash codes");
 		}
 
 		if (equal1.hashCode() != equal1.hashCode())
 		{
 			throw new ValidationException("Multiple invocations of hashCode() produced different results");
 		}
 	}
 
 	/**
 	 * Validates the <code>compareTo()</code> and <code>hashCode()</code> methods for correctness. This method requires
 	 * that <code>compareTo()</code> be consistent with <code>equals()</code>.
 	 *
 	 * @param <T>
 	 *     The type of objects under test
 	 * @param equal1
 	 *     An object that must be equal to <code>equal2</code>
 	 * @param equal2
 	 *     An object that must be equal to <code>equal1</code>
 	 * @param greater
 	 *     An object that must be greater than <code>equal1</code>
 	 * @param less
 	 *     An object that must be greater than <code>equal1</code>
 	 *
 	 * @throws ValidationException
 	 *     If <code>compareTo()</code>, <code>equals()</code> or <code>hashCode()</code> is not behaving properly
 	 */
 	public static <T extends Comparable> void validateComparisonMethods(T equal1, T equal2, T greater, T less)
 		throws ValidationException
 	{
 		ComparisonTester.testEqualsHashCode(equal1, equal2, greater);
 		ComparisonTester.testCompareTo(equal1, equal2, greater, less);
 	}
 }

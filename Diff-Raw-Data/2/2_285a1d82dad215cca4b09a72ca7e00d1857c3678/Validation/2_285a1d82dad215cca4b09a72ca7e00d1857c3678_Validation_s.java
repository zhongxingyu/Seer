 /*
  * utils - Validation.java - Copyright © 2009 David Roden
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.pterodactylus.util.validation;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * <p>
  * Helps with parameter validation. Parameters can be checked using a construct
  * like this:
  * </p>
  * <code><pre>
  * public void copy(Object[] object, int leftValue, int ríghtValue) {
  *     Validation.begin().isNotNull(object, &quot;object&quot;).check()
  *         .isPositive(leftValue, &quot;leftValue&quot;).isLess(leftValue, object.length, &quot;leftValue&quot;).check()
  *         .isPositive(rightValue, &quot;rightValue&quot;).isLess(rightValue, object.length, &quot;rightValue&quot;).isGreater(rightValue, leftValue, &quot;rightValue&quot;).check();
  *     // do something with the values
  * }
  * </pre></code>
  * <p>
  * This example will perform several checks. Only the {@link #check()} method
  * will throw an {@link IllegalArgumentException} if one of the previous checks
  * failed, so you can gather several reasons for a validation failure before
  * throwing an exception which will in turn decrease the time spent in
  * debugging.
  * </p>
  * <p>
  * In the example, <code>object</code> is first checked for a non-
  * <code>null</code> value and an {@link IllegalArgumentException} is thrown if
  * <code>object</code> is <code>null</code>. Afterwards <code>leftValue</code>
  * is checked for being a positive value that is also smaller than the length of
  * the array <code>object</code>. The {@link IllegalArgumentException} that is
  * thrown if the checks failed will contain a message for each of the failed
  * checks. At last <code>rightValue</code> is checked for being positive,
  * smaller than the array’s length and larger than <code>leftValue</code>.
  * </p>
  * <p>
  * Remember to call the {@link #check()} method after performing the checks,
  * otherwise the {@link IllegalArgumentException} will never be thrown!
  * </p>
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class Validation {
 
 	/** The list of failed checks. */
 	private List<String> failedChecks;
 
 	/**
 	 * Private constructor to prevent construction from the outside.
 	 */
 	private Validation() {
 		/* do nothing. */
 	}
 
 	/**
 	 * Adds a check to the list of failed checks, instantiating a new list if
 	 * {@link #failedChecks} is still <code>null</code>.
 	 *
 	 * @param check
 	 *            The check to add
 	 */
 	private void addFailedCheck(String check) {
 		if (failedChecks == null) {
 			failedChecks = new ArrayList<String>();
 		}
 		failedChecks.add(check);
 	}
 
 	/**
 	 * Returns a new {@link Validation} object.
 	 *
 	 * @return A new validation
 	 */
 	public static Validation begin() {
 		return new Validation();
 	}
 
 	/**
 	 * Checks if one of the previous checks failed and throws an
 	 * {@link IllegalArgumentException} if a previous check did fail.
 	 *
 	 * @return This {@link Validation} object to allow method chaining
 	 * @throws IllegalArgumentException
 	 *             if a previous check failed
 	 */
 	public Validation check() throws IllegalArgumentException {
 		if (failedChecks == null) {
 			return this;
 		}
 		StringBuilder message = new StringBuilder();
 		message.append("Failed checks: ");
 		for (String failedCheck : failedChecks) {
 			message.append(failedCheck).append(", ");
 		}
 		message.setLength(message.length() - 2);
 		message.append('.');
 		throw new IllegalArgumentException(message.toString());
 	}
 
 	/**
 	 * Checks if the given object is not <code>null</code>.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param object
 	 *            The object to check
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isNotNull(String objectName, Object object) {
 		if (object == null) {
 			addFailedCheck(objectName + " should not be null");
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if the given object is <code>null</code>.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param object
 	 *            The object to check
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isNull(String objectName, Object object) {
 		if (object != null) {
 			addFailedCheck(objectName + " should be null");
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if <code>value</code> is less than <code>upperBound</code>.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @param upperBound
 	 *            The upper bound to check <code>value</code> against
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isLess(String objectName, long value, long upperBound) {
 		if (value >= upperBound) {
 			addFailedCheck(objectName + " should be < " + upperBound + " but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if <code>value</code> is less than <code>upperBound</code>.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @param upperBound
 	 *            The upper bound to check <code>value</code> against
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isLess(String objectName, double value, double upperBound) {
 		if (value >= upperBound) {
 			addFailedCheck(objectName + " should be < " + upperBound + " but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if <code>value</code> is less than <code>upperBound</code>.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @param upperBound
 	 *            The upper bound to check <code>value</code> against
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isLessOrEqual(String objectName, long value, long upperBound) {
 		if (value > upperBound) {
 			addFailedCheck(objectName + " should be <= " + upperBound + " but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if <code>value</code> is less than <code>upperBound</code>.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @param upperBound
 	 *            The upper bound to check <code>value</code> against
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isLessOrEqual(String objectName, double value, double upperBound) {
 		if (value > upperBound) {
 			addFailedCheck(objectName + " should be <= " + upperBound + " but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if the given value is equal to the expected value.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @param expected
 	 *            The expected value to check <code>value</code> against
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isEqual(String objectName, long value, long expected) {
 		if (value != expected) {
 			addFailedCheck(objectName + " should be == " + expected + " but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if the given value is equal to the expected value.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @param expected
 	 *            The expected value to check <code>value</code> against
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isEqual(String objectName, double value, double expected) {
 		if (value != expected) {
 			addFailedCheck(objectName + " should be == " + expected + " but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if the given value is equal to the expected value.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @param expected
 	 *            The expected value to check <code>value</code> against
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isEqual(String objectName, boolean value, boolean expected) {
 		if (value != expected) {
 			addFailedCheck(objectName + " should be == " + expected + " but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if the given value is equal to the expected value.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @param expected
 	 *            The expected value to check <code>value</code> against
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isEqual(String objectName, Object value, Object expected) {
 		if (!value.equals(expected)) {
 			addFailedCheck(objectName + " should equal " + expected + " but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if the given value is the same as the expected value.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @param expected
 	 *            The expected value to check <code>value</code> against
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isSame(String objectName, Object value, Object expected) {
 		if (value != expected) {
 			addFailedCheck(objectName + " should be == " + expected + " but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if the given value is not equal to the expected value.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @param expected
 	 *            The expected value to check <code>value</code> against
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isNotEqual(String objectName, long value, long expected) {
 		if (value == expected) {
 			addFailedCheck(objectName + " should be != " + expected + " but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if the given value is not equal to the expected value.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @param expected
 	 *            The expected value to check <code>value</code> against
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isNotEqual(String objectName, double value, double expected) {
 		if (value == expected) {
 			addFailedCheck(objectName + " should be != " + expected + " but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if the given value is not equal to the expected value.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @param expected
 	 *            The expected value to check <code>value</code> against
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isNotEqual(String objectName, boolean value, boolean expected) {
 		if (value == expected) {
 			addFailedCheck(objectName + " should be != " + expected + " but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if <code>value</code> is greater than <code>lowerBound</code>.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @param lowerBound
 	 *            The lower bound to check <code>value</code> against
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isGreater(String objectName, long value, long lowerBound) {
 		if (value <= lowerBound) {
 			addFailedCheck(objectName + " should be > " + lowerBound + " but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if <code>value</code> is greater than <code>lowerBound</code>.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @param lowerBound
 	 *            The lower bound to check <code>value</code> against
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isGreater(String objectName, double value, double lowerBound) {
 		if (value <= lowerBound) {
 			addFailedCheck(objectName + " should be > " + lowerBound + " but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if <code>value</code> is greater than <code>lowerBound</code>.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @param lowerBound
 	 *            The lower bound to check <code>value</code> against
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isGreaterOrEqual(String objectName, long value, long lowerBound) {
 		if (value < lowerBound) {
 			addFailedCheck(objectName + " should be >= " + lowerBound + " but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if <code>value</code> is greater than <code>lowerBound</code>.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @param lowerBound
 	 *            The lower bound to check <code>value</code> against
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isGreaterOrEqual(String objectName, double value, double lowerBound) {
 		if (value < lowerBound) {
 			addFailedCheck(objectName + " should be >= " + lowerBound + " but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if the given value is greater to or equal to <code>0</code>.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isPositive(String objectName, long value) {
 		if (value < 0) {
 			addFailedCheck(objectName + " should be >= 0 but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if the given value is greater to or equal to <code>0</code>.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isPositive(String objectName, double value) {
 		if (value < 0) {
 			addFailedCheck(objectName + " should be >= 0 but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if the given value is less than <code>0</code>.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isNegative(String objectName, long value) {
 		if (value >= 0) {
 			addFailedCheck(objectName + " should be < 0 but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks if the given value is less than <code>0</code>.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The value to check
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isNegative(String objectName, double value) {
 		if (value >= 0) {
 			addFailedCheck(objectName + " should be < 0 but was " + value);
 		}
 		return this;
 	}
 
 	/**
 	 * Checks whether the given object is assignable to an object of the given
 	 * class.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param object
 	 *            The object to check
 	 * @param clazz
 	 *            The class the object should be representable as
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isInstanceOf(String objectName, Object object, Class<?> clazz) {
		if (!object.getClass().isAssignableFrom(clazz)) {
 			addFailedCheck(objectName + " should be a kind of " + clazz.getName() + " but is " + object.getClass().getName());
 		}
 		return this;
 	}
 
 	/**
 	 * Checks whether the given value is one of the expected values.
 	 *
 	 * @param objectName
 	 *            The object’s name
 	 * @param value
 	 *            The object’s value
 	 * @param expectedValues
 	 *            The expected values
 	 * @return This {@link Validation} object to allow method chaining
 	 */
 	public Validation isOneOf(String objectName, Object value, Object... expectedValues) {
 		List<?> values;
 		if (!(values = Arrays.asList(expectedValues)).contains(value)) {
 			addFailedCheck(objectName + " should be one of " + values + " but is " + value);
 		}
 		return this;
 	}
 
 }

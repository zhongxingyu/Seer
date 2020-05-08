 package ch.bitwave.platform.codestyle;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 import org.hamcrest.StringDescription;
 
 /**
  * This Design By Contract implementation allows preconditions, interim and post
  * conditions. Hamcrest matching is supported.
  */
 public class DBC {
 
 	public static final DBC INTERIM = new DBC(InterimConditionException.class);
 	public static final DBC POST = new DBC(PostConditionException.class);
 	public static final DBC PRE = new DBC(PreConditionException.class);
 
 	private Class<? extends DBCException> exceptionClass;
 
 	private DBC(final Class<? extends DBCException> exceptionClass) {
 		this.exceptionClass = exceptionClass;
 	}
 
 	/**
 	 * Asserts that the given value is false, throwing a DBCException with the
 	 * given message if it is not.
 	 * 
 	 * @param value
 	 *            the value to check.
 	 * @param message
 	 *            the message to throw along with the DBCException if the value
 	 *            is false.
 	 */
 	public void assertFalse(final String message, final boolean value) {
		if (value)
 			throwException(message);
 	}
 
 	public void assertGreaterThan(final int actual, final int limit, final String valueName) {
 		if (actual <= limit) {
 			throwException(String.format(
 					"The %s must be greater than %s, but is %s.", valueName, limit, actual)); //$NON-NLS-1$
 		}
 
 	}
 
 	/**
 	 * Asserts that the given actual is not null, by throwing a DBCException
 	 * mentioning the given valueName if it is.
 	 * 
 	 * @param actual
 	 *            the object to check against null.
 	 * @param valueName
 	 *            the name of the object, which is formatted into the message
 	 *            "The %s must not be null.".
 	 */
 	public void assertNotNull(@Nullable final Object actual, final String valueName) {
 		if (actual == null) {
 			String message = String.format("The %s must not be null.", valueName);
 			throwException(message);
 		}
 	}
 
 	/**
 	 * Asserts that the given double is not NaN.
 	 * 
 	 * @param actual
 	 *            the value to check against NaN.
 	 * @param valueName
 	 *            the name of the value, which is formatted into the exception
 	 *            message "The %s must be a number.".
 	 */
 	public void assertNumber(final double actual, final String valueName) {
 		if (Double.isNaN(actual)) {
 			throwException(String.format("The %s must be a number.", valueName));
 		}
 
 	}
 
 	public <T> void assertThat(final T actual, @Nonnull final Matcher<T> matcher) {
 		if (!matcher.matches(actual)) {
 			describeAndThrow(actual, matcher);
 		}
 	}
 
 	/**
 	 * Asserts that the given value is true, throwing a DBCException with the
 	 * given message if it is not.
 	 * 
 	 * @param value
 	 *            the value to check.
 	 * @param message
 	 *            the message to throw along with the DBCException if the value
 	 *            is false.
 	 */
 	public void assertTrue(final String message, final boolean value) {
 		if (!value)
 			throwException(message);
 	}
 
 	private <T> void describeAndThrow(final T actual, @Nonnull final Matcher<T> matcher) {
 		Description description = new StringDescription();
 		description.appendText("Expected: ");
 		description.appendDescriptionOf(matcher);
 		description.appendText("\nbut got ");
 		description.appendValue(actual);
 		String message = description.toString();
 		throwException(message);
 	}
 
 	private void throwException(@Nonnull final String message) {
 		DBCException dbce;
 		try {
 			dbce = this.exceptionClass.getConstructor(String.class).newInstance(message);
 		} catch (Exception e) {
 			throw new RuntimeException(String.format(
 					"Failed to create an instance of the required DBCException type %s due to %s",
 					this.exceptionClass.getSimpleName(), e.getMessage()), e);
 		}
 		throw dbce;
 	}
 
 }

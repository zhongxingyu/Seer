 package de.hapm.test;
 
 public class Matchers {
 	/**
 	 * Creates a Matcher, that checks if the {@link ThrowingContext} to match, throws
 	 * the given exception, when it is called with the given value.
 	 * 
 	 * @param exception The exception class of the exception, that should be thrown by
 	 *                  the {@link ThrowingContext}.
 	 * @param badValue  The value that will cause the exception to be thrown.
	 * @return			The generated {@link ExceptionMatcher} for the given parameters.
 	 */
 	public static <T, S extends ThrowingContext<T>> ExceptionMatcher<T, S> throwsEx(
 			Class<? extends Throwable> exception, T badValue) {
 		return new ExceptionMatcher<T, S>(exception).andOn(badValue);
 	}
 }

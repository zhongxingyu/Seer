 package org.gjt.sp.jedit.search;
 
 import static junit.framework.Assert.assertNotNull;
 import junit.framework.Assert;
 
 import org.gjt.sp.jedit.search.SearchMatcher.Match;
 
 /**
  * Collection of {@link Assert} like static methods for asserting
  * the return values of {@link SearchMatcher} implementations in a 
  * single line fashion. All methods throw {@link AssertionError} on
  * failed assertions.
  * @author joonas
  */
 public abstract class MatchAssert {
 	
 	/**
 	 * Asserts that the match {@code m} has the given {@code start} and {@code end} values.
 	 * @param start the expected start value
 	 * @param end the expected end value
 	 * @param m the match (asserted not null)
 	 */
 	public static void assertMatch(int start, int end, Match m) {
 		assertNotNull("no match", m);
 		assertMatch(start, end, m.start, m.end);
 	}
 	
 	/**
 	 * asserts that the match returned from a reverse match has the correct {@code start} and
	 * {@code end} values. This differs from {@link #assertReverseMatch(int, int, int, Match)} by requring
 	 * the matched {@code text} in order to reverse the expected start and end values. 
 	 * @param text the matched text (used for length only)
 	 * @param start the non-reversed expected start
 	 * @param end the non-reversed expected end
 	 * @param m the returned match
 	 */
 	public static void assertReverseMatch(String text, int start, int end, Match m) {
 		assertNotNull("null matched text", text);
 		assertReverseMatch(text.length(), start, end, m);
 	}
 	
 	/**
 	 * Just like {@link #assertMatch(String, int, int, int, int)} but can be used without
 	 * the matched text.
 	 * @param textLength the length of the matched text
 	 * @param start the non-reversed expected start
 	 * @param end the non-reversed expected end
 	 * @param m the returned match
 	 */
 	public static void assertReverseMatch(int textLength, int start, int end, Match m) {
 		assertNotNull("no match", m);
 		
 		int matchLength = m.end - m.start;
 		int correctedEnd = textLength - m.start;
 		int correctedStart = correctedEnd - matchLength;
 		assertMatch(start, end, correctedStart, correctedEnd);
 	}
 	
 	/**
 	 * Messageless version of {@link #assertMatch(String, int, int, int, int)}.
 	 * @param start the expected start
 	 * @param end the expected end
 	 * @param actualStart the actual start
 	 * @param actualEnd the actual end
 	 */
 	public static void assertMatch(int start, int end, int actualStart, int actualEnd) {
 		assertMatch(null, start, end, actualStart, actualEnd);
 	}
 	
 	/**
 	 * Asserts that the ({@code start}, {@code end}) matches ({@code actualStart}, {@code actualEnd}).
 	 * If the assertion fails, the {@code message} (when not null) is prepended to the assertion message
 	 * explaining the difference between the two tuples. 
 	 * @param message the optional assertion message
 	 * @param start the expected start
 	 * @param end the expected end
 	 * @param actualStart the actual start ({@link Match#start})
 	 * @param actualEnd the actual end ({@link Match#end})
 	 */
 	public static void assertMatch(String message, int start, int end, int actualStart, int actualEnd) {
 		assertIndex("start", start);
 		assertIndex("end", end);
 		assertIndex("m.start", actualStart);
 		assertIndex("m.end", actualEnd);
 		
 		if (start != actualStart || end != actualEnd) {
 			throw new AssertionError(
 				String.format(
 					(message != null ? message + ": " : "") 
 					+ "Expected <%d, %d> but was: <%d, %d>", start, end, actualStart, actualEnd));
 		}
 	}
 	
 	/**
 	 * Asserts that the index is a valid index in a match; index must be greater or equal than zero.
 	 * @see CharSequence#charAt(int)
 	 * @param name the name of the parameter to validate, optional
 	 * @param index the index value
 	 */
 	protected static void assertIndex(String name, int index) {
 		if (index < 0) {
 			throw new AssertionError(
 				String.format("Invalid index%s: %d", (name != null ? " for " + name : ""), index));
 		}
 	}
 }

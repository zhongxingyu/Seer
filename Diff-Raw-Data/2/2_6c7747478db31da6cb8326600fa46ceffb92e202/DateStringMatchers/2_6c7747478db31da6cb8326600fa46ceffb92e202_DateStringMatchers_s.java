 package de.jardas.hamcrest.joda;
 
 import static org.hamcrest.Condition.matched;
 import static org.hamcrest.Condition.notMatched;
 
 import org.hamcrest.*;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 
 public final class DateStringMatchers {
 	private DateStringMatchers() {
 		// utility class
 	}
 
 	public static Matcher<String> dateString(final DateTimeFormatter formatter, final Matcher<DateTime> valueMatcher) {
 		return new DateStringValueMatcher(formatter, valueMatcher);
 	}
 
 	public static Matcher<String> dateString(final DateTimeFormatter formatter) {
 		return new DateStringFormatMatcher(formatter);
 	}
 
 	private static final class DateStringFormatMatcher extends TypeSafeDiagnosingMatcher<String> {
 		private final DateTimeFormatter formatter;
 
 		public DateStringFormatMatcher(final DateTimeFormatter formatter) {
 			this.formatter = formatter;
 		}
 
 		@Override
 		protected boolean matchesSafely(final String item, final Description mismatch) {
 			if (item == null) {
				mismatch.appendText("was ").appendValue(item);
 				return false;
 			}
 
 			try {
 				formatter.parseDateTime(item);
 				return true;
 			} catch (final IllegalArgumentException e) {
 				mismatch.appendText("no valid date (" + e.getMessage() + ")");
 				return false;
 			}
 		}
 
 		@Override
 		public void describeTo(final Description description) {
 			description.appendText("valid date");
 		}
 	}
 
 	private static final class DateStringValueMatcher extends TypeSafeDiagnosingMatcher<String> {
 		private final DateTimeFormatter formatter;
 		private final Matcher<DateTime> valueMatcher;
 
 		public DateStringValueMatcher(final DateTimeFormatter formatter, final Matcher<DateTime> valueMatcher) {
 			this.formatter = formatter;
 			this.valueMatcher = valueMatcher;
 		}
 
 		@Override
 		protected boolean matchesSafely(final String item, final Description mismatch) {
 			return parseDate(item, mismatch).matching(valueMatcher);
 		}
 
 		private Condition<DateTime> parseDate(final String item, final Description mismatch) {
 			if (item == null) {
 				return matched(null, mismatch);
 			}
 
 			try {
 				final DateTime date = formatter.parseDateTime(item);
 				return matched(date, mismatch);
 			} catch (final IllegalArgumentException e) {
 				mismatch.appendText("no valid date (" + e.getMessage() + ")");
 				return notMatched();
 			}
 		}
 
 		@Override
 		public void describeTo(final Description description) {
 			description.appendText("date matching ").appendDescriptionOf(valueMatcher);
 		}
 	}
 }

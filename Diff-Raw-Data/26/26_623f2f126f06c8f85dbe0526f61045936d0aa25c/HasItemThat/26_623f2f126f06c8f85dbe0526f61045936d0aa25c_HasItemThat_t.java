 package edu.teco.dnd.tests;
 
 import static org.hamcrest.CoreMatchers.not;
 
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 import org.hamcrest.TypeSafeMatcher;
 
public class HasItemThat<T> extends TypeSafeMatcher<Iterable<? super T>> {
 	private final Matcher<T> subMatcher;
 
 	public HasItemThat(final Matcher<T> subMatcher) {
 		this.subMatcher = subMatcher;
 	}
 
 	/**
 	 * A matcher for Iterables which contain at least on item that is matched by <code>subMatcher</code>.
 	 * 
 	 * @param subMatcher
 	 *            is used to test the items of the Iterable
 	 * @return a Matcher that tests that an Iterable contains at least one item matched by <code>subMatcher</code>
 	 */
 	public static <T> HasItemThat<T> hasItemThat(final Matcher<T> subMatcher) {
 		return new HasItemThat<T>(subMatcher);
 	}
 
 	/**
 	 * A matcher for Iterables that contain no item that is matched by <code>subMatcher</code>
 	 * 
 	 * @see #hasItemThat(Matcher)
 	 */
	public static <T> Matcher<Iterable<? super T>> hasNoItemThat(final Matcher<T> subMatcher) {
 		return not(hasItemThat(subMatcher));
 	}
 
 	@Override
 	public void describeTo(final Description description) {
 		description.appendText("an Iterable that contains an item that ").appendDescriptionOf(subMatcher);
 	}
 
 	@Override
	public boolean matchesSafely(final Iterable<? super T> actual) {
		for (final Object item : actual) {
 			if (subMatcher.matches(item)) {
 				return true;
 			}
 		}
 		return false;
 	}
 }

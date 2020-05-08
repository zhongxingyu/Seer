 // © Maastro, 2013
 package nl.maastro.eureca.aida.indexer.tika.parser;
 
 import java.util.Comparator;
 
 /**
  * Compare two Strings treating {@code null} values as equal.
  * 
  * @author Kasper van den Berg <kasper.vandenberg@maastro.nL> <kasper@kaspervandenberg.net>
  */
 public class NullStringComperator implements Comparator<String> {
 	private static final int oneLessThanTwo = -1;
 	private static final int oneEqualToTwo = 0;
 	private static final int oneGreaterThanTwo = 1;
 
 	/**
 	 * Compare two Strings treating {@code null} values as less than any
 	 * other String value.
 	 *
 	 * <p>For non-null arguments this comparator behaves as
 	 * {@code o1.}{@link String#compareTo(java.lang.String) compareTo(o2)}.</p>
 	 *
 	 * <p><i>NOTE: empty strings and null values are different:
 	 * {@code compare("", null) != 0}.</i></p>
 	 *
 	 * @param o1	<ul><li>a {@link String} to compare with {@code o2};<</li>
 	 * 				<li>{@code null}.</li></ul>
 	 * @param o2	<ul><li>a {@link String} to compare with {@code o1};<</li>
 	 * 				<li>{@code null}.</li></ul>
 	 * @return <ul><li>{@value #oneEqualToTwo}, when the two Strings are
 	 * 			equal or both arguments are {@code null};</li>
 	 * 		<li>negative integer, when {@code o1} is lexicolographically
 	 * 			less than {@code o2} or {@code o1} is {@code null} and
 	 * 			{@code o2} is not; or</li>
 	 * 		<li>positive integer, when {@code o1} is lexicolographically
 	 * 			greater than {@code o2} or {@code o2} is {@code null} and
 	 * 			{@code o1} is not.</li></ul>
 	 */
 	@Override
 	public int compare(String o1, String o2) {
 		if (o1 != null) {
 			if (o2 != null) {
 				return o1.compareTo(o2);
 			} else {
 				return oneGreaterThanTwo;
 			}
 		} else {
 			if (o2 != null) {
 				return oneLessThanTwo;
 			} else {
 				return oneEqualToTwo;
 			}
 		}
 	}
 
 	/**
 	 * Check whether {@code target} starts with {@code prefix}, and allowing
 	 * {@code null} arguments.
 	 *
 	 * <p><i>For non null arguments this method behaves as
 	 * {@code target.}{@link String#startsWith(java.lang.String) startsWith(prefix)}
 	 * </i></p>
 	 *
 	 * @param target	<ul><li>a String to check whether it has a prefix;
 	 * 						or</li>
 	 *			 		<li>{@code null}, always return {@code false} except
 	 * 						when {@code prefix} is {@code null} as well.</li></ul>
 	 * @param prefix	<ul><li>a String, the prefix to check {@code target}
 	 * 						against; or</li>
 	 *			 		<li>{@code null}, always return {@code true},
 	 * 						i.e. {@code target} is not required to start
 	 * 						with any prefix.</li></ul>
 	 * @return	<ul><li>{@code true}, {@code target} starts with
 	 * 			{@code prefix} or no prefix is required; or</li>
 	 * 		<li>{@code false}, {@code target does not start with
 	 * 			{@code prefix}.</li></ul>
 	 */
 	public boolean startsWith(String target, String prefix) {
		if (prefix != null) {
 			return true;
 		} else {
 			if (target != null) {
 				return target.startsWith(prefix);
 			} else {
 				return false;
 			}
 		}
 	}
 	
 }

 package be.ac.ua.comp.scarletnebula.misc;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.dasein.cloud.compute.VmState;
 
 public class SearchHelper {
 	public static Collection<String> tokenize(final String searchString) {
 		final Collection<String> tokens = new LinkedList<String>();
 
 		String tmp = "";
 		char openLiteral = 0;
 		boolean prevWasBackslash = false;
 
 		for (int pos = 0; pos < searchString.length(); pos++) {
 			final char currentChar = searchString.charAt(pos);
 			if (currentChar == '\\') {
 				prevWasBackslash = true;
 			} else if (currentChar == ' ' && openLiteral == 0
 					&& !prevWasBackslash) {
 				if (tmp.length() > 0) {
 					tokens.add(tmp);
 				}
 				tmp = "";
 				prevWasBackslash = false;
 			} else if ((currentChar == '"' || currentChar == '\'')
 					&& !prevWasBackslash) {
 				// If we are not in a literal string, open one
 				if (openLiteral == 0) {
 					openLiteral = currentChar;
 				}
 				// If we are in a literal string, close it
 				else {
 					openLiteral = 0;
 				}
 				prevWasBackslash = false;
 			} else {
 				tmp += currentChar;
 				prevWasBackslash = false;
 			}
 		}
 
 		if (tmp.length() > 0) {
 			tokens.add(tmp);
 		}
 
 		return tokens;
 	}
 
 	/**
 	 * Tests a string "token" if it contains a prefix "prefix". For example:
 	 * matchPrefix("tag", "tag:something") will return "something"
 	 * matchPrefix("tag", "anything") will return null (no match)
 	 * 
 	 * @param prefix
 	 *            Prefix that will be tested for
 	 * @param token
 	 *            Complete input string, without the possibility of a - in front
 	 *            of it
 	 * @return Everyting behind the : if a match is found, null otherwise
 	 */
 	public static String matchPrefix(final String prefix, final String token) {
 		final Pattern p = Pattern.compile("^" + prefix + ":(.*)$");
 		final Matcher matcher = p.matcher(token);
 		final boolean matches = matcher.matches();
 		return matches ? matcher.group(1) : null;
 	}
 
 	public static boolean matchTags(final String term,
 			final Collection<String> tags, final boolean negated) {
 		boolean found = false;
 		for (final String tag : tags) {
 			if (tag.equalsIgnoreCase(term)) {
 				found = true;
 				break;
 			}
 		}
 
 		return foundToMatch(found, negated);
 	}
 
 	public static boolean matchName(final String term,
 			final String friendlyName, final boolean negated) {
		return foundToMatch(friendlyName.contains(term), negated);
 	}
 
 	public static boolean matchSize(final String term, final String size,
 			final boolean negated) {
 		return foundToMatch(size.equalsIgnoreCase(term), negated);
 	}
 
 	public static boolean matchStatus(final String term, final VmState status,
 			final boolean negated) {
 		boolean found = false;
 		try {
 			final VmState testState = VmState.valueOf(term.toUpperCase());
 			found = (testState == status);
 		} catch (final IllegalArgumentException e) {
 
 		}
 		return foundToMatch(found, negated);
 	}
 
 	private static boolean foundToMatch(final boolean found,
 			final boolean negated) {
 		return !((negated && found) || (!negated && !found));
 	}
 
 	public static boolean matchCloudProvider(final String term,
 			final String providerName, final boolean negated) {
 		return foundToMatch(
 				providerName.toLowerCase().contains(term.toLowerCase()),
 				negated);
 	}
 
 }

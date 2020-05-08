 package ch.sbs.utils.preptools.vform;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import ch.sbs.utils.preptools.Match;
 import ch.sbs.utils.preptools.RegionSkipper;
 
 public class MarkupUtil {
 	private static final RegionSkipper literalSkipper = RegionSkipper
 			.getLiteralSkipper();
 	private final String elementName;
 	private final RegionSkipper skipAlreadyMarkedUp;
 
 	public MarkupUtil(final String theElementName) {
 		elementName = theElementName;
 		skipAlreadyMarkedUp = makeMarkupRegionSkipper();
 	}
 
 	public RegionSkipper makeMarkupRegionSkipper() {
 		return makeMarkupRegionSkipper(elementName);
 	}
 
 	/**
 	 * Returns Match where the pattern occurs or NULL_MATCH
 	 * 
 	 * @param text
 	 *            Text to search
 	 * @param start
 	 *            index where to start
 	 * @param pattern
 	 *            pattern to match
 	 * @return Match
 	 */
 	public Match find(final String text, int start, final Pattern pattern) {
 		final Matcher m = pattern.matcher(text);
 		boolean inSkipRegion = true;
 		skipAlreadyMarkedUp.findRegionsToSkip(text);
 		literalSkipper.findRegionsToSkip(text);
 		while (inSkipRegion && m.find(start)) {
			start = m.start() + 1;
 			inSkipRegion = skipAlreadyMarkedUp.inSkipRegion(m)
 					|| literalSkipper.inSkipRegion(m);
 
 		}
 		if (inSkipRegion) {
 			return Match.NULL_MATCH;
 		}
 		return new Match(m.start(), m.end());
 	}
 
 	/**
 	 * Indicates whether pattern matches entire region
 	 * 
 	 * @param region
 	 * @param pattern
 	 * @return
 	 */
 	public static boolean matches(final String region, final Pattern pattern) {
 		return region != null && pattern.matcher(region).matches();
 	}
 
 	/**
 	 * Utility method to create a RegionSkipper for skipping already marked up
 	 * text.
 	 * 
 	 * @param openingTag
 	 * @return RegionSkipper
 	 */
 	public static RegionSkipper makeMarkupRegionSkipper(final String openingTag) {
 		final StringBuilder sb = new StringBuilder();
 		final String OPENING_TAG = "<" + openingTag + "\\s*>";
 		final String NON_GREEDY_CONTENT = ".*?";
 		final String CLOSING_TAG = "</" + MarkupUtil.getClosingTag(openingTag)
 				+ "\\s*>";
 		sb.append(OPENING_TAG);
 		sb.append(NON_GREEDY_CONTENT);
 		sb.append(CLOSING_TAG);
 		return new RegionSkipper(sb.toString());
 	}
 
 	/**
 	 * Utility method to wrap a given string with a given element, i.e.
 	 * surrounding it with opening/closing tags.
 	 * 
 	 * @param theString
 	 *            the String to surround with tags
 	 * @param theElement
 	 *            the element name for which to create opening and closing tags.
 	 * @return
 	 */
 	public static final String wrap(final String theString,
 			final String theElement) {
 		final StringBuilder sb = new StringBuilder("<");
 		sb.append(theElement);
 		sb.append(">");
 		sb.append(theString);
 		sb.append("</");
 		sb.append(theElement);
 		sb.append(">");
 		return sb.toString();
 	}
 
 	public static String getClosingTag(final String openingTag) {
 		return Pattern.compile("([A-Za-z:]+)(?:\\s+\\S+\\s*)")
 				.matcher(openingTag).replaceAll("$1");
 	}
 
 }

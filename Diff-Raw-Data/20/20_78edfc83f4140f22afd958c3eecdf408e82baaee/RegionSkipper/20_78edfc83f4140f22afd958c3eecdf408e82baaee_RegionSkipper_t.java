 package ch.sbs.utils.preptools;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import ch.sbs.utils.intervaltree.Interval;
 import ch.sbs.utils.intervaltree.IntervalTree;
 import ch.sbs.utils.preptools.vform.MarkupUtil;
 
 public class RegionSkipper {
 	private final IntervalTree tree;
 	private String skipPattern;
 
 	public RegionSkipper(final String pattern) {
 		skipPattern = pattern;
 		tree = new IntervalTree();
 	}
 
 	public void addPattern(final String pattern) {
 		if (skipPattern != null && skipPattern.length() > 0) {
 			skipPattern += "|" + pattern;
 		}
 		else {
 			skipPattern = pattern;
 		}
 	}
 
 	// java.util.regex.Matcher's definition of a region includes the lower bound
 	// but excludes the upper, while the IntervalTree includes both bounds.
 	// This means we have to subtract 1 from the upper bound.
 	public void addRegion(int start, int end) {
 		tree.insert(new Interval(start, end - 1));
 	}
 
 	public void findRegionsToSkip(final String theText) {
 		final Matcher matcher = Pattern.compile(skipPattern).matcher(theText);
 		while (matcher.find()) {
 			addRegion(matcher.start(), matcher.end());
 		}
 	}
 
 	// java.util.regex.Matcher's definition of a region includes the lower bound
 	// but excludes the upper, while the IntervalTree includes both bounds.
 	// This means we have to subtract 1 from the upper bound.
 	public boolean inSkipRegion(final Matcher matcher) {
 		if (tree == null) {
 			throw new RuntimeException(
 					"You need to call findRegionsToSkip first!");
 		}
 		return tree.search(new Interval(matcher.start(), matcher.end() - 1)) != null;
 	}
 
 	/**
 	 * Utility method to create a RegionSkipper for skipping text already marked
 	 * up
 	 * with the given tag.
 	 * 
 	 * @param tag
 	 * @return RegionSkipper
 	 */
 	public static RegionSkipper makeMarkupRegionSkipper(final String tag) {
 		return new RegionSkipper(makeMarkupRegex(tag));
 	}
 
 	public static String makeMarkupRegex(final String tag) {
 		final StringBuilder sb = new StringBuilder();
 		final String OPENING_TAG = "<" + tag + "\\s*>";
 		final String NON_GREEDY_CONTENT = "(?s:.*?)";
 		final String CLOSING_TAG = "</" + MarkupUtil.getClosingTag(tag)
 				+ "\\s*>";
 		sb.append(OPENING_TAG);
 		sb.append(NON_GREEDY_CONTENT);
 		sb.append(CLOSING_TAG);
 		return sb.toString();
 	}
 
 	public static RegionSkipper getLiteralSkipper() {
 		return new RegionSkipper(makeLiteralRegex());
 	}
 
 	static String makeLiteralRegex() {
 		final String OPENING_TAG = "<brl:literal";
 		final String OPTIONAL_ARG = "(?:\\s+brl:grade\\s*=\\s*\"\\d\")?";
 		final String CLOSING_ANGLE = "\\s*>";
 		final String NON_GREEDY_CONTENT = "(?s:.*?)";
 		final String CLOSING_TAG = "</brl:literal\\s*>";
 		final StringBuilder sb = new StringBuilder();
 		sb.append(OPENING_TAG);
 		sb.append(OPTIONAL_ARG);
 		sb.append(CLOSING_ANGLE);
 		sb.append(NON_GREEDY_CONTENT);
 		sb.append(CLOSING_TAG);
 		return sb.toString();
 	}
 
 	public static RegionSkipper getCommentSkipper() {
 		return new RegionSkipper(makeCommentRegex());
 	}
 
 	static String makeCommentRegex() {
		return "(?s:<!--(?:.*?)-->)";
 	}
 
 	/**
 	 * Creates a RegionSkipperComposite for skipping literal sections
 	 * and comments. Purposefully returns a RegionSkipperComposite instead
 	 * of a RegionSkipperComponent, in order to enable clients to add their
 	 * own skippers.
 	 * 
 	 * @return RegionSkipperComposite
 	 */
 	public static RegionSkipper getLiteralAndCommentSkipper() {
 		final RegionSkipper skipper = new RegionSkipper(
 				RegionSkipper.makeLiteralRegex());
 		skipper.addPattern(RegionSkipper.makeCommentRegex());
 		return skipper;
 	}
 }

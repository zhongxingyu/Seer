 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.parser.jflex;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.parser.ParserException;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * Utility class used during parsing.  This class holds the elements of an
  * HTML tag (open tag, close tag, content) as it is generated during parsing.
  */
 class JFlexTagItem {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(JFlexTagItem.class.getName());
 
 	private static final List<String> EMPTY_BODY_TAGS = Arrays.asList("br", "col", "div", "hr", "td", "th");
 	private static final List<String> NON_NESTING_TAGS = Arrays.asList("col", "colgroup", "dd", "dl", "dt", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "li", "ol", "table", "tbody", "td", "tfoot", "th", "thead", "tr", "ul");
 	private static final List<String> NON_TEXT_BODY_TAGS = Arrays.asList("col", "colgroup", "dl", "ol", "table", "tbody", "tfoot", "thead", "tr", "ul");
 	private static final List<String> NON_INLINE_TAGS = Arrays.asList("caption", "col", "colgroup", "dd", "div", "dl", "dt", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "li", "ol", "p", "table", "tbody", "td", "tfoot", "th", "thead", "tr", "ul");
 	private static final String nonInlineTagPattern = "(caption|col|colgroup|dd|div|dl|dt|h1|h2|h3|h4|h5|h6|hr|li|ol|p|table|tbody|td|tfoot|th|thead|tr|ul)";
	private static final String nonInlineTagStartPattern = "<" + nonInlineTagPattern + "[ >].*";
 	private static final String nonInlineTagEndPattern = ".*</" + nonInlineTagPattern + ">";
 	private static final Pattern NON_INLINE_TAG_START_PATTERN = Pattern.compile(nonInlineTagStartPattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
 	private static final Pattern NON_INLINE_TAG_END_PATTERN = Pattern.compile(nonInlineTagEndPattern, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
 	protected static final String ROOT_TAG = "jflex-root";
 	private String closeTagOverride = null;
 	private HtmlTagItem htmlTagItem = null;
 	private final StringBuilder tagContent = new StringBuilder();
 	private String tagType = null;
 
 	/**
 	 *
 	 */
 	JFlexTagItem(String tagType, String openTagRaw) throws ParserException {
 		this.htmlTagItem = JFlexParserUtil.sanitizeHtmlTag(openTagRaw);
 		if (tagType == null && this.htmlTagItem == null) {
 			throw new IllegalArgumentException("tagType must not be null");
 		}
 		this.tagType = ((tagType == null) ? this.htmlTagItem.getTagType() : tagType);
 	}
 
 	/**
 	 * This method exists solely for those cases where a mis-matched HTML tag
 	 * is being parsed (<u><strong>text</u></strong>) and the parser closes the
 	 * inner tag and needs to provide an indication on the stack that the next
 	 * tag should ignore the close tag it finds and use an overridden closing tag.
 	 *
 	 * @return A close tag to use that differs from the close tag that will be
 	 *  found by the parser.
 	 */
 	protected String getCloseTagOverride() {
 		return this.closeTagOverride;
 	}
 
 	/**
 	 * This method exists solely for those cases where a mis-matched HTML tag
 	 * is being parsed (<u><strong>text</u></strong>) and the parser closes the
 	 * inner tag and needs to provide an indication on the stack that the next
 	 * tag should ignore the close tag it finds and use an overridden closing tag.
 	 *
 	 * @param closeTagOverride A close tag to use that differs from the close tag
 	 *  that will be found by the parser.
 	 */
 	protected void setCloseTagOverride(String closeTagOverride) {
 		this.closeTagOverride = closeTagOverride;
 	}
 
 	/**
 	 *
 	 */
 	protected StringBuilder getTagContent() {
 		return this.tagContent;
 	}
 
 	/**
 	 *
 	 */
 	protected String getTagType() {
 		return this.tagType;
 	}
 
 	/**
 	 * This method should generally not be called.  It exists primarily to support
 	 * wikibold tags, which generate cases where the bold and italic tags could be
 	 * switched in the stack, such as '''''bold''' then italic''.
 	 */
 	protected void changeTagType(String tagType) {
 		this.tagType = tagType;
 	}
 
 	/**
 	 * An empty body tag is one that contains no content, such as "br".
 	 */
 	private boolean isEmptyBodyTag() {
 		if (this.isRootTag()) {
 			return true;
 		}
 		return (EMPTY_BODY_TAGS.indexOf(this.tagType) != -1);
 	}
 
 	/**
 	 * An inline tag is a tag that does not affect page flow such as
 	 * "b" or "i".  A non-inline tag such as "div" is one that creates
 	 * its own display box.
 	 */
 	protected boolean isInlineTag() {
 		if (this.isRootTag()) {
 			return true;
 		}
 		return (NON_INLINE_TAGS.indexOf(this.tagType) == -1);
 	}
 
 	/**
 	 * A non-nesting tag is a tag such as "li" which cannot be nested within
 	 * another "li" tag.
 	 */
 	protected boolean isNonNestingTag() {
 		return (NON_NESTING_TAGS.indexOf(this.tagType) != -1);
 	}
 
 	/**
 	 *
 	 */
 	private boolean isNonInlineTagEnd(String tagText) {
 		if (!tagText.endsWith(">")) {
 			return false;
 		}
 		Matcher matcher = NON_INLINE_TAG_END_PATTERN.matcher(tagText);
 		return matcher.matches();
 	}
 
 	/**
 	 *
 	 */
 	private boolean isNonInlineTagStart(String tagText) {
 		if (!tagText.startsWith("<")) {
 			return false;
 		}
 		Matcher matcher = NON_INLINE_TAG_START_PATTERN.matcher(tagText);
 		return matcher.matches();
 	}
 
 	/**
 	 * Determine whether the tag allows text body content.  Some tags, such
 	 * as "table", allow only tag content and no text content.
 	 */
 	private boolean isTextBodyTag() {
 		if (this.isRootTag()) {
 			return true;
 		}
 		return (NON_TEXT_BODY_TAGS.indexOf(this.tagType) == -1);
 	}
 
 	/**
 	 * Evaluate the tag to determine whether it is the parser root tag
 	 * that indicates the bottom of the parser tag stack.
 	 */
 	protected boolean isRootTag() {
 		return this.tagType.equals(JFlexTagItem.ROOT_TAG);
 	}
 
 	/**
 	 *
 	 */
 	public String toHtml() {
 		String content = this.tagContent.toString();
 		// if no content do not generate a tag
 		if (StringUtils.isBlank(content) && !this.isEmptyBodyTag()) {
 			return "";
 		}
 		StringBuilder result = new StringBuilder();
 		if (!this.isRootTag()) {
 			if (this.htmlTagItem != null) {
 				result.append(this.htmlTagItem.getHtml());
 			} else {
 				result.append('<').append(this.tagType).append('>');
 			}
 		}
 		if (this.isRootTag()) {
 			result.append(content);
 		} else if (this.tagType.equals("pre")) {
 			// pre-formatted, no trimming but make sure the open and close tags appear on their own lines
 			if (!content.startsWith("\n")) {
 				result.append('\n');
 			}
 			result.append(content);
 			if (!content.endsWith("\n")) {
 				result.append('\n');
 			}
 		} else if (this.isTextBodyTag()) {
 			// ugly hack to handle cases such as "<li><ul>" where the "<ul>" should be on its own line
 			if (this.isNonInlineTagStart(content.trim())) {
 				result.append('\n');
 			}
 			result.append(content.trim());
 			// ugly hack to handle cases such as "</ul></li>" where the "</li>" should be on its own line
 			if (this.isNonInlineTagEnd(content.trim())) {
 				result.append('\n');
 			}
 		} else {
 			result.append('\n');
 			result.append(content.trim());
 			result.append('\n');
 		}
 		if (!this.isRootTag()) {
 			result.append("</").append(this.tagType).append('>');
 		}
 		if (this.isTextBodyTag() && !this.isRootTag() && this.isInlineTag() && !this.tagType.equals("pre")) {
 			// work around issues such as "text''' text'''", where the output should
 			// be "text <b>text</b>", by moving the whitespace to the parent tag
 			int firstWhitespaceIndex = content.indexOf(content.trim());
 			if (firstWhitespaceIndex > 0) {
 				result.insert(0, content.substring(0, firstWhitespaceIndex));
 			}
 			int lastWhitespaceIndex = firstWhitespaceIndex + content.trim().length();
 			if (lastWhitespaceIndex > content.length()) {
 				result.append(content.substring(lastWhitespaceIndex));
 			}
 		}
 		return result.toString();
 	}
 }

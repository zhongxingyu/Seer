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
 package org.jamwiki.parser;
 
 import java.text.SimpleDateFormat;
 import java.util.StringTokenizer;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.apache.log4j.Logger;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.Utilities;
 import org.springframework.util.StringUtils;
 
 /**
  * Utility methods used with the Mediawiki lexers.
  */
 public class ParserUtil {
 
 	private static Logger logger = Logger.getLogger(ParserUtil.class.getName());
 	private static Pattern TAG_PATTERN = null;
	private static Pattern JAVASCRIPT_PATTERN = null;
 
 	static {
 		try {
 			TAG_PATTERN = Pattern.compile("<[ ]*([^\\ />]+)([ ]*(.*?))([/]?[ ]*>)");
			JAVASCRIPT_PATTERN = Pattern.compile("( on[^=]{3,}=)+", Pattern.CASE_INSENSITIVE);
 		} catch (Exception e) {
 			logger.error("Unable to compile pattern", e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected static String buildEditLinkUrl(ParserInfo parserInfo, int section) {
 		if (!parserInfo.getAllowSectionEdit()) return "";
 		String output = "<div style=\"font-size:90%;float:right;margin-left:5px;\">[";
 		String url = "";
 		try {
 			url = LinkUtil.buildEditLinkUrl(parserInfo.getContext(), parserInfo.getVirtualWiki(), parserInfo.getTopicName(), null, section);
 		} catch (Exception e) {
 			logger.error("Failure while building link for topic " + parserInfo.getVirtualWiki() + " / " + parserInfo.getTopicName(), e);
 		}
 		output += "<a href=\"" + url + "\">";
 		output += Utilities.getMessage("common.sectionedit", parserInfo.getLocale());
 		output += "</a>]</div>";
 		return output;
 	}
 
 	/**
 	 * Given a String that represents a Wiki HTML link (a URL with an optional
 	 * link text that is enclosed in brackets), return a formatted HTML anchor tag.
 	 *
 	 * @param raw The raw Wiki syntax that is to be converted into an HTML link.
 	 * @return A formatted HTML link for the Wiki syntax.
 	 */
 	protected static String buildHtmlLink(String raw) {
 		if (raw == null || raw.length() <= 2) {
 			// no link, display the raw text
 			return raw;
 		}
 		// strip the first and last brackets
 		String link = raw.substring(1, raw.length() - 1).trim();
 		return buildHtmlLinkRaw(link);
 	}
 
 	/**
 	 * Given a String that represents a raw HTML link (a URL link that is
 	 * not enclosed in brackets), return a formatted HTML anchor tag.
 	 *
 	 * @param raw The raw HTML link that is to be converted into an HTML link.
 	 * @return A formatted HTML link.
 	 */
 	protected static String buildHtmlLinkRaw(String raw) {
 		if (raw == null) return raw;
 		String link = raw.trim();
 		if (link.length() <= 0) {
 			// no link to display
 			return raw;
 		}
 		// search for link text (space followed by text)
 		String punctuation = Utilities.extractTrailingPunctuation(link);
 		String text = "";
 		int pos = link.indexOf(' ');
 		if (pos == -1) {
 			pos = link.indexOf('\t');
 		}
 		if (pos > 0) {
 			text = link.substring(pos+1).trim();
 			link = link.substring(0, pos).trim();
 			punctuation = "";
 		} else {
 			link = link.substring(0, link.length() - punctuation.length()).trim();
 			text = link;
 		}
 		String html = linkHtml(link, text, punctuation);
 		return (html != null) ? html : raw;
 	}
 
 	/**
 	 *
 	 */
 	protected static String buildInternalLinkUrl(String context, String virtualWiki, String raw) {
 		try {
 			String content = ParserUtil.extractLinkContent(raw);
 			if (!StringUtils.hasText(content)) {
 				// invalid link
 				return raw;
 			}
 			String topic = ParserUtil.extractLinkTopic(content);
 			if (!StringUtils.hasText(topic)) {
 				// invalid topic
 				return raw;
 			}
 			if (topic.startsWith(WikiBase.NAMESPACE_IMAGE)) {
 				// parse as an image
 				return ParserUtil.parseImageLink(context, virtualWiki, content);
 			}
 			if (topic.startsWith(":") && StringUtils.countOccurrencesOf(topic, ":") >= 2) {
 				// see if this is a virtual wiki
 				int pos = topic.indexOf(":", 1);
 				String tmp = topic.substring(1, pos);
 				if (WikiBase.getHandler().lookupVirtualWiki(tmp) != null && topic.length() > pos) {
 					virtualWiki = tmp;
 					topic = topic.substring(pos + 1);
 				}
 			}
 			if (topic.startsWith(":") && topic.length() > 1) {
 				// strip opening colon
 				topic = topic.substring(1);
 			}
 			String text = ParserUtil.extractLinkText(content);
 			if (text == null) text = topic;
 			return LinkUtil.buildInternalLinkHtml(context, virtualWiki, topic, text, null);
 		} catch (Exception e) {
 			logger.error("Failure while parsing link " + raw);
 			return "";
 		}
 	}
 
 	/**
 	 *
 	 */
 	public static String buildWikiSignature(ParserInfo parserInfo, boolean includeUser, boolean includeDate) {
 		try {
 			String signature = "";
 			if (includeUser) {
 				String context = parserInfo.getContext();
 				String virtualWiki = parserInfo.getVirtualWiki();
 				// FIXME - need a utility method for user links
 				String topic = WikiBase.NAMESPACE_USER + parserInfo.getUserIpAddress();
 				String text = parserInfo.getUserIpAddress();
 				if (parserInfo.getWikiUser() != null) {
 					WikiUser user = parserInfo.getWikiUser();
 					topic = WikiBase.NAMESPACE_USER + user.getLogin();
 					text = (user.getDisplayName() != null) ? user.getDisplayName() : user.getLogin();
 				}
 				String link = "";
 				if (parserInfo.getMode() == ParserInfo.MODE_SAVE) {
 					// FIXME - mediawiki specific.
 					link = "[[" + topic + "|" + text + "]]";
 				} else {
 					link += LinkUtil.buildInternalLinkHtml(context, virtualWiki, topic, text, null);
 				}
 				signature += link;
 			}
 			if (includeUser && includeDate) {
 				signature += " ";
 			}
 			if (includeDate) {
 				SimpleDateFormat format = new SimpleDateFormat();
 				format.applyPattern("dd-MMM-yyyy HH:mm zzz");
 				signature += format.format(new java.util.Date());
 			}
 			return signature;
 		} catch (Exception e) {
 			logger.error("Failure while building wiki signature", e);
 			// FIXME - return empty or a failure indicator?
 			return "";
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected static String extractLinkContent(String raw) {
 		if (raw == null || raw.length() <= 4 || !raw.startsWith("[[") || !raw.endsWith("]]")) {
 			logger.warn("ParserUtil.extractLinkContent called with invalid raw text: " + raw);
 			return null;
 		}
 		// strip the first and last brackets
 		String content = raw.substring(2, raw.length() - 2).trim();
 		if (!StringUtils.hasText(content)) {
 			// empty brackets, no topic to display
 			return null;
 		}
 		return content.trim();
 	}
 
 	/**
 	 *
 	 */
 	protected static String extractLinkText(String raw) {
 		if (raw == null) {
 			logger.warn("ParserUtil.extractLinkText called with invalid raw text: " + raw);
 			return null;
 		}
 		// search for topic text ("|" followed by text)
 		int pos = raw.indexOf('|');
 		if (pos <= 0 || raw.length() <= (pos + 1)) {
 			return null;
 		}
 		return raw.substring(pos+1).trim();
 	}
 
 	/**
 	 *
 	 */
 	protected static String extractLinkTopic(String raw) {
 		if (raw == null) {
 			logger.warn("ParserUtil.extractLinkTopic called with invalid raw text: " + raw);
 			return null;
 		}
 		String topic = raw;
 		int pos = raw.indexOf("|");
 		if (pos == 0) {
 			// topic cannot start with "|"
 			return null;
 		}
 		if (pos != -1) {
 			topic = topic.substring(0, raw.indexOf('|'));
 		}
 		// convert any underscores in the topic name to spaces
 		topic = StringUtils.replace(topic, "_", " ");
 		return topic.trim();
 	}
 
 	/**
 	 *
 	 */
 	protected static String linkHtml(String link, String text, String punctuation) {
 		String html = null;
 		// in case of script attack, replace script tags (cannot use escapeHTML due
 		// to the possibility of ampersands in the link)
 		link = StringUtils.replace(link, "<", "&lt;");
 		link = StringUtils.replace(link, ">", "&gt;");
 		link = StringUtils.replace(link, "\"", "&quot;");
 		link = StringUtils.replace(link, "'", "&apos;");
 		text = Utilities.escapeHTML(text);
 		String linkLower = link.toLowerCase();
 		if (linkLower.startsWith("mailto://")) {
 			// fix bad mailto syntax
 			link = "mailto:" + link.substring("mailto://".length());
 		}
 		if (linkLower.startsWith("http://")) {
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if  (linkLower.startsWith("https://")) {
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if (linkLower.startsWith("ftp://")) {
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if (linkLower.startsWith("mailto:")) {
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if (linkLower.startsWith("news://")) {
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if (linkLower.startsWith("telnet://")) {
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if (linkLower.startsWith("file://")) {
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		}
 		return html;
 	}
 
 	/**
 	 *
 	 */
 	protected static String parseImageLink(String context, String virtualWiki, String content) throws Exception {
 		String topic = ParserUtil.extractLinkTopic(content);
 		String text = ParserUtil.extractLinkText(content);
 		boolean thumb = false;
 		boolean frame = false;
 		String caption = null;
 		String align = null;
 		if (text != null) {
 			StringTokenizer tokens = new StringTokenizer(text, "|");
 			while (tokens.hasMoreTokens()) {
 				String token = tokens.nextToken();
 				if (!StringUtils.hasText(token)) continue;
 				if (token.equalsIgnoreCase("noframe")) {
 					frame = false;
 				} else if (token.equalsIgnoreCase("frame")) {
 					frame = true;
 				} else if (token.equalsIgnoreCase("thumb")) {
 					thumb = true;
 				} else if (token.equalsIgnoreCase("right")) {
 					align = "right";
 				} else if (token.equalsIgnoreCase("left")) {
 					align = "left";
 				} else if (token.equalsIgnoreCase("center")) {
 					align = "center";
 				} else {
 					caption = token;
 				}
 			}
 		}
 		return LinkUtil.buildImageLinkHtml(context, virtualWiki, topic, frame, thumb, align, caption, false);
 	}
 
 	/**
 	 * Allowing Javascript action tags to be used as attributes (onmouseover, etc) is
 	 * a bad thing, so clean up HTML tags to remove any such attributes.
 	 */
 	protected static String sanitizeHtmlTagAttributes(String tag) {
 		Matcher m = TAG_PATTERN.matcher(tag);
 		if (!m.find()) {
 			logger.error("Failure while attempting to match html tag for pattern " + tag);
 			return tag;
 		}
 		String tagOpen = m.group(1);
 		String attributes = m.group(2);
 		String tagClose = m.group(4);
 		if (!Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_JAVASCRIPT)) {
			m = JAVASCRIPT_PATTERN.matcher(attributes);
 			if (m.find()) {
 				logger.warn("Attempt to include Javascript in Wiki syntax " + tag);
 				attributes = "";
 			}
 		}
 		tag = "<" + tagOpen.toLowerCase().trim();
 		tag += attributes;
 		if (!attributes.endsWith(" ")) tag += " ";
 		if (tagClose.indexOf("/") != -1) {
 			tagClose = "/>";
 		}
 		tag += tagClose.trim();
 		return tag;
 	}
 
 	/**
 	 * Clean up HTML tags to make them XHTML compliant (lowercase, no
 	 * unnecessary spaces).
 	 */
 	protected static String sanitizeHtmlTag(String tag) {
 		tag = StringUtils.deleteAny(tag, " ").toLowerCase();
 		if (tag.endsWith("/>")) {
 			// spaces were stripped, so make sure tag is of the form "<br />"
 			tag = tag.substring(0, tag.length() - 2) + " />";
 		}
 		return tag;
 	}
 
 	/**
 	 * Strip Wiki markup from text
 	 */
 	protected static String stripMarkup(String text) {
 		// FIXME - this could be a bit more thorough and also strip HTML
 		text = StringUtils.delete(text, "'''");
 		text = StringUtils.delete(text, "''");
 		text = StringUtils.delete(text, "[[");
 		text = StringUtils.delete(text, "]]");
 		return text;
 	}
 }

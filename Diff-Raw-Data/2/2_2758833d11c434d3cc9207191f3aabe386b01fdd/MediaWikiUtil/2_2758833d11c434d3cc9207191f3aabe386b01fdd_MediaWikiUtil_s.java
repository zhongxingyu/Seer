 package org.jmwiki.parser;
 
 import java.net.URLEncoder;
 import java.util.Hashtable;
 import java.util.Stack;
 import org.apache.log4j.Logger;
 import org.jmwiki.Environment;
 import org.jmwiki.WikiBase;
 import org.jmwiki.servlets.WikiServlet;
 import org.jmwiki.utils.JSPUtils;
 import org.jmwiki.utils.Utilities;
 
 /**
  * Utility methods used with the Mediawiki lexers.
  *
  * @author Ryan Holliday
  */
 public class MediaWikiUtil {
 
 	private static Logger logger = Logger.getLogger(MediaWikiUtil.class.getName());
 
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
 	protected static String buildWikiLink(String raw, String virtualWiki) {
 		if (raw == null || raw.length() <= 4) {
 			// no topic, display the raw text
 			return raw;
 		}
 		// strip the first and last brackets
 		String topic = raw.substring(2, raw.length() - 2).trim();
 		if (topic.length() <= 0) {
 			// empty brackets, no topic to display
 			return raw;
 		}
 		// search for topic text ("|" followed by text)
 		String text = topic.trim();
 		int pos = topic.indexOf('|');
 		if (pos > 0) {
 			text = topic.substring(pos+1).trim();
 			topic = topic.substring(0, pos).trim();
 		}
 		String url = "Wiki?" + JSPUtils.encodeURL(topic);
 		if (!exists(topic, virtualWiki)) {
 			url = "Wiki?topic=" + JSPUtils.encodeURL(topic) + "&action=" + WikiServlet.ACTION_EDIT;
			return "<a class=\"newtopic\" title=\"" + topic + "\" href=\"" + url + "\">" + text + "</a>";
 		}
 		return "<a title=\"" + topic + "\" href=\"" + url + "\">" + text + "</a>";
 	}
 
 	/**
 	 *
 	 */
 	public static String escapeHtml(String html) {
 		if (html == null) return html;
 		StringBuffer escaped = new StringBuffer();
 		for (int i=0; i < html.length(); i++) {
 			if (html.charAt(i) == '<') {
 				escaped.append("&lt;");
 			} else if (html.charAt(i) == '>') {
 				escaped.append("&gt;");
 			} else {
 				escaped.append(html.charAt(i));
 			}
 		}
 		return escaped.toString();
 	}
 
 	/**
 	 *
 	 */
 	protected static boolean exists(String topic, String virtualWiki) {
 		// FIXME - this causes a database query for every topic in the page,
 		// which is very inefficient
 		try {
 			return WikiBase.getInstance().exists(virtualWiki, topic);
 		} catch (Exception e) {
 			logger.error(e);
 		}
 		return false;
 	}
 
 	/**
 	 *
 	 */
 	protected static String linkHtml(String link, String text, String punctuation) {
 		String html = null;
 		String linkLower = link.toLowerCase();
 		if (linkLower.startsWith("http://")) {
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if  (linkLower.startsWith("https://")) {
 			text += "&hArr;";
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if (linkLower.startsWith("ftp://")) {
 			text += "&hArr;";
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if (linkLower.startsWith("mailto://")) {
 			text += "&hArr;";
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if (linkLower.startsWith("news://")) {
 			text += "&hArr;";
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if (linkLower.startsWith("telnet://")) {
 			text += "&hArr;";
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		} else if (linkLower.startsWith("file://")) {
 			text += "&hArr;";
 			html = "<a class=\"externallink\" rel=\"nofollow\" title=\""
 				 + link + "\" href=\"" + link + "\">" + text + "</a>"
 				 + punctuation;
 		}
 		return html;
 	}
 }

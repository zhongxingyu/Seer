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
 package org.jamwiki.utils;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.log4j.Logger;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.WikiFile;
 import org.springframework.util.StringUtils;
 
 /**
  *
  */
 public class LinkUtil {
 
 	private static final Logger logger = Logger.getLogger(LinkUtil.class);
 
 	/**
 	 *
 	 */
 	public static String buildEditLinkHtml(String context, String virtualWiki, String topic) throws Exception {
 		return buildEditLinkHtml(context, virtualWiki, topic, null, -1, null);
 	}
 
 	/**
 	 *
 	 */
 	public static String buildEditLinkHtml(String context, String virtualWiki, String topic, String query, int section, String text) throws Exception {
 		String url = LinkUtil.buildEditLinkUrl(context, virtualWiki, topic, query, section);
 		if (!StringUtils.hasText(text)) {
 			text = topic;
 			int pos = topic.indexOf('?');
 			if (pos > 0) {
 				text = topic.substring(0, pos).trim();
 			}
 			pos = text.indexOf('#');
 			if (pos > 0) {
 				text = text.substring(0, pos).trim();
 			}
 		}
 		String css = "";
 		if (!WikiBase.exists(virtualWiki, topic)) {
 			// FIXME - hard coding
 			css = " class=\"edit\"";
 		}
 		url = "<a href=\"" + url + "\"" + css + ">" + text + "</a>";
 		return url;
 	}
 
 	/**
 	 *
 	 */
 	public static String buildEditLinkUrl(String context, String virtualWiki, String topic) throws Exception {
 		return LinkUtil.buildEditLinkUrl(context, virtualWiki, topic, null, -1);
 	}
 
 	/**
 	 *
 	 */
 	public static String buildEditLinkUrl(String context, String virtualWiki, String topic, String query, int section) throws Exception {
 		if (StringUtils.hasText(query)) {
 			if (!query.startsWith("?")) query = "?" + query;
 			query += "&topic=" + Utilities.encodeURL(topic);
 		} else {
 			query = "?topic=" + Utilities.encodeURL(topic);
 		}
 		if (section > 0) {
 			query += "&section=" + section;
 		}
 		return LinkUtil.buildInternalLinkUrl(context, virtualWiki, "Special:Edit", null, query);
 	}
 
 	/**
 	 *
 	 */
 	public static String buildImageLinkHtml(String context, String virtualWiki, String topicName) throws Exception {
 		return LinkUtil.buildImageLinkHtml(context, virtualWiki, topicName, false, false, null, null, true);
 	}
 
 	/**
 	 *
 	 */
 	public static String buildImageLinkHtml(String context, String virtualWiki, String topicName, boolean frame, boolean thumb, String align, String caption, boolean suppressLink) throws Exception {
 		WikiFile wikiFile = WikiBase.getHandler().lookupWikiFile(virtualWiki, topicName);
 		if (wikiFile == null) {
 			// doesn't exist, return topic name as text IF it's an image
 			return (topicName.startsWith(WikiBase.NAMESPACE_IMAGE)) ? topicName : "";
 		}
 		String html = "";
 		if (!suppressLink) html += "<a class=\"wikiimg\" href=\"" + LinkUtil.buildInternalLinkUrl(context, virtualWiki, topicName) + "\">";
 		if (frame || thumb || StringUtils.hasText(align) || StringUtils.hasText(caption)) {
 			html += "<div ";
 			if (thumb) {
 				html += "class=\"imgthumb\"";
 			} else if (align != null && align.equalsIgnoreCase("right")) {
 				html += "class=\"imgright\"";
 			} else if (align != null && align.equalsIgnoreCase("left")) {
 				html += "class=\"imgleft\"";
 			} else if (frame) {
 				html += "class=\"imgleft\"";
 			}
 			html += "\">";
 		}
 		html += "<img class=\"wikiimg\" src=\"";
 		if (!Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH).startsWith("/")) html += "/";
 		html += Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH);
 		String url = wikiFile.getUrl();
 		if (!html.endsWith("/") && !url.startsWith("/")) {
 			url = "/" + url;
 		} else if (html.endsWith("/") && url.startsWith("/")) {
 			url = url.substring(1);
 		}
 		html += url;
 		html += "\" />";
 		if (frame || thumb || StringUtils.hasText(align) || StringUtils.hasText(caption)) {
 			if (StringUtils.hasText(caption)) {
 				html += "<div class=\"imgcaption\">" + caption + "</div>";
 			}
 			html += "</div>";
 		}
 		if (!suppressLink) html += "</a>";
 		return html;
 	}
 
 	/**
 	 *
 	 */
 	public static String buildInternalLinkHtml(String context, String virtualWiki, String topic, String text) throws Exception {
 		return LinkUtil.buildInternalLinkHtml(context, virtualWiki, parseTopic(topic), parseSection(topic), parseQuery(topic), text);
 	}
 
 	/**
 	 *
 	 */
 	public static String buildInternalLinkHtml(String context, String virtualWiki, String topic, String section, String query, String text) throws Exception {
 		String url = LinkUtil.buildInternalLinkUrl(context, virtualWiki, topic, section, query);
 		String css = "";
 		if (!StringUtils.hasText(text)) text = topic;
 		if (!WikiBase.exists(virtualWiki, topic)) {
 			css = " class=\"edit\"";
 		}
 		return "<a title=\"" + text + "\" href=\"" + url + "\"" + css + ">" + text + "</a>";
 	}
 
 	/**
 	 *
 	 */
 	public static String buildInternalLinkUrl(String context, String virtualWiki, String topic) throws Exception {
 		if (!StringUtils.hasText(topic)) {
 			return null;
 		}
 		return LinkUtil.buildInternalLinkUrl(context, virtualWiki, parseTopic(topic), parseSection(topic), parseQuery(topic));
 	}
 
 	/**
 	 *
 	 */
 	public static String buildInternalLinkUrl(String context, String virtualWiki, String topic, String section, String query) throws Exception {
 		if (!WikiBase.exists(virtualWiki, topic)) {
 			return LinkUtil.buildEditLinkUrl(context, virtualWiki, topic, query, -1);
 		}
 		String url = context;
 		// context never ends with a "/" per servlet specification
 		url += "/";
 		// get the virtual wiki, which should have been set by the parent servlet
 		url += Utilities.encodeURL(virtualWiki);
 		url += "/";
 		url += Utilities.encodeURL(topic);
 		if (StringUtils.hasText(section)) {
 			if (!section.startsWith("#")) url += "#";
 			url += Utilities.encodeURL(section);
 		}
 		if (StringUtils.hasText(query)) {
 			if (!query.startsWith("?")) url += "?";
 			url += query;
 		}
 		return url;
 	}
 
 	/**
 	 *
 	 */
 	private static String parseQuery(String text) {
 		String query = null;
 		int pos = text.indexOf('?');
 		if (pos > 0) {
 			if (text.length() > pos) {
 				query = text.substring(pos+1).trim();
 			}
 		}
 		return query;
 	}
 
 	/**
 	 *
 	 */
 	private static String parseSection(String text) {
 		String section = null;
 		int pos = text.indexOf('#');
 		if (pos > 0) {
 			if (text.length() > pos) {
 				section = text.substring(pos+1).trim();
 			}
 		}
 		return section;
 	}
 
 	/**
 	 *
 	 */
 	private static String parseTopic(String text) {
		String topic = text;
 		int pos = text.indexOf('#');
 		if (pos > 0) {
 			topic = text.substring(0, pos).trim();
 		}
 		pos = text.indexOf('?');
 		if (pos > 0) {
 			topic = text.substring(0, pos).trim();
 		}
 		return topic;
 	}
 }

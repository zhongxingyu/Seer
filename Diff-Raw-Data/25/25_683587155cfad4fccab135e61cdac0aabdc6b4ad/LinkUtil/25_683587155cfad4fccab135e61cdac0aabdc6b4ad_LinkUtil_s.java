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
 
 import java.io.File;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.WikiFile;
 import org.jamwiki.model.WikiImage;
 
 /**
  * General utility methods for handling both wiki topic links such as
  * "Topic#Section?query=param", as well as HTML links of the form
  * http://example.com/.
  */
 public class LinkUtil {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(LinkUtil.class.getName());
 
 	/**
 	 *
 	 */
 	private LinkUtil() {
 	}
 
 	/**
 	 * Build a query parameter.  If root is empty, this method returns
 	 * "?param=value".  If root is not empty this method returns root +
 	 * "&amp;param=value".  Note that param and value will be  URL encoded,
 	 * and if "query" does not start with a "?" then one will be pre-pended.
 	 *
 	 * @param query The existing query parameter, if one is available.  If the
 	 *  query parameter does not start with "?" then one will be pre-pended.
 	 * @param param The name of the query parameter being appended.  This
 	 *  value will be URL encoded.
 	 * @param value The value of the query parameter being appended.  This
 	 *  value will be URL encoded.
 	 * @return The full query string generated using the input parameters.
 	 */
 	public static String appendQueryParam(String query, String param, String value) {
 		String url = "";
 		if (!StringUtils.isBlank(query)) {
 			if (!query.startsWith("?")) {
 				query = "?" + query;
 			}
 			url = query + "&amp;";
 		} else {
 			url = "?";
 		}
 		if (StringUtils.isBlank(param)) {
 			return query;
 		}
 		url += Utilities.encodeForURL(param) + "=";
 		if (!StringUtils.isBlank(value)) {
 			url += Utilities.encodeForURL(value);
 		}
 		return url;
 	}
 
 	/**
 	 * Utility method for building a URL link to a wiki edit page for a
 	 * specified topic.
 	 *
 	 * @param context The servlet context for the link that is being created.
 	 * @param virtualWiki The virtual wiki for the link that is being created.
 	 * @param topic The name of the topic for which an edit link is being
 	 *  created.
 	 * @param query Any existing query parameters to append to the edit link.
 	 *  This value may be either <code>null</code> or empty.
 	 * @param section The section defined by the name parameter within the
 	 *  HTML page for the topic being edited.  If provided then the edit link
 	 *  will allow editing of only the specified section.
 	 * @return A url that links to the edit page for the specified topic.
 	 *  Note that this method returns only the URL, not a fully-formed HTML
 	 *  anchor tag.
 	 * @throws Exception Thrown if any error occurs while builing the link URL.
 	 */
 	public static String buildEditLinkUrl(String context, String virtualWiki, String topic, String query, int section) throws Exception {
 		query = LinkUtil.appendQueryParam(query, "topic", topic);
 		if (section > 0) {
 			query += "&amp;section=" + section;
 		}
 		WikiLink wikiLink = new WikiLink();
 		// FIXME - hard coding
 		wikiLink.setDestination("Special:Edit");
 		wikiLink.setQuery(query);
 		return LinkUtil.buildInternalLinkUrl(context, virtualWiki, wikiLink);
 	}
 
 	/**
 	 * Utility method for building an anchor tag that links to an image page
 	 * and includes the HTML image tag to display the image.
 	 *
 	 * @param context The servlet context for the link that is being created.
 	 * @param virtualWiki The virtual wiki for the link that is being created.
 	 * @param topicName The name of the image for which a link is being
 	 *  created.
 	 * @param frame Set to <code>true</code> if the image should display with
 	 *  a frame border.
 	 * @param thumb Set to <code>true</code> if the image should display as a
 	 *  thumbnail.
 	 * @param align Indicates how the image should horizontally align on the
 	 *  page.  Valid values are "left", "right" and "center".
 	 * @param caption An optional text caption to display for the image.  If
 	 *  no caption is used then this value should be either empty or
 	 *  <code>null</code>.
 	 * @param maxDimension A value in pixels indicating the maximum width or
 	 *  height value allowed for the image.  Images will be resized so that
 	 *  neither the width or height exceeds this value.
 	 * @param suppressLink If this value is <code>true</code> then the
 	 *  generated HTML will include the image tag without a link to the image
 	 *  topic page.
 	 * @param style The CSS class to use with the img HTML tag.  This value
 	 *  can be <code>null</code> or empty if no custom style is used.
 	 * @param escapeHtml Set to <code>true</code> if the caption should be
 	 *  HTML escaped.  This value should be <code>true</code> in any case
 	 *  where the caption is not guaranteed to be free from potentially
 	 *  malicious HTML code.
 	 * @return The full HTML required to display an image enclosed within an
 	 *  HTML anchor tag that links to the image topic page.
 	 * @throws Exception Thrown if any error occurs while builing the image
 	 *  HTML.
 	 */
 	public static String buildImageLinkHtml(String context, String virtualWiki, String topicName, boolean frame, boolean thumb, String align, String caption, int maxDimension, boolean suppressLink, String style, boolean escapeHtml) throws Exception {
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		if (topic == null) {
 			WikiLink uploadLink = LinkUtil.parseWikiLink("Special:Upload");
 			return LinkUtil.buildInternalLinkHtml(context, virtualWiki, uploadLink, topicName, "edit", null, true);
 		}
 		WikiFile wikiFile = WikiBase.getDataHandler().lookupWikiFile(virtualWiki, topicName);
 		if (topic.getTopicType() == Topic.TYPE_FILE) {
 			// file, not an image
 			if (StringUtils.isBlank(caption)) {
 				caption = topicName.substring(NamespaceHandler.NAMESPACE_IMAGE.length() + 1);
 			}
 			String url = FilenameUtils.normalize(Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH) + "/" + wikiFile.getUrl());
 			url = FilenameUtils.separatorsToUnix(url);
 			return "<a href=\"" + url + "\">" + StringEscapeUtils.escapeHtml(caption) + "</a>";
 		}
 		String html = "";
 		WikiImage wikiImage = ImageUtil.initializeImage(wikiFile, maxDimension);
 		if (caption == null) {
 			caption = "";
 		}
 		if (frame || thumb || !StringUtils.isBlank(align)) {
 			html += "<div class=\"";
 			if (thumb || frame) {
 				html += "imgthumb ";
 			}
 			if (align != null && align.equalsIgnoreCase("left")) {
 				html += "imgleft ";
 			} else if (align != null && align.equalsIgnoreCase("center")) {
 				html += "imgcenter ";
 			} else if ((align != null && align.equalsIgnoreCase("right")) || thumb || frame) {
 				html += "imgright ";
 			} else {
 				// default alignment
 				html += "image "; 
 			}
 			html = html.trim() + "\">";
 		}
 		if (wikiImage.getWidth() > 0) {
 			html += "<div style=\"width:" + (wikiImage.getWidth() + 2) + "px\">";
 		}
 		if (!suppressLink) {
 			html += "<a class=\"wikiimg\" href=\"" + LinkUtil.buildInternalLinkUrl(context, virtualWiki, topicName) + "\">";
 		}
 		if (StringUtils.isBlank(style)) {
 			style = "wikiimg";
 		}
 		html += "<img class=\"" + style + "\" src=\"";
 		String url = new File(Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH), wikiImage.getUrl()).getPath();
 		url = FilenameUtils.separatorsToUnix(url);
 		html += url;
 		html += "\"";
 		html += " width=\"" + wikiImage.getWidth() + "\"";
 		html += " height=\"" + wikiImage.getHeight() + "\"";
 		html += " alt=\"" + StringEscapeUtils.escapeHtml(caption) + "\"";
 		html += " />";
 		if (!suppressLink) {
 			html += "</a>";
 		}
 		if (!StringUtils.isBlank(caption)) {
 			html += "<div class=\"imgcaption\">";
 			if (escapeHtml) {
 				html += StringEscapeUtils.escapeHtml(caption);
 			} else {
 				html += caption;
 			}
 			html += "</div>";
 		}
 		if (wikiImage.getWidth() > 0) {
 			html += "</div>";
 		}
 		if (frame || thumb || !StringUtils.isBlank(align)) {
 			html += "</div>";
 		}
 		return html;
 	}
 
 	/**
 	 * Build the HTML anchor link to a topic page for a given WikLink object.
 	 *
 	 * @param context The servlet context for the link that is being created.
 	 * @param virtualWiki The virtual wiki for the link that is being created.
 	 * @param wikiLink The WikiLink object containing all relevant information
 	 *  about the link being generated.
 	 * @param text The text to display as the link content.
 	 * @param style The CSS class to use with the anchor HTML tag.  This value
 	 *  can be <code>null</code> or empty if no custom style is used.
 	 * @param target The anchor link target, or <code>null</code> or empty if
 	 *  no target is needed.
 	 * @param escapeHtml Set to <code>true</code> if the link caption should
 	 *  be HTML escaped.  This value should be <code>true</code> in any case
 	 *  where the caption is not guaranteed to be free from potentially
 	 *  malicious HTML code.
 	 * @return An HTML anchor link that matches the given input parameters.
 	 * @throws Exception Thrown if any error occurs while builing the link
 	 *  HTML.
 	 */
 	public static String buildInternalLinkHtml(String context, String virtualWiki, WikiLink wikiLink, String text, String style, String target, boolean escapeHtml) throws Exception {
 		String url = LinkUtil.buildInternalLinkUrl(context, virtualWiki, wikiLink);
 		String topic = wikiLink.getDestination();
 		if (StringUtils.isBlank(text)) {
 			text = topic;
 		}
 		if (!StringUtils.isBlank(topic) && StringUtils.isBlank(style)) {
 			if (InterWikiHandler.isInterWiki(virtualWiki)) {
 				style = "interwiki";
 			} else if (!LinkUtil.isExistingArticle(virtualWiki, topic)) {
 				style = "edit";
 			}
 		}
 		if (!StringUtils.isBlank(style)) {
 			style = " class=\"" + style + "\"";
 		} else {
 			style = "";
 		}
 		if (!StringUtils.isBlank(target)) {
 			target = " target=\"" + target + "\"";
 		} else {
 			target = "";
 		}
		String html = "<a title=\"" + StringEscapeUtils.escapeHtml(text) + "\" href=\"" + url + "\"" + style + target + ">";
 		if (escapeHtml) {
 			html += StringEscapeUtils.escapeHtml(text);
 		} else {
 			html += text;
 		}
 		html += "</a>";
 		return html;
 	}
 
 	/**
 	 * Build a URL to the topic page for a given topic.
 	 *
 	 * @param context The servlet context path.  If this value is
 	 *  <code>null</code> then the resulting URL will NOT include context path,
 	 *  which breaks HTML links but is useful for servlet redirection URLs.
 	 * @param virtualWiki The virtual wiki for the link that is being created.
 	 * @param topic The topic name for the URL that is being generated.
 	 * @throws Exception Thrown if any error occurs while builing the link
 	 *  URL.
 	 */
 	public static String buildInternalLinkUrl(String context, String virtualWiki, String topic) throws Exception {
 		if (StringUtils.isBlank(topic)) {
 			return null;
 		}
 		WikiLink wikiLink = LinkUtil.parseWikiLink(topic);
 		return LinkUtil.buildInternalLinkUrl(context, virtualWiki, wikiLink);
 	}
 
 	/**
 	 * Build a URL to the topic page for a given topic.
 	 *
 	 * @param context The servlet context path.  If this value is
 	 *  <code>null</code> then the resulting URL will NOT include context path,
 	 *  which breaks HTML links but is useful for servlet redirection URLs.
 	 * @param virtualWiki The virtual wiki for the link that is being created.
 	 * @param wikiLink The WikiLink object containing all relevant information
 	 *  about the link being generated.
 	 * @throws Exception Thrown if any error occurs while builing the link
 	 *  URL.
 	 */
 	public static String buildInternalLinkUrl(String context, String virtualWiki, WikiLink wikiLink) throws Exception {
 		String topic = wikiLink.getDestination();
 		String section = wikiLink.getSection();
 		String query = wikiLink.getQuery();
 		if (StringUtils.isBlank(topic) && !StringUtils.isBlank(section)) {
 			return "#" + Utilities.encodeForURL(section);
 		}
 		if (!LinkUtil.isExistingArticle(virtualWiki, topic)) {
 			return LinkUtil.buildEditLinkUrl(context, virtualWiki, topic, query, -1);
 		}
 		String url = "";
 		if (context != null) {
 			url += context;
 		}
 		// context never ends with a "/" per servlet specification
 		url += "/";
 		// get the virtual wiki, which should have been set by the parent servlet
 		url += Utilities.encodeForURL(virtualWiki);
 		url += "/";
 		url += Utilities.encodeForURL(topic);
 		if (!StringUtils.isBlank(section)) {
 			if (!section.startsWith("#")) {
 				url += "#";
 			}
 			url += Utilities.encodeForURL(section);
 		}
 		if (!StringUtils.isBlank(query)) {
 			if (!query.startsWith("?")) {
 				url += "?";
 			}
 			url += query;
 		}
 		return url;
 	}
 
 	/**
 	 * Generate the HTML for an interwiki anchor link.
 	 *
 	 * @param wikiLink The WikiLink object containing all relevant information
 	 *  about the link being generated.
 	 * @return The HTML anchor tag for the interwiki link.
 	 */
 	public static String interWiki(WikiLink wikiLink) {
 		// remove namespace from link destination
 		String destination = wikiLink.getDestination();
 		String namespace = wikiLink.getNamespace();
 		destination = destination.substring(wikiLink.getNamespace().length() + NamespaceHandler.NAMESPACE_SEPARATOR.length());
 		String url = InterWikiHandler.formatInterWiki(namespace, destination);
 		String text = (!StringUtils.isBlank(wikiLink.getText())) ? wikiLink.getText() : wikiLink.getDestination();
 		return "<a class=\"interwiki\" rel=\"nofollow\" title=\"" + text + "\" href=\"" + url + "\">" + text + "</a>";
 	}
 
 	/**
 	 * Utility method for determining if an article name corresponds to a valid
 	 * wiki link.  In this case an "article name" could be an existing topic, a
 	 * "Special:" page, a user page, an interwiki link, etc.  This method will
 	 * return true if the given name corresponds to a valid special page, user
 	 * page, topic, or other existing article.
 	 *
 	 * @param virtualWiki The virtual wiki for the topic being checked.
 	 * @param articleName The name of the article that is being checked.
 	 * @return <code>true</code> if there is an article that exists for the given
 	 *  name and virtual wiki.
 	 * @throws Exception Thrown if any error occurs during lookup.
 	 */
 	public static boolean isExistingArticle(String virtualWiki, String articleName) throws Exception {
 		if (StringUtils.isBlank(virtualWiki) || StringUtils.isBlank(articleName)) {
 			return false;
 		}
 		if (PseudoTopicHandler.isPseudoTopic(articleName)) {
 			return true;
 		}
 		if (InterWikiHandler.isInterWiki(articleName)) {
 			return true;
 		}
 		if (StringUtils.isBlank(Environment.getValue(Environment.PROP_BASE_FILE_DIR)) || !Environment.getBooleanValue(Environment.PROP_BASE_INITIALIZED)) {
 			// not initialized yet
 			return false;
 		}
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, articleName, false, null);
 		return (topic != null);
 	}
 
 	/**
 	 * Parse a topic name of the form "Topic#Section?Query", and return a WikiLink
 	 * object representing the link.
 	 *
 	 * @param raw The raw topic link text.
 	 * @return A WikiLink object that represents the link.
 	 */
 	public static WikiLink parseWikiLink(String raw) {
 		// note that this functionality was previously handled with a regular
 		// expression, but the expression caused CPU usage to spike to 100%
 		// with topics such as "Urnordisch oder Nordwestgermanisch?"
 		String processed = raw.trim();
 		WikiLink wikiLink = new WikiLink();
 		if (StringUtils.isBlank(processed)) {
 			return new WikiLink();
 		}
 		// first see if the link ends with a query param - "?..."
 		int queryPos = processed.indexOf('?', 1);
 		if (queryPos != -1 && queryPos < processed.length()) {
 			String queryString = processed.substring(queryPos + 1);
 			wikiLink.setQuery(queryString);
 			processed = processed.substring(0, queryPos);
 		}
 		// now look for a section param - "#..."
 		int sectionPos = processed.indexOf('#', 1);
 		if (sectionPos != -1 && sectionPos < processed.length()) {
 			String sectionString = processed.substring(sectionPos + 1);
 			wikiLink.setSection(sectionString);
 			processed = processed.substring(0, sectionPos);
 		}
 		// since we're having so much fun, let's find a namespace (default empty).
 		String namespaceString = "";
 		int namespacePos = processed.indexOf(':', 1);
 		if (namespacePos != -1 && namespacePos < processed.length()) {
 			namespaceString = processed.substring(0, namespacePos);
 		}
 		wikiLink.setNamespace(namespaceString);
 		String topic = processed;
 		if (namespacePos > 0) {
 			topic = processed.substring(namespacePos + 1);
 		}
 		wikiLink.setArticle(Utilities.decodeFromURL(topic, true));
 		// destination is namespace + topic
 		wikiLink.setDestination(Utilities.decodeFromURL(processed, true));
 		return wikiLink;
 	}
 }

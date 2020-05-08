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
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.Namespace;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicType;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.WikiFile;
 import org.jamwiki.model.WikiImage;
 
 /**
  * General utility methods for handling both wiki topic links and HTML links.
  * Wiki topic links are generally of the form "Topic?query=param#Section".
  * HTML links are of the form http://example.com/.
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
 			if (query.charAt(0) != '?') {
 				query = "?" + query;
 			}
 			url = query + "&amp;";
 		} else {
 			url = "?";
 		}
 		if (StringUtils.isBlank(param)) {
 			return query;
 		}
 		url += Utilities.encodeAndEscapeTopicName(param) + "=";
 		if (!StringUtils.isBlank(value)) {
 			url += Utilities.encodeAndEscapeTopicName(value);
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
 	 * @throws DataAccessException Thrown if any error occurs while builing the link URL.
 	 */
 	public static String buildEditLinkUrl(String context, String virtualWiki, String topic, String query, int section) throws DataAccessException {
 		query = LinkUtil.appendQueryParam(query, "topic", topic);
 		if (section > 0) {
 			query += "&amp;section=" + section;
 		}
 		WikiLink wikiLink = new WikiLink();
 		// FIXME - hard coding
 		wikiLink.setDestination("Special:Edit");
 		wikiLink.setQuery(query);
 		return LinkUtil.buildTopicUrl(context, virtualWiki, wikiLink);
 	}
 
 	/**
 	 * Utility method for building the URL to an image file (NOT the image topic
 	 * page).  If the file does not exist then this method will return
 	 * <code>null</code>.
 	 *
 	 * @param context The current servlet context.
 	 * @param virtualWiki The virtual wiki for the URL that is being created.
 	 * @param topicName The name of the image for which a link is being created.
 	 * @return The URL to an image file (not the image topic) or <code>null</code>
 	 *  if the file does not exist.
 	 * @throws DataAccessException Thrown if any error occurs while retrieving file info.
 	 */
 	public static String buildImageFileUrl(String context, String virtualWiki, String topicName) throws DataAccessException {
 		WikiFile wikiFile = WikiBase.getDataHandler().lookupWikiFile(virtualWiki, topicName);
 		if (wikiFile == null) {
 			return null;
 		}
 		String url = FilenameUtils.normalize(Environment.getValue(Environment.PROP_FILE_DIR_RELATIVE_PATH) + "/" + wikiFile.getUrl());
 		return FilenameUtils.separatorsToUnix(url);
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
 	 * @throws DataAccessException Thrown if any error occurs while retrieving image
 	 *  information.
 	 * @throws IOException Thrown if any error occurs while reading image information.
 	 */
 	public static String buildImageLinkHtml(String context, String virtualWiki, String topicName, boolean frame, boolean thumb, String align, String caption, int maxDimension, boolean suppressLink, String style, boolean escapeHtml) throws DataAccessException, IOException {
 		String url = LinkUtil.buildImageFileUrl(context, virtualWiki, topicName);
 		if (url == null) {
 			return LinkUtil.buildUploadLink(context, virtualWiki, topicName);
 		}
 		WikiFile wikiFile = WikiBase.getDataHandler().lookupWikiFile(virtualWiki, topicName);
 		Topic topic = WikiBase.getDataHandler().lookupTopic(virtualWiki, topicName, false, null);
 		StringBuilder html = new StringBuilder();
 		if (topic.getTopicType() == TopicType.FILE) {
 			// file, not an image
 			if (StringUtils.isBlank(caption)) {
 				caption = topicName.substring(Namespace.namespace(Namespace.FILE_ID).getLabel(virtualWiki).length() + 1);
 			}
 			html.append("<a href=\"").append(url).append("\">");
 			if (escapeHtml) {
 				html.append(StringEscapeUtils.escapeHtml(caption));
 			} else {
 				html.append(caption);
 			}
 			html.append("</a>");
 			return html.toString();
 		}
 		WikiImage wikiImage = null;
 		try {
 			wikiImage = ImageUtil.initializeImage(wikiFile, maxDimension);
 		} catch (FileNotFoundException e) {
 			// do not log the full exception as the logs can fill up very for this sort of error, and it is generally due to a bad configuration.  instead log a warning message so that the administrator can try to fix the problem
 			logger.warning("File not found while parsing image link for topic: " + virtualWiki + " / " + topicName + ".  Make sure that the following file exists and is readable by the JAMWiki installation: " + e.getMessage());
 			return LinkUtil.buildUploadLink(context, virtualWiki, topicName);
 		}
 		if (frame || thumb || !StringUtils.isBlank(align)) {
 			html.append("<div class=\"");
 			if (thumb || frame) {
 				html.append("imgthumb ");
 			}
 			if (align != null && align.equalsIgnoreCase("left")) {
 				html.append("imgleft ");
 			} else if (align != null && align.equalsIgnoreCase("center")) {
 				html.append("imgcenter ");
 			} else if ((align != null && align.equalsIgnoreCase("right")) || thumb || frame) {
 				html.append("imgright ");
 			} else {
 				// default alignment
 				html.append("image ");
 			}
 			html = new StringBuilder(html.toString().trim()).append("\">");
 		}
 		if (wikiImage.getWidth() > 0) {
 			html.append("<div style=\"width:").append((wikiImage.getWidth() + 2)).append("px;\">");
 		}
 		if (!suppressLink) {
 			html.append("<a class=\"wikiimg\" href=\"").append(LinkUtil.buildTopicUrl(context, virtualWiki, topicName, true)).append("\">");
 		}
 		if (StringUtils.isBlank(style)) {
 			style = "wikiimg";
 		}
 		html.append("<img class=\"").append(style).append("\" src=\"");
 		html.append(url);
 		html.append('\"');
 		html.append(" width=\"").append(wikiImage.getWidth()).append('\"');
 		html.append(" height=\"").append(wikiImage.getHeight()).append('\"');
 		String alt = (caption != null) ? caption : topic.getPageName();
 		html.append(" alt=\"").append(StringEscapeUtils.escapeHtml(alt)).append('\"');
 		html.append(" />");
 		if (!suppressLink) {
 			html.append("</a>");
 		}
 		if (!StringUtils.isBlank(caption)) {
 			html.append("<div class=\"imgcaption\">");
 			if (escapeHtml) {
 				html.append(StringEscapeUtils.escapeHtml(caption));
 			} else {
 				html.append(caption);
 			}
 			html.append("</div>");
 		}
 		if (wikiImage.getWidth() > 0) {
 			html.append("</div>");
 		}
 		if (frame || thumb || !StringUtils.isBlank(align)) {
 			html.append("</div>");
 		}
 		return html.toString();
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
 	 * @throws DataAccessException Thrown if any error occurs while retrieving
 	 *  topic information.
 	 */
 	public static String buildInternalLinkHtml(String context, String virtualWiki, WikiLink wikiLink, String text, String style, String target, boolean escapeHtml) throws DataAccessException {
 		String url = LinkUtil.buildTopicUrl(context, virtualWiki, wikiLink);
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
 		if (StringUtils.isBlank(topic) && !StringUtils.isBlank(wikiLink.getSection())) {
 			topic = wikiLink.getSection();
 		}
 		StringBuilder html = new StringBuilder();
 		html.append("<a href=\"").append(url).append('\"').append(style);
 		html.append(" title=\"").append(StringEscapeUtils.escapeHtml(topic)).append('\"').append(target).append('>');
 		if (escapeHtml) {
 			html.append(StringEscapeUtils.escapeHtml(text));
 		} else {
 			html.append(text);
 		}
 		html.append("</a>");
 		return html.toString();
 	}
 
 	/**
 	 * Build a URL to the topic page for a given topic.
 	 *
 	 * @param context The servlet context path.  If this value is
 	 *  <code>null</code> then the resulting URL will NOT include context path,
 	 *  which breaks HTML links but is useful for servlet redirection URLs.
 	 * @param virtualWiki The virtual wiki for the link that is being created.
 	 * @param topic The topic name for the URL that is being generated.
 	 * @param validateTopic Set to <code>true</code> if the topic must exist and
 	 *  must not be a "Special:" page.  If the topic does not exist then a link to
 	 *  an edit page will be returned.
 	 * @throws DataAccessException Thrown if any error occurs while retrieving topic
 	 *  information.
 	 */
 	public static String buildTopicUrl(String context, String virtualWiki, String topic, boolean validateTopic) throws DataAccessException {
 		if (StringUtils.isBlank(topic)) {
 			return null;
 		}
 		WikiLink wikiLink = LinkUtil.parseWikiLink(virtualWiki, topic);
 		if (validateTopic) {
 			return LinkUtil.buildTopicUrl(context, virtualWiki, wikiLink);
 		} else {
 			return LinkUtil.buildTopicUrlNoEdit(context, virtualWiki, wikiLink.getDestination(), wikiLink.getSection(), wikiLink.getQuery());
 		}
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
 	 * @throws DataAccessException Thrown if any error occurs while retrieving topic
 	 *  information.
 	 */
 	public static String buildTopicUrl(String context, String virtualWiki, WikiLink wikiLink) throws DataAccessException {
 		String topic = wikiLink.getDestination();
 		String section = wikiLink.getSection();
 		String query = wikiLink.getQuery();
 		String url = LinkUtil.buildTopicUrlNoEdit(context, virtualWiki, topic, section, query);
 		if (StringUtils.isBlank(topic) && !StringUtils.isBlank(section)) {
 			// do not check existence for section links
 			return url;
 		}
 		if (!LinkUtil.isExistingArticle(virtualWiki, topic)) {
 			url = LinkUtil.buildEditLinkUrl(context, virtualWiki, topic, query, -1);
 		}
 		return url;
 	}
 
 	/**
 	 * Build a URL to the topic page for a given topic.  This method does NOT verify
 	 * if the topic exists or if it is a "Special:" page, simply returning the URL
 	 * for the topic and virtual wiki.
 	 *
 	 * @param context The servlet context path.  If this value is
 	 *  <code>null</code> then the resulting URL will NOT include context path,
 	 *  which breaks HTML links but is useful for servlet redirection URLs.
 	 * @param virtualWiki The virtual wiki for the link that is being created.
 	 * @param topicName The name of the topic for which a link is being built.
 	 * @param section The section of the page (#section) for which a link is
 	 *  being built.
 	 * @param queryString Query string parameters to append to the link.
 	 * @throws Exception Thrown if any error occurs while builing the link URL.
 	 */
 	private static String buildTopicUrlNoEdit(String context, String virtualWiki, String topicName, String section, String queryString) {
 		if (StringUtils.isBlank(topicName) && !StringUtils.isBlank(section)) {
 			return "#" + Utilities.encodeAndEscapeTopicName(section);
 		}
 		StringBuilder url = new StringBuilder();
 		if (context != null) {
 			url.append(context);
 		}
 		// context never ends with a "/" per servlet specification
 		url.append('/');
 		// get the virtual wiki, which should have been set by the parent servlet
 		url.append(Utilities.encodeAndEscapeTopicName(virtualWiki));
 		url.append('/');
 		url.append(Utilities.encodeAndEscapeTopicName(topicName));
 		if (!StringUtils.isBlank(queryString)) {
 			if (queryString.charAt(0) != '?') {
 				url.append('?');
 			}
 			url.append(queryString);
 		}
 		if (!StringUtils.isBlank(section)) {
 			if (section.charAt(0) != '#') {
 				url.append('#');
 			}
 			url.append(Utilities.encodeAndEscapeTopicName(section));
 		}
 		return url.toString();
 	}
 
 	/**
 	 *
 	 */
 	private static String buildUploadLink(String context, String virtualWiki, String topicName) throws DataAccessException {
 		WikiLink uploadLink = LinkUtil.parseWikiLink(virtualWiki, "Special:Upload?topic=" + topicName);
 		return LinkUtil.buildInternalLinkHtml(context, virtualWiki, uploadLink, topicName, "edit", null, true);
 	}
 
 	/**
 	 * Generate the HTML for an interwiki anchor link.
 	 *
 	 * @param wikiLink The WikiLink object containing all relevant information
 	 *  about the link being generated.
 	 * @return The HTML anchor tag for the interwiki link.
 	 */
 	public static String interWiki(WikiLink wikiLink) {
 		String url = InterWikiHandler.formatInterWiki(wikiLink.getInterWiki(), wikiLink.getDestination());
 		String text = (!StringUtils.isBlank(wikiLink.getText())) ? wikiLink.getText() : wikiLink.getDestination();
 		return "<a class=\"interwiki\" title=\"" + text + "\" href=\"" + url + "\">" + text + "</a>";
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
 	 * @throws DataAccessException Thrown if an error occurs during lookup.
 	 */
 	public static boolean isExistingArticle(String virtualWiki, String articleName) throws DataAccessException {
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
 		return (WikiBase.getDataHandler().lookupTopicId(virtualWiki, articleName) != null);
 	}
 
 	/**
 	 *
 	 */
 	private static int prefixPosition(String topicName) {
 		int prefixPosition = topicName.indexOf(Namespace.SEPARATOR, 1);
 		// if a match is found and it's not the last character of the name, it's a prefix.
 		return (prefixPosition != -1 && (prefixPosition + 1) < topicName.length()) ? prefixPosition : -1;
 	}
 
 	/**
 	 * Make sure a URL does not contain any extraneous characters such as "//" in
 	 * places where it should not.
 	 *
 	 * @param url The URL to be normalized.
 	 * @return The normalized URL.
 	 */
 	public static String normalize(String url) {
 		if (StringUtils.isBlank(url)) {
 			return url;
 		}
 		// first find the protocol
 		int pos = url.indexOf("://");
 		if (pos == -1 || pos == (url.length() - 1)) {
 			return url;
 		}
 		String protocol = url.substring(0, pos + "://".length());
 		String remainder = url.substring(protocol.length());
 		return protocol + StringUtils.replace(remainder, "//", "/");
 	}
 
 	/**
 	 * Parse a wiki topic link and return a <code>WikiLink</code> object
 	 * representing the link.  Wiki topic links are of the form "Topic?Query#Section".
 	 *
 	 * @param virtualWiki The current virtual wiki.
 	 * @param raw The raw topic link text.
 	 * @return A WikiLink object that represents the link.
 	 */
 	public static WikiLink parseWikiLink(String virtualWiki, String raw) {
 		// note that this functionality was previously handled with a regular
 		// expression, but the expression caused CPU usage to spike to 100%
 		// with topics such as "Urnordisch oder Nordwestgermanisch?"
 		String processed = raw.trim();
 		WikiLink wikiLink = new WikiLink();
 		if (StringUtils.isBlank(processed)) {
 			return new WikiLink();
 		}
 		// first look for a section param - "#..."
 		int sectionPos = processed.indexOf('#');
 		if (sectionPos != -1 && sectionPos < processed.length()) {
 			String sectionString = processed.substring(sectionPos + 1);
 			wikiLink.setSection(sectionString);
 			if (sectionPos == 0) {
 				// link is of the form #section, no more to process
 				return wikiLink;
 			}
 			processed = processed.substring(0, sectionPos);
 		}
 		// now see if the link ends with a query param - "?..."
 		int queryPos = processed.indexOf('?', 1);
 		if (queryPos != -1 && queryPos < processed.length()) {
 			String queryString = processed.substring(queryPos + 1);
 			wikiLink.setQuery(queryString);
 			processed = processed.substring(0, queryPos);
 		}
 		// search for a namespace or virtual wiki
 		String topic = LinkUtil.processVirtualWiki(processed, wikiLink);
 		if (wikiLink.getVirtualWiki() != null) {
 			// strip the virtual wiki
 			processed = topic;
 			virtualWiki = wikiLink.getVirtualWiki().getName();
 		}
 		wikiLink.setText(processed);
 		topic = LinkUtil.processNamespace(virtualWiki, topic, wikiLink);
 		if (!wikiLink.getNamespace().getId().equals(Namespace.MAIN_ID)) {
 			// store the display name WITH any extra spaces
 			wikiLink.setText(processed);
 			// update original text in case topic was of the form "xxx: topic"
 			processed = wikiLink.getNamespace().getLabel(virtualWiki) + Namespace.SEPARATOR + topic;
 		}
 		// if no namespace or virtual wiki, see if there's an interwiki link
 		if (wikiLink.getNamespace().getId().equals(Namespace.MAIN_ID) && wikiLink.getVirtualWiki() == null) {
 			topic = LinkUtil.processInterWiki(processed, wikiLink);
 			if (wikiLink.getInterWiki() != null) {
 				// strip the interwiki
 				processed = topic;
 				wikiLink.setText(processed);
 			}
 		}
 		wikiLink.setArticle(Utilities.decodeTopicName(topic, true));
 		// destination is namespace + topic
 		wikiLink.setDestination(Utilities.decodeTopicName(processed, true));
 		return wikiLink;
 	}
 
 	/**
 	 *
 	 */
 	private static String processInterWiki(String processed, WikiLink wikiLink) {
 		int prefixPosition = LinkUtil.prefixPosition(processed);
 		if (prefixPosition == -1) {
 			return processed;
 		}
 		String linkPrefix = processed.substring(0, prefixPosition).trim();
 		if (InterWikiHandler.isInterWiki(linkPrefix)) {
 			wikiLink.setInterWiki(linkPrefix);
 		}
 		return (wikiLink.getInterWiki() != null) ? processed.substring(prefixPosition + Namespace.SEPARATOR.length()).trim(): processed;
 	}
 
 	/**
 	 *
 	 */
 	private static String processVirtualWiki(String processed, WikiLink wikiLink) {
 		int prefixPosition = LinkUtil.prefixPosition(processed);
 		if (prefixPosition == -1) {
 			return processed;
 		}
 		String linkPrefix = processed.substring(0, prefixPosition).trim();
 		try {
 			VirtualWiki virtualWiki = WikiBase.getDataHandler().lookupVirtualWiki(linkPrefix);
 			if (virtualWiki != null) {
 				wikiLink.setVirtualWiki(virtualWiki);
 			}
 		} catch (DataAccessException e) {
 			// this should not happen, if it does then swallow the error
 			logger.warning("Failure while trying to lookup virtual wiki: " + linkPrefix, e);
 		}
 		return (wikiLink.getVirtualWiki() != null) ? processed.substring(prefixPosition + Namespace.SEPARATOR.length()).trim(): processed;
 	}
 
 	/**
 	 *
 	 */
 	private static String processNamespace(String virtualWiki, String processed, WikiLink wikiLink) {
 		int prefixPosition = LinkUtil.prefixPosition(processed);
 		if (prefixPosition == -1) {
 			return processed;
 		}
 		String linkPrefix = processed.substring(0, prefixPosition).trim();
 		try {
 			Namespace namespace = WikiBase.getDataHandler().lookupNamespace(virtualWiki, linkPrefix);
 			if (namespace != null) {
 				wikiLink.setNamespace(namespace);
 			}
 		} catch (DataAccessException e) {
 			// this should not happen, if it does then swallow the error
 			logger.warning("Failure while trying to lookup namespace: " + linkPrefix, e);
 		}
 		return (!wikiLink.getNamespace().getId().equals(Namespace.MAIN_ID)) ? processed.substring(prefixPosition + Namespace.SEPARATOR.length()).trim(): processed;
 	}
 }

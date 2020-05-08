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
 package org.jamwiki.migrate;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.FileWriter;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.regex.Pattern;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.parsers.SAXParser;
 import org.apache.commons.lang.time.DateUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiVersion;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.NamespaceHandler;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLink;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.jamwiki.utils.XMLUtil;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * The purpose of this class is to import a MediaWiki XML file into JAMWiki.
  */
 public class MediaWikiXmlMigrator extends DefaultHandler implements Migrator {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(MediaWikiXmlMigrator.class.getName());
 	private static final int MEDIAWIKI_MEDIA_NAMESPACE_ID = -2;
 	private static final int MEDIAWIKI_SPECIAL_NAMESPACE_ID = -1;
 	private static final int MEDIAWIKI_MAIN_NAMESPACE_ID = 0;
 	private static final int MEDIAWIKI_TALK_NAMESPACE_ID = 1;
 	private static final int MEDIAWIKI_USER_NAMESPACE_ID = 2;
 	private static final int MEDIAWIKI_USER_TALK_NAMESPACE_ID = 3;
 	private static final int MEDIAWIKI_FILE_NAMESPACE_ID = 6;
 	private static final int MEDIAWIKI_FILE_TALK_NAMESPACE_ID = 7;
 	private static final int MEDIAWIKI_MEDIAWIKI_NAMESPACE_ID = 8;
 	private static final int MEDIAWIKI_MEDIAWIKI_TALK_NAMESPACE_ID = 9;
 	private static final int MEDIAWIKI_TEMPLATE_NAMESPACE_ID = 10;
 	private static final int MEDIAWIKI_TEMPLATE_TALK_NAMESPACE_ID = 11;
 	private static final int MEDIAWIKI_HELP_NAMESPACE_ID = 12;
 	private static final int MEDIAWIKI_HELP_TALK_NAMESPACE_ID = 13;
 	private static final int MEDIAWIKI_CATEGORY_NAMESPACE_ID = 14;
 	private static final int MEDIAWIKI_CATEGORY_TALK_NAMESPACE_ID = 15;
 	private static final String MEDIAWIKI_ELEMENT_NAMESPACE = "namespace";
 	private static final String MEDIAWIKI_ELEMENT_TOPIC = "page";
 	private static final String MEDIAWIKI_ELEMENT_TOPIC_CONTENT = "text";
 	private static final String MEDIAWIKI_ELEMENT_TOPIC_NAME = "title";
 	private static final String MEDIAWIKI_ELEMENT_TOPIC_VERSION = "revision";
 	private static final String MEDIAWIKI_ELEMENT_TOPIC_VERSION_COMMENT = "comment";
 	private static final String MEDIAWIKI_ELEMENT_TOPIC_VERSION_EDIT_DATE = "timestamp";
 	private static final String MEDIAWIKI_ELEMENT_TOPIC_VERSION_IP = "ip";
 	private static final String MEDIAWIKI_ELEMENT_TOPIC_VERSION_USERNAME = "username";
 	// the Mediawiki XML file uses ISO 8601 format for dates
 	private static final String ISO_8601_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
 	private static Map<Integer, String> NAMESPACE_CONVERSION_MAP = new HashMap<Integer, String>();
 	static {
 		NAMESPACE_CONVERSION_MAP.put(MEDIAWIKI_TALK_NAMESPACE_ID, NamespaceHandler.NAMESPACE_COMMENTS);
 		NAMESPACE_CONVERSION_MAP.put(MEDIAWIKI_USER_NAMESPACE_ID, NamespaceHandler.NAMESPACE_USER);
 		NAMESPACE_CONVERSION_MAP.put(MEDIAWIKI_USER_TALK_NAMESPACE_ID, NamespaceHandler.NAMESPACE_USER_COMMENTS);
 		NAMESPACE_CONVERSION_MAP.put(MEDIAWIKI_FILE_NAMESPACE_ID, NamespaceHandler.NAMESPACE_IMAGE);
		NAMESPACE_CONVERSION_MAP.put(MEDIAWIKI_FILE_TALK_NAMESPACE_ID, NamespaceHandler.NAMESPACE_IMAGE_COMMENTS);
 		NAMESPACE_CONVERSION_MAP.put(MEDIAWIKI_TEMPLATE_NAMESPACE_ID, NamespaceHandler.NAMESPACE_TEMPLATE);
 		NAMESPACE_CONVERSION_MAP.put(MEDIAWIKI_TEMPLATE_TALK_NAMESPACE_ID, NamespaceHandler.NAMESPACE_TEMPLATE_COMMENTS);
 		NAMESPACE_CONVERSION_MAP.put(MEDIAWIKI_CATEGORY_NAMESPACE_ID, NamespaceHandler.NAMESPACE_CATEGORY);
 		NAMESPACE_CONVERSION_MAP.put(MEDIAWIKI_CATEGORY_TALK_NAMESPACE_ID, NamespaceHandler.NAMESPACE_CATEGORY_COMMENTS);
 	}
 	private static Map<Integer, String> MEDIAWIKI_NAMESPACE_MAP = new TreeMap<Integer, String>();
 	static {
 		MEDIAWIKI_NAMESPACE_MAP.put(MEDIAWIKI_MEDIA_NAMESPACE_ID, "Media");
 		MEDIAWIKI_NAMESPACE_MAP.put(MEDIAWIKI_SPECIAL_NAMESPACE_ID, "Special");
 		MEDIAWIKI_NAMESPACE_MAP.put(MEDIAWIKI_MAIN_NAMESPACE_ID, "");
 		MEDIAWIKI_NAMESPACE_MAP.put(MEDIAWIKI_TALK_NAMESPACE_ID, "Talk");
 		MEDIAWIKI_NAMESPACE_MAP.put(MEDIAWIKI_USER_NAMESPACE_ID, "User");
 		MEDIAWIKI_NAMESPACE_MAP.put(MEDIAWIKI_USER_TALK_NAMESPACE_ID, "User talk");
 		MEDIAWIKI_NAMESPACE_MAP.put(MEDIAWIKI_FILE_NAMESPACE_ID, "File");
 		MEDIAWIKI_NAMESPACE_MAP.put(MEDIAWIKI_FILE_TALK_NAMESPACE_ID, "File talk");
 		MEDIAWIKI_NAMESPACE_MAP.put(MEDIAWIKI_MEDIAWIKI_NAMESPACE_ID, "Mediawiki");
 		MEDIAWIKI_NAMESPACE_MAP.put(MEDIAWIKI_MEDIAWIKI_TALK_NAMESPACE_ID, "Mediawiki talk");
 		MEDIAWIKI_NAMESPACE_MAP.put(MEDIAWIKI_TEMPLATE_NAMESPACE_ID, "Template");
 		MEDIAWIKI_NAMESPACE_MAP.put(MEDIAWIKI_TEMPLATE_TALK_NAMESPACE_ID, "Template talk");
 		MEDIAWIKI_NAMESPACE_MAP.put(MEDIAWIKI_HELP_NAMESPACE_ID, "Help");
 		MEDIAWIKI_NAMESPACE_MAP.put(MEDIAWIKI_HELP_TALK_NAMESPACE_ID, "Help talk");
 		MEDIAWIKI_NAMESPACE_MAP.put(MEDIAWIKI_CATEGORY_NAMESPACE_ID, "Category");
 		MEDIAWIKI_NAMESPACE_MAP.put(MEDIAWIKI_CATEGORY_TALK_NAMESPACE_ID, "Category talk");
 	}
 
 	/** This map holds the current tag's attribute names and values.  It is cleared after an end-element is called and thus fails for nested elements. */
 	private Map<String, String> currentAttributeMap = new HashMap<String, String>();
 	/** This buffer holds the content of the current element during parsing.  It will be flushed after an end-element tag is reached. */
 	private StringBuffer currentElementBuffer = new StringBuffer();
 	private Topic currentTopic = new Topic();
 	private TopicVersion currentTopicVersion = new TopicVersion();
 	private Map<Date, TopicVersion> currentTopicVersions = new TreeMap<Date, TopicVersion>();
 	private final Map<String, String> mediawikiNamespaceMap = new HashMap<String, String>();
 	private Map<Topic, List<TopicVersion>> parsedTopics = new HashMap<Topic, List<TopicVersion>>();
 	private int previousTopicContentLength = 0;
 
 	/**
 	 *
 	 */
 	protected MediaWikiXmlMigrator() {
 		super();
 	}
 
 	//===========================================================
 	// Import methods
 	//===========================================================
 
 	/**
 	 *
 	 */
 	public Map<Topic, List<TopicVersion>> importFromFile(File file) throws MigrationException {
 		this.importWikiXml(file);
 		return this.parsedTopics;
 	}
 
 	/**
 	 *
 	 */
 	private void importWikiXml(File file) throws MigrationException {
 		// For big file parsing
 		System.setProperty("entityExpansionLimit", "1000000");
 		SAXParserFactory factory = SAXParserFactory.newInstance();
 		try {
 			SAXParser saxParser = factory.newSAXParser();
 			saxParser.parse(file, this);
 		} catch (ParserConfigurationException e) {
 			throw new MigrationException(e);
 		} catch (IOException e) {
 			throw new MigrationException(e);
 		} catch (SAXException e) {
 			throw new MigrationException(e);
 		}
 	}
 
 	/**
 	 * Convert the Wikipedia article namespace (if any) to a JAMWiki article namespace.
 	 */
 	private String convertArticleNameFromWikipediaToJAMWiki(String fullName) {
 		String ret = fullName;
 		int pos = fullName.indexOf(':');
 		if (pos > 0) {
 			String namespace = fullName.substring(0, pos);
 			String title = fullName.substring(pos+1);
 			String jamwikiNamespace = mediawikiNamespaceMap.get(namespace);
 			if (!StringUtils.isBlank(jamwikiNamespace)) {
 				// matching JAMWiki namespace found
 				ret = jamwikiNamespace + ":" + title;
 			}
 		}
 		// remove any characters that are valid for Mediawiki but not JAMWiki
 		ret = StringUtils.remove(ret, '?');
 		return ret;
 	}
 
 	/**
 	 * Convert all namespaces names from MediaWiki to JAMWiki local representation.
 	 */
 	private String convertNamespaces(String text) {
 		String ret = text;
 		// convert all namespaces names from MediaWiki to JAMWiki local representation
 		String jamwikiNamespace;
 		for (String mediawikiNamespace : mediawikiNamespaceMap.keySet()) {
 			jamwikiNamespace = mediawikiNamespaceMap.get(mediawikiNamespace);
 			ret = Pattern.compile("\\[\\[" + mediawikiNamespace + "\\:", Pattern.CASE_INSENSITIVE).matcher(ret).replaceAll("[[" + jamwikiNamespace + ":");
 		}
 		return ret;
 	}
 
 	/**
 	 *
 	 */
 	private Timestamp parseMediaWikiTimestamp(String timestamp) {
 		try {
 			Date date = DateUtils.parseDate(timestamp, new String[]{ISO_8601_DATE_FORMAT});
 			return new Timestamp(date.getTime());
 		} catch (ParseException e) {
 			// FIXME - this should be handled somehow
 			return new Timestamp(System.currentTimeMillis());
 		}
 	}
 
 	//===========================================================
 	// SAX DocumentHandler methods
 	//===========================================================
 
 	/**
 	 * start of xml-tag
 	 *
 	 * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or
 	 *  if Namespace processing is not being performed.
 	 * @param localName The local name (without prefix), or the empty string if Namespace processing
 	 *  is not being performed.
 	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
 	 * @param attributes The attributes attached to the element. If there are no attributes, it shall be an
 	 *  empty Attributes object.
 	 */
 	public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
 		this.currentElementBuffer = new StringBuffer();
 		this.currentAttributeMap = new HashMap<String, String>();
 		String key;
 		if (attrs != null) {
 			// populate the attribute map
 			for (int i = 0; i < attrs.getLength(); i++) {
 				key = attrs.getQName(i);
 				if (!StringUtils.isBlank(key)) {
 					this.currentAttributeMap.put(key, attrs.getValue(i));
 				}
 			}
 		}
 		if (MEDIAWIKI_ELEMENT_TOPIC_VERSION.equals(qName)) {
 			this.currentTopicVersion = new TopicVersion();
 			this.currentTopicVersion.setEditType(TopicVersion.EDIT_IMPORT);
 		} else if (MEDIAWIKI_ELEMENT_TOPIC.equals(qName)) {
 			this.currentTopic = new Topic();
 			this.currentTopicVersions = new TreeMap<Date, TopicVersion>();
 		}
 	}
 
 	/**
 	 * end of xml-tag
 	 *
 	 * @param uri The Namespace URI, or the empty string if the element has no Namespace URI or
 	 *  if Namespace processing is not being performed.
 	 * @param localName The local name (without prefix), or the empty string if Namespace processing
 	 *  is not being performed.
 	 * @param qName The qualified name (with prefix), or the empty string if qualified names are not available.
 	 */
 	public void endElement(String uri, String localName, String qName) throws SAXException {
 		if (StringUtils.equals(MEDIAWIKI_ELEMENT_NAMESPACE, qName)) {
 			int key = NumberUtils.toInt(this.currentAttributeMap.get("key"));
 			String jamwikiNamespace = NAMESPACE_CONVERSION_MAP.get(key);
 			if (!StringUtils.isBlank(jamwikiNamespace)) {
 				String mediawikiNamespace = currentElementBuffer.toString().trim();
 				mediawikiNamespaceMap.put(mediawikiNamespace, jamwikiNamespace);
 			}
 		} else if (MEDIAWIKI_ELEMENT_TOPIC_NAME.equals(qName)) {
 			String topicName = currentElementBuffer.toString().trim();
 			topicName = convertArticleNameFromWikipediaToJAMWiki(topicName);
 			WikiLink wikiLink = LinkUtil.parseWikiLink(topicName);
 			currentTopic.setTopicType(WikiUtil.findTopicTypeForNamespace(wikiLink.getNamespace()));
 			currentTopic.setName(topicName);
 		} else if (MEDIAWIKI_ELEMENT_TOPIC_CONTENT.equals(qName)) {
 			String topicContent = currentElementBuffer.toString().trim();
 			topicContent = convertNamespaces(topicContent);
 			currentTopicVersion.setVersionContent(topicContent);
 			currentTopicVersion.setCharactersChanged(StringUtils.length(topicContent) - previousTopicContentLength);
 			previousTopicContentLength = StringUtils.length(topicContent);
 		} else if (MEDIAWIKI_ELEMENT_TOPIC_VERSION_COMMENT.equals(qName)) {
 			this.currentTopicVersion.setEditComment(currentElementBuffer.toString().trim());
 		} else if (MEDIAWIKI_ELEMENT_TOPIC_VERSION_EDIT_DATE.equals(qName)) {
 			this.currentTopicVersion.setEditDate(this.parseMediaWikiTimestamp(currentElementBuffer.toString().trim()));
 		} else if (MEDIAWIKI_ELEMENT_TOPIC_VERSION_IP.equals(qName) || MEDIAWIKI_ELEMENT_TOPIC_VERSION_USERNAME.equals(qName)) {
 			this.currentTopicVersion.setAuthorDisplay(currentElementBuffer.toString().trim());
 		} else if (MEDIAWIKI_ELEMENT_TOPIC_VERSION.equals(qName)) {
 			this.currentTopicVersions.put(currentTopicVersion.getEditDate(), currentTopicVersion);
 		} else if (MEDIAWIKI_ELEMENT_TOPIC.equals(qName)) {
 			if (this.currentTopicVersions.isEmpty()) {
 				throw new SAXException("No topic versions found for " + currentTopic.getName());
 			}
 			List<TopicVersion> currentTopicVersionList = new ArrayList<TopicVersion>();
 			// topic versions are stored in a tree map to allow sorting... convert to a list
 			TopicVersion lastTopicVersion = null;
 			for (TopicVersion topicVersion : this.currentTopicVersions.values()) {
 				currentTopicVersionList.add(topicVersion);
 				lastTopicVersion = topicVersion;
 			}
 			currentTopic.setTopicContent(lastTopicVersion.getVersionContent());
 			this.parsedTopics.put(currentTopic, currentTopicVersionList);
 		}
 	}
 
 	/**
 	 * When the parser encounters plain text (not XML elements), it calls this method
 	 * which accumulates them in a string buffer
 	 */
 	public void characters(char buf[], int offset, int len) throws SAXException {
 		currentElementBuffer.append(buf, offset, len);
 	}
 
 	//===========================================================
 	// Export methods
 	//===========================================================
 
 	/**
 	 *
 	 */
 	public void exportToFile(File file, Map<Topic, List<TopicVersion>> data) throws MigrationException {
 		FileWriter writer = null;
 		try {
 			writer = new FileWriter(file);
 			writer.write("<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.3/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.3/ http://www.mediawiki.org/xml/export-0.3.xsd\" version=\"0.3\" xml:lang=\"en\">");
 			this.writeSiteInfo(writer);
 			this.writePages(writer, data);
 			writer.write("\n</mediawiki>");
 		} catch (DataAccessException e) {
 			throw new MigrationException(e);
 		} catch (IOException e) {
 			throw new MigrationException(e);
 		} finally {
 			if (writer != null) {
 				try {
 					writer.close();
 				} catch (IOException ignore) {}
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void writeSiteInfo(FileWriter writer) throws DataAccessException, IOException {
 		writer.write("\n<siteinfo>");
 		String sitename = Environment.getValue(Environment.PROP_SITE_NAME);
 		writer.write('\n' + XMLUtil.buildTag("sitename", sitename, true));
 		String base = WikiUtil.getBaseUrl();
 		writer.write('\n' + XMLUtil.buildTag("base", base, true));
 		String generator = "JAMWiki " + WikiVersion.CURRENT_WIKI_VERSION;
 		writer.write('\n' + XMLUtil.buildTag("generator", generator, true));
 		/*
 		Cannot have two titles differing only by case of first letter.  Default behavior through 1.5, $wgCapitalLinks = true
 			<enumeration value="first-letter" />
 		Complete title is case-sensitive. Behavior when $wgCapitalLinks = false
 			<enumeration value="case-sensitive" />
 		Cannot have two titles differing only by case. Not yet implemented as of MediaWiki 1.5
 			<enumeration value="case-insensitive" />
 		*/
 		writer.write('\n' + XMLUtil.buildTag("case", "first-letter", true));
 		writer.write("\n<namespaces>");
 		Map<String, String> attributes = new HashMap<String, String>();
 		String namespace = null;
 		for (Integer key : MEDIAWIKI_NAMESPACE_MAP.keySet()) {
 			namespace = MEDIAWIKI_NAMESPACE_MAP.get(key);
 			attributes.put("key", key.toString());
 			writer.write('\n' + XMLUtil.buildTag("namespace", namespace, attributes, true));
 		}
 		writer.write("\n</namespaces>");
 		writer.write("\n</siteinfo>");
 	}
 
 	/**
 	 *
 	 */
 	private void writePages(FileWriter writer, Map<Topic, List<TopicVersion>> data) throws DataAccessException, IOException {
 		List<TopicVersion> topicVersions = null;
 		Map<String, String> textAttributes = new HashMap<String, String>();
 		textAttributes.put("xml:space", "preserve");
 		for (Topic topic : data.keySet()) {
 			writer.write("\n<page>");
 			writer.write('\n' + XMLUtil.buildTag("title", topic.getName(), true));
 			writer.write('\n' + XMLUtil.buildTag("id", topic.getTopicId()));
 			topicVersions = data.get(topic);
 			for (TopicVersion topicVersion : topicVersions) {
 				writer.write("\n<revision>");
 				writer.write('\n' + XMLUtil.buildTag("id", topicVersion.getTopicVersionId()));
 				writer.write('\n' + XMLUtil.buildTag("timestamp", this.parseJAMWikiTimestamp(topicVersion.getEditDate()), true));
 				writer.write("\n<contributor>");
 				WikiUser user = (topicVersion.getAuthorId() != null) ? WikiBase.getDataHandler().lookupWikiUser(topicVersion.getAuthorId()) : null;
 				if (user != null) {
 					writer.write('\n' + XMLUtil.buildTag("username", user.getUsername(), true));
 					writer.write('\n' + XMLUtil.buildTag("id", user.getUserId()));
 				} else if (Utilities.isIpAddress(topicVersion.getAuthorDisplay())) {
 					writer.write('\n' + XMLUtil.buildTag("ip", topicVersion.getAuthorDisplay(), true));
 				} else {
 					writer.write('\n' + XMLUtil.buildTag("username", topicVersion.getAuthorDisplay(), true));
 				}
 				writer.write("\n</contributor>");
 				writer.write('\n' + XMLUtil.buildTag("comment", topicVersion.getEditComment(), true));
 				writer.write('\n' + XMLUtil.buildTag("text", topicVersion.getVersionContent(), textAttributes, true));
 				writer.write("\n</revision>");
 			}
 			writer.write("\n</page>");
 		}
 	}
 
 	/**
 	 *
 	 */
 	private String parseJAMWikiTimestamp(Timestamp timestamp) {
 		SimpleDateFormat sdf = new SimpleDateFormat(ISO_8601_DATE_FORMAT);
 		return sdf.format(timestamp);
 	}
 }

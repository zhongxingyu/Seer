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
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.parsers.SAXParser;
 import org.apache.commons.lang.time.DateUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.Namespace;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.WikiLink;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 /**
  * Provide functionality for importing a Mediawiki XML file into JAMWiki.
  */
 public class MediaWikiXmlImporter extends DefaultHandler implements TopicImporter {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(MediaWikiXmlImporter.class.getName());
 
 	/** This map holds the current tag's attribute names and values.  It is cleared after an end-element is called and thus fails for nested elements. */
 	private Map<String, String> currentAttributeMap = new HashMap<String, String>();
 	/** This buffer holds the content of the current element during parsing.  It will be flushed after an end-element tag is reached. */
 	private StringBuilder currentElementBuffer = new StringBuilder();
 	private Topic currentTopic = null;
 	private TopicVersion currentTopicVersion = new TopicVersion();
 	private Map<Date, Integer> currentTopicVersions = new TreeMap<Date, Integer>();
 	private final Map<String, String> mediawikiNamespaceMap = new HashMap<String, String>();
 	private Map<Topic, List<Integer>> parsedTopics = new HashMap<Topic, List<Integer>>();
 	private int previousTopicContentLength = 0;
 	private String virtualWiki;
 
 	/**
 	 *
 	 */
 	public Map<Topic, List<Integer>> importFromFile(File file, String virtualWiki) throws MigrationException {
 		this.virtualWiki = virtualWiki;
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
 		FileInputStream fis = null;
 		try {
 			// at least in 1.5, the SaxParser has a bug where files with names like "%25s"
 			// will be read as "%s", generating FileNotFound exceptions.  To work around this
 			// issue use a FileInputStream rather than just SAXParser.parse(file, handler)
 			fis = new FileInputStream(file);
 			SAXParser saxParser = factory.newSAXParser();
 			saxParser.parse(fis, this);
 		} catch (ParserConfigurationException e) {
 			throw new MigrationException(e);
 		} catch (IOException e) {
 			throw new MigrationException(e);
 		} catch (SAXException e) {
 			if (e.getCause() instanceof DataAccessException || e.getCause() instanceof WikiException) {
 				throw new MigrationException(e.getCause());
 			} else {
 				throw new MigrationException(e);
 			}
 		} finally {
 			if (fis != null) {
 				try {
 					fis.close();
 				} catch (IOException ignore) {}
 			}
 		}
 	}
 
 	/**
 	 * Convert the Wikipedia article namespace (if any) to a JAMWiki article namespace.
 	 */
 	private String convertArticleNameFromWikipediaToJAMWiki(String fullName) {
 		String ret = fullName;
 		int pos = fullName.indexOf(Namespace.SEPARATOR);
 		if (pos > 0) {
 			String namespace = fullName.substring(0, pos);
 			String title = fullName.substring(pos+1);
 			String jamwikiNamespace = mediawikiNamespaceMap.get(namespace);
 			if (!StringUtils.isBlank(jamwikiNamespace)) {
 				// matching JAMWiki namespace found
 				ret = jamwikiNamespace + Namespace.SEPARATOR + title;
 			}
 		}
 		// remove any characters that are valid for Mediawiki but not JAMWiki
 		ret = StringUtils.remove(ret, '?');
 		return ret;
 	}
 
 	/**
 	 * Convert all namespaces names from MediaWiki to JAMWiki local representation.
 	 */
 	private void convertToJAMWikiNamespaces(StringBuilder builder) {
 		// convert all namespaces names from MediaWiki to JAMWiki local representation
 		String jamwikiNamespace;
 		for (String mediawikiNamespace : mediawikiNamespaceMap.keySet()) {
 			jamwikiNamespace = mediawikiNamespaceMap.get(mediawikiNamespace);
 			if (jamwikiNamespace == null || StringUtils.equalsIgnoreCase(jamwikiNamespace, mediawikiNamespace)) {
 				continue;
 			}
 			// convert from Mediawiki to JAMWiki namespaces.  handle "[[", "[[:", "{{", "{{:".
 			String wikiLinkPatternString = "(\\[\\[[ ]*(:)?)" + mediawikiNamespace + Namespace.SEPARATOR;
 			this.replaceNamespace(builder, jamwikiNamespace, wikiLinkPatternString);
 			String templatePatternString = "(\\{\\{[ ]*(:)?)" + mediawikiNamespace + Namespace.SEPARATOR;
 			this.replaceNamespace(builder, jamwikiNamespace, templatePatternString);
 		}
 	}
 
 	/**
 	 * Utility method for replacing the original namespaces from the XML with
 	 * namespaces configured for the current JAMWiki instance.
 	 */
 	private void replaceNamespace(StringBuilder builder, String jamwikiNamespace, String patternString) {
 		Pattern mediawikiPattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
 		Matcher matcher = mediawikiPattern.matcher(builder);
 		String replacement;
 		while (matcher.find()) {
 			replacement = matcher.group(1) + jamwikiNamespace + Namespace.SEPARATOR;
 			builder.replace(0, builder.length(), matcher.replaceFirst(replacement));
 			matcher.reset(builder);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private Timestamp parseMediaWikiTimestamp(String timestamp) {
 		try {
 			Date date = DateUtils.parseDate(timestamp, new String[]{MediaWikiConstants.ISO_8601_DATE_FORMAT});
 			return new Timestamp(date.getTime());
 		} catch (ParseException e) {
 			// FIXME - this should be handled somehow
 			return new Timestamp(System.currentTimeMillis());
 		}
 	}
 
 	/**
 	 * Initialize the current topic, validating that it does not yet exist.
 	 */
 	private void initCurrentTopic(String topicName) throws SAXException {
 		Topic existingTopic = null;
 		try {
 			existingTopic = WikiBase.getDataHandler().lookupTopic(this.virtualWiki, topicName, false, null);
 		} catch (DataAccessException e) {
 			throw new SAXException("Failure while validating topic name: " + topicName, e);
 		}
 		if (existingTopic != null && existingTopic.getVirtualWiki().equals(this.virtualWiki)) {
			// FIXME - update so that this merges any new versions instead of throwing an error
			WikiException e = new WikiException(new WikiMessage("import.error.topicexists", topicName));
			throw new SAXException("Topic " + topicName + " already exists and cannot be imported", e);
 		}
		topicName = convertArticleNameFromWikipediaToJAMWiki(topicName);
		WikiLink wikiLink = LinkUtil.parseWikiLink(this.virtualWiki, topicName);
 		this.currentTopic = new Topic(this.virtualWiki, topicName);
 		this.currentTopic.setTopicType(WikiUtil.findTopicTypeForNamespace(wikiLink.getNamespace()));
 	}
 
 	/**
 	 * Write a topic version record to the database.
 	 */
 	private void commitTopicVersion() throws SAXException {
 		// FIXME - support rollback
 		this.currentTopic.setTopicContent(currentTopicVersion.getVersionContent());
 		// only the final import version is logged
 		this.currentTopicVersion.setLoggable(false);
 		// no recent change record needed - can be added by reloading all recent changes if desired
 		this.currentTopicVersion.setRecentChangeAllowed(false);
 		try {
 			// for performance reasons write the topic once to create an initial record, then write
 			// only the version record.
 			if (this.currentTopic.getTopicId() <= 0) {
 				// metadata is needed only for the final import version, so for performance reasons
 				// do not include category or link data for older versions
 				WikiBase.getDataHandler().writeTopic(this.currentTopic, this.currentTopicVersion, null, null);
 			} else {
 				WikiBase.getDataHandler().writeTopicVersion(this.currentTopic, this.currentTopicVersion);
 			}
 		} catch (DataAccessException e) {
 			throw new SAXException("Failure while writing topic: " + this.currentTopic.getName(), e);
 		} catch (WikiException e) {
 			throw new SAXException("Failure while writing topic: " + this.currentTopic.getName(), e);
 		}
 		this.currentTopicVersions.put(this.currentTopicVersion.getEditDate(), this.currentTopicVersion.getTopicVersionId());
 	}
 
 	/**
 	 * After all topic versions have been created for a topic, go back and set the previous topic version
 	 * ID values for each version.  This must be done after parsing since the XML file may not contain
 	 * version records sorted chronologically from oldest to newest.
 	 */
 	private void orderTopicVersions() throws SAXException {
 		if (this.currentTopicVersions.isEmpty()) {
 			throw new SAXException("No topic versions found for " + this.currentTopic.getName());
 		}
 		// topic versions are stored in a tree map to allow sorting... convert to a list
 		List<Integer> currentTopicVersionIdList = new ArrayList<Integer>(this.currentTopicVersions.values());
 		try {
 			WikiBase.getDataHandler().orderTopicVersions(this.currentTopic, currentTopicVersionIdList);
 		} catch (DataAccessException e) {
 			throw new SAXException("Failure while ordering topic versions for topic: " + this.currentTopic.getName(), e);
 		}
 		this.parsedTopics.put(this.currentTopic, currentTopicVersionIdList);
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
 	 * @param attrs The attributes attached to the element. If there are no attributes, it shall be an
 	 *  empty Attributes object.
 	 */
 	public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
 		this.currentElementBuffer = new StringBuilder();
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
 		if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC_VERSION.equals(qName)) {
 			this.currentTopicVersion = new TopicVersion();
 			this.currentTopicVersion.setEditType(TopicVersion.EDIT_IMPORT);
 		} else if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC.equals(qName)) {
 			this.currentTopicVersions = new TreeMap<Date, Integer>();
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
 		if (StringUtils.equals(MediaWikiConstants.MEDIAWIKI_ELEMENT_NAMESPACE, qName)) {
 			int key = NumberUtils.toInt(this.currentAttributeMap.get("key"));
 			try {
 				Namespace jamwikiNamespace = WikiBase.getDataHandler().lookupNamespaceById(key);
 				if (jamwikiNamespace != null) {
 					String mediawikiNamespace = currentElementBuffer.toString().trim();
 					mediawikiNamespaceMap.put(mediawikiNamespace, jamwikiNamespace.getLabel(this.virtualWiki));
 				}
 			} catch (DataAccessException e) {
 				throw new SAXException("Failure while processing namespace with ID: " + key, e);
 			}
 		} else if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC_NAME.equals(qName)) {
 			String topicName = currentElementBuffer.toString().trim();
 			this.initCurrentTopic(topicName);
 		} else if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC_CONTENT.equals(qName)) {
 			this.convertToJAMWikiNamespaces(currentElementBuffer);
 			String topicContent = currentElementBuffer.toString().trim();
 			currentTopicVersion.setVersionContent(topicContent);
 			currentTopicVersion.setCharactersChanged(StringUtils.length(topicContent) - previousTopicContentLength);
 			previousTopicContentLength = StringUtils.length(topicContent);
 		} else if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC_VERSION_COMMENT.equals(qName)) {
 			this.currentTopicVersion.setEditComment(currentElementBuffer.toString().trim());
 		} else if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC_VERSION_EDIT_DATE.equals(qName)) {
 			this.currentTopicVersion.setEditDate(this.parseMediaWikiTimestamp(currentElementBuffer.toString().trim()));
 		} else if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC_VERSION_IP.equals(qName) || MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC_VERSION_USERNAME.equals(qName)) {
 			// Login name in Mediawiki can be longer than 100 characters, so trim to conform to
 			// JAMWiki limits.  In general very long login names seem to be used only by vandals,
 			// so this should be an acceptable workaround.
 			String authorDisplay = currentElementBuffer.toString().trim();
 			if (authorDisplay.length() > 100) {
 				authorDisplay = authorDisplay.substring(0, 100);
 			}
 			this.currentTopicVersion.setAuthorDisplay(authorDisplay);
 		} else if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC_VERSION.equals(qName)) {
 			this.commitTopicVersion();
 		} else if (MediaWikiConstants.MEDIAWIKI_ELEMENT_TOPIC.equals(qName)) {
 			this.orderTopicVersions();
 		}
 	}
 
 	/**
 	 * When the parser encounters plain text (not XML elements), it calls this method
 	 * which accumulates them in a string buffer
 	 */
 	public void characters(char buf[], int offset, int len) throws SAXException {
 		currentElementBuffer.append(buf, offset, len);
 	}
 }

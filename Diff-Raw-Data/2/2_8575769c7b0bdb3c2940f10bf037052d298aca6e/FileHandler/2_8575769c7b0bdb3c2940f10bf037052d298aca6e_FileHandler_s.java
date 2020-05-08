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
  * along with this program (gpl.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.persistency.file;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.Writer;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Vector;
 import org.apache.log4j.Logger;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.model.RecentChange;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.persistency.PersistencyHandler;
 import org.jamwiki.persistency.db.DBDate;
 import org.jamwiki.servlets.JAMController;
 import org.jamwiki.utils.TextFileFilter;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.XMLUtil;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  *
  */
 public class FileHandler extends PersistencyHandler {
 
 	private static final Logger logger = Logger.getLogger(FileHandler.class);
 
 	public static final String TOPIC_DIR = "topics";
 	public static final String VERSION_DIR = "versions";
 	public static final String RECENT_CHANGE_DIR = "changes";
 	public static final String READ_ONLY_DIR = "readonly";
 	public static final String LOCK_DIR = "locks";
 	public final static String EXT = ".xml";
 	// the read-only topics
 	protected Map readOnlyTopics;
 	// file used for storing read-only topics
 	private final static String READ_ONLY_FILE = "ReadOnlyTopics";
 	public static final String VIRTUAL_WIKI_LIST = "virtualwikis.lst";
 	protected static final String XML_RECENT_CHANGE_ROOT = "change";
 	protected static final String XML_RECENT_CHANGE_TOPIC_ID = "topicid";
 	protected static final String XML_RECENT_CHANGE_TOPIC_NAME = "topicname";
 	protected static final String XML_RECENT_CHANGE_TOPIC_VERSION_ID = "topicversionid";
 	protected static final String XML_RECENT_CHANGE_PREVIOUS_TOPIC_VERSION_ID = "previoustopicversionid";
 	protected static final String XML_RECENT_CHANGE_AUTHOR_ID = "authorid";
 	protected static final String XML_RECENT_CHANGE_AUTHOR_NAME = "authorname";
 	protected static final String XML_RECENT_CHANGE_EDIT_COMMENT = "editcomment";
 	protected static final String XML_RECENT_CHANGE_EDIT_DATE = "editdate";
 	protected static final String XML_RECENT_CHANGE_EDIT_TYPE = "edittype";
 	protected static final String XML_RECENT_CHANGE_VIRTUAL_WIKI = "virtualwiki";
 	protected static final String XML_TOPIC_ROOT = "page";
 	protected static final String XML_TOPIC_TITLE = "title";
 	protected static final String XML_TOPIC_ID = "id";
 	protected static final String XML_TOPIC_VIRTUAL_WIKI = "virtualwiki";
 	protected static final String XML_TOPIC_TEXT = "text";
 	protected static final String XML_TOPIC_ADMIN_ONLY = "admin";
 	protected static final String XML_TOPIC_LOCKED_BY = "lockedby";
 	protected static final String XML_TOPIC_LOCK_DATE = "lockdate";
 	protected static final String XML_TOPIC_LOCK_KEY = "lockkey";
 	protected static final String XML_TOPIC_READ_ONLY = "readonly";
 	protected static final String XML_TOPIC_TYPE = "type";
 	protected static final String XML_TOPIC_VERSION_ROOT = "revision";
 	protected static final String XML_TOPIC_VERSION_ID = "id";
 	protected static final String XML_TOPIC_VERSION_TOPIC_ID = "topicid";
 	protected static final String XML_TOPIC_VERSION_AUTHOR = "contributor";
 	protected static final String XML_TOPIC_VERSION_AUTHOR_ID = "id";
 	protected static final String XML_TOPIC_VERSION_AUTHOR_IP_ADDRESS = "ip";
 	protected static final String XML_TOPIC_VERSION_EDIT_COMMENT = "comment";
 	protected static final String XML_TOPIC_VERSION_EDIT_DATE = "timestamp";
 	protected static final String XML_TOPIC_VERSION_EDIT_TYPE = "edittype";
 	protected static final String XML_TOPIC_VERSION_TEXT = "text";
 	private static final String TOPIC_VERSION_ID_FILE = "topic_version.id";
 	private static final String TOPIC_ID_FILE = "topic.id";
 	private static int NEXT_TOPIC_VERSION_ID = -1;
 	private static int NEXT_TOPIC_ID = -1;
 
 	/**
 	 *
 	 */
 	public FileHandler() {
 		this.readOnlyTopics = new HashMap();
 		createDefaults(Locale.ENGLISH);
 	}
 
 	/**
 	 *
 	 */
 	public void addReadOnlyTopic(String virtualWiki, String topicName) throws Exception {
 		Collection roTopics = (Collection) this.readOnlyTopics.get(virtualWiki);
 		roTopics.add(topicName);
 		this.saveReadOnlyTopics(virtualWiki);
 	}
 
 	/**
 	 *
 	 */
 	public void addRecentChange(RecentChange change) throws Exception {
 		StringBuffer content = new StringBuffer();
 		content.append("<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.3/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.3/ http://www.mediawiki.org/xml/export-0.3.xsd\" version=\"0.3\" xml:lang=\"en\">");
 		content.append("\n");
 		content.append("<").append(XML_RECENT_CHANGE_ROOT).append(">");
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_TOPIC_ID, change.getTopicId()));
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_TOPIC_NAME, change.getTopicName(), true));
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_TOPIC_VERSION_ID, change.getTopicVersionId()));
 		content.append("\n");
 		if (change.getPreviousTopicVersionId() > 0) {
 			content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_PREVIOUS_TOPIC_VERSION_ID, change.getPreviousTopicVersionId()));
 			content.append("\n");
 		}
 		content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_AUTHOR_ID, change.getAuthorId()));
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_AUTHOR_NAME, change.getAuthorName(), true));
 		content.append("\n");
 		if (change.getEditComment() != null) {
 			content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_EDIT_COMMENT, change.getEditComment(), true));
 			content.append("\n");
 		}
 		content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_EDIT_DATE, change.getEditDate()));
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_EDIT_TYPE, change.getEditType()));
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_RECENT_CHANGE_VIRTUAL_WIKI, change.getVirtualWiki(), true));
 		content.append("\n");
 		content.append("</").append(XML_RECENT_CHANGE_ROOT).append(">");
 		content.append("\n");
 		content.append("</mediawiki>");
 		String filename = recentChangeFilename(change.getTopicVersionId());
 		File file = FileHandler.getPathFor(change.getVirtualWiki(), FileHandler.RECENT_CHANGE_DIR, filename);
 		Writer writer = new OutputStreamWriter(new FileOutputStream(file), Environment.getValue(Environment.PROP_FILE_ENCODING));
 		writer.write(content.toString());
 		writer.close();
 	}
 
 	/**
 	 *
 	 */
 	public void addTopic(Topic topic) throws Exception {
 		if (topic.getTopicId() <= 0) {
 			topic.setTopicId(nextTopicId());
 		}
 		if (topic.getTopicId() > NEXT_TOPIC_ID) {
 			NEXT_TOPIC_ID = topic.getTopicId();
 		}
 		StringBuffer content = new StringBuffer();
 		content.append("<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.3/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.3/ http://www.mediawiki.org/xml/export-0.3.xsd\" version=\"0.3\" xml:lang=\"en\">");
 		content.append("\n");
 		content.append("<").append(XML_TOPIC_ROOT).append(">");
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_TOPIC_TITLE, topic.getName(), true));
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_TOPIC_ID, topic.getTopicId()));
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_TOPIC_VIRTUAL_WIKI, topic.getVirtualWiki(), true));
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_TOPIC_TEXT, topic.getTopicContent(), true));
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_TOPIC_ADMIN_ONLY, topic.getAdminOnly()));
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_TOPIC_LOCKED_BY, topic.getLockedBy()));
 		content.append("\n");
 		if (topic.getLockedDate() != null) {
 			content.append(XMLUtil.buildTag(XML_TOPIC_LOCK_DATE, topic.getLockedDate()));
 			content.append("\n");
 		}
 		if (topic.getLockSessionKey() != null) {
 			content.append(XMLUtil.buildTag(XML_TOPIC_LOCK_KEY, topic.getLockSessionKey(), true));
 			content.append("\n");
 		}
 		content.append(XMLUtil.buildTag(XML_TOPIC_READ_ONLY, topic.getReadOnly()));
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_TOPIC_TYPE, topic.getTopicType()));
 		content.append("\n");
 		content.append("</").append(XML_TOPIC_ROOT).append(">");
 		content.append("\n");
 		content.append("</mediawiki>");
 		String filename = topicFilename(topic.getName());
 		File file = FileHandler.getPathFor(topic.getVirtualWiki(), FileHandler.TOPIC_DIR, filename);
 		Writer writer = new OutputStreamWriter(new FileOutputStream(file), Environment.getValue(Environment.PROP_FILE_ENCODING));
 		writer.write(content.toString());
 		writer.close();
 	}
 
 	/**
 	 *
 	 */
 	public void addTopicVersion(String virtualWiki, String topicName, TopicVersion topicVersion) throws Exception {
 		if (topicVersion.getTopicVersionId() <= 0) {
 			topicVersion.setTopicVersionId(nextTopicVersionId());
 		}
 		if (topicVersion.getTopicVersionId() > NEXT_TOPIC_VERSION_ID) {
 			NEXT_TOPIC_VERSION_ID = topicVersion.getTopicVersionId();
 		}
 		StringBuffer content = new StringBuffer();
 		content.append("<mediawiki xmlns=\"http://www.mediawiki.org/xml/export-0.3/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.mediawiki.org/xml/export-0.3/ http://www.mediawiki.org/xml/export-0.3.xsd\" version=\"0.3\" xml:lang=\"en\">");
 		content.append("\n");
 		content.append("<").append(XML_TOPIC_VERSION_ROOT).append(">");
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_ID, topicVersion.getTopicVersionId()));
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_TOPIC_ID, topicVersion.getTopicId()));
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_EDIT_DATE, topicVersion.getEditDate()));
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_TEXT, topicVersion.getVersionContent(), true));
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_EDIT_TYPE, topicVersion.getEditType()));
 		content.append("\n");
 		content.append("<").append(XML_TOPIC_VERSION_AUTHOR).append(">");
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_AUTHOR_ID, topicVersion.getAuthorId()));
 		content.append("\n");
 		content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_AUTHOR_IP_ADDRESS, topicVersion.getAuthorIpAddress(), true));
 		content.append("\n");
 		content.append("</").append(XML_TOPIC_VERSION_AUTHOR).append(">");
 		content.append("\n");
 		if (topicVersion.getEditComment() != null) {
 			content.append(XMLUtil.buildTag(XML_TOPIC_VERSION_EDIT_COMMENT, topicVersion.getEditComment(), true));
 			content.append("\n");
 		}
 		content.append("</").append(XML_TOPIC_VERSION_ROOT).append(">");
 		content.append("\n");
 		content.append("</mediawiki>");
 		String filename = topicVersionFilename(topicVersion.getTopicVersionId());
 		File versionFile = FileHandler.getPathFor(virtualWiki, FileHandler.VERSION_DIR, topicName, filename);
 		Writer writer = new OutputStreamWriter(new FileOutputStream(versionFile), Environment.getValue(Environment.PROP_FILE_ENCODING));
 		writer.write(content.toString());
 		writer.close();
 	}
 
 	/**
 	 *
 	 */
 	public void addVirtualWiki(String virtualWiki) throws Exception {
 		Collection all = new ArrayList();
 		File file = getPathFor("", null, VIRTUAL_WIKI_LIST);
 		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), Environment.getValue(Environment.PROP_FILE_ENCODING)));
 		while (true) {
 			String line = in.readLine();
 			if (line == null) break;
 			all.add(line);
 		}
 		in.close();
 		PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), Environment.getValue(Environment.PROP_FILE_ENCODING)));
 		for (Iterator iterator = all.iterator(); iterator.hasNext();) {
 			String s = (String) iterator.next();
 			writer.println(s);
 		}
 		writer.println(virtualWiki);
 		writer.close();
 	}
 
 	/**
 	 * Set up the file system and default topics if necessary
 	 */
 	public void createDefaults(Locale locale) {
 		// create wiki home if necessary
 		File dirCheck = new File(fileBase(""));
 		dirCheck.mkdir();
 		// create default virtual wiki versions directory if necessary
 		File versionDirCheck = getPathFor(null, null, VERSION_DIR);
 		// create the virtual wiki list file if necessary
 		File virtualList = getPathFor(null, null, VIRTUAL_WIKI_LIST);
 		// get the virtual wiki list and set up the file system
 		try {
 			if (!virtualList.exists()) {
 				createVirtualWikiList(virtualList);
 			}
 			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(virtualList), Environment.getValue(Environment.PROP_FILE_ENCODING)));
 			boolean lastOne = false;
 			while (true) {
 				String vWiki = in.readLine();
 				if (vWiki == null) {
 					if (lastOne) {
 						break;
 					} else {
 						// default Wiki (no sub-directory)
 						vWiki = "";
 						lastOne = true;
 					}
 				}
 				logger.debug("Creating defaults for " + vWiki);
 				File dummy;
 				// create the directories for the virtual wiki
 				dummy = getPathFor(vWiki, null, "");
 				dummy = getPathFor(vWiki, null, VERSION_DIR);
 				// write out default topics
 				setupSpecialPage(vWiki, JAMController.getMessage("specialpages.startingpoints", locale));
 				setupSpecialPage(vWiki, JAMController.getMessage("specialpages.leftMenu", locale));
 				setupSpecialPage(vWiki, JAMController.getMessage("specialpages.topArea", locale));
 				setupSpecialPage(vWiki, JAMController.getMessage("specialpages.bottomArea", locale));
 				setupSpecialPage(vWiki, JAMController.getMessage("specialpages.stylesheet", locale));
 				setupSpecialPage(vWiki, JAMController.getMessage("specialpages.adminonlytopics", locale));
 				loadReadOnlyTopics(vWiki);
 			}
 			in.close();
 		} catch (Exception ex) {
 			logger.error(ex);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void createVirtualWikiList(File virtualList) throws Exception {
 		PrintWriter writer = getNewPrintWriter(virtualList, true);
 		writer.println(WikiBase.DEFAULT_VWIKI);
 		writer.close();
 	}
 
 	/**
 	 *
 	 */
 	public static String fileBase(String virtualWiki) {
 		return Environment.getValue(Environment.PROP_BASE_FILE_DIR) + Utilities.sep() + virtualWiki;
 	}
 
 	/**
 	 *
 	 */
 	public List getAllTopicNames(String virtualWiki) throws Exception {
 		List all = new ArrayList();
 		// FIXME - implement
 		return all;
 	}
 
 	/**
 	 * Returns all versions of the given topic in reverse chronological order
 	 * @param virtualWiki
 	 * @param topicName
 	 * @return
 	 * @throws Exception
 	 */
 	public List getAllVersions(String virtualWiki, String topicName) throws Exception {
 		List all = new LinkedList();
 		File[] files = retrieveTopicVersionFiles(virtualWiki, topicName);
 		for (int i = 0; i < files.length; i++) {
 			TopicVersion version = initTopicVersion(files[i]);
 			all.add(version);
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	public List getLockList(String virtualWiki) throws Exception {
 		List all = new LinkedList();
 		File[] files = retrieveLockFiles(virtualWiki);
 		for (int i = 0; i < files.length; i++) {
 			String topicName = Utilities.decodeURL(files[i].getName());
 			Topic topic = lookupTopic(virtualWiki, topicName);
 			if (topic == null) {
 				logger.error("Unable to find topic for locked file " + virtualWiki + " / " + topicName);
 				continue;
 			}
 			all.add(topic);
 		}
 		return all;
 	}
 
 	/**
 	 *  returns a printwriter using utf-8 encoding
 	 *
 	 */
 	private PrintWriter getNewPrintWriter(File file, boolean autoflush) throws Exception {
 		return new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), Environment.getValue(Environment.PROP_FILE_ENCODING)), autoflush);
 	}
 
 	/**
 	 *
 	 */
 	public int getNumberOfVersions(String virtualWiki, String topicName) throws Exception {
 		File[] files = retrieveTopicVersionFiles(virtualWiki, topicName);
 		return (files != null) ? files.length : -1;
 	}
 
 	/**
 	 *
 	 */
 	public static File getPathFor(String virtualWiki, String dir, String fileName) {
 		return getPathFor(virtualWiki, dir, null, fileName);
 	}
 
 	/**
 	 *
 	 */
 	public static File getPathFor(String virtualWiki, String dir1, String dir2, String fileName) {
 		StringBuffer buffer = new StringBuffer();
 		if (virtualWiki == null || virtualWiki.equals(WikiBase.DEFAULT_VWIKI)) {
 			virtualWiki = "";
 		}
 		buffer.append(fileBase(virtualWiki));
 		buffer.append(File.separator);
 		if (dir1 != null) {
 			buffer.append(Utilities.encodeSafeFileName(dir1));
 			buffer.append(File.separator);
 		}
 		if (dir2 != null) {
 			buffer.append(Utilities.encodeSafeFileName(dir2));
 			buffer.append(File.separator);
 		}
 		File directory = new File(buffer.toString());
 		if (!directory.exists()) {
 			directory.mkdirs();
 		}
 		if (fileName != null) {
 			buffer.append(Utilities.encodeSafeFileName(fileName));
 		}
 		return new File(buffer.toString());
 	}
 
 	/**
 	 * Return a list of all read-only topics
 	 */
 	public Collection getReadOnlyTopics(String virtualWiki) throws Exception {
 		logger.debug("Returning read only topics for " + virtualWiki);
 		return (Collection) this.readOnlyTopics.get(virtualWiki);
 	}
 
 	/**
 	 *
 	 */
 	public Collection getRecentChanges(String virtualWiki, int numChanges) throws Exception {
 		List all = new LinkedList();
 		File[] files = retrieveRecentChangeFiles(virtualWiki);
 		if (files == null) return all;
 		for (int i = 0; i < files.length; i++) {
 			if (i >= numChanges) break;
 			RecentChange change = initRecentChange(files[i]);
 			all.add(change);
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	public Collection getVirtualWikiList() throws Exception {
 		Collection all = new ArrayList();
 		File file = getPathFor("", null, VIRTUAL_WIKI_LIST);
 		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), Environment.getValue(Environment.PROP_FILE_ENCODING)));
 		while (true) {
 			String line = in.readLine();
 			if (line == null) break;
 			all.add(line);
 		}
 		in.close();
 		if (!all.contains(WikiBase.DEFAULT_VWIKI)) {
 			all.add(WikiBase.DEFAULT_VWIKI);
 		}
 		return all;
 	}
 
 	/**
 	 * Checks if lock exists
 	 */
 	public synchronized boolean holdsLock(String virtualWiki, String topicName, String key) throws Exception {
 		String filename = lockFilename(topicName);
 		File lockFile = getPathFor(virtualWiki, LOCK_DIR, filename);
 		Topic topic = lookupTopic(virtualWiki, topicName);
 		if (lockFile.exists() && topic != null) {
 			String lockKey = topic.getLockSessionKey();
 			return (lockKey != null && key.equals(lockKey));
 		}
 		return lockTopic(virtualWiki, topicName, key);
 	}
 
 	/**
 	 *
 	 */
 	protected static RecentChange initRecentChange(File file) {
 		if (!file.exists()) return null;
 		try {
 			RecentChange change = new RecentChange();
 			Document document = XMLUtil.parseXML(file, false);
 			// get root node
 			Node rootNode = document.getElementsByTagName(XML_RECENT_CHANGE_ROOT).item(0);
 			NodeList rootChildren = rootNode.getChildNodes();
 			Node rootChild = null;
 			String childName = null;
 			for (int i=0; i < rootChildren.getLength(); i++) {
 				rootChild = rootChildren.item(i);
 				childName = rootChild.getNodeName();
 				if (childName.equals(XML_RECENT_CHANGE_TOPIC_ID)) {
 					change.setTopicId(new Integer(rootChild.getTextContent()).intValue());
 				} else if (childName.equals(XML_RECENT_CHANGE_TOPIC_NAME)) {
 					change.setTopicName(rootChild.getTextContent());
 				} else if (childName.equals(XML_RECENT_CHANGE_TOPIC_VERSION_ID)) {
 					change.setTopicVersionId(new Integer(rootChild.getTextContent()).intValue());
 				} else if (childName.equals(XML_RECENT_CHANGE_PREVIOUS_TOPIC_VERSION_ID)) {
 					change.setPreviousTopicVersionId(new Integer(rootChild.getTextContent()).intValue());
 				} else if (childName.equals(XML_RECENT_CHANGE_AUTHOR_ID)) {
 					change.setAuthorId(new Integer(rootChild.getTextContent()).intValue());
 				} else if (childName.equals(XML_RECENT_CHANGE_AUTHOR_NAME)) {
 					change.setAuthorName(rootChild.getTextContent());
 				} else if (childName.equals(XML_RECENT_CHANGE_EDIT_COMMENT)) {
 					change.setEditComment(rootChild.getTextContent());
 				} else if (childName.equals(XML_RECENT_CHANGE_EDIT_DATE)) {
 					change.setEditDate(Timestamp.valueOf(rootChild.getTextContent()));
 				} else if (childName.equals(XML_RECENT_CHANGE_EDIT_TYPE)) {
 					change.setEditType(new Integer(rootChild.getTextContent()).intValue());
 				} else if (childName.equals(XML_RECENT_CHANGE_VIRTUAL_WIKI)) {
 					change.setVirtualWiki(rootChild.getTextContent());
 				}
 			}
 			return change;
 		} catch (Exception e) {
 			logger.error("Failure while initializing recent changes for file " + file.getAbsolutePath(), e);
 			return null;
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected static Topic initTopic(File file) {
 		if (!file.exists()) return null;
 		try {
 			Topic topic = new Topic();
 			Document document = XMLUtil.parseXML(file, false);
 			// get root node
 			Node rootNode = document.getElementsByTagName(XML_TOPIC_ROOT).item(0);
 			NodeList rootChildren = rootNode.getChildNodes();
 			Node rootChild = null;
 			String childName = null;
 			for (int i=0; i < rootChildren.getLength(); i++) {
 				rootChild = rootChildren.item(i);
 				childName = rootChild.getNodeName();
 				if (childName.equals(XML_TOPIC_TITLE)) {
 					topic.setName(rootChild.getTextContent());
 				} else if (childName.equals(XML_TOPIC_ID)) {
 					topic.setTopicId(new Integer(rootChild.getTextContent()).intValue());
 				} else if (childName.equals(XML_TOPIC_VIRTUAL_WIKI)) {
 					topic.setVirtualWiki(rootChild.getTextContent());
 				} else if (childName.equals(XML_TOPIC_TEXT)) {
 					topic.setTopicContent(rootChild.getTextContent());
 				} else if (childName.equals(XML_TOPIC_ADMIN_ONLY)) {
 					topic.setAdminOnly(new Boolean(rootChild.getTextContent()).booleanValue());
 				} else if (childName.equals(XML_TOPIC_LOCKED_BY)) {
 					topic.setLockedBy(new Integer(rootChild.getTextContent()).intValue());
 				} else if (childName.equals(XML_TOPIC_LOCK_DATE)) {
 					topic.setLockedDate(Timestamp.valueOf(rootChild.getTextContent()));
 				} else if (childName.equals(XML_TOPIC_LOCK_KEY)) {
 					topic.setLockSessionKey(rootChild.getTextContent());
 				} else if (childName.equals(XML_TOPIC_READ_ONLY)) {
 					topic.setReadOnly(new Boolean(rootChild.getTextContent()).booleanValue());
 				} else if (childName.equals(XML_TOPIC_TYPE)) {
 					topic.setTopicType(new Integer(rootChild.getTextContent()).intValue());
 				}
 			}
 			return topic;
 		} catch (Exception e) {
 			logger.error("Failure while initializing topic for file " + file.getAbsolutePath(), e);
 			return null;
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected static TopicVersion initTopicVersion(File file) {
 		if (!file.exists()) return null;
 		try {
 			TopicVersion topicVersion = new TopicVersion();
 			Document document = XMLUtil.parseXML(file, false);
 			// get root node
 			Node rootNode = document.getElementsByTagName(XML_TOPIC_VERSION_ROOT).item(0);
 			NodeList rootChildren = rootNode.getChildNodes();
 			Node rootChild = null;
 			String childName = null;
 			for (int i=0; i < rootChildren.getLength(); i++) {
 				rootChild = rootChildren.item(i);
 				childName = rootChild.getNodeName();
 				if (childName.equals(XML_TOPIC_VERSION_ID)) {
 					topicVersion.setTopicVersionId(new Integer(rootChild.getTextContent()).intValue());
 				} else if (childName.equals(XML_TOPIC_VERSION_TOPIC_ID)) {
 					topicVersion.setTopicId(new Integer(rootChild.getTextContent()).intValue());
 				} else if (childName.equals(XML_TOPIC_VERSION_EDIT_COMMENT)) {
 					topicVersion.setEditComment(rootChild.getTextContent());
 				} else if (childName.equals(XML_TOPIC_VERSION_EDIT_DATE)) {
 					topicVersion.setEditDate(Timestamp.valueOf(rootChild.getTextContent()));
 				} else if (childName.equals(XML_TOPIC_VERSION_EDIT_TYPE)) {
 					topicVersion.setEditType(new Integer(rootChild.getTextContent()).intValue());
 				} else if (childName.equals(XML_TOPIC_VERSION_TEXT)) {
 					topicVersion.setVersionContent(rootChild.getTextContent());
 				} else if (childName.equals(XML_TOPIC_VERSION_AUTHOR)) {
 					NodeList authorChildren = rootChild.getChildNodes();
 					for (int j=0; j < authorChildren.getLength(); j++) {
 						Node authorChild = authorChildren.item(j);
 						if (authorChild.getNodeName().equals(XML_TOPIC_VERSION_AUTHOR_ID)) {
 							topicVersion.setAuthorId(new Integer(authorChild.getTextContent()).intValue());
 						} else if (childName.equals(XML_TOPIC_VERSION_AUTHOR_IP_ADDRESS)) {
 							topicVersion.setAuthorIpAddress(authorChild.getTextContent());
 						}
 					}
 				}
 			}
 			return topicVersion;
 		} catch (Exception e) {
 			logger.error("Failure while initializing topic version for file " + file.getAbsolutePath(), e);
 			return null;
 		}
 	}
 
 	/**
 	 * Read the read-only topics from disk
 	 */
 	protected synchronized void loadReadOnlyTopics(String virtualWiki) {
 		logger.debug("Loading read only topics for " + virtualWiki);
 		Collection roTopics = new ArrayList();
 		File roFile = getPathFor(virtualWiki, null, READ_ONLY_FILE);
 		if (!roFile.exists()) {
 			logger.debug("Empty read only topics for " + virtualWiki);
 			if (virtualWiki == null || virtualWiki.equals("")) {
 				virtualWiki = WikiBase.DEFAULT_VWIKI;
 			}
 			this.readOnlyTopics.put(virtualWiki, roTopics);
 			return;
 		}
 		logger.debug("Loading read-only topics from " + roFile);
 		BufferedReader in = null;
 		try {
 			roFile.createNewFile();
 			in = new BufferedReader(new InputStreamReader(new FileInputStream(roFile), Environment.getValue(Environment.PROP_FILE_ENCODING)));
 		} catch (Exception e) {
 			logger.error(e);
 		}
 		while (true) {
 			String line = null;
 			try {
 				line = in.readLine();
 			} catch (Exception e) {
 				logger.error(e);
 			}
 			if (line == null) break;
 			roTopics.add(line);
 		}
 		try {
 			in.close();
 		} catch (Exception e) {
 			logger.error(e);
 		}
 		if (virtualWiki.equals("")) {
 			virtualWiki = WikiBase.DEFAULT_VWIKI;
 		}
 		this.readOnlyTopics.put(virtualWiki, roTopics);
 	}
 
 	/**
 	 *
 	 */
 	protected static String lockFilename(String topicName) {
 		return topicName;
 	}
 
 	/**
 	 * Locks a file for editing
 	 */
 	public synchronized boolean lockTopic(String virtualWiki, String topicName, String key) throws Exception {
 		if (!super.lockTopic(virtualWiki, topicName, key)) {
 			return false;
 		}
 		String filename = lockFilename(topicName);
 		File lockFile = getPathFor(virtualWiki, LOCK_DIR, filename);
 		Writer writer = new OutputStreamWriter(new FileOutputStream(lockFile), Environment.getValue(Environment.PROP_FILE_ENCODING));
 		writer.write(key);
 		writer.close();
 		return true;
 	}
 
 	/**
 	 *
 	 */
 	public synchronized TopicVersion lookupLastTopicVersion(String virtualWiki, String topicName) throws Exception {
 		// get all files, sorted.  last one is last version.
 		File[] files = retrieveTopicVersionFiles(virtualWiki, topicName);
 		if (files == null) return null;
 		File file = files[0];
 		return initTopicVersion(file);
 	}
 
 	/**
 	 *
 	 */
 	public Topic lookupTopic(String virtualWiki, String topicName) throws Exception {
 		String filename = topicFilename(topicName);
 		File file = getPathFor(virtualWiki, FileHandler.TOPIC_DIR, filename);
 		return initTopic(file);
 	}
 
 	/**
 	 *
 	 */
 	public TopicVersion lookupTopicVersion(String virtualWiki, String topicName, int topicVersionId) throws Exception {
 		String filename = topicVersionFilename(topicVersionId);
 		File file = getPathFor(virtualWiki, VERSION_DIR, topicName, filename);
 		return initTopicVersion(file);
 	}
 
 	/**
 	 *
 	 */
 	private static int nextTopicId() throws Exception {
 		if (NEXT_TOPIC_ID < 0) {
 			// read value from file
 			File topicIdFile = getPathFor(null, null, TOPIC_ID_FILE);
 			if (!topicIdFile.exists()) {
 				NEXT_TOPIC_ID = 0;
 			} else {
 				NEXT_TOPIC_ID = new Integer(read(topicIdFile).toString()).intValue();
 			}
 		}
 		// FIXME - need to update topic.id file
 		return NEXT_TOPIC_ID++;
 	}
 
 	/**
 	 *
 	 */
 	private static int nextTopicVersionId() throws Exception {
 		if (NEXT_TOPIC_VERSION_ID < 0) {
 			// read value from file
 			File topicVersionIdFile = getPathFor(null, null, TOPIC_VERSION_ID_FILE);
 			if (!topicVersionIdFile.exists()) {
 				NEXT_TOPIC_VERSION_ID = 0;
 			} else {
 				NEXT_TOPIC_VERSION_ID = new Integer(read(topicVersionIdFile).toString()).intValue();
 			}
 		}
 		// FIXME - need to update topic-version.id file
 		return NEXT_TOPIC_VERSION_ID++;
 	}
 
 	/**
 	 *
 	 */
 	public Collection purgeDeletes(String virtualWiki) throws Exception {
 		Collection all = new ArrayList();
 		File file = getPathFor(virtualWiki, null, "");
 		File[] files = file.listFiles(new TextFileFilter());
 		for (int i = 0; i < files.length; i++) {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(files[i]), Environment.getValue(Environment.PROP_FILE_ENCODING)));
 			String line = reader.readLine();
 			reader.close();
 			if (line != null) {
 				if (line.trim().equals("delete")) {
 					files[i].delete();
 					String name = files[i].getName();
 					all.add(Utilities.decodeSafeFileName(name.substring(0, name.length() - 4)));
 				}
 			}
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	public void purgeVersionsOlderThan(String virtualWiki, DBDate date) throws Exception {
 		throw new UnsupportedOperationException("New version purging available for file handler yet");
 	}
 
 	/**
 	 *
 	 */
 	public static StringBuffer read(File file) throws Exception {
 		StringBuffer contents = new StringBuffer();
 		if (file.exists()) {
 			FileReader reader = new FileReader(file);
 			char[] buf = new char[4096];
 			int c;
 			while ((c = reader.read(buf, 0, buf.length)) != -1) {
 				contents.append(buf, 0, c);
 			}
 			reader.close();
 		} else {
 			logger.debug("File does not exist, returning default contents: " + file);
 			contents.append("This is a new topic");
 		}
 		return contents;
 	}
 
 	/**
 	 *
 	 */
 	protected static String recentChangeFilename(int topicVersionId) {
 		return topicVersionId + EXT;
 	}
 
 	/**
 	 *
 	 */
 	public void removeReadOnlyTopic(String virtualWiki, String topicName) throws Exception {
 		((Collection) this.readOnlyTopics.get(virtualWiki)).remove(topicName);
 		this.saveReadOnlyTopics(virtualWiki);
 	}
 
 	/**
 	 *
 	 */
 	private File[] retrieveLockFiles(String virtualWiki) throws Exception {
 		File file = FileHandler.getPathFor(virtualWiki, null, FileHandler.LOCK_DIR);
 		File[] files = file.listFiles();
 		if (files == null) return null;
 		return files;
 	}
 
 	/**
 	 *
 	 */
 	private File[] retrieveRecentChangeFiles(String virtualWiki) throws Exception {
 		File file = FileHandler.getPathFor(virtualWiki, null, FileHandler.RECENT_CHANGE_DIR);
 		File[] files = file.listFiles();
 		if (files == null) return null;
 		Comparator comparator = new WikiFileComparator();
 		Arrays.sort(files, comparator);
 		return files;
 	}
 
 	/**
 	 *
 	 */
 	private File[] retrieveTopicVersionFiles(String virtualWiki, String topicName) throws Exception {
 		File file = FileHandler.getPathFor(virtualWiki, FileHandler.VERSION_DIR, topicName);
 		File[] files = file.listFiles();
 		if (files == null) return null;
 		Comparator comparator = new WikiFileComparator();
 		Arrays.sort(files, comparator);
 		return files;
 	}
 
 	/**
 	 * Write the read-only list out to disk
 	 */
 	protected synchronized void saveReadOnlyTopics(String virtualWiki) throws Exception {
 		File roFile = getPathFor(virtualWiki, null, READ_ONLY_FILE);
 		logger.debug("Saving read-only topics to " + roFile);
 		Writer out = new OutputStreamWriter(new FileOutputStream(roFile), Environment.getValue(Environment.PROP_FILE_ENCODING));
 		Iterator it = ((Collection) this.readOnlyTopics.get(virtualWiki)).iterator();
 		while (it.hasNext()) {
 			out.write((String) it.next() + System.getProperty("line.separator"));
 		}
 		out.close();
 		logger.debug("Saved read-only topics: " + this.readOnlyTopics);
 	}
 
 	/**
 	 *
 	 */
 	protected static String topicFilename(String topicName) {
 		return topicName + EXT;
 	}
 
 	/**
 	 *
 	 */
 	protected static String topicVersionFilename(int topicVersionId) {
 		return topicVersionId + EXT;
 	}
 
 	/**
 	 * Unlocks a locked file
 	 */
 	public synchronized void unlockTopic(Topic topic) throws Exception {
 		super.unlockTopic(topic);
 		String filename = lockFilename(topic.getName());
		File lockFile = getPathFor(topic.getVirtualWiki(), null, filename);
 		if (!lockFile.exists()) {
 			logger.warn("No lockfile to unlock topic " + topic.getVirtualWiki() + " / " + topic.getName());
 		}
 		lockFile.delete();
 	}
 
 	/**
 	 *
 	 */
 	public void write(Topic topic, TopicVersion topicVersion) throws Exception {
 		super.write(topic, topicVersion);
 		unlockTopic(topic);
 	}
 
 	/**
 	 *
 	 */
 	class WikiFileComparator implements Comparator {
 
 		/**
 		 *
 		 */
 		public int compare(Object first, Object second) {
 			String one = ((File)first).getName();
 			String two = ((File)second).getName();
 			int pos = one.lastIndexOf(EXT);
 			int arg1 = new Integer(one.substring(0, pos)).intValue();
 			pos = two.lastIndexOf(EXT);
 			int arg2 = new Integer(two.substring(0, pos)).intValue();
 			return arg2 - arg1;
 		}
 	}
 }

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
 package org.jamwiki;
 
 import java.io.StringReader;
 import java.lang.reflect.Constructor;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Set;
 import java.util.TreeSet;
 import org.apache.log4j.Logger;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.persistency.PersistencyHandler;
 import org.jamwiki.persistency.db.DatabaseHandler;
 import org.jamwiki.persistency.db.DatabaseSearchEngine;
 import org.jamwiki.persistency.file.FileHandler;
 import org.jamwiki.persistency.file.FileSearchEngine;
 import org.jamwiki.parser.AbstractParser;
 import org.jamwiki.parser.ParserInfo;
 import org.jamwiki.parser.alt.BackLinkLex;
 import org.jamwiki.search.SearchEngine;
 import org.jamwiki.search.SearchRefreshThread;
 import org.jamwiki.servlets.JAMWikiServlet;
 import org.jamwiki.users.LdapUsergroup;
 import org.jamwiki.users.NoUsergroup;
 import org.jamwiki.users.Usergroup;
 import org.jamwiki.utils.Utilities;
 
 /**
  * This class represents the core of JAMWiki. It has some central methods, like parsing the URI, and keeps an
  * instance of the <code>Environment</code> class.
  */
 public class WikiBase {
 
 	// FIXME - remove this
 	public final static String WIKI_VERSION = "0.0.7";
 	/** An instance to myself. Singleton pattern. */
 	private static WikiBase instance;
 	/** The topics are stored in a flat file */
 	public static final int FILE = 0;
 	/** The topics are stored in a database */
 	public static final int DATABASE = 1;
 	/** Members are retrieved from LDAP */
 	public static final int LDAP = 2;
 	/** Name of the default wiki */
 	public static final String DEFAULT_VWIKI = "en";
 	/** Log output */
 	private static final Logger logger = Logger.getLogger(WikiBase.class);
 	/** The handler that looks after read/write operations for a persitence type */
 	private static PersistencyHandler handler;
 	/** Number of virtual wikis */
 	private static int virtualWikiCount;
 	public static final String NAMESPACE_COMMENTS = "Comments:";
 	public static final String NAMESPACE_IMAGE = "Image:";
 	public static final String NAMESPACE_IMAGE_COMMENTS = "Image comments:";
 	public static final String NAMESPACE_SPECIAL = "Special:";
 	public static final String NAMESPACE_USER = "User:";
 	public static final String NAMESPACE_USER_COMMENTS = "User comments:";
 
 	static {
 		try {
 			instance = new WikiBase();
 		} catch (Exception e) {
 			logger.error("Failure while initializing WikiBase", e);
 		}
 	}
 
 	/**
 	 * Creates an instance of <code>WikiBase</code> with a specified persistency sub-system.
 	 *
 	 * @param persistencyType
 	 * @throws Exception If the handler cannot be instanciated.
 	 */
 	private WikiBase() throws Exception {
 		WikiBase.init();
 	}
 
 	/**
 	 *
 	 */
 	public static synchronized String parse(ParserInfo parserInfo, String content, String topicName) throws Exception {
 		return WikiBase.parse(parserInfo, content, topicName, false);
 	}
 
 	/**
 	 *
 	 */
 	public static synchronized String parsePreSave(ParserInfo parserInfo, String content) throws Exception {
 		return WikiBase.parse(parserInfo, content, null, true);
 	}
 
 	/**
 	 *
 	 */
 	private static String parse(ParserInfo parserInfo, String content, String topicName, boolean preSave) throws Exception {
 		if (content == null) {
 			// FIXME - return empty or something else?
 			return "";
 		}
 		String parserClass = Environment.getValue(Environment.PROP_PARSER_CLASS);
 		logger.debug("Using parser: " + parserClass);
 		Class clazz = Class.forName(parserClass);
 		Class[] parameterTypes = new Class[1];
 		parameterTypes[0] = Class.forName("org.jamwiki.parser.ParserInfo");
 		Constructor constructor = clazz.getConstructor(parameterTypes);
 		Object[] initArgs = new Object[1];
 		initArgs[0] = parserInfo;
 		AbstractParser parser = (AbstractParser)constructor.newInstance(initArgs);
		return (preSave) ? parser.parsePreSave(content) : parser.parseHTML(content, topicName);
 	}
 
 	/**
 	 * TODO: DOCUMENT ME!
 	 *
 	 * @param virtualWiki TODO: DOCUMENT ME!
 	 * @param topicName   TODO: DOCUMENT ME!
 	 * @return TODO: DOCUMENT ME!
 	 * @throws Exception TODO: DOCUMENT ME!
 	 */
 	public static synchronized boolean exists(String virtualWiki, String topicName) throws Exception {
 		if (PseudoTopicHandler.isPseudoTopic(topicName)) {
 			return true;
 		}
 		return handler.exists(virtualWiki, topicName);
 	}
 
 	/**
 	 * return the current handler instance
 	 *
 	 * @return the current handler instance
 	 */
 	public static PersistencyHandler getHandler() {
 		return handler;
 	}
 
 	/**
 	 * Find all topics without links to them
 	 */
 	public static Collection getOrphanedTopics(String virtualWiki) throws Exception {
 		Collection results = new HashSet();
 		Collection all = getHandler().getAllTopicNames(virtualWiki);
 		for (Iterator iterator = all.iterator(); iterator.hasNext();) {
 			String topicName = (String) iterator.next();
 			Collection matches = getSearchEngineInstance().findLinkedTo(
 				virtualWiki,
 				topicName
 			);
 			logger.debug(topicName + ": " + matches.size() + " matches");
 			if (matches.size() == 0) {
 				results.add(topicName);
 			}
 		}
 		logger.debug(results.size() + " orphaned topics found");
 		return results;
 	}
 
 	/**
 	 *
 	 */
 	public static int getPersistenceType() {
 		if (Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE).equals("DATABASE")) {
 			return WikiBase.DATABASE;
 		} else {
 			return WikiBase.FILE;
 		}
 	}
 
 	/**
 	 * Get an instance to the search enginge.
 	 *
 	 * @return Reference to the SearchEngine
 	 * @throws Exception the current search engine
 	 */
 	public static SearchEngine getSearchEngineInstance() throws Exception {
 		switch (WikiBase.getPersistenceType()) {
 			case FILE:
 				return FileSearchEngine.getInstance();
 			case DATABASE:
 				return DatabaseSearchEngine.getInstance();
 			default:
 				return FileSearchEngine.getInstance();
 		}
 	}
 
 	/**
 	 * Find all topics that haven't been written but are linked to
 	 */
 	public static Collection getToDoWikiTopics(String virtualWiki) throws Exception {
 		Collection results = new TreeSet();
 		Collection all = getHandler().getAllTopicNames(virtualWiki);
 		Set topicNames = new HashSet();
 		for (Iterator iterator = all.iterator(); iterator.hasNext();) {
 			String topicName = (String) iterator.next();
 			Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
 			String content = topic.getTopicContent();
 			StringReader reader = new StringReader(content);
 			BackLinkLex lexer = new BackLinkLex(reader);
 			while (lexer.yylex() != null) {
 				;
 			}
 			reader.close();
 			topicNames.addAll(lexer.getLinks());
 		}
 		for (Iterator iterator = topicNames.iterator(); iterator.hasNext();) {
 			String topicName = (String) iterator.next();
 			if (!PseudoTopicHandler.isPseudoTopic(topicName)
 				&& !handler.exists(virtualWiki, topicName)
 				&& !"\\\\\\\\link\\\\\\\\".equals(topicName)) {
 				results.add(topicName);
 			}
 		}
 		logger.debug(results.size() + " todo topics found");
 		return results;
 	}
 
 	/**
 	 * Get an instance of the user group
 	 *
 	 * @return Reference to the SearchEngine
 	 * @throws Exception the current search engine
 	 */
 	public static Usergroup getUsergroupInstance() throws Exception {
 		switch (Usergroup.getUsergroupType()) {
 			case LDAP:
 				return LdapUsergroup.getInstance();
 			//TODO case DATABASE:
 			//  return DatabaseUsergroup.getInstance();
 			default:
 				return NoUsergroup.getInstance();
 		}
 	}
 
 	/**
 	 * get a count of the number of virtual wikis in the system
 	 *
 	 * @return the number of virtual wikis
 	 */
 	public static int getVirtualWikiCount() {
 		if (virtualWikiCount == 0) {
 			try {
 				virtualWikiCount = getVirtualWikiList().size();
 			} catch (Exception e) {
 				logger.warn(e);
 			}
 		}
 		return virtualWikiCount;
 	}
 
 	/**
 	 * Return a list of all virtual wikis on the server
 	 */
 	public static Collection getVirtualWikiList() throws Exception {
 		return WikiBase.handler.getVirtualWikiList();
 	}
 
 	/**
 	 * Singleton. Retrieves an intance of <code>WikiBase</code> and creates one if it doesn't exist yet.
 	 *
 	 * @return Instance of this class
 	 * @throws Exception If the storage produces errors
 	 */
 	private static void init() throws Exception {
 		int persistenceType = -1;
 		String type = Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE);
 		if (type != null && type.equals("DATABASE")) {
 			WikiBase.handler = new DatabaseHandler();
 		} else {
 			WikiBase.handler = new FileHandler();
 		}
 		new SearchRefreshThread(Environment.getIntValue(Environment.PROP_SEARCH_INDEX_REFRESH_INTERVAL));
 	}
 
 	/**
 	 * TODO: DOCUMENT ME!
 	 *
 	 * @throws Exception TODO: DOCUMENT ME!
 	 */
 	public static void initialise(Locale locale, WikiUser user) throws Exception {
 		int persistenceType = WikiBase.getPersistenceType();
 		WikiMail.init();
 		instance = new WikiBase();
 		instance.getHandler().initialize(locale, user);
 		JAMWikiServlet.removeCachedContents();
 	}
 
 	/**
 	 * Reads a file and returns the raw contents. Used for the editing version.
 	 */
 	public static synchronized String readRaw(String virtualWiki, String topicName) throws Exception {
 		Topic topic = WikiBase.getHandler().lookupTopic(virtualWiki, topicName);
 		// FIXME - return null or empty?
 		if (topic == null) return "";
 		return topic.getTopicContent();
 	}
 }

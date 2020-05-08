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
 
 import java.util.Locale;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.persistency.PersistencyHandler;
 import org.jamwiki.persistency.db.DatabaseHandler;
 import org.jamwiki.persistency.file.FileHandler;
 import org.jamwiki.search.LuceneSearchEngine;
 import org.jamwiki.search.SearchEngine;
 import org.jamwiki.servlets.JAMWikiServlet;
 import org.jamwiki.users.LdapUsergroup;
 import org.jamwiki.users.NoUsergroup;
 import org.jamwiki.users.Usergroup;
 import org.jamwiki.utils.InterWikiHandler;
 import org.jamwiki.utils.PseudoTopicHandler;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.util.StringUtils;
 
 /**
  * This class should be used as a singleton instance for retrieving core JAMWiki
  * elements, such as the current persitency handler (database, file, etc), current
  * software version, etc.
  */
 public class WikiBase {
 
 	/** Standard logger. */
 	private static WikiLogger logger = WikiLogger.getLogger(WikiBase.class.getName());
 	/** The singleton instance of this class. */
 	private static WikiBase instance = null;
 	/** The handler that looks after read/write operations for a persistence type. */
 	private static PersistencyHandler handler = null;
 	/** The search engine instance. */
 	private static SearchEngine searchEngine = null;
 
 	/** The topics are stored in a flat file */
 	public static final int FILE = 0;
 	/** The topics are stored in a database */
 	public static final int DATABASE = 1;
 	/** Members are retrieved from LDAP */
 	public static final int LDAP = 2;
 	/** Name of the default wiki */
 	public static final String DEFAULT_VWIKI = "en";
 	public static final String NAMESPACE_CATEGORY = "Category";
 	public static final String NAMESPACE_CATEGORY_COMMENTS = "Category comments";
 	public static final String NAMESPACE_COMMENTS = "Comments";
 	public static final String NAMESPACE_IMAGE = "Image";
 	public static final String NAMESPACE_IMAGE_COMMENTS = "Image comments";
 	public static final String NAMESPACE_JAMWIKI = "JAMWiki";
	public static final String NAMESPACE_JAMWIKI_COMMENTS = "JAMWiki comments";
 	public static final String NAMESPACE_SPECIAL = "Special";
 	public static final String NAMESPACE_USER = "User";
 	public static final String NAMESPACE_USER_COMMENTS = "User comments";
 	public static final String NAMESPACE_SEPARATOR = ":";
 	public static final String SPECIAL_PAGE_DIR = "pages";
 	public static final String SPECIAL_PAGE_STARTING_POINTS = "StartingPoints";
 	public static final String SPECIAL_PAGE_LEFT_MENU = "LeftMenu";
 	public static final String SPECIAL_PAGE_BOTTOM_AREA = "BottomArea";
 	public static final String SPECIAL_PAGE_STYLESHEET = "StyleSheet";
 	public static final String SPECIAL_PAGE_ADMIN_ONLY_TOPICS = "AdminOnlyTopics";
 
 	static {
 		try {
 			WikiBase.instance = new WikiBase();
 		} catch (Exception e) {
 			logger.severe("Failure while initializing WikiBase", e);
 		}
 	}
 
 	/**
 	 * Creates an instance of <code>WikiBase</code> with a specified persistency sub-system.
 	 *
 	 * @throws Exception If the handler cannot be instantiated.
 	 */
 	private WikiBase() throws Exception {
 		String type = Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE);
 		if (type != null && type.equals("DATABASE")) {
 			WikiBase.handler = new DatabaseHandler();
 		} else {
 			WikiBase.handler = new FileHandler();
 		}
 		this.searchEngine = new LuceneSearchEngine();
 	}
 
 	/**
 	 * Utility method for determining if a topic exists.  This method will
 	 * return true if a method is a special topic (such as the recent changes
 	 * page) or if it is an existing topic.
 	 *
 	 * @param virtualWiki The virtual wiki for the topic being checked.
 	 * @param topicName The name of the topic that is being checked.
 	 * @return Returns true if the topic exists or is a special system topic.
 	 * @throws Exception Thrown if any error occurs during lookup.
 	 */
 	public static boolean exists(String virtualWiki, String topicName) throws Exception {
 		if (PseudoTopicHandler.isPseudoTopic(topicName)) {
 			return true;
 		}
 		if (InterWikiHandler.isInterWiki(topicName)) {
 			return true;
 		}
 		if (!StringUtils.hasText(Environment.getValue(Environment.PROP_BASE_FILE_DIR)) || !Environment.getBooleanValue(Environment.PROP_BASE_INITIALIZED)) {
 			// not initialized yet
 			return false;
 		}
 		return WikiBase.handler.exists(virtualWiki, topicName);
 	}
 
 	/**
 	 * Get an instance of the current persistency handler.
 	 *
 	 * @return The current handler instance.
 	 */
 	public static PersistencyHandler getHandler() {
 		if (!WikiBase.handler.isInitialized()) {
 			// not initialized yet
 			return null;
 		}
 		return WikiBase.handler;
 	}
 
 	/**
 	 * Return an instance of the current persistency type (usually file or database).
 	 *
 	 * @return The current persistency type.
 	 */
 	public static int getPersistenceType() {
 		if (Environment.getValue(Environment.PROP_BASE_PERSISTENCE_TYPE).equals("DATABASE")) {
 			return WikiBase.DATABASE;
 		} else {
 			return WikiBase.FILE;
 		}
 	}
 
 	/**
 	 * Get an instance of the current persistency handler.
 	 *
 	 * @return The current handler instance.
 	 */
 	public static SearchEngine getSearchEngine() {
 		return WikiBase.searchEngine;
 	}
 
 	/**
 	 * Return an instance of the current user group type (usually LDAP or database).
 	 *
 	 * @return The current user group type.
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
 	 * Reset the WikiBase object, re-initializing persistency type and
 	 * other values.
 	 *
 	 * @param locale The locale to be used if any system pages need to be set up
 	 *  as a part of the initialization process.
 	 * @param user A sysadmin user to be used in case any system pages need to
 	 *  be created as a part of the initialization process.
 	 * @throws Exception Thrown if an error occurs during re-initialization.
 	 */
 	public static void reset(Locale locale, WikiUser user) throws Exception {
 		WikiMail.init();
 		WikiBase.instance = new WikiBase();
 		if (!WikiBase.handler.isInitialized()) {
 			WikiBase.handler.initialize(locale, user);
 		}
 		JAMWikiServlet.removeCachedContents();
 		WikiBase.searchEngine.refreshIndex();
 	}
 
 	/**
 	 * Set the current persistency type.  This method is not meant for general
 	 * use - WikiBase.reset() should be used in most cases when changing persistency
 	 * type.
 	 */
 	public static void setHandler(PersistencyHandler handler) {
 		WikiBase.handler = handler;
 	}
 }

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
 import org.jamwiki.authentication.JAMWikiAnonymousProcessingFilter;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.search.SearchEngine;
 import org.jamwiki.utils.InterWikiHandler;
 import org.jamwiki.utils.PseudoTopicHandler;
 import org.jamwiki.utils.WikiUtil;
 import org.jamwiki.utils.WikiCache;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.util.StringUtils;
 
 /**
  * <code>WikiBase</code> is loaded as a singleton class and provides access
  * to all core wiki structures.  In addition this class provides utility methods
  * for resetting core structures including caches and permissions.
  *
  * @see org.jamwiki.DataHandler
  * @see org.jamwiki.UserHandler
  * @see org.jamwiki.search.SearchEngine
  */
 public class WikiBase {
 
 	/** Standard logger. */
 	private static final WikiLogger logger = WikiLogger.getLogger(WikiBase.class.getName());
 	/** The singleton instance of this class. */
 	private static WikiBase instance = null;
 	/** The data handler that looks after read/write operations. */
 	private static DataHandler dataHandler = null;
 	/** The handler for user login/authentication. */
 	private static UserHandler userHandler = null;
 	/** The search engine instance. */
 	private static SearchEngine searchEngine = null;
 
 	/** Cache name for the cache of parsed topic content. */
 	public static final String CACHE_PARSED_TOPIC_CONTENT = "org.jamwiki.WikiBase.CACHE_PARSED_TOPIC_CONTENT";
 	/** Ansi data handler class */
 	public static final String DATA_HANDLER_ANSI = "org.jamwiki.db.AnsiDataHandler";
 	/** DB2 data handler class */
 	public static final String DATA_HANDLER_DB2 = "org.jamwiki.db.DB2DataHandler";
 	/** DB2/400 data handler class */
 	public static final String DATA_HANDLER_DB2400 = "org.jamwiki.db.DB2400DataHandler";
 	/** HSql data handler class */
 	public static final String DATA_HANDLER_HSQL = "org.jamwiki.db.HSqlDataHandler";
 	/** MSSql data handler class */
 	public static final String DATA_HANDLER_MSSQL = "org.jamwiki.db.MSSqlDataHandler";
 	/** MySql data handler class */
 	public static final String DATA_HANDLER_MYSQL = "org.jamwiki.db.MySqlDataHandler";
 	/** Oracle data handler class */
 	public static final String DATA_HANDLER_ORACLE = "org.jamwiki.db.OracleDataHandler";
 	/** Postgres data handler class */
 	public static final String DATA_HANDLER_POSTGRES = "org.jamwiki.db.PostgresDataHandler";
 	/** Name of the default wiki */
 	// FIXME - make this configurable
 	public static final String DEFAULT_VWIKI = "en";
 	/** Data stored using an external database */
 	public static final String PERSISTENCE_EXTERNAL = "DATABASE";
 	/** Data stored using an internal copy of the HSQL database */
 	public static final String PERSISTENCE_INTERNAL = "INTERNAL";
 	/** Lucene search engine class */
 	public static final String SEARCH_ENGINE_LUCENE = "org.jamwiki.search.LuceneSearchEngine";
 	/** Root directory within the WAR distribution that contains the default topic pages. */
 	public static final String SPECIAL_PAGE_DIR = "pages";
 	/** Name of the default starting points topic. */
 	public static final String SPECIAL_PAGE_STARTING_POINTS = "StartingPoints";
 	/** Name of the default left menu topic. */
 	public static final String SPECIAL_PAGE_LEFT_MENU = "LeftMenu";
 	/** Name of the default footer topic. */
 	public static final String SPECIAL_PAGE_BOTTOM_AREA = "BottomArea";
 	/** Name of the default jamwiki.css topic. */
 	public static final String SPECIAL_PAGE_SPECIAL_PAGES = "SpecialPages";
 	/** Name of the default jamwiki.css topic. */
 	public static final String SPECIAL_PAGE_STYLESHEET = "StyleSheet";
 	/** Allow file uploads of any file type. */
 	public static final int UPLOAD_ALL = 0;
 	/** Use a blacklist to determine what file types can be uploaded. */
 	public static final int UPLOAD_BLACKLIST = 2;
 	/** Disable all file uploads. */
 	public static final int UPLOAD_NONE = 1;
 	/** Use a whitelist to determine what file types can be uploaded. */
 	public static final int UPLOAD_WHITELIST = 3;
 	/** Database user handler class */
 	public static final String USER_HANDLER_DATABASE = "org.jamwiki.db.DatabaseUserHandler";
 	/** LDAP user handler class */
 	public static final String USER_HANDLER_LDAP = "org.jamwiki.ldap.LdapUserHandler";
 
 	static {
 		try {
 			WikiBase.instance = new WikiBase();
 		} catch (Exception e) {
 			logger.severe("Failure while initializing WikiBase", e);
 		}
 	}
 
 	/**
 	 * Creates an instance of <code>WikiBase</code>, initializing the default
 	 * data handler instance and search engine instance.
 	 *
 	 * @throws Exception If the instance cannot be instantiated.
 	 */
 	private WikiBase() throws Exception {
		WikiBase.dataHandler = WikiUtil.dataHandlerInstance();
		WikiBase.userHandler = WikiUtil.userHandlerInstance();
		WikiBase.searchEngine = WikiUtil.searchEngineInstance();
 	}
 
 	/**
 	 * Utility method for determining if a topic exists.  This method will
 	 * return true if a method is a special topic (such as the recent changes
 	 * page) or if it is an existing topic.
 	 *
 	 * @param virtualWiki The virtual wiki for the topic being checked.
 	 * @param topicName The name of the topic that is being checked.
 	 * @return <code>true</code> if the topic exists or is a special system topic.
 	 * @throws Exception Thrown if any error occurs during lookup.
 	 */
 	// FIXME - this method isn't really appropriate for this class and should be moved.
 	public static boolean exists(String virtualWiki, String topicName) throws Exception {
 		if (!StringUtils.hasText(virtualWiki) || !StringUtils.hasText(topicName)) {
 			return false;
 		}
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
 		Topic topic = WikiBase.dataHandler.lookupTopic(virtualWiki, topicName, false, null);
 		return (topic != null);
 	}
 
 	/**
 	 * Get an instance of the current data handler.
 	 *
 	 * @return The current data handler instance, or <code>null</code>
 	 *  if the handler has not yet been initialized.
 	 */
 	public static DataHandler getDataHandler() {
 		return WikiBase.dataHandler;
 	}
 
 	/**
 	 * Get an instance of the current search engine.
 	 *
 	 * @return The current search engine instance.
 	 */
 	public static SearchEngine getSearchEngine() {
 		return WikiBase.searchEngine;
 	}
 
 	/**
 	 *
 	 */
 	public static UserHandler getUserHandler() {
 		return WikiBase.userHandler;
 	}
 
 	/**
 	 * Reset the WikiBase object, re-initializing the data handler and
 	 * other values.
 	 *
 	 * @param locale The locale to be used if any system pages need to be set up
 	 *  as a part of the initialization process.
 	 * @param user A sysadmin user to be used in case any system pages need to
 	 *  be created as a part of the initialization process.
 	 * @throws Exception Thrown if an error occurs during re-initialization.
 	 */
 	public static void reset(Locale locale, WikiUser user) throws Exception {
 		WikiBase.instance = new WikiBase();
 		WikiCache.initialize();
 		WikiBase.dataHandler.setup(locale, user);
 		WikiUser.resetDefaultGroupRoles();
 		JAMWikiAnonymousProcessingFilter.reset();
 	}
 }

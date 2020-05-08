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
 package org.jamwiki.db;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.TreeMap;
 import java.util.Vector;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.utils.Pagination;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.model.Category;
 import org.jamwiki.model.RecentChange;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.WikiFile;
 import org.jamwiki.model.WikiFileVersion;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.file.FileHandler;
 import org.jamwiki.utils.DiffUtil;
 import org.jamwiki.utils.Encryption;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiCacheMap;
 import org.springframework.util.StringUtils;
 
 /**
  *
  */
 public class DatabaseHandler {
 
 	// FIXME - possibly make this a property, or configurable based on number of topics in the system
 	private static int MAX_CACHED_LIST_SIZE = 2000;
 	/** For performance reasons, keep a (small) list of recently looked-up topics around in memory. */
 	private static WikiCacheMap cachedTopicsList = new WikiCacheMap(MAX_CACHED_LIST_SIZE);
 	/** For performance reasons, keep a (small) list of recently looked-up non-topics around in memory. */
 	private static WikiCacheMap cachedNonTopicsList = new WikiCacheMap(MAX_CACHED_LIST_SIZE);
 	/** For performance reasons, keep a (small) list of recently looked-up user logins and ids around in memory. */
 	private static WikiCacheMap cachedUserLoginHash = new WikiCacheMap(MAX_CACHED_LIST_SIZE);
 	private static Hashtable virtualWikiIdHash = null;
 	private static Hashtable virtualWikiNameHash = null;
 	public static final String DB_TYPE_ANSI = "ansi";
 	public static final String DB_TYPE_DB2 = "db2";
 	public static final String DB_TYPE_DB2_400 = "db2/400";
 	public static final String DB_TYPE_HSQL = "hsql";
 	public static final String DB_TYPE_MSSQL = "mssql";
 	public static final String DB_TYPE_MYSQL = "mysql";
 	public static final String DB_TYPE_ORACLE = "oracle";
 	public static final String DB_TYPE_POSTGRES = "postgres";
 	private static String CONNECTION_VALIDATION_QUERY = null;
 	private static final WikiLogger logger = WikiLogger.getLogger(DatabaseHandler.class.getName());
 	private static QueryHandler queryHandler = null;
 	private boolean initialized = false;
 
 	/**
 	 *
 	 */
 	public DatabaseHandler() {
 		if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_DB2)) {
 			DatabaseHandler.queryHandler = new DB2QueryHandler();
 		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_DB2_400)) {
 			DatabaseHandler.queryHandler = new DB2400QueryHandler();
 		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_MSSQL)) {
 			DatabaseHandler.queryHandler = new MSSqlQueryHandler();
 		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_HSQL)) {
 			DatabaseHandler.queryHandler = new HSQLQueryHandler();
 		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_MYSQL)) {
 			DatabaseHandler.queryHandler = new MySqlQueryHandler();
 		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_ORACLE)) {
 			DatabaseHandler.queryHandler = new OracleQueryHandler();
 		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_POSTGRES)) {
 			DatabaseHandler.queryHandler = new PostgresQueryHandler();
 		} else {
 			DatabaseHandler.queryHandler = new DefaultQueryHandler();
 		}
 		DatabaseHandler.CONNECTION_VALIDATION_QUERY = DatabaseHandler.queryHandler.connectionValidationQuery();
 		// initialize connection pool in its own try-catch to avoid an error
 		// causing property values not to be saved.
 		DatabaseConnection.setPoolInitialized(false);
 	}
 
 	/**
 	 *
 	 */
 	private void addCategory(Category category, Connection conn) throws Exception {
 		Topic childTopic = lookupTopic(category.getVirtualWiki(), category.getChildTopicName(), true, false, conn);
 		int childTopicId = childTopic.getTopicId();
 		DatabaseHandler.queryHandler.insertCategory(childTopicId, category.getName(), category.getSortKey(), conn);
 	}
 
 	/**
 	 *
 	 */
 	private void addRecentChange(RecentChange change, Connection conn) throws Exception {
 		int virtualWikiId = this.lookupVirtualWikiId(change.getVirtualWiki());
 		DatabaseHandler.queryHandler.insertRecentChange(change, virtualWikiId, conn);
 	}
 
 	/**
 	 *
 	 */
 	private void addTopic(Topic topic, Connection conn) throws Exception {
 		int virtualWikiId = this.lookupVirtualWikiId(topic.getVirtualWiki());
 		if (topic.getTopicId() < 1) {
 			int topicId = DatabaseHandler.queryHandler.nextTopicId(conn);
 			topic.setTopicId(topicId);
 		}
 		DatabaseHandler.queryHandler.insertTopic(topic, virtualWikiId, conn);
 	}
 
 	/**
 	 *
 	 */
 	private void addTopicVersion(String topicName, TopicVersion topicVersion, Connection conn) throws Exception {
 		if (topicVersion.getTopicVersionId() < 1) {
 			int topicVersionId = DatabaseHandler.queryHandler.nextTopicVersionId(conn);
 			topicVersion.setTopicVersionId(topicVersionId);
 		}
 		if (topicVersion.getEditDate() == null) {
 			Timestamp editDate = new Timestamp(System.currentTimeMillis());
 			topicVersion.setEditDate(editDate);
 		}
 		DatabaseHandler.queryHandler.insertTopicVersion(topicVersion, conn);
 	}
 
 	/**
 	 *
 	 */
 	private void addVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws Exception {
 		if (virtualWiki.getVirtualWikiId() < 1) {
 			int virtualWikiId = DatabaseHandler.queryHandler.nextVirtualWikiId(conn);
 			virtualWiki.setVirtualWikiId(virtualWikiId);
 		}
 		DatabaseHandler.queryHandler.insertVirtualWiki(virtualWiki, conn);
 	}
 
 	/**
 	 *
 	 */
 	private void addWikiFile(String topicName, WikiFile wikiFile, Connection conn) throws Exception {
 		if (wikiFile.getFileId() < 1) {
 			int fileId = DatabaseHandler.queryHandler.nextWikiFileId(conn);
 			wikiFile.setFileId(fileId);
 		}
 		int virtualWikiId = this.lookupVirtualWikiId(wikiFile.getVirtualWiki());
 		DatabaseHandler.queryHandler.insertWikiFile(wikiFile, virtualWikiId, conn);
 	}
 
 	/**
 	 *
 	 */
 	private void addWikiFileVersion(String topicName, WikiFileVersion wikiFileVersion, Connection conn) throws Exception {
 		if (wikiFileVersion.getFileVersionId() < 1) {
 			int fileVersionId = DatabaseHandler.queryHandler.nextWikiFileVersionId(conn);
 			wikiFileVersion.setFileVersionId(fileVersionId);
 		}
 		if (wikiFileVersion.getUploadDate() == null) {
 			Timestamp uploadDate = new Timestamp(System.currentTimeMillis());
 			wikiFileVersion.setUploadDate(uploadDate);
 		}
 		DatabaseHandler.queryHandler.insertWikiFileVersion(wikiFileVersion, conn);
 	}
 
 	/**
 	 *
 	 */
 	private void addWikiUser(WikiUser user, Connection conn) throws Exception {
 		if (user.getUserId() < 1) {
 			int nextUserId = DatabaseHandler.queryHandler.nextWikiUserId(conn);
 			user.setUserId(nextUserId);
 		}
 		DatabaseHandler.queryHandler.insertWikiUser(user, conn);
 		// FIXME - may be in LDAP
 		DatabaseHandler.queryHandler.insertWikiUserInfo(user, conn);
 	}
 
 	/**
 	 *
 	 */
 	public boolean canMoveTopic(Topic fromTopic, String destination) throws Exception {
 		Topic toTopic = WikiBase.getHandler().lookupTopic(fromTopic.getVirtualWiki(), destination, true);
 		if (toTopic == null || toTopic.getDeleteDate() != null) {
 			// destination doesn't exist or is deleted, so move is OK
 			return true;
 		}
 		if (toTopic.getRedirectTo() != null && toTopic.getRedirectTo().equals(fromTopic.getName())) {
 			// source redirects to destination, so move is OK
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * @deprecated This method exists solely to allow upgrades to JAMWiki 0.4.0 or
 	 *  greater and will be replaced during the JAMWiki 0.5.x or JAMWiki 0.6.x series.
 	 */
 	public static Vector convertFromFile(WikiUser user, Locale locale, FileHandler fromHandler, DatabaseHandler toHandler) throws Exception {
 		Connection conn = null;
 		try {
 			toHandler.initialize(locale, user);
 			DatabaseHandler.virtualWikiNameHash = new Hashtable();
 			DatabaseHandler.virtualWikiIdHash = new Hashtable();
 			conn = toHandler.getConnection();
 			// FIXME - hard coding of messages
 			Vector messages = new Vector();
 			// purge EVERYTHING from the destination handler
 			toHandler.purgeData(conn);
 			// users
 			Collection userNames = fromHandler.getAllWikiUserLogins();
 			int success = 0;
 			int failed = 0;
 			for (Iterator userIterator = userNames.iterator(); userIterator.hasNext();) {
 				String userName = (String)userIterator.next();
 				try {
 					WikiUser wikiUser = fromHandler.lookupWikiUser(userName);
 					toHandler.addWikiUser(wikiUser, conn);
 					success++;
 				} catch (Exception e) {
 					String msg = "Unable to convert user: " + userName;
 					logger.severe(msg, e);
 					messages.add(msg + ": " + e.getMessage());
 					failed++;
 				}
 			}
 			messages.add("Converted " + success + " users successfully, " + failed + " conversions failed");
 			success = 0;
 			failed = 0;
 			Collection virtualWikis = fromHandler.getVirtualWikiList();
 			for (Iterator virtualWikiIterator = virtualWikis.iterator(); virtualWikiIterator.hasNext();) {
 				VirtualWiki virtualWiki = (VirtualWiki)virtualWikiIterator.next();
 				try {
 					toHandler.addVirtualWiki(virtualWiki, conn);
 					messages.add("Added virtual wiki " + virtualWiki.getName());
 				} catch (Exception e) {
 					String msg = "Unable to convert virtual wiki " + virtualWiki.getName();
 					logger.severe(msg, e);
 					messages.add(msg + ": " + e.getMessage());
 				}
 				DatabaseHandler.virtualWikiNameHash.put(virtualWiki.getName(), virtualWiki);
 				DatabaseHandler.virtualWikiIdHash.put(new Integer(virtualWiki.getVirtualWikiId()), virtualWiki);
 				success = 0;
 				failed = 0;
 				// topics
 				Collection topicNames = fromHandler.getAllTopicNames(virtualWiki.getName());
 				for (Iterator topicIterator = topicNames.iterator(); topicIterator.hasNext();) {
 					String topicName = (String)topicIterator.next();
 					try {
 						Topic topic = fromHandler.lookupTopic(virtualWiki.getName(), topicName, true);
 						toHandler.addTopic(topic, conn);
 						success++;
 					} catch (Exception e) {
 						String msg = "Unable to convert topic: " + virtualWiki.getName() + " / " + topicName;
 						logger.severe(msg, e);
 						messages.add(msg + ": " + e.getMessage());
 						failed++;
 					}
 				}
 				messages.add("Converted " + success + " topics in virtual wiki " + virtualWiki.getName() + " successfully, " + failed + " conversions failed");
 				success = 0;
 				failed = 0;
 				// topic versions - must be added numerically due to previousTopicVersionId constraint
 				TreeMap versionsMap = new TreeMap();
 				Hashtable topicNameMap = new Hashtable();
 				for (Iterator topicIterator = topicNames.iterator(); topicIterator.hasNext();) {
 					String topicName = (String)topicIterator.next();
 					Collection versions = fromHandler.getAllTopicVersions(virtualWiki.getName(), topicName, false);
 					for (Iterator topicVersionIterator = versions.iterator(); topicVersionIterator.hasNext();) {
 						TopicVersion topicVersion = (TopicVersion)topicVersionIterator.next();
 						Integer key = new Integer(topicVersion.getTopicVersionId());
 						topicNameMap.put(key, topicName);
 						versionsMap.put(key, topicVersion);
 					}
 				}
 				for (Iterator topicVersionIterator = versionsMap.keySet().iterator(); topicVersionIterator.hasNext();) {
 					Integer key = (Integer)topicVersionIterator.next();
 					TopicVersion topicVersion = (TopicVersion)versionsMap.get(key);
 					String topicName = (String)topicNameMap.get(key);
 					try {
 						toHandler.addTopicVersion(topicName, topicVersion, conn);
 						success++;
 					} catch (Exception e) {
 						String msg = "Unable to convert topic version: " + virtualWiki.getName() + " / " + topicName + " / " + topicVersion.getTopicVersionId();
 						logger.severe(msg, e);
 						messages.add(msg + ": " + e.getMessage());
 						failed++;
 					}
 				}
 				messages.add("Converted " + success + " topic versions in virtual wiki " + virtualWiki.getName() + " successfully, " + failed + " conversions failed");
 				success = 0;
 				failed = 0;
 				// wiki files
 				Collection wikiFileNames = fromHandler.getAllWikiFileTopicNames(virtualWiki.getName());
 				for (Iterator wikiFileIterator = wikiFileNames.iterator(); wikiFileIterator.hasNext();) {
 					String topicName = (String)wikiFileIterator.next();
 					try {
 						WikiFile wikiFile = fromHandler.lookupWikiFile(virtualWiki.getName(), topicName);
 						toHandler.addWikiFile(topicName, wikiFile, conn);
 						success++;
 					} catch (Exception e) {
 						String msg = "Unable to convert wiki file: " + virtualWiki.getName() + " / " + topicName;
 						logger.severe(msg, e);
 						messages.add(msg + ": " + e.getMessage());
 						failed++;
 					}
 				}
 				messages.add("Converted " + success + " wiki files in virtual wiki " + virtualWiki.getName() + " successfully, " + failed + " conversions failed");
 				success = 0;
 				failed = 0;
 				// wiki file versions
 				for (Iterator topicIterator = wikiFileNames.iterator(); topicIterator.hasNext();) {
 					String topicName = (String)topicIterator.next();
 					Collection versions = fromHandler.getAllWikiFileVersions(virtualWiki.getName(), topicName, false);
 					for (Iterator wikiFileVersionIterator = versions.iterator(); wikiFileVersionIterator.hasNext();) {
 						WikiFileVersion wikiFileVersion = (WikiFileVersion)wikiFileVersionIterator.next();
 						try {
 							toHandler.addWikiFileVersion(topicName, wikiFileVersion, conn);
 							success++;
 						} catch (Exception e) {
 							String msg = "Unable to convert wiki file version: " + virtualWiki.getName() + " / " + topicName;
 							logger.severe(msg, e);
 							messages.add(msg + ": " + e.getMessage());
 							failed++;
 						}
 					}
 				}
 				messages.add("Converted " + success + " wiki file versions in virtual wiki " + virtualWiki.getName() + " successfully, " + failed + " conversions failed");
 				toHandler.reloadRecentChanges();
 			}
 			// FIXME - since search index info is in the same directory it gets deleted
 			WikiBase.getSearchEngine().refreshIndex();
 			return messages;
 		} catch (Exception e) {
 			toHandler.handleErrors(conn);
 			throw e;
 		} finally {
 			toHandler.releaseParams(conn);
 			WikiBase.reset(locale, user);
 		}
 	}
 
 	/**
 	 * @deprecated This method exists solely to allow upgrades to JAMWiki 0.4.0 or
 	 *  greater and will be replaced during the JAMWiki 0.5.x or JAMWiki 0.6.x series.
 	 */
 	public static Vector convertToFile(WikiUser user, Locale locale, DatabaseHandler fromHandler, FileHandler toHandler) throws Exception {
 		try {
 			fromHandler.initialize(locale, user);
 			// FIXME - hard coding of messages
 			Vector messages = new Vector();
 			// purge EVERYTHING from the destination handler
 			toHandler.purgeData();
 			// users
 			Collection userNames = fromHandler.getAllWikiUserLogins();
 			int success = 0;
 			int failed = 0;
 			for (Iterator userIterator = userNames.iterator(); userIterator.hasNext();) {
 				String userName = (String)userIterator.next();
 				try {
 					WikiUser wikiUser = fromHandler.lookupWikiUser(userName);
 					toHandler.addWikiUser(wikiUser);
 					success++;
 				} catch (Exception e) {
 					String msg = "Unable to convert user: " + userName;
 					logger.severe(msg, e);
 					messages.add(msg + ": " + e.getMessage());
 					failed++;
 				}
 			}
 			messages.add("Converted " + success + " users successfully, " + failed + " conversions failed");
 			success = 0;
 			failed = 0;
 			Collection virtualWikis = fromHandler.getVirtualWikiList();
 			for (Iterator virtualWikiIterator = virtualWikis.iterator(); virtualWikiIterator.hasNext();) {
 				VirtualWiki virtualWiki = (VirtualWiki)virtualWikiIterator.next();
 				try {
 					toHandler.addVirtualWiki(virtualWiki);
 					messages.add("Added virtual wiki " + virtualWiki.getName());
 				} catch (Exception e) {
 					String msg = "Unable to convert virtual wiki " + virtualWiki.getName();
 					logger.severe(msg, e);
 					messages.add(msg + ": " + e.getMessage());
 				}
 				success = 0;
 				failed = 0;
 				// topics
 				Collection topicNames = fromHandler.getAllTopicNames(virtualWiki.getName());
 				for (Iterator topicIterator = topicNames.iterator(); topicIterator.hasNext();) {
 					String topicName = (String)topicIterator.next();
 					try {
 						Topic topic = fromHandler.lookupTopic(virtualWiki.getName(), topicName, true);
 						toHandler.addTopic(topic);
 						success++;
 					} catch (Exception e) {
 						String msg = "Unable to convert topic: " + virtualWiki.getName() + " / " + topicName;
 						logger.severe(msg, e);
 						messages.add(msg + ": " + e.getMessage());
 						failed++;
 					}
 				}
 				messages.add("Converted " + success + " topics in virtual wiki " + virtualWiki.getName() + " successfully, " + failed + " conversions failed");
 				success = 0;
 				failed = 0;
 				// topic versions
 				for (Iterator topicIterator = topicNames.iterator(); topicIterator.hasNext();) {
 					String topicName = (String)topicIterator.next();
 					Collection versions = fromHandler.getAllTopicVersions(virtualWiki.getName(), topicName, false);
 					for (Iterator topicVersionIterator = versions.iterator(); topicVersionIterator.hasNext();) {
 						TopicVersion topicVersion = (TopicVersion)topicVersionIterator.next();
 						try {
 							toHandler.addTopicVersion(virtualWiki.getName(), topicName, topicVersion);
 							success++;
 						} catch (Exception e) {
 							String msg = "Unable to convert topic version: " + virtualWiki.getName() + " / " + topicName + " / " + topicVersion.getTopicVersionId();
 							logger.severe(msg, e);
 							messages.add(msg + ": " + e.getMessage());
 							failed++;
 						}
 					}
 				}
 				messages.add("Converted " + success + " topic versions in virtual wiki " + virtualWiki.getName() + " successfully, " + failed + " conversions failed");
 				success = 0;
 				failed = 0;
 				// wiki files
 				Collection wikiFileNames = fromHandler.getAllWikiFileTopicNames(virtualWiki.getName());
 				for (Iterator wikiFileIterator = wikiFileNames.iterator(); wikiFileIterator.hasNext();) {
 					String topicName = (String)wikiFileIterator.next();
 					try {
 						WikiFile wikiFile = fromHandler.lookupWikiFile(virtualWiki.getName(), topicName);
 						toHandler.addWikiFile(topicName, wikiFile);
 						success++;
 					} catch (Exception e) {
 						String msg = "Unable to convert wiki file: " + virtualWiki.getName() + " / " + topicName;
 						logger.severe(msg, e);
 						messages.add(msg + ": " + e.getMessage());
 						failed++;
 					}
 				}
 				messages.add("Converted " + success + " wiki files in virtual wiki " + virtualWiki.getName() + " successfully, " + failed + " conversions failed");
 				success = 0;
 				failed = 0;
 				// wiki file versions
 				for (Iterator topicIterator = wikiFileNames.iterator(); topicIterator.hasNext();) {
 					String topicName = (String)topicIterator.next();
 					Collection versions = fromHandler.getAllWikiFileVersions(virtualWiki.getName(), topicName, false);
 					for (Iterator wikiFileVersionIterator = versions.iterator(); wikiFileVersionIterator.hasNext();) {
 						WikiFileVersion wikiFileVersion = (WikiFileVersion)wikiFileVersionIterator.next();
 						try {
 							toHandler.addWikiFileVersion(virtualWiki.getName(), topicName, wikiFileVersion);
 							success++;
 						} catch (Exception e) {
 							String msg = "Unable to convert wiki file version: " + virtualWiki.getName() + " / " + topicName;
 							logger.severe(msg, e);
 							messages.add(msg + ": " + e.getMessage());
 							failed++;
 						}
 					}
 				}
 				messages.add("Converted " + success + " wiki file versions in virtual wiki " + virtualWiki.getName() + " successfully, " + failed + " conversions failed");
 			}
 			// FIXME - since search index info is in the same directory it gets deleted
 			WikiBase.getSearchEngine().refreshIndex();
 			return messages;
 		} finally {
 			WikiBase.reset(locale, user);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void deleteRecentChanges(Topic topic, Connection conn) throws Exception {
 		DatabaseHandler.queryHandler.deleteRecentChanges(topic.getTopicId(), conn);
 	}
 
 	/**
 	 *
 	 */
 	public void deleteTopic(Topic topic, TopicVersion topicVersion, boolean userVisible) throws Exception {
 		Connection conn = null;
 		try {
 			conn = this.getConnection();
 			this.deleteTopic(topic, topicVersion, userVisible, conn);
 		} catch (Exception e) {
 			this.handleErrors(conn);
 			throw e;
 		} finally {
 			this.releaseParams(conn);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void deleteTopic(Topic topic, TopicVersion topicVersion, boolean userVisible, Connection conn) throws Exception {
 		if (userVisible) {
 			// delete old recent changes
 			deleteRecentChanges(topic, conn);
 		}
 		// update topic to indicate deleted, add delete topic version.  parser output
 		// should be empty since nothing to add to search engine.
 		ParserOutput parserOutput = new ParserOutput();
 		topic.setDeleteDate(new Timestamp(System.currentTimeMillis()));
 		writeTopic(topic, topicVersion, parserOutput, conn, userVisible);
 		// reset topic existence vector
 		cachedTopicsList = new WikiCacheMap(MAX_CACHED_LIST_SIZE);
 	}
 
 	/**
 	 *
 	 */
 	private void deleteTopicCategories(Topic topic, Connection conn) throws Exception {
 		DatabaseHandler.queryHandler.deleteTopicCategories(topic.getTopicId(), conn);
 	}
 
 	/**
 	 *
 	 */
 	public Vector diff(String topicName, int topicVersionId1, int topicVersionId2) throws Exception {
 		TopicVersion version1 = lookupTopicVersion(topicName, topicVersionId1);
 		TopicVersion version2 = lookupTopicVersion(topicName, topicVersionId2);
 		if (version1 == null && version2 == null) {
 			String msg = "Versions " + topicVersionId1 + " and " + topicVersionId2 + " not found for " + topicName;
 			logger.severe(msg);
 			throw new Exception(msg);
 		}
 		String contents1 = null;
 		if (version1 != null) {
 			contents1 = version1.getVersionContent();
 		}
 		String contents2 = null;
 		if (version2 != null) {
 			contents2 = version2.getVersionContent();
 		}
 		if (contents1 == null && contents2 == null) {
 			String msg = "No versions found for " + topicVersionId1 + " against " + topicVersionId2;
 			logger.severe(msg);
 			throw new Exception(msg);
 		}
 		return DiffUtil.diff(contents1, contents2);
 	}
 
 	/**
 	 * See if a topic exists and if it has not been deleted.
 	 *
 	 * @param virtualWiki The virtual wiki for the topic being checked.
 	 * @param topicName The name of the topic that is being checked.
 	 * @param caseSensitive Set to <code>true</code> if the topic name should be
 	 *  searched for in a case-sensitive manner.
 	 * @return <code>true</code> if the topic exists.
 	 * @throws Exception Thrown if any error occurs during lookup.
 	 */
 	public boolean exists(String virtualWiki, String topicName, boolean caseSensitive) throws Exception {
 		if (!StringUtils.hasText(virtualWiki) || !StringUtils.hasText(topicName)) {
 			return false;
 		}
 		// first check a cache of recently looked-up topics for performance reasons
 		String key = virtualWiki + "/" + topicName;
 		if (cachedTopicsList.containsKey(key)) {
 			return true;
 		}
 		if (cachedNonTopicsList.containsKey(key)) {
 			return false;
 		}
 		Topic topic = lookupTopic(virtualWiki, topicName, caseSensitive);
 		if (topic == null || topic.getDeleteDate() != null) {
 			cachedNonTopicsList.put(key, null);
 			return false;
 		}
 		cachedTopicsList.put(key, null);
 		return true;
 	}
 
 	/**
 	 *
 	 */
 	public Collection getAllCategories(String virtualWiki, Pagination pagination) throws Exception {
 		Collection results = new Vector();
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		WikiResultSet rs = DatabaseHandler.queryHandler.getCategories(virtualWikiId, pagination);
 		while (rs.next()) {
 			Category category = new Category();
 			category.setName(rs.getString("category_name"));
 			// FIXME - child topic name not initialized
 			category.setVirtualWiki(virtualWiki);
 			category.setSortKey(rs.getString("sort_key"));
 			results.add(category);
 		}
 		return results;
 	}
 
 	/**
 	 *
 	 */
 	public Collection getAllTopicNames(String virtualWiki) throws Exception {
 		Vector all = new Vector();
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		WikiResultSet rs = DatabaseHandler.queryHandler.getAllTopicNames(virtualWikiId);
 		while (rs.next()) {
 			all.add(rs.getString("topic_name"));
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	private Collection getAllTopicVersions(String virtualWiki, String topicName, boolean descending) throws Exception {
 		Vector all = new Vector();
 		Topic topic = lookupTopic(virtualWiki, topicName, true);
 		if (topic == null) {
 			throw new Exception("No topic exists for " + virtualWiki + " / " + topicName);
 		}
 		WikiResultSet rs = DatabaseHandler.queryHandler.getAllTopicVersions(topic, descending);
 		while (rs.next()) {
 			all.add(initTopicVersion(rs));
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	private Collection getAllWikiFileTopicNames(String virtualWiki) throws Exception {
 		Vector all = new Vector();
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		WikiResultSet rs = DatabaseHandler.queryHandler.getAllWikiFileTopicNames(virtualWikiId);
 		while (rs.next()) {
 			all.add(rs.getString("topic_name"));
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	public Collection getAllWikiFileVersions(String virtualWiki, String topicName, boolean descending) throws Exception {
 		Vector all = new Vector();
 		WikiFile wikiFile = lookupWikiFile(virtualWiki, topicName);
 		if (wikiFile == null) {
 			throw new Exception("No topic exists for " + virtualWiki + " / " + topicName);
 		}
 		WikiResultSet rs = DatabaseHandler.queryHandler.getAllWikiFileVersions(wikiFile, descending);
 		while (rs.next()) {
 			all.add(initWikiFileVersion(rs));
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	public Collection getAllWikiUserLogins() throws Exception {
 		Vector all = new Vector();
 		WikiResultSet rs = DatabaseHandler.queryHandler.getAllWikiUserLogins();
 		while (rs.next()) {
 			all.add(rs.getString("login"));
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	private Connection getConnection() throws Exception {
 		// add a connection to the conn array.  BE SURE TO RELEASE IT!
 		Connection conn = DatabaseConnection.getConnection();
 		conn.setAutoCommit(false);
 		return conn;
 	}
 
 	/**
 	 *
 	 */
 	public Collection getRecentChanges(String virtualWiki, Pagination pagination, boolean descending) throws Exception {
 		Vector all = new Vector();
 		WikiResultSet rs = DatabaseHandler.queryHandler.getRecentChanges(virtualWiki, pagination, descending);
 		while (rs.next()) {
 			RecentChange change = initRecentChange(rs);
 			all.add(change);
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	public Collection getRecentChanges(String virtualWiki, String topicName, Pagination pagination, boolean descending) throws Exception {
 		Vector all = new Vector();
 		Topic topic = lookupTopic(virtualWiki, topicName, true, true);
 		if (topic == null) return all;
 		WikiResultSet rs = DatabaseHandler.queryHandler.getRecentChanges(topic.getTopicId(), pagination, descending);
 		while (rs.next()) {
 			RecentChange change = initRecentChange(rs);
 			all.add(change);
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	public Collection getUserContributions(String virtualWiki, String userString, Pagination pagination, boolean descending) throws Exception {
 		Collection all = new Vector();
 		WikiResultSet rs = DatabaseHandler.queryHandler.getUserContributions(virtualWiki, userString, pagination, descending);
 		while (rs.next()) {
 			RecentChange change = initRecentChange(rs);
 			all.add(change);
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	public static String getValidationQuery() {
 		return (StringUtils.hasText(CONNECTION_VALIDATION_QUERY)) ? CONNECTION_VALIDATION_QUERY : null;
 	}
 
 	/**
 	 *
 	 */
 	public Collection getVirtualWikiList() throws Exception {
 		if (virtualWikiNameHash == null) {
 			loadVirtualWikiHashes();
 		}
 		return virtualWikiNameHash.values();
 	}
 
 	/**
 	 *
 	 */
 	private void handleErrors(Connection conn) {
 		if (conn == null) return;
 		try {
 			logger.warning("Rolling back database transactions");
 			conn.rollback();
 		} catch (Exception e) {
 			logger.severe("Unable to rollback connection", e);
 		}
 	}
 
 	/**
 	 * Set up database tables, and then call the parent method to initialize
 	 * default values.
 	 */
 	public void initialize(Locale locale, WikiUser user) throws Exception {
 		if (this.isInitialized()) {
 			logger.warning("Attempt to initialize when initialization already complete");
 			return;
 		}
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			// set up tables
 			DatabaseHandler.queryHandler.createTables(conn);
 		} catch (Exception e) {
 			logger.severe("Unable to set up database tables", e);
 			// clean up anything that might have been created
 			DatabaseHandler.queryHandler.dropTables(conn);
 		} finally {
 			if (conn != null) DatabaseConnection.closeConnection(conn);
 		}
 		if (!Environment.getBooleanValue(Environment.PROP_BASE_INITIALIZED)) {
 			return;
 		}
 		conn = null;
 		try {
 			this.resetCache();
 			conn = this.getConnection();
 			setupDefaultVirtualWiki();
 			setupAdminUser(user, conn);
 			setupSpecialPages(locale, user, conn);
 			this.loadVirtualWikiHashes();
 		} catch (Exception e) {
 			this.handleErrors(conn);
 			throw e;
 		} finally {
 			this.releaseParams(conn);
 		}
 		this.initialized = true;
 	}
 
 	/**
 	 *
 	 */
 	private RecentChange initRecentChange(WikiResultSet rs) {
 		try {
 			RecentChange change = new RecentChange();
 			change.setTopicVersionId(rs.getInt("topic_version_id"));
 			int previousTopicVersionId = rs.getInt("previous_topic_version_id");
 			if (previousTopicVersionId > 0) change.setPreviousTopicVersionId(new Integer(previousTopicVersionId));
 			change.setTopicId(rs.getInt("topic_id"));
 			change.setTopicName(rs.getString("topic_name"));
 			change.setEditDate(rs.getTimestamp("edit_date"));
 			change.setEditComment(rs.getString("edit_comment"));
 			int userId = rs.getInt("wiki_user_id");
 			if (userId > 0) change.setAuthorId(new Integer(userId));
 			change.setAuthorName(rs.getString("display_name"));
 			change.setEditType(rs.getInt("edit_type"));
 			change.setVirtualWiki(rs.getString("virtual_wiki_name"));
 			return change;
 		} catch (Exception e) {
 			logger.severe("Failure while initializing recent change", e);
 			return null;
 		}
 	}
 
 	/**
 	 *
 	 */
 	private Topic initTopic(WikiResultSet rs) {
 		try {
 			int virtualWikiId = rs.getInt("virtual_wiki_id");
 			String virtualWiki = this.lookupVirtualWikiName(virtualWikiId);
 			Topic topic = new Topic();
 			topic.setAdminOnly(rs.getInt("topic_admin_only") != 0);
 			topic.setName(rs.getString("topic_name"));
 			topic.setVirtualWiki(virtualWiki);
 			topic.setTopicContent(rs.getString("topic_content"));
 			topic.setTopicId(rs.getInt("topic_id"));
 			topic.setReadOnly(rs.getInt("topic_read_only") != 0);
 			topic.setDeleteDate(rs.getTimestamp("delete_date"));
 			topic.setTopicType(rs.getInt("topic_type"));
 			topic.setRedirectTo(rs.getString("redirect_to"));
 			return topic;
 		} catch (Exception e) {
 			logger.severe("Failure while initializing topic", e);
 			return null;
 		}
 	}
 
 	/**
 	 *
 	 */
 	private TopicVersion initTopicVersion(WikiResultSet rs) {
 		try {
 			TopicVersion topicVersion = new TopicVersion();
 			topicVersion.setTopicVersionId(rs.getInt("topic_version_id"));
 			topicVersion.setTopicId(rs.getInt("topic_id"));
 			topicVersion.setEditComment(rs.getString("edit_comment"));
 			topicVersion.setVersionContent(rs.getString("version_content"));
 			int previousTopicVersionId = rs.getInt("previous_topic_version_id");
 			if (previousTopicVersionId > 0) topicVersion.setPreviousTopicVersionId(new Integer(previousTopicVersionId));
 			int userId = rs.getInt("wiki_user_id");
 			if (userId > 0) topicVersion.setAuthorId(new Integer(userId));
 			topicVersion.setEditDate(rs.getTimestamp("edit_date"));
 			topicVersion.setEditType(rs.getInt("edit_type"));
 			topicVersion.setAuthorIpAddress(rs.getString("wiki_user_ip_address"));
 			return topicVersion;
 		} catch (Exception e) {
 			logger.severe("Failure while initializing topic version", e);
 			return null;
 		}
 	}
 
 	/**
 	 *
 	 */
 	private VirtualWiki initVirtualWiki(WikiResultSet rs) {
 		try {
 			VirtualWiki virtualWiki = new VirtualWiki();
 			virtualWiki.setVirtualWikiId(rs.getInt("virtual_wiki_id"));
 			virtualWiki.setName(rs.getString("virtual_wiki_name"));
 			virtualWiki.setDefaultTopicName(rs.getString("default_topic_name"));
 			return virtualWiki;
 		} catch (Exception e) {
 			logger.severe("Failure while initializing virtual wiki", e);
 			return null;
 		}
 	}
 
 	/**
 	 *
 	 */
 	private WikiFile initWikiFile(WikiResultSet rs) {
 		try {
 			int virtualWikiId = rs.getInt("virtual_wiki_id");
 			String virtualWiki = this.lookupVirtualWikiName(virtualWikiId);
 			WikiFile wikiFile = new WikiFile();
 			wikiFile.setFileId(rs.getInt("file_id"));
 			wikiFile.setAdminOnly(rs.getInt("file_admin_only") != 0);
 			wikiFile.setFileName(rs.getString("file_name"));
 			wikiFile.setVirtualWiki(virtualWiki);
 			wikiFile.setUrl(rs.getString("file_url"));
 			wikiFile.setTopicId(rs.getInt("topic_id"));
 			wikiFile.setReadOnly(rs.getInt("file_read_only") != 0);
 			wikiFile.setDeleteDate(rs.getTimestamp("delete_date"));
 			wikiFile.setMimeType(rs.getString("mime_type"));
 			wikiFile.setFileSize(rs.getInt("file_size"));
 			return wikiFile;
 		} catch (Exception e) {
 			logger.severe("Failure while initializing file", e);
 			return null;
 		}
 	}
 
 	/**
 	 *
 	 */
 	private WikiFileVersion initWikiFileVersion(WikiResultSet rs) {
 		try {
 			WikiFileVersion wikiFileVersion = new WikiFileVersion();
 			wikiFileVersion.setFileVersionId(rs.getInt("file_version_id"));
 			wikiFileVersion.setFileId(rs.getInt("file_id"));
 			wikiFileVersion.setUploadComment(rs.getString("upload_comment"));
 			wikiFileVersion.setUrl(rs.getString("file_url"));
 			int userId = rs.getInt("wiki_user_id");
 			if (userId > 0) wikiFileVersion.setAuthorId(new Integer(userId));
 			wikiFileVersion.setUploadDate(rs.getTimestamp("upload_date"));
 			wikiFileVersion.setMimeType(rs.getString("mime_type"));
 			wikiFileVersion.setAuthorIpAddress(rs.getString("wiki_user_ip_address"));
 			wikiFileVersion.setFileSize(rs.getInt("file_size"));
 			return wikiFileVersion;
 		} catch (Exception e) {
 			logger.severe("Failure while initializing wiki file version", e);
 			return null;
 		}
 	}
 
 	/**
 	 *
 	 */
 	private WikiUser initWikiUser(WikiResultSet rs) {
 		try {
 			WikiUser user = new WikiUser();
 			user.setUserId(rs.getInt("wiki_user_id"));
 			user.setLogin(rs.getString("login"));
 			user.setDisplayName(rs.getString("display_name"));
 			user.setCreateDate(rs.getTimestamp("create_date"));
 			user.setLastLoginDate(rs.getTimestamp("last_login_date"));
 			user.setCreateIpAddress(rs.getString("create_ip_address"));
 			user.setLastLoginIpAddress(rs.getString("last_login_ip_address"));
 			user.setAdmin(rs.getInt("is_admin") != 0);
 			// FIXME - may be in LDAP
 			user.setEmail(rs.getString("email"));
 			user.setFirstName(rs.getString("first_name"));
 			user.setLastName(rs.getString("last_name"));
 			user.setEncodedPassword(rs.getString("encoded_password"));
 			return user;
 		} catch (Exception e) {
 			logger.severe("Failure while initializing user", e);
 			return null;
 		}
 	}
 
 	/**
 	 * Return <code>true</code> if the handler is initialized and ready to
 	 * retrieve and save data.
 	 */
 	public boolean isInitialized() {
 		if (!Environment.getBooleanValue(Environment.PROP_BASE_INITIALIZED)) {
 			// properties not initialized
 			return false;
 		}
 		if (this.initialized) {
 			return true;
 		}
 		try {
 			WikiResultSet rs = DatabaseHandler.queryHandler.getVirtualWikis();
 			return rs.next();
 		} catch (Exception e) {
 			// tables don't exist, or some other problem
 			logger.warning("Database handler not initialized: " + e.getMessage());
 			return false;
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void loadVirtualWikiHashes() throws Exception {
 		DatabaseHandler.virtualWikiNameHash = new Hashtable();
 		DatabaseHandler.virtualWikiIdHash = new Hashtable();
 		try {
 			WikiResultSet rs = DatabaseHandler.queryHandler.getVirtualWikis();
 			while (rs.next()) {
 				VirtualWiki virtualWiki = initVirtualWiki(rs);
 				DatabaseHandler.virtualWikiNameHash.put(virtualWiki.getName(), virtualWiki);
 				DatabaseHandler.virtualWikiIdHash.put(new Integer(virtualWiki.getVirtualWikiId()), virtualWiki);
 			}
 		} catch (Exception e) {
 			logger.severe("Failure while loading virtual wiki hashtable ", e);
 			// if there is an error make sure the hashtable is reset since it wasn't
 			// properly initialized
 			DatabaseHandler.virtualWikiNameHash = null;
 			DatabaseHandler.virtualWikiIdHash = null;
 			throw e;
 		}
 	}
 
 	/**
 	 *
 	 */
 	public Collection lookupCategoryTopics(String virtualWiki, String categoryName, int topicType) throws Exception {
 		Vector results = new Vector();
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupCategoryTopics(virtualWikiId, categoryName, topicType);
 		while (rs.next()) {
 			Category category = new Category();
 			category.setName(categoryName);
 			category.setVirtualWiki(virtualWiki);
 			category.setChildTopicName(rs.getString("topic_name"));
 			category.setSortKey(rs.getString("sort_key"));
 			results.add(category);
 		}
 		return results;
 	}
 
 	/**
 	 *
 	 */
 	public TopicVersion lookupLastTopicVersion(String virtualWiki, String topicName) throws Exception {
 		Topic topic = lookupTopic(virtualWiki, topicName, true, true);
 		if (topic == null) return null;
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupLastTopicVersion(topic);
 		if (rs.size() == 0) return null;
 		int topicVersionId = rs.getInt("topic_version_id");
 		return lookupTopicVersion(topicName, topicVersionId);
 	}
 
 	/**
 	 *
 	 */
 	public TopicVersion lookupLastTopicVersion(String virtualWiki, String topicName, Connection conn) throws Exception {
 		Topic topic = lookupTopic(virtualWiki, topicName, true, true, conn);
 		if (topic == null) return null;
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupLastTopicVersion(topic, conn);
 		if (rs.size() == 0) return null;
 		int topicVersionId = rs.getInt("topic_version_id");
 		return lookupTopicVersion(topicName, topicVersionId, conn);
 	}
 
 	/**
 	 *
 	 * @param caseSensitive Set to <code>true</code> if the topic name should be
 	 *  searched for in a case-sensitive manner.
 	 */
 	public Topic lookupTopic(String virtualWiki, String topicName, boolean caseSensitive) throws Exception {
 		return lookupTopic(virtualWiki, topicName, caseSensitive, false);
 	}
 
 	/**
 	 *
 	 * @param caseSensitive Set to <code>true</code> if the topic name should be
 	 *  searched for in a case-sensitive manner.
 	 */
 	public Topic lookupTopic(String virtualWiki, String topicName, boolean caseSensitive, boolean deleteOK) throws Exception {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupTopic(virtualWikiId, topicName, caseSensitive, deleteOK);
 		if (rs.size() == 0) return null;
 		return initTopic(rs);
 	}
 
 	/**
 	 *
 	 * @param caseSensitive Set to <code>true</code> if the topic name should be
 	 *  searched for in a case-sensitive manner.
 	 */
 	public Topic lookupTopic(String virtualWiki, String topicName, boolean caseSensitive, boolean deleteOK, Connection conn) throws Exception {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupTopic(virtualWikiId, topicName, caseSensitive, deleteOK, conn);
 		if (rs.size() == 0) return null;
 		return initTopic(rs);
 	}
 
 	/**
 	 *
 	 */
 	public Collection lookupTopicByType(String virtualWiki, int topicType, Pagination pagination) throws Exception {
 		Vector results = new Vector();
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupTopicByType(virtualWikiId, topicType, pagination);
 		while (rs.next()) {
 			results.add(rs.getString("topic_name"));
 		}
 		return results;
 	}
 
 	/**
 	 *
 	 */
 	public TopicVersion lookupTopicVersion(String topicName, int topicVersionId) throws Exception {
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupTopicVersion(topicVersionId);
 		if (rs.size() == 0) return null;
 		return initTopicVersion(rs);
 	}
 
 	/**
 	 *
 	 */
 	public TopicVersion lookupTopicVersion(String topicName, int topicVersionId, Connection conn) throws Exception {
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupTopicVersion(topicVersionId, conn);
 		if (rs.size() == 0) return null;
 		return initTopicVersion(rs);
 	}
 
 	/**
 	 *
 	 */
 	public VirtualWiki lookupVirtualWiki(String virtualWikiName) throws Exception {
 		if (virtualWikiNameHash == null) {
 			loadVirtualWikiHashes();
 		}
 		return (VirtualWiki)virtualWikiNameHash.get(virtualWikiName);
 	}
 
 	/**
 	 *
 	 */
 	public int lookupVirtualWikiId(String virtualWikiName) throws Exception {
 		if (virtualWikiNameHash == null) {
 			this.loadVirtualWikiHashes();
 		}
 		VirtualWiki virtualWiki = (VirtualWiki)virtualWikiNameHash.get(virtualWikiName);
 		return (virtualWiki != null) ? virtualWiki.getVirtualWikiId() : -1;
 	}
 
 	/**
 	 *
 	 */
 	public String lookupVirtualWikiName(int virtualWikiId) throws Exception {
 		if (virtualWikiIdHash == null) {
 			this.loadVirtualWikiHashes();
 		}
 		VirtualWiki virtualWiki = (VirtualWiki)virtualWikiIdHash.get(new Integer(virtualWikiId));
 		return (virtualWiki != null) ? virtualWiki.getName() : null;
 	}
 
 	/**
 	 *
 	 */
 	public WikiFile lookupWikiFile(String virtualWiki, String topicName) throws Exception {
 		Topic topic = lookupTopic(virtualWiki, topicName, true);
 		if (topic == null) return null;
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupWikiFile(virtualWikiId, topic.getTopicId());
 		if (rs.size() == 0) return null;
 		return initWikiFile(rs);
 	}
 
 	/**
 	 *
 	 */
 	public WikiUser lookupWikiUser(int userId) throws Exception {
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupWikiUser(userId);
 		if (rs.size() == 0) return null;
 		return initWikiUser(rs);
 	}
 
 	/**
 	 *
 	 */
 	private WikiUser lookupWikiUser(int userId, Connection conn) throws Exception {
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupWikiUser(userId, conn);
 		if (rs.size() == 0) return null;
 		return initWikiUser(rs);
 	}
 
 	/**
 	 *
 	 */
 	public WikiUser lookupWikiUser(String login) throws Exception {
 		// FIXME - handle LDAP
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupWikiUser(login);
 		if (rs.size() == 0) return null;
 		int userId = rs.getInt("wiki_user_id");
 		return lookupWikiUser(userId);
 	}
 
 	/**
 	 *
 	 */
 	public WikiUser lookupWikiUser(String login, String password, boolean encrypted) throws Exception {
 		// FIXME - handle LDAP
 		String encryptedPassword = password;
 		if (!encrypted) {
 			encryptedPassword = Encryption.encrypt(password);
 		}
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupWikiUser(login, encryptedPassword);
 		if (rs.size() == 0) return null;
 		int userId = rs.getInt("wiki_user_id");
 		return lookupWikiUser(userId);
 	}
 
 	/**
 	 *
 	 */
 	private String lookupWikiUserLogin(Integer authorId) throws Exception {
 		String login = (String)cachedUserLoginHash.get(authorId);
 		if (login != null) {
 			return login;
 		}
 		WikiUser user = lookupWikiUser(authorId.intValue());
 		login = user.getLogin();
 		if (login != null) {
 			cachedUserLoginHash.put(authorId, login);
 		}
 		return login;
 	}
 
 	/**
 	 *
 	 */
 	public void moveTopic(Topic fromTopic, TopicVersion fromVersion, String destination) throws Exception {
 		Connection conn = null;
 		try {
 			conn = this.getConnection();
 			if (!this.canMoveTopic(fromTopic, destination)) {
 				throw new WikiException(new WikiMessage("move.exception.destinationexists", destination));
 			}
 			Topic toTopic = WikiBase.getHandler().lookupTopic(fromTopic.getVirtualWiki(), destination, true);
 			boolean detinationExistsFlag = (toTopic != null && toTopic.getDeleteDate() == null);
 			if (detinationExistsFlag) {
 				// if the target topic is a redirect to the source topic then the
 				// target must first be deleted.
 				this.deleteTopic(toTopic, null, false, conn);
 			}
 			String fromTopicName = fromTopic.getName();
 			fromTopic.setName(destination);
 			writeTopic(fromTopic, fromVersion, Utilities.parserOutput(fromTopic.getTopicContent()), conn, true);
 			if (detinationExistsFlag) {
 				// target topic was deleted, so rename and undelete
 				toTopic.setName(fromTopicName);
 				writeTopic(toTopic, null, null, conn, false);
 				this.undeleteTopic(toTopic, null, false, conn);
 			} else {
 				// create a new topic that redirects to the destination
 				toTopic = fromTopic;
 				toTopic.setTopicId(-1);
 				toTopic.setName(fromTopicName);
 			}
 			String content = Utilities.parserRedirectContent(destination);
 			toTopic.setRedirectTo(destination);
 			toTopic.setTopicType(Topic.TYPE_REDIRECT);
 			toTopic.setTopicContent(content);
 			TopicVersion toVersion = fromVersion;
 			toVersion.setTopicVersionId(-1);
 			toVersion.setVersionContent(content);
 			writeTopic(toTopic, toVersion, Utilities.parserOutput(content), conn, true);
 		} catch (Exception e) {
 			this.handleErrors(conn);
 			throw e;
 		} finally {
 			this.releaseParams(conn);
 		}
 	}
 
 	/**
 	 * This method causes all existing data to be deleted from the Wiki.  Use only
 	 * when totally re-initializing a system.  To reiterate: CALLING THIS METHOD WILL
 	 * DELETE ALL WIKI DATA!
 	 */
 	private void purgeData(Connection conn) throws Exception {
 		// BOOM!  Everything gone...
 		DatabaseHandler.queryHandler.dropTables(conn);
 		try {
 			// re-create empty tables
 			DatabaseHandler.queryHandler.createTables(conn);
 		} catch (Exception e) {
 			// creation failure, don't leave tables half-committed
 			DatabaseHandler.queryHandler.dropTables(conn);
 		}
 	}
 
 	/**
 	 * Utility method for reading default topic values from files and returning
 	 * the file contents.
 	 */
 	private static String readSpecialPage(Locale locale, String topicName) throws Exception {
 		String contents = null;
 		String filename = null;
 		String language = null;
 		String country = null;
 		if (locale != null) {
 			language = locale.getLanguage();
 			country = locale.getCountry();
 		}
 		String subdirectory = WikiBase.SPECIAL_PAGE_DIR + File.separator;
 		if (StringUtils.hasText(language) && StringUtils.hasText(country)) {
 			try {
 				filename = subdirectory + Utilities.encodeForFilename(topicName + "_" + language + "_" + country) + ".txt";
 				contents = Utilities.readFile(filename);
 			} catch (Exception e) {
 				logger.warning("File " + filename + " does not exist");
 			}
 		}
 		if (contents == null && StringUtils.hasText(language)) {
 			try {
 				filename = subdirectory + Utilities.encodeForFilename(topicName + "_" + language) + ".txt";
 				contents = Utilities.readFile(filename);
 			} catch (Exception e) {
 				logger.warning("File " + filename + " does not exist");
 			}
 		}
 		if (contents == null) {
 			try {
 				filename = subdirectory + Utilities.encodeForFilename(topicName) + ".txt";
 				contents = Utilities.readFile(filename);
 			} catch (Exception e) {
 				logger.warning("File " + filename + " could not be read", e);
 				throw e;
 			}
 		}
 		return contents;
 	}
 
 	/**
 	 *
 	 */
 	private void releaseParams(Connection conn) throws Exception {
 		if (conn == null) return;
 		try {
 			conn.commit();
 		} finally {
 			if (conn != null) DatabaseConnection.closeConnection(conn);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void reloadRecentChanges() throws Exception {
 		Connection conn = null;
 		try {
 			conn = this.getConnection();
 			DatabaseHandler.queryHandler.reloadRecentChanges(conn);
 		} catch (Exception e) {
 			this.handleErrors(conn);
 			throw e;
 		} finally {
 			this.releaseParams(conn);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void resetCache() {
 		DatabaseHandler.virtualWikiIdHash = null;
 		DatabaseHandler.virtualWikiNameHash = null;
 		DatabaseHandler.cachedTopicsList = new WikiCacheMap(MAX_CACHED_LIST_SIZE);
 		DatabaseHandler.cachedNonTopicsList = new WikiCacheMap(MAX_CACHED_LIST_SIZE);
 		DatabaseHandler.cachedUserLoginHash = new WikiCacheMap(MAX_CACHED_LIST_SIZE);
 	}
 
 	/**
 	 *
 	 */
 	private void setupAdminUser(WikiUser user, Connection conn) throws Exception {
 		if (user == null) {
 			throw new Exception("Admin user not specified");
 		}
 		if (lookupWikiUser(user.getUserId(), conn) != null) {
 			logger.warning("Admin user already exists");
 		}
 		addWikiUser(user, conn);
 	}
 
 	/**
 	 *
 	 */
 	public static void setupDefaultDatabase(Properties props) {
 		props.setProperty(Environment.PROP_BASE_PERSISTENCE_TYPE, "INTERNAL");
 		props.setProperty(Environment.PROP_DB_DRIVER, "org.hsqldb.jdbcDriver");
 		props.setProperty(Environment.PROP_DB_TYPE, DatabaseHandler.DB_TYPE_HSQL);
 		props.setProperty(Environment.PROP_DB_USERNAME, "sa");
 		props.setProperty(Environment.PROP_DB_PASSWORD, "");
 		File file = new File(props.getProperty(Environment.PROP_BASE_FILE_DIR), "database");
 		if (!file.exists()) {
 			file.mkdirs();
 		}
 		String url = "jdbc:hsqldb:file:" + new File(file.getPath(), "jamwiki").getPath();
 		props.setProperty(Environment.PROP_DB_URL, url);
 	}
 
 	/**
 	 *
 	 */
 	private void setupDefaultVirtualWiki() throws Exception {
 		if (lookupVirtualWiki(WikiBase.DEFAULT_VWIKI) != null) {
 			logger.warning("Default virtual wiki already exists");
 			return;
 		}
 		VirtualWiki virtualWiki = new VirtualWiki();
 		virtualWiki.setName(WikiBase.DEFAULT_VWIKI);
 		virtualWiki.setDefaultTopicName(Environment.getValue(Environment.PROP_BASE_DEFAULT_TOPIC));
 		writeVirtualWiki(virtualWiki);
 	}
 
 	/**
 	 *
 	 */
 	private void setupSpecialPage(Locale locale, String virtualWiki, String topicName, WikiUser user, boolean adminOnly, Connection conn) throws Exception {
		if (exists(virtualWiki, topicName, true)) {
 			logger.warning("Special page " + virtualWiki + " / " + topicName + " already exists");
 			return;
 		}
 		logger.info("Setting up special page " + virtualWiki + " / " + topicName);
 		String contents = DatabaseHandler.readSpecialPage(locale, topicName);
 		Topic topic = new Topic();
 		topic.setName(topicName);
 		topic.setVirtualWiki(virtualWiki);
 		topic.setTopicContent(contents);
 		topic.setAdminOnly(adminOnly);
 		// FIXME - hard coding
 		TopicVersion topicVersion = new TopicVersion(user, user.getLastLoginIpAddress(), "Automatically created by system setup", contents);
 		writeTopic(topic, topicVersion, Utilities.parserOutput(topic.getTopicContent()), conn, true);
 	}
 
 	/**
 	 *
 	 */
 	public void setupSpecialPages(Locale locale, WikiUser user, VirtualWiki virtualWiki) throws Exception {
 		Connection conn = null;
 		try {
 			conn = this.getConnection();
 			// create the default topics
 			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STARTING_POINTS, user, false, conn);
 			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_LEFT_MENU, user, true, conn);
 			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_BOTTOM_AREA, user, true, conn);
 			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STYLESHEET, user, true, conn);
 			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_ADMIN_ONLY_TOPICS, user, true, conn);
 		} catch (Exception e) {
 			this.handleErrors(conn);
 			throw e;
 		} finally {
 			this.releaseParams(conn);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void setupSpecialPages(Locale locale, WikiUser user, Connection conn) throws Exception {
 		Collection all = getVirtualWikiList();
 		for (Iterator iterator = all.iterator(); iterator.hasNext();) {
 			VirtualWiki virtualWiki = (VirtualWiki)iterator.next();
 			// create the default topics
 			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STARTING_POINTS, user, false, conn);
 			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_LEFT_MENU, user, true, conn);
 			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_BOTTOM_AREA, user, true, conn);
 			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STYLESHEET, user, true, conn);
 			setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_ADMIN_ONLY_TOPICS, user, true, conn);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void undeleteTopic(Topic topic, TopicVersion topicVersion, boolean userVisible) throws Exception {
 		Connection conn = null;
 		try {
 			conn = this.getConnection();
 			this.undeleteTopic(topic, topicVersion, userVisible, conn);
 		} catch (Exception e) {
 			this.handleErrors(conn);
 			throw e;
 		} finally {
 			this.releaseParams(conn);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void undeleteTopic(Topic topic, TopicVersion topicVersion, boolean userVisible, Connection conn) throws Exception {
 		// update topic to indicate deleted, add delete topic version.  parser output
 		// should be empty since nothing to add to search engine.
 		ParserOutput parserOutput = new ParserOutput();
 		topic.setDeleteDate(null);
 		writeTopic(topic, topicVersion, parserOutput, conn, userVisible);
 		// reset topic existence vector
 		cachedTopicsList = new WikiCacheMap(MAX_CACHED_LIST_SIZE);
 	}
 
 	/**
 	 *
 	 */
 	public void updateSpecialPage(Locale locale, String virtualWiki, String topicName, WikiUser user, String ipAddress) throws Exception {
 		logger.info("Updating special page " + virtualWiki + " / " + topicName);
 		Connection conn = null;
 		try {
 			conn = this.getConnection();
 			String contents = DatabaseHandler.readSpecialPage(locale, topicName);
 			Topic topic = this.lookupTopic(virtualWiki, topicName, true);
 			topic.setTopicContent(contents);
 			// FIXME - hard coding
 			TopicVersion topicVersion = new TopicVersion(user, ipAddress, "Automatically updated by system upgrade", contents);
 			writeTopic(topic, topicVersion, Utilities.parserOutput(topic.getTopicContent()), conn, true);
 		} catch (Exception e) {
 			this.handleErrors(conn);
 			throw e;
 		} finally {
 			this.releaseParams(conn);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void updateTopic(Topic topic, Connection conn) throws Exception {
 		int virtualWikiId = this.lookupVirtualWikiId(topic.getVirtualWiki());
 		DatabaseHandler.queryHandler.updateTopic(topic, virtualWikiId, conn);
 	}
 
 	/**
 	 *
 	 */
 	private void updateVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws Exception {
 		DatabaseHandler.queryHandler.updateVirtualWiki(virtualWiki, conn);
 	}
 
 	/**
 	 *
 	 */
 	private void updateWikiFile(String topicName, WikiFile wikiFile, Connection conn) throws Exception {
 		int virtualWikiId = this.lookupVirtualWikiId(wikiFile.getVirtualWiki());
 		DatabaseHandler.queryHandler.updateWikiFile(wikiFile, virtualWikiId, conn);
 	}
 
 	/**
 	 *
 	 */
 	private void updateWikiUser(WikiUser user, Connection conn) throws Exception {
 		DatabaseHandler.queryHandler.updateWikiUser(user, conn);
 		// FIXME - may be in LDAP
 		DatabaseHandler.queryHandler.updateWikiUserInfo(user, conn);
 	}
 
 	/**
 	 *
 	 */
 	public void writeFile(String topicName, WikiFile wikiFile, WikiFileVersion wikiFileVersion) throws Exception {
 		Connection conn = null;
 		try {
 			conn = this.getConnection();
 			if (wikiFile.getFileId() <= 0) {
 				addWikiFile(topicName, wikiFile, conn);
 			} else {
 				updateWikiFile(topicName, wikiFile, conn);
 			}
 			wikiFileVersion.setFileId(wikiFile.getFileId());
 			if (Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
 				// write version
 				addWikiFileVersion(topicName, wikiFileVersion, conn);
 			}
 		} catch (Exception e) {
 			this.handleErrors(conn);
 			throw e;
 		} finally {
 			this.releaseParams(conn);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void writeTopic(Topic topic, TopicVersion topicVersion, ParserOutput parserOutput) throws Exception {
 		Connection conn = null;
 		try {
 			conn = this.getConnection();
 			WikiUser user = null;
 			if (topicVersion.getAuthorId() != null) {
 				user = lookupWikiUser(topicVersion.getAuthorId().intValue(), conn);
 			}
 			this.writeTopic(topic, topicVersion, parserOutput, conn, true);
 		} catch (Exception e) {
 			this.handleErrors(conn);
 			throw e;
 		} finally {
 			this.releaseParams(conn);
 		}
 	}
 
 	/**
 	 * Commit changes to a topic (and its version) to the database or filesystem.
 	 *
 	 * @param topic The topic object that is to be committed.  If the topic id is
 	 *  empty or less than zero then the topic is added, otherwise an update is performed.
 	 * @param topicVersion The version associated with the topic that is being added.
 	 *  This parameter should never be null UNLESS the change is not user visible, such as
 	 *  when deleting a topic temporarily during page moves.
 	 * @param parserOutput The parserOutput object that contains a list of links in the
 	 *  topic content, categories, etc.  This parameter may be set with the
 	 *  Utilities.getParserOutput() method.
 	 * @param conn Database connection or other parameters required for updates.
 	 * @param userVisible A flag indicating whether or not this change should be visible
 	 *  to Wiki users.  This flag should be true except in rare cases, such as when
 	 *  temporarily deleting a topic during page moves.
 	 */
 	private void writeTopic(Topic topic, TopicVersion topicVersion, ParserOutput parserOutput, Connection conn, boolean userVisible) throws Exception {
 		if (!Utilities.validateTopicName(topic.getName())) {
 			throw new WikiException(new WikiMessage("common.exception.name", topic.getName()));
 		}
 		if (topic.getTopicId() <= 0) {
 			addTopic(topic, conn);
 		} else {
 			updateTopic(topic, conn);
 		}
 		if (userVisible) {
 			if (topicVersion.getPreviousTopicVersionId() == null) {
 				TopicVersion tmp = lookupLastTopicVersion(topic.getVirtualWiki(), topic.getName(), conn);
 				if (tmp != null) topicVersion.setPreviousTopicVersionId(new Integer(tmp.getTopicVersionId()));
 			}
 			topicVersion.setTopicId(topic.getTopicId());
 			if (Environment.getBooleanValue(Environment.PROP_TOPIC_VERSIONING_ON)) {
 				// write version
 				addTopicVersion(topic.getName(), topicVersion, conn);
 			}
 			String authorName = topicVersion.getAuthorIpAddress();
 			Integer authorId = topicVersion.getAuthorId();
 			if (authorId != null) {
 				WikiUser user = lookupWikiUser(topicVersion.getAuthorId().intValue(), conn);
 				authorName = user.getLogin();
 			}
 			RecentChange change = new RecentChange(topic, topicVersion, authorName);
 			addRecentChange(change, conn);
 		}
 		if (parserOutput != null) {
 			// add / remove categories associated with the topic
 			this.deleteTopicCategories(topic, conn);
 			LinkedHashMap categories = parserOutput.getCategories();
 			for (Iterator iterator = categories.keySet().iterator(); iterator.hasNext();) {
 				String categoryName = (String)iterator.next();
 				Category category = new Category();
 				category.setName(categoryName);
 				category.setSortKey((String)categories.get(categoryName));
 				category.setVirtualWiki(topic.getVirtualWiki());
 				category.setChildTopicName(topic.getName());
 				this.addCategory(category, conn);
 			}
 		}
 		// reset topic non-existence vector
 		cachedNonTopicsList = new WikiCacheMap(MAX_CACHED_LIST_SIZE);
 		if (parserOutput != null) {
 			WikiBase.getSearchEngine().deleteFromIndex(topic);
 			WikiBase.getSearchEngine().addToIndex(topic, parserOutput.getLinks());
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void writeVirtualWiki(VirtualWiki virtualWiki) throws Exception {
 		if (!Utilities.validateTopicName(virtualWiki.getName())) {
 			throw new WikiException(new WikiMessage("common.exception.name", virtualWiki.getName()));
 		}
 		Connection conn = null;
 		try {
 			conn = this.getConnection();
 			if (virtualWiki.getVirtualWikiId() <= 0) {
 				this.addVirtualWiki(virtualWiki, conn);
 			} else {
 				this.updateVirtualWiki(virtualWiki, conn);
 			}
 		} catch (Exception e) {
 			this.handleErrors(conn);
 			throw e;
 		} finally {
 			this.releaseParams(conn);
 		}
 		// update the hashtable AFTER the commit
 		this.loadVirtualWikiHashes();
 	}
 
 	/**
 	 *
 	 */
 	public void writeWikiUser(WikiUser user) throws Exception {
 		if (!Utilities.validateUserName(user.getLogin())) {
 			throw new WikiException(new WikiMessage("common.exception.name", user.getLogin()));
 		}
 		Connection conn = null;
 		try {
 			conn = this.getConnection();
 			if (user.getUserId() <= 0) {
 				this.addWikiUser(user, conn);
 			} else {
 				this.updateWikiUser(user, conn);
 			}
 		} catch (Exception e) {
 			this.handleErrors(conn);
 			throw e;
 		} finally {
 			this.releaseParams(conn);
 		}
 	}
 }

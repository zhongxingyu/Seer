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
 
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import net.sf.ehcache.Element;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.DataAccessException;
 import org.jamwiki.DataHandler;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.WikiException;
 import org.jamwiki.WikiMessage;
 import org.jamwiki.model.Category;
 import org.jamwiki.model.Interwiki;
 import org.jamwiki.model.LogItem;
 import org.jamwiki.model.Namespace;
 import org.jamwiki.model.RecentChange;
 import org.jamwiki.model.Role;
 import org.jamwiki.model.RoleMap;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicType;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.UserBlock;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.Watchlist;
 import org.jamwiki.model.WikiFile;
 import org.jamwiki.model.WikiFileVersion;
 import org.jamwiki.model.WikiGroup;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.model.WikiUserDetails;
 import org.jamwiki.parser.ParserException;
 import org.jamwiki.parser.ParserOutput;
 import org.jamwiki.parser.ParserUtil;
 import org.jamwiki.utils.Encryption;
 import org.jamwiki.utils.LinkUtil;
 import org.jamwiki.utils.Pagination;
 import org.jamwiki.utils.WikiCache;
 import org.jamwiki.utils.WikiLogger;
 import org.jamwiki.utils.WikiUtil;
 import org.springframework.transaction.TransactionStatus;
 
 /**
  * Default implementation of the {@link org.jamwiki.DataHandler} interface for
  * ANSI SQL compatible databases.
  */
 public class AnsiDataHandler implements DataHandler {
 
 	/** Any topic lookup that takes longer than the specified time (in ms) will trigger a log message. */
 	private static final int TIME_LIMIT_TOPIC_LOOKUP = 20;
 	private static final String CACHE_INTERWIKI_LIST = "org.jamwiki.db.AnsiDataHandler.CACHE_INTERWIKI_LIST";
 	private static final String CACHE_NAMESPACE_LIST = "org.jamwiki.db.AnsiDataHandler.CACHE_NAMESPACE_LIST";
 	private static final String CACHE_ROLE_MAP_GROUP = "org.jamwiki.db.AnsiDataHandler.CACHE_ROLE_MAP_GROUP";
 	private static final String CACHE_TOPIC_NAMES_BY_NAME = "org.jamwiki.db.AnsiDataHandler.CACHE_TOPIC_NAMES_BY_NAME";
 	private static final String CACHE_TOPICS_BY_ID = "org.jamwiki.db.AnsiDataHandler.CACHE_TOPICS_BY_ID";
 	private static final String CACHE_TOPICS_BY_NAME = "org.jamwiki.db.AnsiDataHandler.CACHE_TOPICS_BY_NAME";
 	private static final String CACHE_TOPIC_VERSIONS = "org.jamwiki.db.AnsiDataHandler.CACHE_TOPIC_VERSIONS";
 	private static final String CACHE_USER_BLOCKS_ACTIVE = "org.jamwiki.db.AnsiDataHandler.CACHE_USER_BLOCKS_ACTIVE";
 	private static final String CACHE_USER_BY_USER_ID = "org.jamwiki.db.AnsiDataHandler.CACHE_USER_BY_USER_ID";
 	private static final String CACHE_USER_BY_USER_NAME = "org.jamwiki.db.AnsiDataHandler.CACHE_USER_BY_USER_NAME";
 	private static final String CACHE_VIRTUAL_WIKI_LIST = "org.jamwiki.db.AnsiDataHandler.CACHE_VIRTUAL_WIKI_LIST";
 	private static final WikiLogger logger = WikiLogger.getLogger(AnsiDataHandler.class.getName());
 
 	private final QueryHandler queryHandler = new AnsiQueryHandler();
 
 	/**
 	 *
 	 */
 	private void addCategories(List<Category> categoryList, int topicId, Connection conn) throws DataAccessException, WikiException {
 		int virtualWikiId = -1;
 		for (Category category : categoryList) {
 			virtualWikiId = this.lookupVirtualWikiId(category.getVirtualWiki());
 			this.validateCategory(category);
 		}
 		try {
 			this.queryHandler().insertCategories(categoryList, virtualWikiId, topicId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addGroupMember(String username, int groupId, Connection conn) throws DataAccessException {
 		try {
 			this.queryHandler().insertGroupMember(username, groupId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addLogItem(LogItem logItem, Connection conn) throws DataAccessException, WikiException {
 		int virtualWikiId = this.lookupVirtualWikiId(logItem.getVirtualWiki());
 		this.validateLogItem(logItem);
 		try {
 			this.queryHandler().insertLogItem(logItem, virtualWikiId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addRecentChange(RecentChange change, Connection conn) throws DataAccessException, WikiException {
 		int virtualWikiId = this.lookupVirtualWikiId(change.getVirtualWiki());
 		this.validateRecentChange(change);
 		try {
 			this.queryHandler().insertRecentChange(change, virtualWikiId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addTopic(Topic topic, Connection conn) throws DataAccessException, WikiException {
 		int virtualWikiId = this.lookupVirtualWikiId(topic.getVirtualWiki());
 		try {
 			this.validateTopic(topic);
 			this.queryHandler().insertTopic(topic, virtualWikiId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addTopicLinks(List<String> links, String virtualWiki, int topicId, Connection conn) throws DataAccessException {
 		// strip any links longer than 200 characters and any duplicates
 		Map<String, Topic> linksMap = new HashMap<String, Topic>();
 		for (String link : links) {
 			if (link.length() <= 200) {
 				Namespace namespace = LinkUtil.retrieveTopicNamespace(virtualWiki, link);
 				String pageName = LinkUtil.retrieveTopicPageName(namespace, virtualWiki, link);
 				// FIXE - link to records are always capitalized, which will cause problems for the
 				// rare case of two topics such as "eBay" and "EBay".
 				pageName = StringUtils.capitalize(pageName);
 				Topic topic = new Topic(virtualWiki, namespace, pageName);
 				linksMap.put(topic.getName(), topic);
 			}
 		}
 		List<Topic> topicLinks = new ArrayList<Topic>(linksMap.values());
 		try {
 			this.queryHandler().insertTopicLinks(topicLinks, topicId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addTopicVersion(Topic topic, TopicVersion topicVersion, Connection conn) throws DataAccessException, WikiException {
 		if (topicVersion.getPreviousTopicVersionId() == null && topic.getCurrentVersionId() != null) {
 			topicVersion.setPreviousTopicVersionId(topic.getCurrentVersionId());
 		}
 		topicVersion.setTopicId(topic.getTopicId());
 		topicVersion.initializeVersionParams(topic);
 		try {
 			this.validateTopicVersion(topicVersion);
 			this.queryHandler().insertTopicVersion(topicVersion, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 		topic.setCurrentVersionId(topicVersion.getTopicVersionId());
 	}
 
 	/**
 	 *
 	 */
 	private void addUserBlock(UserBlock userBlock, Connection conn) throws DataAccessException, WikiException {
 		try {
 			this.validateUserBlock(userBlock);
 			this.queryHandler().insertUserBlock(userBlock, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addUserDetails(WikiUserDetails userDetails, Connection conn) throws DataAccessException, WikiException {
 		this.validateUserDetails(userDetails);
 		try {
 			this.queryHandler().insertUserDetails(userDetails, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws DataAccessException, WikiException {
 		try {
 			this.validateVirtualWiki(virtualWiki);
 			this.queryHandler().insertVirtualWiki(virtualWiki, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addWatchlistEntry(int virtualWikiId, String topicName, int userId, Connection conn) throws DataAccessException, WikiException {
 		this.validateWatchlistEntry(topicName);
 		try {
 			this.queryHandler().insertWatchlistEntry(virtualWikiId, topicName, userId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addWikiFile(WikiFile wikiFile, Connection conn) throws DataAccessException, WikiException {
 		try {
 			int virtualWikiId = this.lookupVirtualWikiId(wikiFile.getVirtualWiki());
 			this.validateWikiFile(wikiFile);
 			this.queryHandler().insertWikiFile(wikiFile, virtualWikiId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addWikiFileVersion(WikiFileVersion wikiFileVersion, Connection conn) throws DataAccessException, WikiException {
 		try {
 			this.validateWikiFileVersion(wikiFileVersion);
 			this.queryHandler().insertWikiFileVersion(wikiFileVersion, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addWikiGroup(WikiGroup group, Connection conn) throws DataAccessException, WikiException {
 		try {
 			this.validateWikiGroup(group);
 			this.queryHandler().insertWikiGroup(group, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void addWikiUser(WikiUser user, Connection conn) throws DataAccessException, WikiException {
 		try {
 			this.validateWikiUser(user);
 			this.queryHandler().insertWikiUser(user, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public boolean authenticate(String username, String password) throws DataAccessException {
 		if (StringUtils.isBlank(password)) {
 			return false;
 		}
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			// password is stored encrypted, so encrypt password
 			String encryptedPassword = Encryption.encrypt(password);
 			return this.queryHandler().authenticateUser(username, encryptedPassword, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		} finally {
 			DatabaseConnection.closeConnection(conn);
 		}
 	}
 
 	/**
 	 * Utility method for retrieving a user display name.
 	 */
 	private String authorName(Integer authorId, String authorName) throws DataAccessException {
 		if (authorId != null) {
 			WikiUser user = this.lookupWikiUser(authorId);
 			authorName = user.getUsername();
 		}
 		return authorName;
 	}
 
 	/**
 	 * Given a virtual wiki and topic name, generate the key used for caching
 	 * the corresponding topic information.
 	 */
 	private String cacheTopicKey(String virtualWiki, Namespace namespace, String pageName) {
 		String topicName = namespace.getLabel(virtualWiki);
 		if (topicName.length() != 0) {
 			topicName += Namespace.SEPARATOR;
 		}
 		topicName += pageName;
 		return WikiCache.key(virtualWiki, topicName);
 	}
 
 	/**
 	 * Call this method whenever a topic is updated to update all relevant caches
 	 * for the topic.
 	 */
 	private void cacheTopicRefresh(Topic topic) {
 		String key = this.cacheTopicKey(topic.getVirtualWiki(), topic.getNamespace(), topic.getPageName());
 		// because some topics may be cached in a case-insensitive manner remove all possible
 		// cache keys for the topic, regardless of case
 		WikiCache.removeFromCacheCaseInsensitive(WikiBase.CACHE_PARSED_TOPIC_CONTENT, key);
 		WikiCache.removeFromCacheCaseInsensitive(CACHE_TOPIC_NAMES_BY_NAME, key);
 		WikiCache.removeFromCacheCaseInsensitive(CACHE_TOPICS_BY_NAME, key);
 		if (topic.getDeleteDate() == null) {
 			WikiCache.addToCache(CACHE_TOPIC_NAMES_BY_NAME, key, topic.getName());
 		}
 		WikiCache.addToCache(CACHE_TOPICS_BY_NAME, key, topic);
 		WikiCache.addToCache(CACHE_TOPICS_BY_ID, topic.getTopicId(), topic);
 	}
 
 	/**
 	 *
 	 */
 	public boolean canMoveTopic(Topic fromTopic, String destination) throws DataAccessException {
 		Topic toTopic = this.lookupTopic(fromTopic.getVirtualWiki(), destination, false);
 		if (toTopic == null || toTopic.getDeleteDate() != null) {
 			// destination doesn't exist or is deleted, so move is OK
 			return true;
 		}
 		if (!toTopic.getVirtualWiki().equals(fromTopic.getVirtualWiki())) {
 			// topics are on different virtual wikis (can happen with shared images) so move is not allowed
 			return false;
 		}
 		if (toTopic.getRedirectTo() != null && toTopic.getRedirectTo().equals(fromTopic.getName())) {
 			// source redirects to destination, so move is OK
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 *
 	 */
 	private static void checkLength(String value, int maxLength) throws WikiException {
 		if (value != null && value.length() > maxLength) {
 			throw new WikiException(new WikiMessage("error.fieldlength", value, Integer.valueOf(maxLength).toString()));
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void deleteInterwiki(Interwiki interwiki) throws DataAccessException {
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			this.queryHandler().deleteInterwiki(interwiki, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		} finally {
 			DatabaseConnection.closeConnection(conn);
 		}
 		WikiCache.removeAllFromCache(CACHE_INTERWIKI_LIST);
 	}
 
 	/**
 	 *
 	 */
 	private void deleteRecentChanges(Topic topic, Connection conn) throws DataAccessException {
 		try {
 			this.queryHandler().deleteRecentChanges(topic.getTopicId(), conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void deleteTopic(Topic topic, TopicVersion topicVersion) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			if (topicVersion != null) {
 				// delete old recent changes
 				deleteRecentChanges(topic, conn);
 			}
 			// update topic to indicate deleted, add delete topic version.  parser output
 			// should be empty since no links or categories to update.
 			ParserOutput parserOutput = new ParserOutput();
 			topic.setDeleteDate(new Timestamp(System.currentTimeMillis()));
 			this.writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks());
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	private void deleteTopicCategories(Topic topic, Connection conn) throws DataAccessException {
 		try {
 			this.queryHandler().deleteTopicCategories(topic.getTopicId(), conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void deleteTopicLinks(int topicId, Connection conn) throws DataAccessException {
 		try {
 			this.queryHandler().deleteTopicLinks(topicId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void deleteWatchlistEntry(int virtualWikiId, String topicName, int userId, Connection conn) throws DataAccessException {
 		try {
 			this.queryHandler().deleteWatchlistEntry(virtualWikiId, topicName, userId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void executeUpgradeQuery(String prop, Connection conn) throws SQLException {
 		this.queryHandler().executeUpgradeQuery(prop, conn);
 	}
 
 	/**
 	 *
 	 */
 	public void executeUpgradeUpdate(String prop, Connection conn) throws SQLException {
 		this.queryHandler().executeUpgradeUpdate(prop, conn);
 	}
 
 	/**
 	 * Determine the largest namespace ID for all current defined namespaces.
 	 */
 	private int findMaxNamespaceId() throws DataAccessException {
 		List<Namespace> namespaces = this.lookupNamespaces();
 		int namespaceEnd = 0;
 		for (Namespace namespace : namespaces) {
 			namespaceEnd = (namespace.getId() > namespaceEnd) ? namespace.getId() : namespaceEnd;
 		}
 		return namespaceEnd;
 	}
 
 	/**
 	 *
 	 */
 	public List<Category> getAllCategories(String virtualWiki, Pagination pagination) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			return this.queryHandler().getCategories(virtualWikiId, virtualWiki, pagination);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Role> getAllRoles() throws DataAccessException {
 		try {
 			return this.queryHandler().getRoles();
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<String> getAllTopicNames(String virtualWiki, boolean includeDeleted) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			return new ArrayList<String>(this.queryHandler().lookupTopicNames(virtualWikiId, includeDeleted, conn).values());
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		} finally {
 			DatabaseConnection.closeConnection(conn);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<WikiFileVersion> getAllWikiFileVersions(String virtualWiki, String topicName, boolean descending) throws DataAccessException {
 		WikiFile wikiFile = lookupWikiFile(virtualWiki, topicName);
 		if (wikiFile == null) {
 			throw new DataAccessException("No topic exists for " + virtualWiki + " / " + topicName);
 		}
 		try {
 			return this.queryHandler().getAllWikiFileVersions(wikiFile, descending);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<LogItem> getLogItems(String virtualWiki, int logType, Pagination pagination, boolean descending) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			return this.queryHandler().getLogItems(virtualWikiId, virtualWiki, logType, pagination, descending);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RecentChange> getRecentChanges(String virtualWiki, Pagination pagination, boolean descending) throws DataAccessException {
 		try {
 			return this.queryHandler().getRecentChanges(virtualWiki, pagination, descending);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RoleMap> getRoleMapByLogin(String loginFragment) throws DataAccessException {
 		try {
 			return this.queryHandler().getRoleMapByLogin(loginFragment);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RoleMap> getRoleMapByRole(String authority) throws DataAccessException {
 		// first check the cache
 		Element cacheElement = WikiCache.retrieveFromCache(CACHE_ROLE_MAP_GROUP, authority);
 		if (cacheElement != null) {
 			return (List<RoleMap>)cacheElement.getObjectValue();
 		}
 		// if not in the cache, go to the database
 		List<RoleMap> roleMapList = null;
 		try {
 			roleMapList = this.queryHandler().getRoleMapByRole(authority);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 		WikiCache.addToCache(CACHE_ROLE_MAP_GROUP, authority, roleMapList);
 		return roleMapList;
 	}
 
 	/**
 	 *
 	 */
 	public List<Role> getRoleMapGroup(String groupName) throws DataAccessException {
 		try {
 			return this.queryHandler().getRoleMapGroup(groupName);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RoleMap> getRoleMapGroups() throws DataAccessException {
 		try {
 			return this.queryHandler().getRoleMapGroups();
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Role> getRoleMapUser(String login) throws DataAccessException {
 		try {
 			return this.queryHandler().getRoleMapUser(login);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RecentChange> getTopicHistory(Topic topic, Pagination pagination, boolean descending) throws DataAccessException {
 		if (topic == null) {
 			return new ArrayList<RecentChange>();
 		}
 		try {
 			return this.queryHandler().getTopicHistory(topic.getTopicId(), pagination, descending, topic.getDeleted());
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<String> getTopicsAdmin(String virtualWiki, Pagination pagination) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			return this.queryHandler().getTopicsAdmin(virtualWikiId, pagination);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public Map<Object, UserBlock> getUserBlocks() throws DataAccessException {
 		// for performance reasons cache all active blocks.  in general there
 		// shouldn't be a huge number of active blocks at any given time, so
 		// rather than hit the database for every page request to verify whether
 		// or not the user is blocked it is far more efficient to cache the few
 		// active blocks and query against that cached list.
 		Element cacheElement = WikiCache.retrieveFromCache(CACHE_USER_BLOCKS_ACTIVE, CACHE_USER_BLOCKS_ACTIVE);
 		if (cacheElement != null) {
 			// note that due to caching some blocks may have expired, so the caller
 			// should be sure to check whether a result is still active or not
 			return (Map<Object, UserBlock>)cacheElement.getObjectValue();
 		}
 		Map<Object, UserBlock> userBlocks = new LinkedHashMap<Object, UserBlock>();
 		try {
 			Connection conn = DatabaseConnection.getConnection();
 			userBlocks = this.queryHandler().getUserBlocks(conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 		WikiCache.addToCache(CACHE_USER_BLOCKS_ACTIVE, CACHE_USER_BLOCKS_ACTIVE, userBlocks);
 		return userBlocks;
 	}
 
 	/**
 	 *
 	 */
 	public List<RecentChange> getUserContributions(String virtualWiki, String userString, Pagination pagination, boolean descending) throws DataAccessException {
 		try {
 			if (this.lookupWikiUser(userString) != null) {
 				return this.queryHandler().getUserContributionsByLogin(virtualWiki, userString, pagination, descending);
 			} else {
 				return this.queryHandler().getUserContributionsByUserDisplay(virtualWiki, userString, pagination, descending);
 			}
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 * Return a List of all VirtualWiki objects that exist for the Wiki.
 	 */
 	public List<VirtualWiki> getVirtualWikiList() throws DataAccessException {
 		Element cacheElement = WikiCache.retrieveFromCache(CACHE_VIRTUAL_WIKI_LIST, CACHE_VIRTUAL_WIKI_LIST);
 		if (cacheElement != null) {
 			return (List<VirtualWiki>)cacheElement.getObjectValue();
 		}
 		List<VirtualWiki> virtualWikis = new ArrayList<VirtualWiki>();
 		try {
 			Connection conn = DatabaseConnection.getConnection();
 			virtualWikis = this.queryHandler().getVirtualWikis(conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 		WikiCache.addToCache(CACHE_VIRTUAL_WIKI_LIST, CACHE_VIRTUAL_WIKI_LIST, virtualWikis);
 		return virtualWikis;
 	}
 
 	/**
 	 * Retrieve a watchlist containing a List of topic ids and topic
 	 * names that can be used to determine if a topic is in a user's current
 	 * watchlist.
 	 */
 	public Watchlist getWatchlist(String virtualWiki, int userId) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			List<String> watchedTopicNames = this.queryHandler().getWatchlist(virtualWikiId, userId);
 			return new Watchlist(virtualWiki, watchedTopicNames);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 * Retrieve a watchlist containing a List of RecentChanges objects
 	 * that can be used for display on the Special:Watchlist page.
 	 */
 	public List<RecentChange> getWatchlist(String virtualWiki, int userId, Pagination pagination) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			return this.queryHandler().getWatchlist(virtualWikiId, userId, pagination);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Category> lookupCategoryTopics(String virtualWiki, String categoryName) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			return this.queryHandler().lookupCategoryTopics(virtualWikiId, virtualWiki, categoryName);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public Map<String, String> lookupConfiguration() throws DataAccessException {
 		try {
 			return this.queryHandler().lookupConfiguration();
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public Interwiki lookupInterwiki(String interwikiPrefix) throws DataAccessException {
 		if (interwikiPrefix == null) {
 			return null;
 		}
 		List<Interwiki> interwikis = this.lookupInterwikis();
 		for (Interwiki interwiki : interwikis) {
 			if (interwiki.getInterwikiPrefix().equalsIgnoreCase(interwikiPrefix.trim())) {
 				// found a match, return it
 				return interwiki;
 			}
 		}
 		// no result found
 		return null;
 	}
 
 	/**
 	 *
 	 */
 	public List<Interwiki> lookupInterwikis() throws DataAccessException {
 		// first check the cache
 		Element cacheElement = WikiCache.retrieveFromCache(CACHE_INTERWIKI_LIST, CACHE_INTERWIKI_LIST);
 		if (cacheElement != null) {
 			return (List<Interwiki>)cacheElement.getObjectValue();
 		}
 		// if not in the cache, go to the database
 		List<Interwiki> interwikis = null;
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			interwikis = this.queryHandler().lookupInterwikis(conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		} finally {
 			DatabaseConnection.closeConnection(conn);
 		}
 		WikiCache.addToCache(CACHE_INTERWIKI_LIST, CACHE_INTERWIKI_LIST, interwikis);
 		return interwikis;
 	}
 
 	/**
 	 *
 	 */
 	public Namespace lookupNamespace(String virtualWiki, String namespaceString) throws DataAccessException {
 		if (namespaceString == null) {
 			return null;
 		}
 		List<Namespace> namespaces = this.lookupNamespaces();
 		for (Namespace namespace : namespaces) {
 			if (namespace.getLabel(virtualWiki).equalsIgnoreCase(namespaceString) || namespace.getDefaultLabel().equals(namespaceString)) {
 				// found a match, return it
 				return namespace;
 			}
 		}
 		// no result found
 		return null;
 	}
 
 	/**
 	 *
 	 */
 	public Namespace lookupNamespaceById(int namespaceId) throws DataAccessException {
 		List<Namespace> namespaces = this.lookupNamespaces();
 		for (Namespace namespace : namespaces) {
 			if (namespace.getId() != null && namespace.getId().intValue() == namespaceId) {
 				// found a match, return it
 				return namespace;
 			}
 		}
 		// no result found
 		return null;
 	}
 
 	/**
 	 *
 	 */
 	public List<Namespace> lookupNamespaces() throws DataAccessException {
 		// first check the cache
 		Element cacheElement = WikiCache.retrieveFromCache(CACHE_NAMESPACE_LIST, CACHE_NAMESPACE_LIST);
 		if (cacheElement != null) {
 			return (List<Namespace>)cacheElement.getObjectValue();
 		}
 		// if not in the cache, go to the database
 		List<Namespace> namespaces = null;
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			namespaces = this.queryHandler().lookupNamespaces(conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		} finally {
 			DatabaseConnection.closeConnection(conn);
 		}
 		WikiCache.addToCache(CACHE_NAMESPACE_LIST, CACHE_NAMESPACE_LIST, namespaces);
 		return namespaces;
 	}
 
 	/**
 	 *
 	 */
 	public Topic lookupTopic(String virtualWiki, String topicName, boolean deleteOK) throws DataAccessException {
 		return this.lookupTopic(virtualWiki, topicName, deleteOK, null);
 	}
 
 	/**
 	 *
 	 */
 	private Topic lookupTopic(String virtualWiki, String topicName, boolean deleteOK, Connection conn) throws DataAccessException {
 		if (StringUtils.isBlank(virtualWiki) || StringUtils.isBlank(topicName)) {
 			return null;
 		}
 		Namespace namespace = LinkUtil.retrieveTopicNamespace(virtualWiki, topicName);
 		String pageName = LinkUtil.retrieveTopicPageName(namespace, virtualWiki, topicName);
 		return this.lookupTopic(virtualWiki, namespace, pageName, deleteOK, conn);
 	}
 
 	/**
 	 *
 	 */
 	private Topic lookupTopic(String virtualWiki, Namespace namespace, String pageName, boolean deleteOK, Connection conn) throws DataAccessException {
 		long start = System.currentTimeMillis();
 		String key = this.cacheTopicKey(virtualWiki, namespace, pageName);
 		if (conn == null) {
 			// retrieve topic from the cache only if this call is not currently a part
 			// of a transaction to avoid retrieving data that might have been updated
 			// as part of this transaction and would thus now be out of date
 			Element cacheElement = WikiCache.retrieveFromCache(CACHE_TOPICS_BY_NAME, key);
 			if (cacheElement != null) {
 				Topic cacheTopic = (Topic)cacheElement.getObjectValue();
 				return (cacheTopic == null || (!deleteOK && cacheTopic.getDeleteDate() != null)) ? null : new Topic(cacheTopic);
 			}
 		}
 		boolean checkSharedVirtualWiki = this.useSharedVirtualWiki(virtualWiki, namespace);
 		String sharedVirtualWiki = Environment.getValue(Environment.PROP_SHARED_UPLOAD_VIRTUAL_WIKI);
 		if (conn == null && checkSharedVirtualWiki) {
 			String sharedKey = this.cacheTopicKey(sharedVirtualWiki, namespace, pageName);
 			Element cacheElement = WikiCache.retrieveFromCache(CACHE_TOPICS_BY_NAME, sharedKey);
 			if (cacheElement != null) {
 				Topic cacheTopic = (Topic)cacheElement.getObjectValue();
 				return (cacheTopic == null || (!deleteOK && cacheTopic.getDeleteDate() != null)) ? null : new Topic(cacheTopic);
 			}
 		}
 		Topic topic = null;
 		try {
 			int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 			topic = this.queryHandler().lookupTopic(virtualWikiId, virtualWiki, namespace, pageName, conn);
 			if (topic == null && Environment.getBooleanValue(Environment.PROP_PARSER_ALLOW_CAPITALIZATION)) {
 				String alternativePageName = (StringUtils.equals(pageName, StringUtils.capitalize(pageName))) ? StringUtils.lowerCase(pageName) : StringUtils.capitalize(pageName);
 				topic = this.queryHandler().lookupTopic(virtualWikiId, virtualWiki, namespace, alternativePageName, conn);
 			}
 			if (topic == null && checkSharedVirtualWiki) {
 				topic = this.lookupTopic(sharedVirtualWiki, namespace, pageName, deleteOK, conn);
 			}
 			if (conn == null) {
 				// add topic to the cache only if it is not currently a part of a transaction
 				// to avoid caching something that might need to be rolled back
 				Topic cacheTopic = (topic == null) ? null : new Topic(topic);
 				WikiCache.addToCache(CACHE_TOPICS_BY_NAME, key, cacheTopic);
 				// do not cache deleted topics
 				WikiCache.addToCache(CACHE_TOPIC_NAMES_BY_NAME, key, (cacheTopic == null || cacheTopic.getDeleteDate() != null) ? null : cacheTopic.getName());
 			}
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 		if (logger.isDebugEnabled()) {
 			long execution = (System.currentTimeMillis() - start);
 			if (execution > TIME_LIMIT_TOPIC_LOOKUP) {
 				logger.debug("Slow topic lookup for: " + Topic.buildTopicName(virtualWiki, namespace, pageName) + " (" + (execution / 1000.000) + " s)");
 			}
 		}
 		return (topic == null || (!deleteOK && topic.getDeleteDate() != null)) ? null : topic;
 	}
 
 	/**
 	 *
 	 */
 	public Topic lookupTopicById(String virtualWiki, int topicId) throws DataAccessException {
 		Element cacheElement = WikiCache.retrieveFromCache(CACHE_TOPICS_BY_ID, topicId);
 		if (cacheElement != null) {
 			return (Topic)cacheElement.getObjectValue();
 		}
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		Topic result = null;
 		try {
 			result = this.queryHandler().lookupTopicById(virtualWikiId, virtualWiki, topicId);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 		WikiCache.addToCache(CACHE_TOPICS_BY_ID, topicId, result);
 		return result;
 	}
 
 	/**
 	 * Return a count of all topics, including redirects, comments pages and templates,
 	 * currently available on the Wiki.  This method excludes deleted topics.
 	 *
 	 * @param virtualWiki The virtual wiki for which the total topic count is being returned
 	 *  for.
 	 */
 	public int lookupTopicCount(String virtualWiki, Integer namespaceId) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		int namespaceStart = (namespaceId != null) ? namespaceId : 0;
 		int namespaceEnd = (namespaceId != null) ? namespaceId : this.findMaxNamespaceId();
 		try {
 			return this.queryHandler().lookupTopicCount(virtualWikiId, namespaceStart, namespaceEnd);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public Map<Integer, String> lookupTopicByType(String virtualWiki, TopicType topicType1, TopicType topicType2, Integer namespaceId, Pagination pagination) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		int namespaceStart = (namespaceId != null) ? namespaceId : 0;
 		int namespaceEnd = (namespaceId != null) ? namespaceId : this.findMaxNamespaceId();
 		try {
 			return this.queryHandler().lookupTopicByType(virtualWikiId, topicType1, topicType2, namespaceStart, namespaceEnd, pagination);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public String lookupTopicName(String virtualWiki, String topicName) throws DataAccessException {
 		if (StringUtils.isBlank(virtualWiki) || StringUtils.isBlank(topicName)) {
 			return null;
 		}
 		Namespace namespace = LinkUtil.retrieveTopicNamespace(virtualWiki, topicName);
 		String pageName = LinkUtil.retrieveTopicPageName(namespace, virtualWiki, topicName);
 		return this.lookupTopicName(virtualWiki, namespace, pageName);
 	}
 
 	/**
 	 *
 	 */
 	private String lookupTopicName(String virtualWiki, Namespace namespace, String pageName) throws DataAccessException {
 		long start = System.currentTimeMillis();
 		String key = this.cacheTopicKey(virtualWiki, namespace, pageName);
 		Element cacheElement = WikiCache.retrieveFromCache(CACHE_TOPIC_NAMES_BY_NAME, key);
 		if (cacheElement != null) {
 			return (String)cacheElement.getObjectValue();
 		}
 		boolean checkSharedVirtualWiki = this.useSharedVirtualWiki(virtualWiki, namespace);
 		String sharedVirtualWiki = Environment.getValue(Environment.PROP_SHARED_UPLOAD_VIRTUAL_WIKI);
 		if (checkSharedVirtualWiki) {
 			String sharedKey = this.cacheTopicKey(sharedVirtualWiki, namespace, pageName);
 			cacheElement = WikiCache.retrieveFromCache(CACHE_TOPIC_NAMES_BY_NAME, sharedKey);
 			if (cacheElement != null) {
 				return (String)cacheElement.getObjectValue();
 			}
 		}
 		String topicName = null;
 		try {
 			int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 			topicName = this.queryHandler().lookupTopicName(virtualWikiId, virtualWiki, namespace, pageName);
 			if (topicName == null && checkSharedVirtualWiki) {
 				topicName = this.lookupTopicName(sharedVirtualWiki, namespace, pageName);
 			}
 			WikiCache.addToCache(CACHE_TOPIC_NAMES_BY_NAME, key, topicName);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 		if (logger.isDebugEnabled()) {
 			long execution = (System.currentTimeMillis() - start);
 			if (execution > TIME_LIMIT_TOPIC_LOOKUP) {
 				logger.debug("Slow topic existence lookup for: " + Topic.buildTopicName(virtualWiki, namespace, pageName) + " (" +  (execution / 1000.000) + " s)");
 			}
 		}
 		return topicName;
 	}
 
 	/**
 	 *
 	 */
 	public List<String> lookupTopicLinks(String virtualWiki, String topicName) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		Namespace namespace = LinkUtil.retrieveTopicNamespace(virtualWiki, topicName);
 		String pageName = LinkUtil.retrieveTopicPageName(namespace, virtualWiki, topicName);
 		// FIXE - link to records are always capitalized, which will cause problems for the
 		// rare case of two topics such as "eBay" and "EBay".
 		pageName = StringUtils.capitalize(pageName);
 		try {
 			return this.queryHandler().lookupTopicLinks(virtualWikiId, namespace, pageName);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<String> lookupTopicLinkOrphans(String virtualWiki, int namespaceId) throws DataAccessException {
 		// FIXME - caching needed
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			return this.queryHandler().lookupTopicLinkOrphans(virtualWikiId, namespaceId);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public TopicVersion lookupTopicVersion(int topicVersionId) throws DataAccessException {
 		Element cacheElement = WikiCache.retrieveFromCache(CACHE_TOPIC_VERSIONS, topicVersionId);
 		if (cacheElement != null) {
 			return (TopicVersion)cacheElement.getObjectValue();
 		}
 		TopicVersion topicVersion = null;
 		try {
 			topicVersion = this.queryHandler().lookupTopicVersion(topicVersionId);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 		WikiCache.addToCache(CACHE_TOPIC_VERSIONS, topicVersionId, topicVersion);
 		return topicVersion;
 	}
 
 	/**
 	 *
 	 */
 	public Integer lookupTopicVersionNextId(int topicVersionId) throws DataAccessException {
 		try {
 			return this.queryHandler().lookupTopicVersionNextId(topicVersionId);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public UserBlock lookupUserBlock(Integer wikiUserId, String ipAddress) throws DataAccessException {
 		Map<Object, UserBlock> userBlocks = this.getUserBlocks();
 		UserBlock userBlock = null;
 		if (wikiUserId != null) {
 			userBlock = userBlocks.get(wikiUserId);
 		}
 		if (userBlock == null && ipAddress != null) {
 			userBlock = userBlocks.get(ipAddress);
 		}
 		// verify that the block has not expired since being cached
 		return (userBlock != null && userBlock.isExpired()) ? null : userBlock;
 	}
 
 	/**
 	 *
 	 */
 	public VirtualWiki lookupVirtualWiki(String virtualWikiName) throws DataAccessException {
 		List<VirtualWiki> virtualWikis = this.getVirtualWikiList();
 		for (VirtualWiki virtualWiki : virtualWikis) {
 			if (virtualWiki.getName().equals(virtualWikiName)) {
 				// found a match, return it
 				return virtualWiki;
 			}
 		}
 		// no result found
 		return null;
 	}
 
 	/**
 	 *
 	 */
 	private int lookupVirtualWikiId(String virtualWikiName) throws DataAccessException {
 		VirtualWiki virtualWiki = this.lookupVirtualWiki(virtualWikiName);
 		return (virtualWiki == null) ? -1 : virtualWiki.getVirtualWikiId();
 	}
 
 	/**
 	 *
 	 */
 	public WikiFile lookupWikiFile(String virtualWiki, String topicName) throws DataAccessException {
 		if (StringUtils.isBlank(virtualWiki) || StringUtils.isBlank(topicName)) {
 			return null;
 		}
 		Namespace namespace = LinkUtil.retrieveTopicNamespace(virtualWiki, topicName);
 		String pageName = LinkUtil.retrieveTopicPageName(namespace, virtualWiki, topicName);
 		return this.lookupWikiFile(virtualWiki, namespace, pageName);
 	}
 
 	/**
 	 *
 	 */
 	private WikiFile lookupWikiFile(String virtualWiki, Namespace namespace, String pageName) throws DataAccessException {
 		Topic topic = this.lookupTopic(virtualWiki, namespace, pageName, false, null);
 		if (topic == null) {
 			return null;
 		}
 		try {
 			int virtualWikiId = this.lookupVirtualWikiId(topic.getVirtualWiki());
 			WikiFile wikiFile = this.queryHandler().lookupWikiFile(virtualWikiId, topic.getVirtualWiki(), topic.getTopicId());
 			if (wikiFile == null && this.useSharedVirtualWiki(topic.getVirtualWiki(), topic.getNamespace())) {
 				// this is a weird corner case.  if there is a shared virtual wiki
 				// then someone might have uploaded the image to the shared virtual
 				// wiki but then created a description on the non-shared image page,
 				// so check for a file on the shared virtual wiki.
 				String sharedVirtualWiki = Environment.getValue(Environment.PROP_SHARED_UPLOAD_VIRTUAL_WIKI);
 				wikiFile = this.lookupWikiFile(sharedVirtualWiki, namespace, pageName);
 			}
 			return wikiFile;
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 * Return a count of all wiki files currently available on the Wiki.  This
 	 * method excludes deleted files.
 	 *
 	 * @param virtualWiki The virtual wiki of the files being retrieved.
 	 */
 	public int lookupWikiFileCount(String virtualWiki) throws DataAccessException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		try {
 			return this.queryHandler().lookupWikiFileCount(virtualWikiId);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public WikiGroup lookupWikiGroup(String groupName) throws DataAccessException {
 		try {
 			return this.queryHandler().lookupWikiGroup(groupName);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public WikiUser lookupWikiUser(int userId) throws DataAccessException {
 		Element cacheElement = WikiCache.retrieveFromCache(CACHE_USER_BY_USER_ID, userId);
 		if (cacheElement != null) {
 			return (WikiUser)cacheElement.getObjectValue();
 		}
 		WikiUser user = null;
 		try {
 			user = this.queryHandler().lookupWikiUser(userId);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 		WikiCache.addToCache(CACHE_USER_BY_USER_ID, userId, user);
 		return user;
 	}
 
 	/**
 	 *
 	 */
 	public WikiUser lookupWikiUser(String username) throws DataAccessException {
 		Element cacheElement = WikiCache.retrieveFromCache(CACHE_USER_BY_USER_NAME, username);
 		if (cacheElement != null) {
 			return (WikiUser)cacheElement.getObjectValue();
 		}
 		WikiUser result = null;
 		try {
 			Connection conn = DatabaseConnection.getConnection();
 			int userId = this.queryHandler().lookupWikiUser(username, conn);
 			if (userId != -1) {
 				result = lookupWikiUser(userId);
 			}
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 		WikiCache.addToCache(CACHE_USER_BY_USER_NAME, username, result);
 		return result;
 	}
 
 	/**
 	 * Return a count of all wiki users.
 	 */
 	public int lookupWikiUserCount() throws DataAccessException {
 		try {
 			return this.queryHandler().lookupWikiUserCount();
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public String lookupWikiUserEncryptedPassword(String username) throws DataAccessException {
 		try {
 			return this.queryHandler().lookupWikiUserEncryptedPassword(username);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<String> lookupWikiUsers(Pagination pagination) throws DataAccessException {
 		try {
 			return this.queryHandler().lookupWikiUsers(pagination);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void moveTopic(Topic fromTopic, String destination, WikiUser user, String ipAddress, String moveComment) throws DataAccessException, WikiException {
 		// set up the version record to record the topic move
 		TopicVersion fromVersion = new TopicVersion(user, ipAddress, moveComment, fromTopic.getTopicContent(), 0);
 		fromVersion.setEditType(TopicVersion.EDIT_MOVE);
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			if (!this.canMoveTopic(fromTopic, destination)) {
 				throw new WikiException(new WikiMessage("move.exception.destinationexists", destination));
 			}
 			Topic toTopic = this.lookupTopic(fromTopic.getVirtualWiki(), destination, false, conn);
 			boolean detinationExistsFlag = (toTopic != null && toTopic.getDeleteDate() == null);
 			if (detinationExistsFlag) {
 				// if the target topic is a redirect to the source topic then the
 				// target must first be deleted.
 				this.deleteTopic(toTopic, null);
 			}
 			// first rename the source topic with the new destination name
 			String fromTopicName = fromTopic.getName();
 			fromTopic.setName(destination);
 			// only one version needs to create a recent change entry, so do not create a log entry
 			// for the "from" version
 			fromVersion.setRecentChangeAllowed(false);
 			// handle categories
 			ParserOutput fromParserOutput = ParserUtil.parserOutput(fromTopic.getTopicContent(), fromTopic.getVirtualWiki(), fromTopic.getName());
 			writeTopic(fromTopic, fromVersion, fromParserOutput.getCategories(), fromParserOutput.getLinks());
 			// now either create a new topic that is a redirect with the
 			// source topic's old name, or else undelete the new topic and
 			// rename.
 			if (detinationExistsFlag) {
 				// target topic was deleted, so rename and undelete
 				toTopic.setName(fromTopicName);
 				writeTopic(toTopic, null, null, null);
 				this.undeleteTopic(toTopic, null);
 			} else {
 				// create a new topic that redirects to the destination
 				toTopic = new Topic(fromTopic);
 				toTopic.setTopicId(-1);
 				toTopic.setName(fromTopicName);
 			}
 			String content = ParserUtil.parserRedirectContent(destination);
 			toTopic.setRedirectTo(destination);
 			toTopic.setTopicType(TopicType.REDIRECT);
 			toTopic.setTopicContent(content);
 			TopicVersion toVersion = fromVersion;
 			toVersion.setTopicVersionId(-1);
 			toVersion.setVersionContent(content);
 			toVersion.setRecentChangeAllowed(true);
 			ParserOutput toParserOutput = ParserUtil.parserOutput(toTopic.getTopicContent(), toTopic.getVirtualWiki(), toTopic.getName());
 			writeTopic(toTopic, toVersion, toParserOutput.getCategories(), toParserOutput.getLinks());
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (ParserException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void orderTopicVersions(Topic topic, List<Integer> topicVersionIdList) throws DataAccessException {
 		try {
 			int virtualWikiId = this.lookupVirtualWikiId(topic.getVirtualWiki());
 			this.queryHandler().orderTopicVersions(topic, virtualWikiId, topicVersionIdList);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 		this.cacheTopicRefresh(topic);
 	}
 
 	/**
 	 *
 	 */
 	public void purgeTopicVersion(String virtualWiki, int topicVersionId, WikiUser user, String ipAddress) throws DataAccessException, WikiException {
 		// 1. get the topic version record.  if no such record exists
 		// throw an exception.
 		TopicVersion topicVersion = this.lookupTopicVersion(topicVersionId);
 		if (topicVersion == null) {
 			throw new WikiException(new WikiMessage("purge.error.noversion", Integer.toString(topicVersionId)));
 		}
 		// 2. get the current version's previous_topic_version_id
 		// record.  if there is no such record get the topic version
 		// with the current version as its previous_topic_version_id.
 		// if there is still no such record throw an exception.
 		Topic topic = this.lookupTopicById(virtualWiki, topicVersion.getTopicId());
 		Integer previousTopicVersionId = topicVersion.getPreviousTopicVersionId();
 		Integer nextTopicVersionId = this.lookupTopicVersionNextId(topicVersionId);
 		if (previousTopicVersionId == null && nextTopicVersionId == null) {
 			throw new WikiException(new WikiMessage("purge.error.onlyversion", Integer.toString(topicVersionId), topic.getName()));
 		}
 		Integer replacementTopicVersionId = (previousTopicVersionId != null) ? previousTopicVersionId : nextTopicVersionId;
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			// 3. get a reference to any topic which has this topic as its
 			// current_version_id, and update with the value from #2.
 			if (topicVersionId == topic.getCurrentVersionId().intValue()) {
 				topic.setCurrentVersionId(replacementTopicVersionId);
 				this.updateTopic(topic, conn);
 			}
 			// 4. if there is a topic version with this version as its
 			// previous_topic_version_id update it with the value from #2
 			if (nextTopicVersionId != null) {
 				TopicVersion nextTopicVersion = this.lookupTopicVersion(nextTopicVersionId);
 				nextTopicVersion.setPreviousTopicVersionId(topicVersion.getPreviousTopicVersionId());
 				this.queryHandler().updateTopicVersion(nextTopicVersion, conn);
 			}
 			// 5. delete the topic version record from all tables
 			this.queryHandler().deleteTopicVersion(topicVersionId, topicVersion.getPreviousTopicVersionId(), conn);
 			// 6. create a log record
 			LogItem logItem = LogItem.initLogItemPurge(topic, topicVersion, user, ipAddress);
 			this.addLogItem(logItem, conn);
 			RecentChange change = RecentChange.initRecentChange(logItem);
 			this.addRecentChange(change, conn);
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	protected QueryHandler queryHandler() {
 		return this.queryHandler;
 	}
 
 	/**
 	 *
 	 */
 	public void reloadLogItems() throws DataAccessException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			List<VirtualWiki> virtualWikis = this.getVirtualWikiList();
 			for (VirtualWiki virtualWiki : virtualWikis) {
 				this.queryHandler().reloadLogItems(virtualWiki.getVirtualWikiId(), conn);
 			}
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void reloadRecentChanges() throws DataAccessException {
 		int limit = Environment.getIntValue(Environment.PROP_MAX_RECENT_CHANGES);
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			this.queryHandler().reloadRecentChanges(conn, limit);
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void setup(Locale locale, WikiUser user, String username, String encryptedPassword) throws DataAccessException, WikiException {
 		WikiDatabase.initialize();
 		// determine if database exists
 		Connection conn = null;
 		Statement stmt = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.createStatement();
 			stmt.executeQuery(WikiDatabase.getExistenceValidationQuery());
 			return;
 		} catch (SQLException e) {
 			// database not yet set up
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt);
 		}
 		WikiDatabase.setup(locale, user, username, encryptedPassword);
 	}
 
 	/**
 	 *
 	 */
 	public void setupSpecialPages(Locale locale, WikiUser user, VirtualWiki virtualWiki) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			// create the default topics
 			WikiDatabase.setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STARTING_POINTS, user, false);
 			WikiDatabase.setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_LEFT_MENU, user, true);
 			WikiDatabase.setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_BOTTOM_AREA, user, true);
 			WikiDatabase.setupSpecialPage(locale, virtualWiki.getName(), WikiBase.SPECIAL_PAGE_STYLESHEET, user, true);
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void undeleteTopic(Topic topic, TopicVersion topicVersion) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			// update topic to indicate deleted, add delete topic version.  if
 			// topic has categories or other metadata then parser document is
 			// also needed.
 			ParserOutput parserOutput = ParserUtil.parserOutput(topic.getTopicContent(), topic.getVirtualWiki(), topic.getName());
 			topic.setDeleteDate(null);
 			this.writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks());
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (ParserException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void updateSpecialPage(Locale locale, String virtualWiki, String topicName, String userDisplay) throws DataAccessException, WikiException {
 		logger.info("Updating special page " + virtualWiki + " / " + topicName);
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			String contents = WikiDatabase.readSpecialPage(locale, topicName);
 			Topic topic = this.lookupTopic(virtualWiki, topicName, false, conn);
 			int charactersChanged = StringUtils.length(contents) - StringUtils.length(topic.getTopicContent());
 			topic.setTopicContent(contents);
 			// FIXME - hard coding
 			TopicVersion topicVersion = new TopicVersion(null, userDisplay, "Automatically updated by system upgrade", contents, charactersChanged);
 			ParserOutput parserOutput = ParserUtil.parserOutput(topic.getTopicContent(), virtualWiki, topicName);
 			writeTopic(topic, topicVersion, parserOutput.getCategories(), parserOutput.getLinks());
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (ParserException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (IOException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	private void updateTopic(Topic topic, Connection conn) throws DataAccessException, WikiException {
 		int virtualWikiId = this.lookupVirtualWikiId(topic.getVirtualWiki());
 		this.validateTopic(topic);
 		try {
 			this.queryHandler().updateTopic(topic, virtualWikiId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void updateUserBlock(UserBlock userBlock, Connection conn) throws DataAccessException, WikiException {
 		this.validateUserBlock(userBlock);
 		try {
 			this.queryHandler().updateUserBlock(userBlock, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void updateUserDetails(WikiUserDetails userDetails, Connection conn) throws DataAccessException, WikiException {
 		this.validateUserDetails(userDetails);
 		try {
 			this.queryHandler().updateUserDetails(userDetails, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void updateVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws DataAccessException, WikiException {
 		this.validateVirtualWiki(virtualWiki);
 		try {
 			this.queryHandler().updateVirtualWiki(virtualWiki, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void updateWikiFile(WikiFile wikiFile, Connection conn) throws DataAccessException, WikiException {
 		int virtualWikiId = this.lookupVirtualWikiId(wikiFile.getVirtualWiki());
 		this.validateWikiFile(wikiFile);
 		try {
 			this.queryHandler().updateWikiFile(wikiFile, virtualWikiId, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void updateWikiGroup(WikiGroup group, Connection conn) throws DataAccessException, WikiException {
 		this.validateWikiGroup(group);
 		try {
 			this.queryHandler().updateWikiGroup(group, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 *
 	 */
 	private void updateWikiUser(WikiUser user, Connection conn) throws DataAccessException, WikiException {
 		this.validateWikiUser(user);
 		try {
 			this.queryHandler().updateWikiUser(user, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		}
 	}
 
 	/**
 	 * Utility method to determine whether to check a shared virtual wiki when
 	 * performing a topic lookup.
 	 *
 	 * @param virtualWiki The current virtual wiki being used for a lookup.
 	 * @param namespace The namespace for the current topic being retrieved.
 	 */
 	private boolean useSharedVirtualWiki(String virtualWiki, Namespace namespace) {
 		String sharedVirtualWiki = Environment.getValue(Environment.PROP_SHARED_UPLOAD_VIRTUAL_WIKI);
 		if (!StringUtils.isBlank(sharedVirtualWiki) && !StringUtils.equals(virtualWiki, sharedVirtualWiki)) {
 			return (namespace.getId().equals(Namespace.FILE_ID) || namespace.getId().equals(Namespace.MEDIA_ID));
 		}
 		return false;
 	}
 
 	/**
 	 *
 	 */
 	protected void validateAuthority(String role) throws WikiException {
 		checkLength(role, 30);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateCategory(Category category) throws WikiException {
 		checkLength(category.getName(), 200);
 		checkLength(category.getSortKey(), 200);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateConfiguration(Map<String, String> configuration) throws WikiException {
 		for (String key : configuration.keySet()) {
 			checkLength(key, 50);
 			checkLength(configuration.get(key), 500);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected void validateLogItem(LogItem logItem) throws WikiException {
 		checkLength(logItem.getUserDisplayName(), 200);
 		checkLength(logItem.getLogParamString(), 500);
 		logItem.setLogComment(StringUtils.substring(logItem.getLogComment(), 0, 200));
 	}
 
 	/**
 	 *
 	 */
 	protected void validateNamespace(Namespace mainNamespace, Namespace commentsNamespace) throws WikiException {
 		checkLength(mainNamespace.getDefaultLabel(), 200);
 		if (commentsNamespace != null) {
 			checkLength(commentsNamespace.getDefaultLabel(), 200);
 			if (commentsNamespace.getMainNamespace() == null || !commentsNamespace.getMainNamespace().equals(mainNamespace)) {
 				throw new WikiException(new WikiMessage("error.commentsnamespace", commentsNamespace.getDefaultLabel(), mainNamespace.getDefaultLabel()));
 			}
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected void validateNamespaceTranslation(Namespace namespace, String virtualWiki) throws WikiException {
 		checkLength(namespace.getLabel(virtualWiki), 200);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateRecentChange(RecentChange change) throws WikiException {
 		checkLength(change.getTopicName(), 200);
 		checkLength(change.getAuthorName(), 200);
 		checkLength(change.getVirtualWiki(), 100);
 		change.setChangeComment(StringUtils.substring(change.getChangeComment(), 0, 200));
 		checkLength(change.getParamString(), 500);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateRole(Role role) throws WikiException {
 		checkLength(role.getAuthority(), 30);
 		role.setDescription(StringUtils.substring(role.getDescription(), 0, 200));
 	}
 
 	/**
 	 *
 	 */
 	protected void validateTopic(Topic topic) throws WikiException {
 		checkLength(topic.getName(), 200);
 		checkLength(topic.getRedirectTo(), 200);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateTopicVersion(TopicVersion topicVersion) throws WikiException {
 		checkLength(topicVersion.getAuthorDisplay(), 100);
 		checkLength(topicVersion.getVersionParamString(), 500);
 		topicVersion.setEditComment(StringUtils.substring(topicVersion.getEditComment(), 0, 200));
 	}
 
 	/**
 	 *
 	 */
 	protected void validateUserBlock(UserBlock userBlock) throws WikiException {
 		checkLength(userBlock.getBlockReason(), 200);
 		checkLength(userBlock.getUnblockReason(), 200);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateUserDetails(WikiUserDetails userDetails) throws WikiException {
 		checkLength(userDetails.getUsername(), 100);
 		// do not throw exception containing password info
 		if (userDetails.getPassword() != null && userDetails.getPassword().length() > 100) {
 			throw new WikiException(new WikiMessage("error.fieldlength", "-", "100"));
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected void validateVirtualWiki(VirtualWiki virtualWiki) throws WikiException {
 		checkLength(virtualWiki.getName(), 100);
 		checkLength(virtualWiki.getRootTopicName(), 200);
 		checkLength(virtualWiki.getLogoImageUrl(), 200);
 		checkLength(virtualWiki.getMetaDescription(), 500);
 		checkLength(virtualWiki.getSiteName(), 200);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateWatchlistEntry(String topicName) throws WikiException {
 		checkLength(topicName, 200);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateWikiFile(WikiFile wikiFile) throws WikiException {
 		checkLength(wikiFile.getFileName(), 200);
 		checkLength(wikiFile.getUrl(), 200);
 		checkLength(wikiFile.getMimeType(), 100);
 	}
 
 	/**
 	 *
 	 */
 	protected void validateWikiFileVersion(WikiFileVersion wikiFileVersion) throws WikiException {
 		checkLength(wikiFileVersion.getUrl(), 200);
 		checkLength(wikiFileVersion.getMimeType(), 100);
 		checkLength(wikiFileVersion.getAuthorDisplay(), 100);
 		wikiFileVersion.setUploadComment(StringUtils.substring(wikiFileVersion.getUploadComment(), 0, 200));
 	}
 
 	/**
 	 *
 	 */
 	protected void validateWikiGroup(WikiGroup group) throws WikiException {
 		checkLength(group.getName(), 30);
 		group.setDescription(StringUtils.substring(group.getDescription(), 0, 200));
 	}
 
 	/**
 	 *
 	 */
 	protected void validateWikiUser(WikiUser user) throws WikiException {
 		checkLength(user.getUsername(), 100);
 		checkLength(user.getDisplayName(), 100);
 		checkLength(user.getCreateIpAddress(), 39);
 		checkLength(user.getLastLoginIpAddress(), 39);
 		checkLength(user.getDefaultLocale(), 8);
 		checkLength(user.getEmail(), 100);
 		checkLength(user.getEditor(), 50);
 		checkLength(user.getSignature(), 255);
 	}
 
 	/**
 	 *
 	 */
 	public void writeConfiguration(Map<String, String> configuration) throws DataAccessException, WikiException {
 		this.validateConfiguration(configuration);
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			this.queryHandler().updateConfiguration(configuration, conn);
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void writeFile(WikiFile wikiFile, WikiFileVersion wikiFileVersion) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			WikiUtil.validateTopicName(wikiFile.getVirtualWiki(), wikiFile.getFileName(), false);
 			if (wikiFile.getFileId() <= 0) {
 				addWikiFile(wikiFile, conn);
 			} else {
 				updateWikiFile(wikiFile, conn);
 			}
 			wikiFileVersion.setFileId(wikiFile.getFileId());
 			// write version
 			addWikiFileVersion(wikiFileVersion, conn);
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void writeInterwiki(Interwiki interwiki) throws DataAccessException, WikiException {
 		interwiki.validate();
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			this.queryHandler().deleteInterwiki(interwiki, conn);
 			this.queryHandler().insertInterwiki(interwiki, conn);
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		}
 		DatabaseConnection.commit(status);
 		WikiCache.removeAllFromCache(CACHE_INTERWIKI_LIST);
 	}
 
 	/**
 	 *
 	 */
 	public void writeNamespace(Namespace mainNamespace, Namespace commentsNamespace) throws DataAccessException, WikiException {
 		this.validateNamespace(mainNamespace, commentsNamespace);
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			this.queryHandler().updateNamespace(mainNamespace, commentsNamespace, conn);
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		}
 		DatabaseConnection.commit(status);
 		WikiCache.removeAllFromCache(CACHE_NAMESPACE_LIST);
 	}
 
 	/**
 	 *
 	 */
 	public void writeNamespaceTranslations(List<Namespace> namespaces, String virtualWiki) throws DataAccessException, WikiException {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		for (Namespace namespace : namespaces) {
 			this.validateNamespaceTranslation(namespace, virtualWiki);
 		}
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			this.queryHandler().updateNamespaceTranslations(namespaces, virtualWiki, virtualWikiId, conn);
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		}
 		DatabaseConnection.commit(status);
 		WikiCache.removeAllFromCache(CACHE_NAMESPACE_LIST);
 	}
 
 	/**
 	 *
 	 */
 	public void writeRole(Role role, boolean update) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			this.validateRole(role);
 			if (update) {
 				this.queryHandler().updateRole(role, conn);
 			} else {
 				this.queryHandler().insertRole(role, conn);
 			}
 			// FIXME - add caching
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void writeRoleMapGroup(int groupId, List<String> roles) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			this.queryHandler().deleteGroupAuthorities(groupId, conn);
 			for (String authority : roles) {
 				this.validateAuthority(authority);
 				this.queryHandler().insertGroupAuthority(groupId, authority, conn);
 			}
 			// flush the cache
 			WikiCache.removeAllFromCache(CACHE_ROLE_MAP_GROUP);
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void writeRoleMapUser(String username, List<String> roles) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			this.queryHandler().deleteUserAuthorities(username, conn);
 			for (String authority : roles) {
 				this.validateAuthority(authority);
 				this.queryHandler().insertUserAuthority(username, authority, conn);
 			}
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 * Commit changes to a topic (and its version) to the database or
 	 * filesystem.
 	 *
 	 * @param topic The topic object that is to be committed.  If the topic
 	 *  id is empty or less than zero then the topic is added, otherwise an
 	 *  update is performed.
 	 * @param topicVersion The version associated with the topic that is
 	 *  being added.  This parameter should never be null UNLESS the change is
 	 *  not user visible, such as when deleting a topic temporarily during
 	 *  page moves.
 	 * @param categories A mapping of categories and their associated sort keys (if any)
 	 *  for all categories that are associated with the current topic.
 	 * @param links A List of all topic names that are linked to from the
 	 *  current topic.
 	 */
 	public void writeTopic(Topic topic, TopicVersion topicVersion, LinkedHashMap<String, String> categories, List<String> links) throws DataAccessException, WikiException {
 		long start = System.currentTimeMillis();
 		WikiUtil.validateTopicName(topic.getVirtualWiki(), topic.getName(), false);
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			if (topic.getTopicId() <= 0) {
 				// create the initial topic record
 				addTopic(topic, conn);
 			} else if (topicVersion == null) {
 				// if there is no version record then update the topic.  if there is a version
 				// record then the topic will be updated AFTER the version record is created.
 				this.updateTopic(topic, conn);
 			}
 			if (topicVersion != null) {
 				// write version
 				addTopicVersion(topic, topicVersion, conn);
 				// update the topic AFTER creating the version so that the current_topic_version_id parameter is set properly
 				this.updateTopic(topic, conn);
 				String authorName = this.authorName(topicVersion.getAuthorId(), topicVersion.getAuthorDisplay());
 				LogItem logItem = LogItem.initLogItem(topic, topicVersion, authorName);
 				RecentChange change = null;
 				if (logItem != null) {
 					this.addLogItem(logItem, conn);
 					change = RecentChange.initRecentChange(logItem);
 				} else {
 					change = RecentChange.initRecentChange(topic, topicVersion, authorName);
 				}
 				if (topicVersion.isRecentChangeAllowed()) {
 					this.addRecentChange(change, conn);
 				}
 			}
 			if (categories != null) {
 				// add / remove categories associated with the topic
 				this.deleteTopicCategories(topic, conn);
 				if (topic.getDeleteDate() == null && !categories.isEmpty()) {
 					List<Category> categoryList = new ArrayList<Category>();
 					for (String categoryName : categories.keySet()) {
 						Category category = new Category();
 						category.setName(categoryName);
 						category.setSortKey(categories.get(categoryName));
 						category.setVirtualWiki(topic.getVirtualWiki());
 						category.setChildTopicName(topic.getName());
 						categoryList.add(category);
 					}
 					this.addCategories(categoryList, topic.getTopicId(), conn);
 				}
 			}
 			if (links != null) {
 				// add / remove links associated with the topic
 				this.deleteTopicLinks(topic.getTopicId(), conn);
 				if (topic.getDeleteDate() == null && !links.isEmpty()) {
 					this.addTopicLinks(links, topic.getVirtualWiki(), topic.getTopicId(), conn);
 				}
 			}
 			if (topicVersion != null) {
 				// topic version is only null during changes that aren't user visible
 				WikiBase.getSearchEngine().updateInIndex(topic);
 			}
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 		// update the cache AFTER the commit
 		this.cacheTopicRefresh(topic);
 		logger.debug("Wrote topic " + topic.getName() + " with params [categories is null: " + (categories == null) + "] / [links is null: " + (links == null) + "] in " + ((System.currentTimeMillis() - start) / 1000.000) + " s.");
 	}
 
 	/**
 	 *
 	 */
 	public void writeTopicVersion(Topic topic, TopicVersion topicVersion) throws DataAccessException, WikiException {
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			this.addTopicVersion(topic, topicVersion, conn);
 		} catch (SQLException e) {
 			throw new DataAccessException(e);
 		} finally {
 			DatabaseConnection.closeConnection(conn);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void writeUserBlock(UserBlock userBlock) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			if (userBlock.getBlockId() <= 0) {
 				this.addUserBlock(userBlock, conn);
 			} else {
 				this.updateUserBlock(userBlock, conn);
 			}
 			// FIXME - reconsider this approach of separate entries for every virtual wiki
 			List<VirtualWiki> virtualWikis = this.getVirtualWikiList();
 			for (VirtualWiki virtualWiki : virtualWikis) {
 				LogItem logItem = LogItem.initLogItem(userBlock, virtualWiki.getName());
 				this.addLogItem(logItem, conn);
 				RecentChange change = RecentChange.initRecentChange(logItem);
 				this.addRecentChange(change, conn);
 			}
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 		// flush the cache
 		WikiCache.removeAllFromCache(CACHE_USER_BLOCKS_ACTIVE);
 	}
 
 	/**
 	 *
 	 */
 	public void writeVirtualWiki(VirtualWiki virtualWiki) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			WikiUtil.validateVirtualWikiName(virtualWiki.getName());
 			if (virtualWiki.getVirtualWikiId() <= 0) {
 				this.addVirtualWiki(virtualWiki, conn);
 			} else {
 				this.updateVirtualWiki(virtualWiki, conn);
 			}
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 		// flush the cache
 		WikiCache.removeAllFromCache(CACHE_VIRTUAL_WIKI_LIST);
 	}
 
 	/**
 	 *
 	 */
 	public void writeWatchlistEntry(Watchlist watchlist, String virtualWiki, String topicName, int userId) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 			String article = WikiUtil.extractTopicLink(virtualWiki, topicName);
 			String comments = WikiUtil.extractCommentsLink(virtualWiki, topicName);
 			if (watchlist.containsTopic(topicName)) {
 				// remove from watchlist
 				this.deleteWatchlistEntry(virtualWikiId, article, userId, conn);
 				this.deleteWatchlistEntry(virtualWikiId, comments, userId, conn);
 				watchlist.remove(article);
 				watchlist.remove(comments);
 			} else {
 				// add to watchlist
 				this.addWatchlistEntry(virtualWikiId, article, userId, conn);
 				this.addWatchlistEntry(virtualWikiId, comments, userId, conn);
 				watchlist.add(article);
 				watchlist.add(comments);
 			}
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void writeWikiGroup(WikiGroup group) throws DataAccessException, WikiException {
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			if (group.getGroupId() <= 0) {
 				this.addWikiGroup(group, conn);
 			} else {
 				this.updateWikiGroup(group, conn);
 			}
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public void writeWikiUser(WikiUser user, String username, String encryptedPassword) throws DataAccessException, WikiException {
 		WikiUtil.validateUserName(user.getUsername());
 		TransactionStatus status = null;
 		try {
 			status = DatabaseConnection.startTransaction();
 			Connection conn = DatabaseConnection.getConnection();
 			if (user.getUserId() <= 0) {
 				WikiUserDetails userDetails = new WikiUserDetails(username, encryptedPassword);
 				this.addUserDetails(userDetails, conn);
 				this.addWikiUser(user, conn);
 				// add all users to the registered user group
 				this.addGroupMember(user.getUsername(), WikiBase.getGroupRegisteredUser().getGroupId(), conn);
 				// FIXME - reconsider this approach of separate entries for every virtual wiki
 				List<VirtualWiki> virtualWikis = this.getVirtualWikiList();
 				for (VirtualWiki virtualWiki : virtualWikis) {
 					LogItem logItem = LogItem.initLogItem(user, virtualWiki.getName());
 					this.addLogItem(logItem, conn);
 					RecentChange change = RecentChange.initRecentChange(logItem);
 					this.addRecentChange(change, conn);
 				}
 			} else {
 				if (!StringUtils.isBlank(encryptedPassword)) {
 					WikiUserDetails userDetails = new WikiUserDetails(username, encryptedPassword);
 					this.updateUserDetails(userDetails, conn);
 				}
 				this.updateWikiUser(user, conn);
 			}
 		} catch (DataAccessException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (SQLException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw new DataAccessException(e);
 		} catch (WikiException e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 		// update the cache AFTER the commit
 		WikiCache.addToCache(CACHE_USER_BY_USER_ID, user.getUserId(), user);
 		WikiCache.addToCache(CACHE_USER_BY_USER_NAME, user.getUsername(), user);
 	}
 }

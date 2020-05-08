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
 package org.jamwiki.persistency.db;
 
 import java.sql.Connection;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.Locale;
 import java.util.Vector;
 import org.apache.log4j.Logger;
 import org.jamwiki.Environment;
 import org.jamwiki.persistency.PersistencyHandler;
 import org.jamwiki.model.Category;
 import org.jamwiki.model.RecentChange;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.WikiFile;
 import org.jamwiki.model.WikiFileVersion;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.Encryption;
 
 /**
  *
  */
 public class DatabaseHandler extends PersistencyHandler {
 
 	public static final String DB_TYPE_ORACLE = "oracle";
 	public static final String DB_TYPE_MYSQL = "mysql";
 	public static final String DB_TYPE_POSTGRES = "postgres";
 	private static final String INIT_SCRIPT_ANSI = "create_ansi.sql";
 	private static final String INIT_SCRIPT_ORACLE = "create_oracle.sql";
 	private static final Logger logger = Logger.getLogger(DatabaseHandler.class);
 	private static QueryHandler queryHandler = null;
 	private boolean initialized = false;
 
 	/**
 	 *
 	 */
 	public DatabaseHandler() {
 		if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_POSTGRES)) {
 			DatabaseHandler.queryHandler = new PostgresQueryHandler();
 		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_MYSQL)) {
 			DatabaseHandler.queryHandler = new MySqlQueryHandler();
 		} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(DB_TYPE_ORACLE)) {
 			DatabaseHandler.queryHandler = new OracleQueryHandler();
 		} else {
 			DatabaseHandler.queryHandler = new DefaultQueryHandler();
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected void addCategory(Category category, Object[] params) throws Exception {
 		Connection conn = (Connection)params[0];
		Topic childTopic = lookupTopic(category.getVirtualWiki(), category.getChildTopicName());
 		int childTopicId = childTopic.getTopicId();
 		DatabaseHandler.queryHandler.insertCategory(childTopicId, category.getName(), category.getSortKey(), conn);
 	}
 
 	/**
 	 *
 	 */
 	protected void addRecentChange(RecentChange change, Object[] params) throws Exception {
 		int virtualWikiId = this.lookupVirtualWikiId(change.getVirtualWiki());
 		Connection conn = (Connection)params[0];
 		DatabaseHandler.queryHandler.insertRecentChange(change, virtualWikiId, conn);
 	}
 
 	/**
 	 *
 	 */
 	protected void addTopic(Topic topic, Object[] params) throws Exception {
 		int virtualWikiId = this.lookupVirtualWikiId(topic.getVirtualWiki());
 		Connection conn = (Connection)params[0];
 		if (topic.getTopicId() < 1) {
 			int topicId = DatabaseHandler.queryHandler.nextTopicId(conn);
 			topic.setTopicId(topicId);
 		}
 		DatabaseHandler.queryHandler.insertTopic(topic, virtualWikiId, conn);
 	}
 
 	/**
 	 *
 	 */
 	protected void addTopicVersion(String virtualWiki, String topicName, TopicVersion topicVersion, Object[] params) throws Exception {
 		Connection conn = (Connection)params[0];
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
 	protected void addVirtualWiki(VirtualWiki virtualWiki, Object[] params) throws Exception {
 		Connection conn = (Connection)params[0];
 		if (virtualWiki.getVirtualWikiId() < 1) {
 			int virtualWikiId = DatabaseHandler.queryHandler.nextVirtualWikiId(conn);
 			virtualWiki.setVirtualWikiId(virtualWikiId);
 		}
 		DatabaseHandler.queryHandler.insertVirtualWiki(virtualWiki, conn);
 	}
 
 	/**
 	 *
 	 */
 	protected void addWikiFile(String topicName, WikiFile wikiFile, Object[] params) throws Exception {
 		Connection conn = (Connection)params[0];
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
 	protected void addWikiFileVersion(String virtualWiki, String wikiFileName, WikiFileVersion wikiFileVersion, Object[] params) throws Exception {
 		Connection conn = (Connection)params[0];
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
 	protected void addWikiUser(WikiUser user, Object[] params) throws Exception {
 		Connection conn = (Connection)params[0];
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
 	protected void deleteRecentChanges(Topic topic, Object params[]) throws Exception {
 		Connection conn = (Connection)params[0];
 		DatabaseHandler.queryHandler.deleteRecentChanges(topic.getTopicId(), conn);
 	}
 
 	/**
 	 *
 	 */
 	protected void deleteTopicCategories(Topic topic, Object params[]) throws Exception {
 		Connection conn = (Connection)params[0];
 		DatabaseHandler.queryHandler.deleteTopicCategories(topic.getTopicId(), conn);
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
 	protected Collection getAllTopicVersions(String virtualWiki, String topicName, boolean descending) throws Exception {
 		Vector all = new Vector();
 		Topic topic = lookupTopic(virtualWiki, topicName);
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
 	protected Collection getAllWikiFileTopicNames(String virtualWiki) throws Exception {
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
 	protected Collection getCategories(String virtualWiki) throws Exception {
 		Collection results = new Vector();
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		WikiResultSet rs = DatabaseHandler.queryHandler.getCategories(virtualWikiId);
 		while (rs.next()) {
 			Category category = new Category();
 			category.setName(rs.getString("category_name"));
 			category.setChildTopicName(rs.getString("topic_name"));
 			category.setVirtualWiki(virtualWiki);
 			category.setSortKey(rs.getString("sort_key"));
 			results.add(category);
 		}
 		return results;
 	}
 
 	/**
 	 *
 	 */
 	public Collection getRecentChanges(String virtualWiki, int num, boolean descending) throws Exception {
 		Vector all = new Vector();
 		WikiResultSet rs = DatabaseHandler.queryHandler.getRecentChanges(virtualWiki, num, descending);
 		while (rs.next()) {
 			RecentChange change = initRecentChange(rs);
 			all.add(change);
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	public Collection getUserContributions(String virtualWiki, String userString, int num, boolean descending) throws Exception {
 		Collection all = new Vector();
 		WikiResultSet rs = DatabaseHandler.queryHandler.getUserContributions(virtualWiki, userString, num, descending);
 		while (rs.next()) {
 			RecentChange change = initRecentChange(rs);
 			all.add(change);
 		}
 		return all;
 	}
 
 	/**
 	 *
 	 */
 	protected void handleErrors(Object[] params) {
 		if (params == null) return;
 		try {
 			logger.warn("Rolling back database transactions");
 			Connection conn = (Connection)params[0];
 			conn.rollback();
 		} catch (Exception e) {
 			logger.error("Unable to rollback connection", e);
 		}
 	}
 
 	/**
 	 * Set up database tables, and then call the parent method to initialize
 	 * default values.
 	 */
 	public void initialize(Locale locale, WikiUser user) throws Exception {
 		if (this.isInitialized()) {
 			logger.warn("Attempt to initialize when initialization already complete");
 			return;
 		}
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			// set up tables
 			DatabaseHandler.queryHandler.createTables(conn);
 		} catch (Exception e) {
 			logger.error("Unable to set up database tables", e);
 			// clean up anything that might have been created
 			DatabaseHandler.queryHandler.dropTables(conn);
 		} finally {
 			if (conn != null) DatabaseConnection.closeConnection(conn);
 		}
 		super.initialize(locale, user);
 		this.initialized = true;
 	}
 
 	/**
 	 *
 	 */
 	protected Object[] initParams() throws Exception {
 		// add a connection to the params array.  BE SURE TO RELEASE IT!
 		Connection conn = DatabaseConnection.getConnection();
 		conn.setAutoCommit(false);
 		Object[] params = new Object[1];
 		params[0] = conn;
 		return params;
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
 			logger.error("Failure while initializing recent change", e);
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
 			topic.setDeleted(rs.getInt("topic_deleted") != 0);
 			topic.setTopicType(rs.getInt("topic_type"));
 			return topic;
 		} catch (Exception e) {
 			logger.error("Failure while initializing topic", e);
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
 			logger.error("Failure while initializing topic version", e);
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
 			logger.error("Failure while initializing virtual wiki", e);
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
 			wikiFile.setDeleted(rs.getInt("file_deleted") != 0);
 			wikiFile.setMimeType(rs.getString("mime_type"));
 			wikiFile.setFileSize(rs.getInt("file_size"));
 			return wikiFile;
 		} catch (Exception e) {
 			logger.error("Failure while initializing file", e);
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
 			logger.error("Failure while initializing wiki file version", e);
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
 			logger.error("Failure while initializing user", e);
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
 			logger.warn("Database handler not initialized: " + e.getMessage());
 			return false;
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected void loadVirtualWikiHashes() throws Exception {
 		PersistencyHandler.virtualWikiNameHash = new Hashtable();
 		PersistencyHandler.virtualWikiIdHash = new Hashtable();
 		try {
 			WikiResultSet rs = DatabaseHandler.queryHandler.getVirtualWikis();
 			while (rs.next()) {
 				VirtualWiki virtualWiki = initVirtualWiki(rs);
 				PersistencyHandler.virtualWikiNameHash.put(virtualWiki.getName(), virtualWiki);
 				PersistencyHandler.virtualWikiIdHash.put(new Integer(virtualWiki.getVirtualWikiId()), virtualWiki);
 			}
 		} catch (Exception e) {
 			logger.error("Failure while loading virtual wiki hashtable ", e);
 			// if there is an error make sure the hashtable is reset since it wasn't
 			// properly initialized
 			PersistencyHandler.virtualWikiNameHash = null;
 			PersistencyHandler.virtualWikiIdHash = null;
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
 		Topic topic = lookupTopic(virtualWiki, topicName);
 		if (topic == null) return null;
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupLastTopicVersion(topic);
 		if (rs.size() == 0) return null;
 		int topicVersionId = rs.getInt("topic_version_id");
 		return lookupTopicVersion(virtualWiki, topicName, topicVersionId);
 	}
 
 	/**
 	 *
 	 */
 	public TopicVersion lookupLastTopicVersion(String virtualWiki, String topicName, Object[] params) throws Exception {
 		Connection conn = (Connection)params[0];
 		Topic topic = lookupTopic(virtualWiki, topicName, params);
 		if (topic == null) return null;
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupLastTopicVersion(topic, conn);
 		if (rs.size() == 0) return null;
 		int topicVersionId = rs.getInt("topic_version_id");
 		return lookupTopicVersion(virtualWiki, topicName, topicVersionId, params);
 	}
 
 	/**
 	 *
 	 */
 	public Topic lookupTopic(String virtualWiki, String topicName) throws Exception {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupTopic(virtualWikiId, topicName);
 		if (rs.size() == 0) return null;
 		return initTopic(rs);
 	}
 
 	/**
 	 *
 	 */
 	public Topic lookupTopic(String virtualWiki, String topicName, Object[] params) throws Exception {
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		Connection conn = (Connection)params[0];
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupTopic(virtualWikiId, topicName, conn);
 		if (rs.size() == 0) return null;
 		return initTopic(rs);
 	}
 
 	/**
 	 *
 	 */
 	public Collection lookupTopicByType(String virtualWiki, int topicType) throws Exception {
 		Vector results = new Vector();
 		int virtualWikiId = this.lookupVirtualWikiId(virtualWiki);
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupTopicByType(virtualWikiId, topicType);
 		while (rs.next()) {
 			results.add(rs.getString("topic_name"));
 		}
 		return results;
 	}
 
 	/**
 	 *
 	 */
 	public TopicVersion lookupTopicVersion(String virtualWiki, String topicName, int topicVersionId) throws Exception {
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupTopicVersion(topicVersionId);
 		if (rs.size() == 0) return null;
 		return initTopicVersion(rs);
 	}
 
 	/**
 	 *
 	 */
 	public TopicVersion lookupTopicVersion(String virtualWiki, String topicName, int topicVersionId, Object[] params) throws Exception {
 		Connection conn = (Connection)params[0];
 		WikiResultSet rs = DatabaseHandler.queryHandler.lookupTopicVersion(topicVersionId, conn);
 		if (rs.size() == 0) return null;
 		return initTopicVersion(rs);
 	}
 
 	/**
 	 *
 	 */
 	public WikiFile lookupWikiFile(String virtualWiki, String topicName) throws Exception {
 		Topic topic = lookupTopic(virtualWiki, topicName);
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
 	protected WikiUser lookupWikiUser(int userId, Object[] params) throws Exception {
 		Connection conn = (Connection)params[0];
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
 	 * This method causes all existing data to be deleted from the Wiki.  Use only
 	 * when totally re-initializing a system.  To reiterate: CALLING THIS METHOD WILL
 	 * DELETE ALL WIKI DATA!
 	 */
 	protected void purgeData(Object[] params) throws Exception {
 		Connection conn = (Connection)params[0];
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
 	 *
 	 */
 	protected void releaseParams(Object[] params) throws Exception {
 		if (params == null) return;
 		Connection conn = (Connection)params[0];
 		try {
 			conn.commit();
 		} finally {
 			if (conn != null) DatabaseConnection.closeConnection(conn);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected void reloadRecentChanges(Object[] params) throws Exception {
 		Connection conn = (Connection)params[0];
 		DatabaseHandler.queryHandler.reloadRecentChanges(conn);
 	}
 
 	/**
 	 *
 	 */
 	protected void updateTopic(Topic topic, Object[] params) throws Exception {
 		int virtualWikiId = this.lookupVirtualWikiId(topic.getVirtualWiki());
 		Connection conn = (Connection)params[0];
 		DatabaseHandler.queryHandler.updateTopic(topic, virtualWikiId, conn);
 	}
 
 	/**
 	 *
 	 */
 	protected void updateVirtualWiki(VirtualWiki virtualWiki, Object[] params) throws Exception {
 		Connection conn = (Connection)params[0];
 		DatabaseHandler.queryHandler.updateVirtualWiki(virtualWiki, conn);
 	}
 
 	/**
 	 *
 	 */
 	protected void updateWikiFile(String topicName, WikiFile wikiFile, Object[] params) throws Exception {
 		int virtualWikiId = this.lookupVirtualWikiId(wikiFile.getVirtualWiki());
 		Connection conn = (Connection)params[0];
 		DatabaseHandler.queryHandler.updateWikiFile(wikiFile, virtualWikiId, conn);
 	}
 
 	/**
 	 *
 	 */
 	protected void updateWikiUser(WikiUser user, Object[] params) throws Exception {
 		Connection conn = (Connection)params[0];
 		DatabaseHandler.queryHandler.updateWikiUser(user, conn);
 		// FIXME - may be in LDAP
 		DatabaseHandler.queryHandler.updateWikiUserInfo(user, conn);
 	}
 }

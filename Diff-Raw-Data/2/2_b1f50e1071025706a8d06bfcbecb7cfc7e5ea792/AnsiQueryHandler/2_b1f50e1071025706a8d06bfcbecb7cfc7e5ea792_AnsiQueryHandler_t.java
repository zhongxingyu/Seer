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
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Properties;
 import org.apache.commons.lang.StringUtils;
 import org.jamwiki.Environment;
 import org.jamwiki.authentication.RoleImpl;
 import org.jamwiki.authentication.WikiUserDetails;
 import org.jamwiki.model.Category;
 import org.jamwiki.model.LogItem;
 import org.jamwiki.model.RecentChange;
 import org.jamwiki.model.Role;
 import org.jamwiki.model.RoleMap;
 import org.jamwiki.model.Topic;
 import org.jamwiki.model.TopicVersion;
 import org.jamwiki.model.VirtualWiki;
 import org.jamwiki.model.WikiFile;
 import org.jamwiki.model.WikiFileVersion;
 import org.jamwiki.model.WikiGroup;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.Pagination;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * Default implementation of the QueryHandler implementation for retrieving, inserting,
  * and updating data in the database.  This method uses ANSI SQL and should therefore
  * work with any fully ANSI-compliant database.
  */
 public class AnsiQueryHandler implements QueryHandler {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(AnsiQueryHandler.class.getName());
 	protected static final String SQL_PROPERTY_FILE_NAME = "sql.ansi.properties";
 
 	protected static String STATEMENT_CONNECTION_VALIDATION_QUERY = null;
 	protected static String STATEMENT_CREATE_AUTHORITIES_TABLE = null;
 	protected static String STATEMENT_CREATE_CATEGORY_TABLE = null;
 	protected static String STATEMENT_CREATE_GROUP_AUTHORITIES_TABLE = null;
 	protected static String STATEMENT_CREATE_GROUP_MEMBERS_TABLE = null;
 	protected static String STATEMENT_CREATE_GROUP_TABLE = null;
 	protected static String STATEMENT_CREATE_LOG_TABLE = null;
 	protected static String STATEMENT_CREATE_RECENT_CHANGE_TABLE = null;
 	protected static String STATEMENT_CREATE_ROLE_TABLE = null;
 	protected static String STATEMENT_CREATE_TOPIC_CURRENT_VERSION_CONSTRAINT = null;
 	protected static String STATEMENT_CREATE_TOPIC_TABLE = null;
 	protected static String STATEMENT_CREATE_TOPIC_VERSION_TABLE = null;
 	protected static String STATEMENT_CREATE_USERS_TABLE = null;
 	protected static String STATEMENT_CREATE_VIRTUAL_WIKI_TABLE = null;
 	protected static String STATEMENT_CREATE_WATCHLIST_TABLE = null;
 	protected static String STATEMENT_CREATE_WIKI_FILE_TABLE = null;
 	protected static String STATEMENT_CREATE_WIKI_FILE_VERSION_TABLE = null;
 	protected static String STATEMENT_CREATE_WIKI_USER_TABLE = null;
 	protected static String STATEMENT_CREATE_WIKI_USER_LOGIN_INDEX = null;
 	protected static String STATEMENT_DELETE_AUTHORITIES = null;
 	protected static String STATEMENT_DELETE_GROUP_AUTHORITIES = null;
 	protected static String STATEMENT_DELETE_LOG_ITEMS = null;
 	protected static String STATEMENT_DELETE_RECENT_CHANGES = null;
 	protected static String STATEMENT_DELETE_RECENT_CHANGES_TOPIC = null;
 	protected static String STATEMENT_DELETE_TOPIC_CATEGORIES = null;
 	protected static String STATEMENT_DELETE_WATCHLIST_ENTRY = null;
 	protected static String STATEMENT_DROP_AUTHORITIES_TABLE = null;
 	protected static String STATEMENT_DROP_CATEGORY_TABLE = null;
 	protected static String STATEMENT_DROP_GROUP_AUTHORITIES_TABLE = null;
 	protected static String STATEMENT_DROP_GROUP_MEMBERS_TABLE = null;
 	protected static String STATEMENT_DROP_GROUP_TABLE = null;
 	protected static String STATEMENT_DROP_LOG_TABLE = null;
 	protected static String STATEMENT_DROP_RECENT_CHANGE_TABLE = null;
 	protected static String STATEMENT_DROP_ROLE_TABLE = null;
 	protected static String STATEMENT_DROP_TOPIC_CURRENT_VERSION_CONSTRAINT = null;
 	protected static String STATEMENT_DROP_TOPIC_TABLE = null;
 	protected static String STATEMENT_DROP_TOPIC_VERSION_TABLE = null;
 	protected static String STATEMENT_DROP_USERS_TABLE = null;
 	protected static String STATEMENT_DROP_VIRTUAL_WIKI_TABLE = null;
 	protected static String STATEMENT_DROP_WATCHLIST_TABLE = null;
 	protected static String STATEMENT_DROP_WIKI_FILE_TABLE = null;
 	protected static String STATEMENT_DROP_WIKI_FILE_VERSION_TABLE = null;
 	protected static String STATEMENT_DROP_WIKI_USER_TABLE = null;
 	protected static String STATEMENT_DROP_WIKI_USER_LOGIN_INDEX = null;
 	protected static String STATEMENT_INSERT_AUTHORITY = null;
 	protected static String STATEMENT_INSERT_CATEGORY = null;
 	protected static String STATEMENT_INSERT_GROUP = null;
 	protected static String STATEMENT_INSERT_GROUP_AUTO_INCREMENT = null;
 	protected static String STATEMENT_INSERT_GROUP_AUTHORITY = null;
 	protected static String STATEMENT_INSERT_GROUP_MEMBER = null;
 	protected static String STATEMENT_INSERT_GROUP_MEMBER_AUTO_INCREMENT = null;
 	protected static String STATEMENT_INSERT_LOG_ITEM = null;
 	protected static String STATEMENT_INSERT_LOG_ITEMS_BY_TOPIC_VERSION_TYPE = null;
 	protected static String STATEMENT_INSERT_LOG_ITEMS_IMPORT = null;
 	protected static String STATEMENT_INSERT_LOG_ITEMS_MOVE = null;
 	protected static String STATEMENT_INSERT_LOG_ITEMS_UPLOAD = null;
 	protected static String STATEMENT_INSERT_LOG_ITEMS_USER = null;
 	protected static String STATEMENT_INSERT_RECENT_CHANGE = null;
 	protected static String STATEMENT_INSERT_RECENT_CHANGES_LOGS = null;
 	protected static String STATEMENT_INSERT_RECENT_CHANGES_VERSIONS = null;
 	protected static String STATEMENT_INSERT_ROLE = null;
 	protected static String STATEMENT_INSERT_TOPIC = null;
 	protected static String STATEMENT_INSERT_TOPIC_AUTO_INCREMENT = null;
 	protected static String STATEMENT_INSERT_TOPIC_VERSION = null;
 	protected static String STATEMENT_INSERT_TOPIC_VERSION_AUTO_INCREMENT = null;
 	protected static String STATEMENT_INSERT_USER = null;
 	protected static String STATEMENT_INSERT_VIRTUAL_WIKI = null;
 	protected static String STATEMENT_INSERT_VIRTUAL_WIKI_AUTO_INCREMENT = null;
 	protected static String STATEMENT_INSERT_WATCHLIST_ENTRY = null;
 	protected static String STATEMENT_INSERT_WIKI_FILE = null;
 	protected static String STATEMENT_INSERT_WIKI_FILE_AUTO_INCREMENT = null;
 	protected static String STATEMENT_INSERT_WIKI_FILE_VERSION = null;
 	protected static String STATEMENT_INSERT_WIKI_FILE_VERSION_AUTO_INCREMENT = null;
 	protected static String STATEMENT_INSERT_WIKI_USER = null;
 	protected static String STATEMENT_INSERT_WIKI_USER_AUTO_INCREMENT = null;
 	protected static String STATEMENT_SELECT_AUTHORITIES_AUTHORITY = null;
 	protected static String STATEMENT_SELECT_AUTHORITIES_LOGIN = null;
 	protected static String STATEMENT_SELECT_AUTHORITIES_USER = null;
 	protected static String STATEMENT_SELECT_CATEGORIES = null;
 	protected static String STATEMENT_SELECT_CATEGORY_TOPICS = null;
 	protected static String STATEMENT_SELECT_GROUP = null;
 	protected static String STATEMENT_SELECT_GROUP_AUTHORITIES = null;
 	protected static String STATEMENT_SELECT_GROUPS_AUTHORITIES = null;
 	protected static String STATEMENT_SELECT_GROUP_MEMBERS_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_GROUP_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_LOG_ITEMS = null;
 	protected static String STATEMENT_SELECT_LOG_ITEMS_BY_TYPE = null;
 	protected static String STATEMENT_SELECT_RECENT_CHANGES = null;
 	protected static String STATEMENT_SELECT_ROLES = null;
 	protected static String STATEMENT_SELECT_TOPIC_BY_TYPE = null;
 	protected static String STATEMENT_SELECT_TOPIC_COUNT = null;
 	protected static String STATEMENT_SELECT_TOPIC = null;
 	protected static String STATEMENT_SELECT_TOPIC_HISTORY = null;
 	protected static String STATEMENT_SELECT_TOPIC_LOWER = null;
 	protected static String STATEMENT_SELECT_TOPICS = null;
 	protected static String STATEMENT_SELECT_TOPICS_ADMIN = null;
 	protected static String STATEMENT_SELECT_TOPIC_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_TOPIC_VERSION = null;
 	protected static String STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_USERS_AUTHENTICATION = null;
 	protected static String STATEMENT_SELECT_VIRTUAL_WIKIS = null;
 	protected static String STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_WATCHLIST = null;
 	protected static String STATEMENT_SELECT_WATCHLIST_CHANGES = null;
 	protected static String STATEMENT_SELECT_WIKI_FILE = null;
 	protected static String STATEMENT_SELECT_WIKI_FILE_COUNT = null;
 	protected static String STATEMENT_SELECT_WIKI_FILE_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_WIKI_FILE_VERSION_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_WIKI_FILE_VERSIONS = null;
 	protected static String STATEMENT_SELECT_WIKI_USER = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_COUNT = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_DETAILS_PASSWORD = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_LOGIN = null;
 	protected static String STATEMENT_SELECT_WIKI_USER_SEQUENCE = null;
 	protected static String STATEMENT_SELECT_WIKI_USERS = null;
 	protected static String STATEMENT_UPDATE_GROUP = null;
 	protected static String STATEMENT_UPDATE_ROLE = null;
 	protected static String STATEMENT_UPDATE_TOPIC = null;
 	protected static String STATEMENT_UPDATE_USER = null;
 	protected static String STATEMENT_UPDATE_VIRTUAL_WIKI = null;
 	protected static String STATEMENT_UPDATE_WIKI_FILE = null;
 	protected static String STATEMENT_UPDATE_WIKI_USER = null;
 	private Properties props = null;
 
 	/**
 	 *
 	 */
 	protected AnsiQueryHandler() {
 		props = Environment.loadProperties(SQL_PROPERTY_FILE_NAME);
 		this.init(props);
 	}
 
 	/**
 	 *
 	 */
 	public boolean authenticateUser(String username, String encryptedPassword, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_SELECT_USERS_AUTHENTICATION);
 			stmt.setString(1, username);
 			stmt.setString(2, encryptedPassword);
 			return (stmt.executeQuery().next());
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public boolean autoIncrementPrimaryKeys() {
 		return false;
 	}
 
 	/**
 	 *
 	 */
 	public String connectionValidationQuery() {
 		return STATEMENT_CONNECTION_VALIDATION_QUERY;
 	}
 
 	/**
 	 *
 	 */
 	public void createTables(Connection conn) throws SQLException {
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_VIRTUAL_WIKI_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_USERS_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_USER_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_USER_LOGIN_INDEX, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_VERSION_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_TOPIC_CURRENT_VERSION_CONSTRAINT, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_FILE_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WIKI_FILE_VERSION_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_CATEGORY_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_GROUP_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_GROUP_MEMBERS_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_ROLE_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_AUTHORITIES_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_GROUP_AUTHORITIES_TABLE, conn);
		DatabaseConnection.executeUpdate(STATEMENT_CREATE_LOG_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_RECENT_CHANGE_TABLE, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_CREATE_WATCHLIST_TABLE, conn);
 	}
 
 	/**
 	 *
 	 */
 	public void deleteGroupAuthorities(int groupId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_DELETE_GROUP_AUTHORITIES);
 			stmt.setInt(1, groupId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void deleteRecentChanges(int topicId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_DELETE_RECENT_CHANGES_TOPIC);
 			stmt.setInt(1, topicId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void deleteTopicCategories(int childTopicId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_DELETE_TOPIC_CATEGORIES);
 			stmt.setInt(1, childTopicId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void deleteUserAuthorities(String username, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_DELETE_AUTHORITIES);
 			stmt.setString(1, username);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void deleteWatchlistEntry(int virtualWikiId, String topicName, int userId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_DELETE_WATCHLIST_ENTRY);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setString(2, topicName);
 			stmt.setInt(3, userId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void dropTables(Connection conn) {
 		// note that this method is called during creation failures, so be careful to
 		// catch errors that might result from a partial failure during install.  also
 		// note that the coding style violation here is intentional since it makes the
 		// actual work of the method more obvious.
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_WATCHLIST_TABLE, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_RECENT_CHANGE_TABLE, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_LOG_TABLE, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_GROUP_AUTHORITIES_TABLE, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_AUTHORITIES_TABLE, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_ROLE_TABLE, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_GROUP_MEMBERS_TABLE, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_GROUP_TABLE, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_CATEGORY_TABLE, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_WIKI_FILE_VERSION_TABLE, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_WIKI_FILE_TABLE, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_TOPIC_CURRENT_VERSION_CONSTRAINT, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_TOPIC_VERSION_TABLE, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_TOPIC_TABLE, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_WIKI_USER_LOGIN_INDEX, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_WIKI_USER_TABLE, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_USERS_TABLE, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 		try {
 			DatabaseConnection.executeUpdate(STATEMENT_DROP_VIRTUAL_WIKI_TABLE, conn);
 		} catch (SQLException e) { logger.severe(e.getMessage()); }
 	}
 
 	/**
 	 *
 	 */
 	public void executeUpgradeQuery(String prop, Connection conn) throws SQLException {
 		String sql = this.props.getProperty(prop);
 		if (sql == null) {
 			throw new SQLException("No property found for " + prop);
 		}
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(sql);
 			stmt.executeQuery();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void executeUpgradeUpdate(String prop, Connection conn) throws SQLException {
 		String sql = this.props.getProperty(prop);
 		if (sql == null) {
 			throw new SQLException("No property found for " + prop);
 		}
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(sql);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 * Return a simple query, that if successfully run indicates that JAMWiki
 	 * tables have been initialized in the database.
 	 *
 	 * @return Returns a simple query that, if successfully run, indicates
 	 *  that JAMWiki tables have been set up in the database.
 	 */
 	public String existenceValidationQuery() {
 		return STATEMENT_SELECT_VIRTUAL_WIKIS;
 	}
 
 	/**
 	 *
 	 */
 	public List<String> getAllTopicNames(int virtualWikiId) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPICS);
 			stmt.setInt(1, virtualWikiId);
 			rs = stmt.executeQuery();
 			List<String> topicNames = new ArrayList<String>();
 			while (rs.next()) {
 				topicNames.add(rs.getString("topic_name"));
 			}
 			return topicNames;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<WikiFileVersion> getAllWikiFileVersions(WikiFile wikiFile, boolean descending) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_FILE_VERSIONS);
 			// FIXME - sort order ignored
 			stmt.setInt(1, wikiFile.getFileId());
 			rs = stmt.executeQuery();
 			List<WikiFileVersion> fileVersions = new ArrayList<WikiFileVersion>();
 			while (rs.next()) {
 				fileVersions.add(this.initWikiFileVersion(rs));
 			}
 			return fileVersions;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Category> getCategories(int virtualWikiId, String virtualWikiName, Pagination pagination) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_CATEGORIES);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setInt(2, pagination.getNumResults());
 			stmt.setInt(3, pagination.getOffset());
 			rs = stmt.executeQuery();
 			List<Category> results = new ArrayList<Category>();
 			while (rs.next()) {
 				Category category = new Category();
 				category.setName(rs.getString("category_name"));
 				// child topic name not initialized since it is not needed
 				category.setVirtualWiki(virtualWikiName);
 				category.setSortKey(rs.getString("sort_key"));
 				// topic type not initialized since it is not needed
 				results.add(category);
 			}
 			return results;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<LogItem> getLogItems(int virtualWikiId, String virtualWikiName, int logType, Pagination pagination, boolean descending) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		int index = 1;
 		List<LogItem> logItems = new ArrayList<LogItem>();
 		try {
 			conn = DatabaseConnection.getConnection();
 			if (logType == -1) {
 				stmt = conn.prepareStatement(STATEMENT_SELECT_LOG_ITEMS);
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_SELECT_LOG_ITEMS_BY_TYPE);
 				stmt.setInt(index++, logType);
 			}
 			stmt.setInt(index++, virtualWikiId);
 			stmt.setInt(index++, pagination.getNumResults());
 			stmt.setInt(index++, pagination.getOffset());
 			// FIXME - sort order ignored
 			rs = stmt.executeQuery();
 			while (rs.next()) {
 				logItems.add(this.initLogItem(rs, virtualWikiName));
 			}
 			return logItems;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RecentChange> getRecentChanges(String virtualWiki, Pagination pagination, boolean descending) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_RECENT_CHANGES);
 			stmt.setString(1, virtualWiki);
 			stmt.setInt(2, pagination.getNumResults());
 			stmt.setInt(3, pagination.getOffset());
 			// FIXME - sort order ignored
 			rs = stmt.executeQuery();
 			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
 			while (rs.next()) {
 				recentChanges.add(this.initRecentChange(rs));
 			}
 			return recentChanges;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RoleMap> getRoleMapByLogin(String loginFragment) throws SQLException {
 		if (StringUtils.isBlank(loginFragment)) {
 			return new ArrayList<RoleMap>();
 		}
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_AUTHORITIES_LOGIN);
 			loginFragment = '%' + loginFragment.toLowerCase() + '%';
 			stmt.setString(1, loginFragment);
 			rs = stmt.executeQuery();
 			LinkedHashMap<Integer, RoleMap> roleMaps = new LinkedHashMap<Integer, RoleMap>();
 			while (rs.next()) {
 				Integer userId = rs.getInt("wiki_user_id");
 				RoleMap roleMap = new RoleMap();
 				if (roleMaps.containsKey(userId)) {
 					roleMap = roleMaps.get(userId);
 				} else {
 					roleMap.setUserId(userId);
 					roleMap.setUserLogin(rs.getString("username"));
 				}
 				roleMap.addRole(rs.getString("authority"));
 				roleMaps.put(userId, roleMap);
 			}
 			return new ArrayList<RoleMap>(roleMaps.values());
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RoleMap> getRoleMapByRole(String authority) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_AUTHORITIES_AUTHORITY);
 			stmt.setString(1, authority);
 			stmt.setString(2, authority);
 			rs = stmt.executeQuery();
 			LinkedHashMap<String, RoleMap> roleMaps = new LinkedHashMap<String, RoleMap>();
 			while (rs.next()) {
 				int userId = rs.getInt("wiki_user_id");
 				int groupId = rs.getInt("group_id");
 				RoleMap roleMap = new RoleMap();
 				String key = userId + "|" + groupId;
 				if (roleMaps.containsKey(key)) {
 					roleMap = roleMaps.get(key);
 				} else {
 					if (userId > 0) {
 						roleMap.setUserId(userId);
 						roleMap.setUserLogin(rs.getString("username"));
 					}
 					if (groupId > 0) {
 						roleMap.setGroupId(groupId);
 						roleMap.setGroupName(rs.getString("group_name"));
 					}
 				}
 				roleMap.addRole(rs.getString("authority"));
 				roleMaps.put(key, roleMap);
 			}
 			return new ArrayList<RoleMap>(roleMaps.values());
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Role> getRoleMapGroup(String groupName) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_GROUP_AUTHORITIES);
 			stmt.setString(1, groupName);
 			rs = stmt.executeQuery();
 			List<Role> roles = new ArrayList<Role>();
 			while (rs.next()) {
 				roles.add(this.initRole(rs));
 			}
 			return roles;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RoleMap> getRoleMapGroups() throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_GROUPS_AUTHORITIES);
 			rs = stmt.executeQuery();
 			LinkedHashMap<Integer, RoleMap> roleMaps = new LinkedHashMap<Integer, RoleMap>();
 			while (rs.next()) {
 				Integer groupId = rs.getInt("group_id");
 				RoleMap roleMap = new RoleMap();
 				if (roleMaps.containsKey(groupId)) {
 					roleMap = roleMaps.get(groupId);
 				} else {
 					roleMap.setGroupId(groupId);
 					roleMap.setGroupName(rs.getString("group_name"));
 				}
 				roleMap.addRole(rs.getString("authority"));
 				roleMaps.put(groupId, roleMap);
 			}
 			return new ArrayList<RoleMap>(roleMaps.values());
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Role> getRoleMapUser(String login) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_AUTHORITIES_USER);
 			stmt.setString(1, login);
 			rs = stmt.executeQuery();
 			List<Role> roles = new ArrayList<Role>();
 			while (rs.next()) {
 				roles.add(this.initRole(rs));
 			}
 			return roles;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Role> getRoles() throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_ROLES);
 			rs = stmt.executeQuery();
 			List<Role> roles = new ArrayList<Role>();
 			while (rs.next()) {
 				roles.add(this.initRole(rs));
 			}
 			return roles;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RecentChange> getTopicHistory(int topicId, Pagination pagination, boolean descending) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_HISTORY);
 			stmt.setInt(1, topicId);
 			stmt.setInt(2, pagination.getNumResults());
 			stmt.setInt(3, pagination.getOffset());
 			// FIXME - sort order ignored
 			rs = stmt.executeQuery();
 			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
 			while (rs.next()) {
 				recentChanges.add(this.initRecentChange(rs));
 			}
 			return recentChanges;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<String> getTopicsAdmin(int virtualWikiId, Pagination pagination) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPICS_ADMIN);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setInt(2, pagination.getNumResults());
 			stmt.setInt(3, pagination.getOffset());
 			rs = stmt.executeQuery();
 			List<String> results = new ArrayList<String>();
 			while (rs.next()) {
 				results.add(rs.getString("topic_name"));
 			}
 			return results;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RecentChange> getUserContributionsByLogin(String virtualWiki, String login, Pagination pagination, boolean descending) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN);
 			stmt.setString(1, virtualWiki);
 			stmt.setString(2, login);
 			stmt.setInt(3, pagination.getNumResults());
 			stmt.setInt(4, pagination.getOffset());
 			// FIXME - sort order ignored
 			rs = stmt.executeQuery();
 			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
 			while (rs.next()) {
 				recentChanges.add(this.initRecentChange(rs));
 			}
 			return recentChanges;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RecentChange> getUserContributionsByUserDisplay(String virtualWiki, String userDisplay, Pagination pagination, boolean descending) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS);
 			stmt.setString(1, virtualWiki);
 			stmt.setString(2, userDisplay);
 			stmt.setInt(3, pagination.getNumResults());
 			stmt.setInt(4, pagination.getOffset());
 			// FIXME - sort order ignored
 			rs = stmt.executeQuery();
 			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
 			while (rs.next()) {
 				recentChanges.add(this.initRecentChange(rs));
 			}
 			return recentChanges;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<VirtualWiki> getVirtualWikis(Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_SELECT_VIRTUAL_WIKIS);
 			rs = stmt.executeQuery();
 			List<VirtualWiki> results = new ArrayList<VirtualWiki>();
 			while (rs.next()) {
 				VirtualWiki virtualWiki = new VirtualWiki();
 				virtualWiki.setVirtualWikiId(rs.getInt("virtual_wiki_id"));
 				virtualWiki.setName(rs.getString("virtual_wiki_name"));
 				virtualWiki.setDefaultTopicName(rs.getString("default_topic_name"));
 				results.add(virtualWiki);
 			}
 			return results;
 		} finally {
 			// close only the statement and result set - leave the connection open for further use
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<String> getWatchlist(int virtualWikiId, int userId) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WATCHLIST);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setInt(2, userId);
 			rs = stmt.executeQuery();
 			List<String> watchedTopicNames = new ArrayList<String>();
 			while (rs.next()) {
 				watchedTopicNames.add(rs.getString("topic_name"));
 			}
 			return watchedTopicNames;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<RecentChange> getWatchlist(int virtualWikiId, int userId, Pagination pagination) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WATCHLIST_CHANGES);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setInt(2, userId);
 			stmt.setInt(3, pagination.getNumResults());
 			stmt.setInt(4, pagination.getOffset());
 			rs = stmt.executeQuery();
 			List<RecentChange> recentChanges = new ArrayList<RecentChange>();
 			while (rs.next()) {
 				recentChanges.add(this.initRecentChange(rs));
 			}
 			return recentChanges;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	protected void init(Properties properties) {
 		this.props = properties;
 		STATEMENT_CONNECTION_VALIDATION_QUERY    = props.getProperty("STATEMENT_CONNECTION_VALIDATION_QUERY");
 		STATEMENT_CREATE_GROUP_TABLE             = props.getProperty("STATEMENT_CREATE_GROUP_TABLE");
 		STATEMENT_CREATE_ROLE_TABLE              = props.getProperty("STATEMENT_CREATE_ROLE_TABLE");
 		STATEMENT_CREATE_VIRTUAL_WIKI_TABLE      = props.getProperty("STATEMENT_CREATE_VIRTUAL_WIKI_TABLE");
 		STATEMENT_CREATE_WIKI_USER_TABLE         = props.getProperty("STATEMENT_CREATE_WIKI_USER_TABLE");
 		STATEMENT_CREATE_WIKI_USER_LOGIN_INDEX   = props.getProperty("STATEMENT_CREATE_WIKI_USER_LOGIN_INDEX");
 		STATEMENT_CREATE_TOPIC_CURRENT_VERSION_CONSTRAINT = props.getProperty("STATEMENT_CREATE_TOPIC_CURRENT_VERSION_CONSTRAINT");
 		STATEMENT_CREATE_TOPIC_TABLE             = props.getProperty("STATEMENT_CREATE_TOPIC_TABLE");
 		STATEMENT_CREATE_TOPIC_VERSION_TABLE     = props.getProperty("STATEMENT_CREATE_TOPIC_VERSION_TABLE");
 		STATEMENT_CREATE_USERS_TABLE             = props.getProperty("STATEMENT_CREATE_USERS_TABLE");
 		STATEMENT_CREATE_WIKI_FILE_TABLE         = props.getProperty("STATEMENT_CREATE_WIKI_FILE_TABLE");
 		STATEMENT_CREATE_WIKI_FILE_VERSION_TABLE = props.getProperty("STATEMENT_CREATE_WIKI_FILE_VERSION_TABLE");
 		STATEMENT_CREATE_AUTHORITIES_TABLE       = props.getProperty("STATEMENT_CREATE_AUTHORITIES_TABLE");
 		STATEMENT_CREATE_CATEGORY_TABLE          = props.getProperty("STATEMENT_CREATE_CATEGORY_TABLE");
 		STATEMENT_CREATE_GROUP_AUTHORITIES_TABLE = props.getProperty("STATEMENT_CREATE_GROUP_AUTHORITIES_TABLE");
 		STATEMENT_CREATE_GROUP_MEMBERS_TABLE     = props.getProperty("STATEMENT_CREATE_GROUP_MEMBERS_TABLE");
 		STATEMENT_CREATE_LOG_TABLE               = props.getProperty("STATEMENT_CREATE_LOG_TABLE");
 		STATEMENT_CREATE_RECENT_CHANGE_TABLE     = props.getProperty("STATEMENT_CREATE_RECENT_CHANGE_TABLE");
 		STATEMENT_CREATE_WATCHLIST_TABLE         = props.getProperty("STATEMENT_CREATE_WATCHLIST_TABLE");
 		STATEMENT_DELETE_AUTHORITIES             = props.getProperty("STATEMENT_DELETE_AUTHORITIES");
 		STATEMENT_DELETE_GROUP_AUTHORITIES       = props.getProperty("STATEMENT_DELETE_GROUP_AUTHORITIES");
 		STATEMENT_DELETE_LOG_ITEMS               = props.getProperty("STATEMENT_DELETE_LOG_ITEMS");
 		STATEMENT_DELETE_RECENT_CHANGES          = props.getProperty("STATEMENT_DELETE_RECENT_CHANGES");
 		STATEMENT_DELETE_RECENT_CHANGES_TOPIC    = props.getProperty("STATEMENT_DELETE_RECENT_CHANGES_TOPIC");
 		STATEMENT_DELETE_TOPIC_CATEGORIES        = props.getProperty("STATEMENT_DELETE_TOPIC_CATEGORIES");
 		STATEMENT_DELETE_WATCHLIST_ENTRY         = props.getProperty("STATEMENT_DELETE_WATCHLIST_ENTRY");
 		STATEMENT_DROP_AUTHORITIES_TABLE         = props.getProperty("STATEMENT_DROP_AUTHORITIES_TABLE");
 		STATEMENT_DROP_CATEGORY_TABLE            = props.getProperty("STATEMENT_DROP_CATEGORY_TABLE");
 		STATEMENT_DROP_GROUP_AUTHORITIES_TABLE   = props.getProperty("STATEMENT_DROP_GROUP_AUTHORITIES_TABLE");
 		STATEMENT_DROP_GROUP_MEMBERS_TABLE       = props.getProperty("STATEMENT_DROP_GROUP_MEMBERS_TABLE");
 		STATEMENT_DROP_GROUP_TABLE               = props.getProperty("STATEMENT_DROP_GROUP_TABLE");
 		STATEMENT_DROP_LOG_TABLE                 = props.getProperty("STATEMENT_DROP_LOG_TABLE");
 		STATEMENT_DROP_RECENT_CHANGE_TABLE       = props.getProperty("STATEMENT_DROP_RECENT_CHANGE_TABLE");
 		STATEMENT_DROP_ROLE_TABLE                = props.getProperty("STATEMENT_DROP_ROLE_TABLE");
 		STATEMENT_DROP_TOPIC_CURRENT_VERSION_CONSTRAINT = props.getProperty("STATEMENT_DROP_TOPIC_CURRENT_VERSION_CONSTRAINT");
 		STATEMENT_DROP_TOPIC_TABLE               = props.getProperty("STATEMENT_DROP_TOPIC_TABLE");
 		STATEMENT_DROP_TOPIC_VERSION_TABLE       = props.getProperty("STATEMENT_DROP_TOPIC_VERSION_TABLE");
 		STATEMENT_DROP_USERS_TABLE               = props.getProperty("STATEMENT_DROP_USERS_TABLE");
 		STATEMENT_DROP_VIRTUAL_WIKI_TABLE        = props.getProperty("STATEMENT_DROP_VIRTUAL_WIKI_TABLE");
 		STATEMENT_DROP_WATCHLIST_TABLE           = props.getProperty("STATEMENT_DROP_WATCHLIST_TABLE");
 		STATEMENT_DROP_WIKI_USER_LOGIN_INDEX     = props.getProperty("STATEMENT_DROP_WIKI_USER_LOGIN_INDEX");
 		STATEMENT_DROP_WIKI_USER_TABLE           = props.getProperty("STATEMENT_DROP_WIKI_USER_TABLE");
 		STATEMENT_DROP_WIKI_FILE_TABLE           = props.getProperty("STATEMENT_DROP_WIKI_FILE_TABLE");
 		STATEMENT_DROP_WIKI_FILE_VERSION_TABLE   = props.getProperty("STATEMENT_DROP_WIKI_FILE_VERSION_TABLE");
 		STATEMENT_INSERT_AUTHORITY               = props.getProperty("STATEMENT_INSERT_AUTHORITY");
 		STATEMENT_INSERT_CATEGORY                = props.getProperty("STATEMENT_INSERT_CATEGORY");
 		STATEMENT_INSERT_GROUP                   = props.getProperty("STATEMENT_INSERT_GROUP");
 		STATEMENT_INSERT_GROUP_AUTO_INCREMENT    = props.getProperty("STATEMENT_INSERT_GROUP_AUTO_INCREMENT");
 		STATEMENT_INSERT_GROUP_AUTHORITY         = props.getProperty("STATEMENT_INSERT_GROUP_AUTHORITY");
 		STATEMENT_INSERT_GROUP_MEMBER            = props.getProperty("STATEMENT_INSERT_GROUP_MEMBER");
 		STATEMENT_INSERT_GROUP_MEMBER_AUTO_INCREMENT = props.getProperty("STATEMENT_INSERT_GROUP_MEMBER_AUTO_INCREMENT");
 		STATEMENT_INSERT_LOG_ITEM                = props.getProperty("STATEMENT_INSERT_LOG_ITEM");
 		STATEMENT_INSERT_LOG_ITEMS_BY_TOPIC_VERSION_TYPE = props.getProperty("STATEMENT_INSERT_LOG_ITEMS_BY_TOPIC_VERSION_TYPE");
 		STATEMENT_INSERT_LOG_ITEMS_IMPORT        = props.getProperty("STATEMENT_INSERT_LOG_ITEMS_IMPORT");
 		STATEMENT_INSERT_LOG_ITEMS_MOVE          = props.getProperty("STATEMENT_INSERT_LOG_ITEMS_MOVE");
 		STATEMENT_INSERT_LOG_ITEMS_UPLOAD        = props.getProperty("STATEMENT_INSERT_LOG_ITEMS_UPLOAD");
 		STATEMENT_INSERT_LOG_ITEMS_USER          = props.getProperty("STATEMENT_INSERT_LOG_ITEMS_USER");
 		STATEMENT_INSERT_RECENT_CHANGE           = props.getProperty("STATEMENT_INSERT_RECENT_CHANGE");
 		STATEMENT_INSERT_RECENT_CHANGES_LOGS     = props.getProperty("STATEMENT_INSERT_RECENT_CHANGES_LOGS");
 		STATEMENT_INSERT_RECENT_CHANGES_VERSIONS = props.getProperty("STATEMENT_INSERT_RECENT_CHANGES_VERSIONS");
 		STATEMENT_INSERT_ROLE                    = props.getProperty("STATEMENT_INSERT_ROLE");
 		STATEMENT_INSERT_TOPIC                   = props.getProperty("STATEMENT_INSERT_TOPIC");
 		STATEMENT_INSERT_TOPIC_AUTO_INCREMENT    = props.getProperty("STATEMENT_INSERT_TOPIC_AUTO_INCREMENT");
 		STATEMENT_INSERT_TOPIC_VERSION           = props.getProperty("STATEMENT_INSERT_TOPIC_VERSION");
 		STATEMENT_INSERT_TOPIC_VERSION_AUTO_INCREMENT = props.getProperty("STATEMENT_INSERT_TOPIC_VERSION_AUTO_INCREMENT");
 		STATEMENT_INSERT_USER                    = props.getProperty("STATEMENT_INSERT_USER");
 		STATEMENT_INSERT_VIRTUAL_WIKI            = props.getProperty("STATEMENT_INSERT_VIRTUAL_WIKI");
 		STATEMENT_INSERT_VIRTUAL_WIKI_AUTO_INCREMENT = props.getProperty("STATEMENT_INSERT_VIRTUAL_WIKI_AUTO_INCREMENT");
 		STATEMENT_INSERT_WATCHLIST_ENTRY         = props.getProperty("STATEMENT_INSERT_WATCHLIST_ENTRY");
 		STATEMENT_INSERT_WIKI_FILE               = props.getProperty("STATEMENT_INSERT_WIKI_FILE");
 		STATEMENT_INSERT_WIKI_FILE_AUTO_INCREMENT = props.getProperty("STATEMENT_INSERT_WIKI_FILE_AUTO_INCREMENT");
 		STATEMENT_INSERT_WIKI_FILE_VERSION       = props.getProperty("STATEMENT_INSERT_WIKI_FILE_VERSION");
 		STATEMENT_INSERT_WIKI_FILE_VERSION_AUTO_INCREMENT = props.getProperty("STATEMENT_INSERT_WIKI_FILE_VERSION_AUTO_INCREMENT");
 		STATEMENT_INSERT_WIKI_USER               = props.getProperty("STATEMENT_INSERT_WIKI_USER");
 		STATEMENT_INSERT_WIKI_USER_AUTO_INCREMENT = props.getProperty("STATEMENT_INSERT_WIKI_USER_AUTO_INCREMENT");
 		STATEMENT_SELECT_AUTHORITIES_AUTHORITY   = props.getProperty("STATEMENT_SELECT_AUTHORITIES_AUTHORITY");
 		STATEMENT_SELECT_AUTHORITIES_LOGIN       = props.getProperty("STATEMENT_SELECT_AUTHORITIES_LOGIN");
 		STATEMENT_SELECT_AUTHORITIES_USER        = props.getProperty("STATEMENT_SELECT_AUTHORITIES_USER");
 		STATEMENT_SELECT_CATEGORIES              = props.getProperty("STATEMENT_SELECT_CATEGORIES");
 		STATEMENT_SELECT_CATEGORY_TOPICS         = props.getProperty("STATEMENT_SELECT_CATEGORY_TOPICS");
 		STATEMENT_SELECT_GROUP                   = props.getProperty("STATEMENT_SELECT_GROUP");
 		STATEMENT_SELECT_GROUP_AUTHORITIES       = props.getProperty("STATEMENT_SELECT_GROUP_AUTHORITIES");
 		STATEMENT_SELECT_GROUPS_AUTHORITIES      = props.getProperty("STATEMENT_SELECT_GROUPS_AUTHORITIES");
 		STATEMENT_SELECT_GROUP_MEMBERS_SEQUENCE  = props.getProperty("STATEMENT_SELECT_GROUP_MEMBERS_SEQUENCE");
 		STATEMENT_SELECT_GROUP_SEQUENCE          = props.getProperty("STATEMENT_SELECT_GROUP_SEQUENCE");
 		STATEMENT_SELECT_LOG_ITEMS               = props.getProperty("STATEMENT_SELECT_LOG_ITEMS");
 		STATEMENT_SELECT_LOG_ITEMS_BY_TYPE       = props.getProperty("STATEMENT_SELECT_LOG_ITEMS_BY_TYPE");
 		STATEMENT_SELECT_RECENT_CHANGES          = props.getProperty("STATEMENT_SELECT_RECENT_CHANGES");
 		STATEMENT_SELECT_ROLES                   = props.getProperty("STATEMENT_SELECT_ROLES");
 		STATEMENT_SELECT_TOPIC_BY_TYPE           = props.getProperty("STATEMENT_SELECT_TOPIC_BY_TYPE");
 		STATEMENT_SELECT_TOPIC_COUNT             = props.getProperty("STATEMENT_SELECT_TOPIC_COUNT");
 		STATEMENT_SELECT_TOPIC                   = props.getProperty("STATEMENT_SELECT_TOPIC");
 		STATEMENT_SELECT_TOPIC_HISTORY           = props.getProperty("STATEMENT_SELECT_TOPIC_HISTORY");
 		STATEMENT_SELECT_TOPIC_LOWER             = props.getProperty("STATEMENT_SELECT_TOPIC_LOWER");
 		STATEMENT_SELECT_TOPICS                  = props.getProperty("STATEMENT_SELECT_TOPICS");
 		STATEMENT_SELECT_TOPICS_ADMIN            = props.getProperty("STATEMENT_SELECT_TOPICS_ADMIN");
 		STATEMENT_SELECT_TOPIC_SEQUENCE          = props.getProperty("STATEMENT_SELECT_TOPIC_SEQUENCE");
 		STATEMENT_SELECT_TOPIC_VERSION           = props.getProperty("STATEMENT_SELECT_TOPIC_VERSION");
 		STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE  = props.getProperty("STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE");
 		STATEMENT_SELECT_USERS_AUTHENTICATION    = props.getProperty("STATEMENT_SELECT_USERS_AUTHENTICATION");
 		STATEMENT_SELECT_VIRTUAL_WIKIS           = props.getProperty("STATEMENT_SELECT_VIRTUAL_WIKIS");
 		STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE   = props.getProperty("STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE");
 		STATEMENT_SELECT_WATCHLIST               = props.getProperty("STATEMENT_SELECT_WATCHLIST");
 		STATEMENT_SELECT_WATCHLIST_CHANGES       = props.getProperty("STATEMENT_SELECT_WATCHLIST_CHANGES");
 		STATEMENT_SELECT_WIKI_FILE               = props.getProperty("STATEMENT_SELECT_WIKI_FILE");
 		STATEMENT_SELECT_WIKI_FILE_COUNT         = props.getProperty("STATEMENT_SELECT_WIKI_FILE_COUNT");
 		STATEMENT_SELECT_WIKI_FILE_SEQUENCE      = props.getProperty("STATEMENT_SELECT_WIKI_FILE_SEQUENCE");
 		STATEMENT_SELECT_WIKI_FILE_VERSION_SEQUENCE = props.getProperty("STATEMENT_SELECT_WIKI_FILE_VERSION_SEQUENCE");
 		STATEMENT_SELECT_WIKI_FILE_VERSIONS      = props.getProperty("STATEMENT_SELECT_WIKI_FILE_VERSIONS");
 		STATEMENT_SELECT_WIKI_USER               = props.getProperty("STATEMENT_SELECT_WIKI_USER");
 		STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS = props.getProperty("STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS");
 		STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN = props.getProperty("STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN");
 		STATEMENT_SELECT_WIKI_USER_COUNT         = props.getProperty("STATEMENT_SELECT_WIKI_USER_COUNT");
 		STATEMENT_SELECT_WIKI_USER_DETAILS_PASSWORD = props.getProperty("STATEMENT_SELECT_WIKI_USER_DETAILS_PASSWORD");
 		STATEMENT_SELECT_WIKI_USER_LOGIN         = props.getProperty("STATEMENT_SELECT_WIKI_USER_LOGIN");
 		STATEMENT_SELECT_WIKI_USER_SEQUENCE      = props.getProperty("STATEMENT_SELECT_WIKI_USER_SEQUENCE");
 		STATEMENT_SELECT_WIKI_USERS              = props.getProperty("STATEMENT_SELECT_WIKI_USERS");
 		STATEMENT_UPDATE_GROUP                   = props.getProperty("STATEMENT_UPDATE_GROUP");
 		STATEMENT_UPDATE_ROLE                    = props.getProperty("STATEMENT_UPDATE_ROLE");
 		STATEMENT_UPDATE_TOPIC                   = props.getProperty("STATEMENT_UPDATE_TOPIC");
 		STATEMENT_UPDATE_USER                    = props.getProperty("STATEMENT_UPDATE_USER");
 		STATEMENT_UPDATE_VIRTUAL_WIKI            = props.getProperty("STATEMENT_UPDATE_VIRTUAL_WIKI");
 		STATEMENT_UPDATE_WIKI_FILE               = props.getProperty("STATEMENT_UPDATE_WIKI_FILE");
 		STATEMENT_UPDATE_WIKI_USER               = props.getProperty("STATEMENT_UPDATE_WIKI_USER");
 	}
 
 	/**
 	 *
 	 */
 	private Category initCategory(ResultSet rs, String virtualWikiName) throws SQLException {
 		Category category = new Category();
 		category.setName("category_name");
 		category.setVirtualWiki(virtualWikiName);
 		category.setChildTopicName(rs.getString("topic_name"));
 		category.setSortKey(rs.getString("sort_key"));
 		category.setTopicType(rs.getInt("topic_type"));
 		return category;
 	}
 
 	/**
 	 *
 	 */
 	protected LogItem initLogItem(ResultSet rs, String virtualWikiName) throws SQLException {
 		LogItem logItem = new LogItem();
 		int userId = rs.getInt("wiki_user_id");
 		if (userId > 0) {
 			logItem.setUserId(userId);
 		}
 		logItem.setUserDisplayName(rs.getString("display_name"));
 		int topicId = rs.getInt("topic_id");
 		if (topicId > 0) {
 			logItem.setTopicId(topicId);
 		}
 		int topicVersionId = rs.getInt("topic_version_id");
 		if (topicVersionId > 0) {
 			logItem.setTopicVersionId(topicVersionId);
 		}
 		logItem.setLogDate(rs.getTimestamp("log_date"));
 		logItem.setLogComment(rs.getString("log_comment"));
 		logItem.setLogParamString(rs.getString("log_params"));
 		logItem.setLogType(rs.getInt("log_type"));
 		logItem.setVirtualWiki(virtualWikiName);
 		return logItem;
 	}
 
 	/**
 	 *
 	 */
 	protected RecentChange initRecentChange(ResultSet rs) throws SQLException {
 		RecentChange change = new RecentChange();
 		int topicVersionId = rs.getInt("topic_version_id");
 		if (topicVersionId > 0) {
 			change.setTopicVersionId(topicVersionId);
 		}
 		int previousTopicVersionId = rs.getInt("previous_topic_version_id");
 		if (previousTopicVersionId > 0) {
 			change.setPreviousTopicVersionId(previousTopicVersionId);
 		}
 		int topicId = rs.getInt("topic_id");
 		if (topicId > 0) {
 			change.setTopicId(topicId);
 		}
 		change.setTopicName(rs.getString("topic_name"));
 		change.setCharactersChanged(rs.getInt("characters_changed"));
 		change.setChangeDate(rs.getTimestamp("change_date"));
 		change.setChangeComment(rs.getString("change_comment"));
 		int userId = rs.getInt("wiki_user_id");
 		if (userId > 0) {
 			change.setAuthorId(userId);
 		}
 		change.setAuthorName(rs.getString("display_name"));
 		int editType = rs.getInt("edit_type");
 		if (editType > 0) {
 			change.setEditType(editType);
 			change.initChangeWikiMessageForVersion(editType, rs.getString("log_params"));
 		}
 		int logType = rs.getInt("log_type");
 		if (logType > 0) {
 			change.setLogType(logType);
 			change.initChangeWikiMessageForLog(logType, rs.getString("log_params"));
 		}
 		change.setVirtualWiki(rs.getString("virtual_wiki_name"));
 		return change;
 	}
 
 	/**
 	 *
 	 */
 	private Role initRole(ResultSet rs) throws SQLException {
 		Role role = new RoleImpl(rs.getString("role_name"));
 		role.setDescription(rs.getString("role_description"));
 		return role;
 	}
 
 	/**
 	 *
 	 */
 	private Topic initTopic(ResultSet rs, String virtualWikiName) throws SQLException {
 		// if a topic by this name has been deleted then there will be
 		// multiple results.  the first will be a non-deleted topic (if
 		// one exists), otherwise the last is the most recently deleted
 		// topic.
 		if (rs.getTimestamp("delete_date") != null) {
 			// go to the last result
 			rs.last();
 		}
 		Topic topic = new Topic();
 		topic.setAdminOnly(rs.getInt("topic_admin_only") != 0);
 		topic.setName(rs.getString("topic_name"));
 		topic.setVirtualWiki(virtualWikiName);
 		int currentVersionId = rs.getInt("current_version_id");
 		if (currentVersionId > 0) {
 			topic.setCurrentVersionId(currentVersionId);
 		}
 		topic.setTopicContent(rs.getString("version_content"));
 		// FIXME - Oracle cannot store an empty string - it converts them
 		// to null - so add a hack to work around the problem.
 		if (topic.getTopicContent() == null) {
 			topic.setTopicContent("");
 		}
 		topic.setTopicId(rs.getInt("topic_id"));
 		topic.setReadOnly(rs.getInt("topic_read_only") != 0);
 		topic.setDeleteDate(rs.getTimestamp("delete_date"));
 		topic.setTopicType(rs.getInt("topic_type"));
 		topic.setRedirectTo(rs.getString("redirect_to"));
 		return topic;
 	}
 
 	/**
 	 *
 	 */
 	private TopicVersion initTopicVersion(ResultSet rs) throws SQLException {
 		TopicVersion topicVersion = new TopicVersion();
 		topicVersion.setTopicVersionId(rs.getInt("topic_version_id"));
 		topicVersion.setTopicId(rs.getInt("topic_id"));
 		topicVersion.setEditComment(rs.getString("edit_comment"));
 		topicVersion.setVersionContent(rs.getString("version_content"));
 		// FIXME - Oracle cannot store an empty string - it converts them
 		// to null - so add a hack to work around the problem.
 		if (topicVersion.getVersionContent() == null) {
 			topicVersion.setVersionContent("");
 		}
 		int previousTopicVersionId = rs.getInt("previous_topic_version_id");
 		if (previousTopicVersionId > 0) {
 			topicVersion.setPreviousTopicVersionId(previousTopicVersionId);
 		}
 		int userId = rs.getInt("wiki_user_id");
 		if (userId > 0) {
 			topicVersion.setAuthorId(userId);
 		}
 		topicVersion.setCharactersChanged(rs.getInt("characters_changed"));
 		topicVersion.setVersionParamString(rs.getString("version_params"));
 		topicVersion.setEditDate(rs.getTimestamp("edit_date"));
 		topicVersion.setEditType(rs.getInt("edit_type"));
 		topicVersion.setAuthorDisplay(rs.getString("wiki_user_display"));
 		return topicVersion;
 	}
 
 	/**
 	 *
 	 */
 	private WikiFile initWikiFile(ResultSet rs, String virtualWikiName) throws SQLException {
 		WikiFile wikiFile = new WikiFile();
 		wikiFile.setFileId(rs.getInt("file_id"));
 		wikiFile.setAdminOnly(rs.getInt("file_admin_only") != 0);
 		wikiFile.setFileName(rs.getString("file_name"));
 		wikiFile.setVirtualWiki(virtualWikiName);
 		wikiFile.setUrl(rs.getString("file_url"));
 		wikiFile.setTopicId(rs.getInt("topic_id"));
 		wikiFile.setReadOnly(rs.getInt("file_read_only") != 0);
 		wikiFile.setDeleteDate(rs.getTimestamp("delete_date"));
 		wikiFile.setMimeType(rs.getString("mime_type"));
 		wikiFile.setFileSize(rs.getInt("file_size"));
 		return wikiFile;
 	}
 
 	/**
 	 *
 	 */
 	private WikiFileVersion initWikiFileVersion(ResultSet rs) throws SQLException {
 		WikiFileVersion wikiFileVersion = new WikiFileVersion();
 		wikiFileVersion.setFileVersionId(rs.getInt("file_version_id"));
 		wikiFileVersion.setFileId(rs.getInt("file_id"));
 		wikiFileVersion.setUploadComment(rs.getString("upload_comment"));
 		wikiFileVersion.setUrl(rs.getString("file_url"));
 		int userId = rs.getInt("wiki_user_id");
 		if (userId > 0) {
 			wikiFileVersion.setAuthorId(userId);
 		}
 		wikiFileVersion.setUploadDate(rs.getTimestamp("upload_date"));
 		wikiFileVersion.setMimeType(rs.getString("mime_type"));
 		wikiFileVersion.setAuthorDisplay(rs.getString("wiki_user_display"));
 		wikiFileVersion.setFileSize(rs.getInt("file_size"));
 		return wikiFileVersion;
 	}
 
 	/**
 	 *
 	 */
 	private WikiGroup initWikiGroup(ResultSet rs) throws SQLException {
 		WikiGroup wikiGroup = new WikiGroup();
 		wikiGroup.setGroupId(rs.getInt("group_id"));
 		wikiGroup.setName(rs.getString("group_name"));
 		wikiGroup.setDescription(rs.getString("group_description"));
 		return wikiGroup;
 	}
 
 	/**
 	 *
 	 */
 	private WikiUser initWikiUser(ResultSet rs) throws SQLException {
 		String username = rs.getString("login");
 		WikiUser user = new WikiUser(username);
 		user.setUserId(rs.getInt("wiki_user_id"));
 		user.setDisplayName(rs.getString("display_name"));
 		user.setCreateDate(rs.getTimestamp("create_date"));
 		user.setLastLoginDate(rs.getTimestamp("last_login_date"));
 		user.setCreateIpAddress(rs.getString("create_ip_address"));
 		user.setLastLoginIpAddress(rs.getString("last_login_ip_address"));
 		user.setDefaultLocale(rs.getString("default_locale"));
 		user.setEmail(rs.getString("email"));
 		user.setEditor(rs.getString("editor"));
 		user.setSignature(rs.getString("signature"));
 		return user;
 	}
 
 	/**
 	 *
 	 */
 	public void insertCategory(Category category, int virtualWikiId, int topicId, Connection conn) throws SQLException {
 		if (topicId == -1) {
 			throw new SQLException("Unable to find child topic " + category.getChildTopicName() + " for category " + category.getName());
 		}
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_CATEGORY);
 			stmt.setInt(1, topicId);
 			stmt.setString(2, category.getName());
 			stmt.setString(3, category.getSortKey());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertGroupAuthority(int groupId, String authority, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_GROUP_AUTHORITY);
 			stmt.setInt(1, groupId);
 			stmt.setString(2, authority);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertGroupMember(String username, int groupId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_GROUP_MEMBER);
 				int groupMemberId = this.nextGroupMemberId(conn);
 				stmt.setInt(index++, groupMemberId);
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_GROUP_MEMBER_AUTO_INCREMENT);
 			}
 			stmt.setString(index++, username);
 			stmt.setInt(index++, groupId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertLogItem(LogItem logItem, int virtualWikiId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEM);
 			stmt.setTimestamp(1, logItem.getLogDate());
 			stmt.setInt(2, virtualWikiId);
 			if (logItem.getUserId() == null) {
 				stmt.setNull(3, Types.INTEGER);
 			} else {
 				stmt.setInt(3, logItem.getUserId());
 			}
 			stmt.setString(4, logItem.getUserDisplayName());
 			stmt.setInt(5, logItem.getLogType());
 			stmt.setString(6, logItem.getLogComment());
 			stmt.setString(7, logItem.getLogParamString());
 			if (logItem.getTopicId() == null) {
 				stmt.setNull(8, Types.INTEGER);
 			} else {
 				stmt.setInt(8, logItem.getTopicId());
 			}
 			if (logItem.getTopicVersionId() == null) {
 				stmt.setNull(9, Types.INTEGER);
 			} else {
 				stmt.setInt(9, logItem.getTopicVersionId());
 			}
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertRecentChange(RecentChange change, int virtualWikiId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_RECENT_CHANGE);
 			if (change.getTopicVersionId() == null) {
 				stmt.setNull(1, Types.INTEGER);
 			} else {
 				stmt.setInt(1, change.getTopicVersionId());
 			}
 			if (change.getPreviousTopicVersionId() == null) {
 				stmt.setNull(2, Types.INTEGER);
 			} else {
 				stmt.setInt(2, change.getPreviousTopicVersionId());
 			}
 			if (change.getTopicId() == null) {
 				stmt.setNull(3, Types.INTEGER);
 			} else {
 				stmt.setInt(3, change.getTopicId());
 			}
 			stmt.setString(4, change.getTopicName());
 			stmt.setTimestamp(5, change.getChangeDate());
 			stmt.setString(6, change.getChangeComment());
 			if (change.getAuthorId() == null) {
 				stmt.setNull(7, Types.INTEGER);
 			} else {
 				stmt.setInt(7, change.getAuthorId());
 			}
 			stmt.setString(8, change.getAuthorName());
 			if (change.getEditType() == null) {
 				stmt.setNull(9, Types.INTEGER);
 			} else {
 				stmt.setInt(9, change.getEditType());
 			}
 			stmt.setInt(10, virtualWikiId);
 			stmt.setString(11, change.getVirtualWiki());
 			if (change.getCharactersChanged() == null) {
 				stmt.setNull(12, Types.INTEGER);
 			} else {
 				stmt.setInt(12, change.getCharactersChanged());
 			}
 			if (change.getLogType() == null) {
 				stmt.setNull(13, Types.INTEGER);
 			} else {
 				stmt.setInt(13, change.getLogType());
 			}
 			stmt.setString(14, change.getParamString());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertRole(Role role, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_ROLE);
 			stmt.setString(1, role.getAuthority());
 			stmt.setString(2, role.getDescription());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertTopic(Topic topic, int virtualWikiId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_TOPIC);
 				int topicId = this.nextTopicId(conn);
 				topic.setTopicId(topicId);
 				stmt.setInt(index++, topic.getTopicId());
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_TOPIC_AUTO_INCREMENT, Statement.RETURN_GENERATED_KEYS);
 			}
 			stmt.setInt(index++, virtualWikiId);
 			stmt.setString(index++, topic.getName());
 			stmt.setInt(index++, topic.getTopicType());
 			stmt.setInt(index++, (topic.getReadOnly() ? 1 : 0));
 			if (topic.getCurrentVersionId() == null) {
 				stmt.setNull(index++, Types.INTEGER);
 			} else {
 				stmt.setInt(index++, topic.getCurrentVersionId());
 			}
 			stmt.setTimestamp(index++, topic.getDeleteDate());
 			stmt.setInt(index++, (topic.getAdminOnly() ? 1 : 0));
 			stmt.setString(index++, topic.getRedirectTo());
 			stmt.executeUpdate();
 			if (this.autoIncrementPrimaryKeys()) {
 				ResultSet rs = stmt.getGeneratedKeys();
 				if (!rs.next()) {
 					throw new SQLException("Unable to determine auto-generated ID for database record");
 				}
 				topic.setTopicId(rs.getInt(1));
 			}
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertTopicVersion(TopicVersion topicVersion, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_TOPIC_VERSION);
 				int topicVersionId = this.nextTopicVersionId(conn);
 				topicVersion.setTopicVersionId(topicVersionId);
 				stmt.setInt(index++, topicVersion.getTopicVersionId());
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_TOPIC_VERSION_AUTO_INCREMENT, Statement.RETURN_GENERATED_KEYS);
 			}
 			if (topicVersion.getEditDate() == null) {
 				Timestamp editDate = new Timestamp(System.currentTimeMillis());
 				topicVersion.setEditDate(editDate);
 			}
 			stmt.setInt(index++, topicVersion.getTopicId());
 			stmt.setString(index++, topicVersion.getEditComment());
 			stmt.setString(index++, topicVersion.getVersionContent());
 			if (topicVersion.getAuthorId() == null) {
 				stmt.setNull(index++, Types.INTEGER);
 			} else {
 				stmt.setInt(index++, topicVersion.getAuthorId());
 			}
 			stmt.setInt(index++, topicVersion.getEditType());
 			stmt.setString(index++, topicVersion.getAuthorDisplay());
 			stmt.setTimestamp(index++, topicVersion.getEditDate());
 			if (topicVersion.getPreviousTopicVersionId() == null) {
 				stmt.setNull(index++, Types.INTEGER);
 			} else {
 				stmt.setInt(index++, topicVersion.getPreviousTopicVersionId());
 			}
 			stmt.setInt(index++, topicVersion.getCharactersChanged());
 			stmt.setString(index++, topicVersion.getVersionParamString());
 			stmt.executeUpdate();
 			if (this.autoIncrementPrimaryKeys()) {
 				ResultSet rs = stmt.getGeneratedKeys();
 				if (!rs.next()) {
 					throw new SQLException("Unable to determine auto-generated ID for database record");
 				}
 				topicVersion.setTopicVersionId(rs.getInt(1));
 			}
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertUserDetails(WikiUserDetails userDetails, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_USER);
 			stmt.setString(1, userDetails.getUsername());
 			stmt.setString(2, userDetails.getPassword());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertUserAuthority(String username, String authority, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_AUTHORITY);
 			stmt.setString(1, username);
 			stmt.setString(2, authority);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_VIRTUAL_WIKI);
 				int virtualWikiId = this.nextVirtualWikiId(conn);
 				virtualWiki.setVirtualWikiId(virtualWikiId);
 				stmt.setInt(index++, virtualWiki.getVirtualWikiId());
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_VIRTUAL_WIKI_AUTO_INCREMENT, Statement.RETURN_GENERATED_KEYS);
 			}
 			stmt.setString(index++, virtualWiki.getName());
 			stmt.setString(index++, virtualWiki.getDefaultTopicName());
 			stmt.executeUpdate();
 			if (this.autoIncrementPrimaryKeys()) {
 				ResultSet rs = stmt.getGeneratedKeys();
 				if (!rs.next()) {
 					throw new SQLException("Unable to determine auto-generated ID for database record");
 				}
 				virtualWiki.setVirtualWikiId(rs.getInt(1));
 			}
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertWatchlistEntry(int virtualWikiId, String topicName, int userId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_INSERT_WATCHLIST_ENTRY);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setString(2, topicName);
 			stmt.setInt(3, userId);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertWikiFile(WikiFile wikiFile, int virtualWikiId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_WIKI_FILE);
 				int fileId = this.nextWikiFileId(conn);
 				wikiFile.setFileId(fileId);
 				stmt.setInt(index++, wikiFile.getFileId());
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_WIKI_FILE_AUTO_INCREMENT, Statement.RETURN_GENERATED_KEYS);
 			}
 			stmt.setInt(index++, virtualWikiId);
 			stmt.setString(index++, wikiFile.getFileName());
 			stmt.setString(index++, wikiFile.getUrl());
 			stmt.setString(index++, wikiFile.getMimeType());
 			stmt.setInt(index++, wikiFile.getTopicId());
 			stmt.setTimestamp(index++, wikiFile.getDeleteDate());
 			stmt.setInt(index++, (wikiFile.getReadOnly() ? 1 : 0));
 			stmt.setInt(index++, (wikiFile.getAdminOnly() ? 1 : 0));
 			stmt.setLong(index++, wikiFile.getFileSize());
 			stmt.executeUpdate();
 			if (this.autoIncrementPrimaryKeys()) {
 				ResultSet rs = stmt.getGeneratedKeys();
 				if (!rs.next()) {
 					throw new SQLException("Unable to determine auto-generated ID for database record");
 				}
 				wikiFile.setFileId(rs.getInt(1));
 			}
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertWikiFileVersion(WikiFileVersion wikiFileVersion, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_WIKI_FILE_VERSION);
 				int fileVersionId = this.nextWikiFileVersionId(conn);
 				wikiFileVersion.setFileVersionId(fileVersionId);
 				stmt.setInt(index++, wikiFileVersion.getFileVersionId());
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_WIKI_FILE_VERSION_AUTO_INCREMENT, Statement.RETURN_GENERATED_KEYS);
 			}
 			if (wikiFileVersion.getUploadDate() == null) {
 				Timestamp uploadDate = new Timestamp(System.currentTimeMillis());
 				wikiFileVersion.setUploadDate(uploadDate);
 			}
 			stmt.setInt(index++, wikiFileVersion.getFileId());
 			stmt.setString(index++, wikiFileVersion.getUploadComment());
 			stmt.setString(index++, wikiFileVersion.getUrl());
 			if (wikiFileVersion.getAuthorId() == null) {
 				stmt.setNull(index++, Types.INTEGER);
 			} else {
 				stmt.setInt(index++, wikiFileVersion.getAuthorId());
 			}
 			stmt.setString(index++, wikiFileVersion.getAuthorDisplay());
 			stmt.setTimestamp(index++, wikiFileVersion.getUploadDate());
 			stmt.setString(index++, wikiFileVersion.getMimeType());
 			stmt.setLong(index++, wikiFileVersion.getFileSize());
 			stmt.executeUpdate();
 			if (this.autoIncrementPrimaryKeys()) {
 				ResultSet rs = stmt.getGeneratedKeys();
 				if (!rs.next()) {
 					throw new SQLException("Unable to determine auto-generated ID for database record");
 				}
 				wikiFileVersion.setFileVersionId(rs.getInt(1));
 			}
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertWikiGroup(WikiGroup group, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_GROUP);
 				int groupId = this.nextWikiGroupId(conn);
 				group.setGroupId(groupId);
 				stmt.setInt(index++, group.getGroupId());
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_GROUP_AUTO_INCREMENT, Statement.RETURN_GENERATED_KEYS);
 			}
 			stmt.setString(index++, group.getName());
 			stmt.setString(index++, group.getDescription());
 			stmt.executeUpdate();
 			if (this.autoIncrementPrimaryKeys()) {
 				ResultSet rs = stmt.getGeneratedKeys();
 				if (!rs.next()) {
 					throw new SQLException("Unable to determine auto-generated ID for database record");
 				}
 				group.setGroupId(rs.getInt(1));
 			}
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void insertWikiUser(WikiUser user, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			int index = 1;
 			if (!this.autoIncrementPrimaryKeys()) {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_WIKI_USER);
 				int nextUserId = this.nextWikiUserId(conn);
 				user.setUserId(nextUserId);
 				stmt.setInt(index++, user.getUserId());
 			} else {
 				stmt = conn.prepareStatement(STATEMENT_INSERT_WIKI_USER_AUTO_INCREMENT, Statement.RETURN_GENERATED_KEYS);
 			}
 			stmt.setString(index++, user.getUsername());
 			stmt.setString(index++, user.getDisplayName());
 			stmt.setTimestamp(index++, user.getCreateDate());
 			stmt.setTimestamp(index++, user.getLastLoginDate());
 			stmt.setString(index++, user.getCreateIpAddress());
 			stmt.setString(index++, user.getLastLoginIpAddress());
 			stmt.setString(index++, user.getDefaultLocale());
 			stmt.setString(index++, user.getEmail());
 			stmt.setString(index++, user.getEditor());
 			stmt.setString(index++, user.getSignature());
 			stmt.executeUpdate();
 			if (this.autoIncrementPrimaryKeys()) {
 				ResultSet rs = stmt.getGeneratedKeys();
 				if (!rs.next()) {
 					throw new SQLException("Unable to determine auto-generated ID for database record");
 				}
 				user.setUserId(rs.getInt(1));
 			}
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<Category> lookupCategoryTopics(int virtualWikiId, String virtualWikiName, String categoryName) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_CATEGORY_TOPICS);
 			// category name must be lowercase since search is case-insensitive
 			categoryName = categoryName.toLowerCase();
 			stmt.setInt(1, virtualWikiId);
 			stmt.setString(2, categoryName);
 			rs = stmt.executeQuery();
 			List<Category> results = new ArrayList<Category>();
 			while (rs.next()) {
 				results.add(this.initCategory(rs, virtualWikiName));
 			}
 			return results;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public Topic lookupTopic(int virtualWikiId, String virtualWikiName, String topicName, boolean caseSensitive, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			if (caseSensitive) {
 				stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC);
 			} else {
 				topicName = topicName.toLowerCase();
 				stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_LOWER);
 			}
 			stmt.setInt(1, virtualWikiId);
 			stmt.setString(2, topicName);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? this.initTopic(rs, virtualWikiName) : null;
 		} finally {
 			// close only the statement and result set - leave the connection open for further use
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<String> lookupTopicByType(int virtualWikiId, int topicType, Pagination pagination) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_BY_TYPE);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setInt(2, topicType);
 			stmt.setInt(3, pagination.getNumResults());
 			stmt.setInt(4, pagination.getOffset());
 			rs = stmt.executeQuery();
 			List<String> results = new ArrayList<String>();
 			while (rs.next()) {
 				results.add(rs.getString("topic_name"));
 			}
 			return results;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public int lookupTopicCount(int virtualWikiId) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_COUNT);
 			stmt.setInt(1, virtualWikiId);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? rs.getInt("topic_count") : 0;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public TopicVersion lookupTopicVersion(int topicVersionId) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_TOPIC_VERSION);
 			stmt.setInt(1, topicVersionId);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? this.initTopicVersion(rs) : null;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public WikiFile lookupWikiFile(int virtualWikiId, String virtualWikiName, int topicId) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_FILE);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setInt(2, topicId);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? this.initWikiFile(rs, virtualWikiName) : null;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 * Return a count of all wiki files currently available on the Wiki.  This
 	 * method excludes deleted files.
 	 *
 	 * @param virtualWikiId The virtual wiki id for the virtual wiki of the files
 	 *  being retrieved.
 	 */
 	public int lookupWikiFileCount(int virtualWikiId) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_FILE_COUNT);
 			stmt.setInt(1, virtualWikiId);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? rs.getInt("file_count") : 0;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public WikiGroup lookupWikiGroup(String groupName) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_GROUP);
 			stmt.setString(1, groupName);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? this.initWikiGroup(rs) : null;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public WikiUser lookupWikiUser(int userId) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER);
 			stmt.setInt(1, userId);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? this.initWikiUser(rs) : null;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public int lookupWikiUser(String username, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER_LOGIN);
 			stmt.setString(1, username);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? rs.getInt("wiki_user_id") : -1;
 		} finally {
 			// close only the statement and result set - leave the connection open for further use
 			DatabaseConnection.closeConnection(null, stmt, rs);
 		}
 	}
 
 	/**
 	 * Return a count of all wiki users.
 	 */
 	public int lookupWikiUserCount() throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER_COUNT);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? rs.getInt("user_count") : 0;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public String lookupWikiUserEncryptedPassword(String username) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USER_DETAILS_PASSWORD);
 			stmt.setString(1, username);
 			rs = stmt.executeQuery();
 			return (rs.next()) ? rs.getString("password") : null;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public List<String> lookupWikiUsers(Pagination pagination) throws SQLException {
 		Connection conn = null;
 		PreparedStatement stmt = null;
 		ResultSet rs = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			stmt = conn.prepareStatement(STATEMENT_SELECT_WIKI_USERS);
 			stmt.setInt(1, pagination.getNumResults());
 			stmt.setInt(2, pagination.getOffset());
 			rs = stmt.executeQuery();
 			List<String> results = new ArrayList<String>();
 			while (rs.next()) {
 				results.add(rs.getString("login"));
 			}
 			return results;
 		} finally {
 			DatabaseConnection.closeConnection(conn, stmt, rs);
 		}
 	}
 
 	/**
 	 * Retrieve the next available group member id from the group members table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available group member id from the group members table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 */
 	private int nextGroupMemberId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_GROUP_MEMBERS_SEQUENCE, "id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 * Retrieve the next available topic id from the topic table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available topic id from the topic table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 */
 	private int nextTopicId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_TOPIC_SEQUENCE, "topic_id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 * Retrieve the next available topic version id from the topic version table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available topic version id from the topic version table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 */
 	private int nextTopicVersionId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_TOPIC_VERSION_SEQUENCE, "topic_version_id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 * Retrieve the next available virtual wiki id from the virtual wiki table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available virtual wiki id from the virtual wiki table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 */
 	private int nextVirtualWikiId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_VIRTUAL_WIKI_SEQUENCE, "virtual_wiki_id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 * Retrieve the next available wiki file id from the wiki file table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available wiki file id from the wiki file table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 */
 	private int nextWikiFileId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_WIKI_FILE_SEQUENCE, "file_id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 * Retrieve the next available wiki file version id from the wiki file
 	 * version table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available wiki file version id from the wiki file
 	 *  version table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 */
 	private int nextWikiFileVersionId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_WIKI_FILE_VERSION_SEQUENCE, "file_version_id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 * Retrieve the next available wiki group id from the wiki group table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available wiki group id from the wiki group table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 */
 	private int nextWikiGroupId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_GROUP_SEQUENCE, "group_id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 * Retrieve the next available wiki user id from the wiki user table.
 	 *
 	 * @param conn A database connection to use when connecting to the database
 	 *  from this method.
 	 * @return The next available wiki user id from the wiki user table.
 	 * @throws SQLException Thrown if any error occurs during method execution.
 	 */
 	private int nextWikiUserId(Connection conn) throws SQLException {
 		int nextId = DatabaseConnection.executeSequenceQuery(STATEMENT_SELECT_WIKI_USER_SEQUENCE, "wiki_user_id", conn);
 		// note - this returns the last id in the system, so add one
 		return nextId + 1;
 	}
 
 	/**
 	 *
 	 */
 	public void reloadLogItems(int virtualWikiId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_DELETE_LOG_ITEMS);
 			stmt.setInt(1, virtualWikiId);
 			stmt.executeUpdate();
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEMS_BY_TOPIC_VERSION_TYPE);
 			stmt.setInt(1, LogItem.LOG_TYPE_DELETE);
 			stmt.setString(2, "");
 			stmt.setInt(3, virtualWikiId);
 			stmt.setInt(4, TopicVersion.EDIT_DELETE);
 			stmt.executeUpdate();
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEMS_BY_TOPIC_VERSION_TYPE);
 			stmt.setInt(1, LogItem.LOG_TYPE_DELETE);
 			stmt.setString(2, "|" + TopicVersion.EDIT_UNDELETE);
 			stmt.setInt(3, virtualWikiId);
 			stmt.setInt(4, TopicVersion.EDIT_UNDELETE);
 			stmt.executeUpdate();
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEMS_BY_TOPIC_VERSION_TYPE);
 			stmt.setInt(1, LogItem.LOG_TYPE_PERMISSION);
 			stmt.setString(2, "");
 			stmt.setInt(3, virtualWikiId);
 			stmt.setInt(4, TopicVersion.EDIT_PERMISSION);
 			stmt.executeUpdate();
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEMS_IMPORT);
 			stmt.setInt(1, LogItem.LOG_TYPE_IMPORT);
 			stmt.setInt(2, TopicVersion.EDIT_IMPORT);
 			stmt.setInt(3, virtualWikiId);
 			stmt.setInt(4, TopicVersion.EDIT_IMPORT);
 			stmt.executeUpdate();
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEMS_MOVE);
 			stmt.setInt(1, LogItem.LOG_TYPE_MOVE);
 			stmt.setInt(2, virtualWikiId);
 			stmt.setInt(3, TopicVersion.EDIT_MOVE);
 			stmt.executeUpdate();
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEMS_UPLOAD);
 			stmt.setInt(1, LogItem.LOG_TYPE_UPLOAD);
 			stmt.setInt(2, virtualWikiId);
 			stmt.setInt(3, TopicVersion.EDIT_NORMAL);
 			stmt.executeUpdate();
 			stmt = conn.prepareStatement(STATEMENT_INSERT_LOG_ITEMS_USER);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setInt(2, LogItem.LOG_TYPE_USER_CREATION);
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void reloadRecentChanges(Connection conn) throws SQLException {
 		DatabaseConnection.executeUpdate(STATEMENT_DELETE_RECENT_CHANGES, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_INSERT_RECENT_CHANGES_VERSIONS, conn);
 		DatabaseConnection.executeUpdate(STATEMENT_INSERT_RECENT_CHANGES_LOGS, conn);
 	}
 
 	/**
 	 *
 	 */
 	public void updateRole(Role role, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_ROLE);
 			stmt.setString(1, role.getDescription());
 			stmt.setString(2, role.getAuthority());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateTopic(Topic topic, int virtualWikiId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_TOPIC);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setString(2, topic.getName());
 			stmt.setInt(3, topic.getTopicType());
 			stmt.setInt(4, (topic.getReadOnly() ? 1 : 0));
 			if (topic.getCurrentVersionId() == null) {
 				stmt.setNull(5, Types.INTEGER);
 			} else {
 				stmt.setInt(5, topic.getCurrentVersionId());
 			}
 			stmt.setTimestamp(6, topic.getDeleteDate());
 			stmt.setInt(7, (topic.getAdminOnly() ? 1 : 0));
 			stmt.setString(8, topic.getRedirectTo());
 			stmt.setInt(9, topic.getTopicId());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateUserDetails(WikiUserDetails userDetails, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_USER);
 			stmt.setString(1, userDetails.getPassword());
 			stmt.setInt(2, 1);
 			stmt.setString(3, userDetails.getUsername());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateVirtualWiki(VirtualWiki virtualWiki, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_VIRTUAL_WIKI);
 			stmt.setString(1, virtualWiki.getDefaultTopicName());
 			stmt.setInt(2, virtualWiki.getVirtualWikiId());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateWikiFile(WikiFile wikiFile, int virtualWikiId, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_WIKI_FILE);
 			stmt.setInt(1, virtualWikiId);
 			stmt.setString(2, wikiFile.getFileName());
 			stmt.setString(3, wikiFile.getUrl());
 			stmt.setString(4, wikiFile.getMimeType());
 			stmt.setInt(5, wikiFile.getTopicId());
 			stmt.setTimestamp(6, wikiFile.getDeleteDate());
 			stmt.setInt(7, (wikiFile.getReadOnly() ? 1 : 0));
 			stmt.setInt(8, (wikiFile.getAdminOnly() ? 1 : 0));
 			stmt.setLong(9, wikiFile.getFileSize());
 			stmt.setInt(10, wikiFile.getFileId());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateWikiGroup(WikiGroup group, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_GROUP);
 			stmt.setString(1, group.getName());
 			stmt.setString(2, group.getDescription());
 			stmt.setInt(3, group.getGroupId());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 
 	/**
 	 *
 	 */
 	public void updateWikiUser(WikiUser user, Connection conn) throws SQLException {
 		PreparedStatement stmt = null;
 		try {
 			stmt = conn.prepareStatement(STATEMENT_UPDATE_WIKI_USER);
 			stmt.setString(1, user.getUsername());
 			stmt.setString(2, user.getDisplayName());
 			stmt.setTimestamp(3, user.getLastLoginDate());
 			stmt.setString(4, user.getLastLoginIpAddress());
 			stmt.setString(5, user.getDefaultLocale());
 			stmt.setString(6, user.getEmail());
 			stmt.setString(7, user.getEditor());
 			stmt.setString(8, user.getSignature());
 			stmt.setInt(9, user.getUserId());
 			stmt.executeUpdate();
 		} finally {
 			DatabaseConnection.closeStatement(stmt);
 		}
 	}
 }

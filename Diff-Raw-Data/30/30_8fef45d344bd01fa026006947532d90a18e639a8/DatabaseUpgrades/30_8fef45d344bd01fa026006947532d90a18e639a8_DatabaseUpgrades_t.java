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
 import java.sql.Timestamp;
 import java.util.Vector;
 import org.jamwiki.Environment;
 import org.jamwiki.WikiBase;
 import org.jamwiki.model.WikiUser;
 import org.jamwiki.utils.Encryption;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  * This class simply contains utility methods for upgrading database schemas
  * (if needed) between JAMWiki versions.  In general upgrade methods will only
  * be maintained for a few versions and then deleted - for example, JAMWiki version 10.0.0
  * does not need to keep the upgrade methods from JAMWiki 0.0.1 around.
  */
 public class DatabaseUpgrades {
 
 	private static WikiLogger logger = WikiLogger.getLogger(DatabaseUpgrades.class.getName());
 
 	/**
 	 *
 	 */
 	public static WikiUser login(String username, String password, boolean encrypted) throws Exception {
 		// prior to JAMWiki 0.5.0 the remember_key column did not exist.  once
 		// the ability to upgrade to JAMWiki 0.5.0 is removed this code can be
 		// replaced with the method (below) that has been commented out
 		String encryptedPassword = password;
 		if (!encrypted) {
 			encryptedPassword = Encryption.encrypt(password);
 		}
 		AnsiQueryHandler queryHandler = new AnsiQueryHandler();
 		WikiResultSet rs = queryHandler.lookupWikiUser(username, encryptedPassword);
 		if (rs.size() == 0) return null;
 		int userId = rs.getInt("wiki_user_id");
 		String sql = "select * from jam_wiki_user where wiki_user_id = ? ";
 		WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
 		stmt.setInt(1, userId);
 		rs = stmt.executeQuery();
 		WikiUser user = new WikiUser();
 		user.setUserId(rs.getInt("wiki_user_id"));
 		user.setUsername(rs.getString("login"));
 		user.setDisplayName(rs.getString("display_name"));
 		user.setCreateDate(rs.getTimestamp("create_date"));
 		user.setLastLoginDate(rs.getTimestamp("last_login_date"));
 		user.setCreateIpAddress(rs.getString("create_ip_address"));
 		user.setLastLoginIpAddress(rs.getString("last_login_ip_address"));
 		user.setAdmin(rs.getInt("is_admin") != 0);
 //		user = WikiBase.getDataHandler().lookupWikiUser(username, password, false);
 		return user;
 	}
 
 	/**
 	 *
 	 */
 	public static Vector upgrade030(Vector messages) throws Exception {
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			conn.setAutoCommit(false);
 			String sql = "drop table jam_image";
 			DatabaseConnection.executeUpdate(sql, conn);
 			// FIXME - hard coding
 			messages.add("Dropped jam_image table");
 			DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_CREATE_CATEGORY_TABLE, conn);
 			// FIXME - hard coding
 			messages.add("Added jam_category table");
 			conn.commit();
 		} catch (Exception e) {
 			try {
 				DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_DROP_CATEGORY_TABLE, conn);
 			} catch (Exception ex) {}
 			DatabaseConnection.handleErrors(conn);
 			throw e;
 		} finally {
 			DatabaseConnection.closeConnection(conn);
 		}
 		return messages;
 	}
 
 	/**
 	 *
 	 */
 	public static Vector upgrade031(Vector messages) throws Exception {
 		Connection conn = null;
 		try {
 			// FIXME - hard coding
 			conn = DatabaseConnection.getConnection();
 			conn.setAutoCommit(false);
 			// add redirection column
 			String sql = "alter table jam_topic add column redirect_to VARCHAR(200) ";
 			DatabaseConnection.executeUpdate(sql, conn);
 			messages.add("Added redirect_to column to table jam_topic");
 			// convert topic_deleted (int) to delete_date (timestamp)
 			if (Environment.getValue(Environment.PROP_DB_TYPE).equals(WikiBase.DATA_HANDLER_MYSQL)) {
 				sql = "alter table jam_topic add column delete_date DATETIME ";
 			} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(WikiBase.DATA_HANDLER_MSSQL)) {
 				sql = "alter table jam_topic add column delete_date DATETIME ";
			} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(WikiBase.DATA_HANDLER_ORACLE)) {
				sql = "alter table jam_topic add (delete_date TIMESTAMP) ";
 			} else {
 				sql = "alter table jam_topic add column delete_date TIMESTAMP ";
 			}
 			DatabaseConnection.executeUpdate(sql, conn);
 			messages.add("Added delete_date column to table jam_topic");
 			if (Environment.getValue(Environment.PROP_DB_TYPE).equals(WikiBase.DATA_HANDLER_MYSQL)) {
 				sql = "alter table jam_topic drop index jam_unique_topic_name_vwiki ";
 			} else {
 				sql = "alter table jam_topic drop constraint jam_unique_topic_name_vwiki ";
 			}
 			DatabaseConnection.executeUpdate(sql, conn);
 			if (Environment.getValue(Environment.PROP_DB_TYPE).equals(WikiBase.DATA_HANDLER_MYSQL)) {
 				sql = "create unique index jam_unique_topic_name_vwiki on jam_topic(topic_name, virtual_wiki_id, delete_date) ";
 			} else {
 				sql = "alter table jam_topic add constraint jam_unique_topic_name_vwiki UNIQUE (topic_name, virtual_wiki_id, delete_date) ";
 			}
 			DatabaseConnection.executeUpdate(sql, conn);
 			messages.add("Updated unique topic name constraint");
 			sql = "update jam_topic set delete_date = ? where topic_deleted = '1' ";
 			WikiPreparedStatement stmt = new WikiPreparedStatement(sql);
 			stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
 			stmt.executeUpdate(conn);
 			messages.add("Updated deleted topics in jam_topic");
 			sql = "alter table jam_topic drop column topic_deleted ";
 			DatabaseConnection.executeUpdate(sql, conn);
 			messages.add("Dropped column topic_deleted from table jam_topic");
 			// convert file_deleted (int) to file_deleted (timestamp)
 			if (Environment.getValue(Environment.PROP_DB_TYPE).equals(WikiBase.DATA_HANDLER_MYSQL)) {
 				sql = "alter table jam_file add column delete_date DATETIME ";
 			} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(WikiBase.DATA_HANDLER_MSSQL)) {
 				sql = "alter table jam_file add column delete_date DATETIME ";
			} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(WikiBase.DATA_HANDLER_ORACLE)) {
				sql = "alter table jam_file add (delete_date DATETIME) ";
 			} else {
 				sql = "alter table jam_file add column delete_date TIMESTAMP ";
 			}
 			DatabaseConnection.executeUpdate(sql, conn);
 			messages.add("Added delete_date column to table jam_file");
 			sql = "update jam_file set delete_date = ? where file_deleted = '1' ";
 			stmt = new WikiPreparedStatement(sql);
 			stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
 			stmt.executeUpdate(conn);
 			messages.add("Updated deleted files in jam_file");
 			sql = "alter table jam_file drop column file_deleted ";
 			DatabaseConnection.executeUpdate(sql, conn);
 			messages.add("Dropped column file_deleted from table jam_file");
 			// make user login constraint "lower(login)"
 			if (Environment.getValue(Environment.PROP_DB_TYPE).equals(WikiBase.DATA_HANDLER_MYSQL)) {
 				sql = "alter table jam_wiki_user drop index jam_unique_wiki_user_login ";
 			} else {
 				sql = "alter table jam_wiki_user drop constraint jam_unique_wiki_user_login ";
 			}
 			DatabaseConnection.executeUpdate(sql, conn);
 			DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_CREATE_WIKI_USER_LOGIN_INDEX, conn);
 			messages.add("Updated unique wiki user login constraint");
 			conn.commit();
 		} catch (Exception e) {
 			DatabaseConnection.handleErrors(conn);
 			throw e;
 		} finally {
 			DatabaseConnection.closeConnection(conn);
 		}
 		return messages;
 	}
 
 	/**
 	 *
 	 */
 	public static Vector upgrade042(Vector messages) throws Exception {
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			conn.setAutoCommit(false);
 			// drop topic_content column
 			String sql = "alter table jam_topic drop column topic_content ";
 			DatabaseConnection.executeUpdate(sql, conn);
 			messages.add("Dropped topic_content column from jam_topic");
 			// add current_version_id column
			if (Environment.getValue(Environment.PROP_DB_TYPE).equals(WikiBase.DATA_HANDLER_ORACLE)) {
				sql = "alter table jam_topic add (current_version_id INTEGER) ";
			} else {
				sql = "alter table jam_topic add column current_version_id INTEGER ";
			}
 			DatabaseConnection.executeUpdate(sql, conn);
 			messages.add("Added current_version_id column to jam_topic");
 			// add current_version_id constraint
 			DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_CREATE_TOPIC_CURRENT_VERSION_CONSTRAINT, conn);
 			messages.add("Added jam_fk_topic_topic_ver constraint to jam_topic");
 			// update jam_topic records
 			DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_UPDATE_TOPIC_CURRENT_VERSIONS, conn);
 			messages.add("Added current_version_id values for jam_topic records");
 			// create the jam_watchlist table
 			DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_CREATE_WATCHLIST_TABLE, conn);
 			messages.add("Created watchlist table");
 			conn.commit();
 		} catch (Exception e) {
 			DatabaseConnection.handleErrors(conn);
 			DatabaseConnection.executeUpdate(AnsiQueryHandler.STATEMENT_DROP_WATCHLIST_TABLE);
 			throw e;
 		} finally {
 			DatabaseConnection.closeConnection(conn);
 		}
 		return messages;
 	}
 
 	/**
 	 *
 	 */
 	public static Vector upgrade050(Vector messages) throws Exception {
 		Connection conn = null;
 		try {
 			conn = DatabaseConnection.getConnection();
 			conn.setAutoCommit(false);
 			// add remember_key to jam_wiki_user
			String sql = "";
			if (Environment.getValue(Environment.PROP_DB_TYPE).equals(WikiBase.DATA_HANDLER_ORACLE)) {
				sql = "alter table jam_wiki_user add (remember_key VARCHAR(100)) ";
			} else {
				sql = "alter table jam_wiki_user add column remember_key VARCHAR(100) ";
			}
 			DatabaseConnection.executeUpdate(sql, conn);
 			messages.add("Added remember_key column to jam_wiki_user");
 			// populate remember_key column
 			sql = "update jam_wiki_user set remember_key = (select encoded_password from jam_wiki_user_info where jam_wiki_user.wiki_user_id = jam_wiki_user_info.wiki_user_id) ";
 			DatabaseConnection.executeUpdate(sql, conn);
 			messages.add("Populated the remember_key column with data");
 			// set column not null
 			if (Environment.getValue(Environment.PROP_DB_TYPE).equals(WikiBase.DATA_HANDLER_MYSQL)) {
 				sql = "alter table jam_wiki_user MODIFY COLUMN remember_key VARCHAR(100) NOT NULL ";
 			} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(WikiBase.DATA_HANDLER_MSSQL)) {
 				sql = "alter table jam_wiki_user ALTER COLUMN remember_key VARCHAR(100) NOT NULL ";
			} else if (Environment.getValue(Environment.PROP_DB_TYPE).equals(WikiBase.DATA_HANDLER_ORACLE)) {
				sql = "alter table jam_wiki_user modify (remember_key VARCHAR(100) NOT NULL) ";
 			} else {
 				sql = "alter table jam_wiki_user ALTER COLUMN remember_key SET NOT NULL ";
 			}
 			DatabaseConnection.executeUpdate(sql, conn);
 			messages.add("remember_key column set to NOT NULL");
 			// add default_locale column
			if (Environment.getValue(Environment.PROP_DB_TYPE).equals(WikiBase.DATA_HANDLER_ORACLE)) {
				sql = "alter table jam_wiki_user add (default_locale VARCHAR(8)) ";
			} else {
				sql = "alter table jam_wiki_user add column default_locale VARCHAR(8) ";
			}
 			DatabaseConnection.executeUpdate(sql, conn);
 			messages.add("Added default_locale column to jam_wiki_user");
 			conn.commit();
 		} catch (Exception e) {
 			DatabaseConnection.handleErrors(conn);
 			throw e;
 		} finally {
 			DatabaseConnection.closeConnection(conn);
 		}
 		return messages;
 	}
 }

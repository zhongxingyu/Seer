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
 import org.jamwiki.UserHandler;
 import org.jamwiki.model.WikiUserInfo;
 import org.jamwiki.utils.Encryption;
 import org.jamwiki.utils.WikiLogger;
 import org.springframework.transaction.TransactionStatus;
 
 /**
  * Implementation of the {@link org.jamwiki.UserHandler} interface that uses a
  * database for storing user login, password and other basic user information.
  */
 public class DatabaseUserHandler implements UserHandler {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(DatabaseUserHandler.class.getName());
 
 	/**
 	 *
 	 */
 	public void addWikiUserInfo(WikiUserInfo userInfo, Object transactionObject) throws Exception {
 		TransactionStatus status = DatabaseConnection.startTransaction();
 		try {
 			Connection conn = DatabaseConnection.getConnection();
 			WikiDatabase.queryHandler().insertWikiUserInfo(userInfo, conn);
 		} catch (Exception e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (Error e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		}
 		DatabaseConnection.commit(status);
 	}
 
 	/**
 	 *
 	 */
 	public boolean authenticate(String username, String password) throws Exception {
 		boolean result = false;
 		TransactionStatus status = DatabaseConnection.startTransaction();
 		try {
 			Connection conn = DatabaseConnection.getConnection();
 			// password is stored encrypted, so encrypt password
 			String encryptedPassword = Encryption.encrypt(password);
 			WikiResultSet rs = WikiDatabase.queryHandler().lookupWikiUser(username, encryptedPassword, conn);
 			result = (rs.size() == 0) ? false : true;
 		} catch (Exception e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (Error err) {
 			DatabaseConnection.rollbackOnException(status, err);
 			throw err;
 		}
 		DatabaseConnection.commit(status);
 		return result;
 	}
 
 	/**
 	 *
 	 */
 	private WikiUserInfo initWikiUserInfo(WikiResultSet rs) throws Exception {
 		WikiUserInfo userInfo = new WikiUserInfo();
 		userInfo.setUserId(rs.getInt(AnsiDataHandler.DATA_WIKI_USER_ID));
 		userInfo.setUsername(rs.getString("login"));
 		userInfo.setEmail(rs.getString("email"));
 		userInfo.setFirstName(rs.getString("first_name"));
 		userInfo.setLastName(rs.getString("last_name"));
 		userInfo.setEncodedPassword(rs.getString("encoded_password"));
 		return userInfo;
 	}
 
 	/**
 	 *
 	 */
 	public boolean isWriteable() {
 		return true;
 	}
 
 	/**
 	 *
 	 */
 	public WikiUserInfo lookupWikiUserInfo(String username) throws Exception {
 		WikiResultSet rs = WikiDatabase.queryHandler().lookupWikiUserInfo(username);
 		return (rs.size() == 0) ? null : initWikiUserInfo(rs);
 	}
 
 	/**
 	 *
 	 */
 	public void updateWikiUserInfo(WikiUserInfo userInfo, Object transactionObject) throws Exception {
 		TransactionStatus status = DatabaseConnection.startTransaction();
 		try {
 			Connection conn = DatabaseConnection.getConnection();
 			WikiDatabase.queryHandler().updateWikiUserInfo(userInfo, conn);
 		} catch (Exception e) {
 			DatabaseConnection.rollbackOnException(status, e);
 			throw e;
 		} catch (Error err) {
 			DatabaseConnection.rollbackOnException(status, err);
 			throw err;
 		}
 		DatabaseConnection.commit(status);
 	}
 }

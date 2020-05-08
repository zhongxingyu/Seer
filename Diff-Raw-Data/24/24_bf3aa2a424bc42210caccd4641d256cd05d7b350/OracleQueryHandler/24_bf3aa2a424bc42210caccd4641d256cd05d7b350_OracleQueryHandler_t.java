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
 
 import java.util.Properties;
 import org.jamwiki.Environment;
 import org.jamwiki.utils.Pagination;
 import org.jamwiki.utils.Utilities;
 import org.jamwiki.utils.WikiLogger;
 
 /**
  *
  */
 public class OracleQueryHandler extends DefaultQueryHandler {
 
 	private static WikiLogger logger = WikiLogger.getLogger(OracleQueryHandler.class.getName());
 	private static final String SQL_PROPERTY_FILE_NAME = "sql.oracle.properties";
 	private static Properties props = null;
 	private static Properties defaults = null;
 
 	/**
 	 *
 	 */
 	protected OracleQueryHandler() {
 		defaults = Environment.loadProperties(DefaultQueryHandler.SQL_PROPERTY_FILE_NAME);
 		props = Environment.loadProperties(SQL_PROPERTY_FILE_NAME, defaults);
 		super.init(props);
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet getCategories(int virtualWikiId, Pagination pagination) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_CATEGORIES);
 		stmt.setInt(1, virtualWikiId);
		stmt.setInt(2, pagination.getEnd());
		stmt.setInt(3, pagination.getStart());
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet getRecentChanges(String virtualWiki, Pagination pagination, boolean descending) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_RECENT_CHANGES);
 		stmt.setString(1, virtualWiki);
		stmt.setInt(2, pagination.getEnd());
		stmt.setInt(3, pagination.getStart());
 		// FIXME - sort order ignored
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet getRecentChanges(int topicId, Pagination pagination, boolean descending) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_RECENT_CHANGES_TOPIC);
 		stmt.setInt(1, topicId);
		stmt.setInt(2, pagination.getEnd());
		stmt.setInt(3, pagination.getStart());
 		// FIXME - sort order ignored
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet getUserContributions(String virtualWiki, String userString, Pagination pagination, boolean descending) throws Exception {
 		WikiPreparedStatement stmt = null;
 		if (Utilities.isIpAddress(userString)) {
 			stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_ANONYMOUS);
 		} else {
 			stmt = new WikiPreparedStatement(STATEMENT_SELECT_WIKI_USER_CHANGES_LOGIN);
 		}
 		stmt.setString(1, virtualWiki);
 		stmt.setString(2, userString);
		stmt.setInt(3, pagination.getEnd());
		stmt.setInt(4, pagination.getStart());
 		// FIXME - sort order ignored
 		return stmt.executeQuery();
 	}
 
 	/**
 	 *
 	 */
 	public WikiResultSet lookupTopicByType(int virtualWikiId, int topicType, Pagination pagination) throws Exception {
 		WikiPreparedStatement stmt = new WikiPreparedStatement(STATEMENT_SELECT_TOPIC_BY_TYPE);
 		stmt.setInt(1, virtualWikiId);
 		stmt.setInt(2, topicType);
		stmt.setInt(3, pagination.getEnd());
		stmt.setInt(4, pagination.getStart());
 		return stmt.executeQuery();
 	}
 }

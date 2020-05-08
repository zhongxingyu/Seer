 /*
  * Copyright (C) 2011 Openismus GmbH
  *
  * This file is part of GWT-Glom.
  *
  * GWT-Glom is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published by the
  * Free Software Foundation, either version 3 of the License, or (at your
  * option) any later version.
  *
  * GWT-Glom is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
  * for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with GWT-Glom.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.glom.web.server;
 
 import java.beans.PropertyVetoException;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import org.glom.libglom.Document;
 
 import com.mchange.v2.c3p0.ComboPooledDataSource;
 
 /**
  * A class to hold configuration information for related to the glom document and db access.
  * 
  * @author Ben Konrath <ben@bagu.org>
  * 
  */
 final class ConfiguredDocument {
 
 	private Document document;
 	private ComboPooledDataSource cpds;
 	private boolean authenticated = false;
 
 	@SuppressWarnings("unused")
 	private ConfiguredDocument() {
 	}
 
 	public ConfiguredDocument(Document document) throws PropertyVetoException {
 
 		// load the jdbc driver
 		cpds = new ComboPooledDataSource();
 		try {
 			cpds.setDriverClass("org.postgresql.Driver");
 		} catch (PropertyVetoException e) {
 			Log.fatal("Error loading the PostgreSQL JDBC driver."
 					+ " Is the PostgreSQL JDBC jar available to the servlet?", e);
 			throw e;
 		}
 
 		// setup the JDBC driver for the current glom document
 		cpds.setJdbcUrl("jdbc:postgresql://" + document.get_connection_server() + "/"
 				+ document.get_connection_database());
 
 		this.document = document;
 	}
 
 	/**
 	 * Sets the username and password for the database associated with the Glom document.
 	 * 
 	 * @return true if the username and password works, false otherwise
 	 */
 	boolean setUsernameAndPassword(String username, String password) throws SQLException {
 		cpds.setUser(username);
 		cpds.setPassword(password);
 
 		int acquireRetryAttempts = cpds.getAcquireRetryAttempts();
 		cpds.setAcquireRetryAttempts(1);
 		Connection conn = null;
 		try {
 			// FIXME find a better way to check authentication
 			// it's possible that the connection could be failing for another reason
 			conn = cpds.getConnection();
 			authenticated = true;
 		} catch (SQLException e) {
 			Log.info(document.get_database_title(), "Username and/or password are not correct.");
 			authenticated = false;
 		} finally {
 			if (conn != null)
 				conn.close();
 			cpds.setAcquireRetryAttempts(acquireRetryAttempts);
 		}
		return false;
 	}
 
 	public Document getDocument() {
 		return document;
 	}
 
 	public ComboPooledDataSource getCpds() {
 		return cpds;
 	}
 
 	public boolean isAuthenticated() {
 		return authenticated;
 	}
 
 }

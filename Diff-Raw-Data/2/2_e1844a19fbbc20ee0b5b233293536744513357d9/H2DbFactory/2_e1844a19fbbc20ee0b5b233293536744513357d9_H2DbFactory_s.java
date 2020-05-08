 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Zend Technologies
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.index.sql.h2;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.core.index.sql.DbFactory;
 import org.eclipse.dltk.core.index.sql.IContainerDao;
 import org.eclipse.dltk.core.index.sql.IElementDao;
 import org.eclipse.dltk.core.index.sql.IFileDao;
 import org.eclipse.dltk.core.index.sql.h2.H2Index;
 import org.eclipse.dltk.core.index.sql.h2.H2IndexPreferences;
 import org.h2.jdbcx.JdbcConnectionPool;
 import org.h2.tools.DeleteDbFiles;
 
 /**
  * Abstract database access factory
  * 
  * @author michael
  * 
  */
 public class H2DbFactory extends DbFactory {
 
 	private static final String DB_NAME = "model"; //$NON-NLS-1$
 	private static final String DB_USER = ""; //$NON-NLS-1$
 	private static final String DB_PASS = ""; //$NON-NLS-1$
 	private JdbcConnectionPool pool;
 
 	public H2DbFactory() throws SQLException {
 		try {
 			Class.forName("org.h2.Driver");
 		} catch (ClassNotFoundException e) {
 		}
 
 		IPath dbPath = H2Index.getDefault().getStateLocation();
 
 		int cacheSize = Platform.getPreferencesService().getInt(
 				H2Index.PLUGIN_ID, H2IndexPreferences.DB_CACHE_SIZE, 0, null);
 		String cacheType = Platform.getPreferencesService()
 				.getString(H2Index.PLUGIN_ID, H2IndexPreferences.DB_CACHE_TYPE,
 						null, null);
 
 		String connString = new StringBuilder("jdbc:h2:").append(
 				dbPath.append(DB_NAME).toOSString()).append(
				";UNDO_LOG=0;LOCK_MODE=0;LOG=0;CACHE_TYPE=").append(cacheType)
 				.append(";CACHE_SIZE=").append(cacheSize).toString();
 
 		pool = JdbcConnectionPool.create(connString, DB_USER, DB_PASS);
 
 		Schema schema = new Schema();
 		boolean initializeSchema = false;
 
 		Connection connection = pool.getConnection();
 		try {
 			Statement statement = connection.createStatement();
 			try {
 				statement.executeQuery("SELECT COUNT(*) FROM FILES WHERE 1=0;");
 				initializeSchema = !schema.isCompatible();
 
 			} catch (SQLException e) {
 				// Basic table doesn't exist
 				initializeSchema = true;
 			} finally {
 				statement.close();
 			}
 
 			if (initializeSchema) {
 				connection.close();
 				pool.dispose();
 				DeleteDbFiles.execute(dbPath.toOSString(), DB_NAME, true);
 
 				pool = JdbcConnectionPool.create(connString, DB_USER, DB_PASS);
 				connection = pool.getConnection();
 				schema.initialize(connection);
 			}
 		} finally {
 			connection.close();
 		}
 	}
 
 	public Connection createConnection() throws SQLException {
 		return pool == null ? null : pool.getConnection();
 	}
 
 	public void dispose() throws SQLException {
 		if (pool != null) {
 			pool.dispose();
 			pool = null;
 		}
 	}
 
 	public IContainerDao getContainerDao() {
 		return new H2ContainerDao();
 	}
 
 	public IElementDao getElementDao() {
 		return new H2ElementDao();
 	}
 
 	public IFileDao getFileDao() {
 		return new H2FileDao();
 	}
 }

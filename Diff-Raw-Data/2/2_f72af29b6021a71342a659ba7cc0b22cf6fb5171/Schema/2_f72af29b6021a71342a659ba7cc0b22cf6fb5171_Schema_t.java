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
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.dltk.core.index.sql.h2.H2Index;
 import org.eclipse.dltk.core.index.sql.h2.H2IndexPreferences;
 import org.eclipse.osgi.util.NLS;
 import org.osgi.service.prefs.BackingStoreException;
 
 /**
  * Utilities for initializing model schema
  * 
  * @author michael
  * 
  */
 public class Schema {
 
	public static final String VERSION = "0.7.1"; //$NON-NLS-1$
 
 	/** Contains already created tables names */
 	private static final Set<String> TABLES_CACHE = new HashSet<String>();
 
 	/**
 	 * Creates the database schema using given connection.
 	 * 
 	 * @param connection
 	 *            Database connection
 	 * @throws SQLException
 	 */
 	public void initialize(Connection connection) throws SQLException {
 		try {
 			Statement statement = connection.createStatement();
 			try {
 				statement.executeUpdate(readSqlFile("resources/basic.sql")); //$NON-NLS-1$
 			} finally {
 				statement.close();
 			}
 
 			// Store new schema version:
 			storeSchemaVersion(VERSION);
 
 		} catch (SQLException e) {
 			H2Index.error(
 					"An exception was thrown while initializing schema", e); //$NON-NLS-1$
 			throw e;
 		}
 	}
 
 	/**
 	 * Generate table name according to element type and language nature
 	 * 
 	 * @param elementType
 	 *            Element type
 	 * @param natureId
 	 *            Language nature
 	 * @param isReference
 	 *            Whether the element is reference or declaration
 	 * @return
 	 */
 	public String getTableName(int elementType, String natureId,
 			boolean isReference) {
 		StringBuilder tableName = new StringBuilder();
 		if (isReference) {
 			tableName.append("R_"); //$NON-NLS-1$
 		} else {
 			tableName.append("D_"); //$NON-NLS-1$
 		}
 		tableName.append(natureId.toUpperCase().replace('.', '_')).append('_')
 				.append(elementType).toString();
 		return tableName.toString();
 	}
 
 	/**
 	 * Creates elements table
 	 * 
 	 * @param connection
 	 *            Database connection
 	 * @param tableName
 	 *            Table name
 	 * @param isReference
 	 *            Whether to create table for element references or element
 	 *            declarations
 	 * @throws SQLException
 	 */
 	public void createTable(Connection connection, String tableName,
 			boolean isReference) throws SQLException {
 
 		synchronized (TABLES_CACHE) {
 			if (TABLES_CACHE.add(tableName)) {
 
 				String query = isReference ? readSqlFile("resources/element_ref.sql") //$NON-NLS-1$
 						: readSqlFile("resources/element_decl.sql"); //$NON-NLS-1$
 				query = NLS.bind(query, tableName);
 
 				try {
 					Statement statement = connection.createStatement();
 					try {
 						statement.executeUpdate(query);
 					} finally {
 						statement.close();
 					}
 				} catch (SQLException e) {
 					H2Index.error(
 							"An exception was thrown while creating elements table", //$NON-NLS-1$
 							e);
 					throw e;
 				}
 			}
 		}
 	}
 
 	/**
 	 * Checks whether the schema version is compatible with the stored one.
 	 */
 	public boolean isCompatible() {
 		String storedVersion = getStoredSchemaVersion();
 		if (storedVersion != null && VERSION.equals(storedVersion)) {
 			return true;
 		}
 		return false;
 	}
 
 	private String getStoredSchemaVersion() {
 		return Platform.getPreferencesService().getString(H2Index.PLUGIN_ID,
 				H2IndexPreferences.SCHEMA_VERSION, null, null);
 	}
 
 	private void storeSchemaVersion(String newVersion) {
 		IEclipsePreferences node = new InstanceScope()
 				.getNode(H2Index.PLUGIN_ID);
 		node.put(H2IndexPreferences.SCHEMA_VERSION, newVersion);
 		try {
 			node.flush();
 		} catch (BackingStoreException e) {
 		}
 	}
 
 	static String readSqlFile(String sqlFile) {
 		try {
 			URL url = FileLocator.find(H2Index.getDefault().getBundle(),
 					new Path(sqlFile), null);
 			URL resolved = FileLocator.resolve(url);
 
 			StringBuilder buf = new StringBuilder();
 			BufferedReader r = new BufferedReader(new InputStreamReader(
 					resolved.openStream()));
 			try {
 				String line;
 				while ((line = r.readLine()) != null) {
 					buf.append(line).append('\n');
 				}
 			} finally {
 				r.close();
 			}
 			return buf.toString();
 		} catch (IOException e) {
 			H2Index.error("An exception is thrown while reading file: " //$NON-NLS-1$
 					+ sqlFile, e);
 		}
 		return null;
 	}
 }

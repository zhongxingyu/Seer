 /**
  * This file is part of Jahia, next-generation open source CMS:
  * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
  * of enterprise application convergence - web, search, document, social and portal -
  * unified by the simplicity of web content management.
  *
  * For more information, please visit http://www.jahia.com.
  *
  * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  *
  * As a special exception to the terms and conditions of version 2.0 of
  * the GPL (or any later version), you may redistribute this Program in connection
  * with Free/Libre and Open Source Software ("FLOSS") applications as described
  * in Jahia's FLOSS exception. You should have received a copy of the text
  * describing the FLOSS exception, and it is also available here:
  * http://www.jahia.com/license
  *
  * Commercial and Supported Versions of the program (dual licensing):
  * alternatively, commercial and supported versions of the program may be used
  * in accordance with the terms and conditions contained in a separate
  * written agreement between you and Jahia Solutions Group SA.
  *
  * If you are unsure which license is appropriate for your use,
  * please contact the sales department at sales@jahia.com.
  */
 
 package org.jahia.modules.external.test.db;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.*;
 
 import javax.jcr.ItemNotFoundException;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.PropertyType;
 import javax.jcr.RepositoryException;
 import javax.jcr.Value;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.jackrabbit.core.util.db.DbUtility;
 import org.jahia.exceptions.JahiaRuntimeException;
 import org.jahia.modules.external.ExternalData;
 import org.jahia.modules.external.ExternalDataSource;
 import org.jahia.modules.external.ExternalDataSource.Initializable;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Base implementation of the external data source that uses sample database to fetch the data.
  * 
  * @author Sergiy Shyrkov
  */
 abstract class BaseDatabaseDataSource implements ExternalDataSource, Initializable {
     private static final String DB_URI = "jdbc:derby:classpath:toursdb";
 
     private static final Logger logger = LoggerFactory.getLogger(BaseDatabaseDataSource.class);
 
     @Override
     public final List<String> getChildren(String path) {
         String nodeType = null;
         try {
             nodeType = getNodeTypeName(path);
         } catch (PathNotFoundException e) {
             // cannot handle that path
             return Collections.emptyList();
         }
         if (nodeType.equals(getSchemaNodeType())) {
             return getTableNames();
         } else if (nodeType.equals(getTableNodeType())) {
             return getRowIDs(StringUtils.substringAfterLast(path, "/"), new HashMap<String, Value>());
         } else {
             return Collections.emptyList();
         }
     }
 
     /**
      * Returns the DB connection.
      * 
      * @return the DB connection
      */
     protected Connection getConnection() {
         try {
             return DriverManager.getConnection(DB_URI);
         } catch (Exception e) {
             logger.error(e.getMessage(), e);
             throw new JahiaRuntimeException(e);
         }
     }
 
     @Override
     public final ExternalData getItemByIdentifier(String identifier) throws ItemNotFoundException {
         if (identifier.startsWith("/") && !identifier.contains(":")) {
             try {
                 return getItemByPath(identifier);
             } catch (PathNotFoundException e) {
                 throw new ItemNotFoundException(identifier, e);
             }
         }
         throw new ItemNotFoundException(identifier);
     }
 
     @Override
     public final ExternalData getItemByPath(String path) throws PathNotFoundException {
         if (path.startsWith("/") && !path.contains(":")) {
             String type = getNodeTypeName(path);
 
             if (type.equals(getSchemaNodeType()) || type.equals(getTableNodeType())) {
                 if (type.equals(getTableNodeType()) &&  !getTableNames().contains(path.substring(1))) {
                     throw new PathNotFoundException(path);
                 }
                 return new ExternalData(path, path, type, Collections.<String, String[]>emptyMap());
             } else {
                 return getPropertiesForRow(path, type);
             }
         }
         throw new PathNotFoundException(path);
     }
 
     private String getNodeTypeName(String path) throws PathNotFoundException {
         if (path.length() <= 1) {
             return getSchemaNodeType();
         } else {
             String[] pathTokens = StringUtils.split(path, '/');
             if (pathTokens.length == 1) {
                 return getTableNodeType();
             } else if (pathTokens.length == 2) {
                 return getRowNodeTypeName(pathTokens[0]);
             } else {
                 throw new PathNotFoundException(path);
             }
         }
     }
 
     /**
      * Reads the properties for the specified row.
      * 
      * @param path
      *            the node path which corresponds to a table row
      * @return the ExternalData with property values for the specified row
      * @throws PathNotFoundException
      *             if the row cannot be found or the table cannot be handled by this data source
      */
     private ExternalData getPropertiesForRow(String path, String type) throws PathNotFoundException {
         String[] pathTokens = StringUtils.split(path, '/');
         if (pathTokens.length != 2) {
             throw new PathNotFoundException(path);
         }
         String table = pathTokens[0];
         String rowId = pathTokens[1];
 
         Connection conn = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
 
         try {
             conn = getConnection();
             List<String> primaryKeys = getTablePrimaryKeys(table, conn);
             if (primaryKeys.isEmpty()) {
                 stmt = conn.prepareStatement("select * from " + table);
                 rs = stmt.executeQuery();
                 int targetPos = Integer.parseInt(rowId);
                 int pos = 1;
                 while (rs.next() && pos < targetPos) {
                     pos++;
                 }
                 if (pos == targetPos) {
                     return getRowProperties(path, type, table, rs);
                 } else {
                     throw new PathNotFoundException(path);
                 }
             } else {
                 String query = null;
                 if (primaryKeys.size() == 1) {
                     query = "select * from " + table + " where " + primaryKeys.get(0) + "=?";
                 } else {
                     StringBuilder buff = new StringBuilder();
                     for (String col : primaryKeys) {
                         if (buff.length() > 0) {
                             buff.append(" and ");
                         }
                         buff.append(col).append("=?");
                     }
                     buff.insert(0, " where ").insert(0, table).insert(0, "select * from ");
                     query = buff.toString();
                 }
 
                 stmt = conn.prepareStatement(query);
                 String[] rowData = getValuesForPrimayKeys(rowId);
                 for (int i = 0; i < rowData.length; i++) {
                     stmt.setString(i + 1, rowData[i]);
                 }
                 rs = stmt.executeQuery();
                 if (rs.next()) {
                     return getRowProperties(path, type, table, rs);
                 } else {
                     throw new PathNotFoundException(path);
                 }
             }
         } catch (SQLException e) {
             logger.debug(e.getMessage(), e);
             throw new PathNotFoundException(path,e);
         } finally {
             DbUtility.close(conn, stmt, rs);
         }
     }
 
     /**
      * Returns the String ID of the current row.
      * 
      * @param rs
      *            the current result set positioned at the row in question
      * @param primaryKeys
      *            the list of primary keys of a table
      * @return the String ID of the current row
      * @throws SQLException
      *             in case of a DB operation error
      */
     protected abstract String getRowID(ResultSet rs, List<String> primaryKeys) throws SQLException;
 
     /**
      * Returns a list of row IDs (names) in the specified table.
      * 
      * 
      * @param tableName
      *            the name of the table to read rows from
      * @param constraints
      * @return a list of row IDs (names) in the specified table
      */
     protected final List<String> getRowIDs(String tableName, Map<String, Value> constraints) {
         List<String> ids = new LinkedList<String>();
         Connection conn = null;
         PreparedStatement stmt = null;
         ResultSet rs = null;
 
         try {
             conn = getConnection();
             List<String> primaryKeys = getTablePrimaryKeys(tableName, conn);
             boolean hasPrimaryKeys = !primaryKeys.isEmpty();
             if (logger.isDebugEnabled()) {
                 logger.debug("primaryKeys for table {}: {}", tableName, primaryKeys);
             }
             StringBuilder sql = new StringBuilder(64);
             sql.append("select * from ").append(tableName);
             if (!constraints.isEmpty()) {
                 String next = " where ";
                 for (Map.Entry<String, Value> entry : constraints.entrySet()) {
                     try {
                         switch (entry.getValue().getType()) {
                             case PropertyType.LONG:
                                 sql.append(next).append(entry.getKey()).append("=")
                                         .append(entry.getValue().getString());
                                 break;
                             default:
                                 sql.append(next).append(entry.getKey()).append("='")
                                         .append(entry.getValue().getString()).append("'");
                         }
                         next = " and ";
                     } catch (RepositoryException e) {
                         logger.error(e.getMessage(), e);
                     }
                 }
             }
            rs = conn.createStatement().executeQuery(sql.toString());
             int position = 0;
             while (rs.next()) {
                 position++;
                 String rowID = hasPrimaryKeys ? getRowID(rs, primaryKeys) : String.valueOf(position);
                 if (rowID != null) {
                     ids.add(rowID);
                 }
             }
             return ids;
         } catch (SQLException e) {
             logger.error(e.getMessage(), e);
             return Collections.emptyList();
         } finally {
             DbUtility.close(conn, stmt, rs);
         }
     }
 
     /**
      * Returns the node type name of rows in the specified table.
      * 
      * @param tableName
      *            the name of the table of the current row
      * @return the node type name of rows in the specified table
      * @throws PathNotFoundException
      *             in case the table name cannot be handled by this data source
      */
     protected abstract String getRowNodeTypeName(String tableName) throws PathNotFoundException;
 
     protected abstract ExternalData getRowProperties(String path, String type, String table, ResultSet rs) throws SQLException,
             PathNotFoundException;
 
     /**
      * Returns the node type name that corresponds to the DB schema, i.e. to a top node.
      * 
      * @return the node type name that corresponds to the DB schema, i.e. to a top node
      */
     protected abstract String getSchemaNodeType();
 
     /**
      * Returns list of table names in the database schema.
      * 
      * @return a list of table names in the database schema
      */
     protected abstract List<String> getTableNames();
 
     /**
      * Returns the node type name that corresponds to a DB table.
      * 
      * @return the node type name that corresponds to a DB table
      */
     protected abstract String getTableNodeType();
 
     protected List<String> getTablePrimaryKeys(String table, Connection conn) {
         List<String> keys = new LinkedList<String>();
         ResultSet rs = null;
 
         try {
             rs = conn.getMetaData().getPrimaryKeys(null, null, table);
             while (rs.next()) {
                 keys.add(rs.getString("COLUMN_NAME"));
             }
             return keys;
         } catch (SQLException e) {
             logger.error(e.getMessage(), e);
             return Collections.emptyList();
         } finally {
             DbUtility.close(null, null, rs);
         }
     }
 
     protected abstract String[] getValuesForPrimayKeys(String rowId);
 
     @Override
     public final boolean isSupportsHierarchicalIdentifiers() {
         return false;
     }
 
     @Override
     public final boolean isSupportsUuid() {
         return false;
     }
 
     @Override
     public final boolean itemExists(String path) {
         try {
             getItemByPath(path);
             return true;
         } catch (PathNotFoundException e) {
             return false;
         }
     }
 
     @Override
     public final void start() {
         long timer = System.currentTimeMillis();
         Connection connection = null;
         try {
             Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
             connection = getConnection();
         } catch (Exception e) {
             logger.error(e.getMessage(), e);
             throw new JahiaRuntimeException(e);
         } finally {
             if (connection != null) {
                 try {
                     connection.close();
                 } catch (SQLException e) {
                     logger.error(e.getMessage(), e);
                 }
             }
         }
         logger.info("Provider successfully started in {} ms", System.currentTimeMillis() - timer);
     }
 
     @Override
     public final void stop() {
         try {
             DriverManager.getConnection(DB_URI + ";shutdown=true");
         } catch (Exception e) {
             logger.warn(e.getMessage());
         }
         logger.info("Provider stopped");
     }
 }

 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.core.storage.H2;
 
 import com.flexive.core.Database;
 import com.flexive.core.search.DataFilter;
 import com.flexive.core.search.DataSelector;
 import com.flexive.core.search.H2.H2SQLDataFilter;
 import com.flexive.core.search.H2.H2SQLDataSelector;
 import com.flexive.core.search.SqlSearch;
 import com.flexive.core.search.cmis.impl.CmisSqlQuery;
 import com.flexive.core.search.cmis.impl.sql.H2.H2Dialect;
 import com.flexive.core.search.cmis.impl.sql.SqlDialect;
 import com.flexive.core.storage.*;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.exceptions.FxNotFoundException;
 import com.flexive.shared.exceptions.FxSqlSearchException;
 import com.flexive.shared.interfaces.ContentEngine;
 import com.flexive.shared.structure.FxEnvironment;
 import com.flexive.shared.structure.TypeStorageMode;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.lang.reflect.Method;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Factory for the MySQL storage
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class H2StorageFactory extends GenericDBStorage implements DBStorage {
     private static final Log LOG = LogFactory.getLog(H2StorageFactory.class);
     private final static String VENDOR = "H2";
 
     /**
      * {@inheritDoc}
      */
     public String getStorageVendor() {
         return VENDOR;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean canHandle(DatabaseMetaData dbm) {
         try {
             return VENDOR.equals(dbm.getDatabaseProductName());
         } catch (SQLException e) {
             LOG.error(e);
             return false;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public ContentStorage getContentStorage(TypeStorageMode mode) throws FxNotFoundException {
         if (mode == TypeStorageMode.Hierarchical)
             return H2HierarchicalStorage.getInstance();
         throw new FxNotFoundException("ex.structure.typeStorageMode.notImplemented", mode);
     }
 
     /**
      * {@inheritDoc}
      */
     public EnvironmentLoader getEnvironmentLoader() {
         return H2EnvironmentLoader.getInstance();
     }
 
     /**
      * {@inheritDoc}
      */
     public SequencerStorage getSequencerStorage() {
         return H2SequencerStorage.getInstance();
     }
 
     /**
      * {@inheritDoc}
      */
     public TreeStorage getTreeStorage() {
         return H2TreeStorage.getInstance();
     }
 
     /**
      * {@inheritDoc}
      */
     public DataSelector getDataSelector(SqlSearch search) throws FxSqlSearchException {
         return new H2SQLDataSelector(search);
     }
 
     /**
      * {@inheritDoc}
      */
     public DataFilter getDataFilter(Connection con, SqlSearch search) throws FxSqlSearchException {
         return new H2SQLDataFilter(con, search);
     }
 
     /**
      * {@inheritDoc}
      */
     public SqlDialect getCmisSqlDialect(FxEnvironment environment, ContentEngine contentEngine, CmisSqlQuery query, boolean returnPrimitives) {
         return new H2Dialect(environment, contentEngine, query, returnPrimitives);
     }
 
     /**
      * {@inheritDoc}
      */
     public String getIfFunction(String condition, String exprtrue, String exprfalse) {
         return "CASEWHEN(" + condition + "," + exprtrue + "," + exprfalse + ")";
     }
 
     /**
      * {@inheritDoc}
      */
     public String getRegExpLikeOperator(String column, String regexp) {
         return column + " REGEXP " + regexp;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getReferentialIntegrityChecksStatement(boolean enable) {
         if (LOG.isWarnEnabled()) {
             LOG.warn("H2: SET REFERENTIAL_INTEGRITY called, current transaction committed.");
         }
         return "SET REFERENTIAL_INTEGRITY " + (enable ? "TRUE" : "FALSE");
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isDisableIntegrityTransactional() {
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getTimestampFunction() {
         return "TIMEMILLIS(NOW())";
     }
 
     /**
      * {@inheritDoc}
      */
     public String concat_ws(String delimiter, String... text) {
         if (text.length == 0)
             return "";
         if (text.length == 1)
             return text[0];
         StringBuilder sb = new StringBuilder(500);
         sb.append("CONCAT_WS('").append(delimiter).append("'");
         for (String s : text)
             sb.append(',').append(s);
         sb.append(')');
         return sb.toString();
     }
 
     /**
      * {@inheritDoc}
      */
     public String getLimit(boolean hasWhereClause, long limit) {
         return " LIMIT " + limit;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getLimitOffset(boolean hasWhereClause, long limit, long offset) {
         return " LIMIT " + limit + " OFFSET " + offset;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isForeignKeyViolation(Exception exc) {
         final int errorCode = Database.getSqlErrorCode(exc);
         //see http://h2database.com/javadoc/org/h2/constant/ErrorCode.html#c23002
        return errorCode == 23002 || errorCode == 23003 // H2 <= 1.2
                || errorCode == 23503 /* H2 >= 1.3 */;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isQueryTimeout(Exception e) {
         final int errorCode = Database.getSqlErrorCode(e);
         //see http://h2database.com/javadoc/org/h2/constant/ErrorCode.html
        return errorCode == 90051 /* H2 <= 1.2 */ || errorCode == 57014 /* H2 >= 1.3 */;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isRollbackOnConstraintViolation() {
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isUniqueConstraintViolation(Exception exc) {
         final int sqlErr = Database.getSqlErrorCode(exc);
         //see http://h2database.com/javadoc/org/h2/constant/ErrorCode.html
        return sqlErr == 23001 /* H2 <= 1.2 */ || sqlErr == 23505 /* H2 >= 1.3 */;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isDeadlock(Exception exc) {
         final int sqlErr = Database.getSqlErrorCode(exc);
         //see http://h2database.com/javadoc/org/h2/constant/ErrorCode.html
         return sqlErr == 40001;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isDuplicateKeyViolation(SQLException exc) {
        return isUniqueConstraintViolation(exc);
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean requiresConfigSchema() {
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     public Connection getConnection(String database, String schema, String jdbcURL, String jdbcURLParameters,
                                     String user, String password, boolean createDB, boolean createSchema,
                                     boolean dropDBIfExist) throws Exception {
         Connection con;
         try {
             if (StringUtils.isBlank(database))
                 throw new IllegalArgumentException("No database name (path) specified!");
             if (StringUtils.isBlank(schema))
                 throw new IllegalArgumentException("No database schema specified!");
             String url = jdbcURL + database;
             if (!StringUtils.isBlank(jdbcURLParameters))
                 url = url + jdbcURLParameters.trim();
             schema = schema.trim();
             System.out.println("Using schema [" + schema + "], database [" + database + "] for " + VENDOR);
 
             if (StringUtils.isBlank(url))
                 throw new IllegalArgumentException("No JDBC URL provided!");
             url = url.trim();
 
             try {
                 Class.forName("org.h2.Driver").newInstance();
             } catch (ClassNotFoundException e) {
                 System.err.println("H2 JDBC Driver not found in classpath!");
                 return null;
             }
             if (dropDBIfExist) {
                 Method m = Class.forName("org.h2.tools.DeleteDbFiles").getMethod("execute", String.class, String.class, boolean.class);
                 String path;
                 String db;
                 if (database.lastIndexOf('/') != -1) {
                     path = database.substring(0, database.lastIndexOf('/'));
                     db = database.substring(database.lastIndexOf('/') + 1);
                 } else if (database.lastIndexOf('\\') != -1) {
                     path = database.substring(0, database.lastIndexOf('\\'));
                     db = database.substring(database.lastIndexOf('\\') + 1);
                 } else {
                     System.err.println("Could not extract path and database name from [" + database + "]");
                     return null;
                 }
                 System.out.println("Dropping Database ... Path: [" + path + "], DB: [" + db + "]");
                 m.invoke(null, path, db, false);
             }
             System.out.println("Connecting using JDBC URL " + url);
             con = DriverManager.getConnection(url, user, password);
         } catch (SQLException e) {
             e.printStackTrace();
             return null;
         }
         return con;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean initConfiguration(Connection con, String schema, boolean dropIfExist) throws Exception {
         Map<String, String> scripts = FxSharedUtils.getStorageScriptResources(getStorageVendor());
         List<String> s = new ArrayList<String>();
         s.addAll(scripts.keySet());
         Collections.sort(s);
         Statement stmt;
         stmt = con.createStatement();
         int cnt = 0;
         if (StringUtils.isBlank(schema))
             throw new IllegalArgumentException("No schema name specified!");
         schema = schema.trim();
         try {
             if (dropIfExist) {
                 System.out.println("Resetting configuration schema " + schema);
                 cnt += stmt.executeUpdate("DROP SCHEMA IF EXISTS " + schema);
                 cnt += stmt.executeUpdate("CREATE SCHEMA IF NOT EXISTS " + schema);
             }
             cnt += stmt.executeUpdate("SET SCHEMA " + schema);
 
             for (String script : s) {
                 if (script.startsWith("config/")) {
                     System.out.println("Executing " + script + " ...");
                     cnt += new FxSharedUtils.SQLExecutor(con, scripts.get(script), script.startsWith("SP") ? "|" : ";",
                             false, true, System.out).
                             execute();
                 }
             }
         } finally {
             if (stmt != null)
                 stmt.close();
         }
         if (cnt > 0)
             System.out.println("Executed " + cnt + " statements");
         return cnt > 0;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean initDivision(Connection con, String schema, boolean dropIfExist) throws Exception {
         Map<String, String> scripts = FxSharedUtils.getStorageScriptResources(getStorageVendor());
         List<String> s = new ArrayList<String>();
         s.addAll(scripts.keySet());
         Collections.sort(s);
         Statement stmt;
         stmt = con.createStatement();
         int cnt = 0;
         if (StringUtils.isBlank(schema))
             throw new IllegalArgumentException("No schema name specified!");
         schema = schema.trim();
         try {
             if (dropIfExist) {
                LOG.info("Resetting division schema " + schema);
                 cnt += stmt.executeUpdate("DROP SCHEMA IF EXISTS " + schema);
                 cnt += stmt.executeUpdate("CREATE SCHEMA IF NOT EXISTS " + schema);
             }
             cnt += stmt.executeUpdate("SET SCHEMA " + schema);
 
             for (String script : s) {
                 if (script.indexOf('/') == -1 || script.startsWith("tree/")) {
                    LOG.info("Executing " + script + " ...");
                     cnt += new FxSharedUtils.SQLExecutor(con, scripts.get(script),
                             script.startsWith("SP") || script.startsWith("tree/") ? "|" : ";",
                             false, true, System.out).
                             execute();
                 }
             }
         } finally {
             if (stmt != null)
                 stmt.close();
         }
         if (cnt > 0)
            LOG.info("Executed " + cnt + " statements");
         return cnt > 0;
     }
 }

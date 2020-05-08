 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2008
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
 package com.flexive.core;
 
 import static com.flexive.core.DatabaseConst.DS_GLOBAL_CONFIG;
 import static com.flexive.core.DatabaseConst.ML;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxLanguage;
 import com.flexive.shared.configuration.DivisionData;
 import com.flexive.shared.exceptions.*;
 import com.flexive.shared.interfaces.GlobalConfigurationEngine;
 import com.flexive.shared.value.FxString;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.naming.Context;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Class handling Database stuff
  *
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public final class Database {
     /**
      * Maximum number of divisions
      */
     public static final int MAX_DIVISIONS = 256;
 
     /**
      * Suffix for non-transactional data sources
      */
     static final String NO_TX_SUFFIX = "NoTX";
 
     private static final Log LOG = LogFactory.getLog(Database.class);
     private static DataSource globalDataSource = null;
     private static DataSource testDataSource = null;
     // cached data source references - index = division ID
     private static final DataSource[] dataSources = new DataSource[MAX_DIVISIONS];
     // cached data sources without transaction support
     private static final DataSource[] dataSourcesNoTX = new DataSource[MAX_DIVISIONS];
     // cached data source by (resource) name
     private static final Map<String, DataSource> dataSourcesByName = new HashMap<String, DataSource>();
 
     /**
      * Empty default constructor.
      */
     private Database() {
         // empty default constructor
     }
 
     /**
      * Retrieves a database connection.
      *
      * @param divisionId the requested division Id
      * @return a database connection
      * @throws SQLException If no connection could be retrieved
      */
     public static Connection getDbConnection(int divisionId) throws SQLException {
         // Try to obtain a connection
         try {
             return getDataSource(divisionId, true).getConnection();
         } catch (SQLException exc) {
             String sErr = "FxDbException, unable to retrieve DB Connection: " + exc.getMessage();
             LOG.error(sErr);
             throw new SQLException(sErr);
         }
     }
 
     /**
      * Return a database connection for the current user's division.
      *
      * @return a database connection for the current user's division.
      * @throws SQLException if a DB error occured
      */
     public static Connection getDbConnection() throws SQLException {
         return getDbConnection(FxContext.get().getDivisionId());
     }
 
     /**
      * Retrieves a database connection for the global configuration table, regardless
      * of the current request's division id.
      *
      * @return a database connection
      * @throws SQLException if no connection could be retrieved
      */
     public static Connection getGlobalDbConnection() throws SQLException {
         try {
             return getGlobalDataSource().getConnection();
         } catch (SQLException exc) {
             String sErr = "FxDbException, unable to retrieve global DB Connection: " + exc.getMessage();
             LOG.error(sErr);
             throw new SQLException(sErr);
         }
     }
 
     /**
      * Returns the data source for the calling user's division.
      *
      * @return the data source for the calling user's division
      * @throws SQLException if a DB error occured
      */
     public static DataSource getDataSource() throws SQLException {
         return getDataSource(FxContext.get().getDivisionId(), true);
     }
 
     /**
      * Returns the non-transactional data source for the calling user's division.
      *
      * @return the non-transactional data source for the calling user's division
      * @throws SQLException if a DB error occured
      */
     public static DataSource getNonTXDataSource() throws SQLException {
         return getDataSource(FxContext.get().getDivisionId(), false);
     }
 
     /**
      * Returns the non-transactional data source for the requested division.
      *
      * @param division the division
      * @return the non-transactional data source for the requested division
      * @throws SQLException if a DB error occured
      */
     public static DataSource getNonTXDataSource(int division) throws SQLException {
         return getDataSource(division, false);
     }
 
     /**
      * Retrieves a database connection.
      *
      * @param divisionId the division id
      * @param useTX      request transaction support?
      * @return a database connection
      * @throws SQLException If no connection could be retrieved
      */
     private static DataSource getDataSource(int divisionId, boolean useTX) throws SQLException {
         // Check division
         if (!DivisionData.isValidDivisionId(divisionId)) {
             throw new SQLException("Unable to obtain connection: Division not defined (" + divisionId + ").");
         }
         DataSource[] dataSourceCache = useTX ? dataSources : dataSourcesNoTX;
         // use cached datasource, if available
         if (divisionId == DivisionData.DIVISION_TEST && testDataSource != null) {
             return testDataSource;
         } else if (divisionId != DivisionData.DIVISION_TEST && dataSourceCache[divisionId] != null) {
             return dataSourceCache[divisionId];
         }
         synchronized (Database.class) {
             // Try to obtain a connection
             String finalDsName = null;
             try {
                 if (divisionId == DivisionData.DIVISION_GLOBAL) {
                     // Special case: global config database
                     finalDsName = DS_GLOBAL_CONFIG;
                 } else {
                     // else: get data source from global configuration
                     GlobalConfigurationEngine globalConfiguration = EJBLookup.getGlobalConfigurationEngine();
                     finalDsName = globalConfiguration.getDivisionData(divisionId).getDataSource();
                     if (!useTX)
                         finalDsName += NO_TX_SUFFIX;
                 }
                 LOG.info("Looking up datasource for division " + divisionId + ": " + finalDsName);
                 final DataSource dataSource = getDataSource(finalDsName);
                 if (divisionId == DivisionData.DIVISION_TEST) {
                     testDataSource = dataSource;
                     return testDataSource;
                 } else {
                     dataSourceCache[divisionId] = dataSource;
                     return dataSourceCache[divisionId];
                 }
             } catch (NamingException exc) {
                 String sErr = "Naming Exception, unable to retrieve Connection to [" + finalDsName
                         + "]: " + exc.getMessage();
                 LOG.error(sErr);
                 throw new SQLException(sErr);
             } catch (FxNotFoundException exc) {
                 String sErr = "Failed to retrieve datasource for division " + divisionId
                         + " (not configured).";
                 LOG.error(sErr);
                 throw new SQLException(sErr);
             } catch (FxLoadException exc) {
                 String sErr = "Failed to load datasource configuration: " + exc.getMessage();
                 LOG.error(sErr);
                 throw new SQLException(sErr);
             } catch (FxApplicationException exc) {
                 String sErr = "Unknown error while loading datasource for division " + divisionId
                         + ": " + exc.getMessage();
                 LOG.error(sErr);
                 throw new SQLException(sErr);
             }
         }
     }
 
     public static synchronized DataSource getDataSource(String dataSourceName) throws NamingException, SQLException {
         if (dataSourcesByName.containsKey(dataSourceName)) {
             return dataSourcesByName.get(dataSourceName);
         }
         final Context c = EJBLookup.getInitialContext();
         DataSource dataSource = null;
         for (String path : getPossibleJndiNames(dataSourceName)) {
             try {
                 dataSource = (DataSource) c.lookup(path);
                 break;
             } catch (NamingException e) {
                 // pass
             }
         }
         if (dataSource == null) {
             // Geronimo
             String name = dataSourceName;
             if (name.startsWith("jdbc/"))
                 name = name.substring(5);
             Object o = c.lookup("jca:/console.dbpool/" + name + "/JCAManagedConnectionFactory/" + name);
             try {
                 dataSource = (DataSource) o.getClass().getMethod("$getResource").invoke(o);
             } catch (Exception ex) {
                 String sErr = "Unable to retrieve Connection to [" + dataSourceName
                         + "]: " + ex.getMessage();
                 LOG.error(sErr);
                 throw new SQLException(sErr);
             }
         }
         dataSourcesByName.put(dataSourceName, dataSource);
         return dataSource;
     }
 
     /**
      * Retrieves DivisionData for current division
      *
      * @return DivisionData
      * @throws SQLException If no DivisionData could be retrieved
      */
     public static DivisionData getDivisionData() throws SQLException {
         final FxContext inf = FxContext.get();
         // Check division
         if (!DivisionData.isValidDivisionId(inf.getDivisionId())) {
             throw new SQLException("Unable to obtain DivisionData: Division not defined (" + inf.getDivisionId() + ")");
         }
         try {
             GlobalConfigurationEngine globalConfiguration = EJBLookup.getGlobalConfigurationEngine();
             return globalConfiguration.getDivisionData(inf.getDivisionId());
         } catch (FxNotFoundException exc) {
             String sErr = "Failed to retrieve DivisionData for division " + inf.getDivisionId() + " (not configured).";
             LOG.error(sErr);
             throw new SQLException(sErr);
         } catch (FxApplicationException exc) {
             String sErr = "Failed to load configuration: " + exc.getMessage();
             LOG.error(sErr);
             throw new SQLException(sErr);
         }
     }
 
     /**
      * Get a database vendor specific timestamp of the current time in milliseconds as Long
      *
      * @return database vendor specific timestamp of the current time in milliseconds as Long
      */
     public static String getTimestampFunction() {
         try {
             switch (getDivisionData().getDbVendor()) {
                 case MySQL:
                     return "UNIX_TIMESTAMP()*1000";
                 case H2:
                     return "TIMEMILLIS(NOW())";
             }
         } catch (SQLException e) {
             LOG.error(e);
         }
         //default fallback
         return "UNIX_TIMESTAMP()*1000";
     }
 
     /**
      * Get a database vendor specific "IF" function
      *
      * @return database vendor specific "IF" function
      */
     public static String getIfFunction() {
         try {
             switch (getDivisionData().getDbVendor()) {
                 case MySQL:
                     return "IF";
                 case H2:
                     return "CASEWHEN";
             }
         } catch (SQLException e) {
             LOG.error(e);
         }
         //default fallback
         return "IF";
     }
 
     /**
      * Get the database vendor specific statement to enable or disable referential integrity checks
      *
      * @param enable enable or disable checks?
      * @return database vendor specific statement to enable or disable referential integrity checks
      */
     public static String getReferentialIntegrityChecksStatement(boolean enable) {
         try {
             switch (getDivisionData().getDbVendor()) {
                 case MySQL:
                     return "SET FOREIGN_KEY_CHECKS=" + (enable ? 1 : 0);
                 case H2:
                     return "SET REFERENTIAL_INTEGRITY " + (enable ? "TRUE" : "FALSE");
             }
         } catch (SQLException e) {
             LOG.error(e);
         }
         //default fallback: nothing
         return "";
     }
 
     /**
      * Retrieve data source for global configuration table, regardless
      * of the current request's division id.
      *
      * @return a database connection
      * @throws SQLException if no connection could be retrieved
      */
     public static synchronized DataSource getGlobalDataSource() throws SQLException {
         // Try to obtain a connection
         if (globalDataSource != null) {
             return globalDataSource;
         }
         try {
             final Context c = EJBLookup.getInitialContext();
             for (String path : getPossibleJndiNames(DS_GLOBAL_CONFIG)) {
                 try {
                     if (LOG.isDebugEnabled()) {
                         LOG.debug("Trying JNDI path " + path + "...");
                     }
                     globalDataSource = (DataSource) c.lookup(path);
                     if (LOG.isInfoEnabled()) {
                         LOG.info("Found global datasource under JNDI path " + path);
                     }
                     break;
                 } catch (NamingException e) {
                     // try next path
                 }
             }
             if (globalDataSource == null) {
                //try the wierd geronimo logic as last resort
                 try {
                    Object o = EJBLookup.getInitialContext().lookup("jca:/console.dbpool/flexiveConfiguration/JCAManagedConnectionFactory/flexiveConfiguration");
                     globalDataSource = (DataSource) o.getClass().getMethod("$getResource").invoke(o);
                 } catch (Exception ex) {
                     String msg = "Unable to retrieve Connection to [" + DS_GLOBAL_CONFIG + "]: " + ex.getMessage();
                     LOG.error(msg);
                     throw new SQLException(msg);
                 }
             }
             return globalDataSource;
         } catch (NamingException exc) {
             String msg = "Naming Exception, unable to retrieve Connection to [" + DS_GLOBAL_CONFIG
                     + "]: " + exc.getMessage();
             LOG.error(msg);
             throw new SQLException(msg);
         }
     }
 
     private static String[] getPossibleJndiNames(String dataSourceName) {
         return new String[]{
                 dataSourceName,
                 "java:" + dataSourceName,
                 "java:comp/env/" + dataSourceName,
                 "java:openejb/Resource/" + dataSourceName,
         };
     }
 
     /**
      * Helper function to close connections and statements.
      * A FxDbException is thrown if the close of the connection failed.
      * No Exception is thrown if the Statement failed to close, but a error is logged.
      *
      * @param caller class calling function/module, or null
      * @param con    the connection to close, or null
      * @param stmt   the statement to close, or null
      */
     public static void closeObjects(Class caller, Connection con, Statement stmt) {
         closeObjects(caller.getName(), con, stmt);
     }
 
     /**
      * Helper function to close connections and statements.
      * A FxDbException is thrown if the close of the connection failed.
      * No Exception is thrown if the Statement failed to close, but a error is logged.
      *
      * @param caller a string representing the calling function/module, or null
      * @param con    the connection to close, or null
      * @param stmt   the statement to close, or null
      */
     public static void closeObjects(String caller, Connection con, Statement stmt) {
         try {
             if (stmt != null) stmt.close();
         } catch (Exception exc) {
             //noinspection ThrowableInstanceNeverThrown
             StackTraceElement[] se = new Throwable().getStackTrace();
             LOG.error(((caller != null) ? caller + " f" : "F") + "ailed to close the statement(s): "
                     + exc.getMessage() + " Calling line: " + se[2].toString());
         }
         if (con != null) {
             try {
                 if (!con.isClosed()) {
                     con.close();
                 }
             } catch (SQLException exc) {
                 //noinspection ThrowableInstanceNeverThrown
                 FxDbException dbExc = new FxDbException(((caller != null) ? caller + " is u" : "U")
                         + "nable to close the db connection");
                 LOG.error(dbExc);
                 System.err.println(dbExc.getMessage());
             }
         }
     }
 
     /**
      * Returns true if the SqlError is a unique constraint violation.
      *
      * @param exc the exception
      * @return true if the SqlError is a unique constraint violation
      */
     public static boolean isUniqueConstraintViolation(Exception exc) {
         if (!(exc instanceof SQLException)) {
             return false;
         }
         try {
             int sqlErr = ((SQLException) exc).getErrorCode();
             switch (getDivisionData().getDbVendor()) {
                 case MySQL:
                     //see http://dev.mysql.com/doc/refman/5.1/en/error-messages-server.html
                     // 1582 Example error: Duplicate entry 'ABSTRACT' for key 'UK_TYPEPROPS_NAME'
                     return (sqlErr == 1062 || sqlErr == 1582);
                 case H2:
                     return sqlErr == 23001;
 
             }
         } catch (SQLException e) {
             return false;
         }
 
         // final String sMsg = (exc.getMessage() == null) ? "" : exc.getMessage().toLowerCase();
 
         // Oracle:
         // msg: "unique constraint (XXXX) violated"
         // return sMsg.indexOf("unique constraint") != -1 && sMsg.indexOf("violated") != -1;
 
         // MySQL5:
         // msg="Duplicate key or integrity constraint violation message from server: "Cannot delete or update a
         // parent row: a foreign key constraint fails"
         // SQLState: 23000
         return ((SQLException) exc).getSQLState().equalsIgnoreCase("23000");
     }
 
     /**
      * Returns true if the SqlError is a foreign key violation.
      *
      * @param exc the exception
      * @return true if the SqlError is a foreign key violation
      */
     public static boolean isForeignKeyViolation(Exception exc) {
         if (!(exc instanceof SQLException)) {
             return false;
         }
         try {
             final int errorCode = ((SQLException) exc).getErrorCode();
             switch (getDivisionData().getDbVendor()) {
                 case MySQL:
                     //see http://dev.mysql.com/doc/refman/5.0/en/error-messages-server.html
                     return errorCode == 1451 || errorCode == 1217;
                 case H2:
                     //see http://h2database.com/javadoc/org/h2/constant/ErrorCode.html#c23002
                     return errorCode == 23002 || errorCode == 23003;
                 default:
                     return false;
             }
         } catch (SQLException e) {
             throw new FxDbException(LOG, e, "ex.db.sqlError", e.getMessage()).asRuntimeException();
         }
     }
 
     /**
      * Load a FxString from a translation table
      *
      * @param con         an open connection
      * @param table       the base table (NOT the one with translations!)
      * @param column      the name of the columns from the translations table to load
      * @param whereClause mandatory where clause
      * @return FxString created from the data table
      * @throws SQLException if a database error occured
      */
     public static FxString loadFxString(Connection con, String table, String column, String whereClause)
             throws SQLException {
         Statement stmt = null;
         Map<Long, String> hmTrans = new HashMap<Long, String>(10);
         long defaultLanguageId = -1;
         try {
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT LANG, DEFLANG, " + column + " FROM " + table + DatabaseConst.ML
                     + " WHERE " + whereClause);
             while (rs != null && rs.next()) {
                 hmTrans.put(rs.getLong(1), rs.getString(3));
                 if (rs.getBoolean(2)) {
                     defaultLanguageId = rs.getInt(1);
                 }
             }
         } finally {
             if (stmt != null)
                 stmt.close();
         }
         return new FxString(defaultLanguageId, hmTrans);
     }
 
     /**
      * Load a FxString from the content data
      *
      * @param con         an open connection
      * @param column      the name of the column from the translations table to load
      * @param whereClause mandatory where clause
      * @return FxString created from the data table
      * @throws SQLException if a database error occured
      */
     public static FxString loadContentDataFxString(Connection con, String column, String whereClause)
             throws SQLException {
         Statement stmt = null;
         Map<Long, String> hmTrans = new HashMap<Long, String>(10);
         int defaultLanguageId = -1;
         try {
             stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT LANG, ISMLDEF, " + column + " FROM " + DatabaseConst.TBL_CONTENT_DATA
                     + " WHERE " + whereClause);
             while (rs != null && rs.next()) {
                 hmTrans.put(rs.getLong(1), rs.getString(3));
                 if (rs.getBoolean(2)) {
                     defaultLanguageId = rs.getInt(1);
                 }
             }
         } finally {
             if (stmt != null)
                 stmt.close();
         }
         return new FxString(defaultLanguageId, hmTrans);
     }
 
     /**
      * Loads all FxString entries stored in the given table.
      *
      * @param con     an existing connection
      * @param table   table to use
      * @param columns name of the columns containing the translations
      * @return all FxString entries stored in the given table, indexed by the ID field.
      * @throws SQLException if the query was not successful
      */
     public static Map<Long, FxString[]> loadFxStrings(Connection con, String table, String... columns) throws SQLException {
         Statement stmt = null;
         final StringBuilder sql = new StringBuilder();
         final Map<Long, FxString[]> result = new HashMap<Long, FxString[]>();
         try {
             sql.append("SELECT ID, LANG");
             final boolean hasDefLang = columns.length == 1; // deflang is only meaningful for single-column tables
             if (hasDefLang) {
                 sql.append(", DEFLANG");
             }
             for (String column : columns) {
                 sql.append(',').append(column);
             }
             sql.append(" FROM ").append(table).append(ML).append(" ORDER BY LANG");
             final int startIndex = hasDefLang ? 4 : 3;
             stmt = con.createStatement();
             final ResultSet rs = stmt.executeQuery(sql.toString());
             while (rs.next()) {
                 final long id = rs.getLong(1);
                 final int lang = rs.getInt(2);
                 final boolean defLang = hasDefLang && rs.getBoolean(3);
                 if (lang == FxLanguage.SYSTEM_ID) {
                     continue;   // TODO how to deal with system language? 
                 }
                 FxString[] entry = result.get(id);
                 if (entry == null) {
                     entry = new FxString[columns.length];
                     /*for (int i = 0; i < entry.length; i++) {
                         entry[i] = new FxString(true, "");
                     }
                     result.put(id, entry);*/
                 }
                 for (int i = 0; i < columns.length; i++) {
                     final String translation = rs.getString(startIndex + i);
                     if( entry[i] == null )
                         entry[i] = new FxString(true, lang, translation);
                     else
                         entry[i].setTranslation(lang, translation);
                     if (defLang) {
                         entry[i].setDefaultLanguage(lang);
                     }
                 }
                 result.put(id, entry);
             }
         } finally {
             closeObjects(Database.class, null, stmt);
         }
         return result;
     }
 
     /**
      * Store a FxString in a translation table that only consists of one(!) translation column
      *
      * @param string     string to be stored
      * @param con        existing connection
      * @param table      storage table
      * @param dataColumn name of the data column
      * @param idColumn   name of the id column
      * @param id         id of the given string
      * @throws SQLException if a database error occured
      */
     public static void storeFxString(FxString string, Connection con, String table, String dataColumn,
                                      String idColumn, long id) throws SQLException {
         if (!string.isMultiLanguage()) {
             throw new FxInvalidParameterException("string", LOG, "ex.db.fxString.store.multilang", table).asRuntimeException();
         }
         PreparedStatement ps = null;
         try {
             ps = con.prepareStatement("DELETE FROM " + table + ML + " WHERE " + idColumn + "=?");
             ps.setLong(1, id);
             ps.execute();
             ps.close();
             if (string.getTranslatedLanguages().length > 0) {
                 ps = con.prepareStatement("INSERT INTO " + table + ML + " (" + idColumn + ",LANG,DEFLANG,"
                         + dataColumn + ") VALUES (?,?,?,?)");
                 ps.setLong(1, id);
                 String curr;
                 for (long lang : string.getTranslatedLanguages()) {
                     curr = string.getTranslation(lang);
                     if (curr != null && curr.trim().length() > 0) {
                         ps.setInt(2, (int) lang);
                         ps.setBoolean(3, lang == string.getDefaultLanguage());
                         ps.setString(4, curr);
                         ps.executeUpdate();
                     }
                 }
             }
         } finally {
             if (ps != null)
                 ps.close();
         }
     }
 
     /**
      * Store a FxString in a translation table that only consists of n translation columns
      *
      * @param string     string to be stored
      * @param con        existing connection
      * @param table      storage table
      * @param dataColumn names of the data columns
      * @param idColumn   name of the id column
      * @param id         id of the given string
      * @throws SQLException if a database error occured
      */
     public static void storeFxString(FxString[] string, Connection con, String table, String[] dataColumn,
                                      String idColumn, long id) throws SQLException {
         PreparedStatement ps = null;
         if (string.length != dataColumn.length)
             throw new SQLException("string.length != dataColumn.length");
         for (FxString param : string) {
             if (!param.isMultiLanguage()) {
                 throw new FxInvalidParameterException("string", LOG, "ex.db.fxString.store.multilang", table).asRuntimeException();
             }
         }
         try {
             ps = con.prepareStatement("DELETE FROM " + table + ML + " WHERE " + idColumn + "=?");
             ps.setLong(1, id);
             ps.execute();
 
             //find languages to write
             List<Long> langs = new ArrayList<Long>(5);
             for (FxString curr : string)
                 for (long currLang : curr.getTranslatedLanguages())
                     if (curr.translationExists(currLang)) {
                         if (!langs.contains(currLang))
                             langs.add(currLang);
                     }
             if (langs.size() > 0) {
                 StringBuffer sql = new StringBuffer(300);
                 sql.append("INSERT INTO ").append(table).append(ML + "(").append(idColumn).append(",LANG");
                 for (String dc : dataColumn)
                     sql.append(',').append(dc);
                 sql.append(")VALUES(?,?");
                 //noinspection UnusedDeclaration
                 for (FxString aString : string) sql.append(",?");
                 sql.append(')');
                 ps.close();
                 ps = con.prepareStatement(sql.toString());
 
                 for (long lang : langs) {
                     ps.setLong(1, id);
                     ps.setInt(2, (int) lang);
                     for (int i = 0; i < string.length; i++) {
                         if (FxString.EMPTY.equals(string[i].getTranslation(lang)))
                             ps.setNull(3 + i, java.sql.Types.VARCHAR);
                         else
                             ps.setString(3 + i, string[i].getTranslation(lang)); //get translation or empty string
                     }
                     ps.executeUpdate();
                 }
             }
         } finally {
             if (ps != null)
                 ps.close();
         }
     }
 }

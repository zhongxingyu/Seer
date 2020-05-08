 /*
  * RapidContext JDBC plug-in <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2010 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.app.plugin.jdbc;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.sql.Connection;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.logging.Logger;
 
 import org.rapidcontext.core.data.Array;
 import org.rapidcontext.core.data.Dict;
 import org.rapidcontext.core.env.AdapterConnection;
 import org.rapidcontext.core.env.AdapterException;
 import org.rapidcontext.util.DateUtil;
 
 /**
  * A JDBC adapter connection. This class encapsulates a JDBC
  * connection and allows execution of arbitrary SQL queries or
  * statements.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class JdbcConnection implements AdapterConnection {
 
     /**
      * The class logger.
      */
     private static final Logger LOG =
         Logger.getLogger(JdbcConnection.class.getName());
 
     /**
      * The instance counter, used to identify JDBC connections.
      */
     private static int counter = 0;
 
     /**
      * The encapsulated JDBC connection.
      */
     protected Connection con;
 
     /**
      * The JDBC connection id.
      */
     protected String prefix;
 
     /**
      * The SQL ping query.
      */
     protected String sqlPing;
 
     /**
      * The SQL connection and query timeout.
      */
     protected int timeout;
 
     /**
      * Creates a new JDBC connection.
      *
      * @param driver            the JDBC driver
      * @param url               the connection URL
      * @param props             the connection properties (user and password)
      * @param sqlPing           the SQL ping query
      * @param autoCommit        the auto-commit flag
      * @param timeout           the request timeout (in secs)
      *
      * @throws AdapterException if a connection couldn't be established
      */
     protected JdbcConnection(Driver driver,
                              String url,
                              Properties props,
                              String sqlPing,
                              boolean autoCommit,
                              int timeout)
         throws AdapterException {
 
         String  msg;
 
         this.prefix = "[JDBC:" + (++counter) + "] ";
         this.sqlPing = sqlPing;
         this.timeout = timeout;
         try {
             LOG.info(prefix + "creating connection for " + url);
             DriverManager.setLoginTimeout(timeout);
             con = driver.connect(url, props);
             con.setAutoCommit(autoCommit);
             LOG.fine(prefix + "done creating connection for " + url);
         } catch (SQLException e) {
             msg = "failed to connect to " + url + " with username '" +
                   props.getProperty("user") + "': " + e.getMessage();
             LOG.warning(prefix + msg);
             throw new AdapterException(msg);
         }
     }
 
     /**
      * Activates the connection. This method is called just before a
      * connection is to be used, i.e. when a new connection has been
      * created or when fetched from a resource pool.
      *
      * @throws AdapterException if the connection couldn't be
      *             activated (connection will be closed)
      */
     public void activate() throws AdapterException {
         String  msg;
 
         try {
             if (con.isClosed()) {
                 msg = "failed to activate, connection already closed";
                 LOG.fine(prefix + msg);
                 throw new AdapterException(msg);
             }
         } catch (SQLException e) {
             msg = "failed to activate: " + e.getMessage();
             LOG.fine(prefix + msg);
             throw new AdapterException(msg);
         }
     }
 
     /**
      * Passivates the connection. This method is called just after a
      * connection has been used and will be returned to the pool.
      * This operation should clear or reset the connection, so that
      * it can safely be used again at a later time without affecting
      * previous results or operations.
      *
      * @throws AdapterException if the connection couldn't be
      *             passivated (connection will be closed)
      */
     public void passivate() throws AdapterException {
         // Nothing to do here
     }
 
     /**
      * Validates the connection. This method is called before using
      * a connection and regularly when it is idle in the pool. It can
      * be used to trigger a "ping" of a connection, if implemented by
      * the adapter. An empty implementation is acceptable.
      *
      * @throws AdapterException if the connection didn't validate
      *             correctly
      */
     public void validate() throws AdapterException {
        if (sqlPing != null && !sqlPing.trim().isEmpty()) {
             executeQuery(sqlPing);
         }
     }
 
     /**
      * Closes the connection. This method is used to free any
      * resources used by the connection.  After this method has been
      * called, no further calls will be made to this connection.
      *
      * @throws AdapterException if the connection couldn't be closed
      *             properly (connection discarded anyway)
      */
     public void close() throws AdapterException {
         try {
             LOG.info(prefix + "closing connection");
             con.close();
             LOG.fine(prefix + "done closing connection");
         } catch (SQLException e) {
             LOG.warning(prefix + "failed to close connection: " + e.getMessage());
             throw new AdapterException(e.getMessage());
         }
     }
 
     /**
      * Commits any pending changes. This method is called after each
      * successful procedure tree execution that included this
      * connection. This method may be implemented as a no-op, if
      * the adapter does not support commit and rollback semantics.
      *
      * @throws AdapterException if the pending changes couldn't be
      *             committed to permanent storage (connection will be
      *             closed)
      */
     public void commit() throws AdapterException {
         try {
             con.commit();
         } catch (SQLException e) {
             throw new AdapterException(e.getMessage());
         }
     }
 
     /**
      * Rolls any pending changes back. This method is called after an
      * unsuccessful procedure tree execution that included this
      * connection. This method may be implemented as a no-op, if the
      * adapter does not support commit and rollback semantics.
      *
      * @throws AdapterException if the pending changes couldn't be
      *             rolled back (connection will be closed)
      */
     public void rollback() throws AdapterException {
         try {
             con.rollback();
         } catch (SQLException e) {
             throw new AdapterException(e.getMessage());
         }
     }
 
     /**
      * Executes an SQL statement.
      *
      * @param sql            the SQL statement to execute
      *
      * @return the array with generated keys
      *
      * @throws AdapterException if the execution failed
      */
     public Array executeStatement(String sql) throws AdapterException {
         try {
             LOG.fine(prefix + "executing statement: " + sql);
             Array res = executeStatement(prepare(sql, null));
             LOG.fine(prefix + "done executing statement: " + sql);
             return res;
         } catch (AdapterException e) {
             LOG.warning(prefix + e.getMessage());
             throw e;
         }
     }
 
     /**
      * Executes an SQL prepared statement. The statement will be
      * closed by this method.
      *
      * @param stmt           the prepared SQL statement to execute
      *
      * @return the array with generated keys
      *
      * @throws AdapterException if the execution failed
      */
     protected Array executeStatement(PreparedStatement stmt)
         throws AdapterException {
 
         Array      res = new Array(10);
         ResultSet  set = null;
 
         try {
             stmt.executeUpdate();
             try {
                 set = stmt.getGeneratedKeys();
                 while (set.next()) {
                     res.add(set.getString(1));
                 }
             } catch (SQLException ignore) {
                 // Ignore errors on generated keys
             }
             return res;
         } catch (SQLException e) {
             throw new AdapterException("failed to execute statement: " +
                                        e.getMessage());
         } finally {
             try {
                 if (set != null) {
                     set.close();
                 }
                 stmt.close();
             } catch (SQLException ignore) {
                 // Do nothing
             }
         }
     }
 
     /**
      * Executes an SQL query. Default processing flags will be used, which
      * means that column meta-data will not be included and column names will
      * be mapped into object properties.
      *
      * @param sql            the SQL query to execute
      *
      * @return the object with the result data
      *
      * @throws AdapterException if the execution failed
      */
     public Object executeQuery(String sql) throws AdapterException {
         return executeQuery(sql, "");
     }
 
     /**
      * Executes an SQL query with the specified processing flags.
      *
      * @param sql            the SQL query to execute
      * @param flags          the processing and mapping flags
      *
      * @return the object with the result data
      *
      * @throws AdapterException if the execution failed
      */
     public Object executeQuery(String sql, String flags) throws AdapterException {
         try {
             LOG.fine(prefix + "executing query: " + sql);
             Object res = executeQuery(prepare(sql, null), flags);
             LOG.fine(prefix + "done executing query: " + sql);
             return res;
         } catch (AdapterException e) {
             LOG.warning(prefix + e.getMessage());
             throw e;
         }
     }
 
     /**
      * Executes a prepared SQL query. The prepared statement will be
      * closed by this method. Default processing flags will be used, which
      * means that column meta-data will not be included and column names will
      * be mapped into object properties.
      *
      * @param stmt           the prepared SQL query to execute
      *
      * @return the object with the result data
      *
      * @throws AdapterException if the execution failed
      */
     protected Object executeQuery(PreparedStatement stmt)
         throws AdapterException {
 
         return executeQuery(stmt, "");
     }
 
     /**
      * Executes a prepared SQL query. The prepared statement will be
      * closed by this method.
      *
      * @param stmt           the prepared SQL query to execute
      * @param flags          the processing and mapping flags
      *
      * @return the object with the result data
      *
      * @throws AdapterException if the execution failed
      */
     protected Object executeQuery(PreparedStatement stmt, String flags)
         throws AdapterException {
 
         ResultSet  set = null;
 
         try {
             set = stmt.executeQuery();
             return createResults(set, flags);
         } catch (SQLException e) {
             throw new AdapterException("failed to execute query: " +
                                        e.getMessage());
         } finally {
             try {
                 if (set != null) {
                     set.close();
                 }
                 stmt.close();
             } catch (SQLException ignore) {
                 // Do nothing
             }
         }
     }
 
     /**
      * Prepares an SQL statement.
      *
      * @param sql            the SQL statement to prepare
      * @param params         the optional list of parameters
      *
      * @return the prepared SQL statement
      *
       @throws AdapterException if the statement couldn't be prepared
      */
     protected PreparedStatement prepare(String sql, ArrayList params)
         throws AdapterException {
 
         PreparedStatement  stmt;
         Object             obj;
         String             str;
 
         try {
             stmt = con.prepareStatement(sql,
                                         ResultSet.TYPE_FORWARD_ONLY,
                                         ResultSet.CONCUR_READ_ONLY,
                                         ResultSet.CLOSE_CURSORS_AT_COMMIT);
             for (int i = 0; params != null && i < params.size(); i++) {
                 obj = params.get(i);
                 if (obj instanceof String && ((String) obj).length() > 255) {
                     str = (String) params.get(i);
                     stmt.setCharacterStream(i + 1,
                                             new StringReader(str),
                                             str.length());
                 } else {
                     stmt.setObject(i + 1, obj);
                 }
             }
             stmt.setQueryTimeout(timeout);
             stmt.setFetchSize(Integer.MIN_VALUE);
             return stmt;
         } catch (SQLException e) {
             str = "failed to prepare SQL: " + e.getMessage();
             throw new AdapterException(str);
         }
     }
 
     /**
      * Converts a query result set to a data object.
      *
      * @param rs             the result set to convert
      * @param flags          the processing and mapping flags
      *
      * @return the data object with all the result data
      *
      * @throws AdapterException if the result data couldn't be read
      */
     protected Object createResults(ResultSet rs, String flags)
         throws AdapterException {
 
         boolean            flagMetadata = hasFlag(flags, "metadata", false);
         ResultSetMetaData  meta;
         Dict               dict;
 
         try {
             meta = rs.getMetaData();
         } catch (SQLException e) {
             throw new AdapterException("failed to retrieve query result meta-data: " +
                                        e.getMessage());
         }
         if (flagMetadata) {
             dict = new Dict();
             dict.set("columns", createColumnData(meta, flags));
             dict.set("rows", createRowData(meta, rs, flags));
             return dict;
         } else {
             return createRowData(meta, rs, flags);
         }
     }
 
     /**
      * Converts the query meta-data into an array of column data objects.
      *
      * @param meta           the result set meta-data to convert
      * @param flags          the processing and mapping flags
      *
      * @return the array of column information
      *
      * @throws AdapterException if the result data couldn't be read
      */
     protected Array createColumnData(ResultSetMetaData meta, String flags)
         throws AdapterException {
 
         Array  cols;
         Dict   obj;
         int    colCount;
 
         try {
             colCount = meta.getColumnCount();
             cols = new Array(colCount);
             for (int i = 0; i < colCount; i++) {
                 obj = new Dict();
                 obj.set("name", meta.getColumnLabel(i + 1).toLowerCase());
                 obj.set("catalog", meta.getCatalogName(i + 1));
                 obj.set("type", meta.getColumnTypeName(i + 1));
                 obj.setInt("jdbcType", meta.getColumnType(i + 1));
                 obj.set("schema", meta.getSchemaName(i + 1));
                 obj.set("table", meta.getTableName(i + 1));
                 obj.set("column", meta.getColumnName(i + 1));
                 cols.add(obj);
             }
         } catch (SQLException e) {
             throw new AdapterException("failed to extract query meta-data: " +
                                        e.getMessage());
         }
         return cols;
     }
 
     /**
      * Converts the query result set into an array or a dictionary
      * (depending on flags).
      *
      * @param meta           the result set meta-data
      * @param rs             the result set to convert
      * @param flags          the processing and mapping flags
      *
      * @return the array of rows or dictionary of a single row
      *
      * @throws AdapterException if the result data couldn't be read
      */
     protected Object createRowData(ResultSetMetaData meta, ResultSet rs, String flags)
         throws AdapterException {
 
         boolean  flagColumnNames = hasFlag(flags, "column-names", true);
         boolean  flagNativeTypes = hasFlag(flags, "native-types", true);
         boolean  flagBinaryData = hasFlag(flags, "binary-data", false);
         boolean  flagSingleRow = hasFlag(flags, "single-row", false);
         int      colCount;
         Array    rows = new Array(10);
         Dict     rowDict;
         Array    rowArr;
         Object   value;
 
         try {
             colCount = meta.getColumnCount();
             while (rs.next()) {
                 if (flagColumnNames) {
                     rowDict = new Dict();
                     for (int i = 0; i < colCount; i++) {
                         value = createValue(meta, rs, i + 1, flagNativeTypes, flagBinaryData);
                         rowDict.add(meta.getColumnLabel(i + 1).toLowerCase(), value);
                     }
                     rows.add(rowDict);
                 } else {
                     rowArr = new Array(colCount);
                     for (int i = 0; i < colCount; i++) {
                         value = createValue(meta, rs, i + 1, flagNativeTypes, flagBinaryData);
                         rowArr.add(value);
                     }
                     rows.add(rowArr);
                 }
             }
         } catch (SQLException e) {
             throw new AdapterException("failed to extract query results: " +
                                        e.getMessage());
         }
         if (flagSingleRow) {
             if (rows.size() < 1) {
                 return null;
             } else if (rows.size() == 1) {
                 return rows.get(0);
             } else {
                 throw new AdapterException("too many rows in query results; " +
                                            "expected 1, but found " +
                                            rows.size());
             }
         }
         return rows;
     }
 
     /**
      * Converts a specific row column value to a scriptable object. Normally
      * this means returning a simple string containing the value. If the native
      * types flag is set, SQL types will be converted into their native Java
      * object types by the JDBC driver. If the binary data flag is set, any
      * binary data will be returned in a byte[] instead of converted to a
      * string.
      *
      * @param meta           the result set meta-data
      * @param rs             the result set to convert
      * @param column         the column index
      * @param nativeTypes    the native value types flag
      * @param binaryData     the binary data support flag
      *
      * @return the scriptable object with the column value
      *
      * @throws AdapterException if the result data couldn't be read
      */
     protected Object createValue(ResultSetMetaData meta,
                                  ResultSet rs,
                                  int column,
                                  boolean nativeTypes,
                                  boolean binaryData)
         throws AdapterException {
 
         try {
             switch (meta.getColumnType(column)) {
             case Types.DATE:
             case Types.TIMESTAMP:
                 try {
                     return DateUtil.formatIsoDateTime(rs.getTimestamp(column));
                 } catch (SQLException e) {
                     // TODO: log this as a warning, it is here due to MySQL
                     //       dates being '0000-00-00' and such
                     return null;
                 }
             case Types.BINARY:
             case Types.BLOB:
             case Types.LONGVARBINARY:
             case Types.VARBINARY:
                 if (binaryData) {
                     ByteArrayOutputStream os = new ByteArrayOutputStream();
                     InputStream is = rs.getBinaryStream(column);
                     int count;
                     byte[] buffer = new byte[16384];
                     while ((count = is.read(buffer)) > 0 && os.size() < 1000000) {
                         os.write(buffer, 0, count);
                     }
                     return os.toByteArray();
                 } else {
                     return rs.getString(column);
                 }
             default:
                 if (nativeTypes) {
                     Object value = rs.getObject(column);
                     return isNativeValue(value) ? value : rs.getString(column);
                 } else {
                     return rs.getString(column);
                 }
             }
         } catch (Exception e) {
             throw new AdapterException("failed to extract query result value " +
                                        "for column " + column + ": " +
                                        e.getMessage());
         }
     }
 
     /**
      * Checks if a specified flag is either set or unset. I.e. this method both
      * checks for "no-whatever" and "whatever" in the flags string. If none of
      * the two variants is found, the default value is returned.
      *
      * @param flags          the flags string to check
      * @param flag           the flag name
      * @param defaultValue   the default flag value
      *
      * @return true if the flag was set, or
      *         false otherwise
      */
     protected boolean hasFlag(String flags, String flag, boolean defaultValue) {
         if (flags == null || flag == null) {
             return defaultValue;
         } else if (flags.indexOf("no-" + flag) >= 0) {
             return false;
         } else if (flags.indexOf(flag) >= 0) {
             return true;
         } else {
             return defaultValue;
         }
     }
 
     /**
      * Checks if a specified value is an acceptable native value. If this
      * method returns true, then the value will be returned. Otherwise a string
      * value will be extracted for the column instead. If native types are not
      * used for a query, this step will naturally be omitted.
      *
      * @param value          the value to check
      *
      * @return true if the value is of an acceptable native type, or
      *         false otherwise
      */
     protected boolean isNativeValue(Object value) {
         return value == null ||
                value instanceof Boolean ||
                value instanceof Number ||
                value instanceof String;
     }
 }

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
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.rapidcontext.core.type.Channel;
 import org.rapidcontext.core.type.ConnectionException;
 import org.rapidcontext.core.data.Array;
 import org.rapidcontext.core.data.Dict;
 import org.rapidcontext.util.DateUtil;
 
 /**
  * A JDBC communications channel. This class encapsulates a JDBC
  * connection and allows execution of arbitrary SQL queries or
  * statements.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class JdbcChannel extends Channel {
 
     /**
      * The class logger.
      */
     private static final Logger LOG =
         Logger.getLogger(JdbcChannel.class.getName());
 
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
      * Creates a new JDBC communications channel.
      *
      * @param parent            the parent JDBC connection
      * @param props             the connection properties (user and password)
      *
      * @throws ConnectionException if a connection couldn't be established
      */
     protected JdbcChannel(JdbcConnection parent, Properties props)
     throws ConnectionException {
 
         super(parent);
         this.prefix = "[JDBC:" + (++counter) + "] ";
         this.sqlPing = parent.ping();
         this.timeout = parent.timeout();
         try {
             LOG.info(prefix + "creating connection for " + parent.url());
             DriverManager.setLoginTimeout(timeout);
             con = parent.driver().connect(parent.url(), props);
             con.setAutoCommit(parent.autoCommit());
             LOG.fine(prefix + "done creating connection for " + parent.url());
         } catch (SQLException e) {
             String msg = "failed to connect to " + parent.url() +
                   " with username '" + props.getProperty("user") + "': " +
                   e.getMessage();
             LOG.warning(prefix + msg);
             throw new ConnectionException(msg);
         }
     }
 
     /**
      * Checks if this channel can be pooled (i.e. reused). This
      * method should return the same value for all instances of a
      * specific channel subclass.
      *
      * @return true if the channel can be pooled and reused, or
      *         false otherwise
      */
     protected boolean isPoolable() {
         return true;
     }
 
     /**
      * Reserves and activates the channel. This method is called just
      * before a channel is to be used, i.e. when a new channel has
      * been created or fetched from a resource pool.
      *
      * @throws ConnectionException if the channel couldn't be
      *             reserved (channel will be destroyed)
      */
     protected void reserve() throws ConnectionException {
         String  msg;
 
         try {
             if (con.isClosed()) {
                 msg = "failed to reserve, connection channel already closed";
                 LOG.fine(prefix + msg);
                 throw new ConnectionException(msg);
             }
         } catch (SQLException e) {
             msg = "failed to reserve: " + e.getMessage();
             LOG.fine(prefix + msg);
             throw new ConnectionException(msg);
         }
     }
 
     /**
      * Releases and passivates the channel. This method is called
      * just after a channel has been used and returned. This should
      * clear or reset the channel, so that it can safely be used
      * again without affecting previous results or operations (if
      * the channel is pooled).
      */
     protected void release() {
         // Nothing to do here
     }
 
     /**
      * Checks if the channel connection is still valid. This method
      * is called before using a channel and regularly when it is idle
      * in the pool. It can be used to trigger a "ping" for a channel.
      * This method can only mark a valid channel as invalid, never
      * the other way around.
      *
      * @see #isValid()
      * @see #invalidate()
      */
     public void validate() {
         if (sqlPing != null && !sqlPing.trim().isEmpty()) {
             try {
                 executeQuery(sqlPing);
             } catch (Exception e) {
                 LOG.log(Level.WARNING, prefix + "validation failure", e);
                 invalidate();
             }
         }
     }
 
     /**
      * Closes the connection. This method is used to free any
      * resources used by the connection.
      */
     protected void close() {
         try {
             LOG.info(prefix + "closing connection");
             con.close();
             LOG.fine(prefix + "done closing connection");
         } catch (SQLException e) {
             LOG.log(Level.WARNING, prefix + "failed to close connection", e);
         }
     }
 
     /**
      * Commits any pending changes. This method is called after each
      * successful procedure tree execution that included this
      * connection.
      */
     public void commit() {
         try {
             con.commit();
         } catch (SQLException e) {
             LOG.log(Level.WARNING, prefix + "failed to commit connection", e);
         }
     }
 
     /**
      * Rolls any pending changes back. This method is called after an
      * unsuccessful procedure tree execution that included this
      * connection.
      */
     public void rollback() {
         try {
             con.rollback();
         } catch (SQLException e) {
             LOG.log(Level.WARNING, prefix + "failed to rollback connection", e);
         }
     }
 
     /**
      * Executes an SQL statement.
      *
      * @param sql            the SQL statement to execute
      *
      * @return the array with generated keys
      *
      * @throws ConnectionException if the execution failed
      */
     public Array executeStatement(String sql) throws ConnectionException {
         try {
             LOG.fine(prefix + "executing statement: " + sql);
             Array res = executeStatement(prepare(sql, null));
             LOG.fine(prefix + "done executing statement: " + sql);
             return res;
         } catch (ConnectionException e) {
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
      * @throws ConnectionException if the execution failed
      */
     protected Array executeStatement(PreparedStatement stmt)
     throws ConnectionException {
 
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
             throw new ConnectionException("failed to execute statement: " +
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
      * @throws ConnectionException if the execution failed
      */
     public Object executeQuery(String sql) throws ConnectionException {
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
      * @throws ConnectionException if the execution failed
      */
     public Object executeQuery(String sql, String flags)
     throws ConnectionException {
 
         try {
             LOG.fine(prefix + "executing query: " + sql);
             Object res = executeQuery(prepare(sql, null), flags);
             LOG.fine(prefix + "done executing query: " + sql);
             return res;
         } catch (ConnectionException e) {
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
      * @throws ConnectionException if the execution failed
      */
     protected Object executeQuery(PreparedStatement stmt)
     throws ConnectionException {
 
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
      * @throws ConnectionException if the execution failed
      */
     protected Object executeQuery(PreparedStatement stmt, String flags)
     throws ConnectionException {
 
         ResultSet  set = null;
 
         try {
             set = stmt.executeQuery();
             return createResults(set, flags);
         } catch (SQLException e) {
             throw new ConnectionException("failed to execute query: " +
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
       @throws ConnectionException if the statement couldn't be prepared
      */
     protected PreparedStatement prepare(String sql, ArrayList params)
     throws ConnectionException {
 
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
             throw new ConnectionException(str);
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
      * @throws ConnectionException if the result data couldn't be read
      */
     protected Object createResults(ResultSet rs, String flags)
     throws ConnectionException {
 
         boolean            flagMetadata = hasFlag(flags, "metadata", false);
         ResultSetMetaData  meta;
         Dict               dict;
 
         try {
             meta = rs.getMetaData();
         } catch (SQLException e) {
             throw new ConnectionException("failed to retrieve query result meta-data: " +
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
      * @throws ConnectionException if the result data couldn't be read
      */
     protected Array createColumnData(ResultSetMetaData meta, String flags)
     throws ConnectionException {
 
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
             throw new ConnectionException("failed to extract query meta-data: " +
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
      * @throws ConnectionException if the result data couldn't be read
      */
     protected Object createRowData(ResultSetMetaData meta, ResultSet rs, String flags)
     throws ConnectionException {
 
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
             throw new ConnectionException("failed to extract query results: " +
                                           e.getMessage());
         }
         if (flagSingleRow) {
             if (rows.size() < 1) {
                 return null;
             } else if (rows.size() == 1) {
                 return rows.get(0);
             } else {
                 throw new ConnectionException("too many rows in query results; " +
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
      * @throws ConnectionException if the result data couldn't be read
      */
     protected Object createValue(ResultSetMetaData meta,
                                  ResultSet rs,
                                  int column,
                                  boolean nativeTypes,
                                  boolean binaryData)
     throws ConnectionException {
 
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
             throw new ConnectionException("failed to extract query result value " +
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

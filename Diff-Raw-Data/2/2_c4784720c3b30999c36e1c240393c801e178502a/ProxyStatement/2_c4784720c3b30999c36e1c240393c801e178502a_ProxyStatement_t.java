 package org.activiti.upgrade;
 
 import java.io.InputStream;
 import java.io.Reader;
 import java.math.BigDecimal;
 import java.net.URL;
 import java.sql.Array;
 import java.sql.Blob;
 import java.sql.Clob;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.NClob;
 import java.sql.ParameterMetaData;
 import java.sql.PreparedStatement;
 import java.sql.Ref;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.RowId;
 import java.sql.SQLException;
 import java.sql.SQLWarning;
 import java.sql.SQLXML;
 import java.sql.Statement;
 import java.sql.Time;
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.activiti.engine.impl.util.IoUtil;
 
 
 public class ProxyStatement implements PreparedStatement {
 
   Statement statement;
   PreparedStatement preparedStatement;
   ProxyConnection connection;
   
   String sql;
   Map<Integer, String> parameters = new HashMap<Integer, String>();
   
   public ProxyStatement(Statement statement, ProxyConnection connection) {
     this.statement = statement;
     this.connection = connection;
   }
 
   public ProxyStatement(PreparedStatement statement, String sql, ProxyConnection connection) {
     this.statement = statement;
     this.preparedStatement = statement;
     this.connection = connection;
     this.sql = sql;
   }
   
   public int executeUpdate() throws SQLException {
     resolveAndAddStatement();
     return preparedStatement.executeUpdate();
   }
   
   public boolean execute() throws SQLException {
     resolveAndAddStatement();
     return preparedStatement.execute();
   }
 
   void resolveAndAddStatement() {
     if (!sql.startsWith("select") && !sql.startsWith("SELECT")) {
       resolveParameters();
       ProxyDriver.addStatement(sql);
     }
   }
 
   void resolveParameters() {
     int parameterIndex = 1;
     int questionMarkIndex = sql.indexOf('?'); 
     while (questionMarkIndex!=-1) {
       sql = sql.substring(0, questionMarkIndex)
             + parameters.get(parameterIndex)
             + sql.substring(questionMarkIndex+1);
       parameterIndex++;
       questionMarkIndex = sql.indexOf('?');
     }
   }
 
   public int executeUpdate(String sql) throws SQLException {
     ProxyDriver.addStatement(sql);
     return statement.executeUpdate(sql);
   }
 
   public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
     ProxyDriver.addStatement(sql);
     return statement.executeUpdate(sql, autoGeneratedKeys);
   }
 
   public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
     ProxyDriver.addStatement(sql);
     return statement.executeUpdate(sql, columnIndexes);
   }
 
   public int executeUpdate(String sql, String[] columnNames) throws SQLException {
     ProxyDriver.addStatement(sql);
     return statement.executeUpdate(sql, columnNames);
   }
   
   //////////////////////////////////////////////////////////////////////
   
   public void setString(int parameterIndex, String x) throws SQLException {
     parameters.put(parameterIndex, "'"+x+"'");
     preparedStatement.setString(parameterIndex, x);
   }
 
   public void setBoolean(int parameterIndex, boolean x) throws SQLException {
     parameters.put(parameterIndex, Boolean.toString(x));
     preparedStatement.setBoolean(parameterIndex, x);
   }
 
   public void setNull(int parameterIndex, int sqlType) throws SQLException {
     parameters.put(parameterIndex, "null");
     preparedStatement.setNull(parameterIndex, sqlType);
   }
 
   public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
     parameters.put(parameterIndex, "null");
     preparedStatement.setNull(parameterIndex, sqlType, typeName);
   }
 
   public void setTime(int parameterIndex, Time x) throws SQLException {
     parameters.put(parameterIndex, ProxyDriver.dateFormat.format(x));
     preparedStatement.setTime(parameterIndex, x);
   }
 
   public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
     parameters.put(parameterIndex, ProxyDriver.dateFormat.format(x));
     preparedStatement.setTime(parameterIndex, x, cal);
   }
 
   public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
     parameters.put(parameterIndex, ProxyDriver.dateFormat.format(x));
     preparedStatement.setTimestamp(parameterIndex, x);
   }
 
   public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
     parameters.put(parameterIndex, ProxyDriver.dateFormat.format(x));
     preparedStatement.setTimestamp(parameterIndex, x, cal);
   }
   
   public void setDate(int parameterIndex, Date x) throws SQLException {
     parameters.put(parameterIndex, ProxyDriver.dateFormat.format(x));
     preparedStatement.setDate(parameterIndex, x);
   }
 
   public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
     parameters.put(parameterIndex, ProxyDriver.dateFormat.format(x));
     preparedStatement.setDate(parameterIndex, x, cal);
   }
 
   public void setInt(int parameterIndex, int x) throws SQLException {
     parameters.put(parameterIndex, Integer.toString(x));
     preparedStatement.setInt(parameterIndex, x);
   }
 
   public void setLong(int parameterIndex, long x) throws SQLException {
     parameters.put(parameterIndex, Long.toString(x));
     preparedStatement.setLong(parameterIndex, x);
   }
   
   public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
     setInputStreamParameter(parameterIndex, x);
     preparedStatement.setBinaryStream(parameterIndex, x);
   }
 
   public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
     setInputStreamParameter(parameterIndex, x);
     preparedStatement.setBinaryStream(parameterIndex, x, length);
   }
 
   public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
     setInputStreamParameter(parameterIndex, x);
     preparedStatement.setBinaryStream(parameterIndex, x, length);
   }
 
   void setInputStreamParameter(int parameterIndex, InputStream x) {
     byte[] bytes = IoUtil.readInputStream(x, "jdbc variable bytes");
     StringBuffer sb = new StringBuffer();
     for (byte b : bytes) {
      sb.append(String.format("%02X", b));
     }
     parameters.put(parameterIndex, "0x"+sb.toString());
   }
 
   //////////////////////////////////////////////////////////////////////
   
   public void setArray(int parameterIndex, Array x) throws SQLException {
     parameters.put(parameterIndex, "setString["+x+"]");
     preparedStatement.setArray(parameterIndex, x);
   }
 
   public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
     parameters.put(parameterIndex, "setAsciiStream["+x+"]");
     preparedStatement.setAsciiStream(parameterIndex, x);
   }
 
   public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
     parameters.put(parameterIndex, "setAsciiStream["+x+"]");
     preparedStatement.setAsciiStream(parameterIndex, x, length);
   }
 
   public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
     parameters.put(parameterIndex, "setAsciiStream["+x+"]");
     preparedStatement.setAsciiStream(parameterIndex, x, length);
   }
 
   public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
     parameters.put(parameterIndex, "setBigDecimal["+x+"]");
     preparedStatement.setBigDecimal(parameterIndex, x);
   }
 
   public void setBlob(int parameterIndex, Blob x) throws SQLException {
     parameters.put(parameterIndex, "setBlob["+x+"]");
     preparedStatement.setBlob(parameterIndex, x);
   }
 
   public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
     parameters.put(parameterIndex, "setBlob["+inputStream+"]");
     preparedStatement.setBlob(parameterIndex, inputStream);
   }
 
   public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
     parameters.put(parameterIndex, "setBlob["+inputStream+"]");
     preparedStatement.setBlob(parameterIndex, inputStream, length);
   }
 
   public void setByte(int parameterIndex, byte x) throws SQLException {
     parameters.put(parameterIndex, "setByte["+x+"]");
     preparedStatement.setByte(parameterIndex, x);
   }
 
   public void setBytes(int parameterIndex, byte[] x) throws SQLException {
     parameters.put(parameterIndex, "setBytes["+x+"]");
     preparedStatement.setBytes(parameterIndex, x);
   }
 
   public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
     parameters.put(parameterIndex, "setCharacterStream["+reader+"]");
     preparedStatement.setCharacterStream(parameterIndex, reader); 
   }
 
   public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
     parameters.put(parameterIndex, "setCharacterStream["+reader+"]");
     preparedStatement.setCharacterStream(parameterIndex, reader, length);
   }
 
   public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
     parameters.put(parameterIndex, "setCharacterStream["+reader+"]");
     preparedStatement.setCharacterStream(parameterIndex, reader, length);
   }
 
   public void setClob(int parameterIndex, Clob x) throws SQLException {
     parameters.put(parameterIndex, "setClob["+x+"]");
     preparedStatement.setClob(parameterIndex, x);
   }
 
   public void setClob(int parameterIndex, Reader reader) throws SQLException {
     parameters.put(parameterIndex, "setClob["+reader+"]");
     preparedStatement.setClob(parameterIndex, reader);
   }
 
   public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
     parameters.put(parameterIndex, "setClob["+reader+"]");
     preparedStatement.setClob(parameterIndex, reader, length);
   }
 
   public void setDouble(int parameterIndex, double x) throws SQLException {
     parameters.put(parameterIndex, "setDouble["+x+"]");
     preparedStatement.setDouble(parameterIndex, x);
   }
 
   public void setFloat(int parameterIndex, float x) throws SQLException {
     parameters.put(parameterIndex, "setDouble["+x+"]");
     preparedStatement.setFloat(parameterIndex, x);
   }
 
   public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
     parameters.put(parameterIndex, "setNCharacterStream["+value+"]");
     preparedStatement.setNCharacterStream(parameterIndex, value);
   }
 
   public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
     parameters.put(parameterIndex, "setNCharacterStream["+value+"]");
     preparedStatement.setNCharacterStream(parameterIndex, value, length);
   }
 
   public void setNClob(int parameterIndex, NClob value) throws SQLException {
     parameters.put(parameterIndex, "setNClob["+value+"]");
     preparedStatement.setNClob(parameterIndex, value);
   }
 
   public void setNClob(int parameterIndex, Reader reader) throws SQLException {
     parameters.put(parameterIndex, "setNClob["+reader+"]");
     preparedStatement.setNClob(parameterIndex, reader);
   }
 
   public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
     parameters.put(parameterIndex, "setNClob["+reader+"]");
     preparedStatement.setNClob(parameterIndex, reader, length);
   }
 
   public void setNString(int parameterIndex, String value) throws SQLException {
     parameters.put(parameterIndex, "setNString["+value+"]");
     preparedStatement.setNString(parameterIndex, value);
   }
 
   public void setObject(int parameterIndex, Object x) throws SQLException {
     parameters.put(parameterIndex, "setObject["+x+"]");
     preparedStatement.setObject(parameterIndex, x);
   }
 
   public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
     parameters.put(parameterIndex, "setObject["+x+"]");
     preparedStatement.setObject(parameterIndex, x, targetSqlType);
   }
 
   public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength) throws SQLException {
     parameters.put(parameterIndex, "setObject["+x+"]");
     preparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
   }
 
   public void setRef(int parameterIndex, Ref x) throws SQLException {
     parameters.put(parameterIndex, "setRef["+x+"]");
     preparedStatement.setRef(parameterIndex, x);
   }
 
   public void setRowId(int parameterIndex, RowId x) throws SQLException {
     parameters.put(parameterIndex, "setRowId["+x+"]");
     preparedStatement.setRowId(parameterIndex, x);
   }
 
   public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
     parameters.put(parameterIndex, "setSQLXML["+xmlObject+"]");
     preparedStatement.setSQLXML(parameterIndex, xmlObject);
   }
 
   public void setShort(int parameterIndex, short x) throws SQLException {
     parameters.put(parameterIndex, "setShort["+x+"]");
     preparedStatement.setShort(parameterIndex, x);
   }
 
   public void setURL(int parameterIndex, URL x) throws SQLException {
     parameters.put(parameterIndex, "setURL["+x+"]");
     preparedStatement.setURL(parameterIndex, x);
   }
 
   public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
     parameters.put(parameterIndex, "setUnicodeStream["+x+"]");
     preparedStatement.setUnicodeStream(parameterIndex, x, length);
   }
 
   //////////////////////////////////////////////////////////////////////
   
   public Connection getConnection() throws SQLException {
     return connection;
   }
 
   public boolean isWrapperFor(Class< ? > iface) throws SQLException {
     return statement.isWrapperFor(iface);
   }
 
   public <T> T unwrap(Class<T> iface) throws SQLException {
     return statement.unwrap(iface);
   }
 
   public void addBatch(String sql) throws SQLException {
     statement.addBatch(sql);
   }
 
   public void cancel() throws SQLException {
     statement.cancel();
   }
 
   public void clearBatch() throws SQLException {
     statement.clearBatch();
   }
 
   public void clearWarnings() throws SQLException {
     statement.clearWarnings();
   }
 
   public void close() throws SQLException {
     statement.close();
   }
 
   public boolean execute(String sql) throws SQLException {
     
     return statement.execute(sql);
   }
 
   public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
     return statement.execute(sql, autoGeneratedKeys);
   }
 
   public boolean execute(String sql, int[] columnIndexes) throws SQLException {
     return statement.execute(sql, columnIndexes);
   }
 
   public boolean execute(String sql, String[] columnNames) throws SQLException {
     return statement.execute(sql, columnNames);
   }
 
   public int[] executeBatch() throws SQLException {
     return statement.executeBatch();
   }
 
   public ResultSet executeQuery(String sql) throws SQLException {
     return statement.executeQuery(sql);
   }
 
   public int getFetchDirection() throws SQLException {
     return statement.getFetchDirection();
   }
 
   public int getFetchSize() throws SQLException {
     return statement.getFetchSize();
   }
 
   public ResultSet getGeneratedKeys() throws SQLException {
     return statement.getGeneratedKeys();
   }
 
   public int getMaxFieldSize() throws SQLException {
     return statement.getMaxFieldSize();
   }
 
   public int getMaxRows() throws SQLException {
     return statement.getMaxRows();
   }
 
   public boolean getMoreResults() throws SQLException {
     return statement.getMoreResults();
   }
 
   public boolean getMoreResults(int current) throws SQLException {
     return statement.getMoreResults(current);
   }
 
   public int getQueryTimeout() throws SQLException {
     return statement.getQueryTimeout();
   }
 
   public ResultSet getResultSet() throws SQLException {
     return statement.getResultSet();
   }
 
   public int getResultSetConcurrency() throws SQLException {
     return statement.getResultSetConcurrency();
   }
 
   public int getResultSetHoldability() throws SQLException {
     return statement.getResultSetHoldability();
   }
 
   public int getResultSetType() throws SQLException {
     return statement.getResultSetType();
   }
 
   public int getUpdateCount() throws SQLException {
     return statement.getUpdateCount();
   }
 
   public SQLWarning getWarnings() throws SQLException {
     return statement.getWarnings();
   }
 
   public boolean isClosed() throws SQLException {
     return statement.isClosed();
   }
 
   public boolean isPoolable() throws SQLException {
     return statement.isPoolable();
   }
 
   public void setCursorName(String name) throws SQLException {
     statement.setCursorName(name);
   }
 
   public void setEscapeProcessing(boolean enable) throws SQLException {
     statement.setEscapeProcessing(enable);
   }
 
   public void setFetchDirection(int direction) throws SQLException {
     statement.setFetchDirection(direction);
   }
 
   public void setFetchSize(int rows) throws SQLException {
     statement.setFetchSize(rows);
   }
 
   public void setMaxFieldSize(int max) throws SQLException {
     statement.setMaxFieldSize(max);
   }
 
   public void setMaxRows(int max) throws SQLException {
     statement.setMaxRows(max);
   }
 
   public void setPoolable(boolean poolable) throws SQLException {
     statement.setPoolable(poolable);
   }
 
   public void setQueryTimeout(int seconds) throws SQLException {
     statement.setQueryTimeout(seconds);
   }
 
   public void addBatch() throws SQLException {
     preparedStatement.addBatch();
   }
 
   public void clearParameters() throws SQLException {
     preparedStatement.clearParameters();
   }
 
   public ResultSet executeQuery() throws SQLException {
     return preparedStatement.executeQuery();
   }
 
   public ResultSetMetaData getMetaData() throws SQLException {
     return preparedStatement.getMetaData();
   }
 
   public ParameterMetaData getParameterMetaData() throws SQLException {
     return preparedStatement.getParameterMetaData();
   }
 
 }

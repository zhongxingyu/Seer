 /**
  * Copyright (c) 2009 Aurora Software Technology Studio. All rights reserved.
  */
 package com.corona.data.sql;
 
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
 import java.sql.Time;
 import java.sql.Timestamp;
 import java.util.Calendar;
 
 /**
  * <p>This class is used to wrapper a prepared JDBC statement. </p>
  *
  * @author $Author$
  * @version $Id$
  */
 public class PreparedStatementWrapper implements PreparedStatement {
 
 	/**
 	 * the prepared SQL statement
 	 */
 	private PreparedStatement statement;
 	
 	/**
 	 * default constructor
 	 */
 	protected PreparedStatementWrapper() {
 		// do nothing
 	}
 	
 	/**
 	 * @param preparedStatement the prepared JDBC statement
 	 */
 	public PreparedStatementWrapper(final PreparedStatement preparedStatement) {
 		this.setPreparedStatement(preparedStatement);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return (this.statement == null) ? "" : this.statement.toString();
 	}
 
 	/**
 	 * @param preparedStatement the prepared JDBC statement
 	 */
 	protected void setPreparedStatement(final PreparedStatement preparedStatement) {
 		this.statement = preparedStatement;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#executeQuery(java.lang.String)
 	 */
 	@Override
 	public ResultSet executeQuery(final String sql) throws SQLException {
 		return this.statement.executeQuery(sql);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#executeUpdate(java.lang.String)
 	 */
 	@Override
 	public int executeUpdate(final String sql) throws SQLException {
 		return this.statement.executeUpdate(sql);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#close()
 	 */
 	@Override
 	public void close() throws SQLException {
 		this.statement.close();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#getMaxFieldSize()
 	 */
 	@Override
 	public int getMaxFieldSize() throws SQLException {
 		return this.statement.getMaxFieldSize();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#setMaxFieldSize(int)
 	 */
 	@Override
 	public void setMaxFieldSize(final int max) throws SQLException {
 		this.statement.setMaxFieldSize(max);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#getMaxRows()
 	 */
 	@Override
 	public int getMaxRows() throws SQLException {
 		return this.statement.getMaxRows();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#setMaxRows(int)
 	 */
 	@Override
 	public void setMaxRows(final int max) throws SQLException {
 		this.statement.setMaxRows(max);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#setEscapeProcessing(boolean)
 	 */
 	@Override
 	public void setEscapeProcessing(final boolean enable) throws SQLException {
 		this.statement.setEscapeProcessing(enable);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#getQueryTimeout()
 	 */
 	@Override
 	public int getQueryTimeout() throws SQLException {
 		return this.statement.getQueryTimeout();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#setQueryTimeout(int)
 	 */
 	@Override
 	public void setQueryTimeout(final int seconds) throws SQLException {
 		this.statement.setQueryTimeout(seconds);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#cancel()
 	 */
 	@Override
 	public void cancel() throws SQLException {
 		this.statement.cancel();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#getWarnings()
 	 */
 	@Override
 	public SQLWarning getWarnings() throws SQLException {
 		return this.statement.getWarnings();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#clearWarnings()
 	 */
 	@Override
 	public void clearWarnings() throws SQLException {
 		this.statement.clearWarnings();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#setCursorName(java.lang.String)
 	 */
 	@Override
 	public void setCursorName(final String name) throws SQLException {
 		this.statement.setCursorName(name);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#execute(java.lang.String)
 	 */
 	@Override
 	public boolean execute(final String sql) throws SQLException {
 		return this.statement.execute(sql);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#getResultSet()
 	 */
 	@Override
 	public ResultSet getResultSet() throws SQLException {
 		return this.statement.getResultSet();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#getUpdateCount()
 	 */
 	@Override
 	public int getUpdateCount() throws SQLException {
 		return this.statement.getUpdateCount();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#getMoreResults()
 	 */
 	@Override
 	public boolean getMoreResults() throws SQLException {
 		return this.statement.getMoreResults();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#setFetchDirection(int)
 	 */
 	@Override
 	public void setFetchDirection(final int direction) throws SQLException {
 		this.statement.setFetchDirection(direction);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#getFetchDirection()
 	 */
 	@Override
 	public int getFetchDirection() throws SQLException {
 		return this.statement.getFetchDirection();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#setFetchSize(int)
 	 */
 	@Override
 	public void setFetchSize(final int rows) throws SQLException {
 		this.statement.setFetchSize(rows);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#getFetchSize()
 	 */
 	@Override
 	public int getFetchSize() throws SQLException {
 		return this.statement.getFetchSize();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#getResultSetConcurrency()
 	 */
 	@Override
 	public int getResultSetConcurrency() throws SQLException {
 		return this.statement.getResultSetConcurrency();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#getResultSetType()
 	 */
 	@Override
 	public int getResultSetType() throws SQLException {
 		return this.statement.getResultSetType();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#addBatch(java.lang.String)
 	 */
 	@Override
 	public void addBatch(final String sql) throws SQLException {
 		this.statement.addBatch(sql);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#clearBatch()
 	 */
 	@Override
 	public void clearBatch() throws SQLException {
 		this.statement.clearBatch();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#executeBatch()
 	 */
 	@Override
 	public int[] executeBatch() throws SQLException {
 		return this.statement.executeBatch();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#getConnection()
 	 */
 	@Override
 	public Connection getConnection() throws SQLException {
 		return this.statement.getConnection();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#getMoreResults(int)
 	 */
 	@Override
 	public boolean getMoreResults(final int current) throws SQLException {
 		return this.statement.getMoreResults(current);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#getGeneratedKeys()
 	 */
 	@Override
 	public ResultSet getGeneratedKeys() throws SQLException {
 		return this.statement.getGeneratedKeys();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#executeUpdate(java.lang.String, int)
 	 */
 	@Override
 	public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
 		return this.statement.executeUpdate(sql, autoGeneratedKeys);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#executeUpdate(java.lang.String, int[])
 	 */
 	@Override
 	public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
 		return this.statement.executeUpdate(sql, columnIndexes);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#executeUpdate(java.lang.String, java.lang.String[])
 	 */
 	@Override
 	public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
 		return this.statement.executeUpdate(sql, columnNames);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#execute(java.lang.String, int)
 	 */
 	@Override
 	public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
 		return this.statement.execute(sql, autoGeneratedKeys);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#execute(java.lang.String, int[])
 	 */
 	@Override
 	public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
 		return this.statement.execute(sql, columnIndexes);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#execute(java.lang.String, java.lang.String[])
 	 */
 	@Override
 	public boolean execute(final String sql, final String[] columnNames) throws SQLException {
 		return this.statement.execute(sql, columnNames);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#getResultSetHoldability()
 	 */
 	@Override
 	public int getResultSetHoldability() throws SQLException {
 		return this.statement.getResultSetHoldability();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#isClosed()
 	 */
 	@Override
 	public boolean isClosed() throws SQLException {
 		return this.statement.isClosed();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#setPoolable(boolean)
 	 */
 	@Override
 	public void setPoolable(final boolean poolable) throws SQLException {
 		this.statement.setPoolable(poolable);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Statement#isPoolable()
 	 */
 	@Override
 	public boolean isPoolable() throws SQLException {
 		return this.statement.isPoolable();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
 	 */
 	@Override
 	public <T> T unwrap(final Class<T> iface) throws SQLException {
 		return this.statement.unwrap(iface);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
 	 */
 	@Override
 	public boolean isWrapperFor(final Class<?> iface) throws SQLException {
 		return this.statement.isWrapperFor(iface);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#executeQuery()
 	 */
 	@Override
 	public ResultSet executeQuery() throws SQLException {
 		return this.statement.executeQuery();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#executeUpdate()
 	 */
 	@Override
 	public int executeUpdate() throws SQLException {
 		return this.statement.executeUpdate();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setNull(int, int)
 	 */
 	@Override
 	public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
 		this.statement.setNull(parameterIndex, sqlType);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setBoolean(int, boolean)
 	 */
 	@Override
 	public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
 		this.statement.setBoolean(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setByte(int, byte)
 	 */
 	@Override
 	public void setByte(final int parameterIndex, final byte x) throws SQLException {
 		this.statement.setByte(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setShort(int, short)
 	 */
 	@Override
 	public void setShort(final int parameterIndex, final short x) throws SQLException {
 		this.statement.setShort(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setInt(int, int)
 	 */
 	@Override
 	public void setInt(final int parameterIndex, final int x) throws SQLException {
 		this.statement.setInt(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setLong(int, long)
 	 */
 	@Override
 	public void setLong(final int parameterIndex, final long x) throws SQLException {
 		this.statement.setLong(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setFloat(int, float)
 	 */
 	@Override
 	public void setFloat(final int parameterIndex, final float x) throws SQLException {
 		this.statement.setFloat(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setDouble(int, double)
 	 */
 	@Override
 	public void setDouble(final int parameterIndex, final double x) throws SQLException {
 		this.statement.setDouble(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setBigDecimal(int, java.math.BigDecimal)
 	 */
 	@Override
 	public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
 		this.statement.setBigDecimal(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setString(int, java.lang.String)
 	 */
 	@Override
 	public void setString(final int parameterIndex, final String x) throws SQLException {
 		this.statement.setString(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setBytes(int, byte[])
 	 */
 	@Override
 	public void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
 		this.statement.setBytes(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date)
 	 */
 	@Override
 	public void setDate(final int parameterIndex, final Date x) throws SQLException {
 		this.statement.setDate(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time)
 	 */
 	@Override
 	public void setTime(final int parameterIndex, final Time x) throws SQLException {
 		this.statement.setTime(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp)
 	 */
 	@Override
 	public void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
 		this.statement.setTimestamp(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, int)
 	 */
 	@Override
 	public void setAsciiStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
 		this.statement.setAsciiStream(parameterIndex, x, length);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setUnicodeStream(int, java.io.InputStream, int)
 	 */
 	@SuppressWarnings("deprecation")
 	@Override
 	public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
 		this.statement.setUnicodeStream(parameterIndex, x, length);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, int)
 	 */
 	@Override
 	public void setBinaryStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
 		this.statement.setBinaryStream(parameterIndex, x, length);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#clearParameters()
 	 */
 	@Override
 	public void clearParameters() throws SQLException {
 		this.statement.clearParameters();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int)
 	 */
 	@Override
 	public void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
 		this.statement.setObject(parameterIndex, x, targetSqlType);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object)
 	 */
 	@Override
 	public void setObject(final int parameterIndex, final Object x) throws SQLException {
 		this.statement.setObject(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#execute()
 	 */
 	@Override
 	public boolean execute() throws SQLException {
 		return this.statement.execute();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#addBatch()
 	 */
 	@Override
 	public void addBatch() throws SQLException {
 		this.statement.addBatch();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, int)
 	 */
 	@Override
 	public void setCharacterStream(
 			final int parameterIndex, final Reader reader, final int length) throws SQLException {
 		this.statement.setCharacterStream(parameterIndex, reader, length);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setRef(int, java.sql.Ref)
 	 */
 	@Override
 	public void setRef(final int parameterIndex, final Ref x) throws SQLException {
 		this.statement.setRef(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setBlob(int, java.sql.Blob)
 	 */
 	@Override
 	public void setBlob(final int parameterIndex, final Blob x) throws SQLException {
 		this.statement.setBlob(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setClob(int, java.sql.Clob)
 	 */
 	@Override
 	public void setClob(final int parameterIndex, final Clob x) throws SQLException {
 		this.statement.setClob(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setArray(int, java.sql.Array)
 	 */
 	@Override
 	public void setArray(final int parameterIndex, final Array x) throws SQLException {
 		this.statement.setArray(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#getMetaData()
 	 */
 	@Override
 	public ResultSetMetaData getMetaData() throws SQLException {
 		return this.statement.getMetaData();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setDate(int, java.sql.Date, java.util.Calendar)
 	 */
 	@Override
 	public void setDate(final int parameterIndex, final Date x, final Calendar cal) throws SQLException {
 		this.statement.setDate(parameterIndex, x, cal);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setTime(int, java.sql.Time, java.util.Calendar)
 	 */
 	@Override
 	public void setTime(final int parameterIndex, final Time x, final Calendar cal) throws SQLException {
 		this.statement.setTime(parameterIndex, x, cal);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setTimestamp(int, java.sql.Timestamp, java.util.Calendar)
 	 */
 	@Override
 	public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException {
 		this.statement.setTimestamp(parameterIndex, x, cal);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setNull(int, int, java.lang.String)
 	 */
 	@Override
 	public void setNull(final int parameterIndex, final int sqlType, final String typeName) throws SQLException {
 		this.statement.setNull(parameterIndex, sqlType, typeName);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setURL(int, java.net.URL)
 	 */
 	@Override
 	public void setURL(final int parameterIndex, final URL x) throws SQLException {
 		this.statement.setURL(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#getParameterMetaData()
 	 */
 	@Override
 	public ParameterMetaData getParameterMetaData() throws SQLException {
 		return this.statement.getParameterMetaData();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setRowId(int, java.sql.RowId)
 	 */
 	@Override
 	public void setRowId(final int parameterIndex, final RowId x) throws SQLException {
 		this.statement.setRowId(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setNString(int, java.lang.String)
 	 */
 	@Override
 	public void setNString(final int parameterIndex, final String value) throws SQLException {
 		this.statement.setNString(parameterIndex, value);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader, long)
 	 */
 	@Override
 	public void setNCharacterStream(
 			final int parameterIndex, final Reader value, final long length) throws SQLException {
 		this.statement.setNCharacterStream(parameterIndex, value, length);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setNClob(int, java.sql.NClob)
 	 */
 	@Override
 	public void setNClob(final int parameterIndex, final NClob value) throws SQLException {
 		this.statement.setNClob(parameterIndex, value);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader, long)
 	 */
 	@Override
 	public void setClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
 		this.statement.setClob(parameterIndex, reader, length);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream, long)
 	 */
 	@Override
 	public void setBlob(
 			final int parameterIndex, final InputStream inputStream, final long length) throws SQLException {
 		this.statement.setBlob(parameterIndex, inputStream, length);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader, long)
 	 */
 	@Override
 	public void setNClob(final int parameterIndex, final Reader reader, final long length) throws SQLException {
 		this.statement.setNClob(parameterIndex, reader, length);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setSQLXML(int, java.sql.SQLXML)
 	 */
 	@Override
 	public void setSQLXML(final int parameterIndex, final SQLXML xmlObject) throws SQLException {
 		this.statement.setSQLXML(parameterIndex, xmlObject);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setObject(int, java.lang.Object, int, int)
 	 */
 	@Override
 	public void setObject(
 			final int parameterIndex, final Object x, final int targetSqlType, final int scaleOrLength
 	) throws SQLException {
 		this.statement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream, long)
 	 */
 	@Override
 	public void setAsciiStream(final int parameterIndex, final InputStream x, final long length) throws SQLException {
 		this.statement.setAsciiStream(parameterIndex, x, length);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream, long)
 	 */
 	@Override
 	public void setBinaryStream(final int parameterIndex, final InputStream x, final long length) throws SQLException {
 		this.statement.setBinaryStream(parameterIndex, x, length);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader, long)
 	 */
 	@Override
 	public void setCharacterStream(
 			final int parameterIndex, final Reader reader, final long length) throws SQLException {
 		this.statement.setCharacterStream(parameterIndex, reader, length);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setAsciiStream(int, java.io.InputStream)
 	 */
 	@Override
 	public void setAsciiStream(final int parameterIndex, final InputStream x) throws SQLException {
 		this.statement.setAsciiStream(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setBinaryStream(int, java.io.InputStream)
 	 */
 	@Override
 	public void setBinaryStream(final int parameterIndex, final InputStream x) throws SQLException {
 		this.statement.setBinaryStream(parameterIndex, x);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setCharacterStream(int, java.io.Reader)
 	 */
 	@Override
 	public void setCharacterStream(final int parameterIndex, final Reader reader) throws SQLException {
 		this.statement.setCharacterStream(parameterIndex, reader);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setNCharacterStream(int, java.io.Reader)
 	 */
 	@Override
 	public void setNCharacterStream(final int parameterIndex, final Reader value) throws SQLException {
 		this.statement.setNCharacterStream(parameterIndex, value);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setClob(int, java.io.Reader)
 	 */
 	@Override
 	public void setClob(final int parameterIndex, final Reader reader) throws SQLException {
 		this.statement.setClob(parameterIndex, reader);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setBlob(int, java.io.InputStream)
 	 */
 	@Override
 	public void setBlob(final int parameterIndex, final InputStream inputStream) throws SQLException {
 		this.statement.setBlob(parameterIndex, inputStream);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see java.sql.PreparedStatement#setNClob(int, java.io.Reader)
 	 */
 	@Override
 	public void setNClob(final int parameterIndex, final Reader reader) throws SQLException {
 		this.statement.setNClob(parameterIndex, reader);
 	}

	@Override
	public void closeOnCompletion() throws SQLException {
		this.statement.closeOnCompletion();
	}

	@Override
	public boolean isCloseOnCompletion() throws SQLException {
		return this.statement.isCloseOnCompletion();
	}
 }

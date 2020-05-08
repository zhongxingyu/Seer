 package com.digitald4.common.jdbc;
 
 import java.sql.Array;
 import java.sql.Blob;
 import java.sql.CallableStatement;
 import java.sql.Clob;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.NClob;
 import java.sql.PreparedStatement;
 import java.sql.SQLClientInfoException;
 import java.sql.SQLException;
 import java.sql.SQLWarning;
 import java.sql.SQLXML;
 import java.sql.Savepoint;
 import java.sql.Statement;
 import java.sql.Struct;
 import java.util.Collection;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Properties;
import java.util.concurrent.Executor;
 
 public class StatsConnection implements Connection {
 	private Connection con;
 	private Hashtable<String,StatsSQLImp> statementHash = new Hashtable<String,StatsSQLImp>();
 	
 	public void addStatement(StatsSQL tSQL){
 		String sql = tSQL.getSQL();
 		StatsSQLImp set = statementHash.get(sql);
 		if(set == null){
 			set = new StatsSQLImp(sql);
 			statementHash.put(sql,set);
 		}
 		set.processStatement(tSQL);
 	}
 	public Collection<StatsSQLImp> getTimedSQLStatements(){
 		return statementHash.values();
 	}
 	
 	public StatsConnection(Connection con){
 		this.con = con;
 	}
 	@Override
 	public boolean isWrapperFor(Class<?> iface) throws SQLException {
 		return con.isWrapperFor(iface);
 	}
 
 	@Override
 	public <T> T unwrap(Class<T> iface) throws SQLException {
 		return con.unwrap(iface);
 	}
 
 	@Override
 	public void clearWarnings() throws SQLException {
 		con.clearWarnings();
 	}
 
 	@Override
 	public void close() throws SQLException {
 		con.close();
 	}
 
 	@Override
 	public void commit() throws SQLException {
 		con.commit();
 	}
 
 	@Override
 	public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
 		return con.createArrayOf(typeName, elements);
 	}
 
 	@Override
 	public Blob createBlob() throws SQLException {
 		return con.createBlob();
 	}
 
 	@Override
 	public Clob createClob() throws SQLException {
 		return con.createClob();
 	}
 
 	@Override
 	public NClob createNClob() throws SQLException {
 		return con.createNClob();
 	}
 
 	@Override
 	public SQLXML createSQLXML() throws SQLException {
 		return con.createSQLXML();
 	}
 
 	@Override
 	public Statement createStatement() throws SQLException {
 		return new StatsStatement(this,con.createStatement());
 	}
 
 	@Override
 	public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
 		return new StatsStatement(this,con.createStatement(resultSetType, resultSetConcurrency));
 	}
 
 	@Override
 	public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)throws SQLException {
 		return new StatsStatement(this,con.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
 	}
 
 	@Override
 	public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
 		return con.createStruct(typeName, attributes);
 	}
 
 	@Override
 	public boolean getAutoCommit() throws SQLException {
 		return con.getAutoCommit();
 	}
 
 	@Override
 	public String getCatalog() throws SQLException {
 		return con.getCatalog();
 	}
 
 	@Override
 	public Properties getClientInfo() throws SQLException {
 		return con.getClientInfo();
 	}
 
 	@Override
 	public String getClientInfo(String name) throws SQLException {
 		return con.getClientInfo(name);
 	}
 
 	@Override
 	public int getHoldability() throws SQLException {
 		return con.getHoldability();
 	}
 
 	@Override
 	public DatabaseMetaData getMetaData() throws SQLException {
 		return con.getMetaData();
 	}
 
 	@Override
 	public int getTransactionIsolation() throws SQLException {
 		return con.getTransactionIsolation();
 	}
 
 	@Override
 	public Map<String, Class<?>> getTypeMap() throws SQLException {
 		return con.getTypeMap();
 	}
 
 	@Override
 	public SQLWarning getWarnings() throws SQLException {
 		return con.getWarnings();
 	}
 
 	@Override
 	public boolean isClosed() throws SQLException {
 		return con.isClosed();
 	}
 
 	@Override
 	public boolean isReadOnly() throws SQLException {
 		return con.isReadOnly();
 	}
 
 	@Override
 	public boolean isValid(int timeout) throws SQLException {
 		return con.isValid(timeout);
 	}
 
 	@Override
 	public String nativeSQL(String sql) throws SQLException {
 		return con.nativeSQL(sql);
 	}
 
 	@Override
 	public CallableStatement prepareCall(String sql) throws SQLException {
 		return new StatsCallableStatement(this,con.prepareCall(sql),sql);
 	}
 
 	@Override
 	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)throws SQLException {
 		return new StatsCallableStatement(this,con.prepareCall(sql, resultSetType, resultSetConcurrency),sql);
 	}
 
 	@Override
 	public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
 		return new StatsCallableStatement(this,con.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability),sql);
 	}
 
 	@Override
 	public PreparedStatement prepareStatement(String sql) throws SQLException {
 		return new StatsPreparedStatement(this,con.prepareStatement(sql),sql);
 	}
 
 	@Override
 	public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)throws SQLException {
 		return new StatsPreparedStatement(this,con.prepareStatement(sql, autoGeneratedKeys),sql);
 	}
 
 	@Override
 	public PreparedStatement prepareStatement(String sql, int[] columnIndexes)throws SQLException {
 		return new StatsPreparedStatement(this,con.prepareStatement(sql, columnIndexes),sql);
 	}
 
 	@Override
 	public PreparedStatement prepareStatement(String sql, String[] columnNames)throws SQLException {
 		return new StatsPreparedStatement(this,con.prepareStatement(sql, columnNames),sql);
 	}
 
 	@Override
 	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)throws SQLException {
 		return new StatsPreparedStatement(this,con.prepareStatement(sql, resultSetType, resultSetConcurrency),sql);
 	}
 
 	@Override
 	public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
 		return new StatsPreparedStatement(this,con.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability),sql);
 	}
 
 	@Override
 	public void releaseSavepoint(Savepoint savepoint) throws SQLException {
 		con.releaseSavepoint(savepoint);
 	}
 
 	@Override
 	public void rollback() throws SQLException {
 		con.rollback();
 	}
 
 	@Override
 	public void rollback(Savepoint savepoint) throws SQLException {
 		con.rollback(savepoint);
 	}
 
 	@Override
 	public void setAutoCommit(boolean autoCommit) throws SQLException {
 		con.setAutoCommit(autoCommit);
 	}
 
 	@Override
 	public void setCatalog(String catalog) throws SQLException {
 		con.setCatalog(catalog);
 	}
 
 	@Override
 	public void setClientInfo(Properties properties) throws SQLClientInfoException {
 		con.setClientInfo(properties);
 	}
 
 	@Override
 	public void setClientInfo(String name, String value)throws SQLClientInfoException {
 		con.setClientInfo(name, value);
 	}
 
 	@Override
 	public void setHoldability(int holdability) throws SQLException {
 		con.setHoldability(holdability);
 	}
 
 	@Override
 	public void setReadOnly(boolean readOnly) throws SQLException {
 		con.setReadOnly(readOnly);
 	}
 
 	@Override
 	public Savepoint setSavepoint() throws SQLException {
 		return con.setSavepoint();
 	}
 
 	@Override
 	public Savepoint setSavepoint(String name) throws SQLException {
 		return con.setSavepoint(name);
 	}
 
 	@Override
 	public void setTransactionIsolation(int level) throws SQLException {
 		con.setTransactionIsolation(level);
 	}
 
 	@Override
 	public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
 		con.setTypeMap(map);
 	}
	@Override
	public void abort(Executor arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public int getNetworkTimeout() throws SQLException {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public String getSchema() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void setNetworkTimeout(Executor arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void setSchema(String arg0) throws SQLException {
		// TODO Auto-generated method stub
		
	}
 }

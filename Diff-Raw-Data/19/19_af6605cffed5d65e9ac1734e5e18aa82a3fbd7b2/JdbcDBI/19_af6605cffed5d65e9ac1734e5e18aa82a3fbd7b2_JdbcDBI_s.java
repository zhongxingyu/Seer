 package mavenp2versionmatch.db;
 
 import mavenp2versionmatch.main.VersionManifest;
 import mavenp2versionmatch.exception.DBIException;
 
 import java.io.File;
 import java.sql.*;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 public abstract class JdbcDBI implements DBI{
 	protected Connection conn;
 
 	protected Map<String, String> config;
 	protected static final String CONFIG_URL = "url";
 	protected static final String CONFIG_DBNAME = "dbName";
 	protected static final String CONFIG_TABLENAME = "tableName";
 	protected static final String CONFIG_USER = "user";
 	protected static final String CONFIG_PASSWORD = "password";
 
 	//TODO: standardize the dbName and tableName or load them from a config file
 	//TODO: the implementation of connection opening and closing connections between
 	//this class and the SQLiteDBI is inconsistent
 
 	public abstract void open() throws DBIException;
 
 	public void close() throws DBIException {
 		try{
 			if(!conn.isClosed()) {
 				conn.close();
 			}
 		}catch(SQLException e){
 			throw new DBIException(e);
 		}
 	}
 
 	public void addRecord(VersionManifest vm) throws DBIException{
 		try{
 			this.open();
 			this.addRecordFromMapQuery(vm.createMap());
 			this.close();
 		}catch(SQLException e){
 			throw new DBIException(e); // create a generic DBIException from this SQLException and throw it
 		}
 	}
 	public void updateRecord(VersionManifest match, VersionManifest update) throws DBIException{
 		try{
 			this.open();
 			this.updateRecordFromMapQuery(match.createMap(), update.createMap());
 			this.close();
 		}catch(SQLException e){
 			throw new DBIException(e); // create a generic DBIException from this SQLException and throw it
 		}
 	}
 	public void updateRecordFromMap(Map<String,String> match, Map<String,String> update) throws DBIException{
 		try{
 			this.open();
 			this.updateRecordFromMapQuery(match, update);
 			this.close();
 		}catch(SQLException e){
 			throw new DBIException(e); // create a generic DBIException from this SQLException and throw it
 		}
 	}
 	public List<VersionManifest> find(VersionManifest vm) throws DBIException{
 		List<VersionManifest> results;
 		try{
 			this.open();
 			results = this.findFromMapQuery(vm.createMap());
 			this.close();
 		}catch(SQLException e){
 			throw new DBIException(e); // create a generic DBIException from this SQLException and throw it
 		}
 		return results;
 	}
 	public List<VersionManifest> findFromMap(Map<String, String> map) throws DBIException{
 		List<VersionManifest> results;
 		try{
 			this.open();
 			results = this.findFromMapQuery(map);
 			this.close();
 		}catch(SQLException e){
 			throw new DBIException(e); // create a generic DBIException from this SQLException and throw it
 		}
 		return results;
 	}
 	public List<VersionManifest> findAll() throws DBIException{
 		List<VersionManifest> results;
 		try{
 			this.open();
 			results = this.findAllQuery();
 			this.close();
 		}catch(SQLException e){
 			throw new DBIException(e); // create a generic DBIException from this SQLException and throw it
 		}
 		return results;
 	}
 
 
 	private void addRecordFromMapQuery(Map<String, String> colMap) throws SQLException{
 		if(conn.isClosed())
 			throw new SQLException("Connection is closed, cannot add record");
 
 		String colString = "";
 		String valString = "";
 		Iterator<String> colIter = colMap.keySet().iterator();
 
 		//Dynamically create the column string for inserting and
 		//add the proper amount of IN variables for the query
 		while( colIter.hasNext()) {
 			colString += colIter.next();
 			valString += "?"; //IN variable for statement
 
 			if( colIter.hasNext()) {
 				colString += ", ";
 				valString += ", ";
 			}
 		}
 
 		String query = "INSERT INTO "+ config.get(CONFIG_TABLENAME) + "("+colString+")" + "VALUES ("+valString+");";
 
 		PreparedStatement stmt = this.conn.prepareStatement(query);
 
 		Iterator<String> valueIter = colMap.values().iterator();
 		//add the values to the prepared statement
 		for( int i = 1; i < colMap.size() + 1; i++ ) {
 			stmt.setString( i, valueIter.next());
 		}
 
 
 		stmt.executeUpdate();
 	}
 
 	/**
 	 * Updates all records (should only ever be one match however) matching the values 
 	 * found in matchMap with the values found in updateMap.
 	 * @param matchMap Map of the key-value pairs to match in the database.
 	 * @param updateMap Map of the key-value pairs to update in the matching record.
 	 * @throws SQLException
 	 */
 	private void updateRecordFromMapQuery(Map<String, String> matchMap, Map<String, String> updateMap) throws SQLException{
 		if(conn.isClosed())
 			throw new SQLException("Connection is closed, cannot add record");
 
 		String matchString = "";
 		String updateString = "";
 		Iterator<String> matchIter = matchMap.keySet().iterator();
 		Iterator<String> updateIter = updateMap.keySet().iterator();
 
 		//Dynamically create the column string for updating and
 		//add the proper amount of IN variables for the query
 		while( updateIter.hasNext()) {
 			updateString += updateIter.next() + "=?";
 			if( updateIter.hasNext()) {
 				updateString += ", ";
 			}
 		}
 		//System.out.println("update string: " + updateString);
 		//Dynamically create the column string for matching and
 		//add the proper amount of IN variables for the query
 		while( matchIter.hasNext()) {
 			matchString += matchIter.next() + "=?";
 			if( matchIter.hasNext()) {
 				matchString += " AND ";
 			}
 		}
 		//System.out.println("match string: " + matchString);
 
 		String query = "UPDATE " + config.get(CONFIG_TABLENAME) + " SET " + updateString + " WHERE " + matchString + ";";
 
 		PreparedStatement stmt = this.conn.prepareStatement(query);
 
 		Iterator<String> valueIter1 = updateMap.values().iterator();
 		Iterator<String> valueIter2 = matchMap.values().iterator();
 		//add the values to the prepared statement
 		for( int i = 1; i < updateMap.size() + 1; i++ ) {
 			stmt.setString( i, valueIter1.next());
 		}
 		for( int i = 1; i < matchMap.size() + 1; i++ ) {
 			stmt.setString( i + updateMap.size(), valueIter2.next());
 		}
 		System.out.println("Rows updated: " + stmt.executeUpdate());
 
 	}
 
 	private List<VersionManifest> findFromMapQuery(Map<String, String> map) throws SQLException {
 		if(conn.isClosed())
 			throw new SQLException("Connection is closed, cannot find record");
 
 		String where = "";
 		String col;
 		String[] values = new String[map.size()];
 
 		Iterator<String> colIter = map.keySet().iterator();
 
 		for( int i = 0; i < map.size(); i++ ) {
 			col = colIter.next();
 			values[i] = map.get(col);
 
 			where += col + " = ?";
 			if( i < map.size() - 1 )
 				where += " AND ";
 		}
 
 		String query = "SELECT * FROM " + config.get(CONFIG_TABLENAME) + " WHERE " + where;
 
 		PreparedStatement stmt = this.conn.prepareStatement(query);
 
 		for(int i = 0; i < values.length; i++) {
 			stmt.setString( i + 1, values[i]);
 		}
 
 		ResultSet rs = stmt.executeQuery();
 
 		List<VersionManifest> mpvList = VersionManifest.fromResultSet(rs);
 
 		rs.close();
 
 		return mpvList;
     }
 
 	private List<VersionManifest> findAllQuery()
 			throws SQLException {
 		if(conn.isClosed())
 			throw new SQLException("Connection is closed, cannot find records");
 		
 		String query = "SELECT * FROM " + config.get(CONFIG_TABLENAME);
         
         PreparedStatement stmt = this.conn.prepareStatement(query);
         
         ResultSet rs = stmt.executeQuery();
         
 		List<VersionManifest> mpvList = VersionManifest.fromResultSet(rs);
 		
 		rs.close();
 		
 		return mpvList;
 	}
 }

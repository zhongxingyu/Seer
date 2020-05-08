 package mavenp2versionmatch.db;
 
 import java.io.File;
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 public class SQLiteDBI {
 	private Connection conn;
 	//TODO: standardize the dbName and tableName or load them from a config file
 	private static final String dbName = "my.db";
 	private static final String tableName = "maven_p2";
 	
 	public SQLiteDBI () throws SQLException {
 		
 		//make sure driver is loaded
 		try {
 			Class.forName("org.sqlite.JDBC");
 		} catch (ClassNotFoundException e) {
 			throw new SQLException("SQLite Driver wasn't loaded");
 		}
 		//make sure DB exists
 		if(!new File(dbName).exists()) {
 			throw new SQLException("Database does not exist");
 		}
 	}
 	
 	public void openDB() throws SQLException {
 		conn = DriverManager.getConnection("jdbc:sqlite:" + dbName);
 	}
 	
 	public void closeDB() throws SQLException {
 		if(!conn.isClosed()) {
 			conn.close();
 		}
 	}
 	//TODO duplicate records are added
 	public void addRecord(Map<String, String> colMap) throws SQLException{
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
 		
 		String query = "INSERT INTO "+ tableName + "("+colString+")" + "VALUES ("+valString+");";
 		
 		PreparedStatement stmt = this.conn.prepareStatement(query);
 		
 		Iterator<String> valueIter = colMap.values().iterator();
 		//add the values to the prepared statement
 		for( int i = 1; i < colMap.size() + 1; i++ ) {
 			stmt.setString( i, valueIter.next());
 		}
 		
 			
 		stmt.executeUpdate();
 	}
 
     public List<MavenP2Version> find(Map<String, String> map) throws SQLException {
 		if(conn.isClosed())
 			throw new SQLException("Connection is closed, cannot find record");
         
         String where = "";
         String col;
         
         Iterator<String> colIter = map.keySet().iterator();
 
         for( int i = 0; i < map.size(); i++ ) {
         	col = colIter.next();
            where += col+" LIKE '%"+map.get(col)+"%'"; //quick fix
             if( i < map.size() - 1 )
                 where += " AND ";
         }
         
         String query = "SELECT * FROM " + tableName + " WHERE " + where;
         System.out.println(query);
         
         PreparedStatement stmt = this.conn.prepareStatement(query);
        //I can't get this to work for the life of me, the result set is always empty
        //but it needs to be figured out before it goes live due to SQLinjection
        /**
        colIter = map.keySet().iterator();
         
        for( int i = 1; i < (map.size() * 2) + 1; i++) {
            col = colIter.next();
			stmt.setString( i++, col);
            stmt.setString( i, map.get(col) );
        }**/
 
         
         ResultSet rs = stmt.executeQuery();
         
 		List<MavenP2Version> mpvList = MavenP2Version.convertFromResultSet(rs);
 		
 		rs.close();
 		
 		return mpvList;
 
     }
 }

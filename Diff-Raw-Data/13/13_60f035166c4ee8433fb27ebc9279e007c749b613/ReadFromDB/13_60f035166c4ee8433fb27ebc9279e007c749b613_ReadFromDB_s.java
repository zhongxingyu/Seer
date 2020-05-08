 package com.search.you;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 
 public class ReadFromDB {
 		
 	private Connection conn;
 	
 	static{
 		initDriver();
 	}
 	
 	
 	private static void initDriver() {
 		try {
 			Class.forName(DBPropertyReader.getInstance().getDBProperties().getProperty(IDBConfiguration.DBDRIVER)).newInstance();
 		} catch (Throwable e) {
            throw new DBException("Error while Database initialization",e);
 		}
 	   
 	}
 	
	private void initConnection(IDBConfiguration dbConfiguration){
 		 conn = dbConfiguration.initConnection();
 	}
 	
 	/**
 	 * Test method
 	 * @param query
 	 * @return
 	 */
 	
 	private ResultSet getResult(String query){
 		ResultSet rs = null;
 		try {
 			PreparedStatement statement = conn.prepareStatement(query);
 			rs = statement.executeQuery();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		return rs;
 	}
 	
 	public static void main(String args[]) throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException{
 		ReadFromDB rfdb = new ReadFromDB();
 		rfdb.initConnection(new MYSQLConfiguration());
 		ResultSet rs = rfdb.getResult("Select * from Message");
 		
 		System.out.println("------------------------------------------------------------------");
 		while(rs.next()){
 			int id = rs.getInt("id");
 			String message = rs.getString("message");
 			Timestamp ts = rs.getTimestamp("insertTime");
 			System.out.println("| "+id+"   |"+message+"          |       "+ts+"               |");
 		}
 		System.out.println("------------------------------------------------------------------");
 	}
 
 }

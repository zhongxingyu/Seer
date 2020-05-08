 package com.niccholaspage.nSQL.query;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class InsertQuery extends Query {
 	private boolean firstValue;
 	
 	public InsertQuery(Connection connection, String sql){
 		super(connection, sql);
 		
 		firstValue = true;
 	}
 	
 	public InsertQuery insert(String insert){
 		sql += insert + ", ";
 		
 		return this;
 	}
 	
 	public InsertQuery value(Object value){
 		sql = sql.substring(0, sql.length() - 2);
 		
 		if (firstValue){
			sql += ") VALUES (" + value + ")";
 			
 			firstValue = false;
 		}else {
			sql += ", " + value + ")";
 		}
 		
 		return this;
 	}
 	
 	public void execute(){
 		Statement statement;
 		try {
 			statement = connection.createStatement();
 			
 			statement.executeUpdate(sql);
 			
 			statement.close();
 		} catch (SQLException e){
 			e.printStackTrace();
 		}
 	}
 }

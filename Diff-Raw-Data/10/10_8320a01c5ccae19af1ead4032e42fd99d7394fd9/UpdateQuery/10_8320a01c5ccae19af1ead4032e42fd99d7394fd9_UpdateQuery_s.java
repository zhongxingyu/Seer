 package com.niccholaspage.nSQL.query;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class UpdateQuery extends Query {
 	private boolean comma;
 	
 	private boolean and;
 	
 	public UpdateQuery(Connection connection, String sql){
 		super(connection, sql);
 		
 		comma = false;
 		
 		and = false;
 	}
 	
 	public UpdateQuery set(String field, Object value){
 		if (comma){
 			sql += ",";
 		}
 		
		sql += " " + field + "=" + value;
 		
 		comma = true;
 		
 		return this;
 	}
 	
 	public UpdateQuery where(String key, Object value){
 		if (and){
 			sql += " AND";
 		}else {
 			sql += " WHERE";
 		}
 		
 		sql += " " + key + "=";
 		
 		StringBuilder builder = new StringBuilder(value + "");
 		
 		if (value instanceof String){
 			builder.insert(0, "'");
 			
 			builder.append("'");
 		}
 		
 		sql += builder.toString();
 		
 		and = true;
 		
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

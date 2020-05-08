 package org.oobium.persist.db.derby.embedded;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class UniqueColumnTrigger {
 
 	public static void checkUnique(String tableName, String columnName, Integer id) throws SQLException {
 		if(id != null) {
 			Connection connection = DriverManager.getConnection("jdbc:default:connection");
 			Statement s = connection.createStatement();
 			
			String sql = "SELECT 1 FROM " + tableName + " WHERE " + columnName + "=" + id;
 			
 			ResultSet rs = s.executeQuery(sql);
			if(rs.next()) {
				throw new SQLException("Duplicate value for " + tableName + "(" + columnName + "): " + id);
 			}
 		}
 	}
 	
 }

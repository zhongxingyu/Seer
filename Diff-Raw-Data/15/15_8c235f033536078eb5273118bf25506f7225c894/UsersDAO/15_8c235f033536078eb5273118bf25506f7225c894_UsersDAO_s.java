 package dao;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 
 
 
 
 
 import javax.sql.DataSource;
 
 import types.User;
 
 public class UsersDAO
 {
 	
 	public static String selectAllUsers (DataSource ds) throws SQLException{
 		Connection connection = null;
 
 		try {
 			connection = ds.getConnection();
			String sql = "SELECT * FROM user";
 			PreparedStatement stmt = connection.prepareStatement(sql);
 			ResultSet rs = stmt.executeQuery();
 			String result = null;
 			
 			while(rs.next()){
 				result = result+ " "+rs.getString(1);
 			}
 			
 			return result;
 			
 		} finally {
 			if (connection != null) {
 				connection.close();
 			}
 		}
 	}
 	
 }

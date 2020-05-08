 /**
  * 
  */
 package org.fubme.helper;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.fubme.models.User;
 import org.fubme.persistency.DBConnection;
 import org.fubme.servlets.Home;
 
 /**
  * @author riccardo
  * 
  */
 public abstract class Credentials {
 	public static final User validateUserCredentials(String id, String pswd) {
 		PreparedStatement stmt = null;
 		Connection connection = null;
 		Logger.getLogger(Home.class.getName()).log(
 				Level.SEVERE,
 				"validating credentials for user: username : " + id
 						+ " password : " + pswd);
 		String sql = "Select * from fuser where id = ?";
 		ResultSet resultset = null;
 		try {
 			connection = DBConnection.getConnection();
 			stmt = connection.prepareStatement(sql);
			stmt.setString(1, id);
			resultset = stmt.executeQuery();
 			Logger.getLogger(Credentials.class.getName())
 					.log(Level.SEVERE, sql);
 			resultset.next();
 			String password = resultset.getString("pswd");
 			if (pswd.equals(password))
 				return new User(id, pswd);
 		} catch (SQLException ex) {
 
 			Logger.getLogger(Credentials.class.getName()).log(Level.SEVERE,
 					null, ex);
 		} finally {
 			if (stmt != null)
 				try {
 					stmt.close();
 				} catch (SQLException e2) {
 				}
 			if (resultset != null)
 				try {
 					resultset.close();
 				} catch (SQLException e1) {
 				}
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (SQLException e) {
 				}
 
 			}
 		}
 		return null;
 	}
 }

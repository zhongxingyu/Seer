 package quizweb.accountmanagement;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import quizweb.User;
 import quizweb.database.*;
 
 
 public class AccountManager {
	private static final String DBTable = "user";
 	public AccountManager(){
 	}
 	
 	public void createNewAccount(String name, String password, int type){
 		Encryption e = new Encryption();
 		String hashedPassword = e.generateHashedPassword(password);
 		new User(name, hashedPassword, name, type);
 	}
 	
 	public boolean accountMatch(String name, String password) {
 		if(User.getUserByUsername(name) == null)
 			return false;
 		Encryption e = new Encryption();
 		String hashedPassword = e.generateHashedPassword(password);
 		try {
			String statement = new String("SELECT password FROM " + DBTable +" WHERE name = ?");
 			PreparedStatement stmt = DBConnection.con.prepareStatement(statement);
 			stmt.setString(1, name);
 			ResultSet rs = DBConnection.DBQuery(stmt);
 			rs.next();
 			String storedPassword = rs.getString("password");
 			if(hashedPassword.equals(storedPassword))
 				return true; 
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 		return false;
 	}
 }

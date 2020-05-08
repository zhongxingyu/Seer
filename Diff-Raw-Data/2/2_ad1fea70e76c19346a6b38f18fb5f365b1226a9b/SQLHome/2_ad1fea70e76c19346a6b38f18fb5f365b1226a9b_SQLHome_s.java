 package vClass;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Calendar;
 
 import dExceptions.UserDoesNotExistException;
 
 
 public class SQLHome extends Home {
 	
 	@Override
 	public User getUser(User user){
 		try {
 		Connection conn = new DBConnector().getConnection();
 		PreparedStatement prepareStatement = conn.prepareStatement("SELECT * FROM USER WHERE USERNAME = ?");
 		prepareStatement.setString(1,user.getUsername());
 		ResultSet result = prepareStatement.executeQuery();
 
 		if (result.next()){
 			User x = new User();
 			x.setName(result.getString("NAME"));
 			x.setEmail(result.getString("EMAIL"));
 			x.setPassword(result.getString("PASSWORD"));
 			x.setUsername(result.getString("USERNAME"));
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(result.getDate("BIRTH"));
 			x.setBirth(cal);
 			x.setLastName(result.getString("LASTNAME"));
 			return x;
 		   } else{
			   throw new UserDoesNotExistException("User do not exist!");
 		   }
 		  }  catch (SQLException e){
 			  throw new RuntimeException(e);
 	      }
 		}
 	@Override
 	public void saveUser(User user) throws Exception {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void changePassword(String userName, String oldPassword,
 			String newPassword) throws Exception {
 		// TODO Auto-generated method stub
 		
 	}
 
 }

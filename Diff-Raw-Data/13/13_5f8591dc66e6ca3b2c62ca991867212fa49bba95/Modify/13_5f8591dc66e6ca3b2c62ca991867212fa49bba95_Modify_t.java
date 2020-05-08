 
 //import java.sql.Connection;
 //import java.sql.DriverManager;
 //import java.sql.PreparedStatement;
 //import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * Updates columns of existing rows with new values.
  * 
  * @author User
  *
  */
 public class Modify extends DataAccess {
 	
 	private static final long serialVersionUID = 1351738512414009001L;
 	
 	/**
 	 * Modifies entries in a table.
 	 * 
 	 * @param dbName the url with the database name
 	 * @param user the username
 	 * @param pwd the password
 	 */
 	public Modify(String dbName, String user, String pwd) {
 		super(dbName, user, pwd);
 	}
 	
 	public void modifyWhere(String table, String where, String column, String value) {
 		openConnection();
 		try{
 			String myStatement = "UPDATE "+table+" SET "+column+"="+value+" WHERE "+where+";";
 			statement = con.prepareStatement(myStatement);
			statement.executeUpdate();
 		}catch(SQLException s){
 			s.printStackTrace();
 		}
 		closeConnection();
 	}
 	
 }

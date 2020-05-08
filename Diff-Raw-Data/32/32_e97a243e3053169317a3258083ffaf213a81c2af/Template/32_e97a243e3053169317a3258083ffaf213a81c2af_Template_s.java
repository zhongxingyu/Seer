 import java.sql.SQLException;
 
 
 public class Template {
 
 	/**
 	 * @param args
 	 * @throws SQLException 
 	 */
 	public static void main(String[] args) throws SQLException {
 		
		LoginController login = new LoginController();
 		login.showAccount(20);
 	}
 
 }

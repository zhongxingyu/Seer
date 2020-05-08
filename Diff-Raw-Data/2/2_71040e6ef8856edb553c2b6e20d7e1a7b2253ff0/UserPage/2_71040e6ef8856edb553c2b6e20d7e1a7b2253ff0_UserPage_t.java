 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 
 public class UserPage {
 
 	/**
 	 * @param args
 	 */
 	
 	static String user;
 	static String createString;
 		
 	public UserPage(String email){
		// TODO:
		// createString shoudl probably be email.
 		createString = "select name from users where email = '"+createString+"';";
 		try
 		{
 
 		Main.m_con = DriverManager.getConnection(Main.m_url, Main.m_userName, Main.m_password);
 
 		Main.stmt = Main.m_con.createStatement();
 		ResultSet rs = Main.stmt.executeQuery(createString);
 				
 		user = rs.getString("name");
 				
 		Main.stmt.close();
 		Main.m_con.close();
 
 		} catch(SQLException ex) {
 
 		System.err.println("SQLException: " +
 		ex.getMessage());
 
 		}
 	}
 	
 	static boolean loggedOn = true;
 	public void startUp(){
 		System.out.println("Welcome "+user+". Here are your options for today.");
 		
 	}
 	
 }

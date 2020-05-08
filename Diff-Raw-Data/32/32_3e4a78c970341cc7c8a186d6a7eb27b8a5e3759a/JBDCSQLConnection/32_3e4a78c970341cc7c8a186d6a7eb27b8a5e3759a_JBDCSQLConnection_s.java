 package pizzawatch.sql.connection;
 
import java.sql.DriverManager;
 import java.sql.Connection;
 import java.sql.SQLException;
 
 public class JBDCSQLConnection {
 
 	private final static String USER_NAME = "ora_i2c8"; /*ora_<cs_id>*/
 	private final static String USER_PASSWORD = "a30758114"; /*a<Student Number> this like how we connected to SQL_Plus in lab*/ 
 	private final static String CONNECTION_URL = "jdbc:oracle:thin:@dbhost.ugrad.cs.ubc.ca:1522:ug"; /*port 1521 has problems*/
 	
 	private Connection connection;
	
 	public void setOracleConnection()
 	{
 		int retry = 5;
		
 		System.out.println("--------------------- Starting Oracle connection ---------------------");
 		try
 		{
 			Class.forName("oracle.jdbc.driver.OracleDriver");
 		}catch(ClassNotFoundException e)
 		{
 			e.printStackTrace();
 			/* will continue on even if class is not found*/
 		}
 		System.out.println("Oracle driver is found");
 		Connection con = makeConnection();
 		while(con==null && retry > 0)
 		{
 			con = makeConnection();
 			retry--;
 		}
		
 		this.connection = con;
 	}
	
 	public Connection getConnection()
 	{
 		return this.connection;
 	}
	
 	private Connection makeConnection()
 	{
 		try {
			Connection connection = DriverManager.getConnection(CONNECTION_URL, USER_NAME, USER_PASSWORD);
 			System.out.println("Successfully Connected");
			return connection;
 		} catch (SQLException e) {
 			System.out.println("Connection failed \n");
 			e.printStackTrace();
 		}
		return null;
 	}
	
	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		JBDCSQLConnection sqlcon = new JBDCSQLConnection();
 		sqlcon.setOracleConnection();
 	}
 
 }

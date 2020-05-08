 import java.sql.*;
 
 public class DBAL
 {
 	// Singleton instance
 	private static DBAL instance = null;
 	public static DBAL getInstance()
 	{
 		if (instance == null)
 		{
 			instance = new DBAL();	
 		}
 		return instance;
 	}
 	
 	// Instance Variables
 	private Connection conn;
 	private Statement stmt;
 	private boolean isConnected;
 	
 	// Constructor
 	protected DBAL() 
 	{
 		// Don't call this from outside, use getInstance()
		try 
		{
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:8889/cse3330a?user=root&password=root");
			stmt = conn.createStatement();
			isConnected = true;
		} 
		catch (Exception e) 
		{
 			System.out.println("Unable to connect to MySQL server");
 			isConnected = false;
		}
 	}
 	
 	// Terminate Connection
 	public void close()
 	{
 		stmt.close();
		conn.close();
 	}
 	
 	// Get a ResultSet for a query
 	public ResultSet query(String query)
 	{
 		 return stmt.executeQuery(query);
 	}
 	
 	// SHA256 easy hashing method, good for passwords
 	public static String SHA256(String text)
 	{
 		MessageDigest sha = MessageDigest.getInstance("SHA-256");
 		sha.update(text.getBytes("UTF-8"));
 		byte[] digest = sha.digest();
 		return new String(Hex.encode(digest));
 	}
 }

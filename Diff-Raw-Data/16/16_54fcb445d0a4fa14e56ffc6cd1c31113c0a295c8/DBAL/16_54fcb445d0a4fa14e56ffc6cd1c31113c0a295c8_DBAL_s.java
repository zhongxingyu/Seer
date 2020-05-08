 import java.sql.*;
 import java.io.PrintWriter;
 
 public class DBAL
 {
 	// Singleton instance
 	private static DBAL instance = null;
 	public static DBAL getInstance(PrintWriter out)
 	{
 		if (instance == null)
 		{
 			instance = new DBAL(out);	
 		}
 		return instance;
 	}
 	
 	// Instance Variables
 	private Connection conn;
 	private Statement stmt;
 	private PrintWriter out;
 	private boolean isConnected;
 	
 	// Constructor
 	protected DBAL(PrintWriter pw) 
 	{
 		// Don't call this from outside, use getInstance()
 		out = pw;
 		try 
 		{
 			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection("jdbc:mysql://localhost:8889/creepers?user=root&password=root");
 			stmt = conn.createStatement();
 			isConnected = true;
 		} 
 		catch (Exception e) 
 		{
 			out.println("Unable to connect to MySQL server.");
 			out.println(e);
 			isConnected = false;
 		}
 	}
 	
 	// Terminate Connection
 	public void close()
 	{
 		if (!isConnected) return;
 		try 
 		{
 			stmt.close();
 			conn.close();
 		} 
 		catch (Exception e) 
 		{
 			out.println("Error with closing connection to MySQL server.");
 			out.println(e);
 		}
 	}
 	
 	// Get a ResultSet for a query
 	public ResultSet query(String query)
 	{
 		if (!isConnected) return null;
 		try 
 		{
 			return stmt.executeQuery(query);
 		} 
 		catch (Exception e) 
 		{
 			out.println("Unable to run query.");
 			out.println(e);
 			out.println("Query attempted:");
 			out.println(query);
 		}
 		return null;
 	}
 	
 	public PreparedStatement preparedStatement(String query)
 	{
 		if (!isConnected) return null;
 		try
 		{
 			return conn.prepareStatement(query);
 		}
 		catch (Exception e) 
 		{
 			out.println("Unable to prepare statement for query.");
 			out.println(e);
 			out.println("Query attempted:");
 			out.println(query);
 		}
 		return null;
 	}
 	
 	// SHA256 easy hashing method, good for passwords
 	/*public static String SHA256(String text)
 	{
 		MessageDigest sha = MessageDigest.getInstance("SHA-256");
 		sha.update(text.getBytes("UTF-8"));
 		byte[] digest = sha.digest();
 		return new String(Hex.encode(digest));
 	}*/
}

 package connect;
 
 import java.sql.*;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 public class connect
 {
	private String ccid;
	private String pass;
 	
 	public connect ()
 	{
 		
 	}
 	
	public setLogin (String ccid, String pass)
 	{
		this.ccid = ccid;
		this.pass = pass;
 	}
 	
 	public static Connection dbConnect ()
 	{
 		Connection conn = null;
 		
 		String driverName = "oracle.jdbc.driver.OracleDriver";
 		String dbstring = "jdbc:oracle:thin:@gwynne.cs.ualberta.ca:1521:CRS";
 		try
 		{
 			//load and register the driver
 			Class drvClass = Class.forName (driverName); 
 			DriverManager.registerDriver ((Driver) drvClass.newInstance ());
 		}
 		catch (Exception ex)
 		{
 			System.out.println ("<hr>" + ex.getMessage () + "<hr>");
 		}
 
 		try
 		{
 			//establish the connection 
 			conn = DriverManager.getConnection (dbstring, ccid, pass);
 			conn.setAutoCommit (false);
 		}
 		catch (Exception ex)
 		{
 			System.out.println ("<hr>" + ex.getMessage () + "<hr>");
 		}
 		
 		return conn;
 	}
 	
 	public static String getDateStringFromDateString (String date)
 	{
 		SimpleDateFormat dateFormat = new SimpleDateFormat ("yyyy-MM-dd");
 		Date convertedDate = new Date ();
 		try
 		{
 			convertedDate = dateFormat.parse (date);
 		}
 		catch (ParseException e)
 		{
 			// TODO Auto-generated catch block
 			System.out.println ("<hr>" + e.getMessage () + "<hr>");
 		}   
 	    SimpleDateFormat finalFormat = new SimpleDateFormat ("dd-MMM-yy");
 	    
 	    return finalFormat.format (convertedDate);
 	}
 }

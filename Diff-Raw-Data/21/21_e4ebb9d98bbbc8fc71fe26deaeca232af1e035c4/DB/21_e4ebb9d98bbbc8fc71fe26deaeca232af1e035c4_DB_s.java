 package ru.sabstest;
 
 import java.io.Closeable;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 
 public class DB implements Closeable{
 	private String server;
 	private String db;
 	private String user;
 	private String pwd;
 	private Connection con;
 	public Statement st;
 
 	DB(String server, String db, String user, String pwd)
 	{
 		this.server = server;
 		this.db = db;
 		this.user = user;
 		this.pwd = pwd;
 	}
 
 	public void connect() throws Exception 
 	{
 		try {
 			if(con != null)	if (con.isValid(10)) throw new SQLException("Connection have been already opened.");
 
 			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
 
 			String URL = "jdbc:sqlserver://" + server + ";databaseName=" + db;
 
 			con = DriverManager.getConnection(URL, user, pwd);
 
 
 			if (con==null) System.exit(0);
 			Log.msg("  " + server + "  " + db + " .");
 			st = con.createStatement();
 		} catch(Exception e) {
 			e.printStackTrace();
 			Log.msg(e);
 		}
 	}
 
 	public boolean isConnected() throws SQLException
 	{		
 		if(con != null)	return con.isValid(10);
 		else return false;	
 
 	}
 
 	@Override
 	public void close()
 	{
 		try {
 			if(con != null)
 			{
 				if(con.isValid(10))
 
 				{
 					st.close();
 					con.close();
 					Log.msg("  " + server + "  " + db + " .");
 				}
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 			Log.msg(e);
 		}
 	}
 
 
 	public static String selectFirstValueSabsDb(String s)
 	{
 		String value = "";
 		try {
 			DB db = new DB(Settings.server, Settings.db, Settings.user, Settings.pwd);
 			db.connect();
 
 			ResultSet rs = db.st.executeQuery(s);
			rs.next();
			value = rs.getString(1);
 
 			db.close();
 		} catch(Exception e) {
 			e.printStackTrace();
 			Log.msg(e);
 		}
 		return value;
 	}
 }

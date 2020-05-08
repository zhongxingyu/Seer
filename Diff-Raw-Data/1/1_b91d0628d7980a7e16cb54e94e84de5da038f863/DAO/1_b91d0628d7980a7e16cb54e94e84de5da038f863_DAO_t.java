 package jmbs.server;
 
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 /**
  * Contains all the possible ways to set a request to the db.
  * 
  */
 public abstract class DAO implements Serializable {
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1746724303428703866L;
 	private Connection con = null;
 	PreparedStatement stmt = null;
 
 	protected ResultSet send(String request)
 	{
 		
 		ResultSet result;
 		try {
 			Statement state = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,ResultSet.CONCUR_READ_ONLY);
 			result = state.executeQuery(request);
 			result.absolute(1);
 		} catch (SQLException e){
 			if(e.getErrorCode() != 0)
 			System.err.println("Unable set the connection to the database!/n");
 			result = null;
 		}
 		return result;
 	}	
 	
 	protected void set(String request)
 	{
 		try {
 			stmt = con.prepareStatement(request);
 		} catch (SQLException e) {
 			System.err.println("Unable to execute querry: "+ request);
 		
 		}
 	}
 	
 	protected void setString (int index,String s)
 	{
 		try {
 			stmt.setString(index,s);
 		} catch (SQLException e) {
 			System.err.println("Unable to set string: "+ s);
 		}
 	}
 	
 	protected void setInt (int index,int i)
 	{
 		try {
 			stmt.setInt(index, i);
 		} catch (SQLException e) {
 			System.err.println("Unable to set int: "+ i);
 		}
 	}
 	
 	protected void setDate(int index,Date dt)
 	{
 		try {
 			stmt.setDate(index,dt);
 		} catch (SQLException e) {
 			System.err.println("Unable to set date: "+ dt);
 		}
 	}
 	
 	protected ResultSet executeQuery() 
 	{
 		ResultSet res = null;
 		try {
 			res = stmt.executeQuery();
			res.absolute(1);
 			stmt.close();
 			stmt = null;
 		} catch (SQLException e) {
 			System.err.println("Unable to execute querry");
 		}
 		
 		return res;
 	}
 	
 	public DAO(Connection c)
 	{
 		con = c;
 	}
 	
 	public Connection getConnection()
 	{
 		return con;
 	}
 }

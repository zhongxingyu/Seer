 package fr.corba.server;
 import java.sql.*;
 
 public class DataBase {

     public DataBase() {
 	    try {
 			Class.forName("org.sqlite.JDBC");
 		    conn = DriverManager.getConnection("jdbc:sqlite:corba.db");
 		    createDataBase();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
	Connection conn;
 	private boolean createDataBase()
 	{
 	    try {
 		    Statement stat = conn.createStatement();
 		    stat.executeUpdate("create table if not exists  users (nickname, password);");
 		    
 		    Statement verifstat = conn.createStatement();
 		    ResultSet rs = verifstat.executeQuery("select * from users where nickname = 'yoms';");
 		    if(!rs.next())
 		    {
 			    PreparedStatement prep = conn.prepareStatement(
 			      "insert into users values (?, ?);");
 		
 			    prep.setString(1, "yoms");
 			    prep.setString(2, "superyoms");
 			    prep.addBatch();
 			    prep.setString(1, "alex");
 			    prep.setString(2, "superalex");
 			    prep.addBatch();
 			    conn.setAutoCommit(false);
 			    prep.executeBatch();
 				conn.setAutoCommit(true);
 		    }
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	    
 	    return true;
 	}
 	public boolean verifyUser(String nick, String password)
 	{
 	    try {
 		    Statement stat = conn.createStatement();
 		    ResultSet rs = stat.executeQuery("select * from users where nickname = '"+nick+"' and password = '"+password+"';");
		    if(rs.next())
		    	return true;
 		} catch (Exception e) {
 			e.printStackTrace();
 		} 
 	    return false;
 	}
 }

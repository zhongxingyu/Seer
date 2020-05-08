 package fr.corba.server;
 import java.sql.*;
 
 public class DataBase {
	Connection conn;
     public DataBase() {
 	    try {
 			Class.forName("org.sqlite.JDBC");
 		    conn = DriverManager.getConnection("jdbc:sqlite:corba.db");
 		    createDataBase();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 	private boolean createDataBase()
 	{
 	    try {
 		    Statement stat = conn.createStatement();
 		    stat.executeUpdate("create table if not exists  users (nickname, password);");
 		    
 		    Statement verifstat = conn.createStatement();
 		    ResultSet rs = verifstat.executeQuery("select * from users where nickname = 'yoms';");
 		    if(!rs.next())
 		    {
		    	System.out.println("Save base users");
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
		    verifstat.close();
		    stat.close();
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
		    boolean ret = rs.next();
		    stat.close();
		    return ret;
 		} catch (Exception e) {
			System.out.println();
 			e.printStackTrace();
 		} 
 	    return false;
 	}
 }

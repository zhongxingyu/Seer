 package util;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 
 public class ConnectDB {
 	Connection dbConnect;
 	
 	public Connection getDdConnection(){
 		return dbConnect;
 	}
 	
 	public ConnectDB(){
 		try{
 			Class.forName("org.sqlite.JDBC");
			dbConnect = DriverManager.getConnection("jdbc:sqlite:/db/sensor_info.sqlite3");
 			System.out.println("connected");
 		}  catch (Exception e) {
 			System.out.println("Did not connect");
             e.printStackTrace();
         }
 	}
 }

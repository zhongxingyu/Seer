 package com.nexus;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 import com.nexus.config.ConfigObject;
 
 public class MySQLHelper {
 
 	public static String GetDatabaseURI(){
 		ConfigObject config = NexusServer.Instance.Config.GetConfig();
 		return "jdbc:mysql://" + config.Database.Host + "/" + config.Database.Database + "?user=" + config.Database.Username + "&password=" + config.Database.Password;
 	}
 	
 	public static Connection GetConnection(){
 		try {
 			return DriverManager.getConnection(GetDatabaseURI());
 		} catch (SQLException e) {
 			return null;
 		}
 	}
 
 	public static void Log(String Message, LogLevel level){
 		NexusServer.Instance.log.write(Message, "MySQLHelper", level);
 	}
 	
 	public static void Startup(){
 		try{
 			DriverManager.getConnection(GetDatabaseURI());
 		}catch(Exception e){
 			Log("Can't connect to MySQL Server, change settings!", LogLevel.CRITICAL);
			System.exit(0);
 		}
 	}
 }

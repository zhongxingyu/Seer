 package com.nexus;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.logging.Logger;
 
 import com.nexus.config.ConfigObject;
 import com.nexus.logging.NexusLogger;
 
 public class MySQLHelper {
 
 	private static Logger Log = Logger.getLogger("MySQLHelper");
 	
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
 	
 	public static void Startup(){
 		Log.setParent(NexusLogger.getLogger());
 		try{
 			DriverManager.getConnection(GetDatabaseURI());
 		}catch(Exception e){
			Log.severe("Can't connect to MySQL Server, change settings!");
			ShutdownManager.ShutdownServer();
 		}
 	}
 }

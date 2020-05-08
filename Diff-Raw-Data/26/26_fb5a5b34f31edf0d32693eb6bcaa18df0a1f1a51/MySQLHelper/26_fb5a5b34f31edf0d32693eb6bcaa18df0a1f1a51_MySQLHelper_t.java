 package com.nexus;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			try{
				Log.severe("Can't connect to MySQL Server, try using other data? (y/n) ");
				if(reader.readLine().equalsIgnoreCase("y")){
					Log.info("Enter MySQL host:");
					NexusServer.Instance.Config.Config.Database.Host = reader.readLine();
					Log.info("Enter MySQL username:");
					NexusServer.Instance.Config.Config.Database.Username = reader.readLine();
					Log.info("Enter MySQL password:");
					NexusServer.Instance.Config.Config.Database.Password = reader.readLine();
					Log.info("Enter MySQL database:");
					NexusServer.Instance.Config.Config.Database.Database = reader.readLine();
					NexusServer.Instance.Config.SaveConfig();
					Startup();
				}else{
					ShutdownManager.ShutdownServer();
				}
			}catch(IOException e1){
				ShutdownManager.ShutdownServer("Crash");
			}
 		}
 	}
 }

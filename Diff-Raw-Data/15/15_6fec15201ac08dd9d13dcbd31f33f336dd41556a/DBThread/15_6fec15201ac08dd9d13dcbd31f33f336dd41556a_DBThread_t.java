 package edu.usu.cosl.util;
 
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Connection;
 import java.sql.Timestamp;
 import java.util.Properties;
 import java.util.Date;
 import java.io.BufferedReader;
 import java.io.FileReader;
 
 import org.apache.commons.dbcp.ConnectionFactory; 
 import org.apache.commons.pool.impl.GenericObjectPool;
 import org.apache.commons.dbcp.PoolableConnectionFactory;
 import org.apache.commons.dbcp.PoolingDriver;
 import org.apache.commons.dbcp.DriverManagerConnectionFactory;
 import org.apache.commons.pool.impl.StackKeyedObjectPoolFactory;
 
 public class DBThread extends Thread {
 
 	protected boolean bStop = false;
 	protected static Logger log = Logger.getLogger();
 	
 	private static String sUser = "root";
 	private static String sPassword = "";
 
	private static String sDatabase;
 	private static String sDBConnectionPrefix = "jdbc:mysql://localhost/";
	private static String sDBConnection;
 
 	private static String sPool = "aggregator";
 	private static String sJDBCConnection = "jdbc:apache:commons:dbcp:" + sPool;
 	
 	private static boolean bDriverLoaded = false;
 	private static String sRailsEnv = "development";
 
 	public static void loadDBDriver() throws ClassNotFoundException
 	{
 		// initialize the connection pool
 //		Class.forName("org.postgresql.Driver");
 		Class.forName("com.mysql.jdbc.Driver");
 		Class.forName("org.apache.commons.dbcp.PoolingDriver");
 		
 		GenericObjectPool connectionPool = new GenericObjectPool(null);
 		ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(sDBConnection, sUser, sPassword);
 		StackKeyedObjectPoolFactory pstPoolFactory = new StackKeyedObjectPoolFactory(20);
 		final String sValidationQuery = null;
 		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory,connectionPool,pstPoolFactory,sValidationQuery,false,true);
 		PoolingDriver driver = new PoolingDriver();
 		driver.registerPool(sPool,connectionPool);
 		bDriverLoaded = true;
 	}
 	public static Connection getConnection() throws ClassNotFoundException, SQLException
 	{
 		if (!bDriverLoaded) loadDBDriver();
 		return DriverManager.getConnection(sJDBCConnection);
 	}
 	public static Timestamp currentTime()
 	{
 		return new Timestamp(new Date().getTime());
 	}
 	public static void getDBOptions(Properties properties)
 	{
         Logger.getOptions(properties);
         
         String sValue = properties.getProperty("db_yml");
         String sDBConfigFile = (sValue == null) ? "config/database.yml" : sValue;
         sDBConfigFile = System.getProperty("RAILS_DB_CONFIG", sDBConfigFile);
 
         sValue = properties.getProperty("rails_env");
         if (sValue != null) sRailsEnv = sValue;
         sRailsEnv = System.getProperty("RAILS_ENV", sRailsEnv);
         
 		try
 		{
 			BufferedReader reader = new BufferedReader(new FileReader(sDBConfigFile));
 			String sLine = reader.readLine();
 			while(sLine != null)
 			{
 				sLine = sLine.trim();
 				if (sLine.startsWith(sRailsEnv)) 
 				{
 					sLine = reader.readLine();
 					while(sLine != null && sLine.startsWith(" "))
 					{
 						sLine = sLine.trim();
						if (sLine.startsWith("database:")) {
 							sDatabase = sLine.substring(9).trim();
							sDBConnection = sDBConnectionPrefix + sDatabase;
						}
 						else if (sLine.startsWith("username:")) 
 							sUser = sLine.substring(10).trim();
 						else if (sLine.startsWith("password:")) 
 							sPassword = sLine.substring(9).trim();
 						sLine = reader.readLine();
 					}
 					break;
 				}
 				sLine = reader.readLine();
 			}
			if (sDatabase == null) {
				throw new Exception("A database was not specified");
			}
 		} 
 		catch (Exception e)
 		{
 			Logger.error("Unable to load database configuration file", e);
 		}
 	}
 }

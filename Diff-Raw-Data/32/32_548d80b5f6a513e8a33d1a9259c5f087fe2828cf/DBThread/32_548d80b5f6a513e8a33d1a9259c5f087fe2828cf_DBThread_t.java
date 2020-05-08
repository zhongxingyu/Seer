 package edu.usu.cosl.util;
 
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Connection;
 import java.sql.Timestamp;
 
import java.util.Enumeration;
 import java.util.Properties;
 import java.util.Date;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 
 import org.apache.commons.dbcp.ConnectionFactory; 
 import org.apache.commons.pool.impl.GenericObjectPool;
 import org.apache.commons.dbcp.PoolableConnectionFactory;
 import org.apache.commons.dbcp.PoolingDriver;
 import org.apache.commons.dbcp.DriverManagerConnectionFactory;
 import org.apache.commons.pool.impl.StackKeyedObjectPoolFactory;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.PropertyConfigurator;
 
 public class DBThread extends Daemon {
 
 	static private boolean bAlreadyLoadedOptions = false;
 	
 	private static String sUser = "root";
 	private static String sPassword = "";
 
 	private static String sDatabase;
 	private static String sDBConnectionPrefix = "jdbc:mysql://localhost/";
 	private static String sDBConnection;
 
 	private static String sPool = "aggregator";
 	private static String sJDBCConnection = "jdbc:apache:commons:dbcp:" + sPool;
 	
 	private static boolean bDriverLoaded = false;
 	protected static String sRailsEnv = "development";
 	
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
 	public static Properties loadPropertyFile(String sFile) throws IOException
 	{
 	    try 
 	    {
 	    	Properties properties = new Properties();
 	    	FileInputStream in = new FileInputStream(sFile);
 	        properties.load(in);
 	        in.close();
 	        return properties;
 	    } catch (IOException e) {
 	    	logger.error("Unable to load properties file: " + sFile, e);
 	    	throw e;
 	    }
 	}
 	public static void getLoggerAndDBOptions(String sFile) throws IOException
 	{
 		getLoggerAndDBOptions(loadPropertyFile(sFile));
 	}
 	public static void getLoggerAndDBOptions(Properties properties) throws IOException
 	{
 		if (bAlreadyLoadedOptions) return;
 
		String sValue = System.getProperty("raker.log.file");
        if (sValue != null) properties.setProperty("log4j.appender.R.File",sValue);

        // make sure the logger dir exists or it will blow chunks
		sValue = properties.getProperty("log4j.appender.R.File");
 		if (sValue != null) {
			File logFile = new File(sValue);
			System.out.println("Logging to " + logFile.getAbsolutePath());
			File logDir = logFile.getParentFile();
 			if (!logDir.exists()) {
 				logDir.mkdirs();
 			}
		}
 		PropertyConfigurator.configure(properties);
 		
         sValue = System.getProperty("raker.log.level");
         if (sValue != null) {
         	logger.setLevel(Level.toLevel(sValue));
     		logger.info("Log level set to: " + sValue);
         }
 		
         sValue = properties.getProperty("raker.database.config");
         String sDBConfigFile = (sValue == null) ? "config/database.yml" : sValue.trim();
         sDBConfigFile = System.getProperty("raker.database.config", sDBConfigFile).trim();
 
 //        logger.info(System.getProperties().toString());
         sValue = properties.getProperty("rails_env");
         if (sValue != null) sRailsEnv = sValue;
         sRailsEnv = System.getProperty("RAILS_ENV", sRailsEnv);
         
 		try
 		{
 			logger.debug("Looking in " + sDBConfigFile + " for " + sRailsEnv + " database settings");
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
 				throw new IOException("A database was not specified");
 			}
 		} 
 		catch (IOException e)
 		{
 			logger.fatal("Unable to load database configuration file", e);
 			throw e;
 		}
 		bAlreadyLoadedOptions = true;
 	}
 }

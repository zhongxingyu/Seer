 package model.database;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.Properties;
 
 import org.sql2o.Sql2o;
 
 /**
  * This is a singleton class which provides static access to an instance of the
  * database connection.
  * 
  * 
  */
 public class Database {
 
 	/**
 	 * The path of the config file
 	 */
 	private static final String CONFIG_PATH = "config.properties";
 
 	private static Database database;
 
 	private static Sql2o sql;
 
 	private Database() {
 		connect();
 	}
 
 	/**
 	 * Connects to database using information from properties file.
 	 */
 	private void connect() {
 		// Properties is a simple key value store
 		Properties p = new Properties();
 		try {
 			// Loads properties file
 			p.load(new FileInputStream(CONFIG_PATH));
 			sql = new Sql2o("jdbc:mysql://" + p.getProperty("host") + ":"
 					+ p.getProperty("port") + "/" + p.getProperty("database"),
 					p.getProperty("username"), p.getProperty("password"));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Checks if connected to database.
 	 * 
 	 * @return returns whether the database connection has been established
 	 */
 	public static synchronized boolean isConnected() {
 		boolean isConnected = false;
 		try {
 			getInstance().getDataSource().getConnection();
 			isConnected = true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return isConnected;
 	}
 
 	/**
 	 * Gets the single instance of Database.
 	 * 
 	 * @return single instance of Database
 	 */
 	public static synchronized Sql2o getInstance() {
 		if (database == null) {
 			database = new Database();
			return Database.sql;
 		}
 		// Rather than returning an instance of this class like in typical
 		// singleton design we instead return a reference to the Sql2o object
 		return sql;
 	}
 
 }

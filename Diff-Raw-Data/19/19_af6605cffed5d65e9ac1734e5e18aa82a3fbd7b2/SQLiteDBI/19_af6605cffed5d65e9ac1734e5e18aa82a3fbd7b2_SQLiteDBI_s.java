 package mavenp2versionmatch.db;
 
 import mavenp2versionmatch.main.VersionManifest;
 import mavenp2versionmatch.exception.DBIException;
 
 import java.sql.*;
 import java.io.File;
 
 public class SQLiteDBI extends JdbcDBI{
 	//TODO: we should be opening and closing the connection in each method rather
 	// than making the caller open it and then closing it ourselves 
 	//TODO: standardize the dbName and tableName or load them from a config file
 	// Defaults
 	private static final String DEFAULT_DBNAME = "my.db";
 	private static final String DEFAULT_TABLENAME = "maven_p2";
 	
 	public SQLiteDBI(String dbName, String tableName){
 		config.put(JdbcDBI.CONFIG_DBNAME, dbName);
 		config.put(JdbcDBI.CONFIG_TABLENAME, tableName);
 
 		//make sure driver is loaded
 		try {
 			Class.forName("org.sqlite.JDBC");
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException("SQLite Driver wasn't loaded");
 		}
 		//make sure DB exists
 		if(!new File(dbName).exists()) {
 			throw new RuntimeException("Database does not exist");
 		}
 	}
 	public SQLiteDBI(String dbName){
 		this(dbName, DEFAULT_TABLENAME);
 	}
 	public SQLiteDBI(){
 		this(DEFAULT_DBNAME);
 	}
 	
 	public void open() throws DBIException {
 		try{
 			conn = DriverManager.getConnection("jdbc:sqlite:" + config.get(JdbcDBI.CONFIG_DBNAME));
 		}
 		catch(SQLException e){
 			throw new DBIException(e);
 		}
 	}
 	
 }

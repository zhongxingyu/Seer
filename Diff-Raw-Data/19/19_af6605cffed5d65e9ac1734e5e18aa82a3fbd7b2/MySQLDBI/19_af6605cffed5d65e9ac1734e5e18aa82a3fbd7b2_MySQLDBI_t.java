 package mavenp2versionmatch.db;
 
 import mavenp2versionmatch.main.VersionManifest;
 import mavenp2versionmatch.exception.DBIException;
 
 import java.sql.*;
 
 public class MySQLDBI extends JdbcDBI{
 	//TODO: standardize the dbName and tableName or load them from a config file
 	//TODO: the implementation of connection opening and closing connections between
 	//this class and the SQLiteDBI is inconsistent
 	private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/eclipse";
 	private static final String DEFAULT_USER = "eclipse";
 	private static final String DEFAULT_PASSWORD = "Excellence";
 	private static final String DEFAULT_DBNAME = "eclipse";
 	private static final String DEFAULT_TABLENAME = "maven_p2";
 
 
 	public MySQLDBI (String url, String dbName, String tableName, String user, String password) {
		super();
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 		} catch (ClassNotFoundException e) {
 			throw new RuntimeException("MySQL driver not found in classpath", e);
 		}
 
 		/* Store configuration in a map provided by the JdbcDBI superclass */
 		if(url != null) config.put(JdbcDBI.CONFIG_URL, url);
 		if(dbName != null) config.put(JdbcDBI.CONFIG_DBNAME, dbName);
 		if(tableName != null) config.put(JdbcDBI.CONFIG_TABLENAME, tableName);
 		if(user != null) config.put(JdbcDBI.CONFIG_USER, user);
 		if(password != null) config.put(JdbcDBI.CONFIG_PASSWORD, password);
 	}
 
 	public MySQLDBI (){
 		this(DEFAULT_URL, DEFAULT_DBNAME, DEFAULT_TABLENAME, DEFAULT_USER, DEFAULT_PASSWORD);
 	}
 
 	public void open() throws DBIException {
 		try{
 			if((config.containsKey(JdbcDBI.CONFIG_USER)) && (config.containsKey(JdbcDBI.CONFIG_PASSWORD))){ // a username and password have been defined
 				conn = DriverManager.getConnection(config.get(JdbcDBI.CONFIG_URL), config.get(JdbcDBI.CONFIG_USER), config.get(JdbcDBI.CONFIG_PASSWORD));
 			}else{ // Connect without username and password, then
 				conn = DriverManager.getConnection(config.get(JdbcDBI.CONFIG_URL));
 			}
 		}
 		catch(SQLException e){
 			throw new DBIException(e);
 		}
 	}
 }

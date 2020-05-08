 /**
  * 
  */
 package webctdbexport.jdbc;
 
 import java.io.File;
 import java.io.FileReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
import java.sql.PreparedStatement;
 import java.sql.SQLException;
import java.sql.Statement;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author cmg
  *
  */
 public class JdbcUtils {
 	static Logger logger = Logger.getLogger(JdbcUtils.class.getName());
 	public static Connection getConnection(String propsfile) {
 		Properties props = new Properties();
 		try {
 			props.load(new FileReader(new File(propsfile)));
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "Error reading properties file "+propsfile, e);
 			System.exit(-1);
 		}
 		String driverClassName = props.getProperty("driver_class", "oracle.jdbc.driver.OracleDriver");
 		String url = props.getProperty("url", "jdbc:oracle:thin:@HOST:PORT:SID");
 		String username = props.getProperty("username", "USERNAME");
 		String password = props.getProperty("password", "PASSWORD");
		String schema = props.getProperty("schema", null);
 		try {
 			logger.log(Level.INFO, "Load JDBC driver class "+driverClassName);
 			Class.forName(driverClassName);
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "Error loading JDBC driver class "+driverClassName, e);
 			System.exit(-1);
 		}
 		Connection conn = null;
 		try {
			logger.log(Level.INFO, "Open DB connection "+url);
 			conn = DriverManager.getConnection(url, username, password);
 		} catch (SQLException e) {
 			logger.log(Level.SEVERE, "Error getting database connection to "+url+" as "+username+" with "+password, e);
 			System.exit(-1);
 		}
		if (schema!=null) {
			try {
				logger.log(Level.INFO, "Set schema to "+schema);
				Statement stmt = conn.createStatement();
				stmt.execute("ALTER SESSION SET CURRENT_SCHEMA="+schema);
			} catch (SQLException e) {
				logger.log(Level.WARNING, "Could not set schema to "+schema, e);
			}
 		}
 		return conn;
 	}
 }

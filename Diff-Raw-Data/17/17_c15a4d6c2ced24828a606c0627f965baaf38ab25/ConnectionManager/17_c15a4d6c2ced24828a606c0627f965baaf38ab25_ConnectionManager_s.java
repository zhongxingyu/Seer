 package com.epam.lab.intouch.dao.util;
 
 import static com.epam.lab.intouch.dao.util.PropertiesReader.getProperty;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.epam.lab.intouch.dao.exception.DBConnectionException;
 
 /**
  * 
  * Class that manage all DataBase connections and helps easy change DB from one to another.
  * 
  * @author Axel
  * 
  */
 public class ConnectionManager {
 
 	private final static Logger LOG = LogManager.getLogger(ConnectionManager.class);
 
 	private static ConnectionManager instance = null;
 
 	/**
 	 * Private constructor due to Singleton pattern
 	 * 
 	 */
 	private ConnectionManager() {
 
 	}
 
 	public Connection getConnection() throws DBConnectionException {
 		Connection connection = null;
 		try {
 			Class.forName(getProperty("driver_class"));
			connection = DriverManager.getConnection(getConnectionUrl() + ";user=admin" + ";password=1111");
 		} catch (ClassNotFoundException e) {
 			LOG.error("DB driver cannot be found", e);
 		} catch (SQLException e) {
 			LOG.error("Cannot establish cpnnection", e);
 		}
 		return connection;
 	}
 
 	private String getConnectionUrl() {
 		StringBuilder builder = new StringBuilder();
 		builder.append(getProperty("db_url"));
 		builder.append(getProperty("server_name"));
 		builder.append(":");
 		builder.append(getProperty("port_number"));
 		builder.append(";").append("databaseName=");
 		builder.append(getProperty("db_name"));
 		return builder.toString();
 	}
 
 	public static ConnectionManager getInstance() {
 		if (instance == null) {
 			instance = new ConnectionManager();
 		}
 
 		return instance;
 	}
 
 	// private Connection connection = null;
 
 	//
 
 	//
 	// private void openConnection() throws DBConnectionException {
 	//
 	// ComboPooledDataSource cpds = new ComboPooledDataSource();
 	// try {
 	//
 	// cpds.setDriverClass(getProperty("driver_class"));
 	// cpds.setJdbcUrl(getConnectionUrl());
 	// cpds.setUser(getProperty("user_name"));
 	// cpds.setPassword(getProperty("user_password"));
 	//
 	// /*cpds.setDriverClass("com.microsoft.sqlserver.jdbc.SQLServerDriver");
 	// cpds.setJdbcUrl("jdbc:sqlserver://localhost:1433;databaseName=inTouchDB");
 	// cpds.setUser("admin");
 	// cpds.setPassword("1111");
 	// */
 	// // the settings below are optional -- c3p0 can work with defaults
 	// cpds.setMinPoolSize(5);
 	// cpds.setAcquireIncrement(5);
 	// cpds.setMaxPoolSize(20);
 	//
 	// connection = cpds.getConnection();
 	// } catch (SQLException | PropertyVetoException e) {
 	// LOG.error("Error occured while open connection to DB", e);
 	// throw new DBConnectionException();
 	//
 	// }
 	//
 	// }
 	//
 	//
 	//
 	//
 	// public Connection getConnection() throws DBConnectionException {
 	// if (connection == null) {
 	// openConnection();
 	// }
 	// return connection;
 	// }
 	//
 	// public void close() throws DBCloseConnectionException {
 	// try {
 	// connection.close();
 	// connection = null;
 	// } catch (SQLException ex) {
 	// LOG.error("Error occured while closing DB connection", ex);
 	// throw new DBCloseConnectionException();
 	// }
 	// }
 
 }

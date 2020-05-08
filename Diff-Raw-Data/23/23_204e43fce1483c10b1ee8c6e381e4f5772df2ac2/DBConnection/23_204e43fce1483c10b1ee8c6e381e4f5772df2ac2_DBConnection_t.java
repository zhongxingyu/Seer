 /**
  * 
  */
 package org.fubme.persistency;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
 
 /**
  * @author riccardo
  * 
  */
 public final class DBConnection {
 
 	private static final BasicDataSource dataSource = new BasicDataSource();
	private static final String database = "fubme";
	private static final String host = "127.0.0.1";
	private static final String username = "fubme";
	private static final String password = "fubme";
	
 	static {
 		dataSource.setDriverClassName("org.postgresql.Driver");
		String url = "jdbc:postgresql://" + host + "/" + database;
		dataSource.setUrl(url);
		dataSource.setUsername(username);
		dataSource.setPassword(password);
 	}
 
 	public static Connection getConnection() throws SQLException {
 		return dataSource.getConnection();
 	}
 
 }

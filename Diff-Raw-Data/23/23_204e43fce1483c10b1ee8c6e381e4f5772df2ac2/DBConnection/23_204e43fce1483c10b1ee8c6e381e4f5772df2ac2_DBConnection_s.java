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

 	static {
 		dataSource.setDriverClassName("org.postgresql.Driver");
		dataSource.setUrl("jdbc:mysql://localhost:3306/data");
		dataSource.setUsername("USERNAME");
		dataSource.setPassword("PASSWORD");
 	}
 
 	public static Connection getConnection() throws SQLException {
 		return dataSource.getConnection();
 	}
 
 }

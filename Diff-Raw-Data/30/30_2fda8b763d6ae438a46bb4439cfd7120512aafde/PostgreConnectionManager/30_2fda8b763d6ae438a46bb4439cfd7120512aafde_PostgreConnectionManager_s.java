 package ar.edu.itba.it.paw.db.managers;
 
 import java.sql.Connection;
 import java.sql.Driver;
 import java.sql.SQLException;
 import java.util.Properties;
 
 import ar.edu.itba.it.paw.db.ConnectionManager;
 
 /**
  * Provides a JDBC connection to a PgSQL server.
  * 
  * @author cris
  */
 public class PostgreConnectionManager implements ConnectionManager {
 
	private final String connectionString;
 
 	private Connection conn;
 
 	private Driver driver = new org.postgresql.Driver();
 
 	private String username = "cris";
 	private String password = "holahola";
 
 	public PostgreConnectionManager(final String connectionString,
 			final String username, final String password) {
 		this.connectionString = connectionString;
 		this.username = username;
 		this.password = password;
 	}
 
 	public PostgreConnectionManager(final String connectionString) {
 		this.connectionString = connectionString;
 	}
 
 	public PostgreConnectionManager() {
 		try {
 			Class.forName("org.postgresql.Driver");
 		} catch (final ClassNotFoundException e) {
 			e.printStackTrace();
 		}
		this.connectionString = "￼￼jdbc:postgresql://localhost:5432/hotelapp";
 	}
 
 	public Connection getConnection() throws SQLException {
 		if (this.conn == null) {
 			final Properties props = new Properties();
 			props.setProperty("user", this.username);
 			props.setProperty("password", this.password);
 			this.conn = this.driver.connect(this.connectionString, props);
 		}
 
 		return this.conn;
 
 	}
 }

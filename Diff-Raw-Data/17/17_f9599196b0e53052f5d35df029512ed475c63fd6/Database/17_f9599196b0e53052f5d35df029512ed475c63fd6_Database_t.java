 package rstrip.utility;
 
 /*
 * based on code from site
  * http://balakhonov-yuriy.ru/java-mysql/
  */
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import rstrip.Server;
 
 public class Database {
 
 	private static Database init;
 	private Connection connection;
 	
 	private Database() throws ClassNotFoundException, SQLException {
 		this.connect();
 	}
 	
 	public static Database init() throws ClassNotFoundException, SQLException {
 		if (init == null) {
 			init = new Database();
 		}
 		return init;
 	}
 	
 	private void connect() throws ClassNotFoundException, SQLException {
 		String driverName = "com.mysql.jdbc.Driver"; 
 		Class.forName(driverName);
 
 		String serverName = Server.settings.getDbHost();
 		String mydatabase = Server.settings.getDbName();
 		String url = "jdbc:mysql://" + serverName + "/" + mydatabase;
 		String username = Server.settings.getDbUsername();
 		String password = Server.settings.getDbPassword();
 
 		this.connection = DriverManager.getConnection(url, username, password);
 		System.out.println("Is connected to DB " + this.connection);
 	}
 	
 	public ResultSet query(String query) throws SQLException{
 		Statement stmt = this.connection.createStatement();
 
 		ResultSet rs = stmt.executeQuery(query);
 		return rs;
 	}
 	
 	public void exec(String query) throws SQLException{
 		Statement stmt = this.connection.createStatement();
 
 		stmt.executeUpdate(query);
 	}
 	
 	public ResultSet exec(String query, String primaryKey) throws SQLException{
 		Statement stmt = this.connection.createStatement();
 
 		stmt.executeUpdate(query);
 		
 		ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
 		return rs;
 	}
 	
 	public void startTransaction() throws SQLException{
 		Statement stmt = this.connection.createStatement();
 
 		stmt.executeQuery("SET AUTOCOMMIT=0");
 		stmt.executeQuery("START TRANSACTION");
 	}
 	
 	public void endTransaction() throws SQLException{
 		Statement stmt = this.connection.createStatement();
 
 		stmt.executeQuery("COMMIT");
 		stmt.executeQuery("SET AUTOCOMMIT=1");
 	}
 	
 	public void rollback() throws SQLException{
 		Statement stmt = this.connection.createStatement();
 
 		stmt.executeQuery("ROLLBACK");
 	}
 	
 	public void disconnect() throws SQLException {	
 		this.connection.close();
 	}
 }

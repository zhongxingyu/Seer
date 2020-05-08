 package server;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Properties;
 
 public class DBConnection {
 	/*
 	 *A connection with the database.
 	 *Fully tested
 	 *Final 
 	 */
 	
 	Connection connection;
 	String url;
 
 	public DBConnection() {
 		url = "jdbc:mysql://46.9.153.76:3306/calendar";
 	}
 
 	public void initialize() throws SQLException, ClassNotFoundException {
 		Class.forName("com.mysql.jdbc.Driver");
 		Properties connectionProperties = new Properties();
 
 		connectionProperties.setProperty("user", "caluser");
 		connectionProperties.setProperty("password", "heyo!");
 		connection = DriverManager.getConnection(url, connectionProperties);
 	}
 
 	// A method to make a query on the database and get a resultset in return
 	public ResultSet makeQuery(String sql) throws SQLException {
 		Statement st = connection.createStatement();
 		return st.executeQuery(sql);
 	}
 
 	// Will update the database according to the sql feeded
 	public void makeUpdate(String sql) throws SQLException {
 		Statement st = connection.createStatement();
		OutputController.output(sql);
 		st.executeUpdate(sql);
 	}
 
 	/*
 	 * This method is only to be used when inserting a single row into a table.
 	 * It will return the generated primary key
 	 */
 	public int makeUpdateReturnID(String sql) throws SQLException {
 		Statement st = connection.createStatement();
 		st.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
 		ResultSet keys = st.getGeneratedKeys();
		OutputController.output(sql);
 		if(keys.next())
 			return keys.getInt(1);
 		else{
 			throw new SQLException();
 		}
		
 	}
 
 }

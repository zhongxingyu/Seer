 package dbtLab3;
 
 import java.util.ArrayList;
 import java.sql.*;
 
 import datatypes.SingleObjectHolder;
 
 /**
  * Database is a class that specifies the interface to the movie database. Uses
  * JDBC and the MySQL Connector/J driver.
  */
 public class Database {
 	/**
 	 * The database connection.
 	 */
 	private Connection conn;
 
 	/**
 	 * An SQL statement object.
 	 */
 	private Statement stmt;
 
 	/**
 	 * Create the database interface object. Connection to the database is
 	 * performed later.
 	 */
 	public Database() {
 		conn = null;
 	}
 
 	/**
 	 * Open a connection to the database, using the specified user name and
 	 * password.
 	 * 
 	 * @param userName
 	 *            The user name.
 	 * @param password
 	 *            The user's password.
 	 * @return true if the connection succeeded, false if the supplied user name
 	 *         and password were not recognized. Returns false also if the JDBC
 	 *         driver isn't found.
 	 */
 	public boolean openConnection(String userName, String password) {
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			conn = DriverManager.getConnection(
 					"jdbc:mysql://puccini.cs.lth.se/" + userName, userName,
 					password);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Close the connection to the database.
 	 */
 	public void closeConnection() {
 		try {
 			if (conn != null) {
 				conn.close();
 			}
 		} catch (SQLException e) {
 		}
 		conn = null;
 	}
 
 	/**
 	 * Check if the connection to the database has been established
 	 * 
 	 * @return true if the connection has been established
 	 */
 	public boolean isConnected() {
 		return conn != null;
 	}
 
 	public ArrayList<SingleObjectHolder<String>> getMovies() {
 		ArrayList<SingleObjectHolder<String>> toReturn = new ArrayList<SingleObjectHolder<String>>();
		String sql = "select Movies.id, Movies.name " +
 		"from Movies";
 		PreparedStatement ps = null;
 		try {
 			ps = conn.prepareStatement(sql);
 			ResultSet rs = ps.executeQuery();
 			while(rs.next()) {
				int id = rs.getInt("id");
 				String name = rs.getString("name");
 				toReturn.add(new SingleObjectHolder<String>(id, name));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if(ps != null) ps.close();
 			} catch(SQLException e2) {
 				e2.printStackTrace();
 			}
 		}
 		return toReturn;
 	}
 
 }

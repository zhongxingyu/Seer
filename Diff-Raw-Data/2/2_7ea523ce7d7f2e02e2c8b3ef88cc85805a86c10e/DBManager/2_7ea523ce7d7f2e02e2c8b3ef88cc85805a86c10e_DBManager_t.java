 package de.mms.db;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class DBManager {
 
 	public static final boolean LOCAL = true;
 
 	protected static String URL = "jdbc:mysql://localhost/krm_db?";
 	protected static String USERDATA = "user=krm_user&password=wlc93Qx6aoJ4v";
 	protected static final String DRIVER = "com.mysql.jdbc.Driver";
 
 	static {
 		try {
 			// load driver
 			Class.forName(DRIVER);
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	/**
 	 * Open the {@link Connection} to the MySQL database
 	 * 
 	 * @return con
 	 * @throws SQLException
 	 */
 	protected static Connection openConnection() throws SQLException {
 		if (LOCAL) {
 			URL = "jdbc:mysql://localhost/krm_db?";
			USERDATA = "user=root&password=1913";
 		} else {
 
 			USERDATA = "user=krm_user&password=wlc93Qx6aoJ4v";
 			URL = "jdbc:mysql://bolonka-zwetna-von-der-kreuzbergquelle.de/krm_db?";
 		}
 		Connection con = null;
 		try {
 			con = DriverManager.getConnection(URL + USERDATA);
 		} catch (SQLException e) {
 			System.out.println("No connection to database possible.");
 			System.exit(2);
 		}
 		return con;
 	}
 
 	// end local databas connection
 
 	/**
 	 * Close the {@link Connection}
 	 */
 	protected static void closeQuietly(Connection connection) {
 		if (null == connection)
 			return;
 		try {
 			connection.close();
 		} catch (SQLException e) {
 			// ignored
 		}
 	}
 
 	/**
 	 * Close the {@link Statement}s
 	 * 
 	 * @param statment
 	 */
 	protected static void closeQuietly(Statement statment) {
 		if (null == statment)
 			return;
 		try {
 			statment.close();
 		} catch (SQLException e) {
 			// ignored
 		}
 	}
 
 	/**
 	 * Close the {@link ResultSet}s
 	 * 
 	 * @param resultSet
 	 */
 	protected static void closeQuietly(ResultSet resultSet) {
 		if (null == resultSet)
 			return;
 		try {
 			resultSet.close();
 		} catch (SQLException e) {
 			// ignored
 		}
 	}
 
 	/**
 	 * for Pro ;)
 	 * 
 	 * @param query
 	 * @throws SQLException
 	 */
 	public static void runQuery(String query) throws SQLException {
 		Connection con = null;
 
 		con = openConnection();
 		Statement stmt = con.createStatement();
 		con.setAutoCommit(false);
 		stmt.executeUpdate(query);
 		try {
 			con.commit();
 		} catch (SQLException exc) {
 			con.rollback(); // bei Fehlschlag Rollback der Transaktion
 			System.out
 					.println("COMMIT fehlgeschlagen - Rollback durchgefuehrt");
 		} finally {
 			closeQuietly(stmt);
 			closeQuietly(con); // Abbau Verbindung zur Datenbank
 		}
 	}
 }

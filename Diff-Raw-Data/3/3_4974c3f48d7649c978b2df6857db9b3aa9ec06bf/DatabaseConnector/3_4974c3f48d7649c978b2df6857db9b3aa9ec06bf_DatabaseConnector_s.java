 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 /* DatabaseHandler
  * Handles the database request for server.
  * 
  * @author Jeanne Deng
  */
 
 public class DatabaseConnector {
 
 	private static final String dburl = "jdbc:mysql://127.0.0.1/CS408";
 	private Connection con;
 	private boolean isConnected = false;
 
 	public DatabaseConnector() {}
 
 	private boolean connect() {
 		if(isConnected) {
 			return true;
 		}
 
 		try {
 			Class.forName("com.mysql.jdbc.Driver");
 			con = DriverManager.getConnection(dburl, "cs408server", "server");
 			isConnected = true;
 		} catch(Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	private void disconnect() {
 		if(!isConnected) {
 			return;
 		}
 		try {
 			con.close();
 			isConnected = false;
 		} catch(Exception e) {}
 	}
 
 	private synchronized ResultSet executeQuery(String query) {
 		this.connect();
 		ResultSet result = null;
 
 		if(!isConnected) {
 			return null;
 		}
 		try {
 			Statement stmt = con.createStatement();
 			result = stmt.executeQuery(query);
 		} catch(Exception e) {
 			result = null;
 		}
 
 		return result;
 	}
 
 	private synchronized boolean execute(String query) {
 		this.connect();
 		boolean result = true;
 
 		if(!isConnected) {
 			return false;
 		}
 		try {
 			Statement stmt = con.createStatement();
 			stmt.execute(query);
 		} catch(Exception e) {
 			result = false;
 		}
 
 		this.disconnect();
 		return result;
 	}
 
 	private int getUserId(String name) {
 		String query = "SELECT id FROM Users WHERE name='" + name + "'";
 		ResultSet rs = executeQuery(query);
 		int id = -1;
 		try {
 			rs.next();
 			id = rs.getInt(0);
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return id;
 	}
 
 	public boolean addUser(String name, String password) {
 		String query = "INSERT INTO Users (name, password) VALUES ('" + name + "','" + password + "')";
		int userId = getUserId(name);
 		if(execute(query)) {
 			for(int i = 0; i < 10; i++) {
 				query = "INSERT INTO Progresses (userId, slotId, progress) VALUES ('" + userId + "','" + i + "','(empty)')";
 				execute(query);
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isValidLogin(String name, String password) {
 		String query = "SELECT password FROM Users WHERE name='" + name + "'";
 		ResultSet rs = executeQuery(query);
 		if(rs == null) {
 			this.disconnect();
 			return false;
 		}
 		boolean result = false;
 		try {
 			rs.next();
 			if(rs.getString(0).equals(password)) {
 				result = true;
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		this.disconnect();
 		return result;
 	}
 
 	public boolean updateUserData(String name, String slotNo, String gameProgress) {
 		int userId = this.getUserId(name);
 		String query = "UPDATE Progresses SET progress='" + gameProgress + "' WHERE userId='" + userId + "' AND slotId='" + slotNo + "'";
 		return execute(query);
 	}
 
 	public String getUserData(String name) {
 		int userId = this.getUserId(name);
 		String query = "SELECT slotId, progress FROM Progresses WHERE userId='" + userId + "'";
 		ResultSet rs = executeQuery(query);
 		if(rs == null) {
 			this.disconnect();
 			return "fail";
 		}
 		String result = "";
 		try {
 			while(rs.next()) {
 				result += (new Integer(rs.getInt(0))).toString() + ":;:" + rs.getString(1) + ":;:";
 			}
 		} catch(SQLException e) {
 			e.printStackTrace();
 		}
 		this.disconnect();
 		return result.substring(0, result.length()-3);
 	}
 }

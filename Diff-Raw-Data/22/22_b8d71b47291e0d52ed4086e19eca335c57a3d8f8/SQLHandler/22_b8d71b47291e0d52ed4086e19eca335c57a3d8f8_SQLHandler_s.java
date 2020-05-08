 package superguild;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
 
 public class SQLHandler {
 	
 	private MysqlDataSource dataSource;
 	private Connection sqlConnection = null;
 	private boolean debugMode;
 	private boolean connected = false;
 	
 	private String host,username,database;
 	
 	public SQLHandler(String host, String username, String password, String database) {
 		debugMode = false;
 		dataSource = new MysqlDataSource();
 		dataSource.setServerName(host);
 		dataSource.setPort(3306);
 		dataSource.setUser(username);
 		dataSource.setPassword(password);
 		dataSource.setDatabaseName(database);
 		
 		this.host = host;
 		this.username = username;
 		this.database = database;
 	}
 	
 	public SQLHandler() {
 		
 	}
 	
 	public void useCredentials(String host, String username, String password, String database) {
 		debugMode = false;
 		dataSource = new MysqlDataSource();
 		dataSource.setServerName(host);
 		dataSource.setPort(3306);
 		dataSource.setUser(username);
 		dataSource.setPassword(password);
 		dataSource.setDatabaseName(database);
 		
 		this.host = host;
 		this.username = username;
 		this.database = database;
 	}
 	
 	public void debug(boolean t) {
 		debugMode = t;
 	}
 	public boolean isConnected(){
 		return connected;
 	}
 	
 	public void connect() {
 		try {
 			sqlConnection = dataSource.getConnection();
 			if(debugMode) System.out.println("SQLHandler connected.");
 			connected = true;
 		} catch (SQLException e) {
 			System.err.println("SQLHandler: Can't connect to database, using "+username+"@"+host);
 			e.printStackTrace();
 			return;
 		}
 	}
 	
 	public String getDBInfo() {
 		try {
 			return sqlConnection.getMetaData().toString();
 		} catch (SQLException e) {
 			System.err.println("SQLHandler: ERROR getting info from DB. Are you connected?");
 			e.printStackTrace();
 			return "";
 		}
 	}
 	
 	public ResultSet selectQuery(String query, String data[]) {
 		ResultSet rs = null;
 		try {
 			PreparedStatement prep = sqlConnection.prepareStatement(query);
 			int varCount = 0;
 			for(int i=0; i<query.length(); i++) {
 				if(query.charAt(i)=='?') varCount++;
 			}
 			
			for(int i=0; i<varCount; i++) {
 				int possibleInt;
 				try {
 					//Is it an int?
					possibleInt = Integer.parseInt(data[i]);
 					prep.setInt(i, possibleInt);
 				} catch(NumberFormatException e) {
 					//It wasn't an int. Treat it as string
					prep.setString(i, data[i]);
 				}
 			}
 			rs = prep.executeQuery();
 			
 		} catch (SQLException e) {
 			System.err.println("SQLHandler: ERROR preparing statement for select.");
 			e.printStackTrace();
 		}
 		return rs;
 	}
 	
 	public ResultSet selectQuery(String query) {
 		ResultSet rs = null;
 		try {
 			PreparedStatement prep = sqlConnection.prepareStatement(query);
 			rs = prep.executeQuery();
 			
 		} catch (SQLException e) {
 			System.err.println("SQLHandler: ERROR preparing statement for simple select.");
 			e.printStackTrace();
 		}
 		return rs;
 	}
 	
 	public int updateQuery(String query, String data[]) {
 		int rs = 0;
 		try {
 			PreparedStatement prep = sqlConnection.prepareStatement(query);
 			int varCount = 0;
 			for(int i=0; i<query.length(); i++) {
 				if(query.charAt(i)=='?') varCount++;
 			}
 			
			for(int i=0; i<varCount; i++) {
 				int possibleInt;
 				try {
 					//Is it an int?
					possibleInt = Integer.parseInt(data[i]);
 					prep.setInt(i, possibleInt);
 				} catch(NumberFormatException e) {
 					//It wasn't an int. Treat it as string
					prep.setString(i, data[i]);
 				}
 			}
 			rs = prep.executeUpdate();
 			
 		} catch (SQLException e) {
 			System.err.println("SQLHandler: ERROR preparing statement for insert/update.");
 			e.printStackTrace();
 		}
 		
 		return rs;
 	}
 	
 	
 	
 
 }

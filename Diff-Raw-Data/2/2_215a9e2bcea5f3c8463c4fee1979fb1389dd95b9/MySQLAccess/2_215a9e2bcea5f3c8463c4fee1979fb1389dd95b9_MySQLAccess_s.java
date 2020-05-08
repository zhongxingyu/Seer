 package mta.devweb.bitcoinbuddy.model.db;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class MySQLAccess {
 	private static final String URL = "jdbc:mysql://localhost:3306/bitcoin";
 	private static final String USER = "bitcoin";
 	private static final String PASSWORD = "1234";
 	private static final String DRIVER = "com.mysql.jdbc.Driver";
 	
 	protected Connection connection;
 	public MySQLAccess() {
 		this.connection = connect();
 	}
 	
 	public Connection getConnection() {
 		if (connection == null) {
 			connection = connect();
 		}
 		return connection;
 	}
 	private  Connection connect() {
 		try {
 			// Load the MySQL driver
 			Class.forName(DRIVER);
 			//if we are in cloud-mode, get connection details from VCAP_SERVICES, else use other params
 			 String jsonEnvVars = java.lang.System.getenv("VCAP_SERVICES");
 		        if(jsonEnvVars != null){
 					return parseUrlFromEnvVarsAndConnect(jsonEnvVars); 
 				}
 		        else {
 		        	//Setup the connection with the DB
 		        	return DriverManager.getConnection(URL, USER, PASSWORD);
 		        }
 			// 
 			
 
 		} catch (Exception e) {
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public static void disconnect(Connection connection) throws SQLException {
 		if (connection != null) {
 			connection.close();
 		}
 	}
 	
     private static Connection parseUrlFromEnvVarsAndConnect(String jsonEnvVars) {
 		String url = "";
 		try {
 			JSONObject jsonObject = new JSONObject(jsonEnvVars);
 			JSONArray jsonArray = jsonObject.getJSONArray("mysql-5.1");
 			jsonObject = jsonArray.getJSONObject(0);
 			jsonObject = jsonObject.getJSONObject("credentials");
 			String host 	=  jsonObject.getString("host");
 			System.out.println("parseUrlFromEnvVarsAndConnect host="+host);
			String port 	=  jsonObject.getString("port");
 			System.out.println("parseUrlFromEnvVarsAndConnect port="+port);
 			String dbName 	=  jsonObject.getString("name");
 			System.out.println("parseUrlFromEnvVarsAndConnect dbName="+dbName);
 			String username =  jsonObject.getString("username");
 			System.out.println("parseUrlFromEnvVarsAndConnect username="+username);
 			String password =  jsonObject.getString("password");
 			
 			System.out.println("parseUrlFromEnvVarsAndConnect password="+password);
 
 			url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
 			return  DriverManager.getConnection(url, username, password);
 		} 
 		catch (JSONException e) {
 			System.err.println("Conn.connect: " + e.getMessage());
 		}
 		catch (SQLException e){
 			System.err.println("Conn.connect: " + e.getMessage());
 		}
 		return null;
 	}
     
     public  void createTable(String createTable) {
 		try {
 			Statement statement = connection.createStatement();
 			statement.executeUpdate(createTable);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 }

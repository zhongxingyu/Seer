 package db;
 
 import java.sql.SQLException;
 
 public class Initializer {
 	public Initializer() throws SQLException {
 		createTables(false);
 	}
 	
 	public void recreateTables() throws SQLException{
 		createTables(true);
 	}
 	
 	private void createTables(boolean force) throws SQLException {
 		createNetworkSignalsTable(force);
 		createPointsTable(force);
 	}
 
 	private void createNetworkSignalsTable(boolean force) throws SQLException {
 		createTable(force, "network_signals", 
 			"id 		INTEGER PRIMARY KEY AUTOINCREMENT, " + 
 			"point_id	INTEGER, " + 
 			"ssid	   	VARCHAR(255), " + 
 			"bssid	   	VARCHAR(255), " + 
 			"rssi	   	VARCHAR(255), " + 
 			"encryption	VARCHAR(255), " +
 			"channel   	VARCHAR(255), " +
 			"speed	   	VARCHAR(255)"
 		);
 	}
 	
 	private void createPointsTable(boolean force) throws SQLException {
 		createTable(force, "signal_points",
 			"id 				INTEGER PRIMARY KEY AUTOINCREMENT, " + 
 			"latitude 			VARCHAR(255), " + 
 	    	"longitude 			VARCHAR(255), " + 
 	    	"quality 			VARCHAR(255), " + 
	    	"satelliteCount		VARCHAR(255) "
 		);
 	}
 	
 	private void createTable(boolean force, String tableName, String tableColumns) throws SQLException {
 		if (force){
 			DatabaseConnection.execute("DROP TABLE IF EXISTS " + tableName + ";");
 		}
 		DatabaseConnection.execute(
 			"CREATE TABLE " + (force ? "" : "IF NOT EXISTS") + " " + tableName + " (" + tableColumns + ");"
 		);
 	}
 }

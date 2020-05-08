 package models.cmu.sv.sensor;
 
 import java.util.ArrayList;
import java.util.Iterator;
 import java.io.FileInputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Properties;
 
 import play.Logger.ALogger;
 
 public class DBHandler {
 	protected Connection connection = null;
 	protected Properties prop = null;
 	
 	protected String serverIP = "";
 	protected String serverPort = "";
 	protected String dbUser= "";
 	protected String dbPassword = "";
 	
 	
 	public DBHandler(String fileName){
 		//For heroku: Use local env instead
 		if(System.getenv("serverip") != null){
 			this.serverIP = System.getenv("serverip");
 			this.serverPort = System.getenv("serverport");
 			this.dbUser = System.getenv("dbuser");
 			this.dbPassword = System.getenv("dbpassword");
 		}
 		else{
 
 			this.prop = new Properties();
 			try {
 				this.prop.load(new FileInputStream(fileName));
 				this.serverIP = prop.getProperty("serverip");
 				this.serverPort = prop.getProperty("serverport");
 				this.dbUser = prop.getProperty("dbuser");
 				this.dbPassword = prop.getProperty("dbpassword");
 			
 			} catch (Exception e) {
 				
 				System.err.println("Unable to read the database properties");
 				return;
 			}
 		}
 	}
 	public boolean makeConnection(){
 		try {
 			if(this.connection != null && this.connection.isValid(0))
 				return true;
 		} catch (SQLException e1) {
 			// TODO Auto-generated catch block
 			//e1.printStackTrace();
 			
 		}
 		try { 
 		
 			this.connection = DriverManager.getConnection( "jdbc:sap://" + serverIP + ":" + serverPort + "/?autocommit=false",dbUser,dbPassword); 
 			//PreparedStatement preparedStatement = this.connection.prepareStatement("SET SCHEMA CMU");
 			//return preparedStatement.execute();
 			this.connection.setAutoCommit(true);
 			return true;
 
 		} catch (SQLException e) {
 			System.err.println(e.getMessage());
 			System.err.println("Connection Failed. User/Passwd Error?");
 			return false;
 
 		}
 	
 	}
 	public void closeConnection(){
 		try {
 			if(this.connection!=null && !this.connection.isClosed()){
 				this.connection.close();
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			//e.printStackTrace();
 		}
 		this.connection = null;
 		
 	}
 	public ResultSet runQuery(String sql){
 		this.makeConnection();
 		ResultSet resultSet = null;
 		PreparedStatement preparedStatement;
 		try {
 			preparedStatement = this.connection.prepareStatement(sql);
 			resultSet = preparedStatement.executeQuery();
 			this.closeConnection();
 		}
 		catch(SQLException e){
 			e.printStackTrace();
 		}
 		return resultSet;
 	}
 	public boolean addReading(String deviceId, Long timeStamp, String sensorType, double value){
 		this.makeConnection();
 		PreparedStatement preparedStatement;
 		try {
 			preparedStatement = this.connection.prepareStatement("INSERT INTO CMU.CMU_SENSOR( deviceID, timeStamp, sensorType, value) VALUES(?, ?, ?, ?)");
 			preparedStatement.setString(1, deviceId);
 			preparedStatement.setLong(2, timeStamp);
 			preparedStatement.setString(3, sensorType);
 			preparedStatement.setDouble(4, value);
 			preparedStatement.executeUpdate();
 			this.closeConnection();
 			return true;
 		} catch (SQLException e) {
 			System.err.println(e.getMessage());
 			ALogger log = play.Logger.of(DBHandler.class);
 			log.warn(e.getMessage());
 			return false;
 		}
 		
 	}
 	
 	public boolean deleteReading(String deviceID, Long timeStamp, String sensorType){
 		this.makeConnection();
 		PreparedStatement preparedStatement;
 		try{
 			preparedStatement = this.connection.prepareStatement("DELETE FROM CMU.CMU_SENSOR WHERE deviceID=? AND timeStamp=? AND sensorType=?");
 			preparedStatement.setString(1, deviceID);
 			preparedStatement.setLong(2, timeStamp);
 			preparedStatement.setString(3, sensorType);
 			preparedStatement.executeUpdate();
 			this.closeConnection();
 			return true;
 		}catch(SQLException e){
 			//e.printStackTrace();
 			return false;
 		}
 	}
 	public Connection getConnection(){
 		try {
 			if(connection == null || connection.isClosed()){
 				this.makeConnection();
 			}
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			this.makeConnection();
 		}
 		return this.connection;
 	}
 	
 	public SensorReading searchReading(String deviceId, Long timeStamp, String sensorType){
 		this.makeConnection();
 		PreparedStatement preparedStatement;
 		try{
 			preparedStatement = this.connection.prepareStatement("SELECT * FROM CMU.CMU_SENSOR WHERE deviceID=? AND timeStamp<=? AND sensorType=? ORDER BY timeStamp DESC LIMIT 1");	
 			preparedStatement.setString(1, deviceId);
 			preparedStatement.setLong(2, timeStamp);
 			preparedStatement.setString(3, sensorType);
 			ResultSet resultSet = preparedStatement.executeQuery();
 			if(!resultSet.next()){
 				return null;
 			}
 			String rs_deviceId = resultSet.getString(1);
 			Long rs_timeStamp = resultSet.getLong(2);
 			String rs_sensorType = resultSet.getString(3);
 			double rs_value = resultSet.getDouble(4);						
 			this.closeConnection();
 			return new SensorReading(rs_deviceId, rs_timeStamp, rs_sensorType, rs_value);			
 		}catch(SQLException e){
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	public ArrayList<SensorReading> searchReading(String deviceId, Long startTime, long endTime, String sensorType){		
 		this.makeConnection();
 		System.out.println(deviceId + "," + startTime + "," + endTime + "," + sensorType);
 		PreparedStatement preparedStatement;
 		try{
 			preparedStatement = this.connection.prepareStatement("SELECT \"TIMESTAMP\", \"VALUE\" FROM \"CMU\".\"CMU_SENSOR\"" 
 					+ " WHERE \"DEVICEID\" = ? AND \"TIMESTAMP\" >= ? AND \"TIMESTAMP\" <= ? AND \"SENSORTYPE\" = ? ORDER BY \"TIMESTAMP\" DESC");	
 			preparedStatement.setString(1, deviceId);
 			preparedStatement.setLong(2, startTime);
 			preparedStatement.setLong(3, endTime);
 			preparedStatement.setString(4, sensorType);
 			ResultSet resultSet = preparedStatement.executeQuery();
 			ArrayList<SensorReading> readings = new ArrayList<SensorReading>();
 			while(resultSet.next()){
 				Long rs_timeStamp = resultSet.getLong(1);
 				double rs_value = resultSet.getDouble(2);
 				readings.add(new SensorReading(deviceId, rs_timeStamp, sensorType, rs_value));
 			}
 			System.out.println(readings.size() + " reading(s) fetched");
 			this.closeConnection();
 			return readings;			
 		}catch(SQLException e){
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 	
 	public ArrayList<SensorReading> lastReadingFromAllDevices(Long timeStamp, String sensorType){
 		this.makeConnection();
 		PreparedStatement preparedStatement;
 		try{
 			preparedStatement = this.connection.prepareStatement("SELECT \"DEVICEID\", \"TIMESTAMP\", \"VALUE\" FROM " +
 				"(SELECT * FROM \"CMU\".\"CMU_SENSOR\" AS a " + 
 				"INNER JOIN " +
 				"(SELECT " +
 				"\"DEVICEID\" as device_id," +
 				"max(\"TIMESTAMP\") as max_timestamp " +
 				"FROM \"CMU\".\"CMU_SENSOR\" " +
 				"WHERE \"SENSORTYPE\" = ? " + // 1st parameter - sensorType
				"AND SECONDS_BETWEEN(add_seconds(TO_TIMESTAMP (\'1970-01-01 00:00:00\'), \"TIMESTAMP\" / 1000), ?) <= 60 " + // 2nd parameter - timeStamp
 				"GROUP BY \"DEVICEID\"" +
 				") b "+
 				"ON "+
 				"a.\"DEVICEID\" = b.device_id AND " +
 				"a.\"TIMESTAMP\" = b.max_timestamp " +
				"WHERE a.\"SENSORTYPE\" = ?)"); // 3rd parameter - sensorType
 			preparedStatement.setString(1, sensorType);
 			preparedStatement.setLong(2, timeStamp);
			preparedStatement.setString(1, sensorType);
 			ResultSet resultSet = preparedStatement.executeQuery();
 			ArrayList<SensorReading> readings = new ArrayList<SensorReading>();
 			while(resultSet.next()){
 				String rs_deviceId = resultSet.getString(1);
 				Long rs_timeStamp = resultSet.getLong(2);
 				double rs_value = resultSet.getDouble(3);
 				readings.add(new SensorReading(rs_deviceId, rs_timeStamp, sensorType, rs_value));
			}
 			System.out.println(readings.size() + " reading(s) fetched");
 			this.closeConnection();			
 			return readings;			
 		}catch(SQLException e){
 			e.printStackTrace();
 			return null;
 		}
 	}	
 	
 	
 }

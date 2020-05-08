 package models.cmu.sv.sensor;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Properties;
 import java.sql.*;
 
 public class DBHandler {
 	protected Connection connection = null;
 	protected Properties prop = null;
 	
 	protected String serverIP = "";
 	protected String serverPort = "";
 	protected String dbUser= "";
 	protected String dbPassword = "";
 	
 	
 	public DBHandler(FileInputStream fs){
 		this.prop = new Properties();
 		try {
 			
 			this.prop.load(fs);
 			this.serverIP = prop.getProperty("serverip");
 			this.serverPort = prop.getProperty("serverport");
 			this.dbUser = prop.getProperty("dbuser");
 			this.dbPassword = prop.getProperty("dbpassword");
 			
 		} catch (IOException e) {
 			//For heroku: Use local env instead 
			
 			this.serverIP = System.getenv("serverip");
 			this.serverPort = System.getenv("serverport");
 			this.dbUser = System.getenv("dbuser");
 			this.dbPassword = System.getenv("dbpassword");
 			
 			if(this.serverIP == "" || this.serverPort == "" || this.dbUser == "" || this.dbPassword == ""){
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
 	public boolean addReading(int deviceId, int timeStamp, String sensorType, double value){
 		this.makeConnection();
 		PreparedStatement preparedStatement;
 		try {
 			preparedStatement = this.connection.prepareStatement("INSERT INTO CMU.CMU_SENSOR( deviceID, timeStamp, sensorType, value) VALUES(?, ?, ?, ?)");
 			preparedStatement.setInt(1, deviceId);
 			preparedStatement.setInt(2, timeStamp);
 			preparedStatement.setString(3, sensorType);
 			preparedStatement.setDouble(4, value);
 			preparedStatement.executeUpdate();
 			this.closeConnection();
 			return true;
 		} catch (SQLException e) {
 			
 			//e.printStackTrace();
 			return false;
 		}
 		
 	}
 	
 	public boolean deleteReading(int deviceId, int timeStamp, String sensorType){
 		this.makeConnection();
 		PreparedStatement preparedStatement;
 		try{
 			preparedStatement = this.connection.prepareStatement("DELETE FROM CMU.CMU_SENSOR WHERE deviceID=? AND timeStamp=? AND sensorType=?");
 			preparedStatement.setInt(1, deviceId);
 			preparedStatement.setInt(2, timeStamp);
 			preparedStatement.setString(3, sensorType);
 			preparedStatement.executeUpdate();
 			this.closeConnection();
 			return true;
 		}catch(SQLException e){
 			//e.printStackTrace();
 			return false;
 		}
 	}
 	public SensorReading searchReading(int deviceId, int timeStamp){
 		this.makeConnection();
 		PreparedStatement preparedStatement;
 		try{
 			preparedStatement = this.connection.prepareStatement("SELECT * FROM CMU.CMU_SENSOR WHERE deviceID=? AND timeStamp=?");
 			preparedStatement.setInt(1, deviceId);
 			preparedStatement.setInt(2, timeStamp);
 			ResultSet resultSet = preparedStatement.executeQuery();
 			if(!resultSet.next()){
 				return null;
 			}
 			deviceId = resultSet.getInt(1);
 			timeStamp = resultSet.getInt(2);
 			String sensorType = resultSet.getString(3);
 			double value = resultSet.getDouble(4);
 			this.closeConnection();
 			return new SensorReading(deviceId, timeStamp, sensorType, value);
 			
 		}catch(SQLException e){
 			//e.printStackTrace();
 			return null;
 		}
 	}
 	
 	
 }

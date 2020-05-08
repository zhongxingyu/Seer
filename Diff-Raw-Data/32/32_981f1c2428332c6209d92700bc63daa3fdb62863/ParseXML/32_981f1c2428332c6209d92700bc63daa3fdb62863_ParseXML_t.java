 package org.mugsandcoffee;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 /**
  * Inserts records into database's table
  * @author Mugs and Coffee
  * @coder Kenneth "digiArtist_ph" P. Vallejos
  * @since Sunday, May 19, 2013
  * @version 1.0
  *
  */
 public class ParseXML {
 	
 	private String mServer;
 	private String mDatabase;
 	private String mUser;
 	private String mPword;
 	private Connection Conn;
 	
 	public ParseXML() {
 		// loads the default connetion details
 		this.mServer = "jdbc:mysql://localhost:3306/";
 		this.mDatabase = "matsim";
 		this.mUser = "root";
 		this.mPword = "";
 		
 	}
 	
 	public void connectToDB() {
 		
 		try {
 			// connects to the selected database
 			this.Conn = DriverManager.getConnection(this.mServer + this.mDatabase, this.mUser, this.mPword);
 		} catch(SQLException e) {
 			System.out.print("Error Message: " + e.getMessage());
 		}
 		
 	}
 	public void insertEvent(float mTime, String mType, int mPerson, int mVehicle, int mFacility, int mLink, String mActtype, String mLegmode) {
 //	public void insertEvent(String mTime, String mType, String mPerson, String mVehicle, String mFacility, String mLink, String mLegmode) {
 		
 		try {
 			PreparedStatement prepStmt = null;
 		
 		// prepares prepared statement
 		prepStmt = this.Conn.prepareStatement("INSERT INTO eventLog SET `time`=" + mTime + ", `type`='" + mType + "', `person`=" + mPerson + ", vehicle=" + mVehicle + ", facility=" + mFacility + ", link=" + mLink + ", acttype='" + mActtype + "', legmode='" + mLegmode + "'");		
 		prepStmt.execute();
 //		prepStmt.ex
 		// closes the db connection
//		this.Conn.close();
 		
 		} catch(SQLException e) {
 			System.out.println("insertEvent: " + e.getMessage());
 		}
 		
 		
 	}
 	
	public void clearEventLog() {
		
		try {
			PreparedStatement prepStmt = null;
			
			prepStmt = this.Conn.prepareStatement("DELETE FROM eventlog;");
			prepStmt.execute();
			
		} catch(SQLException e) {
			System.out.println("clearEventLog: " +  e.getMessage());
		}
	}
	
 	public void closeConnection() {
		try {
			this.Conn.close();
		} catch (SQLException e) {
			System.out.println("closeConnection: " +  e.getMessage());
		}
 		
 	}
 
 }

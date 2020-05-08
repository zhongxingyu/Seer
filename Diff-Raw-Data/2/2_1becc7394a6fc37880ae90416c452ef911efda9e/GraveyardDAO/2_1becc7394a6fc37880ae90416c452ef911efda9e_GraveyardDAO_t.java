 package com.dpedu.graveyard;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 public class GraveyardDAO {
 	/**
 	 * Mysql connection
 	 */
 	private Connection _mysql;
 	
 	/**
 	 * Connect to the database
 	 * Set the timezone
 	 * Load the player ID cache
 	 */
 	public void connect( String hostname, String database, String username, String password, File dir )
 	{ 
 		try
 		{
 			Class.forName( "com.mysql.jdbc.Driver" ).newInstance();
 			this._mysql = DriverManager.getConnection( "jdbc:mysql://" + hostname + "/" + database, username, password );
 		} catch( Exception e ) { 
 			System.err.println( "Cannot connect to database server: " + e.getMessage() );
 			return;
 		}
 	}
 	
 	/**
 	 * Close the connection to the database
 	 */
 	public void disconnect( File dir )
 	{
 		if ( this._mysql == null ) return; // Ignore unopened connections
 		try {
 			this._mysql.close();
 		} catch( Exception e ) {
 			System.err.println( "Cannot close database connection: " + e.getMessage() );
 		}
 	}
 	
 	
 	/**
 	 * Check if the player is dead or not
 	 * @param minecraftID
 	 * @return
 	 */
 	public boolean playerDead( String minecraftID )
 	{
 		if ( this._mysql == null ) return true;
 		try {
 			PreparedStatement s = this._mysql.prepareStatement( "SELECT `Status` FROM `player_status` WHERE `Username` = ? LIMIT 1" );
 			s.setString( 1, minecraftID );
 			ResultSet rs = s.executeQuery();
 			if ( !rs.last() ) return false;
 			if ( rs.getString( 1 ).equals( "Alive" ) ) return false;
 		} catch( SQLException e ) {
 			e.printStackTrace();
 		}
		return false; // Default case
 	}
 	
 
 }

 /*
  * 
  */
 package it.sod.open_politici_topics;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Properties;
 
 /**
  * TODO: Comment me!
  *
  * <dl><dt>date</dt><dd>Feb 7, 2013</dd></dl>
  * @author Marco Brandizi
  *
  */
 public class DbUtils
 {
 	private static Properties connProperties = null; 
 	
 	public static Properties getConnectionProperties ()
 	{
 		if ( connProperties != null ) return connProperties;
 		
 		try
 		{
 			connProperties = new Properties ();
 			ClassLoader loader = Thread.currentThread().getContextClassLoader();
 			InputStream propIn = loader.getResourceAsStream ( "/db.properties" );
 			if ( propIn == null ) propIn = loader.getResourceAsStream ( "db.properties" );
			connProperties.load ( loader.getResourceAsStream ( "db.properties" ) );
 			return connProperties;
 		} 
 		catch ( IOException ex ) {
 			throw new RuntimeException ( "Error while trying to load db.properties: " + ex.getMessage (), ex );
 		}
 
 	}
 	public static Connection createConnection()
 	{
 		getConnectionProperties ();
 		try
 		{
 			Class.forName ( connProperties.getProperty ( "connection.driver_class" ) );
 			return DriverManager.getConnection ( 
 				connProperties.getProperty ( "connection.url" ), 
 				connProperties.getProperty ( "connection.username" ), 
 				connProperties.getProperty ( "connection.password" ) 
 			);
 		} 
 		catch ( ClassNotFoundException ex ) {
 			throw new RuntimeException ( "Error while trying to load politician/topics data: " + ex.getMessage (), ex );
 		}
 		catch ( SQLException ex ) {
 			throw new RuntimeException ( "Error while trying to load politician/topics data: " + ex.getMessage (), ex );
 		}
 	}
 	
 	public static Connection resetDb ()
 	{
 		try
 		{
 			Connection conn = createConnection ();
 			Statement stmt = conn.createStatement ();
 			conn.rollback ();
 			stmt.execute ( "DROP TABLE IF EXISTS topics" );
 			stmt.execute ( "CREATE TABLE topics ( id INT AUTO_INCREMENT, twitter VARCHAR(30), topic VARCHAR(255), weight INT )" );
 			stmt.execute ( "CREATE INDEX twitter ON topics ( twitter )" );
 			stmt.execute ( "CREATE INDEX topic ON topics ( topic )" );
 			stmt.execute ( "CREATE INDEX weight ON topics ( weight )" );
 			conn.commit ();
 			return conn;
 		} 
 		catch ( SQLException ex ) {
 			throw new RuntimeException ( "Error while initialising the data DB: " + ex.getMessage (), ex );
 		}
 	}
 }

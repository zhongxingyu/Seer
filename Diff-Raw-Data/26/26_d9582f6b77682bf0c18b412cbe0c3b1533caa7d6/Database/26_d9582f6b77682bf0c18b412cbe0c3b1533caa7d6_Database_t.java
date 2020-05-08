 package net.maltera.daranable.edinet.model;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import org.hsqldb.cmdline.SqlFile;
 import org.hsqldb.cmdline.SqlToolError;
 
 public class Database {
 	private Connection connection;
 	
 	static {
 		System.setProperty( "edinet.appdata", System.getenv( "APPDATA" ) );
 	}
 	
 	public static class DatabaseVersionException
 	extends Exception {
 		private static final long serialVersionUID = 7912832417861134988L;
 
 		public DatabaseVersionException( String message ) {
 			super( message );
 		}
 	}
 	
 	public Database() 
 	throws DatabaseVersionException, SQLException, IOException {
		if (!new File( getDbPath() + ".script" ).isFile()) {
			connection = DriverManager.getConnection(
					"jdbc:hsqldb:file:" + getDbPath() );
			loadSchema();
			return;
		}
	
		connection = DriverManager.getConnection(
				"jdbc:hsqldb:file:" + getDbPath() + ";create=false" );
	
 		try {
 			final ResultSet result =
 					connection.createStatement().executeQuery(
 							"SELECT value \n" +
 							"FROM properties \n" +
							"WHERE name='schema.version'" );
 			
 			if (!result.next())
 				throw new DatabaseVersionException(
 						"the database exists but is an unknown version" );
 			
 			final String version = result.getString( "value" );
 			if (!"0".equals( version ))
 				throw new DatabaseVersionException(
 						"database is version " + version + ", expected 0" );
 		} catch (SQLException caught) {
			throw new DatabaseVersionException(
					"the database exists but uses an unknown schema" );
 		}
 	}
	
	private static String getDbPath()
	throws FileNotFoundException {
		return getDataDir().getPath() + "/edinet";
	}
 		
 	private static File getDataDir() 
 	throws FileNotFoundException {
 		File homeDir;
 		
 		if ( System.getProperty( "os.name" ).contains( "Windows" ) ) {
 			homeDir = new File( System.getProperty( "edinet.appdata" ) );
 			if ( !homeDir.isDirectory() )
 				throw new FileNotFoundException( 
 						"user's Application Data directory "
 						+ " does not exist or is not a directory" );
 			
 			homeDir = new File( homeDir, "Edinet" );
 		} else {
 			homeDir = new File( System.getProperty( "user.home" ) );
 			if ( !homeDir.isDirectory() )
 				throw new FileNotFoundException( "user's home directory"
 						+ " does not exist or is not a directory" );
 			
 			homeDir = new File( homeDir, ".edinet" );
 		}
 		
 		homeDir.mkdir();
 		
 		return homeDir;
 	}
 	
 	private void loadSchema()
 	throws IOException, SQLException {
 		final ClassLoader loader = Database.class.getClassLoader();
 		final InputStream stream = loader.getResourceAsStream(
 				"net/maltera/daranable/edinet/schema-hsql.sql" );
 		
 		if (null == stream) throw new FileNotFoundException(
 				"unable to locate schema file in resources" );
 		
 		final Reader reader = new InputStreamReader(
 				new BufferedInputStream( stream ), "UTF-8" );
 		
 		final SqlFile schema = new SqlFile(
 				reader, "schema-hsql.sql", null, "UTF-8", false, null );
 		schema.setConnection( connection );
 		
 		try {
 			schema.execute();
 		} catch (SqlToolError caught) {
 			throw new SQLException( "error handling schema file", caught );
 		}
 		
 		schema.closeReader();
 	}
 	
 	public Connection getConnection() {
 		return connection;
 	}
 	
 	public void shutdown()
 	throws SQLException {
 		connection.createStatement().execute( "SHUTDOWN" );
 		connection.close();
 	}
 }

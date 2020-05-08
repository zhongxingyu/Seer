 package org.pmsp;
 
 import static org.pmsp.PMSP_Constants.DATA_DIR_KEY;
 import static org.pmsp.PMSP_Constants.IMPLEMENTATION_KEY;
 import static org.pmsp.PMSP_Constants.LISTEN_HOST_KEY;
 import static org.pmsp.PMSP_Constants.LISTEN_PORT_KEY;
 import static org.pmsp.PMSP_Constants.PROPERTIES_FILE_KEY;
 
 import java.io.FileInputStream;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.sql.DatabaseMetaData;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.pmsp.domain.AudioFile;
 import org.pmsp.domain.FileListRequest;
 import org.pmsp.domain.ListCriteria;
 import org.pmsp.domain.Listing;
 import org.pmsp.domain.LoginRequest;
 import org.pmsp.domain.LogoffRequest;
 import org.pmsp.domain.MediaFile;
 import org.pmsp.domain.MediaFileListing;
 import org.pmsp.domain.MediaMetadataListing;
 import org.pmsp.domain.MetadataListRequest;
 import org.pmsp.domain.Operation;
 import org.pmsp.domain.Retrieval;
 import org.pmsp.domain.RetrievalRequest;
 import org.simpleframework.http.core.Container;
 import org.simpleframework.http.core.ContainerServer;
 import org.simpleframework.transport.Server;
 import org.simpleframework.transport.connect.Connection;
 import org.simpleframework.transport.connect.SocketConnection;
 
 import com.thoughtworks.xstream.XStream;
 
 /*=========================Group/Course Information=========================
  * Group 1:  Adam Himes, Brian Huber, Colin McKenna, Josh Krupka
  * CS 544
  * Spring 2013
  * Drexel University
  * Final Project
  *==========================================================================*/
 
 /**
  * This class is responsible for configuring and launching the PMSP server.  
  */
 public class MediaServer {
 
 	private static final Properties defaultProps = new Properties();
 	static {
 		defaultProps.put(PMSP_Constants.SESSION_TIMEOUT_DURATION_KEY, "1200");
 		defaultProps.put(PMSP_Constants.DATA_DIR_KEY, ".");
 		defaultProps.put(PMSP_Constants.LISTEN_HOST_KEY, "0.0.0.0");
 		defaultProps.put(PMSP_Constants.LISTEN_PORT_KEY, "31415");
 		defaultProps.put(PMSP_Constants.LOGGING_CONFIG_KEY, "logger.properties");
 	}
 	
 	public static Properties props = new Properties(defaultProps);
 	private static final Logger logger = Logger.getLogger(MediaServer.class);
 	private static XStream parser = new XStream();
 	protected static Server server;
 	protected static Connection connection; 
 	
 	// Xstream docs recommend xml configuration is done once and done ahead of time
 	// this processes all the annotations so xstream knows how to load/build our xml docs
 	static {
 		@SuppressWarnings("rawtypes")
 		Class[] classes = new Class[] {Operation.class, ListCriteria.class, FileListRequest.class, RetrievalRequest.class, 
 				AudioFile.class, MediaFile.class, Retrieval.class, Listing.class, MediaMetadataListing.class, 
 				MediaFileListing.class, MetadataListRequest.class, LoginRequest.class, LogoffRequest.class};
 		parser.processAnnotations(classes);
 	}
 	
 	/**
 	 * Sets up the embedded database instance.  Creates the table if it does not exist.  Should only be called at 
 	 * application start up
 	 * @throws ClassNotFoundException
 	 * @throws SQLException
 	 */
 	private static void initDatabase() throws ClassNotFoundException, SQLException {
 		String driver = PMSP_Constants.DB_DRIVER_CLASS;
 		
 	    Class.forName(driver);
 		java.sql.Connection conn = null;
 		Statement s = null;
 		ResultSet rs = null;
 		try {
 			conn = getDbConnection();
 			s = conn.createStatement();
 			
 			//check to see if the music table (our only one at the time) exists
 			DatabaseMetaData dbmd = conn.getMetaData();
 			rs = dbmd.getTables(null, null, "MUSIC", null);
 			
 			//if not, create it
 			if(!rs.next()) {
 				s.execute("CREATE TABLE MUSIC(" +
 						"ID INTEGER NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
 						"GENRE VARCHAR(20) NOT NULL," +
 						"ARTIST VARCHAR(50) NOT NULL," +
 						"ALBUM VARCHAR(50) NOT NULL," +
 						"TITLE VARCHAR(50) NOT NULL," +
 						"FILE_NAME VARCHAR(2000) NOT NULL)");
 				logger.debug("MUSIC table created successfully");
 			}
 
 		}
 		//clean up all the resources
 		finally {
 			try {
 				rs.close();
 			}
 			catch (Throwable t){}
 			try {
 				s.close();
 			}
 			catch (Throwable t){}
 			try {
 				conn.close();
 			}
 			catch (Throwable t){}
 		}
 	}
 	
 	/**
 	 * Gets the sql connection for interacting with the db
 	 * @return sql connection
 	 * @throws SQLException
 	 */
 	public static java.sql.Connection getDbConnection() throws SQLException {
 		//use the DAT_DIR setting as the loc of the db, or the CWD dir if that value isn't set. 
 		String dbName = props.getProperty(DATA_DIR_KEY, ".") + System.getProperty("file.separator") + "pmsp-db";
 		
 		//specify we want to create the db if it doesn't exists
 		String connectionURL = "jdbc:derby:" + dbName + ";create=true";
 	    return(DriverManager.getConnection(connectionURL));
 	}	
 	
 	/**
 	 * A global xstream instance is used.  Xstream docs say that it is thread safe, the only thing not thread safe is 
 	 * the configuration, which is why that's done in a static block.
 	 * @return Xstream instance
 	 */
 	public static XStream getXmlParser() {
 		return parser;
 	}
 	
 	/**
 	 * Shutdown hook for closing the server resources.  This allows a kill command to be used for initiating server shutdown
 	 * It also makes sure that in the event of any kind of jvm termination, it will at least try to clean up the resources.
 	 * That way there are no sockets, db resources, etc left open.
 	 */
 	private static class ShutdownHook {
 		public void attachShutDownHook() {
 			Runtime.getRuntime().addShutdownHook(new Thread() {
 				public void run() {
 					//close the socket connection
 					if (connection != null) {
 						try {
 							connection.close();
 						} catch (Throwable t) {
 						}
 					}
 
 					//shut down the http server
 					if (server != null) {
 						try {
 							server.stop();
 						} catch (Throwable t) {
 						}
 					}
 					
 					//shut down the db server
 					try {
 						DriverManager.getConnection("jdbc:derby:;shutdown=true");
 					} catch (Throwable t) {
 					}
 					logger.info("Shut down is complete");
 				}
 			});
 		}
 	}
 	
 	/**
 	 * Main method used for launching the PMSP server
 	 * @param list
 	 * @throws Exception
 	 */
 	public static void main(String[] list) throws Exception {
 		
 		String propFile = System.getProperty(PROPERTIES_FILE_KEY, PROPERTIES_FILE_KEY);
 		
 		//configure the logger, default to "logger.properties" if no log config file is specified
 		PropertyConfigurator.configureAndWatch(props.getProperty(PMSP_Constants.LOGGING_CONFIG_KEY));
 		FileInputStream fis = null;
 		try {
 			fis = new FileInputStream(propFile);
 			props.load(fis);
 		}
 		finally {
 			fis.close();	
 		}
 		
 		//need to have configuration info... if it didn't load, have to exit
 		if (props == null || props.isEmpty()) {
 			logger.fatal("Error loading properties file, shutting down...");
 			System.exit(1);
 		}
 		
 		//set up the db
 		try {
 			initDatabase();
 			
 		}
 		catch (Exception e) {
 			logger.fatal("Error initializing database, shutting down...", e);
 			System.exit(2);
 		}
 		
 		//attach the shutdown hook
 		ShutdownHook hook = new ShutdownHook();
 		hook.attachShutDownHook();
 		
 		//The implementation class is configurable, would allow us to swap out implementations of the server 
 		Container container = (Container) Class.forName(props.getProperty(IMPLEMENTATION_KEY)).newInstance();
 		
 		//set up the Simple framework server
 		server = new ContainerServer(container);
 		connection = new SocketConnection(server);
 		
 		//default to the wildcard ip and port 31415 but both can be overridden in the config file
 		String host = props.getProperty(LISTEN_HOST_KEY);
 		int port = Integer.parseInt(props.getProperty(LISTEN_PORT_KEY));
 		SocketAddress address = new InetSocketAddress(host, port);
 
 		//do the bind
 		connection.connect(address);
 		
 	}
 }

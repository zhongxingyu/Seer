 package org.mdissjava.commonutils.mongo.db;
 
 import java.net.UnknownHostException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 
 /**
  * This is a singleton class
  * This class manages the connections between MongoDB and the client
  * 
  * Mongo DB manages the connection pool (default 10) so disconnections aren't needed
  * 
  * @author MDISS Java team 2011-2012 University of Deusto
  */
 public final class MongoDBConnection 
 {
 	private static MongoDBConnection instance = null;
 	private Mongo connection = null;
	String URL = null;
	int port = 0;
 	final Logger logger = LoggerFactory.getLogger(this.getClass());
 	
 	private MongoDBConnection()
 	{
 	}
 	
 	/**
 	 * Checks if there is an active connection between the client and MongoDB server instance
 	 */
 	/*private void checkConnetion()
 	{
 		if (connection == null)
 			throw new IllegalStateException("The connection to the database hasn't been created yet.");
 	}*/
 	
 	/**
 	 * Static method that returns the instance of this class
 	 * 
 	 * @Return Instance of this Class (MongoDBConnection)
 	 */
 	public static MongoDBConnection getInstance()
 	{
 		if (MongoDBConnection.instance == null)
 		{
 			MongoDBConnection.instance = new MongoDBConnection();
 		}
 		
 		return MongoDBConnection.instance;
 	}
 	
 	/**
 	 * This method creates a connection between client and MongoDB database instance server
 	 * 
 	 * @param URL URL where MongoDB instance is listening
 	 * @param port Port number where MongoDB instance is listening
 	 * @throws UnknownHostException If the host isn't reachable this exception will be thrown
 	 * @throws MongoException If during the connection or disconnection time something goes wrong this exception will be thrown 
 	 */
 	public void connect(String URL, int port) throws UnknownHostException, MongoException
 	{
 		/*if (this.connection != null)
 		{
 			disconnect();
 		}
 		this.connection = new Mongo(URL, port);*/
 		
 		//only one object needed for all the application (pool is automanged)
		//if the connection URI is different or null then create a new different connection but don't close 
		//the previous one (maybe there are active connections )
		if(this.connection == null || !this.URL.equals(URL) || this.port != port)
 		{
 			this.logger.info("New Mongo connection {}:{}", URL, port);
 			this.connection = new Mongo(URL, port);
			this.URL = URL;
			this.port = port;
 		}
		else
			this.logger.info("Using previous connection {}:{}", this.URL, this.port);
 		
 	}
 	
 	/**
 	 * Frees the instance of this singleton class
 	 */
 	public static void releaseInstance()
 	{
 		MongoDBConnection.instance = null;
 	}
 	
 	/**
 	 * Connects to MongoDB database instance server with the default local URL and default port
 	 * 
 	 * @throws UnknownHostException If the host isn't reachable this exception will be thrown
 	 * @throws MongoException If during the connection or disconnection time something goes wrong this exception will be thrown 
 	 */
 	public void connect() throws UnknownHostException, MongoException
 	{
 		this.connect("127.0.0.1", 27017);
 	}
 	
 	/**
 	 * Disconnects the actual connection between client and MongoDB server instance
 	 * 
 	 * Not needed!!
 	 */
 	/*public void disconnect()
 	{
 		//not needed auto managed
 		this.checkConnetion();
 		this.connection.close();
 		this.connection = null;
 	}
 	*/
 	
 	/**
 	 * Retrieves the connection of between the client and MongoDB server instance
 	 * 
 	 * @return Connection instance of Mongo
 	 */
 	public Mongo getConnection()
 	{
 		if (connection == null)
 		{
 			this.logger.error("Not connection avaiable");
 			throw new IllegalStateException("Not connexion avaiable. connect to MongoDB first");
 		}
 		
 		return this.connection;
 	}
 }

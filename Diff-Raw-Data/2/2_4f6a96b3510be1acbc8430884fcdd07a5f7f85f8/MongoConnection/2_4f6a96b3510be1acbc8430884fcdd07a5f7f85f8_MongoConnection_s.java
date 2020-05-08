 package de.hpi.fgis.database.mongodb;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 import com.mongodb.MongoOptions;
 import com.mongodb.ServerAddress;
 
 import de.hpi.fgis.database.NoConnectionException;
 
 /**
  * simple MongoDB connection singleton
  * 
  * @author tongr
  * 
  */
 public class MongoConnection implements Closeable {
 	private static final Logger logger = Logger
			.getLogger(Logger.GLOBAL_LOGGER_NAME);
 
 	private static MongoConnection CURRENT_INSTANCE = null;
 
 	/**
 	 * the {@link MongoConnection} singleton (initially loads db config from
 	 * "mongodb.private.conf"-resource)
 	 * 
 	 * @return
 	 */
 	public synchronized static MongoConnection getInstance() {
 		if (CURRENT_INSTANCE == null) {
 			getInstance("/mongodb.private.conf");
 		}
 		return CURRENT_INSTANCE;
 	}
 
 	public synchronized static void tryClose() {
 		if (CURRENT_INSTANCE != null) {
 			CURRENT_INSTANCE.close();
 		}
 	}
 
 	/**
 	 * {@link MongoConnection} factory
 	 * 
 	 * @param configFile
 	 *            the db config file resource
 	 * @return a new {@link MongoConnection} instance
 	 */
 	public synchronized static MongoConnection getInstance(String configFile) {
 		// load cfg file
 		Properties prop = new Properties();
 
 		try {
 			// load a properties file
 			prop.load(MongoConnection.class.getResourceAsStream(configFile));
 
 		} catch (IOException ex) {
 			logger.log(Level.SEVERE,
 					"Unable to read MongoDB configuration file!", ex);
 
 			throw new NoConnectionException(ex);
 		}
 
 		return getInstance(prop.getProperty("host", "localhost"),
 				Integer.parseInt(prop.getProperty("port", "27017")),
 				prop.getProperty("database", "db"),
 				prop.getProperty("user", null),
 				prop.getProperty("password", null),
 				Integer.parseInt(prop.getProperty("batch_size", "10000")));
 	}
 
 	/**
 	 * {@link MongoConnection} factory
 	 * 
 	 * @param host
 	 *            the db host
 	 * @param port
 	 *            the db port
 	 * @param dbName
 	 *            the db name
 	 * @param user
 	 *            the username
 	 * @param pw
 	 *            the password
 	 * @return a new {@link MongoConnection} instance
 	 */
 	public synchronized static MongoConnection getInstance(String host,
 			int port, String dbName, String user, String pw) {
 		return CURRENT_INSTANCE = new MongoConnection(host, port, dbName,
 				10000, user, pw);
 	}
 
 	/**
 	 * {@link MongoConnection} factory
 	 * 
 	 * @param host
 	 *            the db host
 	 * @param port
 	 *            the db port
 	 * @param dbName
 	 *            the db name
 	 * @param user
 	 *            the username
 	 * @param pw
 	 *            the password
 	 * @param batchSize
 	 *            th edefault batch size
 	 * @return a new {@link MongoConnection} instance
 	 */
 	public synchronized static MongoConnection getInstance(String host,
 			int port, String dbName, String user, String pw, int batchSize) {
 		return CURRENT_INSTANCE = new MongoConnection(host, port, dbName,
 				batchSize, user, pw);
 	}
 
 	private final DB db;
 	private int maxBatchSize;
 
 	/**
 	 * gets the default batch size for bulk inserts
 	 * 
 	 * @return the default batch size for bulk inserts
 	 */
 	public int getMaxBatchSize() {
 		return this.maxBatchSize;
 	}
 
 	/**
 	 * sets the default batch size for bulk inserts
 	 * 
 	 * @param maxBatchSize
 	 *            the batch size
 	 * @return this instance
 	 */
 	public MongoConnection setMaxBatchSize(int maxBatchSize) {
 		this.maxBatchSize = maxBatchSize;
 		return this;
 	}
 
 	protected MongoConnection(String host, int port, String dbName,
 			int maxBatchSize, String user, String pw) {
 		try {
 			logger.fine("Connectiong to MongoDB ...");
 			MongoOptions mongoOpt = new MongoOptions();
 			mongoOpt.connectionsPerHost = 25;
 			Mongo mongo = new Mongo(new ServerAddress(host, port), mongoOpt);
 
 			DB db = mongo.getDB(dbName);
 			if (user != null && pw != null) {
 				if (!db.authenticate(user, pw.toCharArray())) {
 					throw new NoConnectionException("Unable to connect to "
 							+ dbName + "@" + host + ":" + port
 							+ " with credentials for \"" + user + "\"");
 				}
 			}
 
 			logger.fine("MongoDB connection established!");
 			this.db = db;
 
 			this.maxBatchSize = maxBatchSize;
 		} catch (UnknownHostException e) {
 			logger.log(Level.SEVERE, "Unable to find host to " + host + ":"
 					+ port, e);
 			throw new NoConnectionException("Unable to find host to " + host
 					+ ":" + port, e);
 		} catch (MongoException e) {
 			logger.log(Level.SEVERE, "Unable to find " + dbName + "@" + host
 					+ ":" + port, e);
 			throw new NoConnectionException("Unable to find " + dbName + "@"
 					+ host + ":" + port, e);
 		}
 	}
 
 	/**
 	 * returns the {@link DBCollection}
 	 * 
 	 * @param collectionName
 	 *            the name of the collection to be returned
 	 * @return the {@link DBCollection} instance
 	 */
 	public DBCollection getCollection(String collectionName) {
 		DBCollection collection = this.db.getCollection(collectionName);
 		return collection;
 	}
 
 	/**
 	 * gets the underlying {@link DB} object
 	 */
 	public DB getDB() {
 		return this.db;
 	}
 
 	public void close() {
 		this.db.getMongo().close();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "MongoConnection [server=" + db.getMongo().getAddress()
 				+ ", db=" + db.getName() + "]";
 	}
 }

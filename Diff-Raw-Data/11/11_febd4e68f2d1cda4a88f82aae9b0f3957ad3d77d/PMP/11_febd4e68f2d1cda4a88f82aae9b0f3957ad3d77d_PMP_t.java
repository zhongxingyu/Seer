 package net.unikernel.npss.controller;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.CommandResult;
 import com.mongodb.DB;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.ejb.Singleton;
 import javax.ejb.Startup;
 
 /**
  * PMP = PMP Mongo Preprocessor
  * @author uko
  */
 @Singleton
 @Startup
 public class PMP
 {
 	/**
 	 * Used for size calculations. Determines what kind of size (raw data, whole storage(with meta-data) ext…).
 	 */
 	public enum SizeType
 	{
 		/**
 		 * Means that only the size of raw data should be returned
 		 */
 		DATA_SIZE ("size"),
 		/**
 		 * Means that whole storage size should be returned, it includes indexes and reserved space
 		 */
 		STORAGE_SIZE ("storageSize"),
 		/**
 		 * Means that only index size should be returned
 		 */
 		INDEX_SIZE ("totalIndexSize");
 		/**
 		 * A value of the enum (actually String)
 		 */
 		public final String value;
 		private SizeType(String value)
 		{
 			this.value = value;
 		}
 	};
 	
 	/**
 	 * Used to scale size in a range from bits to gigabytes
 	 */
 	public enum SizeUnit
 	{
 		/**
 		 * Scale size for a bit
 		 */
 		BIT (1/8),
 		/**
 		 * Scale size for a Byte
 		 */
 		BYTE (1),
 		/**
 		 * Scale size for a KiloByte
 		 */
 		KILOBYTE (1024),
 		/**
 		 * Scale size for a MegaByte
 		 */
 		MEGABYTE (1024*1024),
 		/**
 		 * Scale size for a GigaByte
 		 */
 		GIGABYTE (1024*1024*1024);
 		/**
 		 * A value of a scale
 		 */
 		public final double value;
 		private SizeUnit(double value)
 		{
 			this.value = value;
 		}
 	};
 	
 	private final String dbPropertiesFile = "/net/unikernel/npss/controller/resources/connection.props";
 	private Properties dbProperties;
 	
 	private Mongo connection;
 	/**
 	 * @return the db
 	 */
 	private DB getDb() throws BadLoginException
 	{
 		DB db = connection.getDB(dbProperties.getProperty("dbName"));
 		if(dbProperties.getProperty("dbAuth").equals("yes")&&!db.isAuthenticated())
 			if (!db.authenticate(dbProperties.getProperty("dbUser"), dbProperties.getProperty("dbPass").toCharArray()))
 				throw new BadLoginException();	
 		return db;
 	}
 	
 	public Properties getProperties()
 	{
 		Properties result = new Properties();
 		for(String key:dbProperties.stringPropertyNames())
 			result.setProperty(key, dbProperties.getProperty(key));
 		return result;
 	}
 	
 	public boolean validateProperties(Properties props)
 	{
		if(props.containsKey("dbURL")&&props.containsKey("dbName"))
		{
			if(props.containsKey("dbAuth")&&props.getProperty("dbAuth").equals("yes"))
			{
				if(props.containsKey("dbUser")&&props.containsKey("dbPass"))
						return true;
				return false;
			}
 			return true;
		}
 		return false;
 	}
 	
 //	public boolean setProperties(Properties props) throws UnknownHostException
 //	{
 //		if(validateProperties(props))
 //		{
 //			dbProperties=props;
 //			connection=new Mongo(dbProperties.getProperty("db.URL"));
 //			return true;
 //		}
 //		return false;
 //	}
 	
 	/**
 	 * Class that if used to group the data used to exactly determine a "value" in the database
 	 */
 	static public class CombiKey
 	{
 		/**
 		 * Value of task
 		 */
 		private String task;
 		/**
 		 * Value of factory
 		 */
 		private String factory;
 		/**
 		 * Map of parameters
 		 */
 		private TreeMap<String, String> parameters;
 		/**
 		 * Creates an instance with an empty fields
 		 */
 		public CombiKey()
 		{
 			task="";
 			factory="";
 			parameters=new TreeMap<String, String>();
 		}
 		/**
 		 * Creates a new instance and assigns them passed data
 		 * @param task value of task
 		 * @param factory value of factory 
 		 * @param parameters map of parameters
 		 */
 		public CombiKey(String task, String factory, TreeMap<String, String> parameters)
 		{
 			this.task = task;
 			this.factory = factory;
 			this.parameters = parameters;
 		}
 		/**
 		 * @return the task
 		 */
 		public String getTask()
 		{
 			return task;
 		}
 		/**
 		 * @param task the task to set
 		 */
 		public void setTask(String task)
 		{
 			this.task = task;
 		}
 		/**
 		 * @return the factory
 		 */
 		public String getFactory()
 		{
 			return factory;
 		}
 		/**
 		 * @param factory the factory to set
 		 */
 		public void setFactory(String factory)
 		{
 			this.factory = factory;
 		}
 		/**
 		 * @return the parameters
 		 */
 		public TreeMap<String, String> getParameters()
 		{
 			return parameters;
 		}
 		/**
 		 * @param parameters the parameters to set
 		 */
 		public void setParameters(TreeMap<String, String> parameters)
 		{
 			this.parameters = parameters;
 		}
 		/**
 		 * Adds a parameter to the parameters field
 		 * @param name parameters name (key that maps the value of a parameter)
 		 * @param value a value of the parameter
 		 */
 		public void addParameter(String name,String value)
 		{
 			parameters.put(name, value);
 		}
 	}
 
 	/**
 	 * Constructs PMP instance
 	 */
 	public PMP()
 	{
 	}
 	
 	/**
 	 * initializes instance, creating a database connection
 	 * @throws MongoException exception regarding some mongodb stuff
 	 * @throws UnknownHostException as far as the class name can give me an idea, it's all about dns resolving
 	 * @throws net.unikernel.npss.model.PMP.BadLoginException when a bad login or password is entered the exception is thrown
 	 */
 	@PostConstruct
 	public void init() throws MongoException, UnknownHostException, BadLoginException, IOException, WrongPropertiesException
 	{
 		getClass().getResource(dbPropertiesFile);
 		dbProperties=new Properties();
 		dbProperties.load(getClass().getResourceAsStream(dbPropertiesFile));
 		if(!validateProperties(dbProperties))
 		{
 			throw new WrongPropertiesException();
 		}
 		connection = new Mongo(dbProperties.getProperty("dbURL"));
 	}
 	
 	/**
 	 * At the end of instances life db connection is closed
 	 */
 	@PreDestroy
 	public void dispose()
 	{
 		connection.close();
 	}
 	/**
 	 * Creates new document in the database
 	 * @param task value of a task
 	 * @param factory value of a factory
 	 * @param parameters map of parameters
 	 * @param value main value to store (the result of calculation)
 	 * @return returns true if the document is successfully created and false if not (in most of situations document already exists)
 	 * @throws MongoException exception regarding some mongodb stuff
 	 */
 	public boolean create(String task, String factory, Map<String, String> parameters, String value) throws MongoException, BadLoginException
 	{
 		DB db = getDb();
 		//db.requestStart();
 		if(!db.getCollectionNames().contains("st."+task + "." + factory))	
 			db.getCollection("st."+task + "." + factory).ensureIndex(new BasicDBObject("parameters", 1), "i", true);
 		BasicDBObject insert = new BasicDBObject();
 		insert.put("parameters", parameters);
 		insert.put("value", value);
 		if(db.getCollection("st."+task + "." + factory).insert(insert).getLastError().get("err")==null)
 		{
 			//db.requestDone();
 			return true;
 		}
 		else
 		{
 			//db.requestDone();
 			return false;
 		}
 	}
 	
 	/**
 	 * Creates new document in the database
 	 * @param key a "key" to create with
 	 * @param value main value to store (the result of calculation)
 	 * @return returns true if the document is successfully created and false if not (in most of situations document already exists)
 	 * @throws MongoException exception regarding some mongodb stuff
 	 */
 	public boolean create(CombiKey key, String value) throws MongoException, BadLoginException
 	{ 
 		DB db = getDb();
 		db.requestStart();
 		if(!db.getCollectionNames().contains("st."+key.task + "." + key.factory))
 		{
 			BasicDBObject index = new BasicDBObject();
 			index.put("parameters", 1);
 			index.put("unique", true);
 			index.put("name", "i");
 			db.getCollection("st."+key.getTask() + "." + key.getFactory()).ensureIndex(index);
 		}
 		BasicDBObject insert = new BasicDBObject();
 		insert.put("parameters", key.getParameters());
 		insert.put("value", value);
 		if(db.getCollection("st."+key.getTask() + "." + key.getFactory()).insert(insert).getLastError().get("err")==null)
 		{
 			db.requestDone();
 			return true;
 		}
 		else
 		{
 			db.requestDone();
 			return false;
 		}
 	}
 	/**
 	 * Returns a value that matches the "key"
 	 * @param task value of a task
 	 * @param factory value of a factory
 	 * @param parameters map of parameters
 	 * @return the result of calculation
 	 * @throws MongoException exception regarding some mongodb stuff
 	 */
 	public String read(String task, String factory, Map<String, String> parameters) throws MongoException, BadLoginException
 	{
 		DBObject val = getDb().getCollection("st."+task + "." + factory).findOne(new BasicDBObject("parameters", new BasicDBObject(parameters)));
 		if(val == null)
 			return null;
 		else
 			return (String) val.get("value");
 	}
 	/**
 	 * Returns a value that matches the "key"
 	 * @param key a "key" to get the mapped value
 	 * @return the result of calculation
 	 * @throws MongoException exception regarding some mongodb stuff
 	 */
 	public String read(CombiKey key) throws MongoException, BadLoginException
 	{
 		DB db = getDb();
 		DBObject val = getDb().getCollection("st."+key.getTask() + "." + key.getFactory()).findOne(new BasicDBObject("parameters", new BasicDBObject(key.getParameters())));
 		if(val == null)
 			return null;
 		else
 			return (String) val.get("value");
 	}
 	/**
 	 * Updates a value that matches the "key"
 	 * @param task value of a task
 	 * @param factory value of a factory
 	 * @param parameters map of parameters
 	 * @param value main value to update (the result of calculation)
 	 */
 	public void update(String task, String factory, Map<String, String> parameters, String value) throws BadLoginException
 	{
 		getDb().getCollection("st."+task + "." + factory).update(new BasicDBObject("parameters", new BasicDBObject(parameters)), new BasicDBObject("$set", new BasicDBObject("value", value)), false, false);
 	}
 	
 	/**
 	 * Updates a value that matches the "key"
 	 * @param key a "key" to update the mapped value
 	 * @param value main value to update (the result of calculation)
 	 */
 	public void update(CombiKey key, String value) throws BadLoginException
 	{
 		getDb().getCollection("st."+key.getTask() + "." + key.getFactory()).update(new BasicDBObject("parameters", new BasicDBObject(key.getParameters())), new BasicDBObject("$set", new BasicDBObject("value", value)), false, false);
 	}
 	/**
 	 * Deletes a value that matches the "key"
 	 * @param task value of a task
 	 * @param factory value of a factory
 	 * @param parameters map of parameters
 	 * @throws MongoException exception regarding some mongodb stuff
 	 */
 	public void delete(String task, String factory, Map<String, String> parameters) throws MongoException, BadLoginException
 	{
 		getDb().getCollection("st."+task + "." + factory).remove(new BasicDBObject("parameters", new BasicDBObject(parameters)));
 	}
 	
 	/**
 	 * Deletes a value that matches the "key"
 	 * @param key a "key" to delete the mapped value
 	 * @throws MongoException exception regarding some mongodb stuff
 	 */
 	public void delete(CombiKey key) throws MongoException, BadLoginException
 	{
 		getDb().getCollection("st."+key.getTask() + "." + key.getFactory()).remove(new BasicDBObject("parameters", new BasicDBObject(key.getParameters())));
 	}
 	
 	public TreeMap<String,TreeSet<String>> getStructure() throws BadLoginException
 	{
 		TreeMap<String,TreeSet<String>> result = new TreeMap<String,TreeSet<String>>();
 		for(String i :getDb().getCollectionNames())
 		{
 			String[] set = i.split("\\.");
 			if(set[0].equals("st"))
 			{
 				if(!result.containsKey(set[1]))
 				{
 					result.put(set[1], new TreeSet<String>());
 				}
 				((TreeSet<String>)result.get(set[1])).add(set[2]);
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Returns raw data size of specified task and factory in bytes
 	 * @param task value of task
 	 * @param factory value of factory
 	 * @return size itself
 	 */
 	public Double getSize(String task, String factory) throws BadLoginException
 	{
 		return getSize(task, factory, SizeType.DATA_SIZE, SizeUnit.BYTE);
 	}
 	
 	/**
 	 * Returns size of specified task and factory in bytes
 	 * @param task value of task
 	 * @param factory value of factory
 	 * @param sizeType an enum that specifies what size should be returned
 	 * @return size itself
 	 */
 	public Double getSize(String task, String factory, SizeType sizeType) throws BadLoginException
 	{
 		return getSize(task, factory, sizeType, SizeUnit.BYTE);
 	}
 	
 	/**
 	 * Returns raw data size of specified task and factory
 	 * @param task value of task
 	 * @param factory value of factory
 	 * @param sizeUnit an enum that specifies in what units size should be returned
 	 * @return size itself
 	 */
 	public Double getSize(String task, String factory, SizeUnit sizeUnit) throws BadLoginException
 	{
 		return getSize(task, factory, SizeType.DATA_SIZE, sizeUnit);
 	}
 	
 	/**
 	 * Returns size of specified task and factory
 	 * @param task value of task
 	 * @param factory value of factory
 	 * @param sizeType an enum that specifies what size should be returned
 	 * @param sizeUnit an enum that specifies in what units size should be returned
 	 * @return size itself
 	 */
 	public Double getSize(String task, String factory, SizeType sizeType, SizeUnit sizeUnit) throws BadLoginException
 	{
 		CommandResult result = getDb().getCollection("st."+task + "." + factory).getStats();
 		return new Double((Integer)result.get(sizeType.value))/sizeUnit.value;
 	}
 	
 	public void dropAll() throws BadLoginException
 	{
 		DB db = getDb();
 		db.requestStart();
 		for (String i : db.getCollectionNames())
 		{
 			if(i.substring(0, 3).equals("st."))
 			{
 				db.getCollection(i).drop();
 			}
 		}
 		db.requestDone();
 	}
 	
 	/**
 	 * Checked exception that is thrown when there's a problem with login/pass
 	 */
 	public static class BadLoginException extends Exception
 	{
 		
 	}
 	
 	/**
 	 * Checked exception that is thrown when there's a problem with properties format
 	 */
 	public static class WrongPropertiesException extends Exception
 	{
 		
 	}
 }

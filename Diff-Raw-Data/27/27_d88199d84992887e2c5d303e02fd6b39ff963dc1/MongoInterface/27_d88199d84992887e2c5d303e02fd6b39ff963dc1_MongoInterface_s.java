 package com.brotherlogic.memory.db;
 
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bson.types.ObjectId;
 
 import com.brotherlogic.memory.core.Memory;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 
 /**
  * DB Interface down to mongo
  * 
  * @author simon
  * 
  */
 public class MongoInterface extends DBInterface
 {
    /** The main database */
    private static final String DB_NAME = "memorybox";
 
    /** The test database */
    private static final String DB_NAME_TEST = "memorybox_test";
 
    /** The field name for setting references */
    private static final String REF_NAME = "ref_id";
 
    private final Logger logger = Logger.getLogger("com.brotherlogic.memory.db.MongoInterface");
 
    /** The underlying database */
    private DB mongo;
 
    /**
     * Clear the database
     * 
     * @throws IOException
     *            If we can't reach the database
     */
    @Override
    public final void clear() throws IOException
    {
       connect();
       mongo.dropDatabase();
    }
 
    /**
     * Connect to the database
     * 
     * @throws IOException
     *            if we can't connect
     */
    private void connect() throws IOException
    {
       if (mongo == null)
       {
          Mongo m = new Mongo();
 
          if (DBFactory.getMode() == DBFactory.Mode.PRODUCTION)
             mongo = m.getDB(DB_NAME);
          else if (DBFactory.getMode() == DBFactory.Mode.TESTING)
             mongo = m.getDB(DB_NAME_TEST);
          else
             throw new IOException("Unknown mode setting: " + DBFactory.getMode());
       }
    }
 
    /**
     * ENrich a memory with other lower level information
     * 
     * @param obj
     *           The Memory to enrich
     * @param id
     *           The id of the reference
     * @param properties
     *           The properties with which to enrich
     * @throws IOException
     *            If we can't reach the database
     */
    public final void enrichMemory(final Memory obj, final ObjectId id,
          final Map<String, Class<?>> properties) throws IOException
    {
       logger.log(Level.INFO, "Enriching " + obj.getClass() + " with " + properties);
 
       // Ignore the core memory properties!
       Set<Class<?>> classes = new HashSet<Class<?>>(properties.values());
       classes.remove(Memory.class);
       for (Class<?> cls : classes)
          setProperties(cls, obj, properties, id);
 
    }
 
    /**
     * Gets the collection for a given class
     * 
     * @param cls
     *           The class to get the collection
     * @return a valid mongo collection for this class
     * @throws IOException
     *            If we can't reach the database
     */
    protected DBCollection getCollection(final Class<?> cls) throws IOException
    {
       // Derive the name of the collection
       String colName = cls.getName().substring("com.brotherlogic.memory.core".length() + 1);
       return getCollection(colName);
    }
 
    /**
     * Gets a collection with a given name
     * 
     * @param name
     *           The String name for this collection
     * @return A valid Mongo collection for this given name
     * @throws IOException
     *            If we can't reach the database
     */
    protected DBCollection getCollection(final String name) throws IOException
    {
       // Make sure we have a mongo instance up
       connect();
 
       return mongo.getCollection(name);
    }
 
    @Override
    public DownloadQueue getDownloadQueue()
    {
       return new MongoDownloadQueue();
    }
 
    @Override
    public Memory retrieveLatestMemory(final Class<?> cls) throws IOException
    {
       // Have to work our way through the memories to find the first match; -1
       // means descending order
       DBObject querysort = new BasicDBObject();
       querysort.put("timestamp", -1);
       DBCollection col = getCollection(Memory.class);
       DBCursor cursor = col.find(new BasicDBObject()).sort(querysort);
       while (cursor.hasNext())
       {
          DBObject memoryObj = cursor.next();
          ObjectId refId = (ObjectId) memoryObj.get("_id");
 
          // Secondary query
          DBObject query = new BasicDBObject();
          query.put(REF_NAME, refId);
          DBCollection qCol = getCollection(cls);
          DBObject obj = qCol.findOne(query);
 
          if (obj != null)
             return retrieveMemory((Long) memoryObj.get("timestamp"), cls.getName());
       }
 
       return null;
    }
 
    @Override
    public Collection<Memory> retrieveMemories(Calendar day, String className) throws IOException
    {
       Collection<Memory> mems = new LinkedList<Memory>();
 
       DBCollection col = getCollection(Memory.class);
       Calendar query = Calendar.getInstance();
       query.setTimeInMillis(day.getTimeInMillis());
       query.set(Calendar.HOUR, 0);
       query.set(Calendar.MINUTE, 0);
       query.set(Calendar.SECOND, 0);
 
       long qStart = query.getTimeInMillis();
       query.add(Calendar.DAY_OF_YEAR, 1);
       long qEnd = query.getTimeInMillis();
 
       DBObject dbquery = new BasicDBObject();
       DBObject filter = new BasicDBObject();
       filter.put("$gt", qStart);
       filter.put("$lt", qEnd);
       dbquery.put("timestamp", filter);
       dbquery.put("memoryClass", className);
 
       DBCursor res = col.find(dbquery);
       while (res.hasNext())
       {
          DBObject obj = res.next();
          mems.add(retrieveMemory((Long) obj.get("timestamp"), className));
       }
       res.close();
 
       return mems;
    }
 
    @Override
    public Collection<Memory> retrieveMemories(Class cls) throws IOException
    {
       Collection<Memory> mems = new LinkedList<Memory>();
       DBCollection col = getCollection(Memory.class);
       DBObject query = new BasicDBObject();
       query.put("memoryClass", cls.getName());
       DBCursor cursor = col.find(query);
       while (cursor.hasNext())
       {
          DBObject obj = cursor.next();
          mems.add(retrieveMemory((Long) obj.get("timestamp"), cls.getName()));
       }
       cursor.close();
 
       return mems;
    }
 
    @Override
    public final Memory retrieveMemory(final long timestamp, final String className)
          throws IOException
    {
       try
       {
          // Create the given object
          Class<?> objType = Class.forName(className);
          Constructor<?> objCons = objType.getConstructor(new Class[0]);
          Memory memory = (Memory) objCons.newInstance(new Object[0]);
 
          // Get the ID number
          DBObject query = new BasicDBObject();
          query.put("timestamp", timestamp);
          DBObject res = getCollection(Memory.class).findOne(query);
 
          ObjectId id = (ObjectId) res.get("_id");
          memory.setTimestamp(timestamp);
          Map<String, Class<?>> properties = deriveProperties(memory);
          enrichMemory(memory, id, properties);
          return memory;
       }
       catch (ClassNotFoundException e)
       {
          throw new IOException(e);
       }
       catch (NoSuchMethodException e)
       {
          throw new IOException(e);
       }
       catch (InvocationTargetException e)
       {
          throw new IOException(e);
       }
       catch (IllegalAccessException e)
       {
          throw new IOException(e);
       }
       catch (InstantiationException e)
       {
          throw new IOException(e);
       }
    }
 
    /**
     * Sets the properties on a given class
     * 
     * @param cls
     *           The class name to set for
     * @param obj
     *           The object to set the properties on
     * @param properties
     *           A {@link Map} from the property name to the class that
     *           implements it
     * @param refId
     *           The underlying memory reference
     * @throws IOException
     *            If we can't reach the database
     */
    private void setProperties(final Class<?> cls, final Memory obj,
          final Map<String, Class<?>> properties, final ObjectId refId) throws IOException
    {
       logger.log(Level.INFO, "Setting properties " + cls + " given " + obj + " and " + refId);
 
       BasicDBObject query = new BasicDBObject();
       query.put(REF_NAME, refId);
       DBObject retObj = getCollection(cls).findOne(query);
 
       logger.log(Level.INFO, "Found object = " + retObj);
 
       // Match up these properties
       if (retObj != null)
          for (Entry<String, Class<?>> propEntry : properties.entrySet())
             if (propEntry.getValue().equals(cls))
                setProperty(obj, propEntry.getKey(), retObj.get(propEntry.getKey()));
    }
 
    /**
     * Store an object in the database
     * 
     * @param storeType
     *           The type to store
     * @param toStore
     *           The object to store
     * @param potProperties
     *           The properties of the object
     * @param refId
     *           The reference ID
     * @return Gets the underlying ID of the object
     * @throws IOException
     *            If we can't reach the databsae
     */
    private ObjectId store(final Class<?> storeType, final Object toStore,
          final Map<String, Class<?>> potProperties, final ObjectId refId) throws IOException
    {
       connect();
 
       DBCollection col = getCollection(storeType);
       BasicDBObject obj = new BasicDBObject();
 
       for (Entry<String, Class<?>> prop : potProperties.entrySet())
          if (prop.getValue().equals(storeType))
             obj.put(prop.getKey(), getObject(prop.getKey(), toStore));
 
       // Add the reference id if this isn't the base object
       if (refId != null)
          obj.put("ref_id", refId);
 
       // If this is the base class - check that we're not already added
       if (refId == null)
       {
          DBObject storedObj = col.findOne(obj);
          if (storedObj != null)
             return (ObjectId) storedObj.get("_id");
          else
          {
             col.insert(obj);
             return obj.getObjectId("_id");
          }
       }
       else
       {
          // Check that this referenced object isn't already stored
          DBObject refQ = new BasicDBObject();
          refQ.put("ref_id", refId);
          DBObject storedObj = col.findOne(refQ);
          if (storedObj != null)
          {
             for (String key : getAnnotatedProps(potProperties))
                if (storedObj.get(key) != null)
                   obj.put(key, storedObj.get(key));
             col.update(refQ, obj);
          }
          else
             col.insert(obj);
          return obj.getObjectId("_id");
       }
    }
 
    @Override
    public final void storeMemory(final Memory mem) throws IOException
    {
       Map<String, Class<?>> propertyMap = deriveProperties(mem);
 
       // Store the memory in the memory table
       ObjectId id = store(Memory.class, mem, propertyMap, null);
 
       // Work through the other objects - ignore the Memory core stuff
       Set<Class<?>> allClasses = new HashSet<Class<?>>(propertyMap.values());
       allClasses.remove(Memory.class);
       for (Class<?> storeClass : allClasses)
          store(storeClass, mem, propertyMap, id);
    }
 }

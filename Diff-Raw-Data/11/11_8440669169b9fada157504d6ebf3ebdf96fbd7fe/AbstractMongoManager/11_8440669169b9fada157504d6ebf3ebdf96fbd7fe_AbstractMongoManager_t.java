 package de.hpi.fgis.database.mongodb;
 
 import java.io.Closeable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.NoSuchElementException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoException;
 import com.mongodb.MongoException.CursorNotFound;
 
 /**
  * base implementation of MongoDB accessor
  * 
  * @author tongr
  *
  */
 public abstract class AbstractMongoManager implements Closeable {
 	private static final Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
 	protected final MongoConnection connection;
 	private final HashMap<DBCollection, ArrayList<DBObject>> bulks = new HashMap<DBCollection, ArrayList<DBObject>>();
 
 	public AbstractMongoManager(MongoConnection connection) {
 		super();
 		this.connection = connection;
 	}
 
 	/**
 	 * gets a bulk cursor with the specified skip offset
 	 * @param collection the collection to scan
 	 * @param skip the skip offset
 	 * @return a partial cursor or <code>null</code> if no further elements are available
 	 */
 	protected DBCursor nextBulkCursor(DBCollection collection, int skip) {
 		return this.nextBulkCursor(new BasicDBObject(), null, collection, skip);
 	}
 
 	/**
 	 * gets a bulk cursor with the specified skip offset
 	 * @param query the query to search for
 	 * @param collection the collection to scan
 	 * @param skip the skip offset
 	 * @return a partial cursor or <code>null</code> if no further elements are available
 	 */
 	protected DBCursor nextBulkCursor(DBObject query, DBCollection collection, int skip) {
 		return this.nextBulkCursor(query, null, collection, skip);
 	}
 
 	/**
 	 * gets a bulk cursor with the specified skip offset
 	 * @param query the query to search for
 	 * @param keys the keys to filter (null for all keys)
 	 * @param collection the collection to scan
 	 * @param skip the skip offset
 	 * @return a partial cursor or <code>null</code> if no further elements are available
 	 */
 	protected DBCursor nextBulkCursor(DBObject query, DBObject keys, DBCollection collection, int skip) {
 		DBCursor result = collection.find(query).limit(connection.getMaxBatchSize()).skip(skip);
 		if(!result.hasNext()) {
 			result.close();
 			return null;
 		}
 		return result;
 	}
 	protected void bulkInsert(DBCollection collection, DBObject... objects) {
 		ArrayList<DBObject> bulk;
 		synchronized (bulks) {
 			bulk = bulks.get(collection);
 			if(bulk==null) {
 				bulk = new ArrayList<DBObject>(connection.getMaxBatchSize());
 				bulks.put(collection, bulk);
 			}
 		}
 		synchronized (bulk) {
 			for(DBObject object : objects) {
 				bulk.add(object);
 				if(bulk.size()>=connection.getMaxBatchSize()) {
 	    			commitBulk(collection, bulk);
 	    		}
 			}	
 		}
 	}
 
 	private void commitBulk(DBCollection collection, ArrayList<DBObject> bulk) {
 		if(bulk==null || bulk.size()<=0) {
 			// nothing to do
 			return;
 		}
		ArrayList<DBObject> bulkCopy;
		// make a synchronized copy of the bulk
		synchronized (bulk) {
			bulkCopy = new ArrayList<>(bulk);
 			bulk.clear();
 		}
		// commit this bulk
		synchronized (collection) {
			collection.insert(bulkCopy);
		}
 	}
 	
 	/**
 	 * gets the underlying {@link MongoConnection} instance
 	 * @return the connection instance
 	 */
 	public MongoConnection getMongoConnection() {
 		return this.connection;
 	}
 
 	/**
 	 * commits all open transaction buffers
 	 */
 	public void commit() {
 		synchronized (bulks) {
 			for(Entry<DBCollection, ArrayList<DBObject>> entry : bulks.entrySet()) {
 				commitBulk(entry.getKey(), entry.getValue());
 			}
 		}
 	}
 	
 	/**
 	 * this method might clean the db collection(s) and frees the not yet committed change stack
 	 */
 	public void clean() {
 		synchronized (bulks) {
 			for(ArrayList<DBObject> bulk : bulks.values()) {
 				bulk.clear();
 			}
 		}
 	}
 	
 	/**
 	 * this method might drop the db collection(s)
 	 */
 	protected void drop() {
 	}
 
 	@Override
 	protected void finalize() throws Throwable {
 		this.commit();
 		super.finalize();
 	}
 
 	public void close() {
 		this.commit();
 	}
 
 	/**
 	 * a abstract generic instance iterator for mongo collections
 	 * @author tongr
 	 *
 	 * @param <T> the type to be returned by the inner iterator
 	 */
 	public abstract static class MongoIterable<T> implements Iterable<T> {
 		private final DBObject query;
 		private final DBCollection collection;
 		private int initialPosition;
 		public MongoIterable(DBObject query, DBCollection collection) {
 			this(query, collection, 0);
 		}
 		public MongoIterable(DBObject query, DBCollection collection, int initialPosition) {
 			super();
 			this.query = query;
 			this.collection = collection;
 			this.initialPosition = initialPosition;
 		}
 		public Iterator<T> iterator() {
 			return new Iterator<T>() {
 				private int cursorPosition = initialPosition;
 				private DBCursor cursor;
 				{
 					initCursor();
 				}
 				private void initCursor() {
 					cursor = collection.find(query).skip(cursorPosition);
 				}
 				public boolean hasNext() {
 					synchronized (cursor) {
 						try {
 							// get peek next object in cursor
 							return cursor.hasNext();
 						} catch (CursorNotFound e) {
 							logger.log(Level.WARNING, "Server closed cursor, re-initializing cursor ...", e);
 							try {
 								// cursor deleted on server -> retry once!
 								this.initCursor();
 								
 								return cursor.hasNext();
 							} catch (MongoException retryException) {
 								logger.log(Level.SEVERE, "Unable to re-initialize cursor to element " + cursorPosition + "!", retryException);
 								throw retryException;
 							}
 						}
 					}
 				}
 				public T next() {
 					DBObject dbGraph;
 					synchronized (cursor) {
 						// get next object from cursor
 						try {
 							dbGraph = cursor.next();
 						} catch (CursorNotFound e) {
 							logger.log(Level.WARNING, "Server closed cursor, re-initializing cursor ...", e);
 							try {
 								// cursor deleted on server -> retry once!
 								this.initCursor();
 								
 								dbGraph = cursor.next();
 							} catch (MongoException retryException) {
 								logger.log(Level.SEVERE, "Unable to re-initialize cursor to element " + cursorPosition + "!", retryException);
 								throw retryException;
 							}
 						}
 					}
 					if(dbGraph==null) {
 						throw new NoSuchElementException();
 					}
 					// increase the cursor position for broken connection backup
 					cursorPosition++;
 					
 					return transform(dbGraph);
 				}
 				public void remove() {
 					throw new IllegalStateException("Not yet implemented!");
 				}
 			};
 		}
 		protected abstract T transform(DBObject mongoRepresentation);
 	}
 }

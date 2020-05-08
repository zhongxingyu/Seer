 package com.sandeepmore.caching;
 
 import java.util.NoSuchElementException;
 
 import net.spy.memcached.MemcachedClient;
 import net.spy.memcached.OperationTimeoutException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.sandeepmore.pool.CachingPool;
 import com.sandeepmore.pool.ICachingPool;
 
 /**
  * API Used to Set, Get and Delete values from the cache pool. 
  * feel free to add other methods if you wish to.
  * Throws a {@link CachingException} if the Get and Delete operations fails.
  * <p>
  * @author Sandeep More
  * <p> Example usage </p>
  * <pre>
  * {@code
  *  	private CachingBroker cb = new CachingBroker(); 
  *  	
  *  	cb.set(key ,TTL, value); // set
  *  	cb.get(key);             // get
  *  	cb.delete(key);	         // delete
  * }
  * </pre>
  */
 public class CachingBroker implements ICachingBroker{
 	
	private static final Log logger = LogFactory.getLog(CachingBroker.class);
	
 	private ICachingPool deligatePool = null;
	private MemcachedClient client;
	
 
 	public CachingBroker () {
 		this.deligatePool = CachingPool.getInstance(); // get the session instance
 		if (this.deligatePool == null) {
 			logger.error("Retured a null instance for session caching pool");
 		}
 	}
 	
 	public CachingBroker (final ICachingPool deligatePool) {
 		this.deligatePool = deligatePool;
 	}
 	
 	/**
 	 * Set an object in the cache regardless of any existing value.
 	 * If the set fails exception will be logged silently.
 	 * <p>
 	 * @param key The key under which this object should be stored.
 	 * @param exp Time To Live value for the object in seconds.
 	 * @param obj Object value to be stored in the cache.
 	 */
 	public void set(String key, int exp, Object obj){
 		try {
 			// borrow an object from the pool to work on
 			logger.info("SET:Borrowing object from the pool"); 
 			client = (MemcachedClient)deligatePool.borrowObject();	
 			 //do the set operation
 			 client.set(key, exp, obj);
 			 //return the borrowed object back to pool
 			 deligatePool.returnObject(client);
 			 logger.info("SET:Returned object to the pool");
 		}catch (NoSuchElementException ex) {
 			// The pool is full
 			logger.error("Session pool full");
 			ex.printStackTrace();
 		}catch (Exception ex) {
 			logger.error("Set session cache failed for key:"+key);
 			ex.printStackTrace();
 		}	
 	}
 	
 	/**
 	 * Get the object from the cache with a single key.
 	 * If the key is not found a null value is returned. 
 	 * <p>
 	 * @param key Key of the object to be retrieved from the cache.
 	 * @throws CachingException 
 	 * @return The result from the cache (null if there is none).
 	 */
 	public Object get(String key) throws CachingException{
 		Object getValue = null;
 		try {
 			// borrow an object from the pool to work on
 			logger.info("GET:Borrowing object from the pool"); 
 			client = (MemcachedClient)deligatePool.borrowObject();	
 			 //do the get operation
 			 getValue = client.get(key);
 			 //return the borrowed object back to pool
 			 deligatePool.returnObject(client);
 			 logger.info("GET:Returned object to the pool");
 			 return getValue;
 		}catch (NoSuchElementException ex) {
 			// The pool is full, return null.
 			logger.error("Session pool full, get for key:"+key+" returning null ");
 			ex.printStackTrace();
 			return null; 
 		}catch (OperationTimeoutException ex) {
 			logger.error(" ********* Timed out while waiting for the connection ********** ");
 			ex.printStackTrace();
 			throw new CachingException(ex);
 		}catch (Exception ex) {
 			logger.error("Get for key:"+key+" returning null ");
 			ex.printStackTrace();
 			throw new CachingException(ex);
 		}	
 		
 	}
 	
 	/**
 	 * Delete the given key from the session cache. 
 	 * <p>
 	 * @param key Key of the object to be deleted from the cache.
 	 * @throws CachingException
 	 */
 	public void delete(String key) throws CachingException{
 		try {
 			// borrow an object from the pool to work on
 			logger.info("DELETE:Borrowing object from the pool"); 
 			client = (MemcachedClient)deligatePool.borrowObject();	
 			 //do the delete operation
 			 client.delete(key);
 			 //return the borrowed object back to pool
 			 deligatePool.returnObject(client);
 			 logger.info("DELETE:Returned object to the pool");
 		}catch (NoSuchElementException ex) {
 			// The pool is full
 			logger.error("Session pool full, delete for key:"+key+" unsuccessful");
 			ex.printStackTrace();
 		}catch (Exception ex) {
 			// The pool is full
 			logger.error("Delete for key:"+key+" unsuccessful");
 			ex.printStackTrace();
 			throw new CachingException(ex);
 		}
 		
 	}
 }

 package com.sandeepmore.pool;
 
 import java.util.ResourceBundle;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.pool.impl.GenericObjectPool;
 
 /**
  * CashingPool is a singelton class which creates the connection pools
  * The connections objects are of the type net.spy.memcached.MemcachedClient
  * The responsibility of this class is to create {@link #MAXACTIVE} number of connection pools 
  * <p>
  * @author Sandeep More
  *
  */
 public class CachingPool implements ICachingPool {
 
 	private static final Log logger = LogFactory.getLog(CachingPool.class);
 	private ResourceBundle props;
 	/**
 	 * Specifies number of objects in the pool 
 	 */
 	private static int MAXACTIVE; 	// Sets the cap on the number of objects that can be allocated by the pool.
 	/**
 	 * Specifies the max time in seconds the {@link CashingPool#borrowObject()} should block before quitting.
 	 */
 	private static int MAXWAIT; 	// Max time the borrowedObject() should block b4 throwing xception
 	
 	private static ICachingPool instance = null; // sessions class instance
	private static GenericObjectPool cachePool;
 	
 	/**
 	 * Initialize the  Cashing Connection pool.
 	 * sets the {@link #MAXACTIVE} and {@link #MAXWAIT} property for the connections.
 	 * @param cof Connection settings for caching bucket. 
 	 */
 	private CachingPool(CacheObjectFactory cof) {
 		logger.info("instantiating the Caching pool");
 		// create a genericObjectPool using CacheObjectFactory
 		// CacheObjectFactory has all the setting for the getting the cache
 		cachePool = new GenericObjectPool(cof);
 		
 		// get the properties
 		props = ResourceBundle.getBundle("pooling");
 		MAXACTIVE =  Integer.parseInt(props.getString("memcached.poolObjects"));
 		MAXWAIT   =  Integer.parseInt(props.getString("pool.MAXWAIT"));
 		
 		// initialize the pool properties
 		cachePool.setMaxActive(MAXACTIVE);
 		cachePool.setMaxWait(MAXWAIT);
 		cachePool.setWhenExhaustedAction(GenericObjectPool.WHEN_EXHAUSTED_FAIL); // Fail when the pool is empty		
 	}
 	
 	/**
 	 * Returns the instance of a singelton CashingPool class
 	 * <p>       
 	 * @return CashingPool instance
 	 */
 	public static synchronized ICachingPool getInstance() {
 		
 		// return the instance
 		if (instance == null) {
 			logger.info("Creating a new singelton instance: instance");
 			instance = new CachingPool(new CacheObjectFactory());
 		}
 		return instance;
 		
 	}
 
 	/** 
 	 * Obtains an instance from the pool.
 	 * <p>
 	 * @throws Exception 
 	 */
 	public Object borrowObject() {
 		try {
 			return cachePool.borrowObject();
 		} catch(Exception e) {
 			// TODO Do something here
 			e.printStackTrace(); // something bad happened
 			return null;
 		}
 	}
 
 	/** 
 	 * Return an instance to the pool.
 	 * <p>
 	 */
 	public void returnObject(Object obj) {
 		try {
 			cachePool.returnObject(obj);
 		} catch(Exception e) {
 			// TODO Do something here
 			e.printStackTrace();
 		}		
 	}
 	
 	/**
 	 * Just in case anyone tries to create a new instance ... stop them.
 	 */
 	public Object clone() throws CloneNotSupportedException {
 		throw new CloneNotSupportedException();
 	}
 	
 	
 }

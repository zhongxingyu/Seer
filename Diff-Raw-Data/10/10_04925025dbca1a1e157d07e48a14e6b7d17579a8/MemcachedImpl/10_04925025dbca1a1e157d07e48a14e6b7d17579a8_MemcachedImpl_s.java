 package com.snda.grand.space.as.memcached;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.spy.memcached.MemcachedClientIF;
 import net.spy.memcached.compat.log.Log4JLogger;
 
 /**
  * 
  * @author wangzijian
  * 
  */
public class MemcachedImpl implements Memcached {
 	
 	private static final Logger LOGGER = LoggerFactory.getLogger(MemcachedImpl.class);
 
 	static {
 		System.setProperty("net.spy.log.LoggerImpl", Log4JLogger.class.getName());
 	}
 	
 	private final MemcachedClientIF memcachedClient;
 	private final CacheConverter cacheConverter;
 
 	public MemcachedImpl(MemcachedClientIF memcachedClient, CacheConverter cacheConverter) {
 		this.cacheConverter = cacheConverter;
 		this.memcachedClient = memcachedClient;
 	}
 	
 	@Override
 	public void set(String memcachedId, Object object, int expiration) {
 		String encoded = cacheConverter.encode(object);
 		done("Set", memcachedClient.set(memcachedId, expiration, encoded));
 	}
 
 	@Override
 	public void delete(String memcachedId) {
 		done("Delete", memcachedClient.delete(memcachedId));
 	}
 
 	@Override
 	public <T> T get(Class<T> type, String memcachedId) {
 		String cached = (String) done("Get", memcachedClient.asyncGet(memcachedId));
 		LOGGER.info("Cache : {}", cached);
 		if (cached == null) {
 			return null;
 		}
 		return cacheConverter.decode(type, cached);
 	}
 	
 	private <T> T done(String operation, Future<T> future) {
 		try {
 			T result = future.get();
 			return result;
 		} catch (Exception e) {
 			Throwable cause = e instanceof ExecutionException ? e.getCause() : e;
 			cancel(future);
 			throw new MemcachedException(cause);
 		}
 		
 	}
 
 	private <T> void cancel(Future<T> future) {
 		// Since we don't need this, go ahead and cancel the operation.
 		// This is not strictly necessary, but it'll save some work on
 		// the server. It is okay to cancel it if running.
 		future.cancel(true);
 	}
 	
 }

 package org.mobicents.cache;
 
 import org.apache.log4j.Logger;
 import org.jboss.cache.Cache;
 import org.jboss.cache.CacheManager;
import org.jboss.cache.CacheStatus;
 import org.jboss.cache.DefaultCacheFactory;
 import org.jboss.cache.Fqn;
 import org.jboss.cache.Region;
 import org.jboss.cache.config.Configuration;
 import org.jboss.cache.config.Configuration.CacheMode;
 
 /**
  * The container's HA and FT data source.
  * 
  * @author martins
  * 
  */
 public class MobicentsCache {
 
 	private static Logger logger = Logger.getLogger(MobicentsCache.class);
 
 	private final Cache jBossCache;
 	private boolean localMode;
 	private final boolean managedCache;
 
 	@SuppressWarnings("unchecked")
 	public MobicentsCache(Configuration cacheConfiguration) {
 		this.jBossCache = new DefaultCacheFactory().createCache(cacheConfiguration,false);
 		this.managedCache = false;
 		startCache();	
 	}
 
 	@SuppressWarnings("unchecked")
 	public MobicentsCache(String cacheConfigurationLocation) {
 		this.jBossCache = new DefaultCacheFactory().createCache(cacheConfigurationLocation,false);
 		this.managedCache = false;
 		startCache();	
 	}
	@Deprecated
 	public MobicentsCache(CacheManager haCacheManager, String cacheName) throws Exception {
 		this.jBossCache = haCacheManager.getCache(cacheName, true);
 		this.jBossCache.create();
 		this.managedCache = true;
 		startCache();
 	}
 	
	public MobicentsCache(CacheManager haCacheManager, String cacheName, boolean managedCache) throws Exception {
		this.jBossCache = haCacheManager.getCache(cacheName, true);
		this.jBossCache.create();
		this.managedCache = managedCache;
		startCache();
	}
	
 	public MobicentsCache(Cache cache, String cacheName) {
 		this.jBossCache = cache;
 		this.managedCache = true;									
 		startCache();
 	}
 	
	protected void startCache() {
 		if (jBossCache.getConfiguration().getCacheMode() == CacheMode.LOCAL) {
 			localMode = true;
 		}
		if (!managedCache && !(CacheStatus.STARTED == jBossCache.getCacheStatus())) {
 			jBossCache.start();
 		}
 		if (logger.isInfoEnabled()) {
 			logger.info("Mobicents Cache started, status: " + this.jBossCache.getCacheStatus() + ", Mode: " + this.jBossCache.getConfiguration().getCacheModeString());
 		}
 	}
 	
 	public Cache getJBossCache() {
 		return jBossCache;
 	}
 	
 	public void stop() {
 		if (!managedCache) {
 			this.jBossCache.stop();
 			this.jBossCache.destroy();
 		}
 	}
 
 	/**
 	 * Indicates if the cache is not in a cluster environment. 
 	 * @return the localMode
 	 */
 	public boolean isLocalMode() {
 		return localMode;
 	}
 	
 	/**
 	 * Sets the class loader to be used on serialization operations, for data
 	 * stored in the specified fqn and child nodes. Note that if another class
 	 * loader is set for a specific child node tree, the cache will use instead
 	 * that class loader.
 	 * 
 	 * @param regionFqn
 	 * @param classLoader
 	 */
 	public void setReplicationClassLoader(Fqn regionFqn, ClassLoader classLoader) {
 		if (!isLocalMode()) {
 			final Region region = jBossCache.getRegion(regionFqn, true);
 			region.registerContextClassLoader(classLoader);
 			region.activate();
 		}
 	}
 	
 	/**
 	 * Sets the class loader to be used on serialization operations, for all
 	 * data stored. Note that if another class loader is set for a specific
 	 * child node tree, the cache will use instead that class loader.
 	 * 
 	 * @param classLoader
 	 */
 	public void setReplicationClassLoader(ClassLoader classLoader) {
 		setReplicationClassLoader(Fqn.ROOT, classLoader);
 	}
 	
 	/**
 	 * Unsets the class loader to be used on serialization operations, for data
 	 * stored in the specified fqn and child nodes.
 	 * @param regionFqn
 	 * @param classLoader
 	 */
 	public void unsetReplicationClassLoader(Fqn regionFqn, ClassLoader classLoader) {
 		if (!isLocalMode()) {
 			final Region region = jBossCache.getRegion(regionFqn, true);
 			region.unregisterContextClassLoader();
 			region.deactivate();
 			
 		}
 	}
 	
 	/**
 	 * Unsets the class loader to be used on serialization operations, for all
 	 * data stored.
 	 * @param classLoader
 	 */
 	public void unsetReplicationClassLoader(ClassLoader classLoader) {
 		unsetReplicationClassLoader(Fqn.ROOT,classLoader);
 	}
 }

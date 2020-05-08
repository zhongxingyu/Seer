 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2011, Red Hat, Inc. and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
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
 
 	@SuppressWarnings("rawtypes")
 	private final Cache jBossCache;
 	private boolean localMode;
 	private final boolean managedCache;
 
 	@SuppressWarnings("rawtypes")
 	public MobicentsCache(Configuration cacheConfiguration) {
 		this.jBossCache = new DefaultCacheFactory().createCache(cacheConfiguration,false);
 		this.managedCache = false;
 		setLocalMode();
 	}
 
 	@SuppressWarnings("rawtypes")
 	public MobicentsCache(String cacheConfigurationLocation) {
 		this.jBossCache = new DefaultCacheFactory().createCache(cacheConfigurationLocation,false);
 		this.managedCache = false;
 		setLocalMode();
 	}
 	
 	public MobicentsCache(CacheManager haCacheManager, String cacheName, boolean managedCache) throws Exception {
 		this.jBossCache = haCacheManager.getCache(cacheName, true);
 		this.jBossCache.create();
 		this.managedCache = managedCache;
 		setLocalMode();
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public MobicentsCache(Cache cache) {
 		this.jBossCache = cache;
 		this.managedCache = true;									
 		setLocalMode();
 	}
 	
 	private void setLocalMode() {
 		if (jBossCache.getConfiguration().getCacheMode() == CacheMode.LOCAL) {
 			localMode = true;
 		}
 	}
 	
 	public void startCache() {
 		if (!(CacheStatus.STARTED == jBossCache.getCacheStatus())) {
 			logger.info("Starting JBoss Cache...");
 			jBossCache.start();
 		}
 		if (logger.isInfoEnabled()) {
 			logger.info("Mobicents Cache started, status: " + this.jBossCache.getCacheStatus() + ", Mode: " + this.jBossCache.getConfiguration().getCacheModeString());
 		}
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public Cache getJBossCache() {
 		return jBossCache;
 	}
 	
 	public void stopCache() {
 		if (!managedCache) {
 			if (logger.isInfoEnabled()) {
 				logger.info("Mobicents Cache stopping...");
 			}			
 			this.jBossCache.stop();
 			this.jBossCache.destroy();
 		}
 		if (logger.isInfoEnabled()) {
 			logger.info("Mobicents Cache stopped.");
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
 	@SuppressWarnings("rawtypes")
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
 	@SuppressWarnings("rawtypes")
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

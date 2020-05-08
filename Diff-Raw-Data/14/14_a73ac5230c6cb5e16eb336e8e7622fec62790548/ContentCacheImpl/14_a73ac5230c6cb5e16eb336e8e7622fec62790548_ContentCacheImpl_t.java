 /*
  * Copyright (c) 2009 ioko365 Ltd
  *
  * This file is part of ioko tapestry-commons.
  *
  *     Foobar is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     Foobar is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with ioko tapestry-commons.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.co.ioko.tapestry.caching.services;
 
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Ehcache;
 import net.sf.ehcache.Element;
 import net.sf.ehcache.management.ManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import uk.co.ioko.tapestry.caching.services.support.CacheRegion;
 import uk.co.ioko.tapestry.caching.services.support.CachedContent;
 
 import javax.management.MBeanServer;
 import java.lang.management.ManagementFactory;
 import java.net.URL;
 
 /**
  * Provides support for retrieving cached content.  Note that cached content consists of two parts: a simple string
  * representing the plain text of the content after it was rendered and a list of cached method calls made to
  * RenderSupport.
  *
  * @author seldred
  */
 public class ContentCacheImpl implements ContentCache {
 
	private static final Logger logger = LoggerFactory.getLogger(ContentCacheImpl.class);

 	private static final String CACHE_NAME = "content";
 
 	private static final String CACHE_SEPARATOR = "-";
 
 	private CacheManager cacheManager;
 
 	public ContentCacheImpl() {
 		// create a new CacheManager from the config file
 		URL url = this.getClass().getResource("/ehcacheTapestryContent.xml");
 		if (url == null) {
 			url = this.getClass().getResource("/ehcacheTapestryContent-fallback.xml");
 		}
 		cacheManager = new CacheManager(url);
 		cacheManager.setName("TapestryContentCache");
		try {
			registerMBeans(cacheManager);
		} catch (net.sf.ehcache.CacheException e) {
			logger.warn(
					"Unable to register JMX. This is normal during unit testing, bad in production. Error is logged at debug level.");
			logger.debug("Unable to register JMX", e);
		}
 	}
 
 	private void registerMBeans(CacheManager cacheManager) {
 		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
 		ManagementService.registerMBeans(cacheManager, mBeanServer, true, true, true, true);
 	}
 
 
 	/**
 	 * Attempts to get the named content from the specified cache, where cache region is either "short", "medium" or
 	 * "long".  Returns null if no matching component is found.
 	 *
 	 * @param contentKey  the key for storing the content
 	 * @param cacheRegion either "short", "medium" or "long"
 	 * @return the cached content or null if not found
 	 */
 	public CachedContent getContent(String contentKey, CacheRegion cacheRegion) {
 		CachedContent content = null;
 		// check the cache
 		Ehcache cache = getCache(cacheRegion);
 		Element element = cache.get(contentKey);
 		if (element != null) {
 			content = (CachedContent) element.getValue();
 		}
 		return content;
 	}
 
 	/**
 	 * Adds the named content to the specified cache, where cache region is either "short", "medium" or "long".  The
 	 * content is keyed on the given key.
 	 *
 	 * @param contentKey  the key for storing the content
 	 * @param content	 the content to add
 	 * @param cacheRegion either "short", "medium" or "long"
 	 */
 	public void addContent(String contentKey, CachedContent content, CacheRegion cacheRegion) {
 		Ehcache cache = getCache(cacheRegion);
 		Element element = new Element(contentKey, content);
 		cache.put(element);
 	}
 
 	private Ehcache getCache(CacheRegion cacheRegion) {
 		String cacheName = CACHE_NAME + CACHE_SEPARATOR + cacheRegion.getCacheNameSuffix();
 		return cacheManager.getEhcache(cacheName);
 	}
 }

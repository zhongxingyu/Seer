 package nz.co.searchwellington.repositories;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 import nz.co.searchwellington.model.Snapshot;
 
 import org.apache.log4j.Logger;
 
 public class SnapshotDAO {
 		
 	private static final String SNAPSHOTS_CACHE_NAME = "snapshots";
     Logger log = Logger.getLogger(SnapshotDAO.class);
 
 	private CacheManager manager;
    
     
 	public SnapshotDAO(CacheManager manager) {		
 		this.manager = manager;
 	}
 
 	public Snapshot loadSnapshot(String url) {		
 		return getSnapshotFromCache(url);        
 	}
 	
 	public void saveSnapshot(String url, Snapshot snapshot) {
 	    putSyndFeedIntoCache(url, snapshot);
 	}
         
     private void putSyndFeedIntoCache(String url, Snapshot snapshot) {
 		Cache cache = manager.getCache(SNAPSHOTS_CACHE_NAME);
 		if (cache != null && snapshot != null) {
			Element cachedFeedElement = new Element(url, snapshot);
 			cache.put(cachedFeedElement);
			log.info("Caching snapshot for url: " + url);
 		}
 
 	}
 
     
     private Snapshot getSnapshotFromCache(String url) {
 		Cache cache = manager.getCache(SNAPSHOTS_CACHE_NAME);
 		if (cache != null) {
 			Element cacheElement = cache.get(url);
 			if (cacheElement != null) {
				Snapshot snapshot = (Snapshot) cacheElement.getObjectValue();
				log.info("Found snapshot in cache for url: " + url);
				return snapshot;
 			} else {
 				log.info("No cached snapshot found for url: " + url);
 			}
 		}
 		return null;
 	}
 
 }

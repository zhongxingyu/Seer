 package org.jasig.portlet.maps.dao;
 
 import javax.portlet.PortletRequest;
 
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.Element;
 
 import org.jasig.portlet.maps.model.xml.MapData;
 import org.springframework.beans.factory.annotation.Required;
 
 public abstract class AbstractPrefetchableMapDaoImpl implements IPrefetchableMapDao {
 
     protected final static String CACHE_KEY = "map";
     
     private Cache cache;
     
     /**
      * @param cache the cache to set
      */
     @Required
     public void setCache(Cache cache) {
         this.cache = cache;
     }
     
     public Cache getCache() {
         return this.cache;
     }
     
     private int prefetchInterval = 60;
     
     public void setPrefetchInterval(int minutes) {
         this.prefetchInterval = minutes;
     }
     
     public int getPrefetchInterval() {
         return this.prefetchInterval;
     }
 
     @Override
    public MapData prefetchMap(PortletRequest request) {
         final Element cachedMap = this.cache.get(CACHE_KEY);
         if (cachedMap != null) {
             return (MapData) cachedMap.getValue();
         } else {
             return prefetchMap();
         }
     }
     
 
 }

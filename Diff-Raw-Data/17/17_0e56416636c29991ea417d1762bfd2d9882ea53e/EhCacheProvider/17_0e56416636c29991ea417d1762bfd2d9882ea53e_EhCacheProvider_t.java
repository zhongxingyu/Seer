 package org.jboss.seam.cache;
 
 import static org.jboss.seam.annotations.Install.BUILT_IN;
 import static org.jboss.seam.ScopeType.APPLICATION;
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 
 import org.jboss.seam.annotations.AutoCreate;
 import org.jboss.seam.annotations.Create;
 import org.jboss.seam.annotations.Destroy;
 import org.jboss.seam.annotations.Install;
 import org.jboss.seam.annotations.Name;
 import org.jboss.seam.annotations.Scope;
 import org.jboss.seam.annotations.intercept.BypassInterceptors;
 import org.jboss.seam.log.LogProvider;
 import org.jboss.seam.log.Logging;
 
 /**
  * Implementation of CacheProvider backed by EhCache. The default cache region
  * issues <i>org.jboss.seam.cache.DefaultCache</> as the default cache region.
  * 
  * @author Sebastian Hennebrueder
  * @author Pete Muir
  */
 @Name("org.jboss.seam.cache.cacheProvider")
 @Scope(APPLICATION)
 @BypassInterceptors
 @Install(value = false, precedence = BUILT_IN, classDependencies="net.sf.ehcache.Cache")
 @AutoCreate
 public class EhCacheProvider extends CacheProvider<CacheManager>
 {
 
    private CacheManager cacheManager;
 
    private static final LogProvider log = Logging.getLogProvider(EhCacheProvider.class);
 
    @Override
    public CacheManager getDelegate()
    {
       return getCacheManager();
    }
 
    @Override
    public void put(String region, String key, Object object)
    {
       Cache cache = getCacheRegion(region);
       Element element = new Element(key, object);
       cache.put(element);
    }
 
    @Override
    public void clear()
    {
       String[] strings = getCacheManager().getCacheNames();
       for (String cacheName : strings)
       {
          Cache cache = getCacheRegion(cacheName);
          cache.removeAll();
       }
    }
 
    @Override
    public Object get(String region, String key)
    {
       Cache cache = getCacheRegion(region);
       Element element = cache.get(key);
       if (element != null)
       {
          return element.getObjectValue();
       }
       else
       {
          return null;
       }
    }
 
    private net.sf.ehcache.Cache getCacheRegion(String regionName)
    {
       if (regionName == null)
       {
          regionName = getDefaultRegion();
       }
       Cache result = getCacheManager().getCache(regionName);
       if (result == null)
       {
    	  synchronized (cacheManager){
    		  if (!cacheManager.cacheExists(regionName)) {
		          log.debug("Could not find configuration for region [" + regionName + "]; using defaults.");
		          cacheManager.addCache(regionName);
		          log.debug("EHCache region created: " + regionName);
    		  }
    	  }
           result = cacheManager.getCache(regionName);
       }
       return result;
    }
 
    @Override
    public void remove(String region, String key)
    {
       Cache cache = getCacheRegion(region);
       cache.remove(key);
    }
 
    @Create
    public void create()
    {
       log.debug("Starting EhCacheProvider cache");
       createCacheManager();
    }
 
 	private synchronized void createCacheManager() {
 		if (cacheManager == null) {
 			try
 		      {
 		         if (getConfiguration() != null)
 		         {
		            cacheManager = CacheManager.create(getConfigurationAsStream());
 		         }
 		         else
 		         {
		            cacheManager = CacheManager.create();
 		         }
 		      }
 		      catch (net.sf.ehcache.CacheException e)
 		      {
 		         throw new IllegalStateException("Error starting EHCache Cache", e);
 		      }
 		}
 	}
 
    private CacheManager getCacheManager() {
 	   if (cacheManager == null) {
 		   createCacheManager();
 	   }
 		return cacheManager;
 	}
 
 @Destroy
    public void destroy()
    {
       log.debug("Stopping EhCacheProvider cache");
 
       try
       {
     	  getCacheManager().shutdown();
          cacheManager = null;
       }
       catch (RuntimeException e)
       {
          throw new IllegalStateException("Error stopping EHCache Cache", e);
       }
    }
 
 }

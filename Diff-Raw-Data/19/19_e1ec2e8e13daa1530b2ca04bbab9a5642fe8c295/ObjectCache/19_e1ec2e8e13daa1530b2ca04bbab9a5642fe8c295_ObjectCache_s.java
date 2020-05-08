 package newsrack.database;
 
 import newsrack.GlobalConstants;
 import newsrack.user.User;
 import newsrack.archiver.Feed;
 import newsrack.archiver.Source;
 import newsrack.filter.Category;
 import newsrack.filter.Concept;
 import newsrack.filter.Filter;
 import newsrack.filter.Issue;
 
 import java.io.PrintWriter;
 import java.util.Properties;
 import java.util.Map;
 
 import com.opensymphony.oscache.base.*;
 import com.opensymphony.oscache.extra.*;
 import com.opensymphony.oscache.general.*;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class ObjectCache
 {
    private static Log _log = LogFactory.getLog(ObjectCache.class);
 
 	private class OCache
 	{
 		private String _name;
 		private GeneralCacheAdministrator _osCacheAdmin;
 
 		OCache(String name, Properties p) 
 		{ 
 			_name = name; 
 			_osCacheAdmin = new GeneralCacheAdministrator(p);
 		}
 
 		synchronized void add(String[] groups, String key, Object o)
 		{
 			if (_log.isDebugEnabled()) _log.debug(_name + " CACHE: adding key " + key);
 			if (groups == null)
 				_osCacheAdmin.putInCache(key, o);
 			else
 				_osCacheAdmin.putInCache(key, o, groups);
 		}
 
		void remove(String key)
		{
			_osCacheAdmin.removeEntry(key);
		}
 
		void removeGroup(String group)
		{
			_osCacheAdmin.flushGroup(group);
		}
 
 		void removeGroups(String[] groups)
 		{
 			for (String g: groups)
 				_osCacheAdmin.flushGroup(g);
 		}
 
		void clear()
		{
			_osCacheAdmin.flushAll();
		}
 
 		Object get(String key)
 		{
 				// @FIXME  Test efficiency
 				// We won't try to update the cache if we don't find an entry
 				// BUT, won't this lead to wasteful exceptions?
 			try {
 				return _osCacheAdmin.getFromCache(key);
 			}
 			catch (NeedsRefreshException nre) {
          	_osCacheAdmin.cancelUpdate(key);
 				return null;
 			}
 		}
 
 		void printStats(StringBuffer sb)
 		{
 			sb.append("#### CACHE STATS for " + _name + " ####").append("\n");
 			Object[] elList = _osCacheAdmin.getCache().getCacheEventListenerList().getListenerList();
 			for (int i = 0; i < elList.length; i++) {
 				Object el = elList[i];
 				if (el instanceof CacheEntryEventListenerImpl) {
 					sb.append("Cache Entry Event Listener Stats: ").append(el.toString()).append("\n");
 				}
 				else if (el instanceof CacheMapAccessEventListenerImpl) {
 					sb.append("Cache Map Access Event Listener Stats: ").append(el.toString()).append("\n");
 				}
 				else {
 					sb.append("OTHER stats: " + el.toString()).append("\n");
 				}
 			}
 		}
 	}
 
 	private Map<Class, OCache> _caches;	// Class-specific caches
 	private OCache _objectCache;			// Special catch-all cache
 
 	private OCache buildCache(String name, Class clazz, Properties p)
 	{
 		p.setProperty("cache.capacity", GlobalConstants.getProperty(name + ".cache.size"));
 		OCache c = new OCache(name.toUpperCase(), p);
 		_caches.put(clazz, c);
 		return c;
 	}
 
 	private void buildAllCaches()
 	{
 		_caches = new java.util.HashMap<Class, OCache>(10);
 
 		Properties p = new Properties();
 
 		// No disk persistence!
 		// p.setProperty("cache.persistence.class", "com.opensymphony.oscache.plugins.diskpersistence.HashDiskPersistenceListener");
 		// p.setProperty("cache.path", GlobalConstants.getProperty("cache.path"));
 
 		p.setProperty("cache.memory", "true");
 		p.setProperty("cache.event.listeners", "com.opensymphony.oscache.extra.CacheEntryEventListenerImpl, com.opensymphony.oscache.extra.CacheMapAccessEventListenerImpl");
 
 		buildCache("user", User.class, p);
 		buildCache("issue", Issue.class, p);
 		buildCache("feed", Feed.class, p);
 		buildCache("source", Source.class, p);
 		buildCache("category", Category.class, p);
 		buildCache("filter", Filter.class, p);
 		buildCache("newsitem", NewsItem.class, p);
 
 			// Lastly, a generic object cache
 		_objectCache  = buildCache("object", Object.class, p); 	// 10000
 	}
 
 	public ObjectCache()
 	{
 		buildAllCaches();
 	}
 
 	public void printStats(StringBuffer sb)
 	{
 		for (OCache c: _caches.values())
 			c.printStats(sb);
 	}
 
 	public void add(String[] cacheGroups, Object key, Class c, Object o)
 	{
 		String nKey;
 		OCache cache = _caches.get(c);
 
 		if (cache == null) {
 			nKey = Integer.toString(c.hashCode()) + ":" + key.toString();
 			cache = _objectCache;
 		}
 		else {
 			nKey = key.toString();
 		}
 
 		//_log.info("Adding " + o.hashCode() + " to cache " + cache.hashCode() + " for class " + c + " for key " + nKey);
 
 		cache.add(cacheGroups, nKey, o);
 	}
 
 	public void add(Long userKey, Object key, Class c, Object o)
 	{
 		String[] cacheGroups = null;
 		if (userKey != null)
 			cacheGroups = new String[]{userKey.toString()};
 
 		add(cacheGroups, key, c, o);
 	}
 
 /**
  * Problem with these is when different object types are added / queried 
  * for the same abstract class.  Ex: SQL_IssueStub vs. Issue; ArrayList vs. LinkedList
  *
 	public void add(Long userKey, Object key, Object o)
 	{
 		add(userKey, key, o.getClass(), o);
 	}
 
 	public void add(String[] cacheGroups, Object key, Object o)
 	{
 		add(cacheGroups, key, o.getClass(), o);
 	}
 **/
 
 	public Object get(Object key, Class c)
 	{
 		String nKey;
 		OCache cache = _caches.get(c);
 
 		if (cache == null) {
 			nKey = Integer.toString(c.hashCode()) + ":" + key.toString();
 			cache = _objectCache;
 		}
 		else {
 			nKey = key.toString();
 		}
 
 		Object o = cache.get(nKey);
 
 		//_log.info("Fetching " + ((o != null) ? o.hashCode() : "null") + " from cache " + cache.hashCode() + " for class " + c + " and key " + nKey);
 
 		return cache.get(nKey);
 	}
 
 	public void remove(Object key, Class c)
 	{
 		String nKey;
 		OCache cache = _caches.get(c);
 
 		if (cache == null) {
 			nKey = Integer.toString(c.hashCode()) + ":" + key.toString();
 			cache = _objectCache;
 		}
 		else {
 			nKey = key.toString();
 		}
 
 		cache.remove(nKey);
 	}
 
 	public void removeEntriesForGroups(String[] cacheGroups)
 	{
 		for (OCache c: _caches.values())
 			c.removeGroups(cacheGroups);
 	}
 
 	public void purgeCacheEntriesForUser(User u)
 	{
 		removeEntriesForGroups(new String[]{u.getUid(), u.getKey().toString()});
 	}
 
 	public void clearCaches()
 	{
 		Map<Class, OCache> oldCaches = _caches;
 
 			// Build all caches from scratch!
 		buildAllCaches();
 
 			// Destroy all the old caches
 		for (OCache c: oldCaches.values())
 			c.destroy();
 	}
 }

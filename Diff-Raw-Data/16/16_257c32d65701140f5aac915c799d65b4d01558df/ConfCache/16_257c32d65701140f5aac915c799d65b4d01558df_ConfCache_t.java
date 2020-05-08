 package confdb.converter;
 
 import java.lang.ref.SoftReference;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
import java.util.Iterator;
 import java.util.List;
 
 import confdb.data.IConfiguration;
 import confdb.db.ConfDB;
 import confdb.db.DatabaseException;
 
 public class ConfCache 
 {
 	static private ConfCache cache = null;
     static private int fillUpToMB = 40;
     static private int clearLessThanMB = 30;
 	
     private HashMap<String, ConfWrapper> confCache = new HashMap<String, ConfWrapper>();
 
     static public ConfCache getCache()
     {
     	if ( cache == null )
     		cache = new ConfCache();
     	return cache;
     }
 	
     static public int getNumberCacheEntries() 
     {
     	return getCache().confCache.size();
     }
 
     static public void clearCache()
     {
         getCache().confCache = new HashMap<String, ConfWrapper>();
     }
 	
     static public int checkSoftReferences() 
     {
     	return getCache().checkMySoftReferences();
     }
 
     public synchronized IConfiguration getConfiguration( int key, ConfDB database ) throws DatabaseException
     {
 		IConfiguration configuration = null;
 		ConfWrapper conf = confCache.get( getCacheKey( key, database ) );
 		if ( conf != null )
 		{
 			configuration = conf.getConfiguration();
 			if ( configuration != null )
 				return configuration;
 			confCache.remove( conf.getCacheKey() );
 		}
		//Runtime.getRuntime().gc();
 		try {
 			configuration = database.loadConfiguration(key);
 		} catch (DatabaseException e) {
 			// wait, reconnect and try again
 			database.disconnect();
 			try {
 				Thread.sleep( 2000 );
 			} catch (InterruptedException e1) {
 			}
 			database.connect();
 			configuration = database.loadConfiguration(key);
 		}
 		put( key, configuration, database );
 		return configuration;
     }
 		
     private void put( int key, IConfiguration conf, ConfDB database )
     {
     	Runtime runtime = Runtime.getRuntime();
     	float freeMemory = 
     		( runtime.maxMemory() - runtime.totalMemory() + runtime.freeMemory() ) / 1024 /1024;
     	if ( freeMemory < fillUpToMB )
 	    {
     		List<ConfWrapper> list = new ArrayList<ConfWrapper>( confCache.values() );
     		Collections.sort(list);
     		String key1 = list.get(0).getCacheKey();
         	if ( freeMemory < clearLessThanMB  &&  list.size() > 1 )
         		confCache.remove( list.get(1).getCacheKey() );
     		confCache.remove( key1 );
 	    }
     	confCache.put( getCacheKey( key,database ), new ConfWrapper( getCacheKey( key, database ), conf ) );
     }
 
     
 	private String getCacheKey( int key, ConfDB database )
 	{
 		return "" + key + "@" + database.hashCode();
 	}
 	
 	
     private synchronized int checkMySoftReferences() 
     {
     	int dereferenced = 0;
    	for ( Iterator<ConfWrapper> i = confCache.values().iterator(); i.hasNext(); )
     	{
    		if ( i.next().getConfiguration() == null )
     		{
     			dereferenced += 1;
    			i.remove();
     		}
     	}
     	return dereferenced;
     }
 
     private class ConfWrapper implements Comparable<ConfWrapper>
     {
     	private String cacheKey;
     	private SoftReference<IConfiguration> configuration = null;
     	private long timestamp;
 		
     	ConfWrapper( String key, IConfiguration conf ) 
     	{
     		cacheKey = key;
     		configuration = new SoftReference<IConfiguration>( conf );
     		timestamp = System.currentTimeMillis();
     	}
 
     	IConfiguration getConfiguration() 
     	{
     		timestamp = System.currentTimeMillis();
     		return configuration.get();
     	}
 
 
     	public int compareTo(ConfWrapper o) 
     	{
     		return (int)(timestamp - o.timestamp);
     	}
 		
     	public String getCacheKey()
     	{
     		return cacheKey;
     	}
     }
 
 
 	
 
 }

 package com.yahoo.ycsb.db;
 
 import com.yahoo.ycsb.Client;
 import com.yahoo.ycsb.DB;
 import com.yahoo.ycsb.DBException;
 import com.yahoo.ycsb.ByteIterator;
 import com.yahoo.ycsb.StringByteIterator;
 
 import org.infinispan.Cache;
 import org.infinispan.atomic.AtomicMap;
 import org.infinispan.atomic.AtomicMapLookup;
 import org.infinispan.manager.DefaultCacheManager;
 import org.infinispan.manager.EmbeddedCacheManager;
 import org.infinispan.remoting.transport.Transport;
 import org.infinispan.util.logging.Log;
 import org.infinispan.util.logging.LogFactory;
 
 import javax.transaction.SystemException;
 import javax.transaction.TransactionManager;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 /**
  * This is a client implementation for Infinispan 5.x.
  *
  * Some settings:
  *
  * @author Manik Surtani (manik AT jboss DOT org)
  */
 public class InfinispanClient extends DB {
 
 
 
     // An optimisation for clustered mode
     private final boolean clustered;
 
     private static EmbeddedCacheManager infinispanManager = null;
 
     private static Cache globalCache = null;
 
     private static TransactionManager tm = null;
 
     private static final Object syncObject = new Object(); 
 
     private static final Log logger = LogFactory.getLog(InfinispanClient.class);
 
     public InfinispanClient() {
 	clustered = Boolean.getBoolean("infinispan.clustered");
 
 	System.out.println("CLUSTERED: "+clustered);
     }
 
     public void init(int nodes) throws DBException {
 	try {
 	    synchronized (syncObject) {
 		if(infinispanManager == null){
 		    infinispanManager = new DefaultCacheManager("ispn.xml");
 
 		    String table = "usertable";
 		    globalCache = infinispanManager.getCache(table); 
 
 		    tm=globalCache.getAdvancedCache().getTransactionManager();
 		    
 		    
 		    Transport transport = infinispanManager.getTransport();
 		    while (transport.getMembers().size() < nodes) { 
 		        try {
                 Thread.sleep(1000);
             } catch (InterruptedException e) {
                 e.printStackTrace();
             } }
 		    Thread.sleep(5000);
 		    Client.NODE_INDEX = ((CustomHashing)globalCache.getAdvancedCache().getDistributionManager().getConsistentHash()).getMyId(infinispanManager.getTransport().getAddress());
 		    MagicKey.ADDRESS = infinispanManager.getTransport().getAddress();
 		    MagicKey.HASH = ((CustomHashing)globalCache.getAdvancedCache().getDistributionManager().getConsistentHash());
 		    MagicKey.OWNERS = globalCache.getAdvancedCache().getConfiguration().getNumOwners();
 		}
 	    }
 
 	} catch (IOException e) {
 	    throw new DBException(e);
 	} catch (InterruptedException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
     }
     }
 
     public void cleanup() {
 
 	synchronized (syncObject) {
 	    if(infinispanManager != null){
 		infinispanManager.stop();
 		infinispanManager = null;
 	    }
 	}
 
     }
 
     public void waitLoad(){
 	Object waitValue = null;
 
 	while(waitValue == null || ((Integer)waitValue) < 1){
 	    try{ 
 		waitValue = globalCache.get("Sebastiano_key");
 	    }
 	    catch(Exception e){
 
 		waitValue = null;
 	    }
 
 	}		
     }
 
     public void endLoad(){
 
 	boolean loaded = false;
 	while(!loaded){
 	    try{ 
 		globalCache.put("Sebastiano_key", infinispanManager.getTransport().getMembers().size());
 		loaded = true;
 	    }
 	    catch(Exception e){
 
 		loaded = false;
 	    }
 
 	}
 
     }
 
     public int read(String table, String key, Set<String> fields, HashMap<String, ByteIterator> result) {
 	try {
 	    Map<String, String> row;
 	    if (clustered) {
 		row = AtomicMapLookup.getAtomicMap(globalCache, key, false);
 	    } else {
 		Cache<Object, Map<String, String>> cache = globalCache;
 		row = cache.get(key);
 	    }
 	    if (row != null) {
 		result.clear();
 		if (fields == null || fields.isEmpty()) {
 		    StringByteIterator.putAllAsByteIterators(result, row);
 		} else {
 		    for (String field : fields) result.put(field, new StringByteIterator(row.get(field)));
 		}
 	    }
 	    return OK;
 	} catch (Exception e) {
 	    return ERROR;
 	}
     }
 
     public int read(MagicKey key, Set<String> fields, HashMap<String, ByteIterator> result) {
 	try {
 	    Map<String, String> row;
 	    if (clustered) {
 		row = AtomicMapLookup.getAtomicMap(globalCache, key, false);
 	    } else {
 		Cache<Object, Map<String, String>> cache = globalCache;
 		row = cache.get(key);
 	    }
 	    if (row != null) {
 		result.clear();
 		if (fields == null || fields.isEmpty()) {
 		    StringByteIterator.putAllAsByteIterators(result, row);
 		} else {
 		    for (String field : fields) result.put(field, new StringByteIterator(row.get(field)));
 		}
 	    }
 	    return OK;
 	} catch (Exception e) {
 	    return ERROR;
 	}
     }
 
     public int scan(String table, String startkey, int recordcount, Set<String> fields, Vector<HashMap<String, ByteIterator>> result) {
 	logger.warn("Infinispan does not support scan semantics");
 	return OK;
     }
 
     public int update(String table, String key, HashMap<String, ByteIterator> values) {
 	try {
 	    if (clustered) {
 		AtomicMap<String, String> row = AtomicMapLookup.getAtomicMap(globalCache, key);
 		StringByteIterator.putAllAsStrings(row, values);
 	    } else {
 		Cache<Object, Map<String, String>> cache = globalCache;
 		Map<String, String> row = cache.get(key);
 		if (row == null) {
 		    row = StringByteIterator.getStringMap(values);
 		    cache.put(key, row);
 		} else {
 		    StringByteIterator.putAllAsStrings(row, values);
 		}
 	    }
 
 	    return OK;
 	} catch (Exception e) {
 	    return ERROR;
 	}
     }
     
     public int update(MagicKey key, HashMap<String, ByteIterator> values) {
 	try {
 	    if (clustered) {
 		AtomicMap<String, String> row = AtomicMapLookup.getAtomicMap(globalCache, key);
 		StringByteIterator.putAllAsStrings(row, values);
 	    } else {
 		Cache<Object, Map<String, String>> cache = globalCache;
 		Map<String, String> row = cache.get(key);
 		if (row == null) {
 		    row = StringByteIterator.getStringMap(values);
 		    cache.put(key, row);
 		} else {
 		    StringByteIterator.putAllAsStrings(row, values);
		    cache.put(key, row);
 		}
 	    }
 
 	    return OK;
 	} catch (Exception e) {
 	    return ERROR;
 	}
     }
 
     public int insert(String table, String key, HashMap<String, ByteIterator> values) {
 	try {
 	    if (clustered) {
 		AtomicMap<String, String> row = AtomicMapLookup.getAtomicMap(globalCache, key);
 		row.clear();
 		StringByteIterator.putAllAsStrings(row, values);
 	    } else {
 		//globalCache.put(key, values);
 		Cache<Object, Map<String, String>> cache = globalCache;
 		Map<String, String> row = StringByteIterator.getStringMap(values);
 		cache.put(key, row);
 	    }
 
 	    return OK;
 	} catch (Exception e) {
 	    return ERROR;
 	}
     }
     
     public int insert(MagicKey key, HashMap<String, ByteIterator> values) {
 	try {
 	    if (clustered) {
 		AtomicMap<String, String> row = AtomicMapLookup.getAtomicMap(globalCache, key);
 		row.clear();
 		StringByteIterator.putAllAsStrings(row, values);
 	    } else {
 		//globalCache.put(key, values);
 		Cache<Object, Map<String, String>> cache = globalCache;
 		Map<String, String> row = StringByteIterator.getStringMap(values);
 		cache.put(key, row);
 	    }
 
 	    return OK;
 	} catch (Exception e) {
 	    return ERROR;
 	}
     }
 
     public int delete(String table, String key) {
 	try {
 	    if (clustered)
 		AtomicMapLookup.removeAtomicMap(globalCache, key);
 	    else
 		globalCache.remove(key);
 	    return OK;
 	} catch (Exception e) {
 	    return ERROR;
 	}
     }
 
 
     public int beginTransaction(){
 
 	if (tm==null) return ERROR ;
 
 	try {
 	    tm.begin();
 	    return OK;
 	}
 	catch (Exception e) {
 	    //throw new RuntimeException(e);
 	    e.printStackTrace();
 	    return  DB.ERROR;
 	}
 
 
     }
     
     @Override
     public void markWriteTx() {
 	globalCache.markAsWriteTransaction();
     }
 
     public int endTransaction(boolean commit){
 
 	if (tm == null){
 	    return ERROR;
 	}
 	try {
 	    if (commit){
 		tm.commit();
 		return OK;
 	    }	
 	    else{
 		tm.rollback();
 		return ERROR;
 	    }	
 	}
 	catch (Exception e) {
 	    //throw new RuntimeException(e);
 	    return ERROR;
 	}
 
 
     }
     
     static volatile boolean finished = false;
     static final Object finishLock = new Object();
     
     @Override
     public void finish() {
         synchronized (finishLock) {
         if (finished) {
             return;
         }
         Integer waitValue = null;
         
         Integer val = -1;
         while (true) {
         try  {
         tm.begin();
         globalCache.markAsWriteTransaction();
         val = (Integer) globalCache.get("Sebastiano_key");
         globalCache.put("Sebastiano_key", val - 1);
         tm.commit();
         break;
         } catch (Exception e) {
         try {
             tm.rollback();
         } catch (IllegalStateException e1) {
         } catch (SecurityException e1) {
         } catch (SystemException e1) {
         }    
         }
         }
 
         while(waitValue == null || waitValue > 0){
             try{ 
             waitValue = (Integer) globalCache.get("Sebastiano_key");
             }
             catch(Exception e){
 
             waitValue = null;
             }
             try {  Thread.sleep(2000); } catch (InterruptedException e) {}
             System.out.println("Current key: " + waitValue);
         }
         finished = true;
         }
     }
 }

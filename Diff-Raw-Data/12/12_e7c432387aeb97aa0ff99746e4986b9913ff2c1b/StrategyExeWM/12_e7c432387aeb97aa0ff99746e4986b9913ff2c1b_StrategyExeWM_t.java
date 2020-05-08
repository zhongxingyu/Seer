 package am.projects.webtransco.client;
 
 import com.wm.app.b2b.server.Service;
 import com.wm.data.IData;
 import com.wm.data.IDataCursor;
 import com.wm.data.IDataFactory;
 import com.wm.data.IDataUtil;
 import com.wm.util.JournalLogger;
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Iterator;
 
 /**
  * webMethods implementation strategy
  * User: mlecoutre
  * Date: 21/08/12
  */
 public class StrategyExeWM extends AbstractExecutionStrategy {
 
     private static final String TRANSCO_CACHE_MANAGER = "TranscoCacheManager";
     private static final String TRANSCO_CACHE = "TranscoCache";
 
     public static final String DATASTORE_CONNECTION_PACKAGE = "awmf.transco.connections";
     public static final String DATASTORE_EXECUTE_SERVICE = "retrieveTranscoConnection";
    private static CacheManager cacheManager = null;
 
     public StrategyExeWM() {
     }
 
     @Override
     public Connection retrieveConnection(String dataStoreAliasName) throws SQLException {
 
         String connectionAlias = DATASTORE_CONNECTION_PACKAGE+":" + "Transco_TBO";
         IData input = IDataFactory.create(); //nothing in the pipeline
         IDataCursor cursor = input.getCursor();
         //  JournalLogger.log(JournalLogger.DEBUG, JournalLogger.FAC_FLOW_SVC, JournalLogger.DEBUG, "[TRANSCO] override connection alias: "+connectionAlias);
         IDataUtil.put(cursor, "connectionName", connectionAlias);
 
         try{
         IData output = Service.doInvoke(DATASTORE_CONNECTION_PACKAGE, DATASTORE_EXECUTE_SERVICE, input);
         }catch (Exception e){
             JournalLogger.log(JournalLogger.ERROR, JournalLogger.FAC_FLOW_SVC, JournalLogger.ERROR, "[TRANSCO]  ERROR RetrieveConnection done. ", e);
         }
         //JournalLogger.log(JournalLogger.DEBUG, JournalLogger.FAC_FLOW_SVC, JournalLogger.DEBUG, "[TRANSCO] RetrieveConnection done. " + this.getConnection());
 
         return this.getConnection();
     }
 
     /**
      * Initialize cache configuration.
      * Try to get the cache configuration of a webMethods integration server with standard name
      * (TranscoCacheManager and a TranscoCache instance). If not found it try to create a
      * local cache instance.
      *
      * @return cache instance
      */
     public Cache retrieveCache(String cacheName) {
         Cache cache = null;
         boolean isCMFound = false;
        if (cacheManager == null) {
             for (CacheManager cm : CacheManager.ALL_CACHE_MANAGERS) {
                 if (TRANSCO_CACHE_MANAGER.equals(cm.getName())) {
                     isCMFound = true;
                    cacheManager = cm;
                     break;
                 }
             }
             if(isCMFound){
                 JournalLogger.log(JournalLogger.INFO, JournalLogger.FAC_FLOW_SVC, JournalLogger.INFO, "[TRANSCO] CacheManager has been found on the Integration Server");
             } else{
                 JournalLogger.log(JournalLogger.WARNING, JournalLogger.FAC_FLOW_SVC, JournalLogger.WARNING, "[TRANSCO]CacheManager HAS NOT BEEN FOUND on the Integration Server");
             }
         }
        if (cacheManager != null) {
            cache = cacheManager.getCache(TRANSCO_CACHE);
             if (cache == null) {
                 //should not be null if cm has been found (maybe configuration error)
                 JournalLogger.log(JournalLogger.ERROR, JournalLogger.FAC_FLOW_SVC, JournalLogger.ERROR, "[TRANSCO] NO CACHE INSTANCE  FOUND. Please configure cache for the transco application.");
             }
         }
 
         return cache;
     }
 
 }

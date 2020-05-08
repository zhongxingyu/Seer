 package org.sakaiproject.tool.impl;
 
 import net.spy.memcached.MemcachedClient;
 import org.apache.commons.logging.LogFactory;
 import org.sakaiproject.component.api.ServerConfigurationService;
 import org.springframework.util.StringUtils;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: jbush
  * Date: 12/3/13
  * Time: 7:33 PM
  * To change this template use File | Settings | File Templates.
  */
 public class MySessionMemcachedStore {
     private static org.apache.commons.logging.Log M_log = LogFactory.getLog(MySessionMemcachedStore.class);
     private MemcachedClient memcachedClient;
     private ServerConfigurationService serverConfigurationService;
 
     private boolean MEMCACHED_CLUSTER = false;
 
     public void init() {
         String clusterMemcached = System.getProperty("sakai.cluster.memcached");
         MEMCACHED_CLUSTER = "true".equals(clusterMemcached);
         if (MEMCACHED_CLUSTER) {
             M_log.info("Memcached support is enabled.  Get your party on!");
         }
     }
 
     /**
      * kryo is not thread safe, a new instance is needed per thread.
      * may want to consider pushing into threadlocal if instantiating this thing
      * over and over proves to be slow.
      * @return
      */
     protected SessionSerializer getSerializer() {
         return new SessionSerializer();
     }
 
     public MySession findSession(final String sessionId) {
         if (StringUtils.isEmpty(sessionId)) {
             return null;
         }
 
         if (!MEMCACHED_CLUSTER) {
             M_log.debug("Memcached support is disabled.  Enable with -Dsakai.cluster.memcached=true");
             return null;
         }
 
         long start = System.currentTimeMillis();
         Object object  = null;
 
         try {
             object = memcachedClient.get(sessionId);
         } catch (Exception e) {
             M_log.error("error looking up session in memcached: + " + e.getMessage(), e);
         }
         if (object != null) {
             M_log.info("retrieved session:" + sessionId + " from memcached in " + (System.currentTimeMillis() -  start) + " ms, attempting to deserialize");
             try {
                 return getSerializer().deserialize((byte[]) object);
             } catch (Exception e) {
                 M_log.error("Failure deserializing session: " + sessionId + " error: " + e.getMessage(), e);
             }
         }
         return null;
 
     }
 
 
     public void storeSession(final MySession session) {
         if (!MEMCACHED_CLUSTER) {
             M_log.debug("Memcached support is disabled.  Enable with -Dsakai.cluster.memcached=true");
             return;
         }
 
         M_log.info("storing session:" + session.getId() + " in memcached.");
         long start = System.currentTimeMillis();
 
         memcachedClient.delete(session.getId());
 
         memcachedClient.add(session.getId(),
                 serverConfigurationService.getInt("session.memcached.expiration", 0),
                 getSerializer().serialize(session));
         M_log.debug("stored session to memcached in " + (System.currentTimeMillis() -  start) + " ms");
 
     }
 
 
     public void setMemcachedClient(MemcachedClient memcachedClient) {
         this.memcachedClient = memcachedClient;
     }
 
     public void setServerConfigurationService(ServerConfigurationService serverConfigurationService) {
         this.serverConfigurationService = serverConfigurationService;
     }

    public void removeSession(String sessionId) {
        memcachedClient.delete(sessionId);
    }
 }

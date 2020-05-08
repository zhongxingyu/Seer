 package ddth.dasp.common.hazelcastex.impl;
 
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ddth.dasp.common.hazelcastex.IHazelcastClient;
 import ddth.dasp.common.hazelcastex.IHazelcastClientFactory;
 import ddth.dasp.common.hazelcastex.PoolConfig;
 
 public abstract class AbstractHazelcastClientFactory implements IHazelcastClientFactory {
 
     private final static Logger LOGGER = LoggerFactory
             .getLogger(AbstractHazelcastClientFactory.class);
 
     /**
      * Stores established Hazelcast client pools as a map of {key:pool}.
      */
     private ConcurrentMap<String, HazelcastClientPool> hazelcastClientPools = new ConcurrentHashMap<String, HazelcastClientPool>();
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void init() {
         // TODO Auto-generated method stub
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void destroy() {
         for (Entry<String, HazelcastClientPool> entry : hazelcastClientPools.entrySet()) {
             HazelcastClientPool clientPool = entry.getValue();
             try {
                 clientPool.close();
             } catch (Exception e) {
                 LOGGER.warn(e.getMessage());
             }
         }
     }
 
     protected static String calcHazelcastPoolName(List<String> servers, String username,
             String password) {
         StringBuilder sb = new StringBuilder();
         sb.append(servers != null ? servers : "NULL");
         sb.append(".");
         sb.append(username != null ? username : "NULL");
         sb.append(".");
         int passwordHashcode = password != null ? password.hashCode() : "NULL".hashCode();
         return sb.append(passwordHashcode).toString();
     }
 
     protected HazelcastClientPool getPool(String poolName) {
         return hazelcastClientPools.get(poolName);
     }
 
     protected HazelcastClientPool buildHazelcastClientPool(List<String> servers, String username,
             String password) {
         return buildHazelcastClientPool(servers, username, password, null);
     }
 
     protected abstract HazelcastClientPool buildHazelcastClientPool(List<String> servers,
             String username, String password, PoolConfig poolConfig);
 
     /**
      * {@inheritDoc}
      */
     @Override
     public IHazelcastClient getHazelcastClient(List<String> servers) {
         return getHazelcastClient(servers, null, null);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public IHazelcastClient getHazelcastClient(List<String> servers, String username,
             String password) {
         return getHazelcastClient(servers, username, password, null);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public IHazelcastClient getHazelcastClient(List<String> servers, String username,
             String password, PoolConfig poolConfig) {
         String poolName = calcHazelcastPoolName(servers, username, password);
         HazelcastClientPool hazelcastClientPool = getPool(poolName);
         if (hazelcastClientPool == null) {
             synchronized (hazelcastClientPools) {
                 try {
                     hazelcastClientPool = buildHazelcastClientPool(servers, username, password,
                             poolConfig);
                 } catch (Exception e) {
                     LOGGER.error(e.getMessage(), e);
                     throw new RuntimeException(e);
                 }
                 hazelcastClientPools.put(poolName, hazelcastClientPool);
             }
         }
         try {
             return hazelcastClientPool.borrowHazelcastClient();
         } catch (Exception e) {
             LOGGER.error(e.getMessage(), e);
             return null;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void returnHazelcastClient(IHazelcastClient hazelcastClient) {
        hazelcastClient.close();
     }
 }

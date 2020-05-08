 package ddth.dasp.framework.cache.hazelcast;
 
 import java.util.List;
 
 import com.hazelcast.client.ClientConfig;
 import com.hazelcast.client.HazelcastClient;
 
 import ddth.dasp.framework.cache.AbstractCacheManager;
 import ddth.dasp.framework.cache.ICacheManager;
 
 /**
  * <a href="http://www.hazelcast.com/">Hazelcast</a> implementation of
  * {@link ICacheManager}.
  * 
  * @author NBThanh <btnguyen2k@gmail.com>
  * @version 0.1.0
  */
 public class HazelcastCacheManager extends AbstractCacheManager {
 
     private List<String> hazelcastServers;
     private HazelcastClient hazelcastClient;
 
     public void setHazelcastServers(List<String> hazelcastServers) {
         this.hazelcastServers = hazelcastServers;
     }
 
     protected List<String> getHazelcastServers() {
         return hazelcastServers;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void init() {
         super.init();
         ClientConfig clientConfig = new ClientConfig();
         for (String hazelcastServer : hazelcastServers) {
             clientConfig.addAddress(hazelcastServer);
         }
         hazelcastClient = HazelcastClient.newHazelcastClient(clientConfig);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void destroy() {
         try {
             hazelcastClient.shutdown();
         } finally {
             super.destroy();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected HazelcastCache createCacheInternal(String name, long capacity, long expireAfterWrite,
             long expireAfterAccess) {
         HazelcastCache cache = new HazelcastCache(hazelcastClient, name);
         cache.setCapacity(capacity > 0 ? capacity : getDefaultCacheCapacity());
         cache.setExpireAfterAccess(expireAfterAccess);
         cache.setExpireAfterWrite(expireAfterWrite);
         cache.init();
         return cache;
     }
 }

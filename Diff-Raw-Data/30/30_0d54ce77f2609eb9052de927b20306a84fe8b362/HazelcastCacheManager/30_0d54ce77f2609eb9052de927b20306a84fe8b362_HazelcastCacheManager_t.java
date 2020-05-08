 package ddth.dasp.framework.cache.hazelcast;
 
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.hazelcast.client.ClientConfig;
 import com.hazelcast.security.UsernamePasswordCredentials;
 
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
 
     private String hazelcastUsername, hazelcastPassword;
     private List<String> hazelcastServers;
     private ClientConfig clientConfig;
 
     // private HazelcastClient hazelcastClient;
 
     public void setHazelcastUsername(String hazelcastUsername) {
         this.hazelcastUsername = hazelcastUsername;
     }
 
     public void setHazelcastPassword(String hazelcastPassword) {
         this.hazelcastPassword = hazelcastPassword;
     }
 
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
         clientConfig = new ClientConfig();
         // ClientConfig clientConfig = new ClientConfig();
         // clientConfig.setConnectionTimeout(10000);
         // clientConfig.setReconnectionAttemptLimit(10);
         // clientConfig.setInitialConnectionAttemptLimit(10);
         // clientConfig.setReConnectionTimeOut(10000);
         if (!StringUtils.isBlank(hazelcastUsername)) {
             clientConfig.setCredentials(new UsernamePasswordCredentials(hazelcastUsername,
                     hazelcastPassword));
         }
         for (String hazelcastServer : hazelcastServers) {
             clientConfig.addAddress(hazelcastServer);
         }
         // hazelcastClient = HazelcastClient.newHazelcastClient(clientConfig);
     }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public void destroy() {
        // EMPTY
    }
 
     /**
      * {@inheritDoc}
      */
     @Override
     protected HazelcastCache createCacheInternal(String name, long capacity, long expireAfterWrite,
             long expireAfterAccess) {
         // HazelcastCache cache = new HazelcastCache(hazelcastClient, name);
         HazelcastCache cache = new HazelcastCache(clientConfig, name);
         cache.setCapacity(capacity > 0 ? capacity : getDefaultCacheCapacity());
         cache.setExpireAfterAccess(expireAfterAccess);
         cache.setExpireAfterWrite(expireAfterWrite);
         cache.init();
         return cache;
     }
 }

 package me.smecsia.common.camel;
 
 import com.hazelcast.core.HazelcastInstance;
 import com.hazelcast.core.IMap;
 import org.apache.camel.CamelContext;
 import org.apache.camel.Exchange;
 import org.apache.camel.impl.DefaultExchange;
 import org.apache.camel.impl.DefaultExchangeHolder;
 import org.apache.camel.spi.AggregationRepository;
 import org.apache.camel.support.ServiceSupport;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Collections;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 public class HazelcastAggregationRepository extends ServiceSupport implements AggregationRepository {
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     private HazelcastInstance hazelcastInstance;
     private String repository;
     private IMap<String, DefaultExchangeHolder> map;
 
     public static final long WAIT_FOR_LOCK_SEC = 20;
 
     @Override
     protected void doStart() throws Exception {
         map = hazelcastInstance.getMap(repository);
     }
 
     @Override
     protected void doStop() throws Exception {
         /* Nothing to do */
     }
 
     @Override
     public Exchange add(CamelContext camelContext, String key, Exchange exchange) {
         try {
            logger.debug("Adding exchange to '" + key + "'. putAndUnlock...");
             DefaultExchangeHolder holder = DefaultExchangeHolder.marshal(exchange);
             map.putAndUnlock(key, holder);
             return toExchange(camelContext, holder);
         } catch (Exception e) {
             logger.error("Failed to add new exchange!", e);
         }
         return null;
     }
 
     @Override
     public Exchange get(CamelContext camelContext, String key) {
         try {
            logger.debug("Getting '" + key + "' from context. tryLockAndGet...");
             return toExchange(camelContext, map.tryLockAndGet(key, WAIT_FOR_LOCK_SEC, TimeUnit.SECONDS));
         } catch (Exception e) {
             logger.error("Failed to get the exchange!", e);
         }
         return null;
     }
 
     public Exchange getWithoutLock(CamelContext camelContext, String key) {
         return toExchange(camelContext, map.get(key));
     }
 
     private Exchange toExchange(CamelContext camelContext, DefaultExchangeHolder holder) {
         Exchange exchange = null;
         if (holder != null) {
             exchange = new DefaultExchange(camelContext);
             DefaultExchangeHolder.unmarshal(exchange, holder);
         }
         return exchange;
     }
 
     public void lock(String key) {
         try {
            logger.debug("Locking '" + key + "'...");
             map.tryLock(key, WAIT_FOR_LOCK_SEC, TimeUnit.SECONDS);
         } catch (Exception e) {
             logger.error("Failed to lock the key", e);
         }
     }
 
     public void unlock(String key) {
         try {
            logger.debug("Unlocking '" + key + "'...");
             map.unlock(key);
         } catch (Exception e) {
             logger.error("Failed to unlock the key!", e);
         }
     }
 
     @Override
     public void remove(CamelContext camelContext, String key, Exchange exchange) {
         try {
            logger.debug("Removing '" + key + "' tryRemove...");
             map.tryRemove(key, WAIT_FOR_LOCK_SEC, TimeUnit.SECONDS);
             map.unlock(key);
         } catch (Exception e) {
             logger.error("Failed to remove the exchange!", e);
         }
     }
 
     @Override
     public void confirm(CamelContext camelContext, String exchangeId) {
         /* Nothing to do */
     }
 
     @Override
     public Set<String> getKeys() {
         return Collections.unmodifiableSet(map.keySet());
     }
 
     public String getRepository() {
         return repository;
     }
 
     public void setRepository(String repository) {
         this.repository = repository;
     }
 
     public HazelcastInstance getHazelcastInstance() {
         return hazelcastInstance;
     }
 
     public void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
         this.hazelcastInstance = hazelcastInstance;
     }
 }

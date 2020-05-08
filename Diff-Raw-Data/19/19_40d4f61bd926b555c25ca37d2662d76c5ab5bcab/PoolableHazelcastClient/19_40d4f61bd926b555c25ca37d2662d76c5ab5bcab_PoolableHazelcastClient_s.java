 package ddth.dasp.common.hazelcastex.impl;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.hazelcast.client.ClientConfig;
 import com.hazelcast.client.HazelcastClient;
 import com.hazelcast.core.AtomicNumber;
 import com.hazelcast.core.IMap;
 import com.hazelcast.core.IQueue;
 import com.hazelcast.core.ITopic;
 import com.hazelcast.core.MessageListener;
 import com.hazelcast.security.UsernamePasswordCredentials;
 
 import ddth.dasp.common.hazelcastex.IHazelcastClientPool;
 import ddth.dasp.common.hazelcastex.IMessageListener;
 
 public class PoolableHazelcastClient extends AbstractHazelcastClient {
 
     private HazelcastClient hazelcastClient;
     private final Map<String, Set<IMessageListener<?>>> topicSubscriptions = new ConcurrentHashMap<String, Set<IMessageListener<?>>>();
     private final Map<IMessageListener<?>, WrappedMessageListener<?>> topicSubscriptionMappings = new ConcurrentHashMap<IMessageListener<?>, WrappedMessageListener<?>>();
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void init() {
         ClientConfig clientConfig = new ClientConfig();
         clientConfig.setConnectionTimeout(2000);
         clientConfig.setInitialConnectionAttemptLimit(2);
         clientConfig.setReconnectionAttemptLimit(2);
         clientConfig.setReConnectionTimeOut(2000);
         if (!StringUtils.isBlank(getHazelcastUsername())) {
             clientConfig.setCredentials(new UsernamePasswordCredentials(getHazelcastUsername(),
                     getHazelcastPassword()));
         }
         for (String hazelcastServer : getHazelcastServers()) {
             clientConfig.addAddress(hazelcastServer);
         }
         hazelcastClient = HazelcastClient.newHazelcastClient(clientConfig);
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings({ "unchecked", "rawtypes" })
     public void destroy() {
         for (Entry<IMessageListener<?>, WrappedMessageListener<?>> entry : topicSubscriptionMappings
                 .entrySet()) {
             try {
                 WrappedMessageListener msgListener = entry.getValue();
                 ITopic<?> topic = hazelcastClient.getTopic(msgListener.getTopicName());
                 if (topic != null) {
                     topic.removeMessageListener(msgListener);
                 }
             } catch (Exception e) {
                 // EMPTY
             }
         }
 
         try {
             topicSubscriptionMappings.clear();
         } catch (Exception e) {
             // EMPTY
         }
 
         try {
             topicSubscriptions.clear();
         } catch (Exception e) {
             // EMPTY
         }
 
         hazelcastClient.shutdown();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void close() {
         IHazelcastClientPool hazelcastClientPool;
         if ((hazelcastClientPool = getHazelcastClientPool()) != null) {
             hazelcastClientPool.returnHazelcastClient(this);
         }
     }
 
     /* Hazelcast API */
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean ping() {
        AtomicNumber ping = hazelcastClient.getAtomicNumber("_PING_");
        if (ping != null) {
            ping.incrementAndGet();
            return true;
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean mapDeleteAll(String mapName) {
         IMap<String, Object> map = hazelcastClient.getMap(mapName);
         if (map != null) {
             map.clear();
             return true;
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean mapDelete(String mapName, String key) {
         IMap<String, Object> map = hazelcastClient.getMap(mapName);
         if (map != null) {
             map.remove(key);
             return true;
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean mapSetExpiry(String mapName, String key, int ttlSeconds) {
         if (ttlSeconds > 0) {
             IMap<String, Object> map = hazelcastClient.getMap(mapName);
             Object value = map != null ? map.get(key) : null;
             if (value != null && map != null) {
                 map.put(key, value, ttlSeconds, TimeUnit.SECONDS);
                 return true;
             }
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Object mapGet(String mapName, String key) {
         IMap<String, Object> map = hazelcastClient.getMap(mapName);
         return map != null ? map.get(key) : null;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean mapSet(String mapName, String key, Object value, int ttlSeconds) {
         IMap<String, Object> map = hazelcastClient.getMap(mapName);
         if (map != null) {
             if (ttlSeconds > 0) {
                 map.put(key, value, ttlSeconds, TimeUnit.SECONDS);
             } else {
                 map.put(key, value);
             }
             return true;
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int mapSize(String mapName) {
         IMap<String, Object> map = hazelcastClient.getMap(mapName);
         return map != null ? map.size() : -1;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Object queuePoll(String queueName) {
         return queuePoll(queueName, DEFAULT_TIMEOUT, DEFAULT_TIMEUNIT);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Object queuePoll(String queueName, long timeout, TimeUnit timeoutTimeUnit) {
         IQueue<Object> queue = hazelcastClient.getQueue(queueName);
         try {
             return queue != null ? ((timeout <= 0 || timeoutTimeUnit == null) ? queue.poll()
                     : queue.poll(timeout, timeoutTimeUnit)) : null;
         } catch (Exception e) {
             return null;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean queuePush(String queueName, Object value) {
         return queuePush(queueName, value, DEFAULT_TIMEOUT, DEFAULT_TIMEUNIT);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean queuePush(String queueName, Object value, long timeout, TimeUnit timeoutTimeUnit) {
         IQueue<Object> queue = hazelcastClient.getQueue(queueName);
         if (queue != null) {
             try {
                 return (timeout <= 0 || timeoutTimeUnit == null) ? queue.offer(value) : queue
                         .offer(value, timeout, timeoutTimeUnit);
             } catch (Exception e) {
                 return false;
             }
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int queueSize(String queueName) {
         IQueue<Object> queue = hazelcastClient.getQueue(queueName);
         return queue != null ? queue.size() : -1;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean publish(String topicName, Object value) {
         ITopic<Object> topic = hazelcastClient.getTopic(topicName);
         if (topic != null) {
             topic.publish(value);
             return true;
         }
         return false;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public <T> boolean subscribe(String topicName, IMessageListener<T> messageListener) {
         Set<IMessageListener<?>> subcription = topicSubscriptions.get(topicName);
         if (subcription == null) {
             subcription = new HashSet<IMessageListener<?>>();
             topicSubscriptions.put(topicName, subcription);
         }
         synchronized (subcription) {
             ITopic<T> topic = hazelcastClient.getTopic(topicName);
             if (topic != null && subcription.add(messageListener)) {
                 WrappedMessageListener<T> wrappedMessageListener = new WrappedMessageListener<T>(
                         topicName, messageListener);
                 topicSubscriptionMappings.put(messageListener, wrappedMessageListener);
                 topic.addMessageListener(wrappedMessageListener);
                 return true;
             }
             return false;
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     @Override
     public <T> boolean unsubscribe(String topicName, IMessageListener<T> messageListener) {
         Set<IMessageListener<?>> subcription = topicSubscriptions.get(topicName);
         if (subcription != null) {
             synchronized (subcription) {
                 if (subcription.remove(messageListener)) {
                     WrappedMessageListener<?> wrappedMessageListener = topicSubscriptionMappings
                             .remove(messageListener);
                     if (wrappedMessageListener != null) {
                         ITopic<T> topic = hazelcastClient.getTopic(topicName);
                         if (topic != null) {
                             topic.removeMessageListener((MessageListener<T>) wrappedMessageListener);
                             return true;
                         }
                     }
                 }
             }
         }
         return false;
     }
     /* Hazelcast API */
 }

 package ddth.dasp.common.hazelcast;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.hazelcast.client.ClientConfig;
 import com.hazelcast.client.HazelcastClient;
 import com.hazelcast.core.IQueue;
 import com.hazelcast.core.ITopic;
 import com.hazelcast.core.MessageListener;
 import com.hazelcast.security.UsernamePasswordCredentials;
 
 import ddth.dasp.common.DaspGlobal;
 
 /**
  * Hazelcast client factory and utilities.
  * 
  * <ul>
  * <li>Hazelcast client factory with persistent client support.</li>
  * <li>{@link #readFromQueue(String) Read} from and
  * {@link #writeToQueue(String, Object) write} to queues.</li>
  * <li>{@link #subcribeToTopic(String, MessageListener) Subscribe},
  * {@link #publishToTopic(String, Object) publish} to and
  * {@link #unsubscribeFromTopic(String, MessageListener) unsubscribe} from
  * topics. Topic subscriptions are revalidated after server's recovery from
  * failure.</li>
  * </ul>
  * 
  * @author NBThanh <btnguyen2k@gmail.com>
  * @see <a href="">Hazelcast</a>
  * @see #readFromQueue(String)
  * @see #readFromQueue(String, long, TimeUnit)
  * @see #writeToQueue(String, Object)
  * @see #writeToQueue(String, Object, long, TimeUnit)
  * @see #subcribeToTopic(String, MessageListener)
  * @see #unsubscribeFromTopic(String, MessageListener)
  * @see #publishToTopic(String, Object)
  */
 public class HazelcastClientFactory implements IHazelcastClientFactory {
 
     private final Logger LOGGER = LoggerFactory.getLogger(HazelcastClientFactory.class);
     private final static long DEFAULT_TIMEOUT = 5000;
     private final Map<String, Set<MessageListener<?>>> topicSubcription = new ConcurrentHashMap<String, Set<MessageListener<?>>>();
     private final AtomicLong counter = new AtomicLong(0);
 
     private String hazelcastUsername, hazelcastPassword;
     private List<String> hazelcastServers;
     private ClientConfig clientConfig;
     private HazelcastClient _hazelcastClient;
     private boolean _destroyed = false;
 
     public void setHazelcastUsername(String hazelcastUsername) {
         this.hazelcastUsername = hazelcastUsername;
     }
 
     public void setHazelcastPassword(String hazelcastPassword) {
         this.hazelcastPassword = hazelcastPassword;
     }
 
     public void setHazelcastServers(List<String> hazelcastServers) {
         this.hazelcastServers = hazelcastServers;
     }
 
     private class HazelcastClientPing implements Runnable {
         @Override
         public void run() {
             try {
                 HazelcastClient client = getHazelcastClient();
                 if (client != null) {
                     try {
                        client.getMap("");
                     } finally {
                         returnHazelcastClient();
                     }
                 }
             } catch (Exception e) {
                 LOGGER.warn(e.getMessage(), e);
             }
         }
     }
 
     public void init() {
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
         ScheduledExecutorService ses = DaspGlobal.getScheduler();
        ses.scheduleWithFixedDelay(new HazelcastClientPing(), 10, 10, TimeUnit.SECONDS);
     }
 
     public void destroy() {
         _destroyed = true;
         dispostHazelcastClient();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Object readFromQueue(String queueName) {
         return readFromQueue(queueName, DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Object readFromQueue(String queueName, long timeout, TimeUnit timeUnit) {
         HazelcastClient client = getHazelcastClient();
         if (client != null) {
             try {
                 IQueue<Object> queue = client.getQueue(queueName);
                 return queue != null ? queue.poll(timeout, timeUnit) : null;
             } catch (InterruptedException e) {
                 return null;
             } finally {
                 returnHazelcastClient();
             }
         }
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean writeToQueue(String queueName, Object value) {
         return writeToQueue(queueName, value, DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean writeToQueue(String queueName, Object value, long timeout, TimeUnit timeUnit) {
         HazelcastClient client = getHazelcastClient();
         if (client != null) {
             try {
                 IQueue<Object> queue = client.getQueue(queueName);
                 if (queue != null) {
                     queue.offer(value, timeout, timeUnit);
                     return true;
                 } else {
                     throw new NullPointerException("Queue is null!");
                 }
             } catch (InterruptedException e) {
                 return false;
             } finally {
                 returnHazelcastClient();
             }
         } else {
             throw new NullPointerException("Client is null!");
         }
     }
 
     @SuppressWarnings("unchecked")
     synchronized protected void resubcribeToTopics() {
         Iterator<Entry<String, Set<MessageListener<?>>>> iterator = topicSubcription.entrySet()
                 .iterator();
         while (iterator.hasNext()) {
             Entry<String, Set<MessageListener<?>>> subscriptions = iterator.next();
             ITopic<Object> topic = _hazelcastClient.getTopic(subscriptions.getKey());
             if (topic != null) {
                 for (MessageListener<?> messageListener : subscriptions.getValue()) {
                     topic.addMessageListener((MessageListener<Object>) messageListener);
                 }
             } else {
                 iterator.remove();
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public <E> void subcribeToTopic(String topicName, MessageListener<E> messageListener) {
         if (StringUtils.isBlank(topicName) || messageListener == null) {
             throw new IllegalArgumentException("Invalid topic name or listener!");
         }
 
         Set<MessageListener<?>> subcription = topicSubcription.get(topicName);
         if (subcription == null) {
             subcription = new HashSet<MessageListener<?>>();
             topicSubcription.put(topicName, subcription);
         }
 
         synchronized (subcription) {
             if (!subcription.contains(messageListener)) {
                 HazelcastClient client = getHazelcastClient();
                 if (client != null) {
                     try {
                         ITopic<E> topic = client.getTopic(topicName);
                         if (topic != null) {
                             topic.addMessageListener(messageListener);
                             subcription.add(messageListener);
                         } else {
                             throw new NullPointerException("Client is null!");
                         }
                     } finally {
                         returnHazelcastClient();
                     }
                 } else {
                     throw new NullPointerException("Client is null!");
                 }
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void publishToTopic(String topicName, Object message) {
         if (StringUtils.isBlank(topicName) || message == null) {
             throw new IllegalArgumentException("Invalid topic name or message!");
         }
 
         HazelcastClient client = getHazelcastClient();
         if (client != null) {
             try {
                 ITopic<Object> topic = client.getTopic(topicName);
                 if (topic != null) {
                     topic.publish(message);
                 } else {
                     throw new NullPointerException("Topic is null!");
                 }
             } finally {
                 returnHazelcastClient();
             }
         } else {
             throw new NullPointerException("Client is null!");
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public <E> void unsubscribeFromTopic(String topicName, MessageListener<E> messageListener) {
         if (StringUtils.isBlank(topicName) || messageListener == null) {
             throw new IllegalArgumentException("Invalid topic name or listener!");
         }
 
         Set<MessageListener<?>> subcription = topicSubcription.get(topicName);
         if (subcription != null) {
             synchronized (subcription) {
                 subcription.remove(messageListener);
                 HazelcastClient client = getHazelcastClient();
                 if (client != null) {
                     try {
                         ITopic<E> topic = client.getTopic(topicName);
                         if (topic != null) {
                             topic.removeMessageListener(messageListener);
                         }
                     } finally {
                         returnHazelcastClient();
                     }
                 }
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     synchronized public HazelcastClient getHazelcastClient() {
         if (_destroyed) {
             return null;
         }
         if (_hazelcastClient != null && !_hazelcastClient.isActive()) {
             dispostHazelcastClient();
             _hazelcastClient = null;
         }
         try {
             if (_hazelcastClient == null) {
                 _hazelcastClient = HazelcastClient.newHazelcastClient(clientConfig);
                 triggerNewHazelcastClient();
                 // increase counter to make the client not being shutdown when
                 // {@link returnHazelcastClient()} is called.
                 counter.incrementAndGet();
             }
         } catch (Exception e) {
             LOGGER.warn(e.getMessage(), e);
             return null;
         }
         counter.incrementAndGet();
         return _hazelcastClient;
     }
 
     /**
      * Called when a new Hazelcast client is created.
      */
     synchronized protected void triggerNewHazelcastClient() {
         resubcribeToTopics();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     synchronized public void returnHazelcastClient() {
         long value = counter.decrementAndGet();
         if (value < 0) {
             counter.set(0);
             throw new IllegalStateException("No Hazelcast client is currently allocated!");
         }
         if (value == 0) {
             dispostHazelcastClient();
         }
     }
 
     synchronized protected void dispostHazelcastClient() {
         if (_hazelcastClient != null) {
             try {
                 _hazelcastClient.shutdown();
             } finally {
                 counter.set(0);
                 _hazelcastClient = null;
             }
         }
     }
 }

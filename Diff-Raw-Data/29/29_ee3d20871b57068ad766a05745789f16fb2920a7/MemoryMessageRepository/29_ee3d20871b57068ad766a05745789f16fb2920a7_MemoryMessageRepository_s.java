 package cache;
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.springframework.cache.annotation.CacheEvict;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.stereotype.Repository;
 
 @Repository
 public class MemoryMessageRepository implements MessageRepository {
 
     private final Map<String, Message> messages =
 
     new ConcurrentHashMap<String, Message>();
 
     @Override
     @Cacheable(value = "message")
     public Message getMessage(String key) {
         System.out.println("Fetching message");
         return messages.get(key);
     }
 
     @Override
     @CacheEvict(value = "message", key = "#message.key")
     public void save(Message message) {
         System.out.println("Saving message");
         messages.put(message.getKey(), message);
     }
 
     public Collection<Message> findAll() {
         return messages.values();
     }
 
 }

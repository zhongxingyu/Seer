 package bitter.util;
 
 import java.util.concurrent.*;
import bitter.*;
 
 public class MessageListHash {
 
     private ConcurrentHashMap<String, MessageList> hashmap;
 
     public MessageListHash() {
        hashmap = new ConcurrentHashMap<String, MessageList>();
     }
 
     public synchronized void putMessage(String key, Message message) { 
        if (!hashmap.containsKey(key)) {
            hashmap.putIfAbsent(key, new MessageList());
         }
         hashmap.get(key).add(message);
     }
 
     public MessageList getMessageList(String key) {
         return hashmap.get(key);
     }
 
 
 
 } // End class MessageListHash

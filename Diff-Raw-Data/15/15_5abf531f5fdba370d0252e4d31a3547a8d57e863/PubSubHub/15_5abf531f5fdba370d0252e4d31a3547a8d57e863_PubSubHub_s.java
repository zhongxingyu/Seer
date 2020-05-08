 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class PubSubHub {
     private Map<Class, Set<MessageListener>> messageSubscriptions = new HashMap<Class, Set<MessageListener>>();
 
     public <T> void subscribeFor(Class<T> messageClass, MessageListener<T> messageListener) {
         if (messageListener == null) throw new SubscribtionException("Message listeners cannot be null");
 
         Set<MessageListener> listeners = messageSubscriptions.get(messageClass);
         if (listeners == null) {
             listeners = new HashSet<MessageListener>();
             messageSubscriptions.put(messageClass, listeners);
         }
         if (listeners.contains(messageListener)) throw new SubscribtionException("Message listener "+messageListener+" already subscribed for message "+messageClass);
         listeners.add(messageListener);
     }
 
     public void send(Object message) {
         final Set<MessageListener> messageListenerSet = messageSubscriptions.get(message.getClass());
         if (messageListenerSet != null) {
             for (MessageListener messageListener : messageListenerSet) {
                 messageListener.onMessage(message);
             }
         }
     }
 }

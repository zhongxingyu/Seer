 import java.util.Map;
 import java.util.HashMap;
 import java.io.Serializable;
 
 public class MessageQueue implements Serializable {
     static public final String FILE_MSG_QUEUE = "/Users/amitjoshi/programming/java/msgQueue.ser";
 
     private Map<String, MyQueue<String>> queueByTopic;
     private static final int SIZE = 4;
 
     public MessageQueue() {
         queueByTopic = new HashMap<String, MyQueue<String>>();
     }
 
     public void send(String topic, String msg) {
         if (queueByTopic.get(topic) == null) {
             queueByTopic.put(topic, new MyQueue<String>(SIZE));
         }
         final MyQueue<String> queue = queueByTopic.get(topic);
         queue.offer(msg);
     }
 
     public String receive(String topic) {
         if (queueByTopic.get(topic) == null) {
             return null;
         }
         final MyQueue<String> queue = queueByTopic.get(topic);
         return queue.poll();
     }
 }

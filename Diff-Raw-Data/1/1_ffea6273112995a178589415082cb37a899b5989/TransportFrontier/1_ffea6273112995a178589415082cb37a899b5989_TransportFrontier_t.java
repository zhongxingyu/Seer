 package pl.edu.pjwstk.p2pp.transport;
 
 import org.apache.log4j.Logger;
 import pl.edu.pjwstk.p2pp.messages.Message;
 
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 import pl.edu.pjwstk.p2pp.messages.P2PPMessage;
 
 public class TransportFrontier {
 
     public static final Logger LOG = Logger.getLogger(TransportFrontier.class);
 
     //private ConcurrentHashMap<String, LinkedList<Message>> frontier = new ConcurrentHashMap<String, LinkedList<Message>>();
     private ConcurrentHashMap<String, LinkedList<Object[]>> frontier = new ConcurrentHashMap<String, LinkedList<Object[]>>();
 
     final private CopyOnWriteArrayList<String> addresses = new CopyOnWriteArrayList<String>();
 
     private AtomicInteger size = new AtomicInteger(0);
     
     //private final LinkedBlockingQueue<Message> messages = new LinkedBlockingQueue<Message>();
 
     //public void add(Message message) {
     public void add(Object[] messageTransaction) {
 
         Message message = (Message) messageTransaction[0];
 
         if (message == null) {
             LOG.warn("Trying to add null message");
             return;
         }
 
         String receiverAddress = message.getReceiverAddress();
 
         synchronized (this.addresses) {
 
             if (!this.frontier.containsKey(receiverAddress)) {
                 //this.frontier.put(receiverAddress, new LinkedList<Message>());
                 this.frontier.put(receiverAddress, new LinkedList<Object[]>());
             }
 
             if (!this.addresses.contains(receiverAddress)) {
                 this.addresses.add(receiverAddress);
             }
             //sometimes calledMethod removes below address from frontier, below 
             // code moves to synchronized block
             this.size.incrementAndGet();
             //this.frontier.get(receiverAddress).add(message);
             this.frontier.get(receiverAddress).add(messageTransaction);
 
         }
 //        this.messages.add(message);
 //        if(((P2PPMessage)message).getTransactionID()==null){
 //            LOG.fatal("Added message with null transactionID :"+message,new Throwable("null transactionID"));
 //        }
 
     }
 
     private String getRandomAddress() {
 
         String randomAddress;
 
         synchronized (this.addresses) {
             int addressesSize = this.addresses.size();
             if (addressesSize == 0) return null;
             randomAddress = this.addresses.get(((int) (Math.random() * Integer.MAX_VALUE)) % addressesSize);
         }
 
         return randomAddress;
     }
 
     //private Message pollMessage(String address) {
     private Object[] pollMessage(String address) {
         //Message message = null;
         Object[] message = null;
         /*
          * sychronised a whole of code block, sometimes is removing queue, 
          * which holds messages to send - this eliminates that.
          */
         synchronized (this.addresses) {
             if (address == null) {
                 return null;
             }
             //Queue<Message> queue = this.frontier.get(address);
             Queue<Object[]> queue = this.frontier.get(address);
             if (queue == null) {
                 return null;
             }
            // synchronized (queue) {
                 message = queue.poll();
                 
                 if(message!=null) this.size.decrementAndGet();
                 
                 if (queue.size() < 1) {
 
                     this.frontier.remove(address);
                     this.addresses.remove(address);
                 }
             //}
         }        
         return message;
     }
 
     //public Message poll() {
     public Object[] poll() {
 //        try {
                     if (this.size.intValue() < 1) return null;
             
                     String address = this.getRandomAddress();
             
                     return this.pollMessage(address);
 //                    return this.messages.poll(1000, TimeUnit.MILLISECONDS);
 //        } catch (InterruptedException ex) {
 //            return null;
 //        }
 //    }
     }
 
 }

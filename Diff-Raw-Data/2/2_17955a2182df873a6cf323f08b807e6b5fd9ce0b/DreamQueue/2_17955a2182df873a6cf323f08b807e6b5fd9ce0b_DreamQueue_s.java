 package itmo.dreamq;
 
 import itmo.mq.Message;
 import itmo.mq.MessageQueue;
 import itmo.mq.Envelope;
 
 import javax.jws.HandlerChain;
 import javax.jws.WebService;
 import java.util.*;
 import java.util.concurrent.*;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 
 @WebService(endpointInterface = "itmo.mq.MessageQueue")
 public class DreamQueue implements MessageQueue {
 
     private final static long EXPIRE_DELAY = 1000;
 
     private AtomicLong ticket;
 
     private ConcurrentHashMap<Integer, BlockingQueue<Message>> messageQueue;
 
     private Map<Long, Envelope> messagePool;
     private Queue<Ticket> sentMessages;
 
 
     public DreamQueue() {
         ticket = new AtomicLong();
         messageQueue = new ConcurrentHashMap<Integer, BlockingQueue<Message>>();
         messagePool = new ConcurrentHashMap<Long, Envelope>();
         sentMessages = new ConcurrentLinkedQueue<Ticket>();
         Timer timer = new Timer();
 
         timer.schedule(new TimerTask() {
             @Override
             public void run() {
                 try {
                     while (!Thread.currentThread().isInterrupted()) {
                         if (sentMessages.isEmpty()) {
                             Thread.sleep(EXPIRE_DELAY);
                         } else {
                             Ticket t = sentMessages.peek();
                             long difference = t.getExpirationTime() - System.currentTimeMillis();
                             if (difference > 0) {
                                 Thread.sleep(difference);
                             } else {
                                 sentMessages.remove();
                                 Envelope tempTask = messagePool.remove(t.getTicket());
                                 if (tempTask != null) {
                                    messageQueue.get(tempTask.getTag()).put(tempTask.getMsg());
                                 }
                             }
                         }
                     }
                 } catch (InterruptedException e) {
                 }
             }
         }, EXPIRE_DELAY);
     }
 
     @Override
     public boolean createQueue(int tag) {
         if (messageQueue.containsKey(tag)) {
             return false;
         } else {
             System.err.println("Attempting to create queue #" + tag);
             BlockingQueue existingQueue = messageQueue.putIfAbsent(tag, new LinkedBlockingQueue<Message>());
             return existingQueue == null;
         }
     }
 
     @Override
     public void ack(long ticketId) {
         if (messagePool.remove(ticketId) == null) {
             System.err.println("Too late!");
         }
     }
 
     @Override
     public void put(int tag, Message m) {
         createQueue(tag);
         System.err.println("Putting message to queue #" + tag);
         try {
             messageQueue.get(tag).put(m);
             synchronized (messageQueue) {
                 messageQueue.notify();
             }
         } catch (InterruptedException e) {
         }
     }
 
     @Override
     public Envelope getAny() {
         System.err.println("Get any");
         Message tempMessage = null;
         int tempTag = 0;
         try {
             for (Map.Entry<Integer, BlockingQueue<Message>> entry : messageQueue.entrySet()) {
                 tempMessage = entry.getValue().poll(0, TimeUnit.SECONDS);
                 tempTag = entry.getKey();
                 if (tempMessage != null) {
                     break;
                 }
             }
         } catch (InterruptedException e) {
         }
         if (tempMessage == null) {
             System.err.println("Get any returning null");
         } else {
             System.err.println("Get any returning not null");
         }
         return createEnvelope(tempMessage, tempTag);
     }
 
     @Override
     public Envelope getAnyBlocking() {
         System.err.println("Get any blocking");
         Envelope e = getAny();
         if (e.getMsg() != null) {
             return e;
         }
         try {
             synchronized (messageQueue) {
                 System.err.println("Get any blocking entering wait loop");
                 e = getAny();
                 while (e.getMsg() == null) {
                     messageQueue.wait();
                     e = getAny();
                 }
                 System.err.println("Get any blocking returning");
             }
         } catch (InterruptedException e1) {
         }
         return e;
     }
 
     @Override
     public Envelope get(int tag) {
         System.err.println("Get from queue #" + tag);
         Message tempMessage = null;
         BlockingQueue<Message> q = messageQueue.get(tag);
         if (q != null) {
             try {
                 tempMessage = q.poll(0, TimeUnit.SECONDS);
             } catch (InterruptedException e) {
             }
         }
         if (tempMessage == null) {
             System.err.println("Get returning null");
         } else {
             System.err.println("Get returning not null");
         }
         return createEnvelope(tempMessage, tag);
     }
 
     @Override
     public Envelope getBlocking(int tag) {
         System.err.println("Get blocking from queue #" + tag);
         Message tempMessage = null;
         createQueue(tag);
         BlockingQueue<Message> q = messageQueue.get(tag);
         try {
             tempMessage = q.take();
             System.err.println("Get blocking returning from queue #" + tag);
         } catch (InterruptedException e) {
         }
         return createEnvelope(tempMessage, tag);
     }
 
     private Envelope createEnvelope(Message msg, int tag) {
         Envelope envelope;
         if (msg != null) {
             envelope = new Envelope(msg, tag, ticket.getAndIncrement());
             Ticket t = new Ticket(envelope.getTicketId(), System.currentTimeMillis() + EXPIRE_DELAY);
             messagePool.put(envelope.getTicketId(), envelope);
             sentMessages.add(t);
         } else {
             envelope = new Envelope();
         }
         return envelope;
     }
 
 }

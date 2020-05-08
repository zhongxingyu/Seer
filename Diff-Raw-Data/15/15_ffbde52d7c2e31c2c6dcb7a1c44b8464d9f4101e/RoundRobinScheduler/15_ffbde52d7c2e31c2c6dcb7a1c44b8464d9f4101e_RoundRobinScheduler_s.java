 package polly.core.irc;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
import org.apache.log4j.Logger;

 import de.skuzzle.polly.sdk.eventlistener.IrcUser;
 import de.skuzzle.polly.sdk.eventlistener.MessageEvent;
 import de.skuzzle.polly.sdk.exceptions.DisposingException;
 
 /*
  * ISSUE: 0000049
  */
 
 public class RoundRobinScheduler extends Thread implements MessageScheduler {
 
    private static Logger logger = Logger.getLogger(RoundRobinScheduler.class
        .getName());

     private Lock lock;
     private IrcManagerImpl ircManager;
     private Semaphore sema;
     private Map<Object, LinkedList<MessageEvent>> messageQueue;
     private int messageDelay;
     private AtomicBoolean shutdownFlag;
 
 
 
     public RoundRobinScheduler(IrcManagerImpl ircManager, int messageDelay) {
         super("IRC_MESSAGE_SCHEDULER");
         this.ircManager = ircManager;
         this.messageQueue = new HashMap<Object, LinkedList<MessageEvent>>();
         this.sema = new Semaphore(0);
 
         this.messageDelay = messageDelay;
         this.shutdownFlag = new AtomicBoolean(false);
         this.lock = new ReentrantLock(true);
     }
 
 
 
     public void addMessage(String channel, String message, Object source) {
         try {
             this.lock.lock();
             LinkedList<MessageEvent> msgs = this.messageQueue.get(source);
             if (msgs == null) {
                 msgs = new LinkedList<MessageEvent>();
                 this.messageQueue.put(source, msgs);
             }
             msgs.add(new MessageEvent(this.ircManager, new IrcUser("DEBUGGER",
                 "", ""), channel, message));
             this.sema.release();
         } finally {
             this.lock.unlock();
         }
     }
 
 
 
     @Override
     public void run() {
 
        while (!this.shutdownFlag.get()) {
             try {
                 this.sema.acquire();
             } catch (InterruptedException e) {
                logger.warn("", e);
             }
 
             if (this.lock.tryLock()) {
                 try {
                     Iterator<Entry<Object, LinkedList<MessageEvent>>> it = 
                         this.messageQueue.entrySet().iterator();
 
                     while (it.hasNext()) {
                         Entry<Object, LinkedList<MessageEvent>> entry = it
                             .next();
                         LinkedList<MessageEvent> msgs = entry.getValue();
 
                         if (msgs.isEmpty()) {
                             continue;
                         }
 
                         MessageEvent next = msgs.pollFirst();
                         this.ircManager.sendMessage(next.getChannel(),
                             next.getMessage());
 
                         // CONSIDER: Remove iterator entry if the message list
                         // for the current source is empty.
                         // Not removing it should be slightly faster but
                         // will require more memory
 
                         if (msgs.isEmpty()) {
                             it.remove();
                         }
                     }
                 } finally {
                     this.lock.unlock();
                 }
             } else {
                 // put back permit if we could not get the lock in this run
                 this.sema.release();
             }
 
             try {
                 Thread.sleep(this.messageDelay);
             } catch (InterruptedException e) {
                e.printStackTrace();
             }
         }
 
     }
 
 
 
     @Override
     public boolean isDisposed() {
         return !this.shutdownFlag.get();
     }
 
 
 
     @Override
     public synchronized void dispose() throws DisposingException {
         this.shutdownFlag.set(true);
         this.interrupt();
     }
 }

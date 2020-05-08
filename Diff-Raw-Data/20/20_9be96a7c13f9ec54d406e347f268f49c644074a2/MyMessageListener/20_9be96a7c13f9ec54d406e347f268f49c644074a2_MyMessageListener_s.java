 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fi.wsnusbcollect.nodeCom;
 
 import fi.wsnusbcollect.nodes.ConnectedNode;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ExecutorService;
 import net.tinyos.message.Message;
 import net.tinyos.message.MoteIF;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Global message listener intended to receive packets from MoteIF as only one entity
  * in whole system. I think that listeners handling is not performed very well in MoteIF, so 
  * I wrote my very own. 
  * 
  * All module which need to receive messages from network registers here, same as it was performed before 
  * in MoteIf.
  * 
  * Globality of this object enables packetSource restart/change at runtime. When packet source is changed, listener and sender
  * are reseted, listeners are re-registered.
  * 
  * Uses thread-safe data structures to avoid corruption. 1 thread waits for registering/deregistering requests.
  * MessageReceived is invoked by different thread (from TOS lobrary receiving packets from serial interface).
  * 
  * Messages are then pushed to ConcurentQueue.
  * In another thread runs MessageNotifier which scans ConcurentQueue for new elements. 
  * Then is performed callback to registered listeners for particular message. Every single callback
  * is in separate TRY block to separate exceptions among worker modules.
  * 
  * Works very similar like messageSender, in analogous way.
  * 
  * @since  23-20-2011
  * @author ph4r05
  */
 public class MyMessageListener extends Thread implements MessageListenerInterface {
     private static final Logger log = LoggerFactory.getLogger(MyMessageListener.class);
     
     /**
      * maximum number of notify threads in fixed size thread pool
      */
     private static final int MAX_NOTIFY_THREADS=1;
     
     /**
      * Maximum number of messages in queue to reset queue
      */
     public static final int MAX_QUEUE_SIZE_TO_RESET=5000;
 
     /**
      * Message queue
      */
     private final ConcurrentLinkedQueue<MessageReceived> queue;
     
     /**
      * Mote interface for gateway
      */
     private MoteIF gateway;
 
     /**
      * Thread pool
      */
     protected ExecutorService tasks;
 
     /**
      * AMTYPE -> message mapping
      */
     protected ConcurrentHashMap<Integer, net.tinyos.message.Message> amType2Message = null;
     
     /**
      * Message listeners
      */
     protected ConcurrentHashMap<Integer, ConcurrentLinkedQueue<MessageListener>> messageListeners = null;
 
     /**
      * time of last sent message
      */
     protected long timeLastMessageSent;
 
     /**
      * Should I shutdown?
      */
     protected boolean shutdown=false;
     
     /**
      * Am I dropping packets currently?
      */
     protected boolean dropingPackets=true;
     
 //    /**
 //     * My worker, used to notify if something arrived to queue
 //     */
 //    private MessageNotifyWorker myWorker = null;
 
     /**
      *
      * @param gateway
      * @param logger
      */
     public MyMessageListener(MoteIF gateway) {
         // set thread title
         super("MyMessageListener");
         
         // init queues
         queue = new ConcurrentLinkedQueue<MessageReceived>();
         amType2Message = new ConcurrentHashMap<Integer, Message>(1);
         messageListeners = new ConcurrentHashMap<Integer, ConcurrentLinkedQueue<MessageListener>>(4);
         
         this.gateway = gateway;
 
         // instantiate thread pool
 //        tasks = Executors.newFixedThreadPool(MAX_NOTIFY_THREADS);
 //        this.myWorker = new MessageNotifyWorker();
 //        tasks.execute(this.myWorker);
         
         // create notify threads
 //        for(int i=0; i<MAX_NOTIFY_THREADS; i++){
 //            tasks.execute(new MessageNotifyWorker());
 //        }
     }
 
     /**
      * Pausing thread
      * @param microsecs
      */
     private void pause(int microsecs) {
         try {
             Thread.sleep(microsecs);
         } catch (InterruptedException ie) {
             log.warn("Cannot sleep", ie);
         }
     }
     
     /**
      * Performs shutdown
      */
     @Override
     public synchronized void shutdown(){
         this.reset();
         this.dropingPackets=true;
         this.shutdown=true;
         if (this.tasks!=null){
             this.tasks.shutdown();
         }
     }
     
     /**
      * Register message listener
      * If same listener (.equals()) is registered to same message type, request
      * is ignored.
      */
     @Override
     public synchronized void registerListener(net.tinyos.message.Message msg, MessageListener listener){
         // null?
         if (msg==null || listener==null){
             log.error("Cannot register listener when message or listener is null");
             throw new NullPointerException("Cannot register listener when message or listener is null");
         }
         
         try {
             // is message in translate queue?
             Integer amtype = msg.amType();
 
             // if does not contain mapping amtype -> message, create one
             if (this.amType2Message.containsKey(amtype)==false){
                 this.amType2Message.put(amtype, msg);
                 
                 // register this in real, on real listening interface for 
                 // this message type, myself as listener
                 this.gateway.registerListener(msg, this);
             }
 
             // check if linked list exists
             if (this.messageListeners.containsKey(amtype)==false 
                     || this.messageListeners.get(amtype)==null){
                 // none such mapping yet created, create new list of listeners
                 ConcurrentLinkedQueue<MessageListener> queueListener = new ConcurrentLinkedQueue<MessageListener>();
                 this.messageListeners.put(amtype, queueListener);
             }
 
             // here is queueListener set, get it now
             ConcurrentLinkedQueue<MessageListener> queueListeners = this.messageListeners.get(amtype);
 
             // check if is already in linked queue
             if (queueListeners==null){
                 // always have queue initialized
                 queueListeners = new ConcurrentLinkedQueue<MessageListener>();
             }
             
             Iterator<MessageListener> iterator = queueListeners.iterator();
             while(iterator.hasNext()){
                 MessageListener curMessageListener = iterator.next();
                 
                 // check if is null - remove it
                 if (curMessageListener==null){
                     iterator.remove();
                     log.error("Null message listener found in queue, removing");
                     continue;
                 }
                 
                 // equals? already set, not induce redundancy
                 if (curMessageListener.equals(listener)){
                     return;
                 }
             }
 
             // if here, listener is not in, set it
             queueListeners.add(listener);
 
             // update map
             this.messageListeners.put(amtype, queueListeners);
         } catch (Exception e){
             log.error("Exception occurred when registering message listener", e);
         }
     }
     
     /**
      * unregister message listener
      */
     @Override
    public synchronized void deregisterListener(net.tinyos.message.Message msg, net.tinyos.message.MessageListener listener){
         if (msg==null || listener==null){
             log.error("Cannot register listener when message or listener is null");
             throw new NullPointerException("Cannot register listener when message or listener is null");
         }
         
         try {
             // is message in translate queue?
             Integer amtype = msg.amType();
             
             // if does not contain mapping amtype -> message, create one
             if (this.amType2Message.containsKey(amtype)==false){                
                 // if not here, cannot be registered
                 return;
             }
             
             // is amtype established?
             if (this.messageListeners.containsKey(amtype)==false){
                 // no, cannot be in underlying list
                 return;
             }
             
             // get list
             ConcurrentLinkedQueue<MessageListener> listenersList = this.messageListeners.get(amtype);
             if (listenersList==null || listenersList.isEmpty()){
                 // list empty => cannot be in
                 return;
             }
             
             Iterator<MessageListener> iterator = listenersList.iterator();
             while(iterator.hasNext()){
                 MessageListener curListener = iterator.next();
                 if (curListener==null){
                     iterator.remove();
                     continue;
                 }
                 
                 if (curListener.equals(listener)){
                     iterator.remove();
                     break;
                 }
             }
             
             // push back to map
             this.messageListeners.put(amtype, listenersList);
             
         } catch (Exception e){
             log.error("Exception occurred when de-registering message listener", e);
         }
     }
 
     /**
      * perform hard reset to this object = clears entire memory
      */
     @Override
     public synchronized void reset(){
         this.queue.clear();
         
         log.info("MesageListener queues was flushed");
     }
     
     /**
      * After gateway change is needed to register as listener for all registered
      * AMtypes. MyMessageListener is registered as listener to tinyos listener.
      */
     @Override
     public synchronized void reregisterListeners(){
         if (this.amType2Message == null || this.amType2Message.isEmpty()) return;
         
         // if gateway is null - disconnected state
         if (this.gateway==null){
             log.info("Not registering listeners, gateway is empty - disconnected");
             return;
         }
         
         try{
             // register to all registered amtypes to real interface
             Iterator<Integer> iterator = this.amType2Message.keySet().iterator();
             while(iterator.hasNext()){
                 Integer amtype = iterator.next();
                 Message curMsg = this.amType2Message.get(amtype);
 
                 // register
                 this.gateway.registerListener(curMsg, this);
             }
         } catch(Exception e){
             log.error("Exception when re-registering message listeners", e);
         }
     }
     
     /**
      * The thread either executes tasks or sleep.
      */
     @Override
     public void run() {
          // do in infitite loop
         MessageReceived tmpMessage = null;            
         Iterator<MessageListener> iterator = null;
 
          while(true){
             // yield for some time, processor rest
             //this.pause(500);
             Thread.yield();
             try {
                 Thread.sleep(0, 200);
             } catch (InterruptedException ex) {
                 
             }
 
             
             // shutdown
             if (this.shutdown == true){
                 log.info("MyMessageReceiver shutting down.");
                 if (this.tasks!=null){
                     this.tasks.shutdown();
                 }
                 
                 return;
             }
 
             //  nulltest on queue itself - should not happen
             if (queue==null){
                 log.error("Queue is null - should not happen, queue is final, "
                         + "initialized in constructor. Has to exit...");
                 if (this.tasks!=null){
                     this.tasks.shutdown();
                 }
                 
                 break;
             }
          
 
              // yield for some time iff is queue empty
              if (queue == null || queue.isEmpty()) {
                  this.pause(5);
                  continue;
              }
 
              //  nulltest
              if (queue == null) {
                  continue;
              }
 
              // check queue size
              if (queue.size() > MAX_QUEUE_SIZE_TO_RESET) {
                  queue.clear();
 
                  log.warn("Warning! Input queue had to be flushed out!"
                          + "Overflow, size was greater than " + MAX_QUEUE_SIZE_TO_RESET
                          + "; " + gateway.getSource().getName() + " psrc: " + gateway.getSource().getPacketSource().getName());
                  continue;
              }
 
              // if is nonempty, select first element
              if (!queue.isEmpty()) {
                  tmpMessage = queue.remove();
              } else {
                  tmpMessage = null;
              }
 
              // end of synchronization block, check if we have some message
              if (tmpMessage == null) {
                  continue;
              }
 
              // check listener for existence
              if (!(tmpMessage instanceof MessageReceived)) {
                  log.error("Message is not instance of messageReceived - please inspect it");
                  continue;
              }
 
              // determine message received
              Message msg = tmpMessage.getMsg();
              if (msg == null) {
                  // empty message, continue
                  log.error("Message is empty in envelope", tmpMessage);
                  continue;
              }
 
              Integer amtype = Integer.valueOf(msg.amType());
 
              // is this message registered to anybody?
              if (messageListeners.containsKey(amtype) == false) {
                  // registered to no one, continue - why not 
                  // registered message arrived? Weird
                  log.warn("Message arrived but no message listener "
                          + "registered to it - how it could arrive? "
                          + "AMtype: " + amtype);
                  continue;
              }
 
              // get list of listeners interested in receiving messages of
              // this AMtype
              ConcurrentLinkedQueue<MessageListener> listenersList = messageListeners.get(amtype);
              if (listenersList == null || listenersList.isEmpty()) {
                  // list is null || empty, cannot forward to anyone
                  continue;
              }
 
              // iterate over listeners list and notify each listener 
              // in serial manner
              boolean large = queue.size() > 300;
 
              if (large) {
                  log.info(this.getName() + "; XXqsize=" + queue.size() + "; msgtime=" + tmpMessage.getTimeReceivedMili() + "; nowtime=" + System.currentTimeMillis());
              }
              iterator = listenersList.iterator();
              while (iterator.hasNext()) {
                  MessageListener curListener = iterator.next();
                  if (curListener == null) {
                      continue;
                  }
 
                  // notify this listener,, separate try block - exception
                  // can be thrown, this exception affects only one listener
                  try {
                      // notify here
                      curListener.messageReceived(tmpMessage.getI(), msg, tmpMessage.getTimeReceivedMili());
 
                      if (large) {
                          log.info(this.getName() + "; qsize=" + queue.size() + "; msgtime=" + tmpMessage.getTimeReceivedMili() + "; nowtime=" + System.currentTimeMillis());
                      }
                  } catch (Exception e) {
                      log.error("Exception during notifying listener", e);
                      continue;
                  }
              }
 
              // set message to null to release it from memory for garbage collector
              tmpMessage = null;
              
 
             // this code is probably useles since message arrived event notify 
             // is performed by notify thread            
             
 //            // test queue received
 //            synchronized(queue){
 //                if (this.queue.isEmpty()){
 //                    msgReceived=null;
 //                }
 //                else {
 //                    msgReceived=queue.remove();
 //                }
 //            }
 //
 //            // if message was null, continue to sleep - this should not happen
 //            if (msgReceived==null){
 //                continue;
 //            }
 //                
 //            try {
 //                // add message to tonotify queue if needed
 //                if (msgToSend.listener != null && msgToSend.listener.isEmpty()==false){
 //                    // do it in sycnhronized block not to interfere with reading
 //                    // threads
 //                    synchronized(this.toNotify){
 //                        this.toNotify.add(msgToSend);
 //                    }
 //                }
 //
 //                // store last sent messages
 //                this.timeLastMessageSent = System.currentTimeMillis();
 //
 //                Thread.sleep(SENT_SLEEP_TIME);
 //            } catch (Exception e) {
 //                log.error("Exception occurred in message listener", e);
 //            }
         }
     }
 
     /**
      * Return size of message queue to send
      *
      * @return
      */
     public synchronized int getQueueLength(){
         return this.queue != null ? this.queue.size() : 0;
     }
 
     @Override
     public void messageReceived(int i, Message msg) {
         // blocking?
         if (this.dropingPackets) return;
      
         //log.info("PCKT["+System.currentTimeMillis()+"]: " + i + "; AMtype: " + msg.amType() + "; src: " + msg.getSerialPacket().get_header_src());
         // really add message to queue
         MessageReceived msgReceiveed = new MessageReceived(i, msg);
         msgReceiveed.setTimeReceivedMili(System.currentTimeMillis());
         this.queue.add(msgReceiveed);
     }
 
     @Override
    public void deregisterListener(int node, Message msg, net.tinyos.message.MessageListener listener) {
         this.deregisterListener(msg, listener);
     }
 
     @Override
    public void registerListener(int node, Message msg, MessageListener listener) {
         this.registerListener(msg, listener);
     }
 
     @Override
     public void disconnectNode(ConnectedNode nh, boolean resetQueues) {
         if (nh==null){
             throw new NullPointerException("Cannot manipulate with null node");
         }
         
         // if gateway is null, nothing to disconnect here
         if (this.gateway==null){
             log.info("Nothing to disconnect from, gateway is already null");
             return;
         }
         
         // is this that node in gateway?
         if (this.gateway.equals(nh.getMoteIf())==false){
             log.info("Not connected to specified gateway");
             return;
         }
         
         // simply disconnect by setting null gateway
         this.setGateway(null, true);
     }
 
     @Override
     public void disconnectNode(ConnectedNode nh, Properties props) {
         if (nh==null){
             throw new NullPointerException("Cannot manipulate with null node");
         }
         
         this.disconnectNode(nh, true);
     }
 
     @Override
     public void connectNode(ConnectedNode nh, Properties props) {
         // gateway change in another words
         
         if (nh==null){
             throw new NullPointerException("Cannot manipulate with null node");
         }
         
         if (this.gateway!=null){
             log.info("Changing gateway connected to to new one: " + nh.getNodeId());
         }
         
         boolean reset=false;
         if (props!=null){
             String resetQueues = props.getProperty("resetQueues", "true");
             try {
                 reset = Boolean.parseBoolean(resetQueues);
             } catch(Exception e){
                 log.warn("Cannot convert resetQueues to boolean", e);
             }
         }
         
         this.setGateway(nh.getMoteIf(), reset);
     }
 
     /**
      * =========================================================================
      *
      * INTERNAL SUBCLASS NOTIFICATOR WORKER
      *
      * =========================================================================
      */
 
     /**
      * Perform notifications to listeners
      * Isolated from message sender, can be spawned multiple threads, but not recommended
      * when not tested. can cause race conditions.
      * 
      * Thread executing message arrived event for listener is separated from main listener
      * since such notification can be time expensive.
      */
     private class MessageNotifyWorker extends Thread implements Runnable {
         public MessageNotifyWorker() {
             
         }
 
         /**
          * Pausing thread
          * @param microsecs
          */
         private void pause(int mili) {
             try {
                 Thread.yield();
                 Thread.sleep(mili);
             } catch (InterruptedException ie) {
                 log.warn("Cannot sleep", ie);
             }
         }
 
         /**
          * Log if there is some logger. Uses main thread logger
          * 
          * @param s
          * @param subtype
          * @param code
          * @param severity
          */
         private void log(String s, int subtype, int code, int severity){
             log.info(s);
 //            if (logger==null) return;
 //            logger.addLogEntry(s, 57, "MessageNotifyWorker", subtype, code, severity);
         }
 
         /**
          * Main run method
          */
         @Override
         public void run() {
             
             // new message to be notified
             MessageReceived tmpMessage = null;
             log.info("Message notify worker started");
             
             Iterator<MessageListener> iterator = null;
             
             // do in infitite loop
             while (true) {
                 // shutting down
                 if (shutdown) {
                     log.info("MessageNotifyWorker shutdown");
                     return;
                 }
                 
                 if (queue.size()<50){
                     Thread.yield();
                 }
 
 //                synchronized (this) {
                     // yield for some time iff is queue empty
                     if (queue == null || queue.isEmpty()) {
                         this.pause(5);
                         continue;
                     }
 
                     //  nulltest
                     if (queue == null) {
                         continue;
                     }
 
                     // check queue size
                     if (queue.size() > MAX_QUEUE_SIZE_TO_RESET) {
                         queue.clear();
 
                         log.warn("Warning! Input queue had to be flushed out!"
                                 + "Overflow, size was greater than " + MAX_QUEUE_SIZE_TO_RESET
                                 + "; " + gateway.getSource().getName() + " psrc: " + gateway.getSource().getPacketSource().getName());
                         continue;
                     }
 
                     // if is nonempty, select first element
                     if (!queue.isEmpty()) {
                         tmpMessage = queue.remove();
                     } else {
                         tmpMessage = null;
                     }
 
                     // end of synchronization block, check if we have some message
                     if (tmpMessage == null) {
                         continue;
                     }
 
                     // check listener for existence
                     if (!(tmpMessage instanceof MessageReceived)) {
                         log.error("Message is not instance of messageReceived - please inspect it");
                         continue;
                     }
 
                     // determine message received
                     Message msg = tmpMessage.getMsg();
                     if (msg == null) {
                         // empty message, continue
                         log.error("Message is empty in envelope", tmpMessage);
                         continue;
                     }
 
                     Integer amtype = Integer.valueOf(msg.amType());
 
                     // is this message registered to anybody?
                     if (messageListeners.containsKey(amtype) == false) {
                         // registered to no one, continue - why not 
                         // registered message arrived? Weird
                         log.warn("Message arrived but no message listener "
                                 + "registered to it - how it could arrive? "
                                 + "AMtype: " + amtype);
                         continue;
                     }
 
                     // get list of listeners interested in receiving messages of
                     // this AMtype
                     ConcurrentLinkedQueue<MessageListener> listenersList = messageListeners.get(amtype);
                     if (listenersList == null || listenersList.isEmpty()) {
                         // list is null || empty, cannot forward to anyone
                         continue;
                     }
 
                     // iterate over listeners list and notify each listener 
                     // in serial manner
                     boolean large=queue.size()>300;
                     
                     if (large)
                         log.info(this.getName() + "; XXqsize=" + queue.size() + "; msgtime=" + tmpMessage.getTimeReceivedMili() + "; nowtime=" + System.currentTimeMillis());
                     iterator = listenersList.iterator();
                     while (iterator.hasNext()) {
                         MessageListener curListener = iterator.next();
                         if (curListener == null) {
                             continue;
                         }
 
                         // notify this listener,, separate try block - exception
                         // can be thrown, this exception affects only one listener
                         try {
                             // notify here
                             curListener.messageReceived(tmpMessage.getI(), msg, tmpMessage.getTimeReceivedMili());
                             
                             if (large)
                                 log.info(this.getName() + "; qsize=" + queue.size() + "; msgtime=" + tmpMessage.getTimeReceivedMili() + "; nowtime=" + System.currentTimeMillis());
                         } catch (Exception e) {
                             log.error("Exception during notifying listener", e);
                             continue;
                         }
                     }
 
                     // set message to null to release it from memory for garbage collector
                     tmpMessage = null;
                 } //end while(true)
 //            } // end of synchronized
         } // end run()
     }
 
     /**
      * =========================================================================
      *
      * GETTERS + SETTERS
      *
      * =========================================================================
      */
 
     public MoteIF getGateway() {
         return gateway;
     }
 
     public synchronized void setGateway(MoteIF gateway){
         this.setGateway(gateway, true);
     }
     
     public synchronized void setGateway(MoteIF gateway, boolean reset) {
         this.gateway = gateway;
         
         // reset internal queues
         if (reset) {
             this.reset();
         }
         
         // re-register listeners here
         this.reregisterListeners();
         
         // reset queues again
         if (reset) {
             this.reset();
         }
         log.info("Gateway changed for MessageListener");
     }
 
     public ConcurrentLinkedQueue<MessageReceived> getQueue() {
         return queue;
     }
 
     public ExecutorService getTasks() {
         return tasks;
     }
 
     public long getTimeLastMessageSent() {
         return timeLastMessageSent;
     }
 
     public void setTimeLastMessageSent(Long timeLastMessageSent) {
         this.timeLastMessageSent = timeLastMessageSent;
     }
 
     @Override
     public boolean isShutdown() {
         return shutdown;
     }
 
     @Override
     public void setShutdown(boolean shutdown) {
         this.shutdown = shutdown;
     }
 
     @Override
     public boolean isDropingPackets() {
         return dropingPackets;
     }
 
     @Override
     public void setDropingPackets(boolean dropingPackets) {
         this.dropingPackets = dropingPackets;
     }
 }

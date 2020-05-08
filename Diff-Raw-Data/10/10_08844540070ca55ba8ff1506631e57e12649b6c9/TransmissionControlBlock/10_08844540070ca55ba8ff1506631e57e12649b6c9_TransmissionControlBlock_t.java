 package nl.vu.cs.cn.tcp;
 
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import nl.vu.cs.cn.IP;
 import nl.vu.cs.cn.tcp.segment.RetransmissionSegment;
 import nl.vu.cs.cn.tcp.timeout.TimeoutHandler;
 
 /**
  * The Transmission Control Block in TCP keeps track of the state of a connection,
  * and contains all other important information needed during a connection.
  */
 public class TransmissionControlBlock {
 
     private static final int RETRANSMIT_TIMEOUT_SEC = 1;    // number of time before retransmit
     public static final int MAX_RETRANSMITS = 10;           // maximum number of retransmits
     public static final int TIME_WAIT_TIMEOUT_SEC = 5;     // number of time TIME WAIT should wait before entering CLOSE
 
     private static final short IP_HEADER_SIZE = 20;           // size of IP header in bytes
     public static final short MAX_SEGMENT_SIZE = 8 * 1024 - IP_HEADER_SIZE;    // maximum packet size in bytes
 
     private static final int MAX_RETRANSMISSION_THREADS = 5;
 
 
     private String TAG = "TCB";
 
     public enum State {
         CLOSED,
         LISTEN,
         SYN_RECEIVED,
         SYN_SENT,
         ESTABLISHED,
         FIN_WAIT_1,
         FIN_WAIT_2,
         TIME_WAIT,
         CLOSING,
         CLOSE_WAIT,
         LAST_ACK
     };
 
     private boolean isServer;   // used for logging purposes
 
     private State state;
     private final Lock stateLock = new ReentrantLock();
     private final Condition stateChanged = stateLock.newCondition();
 
     private IP.IpAddress localAddr;
     private short localPort;
     private IP.IpAddress foreignAddr;
     private short foreignPort;
 
     // sequence variables
     private int iss;      // initial send sequence number
     private int irs;            // initial receive sequence number
 
     // send sequence variables (note that window and urgent pointer info is not used)
     private int snd_una;        // send - unacknowledged sequence number
     private int snd_nxt;        // send - next sequence number
     private short snd_wnd;        // send - window (offset of snd_una)
 
     private int fin_una;        // unacknowledged FIN sequence number
 
 
     // receive sequence variables
     private int rcv_nxt;        // receive - next sequence number
     private short rcv_wnd;        // receive - window
 
     private final ScheduledExecutorService executor;
 
     private ConcurrentLinkedQueue<Byte> transmissionQueue;
     private ConcurrentLinkedQueue<Byte> processingQueue;
     private final Lock processingQueueLock = new ReentrantLock();
     private final Condition hasDataForProcessing = processingQueueLock.newCondition();
 
 
     private ConcurrentHashMap<RetransmissionSegment, ScheduledFuture> retransmissionMap;
     private final Lock retransmissionLock = new ReentrantLock();
     private final Condition retransmissionQueueChanged = retransmissionLock.newCondition();
 
     private TimeoutHandler timeoutHandler;
     private ScheduledFuture timeWaitScheduledFuture;
 
 
     /**
      * Create a new transmission control block (TCB) to hold connection state information.
      * When this method finishes the state is set to CLOSED.
      */
     public TransmissionControlBlock(IP ip, boolean isServer) {
         iss = getInitialSendSequenceNumber();
         state = State.CLOSED;
 
         executor = Executors.newScheduledThreadPool(MAX_RETRANSMISSION_THREADS);
 
         retransmissionMap = new ConcurrentHashMap<RetransmissionSegment, ScheduledFuture>();
         transmissionQueue = new ConcurrentLinkedQueue<Byte>();
         processingQueue = new ConcurrentLinkedQueue<Byte>();
 
         timeoutHandler = new TimeoutHandler(ip, this);
 
         // set isServer, used to improved logging statements
         this.isServer = isServer;
         TAG += (isServer) ? " [server]" : " [client]";
 
         // implementation specific settings: window is always max size of one packet
         snd_wnd = MAX_SEGMENT_SIZE;
         rcv_wnd = MAX_SEGMENT_SIZE;
     }
 
     /**
      * Enter a specific TCP state
      * @param state
      */
     public synchronized void enterState(State state){
         stateLock.lock();
         try {
             Log.v(TAG, "Entering state: " + state);
             this.state = state;
 
             stateChanged.signalAll();
         } finally {
             stateLock.unlock();
         }
     }
 
     /**
      * Get the current state the TCP is in.
      * @return
      */
     public synchronized State getState(){
         stateLock.lock();
         try {
             return state;
         } finally {
             stateLock.unlock();
         }
     }
 
     /**
      * Wait until the TCP state changes to one of the acceptable states
      * @param states
      */
     public void waitForStates(TransmissionControlBlock.State... states){
         ArrayList<State> acceptableStates =
                 new ArrayList<TransmissionControlBlock.State>(Arrays.asList(states));
 
         stateLock.lock();
         try {
             while(!acceptableStates.contains(state)){
                 try {
                     stateChanged.await();
                 } catch (InterruptedException e) {
                     // ignore, wait again
                 }
             }
 
             // condition has been met, done waiting!
         } finally {
             stateLock.unlock();
         }
     }
 
     /**
      * Wait until the specified sequence number has been ACKed. Note that
      * if you want to wait for the full packet to be acked, make sure to use
      * the last segment number of the packet (SEG.SEQ + SEG.LEN - 1)
      *
      * This has happened when the segment with the given sequence number is
      * removed from the retransmission queue.
      *
      * @param seqNum
      * @return true if and only if the packet has been ACKed within the number of retries
      */
     public boolean waitForAck(int seqNum){
         retransmissionLock.lock();
         try {
             int i;
             for(i=0; i<MAX_RETRANSMITS+1 && getSendUnacknowledged() <= seqNum; i++){
                 try {
                     retransmissionQueueChanged.await();
                 } catch (InterruptedException e) {
                     // ignore, wait again
                 }
             }
 
             // either the packet has been met, or the number of retransmits have been
             // reached, and the packet did not arrive
             Log.v(TAG, "Packet " + seqNum + " acnowledged? : " + (seqNum < getSendUnacknowledged()));
 
             return seqNum < getSendUnacknowledged();
         } finally {
             retransmissionLock.unlock();
         }
     }
 
 
     public boolean isServer() {
         return isServer;
     }
 
     /**
      * Set local socket address and port
      * @param localAddr
      * @param localPort
      */
     public void setLocalSocketInfo(IP.IpAddress localAddr, short localPort) {
         this.localAddr = localAddr;
         this.localPort = localPort;
     }
 
     /**
      * Get the local ip address
      * @return
      */
     public IP.IpAddress getLocalAddr(){
         return localAddr;
     }
 
     /**
      * Get the local tcp port
      * @return
      */
     public short getLocalport(){
         return localPort;
     }
 
     /**
      * Set foreign socket address and port
      * @param foreignAddr
      * @param foreignPort
      */
     public void setForeignSocketInfo(IP.IpAddress foreignAddr, short foreignPort) {
         this.foreignAddr = foreignAddr;
         this.foreignPort = foreignPort;
     }
 
     /**
      * Return true if and only if the foreign address and port have been set.
      * @return
      */
     public boolean hasForeignSocketInfo() {
         return foreignAddr != null && foreignPort != 0;
     }
 
     /**
      * Get foreign ip address
      * @return
      */
     public IP.IpAddress getForeignAddr(){
         return foreignAddr;
     }
 
     /**
      * Get foreign tcp port
      * @return
      */
     public short getForeignPort(){
         return foreignPort;
     }
 
 
 
 
     ////////////////////////
     // Sequence number methods
     ////////////////////////
 
     /**
      * Returns the initial sequence number, and creates it if this is the first call.
      * @return
      */
     public int getInitialSendSequenceNumber() {
         if(iss == 0){
             // TODO: implement correct create method
             iss = new Random().nextInt(Integer.MAX_VALUE / 10);
         }
 
         return iss;
     }
 
     /**
      * Set the initial receive sequencen number.
      * @param irs
      */
     public void setInitialReceiveSequenceNumber(int irs){
         this.irs = irs;
     }
 
     /**
      * Set send unacknowledged sequence number.
      * @param snd_una
      */
    public synchronized void setSendUnacknowledged(int snd_una){
         this.snd_una = snd_una;
     }
 
     /**
      * Get send unacknowledged sequence number.
      * @return
      */
    public synchronized int getSendUnacknowledged(){
         return snd_una;
     }
 
     /**
      * Advance send next sequence number by len.
      * @param len
      */
     public void advanceSendNext(int len){
         this.snd_nxt += len;
     }
 
     /**
      * Set send next sequence number.
      * @return
      */
     public void setSendNext(int snd_nxt){
         this.snd_nxt = snd_nxt;
     }
 
     /**
      * Get send next sequence number.
      * @return
      */
     public int getSendNext(){
         return snd_nxt;
     }
 
     /**
      * Set send window
      * @param snd_wnd
      * @return
      */
     public void setSendWindow(short snd_wnd){
         this.snd_wnd = snd_wnd;
     }
 
     public short getSendWindow(){
         return snd_wnd;
     }
 
     /**
      * Advance receive next sequence number by len
      * @param len
      */
     public void advanceReceiveNext(int len){
         this.rcv_nxt += len;
     }
 
     /**
      * Set receive next sequence number to rcv_nxt
      * @param rcv_nxt
      */
     public void setReceiveNext(int rcv_nxt){
         this.rcv_nxt = rcv_nxt;
     }
 
     /**
      * Get receive next sequence number.
      * @return
      */
     public int getReceiveNext(){
         return rcv_nxt;
     }
 
     /**
      * Get receive window.
      * @return
      */
     public int getReceiveWindow(){
         return rcv_wnd;
     }
 
     /**
      * Set unacknowledged FIN sequence number
      * @param fin_una
      */
     public void setUnacknowledgedFin(int fin_una){
         this.fin_una = fin_una;
     }
 
     /**
      * Get the unacknowledged FIN sequence number
      * @return
      */
     public int getUnacknowledgedFin(){
         return fin_una;
     }
 
 
 
     ////////////////////////
     // Data methods
     ////////////////////////
 
     /**
      * Add data to the transmission queue.
      * @param buf
      * @param offset
      * @param len
      * @return number of bytes added. This is always equal to len.
      */
     public int queueDataForTransmission(byte[] buf, int offset, int len){
         int i;
         for(i=0; i<len; i++){
             transmissionQueue.add(buf[offset+i]);
         }
         return i;
     }
 
     /**
      * Check whether or not there is data to transmit.
      * @return true if and only if there is data queued to transmit. False otherwise.
      */
     public boolean hasDataToTransmit(){
         return transmissionQueue.size() > 0;
     }
 
     /**
      * Add data to the processing queue.
      * @param buf
      * @param offset
      * @param len
      * @return number of bytes added to the queue. This is always equal to len
      */
     public int queueDataForProcessing(byte[] buf, int offset, int len){
         int i;
         for(i=0; i<len; i++){
             processingQueue.add(buf[offset+i]);
         }
 
         // notify threads waiting for data to process
         processingQueueLock.lock();
         try {
             hasDataForProcessing.signalAll();
         } finally {
             processingQueueLock.unlock();
         }
 
         return i;
     }
 
     /**
      * Check whether or not there is data to process.
      * @return true if and only if there is data queued to transmit. False otherwise
      */
     public boolean hasDataToProcess(){
         processingQueueLock.lock();
         try {
             return processingQueue.size() > 0;
         } finally {
             processingQueueLock.unlock();
         }
     }
 
     /**
      * Block until there is data to process
      */
     public void waitForDataToProcess(){
         processingQueueLock.lock();
 
         try {
             while(!hasDataToProcess()){
                 try {
                     hasDataForProcessing.await();
                 } catch (InterruptedException e) {
                     // ignore, wait again
                 }
             }
         } finally {
             processingQueueLock.unlock();
         }
     }
 
     /**
      * Write maxlen bytes of data into buf.
      * @param buf
      * @param offset
      * @param maxlen
      * @return the number of bytes written to buf
      */
     public int getDataToProcess(byte[] buf, int offset, int maxlen){
         int len = Math.min(maxlen, processingQueue.size());
         for(int i=0; i<len; i++){
             buf[offset+i] = processingQueue.remove();
         }
         return len;
     }
 
     
     ////////////////////////
     // 'Timeout' methods
     ////////////////////////
 
     /**
      * Add a segment to the retransmission queue and start a timer to check if the ACK
      * timed out.
      *
      * @param retransmissionSegment
      */
     public void addToRetransmissionQueue(final RetransmissionSegment retransmissionSegment){
         // only segments with length > 0 (so either contains data, SYN of FIN) needs to be retransmitted
         if(retransmissionSegment.getSegment().getLen() > 0){
             ScheduledFuture task = executor.schedule(new Runnable() {
                 @Override
                 public void run() {
                     timeoutHandler.onRetransmissionTimeout(retransmissionSegment);
                 }
             }, RETRANSMIT_TIMEOUT_SEC, TimeUnit.SECONDS);
 
             retransmissionMap.put(retransmissionSegment, task);
         }
     }
 
     /**
      * Remove all segments from the retransmission queue which have a sequence number
      * smaller than the ack number
      * @param ack
      */
     public void removeFromRetransmissionQueue(int ack){
         int numRemoves = 0;
         for(RetransmissionSegment segment : retransmissionMap.keySet()){
            if((segment.getSegment().getSeq() + segment.getSegment().getLen() - 1) < ack){
                 retransmissionMap.remove(segment).cancel(true);
                 Log.v(TAG, "Removed segment " + segment.getSegment().getSeq() + " from retransmission queue");
                 numRemoves++;
             }
         }
 
         // if segments have been removed (because they where ACKed) signal waiting threads
         if(numRemoves > 0){
             retransmissionLock.lock();
             try {
                 retransmissionQueueChanged.signalAll();
             } finally {
                 retransmissionLock.unlock();
             }
         }
 
     }
 
     /**
      * Remove a specific segment from the retransmission queue
      * @param retransmissionSegment
      * @return true if and only if the segment existed (and was removed)
      */
     public boolean removeFromRetransmissionQueue(RetransmissionSegment retransmissionSegment) {
         ScheduledFuture scheduledFuture = retransmissionMap.remove(retransmissionSegment);
 
         if(scheduledFuture != null){
             // Cancel scheduled task (the call to retransmit)
             scheduledFuture.cancel(true);
             return true;
         }
 
         return false;
     }
 
     public void startTimeWaitTimer(){
         if(timeWaitScheduledFuture != null){
             Log.v(TAG, "Restarting TIME WAIT timer ("+ TIME_WAIT_TIMEOUT_SEC+" sec)");
             timeWaitScheduledFuture.cancel(true);
         } else {
             Log.v(TAG, "Starting TIME WAIT timer ("+ TIME_WAIT_TIMEOUT_SEC+" sec)");
         }
 
         timeWaitScheduledFuture = executor.schedule(new Runnable() {
             @Override
             public void run() {
                 timeoutHandler.onTimeWaitTimeout();
             }
         }, TIME_WAIT_TIMEOUT_SEC, TimeUnit.SECONDS);
     }
 }

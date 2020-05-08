 package org.opennms.rancid;
 
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.opennms.rancid.RancidApiException;
 import org.opennms.rancid.RancidNode;
 import org.opennms.rancid.RancidNodeAuthentication;
 import org.opennms.rancid.RWSClientApi;
 import org.opennms.rancid.RWSResourceList;
 import org.opennms.rancid.InventoryNode;
 
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 import java.util.concurrent.locks.Condition;
 
 /**
  * This class runs Rancid provisioning commands on a separated thread.
  * It is not a singleton so it can be instantiated more than once if more 
  * provisioning threads are needed.
  * Rule of thumb is the typical: if Rancid server has high latency then use
  * more threads (4 or 5) is Rancid server has very low latency use less threads (1 or 2).
  * The provisioning queue which stores the commands to be sent to Rancid is shared
  * between all the threads.
  *
  * The provisioning command will be called "message".
  * The message is passed by OpenNMS and is inserted into the buffer (mainBuffer).
  * The presence of a message in the buffer triggers the provisioner thread (or one of them
  * if more than one threads are active), the thread will execute the related command to Rancid.
  * 
  * If the provisioning fails due to a server busy error, the provisioner will
  * insert the message into another buffer (retry buffer) along with time information
  * and number of retries already done (the time information says when the message should
  * be reworked again).
  * A separate thread (this time a singleton) will wake up every 10 secs (by default)
  * and check the presence of a message into the retry buffer, if a message is found
  * and the timestamp is equal or earlier than the current time then it is inserted into the
  * main buffer to be re-worked by the provisioner, otherwise it is re-inserted
  * into the retry buffer.
  * If the number of retries exceed a given number, the message is logged and discarded.
  * 
  * @author <a href="mailto:guglielmoincisa@gmail.com">Guglielmo Incisa </a>
  * @author <a href="http://www.opennms.org/">OpenNMS </a>
  * 
  */
 
 class Message {
     private RancidNode rn;
     private String url;
     private int operation;
     private int retry;
     private long timestamp;
     
     public RancidNode getRancidNode() {
         return rn;
     }
     public String getUrl(){
         return url;
     }
     public int getOperation(){
         return operation;
     }
     public int getRetry() {
         return retry;
     }
     public long getTimestamp(){
         return timestamp;
     }
     public void setRanciNode(RancidNode rn){
         this.rn = rn;
     }
     public void setUrl(String url){
         this.url= url;
     }
     public void incRetry(){
         retry++;
     }
     public void setTimestamp(long timestamp){
         this.timestamp = timestamp;
     }
     public Message(RancidNode rn, String url, int operation, int retry, int timestamp){
         this.rn = rn;
         this.url= url;
         this.retry = retry;
         this.operation = operation;
         this.timestamp = timestamp;
     }
     public Message(int token){
         //reserved
         // the token is used in the retry buffer and says if
         // the queue has been inspected entirely
         this.operation = token;
     }
 
 }
 
 /*
  * This is a singleton, wakes up every 10 secs (configurable) and check
  * the queue for the presence of provisioning commands
  */
 class RetryThread extends Thread {
     
     static ConcurrentLinkedQueue retryBuffer;
     
     RWS_MT_ClientApi mt;
     
     private int sleepTime = 10000;
     
     private static boolean singleton = false;
     
     private static RetryThread instance;
     
     private RetryThread() {}
     
     public static RetryThread getInstance(){
         if (instance == null) {
             instance = new RetryThread();
         }
         return instance;
     }
        
     public void setSleepTime(int sleepTime){
         this.sleepTime = sleepTime;
     }
     
     public void init(RWS_MT_ClientApi mt){
         System.out.println("RetryThread.init()");
         this.mt = mt;        
         retryBuffer = new ConcurrentLinkedQueue();  
         Message m = new Message(RWS_MT_ClientApi.TOKEN);
         retryBuffer.add(m);
     }
     
     // Check the queue if it finds the token goes to sleep
     // else check the time of the message
     // if time has elapsed the message is processed otherwise it
     // will be put in retry queue again
     public void run(){
         System.out.println("RetryThread.run() called");
         while(true){
             System.out.println("RetryThread.run() loop");
     
             Message x = (Message)retryBuffer.poll();
             if (x.getOperation() == RWS_MT_ClientApi.TOKEN){
                 
                 retryBuffer.add(x);
 
                 System.out.println("RetryThread.run() token found");
                 try {
                     Thread.sleep(sleepTime);
                 }
                 catch (InterruptedException e){
                     System.out.println(e.getMessage());
                 }
             }
             else {
                 System.out.println("RetryThread.run() message found " +x.getRancidNode().getDeviceName());
                 long i = System.currentTimeMillis();
                 System.out.println("RetryThread.run() message found timestamp " + x.getTimestamp() + " current " + i);
                 if (x.getTimestamp() <= i){
                     System.out.println("RetryThread.run() message rescheduled");
                     mt.reDoWork(x);
                 }
                 else {
                     System.out.println("RetryThread.run() message delayed");
                     retryBuffer.add(x);
                 }
             }
         }
     }
     
     public void putMessage(Message m){
         retryBuffer.add(m);
     }
 }
 
 // Threaded Provisioner
 public class RWS_MT_ClientApi extends Thread {
     
     public static int ADD_NODE =    1;
     public static int UPDATE_NODE = 2;
     public static int DELETE_NODE = 3;
     
     public static int UP_NODE = 4;
     public static int DOWN_NODE = 5;
     
     public static int TOKEN = 100; 
     
     private static long retryDelay = 30000;
     private static int maxRetry = 3;
     
     private static boolean inited = false; 
     
     private static ConcurrentLinkedQueue mainBuffer;
     
 
     
     // These are needed to put on hold the main thread if there
     // are no messages to be processed
     final private static Lock lock = new ReentrantLock();
     final private static Condition hasMessage = lock.newCondition(); 
 
      
     public void init() {
         if(inited)
             return;
         System.out.println("RWS_MT_ClientApi.init() called");
         RWSClientApi.init();        
         mainBuffer = new ConcurrentLinkedQueue();
         inited = true;
         
         RetryThread.getInstance().init(this);
         RetryThread.getInstance().start();
     }
     
     public void setRetryDelaySeconds(int seconds){
         retryDelay = seconds*1000;
     }
     
     public void setMaxRetry(int maxRetry){
         this.maxRetry = maxRetry;
     }
     
     // Main thread
     public void run(){
         System.out.println("RWS_MT_ClientApi.run() called");
         while(true){
             try {
 
                 lock.lock();
                 try {
                     while (mainBuffer.isEmpty()){
                         System.out.println("RWS_MT_ClientApi.run() await");
                         hasMessage.await();
                     }
                     System.out.println("RWS_MT_ClientApi.run() rancidIt");
                     Message x = (Message)mainBuffer.poll(); 
                     rancidIt(x);
                 } 
                 finally {
                     lock.unlock();
                 }
             }
             catch (InterruptedException e){
                 System.out.println(e.getMessage());
             }
             catch (RancidApiException e){
                 System.out.println(e.getMessage());
             }
         }
     }
     
 
     protected static void reDoWork(Message m){
         System.out.println("RWS_MT_ClientApi.reDoWork() called");
         
         lock.lock();
         
         try {
             mainBuffer.add(m);
             hasMessage.signal();
         } finally {
             lock.unlock();
         }
     }
     
     // Get provisioning command and inserts the related message
     // into the buffer
     // trigger the condition variable to execute the thread
     private void doWork(RancidNode rn, String url, int operation) throws InterruptedException {
         System.out.println("RWS_MT_ClientApi.doWork() called");
         Message m = new Message (rn, url, operation, 0, 0);
         
         lock.lock();
         
         try {
             mainBuffer.add(m);
             hasMessage.signal();
         } finally {
             lock.unlock();
         }
     }
 
     // Execute the RWS command
     // If the server is busy, retry a given number of times
     // if it still fails then throws the exception 
     private void rancidIt(Message m) throws RancidApiException{
         System.out.println("RWS_MT_ClientApi.rancidIt() called");
         try {
             if (m.getOperation() == ADD_NODE) {
                 System.out.println("RWS_MT_ClientApi.rancidIt() ADD_NODE " + m.getRancidNode().getDeviceName());
         
                RWSClientApi.createRWSRancidNode(m.getUrl(), m.getRancidNode());
             }    
             else if (m.getOperation() == UPDATE_NODE) {
                 System.out.println("RWS_MT_ClientApi.rancidIt() UPDATE_NODE" + m.getRancidNode().getDeviceName());
                 
                 RWSClientApi.updateRWSRancidNode(m.getUrl(), m.getRancidNode());   
             }
             else if (m.getOperation() == DELETE_NODE) {
                 System.out.println("RWS_MT_ClientApi.rancidIt() DELETE_NODE" + m.getRancidNode().getDeviceName());
                 
                 RWSClientApi.deleteRWSRancidNode(m.getUrl(), m.getRancidNode());
             }
         }
         catch (RancidApiException e) {
             if (e.getRancidCode() == RancidApiException.RWS_BUSY) {
                 System.out.println("RWS_MT_ClientApi.rancidIt got exception");
                 if (m.getRetry() >= maxRetry) {
                     throw(new RancidApiException("Error: Server Busy", RancidApiException.RWS_BUSY));
                 }
                 m.incRetry();
                 long i = System.currentTimeMillis()+ retryDelay;
                 System.out.println("RWS_MT_ClientApi.rancidIt inserting into retry buffer " + i);
                 m.setTimestamp(i);
                 RetryThread.getInstance().retryBuffer.add(m);
             }
         }
     }
 
     //Public methods
     public void addNode(RancidNode rn, String url) throws RancidApiException, InterruptedException{
         System.out.println("RWS_MT_ClientApi.addNode() called");
         doWork(rn, url, ADD_NODE);
     }
     public void updNode(RancidNode rn, String url) throws RancidApiException, InterruptedException{
         System.out.println("RWS_MT_ClientApi.updNode() called");
         doWork(rn, url, UPDATE_NODE);
     }
     public void delNode(RancidNode rn, String url) throws RancidApiException, InterruptedException{
         System.out.println("RWS_MT_ClientApi.delNode() called");
         doWork(rn, url, DELETE_NODE);
     }
 }

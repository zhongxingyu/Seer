 package com.jrodeo.broker.rest;
 
 import com.jrodeo.queue.internal.Q;
 import com.jrodeo.queue.internal.Queue;
 import com.jrodeo.queue.internal.QueueConfig;
 import com.jrodeo.util.csp.ChannelWaitCondition;
 import com.jrodeo.util.csp.SelectableChannel;
 import com.jrodeo.util.csp.Timer;
 import com.jrodeo.util.memory.TimeoutManager;
 import com.jrodeo.util.process.STP;
 import com.jrodeo.util.process.SelectableActionListener;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: brad_hlista
  */
 
 public class BrokerEngineImpl extends STP implements BrokerEngine {
 
     private static final long ONE_HOUR_IN_MILLIS = 60 * 60 * 1000;
 
     private HashMap<String, Queue> mapOfQueues = new HashMap<String, Queue>();
     private List<Queue> listOfQueues = new ArrayList<Queue>();
     private List<QueueConfig> queueConfigs = new ArrayList<QueueConfig>();
 
     private HashMap<Queue, LinkedList<LeaseRequest>> blockedLeaseRequestsMap = new HashMap<Queue, LinkedList<LeaseRequest>>();
 
     private TimeoutManager<String, BrokerRequest> timeoutManager = new TimeoutManager<String, BrokerRequest>(3);
 
     private final Timer timer = new Timer();
     private long timerInterval = 5000;
     private long requestTimeout = 30000;
     private SelectableChannel workQueue;
 
 
     public BrokerEngineImpl() throws InterruptedException {
         super();
     }
 
     public void shutdown() throws InterruptedException {
         super.shutdownProcess(3000);
     }
 
     public void setTimerInterval(long timerInterval) {
         this.timerInterval = timerInterval;
     }
 
     public long getTimerInterval() {
         return timerInterval;
     }
 
     public SelectableChannel getWorkQueue() {
         return workQueue;
     }
 
     public void setWorkQueue(SelectableChannel workQueue) {
         this.workQueue = workQueue;
     }
 
 
     public HashMap<String, Queue> getMapOfQueues() {
         return mapOfQueues;
     }
 
     public HashMap<Queue, LinkedList<LeaseRequest>> getBlockedLeaseRequestsMap() {
         return blockedLeaseRequestsMap;
     }
 
     public TimeoutManager<String, BrokerRequest> getTimeoutManager() {
         return timeoutManager;
     }
 
     public void setTimeoutManager(TimeoutManager<String, BrokerRequest> timeoutManager) {
         this.timeoutManager = timeoutManager;
     }
 
     public void setRequestTimeout(long requestTimeout) {
         this.requestTimeout = requestTimeout;
     }
 
     public long getRequestTimeout() {
         return requestTimeout;
     }
 
     public void addQueueConfig(QueueConfig queueConfig) {
         queueConfigs.add(queueConfig);
     }
 
     public void setQueueConfigs(List<QueueConfig> queueConfigList) {
         for (QueueConfig queueConfig : queueConfigList) {
             queueConfigs.add(queueConfig);
         }
     }
 
     public void init() throws Exception {
         setupCheckTimer();
         setupWorkQueue();
         setupQueues();
     }
 
     void setupCheckTimer() throws InterruptedException {
         SelectableActionListener heartbeat = new SelectableActionListener(timer) {
             @Override
             public void actionPerformed() throws InterruptedException {
                 try {
                 timer.read();
                 System.out.println("calling doCheckInterval...");
                 doCheckInterval();
                 timer.setTimeout(System.currentTimeMillis() + timerInterval);
                 } catch(InterruptedException ie) {
                     throw ie;
                 } catch(Exception e) {
                     e.printStackTrace();
                 }
             }
         };
 
         timer.setTimeout(System.currentTimeMillis() + timerInterval);
         timer.setEnableable(true);
 
         ChannelWaitCondition wc = addActionListener(heartbeat, requestTimeout);
         if (ChannelWaitCondition.DONE != wc.getState()) {
             throw new InterruptedException("unable to complete init()" + this);
         }
     }
 
     void setupWorkQueue() throws InterruptedException {
         SelectableActionListener workQueueListener = new SelectableActionListener(workQueue) {
             @Override
             public void actionPerformed() throws InterruptedException {
                 BrokerRequest br = (BrokerRequest) workQueue.read();
                 timeoutManager.clear(br.insertToken);
                 try {
                     br.handle();
                 } catch (IOException ioe) {
                     throw new RuntimeException(ioe);
                 }
             }
         };
         ChannelWaitCondition wc = addActionListener(workQueueListener, requestTimeout);
         if (ChannelWaitCondition.DONE != wc.getState()) {
             throw new InterruptedException("unable to complete init()" + this);
         }
     }
 
     void setupQueues() throws IOException {
         for (QueueConfig queueConfig : queueConfigs) {
             Q q = new Q(queueConfig.getFullPath(), queueConfig.getQueueName());
             q.setSegmentMax(queueConfig.getSegmentMax());
             q.load();
             System.out.println("added: " + queueConfig.getQueueName() + "  " +
                     queueConfig.getFullPath() + "  " + queueConfig.getSegmentMax());
             mapOfQueues.put(queueConfig.getQueueName(), q);
             listOfQueues.add(q);
             blockedLeaseRequestsMap.put(q, new LinkedList<LeaseRequest>());
         }
     }
 
     public void checkBlockedLeaseRequests(Queue queue) throws InterruptedException {
         LinkedList<LeaseRequest> blockedLeaseRequests = blockedLeaseRequestsMap.get(queue);
         while (blockedLeaseRequests.size() > 0) {
             LeaseRequest leaseRequest = blockedLeaseRequests.removeFirst();
             if (leaseRequest.state != BrokerRequest.TIMED_OUT) {
                 leaseRequest.handle();
                 if (leaseRequest.state == BrokerRequest.WAITING) {
                     break;
                 }
             }
         }
     }
 
     public void doCheckInterval() throws InterruptedException {
         for (Queue queue : listOfQueues) {
             boolean hasLeaseTimeouts = false;
             try {
                 queue.flush();
                 hasLeaseTimeouts = queue.timeout();
             } catch (Exception e) {
                 e.printStackTrace();
                 // todo: log
             }
 
             if (hasLeaseTimeouts) {
                 LinkedList<LeaseRequest> blockedLeaseRequests = blockedLeaseRequestsMap.get(queue);
                 while (blockedLeaseRequests.size() > 0) {
                     LeaseRequest leaseRequest = blockedLeaseRequests.removeFirst();
                    leaseRequest.handle();
                     if (leaseRequest.state == BrokerRequest.WAITING) {
                         break;
                    } else {
                        timeoutManager.clear(leaseRequest.insertToken);    // todo: do this here???
                     }
                 }
             }
         }
 
         long now = System.currentTimeMillis();
         List<BrokerRequest> requestTimeouts = timeoutManager.timeout(now);
 
         for (BrokerRequest timedOut : requestTimeouts) {
             if (timedOut instanceof LeaseRequest) {
                 // note: slow
                blockedLeaseRequestsMap.get(((LeaseRequest) timedOut).queue).remove(timedOut);
             }
             timedOut.state = BrokerRequest.TIMED_OUT;
             try {
                 timedOut.handle();
             } catch (IOException ioe) {
                 // todo: log
             }
         }
     }
 
     public ControlChannelWaitCondition createBrwc(final BrokerRequest br) {
         ControlChannelWaitCondition wc = new ControlChannelWaitCondition() {
             @Override
             public void doWork() {
                 try {
                     br.insertToken = timeoutManager.insert(br);
                     workQueue.write(br);
                 } catch (InterruptedException ie) {
                 }
             }
         };
 
         wc.setChannel(controlChannel);
         wc.setTimeoutTime(br.requestTimeoutTime);
 
         return wc;
     }
 
 
 }

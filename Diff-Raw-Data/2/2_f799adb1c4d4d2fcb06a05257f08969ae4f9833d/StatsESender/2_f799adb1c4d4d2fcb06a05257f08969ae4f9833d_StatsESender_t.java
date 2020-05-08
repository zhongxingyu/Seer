 /*
 Copyright 2013, The Sporting Exchange Limited
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
    http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
  */
 
 package com.betfair.sre.statse.client;
 
 import org.jeromq.ZMQ;
 import org.jeromq.ZMsg;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jmx.export.annotation.ManagedAttribute;
 import org.springframework.jmx.export.annotation.ManagedResource;
 
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * User: mcintyret2
  * Date: 14/08/2013
  */
 @ManagedResource
 public class StatsESender {
 
     private static final Logger LOG = LoggerFactory.getLogger(StatsESender.class);
 
     private static final int DEFAULT_QUEUE_SIZE = 1000;
 
    private TsdbCleaner cleaner = new NoOpTsdbCleaner();
 
     private int queueSize = DEFAULT_QUEUE_SIZE;
 
     private String agentAddress = "NO DEFAULT";
 
     private boolean enabled;
     private volatile boolean running;
     private Thread senderThread;
     private ZMQ.Context ctx;
     private ZMQ.Socket publisher;
 
     private AtomicLong sentCount = new AtomicLong();
     private AtomicLong droppedCount = new AtomicLong();
 
     private BlockingQueue<StatsEMsgBuilder> events;
 
     public StatsEMsgBuilder newMessageForMetric(String metric) {
         return new StatsEMsgBuilder(metric, this, cleaner);
     }
 
     void sendMessage(StatsEMsgBuilder msg) {
         if (events.offer(msg)) {
             sentCount.incrementAndGet();
         } else {
             droppedCount.incrementAndGet();
         }
     }
 
     @PostConstruct
     public void start() {
         if (enabled) {
             events = new ArrayBlockingQueue<StatsEMsgBuilder>(queueSize);
             startZeroMQ();
 
             running = true;
             senderThread = new Thread(new Runnable() {
 
                 @Override
                 public void run() {
                     processEvents();
                 }
             });
             senderThread.setDaemon(true);
             senderThread.setName("StatsEEventSender");
             senderThread.start();
         }
     }
 
     // visible for testing only
     protected void startZeroMQ() {
         // Prepare our context and publisher
         LOG.info("Starting 0MQ");
         ctx = ZMQ.context(1);
         LOG.info("Connecting to agent on address {}", agentAddress);
         publisher = ctx.socket(ZMQ.PUB);
         publisher.connect(agentAddress);
         LOG.info("Connected to agent on address {}", agentAddress);
     }
 
     @PreDestroy
     public void stop() {
         if (enabled) {
             running = false;
             senderThread.interrupt();
             try {
                 LOG.info("Waiting for sender thread to stop", agentAddress);
                 senderThread.join();
             } catch (InterruptedException e) {
                 LOG.warn("Interrupted waiting for StatsE sender.");
             }
 
             stopZeroMQ();
         }
     }
 
     // visible for testing only
     protected void stopZeroMQ() {
         LOG.info("Disconnecting from agent {}", agentAddress);
         publisher.close();
         LOG.info("Stopping 0MQ");
         ctx.term();
     }
 
     private void processEvents() {
         while (running) {
             try {
                 final StatsEMsgBuilder msg = events.take();
                 final ZMsg zmsg = makeZMsg();
                 zmsg.addString(msg.getHeader());
                 zmsg.addString(msg.getBody());
                 zmsg.send(publisher);
             } catch (InterruptedException e) {
                 LOG.warn("StatsE sender interrupted.");
             }
         }
     }
 
     protected ZMsg makeZMsg() {
         return new ZMsg();
     }
 
     @ManagedAttribute
     public long getSentCount() {
         return sentCount.get();
     }
 
     @ManagedAttribute
     public long getDroppedCount() {
         return droppedCount.get();
     }
 
     @ManagedAttribute
     public long getQueueLength() {
         return events.size();
     }
 
     @ManagedAttribute
     public boolean isEnabled() {
         return enabled;
     }
 
     boolean isRunning() {
         return running;
     }
 
     public void setQueueSize(int queueSize) {
         this.queueSize = queueSize;
     }
 
     public void setAgentAddress(String agentAddress) {
         this.agentAddress = agentAddress;
     }
 
     public void setCleaner(TsdbCleaner cleaner) {
         this.cleaner = cleaner;
     }
 
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
 
     public static void main(String[] args) throws InterruptedException {
         StatsESender sender = new StatsESender();
         sender.setEnabled(true);
         sender.setAgentAddress("tcp://localhost:14444");
         sender.setQueueSize(50);
 
         sender.start();
 
         while (true) {
             System.out.println("Sending message");
             sender.sendMessage(sender.newMessageForMetric("some metric").time(12345D));
 
             Thread.sleep(1000);
         }
     }
 }

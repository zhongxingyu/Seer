 package com.sfdc.stats;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 /**
  * @author psrinivasan
  *         Date: 9/24/12
  *         Time: 5:29 PM
  */
 public class StatsManager {
     private static final StatsManager ourInstance = new StatsManager();
     private static final Logger LOGGER = LoggerFactory.getLogger(StatsManager.class);
     private AtomicInteger completed_handshakes_count;
     private AtomicInteger completed_subscribes_count;
     private AtomicInteger completed_connects_count;
     private AtomicInteger other_http200_operations_count;
     private AtomicInteger unsuccessful_bayeux_response;
 
 
     public static StatsManager getInstance() {
         return ourInstance;
     }
 
     private StatsManager() {
         completed_handshakes_count = new AtomicInteger(0);
         completed_subscribes_count = new AtomicInteger(0);
         completed_connects_count = new AtomicInteger(0);
         other_http200_operations_count = new AtomicInteger(0);
         unsuccessful_bayeux_response = new AtomicInteger(0);
     }
 
     public int incrementHandshakeCount() {
         return completed_handshakes_count.incrementAndGet();
     }
 
     public int incrementSubscriptionCount() {
         return completed_subscribes_count.incrementAndGet();
     }
 
     public int incrementConnectCount() {
         return completed_connects_count.incrementAndGet();
     }
 
     public int incrementOtherHttp200Count() {
         return other_http200_operations_count.incrementAndGet();
     }
 
     public int incrementUnsuccessfulBayeuxResponseCount() {
         return unsuccessful_bayeux_response.incrementAndGet();
     }
 
     public int getHandshakeCount() {
         return completed_handshakes_count.get();
     }
 
     public int getSubscriptionCount() {
        return completed_handshakes_count.get();
     }
 
     public int getConnectionCount() {
        return completed_handshakes_count.get();
     }
 
     public int getOtherHttp200Count() {
         return other_http200_operations_count.get();
     }
 
     public int getUnsuccessfulBayeuxResponseCount() {
         return unsuccessful_bayeux_response.get();
     }
 }

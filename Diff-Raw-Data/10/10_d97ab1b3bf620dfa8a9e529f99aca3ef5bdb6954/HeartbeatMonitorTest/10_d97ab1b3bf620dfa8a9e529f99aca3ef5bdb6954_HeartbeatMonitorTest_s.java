 package org.yajul.util;
 
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import junit.framework.Test;
 
 import java.util.Timer;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Date;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.text.SimpleDateFormat;
 import java.text.DateFormat;
 
 import static org.yajul.util.HeartbeatMonitor.Status.*;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Logger;
 
 /**
  * Hearbeat monitor unit test
  * <br>User: Josh
  * Date: Mar 5, 2009
  * Time: 7:50:36 AM
  */
 public class HeartbeatMonitorTest extends TestCase {
     private final static Logger log = LoggerFactory.getLogger(HeartbeatMonitorTest.class);
 
     public HeartbeatMonitorTest(String n) {
         super(n);
     }
 
     public void testMonitor() throws InterruptedException {
        Timer timer = new Timer("test-timer",true);
         ExecutorService exec = Executors.newCachedThreadPool();
         int scanInterval = 100;
         HeartbeatMonitor monitor = new HeartbeatMonitor(scanInterval, exec);
         MockObserver observer = new MockObserver();
         int suspectTimeout = 20 * scanInterval;
         int failureTimeout = 30 * scanInterval;
         monitor.addObserver(observer);
         monitor.createMonitor("one", suspectTimeout, failureTimeout);
        Thread.sleep(suspectTimeout + scanInterval);
         assertEquals(SUSPECTED, observer.getStatus("one"));
        Thread.sleep((failureTimeout - suspectTimeout) + 2*scanInterval);
         assertEquals(FAILED, observer.getStatus("one"));
         monitor.heartbeat("one");
         assertEquals(RESTORED, observer.getStatus("one"));
         monitor.removeMonitor("one");
 
         monitor.createMonitor("two",0,10*scanInterval);
         assertEquals(CREATED, observer.getStatus("two"));
         for (int i = 0; i < 20 ; i++)
         {
             Thread.sleep(scanInterval);
             monitor.heartbeat("two");
             assertEquals(CREATED, observer.getStatus("two"));
         }
         monitor.clear();
         assertEquals(CANCELED, observer.getStatus("two"));
     }
 
     public static Test suite() {
         return new TestSuite(HeartbeatMonitorTest.class);
     }
 
     private static class MockObserver implements HeartbeatMonitor.HeartbeatObserver {
         private Map<String, HeartbeatMonitor.Status> statusById = new HashMap<String, HeartbeatMonitor.Status>();
 
         public void onEvent(String id, HeartbeatMonitor.Status status, long lastHeartbeat) {
             Date last = new Date(lastHeartbeat);
             DateFormat df = new SimpleDateFormat(DateFormatConstants.ISO8601_DATETIME_FORMAT);
             if (log.isDebugEnabled())
                log.debug("onEvent() : id=" + id + " status=" + status + " last=" + df.format(last));
             synchronized (this) {
                 statusById.put(id, status);
             }
         }
 
         public HeartbeatMonitor.Status getStatus(String id) {
             synchronized (this) {
                 return statusById.get(id);
             }
         }
     }
 }

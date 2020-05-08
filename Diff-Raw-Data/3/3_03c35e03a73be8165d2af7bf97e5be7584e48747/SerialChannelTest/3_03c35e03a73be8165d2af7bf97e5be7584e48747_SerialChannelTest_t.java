 package org.jrivets.event;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.jrivets.event.OnEvent;
 import org.jrivets.event.SerialEventChannel;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 public class SerialChannelTest {
 
     private final ExecutorService executor = Executors.newFixedThreadPool(2);
     
     private SerialEventChannel channel;
     
     private ArrayList<Integer> events = new ArrayList<Integer>();
     
     @Before
     public void init() {
         channel = new SerialEventChannel("testChannel", 10, executor);
         channel.addSubscriber(this);
     }
     
     @SuppressWarnings("unused")
     @OnEvent
     private synchronized void onEventInteger(Integer i) {
         events.add(i);
         notify();
     }
     
     @Test
     public synchronized void sendIntegerItself() throws InterruptedException {
         Random rnd = new Random();
         Integer expectedInt = new Integer(rnd.nextInt());
         channel.publish(expectedInt);
         wait(10000L);
         assertEquals(expectedInt, events.get(0));
     }
     
     @Test
     public synchronized void checkOrder() throws InterruptedException {
         Integer i1 = 1;
         Integer i2 = 2;
         channel.publish(i1);
         channel.publish(i2);
         wait(10000L);
         if (events.size() < 2) {
             wait(10000L);
         }
         assertEquals(i1, events.get(0));
         assertEquals(i2, events.get(1));
     }
     
     @Test
     public void blockerTest() throws InterruptedException {
         final AtomicInteger counter = new AtomicInteger(0);
         synchronized (this) {
             executor.execute(new Runnable() {
                 @Override
                 public void run() {
                     for (int i = 0; i < 20; i++) {
                         counter.addAndGet(1);
                         channel.publish(new Integer(i));
                     }
                 }
             });
             while (counter.get() < 12) {
                 Thread.sleep(10L);
             }
             Thread.sleep(50L);
             assertEquals(12, counter.get());
         }
         while (counter.get() < 20) {
             Thread.sleep(10L);
         }
        while (events.size() < 20) {
            Thread.sleep(10L);
        }
         assertEquals(20, events.size());
         for (int i = 0; i < 20 ; i++) {
             assertEquals(new Integer(i), events.get(i));
         }
     }
     
     @Test
     public synchronized void waitingTimeout() throws InterruptedException {
         channel.setWaitingTimeout(600000L); // 1 minute!
         Integer expectedInt = new Integer(1);
         channel.publish(expectedInt);
         wait(10000L);
         assertEquals(expectedInt, events.get(0));
         while(!SerialEventChannel.State.PROCESSING_WAITING.equals(channel.getState())) {
             Thread.sleep(50); // allow the handled go to sleep            
         }
         expectedInt = new Integer(2);
         channel.publish(expectedInt);
         wait(10000L);
         assertEquals(expectedInt, events.get(1));
     }
 }

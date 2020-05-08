 package com.github.rickyclarkson.monitorablefutures;
 
 import org.junit.Test;
 
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import static com.github.rickyclarkson.monitorablefutures.MonitorableExecutorService.monitorable;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 public class MonitorableFuturesTest {
     @Test
     public void withMonitorable() throws InterruptedException {
         MonitorableExecutorService service = monitorable(Executors.newSingleThreadExecutor());
         class Count extends MonitorableRunnable<Integer> {
             @Override
             public void run() {
                 for (int a = 0; a < 3; a++) {
                     try {
                         if (!updates().offer(a, 1, TimeUnit.SECONDS))
                             new IllegalStateException("Couldn't offer " + a + " to the BlockingQueue after waiting for 1 second.").printStackTrace();
                     } catch (InterruptedException e) {
                         e.printStackTrace();
                     }
                     System.out.println("Added " + a);
                 }
             }
         }
         MonitorableFuture<?, Integer> future = service.submit(new Count());
 
         for (;;) {
             final Integer integer = future.updates().poll(10, TimeUnit.MILLISECONDS);
             if (integer != null)
                 System.out.println("Progress: " + integer);
             if (integer == null)
                 continue;
 
             if (integer == 2) {
                Thread.sleep(500);
                 assertTrue(future.isDone());
                 return;
             }
             if (integer > 2) {
                 fail("The test case is faulty if the value is >2");
             }
         }
     }
 }

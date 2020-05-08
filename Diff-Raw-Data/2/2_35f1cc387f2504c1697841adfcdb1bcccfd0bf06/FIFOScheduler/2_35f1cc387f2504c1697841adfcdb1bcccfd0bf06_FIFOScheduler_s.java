 package com.cloudera.h2.mr.scheduler;
 
 import java.util.Queue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class FIFOScheduler<T> implements Scheduler<T> {
 
   private static final Logger logger = LoggerFactory
       .getLogger(FIFOScheduler.class);
 
   private Queue<T> queue;
 
   public FIFOScheduler() {
     queue = new LinkedBlockingQueue<T>();
   }
 
   @Override
   public synchronized boolean schedule(T item) {
     logger.info("Scheduling item " + item);
 
     return queue.offer(item);
   }
 
   @Override
   public T poll(long timeout, TimeUnit unit) throws InterruptedException {
     T item;
     long now;
     long endTime;
 
     item = null;
     endTime = System.currentTimeMillis() + unit.toMillis(timeout);
 
     do {
       synchronized (queue) {
         item = queue.poll();
       }
 
       now = System.currentTimeMillis();
 
       if (item == null) {
        Thread.sleep(100);
       }
     } while (item == null && now < endTime);
 
     logger.info("Returning item " + item);
 
     return item;
   }
 
   @Override
   public synchronized long getCount() {
     return queue.size();
   }
 
 }

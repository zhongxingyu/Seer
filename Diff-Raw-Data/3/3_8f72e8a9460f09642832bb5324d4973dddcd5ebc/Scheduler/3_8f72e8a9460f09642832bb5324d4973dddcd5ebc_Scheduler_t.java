 package ru.ifmo.mailru.core;
 
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 /**
  * @author Anastasia Lebedeva
  */
 public class Scheduler implements Runnable {
     private static final int POOL_SIZE = 20;
     public final int MAX_PAGE_COUNT_FROM_SITE = 50;
     public final int PART_SIZE = 1000;
     private CollectionHandler collectionHandler;
     private Thread curThread;
 
     public Scheduler(CollectionHandler collectionHandler) {
         this.collectionHandler = collectionHandler;
     }
 
     public void start() {
         curThread = new Thread(this);
         curThread.start();
     }
 
     public void stop() {
         //controller.makeSnapshot();
         curThread = null;
     }
 
     @Override
     public void run() {
         Thread thisThread = Thread.currentThread();
         while (curThread == thisThread) {
             List<WebURL> part = collectionHandler.getNextPart(this);
            ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
             for (WebURL url : part) {
                 executor.submit(new PageProcessingTask(new Page(url), collectionHandler));
             }
             try {
                 executor.shutdown();
                 executor.awaitTermination(20, TimeUnit.MINUTES);
             } catch (InterruptedException e) {
                 stop();
                 e.printStackTrace();
             }
         }
     }
 
 }

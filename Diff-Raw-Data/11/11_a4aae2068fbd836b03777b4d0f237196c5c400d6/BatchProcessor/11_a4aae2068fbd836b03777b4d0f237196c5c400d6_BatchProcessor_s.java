 package com.bonfire.processor;
 
 import java.util.List;
 
 import com.bonfire.util.concurrent.BatchBlockingQueue;
 
 public class BatchProcessor extends Thread{
     
     private volatile boolean stopProcessor;
     private BatchBlockingQueue<Runnable> taskQueue;
     
     public BatchProcessor(int batchSize, long expireTime){
         taskQueue = new BatchBlockingQueue<Runnable>(batchSize, expireTime);
     }
 
     @Override
     public void run(){
         while(!stopProcessor){
             try {
                 List<Runnable> runnables = taskQueue.takeBatch();
                 for(Runnable runnable:runnables){
                     runnable.run();
                 }
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
         }
     }
     
     public void stopProcessor(){
         stopProcessor = true;
     }
 
     public BatchBlockingQueue<Runnable> getTaskQueue() {
         return taskQueue;
     }
 }

 package com.bonfire.util.concurrent;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 public class BatchBlockingQueue<T> {
     private int batchSize;
     private long expireTime;
     private ArrayList<T> batch;
     private volatile int indexPosition = 0;
     final Lock lock = new ReentrantLock();
     final Condition putCondition = lock.newCondition();
     final Condition takeCondition = lock.newCondition();
     
     public BatchBlockingQueue(int batchSize, long expireTime){
         this.batchSize = batchSize;
         this.expireTime = expireTime;
         this.batch = new ArrayList<T>(this.batchSize);
     }
     
     public void put(T t) throws InterruptedException{
         lock.lock();
         try{
             while(batch.size()==batchSize)
                 putCondition.await();
             batch.add(indexPosition, t);
             if(indexPosition==batchSize)
                takeCondition.notify();
         }finally{
             lock.unlock();
         }
     }
     
     public List<T> takeBatch() throws InterruptedException{
         lock.lock();
         try{
             while(batch.size()!=batchSize)
                 takeCondition.await(expireTime, TimeUnit.MILLISECONDS);
             ArrayList<T> currentBatch = new ArrayList<T>(batch);
             batch.clear();
             indexPosition = 0;
            putCondition.notify();
             return currentBatch;
         }finally{
             lock.unlock();
         }
     }
 }

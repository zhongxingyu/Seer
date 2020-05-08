 package com.schoentoon.parallel.intentservice;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 import android.app.Service;
 import android.content.Intent;
 import android.os.IBinder;
 
 public abstract class ParallelIntentService extends Service {
   private static final int CORE_POOL_SIZE = 5;
   private static final int MAXIMUM_POOL_SIZE = 128;
   private static final int KEEP_ALIVE = 1;
 
   private static final ThreadFactory sThreadFactory = new ThreadFactory() {
     private final AtomicInteger mCount = new AtomicInteger(1);
 
     public Thread newThread(Runnable r) {
       return new Thread(r, "AsyncTask #" + mCount.getAndIncrement());
     }
   };
 
   private final BlockingQueue<Runnable> sPoolWorkQueue = new LinkedBlockingQueue<Runnable>(10);
 
   private final ThreadPoolExecutor ThreadPoolExecutor = new ParallelThreadPoolExecutor(CORE_POOL_SIZE,
       MAXIMUM_POOL_SIZE, KEEP_ALIVE, TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);
 
   private class ParallelThreadPoolExecutor extends ThreadPoolExecutor {
     public ParallelThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime,
         TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
       super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
     }
     protected void afterExecute(Runnable r, Throwable t) {
       super.afterExecute(r, t);
      if (getActiveCount() == 0)
         stopSelf();
     }
   }
 
   public class Task implements Runnable {
     public Task(final Intent intent) {
       this.intent = intent;
     }
     public void run() {
       onHandleIntent(intent);
     }
     private final Intent intent;
   }
 
   private boolean mRedelivery;
 
   public ParallelIntentService(String name) {
     super();
   }
 
   public ParallelIntentService() {
     super();
   }
 
   public void setIntentRedelivery(boolean enabled) {
     mRedelivery = enabled;
   }
 
   @Override
   public void onStart(Intent intent, int startId) {
     ThreadPoolExecutor.execute(new Task(intent));
   }
 
   @Override
   public int onStartCommand(Intent intent, int flags, int startId) {
     onStart(intent, startId);
     return mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY;
   }
 
   @Override
   public IBinder onBind(Intent intent) {
     return null;
   }
 
   protected abstract void onHandleIntent(Intent intent);
 }

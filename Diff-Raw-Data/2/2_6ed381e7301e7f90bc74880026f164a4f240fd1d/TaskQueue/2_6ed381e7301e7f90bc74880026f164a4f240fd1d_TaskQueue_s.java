 /* http://google-ukdev.blogspot.com/2009/01/crimes-against-code-and-using-threads.html
  * Some modification made by barclay to be consistent with formatting.
  */
 
 package net.redgeek.android.eventrend.util;
 
 import java.util.LinkedList;
 
 import android.util.Log;
 
 public class TaskQueue {
   private static final String TAG = "TaskQueue";
 
   private LinkedList<Runnable> mTasks;
   private Thread mThread;
   private boolean mRunning;
   private Runnable mInternalRunnable;
 
   private class InternalRunnable implements Runnable {
     public void run() {
       internalRun();
     }
   }
 
   public TaskQueue() {
     mTasks = new LinkedList<Runnable>();
     mInternalRunnable = new InternalRunnable();
   }
 
   public void start() {
     if (!mRunning) {
       mThread = new Thread(mInternalRunnable);
       mThread.setDaemon(true);
       mRunning = true;
       mThread.start();
     }
   }
 
   public void stop() {
     mRunning = false;
   }
 
   public void addTask(Runnable task) {
     synchronized (mTasks) {
       mTasks.addFirst(task);
       mTasks.notify(); // notify any waiting threads
     }
   }
 
   private Runnable getNextTask() {
     synchronized (mTasks) {
      if (mTasks.isEmpty()) {
         try {
           mTasks.wait();
         } catch (InterruptedException e) {
           Log.e(TAG, "Task interrupted", e);
           stop();
         }
       }
       return mTasks.removeLast();
     }
   }
 
   private void internalRun() {
     while (mRunning) {
       Runnable task = getNextTask();
       try {
         task.run();
       } catch (Throwable t) {
         Log.e(TAG, "Task threw an exception", t);
       }
     }
   }
 }

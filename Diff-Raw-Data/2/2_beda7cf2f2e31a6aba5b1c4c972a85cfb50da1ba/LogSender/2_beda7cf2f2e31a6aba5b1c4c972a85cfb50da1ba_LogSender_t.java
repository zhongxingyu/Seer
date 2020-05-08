 package com.scurab.android.rlw;
 
 import com.scurab.gwt.rlw.shared.model.LogItem;
 import com.scurab.gwt.rlw.shared.model.LogItemBlobRequest;
 import com.scurab.gwt.rlw.shared.model.LogItemResponse;
 
 import java.lang.Thread.State;
 import java.util.HashMap;
 import java.util.Objects;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.BlockingQueue;
 
 /**
  * Implementation for communication
  *
  * @author Jiri Bruchanov
  */
 class LogSender {
 
     /**
      * Items to send *
      */
     private final BlockingQueue<LogItem> mItems = new ArrayBlockingQueue<LogItem>(128);
 
     /**
      * Coodata for items
      */
     private final HashMap<LogItem, LogItemBlobRequest> mCoData = new HashMap<LogItem, LogItemBlobRequest>();
 
     private Thread mWorkingThread;
 
     private boolean mIsRunning = true;
 
     private boolean mPause = false;
 
     private ServiceConnector mConnector;
 
     private LogItem mWorkingLogItem = null;
 
     private final Object mSendingLock = new Object();
 
     public LogSender(ServiceConnector connector) {
         mConnector = connector;
         createWorkingThread();
     }
 
     private void createWorkingThread() {
         mWorkingThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 workingThreadImpl();
                 mWorkingThread = null;
             }
         }, "LogSender");
         mWorkingThread.start();
     }
 
     private void workingThreadImpl() {
         while (mIsRunning) {
             checkPause();
             try {
                 mWorkingLogItem = mItems.take();
                 synchronized (mSendingLock) {
                     LogItemResponse lir = mConnector.saveLogItem(mWorkingLogItem);
                     //check if there is a blob for write
                     LogItemBlobRequest blob = mCoData.get(mWorkingLogItem);
                     if (lir != null && blob != null) {
                         //set logid for blob item
                         blob.setLogItemID(lir.getContext().getID());
                         byte[] data = blob.getData();
                         //save data
                         mConnector.saveLogItemBlob(blob, data);
 
                         if (blob.isUncaughtError()) {
                             //delete uncaught exception => we successfully sent
                             RemoteLog.getInstance().clearUncaughtException();
                         }
                     }
                 }
             } catch (Throwable e) {
                 e.printStackTrace();
             }
             //dont forget to remove co-data
             mCoData.remove(mWorkingLogItem);
         }
     }
 
     /**
      * Check if someone paused sender
      */
     private void checkPause() {
         if (mPause) {
             synchronized (mWorkingThread) {
                 try {
                     mWorkingThread.wait();
                 } catch (InterruptedException e) {
                     //ignore waking up exception
                 }
             }
         }
     }
 
     /**
      * Enqueue new item
      *
      * @param item
      * @return true if item is enqueued
      */
     public boolean addLogItem(LogItem item) {
         return addLogItem(item, null);
     }
 
     /**
      * Enqueue new item with blob
      *
      * @param item
      * @param data
      * @return true if item is enqueued
      */
     public boolean addLogItem(LogItem item, LogItemBlobRequest data) {
         try {
             if (data != null) {
                 mCoData.put(item, data);
             }
             mItems.add(item);
             return true;
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }
 
     /**
      * Pause sending
      */
     public void pause() {
         mPause = true;
     }
 
     /**
      * Resume sending
      */
     public void resume() {
         mPause = false;
         synchronized (mWorkingThread) {
             mWorkingThread.notify();
         }
     }
 
     /**
      * Restart sending thread<br/>
      *
      * @throws IllegalStateException if there is another working thread
      */
     public void restart() {
         if (mWorkingThread != null) {
             throw new IllegalStateException("Another working thread is running!");
         }
         createWorkingThread();
     }
 
     /**
      * Block current thread till everything is send<br/>
      * It's active waiting
      */
     public void waitForEmptyQueue() {
         synchronized (mSendingLock) {
             while (mItems.size() > 0 || mCoData.size() > 0 || mWorkingThread.getState() == State.RUNNABLE) {
                 try {
                    mSendingLock.wait(50);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 }

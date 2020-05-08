 package com.razie.pubstage.life;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.razie.pub.assets.AssetKey;
 import com.razie.pub.base.ThreadContext;
 
 /**
  * stupid thread pool model
  * 
  * TODO make nice, dynamic priorities and all that
  * 
  * TODO take into account the being's breathing schedule
  * 
  * @author razvanc99
  * 
  */
 public class Lifegiver {
     static Map<AssetKey, Breather> beings        = new HashMap<AssetKey, Breather>();
     static Thread                  myThread;
     static ThreadContext           threadContext = null;                             // passed at
 
     // init
 
     public static void init(ThreadContext tc) {
         if (myThread == null) {
             threadContext = tc;
             myThread = new Thread(new Runner());
             myThread.setName("Lifegiver" + myThread.getName());
             myThread.setDaemon(true);
             myThread.start();
         }
     }
 
     public static void needstoBreathe(AssetKey key, Breather b) {
         if (myThread == null)
             throw new IllegalStateException("Lifegiver needs init() beforehand!");
 
         beings.put(key, b);
     }
 
     private static class Runner implements Runnable {
         public void run() {
             threadContext.enter();
 
             while (true) {
                 try {
                     Thread.sleep(6 * 1000);
                 } catch (InterruptedException e) {
                     // ignore
                     e.printStackTrace();
                 }
 
                 Breather b[];
 
                 synchronized (beings) {
                     b = beings.values().toArray(new Breather[0]);
                 }
 
                 for (Breather being : b) {
                     being.breathe();
 
                     try {
                         Thread.sleep(1 * 1000);
                     } catch (InterruptedException e) {
                         // don't care
                         e.printStackTrace();
                     }
 
                 }
             }
         }
     }
 }

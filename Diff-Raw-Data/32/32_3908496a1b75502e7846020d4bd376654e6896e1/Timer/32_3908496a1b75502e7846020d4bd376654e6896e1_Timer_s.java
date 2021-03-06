 /*
  * Mobicents Media Gateway
  *
  * The source code contained in this file is in in the public domain.
  * It can be used in any project or product without prior permission,
  * license or royalty payments. There is  NO WARRANTY OF ANY KIND,
  * EXPRESS, IMPLIED OR STATUTORY, INCLUDING, WITHOUT LIMITATION,
  * THE IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE,
  * AND DATA ACCURACY.  We do not warrant or make any representations
  * regarding the use of the software or the  results thereof, including
  * but not limited to the correctness, accuracy, reliability or
  * usefulness of the software.
  */
 package org.mobicents.media.server.impl.clock;
 
 import java.io.Serializable;
 
 /**
  * Provides repited execution at a reqular time intervals.
  * 
  * @author Oleg Kulikov
  */
 public class Timer implements Serializable, Runnable {
 
     public final static Quartz quartz = new Quartz();
     
     private TimerTask handler;
     
     private volatile boolean stopped = true;
     private transient Thread worker;
     
     private long lastTick;
     
     /**
      * Creates new instance of the timer.
      */
     public Timer() {
     }
 
     public void setListener(TimerTask handler) {
         this.handler = handler;
     }
 
     /**
      * Starts execution;
      */
     public void start() {
         if (stopped) {
             worker = new Thread(this, "MediaTimer");
             worker.setPriority(Thread.MAX_PRIORITY);
             stopped = false;
             lastTick = System.currentTimeMillis();
             worker.start();            
         }
     }
 
     /**
      * Terminates execution.
      */
     public void stop() {
     	if(worker != null){
     		worker.interrupt();
     	}
         if (!stopped) {
             stopped = true;
         }
     }
 
     /**
      * Heart beat signals.
      */
     public void heartBeat() {
     }
 
     @SuppressWarnings("static-access")
     private void await(long t1, long t2) {
         long delay = Quartz.HEART_BEAT - (t2 - t1);
         if (delay < 0) {
             delay = 0;
         }        
         try {
             Thread.currentThread().sleep(delay);
         } catch (InterruptedException e) {
             stopped = true;
         }
     }
     
     public void run() {
         if (handler != null) {
             handler.started();
         }
         
         while (!stopped) {
            long t1 = System.currentTimeMillis();
             if (handler != null) {
                 handler.run();
             }
            long t2 = System.currentTimeMillis();
            await(t1, t2);
         }
         
         if (handler != null) {
             handler.ended();
         }
         
     }
 }

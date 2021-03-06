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
 
 /**
  * Provides repited execution at a reqular time intervals.
  * 
  * @author Oleg Kulikov
  */
 public class Timer implements Runnable {
 
     public final static Quartz quartz = new Quartz();
     private Runnable handler;
    private boolean stopped = true;
     private Thread worker;
     
     /**
      * Creates new instance of the timer.
      */
     public Timer() {
     }
 
     public void setListener(Runnable handler) {
         this.handler = handler;
     }
 
     /**
      * Starts execution;
      */
    public synchronized void start() {
         if (stopped) {
             worker = new Thread(this, "MediaTimer");
             stopped = false;
             worker.start();            
         }
     }
 
     /**
      * Terminates execution.
      */
    public synchronized void stop() {
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
     private void await() {
         try {
             Thread.currentThread().sleep(Quartz.HEART_BEAT);
         } catch (InterruptedException e) {
             stopped = true;
         }
     }
     
     public void run() {
         while (!stopped) {
             if (handler != null) {
                 handler.run();
             }
             await();
         }
     }
 }

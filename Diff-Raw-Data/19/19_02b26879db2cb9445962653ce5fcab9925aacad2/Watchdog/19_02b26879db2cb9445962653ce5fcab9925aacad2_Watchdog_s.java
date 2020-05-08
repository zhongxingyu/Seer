 /*-
  * Copyright (c) 2011, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution. 
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.logicprobe.LogicMail.util;
 
 import net.rim.device.api.system.EventLogger;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.conf.ConnectionConfig;
 
 /**
  * This class implements watchdog functionality that is used to guard against
  * stalled synchronous I/O operations.  The watchdog timer is started with a
  * call to {@link #start()}, reset with periodic calls to {@link #kick()},
  * and canceled with a call to {@link #cancel()}.  Any attempt to call these
  * methods out of order may result in an {@link IllegalStateException}.
  * If the watchdog reaches its timeout, the timer will be implicitly canceled
  * and {@link WatchdogListener#watchdogTimeout()} will be called.
  * </p>
  */
 public class Watchdog {
     private static final long DEFAULT_TIMEOUT = 15000;
     private static final long DEFAULT_TIMEOUT_MOBILE = 30000;
     private static final long DEFAULT_TIMEOUT_WIFI = 15000;
     
     private final WatchdogListener listener;
    private final WatchdogThread watchdogThread;
     private boolean started;
     private long defaultTimeout = DEFAULT_TIMEOUT;
     
     /**
      * Instantiates a new watchdog instance.
      *
      * @param listener the listener to be notified of timeouts
      */
     public Watchdog(WatchdogListener listener) {
         if(listener == null) {
             throw new NullPointerException("Must supply a listener");
         }
         this.listener = listener;
        this.watchdogThread = new WatchdogThread();
     }
 
     /**
      * Instantiates a new disabled watchdog.
      * This method is intended to only be called through {@link #getDisabledWatchdog()}
      * to avoid usage confusion.
      */
     private Watchdog() {
         this.listener = null;
         this.watchdogThread = null;
     }
     
     /**
      * Gets a disabled watchdog instance.
      * This is intended for use by code that can operate with or without an
      * active watchdog timer, without requiring that code to constantly check
      * its configuration prior to making watchdog calls.
      *
      * @return a disabled watchdog instance
      */
     public static Watchdog getDisabledWatchdog() {
         return new Watchdog();
     }
     
     /**
      * Shutdown the watchdog timer, if it is running.
      * This will put the watchdog in a state where it can safely be reused,
      * regardless of its prior state.
      */
     public void shutdown() {
         if(listener == null) { return; }
        if(watchdogThread.isAlive()) {
             watchdogThread.shutdown();
             try {
                 watchdogThread.join();
             } catch (InterruptedException e) { }
         }
         started = false;
         defaultTimeout = DEFAULT_TIMEOUT;
     }
     
     /**
      * Gets the default timeout value used when calling {@link #start()}.
      *
      * @return the default timeout
      */
     public long getDefaultTimeout() {
         return defaultTimeout;
     }
     
     /**
      * Sets the default timeout value used when calling {@link #start()}.
      *
      * @param defaultTimeout the new default timeout
      */
     public void setDefaultTimeout(long defaultTimeout) {
         this.defaultTimeout = defaultTimeout;
     }
     
     /**
      * Sets the default timeout value to use when calling {@link #start()},
      * based on the provided network connection type.
      *
      * @param connectionType the connection type, as a {@link ConnectionConfig}
      *     transport type constant.
      */
     public void setDefaultTimeoutForConnection(int connectionType) {
         switch(connectionType) {
         case ConnectionConfig.TRANSPORT_WIFI_ONLY:
             this.defaultTimeout = DEFAULT_TIMEOUT_WIFI;
             break;
         default:
             this.defaultTimeout = DEFAULT_TIMEOUT_MOBILE;
             break;
         }
     }
     
     /**
      * Start the watchdog timer with the default timeout.
      */
     public void start() {
         start(defaultTimeout);
     }
 
     /**
      * Start the watchdog timer.
      *
      * @param timeout the timeout value to use
      */
     public void start(long timeout) {
         if(listener == null) { return; }
         if(started) {
             throw new IllegalStateException();
         }
        if(!watchdogThread.isAlive()) {
             watchdogThread.start();
         }
         watchdogThread.startTimeout(timeout);
         started = true;
     }
     
     /**
      * Kick the watchdog timer, effectively resetting the elapsed time.
      */
     public void kick() {
         if(listener == null) { return; }
        if(!started) {
             throw new IllegalStateException();
         }
         watchdogThread.kickTimeout();
     }
 
     /**
      * Cancel the watchdog timer.
      */
     public void cancel() {
         if(listener == null) { return; }
        if(!started) {
             throw new IllegalStateException();
         }
         watchdogThread.cancelTimeout();
         started = false;
     }
 
     /**
      * Checks if the watchdog timer is started.
      *
      * @return true, if it is started
      */
     public boolean isStarted() {
         return started;
     }
     
     private void notifyListener() {
         started = false;
         EventLogger.logEvent(AppInfo.GUID,
                 "Watchdog timeout".getBytes(), EventLogger.WARNING);
         listener.watchdogTimeout();
     }
 
     private class WatchdogThread extends Thread {
         private final Object monitor = new Object();
         private final Object controlLock = new Object();
         
         private boolean shutdown;
         private long timeout;
         private boolean kicked;
 
         public synchronized void start() {
             synchronized(controlLock) {
                 super.start();
                 try {
                     controlLock.wait();
                 } catch (InterruptedException e) { }
             }
         }
         
         public void run() {
             while(true) {
                 // Synchronize on the control lock, and collect the values
                 // of all the relevant control fields
                 boolean localShutdown;
                 long localTimeout;
                 synchronized(controlLock) {
                     localShutdown = this.shutdown;
                     localTimeout = this.timeout;
                     controlLock.notify();
                 }
                 if(localShutdown) { break; }
 
                 // Wait for the timeout duration, or indefinitely if disabled
                 // (i.e. localTimeout == 0)
                 synchronized(monitor) {
                     try {
                         monitor.wait(localTimeout);
                     } catch (InterruptedException e) { }
                 }
                 
                 // Synchronize on the control lock again, and figure out why
                 // the wait() call returned
                 boolean notifyListener = false;
                 synchronized(controlLock) {
                     // If we were waiting, and the timeout was not zeroed,
                     // then the watchdog timer expired. In this case, we
                     // cancel it and send a notification to the listener.
                     if(localTimeout > 0 && this.timeout > 0 && !kicked) {
                         this.timeout = 0;
                         notifyListener = true;
                     }
                     kicked = false;
                     controlLock.notify();
                 }
                 if(notifyListener) {
                     Watchdog.this.notifyListener();
                 }
             }
         }
 
         public void startTimeout(long timeout) {
             synchronized(controlLock) {
                 this.timeout = timeout;
                 synchronized(monitor) {
                     monitor.notify();
                 }
                 try {
                     controlLock.wait();
                 } catch (InterruptedException e) { }
             }
         }
 
         public void kickTimeout() {
             synchronized(controlLock) {
                 kicked = true;
                 synchronized(monitor) {
                     monitor.notify();
                 }
                 try {
                     controlLock.wait();
                 } catch (InterruptedException e) { }
             }
         }
         
         public void cancelTimeout() {
             synchronized(controlLock) {
                 this.timeout = 0;
                 synchronized(monitor) {
                     monitor.notify();
                 }
                 try {
                     controlLock.wait();
                 } catch (InterruptedException e) { }
             }
         }
         
         public void shutdown() {
             synchronized(controlLock) {
                 this.timeout = 0;
                 this.shutdown = true;
                 synchronized(monitor) {
                     monitor.notify();
                 }
                 try {
                     controlLock.wait();
                 } catch (InterruptedException e) { }
             }
         }
     }
 }

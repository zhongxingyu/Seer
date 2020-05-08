 /*
  * Copyright 2005-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.servicemix.timers.impl;
 
 import java.util.Date;
 import java.util.TimerTask;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.timers.Timer;
 import org.apache.servicemix.timers.TimerListener;
 import org.apache.servicemix.timers.TimerManager;
 
 public class TimerManagerImpl implements TimerManager {
 
     private static final Log log = LogFactory.getLog(TimerManagerImpl.class);
    private static java.util.Timer timer = new java.util.Timer();
     
     public Timer schedule(TimerListener listener, long delay) {
         if (log.isDebugEnabled()) {
             log.debug("Schedule timer " + listener + " for " + delay);
         }
         TimerImpl tt = new TimerImpl(listener);
         timer.schedule(tt, delay);
         return tt;
     }
 
     public Timer schedule(TimerListener listener, Date date) {
         if (log.isDebugEnabled()) {
             log.debug("Schedule timer " + listener + " at " + date);
         }
         TimerImpl tt = new TimerImpl(listener);
         timer.schedule(tt, date);
         return tt;
     }
     
     public void stop() {
         timer.cancel();
     }
     
     protected static class TimerImpl extends TimerTask implements Timer {
 
         private TimerListener timerListener;
         
         public TimerImpl(TimerListener timerListener) {
             this.timerListener = timerListener;
         }
         
         public boolean cancel() {
             if (log.isDebugEnabled()) {
                 log.debug("Timer " + timerListener + " cancelled");
             }
             return super.cancel();
         }
         
         public TimerListener getTimerListener() {
             return this.timerListener;
         }
 
         public void run() {
             if (log.isDebugEnabled()) {
                 log.debug("Timer " + timerListener + " expired");
             }
             this.timerListener.timerExpired(this);
         }
         
     }
 
 }

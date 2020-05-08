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
 package org.apache.servicemix.components.util;
 
 import java.util.Date;
 
 import javax.jbi.JBIException;
 import javax.resource.spi.work.Work;
 import javax.resource.spi.work.WorkManager;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.components.varscheduler.ScheduleIterator;
 import org.apache.servicemix.components.varscheduler.Scheduler;
 import org.apache.servicemix.components.varscheduler.SchedulerTask;
 import org.apache.servicemix.jbi.framework.ComponentContextImpl;
 
 /**
  * An implementation inheritence class for a component which polls some resource at periodic intervals to decide if
  * there is an event to process.
  * 
  * @version $Revision$
  */
 public abstract class PollingComponentSupport extends ComponentSupport implements Work {
     private static final Log log = LogFactory.getLog(PollingComponentSupport.class);
     private WorkManager workManager;
     private Scheduler scheduler;
     private Date firstTime;
     private long period = 5000;
     private long delay;
     private SchedulerTask schedulerTask;
     private ScheduleIterator scheduleIterator;
     private boolean started;
     private boolean scheduleExecutedFlag;
 
     /**
      * Polls the underlying resource to see if some event is required
      * 
      * @throws JBIException
      */
     public abstract void poll() throws Exception;
     
     public void release() {
     }
 
     public void run() {
         try {
             poll();
         }
         catch (Exception e) {
             log.error("Caught exception while polling: " + e, e);
         }
     }
 
     // Properties
     // -------------------------------------------------------------------------
     public WorkManager getWorkManager() {
         return workManager;
     }
 
     public void setWorkManager(WorkManager workManager) {
         this.workManager = workManager;
     }
 
     public long getDelay() {
         return delay;
     }
 
     public void setDelay(long delay) {
         this.delay = delay;
     }
 
     public Date getFirstTime() {
         return firstTime;
     }
 
     public void setFirstTime(Date firstTime) {
         this.firstTime = firstTime;
     }
 
     public long getPeriod() {
         return period;
     }
 
     public void setPeriod(long period) {
         this.period = period;
     }
 
     public Scheduler getScheduler() {
         return scheduler;
     }
 
     public void setScheduler(Scheduler scheduler) {
         this.scheduler = scheduler;
     }
     
     public synchronized void start() throws JBIException {
         if (!started) {
             started = true;
             if (schedulerTask != null) {
                 schedulerTask.cancel();
             }
             schedulerTask = new PollSchedulerTask();
             this.scheduler.schedule(schedulerTask,scheduleIterator);
         }
         super.start();
     }
 
     public synchronized void stop() throws JBIException {
         if (schedulerTask != null) {
             schedulerTask.cancel();
             schedulerTask = null;
         }
         scheduleExecutedFlag = false;
         started = false;
         super.stop();
     }
 
     public synchronized void shutDown() throws JBIException {
         stop();
         scheduler.cancel();
         super.shutDown();
     }
 
     // Implementation methods
     // -------------------------------------------------------------------------
     protected void init() throws JBIException {
         if (scheduler == null) {
             scheduler = new Scheduler(true);
         }
         if (scheduleIterator == null) {
         	scheduleIterator = new PollScheduleIterator();
         }
         if (workManager == null) {
             ComponentContextImpl context = (ComponentContextImpl) getContext();
             workManager = context.getWorkManager();
         }
         super.init();
        
     }
     
     private class PollSchedulerTask extends SchedulerTask {
     	public void run() {
 	        try {
 	            // lets run the work inside the JCA worker pools to ensure
 	            // the threads are setup correctly when we actually do stuff
	            getWorkManager().scheduleWork(PollingComponentSupport.this);
 	        }
 	        catch (Throwable e) {
 	            log.error("Failed to schedule work: " + e, e);
 	        }
     	}
     }
     
     private class PollScheduleIterator implements ScheduleIterator {
     	public Date nextExecution() {
     		long nextTime = System.currentTimeMillis();
         	if (scheduleExecutedFlag) {
         		nextTime += period;        		
         	} else {
         		if (firstTime != null) {
         			nextTime = firstTime.getTime();
         		}
         		nextTime += delay;
         		scheduleExecutedFlag = true;
         	}
     		return (started) ? new Date(nextTime) : null;
         }
     }
 }

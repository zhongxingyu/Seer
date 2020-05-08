 /* 
  * Copyright 2008-2009 the original author or authors.
  * The contents of this file are subject to the Mozilla Public License
  * Version 1.1 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  */
  
 package com.mtgi.analytics;
 
 import java.util.LinkedList;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.BeanNameAware;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.core.task.TaskExecutor;
 import org.springframework.jmx.export.annotation.ManagedAttribute;
 import org.springframework.jmx.export.annotation.ManagedOperation;
 import org.springframework.jmx.export.annotation.ManagedResource;
 
 import com.mtgi.analytics.servlet.SpringSessionContext;
 
 /**
  * <p>Standard implementation of {@link BehaviorTrackingManager}.  BehaviorEvent
  * instances are asynchronously committed to a BehaviorEventPersister when they are
  * complete; user and session IDs for the events are provided by an implementation
  * of {@link SessionContext}.</p>
  * 
  * <p>Event persistence occurs when {@link #flush()} is called (either via JMX control
  * or by the Quartz scheduler), or when the queue of uncommitted events exceeds
  * the configured threshold value.  The flush threshold can be configured with
  * {@link #setFlushThreshold(int)}.</p>
  */
@ManagedResource(objectName="com.mtgi:group=analytics,name=BehaviorTracking", 
		 		 description="Monitor and control user behavior tracking")
 public class BehaviorTrackingManagerImpl implements BehaviorTrackingManager, InitializingBean, BeanNameAware {
 
 	/*
 	 * Implementation is complicated by many issues, the first of which is performance.
 	 * Interface method calls must return as quickly as possible to prevent measurement
 	 * from interfering with application responsiveness.  Our only really expensive
 	 * operation is event persistence flush, so we do this asynchronously on a TaskExecutor.
 	 * We also log an event for each flush, so that if behavior tracking does start
 	 * to consume significant resources, we can find evidence of that in the performance
 	 * database and tune accordingly.
 	 * 
 	 * Another issue is memory.  We anticipate periodic flushing of completed events,
 	 * but if activity temporarily exceeds anticipated levels, we want to avoid an excessive
 	 * backlog of completed events waiting in memory.  So, we add a configurable flush
 	 * threshold.  When more than the threshold number of events are finished, we automatically
 	 * flush.  We also allow child events to be persisted before their parents complete, to avoid
 	 * accumulating too many events in memory for long-running batch processes.
 	 * 
 	 * We also must worry about thread-safety.  Fortunately this one is
 	 * fairly easy for us.  By definition each BehaviorEvent is tied to exactly one thread,
 	 * so we can leave its implementation completely unsynchronized.  The only place where
 	 * we have contention among threads is as completed events are added to the persistence
 	 * queue, so we must add some synchronization there.  We enter synchronized blocks
 	 * sparingly and leave quickly to avoid creating bottlenecks.
 	 * 
 	 * Finally, and perhaps of most concern, we have the issue of event lifecycle contracts.
 	 * Attempting to start or stop created events multiple times or out of sequence, to stop 
 	 * a parent event before all of its children are finished, to start child events before 
 	 * their parents, etc, can screw up our internal bookkeeping.  In the worst case this 
 	 * can create memory leaks in the form of unfinished behavior events tied to thread 
 	 * local storage. At best it means that the performance database contains unreliable information.
 	 * 
 	 * To guard against this, first the system is designed so that direct API calls from
 	 * applications should never be necessary.  Secondly, we do low-cost bookkeeping checks
 	 * on entry to lifecycle methods to make sure that everything is being done in the
 	 * proper order, both here and in BehaviorEvent.
 	 */
 	
 	private static final Log log = LogFactory.getLog(BehaviorTrackingManagerImpl.class);
 
 	private boolean warned;
 	
 	private String name;
 	private SessionContext sessionContext;
 	private BehaviorEventPersister persister;
 	private String application;
 	private int flushThreshold = 100;
 	private TaskExecutor executor;
 
 	//tracks the currently executing event on the calling thread
 	private ThreadLocal<BehaviorEvent> event = new ThreadLocal<BehaviorEvent>();
 
 	//accumulates completed root-level events waiting to be persisted
 	private LinkedList<BehaviorEvent> writeBuffer = new LinkedList<BehaviorEvent>();
 	private Object bufferSync = new Object();
 	//number of total completed events since the last flush job was queued.
 	private volatile int pendingFlush = 0;
 	//whether a flush has been requested since the last flush was run.
 	private volatile boolean flushRequested = false;
 	
 	//whether logging has been temporarily suspended.
 	private volatile boolean suspended = false;
 	
 	//task executor job to flush events to the database.
 	private Runnable flushJob = new Runnable() {
 		public void run() {
 			flush();
 		}
 	};
 	
 	public BehaviorTrackingManagerImpl() {
 	}
 	
 	public void setBeanName(String name) {
 		this.name = name;
 	}
 
 	public String getBeanName() {
 		return name;
 	}
 	
 	public BehaviorEvent createEvent(String type, String name) {
 		//TODO: stack depth limits.  hand out a singleton dummy event if there are already
 		//too many pending events waiting for closure.
 		return new BehaviorEvent(event.get(), type, name, application, 
 								 sessionContext.getContextUserId(), 
 								 sessionContext.getContextSessionId());
 	}
 
 	public void start(BehaviorEvent evt) {
 		//check bookkeeping to prevent a rogue application from screwing up our internal state.
 		if (evt.getParent() != event.get())
 			throw new IllegalStateException("Attempted to start an event that is not a child of the pending event");
 
 		evt.start();
 		//push event on stack.
 		event.set(evt);
 	}
 	
 	public void stop(BehaviorEvent evt) {
 		//check bookkeeping to prevent a rogue application from screwing up our internal state.
 		BehaviorEvent current = event.get();
 		if (evt != current)
 			throw new IllegalStateException("Attempted to stop an event that is not the current event on this thread: got " + evt + " but expected " + current);
 
 		try {
 			evt.stop();
 		} finally {
 			//pop the event stack
 			event.set(evt.getParent());
 		}
 
 		//if logging has been suspended, we just discard the finished event.
 		if (!suspended) {
 			//put event on the write queue and check if the flush
 			//threshold has been crossed.
 			synchronized (bufferSync) {
 				++pendingFlush;
 				writeBuffer.add(evt);
 			}
 			flushIfNeeded();
 		}
 	}
 
 	@ManagedAttribute(description="Returns true if event logging has been temporarily disabled with the suspend() operation.")
 	public boolean isSuspended() {
 		return suspended;
 	}
 	
 	@ManagedOperation(description="Temporarily suspend logging of behavior events.")
 	public String suspend() {
 		suspended = true;
 		return "Event logging temporarily suspended.  Use resume() to resume logging.";
 	}
 	
 	@ManagedOperation(description="Resume logging of behavior events after a previous call to suspend().")
 	public String resume() {
 		suspended = false;
 		return "Event logging resumed.";
 	}
 
 	/**
 	 * Flush any completed events to the event persister.  This operation can be called
 	 * manually via JMX, or can be called on a fixed interval via the Quartz Scheduler.
 	 * This operation results in the logging of a "flush" event to the database.
 	 * 
 	 * @return the number of events persisted
 	 */
 	@ManagedOperation(description="Immediately flush all completed events to the behavior tracking database.  Returns the number of events written to the database (not counting the flush event that is also logged)")
 	public int flush() {
 		
 		LinkedList<BehaviorEvent> oldList = null;
 		//rotate the buffer.
 		synchronized(bufferSync) {
 			oldList = writeBuffer;
 			pendingFlush -= oldList.size();
 			writeBuffer = new LinkedList<BehaviorEvent>();
 			flushRequested = false;
 		}
 		
 		//prevent no-ops from spewing a bunch of noise into the logs.
 		if (oldList.isEmpty())
 			return 0;
 
 		//we log flush events, so that we can correlate flush events to system
 		//resource spikes, and also see evidence of behavior tracking
 		//churn in the database if tuning parameters aren't set correctly.
 		
 		//we don't call our own start/stop/createEvents methods, because that could
 		//recursively lead to another flush() or other nasty problems if the flush 
 		//threshold is set too small
 		BehaviorEvent flushEvent = new FlushEvent(event.get());
 		if (!warned && !flushEvent.isRoot()) {
 			warned = true;
 			log.warn("Flush is being called from inside an application thread!  It is strongly advised the flush only be called from a dedicated, reduced-priority thread pool (are you using a SyncTaskExecutor in your spring configuration?).");
 		}
 		EventDataElement data = flushEvent.addData();
 		flushEvent.start();
 
 		int count = -1;
 		event.set(flushEvent);
 		try {
 			
 			count = persister.persist(oldList);
 			if (log.isDebugEnabled()) log.debug("Flushed " + count + " events with " + pendingFlush + " remaining");
 			
 			return count;
 			
 		} finally {
 			//restore stack state
 			event.set(flushEvent.getParent());
 			
 			data.add("count", count);
 			flushEvent.stop();
 			
 			//persist the flush event immediately.
 			LinkedList<BehaviorEvent> temp = new LinkedList<BehaviorEvent>();
 			temp.add(flushEvent);
 			persister.persist(temp);
 		}
 	}
 	
 	private void flushIfNeeded() {
 		boolean requestFlush = false;
 		synchronized (bufferSync) {
 			//avoid queueing up duplicate requests by checking the 'flushRequested' flag.
 			if (flushRequested)
 				return;
 			if (!writeBuffer.isEmpty() && pendingFlush >= flushThreshold) {
 				requestFlush = flushRequested = true;
 				if (log.isDebugEnabled()) 
 					log.debug("requesting autoflush with " + pendingFlush + " events awaiting save");
 			}
 		}
 		if (requestFlush)
 			executor.execute(flushJob);
 	}
 
 	@ManagedAttribute(description="The application name for events published by this manager")
 	public String getApplication() {
 		return application;
 	}
 	
 	@ManagedAttribute(description="The number of completed events not yet flushed")
 	public int getEventsPendingFlush() {
 		return pendingFlush;
 	}
 	
 	/**
 	 * Set the name of the application in which this manager operates, for
 	 * logging purposes.  This will be the value of {@link BehaviorEvent#getApplication()}
 	 * for all events created by this manager.
 	 */
 	@Required
 	public void setApplication(String application) {
 		this.application = application;
 	}
 
 	/**
 	 * Set a session context for the application, used to determine the
 	 * current user and session ID for a calling thread.
 	 */
 	public void setSessionContext(SessionContext sessionContext) {
 		this.sessionContext = sessionContext;
 	}
 
 	public SessionContext getSessionContext() {
 		return sessionContext;
 	}
 
 	/**
 	 * Provide a persister for saving finished events to the behavior tracking database.
 	 * @param persister
 	 */
 	@Required
 	public void setPersister(BehaviorEventPersister persister) {
 		this.persister = persister;
 	}
 
 	public BehaviorEventPersister getPersister() {
 		return persister;
 	}
 
 	/**
 	 * Provide a task executor on which persistence operations will be performed.
 	 */
 	@Required
 	public void setExecutor(TaskExecutor executor) {
 		this.executor = executor;
 	}
 
 	public TaskExecutor getExecutor() {
 		return executor;
 	}
 	
 	/**
 	 * Specify the maximum number of completed events to queue in memory before
 	 * forcing a flush to the persister.  Default is 100 if unspecified.
 	 * 
 	 * Note that this value is treated as advice and not strictly obeyed.
 	 * For example, additional events may accumulate during the time it takes to
 	 * rotate the event buffer after the flush threshold is first observed crossed.
 	 * 
 	 * In other words, persister implementations must not assume that the flush
 	 * threshold is a hard upper limit on the batch size of persistence operations.
 	 */
 	public void setFlushThreshold(int flushThreshold) {
 		this.flushThreshold = flushThreshold;
 	}
 	
 	public void afterPropertiesSet() throws Exception {
 		if (sessionContext == null) {
 			log.info("No sessionContext specified, using default implementation " + SpringSessionContext.class.getName());
 			sessionContext = new SpringSessionContext();
 		}
 	}
 
 	protected class FlushEvent extends BehaviorEvent {
 
 		private static final long serialVersionUID = 3182195013219330932L;
 
 		protected FlushEvent(BehaviorEvent parent) {
 			super(parent, "behavior-tracking", "flush", application, sessionContext.getContextUserId(), sessionContext.getContextSessionId());
 		}
 		
 	}
 
 }

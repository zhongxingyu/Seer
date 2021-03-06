 /*******************************************************************************
  * Copyright (c) 2000, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.team.internal.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.team.core.TeamException;
 
 /**
  * This class provides the infrastucture for processing/dispatching of events using a 
  * background job. This is useful in the following situations. 
  * <ul>
  * <li>an operation is potentially long running but a resposive UI is desired
  * while the operation is being performed.</li>
  * <li>a change is a POST_CHANGE delta requires further modifications to the workspace
  * which cannot be performed in the delta handler because the workspace is locked.</li>
  * <li>a data structure is not thread safe and requires serialized operations.<li> 
  * </ul>
  * </p>
  * @since 3.0
  */
 public abstract class BackgroundEventHandler {
 	
 	// Events that need to be processed
 	private List awaitingProcessing = new ArrayList();
 	
 	// The job that runs when events need to be processed
 	private Job eventHandlerJob;
 	
 	// Indicate if the event handler has been shutdown
 	private boolean shutdown;
 
 	// Accumulate exceptions that occur
 	private ExceptionCollector errors;
 	
 	// time the last dispath took
 	private long processingEventsDuration = 0L;
 
 	// time between event dispatches
 	private long DISPATCH_DELAY = 1500;
 	
 	// time to wait for messages to be queued
 	private long WAIT_DELAY = 1000;
 
 	private String jobName;
 	
 	/**
 	 * Resource event class. The type is specific to subclasses.
 	 */
 	public static class Event {
 		IResource resource;
 		int type;
 		int depth;
 		public Event(IResource resource, int type, int depth) {
 			this.resource = resource;
 			this.type = type;
 			this.depth = depth;
 		}
 		public int getDepth() {
 			return depth;
 		}
 		public IResource getResource() {
 			return resource;
 		}
 		public int getType() {
 			return type;
 		}
 		public String toString() {
 			StringBuffer buffer = new StringBuffer();
 			buffer.append("resource: "); //$NON-NLS-1$
 			buffer.append(resource.getFullPath());
 			buffer.append(" type: "); //$NON-NLS-1$
 			buffer.append(getTypeString());
 			buffer.append(" depth: "); //$NON-NLS-1$
 			buffer.append(getDepthString());
 			return buffer.toString();
 		}
 		protected String getDepthString() {
 			switch (depth) {
 				case IResource.DEPTH_ZERO :
 					return "DEPTH_ZERO"; //$NON-NLS-1$
 				case IResource.DEPTH_ONE :
 					return "DEPTH_ONE"; //$NON-NLS-1$
 				case IResource.DEPTH_INFINITE :
 					return "DEPTH_INFINITE"; //$NON-NLS-1$
 				default :
 					return "INVALID"; //$NON-NLS-1$
 			}
 		}
 		protected String getTypeString() {
 			return String.valueOf(type);
 		}
 	}
 	
 	protected BackgroundEventHandler(String jobName, String errorTitle) {
 		this.jobName = jobName;
 		errors =
 			new ExceptionCollector(
 				errorTitle,
 				TeamPlugin.ID,
 				IStatus.ERROR,
 				null /* don't log */
 		);
 		createEventHandlingJob();
 		schedule();
 	}
 	
 	/**
 	 * Create the job used for processing the events in the queue. The job stops working when
 	 * the queue is empty.
 	 */
 	protected void createEventHandlingJob() {
 		eventHandlerJob = new Job(getName()) {	
 			public IStatus run(IProgressMonitor monitor) {
 				return processEvents(monitor);
 			}
 			public boolean shouldRun() {
 				return ! isQueueEmpty();
 			}
 			public boolean shouldSchedule() {
 				return ! isQueueEmpty();
 			}
 		};
 		eventHandlerJob.addJobChangeListener(new JobChangeAdapter() {
 			public void done(IJobChangeEvent event) {
 				jobDone(event);
 			}
 		});
 		eventHandlerJob.setSystem(true);
 		eventHandlerJob.setPriority(Job.SHORT);
 	}
 
 	/**
 	 * This method is invoked when the processing job completes. The
 	 * default behavior of the handler is to restart the job if the queue
 	 * is no longer empty and to clear the queue if the handler was shut down.
 	 */
 	protected void jobDone(IJobChangeEvent event) {
 		if (isShutdown()) {
 			// The handler has been shutdown. Clean up the queue.
 			synchronized(this) {
 				awaitingProcessing.clear();
 			}
 		} else if (! isQueueEmpty()) {
 			// An event squeaked in as the job was finishing. Reschedule the job.
 			schedule();
 		}
 	}
 	
 	/**
 	 * Schedule the job to process the events now.
 	 */
 	protected void schedule() {
 		eventHandlerJob.schedule();
 	}
 	
 	/**
 	 * Shutdown the event handler. Any events on the queue will be removed from the queue
 	 * and will not be processed.
 	 */
 	public void shutdown() {
 		shutdown = true;
 		eventHandlerJob.cancel();
 	}
 	
 	/**
 	 * Returns whether the handle has been shutdown.
 	 * @return Returns whether the handle has been shutdown.
 	 */
 	public boolean isShutdown() {
 		return shutdown;
 	}
 	
 	/**
 	 * Queue the event and start the job if it's not already doing work. If the job is 
 	 * already running then notify in case it was waiting.
 	 * @param event the event to be queued
 	 */
 	protected synchronized void queueEvent(Event event, boolean front) {
 		if (Policy.DEBUG_BACKGROUND_EVENTS) {
 			System.out.println("Event queued on " + getName() + ":" + event.toString()); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (front) {
 			awaitingProcessing.add(0, event);
 		} else {
 			awaitingProcessing.add(event);
 		}
 		if (!isShutdown() && eventHandlerJob != null) {
 			if(eventHandlerJob.getState() == Job.NONE) {
 				schedule();
 			} else {
 				notify();
 			}
 		}
 	}
 	
 	/**
 	 * Return the name that is to be associated with the background job.
 	 * @return the job name
 	 */
 	protected String getName() {
 		return jobName;
 	}
 
 	/*
 	 * Return the next event that has been queued, removing it from the queue. 
 	 * @return the next event in the queue
 	 */
 	protected synchronized Event nextElement() {
 		if (isShutdown() || isQueueEmpty()) {
 			return null;
 		}
 		return (Event) awaitingProcessing.remove(0);
 	}
 	
 	protected synchronized Event peek() {
 		if (isShutdown() || isQueueEmpty()) {
 			return null;
 		}
 		return (Event) awaitingProcessing.get(0);
 	}
 	
 	/**
 	 * Return whether there are unprocessed events on the event queue.
 	 * @return whether there are unprocessed events on the queue
 	 */
 	protected synchronized boolean isQueueEmpty() {
 		return awaitingProcessing.isEmpty();
 	}
 	
 	/**
 	 * Process events from the events queue and dispatch results. This method does not
 	 * directly check for or handle cancelation of the provided monitor. However,
 	 * it does invoke <code>processEvent(Event)</code> which may check for and handle
 	 * cancelation by shuting down the receiver.
 	 * <p>
 	 * The <code>isReadyForDispatch()</code> method is used in conjuntion
 	 * with the <code>dispatchEvents(IProgressMonitor)</code> to allow
 	 * the output of the event handler to be batched in order to avoid
 	 * fine grained UI updating.
 	 * @param monitor a progress monitor
 	 */
 	protected IStatus processEvents(IProgressMonitor monitor) {
 		errors.clear();
 		try {
 			// It's hard to know how much work is going to happen
 			// since the queue can grow. Use the current queue size as a hint to
 			// an infinite progress monitor
			monitor.beginTask(null, IProgressMonitor.UNKNOWN);
 			IProgressMonitor subMonitor = Policy.infiniteSubMonitorFor(monitor, 90);
 			subMonitor.beginTask(null, 1024);
 
 			Event event;
 			processingEventsDuration = System.currentTimeMillis();
 			while ((event = nextElement()) != null && ! isShutdown()) {			 	
 				try {
 					processEvent(event, subMonitor);
 					if (Policy.DEBUG_BACKGROUND_EVENTS) {
 						System.out.println("Event processed on " + getName() + ":" + event.toString()); //$NON-NLS-1$ //$NON-NLS-2$
 					}
 					if(isReadyForDispatch(true /*wait if queue is empty*/)) {
 						dispatchEvents(Policy.subMonitorFor(subMonitor, 1));
 						eventsDispatched();
 					}
 				} catch (CoreException e) {
 					// handle exception but keep going
 					handleException(e);
 				}
 			}
 		} finally {
 			monitor.done();
 		}
 		return errors.getStatus();
 	}
 
 	protected void eventsDispatched() {
 		processingEventsDuration = System.currentTimeMillis();
 	}
 
 	/**
 	 * Notify clients of processed events.
 	 * @param monitor a progress monitor
 	 */
 	protected abstract void dispatchEvents(IProgressMonitor monitor) throws TeamException;
 
 	/**
 	 * Returns <code>true</code> if processed events should be dispatched and
 	 * <code>false</code> otherwise. Events are dispatched at regular intervals
 	 * to avoid fine grain events causing the UI to be too jumpy. Also, if the 
 	 * events queue is empty we will wait a small amount of time to allow
 	 * pending events to be queued. The queueEvent notifies when events are
 	 * queued.  
 	 * @return <code>true</code> if processed events should be dispatched and
 	 * <code>false</code> otherwise
 	 */
 	protected boolean isReadyForDispatch(boolean wait) {		
 		long duration = System.currentTimeMillis() - processingEventsDuration;
 		if(duration >= DISPATCH_DELAY) {
 			return true;
 		}
 		synchronized(this) {
 			if(! isQueueEmpty() || ! wait) {
 				return false;
 			}
 			try {
 				wait(WAIT_DELAY);
 			} catch (InterruptedException e) {
 				// just continue
 			}
 		}
 		return isQueueEmpty();
 	}
 
 	/**
 	 * Handle the exception by recording it in the errors list.
 	 * @param e
 	 */
 	protected void handleException(CoreException e) {
 		errors.handleException(e);
 		
 	}
 	
 	/**
 	 * Process the event in the context of a running background job. Subclasses may
 	 * (but are not required to) check the provided monitor for cancelation and shut down the 
 	 * receiver by invoking the <code>shutdown()</code> method.
 	 * <p>
 	 * In many cases, a background event handler will translate incoming events into outgoing
 	 * events. If this is the case, the handler should accumulate these events in the 
 	 * <code>proceessEvent</code> method and propogate them from the <code>dispatchEvent</code>
 	 * method which is invoked periodically in order to batch outgoing events and avoid
 	 * the UI becoming too jumpy.
 	 * 
 	 * @param event the <code>Event</code> to be processed
 	 * @param monitor a progress monitor
 	 */
 	protected abstract void processEvent(Event event, IProgressMonitor monitor) throws CoreException;
 
 	/**
 	 * Return the job from which the <code>processedEvent</code> method is invoked. 
 	 * @return Returns the background event handlig job.
 	 */ 
 	public Job getEventHandlerJob() {
 		return eventHandlerJob;
 	}
 }

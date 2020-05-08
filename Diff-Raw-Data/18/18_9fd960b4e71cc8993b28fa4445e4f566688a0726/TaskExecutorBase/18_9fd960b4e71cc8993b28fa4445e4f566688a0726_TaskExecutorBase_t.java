 package de.fuberlin.wiwiss.ng4j.semwebclient.threadutils;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 /**
  * A base class for threads that execute a specific type of tasks.
  *
  * @author Olaf Hartig
  */
 abstract public class TaskExecutorBase extends Thread {
 
 	// members
 
 	private boolean stopped = false;
 	private Task currentTask;
 
 	private Log log = LogFactory.getLog( TaskExecutorBase.class );
 
 
 	// initialization
 
 	protected TaskExecutorBase () {
 		setName( getClass().getSimpleName() + "_" + String.valueOf(getId()) );
 	}
 
 	// interface
 
 	/**
 	 * Returns the type of the tasks executed by this kind of task execution
 	 * threads.
 	 */
 	abstract public Class getTaskType ();
 
 	/**
 	 * Executes the given task (which is guaranteed to be of the type specified
 	 * by {@link #getTasktype}).
 	 * Note for implementations: if this thread has already been stopped (cf.
 	 * {@link #isStopped}) the execution must be stopped as quickly as possible.
 	 */
 	abstract protected void executeTask ( Task task );
 
 
 	// accessor methods
 
 	/**
 	 * Returns true if this thread is currently busy executing a task.
 	 */
 	final public boolean hasTask () {
 		return ( currentTask != null );
 	}
 
 	/**
 	 * Returns true if this thread has been stopped already.
 	 */
 	final public boolean isStopped () {
 		return stopped;
 	}
 
 
 	// operations
 
 	/**
 	 * Starts to execute the given task.
 	 */
 	final public synchronized void startTask ( Task task ) {
 
 		if ( stopped )
 			throw new IllegalStateException( "This thread '" + getName() + "' (type: " + getClass().getName() + ") has been stopped." );
 
 		if ( currentTask != null )
 			throw new IllegalStateException( "This thread '" + getName() + "' (type: " + getClass().getName() + ") is busy." );
 
 		if ( ! getTaskType().isAssignableFrom(task.getClass()) )
 			throw new IllegalArgumentException( "The given task (type: " + task.getClass().getName() + ") does not have the expected type (" + getTaskType().getName() + ") for thread '" + getName() + "' (type: " + getClass().getName() + ")." );
 
 		currentTask = task;
 		notify();
 	}
 
 	final public void run ()
 	{
 		log.debug( "Thread '" + getName() + "' (type: " + getClass().getName() + ") started." );
 
 		while ( ! stopped ) {
 			if ( currentTask != null ) {
 				long startTime = 0;
 				if ( log.isDebugEnabled() )
 					startTime = System.currentTimeMillis();
 
 				try {
 					executeTask( currentTask );
 				} catch ( RuntimeException e ) {
					log.error( "Executing the task '" + currentTask.getIdentifier() + "' for thread '" + getName() + "' (type: " + getClass().getName() + ") caused an " + e.getClass().getName() + " (" + e.getMessage() + ").", e );
 				}
 
 				if ( log.isDebugEnabled() ) {
 					long execTimeDiff = System.currentTimeMillis() - startTime;
 					log.debug( "Executing the task '" + currentTask.getIdentifier() + "' for thread '" + getName() + "' (type: " + getClass().getName() + ") took " + String.valueOf(execTimeDiff) + " milliseconds." );
 				}
 
 				currentTask = null;
 			}
 
 			try {
 				synchronized ( this ) {
 					if ( stopped )
 						break;
 
 					wait( 1000 );
 				}
 			} catch ( InterruptedException e ) {
 				// Do nothing here. An interruption happens when the thread is stopped.
 			}
 		}
 
 		log.debug( "Thread '" + getName() + "' (type: " + getClass().getName() + ") stopped." );
 	}
 
 	/**
 	 * Stops this thread.
 	 * Sets the stop status of this thread and interrupts the execution.
 	 */
 	final synchronized public void stopThread () {
 		log.debug( "Stopping thread '" + getName() + "' (type: " + getClass().getName() + ")." );
 		stopped = true;
 		interrupt();
 	}
 
 }
 
 /*
  * (c) Copyright 2006 - 2009 Christian Bizer (chris@bizer.de) All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 1. Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution. 3. The name of the author may not
  * be used to endorse or promote products derived from this software without
  * specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
  * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
  * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  */

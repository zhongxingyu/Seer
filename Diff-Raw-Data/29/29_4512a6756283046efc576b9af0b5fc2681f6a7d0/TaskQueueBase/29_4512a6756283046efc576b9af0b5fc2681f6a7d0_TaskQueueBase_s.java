 package de.fuberlin.wiwiss.ng4j.semwebclient.threadutils;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Set;
 import java.util.Queue;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 
 /**
  * A base class for task queues that manage a pool of threads which execute the
  * queued tasks.
  * This queue is implemented as a thread itself. New tasks are assigned to free
  * threads. If all threads are busy a new task is queued and its execution is
  * postponed until a thread becomes free.
  *
  * @author Olaf Hartig
  */
 abstract public class TaskQueueBase extends Thread {
 
 	// members
 
 	static final public int MAXTHREADS_DEFAULT = 10;
 	final protected int maxThreads;
 	final protected Queue<TaskExecutorBase> freeThreads = new LinkedList<TaskExecutorBase> ();
 	final protected Set<TaskExecutorBase> busyThreads = new HashSet<TaskExecutorBase> ();
 	protected Queue<Task> tasks = new LinkedList<Task> ();
 	private boolean closed = false;
 
 	private Log log = LogFactory.getLog( TaskQueueBase.class );
 
 
 	// initialization
 
 	/**
 	 * Creates a task queue with the default ({@link #MAXTHREADS_DEFAULT})
 	 * number of available threads.
 	 */
 	public TaskQueueBase () {
 		this( MAXTHREADS_DEFAULT );
 	}
 
 	/**
 	 * Creates a task queue with the given number of available threads.
 	 *
 	 * @param maxThreads size of the thread pool
 	 */
 	public TaskQueueBase ( int maxThreads ) {
 		this.maxThreads = maxThreads;
 	}
 
 
 	// interface
 
 	/**
 	 * Creates a task-specific thread.
 	 */
 	abstract protected TaskExecutorBase createThread ();
 
 
 	// accessor methods
 
 	/**
 	 * Adds the given task to the queue.
 	 */
	public synchronized void addTask ( Task task ) {
 		if ( closed )
 			throw new IllegalStateException( "This queue '" + getName() + "' (type: " + getClass().getName() + ") has been closed." );
 
		tasks.offer ( task );
 		log.debug( "Enqueued task '" + task.getIdentifier() + "' in queue '" + getName() + "' (type: " + getClass().getName() + ") - " + tasks.size() + " tasks in queue." );
 		notify();
 	}
 
 	/**
 	 * Returns true if the queue is empty and none of the threads is busy.
 	 */
 	final public boolean isIdle () {
 		return ( tasks.isEmpty() && busyThreads.isEmpty() );
 	}
 
 	/**
 	 * Returns true if this queue has been closed already.
 	 */
 	final public boolean isClosed () {
 		return closed;
 	}
 
 
 	// operations
 
 	public void run () {
 		log.debug( "Task queue '" + getName() + "' (type: " + getClass().getName() + ") started." );
 
 		initThreadPool();
 
 		Set<TaskExecutorBase> tmp = new HashSet<TaskExecutorBase> ();
 		while ( ! closed ) {
 			// move threads that finished their task to the pool of free threads
 			tmp.clear();
 			Iterator<TaskExecutorBase> it = busyThreads.iterator();
 			while ( it.hasNext() ) {
 				TaskExecutorBase t = it.next();
 				if ( ! t.hasTask() )
 					tmp.add( t );
 			}
 			busyThreads.removeAll( tmp );
 			freeThreads.addAll( tmp );
 
 			// assign queued tasks to free threads
			while ( ! tasks.isEmpty() && ! freeThreads.isEmpty() ) {
				TaskExecutorBase thread = freeThreads.remove();
				Task task = tasks.remove();
 				thread.startTask( task );
 				busyThreads.add( thread );
 
 				log.debug( "Dequeued task '" + task.getIdentifier() + "' in queue '" + getName() + "' (type: " + getClass().getName() + ") - still " + tasks.size() + " tasks in queue." );
 			}
 
 			if ( log.isDebugEnabled() && ! tasks.isEmpty() )
 				log.debug( "Not enough free threads to assign all open tasks in queue '" + getName() + "' (type: " + getClass().getName() + ")." );
 
 			try {
 				synchronized ( this ) {
 					wait(100);
 				}
 			} catch ( InterruptedException e ) {
 				throw new RuntimeException( "Task queue '" + getName() + "' (type: " + getClass().getName() + ") interrupted (" + e.getMessage() + ").", e );
 			}
 		}
 	}
 
 	/**
 	 * Stops this queue and all threads in the pool.
 	 */
 	public synchronized void close () {
 		log.debug( "Closing task queue '" + getName() + "' (type: " + getClass().getName() + ")." );
 
 		closed = true;
 
 		while ( ! freeThreads.isEmpty() ) {
 			TaskExecutorBase t = freeThreads.remove();
 			t.stopThread();
 			t.interrupt();
 		}
 
 		Iterator<TaskExecutorBase> it = busyThreads.iterator();
 		while ( it.hasNext() ) {
 			TaskExecutorBase t = it.next();
 			t.stopThread();
 			t.interrupt();
 		}
 
 		freeThreads.addAll( busyThreads );
 		busyThreads.clear();
 		tasks.clear();
 
 		notify();
 
 		log.debug( "Task queue '" + getName() + "' (type: " + getClass().getName() + ") closed." );
 	}
 
 
 	// helper methods
 
 	private void initThreadPool () {
 		for ( int i = 0; i < maxThreads; i++ ) {
 			TaskExecutorBase t = createThread();
 
 			if ( getPriority() > Thread.MIN_PRIORITY )
 				t.setPriority( getPriority() - 1 );
 
 			t.start();
 			freeThreads.offer( t );
 		}
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

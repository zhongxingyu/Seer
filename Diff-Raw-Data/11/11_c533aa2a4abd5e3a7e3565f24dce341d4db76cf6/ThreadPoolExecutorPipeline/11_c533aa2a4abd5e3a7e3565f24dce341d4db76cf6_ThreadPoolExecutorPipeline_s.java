 /*
  *  NIO Framework
  *  
  *  Copyright 2009 Boris HUISGEN <bhuisgen@hbis.fr>
  * 
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Library General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 
 package fr.hbis.ircs.lib.nio.pipeline;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.RejectedExecutionHandler;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import fr.hbis.ircs.lib.nio.Pipeline;
 import fr.hbis.ircs.lib.nio.Task;
 import fr.hbis.ircs.lib.nio.factory.PipelineThreadFactory;
 
 /**
  * The class <code>ThreadPoolExecutorPipeline</code> is a Java
  * <code>ThredPoolExecutor</code> of <code>Task</code> objects. The class is
  * thread-safe so many reactors can share the same instance.
  * 
  * @author bhuisgen
  */
 public class ThreadPoolExecutorPipeline implements Pipeline,
 		RejectedExecutionHandler
 {
 	/**
	 * Constructs a new <code>ListPipeline</code> object.
 	 * 
 	 * @param threadClass the class of the worker thread.
 	 */
 	public ThreadPoolExecutorPipeline (Class<?> threadClass)
 	{
 		this.rwLock = new ReentrantReadWriteLock ();
 		this.blockingQueue = null;
 		this.threadPoolExecutor = null;
 		this.currentThreads = 0;
 		this.waitingThreads = 0;
 		this.running = false;
 		this.name = null;
 		this.maxSize = Constants.THREADPOOLEXECUTORPIPELINE_MAXSIZE;
 		this.threadClass = threadClass;
 		this.minThreads = Constants.THREADPOOLEXECUTORPIPELINE_MINTHREADS;
 		this.maxThreads = Constants.THREADPOOLEXECUTORPIPELINE_MAXTHREADS;
 		this.threadIncrement = Constants.THREADPOOLEXECUTORPIPELINE_THREADINCREMENT;
 		this.threadPriority = Constants.THREADPOOLEXECUTORPIPELINE_THREADPRIORITY;
 		this.threadTimeout = Constants.THREADPOOLEXECUTORPIPELINE_THREADTIMEOUT;
 
 		logger.log (Level.FINE, "new pipeline created");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#init()
 	 */
 	public void init ()
 	{
 		rwLock.writeLock ().lock ();
 
 		try
 		{
 			if ((name == null) || ("".equals (name)))
 				throw new IllegalArgumentException ("invalid name");
 
 			if (minThreads < 0)
 				throw new IllegalArgumentException ("invalid minimum threads");
 
 			if (maxThreads < minThreads)
 				throw new IllegalArgumentException ("invalid maximum threads");
 
 			if (threadIncrement < 1)
 				throw new IllegalArgumentException ("invalid thread increment");
 
 			if (threadPoolExecutor != null)
 				throw new IllegalStateException (
 						"pipeline already initialized.");
 
 			if (maxSize > 0)
 				blockingQueue = new LinkedBlockingQueue<Runnable> (maxSize);
 			else
 				blockingQueue = new LinkedBlockingQueue<Runnable> ();
 
 			threadPoolExecutor = new ThreadPoolExecutor (minThreads,
 					maxThreads, threadTimeout, TimeUnit.MILLISECONDS,
 					blockingQueue, new PipelineThreadFactory (threadClass, name,
 							threadPriority), this);
 
 			logger.log (Level.FINE, "pipeline initialized");
 		}
 		finally
 		{
 			rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#start()
 	 */
 	public void start ()
 	{
 		rwLock.writeLock ().lock ();
 
 		try
 		{
 			if (threadPoolExecutor == null)
 				throw new IllegalStateException ("pipeline not initialized.");
 
 			if (running)
 				throw new IllegalStateException ("pipeline already started");
 
 			logger.log (Level.FINE, "starting pipeline");
 
 			threadPoolExecutor.prestartAllCoreThreads ();
 
 			running = true;
 
 			logger.log (Level.FINE, "pipeline started");
 		}
 		finally
 		{
 			rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#terminate()
 	 */
 	public void terminate ()
 	{
 		rwLock.writeLock ().lock ();
 
 		try
 		{
 			if (!running)
 				throw new IllegalStateException ("pipeline not started");
 
 			logger.log (Level.FINE, "stopping pipeline");
 
 			threadPoolExecutor.shutdown ();
 
 			running = false;
 
 			logger.log (Level.FINE, "pipeline terminated");
 		}
 		finally
 		{
 			rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#isStarted()
 	 */
 	public boolean isStarted ()
 	{
 		rwLock.readLock ().lock ();
 
 		try
 		{
 			return running;
 		}
 		finally
 		{
 			rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#pushTask(fr.hbis .ircs.lib.nio.Task)
 	 */
 	public synchronized void pushTask (Task task)
 	{
 		if (task == null)
 			throw new NullPointerException ();
 
 		int count = threadPoolExecutor.getQueue ().size ();
 
 		if ((maxSize > -1) && (count > maxSize))
 		{
 			logger.log (Level.WARNING, "max tasks reached, cancelling task");
 
 			task.cancel ();
 			task.getReactor ().returnTask (task);
 
 			return;
 		}
 
 		logger.log (Level.FINE, "pushing new task");
 
 		threadPoolExecutor.execute ((Runnable) task);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#popTask()
 	 */
 	public synchronized Task popTask ()
 	{
 		logger.log (Level.FINE, "popping task not possible with this pipeline");
 
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#size()
 	 */
 	public int size ()
 	{
 		rwLock.readLock ().lock ();
 
 		try
 		{
 			return threadPoolExecutor.getQueue ().size ();
 		}
 		finally
 		{
 			rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * java.util.concurrent.RejectedExecutionHandler#rejectedExecution(java.
 	 * lang.Runnable, java.util.concurrent.ThreadPoolExecutor)
 	 */
 	public void rejectedExecution (Runnable r, ThreadPoolExecutor executor)
 	{
 		Task task = (Task) r;
 
 		logger.log (Level.WARNING, "task rejected by executor, cancelling task");
 		
 		task.cancel ();
 		task.getReactor ().returnTask (task);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#getName()
 	 */
 	public String getName ()
 	{
 		rwLock.readLock ().lock ();
 
 		try
 		{
 			return name;
 		}
 		finally
 		{
 			rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#getMaxSize()
 	 */
 	public int getMaxSize ()
 	{
 		rwLock.readLock ().lock ();
 
 		try
 		{
 			return maxSize;
 		}
 		finally
 		{
 			rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#getCurrentThreads()
 	 */
 	public int getCurrentThreads ()
 	{
 		rwLock.readLock ().lock ();
 
 		try
 		{
 			return threadPoolExecutor.getPoolSize ();
 		}
 		finally
 		{
 			rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#getProcessingThreads()
 	 */
 	public int getProcessingThreads ()
 	{
 		rwLock.readLock ().lock ();
 
 		try
 		{
 			return threadPoolExecutor.getActiveCount ();
 		}
 		finally
 		{
 			rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#getWaitingThreads()
 	 */
 	public int getWaitingThreads ()
 	{
 		rwLock.readLock ().lock ();
 
 		try
 		{
 			return (threadPoolExecutor.getPoolSize () - threadPoolExecutor
 					.getActiveCount ());
 		}
 		finally
 		{
 			rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#getMaxThreads()
 	 */
 	public int getMaxThreads ()
 	{
 		rwLock.readLock ().lock ();
 
 		try
 		{
 			return maxThreads;
 		}
 		finally
 		{
 			rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#getThreadIncrement()
 	 */
 	public int getThreadIncrement ()
 	{
 		rwLock.readLock ().lock ();
 
 		try
 		{
			return -1;
 		}
 		finally
 		{
 			rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#getThreadPriority()
 	 */
 	public int getThreadPriority ()
 	{
 		rwLock.readLock ().lock ();
 
 		try
 		{
			return -1;
 		}
 		finally
 		{
 			rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#getThreadTimeout()
 	 */
 	public int getThreadTimeout ()
 	{
 		rwLock.readLock ().lock ();
 
 		try
 		{
			return -1;
 		}
 		finally
 		{
 			rwLock.readLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#setName(java.lang.String)
 	 */
 	public void setName (String name)
 	{
 		rwLock.writeLock ().lock ();
 
 		try
 		{
 			this.name = name;
 		}
 		finally
 		{
 			rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#setMaxSize(int)
 	 */
 	public void setMaxSize (int maxSize)
 	{
 		rwLock.writeLock ().lock ();
 
 		try
 		{
 			this.maxSize = maxSize;
 		}
 		finally
 		{
 			rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#setMinThreads(int)
 	 */
 	public void setMinThreads (int minThreads)
 	{
 		rwLock.writeLock ().lock ();
 
 		try
 		{
 			this.minThreads = minThreads;
 		}
 		finally
 		{
 			rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#setMaxThreads(int)
 	 */
 	public void setMaxThreads (int maxThreads)
 	{
 		rwLock.writeLock ().lock ();
 
 		try
 		{
 			this.maxThreads = maxThreads;
 		}
 		finally
 		{
 			rwLock.writeLock ().unlock ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#setThreadIncrement(int)
 	 */
 	public void setThreadIncrement (int threadIncrement)
 	{
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#setThreadPriority(int)
 	 */
 	public void setThreadPriority (int threadPriority)
 	{
 		this.threadPriority = threadPriority;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Pipeline#setThreadTimeout(int)
 	 */
 	public void setThreadTimeout (int threadTimeout)
 	{
 		this.threadTimeout = threadTimeout;
 	}
 
 	private ReentrantReadWriteLock rwLock;
 	private BlockingQueue<Runnable> blockingQueue;
 	private ThreadPoolExecutor threadPoolExecutor;
 	private int currentThreads;
 	private int waitingThreads;
 	private boolean running;
 	private String name;
 	private int maxSize;
 	private Class<?> threadClass;
 	private int minThreads;
 	private int maxThreads;
 	private int threadIncrement;
 	private int threadPriority;
 	private int threadTimeout;
 	private static Logger logger = Logger
 			.getLogger ("fr.hbis.ircs.lib.nio.pipeline.ThreadPoolExecutorPipeline");
 }

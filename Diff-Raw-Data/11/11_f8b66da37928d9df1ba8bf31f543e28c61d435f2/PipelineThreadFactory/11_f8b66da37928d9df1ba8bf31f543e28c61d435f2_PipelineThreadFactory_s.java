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
 
 package fr.hbis.ircs.lib.nio.factory;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.concurrent.ThreadFactory;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import fr.hbis.ircs.lib.nio.WorkerThread;
 
 /**
  * The class <code>PipelineThreadFactory</code> implements a thread factory
  * which create the worker threads used by a pipeline.
  * 
  * @author bhuisgen
  */
 public class PipelineThreadFactory implements ThreadFactory
 {
 	/**
 	 * Constructs a new <code>PipelineThreadFactory</code> object.
 	 * 
 	 * @param threadClass
 	 *            the class of the worker threads.
 	 * @param threadName
 	 *            the threadName of the worker threads.
 	 * @param threadPriority
 	 *            the threadPriority of the worker threads.
 	 */
 	public PipelineThreadFactory (Class<?> threadClass, String threadName,
 			int threadPriority)
 	{
 		this.threadName = threadName;
 		this.threadPriority = threadPriority;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
 	 */
 	public Thread newThread (Runnable runnable)
 	{
		Thread t = createWorkerThread (threadClass, runnable);
 
 		return t;
 	}
 
 	/**
 	 * Creates a new worker thread.
 	 * 
	 * @param threadClass
	 *            the class of a thread which must implement the interface
	 *            <code>WorkerThread</code>.
 	 * @return a <code>Thread</code> object.
 	 */
	private Thread createWorkerThread (Class<?> threadClass, Runnable runnable)
 	{
 		Constructor<?> constructor = null;
 
 		try
 		{
 			constructor = threadClass.getConstructor (new Class[] {
 					Runnable.class, String.class });
 		}
 		catch (NoSuchMethodException noSuchMethodException)
 		{
 			logger.log (Level.WARNING,
 					"unable to get constructor of thread class:"
 							+ threadClass.getName ());
 
 			return null;
 		}
 		catch (SecurityException securityException)
 		{
 			logger.log (Level.WARNING,
 					"security exception when getting constructor of thread class:"
 							+ threadClass.getName ());
 
 			return null;
 		}
 
 		WorkerThread workerThread = null;
 
 		try
 		{
 			workerThread = (WorkerThread) constructor
 					.newInstance (new Object[] { runnable,
 							threadName + "WorkerThread" });
 		}
 		catch (InstantiationException instantiationException)
 		{
 			logger.log (Level.WARNING, "unable to instantiate thread class:"
 					+ threadClass.getName ());
 
 			return null;
 		}
 		catch (IllegalAccessException illegalAccessException)
 		{
 			logger.log (Level.WARNING, "unable to access thread class: "
 					+ threadClass.getName ());
 
 			return null;
 		}
 		catch (InvocationTargetException invocationTargetException)
 		{
 			logger.log (Level.WARNING,
 					"exception during construction of object thread class: "
 							+ threadClass.getName ());
 
 			return null;
 		}
 
 		workerThread.setPriority (threadPriority);
 
 		logger.log (Level.FINE, "new worker thread created");
 
 		return (Thread) workerThread;
 	}
 
 	private Class<?> threadClass;
 	private String threadName;
 	private int threadPriority;
 	private static Logger logger = Logger
 			.getLogger ("fr.hbis.ircs.lib.nio.factory.PipelineThreadFactory");
 }

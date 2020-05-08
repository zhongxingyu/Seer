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
 
 package fr.hbis.ircs.lib.nio.task;
 
 import java.nio.channels.SelectionKey;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import fr.hbis.ircs.lib.nio.Pipeline;
 import fr.hbis.ircs.lib.nio.Reactor;
 import fr.hbis.ircs.lib.nio.Task;
 import fr.hbis.ircs.lib.nio.TaskEvent;
 import fr.hbis.ircs.lib.nio.TaskListener;
 
 /**
  * The class <code>TaskBase</code> is the abstract implementation of
  * <code>Task</code> objects.
  * 
  * @author bhuisgen
  */
 public abstract class TaskBase implements Task, TaskListener
 {
 	/**
 	 * Constructs a new <code>TaskBase</code> object.
 	 */
 	public TaskBase ()
 	{
 		this.pipeline = null;
 		this.reactor = null;
 		this.selectionKey = null;
 		this.listeners = null;
 		this.type = -1;
 		this.recycle = false;
 		this.returned = false;
 
 		logger.log (Level.FINE, "new task created");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#init()
 	 */
 	public void init ()
 	{
 		logger.log (Level.FINE, "task initialized");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#execute()
 	 */
 	public void execute ()
 	{
 		if (pipeline != null)
 		{
 			logger.log (Level.FINE,
 					"task execution deferred, pushing to pipeline");
 
 			pipeline.pushTask (this);
 		}
 		else
 		{
 			logger.log (Level.FINE, "starting task execution");
 
 			run ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Runnable#run()
 	 */
 	public void run ()
 	{
 		logger.log (Level.FINE, "running task");
 
 		try
 		{
 			doTask ();
 		}
 		catch (Exception exception)
 		{			
 			StringBuilder stackTrace = new StringBuilder ("exception during execution: " + exception.getMessage ());
 
 			for (StackTraceElement element : exception.getStackTrace ())
 			{			
 				stackTrace.append ("\n\tat " + element.getClassName () + ":"
 						+ element.getMethodName () + "("
 						+ element.getFileName () + ":"
 						+ element.getLineNumber () + ")");
 			}
 
 			logger.log (Level.SEVERE, stackTrace.toString ());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#terminate()
 	 */
 	public void terminate ()
 	{
 		if (returned)
 			return;
 
 		logger.log (Level.FINE, "task terminated");
 
 		if (getRecycle ())
 		{
 			recycle ();
 			returned = true;
 
 			getReactor ().returnTask (this);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#cancel()
 	 */
 	public void cancel ()
 	{
 		selectionKey.attach (null);
 		selectionKey.cancel ();
 		selectionKey.selector ().wakeup ();
 
 		logger.log (Level.FINE, "task cancelled");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#recycle()
 	 */
 	public void recycle ()
 	{
 		logger.log (Level.FINE, "task recycled");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#fireEvent(fr.hbis.ircs.lib.nio.TaskEvent)
 	 */
 	public void fireEvent (TaskEvent event)
 	{
 		logger.log (Level.FINE, "fire event " + event.getStatus ());
 
 		if (listeners != null)
 		{
 			for (TaskListener listener : listeners)
 			{
 				listener.onTaskEvent (event);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fr.hbis.ircs.lib.nio.TaskListener#onTaskEvent(fr.hbis.ircs.lib.nio.TaskEvent
 	 * )
 	 */
 	public void onTaskEvent (TaskEvent event)
 	{
 		logger.log (Level.FINE, "new task event: " + event.getStatus ());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fr.hbis.ircs.lib.nio.Task#addTaskListener(fr.hbis.ircs.lib.nio.TaskListener
 	 * )
 	 */
 	public void addTaskListener (TaskListener listener)
 	{
 		if (listeners == null)
 		{
 			initListeners ();
 		}
 
 		listeners.add (listener);
 
 		logger.log (Level.FINE, "new task listener added");
 	}
 
 	/**
 	 * Initializes the listeners.
 	 */
 	private void initListeners ()
 	{
 		listeners = new ArrayList<TaskListener> ();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @seefr.hbis.ircs.lib.nio.Task#removeTaskListener(fr.hbis.ircs.lib.nio.
 	 * TaskListener)
 	 */
 	public void removeTaskListener (TaskListener listener)
 	{
 		listeners.remove (listener);
 
 		logger.log (Level.FINE, "task listener removed");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#clearTaskListeners()
 	 */
 	public void clearTaskListeners ()
 	{
 		if (listeners != null)
 		{
 			listeners.clear ();
 			listeners = null;
 
 			logger.log (Level.FINE, "task listeners cleared");
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#getTaskListeners()
 	 */
 	public TaskListener[] getTaskListeners ()
 	{
 		if (listeners == null)
 		{
 			initListeners ();
 		}
 
 		return (listeners.toArray (new TaskListener[0]));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#getReactor()
 	 */
 	public Reactor getReactor ()
 	{
 		return reactor;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#getPipeline()
 	 */
 	public Pipeline getPipeline ()
 	{
 		return pipeline;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#getSelectionKey()
 	 */
 	public SelectionKey getSelectionKey ()
 	{
 		return selectionKey;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#getType()
 	 */
 	public int getType ()
 	{
 		return type;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#getIdleTime()
 	 */
 	public int getIdleTime ()
 	{
 		return idleTime;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#getRecycle()
 	 */
 	public boolean getRecycle ()
 	{
 		return recycle;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#setReactor(fr.hbis.ircs.lib.nio.Reactor)
 	 */
 	public void setReactor (Reactor reactor)
 	{
 		this.reactor = reactor;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#setPipeline(fr.hbis.ircs.lib.nio.Pipeline)
 	 */
 	public void setPipeline (Pipeline pipeline)
 	{
 		this.pipeline = pipeline;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fr.hbis.ircs.lib.nio.Task#setSelectionKey(java.nio.channels.SelectionKey)
 	 */
 	public void setSelectionKey (SelectionKey selectionKey)
 	{
 		this.selectionKey = selectionKey;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#setType(int)
 	 */
 	public void setType (int type)
 	{
 		this.type = type;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#setIdleTime(int)
 	 */
 	public void setIdleTime (int idleTime)
 	{
 		this.idleTime = idleTime;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#setRecycle(boolean)
 	 */
 	public void setRecycle (boolean recycleTasks)
 	{
 		this.recycle = recycleTasks;
 	}
 
 	private Pipeline pipeline;
 	private Reactor reactor;
 	private SelectionKey selectionKey;
 	private List<TaskListener> listeners;
 	private int type;
 	private int idleTime;
 	private boolean recycle;
 	private boolean returned;
 	private static Logger logger = Logger
 			.getLogger ("fr.hbis.ircs.lib.nio.task.TaskBase");
 }

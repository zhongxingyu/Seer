 /*
  *  hbIRCS
  *  
  *  Copyright 2005 Boris HUISGEN <bhuisgen@hbis.fr>
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
 
 package fr.hbis.ircs.nio;
 
 import java.io.IOException;
 import java.nio.channels.CancelledKeyException;
 import java.nio.channels.ClosedChannelException;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.SocketChannel;
 import java.util.Iterator;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import fr.hbis.ircs.Define;
 import fr.hbis.ircs.Manager;
 import fr.hbis.ircs.lib.nio.Algorithm;
 import fr.hbis.ircs.lib.nio.Pipeline;
 import fr.hbis.ircs.lib.nio.Task;
 import fr.hbis.ircs.lib.nio.algorithm.CRLFAlgorithm;
 import fr.hbis.ircs.lib.nio.algorithm.LineAlgorithm;
 import fr.hbis.ircs.lib.nio.pipeline.ListPipeline;
 import fr.hbis.ircs.lib.nio.reactor.ReactorBase;
 import fr.hbis.ircs.lib.nio.worker.DefaultWorkerThread;
 
 /**
  * The class <code>SubReactor</code> implements the reactor which manage
  * <code>MessageTask</code> tasks to read data from clients.
  * 
  * @author Boris HUISGEN
  */
 public class SubReactor extends ReactorBase
 {
 	/**
 	 * Constructs a new <code>SubReactor</code> object.
 	 */
 	private SubReactor ()
 	{
 		m_manager = null;
 		m_mainReactor = null;
 		m_reactorData = null;
 		m_selector = null;
 		m_selectorGuard = new Object ();
 		m_nSelectorTimeout = -1;
 		m_bSelectorAllowIO = false;
 		m_qTasks = new ConcurrentLinkedQueue<MessageTask> ();
 		m_nQueueTaskSize = 1024;
 		m_pipeline = null;
 		m_bPipeline = false;
 		m_nPipelineMaxTasks = -1;
 		m_nPipelineMinThreads = 16;
 		m_nPipelineMaxThreads = 64;
 		m_nPipelineThreadIncrement = 4;
 		m_nPipelineThreadPriority = Thread.NORM_PRIORITY;
 		m_nPipelineThreadTimeout = 60;
 		m_bTaskUseDirectBuffer = true;
 		m_bTaskUseBufferView = true;
 		m_bInitialized = false;
 		m_bRunning = false;
 	}
 
 	/**
 	 * Creates a new <code>SubReactor</code> object.
 	 * 
 	 * @param manager
 	 *            the manager.
 	 * @param mainReactor
 	 *            the main reactor.
 	 * @param reactorData
 	 *            the client reactor data.
 	 * @return the <code>MainReactor</code> object of the reactor.
 	 */
 	public static final SubReactor create (Manager manager,
 			MainReactor mainReactor, ReactorData reactorData)
 	{
 		if (manager == null)
 			throw new IllegalArgumentException ("invalid manager");
 
 		if (mainReactor == null)
 			throw new IllegalArgumentException ("invalid main reactor");
 
 		if (reactorData == null)
 			throw new IllegalArgumentException ("invalid reactor data");
 
 		SubReactor subReactor = new SubReactor ();
 
 		subReactor.m_manager = manager;
 		subReactor.m_mainReactor = mainReactor;
 		subReactor.m_reactorData = reactorData;
 		subReactor.m_nSelectorTimeout = reactorData.getSubSelectorTimeout ();
 		subReactor.m_bSelectorAllowIO = reactorData.getSubSelectorAllowIO ();
 		subReactor.m_nQueueTaskSize = reactorData.getSubQueueTaskSize ();
 		subReactor.m_bPipeline = reactorData.getSubPipeline ();
 		subReactor.m_nPipelineMaxTasks = reactorData.getSubPipelineMaxTasks ();
 		subReactor.m_nPipelineMinThreads = reactorData
 				.getSubPipelineMinThreads ();
 		subReactor.m_nPipelineMaxThreads = reactorData
 				.getSubPipelineMaxThreads ();
 		subReactor.m_nPipelineThreadIncrement = reactorData
 				.getSubPipelineThreadIncrement ();
 		subReactor.m_nPipelineThreadTimeout = reactorData
 				.getSubPipelineThreadTimeout ();
 		subReactor.m_nPipelineThreadPriority = reactorData
 				.getSubPipelineThreadPriority ();
 		subReactor.m_bTaskRecycle = reactorData.getSubTaskRecycle ();
 		subReactor.m_bTaskUseDirectBuffer = reactorData
 				.getSubTaskUseDirectBuffer ();
 		subReactor.m_bTaskUseBufferView = reactorData
 				.getSubTaskUseBufferView ();
 
 		subReactor.setName (mainReactor.getName ());
 
 		m_logger.log (Level.FINE, "reactor created");
 
 		return (subReactor);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Reactor#init()
 	 */
 	public void init ()
 	{
 		if (m_bInitialized)
 			throw new IllegalStateException ("reactor already initialized");
 
 		try
 		{
 			m_selector = Selector.open ();
 		}
 		catch (IOException ioException)
 		{
 			m_logger.log (Level.WARNING, "failed to initialize reactor");
 
 			return;
 		}
 
 		initPipeline ();
 		initTaskPoll (m_nQueueTaskSize);
 
 		m_bInitialized = true;
 
 		m_logger.log (Level.FINE, "reactor initialized");
 	}
 
 	/**
 	 * Initializes the task pipeline.
 	 */
 	private void initPipeline ()
 	{
 		if (m_bPipeline)
 		{
 			m_pipeline = newPipeline (getName () + "-MessagePipeline",
 					m_nPipelineMaxTasks, m_nPipelineMinThreads,
 					m_nPipelineMaxThreads, m_nPipelineThreadIncrement,
 					m_nPipelineThreadPriority, m_nPipelineThreadTimeout);
 
 			m_pipeline.init ();
 		}
 	}
 
 	/**
 	 * Creates a pipeline to execute task.
 	 * 
 	 * @param name
 	 *            the name of the pipeline.
 	 * @param maxTasks
 	 *            the maximum number of tasks in the pipeline.
 	 * @param minThreads
 	 *            the minimum number of threads in the pool.
 	 * @param maxThreads
 	 *            the maximum number of threads in the pool.
 	 * @param threadIncrement
 	 *            the thread increment when no thread are available.
 	 * @param threadPriority
 	 *            the thread priority.
 	 * @param threadTimeout
 	 *            the thread timeout.
 	 * @return the <code>Pipeline</code> object of the pipeline.
 	 */
 	private Pipeline newPipeline (String name, int maxTasks, int minThreads,
 			int maxThreads, int threadIncrement, int threadPriority,
 			int threadTimeout)
 	{
 		Pipeline pipeline = new ListPipeline (DefaultWorkerThread.class);
 
 		pipeline.setName (name);
 		pipeline.setMaxSize (maxTasks);
 		pipeline.setMinThreads (minThreads);
 		pipeline.setMaxThreads (maxThreads);
 		pipeline.setThreadIncrement (threadIncrement);
 		pipeline.setThreadPriority (threadPriority);
 		pipeline.setThreadTimeout (threadTimeout);
 
 		m_logger.log (Level.FINE, "new pipeline created: "
 				+ pipeline.getClass ().getName ());
 
 		return pipeline;
 	}
 
 	/**
 	 * Initializes the task queue.
 	 * 
 	 * @param size
 	 *            the task count of the queue.
 	 */
 	private void initTaskPoll (int size)
 	{
 		MessageTask messageTask;
 
 		for (int i = 0; i < size; i++)
 		{
 			messageTask = newMessageTask ();
 
 			m_qTasks.offer (messageTask);
 		}
 	}
 
 	/**
 	 * Creates a new <code>MessageTask</code> task.
 	 * 
 	 * @return the <code>MessageTask</code> of the task.
 	 */
 	private MessageTask newMessageTask ()
 	{
 		Algorithm algorithm;
 
 		if (Define.RFC_STRICT_EOL)
 			algorithm = new CRLFAlgorithm ();
 		else
 			algorithm = new LineAlgorithm ();
 
 		MessageTask messageTask = MessageTask.create (this);
 
 		messageTask.setReactor (this);
 		messageTask.setPipeline (m_pipeline);
 		messageTask.setRecycle (m_bTaskRecycle);
 
 		m_logger.log (Level.FINE, "new message task created");
 
 		messageTask.init (algorithm, m_bTaskUseDirectBuffer,
 				m_bTaskUseBufferView);
 
 		return (messageTask);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Thread#start()
 	 */
 	public void start ()
 	{
 		if (!m_bInitialized)
 			throw new IllegalStateException ("reactor not initialized");
 
 		if (m_bRunning)
 			throw new IllegalStateException ("reactor already started");
 
 		m_bRunning = true;
 
 		startPipeline ();
 
 		m_logger.log (Level.FINE, "reactor started");
 
 		super.start ();
 	}
 
 	/**
 	 * Starts the task pipeline.
 	 */
 	private void startPipeline ()
 	{
 		if (m_bPipeline)
 		{
 			m_pipeline.start ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Thread#run()
 	 */
 	@Override
 	public void run ()
 	{
 		while (m_bRunning)
 		{
 			doSelect ();
 		}
 
 		try
 		{
 			m_selector.close ();
 		}
 		catch (IOException ioException)
 		{
 		}
 	}
 
 	/**
 	 * Selector loop which waits for read operation.
 	 */
 	private void doSelect ()
 	{
 		SelectionKey key = null;
 
 		try
 		{
 			synchronized (m_selectorGuard)
 			{
 			}
 
 			m_logger.log (Level.FINE, "new select operation");
 
 			try
 			{
 				m_selector.select (m_nSelectorTimeout);
 			}
 			catch (CancelledKeyException cancelledKeyException)
 			{
 			}
 
 			Iterator<SelectionKey> iterator = m_selector.selectedKeys ()
 					.iterator ();
 
 			while (iterator.hasNext ())
 			{
 				m_logger.log (Level.FINE, "new ready key selected");
 
 				key = iterator.next ();
 				iterator.remove ();
 
 				key.interestOps (0);
 
 				if (key.isValid ())
 					handleKey (key);
 				else
 					cancelKey (key);
 			}
 		}
 		catch (IOException ioException)
 		{
 			if (key != null)
 			{
 				key.cancel ();
 			}
 		}
 	}
 
 	/**
 	 * Handles a new read operation.
 	 * 
 	 * @param key
 	 *            the key to handle.
 	 */
 	private void handleKey (SelectionKey key)
 	{
 		if (!key.isReadable ())
 		{
 			m_logger.log (Level.FINE, "invalid selection key");
 
 			return;
 		}
 
 		if (!m_bSelectorAllowIO)
 		{
 			m_logger.log (Level.FINE,
 					"reactor doesn't allow I/O requests; dropping operation");
 
 			return;
 		}
 
 		MessageTask messageTask = getTask ();
 
 		messageTask.setSelectionKey (key);
 
 		messageTask.run ();
 	}
 
 	/**
 	 * Cancels a key from the reactor.
 	 * 
 	 * @param key
 	 */
 	private void cancelKey (SelectionKey key)
 	{
 		try
 		{
 			((SocketChannel) key.channel ()).socket ().close ();
 		}
 		catch (IOException ioException)
 		{
 		}
 		finally
 		{
 			try
 			{
 				key.channel ().close ();
 			}
 			catch (IOException ioException)
 			{
 			}
 		}
 
 		key.attach (null);
 		key.cancel ();
 		key.selector ().wakeup ();
 
 		m_logger.log (Level.FINE, "cancelled key");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.client.lib.nio.ReadReactor#terminate()
 	 */
 	public void terminate ()
 	{
 		if (!m_bRunning)
 			throw new IllegalStateException ("reactor not started");
 
 		m_bRunning = false;
 		m_selector.wakeup ();
 
 		terminatePipeline ();
 
 		m_qTasks.clear ();
 
 		m_logger.log (Level.FINE, "reactor terminated");
 	}
 
 	/**
 	 * Terminates the task pipeline.
 	 */
 	private void terminatePipeline ()
 	{
 		if (m_bPipeline)
 		{
 			m_pipeline.terminate ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.client.lib.nio.ReadReactor#isStarted()
 	 */
 	public boolean isStarted ()
 	{
 		return (m_bRunning);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Reactor#getTask()
 	 */
 	public MessageTask getTask ()
 	{
 		m_logger.log (Level.FINE, "get task");
 
 		MessageTask messageTask = null;
 
 		if (m_qTasks != null)
 		{
 			messageTask = m_qTasks.poll ();
 		}
 
 		if (messageTask == null)
 		{
 			messageTask = newMessageTask ();
 		}
 
 		return (messageTask);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.client.lib.nio.Reactor#returnTask(net.homeip .bhuisgen
 	 * .ircs.lib.nio.Task)
 	 */
 	public void returnTask (Task task)
 	{
 		if ((task == null) || (task.getClass () != MessageTask.class))
 			throw new IllegalArgumentException ("invalid task");
 
 		m_logger.log (Level.FINE, "return task to reactor");
 
 		m_qTasks.offer ((MessageTask) task);
 	}
 	
 
 	/**
 	 * Register a new socket channel in the reactor.
 	 * 
 	 * @param socketChannel
 	 *            the socket channel to register.
 	 */
 	public SelectionKey registerChannel (SocketChannel socketChannel)
 	{
 		if (socketChannel == null)
 			throw new IllegalArgumentException ("invalid socket channel");
 
 		if (!m_bRunning)
 			throw new IllegalStateException ("reactor not started");
 
 		SelectionKey selectionKey;
 
 		synchronized (m_selectorGuard)
 		{
 			m_selector.wakeup ();
 
 			try
 			{
 				selectionKey = socketChannel.register (m_selector,
 						SelectionKey.OP_READ);
 			}
 			catch (ClosedChannelException closedChannelException)
 			{
 				return (null);
 			}
 		}
 
 		m_logger.log (Level.FINE, "socket channel registered to reactor");
 
 		return (selectionKey);
 	}
 
 	/**
 	 * Unregister a socket channel of the reactor.
 	 * 
 	 * @param selectionKey
 	 *            the selection key of the socket channel to unregister.
 	 */
 	public void unregisterChannel (SelectionKey selectionKey)
 	{
 		if (selectionKey == null)
 			throw new IllegalArgumentException ("invalid selection key");
 
 		if (!m_bRunning)
 			throw new IllegalStateException ("reactor not started");
 
 		synchronized (m_selectorGuard)
 		{
 			m_selector.wakeup ();
 
 			selectionKey.cancel ();
 		}
 
 		m_logger.log (Level.FINE, "socket channel unregistered from reactor");
 	}
 
 	/**
 	 * Update a socket channel already registered to this reactor.
 	 * 
 	 * @param selectionKey
 	 *            the selection key of the socket channel to unregister.
 	 * @param ops
 	 *            the new interest ops of the channel.
 	 */
 	public void updateChannel (SelectionKey selectionKey, int ops)
 	{
 		if (selectionKey == null)
 			throw new IllegalArgumentException ("invalid selection key");
 		
 		if (!m_bRunning)
 			throw new IllegalStateException ("reactor not started");
 		
 		synchronized (m_selectorGuard)
 		{
 			m_selector.wakeup ();
 			
 			selectionKey.interestOps (ops);
 		}
 		
		m_logger.log (Level.FINE, "socket channel updated from reactor");
 	}
 
 	/**
 	 * Returns the manager of the reactor
 	 * 
 	 * @return the <code>Manager</code> object of the reactor.
 	 */
 	public final Manager getManager ()
 	{
 		return (m_manager);
 	}
 
 	/**
 	 * Returns the main reactor of this reactor.
 	 * 
 	 * @return the <code>MainReactor</code> of the main reactor.
 	 */
 	public final MainReactor getMainReactor ()
 	{
 		return (m_mainReactor);
 	}
 
 	/**
 	 * Returns the reactor data of the reactor.
 	 * 
 	 * @return the <code>ReactorData</code> object of the reactor.
 	 */
 	public final ReactorData getReactorData ()
 	{
 		return (m_reactorData);
 	}
 
 	/**
 	 * Returns the selector timeout of the reactor.
 	 * 
 	 * @return the selector timeout.
 	 */
 	public final int getSelectorTimeout ()
 	{
 		return (m_nSelectorTimeout);
 	}
 
 	/**
 	 * Checks if the selector of the reactor allow I/O requests.
 	 * 
 	 * @return <code>true</code> if the I/O requests are allowed;
 	 *         <code>false</code> otherwise.
 	 */
 	public final boolean getSelectorAllowIO ()
 	{
 		return (m_bSelectorAllowIO);
 	}
 
 	/**
 	 * Returns the queue task size of the reactor.
 	 * 
 	 * @return the queue task size.
 	 */
 	public final int getQueueTaskSize ()
 	{
 		return (m_nQueueTaskSize);
 	}
 
 	/**
 	 * Checks if a pipeline is used by the reactor.
 	 * 
 	 * @return <code>true</code> if a pipeline is used; <code>false</code>
 	 *         otherwise.
 	 */
 	public final boolean getPipeline ()
 	{
 		return (m_bPipeline);
 	}
 
 	/**
 	 * Returns the pipeline maximal tasks of the reactor.
 	 * 
 	 * @return the maximal tasks.
 	 */
 	public final int getPipelineMaxTasks ()
 	{
 		return (m_nPipelineMaxTasks);
 	}
 
 	/**
 	 * Returns the pipeline minimum threads of the reactor.
 	 * 
 	 * @return the minimum threads.
 	 */
 	public final int getPipelineMinThreads ()
 	{
 		return (m_nPipelineMinThreads);
 	}
 
 	/**
 	 * Returns the pipeline maximum threads of the reactor.
 	 * 
 	 * @return the maximum threads.
 	 */
 	public final int getPipelineMaxThreads ()
 	{
 		return (m_nPipelineMaxThreads);
 	}
 
 	/**
 	 * Returns the pipeline thread increment of the reactor.
 	 * 
 	 * @return the thread increment.
 	 */
 	public final int getPipelineThreadIncrement ()
 	{
 		return (m_nPipelineThreadIncrement);
 	}
 
 	/**
 	 * Returns the pipeline thread priority of the reactor.
 	 * 
 	 * @return the thread priority.
 	 */
 	public final int getPipelineThreadPriority ()
 	{
 		return (m_nPipelineThreadPriority);
 	}
 
 	/**
 	 * Returns the pipeline thread timeout of the reactor.
 	 * 
 	 * @return the thread timeout.
 	 */
 	public final int getPipelineThreadTimeout ()
 	{
 		return (m_nPipelineThreadTimeout);
 	}
 
 	/**
 	 * Returns <code>true</code> if the tasks are recycled.
 	 * 
 	 * @return <code>true</code> if the tasks are recycled; <code>false</code>
 	 *         otherwise.
 	 */
 	public final boolean getTaskRecycle ()
 	{
 		return (m_bTaskRecycle);
 	}
 
 	/**
 	 * Returns <code>true</code> if direct buffers are used by tasks.
 	 * 
 	 * @return <code>true</code> if direct buffers are used; <code>false</code>
 	 *         otherwise.
 	 */
 	public final boolean getTaskUseDirectBuffer ()
 	{
 		return (m_bTaskUseDirectBuffer);
 	}
 
 	/**
 	 * Returns <code>true</code> if buffer view are used by tasks.
 	 * 
 	 * @return <code>true</code> if buffer view are used; <code>false</code>
 	 *         otherwise.
 	 */
 	public final boolean getTaskUseBufferView ()
 	{
 		return (m_bTaskUseBufferView);
 	}
 
 	/**
 	 * Sets the selector timeout of the reactor.
 	 * 
 	 * @param selectorTimeout
 	 *            the timeout of the selector.
 	 */
 	public void setSelectorTimeout (int selectorTimeout)
 	{
 		m_nSelectorTimeout = selectorTimeout;
 	}
 
 	/**
 	 * Sets the flag to allow I/O requests by the reactor.
 	 * 
 	 * @param selectorAllowIO
 	 *            the flag.
 	 */
 	public final void setSelectorAllowIO (boolean selectorAllowIO)
 	{
 		m_bSelectorAllowIO = selectorAllowIO;
 	}
 
 	/**
 	 * Sets the queue task size of the reactor.
 	 * 
 	 * @param queueTaskSize
 	 *            the queue task size.
 	 */
 	public final void setQueueTaskSize (int queueTaskSize)
 	{
 		m_nQueueTaskSize = queueTaskSize;
 	}
 
 	/**
 	 * Sets the flag to use a pipeline by the reactor.
 	 * 
 	 * @param pipeline
 	 *            the flag.
 	 */
 	public final void setPipeline (boolean pipeline)
 	{
 		m_bPipeline = pipeline;
 	}
 
 	/**
 	 * Sets the read pipeline maximum tasks of the reactor.
 	 * 
 	 * @param readPipelineMaxTasks
 	 *            the pipeline maximum tasks.
 	 */
 	public final void setPipelineMaxTasks (int readPipelineMaxTasks)
 	{
 		m_nPipelineMaxTasks = readPipelineMaxTasks;
 	}
 
 	/**
 	 * Sets the pipeline minimum threads of the reactor.
 	 * 
 	 * @param pipelineMinThreads
 	 *            the pipeline minimum threads.
 	 */
 	public final void setPipelineMinThreads (int pipelineMinThreads)
 	{
 		m_nPipelineMinThreads = pipelineMinThreads;
 	}
 
 	/**
 	 * Sets the pipeline maximum threads of the reactor.
 	 * 
 	 * @param pipelineMaxThreads
 	 *            the pipeline maximum threads.
 	 */
 	public final void setPipelineMaxThreads (int pipelineMaxThreads)
 	{
 		m_nPipelineMaxThreads = pipelineMaxThreads;
 	}
 
 	/**
 	 * Sets the pipeline thread increment of the reactor.
 	 * 
 	 * @param pipelineThreadIncrement
 	 *            the pipeline thread increment.
 	 */
 	public final void setPipelineThreadIncrement (int pipelineThreadIncrement)
 	{
 		m_nPipelineThreadIncrement = pipelineThreadIncrement;
 	}
 
 	/**
 	 * Sets the pipeline thread priority of the reactor.
 	 * 
 	 * @param pipelineThreadPriority
 	 *            the pipeline thread priority.
 	 */
 	public final void setPipelineThreadPriority (int pipelineThreadPriority)
 	{
 		m_nPipelineThreadPriority = pipelineThreadPriority;
 	}
 
 	/**
 	 * Sets the flag to recycle read tasks of the reactor.
 	 * 
 	 * @param taskRecycle
 	 *            the flag.
 	 */
 	public final void setTaskRecycle (boolean taskRecycle)
 	{
 		m_bTaskRecycle = taskRecycle;
 	}
 
 	/**
 	 * Sets the flag to use direct buffer with tasks.
 	 * 
 	 * @param taskUseDirectBuffer
 	 *            the flag.
 	 */
 	public final void setTaskUseDirectBuffer (boolean taskUseDirectBuffer)
 	{
 		m_bTaskUseDirectBuffer = taskUseDirectBuffer;
 	}
 
 	/**
 	 * Sets the flag to use buffer views with tasks.
 	 * 
 	 * @param taskUseBufferView
 	 *            the flag.
 	 */
 	public final void setTaskUseBufferView (boolean taskUseBufferView)
 	{
 		m_bTaskUseBufferView = taskUseBufferView;
 	}
 
 	private Manager m_manager;
 	private MainReactor m_mainReactor;
 	private ReactorData m_reactorData;
 	private Selector m_selector;
 	private Object m_selectorGuard;
 	private int m_nSelectorTimeout;
 	private boolean m_bSelectorAllowIO;
 	private Queue<MessageTask> m_qTasks;
 	private int m_nQueueTaskSize;
 	private Pipeline m_pipeline;
 	private boolean m_bPipeline;
 	private int m_nPipelineMaxTasks;
 	private int m_nPipelineMinThreads;
 	private int m_nPipelineMaxThreads;
 	private int m_nPipelineThreadIncrement;
 	private int m_nPipelineThreadPriority;
 	private int m_nPipelineThreadTimeout;
 	private boolean m_bTaskRecycle;
 	private boolean m_bTaskUseDirectBuffer;
 	private boolean m_bTaskUseBufferView;
 	private boolean m_bInitialized;
 	private boolean m_bRunning;
 	private static Logger m_logger = Logger
 			.getLogger ("fr.hbis.ircs.nio.SubReactor");
 }

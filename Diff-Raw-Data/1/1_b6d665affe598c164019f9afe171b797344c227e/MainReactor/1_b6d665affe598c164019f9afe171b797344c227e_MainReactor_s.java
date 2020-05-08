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
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.nio.channels.CancelledKeyException;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import fr.hbis.ircs.Client;
 import fr.hbis.ircs.Manager;
 import fr.hbis.ircs.lib.nio.Pipeline;
 import fr.hbis.ircs.lib.nio.Task;
 import fr.hbis.ircs.lib.nio.pipeline.ListPipeline;
 import fr.hbis.ircs.lib.nio.reactor.ReactorBase;
 import fr.hbis.ircs.lib.nio.worker.DefaultWorkerThread;
 
 
 /**
  * The class <code>MainReactor</code> implements the main reactor which manage
  * <code>ConnectTask</code> tasks to accept new connections. After accepting the
  * new connection, a <code>ConnectTask</code> task is executed in a separated
  * thread to allow interaction with the high level of the application.
  * 
  * @author Boris HUISGEN
  */
 public class MainReactor extends ReactorBase
 {
 	/**
 	 * Constructs a new <code>MainReactor</code> object.
 	 */
 	private MainReactor ()
 	{
 		m_manager = null;
 		m_reactorData = null;
 		m_selector = null;
 		m_nSelectorTimeout = -1;
 		m_bSelectorAllowIO = false;
 		m_serverSocketChannel = null;
 		m_serverSocketInetAddress = null;
 		m_nServerSocketPort = -1;
 		m_qTasks = new ConcurrentLinkedQueue<ConnectTask> ();
 		m_nQueueTaskSize = 32;
 		m_pipeline = null;
 		m_bPipeline = false;
 		m_nPipelineMaxTasks = -1;
 		m_nPipelineMinThreads = 2;
 		m_nPipelineMaxThreads = 8;
 		m_nPipelineThreadIncrement = 2;
 		m_nPipelineThreadPriority = Thread.NORM_PRIORITY;
 		m_nPipelineThreadTimeout = 60;
 		m_lSubReactors = new ArrayList<SubReactor> ();
 		m_nSubReactorsCount = -1;
 		m_nCurrentSubReactor = -1;
 		m_mClients = new ConcurrentHashMap<SocketChannel, Client> ();
 		m_bInitialized = false;
 		m_bRunning = false;
 	}
 
 	/**
 	 * Creates a new <code>MainReactor</code> object.
 	 * 
 	 * @param manager
 	 *            the manager.
 	 * @param reactorData
 	 *            the client reactor data.
 	 * @return the <code>MainReactor</code> object of the reactor.
 	 */
 	public static final MainReactor create (Manager manager,
 			ReactorData reactorData)
 	{
 		if (manager == null)
 			throw new IllegalArgumentException ("invalid manager");
 
 		if (reactorData == null)
 			throw new IllegalArgumentException ("invalid reactor data");
 
 		MainReactor mainReactor = new MainReactor ();
 
 		mainReactor.m_manager = manager;
 		mainReactor.m_reactorData = reactorData;
 		mainReactor.m_nSelectorTimeout = reactorData.getMainSelectorTimeout ();
 		mainReactor.m_bSelectorAllowIO = reactorData.getMainSelectorAllowIO ();
 		mainReactor.m_serverSocketInetAddress = reactorData.getListenAddress ();
 		mainReactor.m_nServerSocketPort = reactorData.getListenPort ();
 		mainReactor.m_nQueueTaskSize = reactorData.getMainQueueTaskSize ();
 		mainReactor.m_nPipelineMaxTasks = reactorData
 				.getMainPipelineMaxTasks ();
 		mainReactor.m_bPipeline = reactorData.getMainPipeline ();
 		mainReactor.m_nPipelineMinThreads = reactorData
 				.getMainPipelineMinThreads ();
 		mainReactor.m_nPipelineMaxThreads = reactorData
 				.getMainPipelineMaxThreads ();
 		mainReactor.m_nPipelineThreadIncrement = reactorData
 				.getMainPipelineThreadIncrement ();
 		mainReactor.m_nPipelineThreadPriority = reactorData
 				.getMainPipelineThreadPriority ();
 		mainReactor.m_nPipelineThreadTimeout = reactorData
 				.getMainPipelineThreadTimeout ();
 		mainReactor.m_bTaskRecycle = reactorData.getMainTaskRecyle ();
 		mainReactor.m_nSubReactorsCount = reactorData.getSubReactors ();
 		mainReactor.m_nCurrentSubReactor = 0;
 
 		mainReactor.setName ("MainReactor["
 				+ mainReactor.m_serverSocketInetAddress.getHostAddress () + ":"
 				+ reactorData.getListenPort () + "]");
 		
 		m_logger.log (Level.FINE, "reactor created");
 
 		return (mainReactor);
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
 
 			m_serverSocketChannel = ServerSocketChannel.open ();
 
 			m_serverSocketChannel.configureBlocking (false);
 			m_serverSocketChannel.socket ().setReuseAddress (true);
 
 			if (m_serverSocketInetAddress == null)
 			{
 				m_serverSocketChannel.socket ().bind (
 						new InetSocketAddress (m_nServerSocketPort));
 			}
 			else
 			{
 				m_serverSocketChannel.socket ().bind (
 						new InetSocketAddress (m_serverSocketInetAddress,
 								m_nServerSocketPort));
 			}
 
 			m_serverSocketChannel.register (m_selector, SelectionKey.OP_ACCEPT);
 		}
 		catch (IOException ioException)
 		{
 			m_logger.log (Level.SEVERE, "failed to initialize reactor");
 
 			return;
 		}
 
 		initPipeline ();
 		initTaskPoll (m_nQueueTaskSize);
 		initSubReactors ();
 
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
 			m_pipeline = newPipeline (getName () + "-ConnectionPipeline",
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
 
 		return (pipeline);
 	}
 
 	/**
 	 * Initializes the task queue.
 	 * 
 	 * @param size
 	 *            the task count of the queue.
 	 */
 	private void initTaskPoll (int size)
 	{
 		ConnectTask connectTask;
 
 		for (int i = 0; i < size; i++)
 		{
 			connectTask = newConnectionTask ();
 
 			m_qTasks.offer (connectTask);
 		}
 	}
 
 	/**
 	 * Creates a new <code>ConnectTask</code> task.
 	 * 
 	 * @return the <code>ConnectTask</code> of the task.
 	 */
 	private ConnectTask newConnectionTask ()
 	{
 		ConnectTask connectTask = ConnectTask.create (this);
 
 		connectTask.setReactor (this);
 		connectTask.setPipeline (m_pipeline);
 		connectTask.setRecycle (m_bTaskRecycle);
 
 		m_logger.log (Level.FINE, "new connection task created");
 
 		connectTask.init ();
 
 		return (connectTask);
 	}
 
 	/**
 	 * Initializes the subreactors which handle <code>MessageTask</code> tasks.
 	 */
 	private void initSubReactors ()
 	{
 		for (int i = 0; i < m_nSubReactorsCount; i++)
 		{
 			SubReactor subReactor = SubReactor.create (m_manager, this,
 					m_reactorData);
 
 			subReactor.init ();
 
 			m_lSubReactors.add (subReactor);
 		}
 
 		m_nCurrentSubReactor = 0;
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
 		startSubReactors ();		
 		
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
 
 	/**
 	 * Starts the subreactors.
 	 */
 	private void startSubReactors ()
 	{
 		Iterator<SubReactor> iterator = m_lSubReactors.iterator ();
 
 		while (iterator.hasNext ())
 		{
 			SubReactor readReactor = iterator.next ();
 
 			readReactor.start ();
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
 		
 		try
 		{
 			m_serverSocketChannel.close ();
 		}
 		catch (IOException ioException)
 		{
 		}
 	}
 
 	/**
 	 * Selector loop which waits for accept operations.
 	 */
 	private void doSelect ()
 	{
 		SelectionKey key = null;
 
 		try
 		{
 			m_logger.log (Level.FINE, "new selection operation");
 			
 			try
 			{								
 				m_selector.select (m_nSelectorTimeout);
 			}
 			catch (CancelledKeyException cancelledKeyException)
 			{
 			}
 
 			Iterator<SelectionKey> iterator = m_selector.selectedKeys ().iterator ();
 
 			while (iterator.hasNext ())
 			{
 				m_logger.log (Level.FINE, "new ready key selected");
 				
 				key = iterator.next ();
 				iterator.remove ();
 
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
 	 * Handles a new accept operation.
 	 * 
 	 * @param key
 	 *            the key to handle.
 	 */
 	private void handleKey (SelectionKey key)
 	{
 		if (!key.isAcceptable ())
 		{
 			m_logger.log (Level.FINE, "invalid selection key");
 
 			return;
 		}
 
 		if (!m_bSelectorAllowIO)
 		{
 			m_logger.log (Level.FINE,
 					"reactor doesn't allow I/O requests; dropping request");
 
 			return;
 		}
 
 		SocketChannel socketChannel = null;
 
 		try
 		{
 			socketChannel = m_serverSocketChannel.accept ();
 			socketChannel.configureBlocking (false);
 		}
 		catch (IOException ioException)
 		{
 			m_logger.log (Level.WARNING,
 					"invalid socket channel, dropping operation");
 
 			return;
 		}
 		
 		m_logger.log (Level.FINE, "new socket channel accepted");
 		
 		ConnectTask connectTask = getTask ();
 
 		connectTask.setSelectionKey (key);
 		connectTask.setSocketChannel (socketChannel);
 		
 		connectTask.run ();
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
 		
 		m_logger.log (Level.FINE, "key cancelled");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Reactor#terminate()
 	 */
 	public void terminate ()
 	{
 		if (!m_bRunning)
 			throw new IllegalStateException ("reactor not started");
 		
 		m_bRunning = false;		
 		m_selector.wakeup ();
 
 		terminatePipeline ();
 		terminateSubReactors ();
 
 		m_mClients.clear ();
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
 
 	/**
 	 * Terminates the execution of subreactors.
 	 */
 	private void terminateSubReactors ()
 	{
 		Iterator<SubReactor> iterator = m_lSubReactors.iterator ();
 
 		while (iterator.hasNext ())
 		{
 			SubReactor readReactor = iterator.next ();
 
 			readReactor.terminate ();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Reactor#isStarted()
 	 */
 	public boolean isStarted ()
 	{
 		return (m_bRunning);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.AcceptReactor#getTask()
 	 */
 	public ConnectTask getTask ()
 	{
 		m_logger.log (Level.FINE, "get task");
 		
 		ConnectTask connectTask = null;
 
 		if (m_qTasks != null)
 		{
 			connectTask = m_qTasks.poll ();
 		}
 
 		if (connectTask == null)
 		{
 			connectTask = newConnectionTask ();
 		}
 
 		return (connectTask);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fr.hbis.ircs.lib.nio.Reactor#returnTask(fr.hbis
 	 * .ircs.lib.nio.Task)
 	 */
 	public void returnTask (Task task)
 	{
 		if ((task == null) || (task.getClass () != ConnectTask.class))
 			throw new IllegalArgumentException ("invalid task");
 		
 		m_logger.log (Level.FINE, "return task to reactor");
 
 		m_qTasks.offer ((ConnectTask) task);
 	}
 
 	/**
 	 * Returns the next available subreactor.
 	 * 
 	 * @return the <code>SubReactor</code> of the next available subreactor.
 	 */
 	public SubReactor getSubReactor ()
 	{	
 		if ((m_nCurrentSubReactor < 0)
 				|| (m_nCurrentSubReactor >= m_lSubReactors.size ()))
 			m_nCurrentSubReactor = 0;
 		
 		m_logger.log (Level.FINE, "getting subreactor '" + m_nCurrentSubReactor + "'");
 		
 		return (m_lSubReactors.get (m_nCurrentSubReactor++));
 	}
 
 	/**
 	 * Adds a client managed through this reactor.
 	 * 
 	 * @param client
 	 *            the client.
 	 */
 	public void addClient (Client client)
 	{
 		m_mClients.put (client.getConnection ().getSocketChannel (), client);
 		
 		m_logger.log (Level.FINE, "new client added to reactor");
 	}
 
 	/**
 	 * Removes a client managed through this reactor.
 	 * 
 	 * @param socketChannel
 	 *            the socket channel of the client.
 	 */
 	public void removeClient (SocketChannel socketChannel)
 	{
 		m_mClients.remove (socketChannel);
 		
 		m_logger.log (Level.FINE, "client removed from reactor");
 	}
 
 	/**
 	 * Gets a <code>client</code> object by his <code>SocketChannel</code>
 	 * object.
 	 * 
 	 * @param socketChannel
 	 *            the <code>SocketChannel</code> of the client.
 	 * 
 	 * @return the <code>Client</code> object of the client if found;
 	 *         <code>null</code> otherwise.
 	 */
 	public final Client getClient (SocketChannel socketChannel)
 	{
 		if (socketChannel == null)
 			throw new IllegalArgumentException ("invalid socket channel");
 		
 		m_logger.log (Level.FINE, "getting client from socket channel");
 
 		return (m_mClients.get (socketChannel));
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
 	 * Returns the reactor data of the reactor.
 	 * 
 	 * @return the <code>ReactorData</code> object of the reactor.
 	 */
 	public final ReactorData getReactorData ()
 	{
 		return m_reactorData;
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
 	 * Returns the server socket channel of the reactor.
 	 * 
 	 * @return the <code>ServerSocketChannel</code> of the reactor.
 	 */
 	public final ServerSocketChannel getServerSocketChannel ()
 	{
 		return (m_serverSocketChannel);
 	}
 
 	/**
 	 * Returns the server socket address of the reactor.
 	 * 
 	 * @return the <code>ServerSocketInetAddress</code> of the reactor.
 	 */
 	public final InetAddress getServerSocketInetAddress ()
 	{
 		return (m_serverSocketInetAddress);
 	}
 
 	/**
 	 * Returns the server socker port of the reactor.
 	 * 
 	 * @return the server socket port of the reactor.
 	 */
 	public final int getServerSocketPort ()
 	{
 		return (m_nServerSocketPort);
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
 	 * Returns the pipeline maximum tasks of the reactor.
 	 * 
 	 * @return the maximum tasks.
 	 */
 	public final int getPipelineMaxTasks ()
 	{
 		return (m_nPipelineMaxTasks);
 	}
 
 	/**
 	 * Returns the pipeline minimal threads of the reactor.
 	 * 
 	 * @return the minimal threads.
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
 	public final boolean getTaskRecyle ()
 	{
 		return (m_bTaskRecycle);
 	}
 
 	/**
 	 * Returns the subreactors count.
 	 * 
 	 * @return the subreactors count.
 	 */
 	public final int getSubReactorsCount ()
 	{
 		return (m_nSubReactorsCount);
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
 	 * Sets the server socket address of the reactor.
 	 * 
 	 * @param serverSocketInetAddress
 	 *            the server socket address.
 	 */
 	public void setServerSocketInetAddress (InetAddress serverSocketInetAddress)
 	{
 		m_serverSocketInetAddress = serverSocketInetAddress;
 	}
 
 	/**
 	 * Sets the server socket port of the reactor.
 	 * 
 	 * @param serverSocketPort
 	 *            the server socket port.
 	 */
 	public void setServerSocketPort (int serverSocketPort)
 	{
 		m_nServerSocketPort = serverSocketPort;
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
 	 * Sets the pipeline maximum tasks of the reactor.
 	 * 
 	 * @param pipelineMaxTasks
 	 *            the pipeline maximum tasks.
 	 */
 	public final void setPipelineMaxTasks (int pipelineMaxTasks)
 	{
 		m_nPipelineMaxTasks = pipelineMaxTasks;
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
 	 * Sets the pipeline thread timeout of the reactor.
 	 * 
 	 * @param pipelineThreadTimeout
 	 *            the pipeline thread timeout.
 	 */
 	public final void setPipelineThreadTimeout (int pipelineThreadTimeout)
 	{
 		m_nPipelineThreadTimeout = pipelineThreadTimeout;
 	}
 
 	/**
 	 * Sets the flag to recycle tasks of the reactor.
 	 * 
 	 * @param taskRecycle
 	 *            the flag.
 	 */
 	public final void setAcceptTaskRecyle (boolean taskRecycle)
 	{
 		m_bTaskRecycle = taskRecycle;
 	}
 
 	/**
 	 * Set the subreactors count.
 	 * 
 	 * @param subReactorsCount
 	 *            the subreactors count.
 	 */
 	public void setSubReactorsCount (int subReactorsCount)
 	{
 		m_nSubReactorsCount = subReactorsCount;
 	}
 
 	private Manager m_manager;
 	private ReactorData m_reactorData;
 	private Selector m_selector;
 	private int m_nSelectorTimeout;
 	private boolean m_bSelectorAllowIO;
 	private ServerSocketChannel m_serverSocketChannel;
 	private InetAddress m_serverSocketInetAddress;
 	private int m_nServerSocketPort;
 	private Queue<ConnectTask> m_qTasks;
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
 	private List<SubReactor> m_lSubReactors;
 	private int m_nSubReactorsCount;
 	private int m_nCurrentSubReactor;
 	private Map<SocketChannel, Client> m_mClients;
 	private boolean m_bInitialized;
 	private boolean m_bRunning;
 	private static Logger m_logger = Logger
 			.getLogger ("fr.hbis.ircs.nio.MainReactor");
 }

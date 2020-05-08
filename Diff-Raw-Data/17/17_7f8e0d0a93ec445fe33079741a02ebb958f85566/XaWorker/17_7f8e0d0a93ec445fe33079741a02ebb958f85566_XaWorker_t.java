 package org.neo4j.util.xaworker;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ArrayBlockingQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import org.neo4j.impl.event.Event;
 import org.neo4j.impl.event.EventData;
 import org.neo4j.impl.event.EventManager;
 import org.neo4j.impl.event.ProActiveEventListener;
 import org.neo4j.util.NeoUtil;
 
 public class XaWorker extends Thread implements ProActiveEventListener
 {
 	private static final int MAX_TRY_COUNT = 10;
 
 	private XaWorkerLog workLog;
 	private XaWorkerLog failLog;
 	private boolean halted;
 	private int maxConsumers = 10;
 	private ExecutorService consumers;
	private Map<Integer, Consumer> consumerMap =
		new HashMap<Integer, Consumer>();
 	private XaWorkerLogEntry unhandledEntry;
 	private XaWorkerHook hook;
 	
 	public XaWorker( int maxConsumers )
 	{
 		super( "SearchUpdateWorker" );
 		this.maxConsumers = maxConsumers;
 	}
 	
 	public XaWorker( int maxConsumers, XaWorkerHook hook )
 	{
 		this( maxConsumers );
 		this.setHook( hook );
 	}
 	
 	void setHook( XaWorkerHook hook )
 	{
 		this.hook = hook;
 	}
 	
 	private void registerShutdownListener()
 	{
 		NeoUtil.registerProActiveEventListener(
 			this, Event.NEO_SHUTDOWN_STARTED );
 	}
 	
 	public void add( XaWorkerLogEntry entry ) throws IOException
 	{
 		if ( this.isHalted() )
 		{
 			throw new IllegalStateException( "Has been shutdown" );
 		}
 		
 		this.workLog.add( entry );
 		this.wakeUp();
 	}
 	
 	protected XaWorkerLog getLog()
 	{
 		return this.workLog;
 	}
 	
 	protected XaWorkerLog createLog( String fileName ) throws IOException
 	{
 		return new XaWorkerLog( this.hook, fileName, true );
 	}
 	
 	private boolean isHalted()
 	{
 		return this.halted;
 	}
 	
 	public void prepareStartUp( String logFile )
 		throws IOException
 	{
 		this.workLog = createLog( logFile );
 		this.consumers = new ThreadPoolExecutor( this.maxConsumers,
 			this.maxConsumers, 30, TimeUnit.SECONDS,
			new ArrayBlockingQueue<Runnable>( maxConsumers ),
				new ThreadFactory()
 			{
 				public Thread newThread( Runnable runnable )
 				{
 					return new Thread( runnable,
 						"SearchUpdateWorker Consumer" );
 				}
 			} );
 		this.failLog = new XaWorkerLog( this.hook, logFile + "-fail." +
 			System.currentTimeMillis(), false );
 	}
 
 	public void startUp()
 	{
 		this.registerShutdownListener();
 		this.start();
 	}
 	
 	public void shutDown()
 	{
 		this.halted = true;
 		this.wakeUp();
 		this.consumers.shutdown();
 	}
 	
 	public void awaitTermination( int seconds )
 	{
 		try
 		{
 			this.consumers.awaitTermination( seconds, TimeUnit.SECONDS );
 		}
 		catch ( InterruptedException e )
 		{
 			throw new RuntimeException( e );
 		}
 		try
 		{
 			this.join();
 		}
 		catch ( InterruptedException e )
 		{
 			throw new RuntimeException( e );
 		}
 	}
 	
 	protected void waitBeforeRun()
 	{
 	}
 	
 	@Override
 	public void run()
 	{
 		this.waitBeforeRun();
 		while ( !this.isHalted() )
 		{
 			this.balanceQueue();
 			if ( !this.isHalted() )
 			{
 				this.waitForChange();
 			}
 		}
 		this.closeLog();
 	}
 	
 	public boolean isIdle()
 	{
 		return this.numberOfConsumers() == 0;
 	}
 	
 	private XaWorkerLogEntry nextEntry() throws IOException
 	{
 		if ( this.unhandledEntry != null )
 		{
 			XaWorkerLogEntry entry = this.unhandledEntry;
 			this.unhandledEntry = null;
 			return entry;
 		}
 		return this.workLog.next();
 	}
 	
 	private void closeLog()
 	{
 		try
 		{
 			this.getLog().close();
 		}
 		catch ( IOException e )
 		{
 			System.out.println( "Couldn't close search update log:" );
 			e.printStackTrace();
 		}
 	}
 	
 	synchronized void wakeUp()
 	{
 		this.notify();
 	}
 	
 	private void addConsumer( Consumer consumer )
 	{
 		this.consumers.submit( consumer );
 		this.wakeUp();
 	}
 	
 	private void consumerDone( int txId )
 	{
 		synchronized ( this.consumerMap )
 		{
 			this.consumerMap.remove( txId );
 		}
 		this.wakeUp();
 	}
 	
 	protected void passOnEntry( XaWorkerLogEntry entry )
 		throws XaWorkerException
 	{
 		if ( this.isHalted() )
 		{
 			return;
 		}
 		
 		try
 		{
 			this.perform( entry.getEntry() );
 			if ( this.isHalted() )
 			{
 				return;
 			}
 			this.getLog().writeCompleted( entry );
 		}
 		catch ( IOException e )
 		{
 			throw new XaWorkerException( e );
 		}
 	}
 	
 	/**
 	 * A default implementation, please override and do something useful
 	 * @param entry the entry to work on
 	 * @throws XaWorkerException if the entry couldn't be processed
 	 */
 	protected void perform( XaWorkerEntry entry )
 		throws XaWorkerException
 	{
 		if ( !EventManager.getManager().generateProActiveEvent(
 			XaWorkerEvent.XA_WORKER_EVENT, new EventData( entry ) ) )
 		{
 			throw new XaWorkerException( "Couldn't perform search updates:" +
 				entry );
 		}
 	}
 	
 	private synchronized void waitForChange()
 	{
 		try
 		{
 			this.wait();
 		}
 		catch ( InterruptedException e )
 		{
 			// TODO:
 		}
 	}
 	
 	private int numberOfConsumers()
 	{
 		synchronized ( this.consumerMap )
 		{
 			return this.consumerMap.size();
 		}
 	}
 	
 	private void balanceQueue()
 	{
 		while ( !this.isHalted() &&
 			this.numberOfConsumers() <= this.maxConsumers )
 		{
 			try
 			{
 				XaWorkerLogEntry entry = this.nextEntry();
 				if ( entry == null )
 				{
 					break;
 				}
 				
 				if ( !this.feedEntryToConsumers( entry ) )
 				{
 					this.unhandledEntry = entry;
 					break;
 				}
 			}
 			catch ( IOException e )
 			{
 				System.out.println( "Couldn't read next entry from log" );
 				e.printStackTrace();
 			}
 			catch ( Exception e )
 			{
 				System.out.println(
 					"Unexpected error while queueing consumer" );
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	private boolean hasRoomForMoreConsumers()
 	{
 		return this.numberOfConsumers() < this.maxConsumers;
 	}
 	
 	/**
 	 * Returns true if the entry could be fed to consumers, i.e. if
 	 * there were a consumer of the same txId or if there were room for
 	 * a new consumer
 	 */
 	private boolean feedEntryToConsumers( XaWorkerLogEntry entry )
 	{
 		boolean newConsumer = false;
 		Consumer consumer = null;
 		synchronized ( this.consumerMap )
 		{
 			int txId = entry.getTransactionId();
 			consumer = this.consumerMap.get( txId );
 			if ( consumer == null )
 			{
 				if ( !this.hasRoomForMoreConsumers() )
 				{
 					return false;
 				}
 				consumer = new Consumer( txId );
 				newConsumer = true;
 				this.consumerMap.put( txId, consumer );
 			}
 		}
 		consumer.add( entry );
 		if ( newConsumer )
 		{
 			this.addConsumer( consumer );
 		}
 		return true;
 	}
 	
 	public void flushLog() throws IOException
 	{
 		this.getLog().flush();
 	}
 	
 	public boolean proActiveEventReceived( Event event, EventData data )
 	{
 		if ( event == Event.NEO_SHUTDOWN_STARTED )
 		{
 			this.halted = true;
 		}
 		return true;
 	}
 	
 	private void addToFailLog( XaWorkerLogEntry entry )
 	{
 		synchronized ( this.failLog )
 		{
 			try
 			{
 				if ( !this.failLog.isStarted() )
 				{
 					this.failLog.start();
 				}
 				this.failLog.add( entry );
 			}
 			catch ( IOException e )
 			{
 				// TODO: Hmm
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private class Consumer implements Runnable
 	{
 		private int txId;
 		private List<XaWorkerLogEntry> entries =
			Collections.synchronizedList( new LinkedList<XaWorkerLogEntry>() );
 		
 		private Consumer( int txId )
 		{
 			this.txId = txId;
 		}
 		
 		void add( XaWorkerLogEntry entry )
 		{
 			this.entries.add( entry );
 		}
 		
 		public void run()
 		{
 			try
 			{
 				while ( !isHalted() && this.entries.size() > 0 )
 				{
 					this.doOne( this.entries.remove( 0 ) );
 				}
 			}
 			catch ( Throwable e )
 			{
 				// TODO How to handle this?
 				e.printStackTrace();
 			}
 			finally
 			{
 				consumerDone( this.txId );
 			}
 		}
 		
 		private void doOne( XaWorkerLogEntry entry )
 		{
 			// Try a couple of times if it fails
 			for ( int i = 0; !isHalted() && i < MAX_TRY_COUNT; i++ )
 			{
 				try
 				{
 					passOnEntry( entry );
 					break;
 				}
 				catch ( Exception e )
 				{
 					// TODO How to handle this
 					if ( i == MAX_TRY_COUNT - 1 )
 					{
 						addToFailLog( entry );
 					}
 				}
 				
 				try
 				{
 					Thread.sleep( 10 * i * i );
 				}
 				catch ( InterruptedException e )
 				{
 					// It is ok
 				}
 			}
 		}
 	}
 }

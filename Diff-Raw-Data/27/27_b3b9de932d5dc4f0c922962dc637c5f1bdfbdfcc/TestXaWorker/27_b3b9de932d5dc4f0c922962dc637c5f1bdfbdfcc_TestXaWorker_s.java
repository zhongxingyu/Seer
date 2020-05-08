 package test;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Random;
 import java.util.concurrent.atomic.AtomicInteger;
 import org.neo4j.util.xaworker.SearchDataEntry;
 import org.neo4j.util.xaworker.SearchDataHook;
 import org.neo4j.util.xaworker.XaWorker;
 import org.neo4j.util.xaworker.XaWorkerEntry;
 import org.neo4j.util.xaworker.XaWorkerException;
 import org.neo4j.util.xaworker.XaWorkerHook;
 import org.neo4j.util.xaworker.XaWorkerLogEntry;
 
 public class TestXaWorker extends NeoTest
 {
 	private static final String LOG_FILE = "target/log";
 	
 	private static AtomicInteger top = new AtomicInteger();
 	private static XaWorkerHook hook = new SearchDataHook();
 	private static Random random = new Random();
 	
	public void testNoTest()
	{
	}
	
 	/**
 	 * Add a bunch of entries and see if the worker behaves correctly
 	 */
	public void notTestSome() throws Exception
 	{
 		MyWorker worker = this.newWorker( true );
 		add( worker, 1, 3 );
 		add( worker, 2, 3 );
 		for ( int i = 0; i < 4; i++ )
 		{
 			add( worker, 1, 1 );
 			add( worker, 2, 1 );
 		}
 		for ( int i = 0; i < 5; i++ )
 		{
 			int ids = random.nextInt( 4 ) + 4;
 			for ( int ii = 0; ii < ids; ii++ )
 			{
 				add( worker, ii, random.nextInt( 2 ) + 1 );
 			}
 		}
 		
 		waitUntilStartWorking( worker );
 		worker.shutDown();
 		worker = this.newWorker( false );
 		waitUntilStartWorking( worker );
 		waitUntilDone( worker );
 		worker.shutDown();
 		worker.awaitTermination( 10 );
 	}
 	
 	private void waitUntilStartWorking( XaWorker worker ) throws Exception
 	{
 		while ( worker.isIdle() )
 		{
 			Thread.sleep( 30 );
 		}
 	}
 	
 	private void waitUntilDone( XaWorker worker ) throws Exception
 	{
 		while ( !worker.isIdle() )
 		{
 			Thread.sleep( 30 );
 		}
 	}
 	
 	private MyWorker newWorker( boolean delete ) throws Exception
 	{
 		MyWorker worker = new MyWorker( 2 );
 		if ( delete )
 		{
 			ensureDeleted( LOG_FILE );
 		}
 		worker.prepareStartUp( LOG_FILE );
 		worker.startUp();
 		return worker;
 	}
 
	public void notTestCloseInTheMiddle() throws Exception
 	{
 		for ( int c = 0; c < 10; c++ )
 		{
 			MyWorker worker = this.newWorker( true );
 			int total = 30 + random.nextInt( 20 );
 			for ( int i = 0; i < total; i++ )
 			{
 				add( worker, random.nextInt( 10 ), 1 );
 			}
 			while ( worker.getCounter() < total / 2 )
 			{
 				Thread.sleep( 5 );
 			}
 			int first = worker.getCounter();
 			worker.shutDown();
 			worker.awaitTermination( 10 );
 			worker = this.newWorker( false );
 			waitUntilStartWorking( worker );
 			waitUntilDone( worker );
 			int second = worker.getCounter();
 			assertTrue( first + second >= total );
 			worker.shutDown();
 			worker.awaitTermination( 10 );
 		}
 	}
 	
 	private void ensureDeleted( String fileName )
 	{
 		File file = new File( fileName );
 		if ( file.exists() )
 		{
 			file.delete();
 		}
 	}
 	
 	private void add( XaWorker worker, int txId, int count )
 		throws IOException
 	{
 		for ( int i = 0; i < count; i++ )
 		{
 			XaWorkerLogEntry entry = hook.newPreparedLogEntry();
 			entry.setEntry( newEntry() );
 			entry.setTransactionId( txId );
 			worker.add( entry );
 		}
 	}
 	
 	private XaWorkerEntry newEntry()
 	{
 		SearchDataEntry entry = new SearchDataEntry();
 		entry.setSearchValues( ( byte ) 0, nextId(), ( byte ) 0 );
 		return entry;
 	}
 	
 	private int nextId()
 	{
 		return top.incrementAndGet();
 	}
 	
 	private static class MyWorker extends XaWorker
 	{
 		private Random random = new Random();
 		private int counter;
 		
 		public MyWorker( int count )
 		{
 			super( count, hook );
 		}
 		
 		@Override
 		protected void perform( XaWorkerEntry entry ) throws XaWorkerException
 		{
 			try
 			{
 				Thread.sleep( 5 + random.nextInt( 4 ) );
 				if ( random.nextInt( 10 ) == 0 )
 				{
 					throw new XaWorkerException( "dsjfkk" );
 				}
 				this.counter++;
 			}
 			catch ( InterruptedException e )
 			{
 				// Ok
 			}
 		}
 		
 		int getCounter()
 		{
 			return this.counter;
 		}
 	}
 }

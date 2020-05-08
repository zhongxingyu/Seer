 package org.neo4j.util.xaworker;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.nio.ByteBuffer;
 import java.nio.channels.FileChannel;
 import java.util.concurrent.atomic.AtomicInteger;
 
 public class XaWorkerLog
 {
 	public static enum LogDisposal
 	{
 		NO_DISPOSAL,
 		ARCHIVE,
 		DELETE,
 	}
 	
 	private static final byte ENTRY_INCOMPLETE = ( byte ) 0;
 	private static final byte ENTRY_COMPLETE = ( byte ) 1;
 	
 	private ByteBuffer buffer;
 	private FileChannel channel;
 	private long readPosition;
 	private String fileName;
 	private boolean recovered;
 	private XaWorkerHook hook;
 	private int entrySize;
 	private AtomicInteger numberOfUncompletedEntries = new AtomicInteger();
 	private boolean started;
 	
 	public XaWorkerLog( XaWorkerHook hook, String fileName, boolean autoStart )
 		throws IOException
 	{
 		this.hook = hook;
 		this.entrySize =
 			XaWorkerLogEntry.getCommandSize( hook.getEntrySize() ) + 1;
 		this.fileName = fileName;
 		if ( autoStart )
 		{
 			this.start();
 		}
 	}
 	
 	public boolean isStarted()
 	{
 		return this.started;
 	}
 	
 	public void start() throws IOException
 	{
 		if ( this.started )
 		{
 			throw new IllegalStateException( "Already started" );
 		}
 		this.channel = this.openChannel();
 		this.buffer = ByteBuffer.allocateDirect( entrySize );
 		this.checkConsistency();
 		this.started = true;
 	}
 	
 	private FileChannel openChannel() throws IOException
 	{
 		FileChannel result =
 			new RandomAccessFile( fileName, "rw" ).getChannel();
 		result.position( result.size() );
 		return result;
 	}
 	
 	private LogDisposal getLogDisposal()
 	{
 		return this.hook.getWorkerLogDisposal();
 	}
 	
 	private void checkConsistency() throws IOException
 	{
 		long size = channel.size();
 		int rest = ( int ) ( size % entrySize );
 		if ( rest != 0 )
 		{
 			channel.close();
 			channel = openChannel();
 			System.out.println( "SearchWorkLog: Inconsistent, truncating" );
 			channel.truncate( size - rest );
 			System.out.println( "SearchWorkLog: Truncated successfully" );
 			this.recovered = true;
 			channel.close();
 			channel = openChannel();
 		}
 	}
 	
 	public boolean hadToDoRecovery()
 	{
 		return this.recovered;
 	}
 	
 	protected FileChannel getChannel()
 	{
 		return this.channel;
 	}
 	
 	protected ByteBuffer getBuffer()
 	{
 		return this.buffer;
 	}
 	
 	private boolean allEntriesAreCompleted() throws IOException
 	{
 		boolean moreEntries = this.numberOfUncompletedEntries.get() > 0 ||
 			this.moreToRead();
 		return !moreEntries;
 	}
 	
 	public synchronized void close() throws IOException
 	{
 		boolean completed = this.allEntriesAreCompleted();
 		this.getChannel().close();
 		if ( completed )
 		{
 			if ( this.getLogDisposal() == LogDisposal.ARCHIVE )
 			{
 				this.renameFile();
 			}
 			else if ( this.getLogDisposal() == LogDisposal.DELETE )
 			{
 				this.deleteFile();
 			}
 		}
 	}
 	
 	private void renameFile()
 	{
 		File file = new File( fileName );
 		File newFile = new File( fileName + getNewExtension() );
 		file.renameTo( newFile );
 	}
 	
 	private void deleteFile()
 	{
 		File file = new File( fileName );
 		if ( file.exists() )
 		{
 			if ( !file.delete() )
 			{
 				file.deleteOnExit();
 			}
 		}
 	}
 	
 	private String getNewExtension()
 	{
 		return "." + System.currentTimeMillis();
 	}
 	
 	/**
 	 * This is called after a commit, so several entries may be added and the
 	 * force to disc.
 	 * @param entries
 	 * @throws IOException
 	 */
 	public synchronized void add( XaWorkerLogEntry... entries )
 		throws IOException
 	{
 		for ( XaWorkerLogEntry entry : entries )
 		{
 			addOne( entry );
 		}
 		this.flush();
 	}
 	
 	/**
 	 * Adds one entry without flushing to disc, expecting more to come
 	 * afterwards
 	 * @param entry
 	 * @throws IOException
 	 */
 	private void addOne( XaWorkerLogEntry entry )
 		throws IOException
 	{
 		// JS: hack, fix this
 		SearchDataEntry searchEntry = ( SearchDataEntry ) entry.getEntry();
 		buffer.clear();
		buffer.limit( 1 + 1 + 8 + 1 + 4 );
 		buffer.put( ENTRY_INCOMPLETE );
 		buffer.put( searchEntry.getType() );
 		buffer.putLong( searchEntry.getId() );
 		buffer.put( searchEntry.getMode() );
		buffer.putInt( entry.getTransactionId() );
 		buffer.flip();
 		channel.write( buffer );
 	}
 	
 	/**
 	 * Returns null if there are no more entries
 	 */
 	public synchronized XaWorkerLogEntry next() throws IOException
 	{
 		long oldPos = channel.position();
 		try
 		{
 			channel.position( this.readPosition );
 			while ( moreToRead() )
 			{
 				buffer.clear();
 				buffer.limit( 1 );
 				channel.read( buffer );
 				buffer.flip();
 				byte complete = buffer.get();
 				XaWorkerLogEntry entry = this.hook.newPreparedLogEntry();
 				try
 				{
 					entry.readFromFile( channel, buffer );
 					if ( complete == ENTRY_INCOMPLETE )
 					{
 						this.numberOfUncompletedEntries.incrementAndGet();
 						return entry;
 					}
 				}
 				catch ( IOException e )
 				{
 					throw e;
 				}
 				catch ( Exception e )
 				{
 					System.out.println(
 						"The work log contained a bad entry, skipping" );
 					e.printStackTrace();
 				}
 				finally
 				{
 					this.readPosition = channel.position();
 				}
 			}
 			return null;
 		}
 		finally
 		{
 			channel.position( oldPos );
 		}
 	}
 	
 	private synchronized boolean moreToRead() throws IOException
 	{
 		long pos = channel.position();
 		buffer.clear();
 		buffer.limit( 1 );
 		int result = channel.read( buffer );
 		channel.position( pos );
 		return result > 0;
 	}
 	
 	/**
 	 * This will be done by the consumer, the SearchUpdateWorker which reads
 	 * this log and marks entries as "COMPLETED" after it has passed them
 	 * to the search engines.
 	 * @param entry
 	 * @throws IOException
 	 */
 	public synchronized void writeCompleted( XaWorkerLogEntry entry )
 		throws IOException
 	{
 		long oldPos = channel.position();
 		channel.position( entry.position() );
 		buffer.clear();
 		buffer.limit( 1 );
 		buffer.put( ENTRY_COMPLETE );
 		buffer.flip();
 		channel.write( buffer );
 		channel.position( oldPos );
 		this.flush();
 		this.numberOfUncompletedEntries.decrementAndGet();
 	}
 	
 	public synchronized void flush() throws IOException
 	{
 		this.channel.force( true );
 	}
 }

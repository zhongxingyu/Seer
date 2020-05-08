 package org.miv.mbox.net;
 
 import org.miv.mbox.*;
 
 import java.io.*;
 import java.net.*;
 import java.nio.*;
 import java.nio.channels.*;
 import java.util.*;
 
 /**
  * Receives messages and dispatches them to message boxes.
  * 
  * <p>
  * A message receiver listen at a given address and port. Several senders ({@link org.miv.mbox.net.Sender})
  * can connect to it, the receiver will demultiplex the data flow and dispatch
  * incoming messages to registered message boxes.
  * </p>
  * 
  * <p>
  * A receiver is created by giving it the host and port on which it must listen
  * at incoming messages (using a {@link org.miv.mbox.net.MBoxLocator},
  * although, the receiver is not a real message box, is represents a set of
  * them). Then one registers several message boxes inside the receiver. The
  * receiver runs in its own thread.
  * </p>
  * 
  * <p>
  * There exist two way to receive messages with the receiver. One is to register
  * a {@link org.miv.mbox.MBoxListener} using the
  * {@link #register(String,MBoxListener)} method. This will return a message box
  * implementation of type {@link org.miv.mbox.MBoxStandalone} that can buffer
  * messages from the receiver thread an later dispatch them to the MBoxListener.
  * </p>
  * 
  * <p>
  * The other way is to directly implement the {@link org.miv.mbox.MBox}
  * interface or reify the {@link org.miv.mbox.MBoxBase} abstract class to
  * receive messages directly by registering using the
  * {@link #register(String,org.miv.mbox.MBox)} method. This is more flexible,
  * but the user must ensure proper locking since the
  * {@link org.miv.mbox.MBox#post(String,Object...)} method will be invoked
  * from the receiver thread (the MBoxBase class already does this job).
  * </p>
  * 
  * TODO: What happen to messages that are directed to inexistent message boxes?
  * 
  * @see org.miv.mbox.MBox
  * @see org.miv.mbox.MBoxBase
  * @see org.miv.mbox.MBoxListener
  * @see org.miv.mbox.net.Sender
  * @author Antoine Dutot
  * @since 20040624
  */
 public class Receiver
 	extends Thread
 {
 // Attributes
 
 	/**
 	 * Host name for this application.
 	 */
 	protected MBoxLocator locator;
 
 	/**
 	 * Receiver socket.
 	 */
 	protected ServerSocketChannel server;
 
 	/**
 	 * Multiplexor.
 	 */
 	protected Selector selector;
 
 	/**
 	 * Key for the selector.
 	 */
 	protected SelectionKey key;
 
 	/**
 	 * While true, the received is running.
 	 */
 	protected boolean loop = true;
 
 	/**
 	 * Show debugging messages.
 	 */
 	protected boolean debug = true;
 
 	/**
 	 * Last encountered error.
 	 */
 	protected String lastError = null;
 
 	/**
 	 * Pairs (key,value) where the key is the listener ID and the value the MBox
 	 * of the listener. This can be modified by other threads and must be
 	 * properly locked.
 	 * @see #register(String,MBoxListener)
 	 */
 	protected HashMap<String,MBox> boxes = new HashMap<String,MBox>();
 
 	/**
 	 * Current active incoming connections.
 	 */
 	protected HashMap<SelectionKey,IncomingBuffer> incoming = new HashMap<SelectionKey,IncomingBuffer>();
 
 // Constructors
 
 	/**
 	 * New receiver, awaiting for messages in its own thread at the given
 	 * locator. The receiver aggregates several message boxes, therefore its
 	 * locator will only contain a host id, something of the form
 	 * "//"+hostname+":"+port. The initialisation is done in the thread that
 	 * creates this receiver. Then the receiver will run in a thread of its own.
 	 * @param locator the host and port of this receiver to listen at messages.
 	 */
 	public Receiver( MBoxLocator locator )
 		throws IOException, UnknownHostException
 	{
 		this( locator.getHostname(), locator.getPort() );
 	}
 
 	/**
 	 * New receiver, awaiting for messages in its own thread at the given
 	 * locator. The receiver aggregates several message boxes, therefore its
 	 * locator will only contain a host id, something of the form
 	 * "//"+hostname+":"+port. The initialisation is done in the thread that
 	 * creates this receiver. Then the receiver will run in a thread of its own.
 	 * @param locator the host and port of this receiver to listen at messages.
 	 * @param debug If true informations are output for each message received.
 	 */
 	public Receiver( MBoxLocator locator, boolean debug )
 		throws IOException, UnknownHostException
 	{
 		this( locator.getHostname(), locator.getPort(), debug );
 	}
 
 	/**
 	 * Like {@link #Receiver(MBoxLocator)} but specify the host name and port
 	 * explicitly.
 	 * @param hostname The host name to listen at messages.
 	 * @param port The port to listen at messages.
 	 */
 	public Receiver( String hostname, int port )
 		throws IOException, UnknownHostException
 	{
 		this( hostname, port, false );
 	}
 
 	/**
 	 * Like {@link #Receiver(MBoxLocator,boolean)} but specify the host name and
 	 * port explicitly.
 	 * @param hostname The host name to listen at messages.
 	 * @param port The port to listen at messages.
 	 * @param debug If true informations are output for each message received.
 	 */
 	public Receiver( String hostname, int port, boolean debug )
 		throws IOException, UnknownHostException
 	{
 		locator = new MBoxLocator( hostname, port );
 
 		setDebugOn( debug );
 		init();
 		start();
 	}
 
 // Access
 
 	/**
 	 * False as soon as the receiver terminates.
 	 */
 	public synchronized boolean isRunning()
 	{
 		return loop;
 	}
 
 	/**
 	 * Locator of this receiver.
 	 */
 	public MBoxLocator getLocator()
 	{
 		return locator;
 	}
 
 	/**
 	 * Message box attached to the given message box name, or null if no MBox is
 	 * registered at this name.
 	 * @param name Identifier of the MBox listener.
 	 */
 	public synchronized MBox getMBox( String name )
 	{
 		return boxes.get( name );
 	}
 
 // Commands
 
 	/**
 	 * Initialise the server socket.
 	 */
 	protected void init()
 		throws IOException, UnknownHostException
 	{
 		selector = Selector.open();
 		server   = ServerSocketChannel.open();
 
 		server.configureBlocking( false );
 
 		InetAddress       ia  = InetAddress.getByName( locator.getHostname() );
 		InetSocketAddress isa = new InetSocketAddress( ia, locator.getPort() );
 
 		server.socket().bind( isa );
 		
 		if( debug )
 			debug( "bound to socket %s:%d", server.socket().getInetAddress(),
 					server.socket().getLocalPort() );
 		
 		// Register a first server socket inside the multiplexer.
 
 		key = server.register( selector, SelectionKey.OP_ACCEPT );
 	}
 
 	/**
 	 * Enable or disable debugging.
 	 */
 	public void setDebugOn( boolean on )
 	{
 		debug = on;
 	}
 
 	/**
 	 * Register a message box listener for incoming messages. All messages with
 	 * the given name will be directed to it. The listener can then call
 	 * {@link org.miv.mbox.MBoxBase#processMessages()} to receive its pending
 	 * messages.
 	 * @param name Filter only message with this name to the given listener.
 	 * @param listener The listener to register.
 	 * @throws IdAlreadyInUseException If another message box is already registered
 	 * 	at the given name.
 	 */
 	public synchronized MBoxStandalone register( String name, MBoxListener listener )
 		throws IdAlreadyInUseException
 	{
 		if( boxes.containsKey( name ) )
 			throw new IdAlreadyInUseException( "name "+name+" already registered" );
 		
 		MBoxStandalone mbox = new MBoxStandalone( listener );
 		boxes.put( name, mbox );
 		
 		if( debug )
 			debug( "registered message box %s", name );
 		
 		return mbox;
 	}
 
 	/**
 	 * Register a message box. All messages with the given name will be directed
 	 * to it. Unlike with the {@link MBoxListener}, the messages are directly
 	 * posted to the given message box that must ensure proper locking (since
 	 * the receiver runs in a distinct thread) that is implemented by the user.
 	 * @param name Filter only message with this name to the given message box.
 	 * @param box The message box implementation to post messages to.
 	 * @throws IdAlreadyInUseException If another message box is already registered
 	 * 	at the given name.
 	 */
 	public synchronized void register( String name, MBox box )
 		throws IdAlreadyInUseException
 	{
 		if( boxes.containsKey( name ) )
 			throw new IdAlreadyInUseException( "name "+name+" already registered" );
 
 		boxes.put( name, box );
 		
 		if( debug )
 			debug( "registered message box %s", name );
 	}
 
 	/**
 	 * Stop the receiver.
 	 */
 	public synchronized void quit()
 	{
 		loop = false;
 		
 		if( debug )
 			debug( "stopped" );
 	}
 
 	/**
 	 * Wait for connections, accept them, demultiplexes them and dispatch
 	 * messages to registered message boxes.
 	 */
 	@Override
 	public void run()
 	{
 		boolean l;
 
 		synchronized( this ) { l = loop; }
 
 		while( l )
 		{
 			poll();
 		
 			synchronized( this ) { l = loop; }
 		}
 
 		try
 		{
 			server.close();
 		}
 		catch( IOException e )
 		{
 			error( "cannot close the server socket: " + e.getMessage(), e );
 		}
 
 		debug( "receiver "+locator+" finished" );
 	}
 
 	/**
 	 * Wait until one or several chunks of message are acceptable. This method
 	 * should be called in a loop. It can be used to block a program until some
 	 * data is available.
 	 */
 	public void
 	poll()
 	{
 		try
 		{
 			// Wait for incoming messages in a loop.
 
 			if( key.selector().select() > 0 )
 			{
 				Set<?> readyKeys = selector.selectedKeys();
 				Iterator<?> i = readyKeys.iterator();
 
 				while( i.hasNext() )
 				{
 					SelectionKey akey = (SelectionKey) i.next();
 
 					i.remove();
 
 					if( akey.isAcceptable() )
 					{
 						// If a new connection occurs, register the new socket in the multiplexer.
 
 						ServerSocketChannel ssocket = (ServerSocketChannel) akey.channel();
 						SocketChannel       socket  = ssocket.accept();
 
 						if( debug )
 							debug( "accepting socket %s:%d", 
 								socket.socket().getInetAddress(),
 								socket.socket().getPort() );
 						
 						socket.configureBlocking( false );
 						socket.finishConnect();
 
 //						SelectionKey otherKey = socket.register( selector, SelectionKey.OP_READ );
 						socket.register( selector, SelectionKey.OP_READ );
 					}
 					else if( akey.isReadable() )
 					{
 						// If a message arrives, read it.
 
 						readDataChunk( akey );
 					}
 					else if( akey.isWritable() )
 					{
 						throw new RuntimeException( "should not happen" );
 					}
 				}
 			}
 		}
 		catch( IOException e )
 		{
 			error( e, "I/O error in receiver %s thread: aborting: %s",
 					locator, e.getMessage() );
 
 			loop = false;
 		}
 		catch( Throwable e )
 		{
 			error( e, "Unknown error: %s", e.getMessage() );
 			
 			loop = false;
 		}
 	}
 
 	/**
 	 * When data is readable on a socket, send it to the appropriate buffer
 	 * (creating it if needed).
 	 */
 	protected void
 	readDataChunk( SelectionKey key )
 		throws IOException
 	{
 		IncomingBuffer buf = incoming.get( key );
 
 		if( buf == null )
 		{
 			buf = new IncomingBuffer();
 			incoming.put( key, buf );
 
 			SocketChannel socket = (SocketChannel) key.channel();
 			
 			if( debug )
 				debug( "creating buffer for new connection from %s:%d",
 						socket.socket().getInetAddress(),
 						socket.socket().getPort() );
 		}
 
 		try
 		{
 			buf.readDataChunk( key );
 		}
 		catch( IOException e )
 		{
 			incoming.remove( key );
 			e.printStackTrace();
 			error( e, "receiver %s cannot read object socket channel (I/O error): %s",
 				locator.toString(), e.getMessage() );
 			loop = false;
 		}
 	}
 
 // Utilities
 
 	protected void
 	error( String message, Object ... data )
 	{
 		error( null, message, data );
 	}
 
 	protected static final String LIGHT_YELLOW = "[33;1m";
 	protected static final String RESET = "[0m";
 	
 	protected void
 	error( Throwable e, String message, Object ... data )
 	{
 		System.err.print( LIGHT_YELLOW );
 		System.err.print( "[" );
 		System.err.print( RESET );
 		System.err.printf( message, data );
 		System.err.print( LIGHT_YELLOW );
 		System.err.print( "]" );
 		System.err.println( RESET );
 
 		if( e != null )
 			e.printStackTrace();
 	}
 
 	protected void
 	debug( String message, Object ... data )
 	{
 		System.err.print( LIGHT_YELLOW );
 		System.err.printf( "[%s|", locator.toString() );
 		System.err.print( RESET );
 		System.err.printf( message, data );
 		System.err.print( LIGHT_YELLOW );
 		System.err.print( "]" );
 		System.err.println( RESET );
 	}
 
 // Nested classes
 
 /**
  * The connection to a sender.
  *
  * The receiver maintains several incoming connections and demultiplexes them.
  */
 protected class IncomingBuffer
 {
 // Attributes
 
 	protected static final int BUFFER_INITIAL_SIZE = 8192; // 65535, 4096
 	
 	/**
 	 * Buffer for reading.
 	 */
 	protected ByteBuffer buf = ByteBuffer.allocate( BUFFER_INITIAL_SIZE );
 
 	/**
 	 * Index in the buffer past the last byte that forms the current message.
 	 * End can be out of the buffer or out of the data read actually.
 	 */
 	protected int end = -1;
 
 	/**
 	 * Index in the buffer of the first byte that forms the currents message.
 	 * Beg does not count the 4 bytes that give the size of the message. While
 	 * the header is being read, beg is the first byte of the header.
 	 */
 	protected int beg = 0;
 
 	/**
 	 * Position inside beg and end past the last byte read. All bytes at and
 	 * after pos have unspecified contents. Pos always verifies pos&gt;=beg and
 	 * pos&lt;end. While the header is being read, pos is past the last byte
 	 * of the header that has been read.
 	 */
 	protected int pos = 0;
 
 	/**
 	 * Object input stream for reading the buffer. This input stream reads data
 	 * from the "bin" positionable byte array input stream, itself mapped on
 	 * the current message to decode.
 	 */
 	ObjectInputStream in;
 
 	/**
 	 * Input stream filter on the buffer. This descendant of
 	 * ByteArrayInputStream is able to change its offset and length so that we
 	 * can map exactly the message to decode inside the buffer.
 	 */
 	PositionableByteArrayInputStream bin;
 
 	/**
 	 * When false the socket is closed and this buffer must be removed
 	 * from the active connections.
 	 */
 	protected boolean active = false;
 
 // Constructors
 
 	public
 	IncomingBuffer()
 	{
 	}
 
 // Commands
 
 	/**
 	 * Read the available bytes and buffers them. If one or more complete
 	 * serialised objects are available, send them to their respective MBoxes.
 	 *
 	 * Here is the junk...
 	 */
 	public void
 	readDataChunk( SelectionKey key )
 		throws IOException
 	{
 		int           limit  = 0;		// Index past the last byte read during the current invocation.
 		int           nbytes = 0;		// Number of bytes read.
 		SocketChannel socket = (SocketChannel) key.channel();
 
 		// Buffers the data.
 
 		nbytes = bufferize( pos, socket );
 		limit  = pos + nbytes;
 
 		if( nbytes <= 0 )
 			return;
 
 		//debug( "<chunk from "+socket.socket().getInetAddress()+":"+socket.socket().getPort()+">" );
 
 		// Read the first header.
 
 		if( end < 0 )
 		{
 			if( ( limit-beg ) >= 4 )
 			{
 				// If no data has been read yet in the buffer or if the buffer
 				// was emptied completely at previous call: prepare to read a
 				// new message by decoding its header.
 
 				buf.position( 0 );
 				end = buf.getInt() + 4;
 				beg = 4;
 			}
 			else
 			{
 				// The header is incomplete, wait next call to complete it.
 
 				pos = limit;
 			}
 		}
 
 		// Read one or more messages or wait next call to buffers more.
 
 		if( end > 0 )
 		{
 			while( end < limit )
 			{
 				// While the end of the message is in the limit of what was
 				// read, there are one or more complete messages. Decode them
 				// and read the header of the next message, until a message is
 				// incomplete or there are no more messages or a header is
 				// incomplete.
 
 				decodeMessage( limit );
 				buf.position( end );
 
 				if( end + 4 <= limit )
 				{
 					// There is a following message.
 					
 					beg = end + 4;
 					end = end + buf.getInt() + 4;
 				}
 				else
 				{
 					// There is the beginning of a following message
 					// but the header is incomplete. Compact the buffer
 					// and stop here.
 					assert( beg >= 4 );
 
 					beg   = end;
 					int p = 4 - ( (end+4) - limit );
 					compactBuffer();
 					pos = p;
 					beg = 0;
 					end = -1;
 					break;
 				}
 			}
 		
 			if( end == limit )
 			{
 				// If the end of the message coincides with the limit of what
 				// was read we have one last complete message. We decode it and
 				// clear the buffer for the next call.
 
 				decodeMessage( limit );
 				buf.clear();
 				pos =  0;
 				beg =  0;
 				end = -1;
 			}
 			else if( end > limit )
 			{
 				// If the end of the message if after what was read, prepare to
 				// read more at next call when we will have buffered more
 				// data. If we are at the end of the buffer compact it (else no
 				// more space will be available for buffering).
 
 				pos = limit;
 
 				if( end > buf.capacity() )
 					compactBuffer();
 			}
 		}
 	}
 
 	/**
 	 * Read more data from the <code>socket</code> and put it in the buffer at
 	 * <code>at</code>. If the read returns -1 bytes (meaning the
 	 * connection ended), the socket is closed and this buffer will be made
 	 * inactive (and therefore removed from the active connections by the
 	 * Receiver that called it).
 	 * @return the number of bytes read.
 	 * @throws IOException if an I/O error occurs, in between the socket is
 	 * closed and the connection is made inactive, then the exception is
 	 * thrown.
 	 */
 	protected int
 	bufferize( int at, SocketChannel socket )
 		throws IOException
 	{
 		int nbytes = 0;
 //		int limit  = 0;
 
 		try
 		{
 			buf.position( at );
 	
 			nbytes = socket.read( buf );
 
 			if( nbytes < 0 )
 			{
 				active = false;
 				if( in != null )
 					in.close();
 				socket.close();
 				if( debug )
 					debug( "socket from %s:%d closed",
 						socket.socket().getInetAddress(),
 						socket.socket().getPort() );
 				return nbytes;
 			}
 			else if( nbytes == 0 )
 			{
 				throw new RuntimeException( "should not happen: buffer to small, 0 bytes read: compact does not function? messages is larger than "+buf.capacity()+"?" );
 				// This means that there are no bytes remaining in the buffer... it is full.	
 				//compactBuffer();
 				//return nbytes;
 			}
 			
 			buf.position( at );
 
 			return nbytes;
 		}
 		catch( IOException e )
 		{
 			if( debug )
 			debug( "socket from %s:%d I/O error: %s",
 					socket.socket().getInetAddress(),socket.socket().getPort(),
 					e.getMessage() );
 			active = false;
 			if( in != null )
 				in.close();
 			socket.close();
 			throw e;
 		}
 	}
 
 	/**
 	 * Decode one message.
 	 */
 	protected void
 	decodeMessage( int limit )
 		throws IOException
 	{
 		Object o = null;
 
 		// Setup the Byte and Object input streams, either by creating them or
 		// by positioning inside them.
 
 		if( in == null )
 		{
 			bin = new PositionableByteArrayInputStream( buf.array(), beg, end );	// Only a wrapping.
 			in  = new ObjectInputStream( bin );
 		}
 		else
 		{
 			bin.setPos( beg, end );
 		}
 
 		// Read the data between beg and end, it should be a serialised form of
 		// an object of class Message.
 
 		try
 		{
 			o = in.readObject();
 		}
 		catch( Throwable e )
 		{
 			throw new IOException( "error in object deserialization: " + e.getMessage() );
 		}
 
 		// If OK, post it to the correct MBox.
 
 		if( o instanceof Packet )
 		{
 			Packet pckt  = (Packet) o;
 			MBox   mbox = getMBox( pckt.getTo() );
 
 			if( mbox != null )
 			{
 				try
 				{
 					mbox.post( pckt.from, pckt.data );
 					Thread.yield();
 				}
 				catch( CannotPostException e )
 				{
 					error( "message from %s to %s ignored since it cannot be posted, error: %s",
 							pckt.getFrom(), pckt.getTo(), e.getMessage() );
 				}
 			}
 			else
 			{
 				error( "message from %s to %s ignored since it has no registered MBox destination in this receiver (alive=%b)",
 						pckt.getFrom(), pckt.getTo(), pckt.getTo(),
 						Thread.currentThread().isAlive() );
 			}
 		}
 		else
 		{
 			throw new IOException( "received an object that is a not a Message instance" );
 		}
 	}
 
 	/**
 	 * Compact the buffer by removing all read data before <code>beg</code>.
 	 * The <code>beg</code>, <code>end</code> and <code>pos</code> markers are
 	 * updated accordingly. Compact works only if beg is larger than four (the
 	 * size of a header).
 	 * @return the offset.
 	 */
 	protected int
 	compactBuffer()
 	{
 		if( beg > 4 )
 		{
 			int off = beg;
 
 			buf.position( beg );
 			buf.limit( buf.capacity() );
 			buf.compact();
 	
 			pos -= beg;
 			end -= beg;
 			beg  = 0;
 
 			return off;
 		}
 
 		return 0;
 	}
 
 	/**
 	 * Not used in the current implementation, we assumes that no message will
 	 * be larger than the size of the buffer.
 	 */
 	protected void
 	enlargeBuffer()
 	{
 		ByteBuffer tmp = ByteBuffer.allocate( buf.capacity() * 2 );
 
 		buf.position( 0 );
 		buf.limit( buf.capacity() );
 		tmp.put( buf );
 		tmp.position( pos );
 
 		buf = tmp;
 
 		if( bin != null )
 			bin.changeBuffer( buf.array() );
 	}
 }
 
 }

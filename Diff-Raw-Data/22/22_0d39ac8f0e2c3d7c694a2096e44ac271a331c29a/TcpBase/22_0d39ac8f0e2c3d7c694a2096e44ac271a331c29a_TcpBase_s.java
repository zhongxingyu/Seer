 package org.xmodel.net.stream;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.CancelledKeyException;
 import java.nio.channels.Channel;
 import java.nio.channels.ClosedChannelException;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.nio.channels.spi.SelectorProvider;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicInteger;
 import org.xmodel.log.Log;
 import org.xmodel.log.SLog;
 import org.xmodel.net.ILink;
 
 /**
  * A base class for java.nio based clients and servers.
  */
 public abstract class TcpBase
 {
   public TcpBase() throws IOException
   {
     this( null);
   }
   
   public TcpBase( ILink.IListener listener) throws IOException
   {
     log.debugf( "New TcpBase[%X], listener=%s", hashCode(), (listener != null)? listener.getClass().getName(): null);
     
     this.listener = listener;
     
     connections = Collections.synchronizedMap( new HashMap<Channel, Connection>());
     selector = SelectorProvider.provider().openSelector();
     queue = new LinkedBlockingQueue<Request>();
     pendingWrites = new HashMap<Channel, List<ByteBuffer>>();
   }
 
   /**
    * Start the socket servicing thread.
    * @param daemon True if servicing thread should be a daemon.
    */
   public synchronized void start( boolean daemon) throws IOException
   {
     log.debugf( "TcpBase[%X].start: daemon=%s", hashCode(), daemon);
     
     Runnable runnable = new Runnable() {
       public void run()
       {
         thread();
       }
     };
     
     String name = String.format( "%s-%d", getClass().getSimpleName(), counter.incrementAndGet());
     thread = new Thread( runnable, name);
     thread.setDaemon( daemon);
     thread.start();
   }
   
   /**
    * Stop the socket servicing thread.
    */
   public synchronized void stop()
   {
     log.debugf( "TcpBase[%X].stop", hashCode());
     
     exit = true;
     selector.wakeup();
   }
   
   /**
    * Return the connection associated with the specified channel.
    * @param channel The channel.
    * @return Return the connection associated with the specified channel.
    */
   protected Connection getConnection( Channel channel)
   {
     return connections.get( channel);
   }
   
   /**
    * Connect to the specified remote address.
    * @param host The remote host.
    * @param port The remote port.
    * @param timeout The timeout in milliseconds.
    * @param listener The listener for socket events.
    * @return Returns an established connection.
    */
   protected Connection connect( String host, int port, int timeout, ILink.IListener listener) throws IOException
   {
     log.debugf( "TcpBase[%X].connect: address=%s:%d, timeout=%d", hashCode(), host, port, timeout);
     
     SocketChannel channel = SocketChannel.open();
     channel.configureBlocking( false);
     
     channel.connect( new InetSocketAddress( host, port));
     Connection connection = createConnection( channel, listener);
     
     Request request = new Request();
     request.channel = channel;
     request.buffer = null;
     request.ops = SelectionKey.OP_CONNECT;
     
     enqueue( request);
     
     try
     {
       if ( !connection.waitForConnect( timeout)) 
       {
         log.debugf( "Connection timeout expired: timeout=%d", timeout);
         return null;
       }
     }
     catch( InterruptedException e)
     {
       log.debug( "Thread interrupted.");
       return null;
     }
     
     log.debug( "Connection established.");
     return connection;
   }
   
   /**
    * Reconnect the specified connection.
    * @param connection The connection.
    * @param timeout The timeout in milliseconds.
    * @return Returns true if the connection was reestablished.
    */
   protected boolean reconnect( Connection connection, int timeout) throws IOException
   {
     if ( connection.isOpen()) connection.close();
     
     log.debugf( "TcpBase[%X].reconnect: address=%s:%d, timeout=%d", hashCode(), connection.getAddress(), connection.getPort(), timeout);
     
     Request request = new Request();
     request.channel = connection.getChannel();
     request.buffer = null;
     request.ops = SelectionKey.OP_CONNECT;
     
     enqueue( request);
   
     try
     {
       if ( !connection.waitForConnect( timeout))
       {
         log.debugf( "Connection timeout expired: timeout=%d", timeout);
         return false;
       }
     }
     catch( InterruptedException e)
     {
       log.debugf( "Thread interrupted!");
       return false;
     }
     
     log.debug( "Connection re-established.");
     return true;
   }
 
   /**
    * Enqueue a request to write data.
    * @param channel The channel.
    * @param bytes The bytes to be written.
    */
   void write( SocketChannel channel, byte[] bytes)
   {
     log.verbosef( "TcpBase[%X] write enqueue: %d bytes", hashCode(), bytes.length);
     
     ByteBuffer clone = ByteBuffer.allocate( bytes.length);
     clone.put( bytes);
     clone.flip();
     
     Request request = new Request();
     request.channel = channel;
     request.buffer = clone;
     request.ops = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
     
     enqueue( request);
   }
   
   /**
    * Enqueue a request to write data.
    * @param channel The channel.
    * @param buffer The buffer to be written.
    */
   void write( SocketChannel channel, ByteBuffer buffer)
   {
     log.verbosef( "TcpBase[%X] write enqueue: %d bytes", hashCode(), buffer.remaining());
     
     Request request = new Request();
     request.channel = channel;
     request.buffer = buffer;
     request.ops = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
     
     enqueue( request);
   }
   
   /**
    * Enqueue the specified request and wakeup the selector.
    * @param request The request.
    */
   private void enqueue( Request request)
   {
     try
     {
       //System.out.println( "->Q");
       queue.put( request);
       selector.wakeup();
     }
     catch( InterruptedException e)
     {
       Thread.interrupted();
       SLog.exception( this, e);
     }
   }
   
   /**
    * Create a new Connection instance for the specified channel.
    * @param channel The channel.
    * @param listener The listener for socket events.
    * @return Returns the new Connection.
    */
   protected Connection createConnection( SocketChannel channel, ILink.IListener listener) throws IOException
   {
     log.debugf( "TcpBase[%X].createConnection: %s", hashCode(), (listener != null)? listener.getClass().getName(): null);
     
     Connection connection = new Connection( this, channel, listener);
     connections.put( connection.getChannel(), connection);
     return connection;
   }
   
   /**
    * Process a newly connected socket.
    * @param key The selection key.
    */
   private void connect( SelectionKey key) throws IOException
   {
     if ( log.isLevelEnabled( Log.debug))
       log.debugf( "TcpBase[%X].connect: key=%s", hashCode(), toLog( key));
     
     SocketChannel channel = (SocketChannel)key.channel();
     Connection connection = getConnection( channel);
     try
     {
       channel.finishConnect();
       channel.register( selector, SelectionKey.OP_READ);
       connection.connected( channel);
     }
     catch( IOException e)
     {
       connection.close();
     }
   }
   
   /**
    * Accept an incoming connection.
    * @param key The selection key.
    */
   private void accept( SelectionKey key) throws IOException
   {
     ServerSocketChannel channel = (ServerSocketChannel)key.channel();    
     Connection connection = createConnection( channel.accept(), listener);
     connection.getChannel().configureBlocking( false);
     connection.getChannel().register( selector, SelectionKey.OP_READ);
     
     InetSocketAddress local = (InetSocketAddress)connection.getChannel().socket().getLocalSocketAddress();
     
     if ( log.isLevelEnabled( Log.debug))
     {
       log.debugf( "TcpBase[%X].accept: key=%s, address=%s:%d.", hashCode(), toLog( key), local.getAddress().getHostAddress(), local.getPort());
     }
   }
   
   /**
    * Read from a ready channel.
    * @param key The selection key.
    */
   protected void read( SelectionKey key) throws IOException
   {
     if ( log.isLevelEnabled( Log.verbose))
       log.verbosef( "TcpBase[%X].read: key=%s", hashCode(), toLog( key));
     
     SocketChannel channel = (SocketChannel)key.channel();
     Connection connection = connections.get( channel);
     try
     {
       connection.buffer = prepareReadBuffer( connection.buffer);
 
       int nread = channel.read( connection.buffer);
 
       log.verbosef( "Read %d bytes", nread);
       
       if ( nread > 0)
       {
         connection.buffer.flip();
         connection.notifyRead( connection.buffer);
       }
       else if ( nread == -1)
       {
         close( key);
       }
     }
     catch( Exception e)
     {
       log.exception( e);
       close( key);
     }
   }
 
   /**
    * Prepare the specified read buffer for appending new data.
    * @param buffer The read buffer.
    * @return Returns a larger buffer if necessary.
    */
   private ByteBuffer prepareReadBuffer( ByteBuffer buffer)
   {
     buffer.compact();
 //    if ( buffer.position() < buffer.limit())
 //    {
 //      buffer.position( buffer.limit());
 //      buffer.limit( buffer.capacity());
 //    }
 //    else
 //    {
 //      buffer.clear();
 //    }
     
     if ( buffer.position() == buffer.capacity())
     {
       ByteBuffer larger = ByteBuffer.allocate( buffer.capacity() << 1);
       buffer.flip();
       larger.put( buffer);
       return larger;
     }
     
     return buffer;
   }
   
   /**
    * Write to a ready channel.
    * @param key The selection key.
    */
   protected void write( SelectionKey key) throws IOException
   {
     if ( log.isLevelEnabled( Log.verbose))
       log.verbosef( "TcpBase[%X].write: key=%s", hashCode(), toLog( key));
     
     SocketChannel channel = (SocketChannel)key.channel();
     List<ByteBuffer> buffers = pendingWrites.get( channel);
     if ( buffers.size() == 0)
     {
       // this should never happen
       channel.register( selector, SelectionKey.OP_READ);
       return;
     }
     
     ByteBuffer buffer = buffers.get( 0);
     while( buffer != null)
     {
       int length = buffer.remaining();
       channel.write( buffer);
   
       log.verbosef( "Wrote %d bytes, %d remaining", length - buffer.remaining(), buffer.remaining());
       
       if ( buffer.remaining() == 0)
       {
         buffers.remove( 0);
         buffer = (buffers.size() > 0)? buffers.get( 0): null;
         if ( buffer == null) channel.register( selector, SelectionKey.OP_READ);
       }
       else
       {
         break;
       }
     }
   }
   
   /**
    * Close the connection with the specified key.
    * @param key The selected key.
    */
   private void close( SelectionKey key)
   {
     if ( log.isLevelEnabled( Log.debug))
       log.debugf( "TcpBase[%X].close: key=%s", hashCode(), toLog( key));
     
     SocketChannel channel = (SocketChannel)key.channel();
     Connection connection = connections.remove( channel);
     if ( connection != null) connection.close();
     key.cancel();
   }
   
   /**
    * Summarize the state of the specified key.
    * @param key The key.
    * @return Returns the summary.
    */
   private static String toLog( SelectionKey key)
   {
     StringBuilder sb = new StringBuilder();
    if ( key.isAcceptable()) sb.append( 'A');
    if ( key.isConnectable()) sb.append( 'C');
    if ( key.isReadable()) sb.append( 'R');
    if ( key.isWritable()) sb.append( 'W');
    if ( key.isValid()) sb.append( 'V');
     return sb.toString();
   }
   
   /**
    * Add a write buffer to the pending buffers for the specified channel.
    * @param channel The channel.
    * @param buffer The buffer.
    */
   private void addWriteBuffer( Channel channel, ByteBuffer buffer)
   {
     List<ByteBuffer> buffers = pendingWrites.get( channel);
     if ( buffers == null)
     {
       buffers = new ArrayList<ByteBuffer>();
       pendingWrites.put( channel, buffers);
     }
     buffers.add( buffer);
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.net.stream.TcpManager#process(int)
    */
   protected boolean process( int timeout) throws IOException
   {
     log.debugf( "TcpBase[%X].process: timeout=%s", hashCode(), timeout);
     
     // wait for connection
     if ( selector.select( timeout) == 0) return false; 
       
     // handle events
     Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
     while( iter.hasNext())
     {
       SelectionKey readyKey = iter.next();
       iter.remove();
       
       if ( !readyKey.isValid()) continue;
       
       if ( log.isLevelEnabled( Log.verbose))
         log.verbosef( "Ready key: %s", toLog( readyKey));
       
       try
       {
         if ( readyKey.isReadable()) read( readyKey);
         if ( readyKey.isWritable()) write( readyKey);
         if ( readyKey.isAcceptable()) accept( readyKey);
         if ( readyKey.isConnectable()) connect( readyKey);
       }
       catch( IOException e)
       {
         readyKey.cancel();
         log.error( e.getMessage());
       }
       catch( CancelledKeyException e)
       {
         log.warnf( "Cancelled Key: %s", toLog( readyKey));
       }
     }
     
     return true;
   }
     
   private void thread()
   {
     log.debugf( "TcpBase[%X].thread", hashCode());
     
     exit = false;
     while( !exit)
     {
       try
       {
         // process socket events
         //System.out.printf( "SLEEP: %d\n", queue.size());
         process( 0);
         //System.out.println( "AWAKE");
         
         // handle requests
         Request first = queue.poll();
         if ( first != null)
         {
           List<Request> requests = new ArrayList<Request>();
           requests.add( first);
           queue.drainTo( requests);
           
           for( Request request: requests)
           {
             try
             {
               if ( request.channel != null)
               {
                 SelectionKey key = request.channel.keyFor( selector);
                 int ops = (key != null)? key.interestOps(): 0;
                 request.channel.register( selector, ops | request.ops);
               }
               
               if ( request.buffer != null)
               {
                 addWriteBuffer( request.channel, request.buffer);
               }
             }
             catch( CancelledKeyException e)
             {
               log.exception( e);
             }
             catch( ClosedChannelException e)
             {
               log.exception( e);
             }
           }
         }
       }
       catch( Exception e)
       {
         log.errorf( "TcpBase may be corrupted by the following exception: ");
         log.exception( e);
         
         try 
         { 
           Thread.sleep( 500);
         }
         catch( InterruptedException e2)
         {
           log.warn( "TcpBase was interrupted - exitting processing loop.");
           break;
         }
       }
     }
   }
 
   protected static class Request
   {
     public SocketChannel channel;
     public ByteBuffer buffer;
     public int ops;
   }
   
   protected final static Log log = Log.getLog( TcpBase.class);
   private static AtomicInteger counter = new AtomicInteger();
 
   private ILink.IListener listener;
   protected Selector selector;
   private Map<Channel, Connection> connections;
   private BlockingQueue<Request> queue;
   private Map<Channel, List<ByteBuffer>> pendingWrites;
   private Thread thread;
   private volatile boolean exit;
 }

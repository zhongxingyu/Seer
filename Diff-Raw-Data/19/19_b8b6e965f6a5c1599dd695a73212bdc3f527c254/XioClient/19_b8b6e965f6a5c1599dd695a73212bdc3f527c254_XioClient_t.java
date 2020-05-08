 package org.xmodel.net;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLEngine;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.jboss.netty.handler.ssl.SslHandler;
 import org.jboss.netty.handler.timeout.IdleStateHandler;
 import org.xmodel.GlobalSettings;
 import org.xmodel.concurrent.ModelThreadFactory;
 import org.xmodel.future.AsyncFuture;
 import org.xmodel.future.UnionFuture;
 import org.xmodel.log.SLog;
 import org.xmodel.xpath.expression.IContext;
 
 /**
  * This class provides an interface for the client-side of the protocol.
  * (thread-safe)
  */
 public class XioClient extends XioPeer
 {
   public interface IListener
   {
     /**
      * Called when the client connects.
      * @param client The client.
      */
     public void notifyConnect( XioClient client);
     
     /**
      * Called when the client connects.
      * @param client The client.
      */
     public void notifyDisconnect( XioClient client);
   }    
   
   /**
    * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
    * The executors for both protocols use GlobalSettings.getInstance().getModel().getExecutor() of this thread.
    * @param executor The executor that dispatches from the worker thread.
    */
   public XioClient( Executor executor)
   {
     this( null, null, executor);
   }
   
   /**
    * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
    * The executors for both protocols use GlobalSettings.getInstance().getModel().getExecutor() of this thread.
    * @param context The context.
    */
   public XioClient( IContext context)
   {
     this( context, null, context.getExecutor());
   }
   
   /**
    * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
    * The executors for both protocols use GlobalSettings.getInstance().getModel().getExecutor() of this thread.
    * @param context The context.
    */
   public XioClient( SSLContext sslContext, IContext context)
   {
     this( context, null, getDefaultChannelFactory(), sslContext, context.getExecutor());
   }
   
   /**
    * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
    * @param context Optional context.
    * @param scheduler Optional scheduler used for protocol timers.
    * @param contextExecutor
    */
   public XioClient( final IContext context, final ScheduledExecutorService scheduler, final Executor contextExecutor)
   {
     this( context, scheduler, getDefaultChannelFactory(), contextExecutor);
   }
   
   /**
    * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
    * @param context Optional context.
    * @param scheduler Optional scheduler used for protocol timers.
    * @param channelFactory User-supplied channel factory.
    * @param contextExecutor
    */
   public XioClient( final IContext context, final ScheduledExecutorService scheduler, ClientSocketChannelFactory channelFactory, final Executor contextExecutor)
   {
     this( context, scheduler, channelFactory, null, contextExecutor);
   }
   
   /**
    * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
    * @param context Optional context.
    * @param scheduler Optional scheduler used for protocol timers.
    * @param channelFactory The channel factory.
    * @param sslContext The SSLContext.
    * @param contextExecutor An executor
    */
   public XioClient( 
       final IContext context, 
       final ScheduledExecutorService scheduler, 
       final ClientSocketChannelFactory channelFactory, 
       final SSLContext sslContext, 
       final Executor contextExecutor)
   {
     this.scheduler = (scheduler != null)? scheduler: GlobalSettings.getInstance().getScheduler();
     this.listeners = new ArrayList<IListener>( 1);
 
     bootstrap = new ClientBootstrap( channelFactory);
     bootstrap.setOption( "tcpNoDelay", true);
     bootstrap.setOption( "keepAlive", true);
     
     bootstrap.setPipelineFactory( new ChannelPipelineFactory() {
       public ChannelPipeline getPipeline() throws Exception
       {
         ChannelPipeline pipeline = Channels.pipeline();
 
         if ( sslContext != null)
         {
           SSLEngine engine = sslContext.createSSLEngine();
           engine.setUseClientMode( true);
           pipeline.addLast( "ssl", new SslHandler( engine));
         }
         
        pipeline.addLast( "idleStateHandler", new IdleStateHandler( Heartbeat.timer, 90, 30, 90));
         pipeline.addLast( "heartbeatHandler", new Heartbeat());
 
         XioChannelHandler handler = new XioChannelHandler( context, contextExecutor, scheduler, null);
         handler.setClient( XioClient.this);
         pipeline.addLast( "xio", handler);
         
         handler.addListener( channelConnectionListener);
                 
         return pipeline;
       }
     });
   }
   
   /**
    * Returns the XioClient associated with this channel, creating one if necessary.
    * @param channel The channel.
    * @return Returns the XioClient associated with this channel.
    */
   public static XioClient getChannelPeer( Channel channel)
   {
     return (XioClient)channel.getAttachment();
   }
   
   /**
    * Add a listener for client event notification.
    * @param listener The listener.
    */
   public void addListener( IListener listener)
   {
     if ( !listeners.contains( listener))
       listeners.add( listener);
   }
   
   /**
    * Remove a listener for client event notification.
    * @param listener The listener.
    */
   public void removeListener( IListener listener)
   {
     listeners.remove( listener);
   }
   
   /**
    * Connect this client to the specified server. A client may only be connected to one server.
    * @param address The address of the server.
    * @param port The port of the server.
    * @return Returns true if the connection was established.
    */
   public AsyncFuture<XioClient> connect( String address, int port)
   {
     return connect( new InetSocketAddress( address, port));
   }
   
   /**
    * Set whether the client will automatically reconnect when disconnected.
    * @param autoReconnect True if client should automatically reconnect when disconnected.
    */
   public void setAutoReconnect( boolean autoReconnect)
   {
     this.autoReconnect = autoReconnect;
   }
   
   /**
    * Attempt to connect this client to the specified server the specified number of times.
    * @param address The address of the server.
    * @param port The port of the server.
    * @param retries The maximum number of retries.
    * @param delay The delay between retries in milliseconds.
    * @return Returns a future that is retry-aware.
    */
   public AsyncFuture<XioClient> connect( String address, int port, int retries, int delay)
   {
     return connect( new InetSocketAddress( address, port), retries, new int[] { delay});
   }
   
   /**
    * Attempt to connect this client to the specified server the specified number of times.
    * @param address The address of the server.
    * @param port The port of the server.
    * @param delays An array of delays between retries in milliseconds.
    * @return Returns a future that is retry-aware.
    */
   public AsyncFuture<XioClient> connect( String address, int port, int[] delays)
   {
     return connect( new InetSocketAddress( address, port), delays.length, delays);
   }
   
   /**
    * Attempt to connect this client to the specified server the specified number of times.
    * @param address The address of the server.
    * @param port The port of the server.
    * @param retries The maximum number of retries.
    * @param delays An array of delays between retries in milliseconds.
    * @return Returns a future that is retry-aware.
    */
   public AsyncFuture<XioClient> connect( String address, int port, int retries, int[] delays)
   {
     return connect( new InetSocketAddress( address, port), retries, delays);
   }
   
   /**
    * Connect this client to the specified server. A client may only be connected to one server.
    * @param address The address of the server.
    * @return Returns true if the connection was established.
    */
   public AsyncFuture<XioClient> connect( InetSocketAddress address)
   {
     return connect( address, 3, 1000);
   }
   
   /**
    * Attempt to connect this client to the specified server the specified number of times.
    * @param address The address of the server.
    * @param port The port of the server.
    * @param retries The maximum number of retries.
    * @param delay The delay between retries in milliseconds.
    * @return Returns a future that is retry-aware.
    */
   public AsyncFuture<XioClient> connect( InetSocketAddress address, int retries, int delay)
   {
     return connect( address, retries, new int[] { delay});
   }
   
   /**
    * Attempt to connect this client to the specified server the specified number of times.
    * @param address The address of the server.
    * @param port The port of the server.
    * @param delays An array of delays between retries in milliseconds.
    * @return Returns a future that is retry-aware.
    */
   public AsyncFuture<XioClient> connect( InetSocketAddress address, int[] delays)
   {
     return connect( address, delays.length, delays);
   }
   
   /**
    * Attempt to connect this client to the specified server the specified number of times.
    * @param address The address of the server.
    * @param retries The maximum number of retries.
    * @param delays An array of delays between retries in milliseconds.
    * @return Returns a future that is retry-aware.
    */
   public AsyncFuture<XioClient> connect( InetSocketAddress address, int retries, int[] delays)
   {
     synchronized( this)
     {
       lastAddress = address;
       lastRetries = retries;
       lastDelays = delays;
     }
     
     final ConnectionRetryFuture retryFuture = new ConnectionRetryFuture( bootstrap, address, scheduler, retries, delays);
     
     final AsyncFuture<XioClient> asyncFuture = new AsyncFuture<XioClient>( this) {
       public void cancel()
       {
         retryFuture.cancel();
       }
     };
     
     retryFuture.addListener( new ChannelFutureListener() {
       public void operationComplete( ChannelFuture retryFuture) throws Exception
       {
         if ( retryFuture.isSuccess()) 
         {
           // ChannelFutureListener is called before XioChannelHandler.channelConnected???
           setChannel( retryFuture.getChannel());
           
           XioChannelHandler xioHandler = retryFuture.getChannel().getPipeline().get( XioChannelHandler.class);
           ChannelFuture handshakeFuture = xioHandler.getSSLHandshakeFuture();
           if ( handshakeFuture != null)
           {
             handshakeFuture.addListener( new ChannelFutureListener() {
               public void operationComplete( ChannelFuture handshakeFuture) throws Exception
               {
                 if ( handshakeFuture.isSuccess()) 
                 {
                   asyncFuture.notifySuccess();
                 }
                 else
                 {
                   asyncFuture.notifyFailure( handshakeFuture.getCause());
                 }
               }
             });
           }
           else
           {
             asyncFuture.notifySuccess();
           }
         }
         else
         {
           asyncFuture.notifyFailure( retryFuture.getCause());
         }
       }
     });
     
     return asyncFuture;
   }
     
   /* (non-Javadoc)
    * @see org.xmodel.net.XioPeer#reconnect()
    */
   @Override
   public AsyncFuture<XioPeer> reconnect()
   {
     InetSocketAddress address = null;
     int retries = 0;
     int[] delays = null;
     
     synchronized( this)
     {
       address = lastAddress;
       retries = lastRetries;
       delays = lastDelays;
     }
     
     AsyncFuture<XioClient> future = (lastAddress != null)? connect( address, retries, delays): null;
     if ( future == null) return null;
     
     UnionFuture<XioPeer, XioClient> wrapperFuture = new UnionFuture<XioPeer, XioClient>( this);
     wrapperFuture.addTask( future);
     return wrapperFuture;
   }
   
   /**
    * @return Returns the address of the last connection attempt.
    */
   public InetSocketAddress getRemoteAddress()
   {
     return lastAddress;
   }
   
   /**
    * @return Returns the default ClientSocketChannelFactory.
    */
   private static synchronized ClientSocketChannelFactory getDefaultChannelFactory()
   {
     if ( defaultChannelFactory == null)
     {
       defaultChannelFactory = new NioClientSocketChannelFactory(
         Executors.newCachedThreadPool( new ModelThreadFactory( "xio-client-boss")),
         Executors.newCachedThreadPool( new ModelThreadFactory( "xio-client-worker")));
     }
     return defaultChannelFactory;
   }
 
   /**
    * Notify listeners that a connection was established.
    */
   private void notifyChannelConnected()
   {
     if ( listeners != null)
     {
       for( IListener listener: listeners)
       {
         try { listener.notifyConnect( XioClient.this); }
         catch( Exception e)
         {
           SLog.errorf( this, "Exception was thrown by listener: %s", e.toString());
         }
       }
     }
   }
   
   /**
    * Notify listeners that a connection has been closed.
    */
   private void notifyChannelDisconnected()
   {
     if ( listeners != null)
     {
       for( IListener listener: listeners)
       {
         try { listener.notifyDisconnect( XioClient.this); }
         catch( Exception e)
         {
           SLog.errorf( this, "Exception was thrown by listener: %s", e.toString());
         }
       }
     }
     
     if ( autoReconnect)
     {
       reconnect();
     }
   }
   
   private ChannelFutureListener channelHandshakeListener = new ChannelFutureListener() {
     public void operationComplete( ChannelFuture future) throws Exception
     {
       if ( future.isSuccess())
         notifyChannelConnected();
     }
   };
   
   private XioChannelHandler.IListener channelConnectionListener = new XioChannelHandler.IListener() {
     public void notifyConnect( XioPeer peer)
     {
       SslHandler sslHandler = peer.getChannel().getPipeline().get( SslHandler.class);
       if ( sslHandler != null)
       {
         ChannelFuture future = sslHandler.handshake();
         future.addListener( channelHandshakeListener);
       }
       else
       {
         notifyChannelConnected();
       }
     }      
     public void notifyDisconnect( XioPeer peer)
     {
       notifyChannelDisconnected();
     }
   };
   
   private static ClientSocketChannelFactory defaultChannelFactory = null;
   
   private ClientBootstrap bootstrap;
   private ScheduledExecutorService scheduler;
   private InetSocketAddress lastAddress;
   private int lastRetries;
   private int[] lastDelays;
   private boolean autoReconnect;
   private List<IListener> listeners;
 }

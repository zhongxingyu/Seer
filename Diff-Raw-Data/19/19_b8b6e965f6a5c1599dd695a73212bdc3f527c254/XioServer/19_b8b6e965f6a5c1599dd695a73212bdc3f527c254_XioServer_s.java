 package org.xmodel.net;
 
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLEngine;
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.socket.ServerSocketChannelFactory;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import org.jboss.netty.handler.ssl.SslHandler;
 import org.jboss.netty.handler.timeout.IdleStateHandler;
 import org.xmodel.concurrent.ModelThreadFactory;
 import org.xmodel.log.SLog;
 import org.xmodel.net.execution.ExecutionPrivilege;
 import org.xmodel.xpath.expression.IContext;
 
 /**
  * This class provides an interface for the server-side of the protocol.
  */
 public class XioServer
 {
   public interface IListener
   {
     /**
      * Called when a client connects.
      * @param peer The peer instance of the client.
      */
     public void notifyConnect( XioPeer peer);
     
     /**
      * Called when a client disconnects.
      * @param peer The peer instance of the client.
      */
     public void notifyDisconnect( XioPeer peer);
     
     /**
      * Called when a client registers.
      * @param peer The peer instance of the client.
      * @param name The name.
      */
     public void notifyRegister( XioPeer peer, String name);
     
     /**
      * Called when a client un-registers.
      * @param peer The peer instance of the client.
      * @param name The name.
      */
     public void notifyUnregister( XioPeer peer, String name);
   }
   
   /**
    * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
    * The executors for both protocols use GlobalSettings.getInstance().getModel().getExecutor() of this thread.
    * @param context The context.
    */
   public XioServer( IContext context)
   {
     this( null, context, null, null);
   }
   
   /**
    * Create a client that uses an NioClientSocketChannelFactory configured with tcp-no-delay and keep-alive.
    * The executors for both protocols use GlobalSettings.getInstance().getModel().getExecutor() of this thread.
    * @param SSLContext An SSLContext.
    * @param context The context.
    */
   public XioServer( SSLContext sslContext, IContext context)
   {
     this( sslContext, context, null, null);
   }
   
   /**
    * Create a server that uses an NioServerSocketChannelFactory configured with tcp-no-delay and keep-alive. Null may be passed
    * for optional arguments. The following table shows defaults used for each optional argument:
    * <ul>
    *   <li>context - Required for remote execution from the server</li>
    *   <li>scheduler - GlobalSettings.getInstance().getScheduler()</li>
    *   <li>bossExecutor - Static ExecutorService.newCachedThreadPool</li>
    *   <li>workerExecutor - Static ExecutorService.newCachedThreadPool</li>
    * </ul>
    * @param SSLContext Optional SSLContext.
    * @param context Optional context.
    * @param scheduler Optional scheduler used for protocol timers.
    * @param bossExecutor Optional The channel factory.
    */
   public XioServer( final SSLContext sslContext, final IContext context, final ScheduledExecutorService scheduler, ServerSocketChannelFactory channelFactory)
   {
     this.listeners = new ArrayList<IListener>( 1);
     
     this.registry = new MemoryXioPeerRegistry( this);
     this.registry.addListener( registryListener);
     
     if ( channelFactory == null) channelFactory = getDefaultChannelFactory();
     
     bootstrap = new ServerBootstrap( channelFactory);
     bootstrap.setOption( "child.tcpNoDelay", true);
     
     bootstrap.setPipelineFactory( new ChannelPipelineFactory() {
       public ChannelPipeline getPipeline() throws Exception
       {
         ChannelPipeline pipeline = Channels.pipeline();
         
         if ( sslContext != null)
         {
           SSLEngine engine = sslContext.createSSLEngine();
           engine.setUseClientMode( false);
           //engine.setNeedClientAuth( true);
           pipeline.addLast( "ssl", new SslHandler( engine));
         }
 
        pipeline.addLast( "idleStateHandler", new IdleStateHandler( Heartbeat.timer, 300, 0, 300));
         pipeline.addLast( "heartbeatHandler", new Heartbeat());
         
         XioChannelHandler handler = new XioChannelHandler( context, context.getExecutor(), scheduler, registry);
         handler.getExecuteProtocol().requestProtocol.setPrivilege( executionPrivilege);
         pipeline.addLast( "xio", handler);
         
         handler.addListener( channelConnectionListener);
         
         return pipeline;
       }
     });
   }
   
   /**
    * Set the execution privileges.
    * @param privilege The privilege instance.
    */
   public void setExecutionPrivileges( ExecutionPrivilege privilege)
   {
     this.executionPrivilege = privilege;
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
    * Start the server on the specified address and port. 
    * @param address The server address.
    * @param port The server port.
    * @return Returns the channel instance of the server socket.
    */
   public Channel start( String address, int port)
   {
     serverChannel = bootstrap.bind( new InetSocketAddress( address, port));
     serverChannel.setAttachment( this);
     return serverChannel;
   }
   
   /**
    * Stop the server from listening for new connections.
    */
   public void stop()
   {
     if ( serverChannel != null) 
     {
       serverChannel.close().addListener( new ChannelFutureListener() {
         public void operationComplete( ChannelFuture future) throws Exception
         {
           serverChannel.getFactory().releaseExternalResources();
         }
       });
     }
   }
   
   /**
    * @return Returns the peer registry for this server.
    */
   public IXioPeerRegistry getPeerRegistry()
   {
     return registry;
   }
   
   private IXioPeerRegistryListener registryListener = new IXioPeerRegistryListener() {
     public void onRegister( XioPeer peer, String name)
     {
       if ( listeners != null)
       {
         for( IListener listener: listeners)
         {
           try { listener.notifyRegister( peer, name); }
           catch( Exception e)
           {
             SLog.errorf( this, "Exception was thrown by listener: %s", e.toString());
           }
         }
       }
     }
     public void onUnregister( XioPeer peer, String name)
     {
       if ( listeners != null)
       {
         for( IListener listener: listeners)
         {
           try { listener.notifyUnregister( peer, name); }
           catch( Exception e)
           {
             SLog.errorf( this, "Exception was thrown by listener: %s", e.toString());
           }
         }
       }
     }
   };
   
   /**
    * @return Returns the default ServerSocketChannelFactory.
    */
   private static synchronized ServerSocketChannelFactory getDefaultChannelFactory()
   {
     if ( defaultChannelFactory == null)
     {
       defaultChannelFactory = new NioServerSocketChannelFactory(
         Executors.newCachedThreadPool( new ModelThreadFactory( "xio-server-boss")),
         Executors.newCachedThreadPool( new ModelThreadFactory( "xio-server-work")));
     }
     return defaultChannelFactory;
   }
     
   /**
    * Notify listeners that a connection was established.
    * @param peer The peer that connected.
    */
   private void notifyChannelConnected( XioPeer peer)
   {
     if ( listeners != null)
     {
       for( IListener listener: listeners)
       {
         try { listener.notifyConnect( peer); }
         catch( Exception e)
         {
           SLog.errorf( this, "Exception was thrown by listener: %s", e.toString());
         }
       }
     }
   }
   
   /**
    * Notify listeners that a connection has been closed.
    * @param peer The peer that connected.
    */
   private void notifyChannelDisconnected( XioPeer peer)
   {
     registry.unregisterAll( peer);
     
     if ( listeners != null)
     {
       for( IListener listener: listeners)
       {
         try { listener.notifyDisconnect( peer); }
         catch( Exception e)
         {
           SLog.errorf( this, "Exception was thrown by listener: %s", e.toString());
         }
       }
     }
   }
     
   private XioChannelHandler.IListener channelConnectionListener = new XioChannelHandler.IListener() {
     public void notifyConnect( XioPeer peer)
     {
       SslHandler sslHandler = peer.getChannel().getPipeline().get( SslHandler.class);
       if ( sslHandler == null) notifyChannelConnected( peer);
     }      
     public void notifyDisconnect( XioPeer peer)
     {
       notifyChannelDisconnected( peer);
     }
   };
   
   private static ServerSocketChannelFactory defaultChannelFactory = null;
 
   private ServerBootstrap bootstrap;
   private Channel serverChannel;
   private IXioPeerRegistry registry;
   private ExecutionPrivilege executionPrivilege;
   private List<IListener> listeners;
 }

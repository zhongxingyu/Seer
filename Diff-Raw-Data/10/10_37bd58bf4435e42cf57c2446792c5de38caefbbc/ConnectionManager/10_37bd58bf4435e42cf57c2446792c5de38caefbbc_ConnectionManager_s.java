 package org.blockout.network.reworked;
 
 import java.net.Inet6Address;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.NetworkInterface;
 import java.net.SocketAddress;
 import java.net.SocketException;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.TimeUnit;
 
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelEvent;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelFutureListener;
 import org.jboss.netty.channel.ChannelHandler;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.ChannelUpstreamHandler;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.ChildChannelStateEvent;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import org.jboss.netty.handler.codec.serialization.ClassResolver;
 import org.jboss.netty.handler.codec.serialization.ClassResolvers;
 import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
 import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
 import org.jboss.netty.handler.timeout.IdleState;
 import org.jboss.netty.handler.timeout.IdleStateHandler;
 import org.jboss.netty.util.Timer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.task.TaskExecutor;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 
 public class ConnectionManager extends SimpleChannelHandler implements IConnectionManager {
 
 	private static final Logger				logger;
 	static {
 		logger = LoggerFactory.getLogger( ConnectionManager.class );
 	}
 
 	private final ChannelGroup				allChannels;
 	private ClientBootstrap					clientBootstrap;
 	private ServerBootstrap					serverBootstrap;
 	private Channel							serverChannel;
 	private final PipelineFactory			pipelineFactory;
 	private final Timer						timer;
 	private final int						keepAliveDelay;
 	private final int						keepAliveTimeout;
 	private final List<ConnectionListener>	listener;
 	private final TaskExecutor				executor;
 
 	public ConnectionManager(final Timer timer, final TaskExecutor executor, final int keepAliveDelay,
 			final int keepAliveTimeout) {
 
 		Preconditions.checkNotNull( timer );
 		Preconditions.checkNotNull( executor );
 
 		this.timer = timer;
 		this.executor = executor;
 		this.keepAliveTimeout = keepAliveTimeout;
 		this.keepAliveDelay = keepAliveDelay;
 		allChannels = new DefaultChannelGroup();
 		pipelineFactory = new PipelineFactory();
 		listener = Lists.newCopyOnWriteArrayList();
 	}
 
 	public ConnectionManager(final Timer timer, final TaskExecutor executor, final int keepAliveDelay,
 			final int keepAliveTimeout, final ChannelInterceptor... interceptors) {
 
 		Preconditions.checkNotNull( timer );
 		Preconditions.checkNotNull( executor );
 
 		this.timer = timer;
 		this.executor = executor;
 		this.keepAliveTimeout = keepAliveTimeout;
 		this.keepAliveDelay = keepAliveDelay;
 		allChannels = new DefaultChannelGroup();
 		pipelineFactory = new PipelineFactory();
 		listener = new CopyOnWriteArrayList<ConnectionListener>();
 		if ( interceptors != null && interceptors.length > 0 ) {
 			for ( ChannelInterceptor interceptor : interceptors ) {
 				addChannelInterceptor( interceptor );
				interceptor.init( this );
 			}
 		}
 	}
 
 	public void init() {
 		initClient();
 		initServer();
 	}
 
 	private void initClient() {
 		clientBootstrap = new ClientBootstrap( new NioClientSocketChannelFactory() );
 		clientBootstrap.setPipelineFactory( pipelineFactory );
 	}
 
 	private void initServer() {
 		serverBootstrap = new ServerBootstrap( new NioServerSocketChannelFactory() );
 		serverBootstrap.setPipelineFactory( pipelineFactory );
 		serverChannel = serverBootstrap.bind( new InetSocketAddress( findLocalNetworkInterface(), 0 ) );
 		logger.info( "Bound server to " + serverChannel.getLocalAddress() );
 	}
 
 	private InetAddress findLocalNetworkInterface() {
 		try {
 			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
 			while ( interfaces.hasMoreElements() ) {
 				NetworkInterface networkInterface = interfaces.nextElement();
 				if ( !networkInterface.isUp() ) {
 					logger.debug( "Skipped (down): " + networkInterface );
 					continue;
 				}
 				if ( networkInterface.isLoopback() ) {
 					logger.debug( "Skipped loopback: " + networkInterface );
 					continue;
 				}
 				if ( networkInterface.isPointToPoint() ) {
 					logger.debug( "Skipped ptp: " + networkInterface );
 					continue;
 				}
 				Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
 				while ( inetAddresses.hasMoreElements() ) {
 					InetAddress address = inetAddresses.nextElement();
 					if ( address.isLoopbackAddress() ) {
 						logger.debug( "Skipped loopback: " + address );
 						continue;
 					}
 					if ( address.isMCNodeLocal() ) {
 						logger.debug( "Skipped node local: " + address );
 						continue;
 					}
 					if ( address instanceof Inet6Address ) {
 						logger.debug( "Skipped V6 address: " + address );
 						continue;
 					}
 					logger.debug( "Selected: " + address + " of " + networkInterface );
 					return address;
 				}
 			}
 		} catch ( SocketException e ) {
 			throw new RuntimeException( "No appropriate network interface found.", e );
 		}
 		throw new RuntimeException( "No appropriate network interface found." );
 	}
 
 	public void addChannelInterceptor( final ChannelInterceptor interceptor ) {
 		pipelineFactory.addChannelInterceptor( interceptor );
 	}
 
 	@Override
 	public SocketAddress getServerAddress() {
 		return serverChannel.getLocalAddress();
 	}
 
 	@Override
 	public Set<SocketAddress> listConnections() {
 		Set<SocketAddress> connections = new HashSet<SocketAddress>();
 		for ( Channel channel : allChannels ) {
 			connections.add( channel.getRemoteAddress() );
 		}
 		return connections;
 	}
 
 	@Override
 	public ConnectionFuture connectTo( final SocketAddress address ) {
 
 		// sync{
 		// - Check if there is already a pending connect
 		//
 		// - if not check the channels
 		// }
 		// connect
 
 		Channel channel = findChannel( address );
 		if ( channel != null ) {
 			logger.debug( "Channel already connected to " + address );
 			return new ConnectionFutureAdapter( Channels.succeededFuture( channel ) );
 		}
 
 		logger.info( "Connecting to " + address );
 		ChannelFuture future = clientBootstrap.connect( address );
 		future.addListener( new ChannelFutureListener() {
 
 			@Override
 			public void operationComplete( final ChannelFuture future ) throws Exception {
 				if ( !future.isSuccess() ) {
 					logger.warn( "Failed to connect to " + future.getChannel().getRemoteAddress(), future.getCause() );
 				}
 				logger.info( "Connected to " + future.getChannel().getRemoteAddress() );
 			}
 		} );
 		return new ConnectionFutureAdapter( future );
 	}
 
 	@Override
 	public void disconnectFrom( final SocketAddress address ) {
 		logger.info( "Disconnecting from " + address );
 		Channel channel = findChannel( address );
 		if ( channel == null ) {
 			return;
 		}
 		channel.close();
 	}
 
 	private Channel findChannel( final SocketAddress address ) {
 
 		for ( Channel channel : allChannels ) {
 			if ( channel.getRemoteAddress().equals( address ) ) {
 				return channel;
 			}
 		}
 
 		return null;
 	}
 
 	@Override
 	public void exceptionCaught( final ChannelHandlerContext ctx, final ExceptionEvent e ) throws Exception {
 		logger.warn( "Caught exception in network stack.", e.getCause() );
 		super.exceptionCaught( ctx, e );
 	}
 
 	@Override
 	public void channelConnected( final ChannelHandlerContext ctx, final ChannelStateEvent e ) throws Exception {
 		logger.info( "ChannelConnected to " + e.getChannel().getRemoteAddress() );
 		if ( e.getChannel().getParent() == null ) {
 			fireConnected( e.getChannel() );
 		} else {
 			fireClientConnected( e.getChannel() );
 		}
 		super.channelConnected( ctx, e );
 	}
 
 	@Override
 	public void channelDisconnected( final ChannelHandlerContext ctx, final ChannelStateEvent e ) throws Exception {
 		logger.info( "Disconnected from " + e.getChannel().getRemoteAddress() );
 		if ( e.getChannel().getParent() == null ) {
 			fireDisconnected( e.getChannel() );
 		} else {
 			fireClientDisconnected( e.getChannel() );
 		}
 		e.getChannel().close();
 		super.channelDisconnected( ctx, e );
 	}
 
 	@Override
 	public void childChannelOpen( final ChannelHandlerContext ctx, final ChildChannelStateEvent e ) throws Exception {
 		logger.info( "Client " + e.getChannel().getRemoteAddress() + " connected." );
 
 		allChannels.add( e.getChannel() );
 
 		super.childChannelOpen( ctx, e );
 	}
 
 	@Override
 	public void childChannelClosed( final ChannelHandlerContext ctx, final ChildChannelStateEvent e ) throws Exception {
 		logger.info( "Client " + e.getChannel().getRemoteAddress() + " disconnected." );
 		super.childChannelClosed( ctx, e );
 	}
 
 	@Override
 	public void addConnectionListener( final ConnectionListener l ) {
 		listener.add( l );
 	}
 
 	@Override
 	public void removeConnectionListener( final ConnectionListener l ) {
 		listener.remove( l );
 	}
 
 	private void fireConnected( final Channel c ) {
 		for ( final ConnectionListener l : listener ) {
 			executor.execute( new Runnable() {
 
 				@Override
 				public void run() {
 					l.connected( ConnectionManager.this, c.getLocalAddress(), c.getRemoteAddress() );
 				}
 			} );
 		}
 	}
 
 	private void fireDisconnected( final Channel c ) {
 		for ( final ConnectionListener l : listener ) {
 			executor.execute( new Runnable() {
 
 				@Override
 				public void run() {
 					l.disconnected( ConnectionManager.this, c.getLocalAddress(), c.getRemoteAddress() );
 				}
 			} );
 		}
 	}
 
 	private void fireClientConnected( final Channel c ) {
 		for ( final ConnectionListener l : listener ) {
 			executor.execute( new Runnable() {
 
 				@Override
 				public void run() {
 					l.clientConnected( ConnectionManager.this, c.getLocalAddress(), c.getRemoteAddress() );
 				}
 			} );
 		}
 	}
 
 	private void fireClientDisconnected( final Channel c ) {
 		for ( final ConnectionListener l : listener ) {
 			executor.execute( new Runnable() {
 
 				@Override
 				public void run() {
 					l.clientDisconnected( ConnectionManager.this, c.getLocalAddress(), c.getRemoteAddress() );
 				}
 			} );
 		}
 	}
 
 	private class PipelineFactory implements ChannelPipelineFactory {
 
 		private final List<ChannelInterceptor>	interceptors;
 		private final ChannelHandler			keepAliveHandler;
 		private final ChannelHandler			timeoutHandler;
 
 		public PipelineFactory() {
 			interceptors = Lists.newArrayList();
 			keepAliveHandler = new KeepAliveChannelHandler( timer, keepAliveDelay, 0, 0, TimeUnit.MILLISECONDS );
 			timeoutHandler = new TimeoutChannelHandler( timer, keepAliveTimeout, 0, 0, TimeUnit.MILLISECONDS );
 		}
 
 		public void addChannelInterceptor( final ChannelInterceptor interceptor ) {
 			interceptors.add( interceptor );
 		}
 
 		@Override
 		public ChannelPipeline getPipeline() throws Exception {
 			ChannelPipeline pipeline = Channels.pipeline();
 			pipeline.addLast( "connectionManager", ConnectionManager.this );
 			pipeline.addLast( "objectEncoder", new ObjectEncoder() );
 			ClassResolver classResolver = ClassResolvers.cacheDisabled( getClass().getClassLoader() );
 			pipeline.addLast( "objectDecoder", new ObjectDecoder( classResolver ) );
 			pipeline.addLast( "keepAliveHandler", keepAliveHandler );
 			pipeline.addLast( "timeoutHandler", timeoutHandler );
 			pipeline.addLast( "keepAliveFilter", new KeepAlivePacketFilter() );
 			for ( ChannelInterceptor interceptor : interceptors ) {
 				pipeline.addLast( interceptor.getName(), new ChannelHandlerInterceptorAdapter( ConnectionManager.this,
 						interceptor ) );
 			}
 			return pipeline;
 		}
 	}
 
 	@ChannelHandler.Sharable
 	private static class KeepAlivePacketFilter implements ChannelUpstreamHandler {
 
 		@Override
 		public void handleUpstream( final ChannelHandlerContext ctx, final ChannelEvent e ) throws Exception {
 			if ( e instanceof MessageEvent ) {
 				MessageEvent evt = (MessageEvent) e;
 				if ( evt.getMessage() instanceof KeepAliveMessage ) {
 					// Discard KeepAlive messages
 					return;
 				}
 			}
 			ctx.sendUpstream( e );
 		}
 	}
 
 	@ChannelHandler.Sharable
 	private static class KeepAliveChannelHandler extends IdleStateHandler {
 		private static final Logger	logger;
 		static {
 			logger = LoggerFactory.getLogger( KeepAliveChannelHandler.class );
 		}
 
 		public KeepAliveChannelHandler(final Timer timer, final int readerIdleTimeSeconds,
 				final int writerIdleTimeSeconds, final int allIdleTimeSeconds) {
 			super( timer, readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds );
 		}
 
 		public KeepAliveChannelHandler(final Timer timer, final long readerIdleTime, final long writerIdleTime,
 				final long allIdleTime, final TimeUnit unit) {
 			super( timer, readerIdleTime, writerIdleTime, allIdleTime, unit );
 		}
 
 		@Override
 		protected void channelIdle( final ChannelHandlerContext ctx, final IdleState state,
 				final long lastActivityTimeMillis ) throws Exception {
 			super.channelIdle( ctx, state, lastActivityTimeMillis );
 			logger.info( "Channel " + ctx.getChannel() + " has been idle. Sending keep alive.." );
 			Channels.write( ctx.getChannel(), new KeepAliveMessage( false ) );
 		}
 
 		@Override
 		public void messageReceived( final ChannelHandlerContext ctx, final MessageEvent e ) throws Exception {
 			super.messageReceived( ctx, e );
 
 			if ( e.getMessage() instanceof KeepAliveMessage ) {
 				KeepAliveMessage msg = (KeepAliveMessage) e.getMessage();
 				// Check that this is not a response to previous ack to prevent
 				// looping
 				if ( !msg.isAck() ) {
 					Channels.write( ctx.getChannel(), new KeepAliveMessage( true ) );
 				}
 			}
 		}
 	}
 
 	@ChannelHandler.Sharable
 	private static class TimeoutChannelHandler extends IdleStateHandler {
 		private static final Logger	logger;
 		static {
 			logger = LoggerFactory.getLogger( KeepAliveChannelHandler.class );
 		}
 
 		public TimeoutChannelHandler(final Timer timer, final int readerIdleTimeSeconds,
 				final int writerIdleTimeSeconds, final int allIdleTimeSeconds) {
 			super( timer, readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds );
 		}
 
 		public TimeoutChannelHandler(final Timer timer, final long readerIdleTime, final long writerIdleTime,
 				final long allIdleTime, final TimeUnit unit) {
 			super( timer, readerIdleTime, writerIdleTime, allIdleTime, unit );
 		}
 
 		@Override
 		protected void channelIdle( final ChannelHandlerContext ctx, final IdleState state,
 				final long lastActivityTimeMillis ) throws Exception {
 			super.channelIdle( ctx, state, lastActivityTimeMillis );
 			logger.info( "Channel " + ctx.getChannel() + " has been timed out. Closing it." );
 			ctx.getChannel().close();
 		}
 	}
 }

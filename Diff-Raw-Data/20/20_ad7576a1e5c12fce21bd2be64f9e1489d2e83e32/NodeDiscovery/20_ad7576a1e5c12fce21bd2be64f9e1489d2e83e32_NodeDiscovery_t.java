 package org.blockout.network.discovery;
 
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.blockout.network.reworked.IConnectionManager;
 import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.ExceptionEvent;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.SimpleChannelHandler;
 import org.jboss.netty.channel.socket.DatagramChannel;
 import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
 import org.jboss.netty.handler.codec.serialization.ClassResolver;
 import org.jboss.netty.handler.codec.serialization.ClassResolvers;
 import org.jboss.netty.handler.codec.serialization.ObjectDecoder;
 import org.jboss.netty.handler.codec.serialization.ObjectEncoder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.scheduling.TaskScheduler;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Sets;
 
 public class NodeDiscovery extends SimpleChannelHandler implements INodeDiscovery {
 
 	private static final Logger								logger;
 	static {
 		logger = LoggerFactory.getLogger( NodeDiscovery.class );
 	}
 
 	public final int										port;
 	private final TaskScheduler								executor;
 
 	private final CopyOnWriteArraySet<SocketAddress>		nodes;
 	private final CopyOnWriteArraySet<DiscoveryListener>	listeners;
 
 	private ConnectionlessBootstrap							bootstrap;
 	private DatagramChannel									discoveryChannel;
 
 	private final IConnectionManager						connectionMgr;
 
 	public NodeDiscovery(final TaskScheduler executor, final int discoveryPort, final int messageDelay,
 			final IConnectionManager connectionMgr) {
 
 		Preconditions.checkNotNull( executor );
 		Preconditions.checkElementIndex( discoveryPort, 65536, "Port must be in range of [0,65535]." );
 		Preconditions.checkArgument( messageDelay > 0, "Message delay must be greater than zero." );
 		Preconditions.checkNotNull( connectionMgr );
 
 		this.executor = executor;
 		port = discoveryPort;
 		this.connectionMgr = connectionMgr;
 
 		nodes = Sets.newCopyOnWriteArraySet();
 		listeners = Sets.newCopyOnWriteArraySet();
 
 		setupDiscoveryChannel();
 		sendContinuousDiscoveryMessages( executor, messageDelay );
 	}
 
 	private void setupDiscoveryChannel() {
 		bootstrap = new ConnectionlessBootstrap( new NioDatagramChannelFactory() );
 
 		bootstrap.setPipelineFactory( new ChannelPipelineFactory() {
 			@Override
 			public ChannelPipeline getPipeline() throws Exception {
 				ObjectEncoder encoder = new ObjectEncoder();
 				ClassResolver classResolver = ClassResolvers.cacheDisabled( this.getClass().getClassLoader() );
 				ObjectDecoder decoder = new ObjectDecoder( classResolver );
 				return Channels.pipeline( encoder, decoder, NodeDiscovery.this );
 			}
 		} );
 
 		bootstrap.setOption( "reuseAddress", "true" );
 		bootstrap.setOption( "broadcast", "true" );
 
 		discoveryChannel = (DatagramChannel) bootstrap.bind( new InetSocketAddress( port ) );
 		logger.info( "NodeDiscovery bound to " + discoveryChannel.getLocalAddress() );
 	}
 
 	private void sendContinuousDiscoveryMessages( final TaskScheduler executor, final int messageDelay ) {
 		logger.info( "Sending discovery messages each " + messageDelay + " milliseconds." );
 		executor.scheduleAtFixedRate( new Runnable() {
 			@Override
 			public void run() {
 				sendDiscoveryMessage( new DiscoveryMsg( connectionMgr.getServerAddress() ) );
 			}
 		}, messageDelay );
 	}
 
 	private void sendDiscoveryMessage( final DiscoveryMsg discoveryMessage ) {
 
 		Preconditions.checkNotNull( discoveryMessage );
 
 		if ( logger.isDebugEnabled() ) {
 			logger.debug( "Sending discovery message: " + discoveryMessage );
 		}
 
 		InetSocketAddress broadcastAddress = new InetSocketAddress( "255.255.255.255", port );
 		discoveryChannel.write( discoveryMessage, broadcastAddress ).awaitUninterruptibly();
 	}
 
 	@Override
 	public void messageReceived( final ChannelHandlerContext ctx, final MessageEvent e ) {
 
 		if ( logger.isDebugEnabled() ) {
 			logger.debug( "Received node discovery message: " + e.getMessage() );
 		}
 		final DiscoveryMsg msg = (DiscoveryMsg) e.getMessage();
 		if ( msg.getServerAddress().equals( connectionMgr.getServerAddress() ) ) {
 			logger.debug( "Discarding own discovery packet: " + e.getMessage() );
 			return;
 		}
 
 		Runnable handleMessage = new Runnable() {
 			@Override
 			public void run() {
 				NodeDiscovery.this.fireNodeDiscoveredIfNewNode( msg.getServerAddress() );
 			}
 		};
 		executor.schedule( handleMessage, Calendar.getInstance().getTime() );
 	}
 
 	@Override
 	public void exceptionCaught( final ChannelHandlerContext ctx, final ExceptionEvent e ) throws Exception {
 		logger.warn( "Closing channel " + e.getChannel() + " due to exception.", e.getCause() );
 		e.getChannel().close();
 	}
 
 	private void fireNodeDiscoveredIfNewNode( final SocketAddress nodeAddress ) {
		synchronized ( nodes ) {
			if ( !nodes.contains( nodeAddress ) ) {
				nodes.add( nodeAddress );
				connectionMgr.connectTo( nodeAddress );
				fireNodeDiscovered( nodeAddress );
			}
 		}
 	}
 
 	private void fireNodeDiscovered( final SocketAddress nodeAddress ) {
 		for ( DiscoveryListener l : listeners ) {
 			l.nodeDiscovered( nodeAddress );
 		}
 	}
 
 	@Override
 	public List<SocketAddress> listNodes() {
 		return new ArrayList<SocketAddress>( nodes );
 	}
 
 	@Override
 	public void addDiscoveryListener( final DiscoveryListener l ) {
 		listeners.add( l );
 	}
 
 	@Override
 	public void removeDiscoveryListener( final DiscoveryListener l ) {
 		listeners.remove( l );
 	}
 }

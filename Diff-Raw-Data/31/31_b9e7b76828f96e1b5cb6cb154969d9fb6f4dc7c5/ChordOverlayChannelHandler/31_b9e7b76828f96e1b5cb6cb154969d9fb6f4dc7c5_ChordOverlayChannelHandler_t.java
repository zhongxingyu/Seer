 package org.blockout.network.reworked;
 
 import java.io.Serializable;
 import java.net.SocketAddress;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.ExecutionException;
 
 import org.blockout.network.LocalNode;
 import org.blockout.network.dht.IHash;
 import org.blockout.network.dht.WrappedRange;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandler;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.MessageEvent;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.channel.socket.ClientSocketChannelFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.task.TaskExecutor;
 import org.springframework.scheduling.TaskScheduler;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Maps;
 
 /**
  * Chord overlay for multiple {@link Channel}s of the netty framework.
  * 
  * TODO: Implement finger table to increase performance
  * 
  * @author Marc-Christian Schulze
  * 
  */
 @ChannelHandler.Sharable
 public class ChordOverlayChannelHandler extends ChannelInterceptorAdapter implements IChordOverlay {
 
 	private static final Logger				logger;
 	static {
 		logger = LoggerFactory.getLogger( ChordOverlayChannelHandler.class );
 	}
 
 	private final Set<FindSuccessorFuture>	pendingSuccessorLookups;
 	private final List<ChordListener>		listener;
 	private volatile WrappedRange<IHash>	responsibility;
 
 	private IHash							successorId;
 	private Channel							successorChannel;
 
 	private IHash							predecessorId;
 	private Channel							predecessorChannel;
 
 	private final ChannelGroup				channels;
 	private final LocalNode					localNode;
 	private final TaskScheduler				scheduler;
 	private final List<SocketAddress>		introductionFilter;
 
 	private IConnectionManager				connectionMgr;
 	private final long						stabilizationRate;
 
 	private final TreeMap<IHash, Channel>	lookupTable;
 
 	/**
 	 * Creates a new chord overlay with the initial responsibility of
 	 * <code>(local_node_id + 1, local_node_id]</code>.
 	 * 
 	 * @param localNode
 	 *            The local node to retrieve the local node id from.
 	 * @param scheduler
 	 *            An executor for dispatching the listener invocations.
 	 */
 	public ChordOverlayChannelHandler(final LocalNode localNode, final TaskScheduler scheduler,
 			final long stabilizationRate) {
 
 		Preconditions.checkNotNull( localNode );
 		Preconditions.checkNotNull( scheduler );
 
 		this.localNode = localNode;
 		this.scheduler = scheduler;
 		this.stabilizationRate = stabilizationRate;
 
 		lookupTable = Maps.newTreeMap();
 		introductionFilter = Collections.synchronizedList( new ArrayList<SocketAddress>() );
 
 		pendingSuccessorLookups = Collections.synchronizedSet( new HashSet<FindSuccessorFuture>() );
 		listener = new CopyOnWriteArrayList<ChordListener>();
 		channels = new DefaultChannelGroup();
 
 		IHash ownNodeId = localNode.getNodeId();
 		responsibility = new WrappedRange<IHash>( ownNodeId.getNext(), ownNodeId );
 		logger.info( "Initialized chord with range = " + responsibility );
 	}
 
 	private Channel getOrCreateChannel( final IHash hash, final SocketAddress address ) {
 		synchronized ( lookupTable ) {
 			Channel channel = lookupTable.get( hash );
 			if ( channel != null ) {
 				return channel;
 			}
 		}
 
 		ConnectionFuture future = connectionMgr.connectTo( address );
 		future.awaitUninterruptibly();
 
 		for ( Channel channel : channels ) {
 			if ( channel.getRemoteAddress().equals( address ) ) {
 				return channel;
 			}
 		}
 		logger.error( "Failed to create channel to " + hash + " - " + address );
 		return null;
 	}
 
 	@Override
 	public void channelClosed( final IConnectionManager connectionMgr, final ChannelHandlerContext ctx,
 			final ChannelStateEvent e ) throws Exception {
 		super.channelClosed( connectionMgr, ctx, e );
 
 		// remove closed channels from lookup table
 		synchronized ( lookupTable ) {
 			for ( Entry<IHash, Channel> entry : lookupTable.entrySet() ) {
 				if ( entry.getValue().equals( e.getChannel() ) ) {
 					lookupTable.remove( entry.getKey() );
 					break;
 				}
 			}
 		}
 	}
 
 	@Override
 	public void messageReceived( final IConnectionManager connectionMgr, final ChannelHandlerContext ctx,
 			final MessageEvent e ) throws Exception {
 
 		Object message = e.getMessage();
 		if ( message instanceof FindSuccessorMessage ) {
 			handleFindSuccessorMessage( connectionMgr, e, (FindSuccessorMessage) message );
 		} else if ( message instanceof SuccessorFoundMessage ) {
 			handleFoundSuccessorMessage( (SuccessorFoundMessage) message );
 		} else if ( message instanceof IAmMessage ) {
 			handleIAmMessage( connectionMgr, e, (IAmMessage) message );
 		} else if ( message instanceof ChordEnvelope ) {
			logger.debug( "Envelope: " + message );
 			ChordEnvelope envelope = (ChordEnvelope) message;
 			fireMessageReceived( envelope.getSenderId(), envelope.getContent() );
 		} else if ( message instanceof WelcomeMessage ) {
 			handleWelcomeMessage( e, (WelcomeMessage) message );
 		} else if ( message instanceof IAmYourPredeccessor ) {
 			handleIAmYourPredeccessorMessage( e, (IAmYourPredeccessor) message );
 		} else {
 			logger.warn( "Discard unknown message type " + message );
 		}
 
 		super.messageReceived( connectionMgr, ctx, e );
 	}
 
 	private void handleIAmYourPredeccessorMessage( final MessageEvent e, final IAmYourPredeccessor msg ) {
 		predecessorChannel = e.getChannel();
 		if ( !msg.getNodeId().equals( predecessorId ) ) {
 			logger.info( "Got notified about new predecessor " + predecessorId + " at " + predecessorChannel );
 			predecessorId = msg.getNodeId();
 
 			updateResponsibility( predecessorId.getNext() );
 		}
 	}
 
 	private void handleWelcomeMessage( final MessageEvent e, final WelcomeMessage msg ) {
 
 		synchronized ( lookupTable ) {
 			lookupTable.put( msg.getSuccessorId(), e.getChannel() );
 		}
 
 		logger.info( "Joining chord ring. My successor will be " + msg.getSuccessorId() + " at "
 				+ msg.getSuccessorAddress() );
 
 		successorId = msg.getSuccessorId();
 		successorChannel = e.getChannel();
 
 		updateResponsibility( msg.getLowerBound() );
 	}
 
 	private void updateResponsibility( final IHash lowerBound ) {
 		WrappedRange<IHash> newResponsibility;
 		newResponsibility = new WrappedRange<IHash>( lowerBound, localNode.getNodeId() );
 		WrappedRange<IHash> tmp = responsibility;
 		if ( !newResponsibility.equals( tmp ) ) {
 			logger.info( "Responsibility changed from " + tmp + " to " + newResponsibility );
 			responsibility = newResponsibility;
 			fireResponsibilityChanged( tmp, newResponsibility );
 		}
 	}
 
 	/**
 	 * Invoked when a {@link FindSuccessorMessage} has been received. It
 	 * responds with a {@link SuccessorFoundMessage} if we are the successor and
 	 * forwards the message to our successor if not.
 	 * 
 	 * @param e
 	 * @param msg
 	 */
 	private void handleFindSuccessorMessage( final IConnectionManager connectionMgr, final MessageEvent e,
 			final FindSuccessorMessage msg ) {
 
 		if ( responsibility.contains( msg.getKey() ) ) {
 			// I'm the successor
 			logger.debug( "I'm the successor: " + responsibility + " contained " + msg.getKey() );
 			// logger.debug( "Local node is the successor of " + msg.getKey() );
 			SuccessorFoundMessage responseMsg;
 			responseMsg = new SuccessorFoundMessage( msg.getOrigin(), msg.getKey(), localNode.getNodeId(),
 					connectionMgr.getServerAddress() );
 			Channels.write( e.getChannel(), responseMsg );
 			return;
 		}
 		// Ask my successor
 		if ( logger.isDebugEnabled() ) {
 			String description = "Forwarding successor lookup to own successor {} ({})";
 			logger.debug( description, successorChannel.getRemoteAddress(), successorId );
 		}
 		successorChannel.write( msg );
 	}
 
 	/**
 	 * Invoked when a {@link SuccessorFoundMessage} has been received. It
 	 * completes all pending successor lookups with the same key and forwards
 	 * all messages not destined for this node to our predecessor.
 	 * 
 	 * @param ctx
 	 * @param e
 	 * @param msg
 	 */
 	private void handleFoundSuccessorMessage( final SuccessorFoundMessage msg ) {
 
 		// We don't care if this is a response to our lookup or someone else's
 		completeFutures( msg );
 
 		if ( !msg.getDestination().equals( localNode.getNodeId() ) ) {
 			// We need to route the message back
 			routeMessage( msg, msg.getDestination() );
 		}
 	}
 
 	private void handleIAmMessage( final IConnectionManager connectionMgr, final MessageEvent e,
 			final IAmMessage message ) {
 
 		synchronized ( lookupTable ) {
 			lookupTable.put( message.getNodeId(), e.getChannel() );
 		}
 
 		if ( responsibility.contains( message.getNodeId() ) ) {
 			// We are the new node's successor
 			welcomeNode( connectionMgr, e, message );
 			return;
 		}
 
 		// Route message to successor of new node
 		ObservableFuture<IHash> future = findSuccessor( message.getNodeId() );
 		future.addFutureListener( new FutureListener<IHash>() {
 
 			@Override
 			public void completed( final ObservableFuture<IHash> future ) {
 				try {
 					IHash nodeId = future.get();
 					logger.debug( "Found successor " + nodeId + " of new node " + message.getNodeId() );
 					routeMessage( message, nodeId );
 				} catch ( InterruptedException e ) {
 					logger.error( "Someone interrupted the successor lookup.", e );
 				} catch ( ExecutionException e ) {
 					logger.error( "Successor lookup failed.", e );
 				}
 			}
 		} );
 	}
 
 	private void welcomeNode( final IConnectionManager connectionMgr, final MessageEvent e, final IAmMessage message ) {
 
 		// mark the new connection so that no I'm message will be sent
 		introductionFilter.add( message.getAddress() );
 
 		logger.info( "Connecting to new node " + message.getAddress() + " to welcome it." );
 		ConnectionFuture future = connectionMgr.connectTo( message.getAddress() );
 		future.addListener( new ConnectionFutureListener() {
 
 			@Override
 			public void operationComplete( final ConnectionFuture connectFuture ) throws Exception {
 				if ( !connectFuture.isSuccess() ) {
 					logger.error( "Couldn't connect to new node " + message.getAddress(), connectFuture.getCause() );
 					return;
 				}
 
 				Channel channel = connectFuture.getChannel();
 
 				sendWelcomeMessage( connectionMgr, message, channel );
 			}
 
 		} );
 	}
 
 	private void sendWelcomeMessage( final IConnectionManager connectionMgr, final IAmMessage message,
 			final Channel channel ) {
 		logger.info( "Connected to " + message.getAddress() + " sending welcome message." );
 		// Send welcome message
 		SocketAddress serverAddress = connectionMgr.getServerAddress();
 		WelcomeMessage welcomeMsg;
 		welcomeMsg = new WelcomeMessage( localNode.getNodeId(), serverAddress, responsibility.getLowerBound() );
 
 		Channels.write( channel, welcomeMsg );
 
 		// Accept our new predecessor
 		predecessorChannel = channel;
 		predecessorId = message.getNodeId();
 
 		updateResponsibility( message.getNodeId().getNext() );
 	}
 
 	/**
 	 * Completes all pending successor lookup futures that wait for the key of
 	 * the given message.
 	 * 
 	 * @param msg
 	 *            The message about the discovered successor
 	 */
 	private void completeFutures( final SuccessorFoundMessage msg ) {
 		synchronized ( pendingSuccessorLookups ) {
 			for ( FindSuccessorFuture future : pendingSuccessorLookups ) {
 				if ( future.getKey().equals( msg.getKey() ) ) {
 					future.complete( new HashAndAddress( msg.getSuccessor(), msg.getServerAddress() ) );
 					// Don't abort since there might be multiple pending lookups
 				}
 			}
 		}
 	}
 
 	@Override
 	public void channelConnected( final IConnectionManager connectionMgr, final ChannelHandlerContext ctx,
 			final ChannelStateEvent e ) throws Exception {
 
 		logger.info( "ChannelConnected: parent=" + e.getChannel().getParent() + ", channel=" + e.getChannel() );
 
 		channels.add( e.getChannel() );
 
 		// Introduce ourself when we have connected.
 		if ( e.getChannel().getFactory() instanceof ClientSocketChannelFactory ) {
 			// only clients introduce themself
 
 			if ( !introductionFilter.contains( e.getChannel().getRemoteAddress() ) ) {
 				logger.info( "We connected to a stranger. Introduce ourself." );
 				Channels.write( e.getChannel(),
 						new IAmMessage( localNode.getNodeId(), connectionMgr.getServerAddress() ) );
 			} else {
 				introductionFilter.remove( e.getChannel().getRemoteAddress() );
 			}
 		}
 
 		super.channelConnected( connectionMgr, ctx, e );
 	}
 
 	@Override
 	public WrappedRange<IHash> getResponsibility() {
 		return responsibility;
 	}
 
 	@Override
 	public ObservableFuture<IHash> findSuccessor( final IHash key ) {
 		FindSuccessorFuture future = findPendingSuccessorLookup( key );
 		if ( future != null ) {
 			// There is already a pending lookup
 			// for the successor of the same key
 			return future;
 		}
 		future = new FindSuccessorFuture( pendingSuccessorLookups, key );
 		if ( responsibility.contains( key ) ) {
 			logger.debug( "LocalNode is the successor: " + responsibility + " contained " + key );
 			// Local node is the successor
 			future.complete( new HashAndAddress( localNode.getNodeId(), connectionMgr.getServerAddress() ) );
 			return future;
 		}
 		// Send a lookup message to our successor
 		Channels.write( successorChannel, new FindSuccessorMessage( localNode.getNodeId(), key ) );
 		return future;
 	}
 
 	/**
 	 * Returns the first {@link FindSuccessorFuture} already pending for the
 	 * given key if present; otherwise null.
 	 * 
 	 * @param key
 	 * @return The pending future or null if no present.
 	 */
 	private FindSuccessorFuture findPendingSuccessorLookup( final IHash key ) {
 		synchronized ( pendingSuccessorLookups ) {
 			for ( FindSuccessorFuture future : pendingSuccessorLookups ) {
 				if ( future.getKey().equals( key ) ) {
 					return future;
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public void addChordListener( final ChordListener l ) {
 		listener.add( l );
 	}
 
 	@Override
 	public void removeChordListener( final ChordListener l ) {
 		listener.remove( l );
 	}
 
 	/**
 	 * Notifies all listener about the responsibility changed using the
 	 * {@link TaskExecutor} passed in the constructor for dispatching.
 	 * 
 	 * @param from
 	 * @param to
 	 */
 	private void fireResponsibilityChanged( final WrappedRange<IHash> from, final WrappedRange<IHash> to ) {
 		for ( final ChordListener l : listener ) {
 			scheduler.schedule( new Runnable() {
 
 				@Override
 				public void run() {
 					l.responsibilityChanged( ChordOverlayChannelHandler.this, from, to );
 				}
 			}, Calendar.getInstance().getTime() );
 		}
 	}
 
 	private void fireMessageReceived( final IHash from, final Object message ) {
 		logger.info( "Received message " + message + " from " + from );
 		for ( final ChordListener l : listener ) {
 			scheduler.schedule( new Runnable() {
 
 				@Override
 				public void run() {
					logger.debug( "Passing message " + message + " to " + l );
 					l.receivedMessage( ChordOverlayChannelHandler.this, message, from );
 				}
 			}, Calendar.getInstance().getTime() );
 		}
 	}
 
 	@Override
 	public void sendMessage( final Serializable message, final IHash nodeId ) {
		if ( responsibility.contains( nodeId ) ) {

			fireMessageReceived( nodeId, message );

			return;
		}
 		routeMessage( new ChordEnvelope( localNode.getNodeId(), nodeId, message ), nodeId );
 	}
 
 	private void routeMessage( final Serializable message, final IHash nodeId ) {
 		logger.debug( "Routing message " + message + " to " + nodeId );
 
 		Channel channel;
 		IHash higherKey = lookupTable.higherKey( nodeId );
 		if ( higherKey == null ) {
 			// either we passed the upper bound
 			// or the last finger is matching the key exactly
 			if ( lookupTable.lastKey().equals( nodeId ) ) {
 				channel = lookupTable.get( nodeId );
 			} else {
 				channel = lookupTable.lastEntry().getValue();
 			}
 		} else {
 			IHash lowerKey = lookupTable.lowerKey( higherKey );
 			channel = lookupTable.get( lowerKey );
 		}
 
 		logger.debug( "Routing message " + message + " using channel " + channel );
 		Channels.write( channel, message );
 	}
 
 	@Override
 	public String getName() {
 		return "ChordOverlayHandler";
 	}
 
 	@Override
 	public void init( final IConnectionManager conMgr ) {
 		connectionMgr = conMgr;
 		// Configure stabilization of successor and fingers
 		scheduler.scheduleAtFixedRate( new Runnable() {
 
 			@Override
 			public void run() {
 				ObservableFuture<IHash> future = findSuccessor( localNode.getNodeId().getNext() );
 				future.addFutureListener( new FutureListener<IHash>() {
 
 					@Override
 					public void completed( final ObservableFuture<IHash> future ) {
 						try {
 							HashAndAddress hash = (HashAndAddress) future.get();
 							if ( hash != null && !hash.equals( successorId ) ) {
 								logger.warn( "Stabilization detected invalid successor. Current " + successorId
 										+ ", actual " + hash );
 
 								successorChannel = getOrCreateChannel( hash, hash.getAddress() );
 								successorId = hash;
 
 								// Notify the new successor about us - so that
 								// he can adjust his responsibility
 								Channels.write( successorChannel, new IAmYourPredeccessor( localNode.getNodeId() ) );
 							}
 						} catch ( InterruptedException e ) {
 							logger.warn( "Stabilization interrupted.", e );
 						} catch ( ExecutionException e ) {
 							logger.error( "Stabilization failed.", e );
 						}
 					}
 
 				} );
 			}
 		}, stabilizationRate );
 	}
 }

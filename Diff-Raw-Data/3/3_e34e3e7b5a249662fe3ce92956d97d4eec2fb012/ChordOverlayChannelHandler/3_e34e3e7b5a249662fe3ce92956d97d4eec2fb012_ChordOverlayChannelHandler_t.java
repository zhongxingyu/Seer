 package org.blockout.network.reworked;
 
 import java.io.Serializable;
 import java.net.SocketAddress;
 import java.util.ArrayList;
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
 	private final TaskExecutor				executor;
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
 			final TaskExecutor executor, final long stabilizationRate) {
 
 		Preconditions.checkNotNull( localNode );
 		Preconditions.checkNotNull( scheduler );
 
 		this.localNode = localNode;
 		this.scheduler = scheduler;
 		this.executor = executor;
 		this.stabilizationRate = stabilizationRate;
 
 		successorId = localNode.getNodeId();
 		predecessorId = localNode.getNodeId();
 
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
		if ( hash.equals( localNode.getNodeId() ) ) {
			return null;
		}
 
 		ConnectionFuture future = connectionMgr.connectTo( address );
 		future.awaitUninterruptibly();
 		return future.getChannel();
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
 
 		// // check if it was our predecessor
 		// if ( e.getChannel().equals( predecessorChannel ) ) {
 		// // replace predecessor with most closest node or ourself
 		// // and adjust our responsibility
 		// logger.info( "Channel " + e.getChannel() + " to our predecessor " +
 		// predecessorId + " has been closed." );
 		// IHash newId;
 		// Channel newChannel;
 		// synchronized ( lookupTable ) {
 		// IHash lowerKey = lookupTable.lowerKey( predecessorId );
 		// if ( lowerKey == null && !lookupTable.isEmpty() ) {
 		// lowerKey = lookupTable.lastKey();
 		// }
 		// if ( lowerKey == null ) {
 		// newId = localNode.getNodeId();
 		// newChannel = null;
 		// } else {
 		// newId = lowerKey;
 		// newChannel = lookupTable.get( lowerKey );
 		// }
 		// }
 		// logger.info( "New predecessor will be " + predecessorId + " at " +
 		// predecessorChannel );
 		// changePredecessor( newId, newChannel );
 		// }
 
 		// check if it was our successor
 		if ( e.getChannel().equals( successorChannel ) ) {
 			logger.info( "Channel " + e.getChannel() + " to our successor " + successorId + " has been closed." );
 			IHash newId;
 			Channel newChannel;
 			synchronized ( lookupTable ) {
 				IHash higherKey = lookupTable.higherKey( successorId );
 				if ( higherKey == null && !lookupTable.isEmpty() ) {
 					higherKey = lookupTable.firstKey();
 				}
 				if ( higherKey == null ) {
 					newId = localNode.getNodeId();
 					newChannel = null;
 				} else {
 					newId = higherKey;
 					newChannel = lookupTable.get( higherKey );
 				}
 			}
 			logger.info( "New successor will be " + successorId + " at " + successorChannel );
 			changeSuccessor( newId, newChannel );
 			ObservableFuture<IHash> future = findSuccessor( localNode.getNodeId().getNext() );
 			future.addFutureListener( new FutureListener<IHash>() {
 
 				@Override
 				public void completed( final ObservableFuture<IHash> future ) {
 					try {
 						HashAndAddress haa = (HashAndAddress) future.get();
 						if ( future.isDone() && !future.isCancelled() ) {
 							changeSuccessor( haa, getOrCreateChannel( haa, haa.getAddress() ) );
 						}
 					} catch ( InterruptedException e ) {
 						logger.warn( "Inerrupted during successor lookup.", e );
 					} catch ( ExecutionException e ) {
 						logger.error( "Successor lookup failed.", e );
 					}
 				}
 			} );
 		}
 	}
 
 	@Override
 	public void messageReceived( final IConnectionManager connectionMgr, final ChannelHandlerContext ctx,
 			final MessageEvent e ) throws Exception {
 
 		// Filter junk
 		Object message = e.getMessage();
 		if ( !(message instanceof AbstractMessage) ) {
 			logger.warn( "Discarding unknown message type: " + message );
 			return;
 		}
 		AbstractMessage msg = (AbstractMessage) message;
 
 		// We don't care if this is a response to our lookup or someone else's
 		if ( msg instanceof SuccessorFoundMessage ) {
 			completeFutures( (SuccessorFoundMessage) msg );
 		}
 
 		// Update our knowledge about the channel
 		if ( msg instanceof NodeIdentificationMessage ) {
 			IHash nodeId = ((NodeIdentificationMessage) msg).getNodeId();
 			synchronized ( lookupTable ) {
 				lookupTable.put( nodeId, e.getChannel() );
 			}
 			// WrappedRange<IHash> successorRange = new WrappedRange<IHash>(
 			// localNode.getNodeId(),
 			// successorId.getPrevious() );
 			// if ( successorRange.contains( nodeId ) ) {
 			// // connected to a node which is a better successor than our
 			// // current
 			// changeSuccessor( nodeId, e.getChannel() );
 			// }
 
 		}
 
 		// check if we have to route the message
 		if ( msg.isRoutable() && !responsibility.contains( msg.getReceiver() ) ) {
 			routeMessage( msg );
 			return;
 		}
 
 		// message is destined for us
 		if ( message instanceof FindSuccessorMessage ) {
 			handleFindSuccessorMessage( (FindSuccessorMessage) message );
 		} else if ( message instanceof ChordEnvelope ) {
 			ChordEnvelope envelope = (ChordEnvelope) message;
 			fireMessageReceived( envelope.getSenderId(), envelope.getContent() );
 		} else if ( message instanceof WelcomeMessage ) {
 			handleWelcomeMessage( e, (WelcomeMessage) message );
 		} else if ( message instanceof IAmYourPredeccessor ) {
 			handleIAmYourPredeccessorMessage( e, (IAmYourPredeccessor) message );
 		} else if ( message instanceof JoinRequestMessage ) {
 			handleJoinRequestMessage( connectionMgr, (JoinRequestMessage) message );
 		}
 
 		super.messageReceived( connectionMgr, ctx, e );
 	}
 
 	private void handleJoinRequestMessage( final IConnectionManager connectionMgr2, final JoinRequestMessage message ) {
 		if ( !message.getNodeId().equals( predecessorId ) ) {
 			welcomeNode( connectionMgr, message.getNodeId(), message.getAddress() );
 		}
 	}
 
 	private void handleIAmYourPredeccessorMessage( final MessageEvent e, final IAmYourPredeccessor msg ) {
 		changePredecessor( msg.getNodeId(), e.getChannel() );
 	}
 
 	private void handleWelcomeMessage( final MessageEvent e, final WelcomeMessage msg ) {
 
 		synchronized ( lookupTable ) {
 			lookupTable.put( msg.getSuccessorId(), e.getChannel() );
 		}
 
 		logger.info( "Joining chord ring. My successor will be " + msg.getSuccessorId() + " at "
 				+ msg.getSuccessorAddress() );
 
 		changeSuccessor( msg.getSuccessorId(), e.getChannel() );
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
 	private void handleFindSuccessorMessage( final FindSuccessorMessage msg ) {
 
 		logger.debug( "I'm the successor: " + responsibility + " contained " + msg.getKey() );
 		SuccessorFoundMessage responseMsg;
 		responseMsg = new SuccessorFoundMessage( msg.getOrigin(), msg.getKey(), localNode.getNodeId(),
 				connectionMgr.getServerAddress() );
 		routeMessage( responseMsg );
 	}
 
 	private void welcomeNode( final IConnectionManager connectionMgr, final IHash nodeId, final SocketAddress address ) {
 
 		// mark the new connection so that no "I'm message" will be sent
 		introductionFilter.add( address );
 
 		logger.info( "Connecting to new node " + address + " to welcome it." );
 		Channel channel = getOrCreateChannel( nodeId, address );
 		sendWelcomeMessage( connectionMgr, nodeId, address, channel );
 	}
 
 	private void sendWelcomeMessage( final IConnectionManager connectionMgr, final IHash nodeId,
 			final SocketAddress address, final Channel channel ) {
 		logger.info( "Connected to " + address + " sending welcome message." );
 		// Send welcome message
 		SocketAddress serverAddress = connectionMgr.getServerAddress();
 		WelcomeMessage welcomeMsg;
 		welcomeMsg = new WelcomeMessage( localNode.getNodeId(), serverAddress, responsibility.getLowerBound() );
 
 		Channels.write( channel, welcomeMsg );
 
 		// Accept our new predecessor
 		changePredecessor( nodeId, channel );
 	}
 
 	private void changePredecessor( final IHash newPredecessor, final Channel newChannel ) {
 		if ( !predecessorId.equals( newPredecessor ) ) {
 			predecessorId = newPredecessor;
 			predecessorChannel = newChannel;
 			if ( predecessorChannel != null && predecessorChannel.isConnected() ) {
 				synchronized ( lookupTable ) {
 					lookupTable.put( predecessorId, predecessorChannel );
 				}
 			}
 			firePredecessorChanged( predecessorId );
 			updateResponsibility( predecessorId.getNext() );
 		}
 	}
 
 	private void changeSuccessor( final IHash newSuccessor, final Channel newChannel ) {
 		if ( !successorId.equals( newSuccessor ) ) {
 
 			// the new successor is only valid if when have no other node in our
 			// routing table that is in the range between
 			// [localNode+1, successor-1]
 			IHash lowerBound = localNode.getNodeId().getNext();
 			WrappedRange<IHash> range = new WrappedRange<IHash>( lowerBound, newSuccessor.getPrevious() );
 			synchronized ( lookupTable ) {
 
 				// IHash smallestMatching = null;
 				for ( IHash key : lookupTable.keySet() ) {
 					if ( range.contains( key ) ) {
 						// smallestMatching = lowerBound.getClosest( key,
 						// smallestMatching );
 						logger.debug( "New successor " + newSuccessor + " is not better than current " + successorId
 								+ "." );
 						return;
 					}
 				}
 
 				//
 				// if ( smallestMatching != null && !smallestMatching.equals(
 				// newSuccessor ) ) {
 				// logger.debug( "Invalid node " + newSuccessor +
 				// " has respond to successor lookup." );
 				// changeSuccessor( smallestMatching, lookupTable.get(
 				// smallestMatching ) );
 				// return;
 				// }
 			}
 
 			successorId = newSuccessor;
 			successorChannel = newChannel;
 			fireSuccessorChanged( successorId );
 			if ( successorChannel != null && successorChannel.isConnected() ) {
 				synchronized ( lookupTable ) {
 					lookupTable.put( successorId, successorChannel );
 				}
 			}
 			// Notify the new successor about us - so that
 			// he can adjust his responsibility
 			if ( successorChannel != null && successorChannel.isConnected() ) {
 				Channels.write( successorChannel, new IAmYourPredeccessor( localNode.getNodeId() ) );
 			}
 		}
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
 
 		Channels.write( e.getChannel(),
 				new NodeIdentificationMessage( localNode.getNodeId(), connectionMgr.getServerAddress() ) );
 
 		// Introduce ourself when we have connected.
 		if ( e.getChannel().getFactory() instanceof ClientSocketChannelFactory ) {
 			// only clients send join requests
 
 			if ( !introductionFilter.contains( e.getChannel().getRemoteAddress() ) ) {
 				logger.info( "We connected to a stranger. Request join." );
 				Channels.write( e.getChannel(),
 						new JoinRequestMessage( localNode.getNodeId(), connectionMgr.getServerAddress() ) );
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
 		// Channels.write( successorChannel, );
 		routeMessage( new FindSuccessorMessage( localNode.getNodeId(), key ) );
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
 			executor.execute( new Runnable() {
 
 				@Override
 				public void run() {
 					l.responsibilityChanged( ChordOverlayChannelHandler.this, from, to );
 				}
 			} );
 		}
 	}
 
 	private void fireSuccessorChanged( final IHash successor ) {
 		for ( final ChordListener l : listener ) {
 			executor.execute( new Runnable() {
 
 				@Override
 				public void run() {
 					l.successorChanged( ChordOverlayChannelHandler.this, successor );
 				}
 			} );
 		}
 	}
 
 	private void firePredecessorChanged( final IHash predecessor ) {
 		for ( final ChordListener l : listener ) {
 			executor.execute( new Runnable() {
 
 				@Override
 				public void run() {
 					l.predecessorChanged( ChordOverlayChannelHandler.this, predecessor );
 				}
 			} );
 		}
 	}
 
 	private void fireMessageReceived( final IHash from, final Object message ) {
 		logger.info( "Received message " + message + " from " + from );
 		for ( final ChordListener l : listener ) {
 			executor.execute( new Runnable() {
 
 				@Override
 				public void run() {
 					logger.debug( "Passing message " + message + " to " + l );
 					l.receivedMessage( ChordOverlayChannelHandler.this, message, from );
 				}
 			} );
 		}
 	}
 
 	@Override
 	public void sendMessage( final Serializable message, final IHash nodeId ) {
 		if ( responsibility.contains( nodeId ) ) {
 
 			logger.debug( "Message " + message + " passed through loopback." );
 			fireMessageReceived( localNode.getNodeId(), message );
 
 			return;
 		}
 		routeMessage( new ChordEnvelope( localNode.getNodeId(), nodeId, message ) );
 	}
 
 	private void routeMessage( final AbstractMessage msg ) {
 		synchronized ( lookupTable ) {
 			if ( lookupTable.isEmpty() ) {
 				logger.warn( "Lookup table is empty. Discarding " + msg );
 				return;
 			}
 
 			// check for perfect match
 			Channel channel2 = lookupTable.get( msg.getReceiver() );
 			if ( channel2 != null ) {
 				logger.info( "Perfect match in lookup table for " + msg.getReceiver() + ". Routing using channel "
 						+ channel2 );
 				Channels.write( channel2, msg );
 				return;
 			}
 
 			Channel channel;
 			IHash destinationKey;
 			IHash higherKey = lookupTable.higherKey( msg.getReceiver() );
 			if ( higherKey == null ) {
 				// There is no higher key, so we need to wrap around to the
 				// first in the ring
 				channel = lookupTable.firstEntry().getValue();
 				destinationKey = lookupTable.firstEntry().getKey();
 				logger.debug( "Router: using first key" );
 			} else {
 				// Take next higher key to route
 				channel = lookupTable.get( higherKey );
 				destinationKey = higherKey;
 				logger.debug( "Router: using next higher key" );
 			}
 
 			logger.debug( "Routing message " + msg + "(destination=" + msg.getReceiver() + ") using channel " + channel
 					+ "(to=" + destinationKey + ")" );
 			Channels.write( channel, msg );
 		}
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
 				stabilize();
 			}
 		}, stabilizationRate );
 		scheduler.scheduleAtFixedRate( new Runnable() {
 
 			@Override
 			public void run() {
 				if ( successorChannel != null && successorChannel.isConnected() ) {
 					Channels.write( successorChannel, new IAmYourPredeccessor( localNode.getNodeId() ) );
 				}
 			}
 		}, 1000 );
 	}
 
 	public void stabilize() {
 		ObservableFuture<IHash> future = findSuccessor( localNode.getNodeId().getNext() );
 		future.addFutureListener( new FutureListener<IHash>() {
 
 			@Override
 			public void completed( final ObservableFuture<IHash> future ) {
 				try {
 					HashAndAddress hash = (HashAndAddress) future.get();
 					if ( hash != null ) {
 						changeSuccessor( hash, getOrCreateChannel( hash, hash.getAddress() ) );
 						logger.info( "Stabilization successful." );
 					}
 				} catch ( InterruptedException e ) {
 					logger.warn( "Stabilization interrupted.", e );
 				} catch ( ExecutionException e ) {
 					logger.error( "Stabilization failed.", e );
 				}
 			}
 
 		} );
 	}
 
 	@Override
 	public IHash getPredecessor() {
 		return predecessorId;
 	}
 
 	@Override
 	public IHash getSuccessor() {
 		return successorId;
 	}
 
 	@Override
 	public IHash getLocalId() {
 		return responsibility.getUpperBound();
 	}
 }

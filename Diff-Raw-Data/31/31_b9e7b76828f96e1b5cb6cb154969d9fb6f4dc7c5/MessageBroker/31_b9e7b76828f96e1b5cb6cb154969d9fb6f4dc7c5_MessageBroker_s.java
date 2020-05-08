 package org.blockout.network.message;
 
 import java.io.Serializable;
 import java.util.Collection;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 
 import org.blockout.network.dht.IHash;
 import org.blockout.network.dht.WrappedRange;
 import org.blockout.network.reworked.ChordListener;
 import org.blockout.network.reworked.FutureListener;
 import org.blockout.network.reworked.IChordOverlay;
 import org.blockout.network.reworked.ObservableFuture;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.task.TaskExecutor;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.HashMultimap;
 import com.google.common.collect.Multimap;
 
 /**
  * Provides message passing facilities to the rest of the System.
  * 
  * It needs a DHT and a ConnectionManager to work correctly. They are injected
  * via Spring at the moment.
  * 
  * @author Marc-Christian Schulze
  * @author Paul Dubs
  * 
  */
 public class MessageBroker implements IMessagePassing, ChordListener {
 	private static final Logger						logger;
 	static {
 		logger = LoggerFactory.getLogger( MessageBroker.class );
 	}
 
 	// Map of registered receivers
 	protected Multimap<Class<?>, IMessageReceiver>	filteredReceivers;
 
 	private final IChordOverlay						chordOverlay;
 	private final TaskExecutor						exec;
 
 	public MessageBroker(final TaskExecutor exec, final IChordOverlay chordOverlay) {
 
 		Preconditions.checkNotNull( exec );
 		Preconditions.checkNotNull( chordOverlay );
 
 		this.exec = exec;
 		this.chordOverlay = chordOverlay;
 
 		chordOverlay.addChordListener( this );
 		filteredReceivers = HashMultimap.create();
 	}
 
 	@Override
 	public void send( final Serializable msg, final IHash keyId ) {
 		logger.debug( "Sending message " + msg + " to node responsible for " + keyId );
 		ObservableFuture<IHash> future = chordOverlay.findSuccessor( keyId );
 		future.addFutureListener( new FutureListener<IHash>() {
 
 			@Override
 			public void completed( final ObservableFuture<IHash> future ) {
 				try {
 					IHash nodeId = future.get();
 					logger.debug( "Routing message " + msg + " to node " + nodeId );
 					chordOverlay.sendMessage( msg, nodeId );
 				} catch ( InterruptedException e ) {
 					logger.error( "Someone interrupted the successor lookup.", e );
 				} catch ( ExecutionException e ) {
 					logger.error( "Successor lookup failed.", e );
 				}
 			}
 		} );
 	}
 
 	private Collection<IMessageReceiver> getReceiver( final Class<?> filterClass ) {
 		Collection<IMessageReceiver> currentList;
 		currentList = filteredReceivers.get( filterClass );
 		return currentList;
 	}
 
 	@Override
 	public void addReceiver( final IMessageReceiver receiver, final Class<?>... filterClasses ) {
 		for ( Class<?> clazz : filterClasses ) {
 			getReceiver( clazz ).add( receiver );
 		}
 	}
 
 	@Override
 	public void addReceiver( final Set<IMessageReceiver> receiver, final Class<?>... filterClasses ) {
 		for ( Class<?> clazz : filterClasses ) {
 			getReceiver( clazz ).addAll( receiver );
 		}
 	}
 
 	@Override
 	public void removeReceiver( final IMessageReceiver receiver, final Class<?>... filterClasses ) {
 		for ( Class<?> clazz : filterClasses ) {
 			filteredReceivers.remove( clazz, receiver );
 		}
 	}
 
 	/**
 	 * Notifies all interested receivers about the given message
 	 */
 	protected void fireMessageReceived( final Object message, final IHash sender ) {
 		Collection<IMessageReceiver> receiverList = getReceiver( message.getClass() );
 		for ( final IMessageReceiver receiver : receiverList ) {
 			exec.execute( new Runnable() {
 
 				@Override
 				public void run() {
 					receiver.receive( message, sender );
 				}
 			} );
 		}
 	}
 
 	@Override
 	public void responsibilityChanged( final IChordOverlay chord, final WrappedRange<IHash> from,
 			final WrappedRange<IHash> to ) {
 		// TODO: Do we need to do something?
 	}
 
 	@Override
 	public void receivedMessage( final IChordOverlay chord, final Object message, final IHash senderId ) {
 		fireMessageReceived( message, senderId );
 	}
 }

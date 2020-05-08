 package org.blockout.world;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import org.blockout.common.TileCoordinate;
 import org.blockout.network.dht.Hash;
 import org.blockout.network.dht.IHash;
 import org.blockout.network.dht.WrappedRange;
 import org.blockout.network.reworked.ChordListener;
 import org.blockout.network.reworked.IChordOverlay;
 import org.blockout.world.entity.Player;
 import org.blockout.world.event.IEvent;
 import org.blockout.world.messeges.ChuckRequestMessage;
 import org.blockout.world.messeges.ChunkDeliveryMessage;
 import org.blockout.world.messeges.ChunkEnteredMessage;
 import org.blockout.world.messeges.EnterGameMessage;
 import org.blockout.world.messeges.EntityAddedMessage;
 import org.blockout.world.messeges.GameEnteredMessage;
 import org.blockout.world.messeges.StateMessage;
 import org.blockout.world.messeges.StopUpdatesMessage;
 import org.blockout.world.state.IStateMachine;
 import org.blockout.world.state.IStateMachineListener;
 import org.blockout.world.state.ValidationResult;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
  * @author Konstantin Ramig
  */
 public class DefaultChunkManager implements IChunkManager, IStateMachineListener, ChordListener {
 
 	private static final Logger									logger;
 	static {
 		logger = LoggerFactory.getLogger( DefaultChunkManager.class );
 	}
 
 	private IStateMachine										stateMachine;
 
 	private final WorldAdapter									worldAdapter;
 
 	private final IChordOverlay									chord;
 
 	private final Hashtable<TileCoordinate, ArrayList<IHash>>	receiver;
 
 	private final Hashtable<TileCoordinate, ArrayList<IHash>>	local;
 
 	public DefaultChunkManager(final WorldAdapter worldAdapter, final IChordOverlay chord) {
 		receiver = new Hashtable<TileCoordinate, ArrayList<IHash>>();
 		local = new Hashtable<TileCoordinate, ArrayList<IHash>>();
 		this.worldAdapter = worldAdapter;
 		this.worldAdapter.setManager( this );
 		this.chord = chord;
 	}
 
 	@Override
 	public void init( final IStateMachine stateMachine ) {
 		this.stateMachine = stateMachine;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void eventCommitted( final IEvent<?> event ) {
 		logger.debug( "Event committed " + event );
 		TileCoordinate coordinate = Chunk.containingCunk( event.getResponsibleTile() );
 		if ( receiver.containsKey( coordinate ) ) {
 			for ( IHash address : receiver.get( coordinate ) ) {
 				chord.sendMessage( new StateMessage( event, StateMessage.COMMIT_MESSAGE ), address );
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void eventPushed( final IEvent<?> event ) {
 		logger.debug( "Event pushed " + event );
 		TileCoordinate coordinate = Chunk.containingCunk( event.getResponsibleTile() );
 		if ( !receiver.containsKey( coordinate ) ) {
 			chord.sendMessage( new StateMessage( event, StateMessage.PUSH_MESSAGE ), new Hash( coordinate ) );
 			if ( local.containsKey( coordinate ) ) {
 				for ( IHash address : local.get( coordinate ) ) {
 					chord.sendMessage( new StateMessage( event, StateMessage.PUSH_MESSAGE ), address );
 				}
 			}
 
 		} else {
 			stateMachine.commitEvent( event );
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void eventRolledBack( final IEvent<?> event ) {
 		TileCoordinate coordinate = Chunk.containingCunk( event.getResponsibleTile() );
 		if ( receiver.containsKey( coordinate ) ) {
 			for ( IHash address : receiver.get( coordinate ) ) {
 				chord.sendMessage( new StateMessage( event, StateMessage.ROLLBAK_MESSAGE ), address );
 			}
 		}
 		if ( local.contains( coordinate ) ) {
 			for ( IHash address : local.get( coordinate ) ) {
 				chord.sendMessage( new StateMessage( event, StateMessage.ROLLBAK_MESSAGE ), address );
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void requestChunk( final TileCoordinate position ) {
 		if ( receiver.containsKey( position ) ) {
 			local.put( position, receiver.get( position ) );
 			worldAdapter.responseChunk( worldAdapter.getChunk( position ) );
 
 			for ( IHash address : local.get( position ) ) {
 				chord.sendMessage( new ChunkEnteredMessage( position ), address );
 			}
 		} else {
 			local.put( position, new ArrayList<IHash>() );
 			chord.sendMessage( new ChuckRequestMessage( position ), new Hash( position ) );
 		}
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void stopUpdating( final TileCoordinate position ) {
 		chord.sendMessage( new StopUpdatesMessage( position ), new Hash( position ) );
 		if ( local.containsKey( position ) ) {
 			for ( IHash address : local.get( position ) ) {
 				chord.sendMessage( new StopUpdatesMessage( position ), address );
 			}
 		}
 		local.remove( position );
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void enterGame( final Player player ) {
 		chord.sendMessage( new EnterGameMessage( player ), new Hash( new TileCoordinate( 0, 0 ) ) );
 	}
 
 	private void receive( final StateMessage msg, final IHash origin ) {
 		logger.debug( "Received message " + msg + " from " + origin );
 		switch ( msg.getType() ) {
 			case StateMessage.ROLLBAK_MESSAGE:
 				stateMachine.rollbackEvent( msg.getEvent() );
 				break;
 			case StateMessage.COMMIT_MESSAGE:
 				stateMachine.commitEvent( msg.getEvent() );
 				break;
 			case StateMessage.PUSH_MESSAGE:
 				ValidationResult result = stateMachine.pushEvent( msg.getEvent() );
 				if ( result == ValidationResult.Invalid ) {
 					eventRolledBack( msg.getEvent() );
 				}
 				if ( receiver.containsKey( Chunk.containingCunk( msg.getEvent().getResponsibleTile() ) ) ) {
 					if ( result == ValidationResult.Valid ) {
 						stateMachine.commitEvent( msg.getEvent() );
 					}
 				}
 				break;
 			default:
 				break;
 		}
 	}
 
 	private void receive( final ChuckRequestMessage msg, final IHash origin ) {
 		Chunk c = worldAdapter.getChunk( msg.getCoordinate() );
 		if ( !receiver.containsKey( c.getPosition() ) ) {
 			receiver.put( c.getPosition(), new ArrayList<IHash>() );
 		}
 		chord.sendMessage(
 				new ChunkDeliveryMessage( c, (ArrayList<IHash>) receiver.get( msg.getCoordinate() ).clone() ), origin );
 		receiver.get( c.getPosition() ).add( origin );
 	}
 
 	private void receive( final ChunkDeliveryMessage msg, final IHash origin ) {
 		Chunk c = msg.getChunk();
 		if ( receiver.containsKey( c.getPosition() ) ) {
 			receiver.get( c.getPosition() ).remove( origin );
 		} else if ( local.containsKey( c.getPosition() ) ) {
 			receiver.put( c.getPosition(), msg.getLocalPlayers() );
 
 			worldAdapter.responseChunk( c );
 
 			for ( IHash address : local.get( c.getPosition() ) ) {
 				chord.sendMessage( new ChunkEnteredMessage( c.getPosition() ), address );
 			}
 		}
 
 	}
 
 	private void receive( final ChunkEnteredMessage msg, final IHash origin ) {
 		if ( local.containsKey( msg.getCoordinate() ) ) {
 			local.get( msg.getCoordinate() ).add( origin );
 		}
 	}
 
 	private void receive( final StopUpdatesMessage msg, final IHash origin ) {
 		if ( receiver.containsKey( msg.getCoordinate() ) ) {
 			receiver.get( msg.getCoordinate() ).remove( origin );
 		}
 		if ( local.contains( msg.getCoordinate() ) ) {
 			local.get( msg.getCoordinate() ).remove( origin );
 		}
 
 		// TODO save local connections?
 	}
 
 	/*
 	 * public void receive( final UnmanageMessage msg, final IHash origin ) {
 	 * IComparator comparator = msg.getComparator(); ManageMessage manageMessage
 	 * = new ManageMessage(); for ( TileCoordinate coordinate :
 	 * receiver.keySet() ) { if ( !comparator.compare( coordinate ) ) {
 	 * manageMessage.add( worldAdapter.unmanageChunk( coordinate ),
 	 * receiver.remove( coordinate ) ); } } chord.sendMessage( manageMessage,
 	 * origin ); }
 	 * 
 	 * public void receive( final ManageMessage msg, final IHash origin ) {
 	 * 
 	 * ArrayList<Chunk> chunks = msg.getChunks(); ArrayList<ArrayList<IHash>>
 	 * addresses = msg.getReceivers();
 	 * 
 	 * for ( int i = 0; i < chunks.size(); i++ ) { receiver.put( chunks.get( i
 	 * ).getPosition(), addresses.get( i ) ); worldAdapter.manageChunk(
 	 * chunks.get( i ) ); }
 	 * 
 	 * }
 	 * 
 	 * public void receive( final FallbackMessage msg, final IHash origin ) {
 	 * 
 	 * // TODO save local connections?
 	 * 
 	 * if ( !receiver.containsKey( msg.getChunk().getPosition() ) ) {
 	 * worldAdapter.manageChunk( msg.getChunk() ); } }
 	 */
 
 	public void receive( final EnterGameMessage msg, final IHash origin ) {
 		Chunk c = worldAdapter.getChunk( new TileCoordinate( 0, 0 ) );
 
 		// TODO better player placement
 		TileCoordinate coord = c.findFreeTile();
 		c.setEntityCoordinate( msg.getPlayer(), coord.getX(), coord.getY() );
 
 		if ( !receiver.containsKey( c.getPosition() ) ) {
 			receiver.put( c.getPosition(), new ArrayList<IHash>() );
 		}
 		chord.sendMessage( new GameEnteredMessage( c, (ArrayList<IHash>) receiver.get( c.getPosition() ).clone() ),
 				origin );
 		receiver.get( c.getPosition() ).add( origin );
 	}
 
 	public void receive( final GameEnteredMessage msg, final IHash origin ) {
 		if ( receiver.containsKey( msg.getChunk().getPosition() ) ) {
 			receiver.get( msg.getChunk().getPosition() ).remove( origin );
 		}
 		worldAdapter.gameEntered( msg.getChunk() );
 		local.put( msg.getChunk().getPosition(), msg.getLocalPlayer() );
 	}
 
 	public void receive( final EntityAddedMessage msg, final IHash origin ) {
 		TileCoordinate coordinate = Chunk.containingCunk( msg.getCoordinate() );
 		if ( local.containsKey( coordinate ) ) {
 			worldAdapter.getChunk( coordinate ).setEntityCoordinate( msg.getEntity(), msg.getCoordinate().getX(),
 					msg.getCoordinate().getY() );
 			local.get( coordinate ).add( msg.getOwner() );
 		}
 	}
 
 	@Override
 	public void responsibilityChanged( final IChordOverlay chord, final WrappedRange<IHash> from,
 			final WrappedRange<IHash> to ) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void receivedMessage( final IChordOverlay chord, final Object message, final IHash senderId ) {
 		if ( message instanceof StateMessage ) {
 			receive( (StateMessage) message, senderId );
 		} else if ( message instanceof ChuckRequestMessage ) {
 			receive( (ChuckRequestMessage) message, senderId );
 		} else if ( message instanceof ChunkDeliveryMessage ) {
 			receive( (ChunkDeliveryMessage) message, senderId );
 		} else if ( message instanceof ChunkEnteredMessage ) {
 			receive( (ChunkEnteredMessage) message, senderId );
 		} else if ( message instanceof StopUpdatesMessage ) {
 			receive( (StopUpdatesMessage) message, senderId );
 		} else if ( message instanceof GameEnteredMessage ) {
 			receive( (GameEnteredMessage) message, senderId );
 		} else if ( message instanceof EnterGameMessage ) {
 			receive( (EnterGameMessage) message, senderId );
 		} else if ( message instanceof EntityAddedMessage ) {
 			receive( (EntityAddedMessage) message, senderId );
 		}
 
 	}
 
 }

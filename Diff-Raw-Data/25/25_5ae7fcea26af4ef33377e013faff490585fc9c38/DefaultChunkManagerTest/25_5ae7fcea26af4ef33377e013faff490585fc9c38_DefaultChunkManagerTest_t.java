 package org.blockout.world;
 
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.never;
 import static org.mockito.Mockito.stub;
 import static org.mockito.Mockito.verify;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 
 import org.blockout.common.TileCoordinate;
 import org.blockout.network.dht.Hash;
 import org.blockout.network.dht.IHash;
import org.blockout.network.dht.WrappedRange;
 import org.blockout.network.message.IMessage;
 import org.blockout.network.reworked.IChordOverlay;
 import org.blockout.world.event.IEvent;
 import org.blockout.world.messeges.ChuckRequestMessage;
 import org.blockout.world.messeges.ChunkDeliveryMessage;
 import org.blockout.world.messeges.StateMessage;
 import org.blockout.world.messeges.StopUpdatesMessage;
 import org.blockout.world.state.IStateMachine;
 import org.junit.Before;
 import org.junit.Test;
 
 public class DefaultChunkManagerTest {
 
 	DefaultChunkManager	manager;
 	WorldAdapter		worldAdapter;
 	IStateMachine		iStateMachine;
 	IChordOverlay		chord;
 	IHash				a1;
 	IHash				a2;
 	TileCoordinate		coord1;
 	TileCoordinate		coord2;
 	TileCoordinate		coord3;
 	TileCoordinate		coord4;
 	Chunk				chunk1;
 	Chunk				chunk2;
 	Chunk				chunk3;
 	IEvent<?>			e1;
 	IEvent<?>			e2;
 	IEvent<?>			e3;
 	IEvent<?>			e4;
 
 	@Before
 	public void init() {
 		worldAdapter = mock( WorldAdapter.class );
 		iStateMachine = mock( IStateMachine.class );
 		chord = mock( IChordOverlay.class );
 		manager = new DefaultChunkManager( worldAdapter, chord );
 		manager.init( iStateMachine );
 
 		coord1 = new TileCoordinate( 1, 1 );
 		coord2 = new TileCoordinate( 2, 3 );
 		coord3 = new TileCoordinate( -4, 4 );
 		coord4 = new TileCoordinate( -4, -4 );
 		a1 = mock( IHash.class );
 		a2 = mock( IHash.class );
 		IChunkGenerator generator = new BasicChunkGenerator();
 		chunk1 = generator.generateChunk( coord1 );
 		chunk2 = generator.generateChunk( coord2 );
 		chunk3 = generator.generateChunk( coord3 );
 		stub( worldAdapter.getChunk( coord1 ) ).toReturn( chunk1 );
 		stub( worldAdapter.getChunk( coord2 ) ).toReturn( chunk2 );
 		stub( worldAdapter.getChunk( coord3 ) ).toReturn( chunk3 );
 
 		e1 = mock( IEvent.class );
 		stub( e1.getResponsibleTile() ).toReturn(
 				new TileCoordinate( coord1.getX() * Chunk.CHUNK_SIZE + 1, coord1.getY() * Chunk.CHUNK_SIZE + 4 ) );
 		e2 = mock( IEvent.class );
 		stub( e2.getResponsibleTile() ).toReturn(
 				new TileCoordinate( coord2.getX() * Chunk.CHUNK_SIZE + 2, coord2.getY() * Chunk.CHUNK_SIZE + 5 ) );
 		e3 = mock( IEvent.class );
 		stub( e3.getResponsibleTile() ).toReturn(
 				new TileCoordinate( coord3.getX() * Chunk.CHUNK_SIZE + 3, coord3.getY() * Chunk.CHUNK_SIZE + 6 ) );
 		e4 = mock( IEvent.class );
 		stub( e4.getResponsibleTile() ).toReturn(
 				new TileCoordinate( -4 * Chunk.CHUNK_SIZE + 3, -4 * Chunk.CHUNK_SIZE + 3 ) );
		
		IHash top = new Hash(coord4);
		WrappedRange<IHash> range = new WrappedRange<IHash>(top.getNext(), top.getPrevious());
		
		stub(chord.getResponsibility()).toReturn(range);
 	}
 
 	private void addreceivers() throws IllegalArgumentException, SecurityException, IllegalAccessException,
 			InvocationTargetException, NoSuchMethodException {
 		IMessage msg = new ChuckRequestMessage( coord1 );
 		manager.receivedMessage( chord, msg, a1 );
 		msg = new ChuckRequestMessage( coord2 );
 		manager.receivedMessage( chord, msg, a1 );
 		msg = new ChuckRequestMessage( coord2 );
 		manager.receivedMessage( chord, msg, a2 );
 		msg = new ChuckRequestMessage( coord3 );
 		manager.receivedMessage( chord, msg, a2 );
 	}
 
 	@Test
 	public void receiveState() throws IllegalArgumentException, SecurityException, IllegalAccessException,
 			InvocationTargetException, NoSuchMethodException {
 		IEvent<?> event = mock( IEvent.class );
 		stub( event.getResponsibleTile() ).toReturn( coord1 );
 		IHash origin = mock( IHash.class );
 		// commit
 		IMessage msg = new StateMessage( event, StateMessage.Type.COMMIT_MESSAGE );
 		manager.receivedMessage( chord, msg, origin );
 		verify( iStateMachine ).commitEvent( event );
 		// push
 		msg = new StateMessage( event, StateMessage.Type.PUSH_MESSAGE );
 		manager.receivedMessage( chord, msg, origin );
 		verify( iStateMachine ).pushEvent( event );
 		// rollback
 		msg = new StateMessage( event, StateMessage.Type.ROLLBAK_MESSAGE );
 		manager.receivedMessage( chord, msg, origin );
 		verify( iStateMachine ).rollbackEvent( event );
 	}
 
 	@Test
 	public void chunkRequest() throws IllegalArgumentException, SecurityException, IllegalAccessException,
 			InvocationTargetException, NoSuchMethodException {
 		IMessage msg = new ChuckRequestMessage( coord1 );
 		manager.receivedMessage( chord, msg, a1 );
 		verify( worldAdapter ).getChunk( coord1 );
 		ArrayList<IHash> addresses = new ArrayList<IHash>();
 		verify( chord ).sendMessage( new ChunkDeliveryMessage( chunk1, addresses ), a1 );
 		// coord2
 		msg = new ChuckRequestMessage( coord2 );
 		manager.receivedMessage( chord, msg, a1 );
 		verify( worldAdapter ).getChunk( coord2 );
 		addresses = new ArrayList<IHash>();
 		verify( chord ).sendMessage( new ChunkDeliveryMessage( chunk2, addresses ), a1 );
 		addresses.add( a1 );
 		msg = new ChuckRequestMessage( coord2 );
 		manager.receivedMessage( chord, msg, a2 );
 		verify( chord ).sendMessage( new ChunkDeliveryMessage( chunk2, addresses ), a2 );
 		// coord3
 		msg = new ChuckRequestMessage( coord3 );
 		manager.receivedMessage( chord, msg, a2 );
 		verify( worldAdapter ).getChunk( coord3 );
 		verify( chord ).sendMessage( new ChunkDeliveryMessage( chunk3, new ArrayList<IHash>() ), a2 );
 	}
 
 	@Test
 	public void pushedEvents() throws IllegalArgumentException, SecurityException, IllegalAccessException,
 			InvocationTargetException, NoSuchMethodException {
 		// adding requests to manager so that actually there is done
 		// something....
 		addreceivers();
 
 		manager.eventPushed( e1 );
 		verify( iStateMachine ).commitEvent( e1 );
 		manager.eventPushed( e2 );
 		verify( iStateMachine ).commitEvent( e2 );
 		manager.eventPushed( e3 );
 		verify( iStateMachine ).commitEvent( e3 );
 		manager.eventPushed( e4 );
 		verify( iStateMachine, never() ).commitEvent( e4 );
 		verify( chord ).sendMessage( new StateMessage( e4, StateMessage.Type.PUSH_MESSAGE ), new Hash( coord4 ) );
 	}
 
 	@Test
 	public void commitedEvents() throws IllegalArgumentException, SecurityException, IllegalAccessException,
 			InvocationTargetException, NoSuchMethodException {
 		addreceivers();
 
 		// commit
 		manager.eventCommitted( e1 );
 		verify( chord ).sendMessage( new StateMessage( e1, StateMessage.Type.COMMIT_MESSAGE ), a1 );
 		verify( chord, never() ).sendMessage( new StateMessage( e1, StateMessage.Type.COMMIT_MESSAGE ), a2 );
 		manager.eventCommitted( e2 );
 		verify( chord ).sendMessage( new StateMessage( e2, StateMessage.Type.COMMIT_MESSAGE ), a1 );
 		verify( chord ).sendMessage( new StateMessage( e2, StateMessage.Type.COMMIT_MESSAGE ), a2 );
 		manager.eventCommitted( e3 );
 		verify( chord, never() ).sendMessage( new StateMessage( e3, StateMessage.Type.COMMIT_MESSAGE ), a1 );
 		verify( chord ).sendMessage( new StateMessage( e3, StateMessage.Type.COMMIT_MESSAGE ), a2 );
 	}
 
 	@Test
 	public void rolledbackEvents() throws IllegalArgumentException, SecurityException, IllegalAccessException,
 			InvocationTargetException, NoSuchMethodException {
 		// adding requests to manager so that actually there is done
 		// something....
 		addreceivers();
 
 		manager.eventRolledBack( e1 );
 		verify( chord ).sendMessage( new StateMessage( e1, StateMessage.Type.ROLLBAK_MESSAGE ), a1 );
 		verify( chord, never() ).sendMessage( new StateMessage( e1, StateMessage.Type.ROLLBAK_MESSAGE ), a2 );
 		manager.eventRolledBack( e2 );
 		verify( chord ).sendMessage( new StateMessage( e2, StateMessage.Type.ROLLBAK_MESSAGE ), a1 );
 		verify( chord ).sendMessage( new StateMessage( e2, StateMessage.Type.ROLLBAK_MESSAGE ), a2 );
 		manager.eventRolledBack( e3 );
 		verify( chord, never() ).sendMessage( new StateMessage( e3, StateMessage.Type.ROLLBAK_MESSAGE ), a1 );
 		verify( chord ).sendMessage( new StateMessage( e3, StateMessage.Type.ROLLBAK_MESSAGE ), a2 );
 	}
 
 	@Test
 	public void stopChunkUpdates() throws IllegalArgumentException, SecurityException, IllegalAccessException,
 			InvocationTargetException, NoSuchMethodException {
 		// adding requests to manager so that actually there is done
 		// something....
 		addreceivers();
 
 		// stopUpdate
 		IMessage msg = new StopUpdatesMessage( coord2 );
 		manager.receivedMessage( chord, msg, a1 );
 		manager.eventCommitted( e2 );
 		verify( chord, never() ).sendMessage( new StateMessage( e2, StateMessage.Type.COMMIT_MESSAGE ), a1 );
 		verify( chord ).sendMessage( new StateMessage( e2, StateMessage.Type.COMMIT_MESSAGE ), a2 );
 	}
 }

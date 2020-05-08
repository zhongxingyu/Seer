 package clueTests;
 
 import static org.junit.Assert.*;
 
 import java.util.Map;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import clueGame.Board;
 import clueGame.BoardCell;
 import clueGame.RoomCell;
 
 public class BoardInitTests {
 	private static Board board;
 
 	@BeforeClass
 	public static void setup() {
 		board = new Board();
 	}
 
 	@Test
 	public void testRooms() {
 		Map<Character, String> rooms = board.getRooms();
 		assertEquals(11, rooms.size());
 		assertEquals("Torture chamber", rooms.get('T'));
 		assertEquals("Foundry", rooms.get('F'));
 		assertEquals("Dungeon", rooms.get('D'));
 		assertEquals("Tower", rooms.get('O'));
 		assertEquals("Walkway", rooms.get('W'));
 	}
 
 	@Test
 	public void testBoardSize() {
 		assertEquals(26, board.getNumColumns());
 		assertEquals(26, board.getNumRows());
 	}
 
 	@Test
 	public void testDoorDirections() {
 		RoomCell room = board.getRoomCellAt(4, 0);
 		assertTrue(room.isDoorway());
 		assertEquals(RoomCell.DoorDirection.DOWN, room.getDoorDirection());
 
 		room = board.getRoomCellAt(7, 13);
 		assertTrue(room.isDoorway());
 		assertEquals(RoomCell.DoorDirection.RIGHT, room.getDoorDirection());
 
 		room = board.getRoomCellAt(10, 16);
 		assertTrue(room.isDoorway());
 		assertEquals(RoomCell.DoorDirection.UP, room.getDoorDirection());
 
 		room = board.getRoomCellAt(12, 21);
 		assertTrue(room.isDoorway());
 		assertEquals(RoomCell.DoorDirection.LEFT, room.getDoorDirection());
 	}
 
 	@Test
 	public void testDoorStatus() {
 		// check a room cell is not a doorway
 		BoardCell cell = board.getRoomCellAt(0, 0);
 		assertFalse(cell.isDoorway());
 
 		// check a walkway is not a doorway
 		cell = board.getCellAt(board.calcIndex(0, 5));
 		assertFalse(cell.isDoorway());
 	}
 
 	// Test that we have the correct number of doors
 	@Test
 	public void testNumberOfDoorways() {
 		int numDoors = 0;
 		int totalCells = board.getNumColumns() * board.getNumRows();
 		assertEquals(676, totalCells);
 		for (int i = 0; i < totalCells; i++) {
 			BoardCell cell = board.getCellAt(i);
 			if (cell.isDoorway())
 				numDoors++;
 		}
 		assertEquals(26, numDoors);
 	}
 
 	@Test
 	public void testRoomInitials() {
 		assertEquals('T', board.getRoomCellAt(0, 0).getInitial());
 		assertEquals('F', board.getRoomCellAt(0, 8).getInitial());
 		assertEquals('B', board.getRoomCellAt(12, 12).getInitial());
 		assertEquals('C', board.getRoomCellAt(0, 24).getInitial());
 		assertEquals('D', board.getRoomCellAt(25, 10).getInitial());
 	}
 
 	@Test
 	public void testCalcIndex() {
 		assertEquals(3, board.calcIndex(0, 3));
 		assertEquals(0, board.calcIndex(0, 0));
 		assertEquals(board.getNumColumns() * board.getNumRows() - 1,
 				board.calcIndex(board.getNumRows() - 1,
 						board.getNumColumns() - 1));
 		assertEquals(board.getNumColumns(), board.calcIndex(1, 0));
 		// The above should be true for any reasonable-size board
 		// the below apply just to our board.
 		assertEquals(207, board.calcIndex(7, 25));
		assertEquals(143, board.calcIndex(11, 11));
 		assertEquals(441, board.calcIndex(16, 25));
 	}
 }

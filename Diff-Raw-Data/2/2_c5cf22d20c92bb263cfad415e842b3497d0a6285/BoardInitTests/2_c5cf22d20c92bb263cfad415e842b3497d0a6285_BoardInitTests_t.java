 package tests;
 
 import static junit.framework.Assert.assertEquals;
 import static org.junit.Assert.*;
 
 import java.io.FileNotFoundException;
 import java.util.Map;
 
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import clueGame.BadConfigFormatException;
 import clueGame.Board;
 import clueGame.BoardCell;
 import clueGame.RoomCell;
 
 public class BoardInitTests {
 	
 	private static Board board;
 	public static final int NUM_ROOMS = 11;
 	public static final int NUM_ROWS = 22;
 	public static final int NUM_COLUMNS = 23;
 	
 	@BeforeClass
 	public static void setup() throws Exception {
 		board = new Board();
 		board.loadConfigFiles("ClueBoardLegend.txt", "ClueBoardLayout.csv", "weapons.txt", "players.txt");
 	}
 	
 	@Test
 	public void testRooms() {
 		Map<Character, String> rooms = board.getRooms();
 		//assure that board contains correct number of rooms
 		assertEquals(NUM_ROOMS, rooms.size());
 		//assure that the name corresponds to correct character
 		assertEquals("Conservatory", rooms.get('C'));
 		assertEquals("Kitchen", rooms.get('K'));
 		assertEquals("Ballroom", rooms.get('B'));
		assertEquals("Billiard Room", rooms.get('R'));
 		assertEquals("Library", rooms.get('L'));
 		assertEquals("Study", rooms.get('S'));
 		assertEquals("Dining room", rooms.get('D'));
 		assertEquals("Lounge", rooms.get('O'));
 		assertEquals("Hall", rooms.get('H'));
 		assertEquals("Closet", rooms.get('X'));
 		assertEquals("Walkway", rooms.get('W'));
 	}
 	
 	@Test
 	public void testBoardDimensions() {
 		assertEquals(NUM_ROWS, board.getNumRows());
 		assertEquals(NUM_COLUMNS, board.getNumColumns());
 	}
 	
 	@Test
 	public void testDoorDirections() {
 		//testing Billiard Room door
 		RoomCell room = board.getRoomCellAt(4, 8);
 		assertTrue(room.isDoorway());
 		assertEquals(RoomCell.DoorDirection.DOWN, room.getDoorDirection());
 		//testing Conservatory door
 		room = board.getRoomCellAt(4, 3);
 		assertTrue(room.isDoorway());
 		assertEquals(RoomCell.DoorDirection.RIGHT, room.getDoorDirection());
 		//testing Library left door
 		room = board.getRoomCellAt(2, 13);
 		assertTrue(room.isDoorway());
 		assertEquals(RoomCell.DoorDirection.LEFT, room.getDoorDirection());
 		//testing Hallway up door
 		room = board.getRoomCellAt(7, 20);
 		assertTrue(room.isDoorway());
 		assertEquals(RoomCell.DoorDirection.UP, room.getDoorDirection());
 		//test walkway (not door)
 		BoardCell cell = board.getCellAt(board.calcIndex(15, 7));
 		assertFalse(cell.isDoorway());
 		//test room cell (not door)
 		room = board.getRoomCellAt(0, 0);
 		assertFalse(room.isDoorway());
 		assertEquals(RoomCell.DoorDirection.NONE, room.getDoorDirection());
 		
 	}
 	
 	@Test
 	public void testCalcIndex() {
 		// test corners of the board
 		assertEquals(0, board.calcIndex(0, 0));
 		assertEquals(NUM_COLUMNS-1, board.calcIndex(0, NUM_COLUMNS-1));
 		assertEquals(483, board.calcIndex(NUM_ROWS-1, 0));
 		assertEquals(505, board.calcIndex(NUM_ROWS-1, NUM_COLUMNS-1));
 		// test other locations
 		assertEquals(24, board.calcIndex(1, 1));
 		assertEquals(66, board.calcIndex(2, 20));		
 	}
 	
 	
 	@Test
 	public void testRoomInitials() {
 		assertEquals('C', board.getRoomCellAt(0, 0).getInitial());
 		assertEquals('R', board.getRoomCellAt(4, 8).getInitial());
 		assertEquals('B', board.getRoomCellAt(9, 0).getInitial());
 		assertEquals('O', board.getRoomCellAt(21, 22).getInitial());
 		assertEquals('K', board.getRoomCellAt(21, 0).getInitial());
 	}
 	
 	@Test
 	public void testNumberOfDoorways() {
 		int numDoors = 0;
 		int totalCells = board.getNumColumns() * board.getNumRows();
 		//check board size
 		Assert.assertEquals(506, totalCells);
 		
 		//calculate number of doors
 		for (int i = 0; i < NUM_ROWS; i++) {
 			for (int j = 0; j < NUM_COLUMNS; j++) {
 				BoardCell cell = board.getCellAt(board.calcIndex(i, j));
 				if (cell.isDoorway())
 					numDoors++;
 			}
 		}
 		Assert.assertEquals(16, numDoors);
 	}
 	
 	@Test
 	public void testNumberOfRooms() {
 		assertEquals(11, board.getRooms().size());
 	}
 	
 }

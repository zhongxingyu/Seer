 import static org.junit.Assert.*;
 
 import java.io.FileNotFoundException;
 import java.util.*;
 
 import org.junit.*;
 
 
 
 public class BoardTests {
 	//Setup Board
 	private final int NUM_ROOMS = 11;
 	private final int NUM_ROWS =25,  NUM_COLUMNS = 25;
 	private Board board;
 	
 	@Before
 	public void setUpBoard(){
 			board = new Board();
 	}
 
 
 	//TESTS FOR ROOMS AND BOARD 
 	@Test
 	public void testCorrectNumOfRooms(){
 		//Testing size of Map rooms
 		Map<Character, String> testMapRooms = board.getRooms();
 		assertEquals(NUM_ROOMS, testMapRooms.size());
 		
 		//test that all the characters map to their respective room (C to 'Conservatory')
 		assertEquals("Walkway", testMapRooms.get('W'));
 		assertEquals("Conservatory", testMapRooms.get('C'));
 		assertEquals("Billiard room", testMapRooms.get('R'));
 		assertEquals("Library", testMapRooms.get('L'));
 		assertEquals("Dining room", testMapRooms.get('D'));
 
 	}
 	
 	@Test
 	public void testBoardDimensions() {
 		// Ensure we have the proper number of rows and columns
 		assertEquals(NUM_ROWS, board.getNumRows());
 		assertEquals(NUM_COLUMNS, board.getNumColumns());		
 	}
 	
 	
 	
 	// Test a doorway in each direction, plus two cells that are not
 	// a doorway.
 	// These cells are white on the planning spreadsheet
 	@Test
 	public void FourDoorDirections() {
 		// Test one each RIGHT/LEFT/UP/DOWN
		RoomCell room = board.getRoomCellAt(3, 3);
 		assertTrue(room.isDoorway());
 		assertEquals(RoomCell.DoorDirection.RIGHT, room.getDoorDirection());
		room = board.getRoomCellAt(2, 10);
 		assertTrue(room.isDoorway());
		assertEquals(RoomCell.DoorDirection.RIGHT, room.getDoorDirection());
		room = board.getRoomCellAt(9, 18);
 		assertTrue(room.isDoorway());
		assertEquals(RoomCell.DoorDirection.DOWN, room.getDoorDirection());
		room = board.getRoomCellAt(19, 12);
 		assertTrue(room.isDoorway());
 		assertEquals(RoomCell.DoorDirection.UP, room.getDoorDirection());
 		// Test that room pieces that aren't doors know it
 		room = board.getRoomCellAt(14, 14);
 		assertFalse(room.isDoorway());	
 		// Test that walkways are not doors
		BoardCell cell = board.getCells(board.calcIndex(12, 16));
 		assertFalse(cell.isDoorway());		
 
 	}
 	
 	// Test that we have the correct number of doors
 	@Test
 	public void testNumberOfDoorways() 
 	{
 		int numDoors = 0;
 		int totalCells = board.getNumColumns() * board.getNumRows();
 		Assert.assertEquals(506, totalCells);
 		for (int i=0; i<totalCells; i++)
 		{
 			BoardCell cell = board.getCells(i);
 			if (cell.isDoorway())
 				numDoors++;
 		}
 		Assert.assertEquals(16, numDoors);
 	}
 
 	
 	
 	
 	@Test
 	public void testCalcIndex() {
 		// Test each corner of the board
 		assertEquals(0, board.calcIndex(0, 0));
 		assertEquals(NUM_COLUMNS-1, board.calcIndex(0, NUM_COLUMNS-1));
 		assertEquals(483, board.calcIndex(NUM_ROWS-1, 0));
 		assertEquals(505, board.calcIndex(NUM_ROWS-1, NUM_COLUMNS-1));
 		// Test a couple others
 		assertEquals(24, board.calcIndex(1, 1));
 		assertEquals(66, board.calcIndex(2, 20));		
 	}
 	
 	
 	
 	// Test a few room cells to ensure the room initial is
 	// correct.
 	@Test
 	public void testRoomInitials() {
 		assertEquals('C', board.getRoomCellAt(0, 0).getRoomInitial());
 		assertEquals('R', board.getRoomCellAt(4, 8).getRoomInitial());
 		assertEquals('B', board.getRoomCellAt(9, 0).getRoomInitial());
 		assertEquals('O', board.getRoomCellAt(21, 22).getRoomInitial());
 		assertEquals('K', board.getRoomCellAt(21, 0).getRoomInitial());
 	}
 	
 	
 	
 	// Test that an exception is thrown for a bad config file
 	@Test (expected = BadConfigFormatException.class)
 	public void testBadColumns() throws BadConfigFormatException, FileNotFoundException {
 		// overloaded Board ctor takes config file names
 		Board b = new Board("ClueLayoutBadColumns.csv", "ClueLegend.txt");
 		// You may change these calls if needed to match your function names
 		// My loadConfigFiles has a try/catch, so I can't call it directly to
 		// see test throwing the BadConfigFormatException
 		b.loadConfigFiles();
 	}
 	
 	
 	// Test that an exception is thrown for a bad config file
 	@Test (expected = BadConfigFormatException.class)
 	public void testBadRoom() throws BadConfigFormatException, FileNotFoundException {
 		// overloaded Board ctor takes config file name
 		Board b = new Board("ClueLayoutBadRoom.csv", "ClueLegend.txt");
 		b.loadConfigFiles();
 	}
 	
 	
 	// Test that an exception is thrown for a bad config file
 	@Test (expected = BadConfigFormatException.class)
 	public void testBadRoomFormat() throws BadConfigFormatException, FileNotFoundException {
 		// overloaded Board ctor takes config file name
 		Board b = new Board("ClueLayout.csv", "ClueLegendBadFormat.txt");
 		b.loadConfigFiles();
 	}
 
 }

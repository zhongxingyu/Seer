 package clueGame;
 
 import static org.junit.Assert.*;
 
 import java.io.FileNotFoundException;
 import java.util.*;
 
 import org.junit.*;
 
 
 
 public class BoardTests {
 	//Setup Board
 	private final int NUM_ROOMS = 11;
 	private final int NUM_ROWS =25,  NUM_COLUMNS = 25;
 	private Board board;
 	private Map<Character, String> testMapRooms;
 
 	@Before
 	public void setUpBoard() throws BadConfigFormatException, FileNotFoundException {
 		board = new Board("ConfigLayout.csv", "ConfigRooms.txt");	
		//board.calcAdjacencies();
 	}
 
 	//TESTS FOR ROOMS AND BOARD 
 	@Test
 	public void testCorrectNumOfRooms(){
 		//Testing size of Map rooms
 		testMapRooms = board.getRooms();
 		assertEquals(NUM_ROOMS, testMapRooms.size());
 	}
 
 	@Test
 	public void testRoomCharacters(){
 		testMapRooms = board.getRooms();
 		//test that all the room characters map to their respective room (C to 'Conservatory')
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
 		Assert.assertEquals(625, totalCells);
 		for (int i=0; i < totalCells; i++)
 		{
 			BoardCell cell = board.getCells(i);
 			if (cell.isDoorway())
 				numDoors++;
 		}
 		Assert.assertEquals(16, numDoors);
 	}
 
 
 	// Test a few room cells to ensure the room initial is
 	// correct.
 	@Test
 	public void testRoomInitials() {
 		//setup Board and rooms		
 
 		assertEquals('L', board.getRoomCellAt(0, 0).getRoomInitial());
 		assertEquals('S', board.getRoomCellAt(4, 8).getRoomInitial());
 		assertEquals('H', board.getRoomCellAt(11, 0).getRoomInitial());
 		assertEquals('D', board.getRoomCellAt(21, 22).getRoomInitial());
 		assertEquals('O', board.getRoomCellAt(21, 0).getRoomInitial());
 	}
 
 
 	// Test that an exception is thrown for a bad config file
 	@Test (expected = BadConfigFormatException.class)
 	public void testBadColumns() throws BadConfigFormatException, FileNotFoundException {
 		// overloaded Board ctor takes config file names
 		Board b = new Board("ClueLayoutBadColumns.csv", "ClueLegend.txt");
 		b.calcAdjacencies();
 		// You may change these calls if needed to match your function names
 		// My loadConfigFiles has a try/catch, so I can't call it directly to
 		// see test throwing the BadConfigFormatException
 	}
 
 
 	// Test that an exception is thrown for a bad config file
 	@Test (expected = BadConfigFormatException.class)
 	public void testBadRoom() throws FileNotFoundException, BadConfigFormatException {
 		// overloaded Board ctor takes config file name
 		Board b = new Board("ClueLayoutBadRoom.csv", "ClueLegend.txt");
 	}
 
 
 	// Test that an exception is thrown for a bad config file
 	@Test (expected = BadConfigFormatException.class)
 	public void testBadRoomFormat() throws BadConfigFormatException, FileNotFoundException {
 		// overloaded Board ctor takes config file name
 		Board b = new Board("ClueLayout.csv", "ClueLegendBadFormat.txt");
 	}
 
 
 	@Test
 	public void testCalcIndex() {
 
 		// Test each corner of the board
 		assertEquals(0, board.calcIndex(0, 0));
 		assertEquals(NUM_COLUMNS-1, board.calcIndex(0, NUM_COLUMNS-1));
 		assertEquals(600, board.calcIndex(NUM_ROWS-1, 0));
 		assertEquals(624, board.calcIndex(NUM_ROWS-1, NUM_COLUMNS-1));
 		// Test a couple others
 		assertEquals(26, board.calcIndex(1, 1));
 		assertEquals(70, board.calcIndex(2, 20));		
 	}
 
 	//testing adjacency lists
 
 	// Ensure that player does not move around within room
 	@Test
 	public void testAdjacenciesInsideRooms()
 	{
 		// Test a corner
 		LinkedList<Integer> testList = board.getAdjList(board.calcIndex(0, 0));
 		Assert.assertEquals(0, testList.size());
 		// Test one that has walkway underneath
 		testList = board.getAdjList(board.calcIndex(8, 1));
 		Assert.assertEquals(0, testList.size());
 		// Test one that has walkway above
 		testList = board.getAdjList(board.calcIndex(2, 15));
 		Assert.assertEquals(0, testList.size());
 		// Test one that is in middle of room
 		testList = board.getAdjList(board.calcIndex(2, 24));
 		Assert.assertEquals(0, testList.size());
 		// Test one beside a door
 		testList = board.getAdjList(board.calcIndex(5, 21));
 		Assert.assertEquals(0, testList.size());
 		// Test one in a corner of room
 		testList = board.getAdjList(board.calcIndex(6, 15));
 		Assert.assertEquals(0, testList.size());
 	}
 
 	// Ensure that the adjacency list from a doorway is only the
 	// walkway. 
 	@Test
 	public void testAdjacencyRoomExit()
 	{
 		// TEST DOORWAY RIGHT 
 		LinkedList<Integer> testList = board.getAdjList(board.calcIndex(3, 3));
 		Assert.assertEquals(1, testList.size());
 		Assert.assertTrue(testList.contains(board.calcIndex(4, 3)));
 		// TEST DOORWAY LEFT 
 		testList = board.getAdjList(board.calcIndex(18, 17));
 		Assert.assertEquals(1, testList.size());
 		Assert.assertTrue(testList.contains(board.calcIndex(17, 17)));
 		//TEST DOORWAY DOWN
 		testList = board.getAdjList(board.calcIndex(15, 3));
 		Assert.assertEquals(1, testList.size());
 		Assert.assertTrue(testList.contains(board.calcIndex(15, 4)));
 		//TEST DOORWAY UP
 		testList = board.getAdjList(board.calcIndex(12, 19));
 		Assert.assertEquals(1, testList.size());
 		Assert.assertTrue(testList.contains(board.calcIndex(12, 20)));
 
 	}
 
 	// Test adjacency at entrance to rooms
 	@Test
 	public void testAdjacencyDoorways()
 	{
 		// Test beside a door direction RIGHT
 		LinkedList<Integer> testList = board.getAdjList(board.calcIndex(4, 4));
 		Assert.assertTrue(testList.contains(board.calcIndex(4, 3)));
 		Assert.assertTrue(testList.contains(board.calcIndex(4, 5)));
 		Assert.assertTrue(testList.contains(board.calcIndex(5, 4)));
 		Assert.assertEquals(3, testList.size());
 		// Test beside a door direction DOWN
 		testList = board.getAdjList(board.calcIndex(6, 15));
 		Assert.assertTrue(testList.contains(board.calcIndex(5, 15)));
 		Assert.assertTrue(testList.contains(board.calcIndex(6, 14)));
 		Assert.assertTrue(testList.contains(board.calcIndex(6, 16)));
 		Assert.assertEquals(3, testList.size());
 		// Test beside a door direction LEFT
 		testList = board.getAdjList(board.calcIndex(15, 17));
 		Assert.assertTrue(testList.contains(board.calcIndex(15, 16)));
 		Assert.assertTrue(testList.contains(board.calcIndex(15, 18)));
 		Assert.assertTrue(testList.contains(board.calcIndex(14, 17)));
 		Assert.assertTrue(testList.contains(board.calcIndex(16, 17)));
 		Assert.assertEquals(4, testList.size());
 		// Test beside a door direction UP
 		testList = board.getAdjList(board.calcIndex(13, 11));
 		Assert.assertTrue(testList.contains(board.calcIndex(13, 10)));
 		Assert.assertTrue(testList.contains(board.calcIndex(13, 12)));
 		Assert.assertTrue(testList.contains(board.calcIndex(12, 11)));
 		Assert.assertTrue(testList.contains(board.calcIndex(14, 11)));
 		Assert.assertEquals(4, testList.size());
 	}
 
 	// Test a variety of walkway scenarios
 	@Test
 	public void testAdjacencyWalkways()
 	{
 		// Test on top edge of board, just one walkway piece
 		LinkedList<Integer> testList = board.getAdjList(board.calcIndex(0, 4));
 		Assert.assertTrue(testList.contains(5));
 		Assert.assertEquals(1, testList.size());
 
 		// Test on left edge of board, three walkway pieces
 		testList = board.getAdjList(board.calcIndex(6, 0));
 		Assert.assertTrue(testList.contains(board.calcIndex(5, 0)));
 		Assert.assertTrue(testList.contains(board.calcIndex(6, 1)));
 		Assert.assertTrue(testList.contains(board.calcIndex(7, 0)));
 		Assert.assertEquals(3, testList.size());
 
 		// Test between two rooms, walkways right and left
 		testList = board.getAdjList(board.calcIndex(6, 21));
 		Assert.assertTrue(testList.contains(board.calcIndex(6, 20)));
 		Assert.assertTrue(testList.contains(board.calcIndex(6, 22)));
 		Assert.assertEquals(2, testList.size());
 
 		// Test surrounded by 4 walkways
 		testList = board.getAdjList(board.calcIndex(15,7));
 		Assert.assertTrue(testList.contains(board.calcIndex(15, 8)));
 		Assert.assertTrue(testList.contains(board.calcIndex(15, 6)));
 		Assert.assertTrue(testList.contains(board.calcIndex(14, 7)));
 		Assert.assertTrue(testList.contains(board.calcIndex(16, 7)));
 		Assert.assertEquals(4, testList.size());
 
 		// Test on bottom edge of board, next to 1 room piece
 		testList = board.getAdjList(board.calcIndex(21, 15));
 		Assert.assertTrue(testList.contains(board.calcIndex(21, 16)));
 		Assert.assertTrue(testList.contains(board.calcIndex(20, 15)));
 		Assert.assertEquals(2, testList.size());
 
 		// Test on right edge of board, next to 1 room piece
 		testList = board.getAdjList(board.calcIndex(14, 22));
 		Assert.assertTrue(testList.contains(board.calcIndex(14, 21)));
 		Assert.assertTrue(testList.contains(board.calcIndex(13, 22)));
 		Assert.assertEquals(2, testList.size());
 
 		// Test on walkway next to  door that is not in the needed
 		// direction to enter
 		testList = board.getAdjList(board.calcIndex(5, 3));
 		Assert.assertTrue(testList.contains(board.calcIndex(5, 2)));
 		Assert.assertTrue(testList.contains(board.calcIndex(5, 4)));
 		Assert.assertTrue(testList.contains(board.calcIndex(6, 3)));
 		Assert.assertEquals(3, testList.size());
 	}
 
 	//testing path creation
 	@Test
 	public void testTargetsOneStep() {
 		board.calcTargets(board.calcIndex(21, 7), 1);
 		Set<BoardCell> targets= board.getTargets();
 		Assert.assertEquals(2, targets.size());
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(20, 7))); 
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(21, 6)));	
 
 		board.calcTargets(board.calcIndex(14, 0), 1);
 		targets= board.getTargets();
 		Assert.assertEquals(3, targets.size());
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(14, 1)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(13, 0)));	
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(15, 0)));			
 	}
 	// Tests of just walkways, 2 steps
 	@Test
 	public void testTargetsTwoSteps() {
 		board.calcTargets(board.calcIndex(21, 7), 2);
 		Set<BoardCell> targets= board.getTargets();
 		Assert.assertEquals(2, targets.size());
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(19, 7)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(20, 6)));
 
 		board.calcTargets(board.calcIndex(14, 0), 2);
 		targets= board.getTargets();
 		Assert.assertEquals(3, targets.size());
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(12, 0)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(14, 2)));	
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(15, 1)));			
 	}
 	// Tests of just walkways, 4 steps
 	@Test
 	public void testTargetsFourSteps() {
 		board.calcTargets(board.calcIndex(21, 7), 4);
 		Set<BoardCell> targets= board.getTargets();
 		Assert.assertEquals(4, targets.size());
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(17, 7)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(19, 7)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(18, 6)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(20, 6)));
 
 		// Includes a path that doesn't have enough length
 		board.calcTargets(board.calcIndex(14, 0), 4);
 		targets= board.getTargets();
 		Assert.assertEquals(4, targets.size());
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(14, 4)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(15, 3)));	
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(14, 2)));	
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(15, 1)));	
 	}	
 	// Tests of just walkways plus one door, 6 steps
 
 	@Test
 	public void testTargetsSixSteps() {
 		board.calcTargets(board.calcIndex(14, 0), 6);
 		Set<BoardCell> targets= board.getTargets();
 		Assert.assertEquals(7, targets.size());
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(14, 6)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(15, 5)));	
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(15, 3)));	
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(14, 4)));	
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(15, 1)));	
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(14, 2)));	
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(13, 4)));	
 	}	
 
 	// Test getting into a room
 
 	@Test 
 	public void testTargetsIntoRoom()
 	{
 		// One room is exactly 2 away
 		board.calcTargets(board.calcIndex(17, 16), 2);
 		Set<BoardCell> targets= board.getTargets();
 		Assert.assertEquals(7, targets.size());
 		// directly left (can't go right 2 steps)
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(17, 14)));
 		// directly up and down
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(15, 16)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(19, 16)));
 		// one up/down, one left/right
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(18, 17)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(18, 15)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(16, 17)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(16, 15)));
 	}
 
 	// Test getting into room, doesn't require all steps
 	@Test
 	public void testTargetsIntoRoomShortcut() 
 	{
 		board.calcTargets(board.calcIndex(12, 7), 3);
 		Set<BoardCell> targets= board.getTargets();
 		Assert.assertEquals(12, targets.size());
 		// directly up and down
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(15, 7)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(9, 7)));
 		// directly right (can't go left)
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(12, 10)));
 		// right then down
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(13, 9)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(13, 7)));
 		// down then left/right
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(14, 6)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(14, 8)));
 		// right then up
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(10, 8)));
 		// into the rooms
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(11, 6)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(10, 6)));		
 		// 
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(11, 7)));		
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(12, 8)));		
 
 	}
 
 	// Test getting out of a room
 	@Test
 	public void testRoomExit()
 	{
 		// Take one step, essentially just the adj list
 		board.calcTargets(board.calcIndex(4,20), 1);
 		Set<BoardCell> targets= board.getTargets();
 		// Ensure doesn't exit through the wall
 		Assert.assertEquals(1, targets.size());
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(4, 19)));
 		// Take two steps
 		board.calcTargets(board.calcIndex(4, 20), 2);
 		targets= board.getTargets();
 		Assert.assertEquals(3, targets.size());
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(3, 19)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(5, 19)));
 		Assert.assertTrue(targets.contains(board.getRoomCellAt(4, 18)));
 	}
 }

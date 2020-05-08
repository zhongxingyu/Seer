 public class LabyrinthTest extends Assert {
 
 	private Labyrinth lab;
 
 	@Before
 	public void setUp() {
 		String[] map = new String[]{
 			"-o-o",
 			"    ",
 			" o-o",
 			"   |",
 			"-o o",
 			"   |"
 		};
 		int[][] treasure = new int[][] {
 			new int[]{50, 5},
 			new int[]{ 0, 0},
 			new int[]{ 0,10}
 		};
 		lab = new Labyrinth(map, treasure);
 	}
 
 	@Tst
 	public void getHeightWidth_works() {
 		assertEquals(3, lab.getHeight());
 		assertEquals(2, lab.getWidth());
 	}
 
 	@Tst
 	public void getCell_worksAndKnowsWalls() {
 		Cell swCell = lab.getCell(0,0);
 		assertTrue(swCell != null);
 		assertEquals(true, swCell.hasNorthWall());
 		assertEquals(false, swCell.hasEastWall());
 
 		// even more tests
 		assertEquals(true, lab.getCell(1,1).hasNorthWall());
 		assertEquals(true, lab.getCell(1,1).hasEastWall());
 		assertEquals(true,  lab.getCell(0,2).hasNorthWall());
 		assertEquals(false, lab.getCell(0,2).hasEastWall());
 	}
 
 	@Tst
 	public void getCell_worksAndKnowsTreasure() {
 		assertEquals(0, lab.getCell(0,0).getGold());
 		assertEquals(0, lab.getCell(1,1).getGold());
 		assertEquals(50, lab.getCell(0,2).getGold());
 		assertEquals(10, lab.getCell(1,0).getGold());
 	}
 
 	@Tst
 	public void getCell_worksAndKnowsCoordinates() {
 		assertEquals(0, lab.getCell(0,0).getX());
 		assertEquals(1, lab.getCell(1,0).getX());
 		assertEquals(0, lab.getCell(0,1).getX());
 		assertEquals(1, lab.getCell(1,1).getX());
 		assertEquals(0, lab.getCell(0,2).getX());
 		assertEquals(1, lab.getCell(1,2).getX());
 
 		assertEquals(0, lab.getCell(0,0).getY());
 		assertEquals(0, lab.getCell(1,0).getY());
 		assertEquals(1, lab.getCell(0,1).getY());
 		assertEquals(1, lab.getCell(1,1).getY());
 		assertEquals(2, lab.getCell(0,2).getY());
 		assertEquals(2, lab.getCell(1,2).getY());
 	}
 }

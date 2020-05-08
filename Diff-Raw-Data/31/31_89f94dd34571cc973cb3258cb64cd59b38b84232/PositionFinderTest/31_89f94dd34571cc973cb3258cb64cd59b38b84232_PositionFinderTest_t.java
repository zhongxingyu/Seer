 package tests;
 
 import static org.junit.Assert.*;
 
 import java.io.*;
 import model.*;
 import java.util.ArrayList;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class PositionFinderTest {
 	PositionFinder pf;
 	
 	@Before
 	public final void Initialize() {
 		pf = new PositionFinder();
 	}
 
 
 	@Test
 	public final void testPlayerInOpenSpace() throws CloneNotSupportedException, IOException {
 
 		BufferedReader br = new BufferedReader(new FileReader("src/tests/maps/posfinder/map1.txt"));
 		Map map = new Map(br);
 		System.out.println(map);
 
 		Position pos = map.getPlayerPosition();
 		ArrayList<Position> sol = new ArrayList<Position>(4);
 		sol.add(pos.unboundMove('U'));
 		sol.add(pos.unboundMove('D'));
 		sol.add(pos.unboundMove('L'));
 		sol.add(pos.unboundMove('R'));
 		assertEquals(sol, pf.findEmptySpacesAround(pos, map, Cell.ECell.PLAYER));
 	}
 	
 	@Test
 	public final void testPlayerByWall() throws CloneNotSupportedException, IOException {
 
 		BufferedReader br = new BufferedReader(new FileReader("src/tests/maps/posfinder/map2.txt"));
 		Map map = new Map(br);
 		System.out.println(map);
 
 		Position pos = map.getPlayerPosition();
 		ArrayList<Position> sol = new ArrayList<Position>(4);
 		sol.add(pos.unboundMove('D'));
 		sol.add(pos.unboundMove('R'));
 		assertEquals(sol, pf.findEmptySpacesAround(pos, map, Cell.ECell.PLAYER));
 	}
 
 	@Test
 	public final void testBoxAtCrossroads() throws CloneNotSupportedException, IOException {
 
 		BufferedReader br = new BufferedReader(new FileReader("src/tests/maps/posfinder/map1.txt"));
 		Map map = new Map(br);
 		System.out.println(map);
 
 		Position pos = map.getBoxes().get(0).getPosition();
 		ArrayList<Position> sol = new ArrayList<Position>(4);
 		sol.add(pos.unboundMove('R'));
 		assertEquals(sol, pf.findEmptySpacesAround(pos, map, Cell.ECell.BOX));
 	}
 
 	@Test
 	public final void testBlockedBox() throws CloneNotSupportedException, IOException {
 
 		BufferedReader br = new BufferedReader(new FileReader("src/tests/maps/posfinder/map6.txt"));
 		Map map = new Map(br);
 		System.out.println(map);
 
 		Position pos = map.getBoxes().get(1).getPosition();
 		ArrayList<Position> sol = new ArrayList<Position>(4);
 		assertEquals(sol, pf.findEmptySpacesAround(pos, map, Cell.ECell.BOX));
 	}
 		
 	@Test
 	public final void testBoxInTunnel() throws CloneNotSupportedException, IOException {
 
 		BufferedReader br = new BufferedReader(new FileReader("src/tests/maps/posfinder/map7.txt"));
 		Map map = new Map(br);
 		System.out.println(map);
 
 		Position pos = map.getBoxes().get(0).getPosition();
 		ArrayList<Position> sol = new ArrayList<Position>(4);
 		sol.add(pos.unboundMove('U'));
 		assertEquals(sol, pf.findEmptySpacesAround(pos, map, Cell.ECell.BOX));
 	}
 
 	@Test
 	public final void testBoxInGoalTunnel() throws CloneNotSupportedException, IOException {
 
 		BufferedReader br = new BufferedReader(new FileReader("src/tests/maps/posfinder/map8.txt"));
 		Map map = new Map(br);
 		System.out.println(map);
 
 		Position pos = map.getBoxes().get(0).getPosition();
 		ArrayList<Position> sol = new ArrayList<Position>(4);
 		sol.add(pos.unboundMove('L'));
 		assertEquals(sol, pf.findEmptySpacesAround(pos, map, Cell.ECell.BOX));
 	}
 
 	@Test
 	public final void testBoxInDeadEnd() throws CloneNotSupportedException, IOException {
 
 		BufferedReader br = new BufferedReader(new FileReader("src/tests/maps/posfinder/map9.txt"));
 		Map map = new Map(br);
 		System.out.println(map);
 
 		Position pos = map.getBoxes().get(0).getPosition();
 		ArrayList<Position> sol = new ArrayList<Position>(4);
 		assertEquals(sol, pf.findEmptySpacesAround(pos, map, Cell.ECell.BOX));
 	}
 
 	@Test
 	public final void testBoxNearTwoOtherBoxes() throws CloneNotSupportedException, IOException {
 
 		BufferedReader br = new BufferedReader(new FileReader("src/tests/maps/posfinder/map10.txt"));
 		Map map = new Map(br);
 		System.out.println(map);
 
 		Position pos = map.getBoxes().get(0).getPosition();
 		ArrayList<Position> sol = new ArrayList<Position>(4);
 		sol.add(pos.unboundMove('U'));
 		sol.add(pos.unboundMove('D'));
 		sol.add(pos.unboundMove('L'));
 		sol.add(pos.unboundMove('R'));
 		assertEquals(sol, pf.findEmptySpacesAround(pos, map, Cell.ECell.BOX));
 	}
 
 	@Test
 	public final void testBoxNearThreeOtherBoxes() throws CloneNotSupportedException, IOException {
 
 		BufferedReader br = new BufferedReader(new FileReader("src/tests/maps/posfinder/map11.txt"));
 		Map map = new Map(br);
 		System.out.println(map);
 
 		Position pos = map.getBoxes().get(0).getPosition();
 		ArrayList<Position> sol = new ArrayList<Position>(4);
 		sol.add(pos.unboundMove('U'));
 		sol.add(pos.unboundMove('L'));
 		sol.add(pos.unboundMove('R'));
 		assertEquals(sol, pf.findEmptySpacesAround(pos, map, Cell.ECell.BOX));
 	}
 
 	@Test
 	public final void testBoxNearACorner() throws CloneNotSupportedException, IOException {
 
 		BufferedReader br = new BufferedReader(new FileReader("src/tests/maps/posfinder/map12.txt"));
 		Map map = new Map(br);
 		System.out.println(map);
 
 		Position pos = map.getBoxes().get(0).getPosition();
 		ArrayList<Position> sol = new ArrayList<Position>(4);
 		sol.add(pos.unboundMove('U'));
 		sol.add(pos.unboundMove('L'));
 		sol.add(pos.unboundMove('R'));
 		assertEquals(sol, pf.findEmptySpacesAround(pos, map, Cell.ECell.BOX));
 	}
 
 	@Test
 	public final void testBoxNearAStickyWall() throws CloneNotSupportedException, IOException {
 
 		BufferedReader br = new BufferedReader(new FileReader("src/tests/maps/posfinder/map13.txt"));
 		Map map = new Map(br);
 		System.out.println(map);
 
 		Position pos = map.getBoxes().get(0).getPosition();
 		ArrayList<Position> sol = new ArrayList<Position>(4);
 		sol.add(pos.unboundMove('U'));
 		sol.add(pos.unboundMove('D'));
 		sol.add(pos.unboundMove('R'));
 		assertEquals(sol, pf.findEmptySpacesAround(pos, map, Cell.ECell.BOX));
 	}
 
 	@Test
 	public final void testBoxNearTwoStickyWalls() throws CloneNotSupportedException, IOException {
 
 		BufferedReader br = new BufferedReader(new FileReader("src/tests/maps/posfinder/map14.txt"));
 		Map map = new Map(br);
 		System.out.println(map);
 
 		Position pos = map.getBoxes().get(0).getPosition();
 		ArrayList<Position> sol = new ArrayList<Position>(4);
 		sol.add(pos.unboundMove('D'));
 		sol.add(pos.unboundMove('R'));
 		assertEquals(sol, pf.findEmptySpacesAround(pos, map, Cell.ECell.BOX));
 	}

	@Test
	public final void testBoxNearGoalWall() throws CloneNotSupportedException, IOException {

		BufferedReader br = new BufferedReader(new FileReader("src/tests/maps/posfinder/map15.txt"));
		Map map = new Map(br);
		System.out.println(map);

		Position pos = map.getBoxes().get(0).getPosition();
		ArrayList<Position> sol = new ArrayList<Position>(4);
		sol.add(pos.unboundMove('U'));
		sol.add(pos.unboundMove('D'));
		sol.add(pos.unboundMove('R'));
		assertEquals(sol, pf.findEmptySpacesAround(pos, map, Cell.ECell.BOX));
	}
 }

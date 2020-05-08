 package test;
 
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.util.ArrayList;
 
 import se.chalmers.dat255.risk.model.Player;
 import se.chalmers.dat255.risk.model.WorldMap;
 
 public class WorldMapTest {
 
 	ArrayList<Player> players = new ArrayList();
 	String provinces = "A-B-C-D-E" + "\n" + "B-A-C-E" + "\n" + "C-A-D" + "\n"
 			+ "D-A-C-E" + "\n" + "E-A-B-D";
 	String continents = "3-A-B-C" + "\n" + "2-D-E";
 	WorldMap worldMap;
 
 	@Before
 	public void beforeTest() {
 		players.add(new Player(0, "Linnea"));
 		players.add(new Player(1, "Andreas"));
 		 worldMap = new WorldMap(provinces, continents, players);
 	}
 
 	@Test
 	public void testConstructProvinces() {
 		// Test if provinces has been created in a correct way
 		assertTrue(worldMap.getProvinces().size() == 5);
 		assertTrue(worldMap.getProvinces().get(0).getId().equals("A"));
 		assertTrue(worldMap.getProvinces().get(1).getId().equals("B"));
 		assertTrue(worldMap.getProvinces().get(2).getId().equals("C"));
 		assertTrue(worldMap.getProvinces().get(3).getId().equals("D"));
 		assertTrue(worldMap.getProvinces().get(4).getId().equals("E"));
 	}
 
 	@Test
 	public void testconstructNeighbors() {
 
 		// Test if neighbors has been created in a correct way
 		assertTrue(worldMap.isNeighbours("A", "B"));
 		assertTrue(worldMap.isNeighbours("A", "E"));
 		assertTrue(worldMap.isNeighbours("E", "A"));
 		assertTrue(worldMap.isNeighbours("C", "D"));
 		assertFalse(worldMap.isNeighbours("B", "D"));
 		assertFalse(worldMap.isNeighbours("C", "E"));
 		assertFalse(worldMap.isNeighbours("B", "B"));
 
 		// Test if continent has been created correct.
 		// Not implemented yet!
 
 	}
 	
 	@Test
	public void testChangeOwner(){
		worldMap.changeOwner("A",players.get(0));
		assertTrue(worldMap.getOwner("A")==players.get(0));
		
		worldMap.changeOwner("B",players.get(0));
		assertTrue(worldMap.getOwner("B")==players.get(0));
		assertTrue(worldMap.getOwner("A")==players.get(0));
		
		worldMap.changeOwner("B",players.get(1));
		assertTrue(worldMap.getOwner("B")==players.get(1));
		assertTrue(worldMap.getOwner("A")==players.get(0));
	}
	
	@Test
	public void testUpdateBonus(){
		
		//So we know which province belongs to which player
 		worldMap.changeOwner("A",players.get(0));
 		worldMap.changeOwner("B",players.get(1));
 		worldMap.changeOwner("C",players.get(1));
 		worldMap.changeOwner("D",players.get(0));
 		worldMap.changeOwner("E",players.get(1));
		
 		worldMap.updateBonus();
 		
 		assertTrue(worldMap.getBonus(players.get(0))==0);
 		assertTrue(worldMap.getBonus(players.get(1))==0);
 		
 		worldMap.changeOwner("A",players.get(1));
 		worldMap.updateBonus();
 		assertTrue(worldMap.getBonus(players.get(1))==3);
 		
 		worldMap.changeOwner("E",players.get(0));
 		worldMap.updateBonus();
 		assertTrue(worldMap.getBonus(players.get(0))==2);
 	}
 
 }

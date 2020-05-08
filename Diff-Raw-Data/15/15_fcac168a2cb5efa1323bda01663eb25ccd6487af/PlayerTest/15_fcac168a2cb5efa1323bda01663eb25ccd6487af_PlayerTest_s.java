 package chalmers.TDA367.B17.model;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class PlayerTest {
 	
 	@Test
 	public void testSetName(){
 		Player player = new Player("Test Player");
 		assertTrue(player.getName().equals("Test Player"));
 		
 		player.setName("New Name");
 		assertTrue(player.getName().equals("New Name"));
 	}
 	
 	@Test
	public void testSetPlayerID(){
		Player player = new Player("Test Player");
		player.setPlayerId(123);
		assertTrue(player.getPlayerId()==123);
	}
	
	@Test
 	public void testSetScore(){
 		Player player = new Player("Test Player");
 		player.setScore(123);
 		assertTrue(player.getScore()==123);
 	}
 	
 	@Test
 	public void testSetEliminated(){
 		Player player = new Player("Test Player");
 		player.setEliminated(false);
 		assertTrue(!player.isEliminated());
 	}
 
 	@Test
 	public void testSetRespawnTime(){
 		Player player = new Player("Test Player");
 		player.setRespawnTime(123);
 		assertTrue(player.getRespawnTime() == 123);
 	}
 	
 	@Test
 	public void testSetLives(){
 		Player player = new Player("Test Player");
 		player.setLives(12);
 		assertTrue(player.getLives() == 12);
 	}
 	
 	@Test
 	public void testSetRespawnTimer(){
 		Player player = new Player("Test Player");
 		player.setRespawnTimer(123);
 		assertTrue(player.getRespawnTimer() == 123);
 	}
 	
 	@Test
 	public void testSetActive(){
 		Player player = new Player("Test Player");
 		player.setActive(false);
 		assertTrue(!player.isActive());
 	}
 	
 	@Test
 	public void testSetInputStatus(){
 		Player player = new Player("Test Player");
 		player.setInputStatus(Player.INPT_W, true);
 		assertTrue(player.getInput().get(0).booleanValue() == true);
 	}
 	
 	@BeforeClass
 	public static void beforeClass(){
 		System.out.println("Before class!");
 	}
 	
 	@AfterClass
 	public static void afterClass(){
 		System.out.println("After class!");
 	}
 	
 	@Before
 	public void before(){
 		System.out.println("Before");
 	}
 	
 	@After
 	public void after(){
 		System.out.println("After");
 	}
 }

 package controller;
 
 import static org.junit.Assert.*;
 
 import model.Arena;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class ArenaControllerTest {
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		model.CubeAttributes.xNumberOfLedsPerRow = 16;
 		model.CubeAttributes.yNumberOfRowsPerPanel = 16;
 		model.CubeAttributes.zNumberOfPanelsPerCube = 16;
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void createAppleTest()
 	{
 
		model.Arena.xMaximum = 15;
		model.Arena.yMaximum = 15;
		model.Arena.zMaximum = 15;
 
 		ArenaController theArenaController = new ArenaController();
 		GameController theGameController = new GameController();
 		
 		theGameController.createSnakes(4);
 		
 		//Create an apple with color 255
 		theArenaController.createApple(255);
 	}
 
 
 }

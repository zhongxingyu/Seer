 package view;
 
 import static org.junit.Assert.*;
 import model.general.Constances;
 
 import org.junit.*;
 
 import view.game.Logger;
 
 import controller.GameController;
 import database.GameContent;
 
 public class LoggerTest {
 	
 	private GameController controller;
 	private Logger logger;
 	
 	@Before
 	public void setUp() {
		controller = new GameController(null);
 		controller.newController(Constances.DEFAULT_ROWS, Constances.DEFAULT_COLUMNS, "Human", GameController.AI_PLAYER_1, GameContent.SINGLEPLAYER);
 		logger = new Logger(controller);		
 	}
 	
 	@Test
 	public void testLogger() {
 		try {
 			new Logger(controller);
 		} catch (Exception exc) {
 			fail("Logger should not throw exception at constructor");
 		}
 	}
 
 	@Test
 	public void testupdate() {
 		assertNotNull(logger);
 		try {
 			logger.update();
 		} catch (Exception exc) {
 			fail("Logger should not throw exception at this method");
 		}
 	}
 
 }

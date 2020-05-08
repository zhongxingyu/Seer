 /**
  * 
  */
 package test.game.unit;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import exception.InvalidMoveException;
 import exception.NoPlayersLeftException;
 import game.Game;
 import game.Race;
 import grid.Direction;
 import grid.TestCaseOneGridBuilder;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import controller.EndTurnController;
 import controller.GameController;
 import controller.MoveController;
 
 /**
  * @author Maxim
  *
  */
 public class RaceTest {
 	
 	Game game;
 	GameController gc;
 	MoveController mc;
 	EndTurnController ec;
 	
 	Race race;
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		gc = new GameController();
 		gc.startNewGame(new TestCaseOneGridBuilder(), new Race());
 		mc = gc.getMoveController();
 		ec = gc.getEndTurnController();
 		game = gc.getGame();
 		
 		race = (Race) game.getMode();
 	}
 
 	/**
 	 * Test method for {@link game.Race#checkWin(game.Player)}.
 	 * @throws InvalidMoveException 
 	 * @throws NoPlayersLeftException 
 	 */
 	@Test
 	public void testCheckWin() throws InvalidMoveException, NoPlayersLeftException {
 		//red player moves
 		mc.move(Direction.NORTHEAST);
 		mc.move(Direction.NORTHEAST);
 		mc.move(Direction.NORTHEAST);
 		mc.move(Direction.NORTHEAST);
 		
 		//blue player moves
 		mc.move(Direction.WEST);
 		ec.endTurn();
 		
 		//red player moves
 		mc.move(Direction.NORTHEAST);
 		mc.move(Direction.NORTHEAST);
 		mc.move(Direction.NORTHEAST);
 		mc.move(Direction.NORTH);
 		
 		//blue player moves
 		mc.move(Direction.WEST);
 		ec.endTurn();
 		
 		//red player moves (9,8)
 		mc.move(Direction.NORTHEAST);
 		mc.move(Direction.EAST);
 		mc.move(Direction.EAST);
 		mc.move(Direction.EAST);
 		
 		//blue player moves
 		mc.move(Direction.WEST);
 		ec.endTurn();
 		
 		//red player moves (9,8)
 		mc.move(Direction.NORTHEAST);
 		mc.move(Direction.EAST);
 		
 		assertTrue(game.getMode().checkWin(game.getTurnHandler().getCurrentPlayer()));
		assertFalse(game.getMode().checkWin(game.getTurnHandler().getActivePlayers()[1]));
 
 	}
 
 	/**
 	 * Test method for {@link game.Mode#checkLoseAfterAction(game.Player)}.
 	 * @throws InvalidMoveException 
 	 * @throws NoPlayersLeftException 
 	 */
 	@Test
 	public void testCheckLoseAfterAction() throws InvalidMoveException, NoPlayersLeftException {
 		
 
 	}
 
 }

 /**
  * 
  */
 package test.item.scenario;
 
 import static org.junit.Assert.*;
 import item.PortableItem;
 import item.Teleporter;
 import exception.InvalidMoveException;
 import exception.NoItemException;
 import exception.NoPlayersLeftException;
 import exception.OutsideTheGridException;
 import exception.OverCapacityException;
 import game.Game;
 import game.Race;
 import grid.Coordinate;
 import grid.Direction;
 import grid.TestCaseTwoGridBuilder;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import controller.EndTurnController;
 import controller.GameController;
 import controller.MoveController;
 import controller.PickUpItemController;
 
 /**
  * @author Maxim
  *
  */
 public class TeleporterScenarioTest {
 	
 	private Game game;
 	private MoveController mc;
 	private PickUpItemController pc;
 	private EndTurnController ec;
 	
 	@Before
 	public void setUp() throws Exception {
 		GameController gc = new GameController();
 		gc.startNewGame(new TestCaseTwoGridBuilder(),new Race());
 		game = gc.getGame();
 		mc = gc.getMoveController();
 		pc = gc.getPickUpItemController();
 		ec = gc.getEndTurnController();
 	}
 
 	/**
 	 * Scenario:
 	 * 
 	 * Perform a normal teleportation.
 	 * 
 	 * @throws InvalidMoveException
 	 * @throws OutsideTheGridException
 	 * @throws NoPlayersLeftException 
 	 */
 	@Test
 	public void testTeleport() throws InvalidMoveException, OutsideTheGridException, NoPlayersLeftException {
 		// Red moves.
 		mc.move(Direction.EAST);
 		Teleporter t = (Teleporter) game.getGrid().getSquareAtCoordinate(new Coordinate(3,2)).getItems()[0];
 		mc.move(Direction.NORTHEAST);
 		assertTrue(game.getTurnHandler().getCurrentPlayer().getLocation() == t.getDestination().getLocation());
 	}
 
 	/**
 	 * Scenario:
 	 * 
 	 * A player is unable to step onto a teleporter if another player is on the square of the corresponding destination teleporter.
 	 * 
 	 * @throws OutsideTheGridException 
 	 * @throws NoPlayersLeftException 
 	 */
 	@Test(expected=InvalidMoveException.class)
 	public void testPlayerOnDestination() throws InvalidMoveException, OutsideTheGridException, NoPlayersLeftException {
 		// Red moves.
 		mc.move(Direction.EAST);
 		mc.move(Direction.NORTHEAST);
 		ec.endTurn();
 		// Blue moves.
 		mc.move(Direction.SOUTHWEST);
 		ec.endTurn();
 		// Red moves.
 		mc.move(Direction.NORTHWEST);
 		mc.move(Direction.SOUTH);
 		mc.move(Direction.SOUTH);
 		ec.endTurn();
 		// Blue moves.
 		mc.move(Direction.SOUTH);
 		ec.endTurn();
 		// Red moves.
 		mc.move(Direction.EAST);
 		mc.move(Direction.NORTH);
 		game.getTurnHandler().getCurrentPlayer().getLightTrail().removeSquare(null);
 		ec.endTurn();
 		// Blue moves.
 		mc.move(Direction.SOUTH);
 		mc.move(Direction.NORTHWEST);
 		// Teleporter, but the red player is on the destination teleporter so exception.
 	}
 	
 	/**
 	 * Scenario:
 	 * 
 	 * Since a player is immediately teleported to the destination teleporter, he has no chance 
 	 * to pick up any items from the source teleporter location.
 	 * 
 	 * @throws InvalidMoveException 
 	 * @throws OutsideTheGridException 
 	 * @throws OverCapacityException 
 	 * @throws NoItemException 
 	 * @throws NoPlayersLeftException 
 	 */
 	@Test(expected=NoItemException.class)
 	public void testPickUpAtSource() throws InvalidMoveException, OutsideTheGridException, NoItemException, OverCapacityException, NoPlayersLeftException {
 		// Red moves.
 		mc.move(Direction.NORTH);
 		ec.endTurn();
 		
 		// Blue moves.
 		PortableItem grenade;
 		if (game.getGrid().getSquareAtCoordinate(new Coordinate(9,8)).getItems()[0] instanceof PortableItem)
 			grenade = (PortableItem) game.getGrid().getSquareAtCoordinate(new Coordinate(9,8)).getItems()[0];
 		else grenade = (PortableItem) game.getGrid().getSquareAtCoordinate(new Coordinate(9,8)).getItems()[1];
 		mc.move(Direction.SOUTHWEST);
 		mc.move(Direction.SOUTHWEST);
 		pc.pickUpItem(grenade);
 	}
 	
 	/**
 	 * Scenario:
 	 * 
 	 * But he can pick up items on the destination teleporter location (if he has an action left in his turn).
 	 * 
 	 * @throws InvalidMoveException 
 	 * @throws OverCapacityException 
 	 * @throws NoItemException 
 	 * @throws OutsideTheGridException 
 	 * @throws NoPlayersLeftException 
 	 */
 	@Test
 	public void testPickUpAtDestination() throws InvalidMoveException, NoItemException, OverCapacityException, OutsideTheGridException, NoPlayersLeftException {
 		// Red moves.
 		mc.move(Direction.EAST);
 		mc.move(Direction.NORTHEAST);
 		PortableItem grenade;
 		if (game.getGrid().getSquareAtCoordinate(new Coordinate(9,8)).getItems()[0] instanceof PortableItem)
 			grenade = (PortableItem) game.getGrid().getSquareAtCoordinate(new Coordinate(9,8)).getItems()[0];
 		else grenade = (PortableItem) game.getGrid().getSquareAtCoordinate(new Coordinate(9,8)).getItems()[1];
 		pc.pickUpItem(grenade);
 		ec.endTurn();
		assertEquals(game.getTurnHandler().getActivePlayers()[1].getInventory().getItems()[0],grenade);
 	}
 
 }

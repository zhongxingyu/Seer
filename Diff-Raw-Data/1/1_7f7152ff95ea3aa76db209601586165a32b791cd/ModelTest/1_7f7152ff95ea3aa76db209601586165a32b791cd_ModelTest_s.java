 package tests;
 
 import junit.framework.TestCase;
 
 import com.example.model.Boat;
 import com.example.model.BoatType;
 import com.example.model.Button;
 import com.example.model.Direction;
 import com.example.model.Model;
 import com.example.model.ModelImplementation;
 import com.example.model.Orientation;
 import com.example.model.Player;
 import com.example.model.Position;
 import com.example.model.Stage;
 
 public class ModelTest extends TestCase {
 
 	public void testShouldBePlayer1sTurnAndPlaceBoatStageWhenStartingNewGame() throws Throwable {
         ModelImplementation model = new ModelImplementation();
         assertEquals(Player.PLAYER1, model.getTurn());
         assertEquals(Stage.PLACE_BOATS, model.getStage());   
     }
 	
 	public void testAllBoatsShouldBeCreatedInTheStart() throws Throwable {
 		ModelImplementation model = new ModelImplementation();
 		assertEquals(BoatType.AIRCRAFT_CARRIER, model.getBoat(BoatType.AIRCRAFT_CARRIER, Player.PLAYER1).getType());
 		assertEquals(BoatType.BATTLESHIP, model.getBoat(BoatType.BATTLESHIP, Player.PLAYER1).getType());
 		assertEquals(BoatType.SUBMARINE, model.getBoat(BoatType.SUBMARINE, Player.PLAYER1).getType());
 		assertEquals(BoatType.DESTROYER, model.getBoat(BoatType.DESTROYER, Player.PLAYER1).getType());
 		assertEquals(BoatType.PATROL_BOAT, model.getBoat(BoatType.PATROL_BOAT, Player.PLAYER1).getType());
 		
 		assertEquals(BoatType.AIRCRAFT_CARRIER, model.getBoat(BoatType.AIRCRAFT_CARRIER, Player.PLAYER2).getType());
 		assertEquals(BoatType.BATTLESHIP, model.getBoat(BoatType.BATTLESHIP, Player.PLAYER2).getType());
 		assertEquals(BoatType.SUBMARINE, model.getBoat(BoatType.SUBMARINE, Player.PLAYER2).getType());
 		assertEquals(BoatType.DESTROYER, model.getBoat(BoatType.DESTROYER, Player.PLAYER2).getType());
 		assertEquals(BoatType.PATROL_BOAT, model.getBoat(BoatType.PATROL_BOAT, Player.PLAYER2).getType());
 	}
 	
 	public void testThereShouldBeNoBoatsPlacedInTheStart() throws Throwable {
 		ModelImplementation model = new ModelImplementation();
 		for (Boat boat : model.getBoats()) {
 			assertSame(null, boat.getOrientation());
 		}
 	}
 	
 	public void testUpdatingWithChangeDirectionButtonShouldUpdateTheModel() throws Throwable {
 		ModelImplementation model = new ModelImplementation();
 		Button button = Button.CHANGE_DIRECTION;
 		Direction originalDirection = model.getDirection();
 		
 		model.update(null, button);
 		
 		Direction maybeChangedDirection = model.getDirection();
 		assertNotSame(originalDirection, maybeChangedDirection);
 	}
 	
 	public void testGetNextBoatToPlaceShouldReturnNextBoatWhenBoatsArePlaced() throws Throwable {
 		ModelImplementation model = new ModelImplementation();
 		Position pos = new Position(5, 'e');
 		Orientation orientation = new Orientation(pos, Direction.RIGHT);
 		Boat boatToPlace;
 		while (model.getNextBoatToPlace() != null) {
 			boatToPlace = model.getNextBoatToPlace();
 			boatToPlace.placeBoat(orientation);
 			assertNotSame(boatToPlace, model.getNextBoatToPlace());
 		}
 	}
 	
 	public void testUpdateWithAPositionShouldWorkAccordingToCurrentState() throws Throwable {
 		ModelImplementation model = new ModelImplementation();
 		Position pos1 = new Position(1, 'a');
 		model.update(null, pos1);
 		Boat boat1 = model.getBoat(BoatType.AIRCRAFT_CARRIER, Player.PLAYER1);
 		assertTrue(boat1.isPlaced());
 		
 		Position pos2 = new Position(5, 'e');
 		model.update(null, pos2);
 		Boat boat2 = model.getBoat(BoatType.BATTLESHIP, Player.PLAYER1);
 		assertTrue(boat2.isPlaced());
 	}
 	
 	public void testPlacingThroughUpdateAllPlayerOneBoatsShouldChangeTurnToPlayerTwo() throws Throwable {
 		ModelImplementation model = new ModelImplementation();
 		sendUpdateOnFiveDifferentPlacesOnGrid(model);
 		assertEquals(Player.PLAYER2, model.getTurn());
 	}
 	
 	private void sendUpdateOnFiveDifferentPlacesOnGrid(Model model) {
 		Position p1 = new Position(1, 'j');
 		Position p2 = new Position(2, 'i');
 		Position p3 = new Position(3, 'h');
 		Position p4 = new Position(4, 'g');
 		Position p5 = new Position(5, 'f');
 		model.update(null, p1);
 		model.update(null, p2);
 		model.update(null, p3);
 		model.update(null, p4);
 		model.update(null, p5);
 	}
 	
 	public void testPlacingThroughUpdateAllPlayerOneBoatsShouldSetShowChangingPlayersScreenToTrue() throws Throwable {
 		ModelImplementation model = new ModelImplementation();
 		sendUpdateOnFiveDifferentPlacesOnGrid(model);
 		assertEquals(true, model.showChangingPlayersScreen());
 	}
 	
 	public void testUpdateWithButtonChangingPlayersPauseScreenNextShouldSetShowChangingPlayersScreenFalse() {
 		ModelImplementation model = new ModelImplementation();
 		sendUpdateOnFiveDifferentPlacesOnGrid(model);
 		assertTrue(model.showChangingPlayersScreen());
 		model.update(null, Button.CHANGING_PLAYERS_PAUSESCREEN_NEXT);
 		assertFalse(model.showChangingPlayersScreen());
 	}
 	
 	public void testPlacingAllBoatsShouldSetStageToPlaceBombs() throws Throwable {
 		ModelImplementation model = new ModelImplementation();
 		goToBombingFace(model);
 		assertEquals(Stage.PLACE_BOMB, model.getStage());
 	}
 	
 	private void goToBombingFace(Model model) {
 		sendUpdateOnFiveDifferentPlacesOnGrid(model);
 		model.update(null, Button.CHANGING_PLAYERS_PAUSESCREEN_NEXT);
 		sendUpdateOnFiveDifferentPlacesOnGrid(model);
 	}
 	
 	public void testPlacingAllBoatsShouldSetTurnToPlayerOne() throws Throwable {
 		ModelImplementation model = new ModelImplementation();
 		sendUpdateOnFiveDifferentPlacesOnGrid(model);
 		model.update(null, Button.CHANGING_PLAYERS_PAUSESCREEN_NEXT);
 		sendUpdateOnFiveDifferentPlacesOnGrid(model);
 		assertEquals(Player.PLAYER1, model.getTurn());
 	}
 	
 	public void testStageShouldTransitToGameOverWhenGameOverReturnsTrue() throws Throwable {
 		ModelImplementation model = new ModelImplementation();
 		goToBombingFace(model);
 		
 		// this bombs everything, for both players
 		char[] rows = new char[] { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j'};
 		Position pos;
 		for (char row : rows) {
 			for (int column = 1; column < 11; column++) {
 				pos = new Position(column, row);
 				model.update(null, pos);
 				model.update(null, Button.CHANGING_PLAYERS_PAUSESCREEN_NEXT);
 				model.update(null, Button.SHOW_OWN_BOARD_FLIP);
 				model.update(null, pos);
 			}
 		}
 		assertSame(Stage.GAME_OVER, model.getStage());
 	}
 	
 	public void testPlacingABombShouldChangeWhichPlayersTurnItIs() throws Throwable {
 		ModelImplementation model = new ModelImplementation();
 		goToBombingFace(model);
 		assertEquals(Stage.PLACE_BOMB, model.getStage());
 		Position p1 = new Position(1, 'j');
 		model.update(null, p1);
 		assertEquals(Player.PLAYER2, model.getTurn());
 		model.update(null, Button.CHANGING_PLAYERS_PAUSESCREEN_NEXT);
 		model.update(null, p1);
 		assertEquals(Player.PLAYER1, model.getTurn());
 	}
 	
 	public void testFailingToPlacingABombShouldNotChangeWhichPlayersTurnItIs() throws Throwable {
 		ModelImplementation model = new ModelImplementation();
 		goToBombingFace(model);
 		assertTrue(model.getStage() == Stage.PLACE_BOMB);
 		Position p1 = new Position(1, 'j');
 		assertEquals(Player.PLAYER1, model.getTurn());
 		model.update(null, p1);
 		model.update(null, Button.CHANGING_PLAYERS_PAUSESCREEN_NEXT);
 		assertEquals(Player.PLAYER2, model.getTurn());
 		model.update(null, p1);
 		model.update(null, Button.CHANGING_PLAYERS_PAUSESCREEN_NEXT);
 		assertEquals(Player.PLAYER1, model.getTurn());
 		model.update(null, p1);
 		assertEquals(Player.PLAYER1, model.getTurn());
 	}
 	
 	public void testLegalPlacementOfBoatShouldConciderBothOverlappingBoatsAndWalls() throws Throwable {
 		ModelImplementation model = new ModelImplementation();
 		Position aircraftCarrierPosition = new Position(1, 'j');
 		Position tooCloseToCarrier = new Position(5, 'j');
 		Position tooCloseToWall = new Position(9, 'j');
 		Player player = Player.PLAYER1;
 		model.update(null, aircraftCarrierPosition);
 		Boat aircraftCarrier = model.getBoat(BoatType.AIRCRAFT_CARRIER, player);
 		assertTrue(aircraftCarrier.isPlaced());
 		model.update(null, tooCloseToCarrier);
 		Boat battleship = model.getBoat(BoatType.BATTLESHIP, player);
 		assertFalse(battleship.isPlaced());
 		model.update(null, tooCloseToWall);
 		assertFalse(battleship.isPlaced());
 	}
 	
 	public void testAttemptToPlaceBoatShouldWork() throws Throwable {
 		ModelImplementation model = new ModelImplementation();
 		Boat boat = model.getBoat(BoatType.AIRCRAFT_CARRIER, Player.PLAYER1);
 		Position pos = new Position(5, 'e');
 		Orientation orientation = new Orientation(pos, Direction.RIGHT);
 		model.attemptToPlaceBoat(boat, orientation);
 		assertTrue(boat.isPlaced());
 	}
 	
 	public void testRestartButtonShouldNotRestartTheGameIfNotGameOver() throws Throwable {
 		ModelImplementation model = new ModelImplementation();
 		Position aircraftCarrierPosition = new Position(1, 'j');
 		Boat aircraftCarrier = model.getBoat(BoatType.AIRCRAFT_CARRIER, Player.PLAYER1);
 		assertFalse(aircraftCarrier.isPlaced());
 		model.update(null, aircraftCarrierPosition);
 		assertTrue(aircraftCarrier.isPlaced());
 		
 		model.update(null, Button.RESTART);
 		
 		Boat freshAircraftCarrier = model.getBoat(BoatType.AIRCRAFT_CARRIER, Player.PLAYER1);
 		assertTrue(freshAircraftCarrier.isPlaced());
 	}
 }

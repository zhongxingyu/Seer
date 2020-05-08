 /*******************************************************************************
  * This files was developed for CS4233: Object-Oriented Analysis & Design.
  * The course was taken at Worcester Polytechnic Institute.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package strategy.game.version.beta;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import strategy.common.PlayerColor;
 import strategy.common.StrategyException;
 import strategy.game.StrategyGameController;
 import strategy.game.StrategyGameFactory;
 import strategy.game.common.Location;
 import strategy.game.common.Location2D;
 import strategy.game.common.MoveResult;
 import strategy.game.common.MoveResultStatus;
 import strategy.game.common.Piece;
 import strategy.game.common.PieceLocationDescriptor;
 import strategy.game.common.PieceType;
 
 /**
  * Description
  * @author gpollice
  * @version Sep 6, 2013
  */
 public class BetaStrategyMasterTest
 {
 	private StrategyGameController game;
 	private StrategyGameFactory gameFactory;
 	private static Collection<PieceLocationDescriptor> redConfiguration;
 	private static Collection<PieceLocationDescriptor> blueConfiguration;
 	
 	@BeforeClass
 	static public void oneTimeSetup(){
 		//Red configuration
 		redConfiguration = new ArrayList<PieceLocationDescriptor>();
 		
 		//Red pieces
 		redConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.FLAG, PlayerColor.RED), new Location2D(0,0)));
 		redConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.MARSHAL, PlayerColor.RED), new Location2D(0,1)));
 		redConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.COLONEL, PlayerColor.RED), new Location2D(0,2)));
 		redConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.COLONEL, PlayerColor.RED), new Location2D(0,3)));
 		redConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.CAPTAIN, PlayerColor.RED), new Location2D(0,4)));
 		redConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.CAPTAIN, PlayerColor.RED), new Location2D(0,5)));
 		redConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.LIEUTENANT, PlayerColor.RED), new Location2D(1,0)));
 		redConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.LIEUTENANT, PlayerColor.RED), new Location2D(1,1)));
 		redConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.LIEUTENANT, PlayerColor.RED), new Location2D(1,2)));
 		redConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.SERGEANT, PlayerColor.RED), new Location2D(1,3)));
 		redConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.SERGEANT, PlayerColor.RED), new Location2D(1,4)));
 		redConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.SERGEANT, PlayerColor.RED), new Location2D(1,5)));
 		
 		//Blue configuration
 		blueConfiguration = new ArrayList<PieceLocationDescriptor>();
 		
 		//Blue pieces
 		blueConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.FLAG, PlayerColor.BLUE), new Location2D(5,0)));
 		blueConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.MARSHAL, PlayerColor.BLUE), new Location2D(5,1)));
 		blueConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.COLONEL, PlayerColor.BLUE), new Location2D(5,2)));
 		blueConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.COLONEL, PlayerColor.BLUE), new Location2D(5,3)));
 		blueConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.CAPTAIN, PlayerColor.BLUE), new Location2D(5,4)));
 		blueConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.CAPTAIN, PlayerColor.BLUE), new Location2D(5,5)));
 		blueConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.LIEUTENANT, PlayerColor.BLUE), new Location2D(4,0)));
 		blueConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.LIEUTENANT, PlayerColor.BLUE), new Location2D(4,1)));
 		blueConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.LIEUTENANT, PlayerColor.BLUE), new Location2D(4,2)));
 		blueConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.SERGEANT, PlayerColor.BLUE), new Location2D(4,3)));
 		blueConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.SERGEANT, PlayerColor.BLUE), new Location2D(4,4)));
 		blueConfiguration.add(new PieceLocationDescriptor(new Piece(PieceType.SERGEANT, PlayerColor.BLUE), new Location2D(4,5)));
 	}
 	
 	@Before
 	public void setup() throws StrategyException
 	{
 		gameFactory = StrategyGameFactory.getInstance();
 		game = gameFactory.makeBetaStrategyGame(redConfiguration, blueConfiguration);
 	}
 	
	@Test
 	public void invalidSetup() throws StrategyException{
 		
 		//Red configuration
 		Collection<PieceLocationDescriptor> invalidConfiguration1 = new ArrayList<PieceLocationDescriptor>();
 		invalidConfiguration1.add(new PieceLocationDescriptor(new Piece(PieceType.FLAG, PlayerColor.RED), new Location2D(0,0)));
 		
 		Collection<PieceLocationDescriptor> invalidConfiguration2 = new ArrayList<PieceLocationDescriptor>();
 		invalidConfiguration2.add(new PieceLocationDescriptor(new Piece(PieceType.FLAG, PlayerColor.BLUE), new Location2D(0,0)));
 
 		
 		gameFactory.makeBetaStrategyGame(invalidConfiguration1, invalidConfiguration2);
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveBeforeInitialization() throws StrategyException
 	{
 		PieceLocationDescriptor r = redConfiguration.iterator().next();
 		PieceLocationDescriptor b = blueConfiguration.iterator().next();
 		game.move(r.getPiece().getType(), r.getLocation(), b.getLocation()); //should throw "You must start the game!"
 	}
 	
 	@Test
 	public void makeValidMove() throws StrategyException
 	{
 		game.startGame(); 
 		Location start = new Location2D(1,0);
 		Location moveTo = new Location2D(2,0);
 		
 		//moves the lieutenant forward one square
 		assertEquals(game.move(game.getPieceAt(start).getType(), start, moveTo), new MoveResult(MoveResultStatus.OK, null)); //shouldn't throw exception
 		
 	}
 	
 	@Test
 	public void makeSixMovesAndDraw() throws StrategyException
 	{
 		game.startGame();
 		Location loc1 = new Location2D(1,0);
 		Location loc2 = new Location2D(2,0);
 		Location loc3 = new Location2D(4,0);
 		Location loc4 = new Location2D(3,0);
 		
 		//make 5 useless moves
 		game.move(PieceType.LIEUTENANT, loc1, loc2);
 		game.move(PieceType.LIEUTENANT, loc3, loc4);
 		game.move(PieceType.LIEUTENANT, loc2, loc1);
 		game.move(PieceType.LIEUTENANT, loc4, loc3);
 		game.move(PieceType.LIEUTENANT, loc1, loc2);
 		
 		//make 6th move, check to see if draw
 		assertEquals(game.move(PieceType.LIEUTENANT, loc3, loc4), new MoveResult(MoveResultStatus.DRAW, null)); //shouldn't throw exception
 		//check if game is over? TODO: figure out if needed.
 		//assertEquals(game.gameStatus(), enum(game_over)); //figure out what to do here...
 		
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveWithWrongPiece() throws StrategyException
 	{
 		game.startGame();
 		Location lieutLoc = new Location2D(1,0);
 		Location lieutDest = new Location2D(2,0);
 		
 		game.move(PieceType.CAPTAIN, lieutLoc, lieutDest);
 		//maybe assert error statement that it should be lieut in that spot?
 	}
 
 	@Test(expected=StrategyException.class)
 	public void makeMoveWithOutOfBoundsXCoordinate() throws StrategyException
 	{
 		game.startGame();
 		game.move(PieceType.CAPTAIN, new Location2D(5, 5), new Location2D(6,5)); //should throw a out of board error
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveWithOutOfBoundsYCoordinate() throws StrategyException
 	{
 		game.startGame();
 		game.move(PieceType.CAPTAIN, new Location2D(5, 5), new Location2D(5,6)); //should throw an out of board error
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveWithIncorrectDistance() throws StrategyException
 	{
 		game.startGame();
 		game.move(PieceType.LIEUTENANT, new Location2D(1,0), new Location2D(3, 0)); //should throw a distance error
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void battleWithAttackerWinning() throws StrategyException
 	{
 		game.startGame();
 		game.move(PieceType.SERGEANT, new Location2D(1, 4), new Location2D(2, 4)); //red
 		game.move(PieceType.LIEUTENANT, new Location2D(4, 3), new Location2D(3, 3)); //blue
 		game.move(PieceType.SERGEANT, new Location2D(2, 4), new Location2D(3, 4)); //red
 		//blue
 		assertEquals(game.move(PieceType.LIEUTENANT, new Location2D(3, 3), new Location2D(3, 4)), new MoveResult(MoveResultStatus.OK,
 				new PieceLocationDescriptor(new Piece(PieceType.LIEUTENANT, PlayerColor.BLUE), new Location2D(3,4))));
 
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void battleWithAttackerLosing() throws StrategyException
 	{
 		game.startGame();
 		game.move(PieceType.LIEUTENANT, new Location2D(1, 3), new Location2D(2, 3)); //red
 		game.move(PieceType.SERGEANT, new Location2D(4, 4), new Location2D(3, 4)); //blue
 		game.move(PieceType.LIEUTENANT, new Location2D(2, 3), new Location2D(3, 3)); //red
 		//blue
 		assertEquals(game.move(PieceType.SERGEANT, new Location2D(3, 4), new Location2D(3, 3)), new MoveResult(MoveResultStatus.OK,
 				new PieceLocationDescriptor(new Piece(PieceType.LIEUTENANT, PlayerColor.RED), new Location2D(3,4))));
 	}
 	
 	@Test
 	public void testGetPieceAt(){
 		assertEquals(new Piece(PieceType.FLAG, PlayerColor.RED), game.getPieceAt(new Location2D(0,0)));
 	}
 	
 	@Test
 	public void testGetPieceAt_noPieceAtLocation(){
 		assertNull(game.getPieceAt(new Location2D(3,3)));
 	}
 	
 	@Test
 	public void testGetPieceAt_invalidLocation(){
 		assertNull(game.getPieceAt(new Location2D(7,9)));
 	}
 	
 	/* These seem useless... TODO: Delete?
 	@Test
 	public void makeValidFirstMoveRedWins() throws StrategyException
 	{
 		
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveWithWrongPiece() throws StrategyException
 	{
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveWithIncorrectFromXCoordinate() throws StrategyException
 	{
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveWithIncorrectFromYCoordinate() throws StrategyException
 	{
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveWithIncorrectToXCoordinate() throws StrategyException
 	{
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveWithIncorrectToYCoordinate() throws StrategyException
 	{
 	}
 	
 	@Test(expected=StrategyException.class)
 	public void makeMoveAfterGameIsComplete() throws StrategyException
 	{
 	}
 	
 	@Test
 	public void playTwoGames() throws StrategyException
 	{
 	}
 	
 	@Test
 	public void initialPositionHasCorrectPiecesPlaced() throws StrategyException
 	{
 	}
 	
 	@Test
 	public void boardIsCorrectAfterFirstMove() throws StrategyException
 	{
 	}
 	
 	*/
 }
 

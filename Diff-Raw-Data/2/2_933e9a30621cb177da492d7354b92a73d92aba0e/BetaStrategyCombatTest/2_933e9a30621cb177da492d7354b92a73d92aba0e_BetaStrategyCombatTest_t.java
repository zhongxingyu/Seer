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
 import static strategy.common.PlayerColor.BLUE;
 import static strategy.common.PlayerColor.RED;
 import static strategy.game.common.PieceType.CAPTAIN;
 import static strategy.game.common.PieceType.COLONEL;
 import static strategy.game.common.PieceType.FLAG;
 import static strategy.game.common.PieceType.LIEUTENANT;
 import static strategy.game.common.PieceType.MARSHAL;
 import static strategy.game.common.PieceType.SERGEANT;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import strategy.common.PlayerColor;
 import strategy.common.StrategyException;
 import strategy.game.StrategyGameController;
 import strategy.game.StrategyGameFactory;
 import strategy.game.common.Location2D;
 import strategy.game.common.MoveResult;
 import strategy.game.common.MoveResultStatus;
 import strategy.game.common.Piece;
 import strategy.game.common.PieceLocationDescriptor;
 import strategy.game.common.PieceType;
 
 /**
  * All tests relating to Beta Strategy and Combat
  * @author rjsmieja, jrspicola
  * @version Sep 11, 2013
  */
 
 public class BetaStrategyCombatTest{
 	private StrategyGameController game;
 	private static StrategyGameFactory gameFactory;
 	
 	private Collection<PieceLocationDescriptor> redConfiguration;
 	private Collection<PieceLocationDescriptor> blueConfiguration;
 	
 	private static List<PieceType> betaPieces;
 	ArrayList<PieceType> losingPieces;
 
 	
 	
 	@BeforeClass
 	public static void oneTimeSetup() throws StrategyException
 	{
 		gameFactory = StrategyGameFactory.getInstance();
 		
 		betaPieces = new ArrayList<PieceType>();
 		betaPieces.add(PieceType.MARSHAL);
 		betaPieces.add(PieceType.COLONEL);
 		betaPieces.add(PieceType.CAPTAIN);
 		betaPieces.add(PieceType.LIEUTENANT);
 		betaPieces.add(PieceType.SERGEANT);
 		betaPieces.add(PieceType.FLAG);
 	}
 	
 	@Before
 	public void setup(){
 		redConfiguration = new ArrayList<PieceLocationDescriptor>();
 		blueConfiguration = new ArrayList<PieceLocationDescriptor>();
 	
 		redConfiguration = new ArrayList<PieceLocationDescriptor>();
 		blueConfiguration = new ArrayList<PieceLocationDescriptor>();
 		
 //		addToConfiguration(FLAG, RED, 0, 1);
 		addToConfiguration(FLAG, RED, 0, 0);
 		addToConfiguration(COLONEL, RED, 1, 0);
 		addToConfiguration(COLONEL, RED, 2, 0);
 		addToConfiguration(CAPTAIN, RED, 3, 0);
 		addToConfiguration(CAPTAIN, RED, 4, 0);
 		addToConfiguration(LIEUTENANT, RED, 5, 0);
 		addToConfiguration(LIEUTENANT, RED, 1, 1);
 		addToConfiguration(LIEUTENANT, RED, 2, 1);
 		addToConfiguration(SERGEANT, RED, 3, 1);
 		addToConfiguration(SERGEANT, RED, 4, 1);
 		addToConfiguration(SERGEANT, RED, 5, 1);
 		
 		addToConfiguration(FLAG, BLUE, 5, 4);
 		addToConfiguration(MARSHAL, BLUE, 0, 5);
 		addToConfiguration(COLONEL, BLUE, 1, 5);
 		addToConfiguration(COLONEL, BLUE, 2, 5);
 		addToConfiguration(CAPTAIN, BLUE, 3, 5);
 		addToConfiguration(CAPTAIN, BLUE, 4, 5);
 		addToConfiguration(LIEUTENANT, BLUE, 5, 5);
 		addToConfiguration(LIEUTENANT, BLUE, 1, 4);
 		addToConfiguration(LIEUTENANT, BLUE, 2, 4);
 		addToConfiguration(SERGEANT, BLUE, 3, 4);
 		addToConfiguration(SERGEANT, BLUE, 4, 4);
 //		addToConfiguration(SERGEANT, BLUE, 0, 4);
 		
 		losingPieces = new ArrayList<PieceType>();
 		
 		game = null;
 	}
 	
 	@Test
 	public void testDraws() throws StrategyException{
 		for (PieceType piece: betaPieces){
 			testDrawHelper(piece);
 		}
 	}
 	
 	@Test
 	public void testEmptySpace() throws StrategyException{
 		for (PieceType piece: PieceType.values()){
 			testMoveIntoEmptySpace(piece);
 		}	
 	}
 	
 	@Test
 	public void testMarshal() throws StrategyException{
 		losingPieces.add(PieceType.COLONEL);
 		losingPieces.add(PieceType.CAPTAIN);
 		losingPieces.add(PieceType.LIEUTENANT);
 		losingPieces.add(PieceType.SERGEANT);
 		losingPieces.add(PieceType.FLAG);
 
 		for (PieceType piece : losingPieces){
 			testRedWinsHelper(PieceType.MARSHAL, piece);
 			testBlueWinsHelper(PieceType.MARSHAL, piece);
 		}
 	}
 	
 	@Test
 	public void testColonel() throws StrategyException{
 		losingPieces.add(PieceType.CAPTAIN);
 		losingPieces.add(PieceType.LIEUTENANT);
 		losingPieces.add(PieceType.SERGEANT);
 		losingPieces.add(PieceType.FLAG);
 
 		for (PieceType piece : losingPieces){
 			testRedWinsHelper(PieceType.COLONEL, piece);
 			testBlueWinsHelper(PieceType.COLONEL, piece);
 		}
 	}
 	
 	@Test
 	public void testCaptain() throws StrategyException{
 		losingPieces.add(PieceType.LIEUTENANT);
 		losingPieces.add(PieceType.SERGEANT);
 		losingPieces.add(PieceType.FLAG);
 
 		for (PieceType piece : losingPieces){
 			testRedWinsHelper(PieceType.CAPTAIN, piece);
 			testBlueWinsHelper(PieceType.CAPTAIN, piece);
 		}
 	}
 	
 	@Test
 	public void testLieutenant() throws StrategyException{
 		losingPieces.add(PieceType.SERGEANT);
 		losingPieces.add(PieceType.FLAG);
 
 		for (PieceType piece : losingPieces){
 			testRedWinsHelper(PieceType.LIEUTENANT, piece);
 			testBlueWinsHelper(PieceType.LIEUTENANT, piece);
 		}
 	}
 	
 	@Test
 	public void testSergant() throws StrategyException{
 		losingPieces.add(PieceType.FLAG);
 
 		for (PieceType piece : losingPieces){
 			testRedWinsHelper(PieceType.SERGEANT, piece);
 			testBlueWinsHelper(PieceType.LIEUTENANT, piece);
 		}
 	}
 	
 	/**
 	 * Helper method to reduce redundant code
 	 * @throws StrategyException 
 	 * @params piece PieceType to test draw with
 	 */
 	private void testDrawHelper(PieceType piece) throws StrategyException{
 		addToConfiguration(piece, RED, 0, 1);
 		addToConfiguration(piece, BLUE, 0, 4);
 
 		game = gameFactory.makeBetaStrategyGame(redConfiguration, blueConfiguration);
 		game.startGame();
 		
 		game.move(piece, new Location2D(0,1), new Location2D(0,2));
 		game.move(piece, new Location2D(0,4), new Location2D(0,3));
 		
 		MoveResult moveResult = game.move(piece, new Location2D(0,2), new Location2D(0,3));
 		assertEquals("Expected <" + MoveResultStatus.OK + "> with piece type <" + piece + ">!", MoveResultStatus.OK, moveResult.getStatus());
		assertNull("Expected null BattleWinner with piece type <" + piece + ">!", moveResult.getBattleWinner());
 	
 		//Reset 
 		setup();
 	}
 	
 	/**
 	 * Helper method to reduce redundant code
 	 * @throws StrategyException 
 	 * @params winningPiece PieceType that is expected to win
 	 * @params losingPiece PieceType that is expected to lose
 	 */
 	private void testRedWinsHelper(PieceType winningPiece, PieceType losingPiece) throws StrategyException{
 		addToConfiguration(winningPiece, RED, 0, 1);
 		addToConfiguration(losingPiece, BLUE, 0, 4);
 
 		game = gameFactory.makeBetaStrategyGame(redConfiguration, blueConfiguration);
 		game.startGame();
 		
 		game.move(winningPiece, new Location2D(0,1), new Location2D(0,2));
 		game.move(losingPiece, new Location2D(0,4), new Location2D(0,3));
 		
 		MoveResult moveResult = game.move(winningPiece, new Location2D(0,2), new Location2D(0,3));
 		assertEquals("Expected <" + MoveResultStatus.OK + "> with piece types <" + winningPiece + "> and <" + losingPiece +">!", MoveResultStatus.OK, moveResult.getStatus());
 		assertEquals("Expected BattleWinner with piece type <" + winningPiece + ">!", winningPiece, moveResult.getBattleWinner().getPiece().getType());
 	
 		//Reset 
 		setup();
 	}
 	
 	/**
 	 * Helper method to reduce redundant code
 	 * @throws StrategyException 
 	 * @params winningPiece PieceType that is expected to win
 	 * @params losingPiece PieceType that is expected to lose
 	 */
 	private void testBlueWinsHelper(PieceType winningPiece, PieceType losingPiece) throws StrategyException{
 		addToConfiguration(winningPiece, RED, 0, 1);
 		addToConfiguration(losingPiece, BLUE, 0, 4);
 
 		game = gameFactory.makeBetaStrategyGame(redConfiguration, blueConfiguration);
 		game.startGame();
 		
 		game.move(winningPiece, new Location2D(0,1), new Location2D(0,2));
 		game.move(losingPiece, new Location2D(0,4), new Location2D(0,3));
 		
 		MoveResult moveResult = game.move(winningPiece, new Location2D(0,2), new Location2D(0,3));
 		assertEquals("Expected <" + MoveResultStatus.OK + "> with piece types <" + winningPiece + "> and <" + losingPiece +">!", MoveResultStatus.OK, moveResult.getStatus());
 		assertEquals("Expected BattleWinner with piece type <" + winningPiece + ">!", winningPiece, moveResult.getBattleWinner().getPiece().getType());
 		
 		//Reset 
 		setup();
 	}
 	
 	/**
 	 * Helper method to reduce redundant code
 	 * @param piece PieceType to test
 	 * @throws StrategyException
 	 */
 	private void testMoveIntoEmptySpace(PieceType piece) throws StrategyException{
 		addToConfiguration(piece, RED, 0, 1);
 		addToConfiguration(piece, BLUE, 0, 4);
 
 		game = gameFactory.makeBetaStrategyGame(redConfiguration, blueConfiguration);
 		game.startGame();
 		
 		MoveResult moveResult = game.move(piece, new Location2D(0,1), new Location2D(0,2));
 		assertEquals("Expected <" + MoveResultStatus.OK + "> with piece type <" + piece + ">!", MoveResultStatus.OK, moveResult.getStatus());
 		assertNull("Expected null BattleWinner with piece type <" + piece + ">!", moveResult.getBattleWinner());
 		
 		//Reset 
 		setup();
 	}
 	
 	// Helper methods
 	private void addToConfiguration(PieceType type, PlayerColor color, int x, int y)
 	{
 		final PieceLocationDescriptor confItem = new PieceLocationDescriptor(
 				new Piece(type, color),
 				new Location2D(x, y));
 		if (color == PlayerColor.RED) {
 			redConfiguration.add(confItem);
 		} else {
 			blueConfiguration.add(confItem);
 		}
 	}
 }

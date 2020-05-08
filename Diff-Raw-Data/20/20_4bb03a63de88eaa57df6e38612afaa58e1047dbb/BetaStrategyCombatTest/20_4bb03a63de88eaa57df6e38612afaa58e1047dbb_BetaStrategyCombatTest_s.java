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
 import static strategy.common.PlayerColor.BLUE;
 import static strategy.common.PlayerColor.RED;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import strategy.common.PlayerColor;
 import strategy.common.StrategyException;
 import strategy.game.common.Location2D;
 import strategy.game.common.Piece;
 import strategy.game.common.PieceLocationDescriptor;
 import strategy.game.common.PieceType;
 import strategy.game.version.beta.combat.BetaCombatRuleSet;
 import strategy.game.version.combat.CombatResult;
 import strategy.game.version.combat.CombatRuleSet;
 
 /**
  * All tests relating to Beta Strategy and Combat
  * @author rjsmieja, jrspicola
  * @version Sep 11, 2013
  */
 
 public class BetaStrategyCombatTest{
 	private CombatRuleSet combatRules;
 	
 	private static List<PieceType> betaPieces;
 	private final static PieceType notBetaPiece = PieceType.SPY;
 	private ArrayList<PieceType> piecesToLoseAgainst;
 	private ArrayList<PieceType> piecesToWinAgainst;
 	
 	@BeforeClass
 	public static void oneTimeSetup() throws StrategyException
 	{
 		betaPieces = new ArrayList<PieceType>();
 		betaPieces.add(PieceType.MARSHAL);
 		betaPieces.add(PieceType.COLONEL);
 		betaPieces.add(PieceType.CAPTAIN);
 		betaPieces.add(PieceType.LIEUTENANT);
 		betaPieces.add(PieceType.SERGEANT);
 //		betaPieces.add(PieceType.FLAG);
 	}
 	
 	@Before
 	public void setup(){
 		combatRules = new BetaCombatRuleSet();
 		
 		piecesToWinAgainst = new ArrayList<PieceType>();
 		piecesToLoseAgainst = new ArrayList<PieceType>();
 	}
 	
 	@Test
 	public void testNonBetaCombat() throws StrategyException{
 		for (PieceType piece: betaPieces){
 			testNonBetaCombat(piece);
 		}
 		
 		try{
 			testCombat(notBetaPiece, RED, PieceType.LIEUTENANT, BLUE);
 		} catch (StrategyException e){
 			assertEquals("Piece is not meant for Beta", e.getMessage());
 		}
 	}
 	
 //	@Test
 //	public void testDraws() throws StrategyException{
 //		for (PieceType piece: betaPieces){
 //			testDrawCombatHelper(piece);
 //		}
 //	}
 	
 	//TODO move into moveTest?
 //	@Test
 //	public void testEmptySpace() throws StrategyException{
 //		for (PieceType piece: betaPieces){
 //			testMoveIntoEmptySpace(piece);
 //		}	
 //	}
 	
 	@Test
 	public void testMarshalWins() throws StrategyException{
 		piecesToWinAgainst.add(PieceType.COLONEL);
 		piecesToWinAgainst.add(PieceType.CAPTAIN);
 		piecesToWinAgainst.add(PieceType.LIEUTENANT);
 		piecesToWinAgainst.add(PieceType.SERGEANT);
 
 		for (PieceType piece : piecesToWinAgainst){
 			testCombat(PieceType.MARSHAL, RED, piece, BLUE);
 			testCombat(PieceType.MARSHAL, BLUE, piece, RED);
 		}
 	}
 	
 	@Test
 	public void testMarshalLosses() throws StrategyException{
 		for (PieceType piece : piecesToLoseAgainst){
 			testCombat(piece, RED, PieceType.MARSHAL, BLUE);
 			testCombat(piece, BLUE, PieceType.MARSHAL, RED);
 		}
 	}
 	
 	@Test
 	public void testColonelWins() throws StrategyException{
 		piecesToWinAgainst.add(PieceType.CAPTAIN);
 		piecesToWinAgainst.add(PieceType.LIEUTENANT);
 		piecesToWinAgainst.add(PieceType.SERGEANT);
 		
 		for (PieceType piece : piecesToWinAgainst){
 			testCombat(PieceType.COLONEL, RED, piece, BLUE);
 			testCombat(PieceType.COLONEL, BLUE, piece, RED);
 		}
 	}
 	
 	@Test
 	public void testColonelLosses() throws StrategyException{
 		piecesToLoseAgainst.add(PieceType.MARSHAL);
 
 		for (PieceType piece : piecesToLoseAgainst){
 			testCombat(piece, RED, PieceType.COLONEL, BLUE);
 			testCombat(piece, BLUE, PieceType.COLONEL, RED);
 		}
 	}
 	
 	@Test
 	public void testCaptainWins() throws StrategyException{
 		piecesToWinAgainst.add(PieceType.LIEUTENANT);
 		piecesToWinAgainst.add(PieceType.SERGEANT);
 
 		for (PieceType piece : piecesToWinAgainst){
 			testCombat(PieceType.CAPTAIN, RED, piece, BLUE);
 			testCombat(PieceType.CAPTAIN, BLUE, piece, RED);
 		}
 	}
 	
 	@Test
 	public void testCaptainLosses() throws StrategyException{
 		piecesToLoseAgainst.add(PieceType.MARSHAL);
 		piecesToLoseAgainst.add(PieceType.COLONEL);
 
 		for (PieceType piece : piecesToLoseAgainst){
 			testCombat(piece, RED, PieceType.CAPTAIN, BLUE);
 			testCombat(piece, BLUE, PieceType.CAPTAIN, RED);
 		}
 	}
 	
 	@Test
 	public void testLieutenantWins() throws StrategyException{
 		piecesToWinAgainst.add(PieceType.SERGEANT);
 
 		for (PieceType piece : piecesToWinAgainst){
 			testCombat(PieceType.LIEUTENANT, RED, piece, BLUE);
 			testCombat(PieceType.LIEUTENANT, BLUE, piece, RED);
 		}
 	}
 	
 	@Test
 	public void testLieutenantLosses() throws StrategyException{
 		piecesToLoseAgainst.add(PieceType.MARSHAL);
 		piecesToLoseAgainst.add(PieceType.COLONEL);
 		piecesToLoseAgainst.add(PieceType.CAPTAIN);
 		
 		for (PieceType piece : piecesToLoseAgainst){
 			testCombat(piece, RED, PieceType.LIEUTENANT, BLUE);
 			testCombat(piece, BLUE, PieceType.LIEUTENANT, RED);
 		}
 	}
 	
 	@Test
 	public void testSergantWins() throws StrategyException{
 		for (PieceType piece : piecesToWinAgainst){
 			testCombat(PieceType.SERGEANT, RED, piece, BLUE);
 			testCombat(PieceType.SERGEANT, BLUE, piece, RED);
 		}
 	}
 	
 	@Test
 	public void testSergantLosses() throws StrategyException{
 		piecesToLoseAgainst.add(PieceType.MARSHAL);
 		piecesToLoseAgainst.add(PieceType.COLONEL);
 		piecesToLoseAgainst.add(PieceType.CAPTAIN);
 		piecesToLoseAgainst.add(PieceType.LIEUTENANT);
 		
 		for (PieceType piece : piecesToLoseAgainst){
 			testCombat(piece, RED, PieceType.SERGEANT, BLUE);
 			testCombat(piece, BLUE, PieceType.SERGEANT, RED);
 		}
 	}
 	
 	//TODO move into MoveTest?
 //	@Test
 //	public void testRedWinsGame() throws StrategyException{
 //		for (PieceType piece : betaPieces){
 //			testRedWinsGameHelper(piece);
 //		}
 //	}
 //	
 //	@Test
 //	public void testBlueWinsGame() throws StrategyException{
 //		for (PieceType piece : betaPieces){
 //			testBlueWinsGameHelper(piece);
 //		}
 //	}
 	
 //	@Test
 //	public void testFlag() throws StrategyException{
 //		addToConfiguration(PieceType.FLAG, RED, 0, 1);
 //		addToConfiguration(PieceType.FLAG, BLUE, 0, 4);
 //
 //		game = gameFactory.makeBetaStrategyGame(redConfiguration, blueConfiguration);
 //		game.startGame();
 //		
 //		game.move(PieceType.FLAG, new Location2D(0,1), new Location2D(0,2));
 //		game.move(PieceType.FLAG, new Location2D(0,4), new Location2D(0,3));
 //		
 //		try{
 //			MoveResult moveResult = game.move(PieceType.FLAG, new Location2D(0,2), new Location2D(0,3));
 //			fail("Exception not thrown!");
 //		} catch (StrategyException e){
 //			assertEquals("Piece cannot attack", e.getMessage());
 //		}
 //	}
 	
 	// Helper methods
 	
 	private void testCombat(PieceType winningPiece, PlayerColor winningPlayer, PieceType losingPiece, PlayerColor losingPlayer) throws StrategyException{
 		
 		PieceLocationDescriptor winner = new PieceLocationDescriptor(new Piece(winningPiece, winningPlayer), new Location2D(0,0));
 		PieceLocationDescriptor loser = new PieceLocationDescriptor(new Piece(losingPiece, losingPlayer), new Location2D(0,1));
 
 		CombatResult combatResult = combatRules.doCombat(winner, loser);
 		
		assertEquals(winner, combatResult.getWinningPiece());
		assertEquals(loser, combatResult.getLosingPiece());
 	}
 	
 	private void testNonBetaCombat(PieceType piece) throws StrategyException{
 		try{
 			testCombat(piece, RED, notBetaPiece, BLUE);
 		} catch (StrategyException e){
 			assertEquals("Piece is not meant for Beta", e.getMessage());
 		}
 	}
 //	
 //	/**
 //	 * Helper method to reduce redundant code
 //	 * @param piece PieceType to test
 //	 * @throws StrategyException
 //	 */
 //	private void testMoveIntoEmptySpace(PieceType piece) throws StrategyException{
 //		addToConfiguration(piece, RED, 0, 1);
 //		addToConfiguration(piece, BLUE, 0, 4);
 //
 //		game = gameFactory.makeBetaStrategyGame(redConfiguration, blueConfiguration);
 //		game.startGame();
 //		
 //		MoveResult moveResult = game.move(piece, new Location2D(0,1), new Location2D(0,2));
 //		assertEquals("Expected <" + MoveResultStatus.OK + "> with piece type <" + piece + ">!", MoveResultStatus.OK, moveResult.getStatus());
 //		assertNull("Expected null BattleWinner with piece type <" + piece + ">!", moveResult.getBattleWinner());
 //		
 //		//Reset 
 //		setup();
 //	}
 //	
 	/**
 	 * @throws StrategyException 
 	 * 
 	 */
 //	
 //	private void testRedWinsGameHelper(PieceType piece) throws StrategyException{
 //		final PlayerColor winner = PlayerColor.RED;
 //		final PlayerColor loser = PlayerColor.BLUE;
 //		
 //		addToConfiguration(piece, winner, 0, 1);
 //		addToConfiguration(PieceType.FLAG, loser, 0, 4);
 //
 //		Piece losingPiece = new Piece(PieceType.FLAG, loser);
 //		Piece winningPiece = new Piece(piece, winner);
 //		
 //		game = gameFactory.makeBetaStrategyGame(redConfiguration, blueConfiguration);
 //		game.startGame();
 //		
 //		game.move(piece, new Location2D(0,1), new Location2D(0,2));
 //		game.move(PieceType.LIEUTENANT, new Location2D(2,4), new Location2D(2,3));
 //		game.move(piece, new Location2D(0,2), new Location2D(0,3));
 //		game.move(PieceType.LIEUTENANT, new Location2D(1,4), new Location2D(1,3));
 //		
 //		MoveResultStatus expectedMoveResult = MoveResultStatus.valueOf(winner + "_WINS");
 //		
 //		MoveResult moveResult = game.move(piece, new Location2D(0,3), new Location2D(0,4));
 //		assertEquals("Expected <" + expectedMoveResult + "> with piece types <" + winningPiece + "> and <" + losingPiece +">!", expectedMoveResult, moveResult.getStatus());
 //		assertEquals("Expected BattleWinner with piece type <" + piece + ">!", piece, moveResult.getBattleWinner().getPiece().getType());
 //		
 //		//Reset 
 //		setup();
 //	}
 //	
 //	/**
 //	 * @throws StrategyException 
 //	 * 
 //	 */
 //	
 //	private void testBlueWinsGameHelper(PieceType piece) throws StrategyException{
 //		final PlayerColor winner = PlayerColor.BLUE;
 //		final PlayerColor loser = PlayerColor.RED;
 //
 //		Piece losingPiece = new Piece(PieceType.FLAG, winner);
 //		Piece winningPiece = new Piece(piece, loser);
 //		
 //		game.move(PieceType.LIEUTENANT, new Location2D(1,1), new Location2D(1,2));
 //		game.move(piece, new Location2D(0,4), new Location2D(0,3));
 //		game.move(PieceType.LIEUTENANT, new Location2D(2,1), new Location2D(2,2));
 //		game.move(piece, new Location2D(0,3), new Location2D(0,2));
 //		game.move(PieceType.LIEUTENANT, new Location2D(1,2), new Location2D(1,1));
 //		
 //		MoveResultStatus expectedMoveResult = MoveResultStatus.valueOf(winner + "_WINS");
 //		
 //		MoveResult moveResult = game.move(piece, new Location2D(0,2), new Location2D(0,1));
 //
 //		assertEquals("Expected <" + expectedMoveResult + "> with piece types <" + winningPiece + "> and <" + losingPiece +">!", expectedMoveResult, moveResult.getStatus());
 //		assertEquals("Expected BattleWinner with piece type <" + piece + ">!", piece, moveResult.getBattleWinner().getPiece().getType());
 //		
 //		//Reset 
 //		setup();
 //	}
 }

 /*******************************************************************************
  * This files was developed for CS4233: Object-Oriented Analysis & Design.
  * The course was taken at Worcester Polytechnic Institute.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package strategy.game.version.epsilon;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import strategy.common.PlayerColor;
 import strategy.common.StrategyException;
 import strategy.game.StrategyGameController;
 import strategy.game.StrategyGameFactory;
 import strategy.game.common.DetailedMoveResult;
 import strategy.game.common.GameVersion;
 import strategy.game.common.Location;
 import strategy.game.common.Location1D;
 import strategy.game.common.Location2D;
 import strategy.game.common.Location3D;
 import strategy.game.common.MoveResult;
 import strategy.game.common.MoveResultStatus;
 import strategy.game.common.Piece;
 import strategy.game.common.PieceLocationDescriptor;
 import strategy.game.common.PieceType;
 import strategy.game.common.StrategyGameObserver;
import strategy.game.reporter.LazyStrategyGameReporter;
 import strategy.game.version.delta.DeltaRules;
 
 /** These are the tests for Epsilon Strategy
  * 
  * @author Dabrowski  
  * @version $Revision: 1.0 $
  */
 public class EpsilonStrategyGameControllerTest {
 
 	// The game!
 	private  StrategyGameController game;
 
 	/*
 	 * The board with the initial configuration looks like this:
 	 *   |  0  |  1  |  2  |  3  |  4  |  5  |
 	 * - +-----+-----+-----+-----+-----+-----+
 	 * 5 | COL2| LT2 | SGT1| LT3 | CPT2| MAR |
 	 * - +-----+-----+-----+-----+-----+-----+
 	 * 4 | SGT3| LT1 | COL1|  F  | CPT1| SGT2|     BLUE
 	 * - +-----+-----+-----+-----+-----+-----+
 	 * 3 |     |     |  X  | X   |     |     |
 	 * - +-----+-----+-----+-----+-----+-----+
 	 * 2 |     |     |  X  | X   |     |     |
 	 * - +-----+-----+-----+-----+-----+-----+
 	 * 1 |  F  | MAR | COL1| CPT2|LT 3 | SGT3|
 	 * - +-----+-----+-----+-----+-----+-----+
 	 * 0 | LT 1| LT 2| CPT1| SGT1| COL2| SGT2|     RED
 	 * - +-----+-----+-----+-----+-----+-----+
 	 *   |  0  |  1  |  2  |  3  |  4  |  5  |
 	 */
 
 	// Red Pieces
 	private final Piece redFlag = new Piece(PieceType.FLAG, PlayerColor.RED);
 	private final Piece redMarshal = new Piece(PieceType.MARSHAL, PlayerColor.RED);
 	private final Piece redColonel = new Piece(PieceType.COLONEL, PlayerColor.RED);
 	private final Piece redCaptain = new Piece(PieceType.CAPTAIN, PlayerColor.RED);
 	private final Piece redLieutenant = new Piece(PieceType.LIEUTENANT, PlayerColor.RED);
 	private final Piece redSergeant = new Piece(PieceType.SERGEANT, PlayerColor.RED);
 	private final Piece redGeneral = new Piece(PieceType.GENERAL, PlayerColor.RED);
 	private final Piece redMiner = new Piece(PieceType.MINER, PlayerColor.RED);
 	private final Piece redScout = new Piece(PieceType.SCOUT, PlayerColor.RED);
 	private final Piece redBomb = new Piece(PieceType.BOMB, PlayerColor.RED);
 	private final Piece redSpy = new Piece(PieceType.SPY, PlayerColor.RED);
 	private final Piece redMajor = new Piece(PieceType.MAJOR, PlayerColor.RED);
 
 
 	// Blue Pieces
 	private final Piece blueFlag = new Piece(PieceType.FLAG, PlayerColor.BLUE);
 	private final Piece blueMarshal = new Piece(PieceType.MARSHAL, PlayerColor.BLUE);
 	private final Piece blueColonel = new Piece(PieceType.COLONEL, PlayerColor.BLUE);
 	private final Piece blueCaptain = new Piece(PieceType.CAPTAIN, PlayerColor.BLUE);
 	private final Piece blueLieutenant = new Piece(PieceType.LIEUTENANT, PlayerColor.BLUE);
 	private final Piece blueSergeant = new Piece(PieceType.SERGEANT, PlayerColor.BLUE);
 	private final Piece blueGeneral = new Piece(PieceType.GENERAL, PlayerColor.BLUE);
 	private final Piece blueMiner = new Piece(PieceType.MINER, PlayerColor.BLUE);
 	private final Piece blueScout = new Piece(PieceType.SCOUT, PlayerColor.BLUE);
 	private final Piece blueBomb = new Piece(PieceType.BOMB, PlayerColor.BLUE);
 	private final Piece blueSpy = new Piece(PieceType.SPY, PlayerColor.BLUE);
 	private final Piece blueMajor = new Piece(PieceType.MAJOR, PlayerColor.BLUE);
 
 
 	// Choke point
 	private final Piece chokePoint = new Piece(PieceType.CHOKE_POINT, null);
 
 	//Pieces + locations
 	private final PieceLocationDescriptor redFlagAtLocation = new PieceLocationDescriptor( redFlag, L(0,0));
 	private final PieceLocationDescriptor blueFlagAtLocation = new PieceLocationDescriptor( blueFlag, L(0,9));
 
 	/** the strategy game factory */
 	private final StrategyGameFactory factory = StrategyGameFactory.getInstance();
 
 	/** Configuration for the red team */
 	private  List<PieceLocationDescriptor> validRedConfiguration = new LinkedList<PieceLocationDescriptor>();
 	/** Configuration for the blue team */
 	private  List<PieceLocationDescriptor> validBlueConfiguration = new LinkedList<PieceLocationDescriptor>();
 	/** a bad configuration for testing purposes */
 	private  List<PieceLocationDescriptor> badConfig = new LinkedList<PieceLocationDescriptor>();
 	/** A game nearly over */
 	private  List<PieceLocationDescriptor> endConfig = new LinkedList<PieceLocationDescriptor>();
 
 	/** a test double for a game */
 	private EpsilonStrategyGameControllerTestDouble EpsilonTestDouble;
 
 	/** Observers */
 	private static Collection<StrategyGameObserver> observers = new LinkedList<StrategyGameObserver>();
 	
 	@BeforeClass
 	public static void setupBefore(){
 		@SuppressWarnings("unused")
 		GameVersion gameVersion = (GameVersion.EPSILON);
 		
 		// Set up observer
		observers.add(new LazyStrategyGameReporter());
 	}
 
 	/**
 	 * Method setup.
 	 * @throws StrategyException
 	 */
 	@Before
 	public void setup() throws StrategyException{
 		validBlueConfiguration = new LinkedList<PieceLocationDescriptor>();
 		validRedConfiguration = new LinkedList<PieceLocationDescriptor>();
 
 		// Generate the blue configuration
 		validBlueConfiguration.add( blueFlagAtLocation ); // 9,9
 		addToConfiguration(blueMarshal,  0,  8);
 		addToConfiguration(blueGeneral,  0,  7);
 		addToConfiguration(blueColonel,  0,  6);
 		addToConfiguration(blueColonel,  1,  9);
 		addToConfiguration(blueMajor,    1,  8);
 		addToConfiguration(blueMajor,    1,  7);
 		addToConfiguration(blueMajor,    1,  6);
 		addToConfiguration(blueCaptain,  2,  9);
 		addToConfiguration(blueCaptain,  2,  8);
 		addToConfiguration(blueCaptain,  2,  7);
 		addToConfiguration(blueCaptain,  2,  6);
 		addToConfiguration(blueLieutenant,  3,  9);
 		addToConfiguration(blueLieutenant,  3,  8);
 		addToConfiguration(blueLieutenant,  3,  7);
 		addToConfiguration(blueLieutenant,  3,  6);
 		addToConfiguration(blueSergeant,  4,  9);
 		addToConfiguration(blueSergeant,  4,  8);
 		addToConfiguration(blueSergeant,  4,  7);
 		addToConfiguration(blueSergeant,  4,  6);
 		addToConfiguration(blueMiner,  5,  9);
 		addToConfiguration(blueMiner,  5,  8);
 		addToConfiguration(blueMiner,  5,  7);
 		addToConfiguration(blueMiner,  5,  6);
 		addToConfiguration(blueMiner,  6,  9);
 		addToConfiguration(blueScout,  6,  8);
 		addToConfiguration(blueScout,  6,  7);
 		addToConfiguration(blueScout,  6,  6);
 		addToConfiguration(blueScout,  7,  9);
 		addToConfiguration(blueScout,  7,  8);
 		addToConfiguration(blueScout,  7,  7);
 		addToConfiguration(blueScout,  7,  6);
 		addToConfiguration(blueScout,  8,  9);
 		addToConfiguration(blueSpy,    8,  8);
 		addToConfiguration(blueBomb,  8,  7);
 		addToConfiguration(blueBomb,  8,  6);
 		addToConfiguration(blueBomb,  9,  9);
 		addToConfiguration(blueBomb,  9,  8);
 		addToConfiguration(blueBomb,  9,  7);
 		addToConfiguration(blueBomb,  9,  6);	
 
 		// Generate the red configuration
 		validRedConfiguration.add(  redFlagAtLocation ); // 0,0
 		addToConfiguration( redMarshal,  0,  1);
 		addToConfiguration( redGeneral,  0,  2);
 		addToConfiguration( redColonel,  0,  3);
 		addToConfiguration( redColonel,  1,  0);
 		addToConfiguration( redMajor,    1,  1);
 		addToConfiguration( redMajor,    1,  2);
 		addToConfiguration( redMajor,    1,  3);
 		addToConfiguration( redCaptain,  2,  0);
 		addToConfiguration( redCaptain,  2,  1);
 		addToConfiguration( redCaptain,  2,  2);
 		addToConfiguration( redCaptain,  2,  3);
 		addToConfiguration( redLieutenant,  3,  0);
 		addToConfiguration( redLieutenant,  3,  1);
 		addToConfiguration( redLieutenant,  3,  2);
 		addToConfiguration( redLieutenant,  3,  3);
 		addToConfiguration( redSergeant,  4,  0);
 		addToConfiguration( redSergeant,  4,  1);
 		addToConfiguration( redSergeant,  4,  2);
 		addToConfiguration( redSergeant,  4,  3);
 		addToConfiguration( redMiner,  5,  0);
 		addToConfiguration( redMiner,  5,  1);
 		addToConfiguration( redMiner,  5,  2);
 		addToConfiguration( redMiner,  5,  3);
 		addToConfiguration( redMiner,  6,  0);
 		addToConfiguration( redScout,  6,  1);
 		addToConfiguration( redScout,  6,  2);
 		addToConfiguration( redScout,  6,  3);
 		addToConfiguration( redScout,  7,  0);
 		addToConfiguration( redScout,  7,  1);
 		addToConfiguration( redScout,  7,  2);
 		addToConfiguration( redScout,  7,  3);
 		addToConfiguration( redScout,  8,  0);
 		addToConfiguration( redSpy,    8,  1);
 		addToConfiguration( redBomb,  8,  2);
 		addToConfiguration( redBomb,  8,  3);
 		addToConfiguration( redBomb,  9,  0);
 		addToConfiguration( redBomb,  9,  1);
 		addToConfiguration( redBomb,  9,  2);
 		addToConfiguration( redBomb,  9,  3);		
 
 		// Fresh valid game
 		game = factory.makeEpsilonStrategyGame(validRedConfiguration, validBlueConfiguration, observers);
 		game.startGame();
		((EpsilonStrategyGameController) game).register(new LazyStrategyGameReporter());
		((EpsilonStrategyGameController) game).unregister(new LazyStrategyGameReporter());
 
 		// For general use
 		badConfig = new LinkedList<PieceLocationDescriptor>();
 		EpsilonTestDouble = new EpsilonStrategyGameControllerTestDouble(validRedConfiguration, validBlueConfiguration, new DeltaRules(), observers);
 		EpsilonTestDouble.startGame();
 
 		// make a configuration that simulates the end of a full game
 		endConfig.add(new PieceLocationDescriptor( blueFlag,    L(0,0)));
 		endConfig.add(new PieceLocationDescriptor( redMarshal,  L(0,1)));	
 		endConfig.add(new PieceLocationDescriptor( blueMarshal, L(0,2)));
 		endConfig.add(new PieceLocationDescriptor( redFlag,     L(5,3)));
 	}
 
 	/////////// TESTING makeEpsilonStrategyGame -- number of pieces
 	/**
 	 * Method controllerTakesValidConfigurations.
 	 * @throws StrategyException
 	 */
 	@Test
 	public void controllerTakesValidConfigurations() throws StrategyException
 	{
 		game = factory.makeEpsilonStrategyGame(validRedConfiguration, validBlueConfiguration, observers);
 		assertTrue(true); // No exceptions thrown!
 	} 
 
 	/**
 	 * Method controllerTakesValidConfigurations.
 	 * @throws StrategyException
 	 */
 	@Test
 	public void controllerHandlesNullObserverList() throws StrategyException
 	{
 		game = factory.makeEpsilonStrategyGame(validRedConfiguration, validBlueConfiguration, null);
 		assertTrue(true); // No exceptions thrown!
 	} 
 	
 	/**
 	 * Method startGameAfterStarting.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void startGameAfterStarting() throws StrategyException{
 		game.startGame();
 	}
 
 	/**
 	 * Method nullRedConfiguration.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void nullRedConfiguration() throws StrategyException{
 		factory.makeEpsilonStrategyGame(null, validBlueConfiguration, observers);
 	}
 
 	/**
 	 * Method noRedFlag.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void noRedFlag() throws StrategyException{
 		validRedConfiguration.remove(redFlagAtLocation);
 		addToConfiguration(PieceType.MARSHAL, PlayerColor.BLUE, 0 ,1);
 		factory.makeEpsilonStrategyGame(validRedConfiguration, validBlueConfiguration, observers);
 	}
 
 	/**
 	 * Method testNoBlueFlag.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void testNoBlueFlag() throws StrategyException{
 		validBlueConfiguration.remove(blueFlagAtLocation);
 		addToConfiguration(PieceType.MARSHAL, PlayerColor.BLUE, 3 , 4);
 		factory.makeEpsilonStrategyGame(validRedConfiguration, validBlueConfiguration, observers);
 	}
 
 	/**
 	 * Method toofewPiecesForRedConfigurationButHasFlag.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void toofewPiecesForRedConfigurationButHasFlag() throws StrategyException{
 		badConfig.add(new PieceLocationDescriptor( redFlag, L(0,1)));
 		factory.makeEpsilonStrategyGame(badConfig, validBlueConfiguration, observers);
 	}
 
 	@Test(expected=StrategyException.class)
 	public void redConfigurationHasTooFewItem() throws StrategyException { 
 		validRedConfiguration.remove(0);
 		factory.makeEpsilonStrategyGame(validRedConfiguration, validBlueConfiguration, observers);
 	}
 
 
 	/**
 	 * Method tooManyPiecesForRedConfigurationWithOnlyFlag.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void tooManyPiecesForRedConfigurationWithOnlyFlag() throws StrategyException{
 		for (int i =0;i<15;i++) badConfig.add(new PieceLocationDescriptor( redFlag, L(0,1)));
 		factory.makeEpsilonStrategyGame(badConfig, validBlueConfiguration, observers);
 	}
 
 	/**
 	 * Method nullForBlueConfiguration.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void nullForBlueConfiguration() throws StrategyException{
 		factory.makeEpsilonStrategyGame(validRedConfiguration, null, observers);
 	}
 
 	/**
 	 * Method tooFewPiecesForBlueConfigurationButHasFlag.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void tooFewPiecesForBlueConfigurationButHasFlag() throws StrategyException{
 		badConfig.add(new PieceLocationDescriptor( blueFlag, L(3,4)));
 		factory.makeEpsilonStrategyGame(validRedConfiguration, badConfig, observers);
 	}
 
 	// This exception is thrown for many reasons, but definitely catches invalid number of pieces
 	@Test(expected=StrategyException.class)
 	public void tooManyPiecesForBlueConfigurationWithOnlyFlag() throws StrategyException{
 		for (int i =0;i<42;i++) badConfig.add(new PieceLocationDescriptor( blueFlag, L(i%6 , (i/6) % 10 )));
 		factory.makeEpsilonStrategyGame(validRedConfiguration, badConfig, observers);
 	}
 
 	/**
 	 * Method nullLocation.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void nullLocation() throws StrategyException{
 		badConfig.add(new PieceLocationDescriptor( blueFlag, null));
 		factory.makeEpsilonStrategyGame(validRedConfiguration, badConfig, observers);
 	}
 
 	/**
 	 * Method nullPiece.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void nullPiece() throws StrategyException{
 		badConfig.add(new PieceLocationDescriptor( null, L(3,4)));
 		factory.makeEpsilonStrategyGame(validRedConfiguration, badConfig, observers);
 	}
 
 	/**
 	 * Method nullPieceLocationDescription.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void nullPieceLocationDescription() throws StrategyException{
 		badConfig.add(null);
 		factory.makeEpsilonStrategyGame(validRedConfiguration, badConfig, observers);
 	}
 
 	/**
 	 * Method nullPieceOwner.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void nullPieceOwner() throws StrategyException{
 		final Piece badPice = new Piece(PieceType.BOMB, null);		
 		badConfig.add(new PieceLocationDescriptor( badPice, L(3,4)));
 		factory.makeEpsilonStrategyGame(validRedConfiguration,badConfig, observers );
 	}
 
 	/**
 	 * Method nullPieceType.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void nullPieceType() throws StrategyException{
 		final Piece badPice = new Piece(null, PlayerColor.BLUE);		
 		badConfig.add(new PieceLocationDescriptor( badPice, L(3,4)));
 		factory.makeEpsilonStrategyGame(validRedConfiguration,badConfig, observers );
 	}
 
 	/////////// TESTING makeEpsilonStrategyGame -- locations
 	/**
 	 * Method locationUsedTwice.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void locationUsedTwice() throws StrategyException{
 		addToConfiguration(PieceType.FLAG, PlayerColor.RED, 3 ,1);
 		factory.makeEpsilonStrategyGame(validRedConfiguration, validBlueConfiguration, observers);
 	}
 
 	/**
 	 * Method bluePieceInNeutralZone.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void bluePieceInNeutralZone() throws StrategyException {
 		validBlueConfiguration.remove(blueFlagAtLocation); // remove flag
 		addToConfiguration(PieceType.FLAG, PlayerColor.BLUE, 3 ,4);
 		factory.makeEpsilonStrategyGame(validRedConfiguration, validBlueConfiguration, observers);	
 	}
 
 	/**
 	 * Method redPieceInNeutralZone.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void redPieceInNeutralZone() throws StrategyException {
 		validRedConfiguration.remove(redFlagAtLocation); // remove flag
 		addToConfiguration(PieceType.FLAG, PlayerColor.RED, 3 ,4);
 		factory.makeEpsilonStrategyGame(validRedConfiguration, validBlueConfiguration, observers);	
 	}
 
 	/**
 	 * Method playerFlagsInEnemyZones.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void playerFlagsInEnemyZones() throws StrategyException {
 		validRedConfiguration.remove(redFlagAtLocation); // remove flag
 		validBlueConfiguration.remove(blueFlagAtLocation); // remove flag
 		addToConfiguration(PieceType.FLAG, PlayerColor.BLUE, 0 ,1);
 		addToConfiguration(PieceType.FLAG, PlayerColor.RED, 3 ,6);
 		factory.makeEpsilonStrategyGame(validRedConfiguration, validBlueConfiguration, observers);		
 	}
 
 	/**
 	 * Method pieceOutOfBoundsY.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void pieceOutOfBoundsY() throws StrategyException {
 		validBlueConfiguration.remove(blueFlagAtLocation); // remove flag
 		addToConfiguration(PieceType.FLAG, PlayerColor.BLUE, 0, 12);
 		factory.makeEpsilonStrategyGame(validRedConfiguration, validBlueConfiguration, observers);	
 	}
 
 	/**
 	 * Method pieceOutOfBoundsX.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void pieceOutOfBoundsX() throws StrategyException {
 		validRedConfiguration.remove(redFlagAtLocation); // remove flag
 		addToConfiguration(PieceType.FLAG, PlayerColor.RED, 11 ,4);
 		factory.makeEpsilonStrategyGame(validRedConfiguration, validBlueConfiguration, observers);	
 	}
 
 	/**
 	 * Method pieceInNegativeX.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void pieceInNegativeX() throws StrategyException {
 		validRedConfiguration.remove(redFlagAtLocation); // remove flag
 		addToConfiguration(PieceType.FLAG, PlayerColor.RED, -1 , 0);
 		factory.makeEpsilonStrategyGame(validRedConfiguration, validBlueConfiguration, observers);	
 	}
 
 	/**
 	 * Method pieceInNegativeY.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void pieceInNegativeY() throws StrategyException {
 		validRedConfiguration.remove(redFlagAtLocation); // remove flag
 		addToConfiguration(PieceType.FLAG, PlayerColor.RED,0 ,-1);
 		factory.makeEpsilonStrategyGame(validRedConfiguration, validBlueConfiguration, observers);	
 	}
 
 	/**
 	 * Method pieceInNegativeCoordinates.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void pieceInNegativeCoordinates() throws StrategyException {
 		validRedConfiguration.remove(redFlagAtLocation); // remove flag
 		addToConfiguration(PieceType.FLAG, PlayerColor.RED, -1 , -1);
 		factory.makeEpsilonStrategyGame(validRedConfiguration, validBlueConfiguration, observers);	
 	}
 
 	////// getPieceAt
 	/**
 	 * Method initialPositionHasCorrectREDPiecesPlaced.
 	 */
 	@Test
 	public void getPieceAtCanUseNonLocation2DSometimes(){
 		assertSame(redFlag, game.getPieceAt(new Location3D(0,0,3)));
 		assertTrue(true);// no exception thrown
 	}
 
 	@Test
 	public void getNullWhenUsingInappropriateLocationsWith_getPieceAt(){
 		assertNull(game.getPieceAt(new Location1D(1)));		
 	}
 
 	@Test
 	public void initialPositionHasCorrectREDPiecesPlaced() {
 		final Iterator<PieceLocationDescriptor> redIter = validRedConfiguration.iterator();
 		// Check the entire red configuration
 		while (redIter.hasNext()){
 			PieceLocationDescriptor next = redIter.next();
 			Piece aPiece = next.getPiece();
 			Location aLocation = next.getLocation();
 			assertEquals(aPiece, game.getPieceAt(aLocation));	
 		}				
 	}
 
 	/**
 	 * Method initialPositionHasCorrectBLUEPiecesPlaced.
 	 */
 	@Test
 	public void initialPositionHasCorrectBLUEPiecesPlaced() {
 		final Iterator<PieceLocationDescriptor> blueIter = validBlueConfiguration.iterator();
 		// Check the entire blue configuration
 		while (blueIter.hasNext()){
 			PieceLocationDescriptor next = blueIter.next();
 			Piece aPiece = next.getPiece();
 			Location aLocation = next.getLocation();
 			assertEquals(aPiece, game.getPieceAt(aLocation));	
 		}				
 	}
 
 	/**
 	 * Method getPieceAtIllegalLocations.
 	 */
 	@Test
 	public void getPieceAtIllegalLocations()  {
 		assertEquals(null, game.getPieceAt( L(-1,  0)));	
 		assertEquals(null, game.getPieceAt( L(-1, -1)));	
 		assertEquals(null, game.getPieceAt( L( 0, -1)));	
 		assertEquals(null, game.getPieceAt( L(11,0)));	
 		assertEquals(null, game.getPieceAt( L(0,11)));	
 		assertEquals(null, game.getPieceAt( L(11,11)));
 	}
 
 	/**
 	 * Method getPieceAt_worksAfterTwoMoves.
 	 * @throws StrategyException
 	 */
 	@Test
 	public void getPieceAt_worksAfterTwoMoves() throws StrategyException {
 		game.move(PieceType.COLONEL, L(0,3), L(0,4));
 		game.move(PieceType.COLONEL, L(0,6), L(0,5));
 		assertTrue(true); // have to get this far with moves
 		assertEquals(PieceType.COLONEL, game.getPieceAt(L(0,4)).getType());
 		assertEquals(PieceType.COLONEL, game.getPieceAt(L(0,5)).getType());				
 	}
 
 	@Test
 	public void getPieceAtChokePoints() throws StrategyException{
 		assertSame(chokePoint.getType(), game.getPieceAt(L(3,4)).getType() );
 		assertSame(chokePoint.getType(), game.getPieceAt(L(3,5)).getType() );
 		assertSame(chokePoint.getType(), game.getPieceAt(L(2,4)).getType() );
 		assertSame(chokePoint.getType(), game.getPieceAt(L(2,5)).getType() );
 
 		assertSame(chokePoint.getType(), game.getPieceAt(L(6,4)).getType() );
 		assertSame(chokePoint.getType(), game.getPieceAt(L(7,5)).getType() );
 		assertSame(chokePoint.getType(), game.getPieceAt(L(7,4)).getType() );
 		assertSame(chokePoint.getType(), game.getPieceAt(L(6,5)).getType() );
 	}
 
 
 	//////////// MOVEMENT
 	/**
 	 * Method makeMoveBeforeInitialization.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void makeMoveBeforeInitialization() throws StrategyException	{
 		final StrategyGameController agame = StrategyGameFactory.getInstance().makeEpsilonStrategyGame(validRedConfiguration, validBlueConfiguration, observers);
 		agame.move(PieceType.SERGEANT, L(5,1), L(5,2));
 	}
 
 	/**
 	 * Method moveOffBoardXNegative.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void moveOffBoardXNegative() throws StrategyException {
 		game.move(PieceType.LIEUTENANT, L(0,0), L(-1,0));	
 	}
 
 	/**
 	 * Method moveOffBoardYNegative.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void moveOffBoardYNegative() throws StrategyException {
 		game.move(PieceType.LIEUTENANT, L(0,0), L(0,-1));		
 	}
 
 	/**
 	 * Method moveOffBoardXPositive.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void moveOffBoardXPositive() throws StrategyException {
 		game.move(PieceType.SERGEANT, L(9,0), L(10,0));			
 	}
 
 	/**
 	 * Method moveOffBoardYPositive.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void moveOffBoardYPositive() throws StrategyException {
 		game.move(PieceType.SERGEANT, L(5,1), L(5,2));
 		game.move(PieceType.MARSHAL,  L(5,5), L(5,11));
 	}
 
 	/**
 	 * Method badFromNegativeX.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void badFromNegativeX() throws StrategyException {
 		game.move(PieceType.SERGEANT, L(-1,0), L(5,2));
 	}
 
 	/**
 	 * Method badFromNegativeY.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void badFromNegativeY() throws StrategyException {
 		game.move(PieceType.SERGEANT, L(0,-1), L(5,2));
 	}
 
 	/**
 	 * Method badFromOutOfBoundsX.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void badFromOutOfBoundsX() throws StrategyException {
 		game.move(PieceType.SERGEANT, L(6,0), L(5,2));
 	}
 
 	/**
 	 * Method badFromOutOfBoundsY.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void badFromOutOfBoundsY() throws StrategyException {
 		game.move(PieceType.SERGEANT, L(0,6), L(5,2));
 	}
 
 	/**
 	 * Method moveFlag.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void moveFlag() throws StrategyException {
 		game.move(PieceType.FLAG, L(0,1), L(0,2));
 	}
 
 	/**
 	 * Method moveWrongPiece.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void moveWrongPiece() throws StrategyException {
 		game.move(PieceType.MARSHAL, L(0,1), L(0,2));
 	}
 
 	/**
 	 * Method blueTriesToGoFirst.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void blueTriesToGoFirst() throws StrategyException {
 		game.move(PieceType.SERGEANT, L(5,4), L(5,3));
 	}
 
 	/**
 	 * Method redTriesToGoTwice.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void redTriesToGoTwice() throws StrategyException {
 		game.move(PieceType.SERGEANT, L(5,1), L(5,3));
 		game.move(PieceType.SERGEANT, L(5,3), L(5,3));
 	}
 
 	/**
 	 * Method moveNonAdjacentX.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void moveNonAdjacentX() throws StrategyException {
 		game.move(PieceType.SERGEANT, L(5,1), L(5,2));
 		game.move(PieceType.SERGEANT, L(5,4), L(5,3));
 		assertTrue(true); // have to get this far
 		game.move(PieceType.SERGEANT, L(5,2) , L(0,2));
 	}
 
 	/**
 	 * Method moveNonAdjacentY.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void moveNonAdjacentY() throws StrategyException {
 		game.move(PieceType.SERGEANT, L(5,1), L(5,4));
 	}
 
 	/**
 	 * Method moveDiagonal.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void moveDiagonal() throws StrategyException {
 		game.move(PieceType.SERGEANT, L(5,1), L(4,2));
 	}
 
 	/**
 	 * Method moveFartherThanDiagonalAdjacent.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void moveFartherThanDiagonalAdjacent() throws StrategyException {
 		game.move(PieceType.SERGEANT, L(5,1), L(0,3));
 	}
 
 	/**
 	 * Method redMovesAPieceOntoHisOtherPiece.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void redMovesAPieceOntoHisOtherPiece() throws StrategyException {
 		game.move(PieceType.SERGEANT, L(5,1), L(5,0));
 	}
 
 	@Test(expected=StrategyException.class)
 	public void redMovesOntoChokePoint() throws StrategyException{
 		assertSame(game.move(PieceType.COLONEL, L(2,1), L(2,2)).getStatus(), MoveResultStatus.OK );
 	}
 
 	/**
 	 * Method gameEndsAfterSIXMoves.
 	 * @throws StrategyException
 	 */
 	@Test
 	public void violateMoveRepitionRule() throws StrategyException {
 		game.move(PieceType.COLONEL, L(0,3), L(0,4));
 		game.move(PieceType.COLONEL, L(0,6), L(0,5));
 		game.move(PieceType.COLONEL, L(0,4), L(0,3));
 		game.move(PieceType.COLONEL, L(0,5), L(0,6));
 
 		assertSame(game.move(PieceType.COLONEL, L(0,3), L(0,4)).getStatus(), MoveResultStatus.BLUE_WINS); 
 	}
 
 	@Test(expected=StrategyException.class)
 	public void tryToMoveBomb() throws StrategyException{
 		// Add spies for fights with marshals
 		endConfig.add(new PieceLocationDescriptor( redBomb,  L(3,0)));
 
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);	
 
 		// Move a bomb lolz
 		EpsilonTestDouble.move(PieceType.BOMB, L(3,0) , L(4,0));		
 	}
 
 	@Test
 	public void scoutMovesFar() throws StrategyException{
 		// Add spies for fights with marshals
 		endConfig.add(new PieceLocationDescriptor( redScout,  L(4,0)));
 
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);	
 
 		// Move a spy
 		MoveResult spyMove = EpsilonTestDouble.move(PieceType.SCOUT, L(4,0) , L(4,5));	
 
 		// Validate the move
 		assertSame(spyMove.getBattleWinner().getPiece(), redScout);
 	}
 
 	@Test(expected=StrategyException.class)
 	public void scoutCantJumpChokePoints() throws StrategyException{
 		// Add spies for fights with marshals
 		endConfig.add(new PieceLocationDescriptor( redScout,    L(0,5)));
 		endConfig.add(new PieceLocationDescriptor( chokePoint,  L(2,5)));
 
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);	
 
 		// Move a spy
 		EpsilonTestDouble.move(PieceType.SCOUT, L(0,5) , L(4,5));	
 		fail();
 	}
 
 	@Test(expected=StrategyException.class)
 	public void scoutCantJumpAllies() throws StrategyException{
 		// Add spies for fights with marshals
 		endConfig.add(new PieceLocationDescriptor( redScout,  L(0,5)));
 		endConfig.add(new PieceLocationDescriptor( redSpy,    L(2,5)));
 
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);	
 
 		// Move a spy
 		EpsilonTestDouble.move(PieceType.SCOUT, L(0,5) , L(4,5));	
 		fail();
 	}
 
 	@Test(expected=StrategyException.class)
 	public void scoutCantJumpEnemies() throws StrategyException{
 		// Add spies for fights with marshals
 		endConfig.add(new PieceLocationDescriptor( redScout,  L(0,5)));
 		endConfig.add(new PieceLocationDescriptor( blueSpy,   L(2,5)));
 
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);	
 
 		// Move a spy
 		EpsilonTestDouble.move(PieceType.SCOUT, L(0,5) , L(4,5));	
 		fail();
 	}
 
 	@Test(expected=StrategyException.class)
 	public void scoutCantAttackOnLongMoves() throws StrategyException{
 		// Add spies for fights with marshals
 		endConfig.add(new PieceLocationDescriptor( redScout,  L(0,5)));
 		endConfig.add(new PieceLocationDescriptor( blueSpy,   L(4,5)));
 
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);	
 
 		// Move a spy
 		EpsilonTestDouble.move(PieceType.SCOUT, L(0,5) , L(4,5));	
 		fail();
 	}
 
 	////// BATTLE
 	/**
 	 * Method battle1_MarshalWinsALot.
 	 * @throws StrategyException
 	 */
 	@Test // colonel captain lieutenant
 	public void battle1_MarshalWinsALot() throws StrategyException {
 		// Add an extra red piece
 		endConfig.add(new PieceLocationDescriptor( blueLieutenant,  L(1,1)));
 		endConfig.add(new PieceLocationDescriptor( blueLieutenant,  L(9,9)));
 		endConfig.add(new PieceLocationDescriptor( redCaptain,  L(9,8)));
 		endConfig.add(new PieceLocationDescriptor( redCaptain,  L(4,3)));
 		endConfig.add(new PieceLocationDescriptor( blueColonel,  L(1,2)));
 		endConfig.add(new PieceLocationDescriptor( blueColonel,  L(1,3)));
 		
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);
 		
 		// Do some battles
 		assertSame(EpsilonTestDouble.move(PieceType.CAPTAIN, L(9,8), L(9,9)).getStatus(), MoveResultStatus.OK );
 		assertSame(EpsilonTestDouble.move(PieceType.LIEUTENANT, L(1,1), L(0,1)).getStatus(), MoveResultStatus.OK );
 		assertSame(EpsilonTestDouble.move(PieceType.MARSHAL, L(1,1), L(1,2)).getStatus(), MoveResultStatus.OK );
 		assertSame(EpsilonTestDouble.move(PieceType.MARSHAL, L(0,2), L(1,2)).getStatus(), MoveResultStatus.OK );
 		assertSame(EpsilonTestDouble.move(PieceType.CAPTAIN, L(9,9), L(8,9)).getStatus(), MoveResultStatus.OK );
 	}
 
 	/**
 	 * Method battle5_blueGetsRedFlag
 	 * @throws StrategyException
 	 */
 	@Test
 	public void battle5_blueGetsRedFlag()throws StrategyException {
 		// Add an extra red piece
 		endConfig.add(new PieceLocationDescriptor( blueSergeant,  L(4,3)));
 
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);
 
 		EpsilonTestDouble.move(PieceType.MARSHAL,   L(0,1), L(1,1));
 		assertSame(EpsilonTestDouble.move(PieceType.SERGEANT,   L(4,3), L(5,3)).getStatus(), MoveResultStatus.BLUE_WINS );
 
 	}	
 
 	/**
 	 * Method battle3_SergeantDraw.
 	 * @throws StrategyException
 	 */
 	@Test
 	public void fullGame_blueCantMove_redWins() throws StrategyException {
 		// Add an extra red piece
 		endConfig.add(new PieceLocationDescriptor( redSergeant,  L(1,3)));
 
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);
 
 		// Make the move that ends the game
 		assertSame(EpsilonTestDouble.move(PieceType.MARSHAL, L(0,1), L(0,2)).getStatus(), MoveResultStatus.RED_WINS);
 	}
 
 	@Test
 	public void fullGame_DRAW_noMoves() throws StrategyException {
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);
 
 		// Make the move that ends the game
 		assertSame(EpsilonTestDouble.move(PieceType.MARSHAL, L(0,1), L(0,2)).getStatus(), MoveResultStatus.DRAW);
 	}
 
 	@Test
 	public void fullGame_blueWins_redCantMove() throws StrategyException {
 		// Add an extra blue piece
 		endConfig.add(new PieceLocationDescriptor( blueSergeant,  L(1,3)));
 
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);
 
 		// Make the move that ends the game
 		assertSame(EpsilonTestDouble.move(PieceType.MARSHAL, L(0,1), L(0,2)).getStatus(), MoveResultStatus.BLUE_WINS);
 	}
 
 	@Test
 	public void fullGame_blueWins_redHasBombsAndFlag() throws StrategyException {
 		// Add an extra blue piece
 		endConfig.add(new PieceLocationDescriptor( blueSergeant,  L(1,3)));
 		endConfig.add(new PieceLocationDescriptor( redBomb,  L(2,3)));
 		endConfig.add(new PieceLocationDescriptor( redBomb,  L(3,3)));
 
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);
 
 		// Make the move that ends the game
 		assertSame(EpsilonTestDouble.move(PieceType.MARSHAL, L(0,1), L(0,2)).getStatus(), MoveResultStatus.BLUE_WINS);
 	}
 
 	@Test
 	public void fullGame_draw_bombs_and_flags() throws StrategyException {
 		// Add an extra blue piece
 		endConfig.add(new PieceLocationDescriptor( redBomb,  L(2,3)));
 		endConfig.add(new PieceLocationDescriptor( blueBomb,  L(3,3)));
 
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);
 
 		// Make the move that ends the game
 		assertSame(EpsilonTestDouble.move(PieceType.MARSHAL, L(0,1), L(0,2)).getStatus(), MoveResultStatus.DRAW);
 	}
 
 
 
 	@Test(expected=StrategyException.class)
 	public void cannotMoveAfterGameOver() throws StrategyException {
 		// Add an extra blue piece
 		endConfig.add(new PieceLocationDescriptor( blueSergeant,  L(1,3)));
 
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);
 
 		// Make the move that ends the game
 		assertSame(EpsilonTestDouble.move(PieceType.MARSHAL, L(0,3), L(0,4)).getStatus(), MoveResultStatus.BLUE_WINS);
 		EpsilonTestDouble.move(PieceType.SERGEANT, L(1,3), L(1,4)); //<-- this is a valid move
 	}
 
 
 	@Test
 	public void bombKillsEverythingButMiner_AND_doesntMove() throws StrategyException{
 		// Add extra things to fight bombs 
 		for (int i = 0; i < 4; i+=2){
 			endConfig.add(new PieceLocationDescriptor( blueBomb,   L(i   ,5)));
 			endConfig.add(new PieceLocationDescriptor( redBomb,    L(i+1 ,5)));
 		}
 
 		endConfig.add(new PieceLocationDescriptor( redMarshal,  L(0,4)));
 		endConfig.add(new PieceLocationDescriptor( blueScout,   L(1,4)));
 		endConfig.add(new PieceLocationDescriptor( redGeneral,  L(2,4)));
 		endConfig.add(new PieceLocationDescriptor( blueMajor,    L(3,4)));
 
 
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);
 
 		// Try to attack bombs...
 		DetailedMoveResult marshalLose = (DetailedMoveResult) EpsilonTestDouble.move(PieceType.MARSHAL,   L(0,4) , L(0,5));
 		DetailedMoveResult scoutLose   = (DetailedMoveResult) EpsilonTestDouble.move(PieceType.SCOUT,     L(1,4) , L(1,5));
 		DetailedMoveResult generalLose = (DetailedMoveResult) EpsilonTestDouble.move(PieceType.GENERAL,   L(2,4) , L(2,5));
 		DetailedMoveResult majorLose   = (DetailedMoveResult) EpsilonTestDouble.move(PieceType.MAJOR,  	L(3,4) , L(3,5));
 
 		// Verify bomb survived
 		assertSame(blueBomb, EpsilonTestDouble.getPieceAt(L(0,5)));
 		assertSame(redBomb,  EpsilonTestDouble.getPieceAt(L(1,5)));
 		assertSame(blueBomb, EpsilonTestDouble.getPieceAt(L(2,5)));
 		assertSame(redBomb,  EpsilonTestDouble.getPieceAt(L(3,5)));
 
 		// Verify everything else lost
 		assertSame(marshalLose.getBattleWinner().getPiece(), 	blueBomb);
 		assertSame(scoutLose.getBattleWinner().getPiece(), 		redBomb);
 		assertSame(generalLose.getBattleWinner().getPiece(), 	blueBomb);
 		assertSame(majorLose.getBattleWinner().getPiece(),		redBomb);
 	}
 
 	@Test
 	public void minerKillsBomb() throws StrategyException{
 		// Add spies for fights with marshals
 		endConfig.add(new PieceLocationDescriptor( redMiner,  L(3,0)));
 		endConfig.add(new PieceLocationDescriptor( blueBomb,  L(4,0)));
 
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);	
 
 		// DO battle
 		DetailedMoveResult minerWin = (DetailedMoveResult) EpsilonTestDouble.move(PieceType.MINER, L(3,0) , L(4,0));		
 
 		// Prove result
 		assertSame(minerWin.getBattleWinner().getPiece(), redMiner);	
 	}
 
 	@Test
 	public void spyKillsMarshalOnStrike_notOnDefence() throws StrategyException{
 		// Add spies for fights with marshals
 		endConfig.add(new PieceLocationDescriptor( redSpy,  L(1,4)));
 		endConfig.add(new PieceLocationDescriptor( blueMarshal,  L(4,3)));
 		endConfig.add(new PieceLocationDescriptor( redSpy,  L(3,3)));
 
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);	
 
 		// Fight!
 		DetailedMoveResult spyWin = (DetailedMoveResult) EpsilonTestDouble.move(PieceType.SPY,      L(1,4) , L(0,4));
 		DetailedMoveResult spyLose = (DetailedMoveResult) EpsilonTestDouble.move(PieceType.MARSHAL, L(4,3) , L(3,3));
 
 		// Prove Results
 		assertSame(spyWin.getBattleWinner().getPiece(), redSpy);
 		assertSame(spyLose.getBattleWinner().getPiece(), blueMarshal);
 	}
 
 	@Test
 	public void battlesBetweenNewPieces() throws StrategyException{
 		// Add some pieces for fighting
 		endConfig.add(new PieceLocationDescriptor( redMiner,     L(1,4)));
 		endConfig.add(new PieceLocationDescriptor( blueGeneral,  L(2,4)));
 		endConfig.add(new PieceLocationDescriptor( blueSpy,      L(3,3)));
 		endConfig.add(new PieceLocationDescriptor( redMiner,     L(3,4)));
 
 		// Forcibly set the configuration		
 		EpsilonTestDouble.setFieldConfiguration(endConfig);
 
 		// Fight!
 		DetailedMoveResult minerLose = (DetailedMoveResult)  EpsilonTestDouble.move(PieceType.MINER,L(1,4), L(2,4));
 		DetailedMoveResult minerWin = (DetailedMoveResult)   EpsilonTestDouble.move(PieceType.SPY, L(3,3) , L(3,4));
 
 		// Prove Results
 		assertSame(minerLose.getBattleWinner().getPiece(),  blueGeneral);
 		assertSame(minerWin.getBattleWinner().getPiece(), redMiner);
 	}
 
 
 	///////////////////// Miscellaneous Epsilon tests
 	@Test 
 	public void redResigns() throws StrategyException {
 		assertSame(game.move(null, null, null).getStatus(), MoveResultStatus.BLUE_WINS);
 	}
 	
 	@Test 
 	public void blueResigns() throws StrategyException {
 		game.move(PieceType.COLONEL, L(0,3), L(0,4));
 		assertSame(game.move(null, null, null).getStatus(), MoveResultStatus.RED_WINS);
 	}
 	
 		
 	
 //	@Test
 //	public void testIfNullGetsUsedAsPieceType1() throws StrategyException{
 //		assertSame(0, new EpsilonPieceMoves().getMovementCapability(null));
 //	}
 //
 //	@Test
 //	public void testIfNullGetsUsedAsPieceType2() throws StrategyException{
 //		assertSame(0, new EpsilonPiecePowers().getPower(null));
 //	}
 //
 //	@Test
 //	public void testIf_CHOKE_POINT_GetsUsedAsPieceType1() throws StrategyException{
 //		assertSame(0, new EpsilonPieceMoves().getMovementCapability(PieceType.CHOKE_POINT));
 //	}
 //
 //	@Test
 //	public void testIf_CHOKE_POINT_GetsUsedAsPieceType2() throws StrategyException{
 //		assertSame(0, new EpsilonPiecePowers().getPower(PieceType.CHOKE_POINT));
 //	}
 
 
 	//////////////// Helper methods /////////////////////
 	/** Makes a location with the given coordinates
 	 * 
 	 * @param x the x coordinate
 	 * @param y the y coordinate
 	 * @return a location2D with the given coordinates
 	 */
 	private static Location L(int x, int y){
 		return new Location2D(x,y);
 	}
 
 	/** Adds the given piece to the right teams config
 	 * 
 	 * @param aPiece piece to add
 	 * @param x x coordinate
 	 * @param y y coordinate
 	 */
 	private void addToConfiguration(Piece aPiece, int x, int y){
 		this.addToConfiguration(aPiece.getType(), aPiece.getOwner(), x, y);
 	}
 
 	/** Adds the given piece + color to the given location
 	 * 
 	 * @param type piece to add
 	 * @param color player to own
 	 * @param x x coordinate
 	 * @param y y coordinate
 	 */
 	private void addToConfiguration(PieceType type, PlayerColor color, int x, int y)
 	{
 		final PieceLocationDescriptor confItem = new PieceLocationDescriptor(
 				new Piece(type, color),
 				L(x, y));
 		if (color == PlayerColor.RED) {
 			validRedConfiguration.add(confItem);
 		} else {
 			validBlueConfiguration.add(confItem);
 		}
 	}
 
 
 }

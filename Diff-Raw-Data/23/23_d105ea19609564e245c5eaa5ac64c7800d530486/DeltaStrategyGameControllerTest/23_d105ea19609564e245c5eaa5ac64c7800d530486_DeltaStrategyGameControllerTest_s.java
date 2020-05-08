 /*******************************************************************************
  * This files was developed for CS4233: Object-Oriented Analysis & Design.
  * The course was taken at Worcester Polytechnic Institute.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package strategy.game.version.delta;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 
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
 import strategy.game.common.GameVersion;
 import strategy.game.common.Location;
 import strategy.game.common.Location1D;
 import strategy.game.common.Location2D;
 import strategy.game.common.Location3D;
 import strategy.game.common.MoveResultStatus;
 import strategy.game.common.Piece;
 import strategy.game.common.PieceLocationDescriptor;
 import strategy.game.common.PieceType;
 
 /** These are the tests for Delta Strategy
  * 
  * @author Dabrowski  
  * @version $Revision: 1.0 $
  */
 public class DeltaStrategyGameControllerTest {
 
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
 
 	// Blue Pieces
 	private final Piece blueFlag = new Piece(PieceType.FLAG, PlayerColor.BLUE);
 	private final Piece blueMarshal = new Piece(PieceType.MARSHAL, PlayerColor.BLUE);
 	private final Piece blueColonel = new Piece(PieceType.COLONEL, PlayerColor.BLUE);
 	private final Piece blueCaptain = new Piece(PieceType.CAPTAIN, PlayerColor.BLUE);
 	private final Piece blueLieutenant = new Piece(PieceType.LIEUTENANT, PlayerColor.BLUE);
 	private final Piece blueSergeant = new Piece(PieceType.SERGEANT, PlayerColor.BLUE);
 
 	// Choke point
 	private final Piece chokePoint = new Piece(PieceType.CHOKE_POINT, null);
 
 	//Piecs + locations
 	private final PieceLocationDescriptor redFlagAtLocation = new PieceLocationDescriptor( redFlag, L(0,1));
 	private final PieceLocationDescriptor blueFlagAtLocation = new PieceLocationDescriptor( blueFlag, L(3,4));
 
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
 	private DeltaStrategyGameControllerTestDouble deltaTestDouble;
 
 	@BeforeClass
 	public static void setupBefore(){
 		@SuppressWarnings("unused")
 		GameVersion gameVersion = (GameVersion.DELTA);
 	}
 
 	/**
 	 * Method setup.
 	 * @throws StrategyException
 	 */
 	@Before
 	public void setup() throws StrategyException{
 		validBlueConfiguration = new LinkedList<PieceLocationDescriptor>();
 		validRedConfiguration = new LinkedList<PieceLocationDescriptor>();
 		// Fresh configuration for each test
 		validBlueConfiguration.add(blueFlagAtLocation);
 		validBlueConfiguration.add(new PieceLocationDescriptor( blueMarshal, L(5,5)));
 		validBlueConfiguration.add(new PieceLocationDescriptor( blueColonel, L(2,4)));
 		validBlueConfiguration.add(new PieceLocationDescriptor( blueColonel, L(0,5)));
 		validBlueConfiguration.add(new PieceLocationDescriptor( blueCaptain, L(4,4)));
 		validBlueConfiguration.add(new PieceLocationDescriptor( blueCaptain, L(4,5)));
 		validBlueConfiguration.add(new PieceLocationDescriptor( blueLieutenant, L(1,4)));
 		validBlueConfiguration.add(new PieceLocationDescriptor( blueLieutenant, L(1,5)));
 		validBlueConfiguration.add(new PieceLocationDescriptor( blueLieutenant, L(3,5)));
 		validBlueConfiguration.add(new PieceLocationDescriptor( blueSergeant, L(2,5)));
 		validBlueConfiguration.add(new PieceLocationDescriptor( blueSergeant, L(5,4)));
 		validBlueConfiguration.add(new PieceLocationDescriptor( blueSergeant, L(0,4)));
 		validRedConfiguration.add(  redFlagAtLocation );
 		validRedConfiguration.add(new PieceLocationDescriptor( redMarshal, L(1,1)));
 		validRedConfiguration.add(new PieceLocationDescriptor( redColonel, L(2,1)));
 		validRedConfiguration.add(new PieceLocationDescriptor( redColonel, L(4,0)));
 		validRedConfiguration.add(new PieceLocationDescriptor( redCaptain, L(2,0)));
 		validRedConfiguration.add(new PieceLocationDescriptor( redCaptain, L(3,1)));
 		validRedConfiguration.add(new PieceLocationDescriptor( redLieutenant, L(0,0)));
 		validRedConfiguration.add(new PieceLocationDescriptor( redLieutenant, L(1,0)));
 		validRedConfiguration.add(new PieceLocationDescriptor( redLieutenant, L(4,1)));
 		validRedConfiguration.add(new PieceLocationDescriptor( redSergeant, L(3,0)));
 		validRedConfiguration.add(new PieceLocationDescriptor( redSergeant, L(5,0)));
 		validRedConfiguration.add(new PieceLocationDescriptor( redSergeant, L(5,1)));
 
 		// Fresh valid game
 		game = factory.makeDeltaStrategyGame(validRedConfiguration, validBlueConfiguration);
 		game.startGame();
 
 		// For general use
 		badConfig = new LinkedList<PieceLocationDescriptor>();
 		deltaTestDouble = new DeltaStrategyGameControllerTestDouble(validRedConfiguration, validBlueConfiguration, new DeltaRules());
 		deltaTestDouble.startGame();
 
 		// make a configuration that simulates the end of a full game
 		endConfig.add(new PieceLocationDescriptor( blueFlag,    L(0,2)));
 		endConfig.add(new PieceLocationDescriptor( redMarshal,  L(0,3)));	
 		endConfig.add(new PieceLocationDescriptor( blueMarshal, L(0,4)));
 		endConfig.add(new PieceLocationDescriptor( redFlag,     L(0,5)));
 	}
 
 	/////////// TESTING makeDeltaStrategyGame -- number of pieces
 	/**
 	 * Method controllerTakesValidConfigurations.
 	 * @throws StrategyException
 	 */
 	@Test
 	public void controllerTakesValidConfigurations() throws StrategyException
 	{
 		game = factory.makeDeltaStrategyGame(validRedConfiguration, validBlueConfiguration);
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
 		factory.makeDeltaStrategyGame(null, validBlueConfiguration);
 	}
 
 	/**
 	 * Method noRedFlag.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void noRedFlag() throws StrategyException{
 		validRedConfiguration.remove(redFlagAtLocation);
 		validRedConfiguration.add(new PieceLocationDescriptor(blueMarshal ,L(0,1)));
 		factory.makeDeltaStrategyGame(validRedConfiguration, validBlueConfiguration);
 	}
 
 	/**
 	 * Method testNoBlueFlag.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void testNoBlueFlag() throws StrategyException{
 		validBlueConfiguration.remove(blueFlagAtLocation);
 		validBlueConfiguration.add(new PieceLocationDescriptor(blueMarshal ,L(3,4)));
 		factory.makeDeltaStrategyGame(validRedConfiguration, validBlueConfiguration);
 	}
 
 	/**
 	 * Method toofewPiecesForRedConfigurationButHasFlag.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void toofewPiecesForRedConfigurationButHasFlag() throws StrategyException{
 		badConfig.add(new PieceLocationDescriptor( redFlag, L(0,1)));
 		factory.makeDeltaStrategyGame(badConfig, validBlueConfiguration);
 	}
 
 	@Test(expected=StrategyException.class)
 	public void redConfigurationHasTooFewItem() throws StrategyException
 	{ 
 		validRedConfiguration.remove(0);
 		factory.makeDeltaStrategyGame(validRedConfiguration, validBlueConfiguration);
 	}
 
 
 	/**
 	 * Method tooManyPiecesForRedConfigurationWithOnlyFlag.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void tooManyPiecesForRedConfigurationWithOnlyFlag() throws StrategyException{
 		for (int i =0;i<15;i++) badConfig.add(new PieceLocationDescriptor( redFlag, L(0,1)));
 		factory.makeDeltaStrategyGame(badConfig, validBlueConfiguration);
 	}
 
 	/**
 	 * Method nullForBlueConfiguration.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void nullForBlueConfiguration() throws StrategyException{
 		factory.makeDeltaStrategyGame(validRedConfiguration, null);
 	}
 
 	/**
 	 * Method tooFewPiecesForBlueConfigurationButHasFlag.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void tooFewPiecesForBlueConfigurationButHasFlag() throws StrategyException{
 		badConfig.add(new PieceLocationDescriptor( blueFlag, L(3,4)));
 		factory.makeDeltaStrategyGame(validRedConfiguration, badConfig);
 	}
 
 	// This exception is thrown for many reasons, but definitely catches invalid number of pieces
 	@Test(expected=StrategyException.class)
 	public void tooManyPiecesForBlueConfigurationWithOnlyFlag() throws StrategyException{
 		for (int i =0;i<15;i++) badConfig.add(new PieceLocationDescriptor( blueFlag, L(i%6 , (i/3) % 6 )));
 		factory.makeDeltaStrategyGame(validRedConfiguration, badConfig);
 	}
   
 	/**
 	 * Method nullLocation.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void nullLocation() throws StrategyException{
 		badConfig.add(new PieceLocationDescriptor( blueFlag, null));
 		factory.makeDeltaStrategyGame(validRedConfiguration, badConfig);
 	}
 
 	/**
 	 * Method nullPiece.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void nullPiece() throws StrategyException{
 		badConfig.add(new PieceLocationDescriptor( null, L(3,4)));
 		factory.makeDeltaStrategyGame(validRedConfiguration, badConfig);
 	}
 
 	/**
 	 * Method nullPieceLocationDescription.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void nullPieceLocationDescription() throws StrategyException{
 		badConfig.add(null);
 		factory.makeDeltaStrategyGame(validRedConfiguration, badConfig);
 	}
 
 	/**
 	 * Method invalidPieceType.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void invalidPieceType() throws StrategyException{
 		final Piece badPice = new Piece(PieceType.BOMB, PlayerColor.BLUE);		
 		badConfig.add(new PieceLocationDescriptor( badPice, L(3,4)));
 		factory.makeDeltaStrategyGame(validRedConfiguration,badConfig );
 	}
 
 	/**
 	 * Method nullPieceOwner.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void nullPieceOwner() throws StrategyException{
 		final Piece badPice = new Piece(PieceType.BOMB, null);		
 		badConfig.add(new PieceLocationDescriptor( badPice, L(3,4)));
 		factory.makeDeltaStrategyGame(validRedConfiguration,badConfig );
 	}
 
 	/**
 	 * Method nullPieceType.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void nullPieceType() throws StrategyException{
 		final Piece badPice = new Piece(null, PlayerColor.BLUE);		
 		badConfig.add(new PieceLocationDescriptor( badPice, L(3,4)));
 		factory.makeDeltaStrategyGame(validRedConfiguration,badConfig );
 	}
 
 	/////////// TESTING makeDeltaStrategyGame -- locations
 	/**
 	 * Method locationUsedTwice.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void locationUsedTwice() throws StrategyException{
 		validRedConfiguration.add(new PieceLocationDescriptor( blueFlag, L(3,4)));
 		factory.makeDeltaStrategyGame(validRedConfiguration, validBlueConfiguration);
 	}
 
 	/**
 	 * Method bluePieceInNeutralZone.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void bluePieceInNeutralZone() throws StrategyException {
 		validBlueConfiguration.remove(blueFlagAtLocation); // remove flag
 		validBlueConfiguration.add(new PieceLocationDescriptor( blueFlag, L(3,3)));
 		factory.makeDeltaStrategyGame(validRedConfiguration, validBlueConfiguration);	
 	}
 
 	/**
 	 * Method redPieceInNeutralZone.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void redPieceInNeutralZone() throws StrategyException {
 		validRedConfiguration.remove(redFlagAtLocation); // remove flag
 		validRedConfiguration.add(new PieceLocationDescriptor( redFlag, L(3,2)));
 		factory.makeDeltaStrategyGame(validRedConfiguration, validBlueConfiguration);	
 	}
 
 	/**
 	 * Method playerFlagsInEnemyZones.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void playerFlagsInEnemyZones() throws StrategyException {
 		validRedConfiguration.remove(redFlagAtLocation); // remove flag
 		validBlueConfiguration.remove(blueFlagAtLocation); // remove flag
 		validBlueConfiguration.add(new PieceLocationDescriptor(redFlag, L(3,4) ));
 		validRedConfiguration.add(new PieceLocationDescriptor( blueFlag,L(0,1)));
 		factory.makeDeltaStrategyGame(validRedConfiguration, validBlueConfiguration);		
 	}
 
 	/**
 	 * Method pieceOutOfBoundsY.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void pieceOutOfBoundsY() throws StrategyException {
 		validBlueConfiguration.remove(blueFlagAtLocation); // remove flag
 		validBlueConfiguration.add(new PieceLocationDescriptor( blueFlag, L(0,12)));
 		factory.makeDeltaStrategyGame(validRedConfiguration, validBlueConfiguration);	
 	}
 
 	/**
 	 * Method pieceOutOfBoundsX.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void pieceOutOfBoundsX() throws StrategyException {
 		validRedConfiguration.remove(redFlagAtLocation); // remove flag
 		validRedConfiguration.add(new PieceLocationDescriptor( redFlag, L(11,1)));
 		factory.makeDeltaStrategyGame(validRedConfiguration, validBlueConfiguration);	
 	}
 
 	/**
 	 * Method pieceInNegativeX.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void pieceInNegativeX() throws StrategyException {
 		validRedConfiguration.remove(redFlagAtLocation); // remove flag
 		validRedConfiguration.add(new PieceLocationDescriptor( redFlag, L(-1,0)));
 		factory.makeDeltaStrategyGame(validRedConfiguration, validBlueConfiguration);	
 	}
 
 	/**
 	 * Method pieceInNegativeY.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void pieceInNegativeY() throws StrategyException {
 		validRedConfiguration.remove(redFlagAtLocation); // remove flag
 		validRedConfiguration.add(new PieceLocationDescriptor( redFlag, L(0,-1)));
 		factory.makeDeltaStrategyGame(validRedConfiguration, validBlueConfiguration);	
 	}
 
 	/**
 	 * Method pieceInNegativeCoordinates.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void pieceInNegativeCoordinates() throws StrategyException {
 		validRedConfiguration.remove(redFlagAtLocation); // remove flag
 		validRedConfiguration.add(new PieceLocationDescriptor( redFlag, L(-1,-1)));
 		factory.makeDeltaStrategyGame(validRedConfiguration, validBlueConfiguration);	
 	}
 
 	////// getPieceAt
 	/**
 	 * Method initialPositionHasCorrectREDPiecesPlaced.
 	 */
 	@Test
 	public void getPieceAtCanUseNonLocation2DSometimes(){
 		assertSame(redLieutenant, game.getPieceAt(new Location3D(0,0,3)));
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
 		assertEquals(null, game.getPieceAt( L(6,0)));	
 		assertEquals(null, game.getPieceAt( L(0,6)));	
 		assertEquals(null, game.getPieceAt( L(6,6)));
 	}
 	
 	/**
 	 * Method getPieceAt_worksAfterTwoMoves.
 	 * @throws StrategyException
 	 */
 	@Test
 	public void getPieceAt_worksAfterTwoMoves() throws StrategyException {
 		game.move(PieceType.SERGEANT, L(5,1), L(5,2));
 		game.move(PieceType.SERGEANT, L(5,4), L(5,3));
 		assertTrue(true); // have to get this far with moves
 		assertEquals(PieceType.SERGEANT, game.getPieceAt(L(5,2)).getType());
 		assertEquals(PieceType.SERGEANT, game.getPieceAt(L(5,3)).getType());				
 	}
 
 	@Test
 	public void getPieceAtChokePoints() throws StrategyException{
 		assertSame(chokePoint.getType(), game.getPieceAt(L(3,3)).getType() );
 		assertSame(chokePoint.getType(), game.getPieceAt(L(3,2)).getType() );
 		assertSame(chokePoint.getType(), game.getPieceAt(L(2,2)).getType() );
 		assertSame(chokePoint.getType(), game.getPieceAt(L(2,3)).getType() );
 	}
 
 
 	//////////// MOVEMENT
 	/**
 	 * Method makeMoveBeforeInitialization.
 	 * @throws StrategyException
 	 */
 	@Test(expected=StrategyException.class)
 	public void makeMoveBeforeInitialization() throws StrategyException	{
 		final StrategyGameController agame = StrategyGameFactory.getInstance().makeDeltaStrategyGame(validRedConfiguration, validBlueConfiguration);
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
 		game.move(PieceType.SERGEANT, L(11,0), L(6,1));			
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
 	@Test(expected=StrategyException.class)
 	public void violateMoveRepitionRule() throws StrategyException {
 		game.move(PieceType.SERGEANT,   L(5,1) ,   L(5,2));
 		game.move(PieceType.LIEUTENANT, L(1,4) ,   L(1,3));
 		game.move(PieceType.SERGEANT,   L(5,2) ,   L(5,1));
 		game.move(PieceType.CAPTAIN,    L(4,4) ,   L(4,3));
 		game.move(PieceType.SERGEANT,   L(5,1) ,   L(5,2)); 
 	}
 
 	////// BATTLE
 	/**
 	 * Method battle1_MarshalWinsALot.
 	 * @throws StrategyException
 	 */
 	@Test
 	public void battle1_MarshalWinsALot() throws StrategyException {
 		assertSame(game.move(PieceType.MARSHAL,    L(1,1), L(1,2)).getStatus(), MoveResultStatus.OK );
 		assertSame(game.move(PieceType.LIEUTENANT, L(1,4), L(1,3)).getStatus(), MoveResultStatus.OK );
 		assertSame(game.move(PieceType.MARSHAL,    L(1,2), L(1,3)).getStatus(), MoveResultStatus.OK );
 		assertSame(game.getPieceAt( L(1,3)  ).getType(),PieceType.MARSHAL );
 		assertSame(game.move(PieceType.SERGEANT,   L(0,4), L(1,4)).getStatus(), MoveResultStatus.OK );
 		assertSame(game.move(PieceType.MARSHAL,    L(1,3), L(1,4)).getStatus(), MoveResultStatus.OK );
 		assertSame(game.getPieceAt(L(1,4)).getType(),PieceType.MARSHAL );
 		assertSame(game.move(PieceType.COLONEL,    L(2,4), L(1,4)).getStatus(), MoveResultStatus.OK );
 		assertSame(game.getPieceAt(L(2,4)).getType(),PieceType.MARSHAL );
 	}
 
 	/**
 	 * Method battle5_blueGetsRedFlag
 	 * @throws StrategyException
 	 */
 	@Test
 	public void battle5_blueGetsRedFlag()throws StrategyException {
 		assertSame(game.move(PieceType.LIEUTENANT, L(4,1), L(4,2)).getStatus(), MoveResultStatus.OK );
 		assertSame(game.move(PieceType.SERGEANT,   L(0,4), L(0,3)).getStatus(), MoveResultStatus.OK );
 		assertSame(game.move(PieceType.LIEUTENANT, L(4,2), L(4,3)).getStatus(), MoveResultStatus.OK );
 		assertSame(game.move(PieceType.SERGEANT,   L(0,3), L(0,2)).getStatus(), MoveResultStatus.OK );
 		assertSame(game.move(PieceType.LIEUTENANT, L(4,3), L(4,4)).getStatus(), MoveResultStatus.OK );
 		assertSame(game.move(PieceType.SERGEANT,   L(0,2), L(0,1)).getStatus(), MoveResultStatus.BLUE_WINS );
 
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
 		deltaTestDouble.setFieldConfiguration(endConfig);
 
 		// Make the move that ends the game
 		assertSame(deltaTestDouble.move(PieceType.MARSHAL, L(0,3), L(0,4)).getStatus(), MoveResultStatus.RED_WINS);
 	}
 
 	@Test
 	public void fullGame_DRAW_noMoves() throws StrategyException {
 		// Forcibly set the configuration		
 		deltaTestDouble.setFieldConfiguration(endConfig);
 
 		// Make the move that ends the game
 		assertSame(deltaTestDouble.move(PieceType.MARSHAL, L(0,3), L(0,4)).getStatus(), MoveResultStatus.DRAW);
 	}
 
 	@Test
 	public void fullGame_blueWins_redCantMove() throws StrategyException {
 		// Add an extra blue piece
 		endConfig.add(new PieceLocationDescriptor( blueSergeant,  L(1,3)));
 
 		// Forcibly set the configuration		
 		deltaTestDouble.setFieldConfiguration(endConfig);
 
 		// Make the move that ends the game
 		assertSame(deltaTestDouble.move(PieceType.MARSHAL, L(0,3), L(0,4)).getStatus(), MoveResultStatus.BLUE_WINS);
 	}
 
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
 }

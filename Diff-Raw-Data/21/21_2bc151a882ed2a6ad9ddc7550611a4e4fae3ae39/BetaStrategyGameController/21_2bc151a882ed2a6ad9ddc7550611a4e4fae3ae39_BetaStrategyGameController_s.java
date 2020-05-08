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
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import strategy.common.StrategyException;
 import strategy.game.StrategyGameController;
 import strategy.game.common.Coordinate;
 import strategy.game.common.Location;
 import strategy.game.common.MoveResult;
 import strategy.game.common.MoveResultStatus;
 import strategy.game.common.Piece;
 import strategy.game.common.PieceLocationDescriptor;
 import strategy.game.common.PieceType;
 
 /**
  * The BetaStrategyGameController implements the game core for
  * the Beta Strategy version.
  * @author rjsmieja, jrspicola
  * @version Sep 10, 2013
  */
 public class BetaStrategyGameController implements StrategyGameController
 {
 	private boolean gameStarted;
 	private boolean gameOver;
 	private int moveCount;
 	
 	private final static int BOARD_X_MAX = 6;
 	private final static int BOARD_Y_MAX = 6;
 	private final static int BOARD_X_MIN = 0;
 	private final static int BOARD_Y_MIN = 0;
 	
 	private final static int MAX_MOVES = 6;
 	
 	private Collection<PieceLocationDescriptor> redConfiguration;
 	private Collection<PieceLocationDescriptor> blueConfiguration;
 	
 	public BetaStrategyGameController(Collection<PieceLocationDescriptor> redConfiguration, Collection<PieceLocationDescriptor> blueConfiguration) throws StrategyException
 	{
 		gameStarted = false;
 		gameOver = false;
 		moveCount = 0;
 		
 		this.redConfiguration = redConfiguration;
 		this.blueConfiguration = blueConfiguration;
 		
 		checkLocationsInConfigurations(redConfiguration, blueConfiguration);
 	}
 	
 	/**
 	 * @see strategy.game.StrategyGameController#startGame()
 	 */
 	@Override
 	public void startGame()
 	{
 		gameStarted = true;
 		gameOver = false;
 		moveCount = 0;
 	}
 
 	/**
 	 * @see strategy.game.StrategyGameController#move(strategy.game.common.PieceType, strategy.game.common.Location, strategy.game.common.Location)
 	 */
 	@Override
 	public MoveResult move(PieceType piece, Location from, Location to)
 			throws StrategyException
 	{
 		if (gameOver) {
 			throw new StrategyException("The game is over, you cannot make a move");
 		}
 		if (!gameStarted) {
 			throw new StrategyException("You must start the game!");
 		}
 				
 		//Validate locations passed in
 		checkLocationBoundaries(to);
 		checkLocationBoundaries(from);
 		
 		//Validate configurations after making the move
 		checkLocationsInConfigurations(redConfiguration, blueConfiguration);
 
 		//Increment counter and return result
 		moveCount++;
 		
 		if (moveCount >= MAX_MOVES){
 			gameOver = true;
 		}
 		
 		return new MoveResult(MoveResultStatus.OK, null);
 	}
 
 	/**
 	 * Check if a location is out the board. Throws an exception if the coordinates
 	 * are not equal to the expected value.
 	 * @param location The location to validate
 	 * @throws StrategyException if the location's coordinates are invalid
 	 */
 	private void checkLocationBoundaries(Location location) 
 			throws StrategyException
 	{
 		//TODO: Rewrite to scale with coordinates?
 		final int locationX = location.getCoordinate(Coordinate.X_COORDINATE);
 		final int locationY = location.getCoordinate(Coordinate.Y_COORDINATE);
 		
 		//X
 		if (locationX >= BOARD_X_MAX || locationX < BOARD_X_MIN){
 			throw new StrategyException("Location value <" + locationX + "> invalid for " + Coordinate.X_COORDINATE + "!");
 		}
 		
 		//Y
 		if (locationY >= BOARD_Y_MAX || locationY < BOARD_Y_MIN){
 			throw new StrategyException("Location value <" + locationY + "> invalid for " + Coordinate.Y_COORDINATE + "!");
 		}
 	}
 
 	/**
 	 * Checks the following
 	 * @param redConfiguration One configuration to validate
 	 * @param blueConfiguration Another configuration to validate
 	 * @throws StrategyException if locations overlap
 	 */
 	@SafeVarargs
 	static private void checkLocationsInConfigurations(Collection<PieceLocationDescriptor>... configurations) throws StrategyException{
 		//HashSet to keep track of duplicates
		HashSet<PieceLocationDescriptor> map = new HashSet<PieceLocationDescriptor>();
 
 		for (Collection<PieceLocationDescriptor> configuration : configurations){
 			for(PieceLocationDescriptor i : configuration){
				if (map.contains(i)){
 					throw new StrategyException("Two pieces are in the same location!");
 				}else{ 
					map.add(i);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public Piece getPieceAt(Location location)
 	{
 		//Loop through all collections
 		for(PieceLocationDescriptor i : redConfiguration){
 			if (i.getLocation().equals(location)){
 				return i.getPiece();
 			}
 		}
 
 		for(PieceLocationDescriptor i : blueConfiguration){
 			if (i.getLocation().equals(location)){
 				return i.getPiece();
 			}
 		}
 
 		return null;
 	}
 }

 /*******************************************************************************
  * This files was developed for CS4233: Object-Oriented Analysis & Design.
  * The course was taken at Worcester Polytechnic Institute.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package strategy.game.version;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import strategy.common.PlayerColor;
 import strategy.common.StrategyException;
 import strategy.game.StrategyGameController;
 import strategy.game.common.Location;
 import strategy.game.common.MoveResult;
 import strategy.game.common.MoveResultStatus;
 import strategy.game.common.Piece;
 import strategy.game.common.PieceLocationDescriptor;
 import strategy.game.common.PieceType;
 import strategy.game.version.configuration.ConfigurationRuleSet;
 import strategy.game.version.move.FinishedMove;
 import strategy.game.version.move.MoveRuleSet;
 
 /**
  * Generic StrategyGameController that can be customized with a ruleSet
  * 
  * @author rjsmieja, jrspicola
  * @version Sept 21, 2013
  */
 public class CommonStrategyGameController implements StrategyGameController {
 
 	private boolean gameStarted;
 	private boolean gameOver;
 
 	final private Map<PlayerColor, Collection<PieceLocationDescriptor>> startingConfigurations;
 
 	private Map<Location, Piece> board;
 
 	final private ConfigurationRuleSet startingConfigurationRules;
 	final private MoveRuleSet moveRules;
 
 	/**
 	 * Constructor for CommonStrategyGameController
 	 * 
 	 * @param redConfiguration
 	 *            Configuration to be used for the red player
 	 * @param blueConfiguration
 	 *            Configuration to be used for the blue player
 	 * @param ruleSet
 	 *            Rule set to be used for this game
 	 * @throws StrategyException
 	 *             Thrown if there is an error in the configurations
 	 */
 	public CommonStrategyGameController(Map<PlayerColor, Collection<PieceLocationDescriptor>> startingConfigurations, StrategyGameRuleSet ruleSet) throws StrategyException {
 		// Rules
 		startingConfigurationRules = ruleSet.getStartingConfigurationRules();
 
 		// State
 		gameStarted = false;
 		gameOver = false;
 
 
 		for (PlayerColor player : startingConfigurations.keySet()){
 			if (!startingConfigurationRules.validateConfiguration(player, startingConfigurations.get(player))) {
 				throw new StrategyException(player + " starting configuration is invalid!");
 			}
 		}
 
 		//Save configurations
 		this.startingConfigurations = startingConfigurations;
 		
 		board = new HashMap<Location, Piece>();
 
 		moveRules = ruleSet.getMoveRules();
 	}
 
 	/**
 	 * @throws StrategyException
 	 * @see strategy.game.StrategyGameController#startGame()
 	 */
 	@Override
 	public void startGame() throws StrategyException {
 		if (gameStarted && !gameOver) {
 			throw new StrategyException("Unfinished game currently in progress");
 		}
 
		if (gameStarted && gameOver){
			throw new StrategyException("Attempting to restart a finished game!");
		}
		
 		gameStarted = true;
 		gameOver = false;
 
 		initializeBoard(startingConfigurations);
 
 		moveRules.reset();
 	}
 
 	/**
 	 * @see strategy.game.StrategyGameController#move(strategy.game.common.PieceType,
 	 *      strategy.game.common.Location, strategy.game.common.Location)
 	 */
 	@Override
	public MoveResult move(PieceType piece, Location from, Location to) throws StrategyException {
 		if (gameOver) {
			throw new StrategyException("The game is over, you cannot make a move");
 		}
 		if (!gameStarted) {
 			throw new StrategyException("You must start the game!");
 		}
 
		final PieceLocationDescriptor movingPiece = new PieceLocationDescriptor(getPieceAt(from), from);
		final PieceLocationDescriptor targetPiece = new PieceLocationDescriptor(getPieceAt(to), to);
 
		final FinishedMove finishedMove = moveRules.doMove(piece, movingPiece, targetPiece, board);
 
 		final MoveResult moveResult = finishedMove.getMoveResult();
 		board = finishedMove.getBoard();
 
 		// Set gameOver
 		if (moveResult.getStatus() != MoveResultStatus.OK) {
 			gameOver = true;
 		}
 
 		return moveResult;
 	}
 
 	/**
 	 * @see strategy.game.StrategyGameController#getPieceAt(strategy.game.common.Location)
 	 */
 	@Override
 	public Piece getPieceAt(Location location)
 	{
 		return board.get(location);
 	}
 
 	private void initializeBoard(Map<PlayerColor, Collection<PieceLocationDescriptor>> startingConfigurations){
 		board.clear();
 
 		for(PlayerColor player : startingConfigurations.keySet()){
 			for (PieceLocationDescriptor piece: startingConfigurations.get(player)){
 				board.put(piece.getLocation(), piece.getPiece());
 			}
 		}
 	}
 }

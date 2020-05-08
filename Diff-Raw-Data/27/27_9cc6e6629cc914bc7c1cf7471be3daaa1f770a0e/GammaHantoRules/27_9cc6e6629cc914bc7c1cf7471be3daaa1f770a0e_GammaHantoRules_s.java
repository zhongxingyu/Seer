 /**
  * This file was developed for CS4233: Object-Oriented Analysis & Design.
  * The course was taken at Worcester Polytechnic Institute.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package hanto.studentndemarinis.gamma;
 
 import hanto.common.HantoException;
 import hanto.studentndemarinis.common.AbstractHantoRuleSet;
 import hanto.studentndemarinis.common.HantoGameState;
 import hanto.studentndemarinis.common.HantoPiece;
 import hanto.studentndemarinis.common.HantoRuleSet;
 import hanto.studentndemarinis.common.HexCoordinate;
 import hanto.util.HantoPieceType;
 import hanto.util.HantoPlayerColor;
 import hanto.util.MoveResult;
 
 /**
  * Abstraction for GammaHanto's move rules
  * 
  * @author ndemarinis
  * @version Jan 31, 2013
  *
  */
 public class GammaHantoRules extends AbstractHantoRuleSet implements HantoRuleSet {
 
 	// Number of moves before we MUST place a butterfly
 	private final int NUM_MOVES_PRE_BUTTERFLY = 3;
 	
 	// Max number of moves before ending in a draw
 	private final int MAX_MOVES = 10;
 	
 	/**
 	 * Make a new set of GammaHanto's rules, given
 	 * the game itself
 	 * @param state The HantoGame we'll be checking
 	 */
 	public GammaHantoRules(HantoGameState state) {
 		this.state = state;
 	}
 
 	/**
 	 * Checks to be performed before a move is made
 	 * 
 	 * @param piece Piece to be placed at the given location
 	 * @param from source coordinate of piece on the board, null if not on the board
 	 * @param to destination coordinate for piece to move
 	 * @throws HantoException if a rule has been violated, 
 	 * leaving the board in an illegal state
 	 */
 	@Override
 	public void doPreMoveChecks(HantoPieceType piece, HexCoordinate from, HexCoordinate to) 
 			throws HantoException 
 	{
 		super.doPreMoveChecks(piece, from, to);
 		verifyButterflyHasBeenPlacedByFourthTurn(piece);
 	}
 
 	/**
 	 * Checks to perform after a move has been made.  
 	 * @param to Destination coordinate of newly-moved piece
 	 * @throws HantoException if the a rule has been violated, 
 	 * leaving the board in an illegal state
 	 */
 	@Override
 	public void doPostMoveChecks(HexCoordinate to) throws HantoException 
 	{
 		verifyBoardIsContiguous();
 	}

 	/**
 	 * Evaluate whether the game needs to end based on the board configuration.  
 	 * Intended to be called after each move to determine if a win has occurred.  
 	 * @throws HantoException if the board is in an illegal state
 	 */
 	@Override
 	public MoveResult evaluateMoveResult() throws HantoException 
 	{	
 		MoveResult ret = winIfButterflyIsSurrounded();
 		
 		// If the previous condition didn't end the game
 		if(ret == MoveResult.OK) {
 			ret = endInDrawAfter10Moves();
 		}
 		
 		// Set the gameOver flag based on the result
 		determineIfGameHasEnded(ret);
 		
 		return ret;
 	}
 	
 	/**
 	 * Ensure that a butterfly must be placed by the fourth term,
 	 * as the rules specify.  Therefore, a player moving on/after 
 	 * the fourth turn with no butterfly on the board MUST 
 	 * place their butterfly.  
 	 * 
 	 * @param piece The piece involved in the move
 	 * @throws HantoException if trying to place a butterfly without
 	 * one for that player on the board
 	 */
 	protected void verifyButterflyHasBeenPlacedByFourthTurn(HantoPieceType piece) 
 			throws HantoException	
 	{
 		if(piece != HantoPieceType.BUTTERFLY && 
 		   state.getNumMoves() >= NUM_MOVES_PRE_BUTTERFLY &&
 		   !state.getBoard().contains(state.getCurrPlayer(), HantoPieceType.BUTTERFLY)) 
 		{
 			throw new HantoException("Illegal move:  " +
 					"Butterfly must be placed by the foruth turn!");
 		}
 	}
 	
 	/**
 	 * Give result ending the game in a draw after 10 moves.  
 	 * @return OK if game has been running for less than 10 moves, 
 	 * DRAW otherwise.  
 	 */
 	protected MoveResult endInDrawAfter10Moves()
 	{
 		return (state.getNumMoves() != MAX_MOVES) ?
 				MoveResult.OK : MoveResult.DRAW;
 	}
	
 	/**
 	 * Check if a player has won by surrounding their opponent's
 	 * butterfly.  If both butterflies are surrounded, it's a DRAW.  
 	 * @return winning player if they have surrounded their opponent's butterfly,
 	 * DRAW if both are surrounded, OK if none of these conditions have been met
 	 */
 	protected MoveResult winIfButterflyIsSurrounded()
 	{
 		MoveResult ret = MoveResult.OK;
 		
 		// Do the simplest thing that works and assume we only have one
 		// red and blue butterfly
 		
 		// TODO:  fix for both butterflies surrounded
 		for(HantoPiece p : state.getBoard().getPiecesOfType(HantoPieceType.BUTTERFLY))
 		{
 			if(state.getBoard().isSurrounded(p)) {
 				ret = (p.getColor() == HantoPlayerColor.BLUE) ? 
 						MoveResult.RED_WINS : MoveResult.BLUE_WINS;
 			}
 		}
 		
 		return ret;
 	}
 
 }

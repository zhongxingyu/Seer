 /**
  * This file was developed for CS4233: Object-Oriented Analysis & Design.
  * The course was taken at Worcester Polytechnic Institute.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */
 package hanto.studentndemarinis.common;
 
 import hanto.common.HantoException;
 import hanto.util.HantoPieceType;
 import hanto.util.MoveResult;
 
 /**
  * Provides common rule implementation for
  * a HantoRuleSet
  * @author ndemarinis
  * @version 4 February 2013
  *
  */
 public abstract class AbstractHantoRuleSet implements HantoRuleSet {
 
 	protected HantoGameState state;
 	
 	/**
 	 * Perform checks that must take place before a move.  
 	 * See HantoRuleSet for details
 	 */
 	@Override
 	public void doPreMoveChecks(HantoPieceType piece, 
 			HexCoordinate from, HexCoordinate to) throws HantoException 
 	{
 		verifyGameIsNotOver();
 		verifySourceAndDestinationCoords(from, to);
 		verifyMoveIsLegal(from, to);
 	}
 	
 	/**
 	 * Perform a move for real
 	 * See HantoRuleSet for details.  
 	 */
 	@Override
 	public void actuallyMakeMove(HantoPieceType type, HexCoordinate from, HexCoordinate to) 
 			throws HantoException
 	{
 		// If this move involved placing a new piece, remove it from the player's hand
 		if(from == null) {
 			state.getPlayersHand(state.getCurrPlayer()).removeFromHand(type);
 		}
 		
 		// Remove the old piece from the board (if we haven't failed yet)
 		if(from != null) {
 			state.getBoard().remove(from);
 		}
 		
 		// Finally, add the new piece to the board.  
 		state.getBoard().addPieceAt(new HantoPiece(state.getCurrPlayer(), type, to), to);
 	}
 	
 	/**
 	 * Perform any checks that must take place after a move
 	 * @param to Destination coordinate
 	 * @throws HantoException if any of these rules have been violated
 	 */
 	@Override
 	public void doPostMoveChecks(HexCoordinate to) throws HantoException {
 		verifyBoardIsContiguous();
 	}
 
 	/**
 	 * Determine the result of a move based on the game's rules.  
 	 * Must be overridden by concrete realization.  
 	 */
 	public abstract MoveResult evaluateMoveResult() throws HantoException;
 
 	
 	/**
 	 * Verify the game is not over
 	 * @throws HantoException if the game is over
 	 */
 	protected void verifyGameIsNotOver() throws HantoException
 	{
 		if(state.isGameOver()) {
 			throw new HantoException("Illegal move:  game has already ended!");
 		}
 	}
 	
 	/**
 	 * Verify the source and destination coordinates exist.  
 	 * If a source is provided, it must exist on the board; 
 	 * a destination coordinate must exist for a valid move.  
 	 * @param from Source coordinate
 	 * @param to Destination coordinate
 	 * @throws HantoException if either of these conditions have been violated
 	 */
 	protected void verifySourceAndDestinationCoords(HexCoordinate from, HexCoordinate to) 
 			throws HantoException
 	{
 		// If provided, a source piece must exist on the board 
 		if(from != null) 
 		{
 			if(state.board.getPieceAt(from) == null) {
 				throw new HantoException("Illegal move:  " +
 						"source piece does not exist on board!");
 			}
 		}
 
 		// The move must have a destination coordinate
 		if(to == null){
 			throw new HantoException("Illegal move:  Destination coordinate must be provided!");
 		}
 	}
 	
 	/**
 	 * Verify a move is legal, meaning that the first piece must be at the origin, 
 	 * players can only move pieces of their own color, and that the destination
 	 * coordinate must be empty
 	 * @param from Source coordinate of move to verify
 	 * @param to Destination coordinate of move to verify
 	 * @throws HantoException if any of these conditions have been violated
 	 */
 	protected void verifyMoveIsLegal(HexCoordinate from, HexCoordinate to) 
 			throws HantoException
 	{
 		// Verify the piece to be moved is owned by the current player  
 		if(from != null) 
 		{
 			if(state.board.getPieceAt(from).getColor() != state.currPlayer) {
 				throw new HantoException("Illegal move:  your can only move pieces" +
 						"of your own color!");
 			}
 		}
 		
 		// If this is the first move, we need a piece at the origin
 		if(state.numMoves == 0 && 
				to.getX() != 0 && to.getY() != 0) {
 			throw new HantoException("Illegal move:  First piece must be placed " +
 					"at origin!");
 		}
 		
 		// If we find any pieces at the destination, it's not a legal move.  
 		if(state.board.getPieceAt(to) != null){
 			throw new HantoException("Illegal move:  can't place a piece " +
 					"on top of an existing piece!");
 		}
 	}
 	
 	/**
 	 * Verify all of the pieces on the board are in a
 	 * single contiguous grouping.  
 	 * @throws HantoException if any pieces are separated from the group
 	 */
 	protected void verifyBoardIsContiguous() throws HantoException
 	{
 		// If we violated the adjacency rules
 		if(!state.board.isBoardContiguous()) {
 			throw new HantoException("Illegal move:  pieces must retain a contiguous group!");
 		}
 	}
 	
 	/**
 	 * Set whether or not the game has ended based on the current move result
 	 * @param res Result to determine game's ending state
 	 */
 	protected void determineIfGameHasEnded(MoveResult res)
 	{
 		state.setGameOver(res == MoveResult.DRAW || 
 				res == MoveResult.BLUE_WINS || res == MoveResult.RED_WINS);
 	}
 }

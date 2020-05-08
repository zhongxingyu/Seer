 /*******************************************************************************
  * This file is used in CS4233, Object-oriented Analysis and Design
  * 
  * Copyright (c) 2011 Worcester Polytechnic Institute. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Author: gpollice, Andrew Hurle
  *******************************************************************************/
 package strategy.alpha;
 
 import strategy.*;
 
 /**
  * The main Game for AlphaStrategy. All interactions with the game go through this class.
  * 
  * @author gpollice, Andrew Hurle
  * @version Aug 29, 2011
  */
 public class AlphaStrategyGame
 {
 
 	private static final int width = 2;
 	private static final int height = 2;
	private final Piece[][] board = new Piece[height][width];
 	private PlayerColor winner;
 	private PlayerColor turnColor;
 	
 	/**
 	 * Accessor for getting a piece at a specific row and column.
 	 * 
 	 * @param row
 	 *            the row index of the piece
 	 * @param col
 	 *            the column index of the piece
 	 * @return the piece at the specified row and column
 	 * @throws StrategyException if coordinates are out-of-bounds
 	 */
 	public Piece getPieceAt(int row, int col) throws StrategyException
 	{
 		if(row >= height || row < 0) {
 			throw new StrategyException("row must be greater than 0 and less than " + height);
 		}
 		if(col >= width || col < 0) {
 			throw new StrategyException("col must be greater than 0 and less than " + width);
 		}
 		return board[row][col];
 	}
 
 	/**
 	 * Initialize the game.
 	 */
 	public void initialize()
 	{
 		board[0][0] = new Piece(PieceType.SCOUT, PlayerColor.RED);
 		board[0][1] = new Piece(PieceType.FLAG, PlayerColor.RED);
 		board[1][0] = new Piece(PieceType.FLAG, PlayerColor.BLUE);
 		board[1][1] = new Piece(PieceType.SCOUT, PlayerColor.BLUE);
 		turnColor = PlayerColor.RED;
 		winner = null;
 	}
 
 	/**
 	 * Move the piece from (fromRow, fromCol) to (toRow, toCol). Resolve the battle if necessary.
 	 * 
 	 * @param fromRow
 	 *            source row
 	 * @param fromCol
 	 *            source column
 	 * @param toRow
 	 *            destination row
 	 * @param toCol
 	 *            destination column
 	 * @throws StrategyException
 	 *             if there is an invalid index, the game is over, or the movement is out of turn
 	 */
 	public void move(int fromRow, int fromCol, int toRow, int toCol)
 			throws StrategyException
 	{
 		if(isOver()) {
 			throw new StrategyException("Cannot move after game is over");
 		}
 		
 		if(fromRow == toRow && fromCol == toCol) {
 			throw new StrategyException("Must move to a different space");
 		}
 		
		final Piece from = getPieceAt(fromRow, fromCol);
		final Piece to = getPieceAt(toRow, toCol);
 		
 		//check for null piece here when necessary
 		
 		if(from.getColor() != turnColor) {
 			throw new StrategyException("Cannot move out of turn");
 		}
 		
		final int displacement;
 		try {
 			displacement = getDisplacement(fromRow, fromCol, toRow, toCol);
 		} catch(StrategyException e) {
 			throw new StrategyException("Cannot move diagonally");
 		}
 		
		final int range = from.getType().getRange();
 		if(range != -1 && displacement > range) {
 			throw new StrategyException("Cannot move farther than piece's range");
 		}
 		
 		if(from.getColor() == to.getColor()) {
 			throw new StrategyException("Cannot move to a space containing a friendly unit");
 		}
 		
		if(to == null || canWinAndResolveBattle(fromRow, fromCol, toRow, toCol)) {
 			board[toRow][toCol] = from;
 			board[fromRow][fromCol] = null;
 		}
 		
 		turnColor = turnColor == PlayerColor.RED ? PlayerColor.BLUE : PlayerColor.RED;
 	}
 	
 	/**
 	 * Returns the displacement between two coordinates.
 	 * Is a static method, so does not make sure coordinates are valid for an instance's board.
 	 * @return the displacement between two coordinates
 	 * @throws StrategyException if displacement is diagonal
 	 */
 	private static int getDisplacement(int fromRow, int fromCol, int toRow, int toCol)
 		throws StrategyException
 	{
		final boolean horizontal = fromCol != toCol;
		final boolean vertical = fromRow != toRow;
 		if(horizontal && vertical) {
 			throw new StrategyException("Diagonal displacement is unsupported");
 		}
 		
 		int displacement;
 		if(horizontal) {
 			displacement = fromCol - toCol;
 		}
 		else {
 			displacement = fromRow - toRow;
 		}
 		if(displacement < 0) {
 			//absolute value
 			displacement *= -1;
 		}
 		
 		return displacement;
 	}
 	
 	/**
 	 * Responsible for determining the outcome of a battle between the pieces at
 	 * the given coordinates, removing dead pieces as necessary.
 	 * Checks for winning condition where flag is captured.
 	 * 
 	 * @return True if the attacking piece should be moved onto the defending space after battle
 	 * @throws StrategyException if coordinates are invalid
 	 */
	private boolean canWinAndResolveBattle(int fromRow, int fromCol, int toRow, int toCol)
 		throws StrategyException
 	{
		final Piece attacker = getPieceAt(fromRow, fromCol);
		final Piece defender = getPieceAt(toRow, toCol);
 		
 		/* 
 		 * many more things to check for the final version, but this is
 		 * all that's necessary for Alpha. 
 		 */
 		
 		if(defender.getType() == PieceType.FLAG) {
 			winner = attacker.getColor();
 		}
 		
 		if(attacker.getType().getRank() > defender.getType().getRank()) {
 			board[toRow][toCol] = null;
 			return true;
 		}
 		
 		return false;
 	}
 
 	/**
 	 * @return true if the game is over; false otherwise.
 	 */
 	public boolean isOver()
 	{
		return winner != null;
 	}
 
 	/**
 	 * @return the color of the winner or null if there is none
 	 */
 	public PlayerColor getWinner()
 	{
 		return winner;
 	}
 }

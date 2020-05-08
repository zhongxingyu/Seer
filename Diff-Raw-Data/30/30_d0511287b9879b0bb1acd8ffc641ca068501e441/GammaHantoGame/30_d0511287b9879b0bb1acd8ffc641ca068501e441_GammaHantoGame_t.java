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
 import hanto.common.HantoGame;
 import hanto.studentndemarinis.common.HantoBoard;
 import hanto.studentndemarinis.common.HantoPiece;
 import hanto.util.HantoCoordinate;
 import hanto.util.HantoPieceType;
 import hanto.util.HantoPlayerColor;
 import hanto.util.MoveResult;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * GammaHanto - Extended hanto implementation
  * supporting Butterflies, Sparrows, and movement 
  * of pieces.  Ends after 10 turns or when butterfly 
  * is surrounded.  
  * 
  * @author ndemarinis
  * @version Jan 21, 2013
  */
 
 /*
  * NOTE:  I started GammaHanto by starting with AlphaHanto and MASSIVELY
  * refactoring it.  This felt like the right way to start for me.  
  * If I was totally off base in doing this, I plead ignorance.  
  * 
  * I did, however, start with a completely new set of tests.  
  */
 
 public class GammaHantoGame implements HantoGame {
 
 	private int numMoves; // Total number of moves elapsed in the game so far
 	private HantoPlayerColor currPlayer; // Player that making the current/next move
 	
 	// Number of moves before we MUST place a butterfly
 	private final int NUM_MOVES_PRE_BUTTERFLY = 3;
 	
 	// Collection of pieces representing the board for now
 	HantoBoard board = new HantoBoard();
 	
 	// Map of player colors to their hands (for now)
 	Map<HantoPlayerColor, GammaHantoPlayer> players = 
 			new HashMap<HantoPlayerColor, GammaHantoPlayer>();
 	
 	// NOTE:  CodePro throws a warning here about the missing exception.  
 	// While it's not technically necessary, I'm leaving it since it's in
 	// the interface.  
 	public GammaHantoGame() throws HantoException {
 		this.initialize(HantoPlayerColor.BLUE); // By default, starting player is blue
 	}
 	
 	
 	// NOTE:  CodePro throws a warning here about the missing exception.  
 	// 	While it's not technically necessary, I'm leaving it since it's in
 	// the interface.  
 	@Override
 	public void initialize(HantoPlayerColor firstPlayer) throws HantoException {
 		numMoves = 0;
 		currPlayer = firstPlayer;
 		board = new HantoBoard();
 		
 		// Initialize each player's hand.  
 		players.put(HantoPlayerColor.BLUE, new GammaHantoPlayer());
 		players.put(HantoPlayerColor.RED, new GammaHantoPlayer());
 		
 		// Based on the specification, red can move first even though it's
 		// technically not in the rules if we initialize the board that way,
 		// therefore, I don't see how we can be breaking a rule here.  
 	}
 
 	@Override
 	public MoveResult makeMove(HantoPieceType pieceType, HantoCoordinate from,
 			HantoCoordinate to) throws HantoException 
 	{
 		HantoPiece toRemove = null;
 		boolean isValid = true;
 		
 		// Verify the source piece is valid, if provided.  
 		if(from != null) 
 		{
 			if(!this.doesPieceExistAt(from)) {
 				throw new HantoException("Illegal move:  " +
 						"source piece does not exist on board!");
 			}
 			
 			if(board.getPieceAt(from).getColor() != currPlayer) {
 				throw new HantoException("Illegal move:  your can only move pieces" +
 						"of your own color!");
 			}
 			
 		}
 		
 		// If we find any pieces at the destination, it's not a legal move.  
 		if(this.doesPieceExistAt(to)){
 			throw new HantoException("Illegal move:  can't place a piece " +
 					"on top of an existing piece!");
 		}
 		
 		// Verify this move doesn't _need_ to place the butterfly.  
 		if(!board.containsPiece(currPlayer, HantoPieceType.BUTTERFLY) &&
 				numMoves >= NUM_MOVES_PRE_BUTTERFLY && pieceType != HantoPieceType.BUTTERFLY) {
 			throw new HantoException("Illegal move:  " +
 					"Butterfly must be placed by the foruth turn!");
 		}
 
 		/* **********************************************************
 		 * .. okay, by now, the move is _probably_ valid.  
 		 * We can now TRY to apply it to the board and then make sure
 		 * it doesn't violate the adjacency rules.
 		 * ***********************************************************/ 
 		
 		
 		// If we were moving a piece, remove the old piece from the "board",
 		// but store it in case we need to revert the change.  
 		if(from != null) {
 			toRemove = board.getPieceAt(from);
 			board.remove(from);
 		}
 		
 		// Finally, add the new piece to the board.  
 		board.add(new HantoPiece(currPlayer, pieceType, to));
 		
 		// Now that we've added the piece, check if it doesn't violate the adjacency rules
 		for(HantoPiece p : board)
 		{
 			// If everything is in one contiguous group, we should be able to
 			// pick any piece on the board and find a path from it
 			// to every other piece.  
 			// If one fails, we broke the rules.  
 			isValid = isValid && board.thereExistsPathBetween(to, p);
 		}
 		
 		if(!isValid) // If we violated the adjacency rules
 		{
 			if(toRemove != null) { // Replace the piece we removed (if any)
 				board.add(toRemove);
 			}
 			board.remove(to); // Remove the piece we wanted to place
 			throw new HantoException("Illegal move:  pieces must retain a contiguous group!");
 		}
 		
 		
 		/* ************************************************
 		 * By now, we know the move is valid.  
 		 * ************************************************/
 		
 		
 		// If this move involved placing a new piece, remove it from the player's hand
 		if(from == null) {
 			players.get(currPlayer).removeFromHand(pieceType);
 		}
 		
 		// After placing the current piece, the current player has made a move, 
 		// so switch the next player
 		currPlayer = (currPlayer == HantoPlayerColor.BLUE) ? 
 					HantoPlayerColor.RED : HantoPlayerColor.BLUE;
 		
 		// Check win conditions (max number of moves, butterfly surrounded)
 		MoveResult ret = (++numMoves != 10) ? MoveResult.OK : MoveResult.DRAW;
 		
 		for(HantoPiece p : board.getPiecesOfType(HantoPieceType.BUTTERFLY))
 		{
 			if(board.isSurrounded(p)) {
 				ret = (p.getColor() == HantoPlayerColor.BLUE) ? 
 						MoveResult.RED_WINS : MoveResult.BLUE_WINS;
 			}
 		}
 		
 		// First move is OK if valid, then the game ends in a draw on the second move
 		return ret;
 	}
 
 	/**
 	 * Return a string representing the current state of the board,
 	 * empty string if the board is empty.  
 	 */
 	@Override
 	public String getPrintableBoard() {
 		String ret = "";
 		
 		for(HantoPiece p : board) {
 			ret += (p + "\n");
 		}
 		
 		return ret;
 	}
 
 	
 	/**
 	 * @return true if a piece exists on the board
 	 * @param c coordinate to check for a piece
 	 * 
	 * NOTE:  this name makes sense to me.  
	 * I don't understand how the suggestions in
	 * CodePro's audit rule could make more sense here.   
 	 */
 	public boolean doesPieceExistAt(HantoCoordinate c) {
 		return board.getPieceAt(c) != null;
 	}
 
 	/**
 	 * Add a coordinate to the board
 	 * @param color color of new piece
 	 * @param type type of new piece
 	 * @param c location of new piece
 	 */
 	public void addToBoard(HantoPlayerColor color, HantoPieceType type, HantoCoordinate c) {
 		final HantoPiece p = new HantoPiece(color, type, c);
 		board.add(p);
 	}
 
 	/**
 	 * @return the number of moves made in this game
 	 */
 	public int getNumMoves() {
 		return numMoves;
 	}
 
 
 	/**
 	 * @param numMoves the number of moves to set
 	 */
 	public void setNumMoves(int numMoves) {
 		this.numMoves = numMoves;
 	}
 
 
 	/**
 	 * @return the current player up for a move
 	 */
 	public HantoPlayerColor getCurrPlayer() {
 		return currPlayer;
 	}
 
 
 	/**
 	 * @param currPlayer player set to be next to move
 	 */
 	public void setCurrPlayer(HantoPlayerColor currPlayer) {
 		this.currPlayer = currPlayer;
 	}
 	
 
 	// TODO:  When we know more about the board, I can write
 	// HashCode in such a way that is works with a real
 	// board implementation.  For now, I'm ignoring the warning.
 }

 /*******************************************************************************
  * This file is used in CS4233, Object-oriented Analysis and Design
  *
  * Copyright (c) 2011 Worcester Polytechnic Institute.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Author:
  *    gpollice
  *******************************************************************************/
 package strategy.beta;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.Set;
 
 import strategy.*;
 import strategy.common.RectangularStrategyBoard;
 
 /**
  * Implementation of the BetaStrategy game.
  * @author gpollice, alextc, andrewdhurle, sternrobbinsgabe
  * @version Aug 1, 2011
  */
 public class BetaStrategyGame implements StrategyGame
 {
 	private RectangularStrategyBoard board;
 	private boolean playerCanPlacePiece;
 	private Set<Piece> placedPieces;
 	private static boolean isOver;
 	private static PlayerColor winnerColor;
 	
 	/**
 	 * The possible results of a battle.
 	 */
 	private enum BattleResult {
 		DEFEAT, VICTORY, DRAW
 	}
 	
 	/**
 	 * Default constructor. Initializes the game.
 	 * @throws StrategyException 
 	 */
 	public BetaStrategyGame() throws StrategyException
 	{
 		this(false);
 	}
 	
 	public BetaStrategyGame(boolean playerPlacePiece) throws StrategyException {
 		board = new RectangularStrategyBoard(6, 6);
 		playerCanPlacePiece = playerPlacePiece;
 		initializeGame();
 	}
 	
 	@Override
 	public void initializeGame() throws StrategyException
 	{
 		board.initializeBoard();
 		if (!playerCanPlacePiece) {
 			placePiecesByColor(PlayerColor.RED);
 			placePiecesByColor(PlayerColor.BLUE);
 			placedPieces = null;
 		}
 		else {
 			placedPieces = new HashSet<Piece>();
 		}
 	}
 	
 	@Override
 	public Piece move(Position source, Position destination) throws StrategyException
 	{
 		if(source.equals(destination)) {
 			throw new StrategyException("destination must be different from source");
 		}
 		if(!board.isOccupied(source)) {
 			throw new StrategyException("source must be occupied by a piece");
 		}
 		
 		final Piece sourcePiece = getPieceAt(source);
 		final Piece destinationPiece = getPieceAt(destination);
 		
 		final int distance = board.getDistance(source, destination); //checks for diagonal
 		final int range = sourcePiece.getType().getRange();
 		if(range >= 0 && distance > range) {
 			throw new StrategyException("Cannot move piece farther than its range");
 		}
 		
 		if(sourcePiece.getColor().equals(destinationPiece.getColor())) {
 			throw new StrategyException("Cannot move onto a friendly piece");
 		}
 		
 		if(board.isOccupiedSpaceBetweenPositions(source, destination)) {
 			throw new StrategyException("Cannot move through occupied spaces");
 		}
 		
 		BattleResult result = BattleResult.VICTORY;
 		if(board.isOccupied(destination)) {
 			result = resolveBattle(sourcePiece, destinationPiece);
 		}
 		
 		switch(result) {
 		case VICTORY:
 			board.putPieceAt(destination, sourcePiece);
 			board.putPieceAt(source, Piece.NULL_PIECE);
 			break;
 		case DRAW:
 			board.putPieceAt(destination, Piece.NULL_PIECE);
 			//fallthrough
 		case DEFEAT:
 			board.putPieceAt(source, Piece.NULL_PIECE);
 			break;
 		}
 		
 		return getPieceAt(destination);
 	}
 
 	/**
 	 * Determines the outcome of a battle between two Pieces.
 	 * 
 	 * @param attacker The attacking Piece
 	 * @param defender The defending Piece
 	 * @return VICTORY if the attacker wins, DEFEAT if the defender wins,
 	 * 		DRAW if both pieces lose
 	 */
 	private static BattleResult resolveBattle(Piece attacker, Piece defender) {
 		if(attacker.getType().equals(PieceType.MINER)
 				&& defender.getType().equals(PieceType.BOMB)) {
 			return BattleResult.VICTORY;
 		}
 		if(attacker.getType().equals(PieceType.SPY)
 				&& defender.getType().equals(PieceType.MARSHAL)) {
 			return BattleResult.VICTORY;
 		}
 		
 		if(attacker.getType().getRank() < defender.getType().getRank()) {
 			return BattleResult.VICTORY;
 		}
 		if(attacker.getType().getRank() == defender.getType().getRank()) {
 			return BattleResult.DRAW;
 		}
		if(defender.getType().equals(PieceType.FLAG)){
			isOver = true;
			winnerColor=attacker.getColor();
		}
 		return BattleResult.DEFEAT;
 	}
 	
 	@Override
 	public boolean isGameOver()
 	{
 		return isOver;
 	}
 
 	@Override
 	public PlayerColor getWinner()
 	{
 		return winnerColor;
 	}
 
 	@Override
 	public Piece getPieceAt(Position position) throws StrategyException
 	{
 		return board.getPieceAt(position);
 	}
 	
 	/**
 	 * Get the number of pieces of both colors currently on the board
 	 * 
 	 * @return numPieces
 	 */
 	public int getNumPiecesOnBoard() {
 		int numPieces = 0;
 		final Iterator<Piece> iter = board.iterator();
 		
 		while(iter.hasNext()) {
 			if (!iter.next().equals(Piece.NULL_PIECE)) {
 				numPieces++;
 			}
 		}
 		
 		return numPieces;
 	}
 	
 	private void placePiecesByColor(PlayerColor pieceColor) throws StrategyException {
 		for (PieceType type : PieceType.values()) {
 			Piece pieceToPlace = new Piece(type, pieceColor);
 			randomlyPlacePiece(pieceToPlace);
 		}
 	}
 	
 	private void randomlyPlacePiece(Piece pieceToPlace) throws StrategyException {
 		final Random randGen = new Random();
 		Position randPos = null;
 		
 		
 		if (pieceToPlace.getColor() == PlayerColor.RED) {
 			do {
 				randPos = new Position(randGen.nextInt(2), randGen.nextInt(board.getNumCols()));
 			} while (board.isOccupied(randPos));
 		}
 		else {
 			do {
 				randPos = new Position((board.getNumRows() - 1) - randGen.nextInt(2), 
 						randGen.nextInt(board.getNumCols()));
 			} while (board.isOccupied(randPos));
 		}
 			
 		board.putPieceAt(randPos, pieceToPlace);
 	}
 	
 	/**
 	 * Set the current StrategyBoard to the given StrategyBoard
 	 * 
 	 * @param board
 	 */
 	protected void setBoard(StrategyBoard board)
 	{
 		this.board = (RectangularStrategyBoard) board;
 	}
 	
 	/**
 	 * Return this BetaStrategyGame's RectangularStrategyBoard
 	 * 
 	 * @return board
 	 */
 	protected RectangularStrategyBoard getBoard() {
 		return board;
 	}
 	
 	@Override
 	public boolean equals(Object other)
 	{
 		if(this == other) {
 			return true;
 		}
 		if (other instanceof BetaStrategyGame) {
 			final BetaStrategyGame that = (BetaStrategyGame) other;
 			return board.equals(that.getBoard());
 		}
 		return false;
 	}
 	
 	@Override
 	public int hashCode()
 	{
 		return 1;
 	}
 
 	public void playerPlacePiece(Position position, Piece piece) throws StrategyException {
 		if (!playerCanPlacePiece) {
 			throw new StrategyException("Player not allowed to place Piece");
 		}
 		if (board.isOccupied(position)) {
 			throw new StrategyException("Player not allowed to place Piece in an occupied space");
 		}
 		if(!placedPieces.add(piece)) {
 			throw new StrategyException("That Piece has already been placed");
 		}
 		
 		board.putPieceAt(position, piece);
 	}
 
 }

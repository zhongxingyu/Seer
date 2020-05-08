 /*******************************************************************************
  * This file is used in CS4233, Object-oriented Analysis and Design
  * 
  * Copyright (c) 2011 Worcester Polytechnic Institute. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Author: Alex Thornton-Clark, Andrew Hurle, Gabriel Stern-Robbins
  *******************************************************************************/
 
 package strategy.delta;
 
 import strategy.GameState;
 import strategy.Piece;
 import strategy.PlayerColor;
 import strategy.Position;
 import strategy.StrategyGame;
 import strategy.common.PiecePositionAssociation;
 import strategy.StrategyBoard;
 
 /**
  * The main Game for DeltaStrategy. All interactions with the game go through this class.
  * 
  * @author Andrew Hurle, Alex Thornton-Clark, Gabriel Stern-Robbins
  * @version Sep 30, 2011
  */
 public class DeltaStrategyGame implements StrategyGame {
 
 	private final GameState state;
 	private final DeltaInitializationStrategy init;
 	
 	/**
 	 * Constructs a DeltaStrategyGame.
 	 * Calls the initializeGame method to place the Pieces in startingRedPieces
 	 * and startingBluePieces on the board
 	 * 
 	 * @param startingRedPieces 
 	 * 			the array of PiecePositionAssociations used to place the red Pieces on the board
 	 * @param startingBluePieces 
 	 * 			the array of PiecePositionAssociations used to place the blue Pieces on the board
 	 */
 	public DeltaStrategyGame(PiecePositionAssociation[] startingRedPieces, 
 			PiecePositionAssociation[] startingBluePieces) {
 		state = new GameState();
 		init = new DeltaInitializationStrategy(state);
 		initializeGame(startingRedPieces, startingBluePieces);
 	}
 	
 	@Override
 	public Piece getPieceAt(Position position) {
 		return state.getBoard().getPieceAt(position);
 	}
 
 	@Override
 	public PlayerColor getWinner() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void initializeGame() {
 		init.initialize();
 
 	}
 	
 	/**
 	 * Places the Pieces in startingRedPieces and startingBluePieces on the board
 	 * 
 	 * @param startingRedPieces 
 	 * 		the array of PiecePositionAssociations used to place the red Pieces on the board
 	 * @param startingBluePieces 
 	 * 		the array of PiecePositionAssociations used to place the blue Pieces on the board 
 	 */
 	public void initializeGame(PiecePositionAssociation[] startingRedPieces, 
 			PiecePositionAssociation[] startingBluePieces) {
 		init.initialize(startingRedPieces, startingBluePieces);
 	}
 
 	@Override
 	public boolean isGameOver() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public Piece move(Position source, Position destination) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	/**
 	 * This method is used for testing purposes only.
 	 * 
 	 * @param board the board to set
 	 */
 	protected void setBoard(StrategyBoard board)
 	{
		// Set the board that you use to the board supplied. This might
		// be implemented as simply as the following if you have a field
		// called "board."
		// this.board = (RectangularStrategyBoard)board;
 	}
 
 }

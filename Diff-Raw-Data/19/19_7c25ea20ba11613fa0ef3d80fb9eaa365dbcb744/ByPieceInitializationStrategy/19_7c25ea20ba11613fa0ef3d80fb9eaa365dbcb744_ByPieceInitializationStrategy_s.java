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
 
 package strategy.beta;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import strategy.GameState;
 import strategy.Piece;
 import strategy.PlayerColor;
 import strategy.Position;
 import strategy.StrategyException;
 
 public class ByPieceInitializationStrategy extends BetaInitializationStrategy {
 
 	private Set<Piece> placedPieces;
 	
 	public ByPieceInitializationStrategy(GameState state) {
 		super(state);
 	}
 
 	@Override
 	public void initialize() {
 		super.initialize();
 		placedPieces = new HashSet<Piece>();
 	}
 	
 	@Override
 	public void playerPlacePiece(Position position, Piece piece) throws StrategyException {
 		if (state.getBoard().isOccupied(position)) {
 			throw new StrategyException("Player not allowed to place Piece in an occupied space");
 		}
 		if (!placedPieces.add(piece)) {
 			throw new StrategyException("That Piece has already been placed");
 		}
 		if (piece.getColor() == PlayerColor.RED && position.getRow() > 1) {
 			throw new StrategyException("Given position is outside RED player's setup zone");
 		}
 		if (piece.getColor() == PlayerColor.BLUE && position.getRow() < 4) {
 			throw new StrategyException("Given position is outside BLUE player's setup zone");
 		}
 		state.getBoard().putPieceAt(position, piece);
 	}
 
 }

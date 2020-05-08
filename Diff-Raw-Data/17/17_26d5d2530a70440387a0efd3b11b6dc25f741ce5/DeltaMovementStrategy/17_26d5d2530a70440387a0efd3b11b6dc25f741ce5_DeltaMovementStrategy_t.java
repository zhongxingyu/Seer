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
 
 import java.util.Iterator;
 
 import strategy.GameState;
 import strategy.Piece;
 import strategy.PlayerColor;
 import strategy.Position;
 import strategy.StrategyException;
 import strategy.beta.BetaMovementStrategy;
 
/**
 * Defines the makeMove method for the DeltaStrategyGame
 * 
 * @author Alex Thortnon-Clark, Andrew Hurle, Gabriel Stern-Robbins
 * @version Oct 1, 2011
 */
 public class DeltaMovementStrategy extends BetaMovementStrategy {
 
 	public DeltaMovementStrategy(GameState state) {
 		super(state);
 	}
 
 	@Override
 	public Piece makeMove(Position source, Position destination)
 			throws StrategyException {
 		if(state.getBoard().getPieceAt(destination).equals(Piece.WATER_PIECE)) {
 			throw new StrategyException("cannot move onto water piece");
 		}
 		final Piece victor = super.makeMove(source, destination);
 		checkMoveable();
 		return victor;
 	}
 	
 	private void checkMoveable() {
 		boolean redMoveable = false;
 		boolean blueMoveable = false;
 		final Iterator<Piece> iter = state.getBoard().iterator();
 		
 		while(iter.hasNext()) {
 			Piece tempPiece = iter.next();
 			if(!tempPiece.equals(Piece.NULL_PIECE) && 
 					!tempPiece.equals(Piece.WATER_PIECE)) {
 				if(tempPiece.getColor().equals(PlayerColor.RED) && 
 						tempPiece.getType().isMoveable()) {
 					redMoveable = true;
 				}
 				else if(tempPiece.getColor().equals(PlayerColor.BLUE) && 
 						tempPiece.getType().isMoveable()) {
 					blueMoveable = true;
 				}
 			}
 		}
 		
 		if(!redMoveable && !blueMoveable) {
 			state.setOver(true);
 		}
 	}
 }

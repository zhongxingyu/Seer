 /*******************************************************************************
  * This file is used in CS4233, Object-oriented Analysis and Design
  * 
  * Copyright (c) 2011 Worcester Polytechnic Institute. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Author: gpollice
  *******************************************************************************/
 package strategy;
 
 import strategy.PieceType;
 import strategy.PlayerColor;
 
 /**
  * The Piece class represents a piece in the game. One instance of this class is created for each
  * piece on the board.
  * 
  * @author gpollice
  * @version Jul 27, 2011
  */
 public class Piece
 {
 	private final PieceType myType;
 	private final PlayerColor myColor;
 	
 	public static final Piece NULL_PIECE = new Piece(null, null);
 
 	/**
 	 * Constructor that takes the type and color of this piece.
 	 * 
 	 * @param type
 	 * @param color
 	 */
 	public Piece(PieceType type, PlayerColor color)
 	{
 		myType = type;
 		myColor = color;
 	}
 
 	/**
 	 * @return tye type of this piece
 	 */
 	public PieceType getType()
 	{
 		return myType;
 	}
 
 	/**
 	 * @return the color of this piece
 	 */
 	public PlayerColor getColor()
 	{
 		return myColor;
 	}
 
 	@Override
 	public boolean equals(Object other)
 	{
		if (other.getClass() == Piece.class) {
			return (myColor.equals(((Piece) other).getColor()) && myType.equals(((Piece) other).getType()));
 		}
		else {
			return false;
 		}
 	}
 
 	@Override
 	public int hashCode()
 	{
 		return 1;
 	}
 
 	/**
 	 * @return true if the piece can be moved
 	 */
 	public boolean isMoveable()
 	{
 		return false;
 	}
 }

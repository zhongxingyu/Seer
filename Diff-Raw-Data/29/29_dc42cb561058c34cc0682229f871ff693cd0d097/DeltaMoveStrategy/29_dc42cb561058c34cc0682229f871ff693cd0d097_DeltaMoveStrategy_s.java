 /*******************************************************************************
  * This files was developed for CS4233: Object-Oriented Analysis & Design.
  * The course was taken at Worcester Polytechnic Institute.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package hanto.studentccasola.tournament;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import hanto.studentccasola.common.AbstractHantoGame;
 import hanto.studentccasola.common.HantoMoveStrategy;
 import hanto.studentccasola.common.HexCell;
 import hanto.studentccasola.common.HexCoordinate;
 import hanto.tournament.HantoMoveRecord;
 import hanto.util.HantoPieceType;
 import hanto.util.HantoPlayerColor;
 
 /**
  * This HantoMoveStrategy uses a very basic algorithm to generate
  * valid Hanto moves. It always places the butterfly first. Then it
  * always places all pieces (if possible) before attempting to
  * move any. Once it must move, it tries to move Sparrows to be adjacent
  * to the opponent's butterfly, otherwise it randomly moves a crab.
  *
  * @author Chris Casola
  * @version Feb 27, 2013
  */
 public class DeltaMoveStrategy extends HantoMoveStrategy
 {
 	private HexCoordinate butterfly = null;
 	private HexCoordinate opponentButterfly = null;
 
 	/**
 	 * Construct the DeltaMoveStrategy
 	 * @param game the HantoGame used to record the game
 	 * @param playerColor the color of this player
 	 */
 	public DeltaMoveStrategy(AbstractHantoGame game, HantoPlayerColor playerColor)
 	{
 		super(game, playerColor);
 	}
 
 	/* 
 	 * @see hanto.studentccasola.tournament.HantoMoveStrategy#getMove()
 	 */
 	@Override
 	public HantoMoveRecord getMove(HantoMoveRecord opponentsMove)
 	{
 		HantoMoveRecord result = null;
 		
 		// Record the location of the opponent's butterfly
 		if (opponentsMove != null && opponentsMove.getPiece() == HantoPieceType.BUTTERFLY)
 		{
 			opponentButterfly = new HexCoordinate(opponentsMove.getTo());
 		}
 
 		// Place butterfly if not already placed
 		if (butterfly == null)
 		{
 			result = placeButterfly(opponentsMove);
 		}
 
 		// If unable to place a butterfly, place another type of piece
 		if (result == null && game.getState().getPieces().get(playerColor).size() > 0)
 		{
 			result = placePiece();
 		}
 
 		// If unable to place any pieces, move a piece
 		if (result == null)
 		{
 			result = movePiece();
 		}
 
 		// Return the move that was chosen
 		return result;
 	}
 	
 	
 	/**
 	 * Returns a move that will place the butterfly on the board
 	 * 
 	 * @param opponentsMove the last move by the opponent
 	 * @return a move that will place the butterfly on the board or
 	 * null if it is not possible
 	 */
 	protected HantoMoveRecord placeButterfly(HantoMoveRecord opponentsMove)
 	{
 		HantoMoveRecord move = null;
 		HantoPieceType piece = HantoPieceType.BUTTERFLY;
 		HexCoordinate dest = null;
 		
 		// Other player went first, so find a place for the butterfly
 		if (opponentsMove != null)
 		{
 			dest = getValidPlaceLocation(piece);
 		}
 		
 		// This player goes first, so place the butterfly at the origin
 		if (dest == null)
 		{
 			dest = new HexCoordinate(0,0);
 		}
 		
 		// Construct a HantoMoveRecord for the chosen destiation cell
 		move = new HantoMoveRecord(piece, null, dest);
 		butterfly = dest;
 
 		return move;
 	}
 	
 	/**
 	 * Returns a move that will place a piece on the board at random
 	 * 
 	 * @return a move that will place a piece on the board at random or
 	 * null if unable to place a piece
 	 */
 	protected HantoMoveRecord placePiece()
 	{
 		HantoMoveRecord move = null;
 		
 		// Get an available piece to place
 		HantoPieceType piece = game.getState().getPieces().get(playerColor).get(0);
 		
 		// Find a place to put the piece
 		HexCoordinate dest = getValidPlaceLocation(piece);
 		
 		// If able to find a place, construct a move record and return
 		if (dest != null)
 		{
 			move = new HantoMoveRecord(piece, null, dest);
 		}
 		return move;
 	}
 	
 	/**
 	 * Returns a move that will move a piece that is already on the board
 	 * to another location. 
 	 * 
 	 * @return a move that will move a piece or null if unable to move a piece
 	 */
 	protected HantoMoveRecord movePiece()
 	{
 		// Get all possible moves
 		List<HantoMoveRecord> possibleMoves = getPossiblePieceMoves();
 
 		// Choose a move at random
 		HantoMoveRecord move = null;
 		if (possibleMoves.size() > 0)
 		{
 			HantoMoveRecord record = null;
 			int movePick = randGen.nextInt(possibleMoves.size());
 			record = possibleMoves.get(movePick);
 			move = record;
 		}
 	
 		return move;
 	}
 	
 	/**
 	 * @return a list of all possible moves that would move a piece
 	 * already on the board
 	 */
 	protected List<HantoMoveRecord> getPossiblePieceMoves()
 	{
 		List<HexCell> cellList = getShuffledBoardCells();
 		List<HantoMoveRecord> possibleMoves = new ArrayList<HantoMoveRecord>();
 		
		//possibleMoves.addAll(getCrabMoves(cellList));
		//possibleMoves.addAll(getSparrowMoves(cellList));
 		
		possibleMoves.addAll(getPieceMoves(cellList, HantoPieceType.CRAB, null));
		possibleMoves.addAll(getPieceMoves(cellList, HantoPieceType.SPARROW,
				(opponentButterfly != null) ? opponentButterfly : butterfly));
		
		return possibleMoves;
	}
	
	protected List<HantoMoveRecord> getPieceMoves(List<HexCell> cellList, HantoPieceType pieceType, HexCoordinate target)
	{
		List<HantoMoveRecord> possibleMoves = new ArrayList<HantoMoveRecord>();
		for (HexCell cell : cellList)
		{
			if (cell.getPlayer() == playerColor && cell.getPiece() == pieceType)
			{
				target = (target == null) ? cell.getCoordinate() : target;
				checkAdjacentCells(cell, target, possibleMoves);
			}
		}

 		return possibleMoves;
 	}
 	
	/*
 	protected List<HantoMoveRecord> getCrabMoves(List<HexCell> cellList)
 	{
 		List<HantoMoveRecord> possibleMoves = new ArrayList<HantoMoveRecord>();
 
 		for (HexCell cell : cellList)
 		{
 			if (cell.getPlayer() == playerColor && cell.getPiece() == HantoPieceType.CRAB)
 			{
 				checkAdjacentCells(cell, cell.getCoordinate(), possibleMoves);
 			}
 		}
 
 		return possibleMoves;
 	}
 	
 	protected List<HantoMoveRecord> getSparrowMoves(List<HexCell> cellList)
 	{
 		List<HantoMoveRecord> possibleMoves = new ArrayList<HantoMoveRecord>();
 		
 		for (HexCell cell : cellList)
 		{
 			if (cell.getPlayer() == playerColor && cell.getPiece() == HantoPieceType.SPARROW)
 			{
 				HexCoordinate target = (opponentButterfly != null) ? opponentButterfly : butterfly;
 				checkAdjacentCells(cell, target, possibleMoves);
 			}
 		}
 		return possibleMoves;
	}*/
 	
 	protected void checkAdjacentCells(HexCell source, HexCoordinate target, List<HantoMoveRecord> possibleMoves)
 	{
 		for (HexCoordinate coord : target.getAdjacentCoordinates())
 		{
 			if (game.getState().getBoard().getCellAtCoordinate(coord) == null &&
 					game.getRuleset().isValidMove(source.getPiece(), source.getCoordinate(), coord))
 			{
 				possibleMoves.add(new HantoMoveRecord(source.getPiece(), source.getCoordinate(), coord));
 			}
 		}
 	}
 }

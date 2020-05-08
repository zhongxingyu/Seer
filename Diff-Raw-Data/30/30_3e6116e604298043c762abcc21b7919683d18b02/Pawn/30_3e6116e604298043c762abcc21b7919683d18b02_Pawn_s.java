 package com.petrifiednightmares.singularityChess.pieces;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import com.petrifiednightmares.singularityChess.GameException;
 import com.petrifiednightmares.singularityChess.R;
 import com.petrifiednightmares.singularityChess.logic.Board;
 import com.petrifiednightmares.singularityChess.logic.Game;
 import com.petrifiednightmares.singularityChess.logic.Square;
 import com.petrifiednightmares.singularityChess.ui.SUI;
 import com.petrifiednightmares.singularityChess.utilities.SingularBitmapFactory;
 
 public class Pawn extends AbstractPiece
 {
 	private boolean canJump = true;
 	private boolean jumped = false;
 
 	public Pawn(Game game, Square location, boolean isWhite)
 	{
 		super(game, location, isWhite, isWhite ? "\u2659" : "\u265F", SingularBitmapFactory
 				.buildSingularScaledBitmap(game.getDrawingPanel().getResources(), isWhite ? R.drawable.pawn
 						: R.drawable.black_pawn, SUI.PIECE_SIZE, SUI.PIECE_SIZE), PieceType.Pawn);
 	}
 
 	public Set<Square> getMoves() throws GameException
 	{
 		Set<Square> moves = new HashSet<Square>();
 		moves.addAll(game.getBoard().getPawnMoves(this));
 
 		moves.addAll(game.getBoard().getPawnCaptures(this));
 
 		return moves;
 	}
 
 	public AbstractPiece makeMove(Square target)
 	{
 		canJump = false;
 
 		return super.makeMove(target);
 	}
 
 	public boolean canJump()
 	{
		return canJump;
 	}
 
 	public boolean isJumped()
 	{
 		return jumped;
 	}
 
 	public static AbstractPiece[] makePawns(Game game, boolean isWhite)
 	{
 		AbstractPiece[] pawns = new AbstractPiece[8];
 
 		// 8 pawns
 		for (int i = 0; i < 8; i++)
 		{
 			char file = (char) ('a' + i);
 			int rank = isWhite ? 2 : Board.boardRanks[file - 'a'] - 1;
 
 			Square location = game.getBoard().getSquares().get(file + "" + rank);
 			Pawn p = new Pawn(game, location, isWhite);
 			pawns[i] = p;
 			location.addPiece(p);
 		}
 
 		return pawns;
 	}
 
 	public AbstractPiece promote(AbstractPiece.PieceType pieceType)
 	{
 		AbstractPiece newPiece;
 		switch (pieceType)
 		{
 		case Knight:
 			newPiece = new Knight(game, getLocation(), isWhite);
 			break;
 		case Queen:
 			newPiece = new Queen(game, getLocation(), isWhite);
 			break;
 		default:
 			newPiece = new Queen(game, getLocation(), isWhite);// just default
 																// to queen if
 																// they try to
 																// be clever
 			break;
 		}
 		getLocation().addPiece(newPiece);
 		return newPiece;
 	}
 
 	public String toString()
 	{
 		return "Pawn";
 	}
 }

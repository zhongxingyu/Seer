 package com.nanu.chess.pieces;
 
 import java.util.ArrayList;
 
 import com.nanu.chess.board.Board;
 import com.nanu.chess.board.Square;
 import com.nanu.chess.support.Team;
 
 public class Knight extends Piece {
 
 	public Knight(Team team) {
 		super(team);
 	}
 
 	public ArrayList<Square> getLegalMoves(Board b, Square s) {
 		ArrayList<Square> moves = new ArrayList<Square> ();
 		Square temp;
 		for ( int i = -2; i <= 2; i++ ) {
 			for ( int j = -2; j <= 2; j++ ) {
				if ( i != 0 && j != 0 && i != j && b.isValid(j, i) ) {
					temp = b.getSquare(s.getY()+j,s.getX()+i);
 					if ( temp != null ) {
 						if ( temp.getPiece() == null )
 							moves.add(temp);
 						else if ( !temp.getPiece().getTeam().equals(_team) )
 							moves.add(temp);
 					}
 				}
 			}
 		}
 		return moves;
 	}
 
 }

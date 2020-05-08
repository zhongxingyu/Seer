 package com.nanu.chess.pieces;
 
 import java.util.ArrayList;
 
 import com.nanu.chess.board.Board;
 import com.nanu.chess.board.Square;
 import com.nanu.chess.support.Team;
 
 public class Pawn extends Piece {
 	
 	public Pawn(Team team) {
 		super(team);
 	}
 	
 	public ArrayList<Square> getLegalMoves(Board b, Square s) {
 		Square temp = s;
 		ArrayList<Square> moves = new ArrayList<Square>();
//		Team team = temp.getPiece().getTeam();
//		if(team.equals(Team.WHITE) ) {
			if(b.getN(s) != null && b.getN(s).getPiece() == null ) {
				moves.add(b.getN(s));
				if(s.getY() == 6 && b.getN(b.getN(s)).getPiece() == null) {
 					moves.add(b.getN(b.getN(temp)));
 				}
 			}
 			if( b.getNE(temp) != null &&
 					b.getNE(temp).getPiece() != null &&
					!b.getNE(temp).getPiece().getTeam().equals(this.getTeam())) {
 				moves.add(b.getNE(temp));
 			}
 			if( b.getNW(temp) != null && 
 					b.getNW(temp).getPiece() != null &&
					!b.getNW(temp).getPiece().getTeam().equals(this.getTeam())) {
 				moves.add(b.getNW(temp));
 			}
//		}
/*		else {
 			if(b.getS(temp) != null) {
 				moves.add(b.getS(temp));
 				if(temp.getY() == 7 && temp.getPiece() == null) {
 					moves.add(b.getS(b.getS(temp)));
 				}
 			}
 			if(b.getNE(temp).getPiece().getTeam().equals(Team.WHITE)) {
 				moves.add(b.getSE(temp));
 			}
 			if(b.getNW(temp).getPiece().getTeam().equals(Team.WHITE)) {
 				moves.add(b.getSW(temp));
 			}
		}*/
 		return moves;
 	}
 
 }

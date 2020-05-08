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
 		Team team = temp.getPiece().getTeam();
 		if(team.equals(Team.WHITE) ) {
			if(b.getN(temp) != null) {
 				moves.add(b.getN(temp));
 				if(temp.getY() == 2 && temp.getPiece() == null) {
 					moves.add(b.getN(b.getN(temp)));
 				}
 			}
			if(b.getNE(temp).getPiece().getTeam().equals(Team.BLACK)) {
 				moves.add(b.getNE(temp));
 			}
			if(b.getNW(temp).getPiece().getTeam().equals(Team.BLACK)) {
 				moves.add(b.getNW(temp));
 			}
 		}
 		else {
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
 		}
 		return moves;
 	}
 
 }

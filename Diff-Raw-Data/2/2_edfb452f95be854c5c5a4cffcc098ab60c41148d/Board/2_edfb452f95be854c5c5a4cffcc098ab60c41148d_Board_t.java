 package com.nanu.chess.board;
 
 import java.util.ArrayList;
 
 import com.nanu.chess.pieces.Bishop;
 import com.nanu.chess.pieces.King;
 import com.nanu.chess.pieces.Knight;
 import com.nanu.chess.pieces.Pawn;
 import com.nanu.chess.pieces.Queen;
 import com.nanu.chess.pieces.Rook;
 import com.nanu.chess.support.Team;
 
 public class Board {
 	
 	private ArrayList< ArrayList<Square> > _grid;
 	
 	public Board() {
 		_grid = new ArrayList< ArrayList<Square> > ();
 		ArrayList<Square> temp;
 		for ( int i = 0; i < 8; i++ ) {
 			temp = new ArrayList<Square> ();
 			for ( int j = 0; j < 8; j++ ) {
 				temp.add(new Square(j,i));
 			}
 			_grid.add(temp);
 		}
 	}
 	
 	public Square getN(Square s) {
 		if ( 0 <= s.getY() - 1 )
 			return _grid.get(s.getY() - 1).get(s.getX());
 		else
 			return null;
 	}
 	
 	public Square getNE(Square s) {
 		if ( 0 <= s.getY() - 1 && _grid.get(0).size() > s.getX() + 1 )
 			return _grid.get(s.getY() - 1).get(s.getX() + 1);
 		else
 			return null;
 	}
 	
 	public Square getE(Square s) {
 		if ( _grid.get(0).size() > s.getX() + 1 )
			return _grid.get(s.getY()).get(s.getX() + 1);
 		else
 			return null;
 	}
 	
 	public Square getSE(Square s) {
 		if ( _grid.size() > s.getY() + 1 && _grid.get(0).size() > s.getX() + 1 )
 			return _grid.get(s.getY() + 1).get(s.getX() + 1);
 		else
 			return null;
 	}
 	
 	public Square getS(Square s) {
 		if ( _grid.size() > s.getY() + 1 )
 			return _grid.get(s.getY() + 1).get(s.getX());
 		else
 			return null;
 	}
 	
 	public Square getSW(Square s) {
 		if ( _grid.size() > s.getY() + 1 && 0 <= s.getX() - 1 )
 			return _grid.get(s.getY() + 1).get(s.getX() - 1);
 		else
 			return null;
 	}
 	
 	public Square getW(Square s) {
 		if ( 0 <= s.getX() - 1 )
 			return _grid.get(s.getY()).get(s.getX() - 1);
 		else
 			return null;
 	}
 	
 	public Square getNW(Square s) {
 		if ( 0 <= s.getY() - 1 && 0 <= s.getX() - 1 )
 			return _grid.get(s.getY() - 1).get(s.getX() - 1);
 		else
 			return null;
 	}
 	
 	public Square getSquare(int x, int y) {
 		if ( isValid(x,y) )
 			return _grid.get(y).get(x);
 		else
 			return null;
 	}
 	
 	public ArrayList< ArrayList<Square> > getGrid() {
 		return _grid;
 	}
 	
 	public void movePiece(Square start, Square end) {
 		end.setPiece(start.getPiece());
 		start.setPiece(null);
 	}
 	
 	public boolean isValid(Square s) {
 		if ( s.getY() >= 0 && s.getY() < _grid.size() && s.getX() >= 0 && s.getY() < _grid.size() )
 			return true;
 		else
 			return false;
 	}
 	
 	public boolean isValid(int x, int y) {
 		if ( y >= 0 && y < _grid.size() && x >= 0 && x < _grid.size() )
 			return true;
 		else
 			return false;
 	}
 	
 	public void resetGrid(Team team) {
 		for ( ArrayList<Square> row : _grid)
 			for ( Square square : row )
 				square.setPiece(null);
 		Team otherTeam = team.equals(Team.WHITE) ? Team.BLACK : Team.WHITE;
 		for ( int i = 0; i < 8; i++ ) {
 			_grid.get(1).get(i).setPiece(new Pawn(otherTeam));
 			_grid.get(6).get(i).setPiece(new Pawn(team));
 		}
 		ArrayList<Square> row = _grid.get(0);
 		row.get(0).setPiece(new Rook(otherTeam));
 		row.get(1).setPiece(new Knight(otherTeam));
 		row.get(2).setPiece(new Bishop(otherTeam));
 		row.get(5).setPiece(new Bishop(otherTeam));
 		row.get(6).setPiece(new Knight(otherTeam));
 		row.get(7).setPiece(new Rook(otherTeam));
 		
 		row = _grid.get(7);
 		row.get(0).setPiece(new Rook(team));
 		row.get(1).setPiece(new Knight(team));
 		row.get(2).setPiece(new Bishop(team));
 		row.get(5).setPiece(new Bishop(team));
 		row.get(6).setPiece(new Knight(team));
 		row.get(7).setPiece(new Rook(team));
 
 		if ( team.equals(Team.WHITE) ) {
 			_grid.get(0).get(3).setPiece(new Queen(otherTeam));
 			_grid.get(0).get(4).setPiece(new King(otherTeam));
 			_grid.get(7).get(3).setPiece(new Queen(team));
 			_grid.get(7).get(4).setPiece(new King(team));
 		}
 		else {
 			_grid.get(0).get(4).setPiece(new Queen(otherTeam));
 			_grid.get(0).get(3).setPiece(new King(otherTeam));
 			_grid.get(7).get(4).setPiece(new Queen(team));
 			_grid.get(7).get(3).setPiece(new King(team));
 		}
 	}
 
 }

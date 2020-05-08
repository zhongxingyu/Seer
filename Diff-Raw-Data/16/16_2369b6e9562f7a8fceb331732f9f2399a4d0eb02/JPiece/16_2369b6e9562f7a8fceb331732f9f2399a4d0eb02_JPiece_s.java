 package edu.touro.cooptetris.pieces;
 
 import java.awt.Color;
 
 public class JPiece extends Piece {
 
 	public JPiece(int x, int y) {
 		super();
		setLocation(x,y);
 		center = squares[1];
 		for (Square square : squares) {
 			square.setColor(Color.BLUE);
 		}
 
 	}
 	public void setLocation(int x, int y){
 		for (int i = 0; i < 3; i++) {
 			Square s = squares[i];
 			s.setY(i * Square.SIDE + y);
 			s.setX(x);
 		}
 
 		Square s = squares[3];
 		s.setY(2 * Square.SIDE  + y);
		s.setX(s.getX() - Square.SIDE + x);
 	}

 }

 package edu.touro.cooptetris;
 
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.inject.Singleton;
 
 import edu.touro.cooptetris.pieces.Piece;
 import edu.touro.cooptetris.pieces.Square;
 
 @Singleton
 public class Board {
 
 	public static final int NUM_ROWS = 20;
 	public static final int NUM_COLUMNS = 11;
 
 	private List<Square[]> squares;
 
 	public Board() {
 		squares = new ArrayList<Square[]>();
 
 		for (int i = 0; i < NUM_ROWS; i++) {
 			squares.add(new Square[NUM_COLUMNS]);
 
 		}
 	}
 
 	public List<Square[]> getSquares() {
 		return squares;
 	}
 
 	public boolean isRowFull(int rowNumber) {
 		for (int i = 0; i < squares.get(rowNumber).length; i++) {
 			if (squares.get(rowNumber)[i] == null) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public void landPiece(Piece piece) {
 		for (Square square : piece.getSquares()) {
 			this.setSquareFull(square);
 
 		}
 
 	}
 
 	public void removeFullRows() {
 		Iterator<Square[]> i = squares.iterator();
 		int rowsToAdd = 0;
 		while (i.hasNext()) {
 			Square row[] = i.next();
 			if (isFullRow(row)) {
 				i.remove();
 				rowsToAdd++;
 			}
 		}
 		addBlankRows(rowsToAdd);
 	}
 
 	private void addBlankRows(int rowsToAdd) {
 		for (int i = 0; i < rowsToAdd; i++) {
 			squares.add(new Square[NUM_COLUMNS]);
 		}
 	}
 
 	private boolean isFullRow(Square[] row) {
 		for (Square s : row) {
 			if (s == null) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public void removeRow(int rowNumber) {
 		squares.remove(rowNumber);
 		squares.add(new Square[NUM_COLUMNS]);
 	}
 
 	public void setSquareEmpty(int rowNumber, int colNumber) {
 		squares.get(rowNumber)[colNumber] = null;
 	}
 
 	public void setSquareFull(Square square) {
 		int rowNumber = square.getY() / Square.SIDE;
 		int colNumber = square.getX() / Square.SIDE;
 
 		squares.get(rowNumber)[colNumber] = square;
 	}
 
 	public void setSquares(ArrayList<Square[]> squares) {
 		this.squares = squares;
 	}
 
 	public void setSquaresArray(Square[] squaresArray, int numRow) {
 		squares.set(numRow, squaresArray);
 	}
 
 	public boolean willCollideWithFloorLeft(Piece piece) {
 		for (Square square : piece.getSquares()) {
 			int rowNumber = square.getY() / Square.SIDE;
 			int colNumber = square.getX() / Square.SIDE;
 
 			if (colNumber == 0) {
 				return true;
 			}
 
 			if (squares.get(rowNumber)[colNumber - 1] != null) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean willCollideWithFloorRight(Piece piece) {
 		for (Square square : piece.getSquares()) {
 			int rowNumber = square.getY() / Square.SIDE;
 			int colNumber = square.getX() / Square.SIDE;
 
 			if (colNumber == NUM_COLUMNS - 1) {
 				return true;
 			}
 
 			if (squares.get(rowNumber)[colNumber + 1] != null) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean isFull() {
 		if (squares.get(0)[NUM_COLUMNS / 2] != null) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean willCollideWithFloorVertical(Piece piece) {
 		for (Square square : piece.getSquares()) {
 			int rowNumber = square.getY() / Square.SIDE;
			if (rowNumber == NUM_ROWS - 1) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean willCollideWithLandedPieceVertical(Piece piece) {
 		for (Square square : piece.getSquares()) {
 			int rowNumber = square.getY() / Square.SIDE;
 			int colNumber = square.getX() / Square.SIDE;
			if (squares.get(rowNumber + 1)[colNumber] != null) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void draw(Graphics g) {
 		for (Square[] row : squares) {
 			for (Square s : row) {
 				if (s != null) {
 					s.draw(g);
 				}
 			}
 		}
 	}
 
 	public int getNumRows() {
 		return squares.size();
 	}
 
 }

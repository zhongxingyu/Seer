 package cs213.chess.pieces;
 
 import java.util.ArrayList;
 
 import cs213.chess.controls.Board;
 import cs213.chess.exceptions.IllegalCoordsException;
 import cs213.chess.exceptions.IllegalFileRankException;
 import cs213.chess.utils.Helper;
 
 /**
  * @author Bilal Quadri
  *
  */
 public abstract class Piece {
 	
 	protected char color;
 	protected char symbol;
 	protected char file;
 	protected int rank;
 	protected Board board;
 	protected int timesMoved;
 	
 	protected Piece(char color, char file, int rank, Board board) {
 		this.color = color;
 		this.file = file;
 		this.rank = rank;
 		this.board = board;
 		this.timesMoved = 0;
 	}
 	
 	protected Piece(char color, int i, int j, Board board) {
 		this.color = color;
 		this.board = board;
 		this.timesMoved = 0;
 		
 		try {
			String rankFile = Helper.coordsTofileRank(i, j);
			this.rank = rankFile.charAt(0);
			this.file = rankFile.charAt(1);
 		} catch (IllegalCoordsException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public char getColor() {
 		return this.color;
 	}
 	
 	public char getSymbol() {
 		return this.symbol;
 	}
 	
 	public int getRank() {
 		return this.rank;
 	}
 	
 	public char getFile() {
 		return this.file;
 	}
 	
 	/**
 	 * @return The number of times this piece has moved on the board.
 	 */
 	public int getTimesMoved() {
 		return this.timesMoved;
 	}
 	
 	/**
 	 * @return Whether or not this piece has ever moved on the board.
 	 */
 	public boolean hasMoved() {
 		return this.timesMoved == 0;
 	}
 	
 	public int incrementTimesMoved() {
 		this.timesMoved++;
 		return this.timesMoved;
 	}
 	
 	public String getRankFile(){
 		return this.rank + "" + this.file;
 	}
 	
 	public int[] getCoords() {
 		try {
 			return Helper.fileRankToCoords(this.file, this.rank);
 		} catch (IllegalFileRankException e) {
 			return null;
 		}
 	}
 	
 	public String getIdentifier() {
 		return this.color + "" + this.symbol;
 	}
 	
 	public void setRank(int rank) {
 		this.rank = rank;
 	}
 	
 	public void setFile(char file) {
 		this.file = file;
 	}
 	
 	public abstract ArrayList<String> getValidMoves();
 
 	public boolean canMoveTo(String fileRank) {
 		return this.getValidMoves().contains(fileRank);
 	}
 
 //	public abstract boolean canMoveTo(char rank, int file);
 	
 //	public abstract boolean canMoveTo(int i, int j);
 	
 }

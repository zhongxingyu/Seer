 package main.chess.model;
 
 import java.awt.Point;
 import java.util.Set;
 
 import main.chess.common.Constants;
 import main.chess.common.Constants.ColorEnum;
 
 /**
  * A class to hold all the information about a given chess piece
  * @author Brandon
  *
  */
 public abstract class ChessPiece implements Comparable<ChessPiece> {
 
 	//Id for serializing and hashcode
 	private String id;
 	
 	//Value used (maybe) for ai and sorting
 	private int value;
 
 	//Piece's current location on board
 	private Point location;
 	
 	//Piece's color, black or white. If you don't understand Enums look it up or text me
 	private ColorEnum color;
 	
	protected boolean hasMoved;
 	
 	public ChessPiece(ColorEnum color, Point pos, String id, int value) {
 		this.color = color;
 		this.location = pos;
 		this.id = ((color == ColorEnum.WHITE) ? Constants.WPRE : Constants.BPRE) + id;
 		this.value = value;
 	}
 	
 	/**
 	 * A method to get the locations that this piece can move to, excluding locations that have other pieces at them. 
 	 *                     -----------NOTE---------------
 	 *                     This method must handle pieces blocking other pieces 
 	 * @param b 
 	 * 			the current game board
 	 * @return 
 	 * 			an array of points representing the locations on the board that can be moved to 
 	 */
 	public abstract Set<Point> getMovePositions(Board b);
 	
 	/**
 	 * A method to get all the locations OF PIECES that this piece can attack at it's current location.
 	 * 					----------NOTE----------
 	 * 					Like the getValidMoveLocations method this must handle pieces blocking
 	 * 					each other
 	 * @param b 
 	 * 			the current game board
 	 * @return
 	 * 			a set of the positions of pieces that this piece can attack. Return an empty array if there
 	 * 			aren't any. This will NOT return any NullPieces
 	 */
 	public abstract Set<Point> getAttackPositions(Board b);
 	
 	@Override
 	public int hashCode() {
 		return id.hashCode();
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if (o == this) return true;
 		if (o == null) return false;
 		if (o.getClass() != this.getClass()) return false;
 		
 		ChessPiece otherPiece = (ChessPiece) o;
 		
 		return otherPiece.hashCode() == this.hashCode();
 	}
 	
 	@Override
 	public int compareTo(ChessPiece other) {
 		if (this.value != other.value) {
 			return this.value - other.value;
 		}
 		else {
 			return this.id.compareTo(other.id);
 		}
 	}
 	
 	public boolean isSameSelectedPiece(ChessPiece other) {
 		return this.equals(other) && this.location.equals(other.location);
 	}
 	
 	public void move(Point newLoc) {
 		this.setLocation(newLoc);
 		this.hasMoved = true;
 	}
 
 	public boolean isOpponent(ChessPiece other) {
 		return this.getColor() != other.getColor();
 	}
 	
 	public Point getLocation() {
 		return location;
 	}
 
 	public void setLocation(Point location) {
 		this.location = location;
 	}
 
 	public void setColor(ColorEnum color) {
 		this.color = color;
 	}
 	
 	public ColorEnum getColor() {
 		return color;
 	}
 	
 	public void setId(String id) {
 		this.id = id;
 	}
 	
 	public String getId() {
 		return this.id;
 	}
 	
 	public int getValue() {
 		return this.value;
 	}
 	
 	public void setValue(int value) {
 		this.value = value;
 	}
 }

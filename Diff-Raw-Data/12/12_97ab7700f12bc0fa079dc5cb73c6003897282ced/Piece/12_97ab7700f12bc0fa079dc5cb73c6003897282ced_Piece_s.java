 package eighteen;
 import java.awt.Color;
 import java.util.ArrayList;
 
 public class Piece {
 	public static class adjLoc{//adjacent location
 		int row;
 		int column;
 		public adjLoc(int x, int y) {
 			row = x;
 			column = y;
 		}
 		public adjLoc(Piece piece) {
 			row = piece.row;
 			column = piece.column;
 		}
 	}
 	private enum PieceState{
 		BLACK, WHITE, EMPTY
 	}
 	public int row;
 	public int column;
 	private boolean isEmpty;
 	private Color pieceColor;
 	private PieceState pState;
 	ArrayList<adjLoc> adjacentLocations = new ArrayList<adjLoc>();
 	public Piece(int x, int y, Color c) {
 		row = x;
 		column = y;
 		setColor(c);
 		if(!isValidSpace()) {
 			throw new IndexOutOfBoundsException();
 		}
 		adjacentLocations();
 	}
 	
 	public Piece(Piece a) {
 		this.row = a.row;
 		this.column = a.column;
 	}
 	public void setColor(Color c)
 	{
 		pieceColor=c;
 		if(c == Color.BLACK)
 		{
 			pState=PieceState.BLACK;
 		}
 		else if(c == Color.WHITE)
 		{
 			pState=PieceState.WHITE;
 		}
 		else if(c == Color.GRAY)
 		{
 			pState=PieceState.EMPTY;
 			isEmpty=true;
 		}
 		else
 		{
 			//set some color like yellow or something, leaving this else here anyway
 		}
 		
 	}
 	public Color getColor()
 	{
 		return pieceColor;
 	}
 	public Color getPieceState()
 	{
 		Color c;
 		if(pState==PieceState.BLACK)
 		{
 			c=Color.BLACK;
 		}
 		else if(pState==PieceState.WHITE)
 		{
 			c=Color.WHITE;
 		}
 		else//is empty
 		{
 			c=Color.GRAY;
 		}
 		return c;
 	}
 	public boolean isEmpty()
 	{
 		return isEmpty;
 	}
 	public boolean isValidSpace() {
 		if(row < Board.ROWS && row >= 0 && column < Board.COLUMNS && column >= 0)
 			return true;
 		return false;
 	}
 	
 	public static boolean isValidSpace(int s_row, int s_column) {
 		if(s_row < Board.ROWS && s_row >= 0 && s_column < Board.COLUMNS && s_column >= 0)
 			return true;
 		return false;
 	}
 	
 	public static boolean isValidSpace(adjLoc location) {
 		return isValidSpace(location.row, location.column);
 	}
 	
 	public void adjacentLocations() 
 	{
 		if((row + column) % 2 == 0) {
 			if(isValidSpace(row - 1, column - 1))
 				adjacentLocations.add(new adjLoc(row - 1, column - 1));
 			if(isValidSpace(row - 1, column + 1))
 				adjacentLocations.add(new adjLoc(row - 1, column + 1));
 			if(isValidSpace(row + 1, column - 1))
 				adjacentLocations.add(new adjLoc(row + 1, column - 1));
 			if(isValidSpace(row + 1, column + 1))
 				adjacentLocations.add(new adjLoc(row + 1, column + 1));
 		}
 		if(isValidSpace(row - 1, column))
 			adjacentLocations.add(new adjLoc(row - 1, column));
 		if(isValidSpace(row + 1, column))
 			adjacentLocations.add(new adjLoc(row + 1, column));
 		if(isValidSpace(row, column - 1))
 			adjacentLocations.add(new adjLoc(row, column - 1));
 		if(isValidSpace(row, column + 1))
 			adjacentLocations.add(new adjLoc(row, column + 1));
 	}
 	
 	public boolean equals(Piece a)
 	{
 		if(a.row == row && a.column == column)
 			return true;
 		return false;
 	}
 }

 package edu.mharper.tp2;
 
 import java.awt.Color;
 
 public class GamePiece
 {
 	public static final int FIRST_PLAYER = 1;
 	public static final int SECOND_PLAYER = 0;
 	public static final Color[] POSSIBLE_COLORS = {Color.black, Color.white};
 	private Color color;
 	private int row;
 	private int col;
 	
 	public GamePiece(int whichColor, int startRow, int startCol)
 	{
 		//switch (whichColor) {
 		//case 0:
 			//color = Color.black;
 			//break;
 		//default:
 			//color = Color.white;
 			//break;
 		//}
 		
 		color = POSSIBLE_COLORS[whichColor];
 		row = startRow;
 		col = startCol;
 	}
 	
 	//Changes piece position
 	//Assumed that the move is valid- validity will be checked by GameBoard
 	public void updatePosition(int newRow, int newCol)
 	{
 		row = newRow;
 		col = newCol;
 	}
 
 	public Color getColor() 
 	{
 		return color;
 	}
 
 	public int getRow() 
 	{
 		return row;
 	}
 	
 	public int getColumn()
 	{
 		return col;
 	}
 	
 	public Point getPoint()
 	{
 		return new Point(col, row);
 	}
 	
 	public boolean equals(GamePiece piece)
 	{
 		if(piece == null)
 			return false;
 		
		return(row == piece.getRow() && col == piece.getColumn() && color.equals(piece.getColor()));
 	}
 }

 package edu.mharper.tp2;
 
 import java.awt.Color;
 
 public class GamePiece
 {
 	/*public enum Color
 	{
 		Black,
 		Red
 	}*/
 	
 	private Color color;
 	private int row;
 	private int col;
 	
 	public GamePiece(int whichColor, int startRow, int startCol)
 	{
 		switch (whichColor) {
 		case 0:
 			color = Color.black;
			break;
 		default:
 			color = Color.red;
			break;
 		}
 		row = startRow;
 		col = startCol;
 	}
 	
 	//Changes piece position
 	//Assumed that the move is valid- validity will be checked by GameBoard
 	public void moveTo(int newRow, int newCol)
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
 	
 }

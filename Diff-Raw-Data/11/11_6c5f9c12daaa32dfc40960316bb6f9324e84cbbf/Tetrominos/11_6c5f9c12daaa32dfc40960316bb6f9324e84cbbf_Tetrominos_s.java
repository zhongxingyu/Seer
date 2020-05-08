 /**
  * 
  */
 package com.aj.slick2d.tetris.gameplay;
 
 import org.newdawn.slick.Color;
 
 /**
  * Enum for Tetrominos base settings: relative coordinates, start column, start
  * row and color.
  * 
  * @author BETON
  */
 public enum Tetrominos {
 	ZShape(
 			new Pair[][] {
 					{ new Pair(-1, 0), new Pair(0, 0), new Pair(0, -1), new Pair(1, -1) },
 					{ new Pair(-1, -1), new Pair(-1, 0), new Pair(0, 0), new Pair(0, 1) } }, 
 						2, 4, Color.red), 
 	SShape(
 			new Pair[][] {
 					{ new Pair(-1, -1), new Pair(0, -1), new Pair(0, 0), new Pair(1, 0) },
 					{ new Pair(-1, 1), new Pair(-1, 0), new Pair(0, 0), new Pair(0, -1) } }, 
 						2, 4, Color.green), 
 	LineShape(
 			new Pair[][] {
 					{ new Pair(-1, 0), new Pair(0, 0), new Pair(1, 0), new Pair(2, 0) },
					{ new Pair(0, -1), new Pair(0, 0), new Pair(0, 1), new Pair(0, 2) } }, 
 						2, 4, Color.cyan), 
 	TShape(
 			new Pair[][] {
 					{ new Pair(-1, 0), new Pair(0, 0), new Pair(0, 1), new Pair(1, 0) },
 					{ new Pair(-1, 0), new Pair(0, -1), new Pair(0, 0), new Pair(0, 1) },
 					{ new Pair(-1, 0), new Pair(0, -1), new Pair(0, 0), new Pair(1, 0) },
 					{ new Pair(0, -1), new Pair(0, 0), new Pair(0, 1), new Pair(1, 0) } }, 
 						3, 4, Color.pink), 
 	SquareShape(
 			new Pair[][] { { new Pair(-1, 1), new Pair(-1, 0), new Pair(0, 0), new Pair(0, 1) } }, 
 				3, 5, Color.yellow), 
 	LShape(
 			new Pair[][] {
 					{ new Pair(-1, 0), new Pair(0, 0), new Pair(1, 0), new Pair(1, 1) },
 					{ new Pair(-1, 1), new Pair(0, 1), new Pair(0, 0), new Pair(0, -1) },
 					{ new Pair(-1, -1), new Pair(-1, 0), new Pair(0, 0), new Pair(1, 0) },
 					{ new Pair(0, 1), new Pair(0, 0), new Pair(0, -1), new Pair(1, -1) } }, 
 						3, 4, Color.orange), 
 	JShape(
 			new Pair[][] {
 					{ new Pair(-1, 1), new Pair(-1, 0), new Pair(0, 0), new Pair(1, 0) },
 					{ new Pair(-1, -1), new Pair(0, -1), new Pair(0, 0), new Pair(0, 1) },
 					{ new Pair(-1, 0), new Pair(0, 0), new Pair(1, 0), new Pair(1, -1) },
 					{ new Pair(0, -1), new Pair(0, 0), new Pair(0, 1), new Pair(1, 1) } }, 
 						3, 4, Color.blue);
 
 	private Pair[][] coords;
 	private int startRow;
 	private int startCol;
 	private Color color;
 
 	Tetrominos(Pair[][] coords, int starRow, int startCol, Color color) {
 		this.coords = coords;
 		this.startRow = starRow;
 		this.startCol = startCol;
 		this.color = color;
 	}
 
 	public Pair[][] getCoords() {
 		return coords;
 	}
 
 	public int getStartRow() {
 		return startRow;
 	}
 
 	public int getStartCol() {
 		return startCol;
 	}
 
 	public Color getColor() {
 		return color;
 	}
 }

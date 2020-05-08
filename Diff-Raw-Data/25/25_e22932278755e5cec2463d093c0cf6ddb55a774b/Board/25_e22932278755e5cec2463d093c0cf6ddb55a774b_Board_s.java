 package game;
 
 public class Board
 {
 	private final int rows;
 	private final int columns;
 	private Piece[][] board;
 
 	public Piece[][] getBoard()
 	{
 		return board;
 	}
 
 	public Board(int rows, int columns)
 	{
 		this.rows = rows;
 		this.columns = columns;
 		board = new Piece[rows][columns];
 	}
 
 	public void setPiece(int r, int c, Piece piece)
 	{
 		board[r][c] = piece;
 	}
 
 	public Piece getPiece(int r, int c)
 	{
 		return board[r][c];
 	}
 
<<<<<<< HEAD
	public int getRows()
	{
=======
 	public boolean isEmpty(int r, int c) {
 		return false;
 	}
 	
 	public int getRows() {
>>>>>>> 772644f487319760b28a67192cf32c1ef384123c
 		return rows;
 	}
 
 	public int getColumns()
 	{
 		return columns;
 	}
 }

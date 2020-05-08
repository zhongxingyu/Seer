 package edu.mharper.tp2;
 
 import java.util.ArrayList;
 
 public class GameBoard
 {
 	//Stores all pieces, in no particular order
 	//private ArrayList<GamePiece> pieces;
 	private GamePiece[][] board;
 	private int numRows;
 	private int numCols;
 	
 	//Default constructor: constructs a board based on option values
 	public GameBoard()
 	{
 		//pieces = new ArrayList<GamePieces>();
 		numRows = Main.verticalSpaces;
 		numCols = Main.horizontalSpaces;
 		board = new GamePiece[numRows][numCols];
 	}
 	
 	//Add pieces in standard starting positions
 	//Undefined when there are an even number of rows or cols
 	public void initPieces()
 	{
 		board = new GamePiece[numRows][numCols];
 		
 		int middleRow = numRows / 2;
 		int middleCol = numCols / 2;
 				
 		for(int r = 0; r < numRows; r++)
 		{
 			for(int c = 0; c < numCols; c++)
 			{
 				if(r < middleRow)
 					board[r][c] = new GamePiece(0, r, c);
 				else if(r > middleRow)
 					board[r][c] = new GamePiece(1, r, c);
 				else
 				{
 					if(c == middleCol)
 						continue;
					else if(c % 2 == 0)
						board[r][c] = new GamePiece(0, r, c);
					else
						board[r][c] = new GamePiece(1, r, c);
 				}
 			}
 		}
 	}
 	
 	//Attempts to move piece at startPoint to endPoint
 	//Returns false and does nothing if move is impossible
 	public boolean movePiece(Point startPoint, Point endPoint)
 	{
 		if(!testMovePiece(startPoint, endPoint))
 			return false;
 		
 		//if movement validity test passes, update board state
 		board[startPoint.getY()][startPoint.getX()].updatePosition(endPoint.getY(), endPoint.getX());
 		board[endPoint.getY()][endPoint.getX()] = board[startPoint.getY()][startPoint.getX()];
 		board[startPoint.getY()][startPoint.getX()] = null;
 		
 		return true;
 	}
 	
 	
 	//Attempts to remove piece from (pieceRow, pieceCol)
 	//Returns false if remove is invalid
 	public boolean removePiece(Point point)
 	{
 		if(board[point.getY()][point.getX()] == null)
 			return false;
 		
 		board[point.getY()][point.getX()] = null;
 		return true;
 	}
 
 	//Checks if represented move is a valid one
 	public boolean testMovePiece(Point startPoint, Point endPoint)
 	{
 		//Check if there is a piece to be moved
 		if(!hasPiece(startPoint))
 			return false;
 		
 		//Check if move is on the board
 		if(endPoint.getX() < 0 || endPoint.getY() < 0)
 			return false;
 		if(endPoint.getY() >= numRows || endPoint.getX() >= numCols)
 			return false;
 		
 		//Check if move is to an adjacent spot
 		int xDist = Math.abs(startPoint.getX() - endPoint.getX());
 		int yDist = Math.abs(startPoint.getY() - endPoint.getY());
 		boolean isAdjacentMove = (xDist <= 1) && (yDist <= 1);
 		
 		if(!isAdjacentMove)
 			return false;
 		
 		boolean isDiagonalMove = xDist == 1 && yDist == 1;
 				//(endPoint.getX() - startPoint.getX()) % 2 == 1 && 
 									//(endPoint.getY() - endPoint.getY()) % 2 == 1;
 		
 		//If move is diagonal, check if spot allows diagonal moves 
 		if(isDiagonalMove && !canMoveDiagonal(startPoint))
 			return false;
 		
 		//Check if spot being moved to is empty
 		if(hasPiece(endPoint))
 			return false;
 		
 		return true;
 	}
 	
 	public ArrayList<Point> getValidCapturingMoves(Point point)
 	{
 		ArrayList<Point> validMoves = getPossibleMoves(point);
 		return validMoves;		
 	}
 	
 	public ArrayList<Point> getValidChainMoves(Point point, ArrayList<Point> prevMoves)
 	{
 		//Get initially valid moves
 		ArrayList<Point> validMoves = getPossibleMoves(point);
 		
 		//Remove moves to previously visited points
 		for(int i = 0; i < validMoves.size(); i++)
 		{
 			for(Point prevMove: prevMoves)
 			{
 				if(validMoves.get(i) == prevMove)
 				{
 					validMoves.remove(i);
 					i--;
 					break;
 				}
 			}
 		}
 		
 		return validMoves;
 	}
 	
 	//Given a point with a piece, determines which moves this piece could go to
 	//Returns null if there is no piece
 	public ArrayList<Point> getPossibleMoves(Point piecePoint)
 	{
 		ArrayList<Point> validMoves = new ArrayList<Point>();
 		int x = piecePoint.getX();
 		int y = piecePoint.getY();
 		
 		for(int i = x-1; i <= x+1; i++)
 		{
 			for(int j = y-1; j <= y+1; j++)
 			{
 				if(i != x || j != y)
 				{
 					validMoves.add(new Point(i,j));
 				}
 			}
 		}
 		
 		for(int i = 0; i < validMoves.size(); i++)
 		{
 			if(!testMovePiece(piecePoint, validMoves.get(i)))
 			{
 				validMoves.remove(i);
 				i--;
 			}
 		}
 		
 		return validMoves;
 	}
 	
 	
 	//Returns true if point contains a piece, false otherwise
 	public boolean hasPiece(Point point)
 	{
 		return (board[point.getY()][point.getX()] != null);
 	}
 	
 	//Returns true if point is on the board
 	public boolean isValidPoint(Point point)
 	{
 		if(point.getX() < 0 || point.getX() >= numCols)
 			return false;
 		
 		if(point.getY() < 0 || point.getY() >= numRows)
 			return false;
 		
 		return true;
 	}
 	
 	public boolean isValidPoint(int x, int y)
 	{
 		return isValidPoint(new Point(x,y));
 	}
 	
 	public GamePiece getPiece(Point point)
 	{
 		return board[point.getY()][point.getX()];
 	}
 	
 	public int getNumRows()
 	{
 		return numRows;
 	}
 	
 	public int getNumCols()
 	{
 		return numCols;
 	}
 	
 	public GamePiece[][] getPieces() {
 		return board;
 	}
 	
 	public ArrayList<GamePiece> getPiecesList() {
 		ArrayList<GamePiece> pieces = new ArrayList<GamePiece>();
 		
 		for (int i = 0; i < Main.verticalSpaces; i++) {
 			for (int j = 0; j < Main.horizontalSpaces; j++) {
 				//if(board[i][j] != null)
 					pieces.add(board[i][j]);
 			}
 		}
 		
 		return pieces;
 	}
 	
 	//Clears board and places all pieces in [pieces] on the board
 	public void update(ArrayList<GamePiece> pieces) {
 		for (int i = 0; i < Main.verticalSpaces; i++) {
 			for (int j = 0; j < Main.horizontalSpaces; j++) {
 				board[i][j] = null;
 			}
 		}
 		for (GamePiece piece : pieces) {
 			if (piece != null)
 				board[piece.getRow()][piece.getColumn()] = piece;
 		}
 	}
 	
 	public ArrayList<Point> getPossibleMoves(GamePiece piece) {
 		return getPossibleMoves(piece.getPoint());
 	}
 		
 	private boolean canMoveDiagonal(Point point)
 	{
 		return (point.getX() + point.getY()) % 2 == 0;
 	}
 }

 package edu.mharper.tp2;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Scanner;
 
 //Responsible for computing and enforcing the game logic
 public class GameManager
 {
 	private GameBoard board;
 	private int turnsLeft = Main.maxTurns;
 	private GamePiece sacrificedPiece;
 	public int currentPlayer;
 	
 	public GameManager()
 	{
 		board = new GameBoard();
 		currentPlayer = GamePiece.FIRST_PLAYER;
 	}
 	
 	public void genBoard() {
 		board = new GameBoard();
 		//View.frame.pack();
 		sacrificedPiece = null;
 		turnsLeft = Main.maxTurns;
 		startGame();
 	}
 	public void startGame()
 	{
 		board.initPieces();
 	}
 	
 	public GameBoard getBoard()
 	{
 		return board;
 	}
 	
 	public int getTurnsLeft()
 	{
 		return turnsLeft;
 	}
 	
 	public GamePiece getSacrificedPiece()
 	{
 		return sacrificedPiece;
 	}
 	
 	public Color getCurrentPlayer()
 	{
 		return GamePiece.POSSIBLE_COLORS[currentPlayer];
 	}
 	
 	public int countWhite() {
 		int count = 0;
 		for (GamePiece piece : board.getPiecesList()) {
 			if (piece != null && piece.getColor().equals(Color.white)) {
 				count++;
 			}
 		}
 		return count;
 	}
 	
 	public int countBlack() {
 		int count = 0;
 		for (GamePiece piece : board.getPiecesList()) {
 			if (piece != null && piece.getColor().equals(Color.black)) {
 				count++;
 			}
 		}
 		return count;
 	}
 	
 	public void saveGame() {
 		// Output the current state of the board
 		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
 		Date date = new Date();
 		String filename = dateFormat.format(date) + ".sav";
 		System.out.println(filename);
 		
 		try {
 			FileOutputStream ofs = new FileOutputStream(filename);
 			String fileText = "";
 			for (GamePiece piece : board.getPiecesList()) {
 				if (piece != null) {
 					String colorText = "";
 					if (piece.getColor() == Color.black)
 						colorText = "black";						
					else if (piece.getColor() == Color.red)
						colorText = "red";
 					
 					fileText += colorText + "\t" + piece.getRow() + "\t" + piece.getColumn() + "\n";
 				}
 				else {
 					fileText += "null\n";
 				}
 			}
 			ofs.write(fileText.getBytes());
 			ofs.close();
 		} catch (FileNotFoundException e) {
 			File file = new File(filename);
 			try {
 				file.createNewFile();
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			saveGame();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 		
 	public void loadGame(String fileName) {
 		ArrayList<GamePiece> pieces = new ArrayList<GamePiece>();
 		File file = new File(fileName);
 		try {
 			Scanner scan = new Scanner(file);
 			while (scan.hasNext()) {
 				pieces.add(stringToPiece(scan.nextLine()));
 			}
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private GamePiece stringToPiece(String string) {
 		if (string.equals("null"))
 			return null;
 		String color = string.substring(0, string.indexOf('\t'));
 		string = string.substring(string.indexOf('\t') + 1);
 		int row = Integer.parseInt(string.substring(0, string.indexOf('\t')));
 		string = string.substring(string.indexOf('\t') + 1);
 		int col = Integer.parseInt(string);
 		
 		int colorInt = 0;
		if (color.equals("red"))
 			colorInt = 1;
 		
 		return new GamePiece(colorInt, row, col);
 	}
 	
 	public ArrayList<GamePiece> getPieces()
 	{
 		ArrayList<GamePiece> pieces = new ArrayList<GamePiece>();
 			
 		for (int i = 0; i < Main.verticalSpaces; i++) {
 			for (int j = 0; j < Main.horizontalSpaces; j++) {
 				//System.out.println("Updating with " + gamePieces[i][j]);
 				GamePiece piece = board.getPiece(new Point(j,i));
 				
 				if (piece != null) {
 					//System.out.println("Updating " + piece.getColor() + " piece");
 					pieces.add(piece);
 				}
 			}
 		}
 		return pieces;
 	}
 	
 	//Returns all valid moves by a piece at Point point
 	//Will enforce capturing/paika move preference
 	public ArrayList<Point> getValidMoves(GamePiece movePiece)
 	{
 		ArrayList<Point> validMoves = board.getPossibleMoves(movePiece.getPoint());
 		
 		//Check if any other piece of same color can capture
 		//If so, remove all paika moves
 		boolean capturePossible = false;
 
 		ArrayList<GamePiece> checkPieces = board.getPiecesList();
 		for(GamePiece checkPiece : checkPieces)
 		{
 			if(checkPiece == null)
 				continue;
 			if(checkPiece.getColor() != movePiece.getColor())
 				continue;
 			
 			ArrayList<Point> checkMoves = board.getPossibleMoves(checkPiece);
 			for(Point move : checkMoves)
 			{
 				if(isAnyCaptureMove(checkPiece, move))
 				{
 					capturePossible = true;
 					break;
 				}
 			}
 				
 			if(capturePossible)
 				break;
 		}
 		
 		if(capturePossible)
 		{
 			for(int i = 0; i < validMoves.size(); i++)
 			{
 				if(isPaikaMove(movePiece, validMoves.get(i)))
 				{
 					validMoves.remove(i);
 					i--;
 				}
 			}
 		}
 		
 		return validMoves;		
 	}	
 	
 	//Gets valid moves, assuming piece is performing a chain capture
 	public ArrayList<Point> getValidChainMoves(GamePiece movePiece, ArrayList<Point> prevMoves)
 	{
 		if(prevMoves.isEmpty())
 			return getValidMoves(movePiece);
 		
 		//Assumed that last move added in prevMoves is latest move
 		Point movePoint = movePiece.getPoint();
 		int deltaX = movePoint.getX() - prevMoves.get(prevMoves.size() - 1).getX();
 		int deltaY = movePoint.getY() - prevMoves.get(prevMoves.size() - 1).getY();
 		Point sameDirectionPoint = new Point(movePoint.getX() + deltaX, movePoint.getY() + deltaY);
 					
 		ArrayList<Point> validMoves = getValidMoves(movePiece);
 		for(int i = 0; i < validMoves.size(); i++)
 		{
 			//Eliminate move if it is a paika move
 			if(isPaikaMove(movePiece, validMoves.get(i)))
 			{
 				validMoves.remove(i);
 				i--;
 				continue;
 			}
 			
 			//Eliminate move if it is in the same direction as last move
 			if(board.isValidPoint(sameDirectionPoint) && validMoves.get(i).equals(sameDirectionPoint))
 			{
 				validMoves.remove(i);
 				i--;
 				continue;
 			}
 			
 			boolean moveRemoved = false;
 			
 			//Eliminate point if it has already been visited in chain
 			for(Point prevMove : prevMoves)
 			{
 				if(validMoves.get(i).equals(prevMove))
 				{
 					validMoves.remove(i);
 					i--;
 					moveRemoved = true;
 					break;
 				}
 			}
 			if(moveRemoved)
 				continue;
 			
 		}
 		
 		return validMoves;
 	}
 	
 	public boolean isValidMove(GamePiece piece, Point movePoint)
 	{
 		if(piece == null || movePoint == null)
 			return false;
 		
 		ArrayList<Point> validMoves = getValidMoves(piece);
 		for(Point move : validMoves)
 		{
 			if(move.equals(movePoint))
 				return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean isValidChainMove(GamePiece piece, Point movePoint, ArrayList<Point> prevVisited)
 	{
 		if(piece == null || movePoint == null || prevVisited == null)
 			return false;
 		
 		if(prevVisited.isEmpty())
 			return isValidMove(piece, movePoint);
 		
 		ArrayList<Point> validMoves = getValidChainMoves(piece, prevVisited);
 		for(Point move : validMoves)
 		{
 			if(move.equals(movePoint))
 				return true;
 		}
 		
 		return false;
 	}
 	
 	//Moves game piece (assumed move is valid)
 	//Returns false if move is unsuccessful for any reason
 	public boolean movePiece(GamePiece piece, Point movePoint)
 	{
 		if(piece == null || movePoint == null)
 			return false;
 		
 		return board.movePiece(piece.getPoint(), movePoint);
 	}
 	//Performs a sacrifice move on a piece
 	public void sacrificePiece(GamePiece piece)
 	{
 		if(sacrificedPiece != null)
 			board.removePiece(sacrificedPiece.getPoint());
 		
 		sacrificedPiece = piece;
 	}
 	
 	public boolean isAdvanceCaptureMove(GamePiece piece, Point movePoint)
 	{
 		//Assumed that movePoint is adjacent to piece's current position
 		int x = piece.getColumn();
 		int y = piece.getRow();
 		
 		int deltaX = movePoint.getX() - x;
 		int deltaY = movePoint.getY() - y;
 		
 		Point advancePoint = new Point(movePoint.getX() + deltaX, movePoint.getY() + deltaY);
 		
 		//Check for a possible advancing capture
 		if(board.isValidPoint(advancePoint) && board.hasPiece(advancePoint) && 
 				board.getPiece(advancePoint).getColor() != piece.getColor() &&
 				!board.getPiece(advancePoint).equals(sacrificedPiece))
 			return true;
 	
 		return false;		
 	}
 	
 	public boolean isWithdrawCaptureMove(GamePiece piece, Point movePoint)
 	{
 		//Assumed that movePoint is adjacent to piece's current position
 		int x = piece.getColumn();
 		int y = piece.getRow();
 		
 		int deltaX = movePoint.getX() - x;
 		int deltaY = movePoint.getY() - y;
 		
 		Point withdrawPoint = new Point(x - deltaX, y - deltaY);
 		
 		//Check for a possible withdrawal capture
 		if(board.isValidPoint(withdrawPoint) && board.hasPiece(withdrawPoint) && 
 				board.getPiece(withdrawPoint).getColor() != piece.getColor() &&
 				!board.getPiece(withdrawPoint).equals(sacrificedPiece))
 			return true;
 		
 		return false;		
 		
 	}
 	
 	public boolean isAnyCaptureMove(GamePiece piece, Point movePoint)
 	{
 		return(isAdvanceCaptureMove(piece, movePoint) || isWithdrawCaptureMove(piece, movePoint));
 	}
 	
 	//Returns if move could be either an advance capture or a withdraw capture
 	public boolean isAdvanceAndWithdrawCaptureMove(GamePiece piece, Point movePoint)
 	{
 		return(isAdvanceCaptureMove(piece, movePoint) && isWithdrawCaptureMove(piece, movePoint));
 	}
 	
 	public boolean isPaikaMove(GamePiece piece, Point movePoint)
 	{
 		//Assumed that movePoint is adjacent to piece's current position
 		return(!isAnyCaptureMove(piece, movePoint));
 	}
 
 	//Removes pieces taken by a valid advancing capture from startPoint to endPoint
 	public void advanceCapturePieces(GamePiece piece, Point movePoint)
 	{
 		Point piecePoint = piece.getPoint();
 		
 		int deltaX = movePoint.getX() - piecePoint.getX();
 		int deltaY = movePoint.getY() - piecePoint.getY();
 		
 		int takeX = movePoint.getX() + deltaX;
 		int takeY = movePoint.getY() + deltaY;
 		
 		Point takePoint = new Point(takeX, takeY);
 		
 		while(board.isValidPoint(takePoint) && board.hasPiece(takePoint)
 				&& board.getPiece(takePoint).getColor() != piece.getColor()
 				&& !board.getPiece(takePoint).equals(sacrificedPiece))
 		{
 			board.removePiece(takePoint);
 			takeX += deltaX;
 			takeY += deltaY;
 			takePoint = new Point(takeX, takeY);
 		}
 	}
 	
 	//Removes pieces taken by a valid withdrawing capture from startPoint to endPoint
 	public void withdrawCapturePieces(GamePiece piece, Point movePoint)
 	{
 		Point piecePoint = piece.getPoint();
 		
 		int deltaX = movePoint.getX() - piecePoint.getX();
 		int deltaY = movePoint.getY() - piecePoint.getY();
 		
 		int takeX = piece.getPoint().getX() - deltaX;
 		int takeY = piece.getPoint().getY() - deltaY;
 		
 		Point takePoint = new Point(takeX, takeY);
 		
 		while(board.isValidPoint(takePoint) && board.hasPiece(takePoint)
 				&& board.getPiece(takePoint).getColor() != piece.getColor()
 				&& !board.getPiece(takePoint).equals(sacrificedPiece))
 		{
 			board.removePiece(takePoint);
 			takeX -= deltaX;
 			takeY -= deltaY;
 			takePoint = new Point(takeX, takeY);
 		}
 	}
 	
 	public void endTurn()
 	{
 		turnsLeft--;
 		if(currentPlayer == GamePiece.FIRST_PLAYER)
 			currentPlayer = GamePiece.SECOND_PLAYER;
 		else
 			currentPlayer = GamePiece.FIRST_PLAYER;
 		
 		if(sacrificedPiece != null && 
 				GamePiece.POSSIBLE_COLORS[currentPlayer].equals(sacrificedPiece.getColor()))
 		{
 			board.removePiece(sacrificedPiece.getPoint());
 			sacrificedPiece = null;
 		}
 	}
 }

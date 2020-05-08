 import java.awt.image.BufferedImage;
 
 
 public class Piece 
 {
 	BufferedImage pieceImage;
 	Board pBoard;
 	Piece[][] pieceBoard;
 	boolean[][] whiteMoves;
 	boolean[][] blackMoves;
 	boolean[][] savedWhiteMoves;
 	boolean[][] savedBlackMoves;
 	boolean white;
 	String pieceType;
 	int xCord;
 	int yCord;
 	int xSpacing = 56;
 	int ySpacing = 0;
 	boolean[][] canMove = new boolean[8][8];
 	boolean isKing = false;
 	boolean isPawn = false;
 	boolean justMovedPawn = false;
 	boolean moved = false;
 
 	public Piece(Board board, boolean white, int xCord, int yCord, Piece[][] pieceBoard, boolean[][] whiteMoves, boolean[][] blackMoves)
 	{
 		pBoard = board;
 		this.white = white;
 		this.xCord = xCord;
 		this.yCord = yCord;
 		this.pieceBoard = pieceBoard;
 		this.whiteMoves = whiteMoves;
 		this.blackMoves = blackMoves;
 	}
 
 	public boolean isWhite()
 	{
 
 		return white;
 	}
 
 	public void setWhite(boolean a)
 	{
 
 		white = a;
 	}
 
 	public void setXandYCord(int x, int y){
 
 		xCord = x;
 		yCord = y;
 	}
 
 	public String getPieceType()
 	{
 
 		return pieceType;
 	}
 
 	public void setMoves()
 	{
 		//do not remove
 	}
 	
 	public void setImage()
 	{
 		pieceImage = Board.boardImage;
 		
 		if(white)
 			ySpacing = 92;
 	}
 
 	public BufferedImage getImage()
 	{
 		return pieceImage;
 	}
 	
 	public void setMoved(boolean a)
 	{
 		moved = a;
 	}
 
 	public void setJustMovedPawn(boolean a)
 	{
 		justMovedPawn = a;
 	}
 	
 	public boolean justMovedPawn()
 	{	
 		return justMovedPawn;
 	}
 	
 	public void clearMoveSet()
 	{
 
 		for(int y=0; y<8; y++)
 		{
 			for(int x=0; x<8; x++)
 			{
 				canMove[x][y] = false;
 			}
 		}
 
 	}
 
 	public boolean isKing(){
 
 		return isKing;
 	}
 
 	public boolean isPawn(){
 
 		return isPawn;
 	}
 
 	public int getColorValuePawn()
 	{
 		int cValue;
 
 		if(white)
 			cValue = -1;
 		else
 			cValue = 1;
 
 		return cValue;
 	}
 
 	public int getColorValueKing()
 	{
 		int cValue;
 
 		if(white)
 			cValue = 7;
 		else
 			cValue = 0;
 
 		return cValue;
 	}
 
 	public boolean isEmpty(int x, int y)
 	{
 		boolean empty = false;
 
 		try
 		{
 			if(pieceBoard[x][y] == null)
 				empty = true;
 		}
 		catch(Exception e)
 		{
 
 		}
 		return empty;
 	}
 
 	public boolean isValid(int x, int y)
 	{
 		boolean valid = false;
 
 		if((x>=0 && x<8) && (y>=0 && y<8))
 			valid = true;
 
 		return valid;
 	}
 
 	public boolean sameColor(int x2, int y2)
 	{
 
 		boolean same = false;
 
 		if(isEmpty(x2,y2) || isEmpty(xCord, yCord))
 			same =true;
 
 		else{
 			if(pieceBoard[xCord][yCord].isWhite()&&pieceBoard[x2][y2].isWhite()
 					||(!pieceBoard[xCord][yCord].isWhite()&&!pieceBoard[x2][y2].isWhite()))
 				same =true;
 
 		}
 		return same;
 	}
 
 	public void addBlackAndWhiteMoves()
 	{
 
 		for(int y=0; y<8; y++){
 			for(int x=0; x<8; x++){
 
 				if(white && canMove(x,y) && !whiteMoves[x][y])
 					whiteMoves[x][y] = true;
 
 				else if(!white && canMove(x,y) && !blackMoves[x][y])
 					blackMoves[x][y] = true;
 
 			}
 		}
 
 	}
 
 	public void addMove(int x, int y)
 	{
 		if(white)
 			whiteMoves[x][y] = true;
 		else
 			blackMoves[x][y] = true;
 	}
 
 	public boolean canMove(int x, int y){
 
 		boolean can = false;
 		try{
 			if(canMove[x][y])
 				can = true;
 		}
 		catch(Exception e){
 
 		}
 		return can;
 	}
 
 	public void setHorizontalAndVertical()
 	{
 		for(int x=(xCord-1); x>=0; x--) //left
 		{
 			if(isValid(x,yCord))
 			{
 				if(isEmpty(x,yCord) || !sameColor(x, yCord))
 					canMove[x][yCord] = true;
 				if(!isEmpty(x,yCord)&&(!sameColor(x, yCord)||sameColor(x,yCord)))
 					break;
 
 			}
 		}
 
 		for(int x=(xCord+1); x<8; x++) //right
 		{
 			if(isValid(x,yCord))
 			{
 				if(isEmpty(x,yCord) || !sameColor(x, yCord))
 					canMove[x][yCord] = true;
 				if(!isEmpty(x,yCord)&&(!sameColor(x, yCord)|| sameColor(x, yCord)))
 					break;
 
 			}
 		}
 
 		for(int y=(yCord-1); y>=0; y--) //up
 		{
 			if(isValid(xCord,y))
 			{
 				if(isEmpty(xCord,y) || !sameColor(xCord, y))
 					canMove[xCord][y] = true;
 				if(!isEmpty(xCord,y)&&(!sameColor(xCord, y)||sameColor(xCord, y)))
 					break;
 
 			}
 		}
 
 		for(int y=(yCord+1); y<8; y++) //down
 		{
 			if(isValid(xCord,y))
 			{
 				if(isEmpty(xCord,y) || !sameColor(xCord, y))
 					canMove[xCord][y] = true;
 				if(!isEmpty(xCord,y)&&(!sameColor(xCord, y)||sameColor(xCord, y)))
 					break;
 
 			}
 		}
 		addBlackAndWhiteMoves();
 	}
 
 	public void setDiagonal(){
 
 		int xL = (xCord-1); //left
 		int yD = (yCord-1); //up
 		while(xL >=0 && yD >=0)
 		{
 			if(isValid(xL,yD))
 			{
 				if(isEmpty(xL,yD) || !sameColor( xL, yD))
 					canMove[xL][yD] = true;
 				if(!isEmpty(xL,yD)&&(!sameColor( xL, yD)||sameColor( xL, yD)))
 					break;
 			}
 			xL--;
 			yD--;
 		}
 		xL = (xCord-1); //left
 		int yU = (yCord+1); //down
 		while(xL >=0 && yU<8)
 		{
 			if(isValid(xL,yU))
 			{
 				if(isEmpty(xL,yU) || !sameColor(xL, yU))
 					canMove[xL][yU] = true;
 				if(!isEmpty(xL,yU)&&(!sameColor(xL, yU)||sameColor(xL, yU)))
 					break;
 
 			}
 			xL--;
 			yU++;
 		}
 
 		int xR = (xCord+1); //right
 		yD = (yCord-1); //down
 		while(xR<8 && yD >=0)
 		{
 			if(isValid(xR,yD))
 			{
 				if(isEmpty(xR,yD) || !sameColor(xR, yD))
 					canMove[xR][yD] = true;
 				if(!isEmpty(xR,yD)&&(!sameColor(xR, yD)||sameColor(xR, yD)))
 					break;
 			}
 			xR++;
 			yD--;
 		}
 
 		xR = (xCord+1); //right
 		yU = (yCord+1); //up
 		while(xR<8 && yD<8)
 		{
 			if(isValid(xR,yU))
 			{
 				if(isEmpty(xR,yU) || !sameColor(xR, yU))
 					canMove[xR][yU] = true;
 				if(!isEmpty(xR,yU)&&(!sameColor(xR, yU)||sameColor(xR, yU)))
 					break;
 			}
 			xR++;
 			yU++;
 		}
 		addBlackAndWhiteMoves();
 	}
 
 	public boolean isAble(int x, int y)
 	{
 
 		boolean able = false;
 
 		if(canMove[x][y]==true)
 			able = true;
 
 		return able;
 	}
 
 	public void printMoves(){
 
 		for(int y=0; y<8; y++){
 			for(int x=0; x<8; x++){
 
 				System.out.print(canMove[x][y]+" ");
 
 			}
 			System.out.println();
 		}
 
 	}
 
 	public boolean validMove(int x2, int y2)
 	{	
 		boolean whiteTurn = (pBoard.getTurnCount()%2==0);
 
 		boolean valid = canMove[x2][y2]; 
 
 		Piece tempPiece1 = pieceBoard[xCord][yCord]; //testing
 		Piece tempPiece2 = pieceBoard[x2][y2]; // move the piece
 
 		pieceBoard[x2][y2] = pieceBoard[xCord][yCord]; //moves black
 		pieceBoard[xCord][yCord] = null;
 
 		resetMoves(whiteTurn);
 		resetMoves(!whiteTurn); //reset white with no restrictions
 
		if(inCheck(white)) //if black moves in check or doesn't get out of check
 			valid = false;
 
 		pieceBoard[xCord][yCord] = tempPiece1; 
 		pieceBoard[x2][y2] = tempPiece2;
 		resetMoves(!whiteTurn); //reset the black moves
 		resetMoves(whiteTurn);
 
 		return valid;
 	}
 
 	public void clearMoveSets(boolean colorWhite)
 	{
 		for(int y=0; y<8; y++)
 		{
 			for(int x=0; x<8; x++)
 			{
 				if(colorWhite){
 					if(!isEmpty(x,y) && pieceBoard[x][y].isWhite())
 						pieceBoard[x][y].clearMoveSet();
 				}
 				else
 					if(!isEmpty(x,y) && !pieceBoard[x][y].isWhite())
 						pieceBoard[x][y].clearMoveSet();
 
 
 			}
 		}
 
 	}
 
 	public void clearMoves(boolean colorWhite)
 	{
 		for(int y=0; y<8; y++)
 		{
 			for(int x=0; x<8; x++)
 			{
 				if(colorWhite)
 					whiteMoves[x][y] = false;
 				else
 					blackMoves[x][y] = false;
 
 			}
 		}
 
 	}
 
 	public boolean inCheck(boolean colorWhite)
 	{		
 		boolean check = false;
 		for(int y=0; y<8; y++)
 		{
 			for(int x=0; x<8; x++)
 			{
 
 				if(colorWhite&&!isEmpty(x,y)&&pieceBoard[x][y].isKing())
 				{
 					if(pieceBoard[x][y].isWhite() && blackMoves[x][y])
 						check = true;
 				}
 				else if(!colorWhite&&!isEmpty(x,y)&&pieceBoard[x][y].isKing())
 				{
 					if(!pieceBoard[x][y].isWhite() && whiteMoves[x][y])
 						check = true;
 				}
 			}
 		}
 		return check;
 	}
 
 	public void initializeMoves(boolean colorWhite)
 	{
 
 		for(int y=0; y<8; y++)
 		{
 			for(int x=0; x<8; x++)
 			{
 				if(colorWhite){
 					if(!isEmpty(x,y)&&pieceBoard[x][y].isWhite())
 						pieceBoard[x][y].setMoves();
 				}
 				else
 					if(!isEmpty(x,y)&&!pieceBoard[x][y].isWhite())
 						pieceBoard[x][y].setMoves();
 
 			}
 		}
 
 	}
 
 	public void resetMoves(boolean color)
 	{
 		clearMoves(color);
 		clearMoveSets(color);
 		initializeMoves(color);
 	}
 }

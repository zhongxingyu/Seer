 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JOptionPane;
 
 
 public class Board 
 {	
 	static BufferedImage boardImage;
 	Piece[][] board;
 	Display boardPlay;
 	int[] reflectNumbers = {0,1,2,3,4,5,6,7};
 	String borderLetters = "ABCDEFGH"; 
 	String borderNumbers = "87654321"; 
 	Font borderFont = new Font("Helvitica", Font.BOLD, 20);; 
 	int tileSize =70;
 	int borderSize = 40;
 	Piece boardPiece;
 	int turnCount = 0;
 	int x1=-1;
 	int y1=-1;
 	int x2;
 	int y2;
 
 	int clickX;
 	int clickY;
 	int dragCordX;
 	int dragCordY;
 
 	boolean checkmate = false;
 	boolean stalemate = false;
 	boolean justMoved = false;
 	boolean gameOver = false;
 
 	boolean[][] whiteMoves;
 	boolean[][] blackMoves;
 	boolean click = false;
 	boolean dragging = false;
 	boolean stopPainting = false;
 
 	String winner = "";
 
 	public Board(Display display, BufferedImage img)
 	{
 		boardPlay = display;
 
 		boardImage = img;
 		whiteMoves = new boolean[8][8];
 		blackMoves = new boolean[8][8];
 		initializeBlackAndWhiteMoves();
 
 		board = new Piece[8][8];
 		initializeBoard();
 	}
 
 	public void movePiece()
 	{
 		if(board[x1][y1].isAble(x2,y2)&& !sameSpot())
 		{
 			if(board[x1][y1].validMove(x2, y2))
 			{
 				boolean white = turnCount%2==0;
 
 				clearPassant(white); 
 				
 				passantCapture(x1,y1,x2,y2);
 				
 				if(castle(x1,y1,x2,y2)!=0)
 				{
 					board[castle(x1,y1,x2,y2)][getColorValueKing(white)] = 
 							new RookPiece(this,white,castle(x1,y1,x2,y2),getColorValueKing(white),board,whiteMoves,blackMoves);
 					board[castle(x1,y1,x2,y2)][getColorValueKing(white)].setImage();
 
 					if(x2<5)
 						board[0][getColorValueKing(white)] = null;
 					else
 						board[7][getColorValueKing(white)] = null;
 				}
 
 				
 				board[x2][y2] = board[x1][y1];
 				addPassant(x1,y1,x2,y2);
 				board[x1][y1] = null;
 				board[x2][y2].setXandYCord(x2, y2);
 
 				if(pawnAtEnd()){
 
 					Object[] options = {"Knight","Bishop","Rook","Queen"};
 
 					int n = JOptionPane.showOptionDialog(boardPlay,"Select a piece","Chess",JOptionPane.YES_NO_CANCEL_OPTION,
 							JOptionPane.QUESTION_MESSAGE,null,options,options[3]);
 					makePiece(x2,y2,n,white);
 				}
 
 				board[x2][y2].setMoved(true);
 
 				resetMoves(white); //no restrictions
 				resetMoves(!white); //restrictions
 				inCheck(!white);
				click = false;
 
 				justMoved = true;
 				if(!checkmate && !stalemate)
 					turnCount++;
 			}
 		}
		
			click = false;
 	}
 
 	public boolean sameSpot()
 	{
 		boolean same = false;
 
 		if(x1 == x2 && y1 == y2)
 			same = true;
 
 		return same;
 	}
 
 	public Piece[][] getBoard()
 	{ 
 
 		return board;
 
 	}
 
 	public int getBorder()
 	{
 		return borderSize;
 	}
 
 	public int getTileSize()
 	{
 
 		return tileSize;
 	}
 
 	public int getTurnCount(){
 
 		return turnCount;
 	}
 
 	public void setX1(int a){
 
 		x1 = a;
 	}
 
 	public int getX1(){
 
 		return x1;
 	}
 
 	public void setX2(int a){
 
 		x2 = a;
 	}
 
 	public void setY1(int a){
 
 		y1 = a;
 	}
 
 	public int getY1(){
 
 		return y1;
 	}
 
 	public String getWinner()
 	{
 		return winner;
 	}
 
 	public boolean gameIsOver()
 	{
 		return gameOver;
 	}
 
 	public void setY2(int a){
 
 		y2 = a;
 	}
 
 	public void setDragging(boolean a){
 
 		dragging = a;
 	}
 
 	public void setDraggingCoordinates(int x, int y)
 	{
 		dragCordX = x;
 		dragCordY = y;
 	}
 
 	public int getClickX(){
 
 		return clickX;
 	}
 
 	public int getClickY(){
 
 		return clickY;
 	}
 
 	public void setClick(boolean a){
 
 		click = a;
 	}
 
 	public void setJustMoved(boolean a)
 	{
 		justMoved=a;
 	}
 
 	public void setClickCoordinates(int x, int y)
 	{
 		clickX = x;
 		clickY = y;
 	}
 
 	public boolean isWhite(int x, int y){
 		boolean white = false;
 
 		if(!isEmpty(x,y)&&board[x][y].isWhite())
 			white = true;
 
 		return white;
 	}
 
 	private void makePiece(int x, int y, int num, boolean white)
 	{
 		Piece[] pieces = new Piece[4];
 		pieces[0] = new KnightPiece(this,white,x,y,board,whiteMoves,blackMoves);
 		pieces[1] = new BishopPiece(this,white,x,y,board,whiteMoves,blackMoves);
 		pieces[2] = new RookPiece(this,white,x,y,board,whiteMoves,blackMoves);
 		pieces[3] = new QueenPiece(this,white,x,y,board,whiteMoves,blackMoves);
 
 		board[x][y] = pieces[num];
 		board[x][y].setImage();
 
 	}
 
 	public void showMoves(int x, int y){
 
 		board[x][y].printMoves();
 	}
 
 	public boolean moveSuccessful(){
 
 		boolean white = (turnCount%2==0);
 
 		boolean successful = true;
 		Piece tempPiece1 = board[x1][y1];
 		Piece tempPiece2 = board[x2][y2];
 
 		board[x2][y2] = board[x1][y1];
 		board[x1][y1] = null;
 
 		if(white)	//white move
 		{ 
 			resetMoves(!white);
 
 			if(inCheck(white)) //still in check
 			{
 				board[x1][y1] = tempPiece1; //undo move
 				board[x2][y2] = tempPiece2;
 				resetMoves(!white);
 				successful = false;
 			}
 			else //still have to undo to the last black move initialization 
 			{
 				board[x1][y1] = tempPiece1; 
 				board[x2][y2] = tempPiece2;
 				resetMoves(!white);
 
 				board[x2][y2] = board[x1][y1];
 				board[x1][y1] = null;
 				board[x2][y2].setMoved(true);
 			}
 
 		}
 		if(successful)
 			board[x2][y2].setXandYCord(x2, y2);
 		return successful;
 	}
 
 	public void setStopPainting(boolean a)
 	{
 		stopPainting = a;
 	}
 
 	public void resetMoves(boolean color)
 	{
 		clearMoves(color);
 		clearMoveSets(color);
 		initializeMoves(color);
 	}
 
 	public void initializeBoard()
 	{
 		for(int y=0; y<9; y+=7)
 		{ //rook
 			for(int x=0; x<9; x+=7)
 			{
 
 				board[x][y] = new RookPiece(this,false,x,y,board,whiteMoves,blackMoves);
 
 
 			}
 		}
 
 		for(int y=0; y<9; y+=7)
 		{ //knight
 			for(int x=1; x<8; x+=5)
 			{
 
 				board[x][y] = new KnightPiece(this,false,x,y,board,whiteMoves,blackMoves);
 			}
 		}
 
 		for(int y=0; y<9; y+=7)
 		{ //bishop
 			for(int x=2; x<8; x+=3)
 			{
 
 				board[x][y] = new BishopPiece(this,false,x,y,board,whiteMoves,blackMoves);
 			}
 		}
 
 		for(int y=0; y<9; y+=7)
 		{ //queen
 			int x = 3;
 
 			board[x][y] = new QueenPiece(this,false,x,y,board,whiteMoves,blackMoves);
 
 		}
 
 		for(int y=0; y<9; y+=7)
 		{ //king
 			int x = 4;
 
 			board[x][y] = new KingPiece(this,false,x,y,board,whiteMoves,blackMoves);
 		}
 
 		for(int y=1; y<7; y+=5)
 		{ //pawn
 			for(int x=0; x<8; x++)
 			{
 
 				board[x][y] = new PawnPiece(this,false,x,y,board,whiteMoves,blackMoves);
 			}
 		}
 
 		for(int y=6; y<8; y++){
 			for(int x=0; x<8; x++){
 
 				if(!isEmpty(x,y))
 					board[x][y].setWhite(true);
 
 			}
 		}		
 		initializeMoves(true);
 		initializeMoves(false);
 		initializeImages();
 	}
 
 	public void initializeImages()
 	{
 		for(int y=0; y<8; y++)
 		{
 			for(int x=0; x<8; x++)
 			{
 				if(!isEmpty(x,y))
 					board[x][y].setImage();
 			}
 		}
 	}
 
 	public void initializeMoves(boolean white)
 	{
 
 		for(int y=0; y<8; y++)
 		{
 			for(int x=0; x<8; x++)
 			{
 				if(white){
 					if(!isEmpty(x,y) && board[x][y].isWhite())
 						board[x][y].setMoves();
 				}
 				else
 					if(!isEmpty(x,y)&&!board[x][y].isWhite())
 						board[x][y].setMoves();
 
 			}
 		}
 
 	}
 
 	public void initializeBlackAndWhiteMoves()
 	{	
 		for(int y=0; y<8; y++)
 		{
 			for(int x=0; x<8; x++)
 			{
 
 				whiteMoves[x][y] = false;
 				blackMoves[x][y] = false;
 
 			}
 		}
 
 	}
 
 	public int getColorValueKing(boolean white)
 	{
 		int cValue;
 
 		if(white)
 			cValue = 7;
 		else
 			cValue = 0;
 
 		return cValue;
 	}
 
 	public int castle(int x1, int y1, int x2, int y2){
 
 		int castle = 0;
 
 		if(board[x1][y1].isKing && !board[x1][y1].moved)
 		{
 			if(x2 == 2)
 				castle = 3;
 			else if(x2 == 6)
 				castle = 5;
 		}
 
 		return castle;
 	}
 	
 	public void clearPassant(boolean white)
 	{
 		int y;
 		if(white)
 			y = 4;
 		else
 			y = 3;
 		for(int x=0; x<8; x++)
 		{
 			if(!isEmpty(x,y)&&board[x][y].justMovedPawn())
 			{
 				board[x][y].setJustMovedPawn(false);
 				break;
 			}
 		}
 	}
 	
 	public void passantCapture(int x1,int y1,int x2,int y2)
 	{
 		int cVal = board[x1][y1].getColorValuePawn()*-1;
 		if(isEmpty(x2,y2) && board[x1][y1].isPawn && (x1-1==x2 || x1+1 == x2))
 			board[x2][y2+cVal] = null;
 	}
 	
 	public void addPassant(int x1, int y1, int x2, int y2)
 	{
 		if(board[x1][y1].isPawn && (y1+2==y2 || y1-2==y2))
 			board[x2][y2].setJustMovedPawn(true);
 	}
 
 	public void clearMoveSets(boolean colorWhite)
 	{
 		for(int y=0; y<8; y++)
 		{
 			for(int x=0; x<8; x++)
 			{
 				if(colorWhite){
 					if(!isEmpty(x,y)&&board[x][y].isWhite())
 						board[x][y].clearMoveSet();
 				}
 				else
 					if(!isEmpty(x,y)&&!board[x][y].isWhite())
 						board[x][y].clearMoveSet();
 
 
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
 
 				if(colorWhite&&!isEmpty(x,y)&&board[x][y].isKing())
 				{
 					if(board[x][y].isWhite() && blackMoves[x][y])
 						check = true;
 				}
 				else if(!colorWhite&&!isEmpty(x,y)&&board[x][y].isKing())
 				{
 					if(!board[x][y].isWhite() && whiteMoves[x][y])
 						check = true;
 				}
 			}
 		}
 		return check;
 	}
 
 
 	public boolean isEmpty(int x, int y)
 	{
 		boolean empty = false;
 
 		try
 		{
 			if(board[x][y] == null)
 				empty = true;
 		}
 		catch(Exception e){
 
 		}
 		return empty;
 	}
 
 	public boolean isValid(int x, int y)
 	{
 
 		boolean valid = true;
 
 		if((x>=0 && x<8) && (y>=0 && y<8))
 			valid = false;
 
 		return valid;
 	}
 
 	public int reflectNumber(int num){
 
 		int searchCount = 0;
 		for(int i=0; i<reflectNumbers.length; i++){
 			if(reflectNumbers[i]==num)
 				break;
 			else
 				searchCount++;
 		}
 
 		int reflectedNum = reflectNumbers[7-searchCount];
 		return reflectedNum;
 	}
 
 	public boolean validMove(int x, int y)
 	{
 		boolean valid = board[x1][y1].validMove(x, y);
 
 		return valid;
 	}
 
 	public boolean pawnAtEnd()
 	{
 		boolean end = false;
 
 		for(int x=0; x<8; x++)
 		{
 			if((!isEmpty(x,0) && board[x][0].isPawn()) || (!isEmpty(x,7) && board[x][7].isPawn()))
 				end = true;
 		}
 
 		return end;
 	}
 	
 //	public int passantValue(boolean white)
 //	{
 //		
 //	}
 
 	public void paintBoard(Graphics g)
 	{
 		g.setColor(Color.BLACK);
 		g.fillRect(0, 0, (getTileSize()+1)*9 + getBorder()*2,(getTileSize()+1)*8+22+getBorder()*2);
 		g.setColor(Color.WHITE);
 		g.fillRect(2, 2, (getTileSize()+1)*8+22+getBorder()+14,(getTileSize()+1)*8+22+getBorder()+14);
 		g.setColor(Color.BLACK);
 		g.fillRect(borderSize-2, borderSize-2, (tileSize+1)*8+4,(tileSize+1)*8+4);
 
 		boolean white = turnCount%2==0;
 
 		int start = 0;
 		int increment = 1;
 		
 		if(!white)
 		{
 			start = 7;
 			increment = -1;
 		}
 		int count = 0;
 		int startingX = start;
 		while(count<8){
 			
 			g.setFont(borderFont);
 			g.setColor(Color.BLACK);
 			g.drawString(""+borderLetters.charAt(startingX), count*tileSize+tileSize, borderSize*2/3);
 			g.drawString(""+borderLetters.charAt(startingX), count*tileSize+tileSize, (tileSize+1)*8+borderSize*7/4);
 			
 			g.drawString(""+borderNumbers.charAt(startingX), borderSize*1/3, count*tileSize+tileSize+borderSize*2/5);
 			g.drawString(""+borderNumbers.charAt(startingX), (tileSize+1)*8+borderSize*5/4, count*tileSize+tileSize+borderSize*2/5);
 			
 			startingX+=increment;
 			count ++;
 		}
 		
 		int tileMarker=0;
 		int ySpacing =borderSize;
 		if(!white) //Black Move
 			ySpacing = (tileSize+1)*7+borderSize;
 		for(int y=0; y<8; y++)
 		{
 			int xSpacing =borderSize;
 			if(!white)
 				xSpacing = (tileSize+1)*7+borderSize;
 
 			for(int x=0; x<8; x++)
 			{
 
 				if(tileMarker%2==0)
 					g.setColor(Color.WHITE);
 				else
 					g.setColor(Color.BLACK);
 
 				g.fillRect(xSpacing, ySpacing, tileSize+1, tileSize+1);
 
 				if(!isEmpty(x,y))
 				{
 					if(!dragging || x!=x1 || y!=y1){
 
 						int tempXSpacing = xSpacing;
 						int tempYSpacing = ySpacing;
 
 						if(board[x][y].isKing)
 							tempXSpacing +=5;
 
 						((Graphics2D) g).drawImage(board[x][y].getImage(), tempXSpacing, ySpacing, boardPlay);
 					}
 					//					g.drawString(""+board[x][y].getPieceType().charAt(0), xSpacing+tileSize/2, ySpacing+tileSize/2);
 				}
 
 				//				else if(isEmpty(x,y))
 				//				{
 				//					g.setColor(Color.CYAN);
 				//					if(whiteMoves[x][y]&&turnCount%2==0)
 				//						g.fillOval(xSpacing+tileSize*1/4-(tileSize*1/8), ySpacing+tileSize*1/4-(tileSize*1/8), tileSize*3/4, tileSize*3/4);
 				//					else if(blackMoves[x][y]&&!white){
 				//						g.setColor(Color.RED);
 				//						g.fillOval(xSpacing+tileSize*1/4-(tileSize*1/8), ySpacing+tileSize*1/4-(tileSize*1/8), tileSize*3/4, tileSize*3/4);
 				//					}
 				//				}
 
 				if(click)
 				{
 					g.setColor(new Color(0,255,255, 80));
 
 					if(board[clickX][clickY].validMove(x, y))
 					{
 						if(isEmpty(x,y))
 							g.fillOval(xSpacing+tileSize*1/4-(tileSize*1/8), ySpacing+tileSize*1/4-(tileSize*1/8), tileSize*3/4, tileSize*3/4);
 						else if(!isEmpty(x,y))
 						{
 							g.setColor(new Color(255,0,0, 80));
 							g.fillRect(xSpacing, ySpacing, tileSize+1, tileSize+1);
 
 						}
 
 					}
 
 				}
 
 
 
 				if(!white)
 					xSpacing-=(tileSize+1);
 				else
 					xSpacing+=(tileSize+1);	
 				tileMarker++;	
 
 			}
 			if(!white)
 				ySpacing-=(tileSize+1);
 			else
 				ySpacing+=(tileSize+1);
 			tileMarker++;
 		}
 
 		if(dragging)
 		{
 			((Graphics2D) g).drawImage(board[x1][y1].getImage(), dragCordX-(tileSize*3/7), dragCordY-(tileSize*5/7), boardPlay);
			if(clickX!= x1 || clickY!=y1) 
				click = false;
 		}
 
 		if(!stopPainting)
 		{
 
 			if(checkmate)
 			{
 				g.setColor(new Color(255, 0, 0,125));
 				g.fillRect(0, 0, (getTileSize()+1)*8 + getBorder()*2,(getTileSize()+1)*8+22+getBorder()*2);
 			}
 
 			else if (inCheck(false) || inCheck(true))
 			{
 				g.setColor(new Color(255, 0, 0,50));
 				g.fillRect(0, 0, (getTileSize()+1)*8 + getBorder()*2,(getTileSize()+1)*8+22+getBorder()*2);
 			}
 		}
 
 		
 		
 	}
 
 	public boolean checkmateOrStaleMate(boolean colorWhite)
 	{
 		int whiteMoves = 0;
 		int blackMoves = 0;
 
 		for(int y=0; y<8; y++)
 		{
 			for(int x=0; x<8; x++)
 			{
 				for(int i=0; i<8; i++)
 				{
 					for(int j=0; j<8; j++)
 					{
 						if(!isEmpty(j,i))
 						{
 
 							if(isWhite(j,i)&&board[j][i].validMove(x,y))
 								whiteMoves++;
 							else if(!isWhite(j,i)&&board[j][i].validMove(x,y))
 								blackMoves++;
 						}
 					}
 				}
 			}
 		}
 
 		if(whiteMoves == 0 || blackMoves == 0)
 		{
 			if(inCheck(colorWhite))
 			{
 				checkmate = true;
 				if(whiteMoves == 0)
 					winner = "Black";
 				else
 					winner = "White";
 			}
 			else
 				stalemate = true;
 		}
 
 		if(checkmate)
 			return checkmate;
 		else 
 			return stalemate;
 	}
 }
 

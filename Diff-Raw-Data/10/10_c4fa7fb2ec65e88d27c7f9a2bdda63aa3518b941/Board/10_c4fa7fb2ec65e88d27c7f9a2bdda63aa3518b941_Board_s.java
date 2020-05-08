 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.*;
 enum MOVE {FOWARD, BACK, LEFT, RIGHT, DF_RIGHT, DF_LEFT, DB_RIGHT, DB_LEFT};
 public class Board extends JPanel {
 	public Board(){
 		/* initialize game state here; create pieces,
 		 * populate board, make players, get everything setup
 		 */
 		addMouseListener(new BoardAdapter());
		int xPos = 25;
		int yPos = 25;
 		ROWS = 5;
 		COLS = 9;
 		
 		piecePositions = new Position[COLS][ROWS];	
 		
 		for(int y=0; y<ROWS; y++) {
 			for(int x=0; x<COLS; x++){
 				piecePositions[x][y] = new Position(xPos, yPos);
 				xPos += 75;
 			}
			xPos = 25;
 			yPos += 75;
 		}
 		
 		/*
 		int count=0;
 		for(int i=0; i<ROWS; i++)
 			for(int j=0; j<COLS; j++){
 				System.out.print(piecePositions[j][i].x + ", ");
 				System.out.print(piecePositions[j][i].y + "\n");
 				count++;
 			}
 		System.out.print(count + "\n");
 		*/
 		
 		pieces = new GamePiece[COLS][ROWS]; // total of 44 pieces & 45 spaces to move
 		
 		// top 2 rows - black
 		for(int y=0; y < ROWS-3; y++){
 			 for(int x=0; x < COLS; x++) {
 				GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.BLACK);
 			 	pieces[x][y] = tempPiece;
 			 }
 		 }
 		
 		// middle row - black
 		for(int x=0; x < COLS-5; x+=2) {
 			int y = ROWS - 3;
 			GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.BLACK);
 		 	pieces[x][y] = tempPiece;
 		}
 		for(int x=5; x < COLS; x+=2) {
 			int y = ROWS - 3;
 			GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.BLACK);
 		 	pieces[x][y] = tempPiece;
 		}
 		
 		// middle row - white
 		for(int x=1; x < COLS-4; x+=2) {
 			int y = ROWS - 3;
 			GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.WHITE);
 		 	pieces[x][y] = tempPiece;
 		}
 		for(int x=6; x < COLS+1; x+=2) {
 			int y = ROWS - 3;
 			GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.WHITE);
 		 	pieces[x][y] = tempPiece;
 		}
 
 		// bottom 2 rows
 		for(int y=3; y < ROWS; y++){
 			for(int x=0; x < COLS; x++){
 				GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.WHITE);
 			 	pieces[x][y] = tempPiece;
 			 }
 		 }
 		 
 		 
 		pieceBoardThere = new boolean[ROWS][COLS]; 
 		for(int i = 0; i < pieceBoardThere.length - 1; i++)
 			for(int j = 0; i < pieceBoardThere.length - 1; i++)
 				pieceBoardThere[3][5] = false;
 		
 		pieceBoardColor = new PieceColor[][]{
 				{PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK},
 				{PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK},
 				{PieceColor.BLACK,PieceColor.WHITE,PieceColor.BLACK,PieceColor.WHITE,PieceColor.NULL, PieceColor.BLACK,PieceColor.WHITE,PieceColor.BLACK,PieceColor.WHITE},
 				{PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE},
 				{PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE}
 				};
 	
 	}
 	
 	// still not done has working checking blocked in and if opposite color is within 2 squaures
 	private boolean isMoveValid(GamePiece piece , MOVE move){
 		//the valid move checker
 		int positionX, positionY;
 		positionX = piece.getPosition().x;
 		positionY = piece.getPosition().y;
 		boolean valid = false;
 		//int[][] gridBoard = new int[][];
 		boolean[][] attackGridThere = new boolean[5][5];
 		PieceColor[][] attackGridColor = new PieceColor[5][5];
 		/*(bool,Color)[][] pieceBoard = new (bool,Color)[][] {{(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK)}
 		,{(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK)},{(true,BLACK),(true,WHITE),(true,BLACK),(true,WHITE),(true,NULL),(true,BLACK),(true,WHITE),(true,BLACK),(true,WHITE)}
 		,{(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE)},{(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE)}};
 		*/
 		/*
 		boolean[][] pieceBoard = new boolean[ROWS][COLS]; 
 		Arrays.fill(pieceBoard, true);
 		pieceBoard[3][5] = false;
 		*/
 		/*
 		
 		for(int j=0;j<attackGrid.length + 1; j++){
 			for(int i=0;i<attackGrid[0].length + 1;i++){
 				attackGrid[j][i] = pieceBoard[positionX-2][positionY-2];
 			}
 		}
 		attackGrid[0][0] = pieceBoard[positionX-2][positionY-2]; attackGrid[0][2] = pieceBoard[positionX-2][positionY]; attackGrid[0][4] = pieceBoard[positionX-2][positionY+2];
 		attackGrid[1][1] = pieceBoard[positionX-1][positionY-2]; attackGrid[1][2] = pieceBoard[positionX-1][positionY]; attackGrid[1][3] = pieceBoard[positionX-1][positionY+1];
 		attackGrid[2][0] = pieceBoard[positionX][positionY-2]; attackGrid[2][1] = pieceBoard[positionX][positionY-2]; attackGrid[2][2] = pieceBoard[positionX][positionY]; attackGrid[2][3] = pieceBoard[positionX][positionY+1]; attackGrid[2][4] = pieceBoard[positionX][positionY+2];
 		attackGrid[3][1] = pieceBoard[positionX+1][positionY-2]; attackGrid[3][2] = pieceBoard[positionX+1][positionY]; attackGrid[3][3] = pieceBoard[positionX+1][positionY+1];
 		attackGrid[4][0] = pieceBoard[positionX+2][positionY-2]; attackGrid[4][2] = pieceBoard[positionX+2][positionY]; attackGrid[4][4] = pieceBoard[positionX+2][positionY+2];
 		*/
 		if(positionX == 0){
 			//fill 11-23
 			attackGridThere[1][1] = pieceBoardThere[positionX-1][positionY-2]; attackGridThere[1][2] = pieceBoardThere[positionX-1][positionY]; attackGridThere[1][3] = pieceBoardThere[positionX-1][positionY+1];
 			attackGridThere[2][1] = pieceBoardThere[positionX][positionY-2]; attackGridThere[2][2] = pieceBoardThere[positionX][positionY]; attackGridThere[2][3] = pieceBoardThere[positionX][positionY+1]; 
 		}
 		else if(positionX == 4){
 			//fill 21-33
 			attackGridThere[2][1] = pieceBoardThere[positionX][positionY-2]; attackGridThere[2][2] = pieceBoardThere[positionX][positionY]; attackGridThere[2][3] = pieceBoardThere[positionX][positionY+1];
 			attackGridThere[3][1] = pieceBoardThere[positionX+1][positionY-2]; attackGridThere[3][2] = pieceBoardThere[positionX+1][positionY]; attackGridThere[3][3] = pieceBoardThere[positionX+1][positionY+1];			 
 		}
 		else if(positionY == 0){
 			//fill 12-33
 			attackGridThere[1][2] = pieceBoardThere[positionX-1][positionY]; attackGridThere[1][3] = pieceBoardThere[positionX-1][positionY+1];
 			attackGridThere[2][2] = pieceBoardThere[positionX][positionY]; attackGridThere[2][3] = pieceBoardThere[positionX][positionY+1];
 			attackGridThere[3][2] = pieceBoardThere[positionX+1][positionY]; attackGridThere[3][3] = pieceBoardThere[positionX+1][positionY+1];
 		}
 		else if(positionY == 8){
 			//fill 11-32
 			attackGridThere[1][1] = pieceBoardThere[positionX-1][positionY-2]; attackGridThere[1][2] = pieceBoardThere[positionX-1][positionY];
 			attackGridThere[2][1] = pieceBoardThere[positionX][positionY-2]; attackGridThere[2][2] = pieceBoardThere[positionX][positionY];
 			attackGridThere[3][1] = pieceBoardThere[positionX+1][positionY-2]; attackGridThere[3][2] = pieceBoardThere[positionX+1][positionY]; 
 		}
 		else{
 			//fill all
 			attackGridThere[1][1] = pieceBoardThere[positionX-1][positionY-2]; attackGridThere[1][2] = pieceBoardThere[positionX-1][positionY]; attackGridThere[1][3] = pieceBoardThere[positionX-1][positionY+1];
 			attackGridThere[2][1] = pieceBoardThere[positionX][positionY-2]; attackGridThere[2][2] = pieceBoardThere[positionX][positionY]; attackGridThere[2][3] = pieceBoardThere[positionX][positionY+1];
 			attackGridThere[3][1] = pieceBoardThere[positionX+1][positionY-2]; attackGridThere[3][2] = pieceBoardThere[positionX+1][positionY]; attackGridThere[3][3] = pieceBoardThere[positionX+1][positionY+1];
 		}
 		
 		for(int j=1;j<attackGridThere.length - 1; j++){
 			for(int i=1;i<attackGridThere[0].length - 1;i++){
 				if(pieceBoardThere[i][j] == true)
 				valid = false;
 				else
 				valid = true;
 			}
 		}
 		
 		if(valid == true){
 			//fill 00,02,04,20,24,40,42,44
 			for(int i=0;i<5;i=i+2){
 				for(int j=0;j<5;j=j+2){
 					attackGridThere[i][j] = pieceBoardThere[positionX+(i-2)][positionY+(j-2)];
 					attackGridColor[i][j] = pieceBoardColor[positionX+(i-2)][positionY+(j-2)];
 				}
 			}
 			
 			for(int i=0;i<5;i=i+2){
 				for(int j=0;j<5;j=j+2){
 					if(attackGridColor[i][j] ==  piece.getColor()){
 						attacksAvailable = false;
 					}
 					else
 						attacksAvailable = true;
 				}
 			}
 			// check if any of those are oppostie color and a clear path
 		}
 		
 		if(valid == false){
 			
 		}
 		/*
 		if(piece.getColor() == BLACK)
 					return false;
 				}
 				else{
 					return true;
 					
 				}
 			*/
 			
 			// know locked in pieces
 			// if not locked in does emimy piece within 2 places
 			// if around emepny pieces does have a clear move
 			// create a 2D array of bool that checks can attack chekcing a 5by5 grid around each piece
 			
 		
 		
 		return valid;
 	}
 	
 	public void movePiece(/*need to figure out what goes here*/){
 		if(/*isMoveValid(move stuff)*/true){
 			//do move
 		}
 	}
 	
 	public void paintComponent(Graphics g){
 		super.paintComponent(g);
 		int w = getWidth();
         int h = getHeight();
 		
         // center pt: (325, 175)
         
 		// draw horizontal lines
 		for(int y=50; y<=375; y+=75) {
 			Graphics2D g2 = (Graphics2D) g;
 			g2.setStroke(new BasicStroke(3));
 			g.drawLine(50,y,650,y);
 		}
 		
 		// draw vertical lines
 		for(int x=50; x<=650; x+=75) {
 			Graphics2D g2 = (Graphics2D) g;
 			g2.setStroke(new BasicStroke(3));
 			g.drawLine(x,50,x,350);
 		}
 		
 		// draw diagonal lines
 		Graphics2D g2 = (Graphics2D) g;
 		g2.setStroke(new BasicStroke(3));
 		
 		// left half of board
 		for(int x=0; x<=275; x+=75) {
 			g.drawLine(50,125+x,125+x,50);
 		}
 		for(int x=0; x<=200; x+=75) {
 			g.drawLine(350,275-x,275-x,350);
 		}
 		for(int x=0; x<=275; x+=75) {
 			g.drawLine(275-x,50,350,125+x);
 		}
 		for(int x=0; x<=200; x+=75) {
 			g.drawLine(50,275-x,125+x,350);
 		}
 		
 		// right half of board
 		for(int x=0; x<=275; x+=75){
 			g.drawLine(575-x,50,650,125+x);
 		}
 		for(int x=0; x<=200; x+=75){
 			g.drawLine(350,275-x,425+x,350);
 		}
 		for(int x=0; x<=275; x+=75){
 			g.drawLine(425+x,50,350,125+x);
 		}
 		for(int x=0; x<=200; x+=75){
 			g.drawLine(650,275-x,575-x,350);
 		}
 		
 		
 		// draw top pieces
 		g.setColor(Color.white);
 		for(int x=50; x<=1350; x+=150){
 			g.drawOval(x/2,25,50,50);
 			g.fillOval(x/2,25,50,50);
 		}
 		for(int x=50; x<=1350; x+=150){
 			g.drawOval(x/2,100,50,50);
 			g.fillOval(x/2,100,50,50);
 		}
 		for(int x=200; x<=500; x+=300){
 			g.drawOval(x/2,175,50,50);
 			g.fillOval(x/2,175,50,50);
 		}
 		for(int x=950; x<=1250; x+=300){
 			g.drawOval(x/2,175,50,50);
 			g.fillOval(x/2,175,50,50);
 		}
 
 		
 		// draw bottom pieces
 		g.setColor(Color.black);
 		for(int x=50; x<=350; x+=300){
 			g.drawOval(x/2,175,50,50);
 			g.fillOval(x/2,175,50,50);
 		}
 		for(int x=800; x<=1100; x+=300){
 			g.drawOval(x/2,175,50,50);
 			g.fillOval(x/2,175,50,50);
 		}
 		for(int x=50; x<=1350; x+=150){
 			g.drawOval(x/2,250,50,50);
 			g.fillOval(x/2,250,50,50);
 		}
 		for(int x=50; x<=1350; x+=150){
 			g.drawOval(x/2,325,50,50);
 			g.fillOval(x/2,325,50,50);
 		}
 		
 	}
 
 	private class BoardAdapter extends MouseAdapter {
 		public void mousePressed(MouseEvent me){
 			int mouseX = me.getX();
 			int mouseY = me.getY();
 			System.out.printf("Mouse x: %d\nMouse y: %d\n",mouseX, mouseY);
 			
			for (int i = 0; i < )
 		}
 	}
 	private GamePiece[][] pieces;
 	private boolean[][] pieceBoardThere;
 	private PieceColor[][] pieceBoardColor;
 	private boolean attacksAvailable;
 	private Player player1;
 	private Player player2;
 	private Position[][] piecePositions;
 	int ROWS;
 	int COLS;
 
 }

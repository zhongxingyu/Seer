 import java.io.*;
 import java.awt.*;
 import java.applet.*;
 import javax.swing.*;
 import java.util.*;
 import java.awt.image.*;
 import java.net.*;
 import java.util.Random;
 import java.awt.event.*;
 import java.lang.Math;
 import javax.sound.sampled.*;
 import java.awt.geom.*;
 import java.awt.MultipleGradientPaint.*;
 
 /** 
  *	Lance Elliott
  *  Ross Hudgins
 	Edgardo Angel
  **/
 
 public class Reversi extends Applet implements MouseListener, KeyListener
 {
     int fontsize=30;									// FONTS
     Font Enter = new Font("Serif",Font.BOLD,fontsize);
     Font sub = new Font("Serif",Font.BOLD,10);
     Font title = new Font("Monospaced",Font.BOLD,18);
     Font script = new Font("Dialogue",Font.BOLD,30);
     Font Failed = new Font("Serif",Font.ITALIC,80);
     Font Title = new Font("Serif",Font.BOLD,80);
 
                                                                                                     //Custom Color
     Color Background = new Color(238,	230, 133);
 	Color Background2 = new Color(238,	200, 100);
 	Color Board = new Color(238, 150, 50);
 
     //not Necessary ATM
     Vector ColorVector;
     String yourgrapher;
     //Random generation
     Random rand = new Random();
     int xaxislength = 800;//in case you want need to manipulate somethine based on window size
     int yaxislength = 600;
     int clicks = 0;
     boolean AiPresent=false; // self explanitory booleans
     boolean BothAi = false;
    int nomoves = 0;
     boolean refreshed =false; // for repaint, if not repainted yet, refresh and repaint
 	boolean hostconnected = false;
 	boolean clientconnected = false;
     public static final int apwidth =800; //unchanging values for window size
     public static final int apheight =600;
     boolean mouse_clicked=false;
     boolean typingClient=false;
     public static int p=-1;// this changes the state of the game: -1 = load all values, 0 = reset, 1 = main menu, 2= game.
     int boardoffset = 100; // Lance uses for the board drawing
     public static String HostId = ""; //set by field and Host button
     public static String ClientId = ""; //set by field and Connect button
     public static String PortId = "4444"; //TODO: add fields for Port under Host and Connect
     //pieces
     final int blank = 0; 
     final int black = 1;  
     final int white = 2; 
     final int green = 3;
     //players
     int whiteplayer=0;
     int blackplayer=0;
     final int human = 0;
     final int Ai = 1;
     //difficulties
     int blackdifficulty = 0;
     int whitedifficulty = 0;
     final int easy = 0;
     final int medium = 1;
     final int hard = 2;
 	ReversiServer reversiServer;
     ReversiClient reversiClient;
 	int forwardturns = 0;
     int board[][] = new int[8][8];
     int weight[][] = new int[8][8];//weight value gets calculated for each position
     int undoboards[][][] = new int[10][8][8];
 
     int wordloc=30; //shifting any words
 	int turnchecker = 0;
     int turn = black;
     int otherturn = white;
     int mX = 0;//clicked
     int mY = 0;
     int[][] weights = new int[][]
 		{
 {99, -40, 8, 6, 6, 8, -40, 99},
 {-40, -24, -4, -3, -3, -4, -24, -40},
 {8, -4, 7, 4, 4, 7, -4, 8},
 {6, -3, 4, 0, 0, 4, -3, 6},
 {6, -3, 4, 0, 0, 4, -3, 6},
 {8, -4, 7, 4, 4, 7, -4, 8},
 {-40, -24, -4, -3, -3, -4, -24, -40},
 {99, -40, 8, 6, 6, 8, -40, 99},
 }; // for Hard mode
 
 // GRAPHICS 2D
 
 		Point2D center = new Point2D.Float(50, 50);
 		float radius = 25;
 		Point2D focus = new Point2D.Float(40, 40);
 		float[] dist = {0.0f, 0.2f, 1.0f};
 		Color[] wcolors = {Color.WHITE, Color.lightGray, Color.lightGray};
 		RadialGradientPaint pRadialw = new RadialGradientPaint(center, radius, focus, dist, wcolors, CycleMethod.NO_CYCLE);
 		Color[] bcolors = {Color.WHITE, Color.darkGray, Color.BLACK};
 		RadialGradientPaint pRadialb = new RadialGradientPaint(center, radius, focus, dist, bcolors, CycleMethod.NO_CYCLE);
 		Color[] gcolors = {Color.WHITE, Color.lightGray, Color.GREEN};
 		RadialGradientPaint pRadialg = new RadialGradientPaint(center, radius, focus, dist, gcolors, CycleMethod.NO_CYCLE);
 		
 		Color[] backgroundcolors = {Background,Background2};
 			
 
 	/*
 	public Reversi() {
 		//Frame frame = new Frame();
         Frame frame = new Frame();
         Applet applet = new Reversi();
         frame.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 System.exit(0);
             }	
         });
 
         frame.add(applet);
         frame.setSize(apwidth,apheight);
         frame.show();
 	}*/
 	
     public void init() {
         start();// start any threads or operations(for later if I use them)
 		
     }
 	
 	/*
     public static void main(String arg[])throws Exception {
         //main menu sets up the frame for the application
 
         //Frame frame = new Frame();
         Frame frame = new Frame();
         Applet applet = new Reversi();
         frame.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 System.exit(0);
             }	
         });
 
         frame.add(applet);
         frame.setSize(apwidth,apheight);
 
         frame.show();
         
         // temp values for testing
         //HostId = "quizzical";
         //ClientId = "quizzical";
         PortId = "4444";
 		
         // have an if statement to choose which to start based on if Host or
         // Connect button is pressed, perhaps set a global bool?
         ReversiServer reversiServer = new ReversiServer(PortId);
         System.out.println("is it not getting here?");
         HostId = reversiServer.hostname;
         ReversiClient reversiClient = new ReversiClient(HostId, PortId);
     }
 	*/
 	
     public void keyPressed(KeyEvent evt) //key events
     {    }
 	
     public void keyReleased(KeyEvent evt)	
     {    }
 	
     public void keyTyped(KeyEvent evt) {
         int key=0; key = evt.getKeyCode(); 
         if(typingClient)
         ClientId+=evt.getKeyChar();
         repaint();
     }
 	
     public void mousePressed(MouseEvent e) {//mouse events
     }
 
     public void mouseReleased(MouseEvent e) {
     }  
 
     public void mouseEntered(MouseEvent e) {
     }
 
     public void mouseExited(MouseEvent e) {
     }
 
     public void mouseClicked(MouseEvent e) {
         mX = e.getX();
         mY = e.getY();
         mouse_clicked=true;
             //System.out.println("uhoh");
             repaint();
     }
 
     public void update(Graphics g) {//part of paint, allows for smooth transition of frames. No Flashing2.
 		Graphics offgc;
 		Image offscreen = null;
 		Dimension d = size();
 
 		// create the offscreen buffer and associated Graphics
 		offscreen = createImage(d.width, d.height);
 		offgc = offscreen.getGraphics();
 		// clear the exposed area
 		offgc.setColor(getBackground());
 		offgc.fillRect(0, 0, d.width, d.height);
 		offgc.setColor(getForeground());
 		// do normal redraw
 		paint(offgc);
 		// transfer offscreen to window
 		g.drawImage(offscreen, 0, 0, this);
     }
 	
 	public void paint(Graphics g) { // the main function where the magic happens
 		Graphics2D g2 = (Graphics2D)g;
 		if(p==-1) { // start up the Listeners (happens once)
 			addMouseListener(this); 
 			addKeyListener(this); 
 			p=0;
 			String [ ] MyFontNames = Toolkit.getDefaultToolkit( ) . getFontList( );
 			for (int x = 0; x < MyFontNames.length; x++)
 				System.out.println(MyFontNames[x]);
 		}
 		
 		if(p==0) {
 			System.out.println("Welcome");// reset board and all values (happens every time before the main menu)
 			
 			for (int i=0; i < 8; i++)
 				for (int j=0; j < 8; j++)
 					board[i][j] = blank;
 				
 			board[3][3] = white;
 			board[4][4] = white;
 			board[3][4] = black;
 			board[4][3] = black;
 			p=1;
 			fillundoboards();
 		}
 		
 		if(p==1) { // main menu (redrawn every time you click or repaint)
 			System.out.println("Main Menu");
 			
 			if (mouse_clicked) {
 				selectMenu(mX,mY);
 				mouse_clicked=false;
 			}
 			drawMenu(g2);
 		}
 		
 		if (p==2) { //game
 			if(!gameover()&&mouse_clicked&&isPlayable(mX,mY)&&isTurn(turn)) { //player move
 				ResetMoveList();
 				for(int i=8;i>=0;i--)
 					for(int j=0;j<8;j++)
 						for(int k=0;k<8;k++) {
 							undoboards[i+1][j][k]=undoboards[i][j][k];// this loads the state of the board before the move into the undo slot.
 						}
 				for(int j=0;j<8;j++)
 					for(int k=0;k<8;k++)
 					{
 						undoboards[0][j][k]=board[j][k];
 					}
 				play(mX,mY);
 				//System.out.print("clicked");
 				otherturn = otherturn == white ? black : white;
 				turn = turn == white ? black : white;
 			
 				ResetMoveList();
 				mouse_clicked=false;
 			}				// re-evaluate after player moves.
 			
 			drawBoard(g2);
 			UndoButton(g2,mX,mY);
 			NewGame(g2,mX,mY);
 			findMoves();
 			drawPieces(g2);	
 			
 			if(!isTurn(turn)&&!gameover()) { // if there is an AI present AI goes.
 				delay(5);
 				for(int i=8;i>=0;i--)
 					for(int j=0;j<8;j++)
 						for(int k=0;k<8;k++) {
 							undoboards[i+1][j][k]=undoboards[i][j][k]; // this loads the state of the board before the move into the undo slot.
 						}
 				for(int j=0;j<8;j++)
 					for(int k=0;k<8;k++) {
 						undoboards[0][j][k]=board[j][k];
 					}
 				AiPlay();
 				otherturn = otherturn == white ? black : white;
 				turn = turn == white ? black : white;
 				ResetMoveList();
 			}// re-evaluate after AI moves.
 		
 			drawBoard(g2);
 			UndoButton(g2,mX,mY);
 			findMoves();
 			drawPieces(g2);	
 			NewGame(g2,mX,mY);
 			g2.setFont(Enter);
 			g2.setColor(Color.black);
 			if (gameover()) {
 				System.out.println("game over");
 				getWinner(g2);
 				if (!refreshed) {
 					repaint();
 					refreshed=true;
 				}
 			}
 			if (!gameover()) {
 				g2.drawString(turn == white ? "white's turn" : "black's turn",200,550);
 				if(BothAi)
 				repaint();
 			}
			if(nomoves == turn)
			{
			otherturn = otherturn == white ? black : white;
			turn = turn == white ? black : white;
			repaint();
			}
 		}
 	}
 	
 	public void NewGame (Graphics2D g2, int x, int y) { // if the main menu is pressed GRAPHICS
 	
 		g2.setColor(Color.darkGray);
 		g2.fillRect(548,453,104,44);
 		g2.setColor(Color.lightGray);
 		g2.fillRect(550,455,100,40);
 		g2.setColor(Color.red);
 		g2.setFont(script);
 		g2.drawString("MENU",555,485);
 		if (mouse_clicked&&x<652&&x>548&&y<497&&y>453) {
 			mouse_clicked=false;
 			p=0;
 			ResetMoveList();
 			refreshed = false;
 			AiPresent=false;
 			BothAi=false;
 			repaint();
 		}
     }
 	
 	public boolean gameover () { // boolean for game over possibility
 		int spaces=0;
 		int movepossibilities = 0;
 		for (int i=0; i<8; i++)
 			for (int j=0; j<8; j++) {
 				if(board[i][j]==blank)
 					spaces++;
 				if(board[i][j]==green)
 					movepossibilities++;
 			}
 			
 		if (spaces == 0 && movepossibilities == 0)
 			return true;
 			
		else if (movepossibilities == 0&&nomoves==otherturn) {
 			return true;
 		}
 		
 		else if (movepossibilities == 0) {
			nomoves=turn;
 			return false;
 		}
 		
 		else {
			nomoves=0;
 			return false;
 		}
     }
 	
 	public void getWinner (Graphics2D g2) { // who won if there is a game over GRAPHICS INVOLVED
 		int spaces=0;
 		int movepossibilities = 0;
 		int blackpieces = 0;
 		int whitepieces = 0;
 		for (int i=0; i<8; i++)
 			for (int j=0; j<8; j++) {
 				if(board[i][j]==blank)
 					spaces++;
 				else if(board[i][j]==black)
 					blackpieces++;
 				else if(board[i][j]==white)
 					whitepieces++;
 				else if(board[i][j]==green)
 					movepossibilities++;
 			}
 		System.out.println("blank = " + blank +"black = " + blackpieces +"white = " + whitepieces +"green = " + green);
 		g2.setColor(Color.black);
 		if (blackpieces != whitepieces) {
 			g2.drawString(blackpieces > whitepieces ? "Black Wins!" : "White Wins!",200,550);
 			System.out.println(blackpieces > whitepieces ? "Black Wins! win by number" : "White Wins! win by number");
 		}
 		else {
 			g2.drawString("TIE!",200,550);
 			System.out.println("tie game");
 		}
     }
 	
 	public boolean isPlayable (int x, int y) { //if a location is playable (green)
 		x=(x-boardoffset)/50;
 		y=(y-boardoffset)/50;
 		if (x>=0&&x<=7&&y>=0&&y<=7) {
 			if (board[x][y]==green)
 				return true;
 		}
 		return false;
     }
 	
 	public boolean isTurn (int turn) { // if it is a humans turn
 		if (turn==black&&blackplayer==0) // blackplayer == 0 -> human : 1 -> AI
 			return true;
 		else if(turn==white&&whiteplayer==0)
 			return true;
 		else
 			return false;
 
     }
 	
 	public void play(int x, int y) { // the basic play. change a piece then call the ChangePieces function to apply the move to the board
 		x=(x-boardoffset)/50;
 		y=(y-boardoffset)/50;
 		board[x][y]=turn;
 		System.out.println("ok");
 		ChangePieces(x,y);
     }
 	
 	public int AiPlay() { // the AI moves, calculates the move with the greatest value, then plays there. Board state is then changed.
 		int highestweight = -100;
 		int ivalue = -1;
 		int jvalue = -1;
 		int tempweight = 0;
 		int  n = rand.nextInt(50) + 1;
 		for (int i=0; i<8; i++)
 			for (int j=0; j<8; j++)	{
 				if(board[i][j]==green) {
 					tempweight=getWeight(i,j);
 					if (tempweight>highestweight) {	
 						highestweight = tempweight;
 						ivalue = i;
 						jvalue = j;
 					}
 					
 					if (tempweight==highestweight) {
 						n = rand.nextInt(50);
 						if (n<26) {
 							highestweight = tempweight;
 							ivalue = i;
 							jvalue = j;
 						}
 					}	
 				}
 			}
		//System.out.println(turn == black ? "highest weight for black was "+highestweight+" from "+ivalue+" "+jvalue : "highest weight for white was "+highestweight+" from "+ivalue+" "+jvalue);
 		if (ivalue>=0&&jvalue>=0) {
 			board[ivalue][jvalue]=turn;
 			ChangePieces(ivalue,jvalue);
 		}
 		return highestweight;
     }
 	
 	public void drawMenu(Graphics2D g2) { // draw main menu GRAPHICS
 		g2.setColor(Background);
 		g2.fillRect(0,0,apwidth,apheight);
 		
 		//White side
 		g2.setColor(Color.lightGray);
 		g2.fillRect(25,200,200,50);//human/AI
 		
 		
 		g2.setColor(Color.darkGray);
 		g2.fillRect(25+100*whiteplayer,200,100,50);//which option for human/ai is selected
 		
 		
 		g2.setColor(Color.black);
 		g2.drawRect(25,200,100,50); //outline for player options
 		g2.drawRect(125,200,100,50);
 		
 		if(whiteplayer==1)
 		{
 		g2.setColor(Color.lightGray);
 		g2.fillRect(25,300,225,50);//easy med hard
 		g2.setColor(Color.darkGray);
 		g2.fillRect(25+75*whitedifficulty,300,75,50);// option for difficulty
 		g2.setColor(Color.black);
 		g2.drawRect(25,300,75,50); // outline for difficulty options
 		g2.drawRect(100,300,75,50);
 		g2.drawRect(175,300,75,50);
 		}
 		//Black side
 		g2.setColor(Color.lightGray);
 		g2.fillRect(400,200,200,50);
 		
 		g2.setColor(Color.darkGray);
 		g2.fillRect(400+100*blackplayer,200,100,50);
 		g2.setColor(Color.black);
 		g2.drawRect(400,200,100,50);
 		g2.drawRect(500,200,100,50);
 		if(blackplayer==1)
 		{
 		g2.setColor(Color.lightGray);
 		g2.fillRect(400,300,225,50);
 		g2.setColor(Color.darkGray);
 		g2.fillRect(400+75*blackdifficulty,300,75,50);
 		g2.setColor(Color.black);
 		g2.drawRect(400,300,75,50);
 		g2.drawRect(475,300,75,50);
 		g2.drawRect(550,300,75,50);
 		}
 		//Play Button
 		g2.setColor(Color.lightGray);
 		g2.fillRect(175,450,300,100);
 		g2.setColor(Color.black);
 		g2.drawRect(175,450,300,100);
 		
 		/*//Host Button
 		g2.setColor(Color.lightGray);
 		g2.fillRect(560,375,75,30);
 		g2.setColor(Color.black);
 		g2.drawRect(560,375,75,30);
 		
 		g2.setColor(Color.lightGray);
 		g2.fillRect(498,408,204,34);
 		if(HostId.length()>0)
 			g2.setColor(Color.black);
 		else
 			g2.setColor(Color.darkGray);
 			g2.fillRect(500,410,200,30);
 		
 		
 		//Connect Button
 		g2.setColor(Color.lightGray);
 		g2.fillRect(560,475,75,30);
 		g2.setColor(Color.black);
 		g2.drawRect(560,475,75,30);
 		
 		g2.setColor(Color.lightGray);
 		g2.fillRect(498,508,204,34);
 		if(typingClient)
 			g2.setColor(Color.black);
 		else
 			g2.setColor(Color.darkGray);
 			g2.fillRect(500,510,200,30);*/
 		
 		g2.setColor(Color.black); // All the words.
 		g2.setFont(script);
 		g2.drawString("MAIN MENU",220,100);
 		g2.drawString("WHITE",30,170);
 		g2.drawString("BLACK",420,170);
 		g2.drawString("VS.",300,300);
 		g2.drawString("PLAY",280,510);
 		g2.setFont(title);
 		g2.drawString("Human",30,240);
 		g2.drawString("AI",130,240);
 		g2.drawString("Human",410,240);
 		g2.drawString("AI",510,240);
 		if(whiteplayer==1)
 		{
 		g2.drawString("Easy",30,340);
 		g2.drawString("Medium",105,340);
 		g2.drawString("Hard",180,340);
 		}
 		if(blackplayer==1)
 		{
 		g2.drawString("Easy",405,340);
 		g2.drawString("Medium",480,340);
 		g2.drawString("Hard",555,340);
 		}
 		/*g2.setFont(title);
 		g2.drawString("Host",580,400);
 		g2.drawString("Connect",562,500);*/
 		
 		g2.setColor(Color.white);
 		if(hostconnected)
 			g2.drawString(PortId,505,435);
 		if(clientconnected)
 			g2.drawString(PortId,505,535);
     }
 	
 	public void selectMenu(int x, int y) { // how the mouse selects the main menu (GRAPHICS)
 		if (x<125&&x>25&&y<250&&y>200)
 			whiteplayer = human;
 		if (x<225&&x>125&&y<250&&y>200)
 			whiteplayer = Ai;
 		
 		if (x<100&&x>25&&y<350&&y>300)
 			whitedifficulty=easy;
 		if (x<175&&x>100&&y<350&&y>300)
 			whitedifficulty=medium;
 		if (x<250&&x>175&&y<350&&y>300)
 			whitedifficulty=hard;
 		
 		
 		if (x<500&&x>400&&y<250&&y>200)
 			blackplayer = human;
 		if (x<600&&x>500&&y<250&&y>200)
 			blackplayer = Ai;
 		
 		if (x<475&&x>400&&y<350&&y>300)
 			blackdifficulty=easy;
 		if (x<550&&x>475&&y<350&&y>300)
 			blackdifficulty=medium;
 		if (x<625&&x>550&&y<350&&y>300)
 			blackdifficulty=hard;
 		//start as client
 		/*
 		if (x<635&&x>560&&y<505&&y>475)
 			try{
 				reversiClient = new ReversiClient(HostId, PortId);
 				clientconnected = true;
 				}catch(IOException e){
 					e.printStackTrace();
 				}
 		else
 			typingClient=false;
 		//start as server
 		if (x<635&&x>560&&y<405&&y>375) {
 			try{
 				reversiServer = new ReversiServer(PortId);
 				hostconnected = true;
 				System.out.println("is it not getting here?");
 				HostId = reversiServer.hostname;
 				}catch(IOException e){
 					e.printStackTrace();
 				}
 		}*/
 		//System.out.println("is it not getting here?");
 		
 		if (x<475&&x>175&&y<550&&y>450) { // if play is pressed
 			if (blackplayer==Ai||whiteplayer==Ai)
 				AiPresent=true;
 			if (blackplayer==Ai&&whiteplayer==Ai)
 				BothAi=true;
 			p=2;// change game state.
 		}
     }
 	
 	public void drawBoard(Graphics2D g2) { // draw the game board (GRAPHICS)
 		
 		
 		GradientPaint gp = new GradientPaint(75, 75, Background, 295, 295, Background2, true); //http://oreilly.com/catalog/java2d/chapter/ch04.html
 		// Fill with a gradient.
 		g2.setPaint(gp);
 		//g2.fill(e);
 		g2.fillRect(0,0,apwidth,apheight);
 		g2.setColor(Board);
 		g2.fill3DRect(boardoffset-10, boardoffset-10, 420, 420, true);
 		g2.setColor(Color.black);
 		g2.drawRect(boardoffset,boardoffset,400,400);
 		for(int i=boardoffset; i<500; i+=50) {
 			g2.drawLine(i,boardoffset,i,boardoffset + 400);
 			g2.drawLine(boardoffset,i,boardoffset + 400,i);
 		}
     }
 	
 	public void UndoButton(Graphics2D g2,int x,int y) { // undo (GRAPHICS)
 		int iterator=1;
 		g2.setColor(Color.darkGray);
 		g2.fillRect(548,353,104,44);
 		g2.setColor(Color.lightGray);
 		g2.fillRect(550,355,100,40);
 		g2.setColor(Color.red);
 		g2.setFont(script);
 		g2.drawString("UNDO",555,385);
 		
 		if (mouse_clicked&&x<652&&x>548&&y<397&&y>353) {
 			mouse_clicked=false;
 			if(AiPresent==true) {
 				iterator=2;
				//System.out.println("doubles");
 			}
 			for(int it=0;it<iterator;it++) {
 				UndoMove();
 			}
 		}
     }
 	public void UndoMove()
 	{
 	boolean difference = false;
 				System.out.println("undo");
 				ResetMoveList();
 				for(int j=0;j<8;j++)
 					for(int k=0;k<8;k++) {
 						if (board[j][k]!=undoboards[0][j][k])
 							difference=true;
 						board[j][k]=undoboards[0][j][k];
 					}
 				if (difference) {
 					otherturn = otherturn == white ? black : white;
 					turn = turn == white ? black : white;
 				}
 				for(int i=0;i<9;i++)
 					for(int j=0;j<8;j++)
 						for(int k=0;k<8;k++) {
 							undoboards[i][j][k]=undoboards[i+1][j][k];
 						}
 				ResetMoveList();
 	}
 	public void drawPieces(Graphics2D g2) { // draw all the pieces on the board (GRAPHICS)
 		for (int i=0; i<8; i++)
 			for (int j=0; j<8; j++) {
 				if(board[i][j]==white) {
 					Point2D center = new Point2D.Float(boardoffset +25+ 50*i,boardoffset +25+50*j); //http://oreilly.com/catalog/java2d/chapter/ch04.html
 					float radius = 25;
 					focus = new Point2D.Float(boardoffset +15+ 50*i,boardoffset +15+50*j);
 					float[] dist = {0.0f, 0.9f, 1.0f};
 					RadialGradientPaint pRadialw = new RadialGradientPaint(center, radius, focus, dist, wcolors, CycleMethod.NO_CYCLE);
 					Ellipse2D e = new Ellipse2D.Double(boardoffset + 50*i,boardoffset + 50*j,50,50);
 					g2.setPaint(pRadialw);
 					g2.fill(e);
 				}
 				else if(board[i][j]==black) {
 					Point2D center = new Point2D.Float(boardoffset +25+ 50*i,boardoffset +25+50*j);
 					float radius = 25;
 					focus = new Point2D.Float(boardoffset +15+ 50*i,boardoffset +15+50*j);
 					float[] dist = {0.0f, 0.1f, 1.0f};
 					RadialGradientPaint pRadialb = new RadialGradientPaint(center, radius, focus, dist, bcolors, CycleMethod.NO_CYCLE);
 					Ellipse2D e = new Ellipse2D.Double(boardoffset + 50*i,boardoffset + 50*j,50,50);
 					g2.setPaint(pRadialb);
 					g2.fill(e);
 				}
 				else if(board[i][j]==green) {
 					Point2D center = new Point2D.Float(boardoffset +25+ 50*i,boardoffset +25+50*j);
 					float radius = 10;
 					focus = new Point2D.Float(boardoffset+ 20 + 50*i,boardoffset+ 20 + 50*j);
 					float[] dist = {0.0f, 0.2f, 1.0f};
 					RadialGradientPaint pRadialg = new RadialGradientPaint(center, radius, focus, dist, gcolors, CycleMethod.NO_CYCLE);
 					Ellipse2D e = new Ellipse2D.Double(boardoffset+ 15 + 50*i,boardoffset+ 15 + 50*j,20,20);
 					g2.setPaint(pRadialg);
 					g2.fill(e);
 				}
 			}
     }
 	
 	public void findMoves() { // find green pieces for every piece you own
 		for (int i=0; i<8; i++)
 			for (int j=0; j<8; j++) {
 				if(board[i][j]==turn) {
 					//System.out.println(turn + " i = " + i + " j = " + j);
 					setMoves(i,j);
 				}
 			}
     }
 	
 	public void ResetMoveList() { // changes all green to blank
 		for (int i=0; i<8; i++)
 			for (int j=0; j<8; j++) {
 				if(board[i][j]==green) {
 					board[i][j]=blank;
 				}
 			}
     }
 	
 	public void ChangePieces(int i,int j) { // applies transformation after move is played, check every direction around the piece and apply the correct color to it
 		//left
 		int resetj=j;
 		int reseti=i;
 		int counter = 0;
 		if(j>0&&board[i][j-1]!=blank&&board[i][j-1]!=green) {
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				if (board[i][j]==turn)
 					counter++;
 					j--;
 			}
 			i=reseti;
 			j=resetj;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green&&counter>0) {
 				if (board[i][j]==turn) {
 					counter--;
 				}
 				board[i][j]=turn;
 				j--;
 			}
 			i=reseti;
 			j=resetj;
 		}
 		
 		//right
 		if(j<7&&board[i][j+1]!=blank&&board[i][j+1]!=green) {
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				if (board[i][j]==turn)
 					counter++;
 				j++;
 			}
 			i=reseti;
 			j=resetj;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green&&counter>0) {
 				if (board[i][j]==turn) {
 					counter--;
 				}
 				board[i][j]=turn;
 				j++;
 			}
 			i=reseti;
 			j=resetj;
 			counter=0;
 		}
 		
 		//up
 		if(i>0&&board[i-1][j]!=blank&&board[i-1][j]!=green) {
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				if (board[i][j]==turn)
 					counter++;
 				i--;
 			}
 			i=reseti;
 			j=resetj;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green&&counter>0) {
 				if (board[i][j]==turn) {
 				counter--;
 				}
 				board[i][j]=turn;
 				i--;
 			}
 			i=reseti;
 			j=resetj;
 			counter=0;
 		}
 		
 		//down
 		if(i<7&&board[i+1][j]!=blank&&board[i+1][j]!=green) {
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				if (board[i][j]==turn)
 					counter++;
 				i++;
 			}
 			i=reseti;
 			j=resetj;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green&&counter>0) {
 				if (board[i][j]==turn) {
 				counter--;
 				}
 				board[i][j]=turn;
 				i++;
 			}
 			i=reseti;
 			j=resetj;
 			counter=0;
 		}
 		
 		//up left
 		if(i<7&&j>0&&board[i+1][j-1]!=blank&&board[i+1][j-1]!=green) {
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				if (board[i][j]==turn)
 					counter++;
 				j--;
 				i++;
 			}
 			i=reseti;
 			j=resetj;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green&&counter>0) {
 				if (board[i][j]==turn) {
 				counter--;
 				}
 				board[i][j]=turn;
 				j--;
 				i++;
 			}
 			i=reseti;
 			j=resetj;
 			counter=0;
 		}
 		
 		//up right
 		if(i<7&&j<7&&board[i+1][j+1]!=blank&&board[i+1][j+1]!=green) {
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				if (board[i][j]==turn)
 					counter++;
 				j++;
 				i++;
 			}
 			i=reseti;
 			j=resetj;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green&&counter>0) {
 				if (board[i][j]==turn) {
 				counter--;
 				}
 				board[i][j]=turn;
 				j++;
 				i++;
 			}
 			i=reseti;
 			j=resetj;
 			counter=0;
 		}
 		
 		//down left
 		if(i>0&&j>0&&board[i-1][j-1]!=blank&&board[i-1][j-1]!=green) {
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				if (board[i][j]==turn)
 					counter++;
 				j--;
 				i--;
 			}
 			i=reseti;
 			j=resetj;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green&&counter>0) {
 				if (board[i][j]==turn) {
 				counter--;
 				}
 				board[i][j]=turn;
 				j--;
 				i--;
 			}
 			i=reseti;
 			j=resetj;
 			counter=0;
 		}
 		
 		//down right
 		if(i>0&&j<7&&board[i-1][j+1]!=blank&&board[i-1][j+1]!=green) {
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				if (board[i][j]==turn)
 					counter++;
 				j++;
 				i--;
 			}
 			i=reseti;
 			j=resetj;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green&&counter>0) {
 				if (board[i][j]==turn) {
 				counter--;
 				}
 				board[i][j]=turn;
 				j++;
 				i--;
 			}
 			i=reseti;
 			j=resetj;
 			counter=0;
 		}	
     }
 	
 	public void setMoves(int i,int j) { // just like change pieces, finds all directions around a piece and finds possible moves
 		//left
 		int resetj=j;
 		int reseti=i;
 		if(j>0&&board[i][j-1]==otherturn) {
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				j--;
 			}
 			if(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]==blank)
 				board[i][j]=green;
 			i=reseti;
 			j=resetj;
 		}
 		
 		//right
 		if(j<7&&board[i][j+1]==otherturn) {
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				j++;
 			}
 			if(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]==blank)
 				board[i][j]=green;
 			i=reseti;
 			j=resetj;
 		}
 		
 		//up
 		if(i>0&&board[i-1][j]==otherturn) {
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				i--;
 			}
 			if(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]==blank)
 				board[i][j]=green;
 			i=reseti;
 			j=resetj;
 		}
 		
 		//down
 		if(i<7&&board[i+1][j]==otherturn) {
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				i++;
 			}
 			if(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]==blank)
 				board[i][j]=green;
 			i=reseti;
 			j=resetj;
 		}	
 		
 		//up left
 		if(i<7&&j>0&&board[i+1][j-1]==otherturn) {
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				j--;
 				i++;
 			}
 			if(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]==blank)
 				board[i][j]=green;
 			i=reseti;
 			j=resetj;
 		}
 		
 		//up right
 		if(i<7&&j<7&&board[i+1][j+1]==otherturn) {
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				j++;
 				i++;
 			}
 			if(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]==blank)
 				board[i][j]=green;
 			i=reseti;
 			j=resetj;
 		}
 		
 		//down left
 		if(i>0&&j>0&&board[i-1][j-1]==otherturn) {
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				j--;
 				i--;
 			}
 			if(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]==blank)
 				board[i][j]=green;
 			i=reseti;
 			j=resetj;
 		}
 		
 		//down right
 		if(i>0&&j<7&&board[i-1][j+1]==otherturn) {
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				j++;
 				i--;
 			}
 			if(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]==blank)
 				board[i][j]=green;
 			i=reseti;
 			j=resetj;
 		}	
     }
 	
 	public int getWeight(int i, int j) { // finds the wieght of any move by the number of pieces it will take in every direction and the other alogrithms
 		//left
 		int resetj=j;
 		int reseti=i;
 		int counter = 0;
 		if(j>0&&board[i][j-1]!=blank&&board[i][j-1]!=green) {
 			j--;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				if (board[i][j]!=turn)
 					counter++;
 				j--;
 			}
 			i=reseti;
 			j=resetj;
 			
 		}
 		
 		//right
 		if(j<7&&board[i][j+1]!=blank&&board[i][j+1]!=green) {
 		j++;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				if (board[i][j]!=turn)
 					counter++;
 				j++;
 			}
 			i=reseti;
 			j=resetj;
 			
 		}
 		
 		//up
 		if(i>0&&board[i-1][j]!=blank&&board[i-1][j]!=green) {
 		i--;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				if (board[i][j]!=turn)
 					counter++;
 				i--;
 			}
 			i=reseti;
 			j=resetj;
 			
 		}
 		
 		//down
 		if(i<7&&board[i+1][j]!=blank&&board[i+1][j]!=green) {
 		i++;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				if (board[i][j]!=turn)
 					counter++;
 				i++;
 			}
 			i=reseti;
 			j=resetj;
 			
 		}
 		
 		//up left
 		if(i<7&&j>0&&board[i+1][j-1]!=blank&&board[i+1][j-1]!=green) {
 			j--;
 			i++;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				if (board[i][j]!=turn)
 					counter++;
 				j--;
 				i++;
 			}
 			i=reseti;
 			j=resetj;
 		}
 		
 		//up right
 		if(i<7&&j<7&&board[i+1][j+1]!=blank&&board[i+1][j+1]!=green) {
 			j++;
 			i++;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				if (board[i][j]!=turn)
 					counter++;
 				j++;
 				i++;
 			}
 			i=reseti;
 			j=resetj;
 		}
 		
 		//down left
 		if(i>0&&j>0&&board[i-1][j-1]!=blank&&board[i-1][j-1]!=green) {
 			j--;
 			i--;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				if (board[i][j]!=turn)
 					counter++;
 				j--;
 				i--;
 			}
 			i=reseti;
 			j=resetj;
 		}
 		
 		//down right
 		if(i>0&&j<7&&board[i-1][j+1]!=blank&&board[i-1][j+1]!=green) {
 			j++;
 			i--;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green) {
 				if (board[i][j]!=turn)
 					counter++;
 				j++;
 				i--;
 			}
 			i=reseti;
 			j=resetj;
 		}
 		
 		if ((turn==black&&blackdifficulty==medium)||(turn==white&&whitedifficulty==medium)) {
 			if (isCorner(i,j))
 				counter+=10;
 			if (isSubCorner(i,j))
 				counter-=3;
 		}
 		
 		if ((turn==black&&blackdifficulty==hard)||(turn==white&&whitedifficulty==hard)) {
 			counter+=applyHard(i,j);
 		}
 		
 		//System.out.println("weight of "+i+" "+j+" = " + counter);
 		return counter;
 	}
 	
 	public int applyHard(int ival, int jval) { // applies the weights of the board to a move
 	double calculation=0;
 	int saveddifficulty=otherturn == white ? whitedifficulty : blackdifficulty; // save both current difficulties
 	int currentdifficulty = turn == white ? whitedifficulty : blackdifficulty; 
 	whitedifficulty=currentdifficulty; // for calculating, assume the other AI thinks like you, both AI are set to the current thinkers level
 	blackdifficulty = currentdifficulty;
 		if (forwardturns==0)//while you are not already looking ahead
 		{
 		while(forwardturns < 1)//look ahead this many
 		{
 		forwardturns++;
 		board[ival][jval]=turn;// apply the current move
 		ChangePieces(ival,jval);//apply
 		otherturn = otherturn == white ? black : white;//next turn
 		turn = turn == white ? black : white;
 		ResetMoveList();//find that turns moves.
 		findMoves();
 		calculation -= AiPlay()/2;//calculate the highest possible score they have
 		//System.out.println(calculation);
 		otherturn = otherturn == white ? black : white;//next turn
 		turn = turn == white ? black : white;
 		
 		}
 		
 		for(int j=0;j<8;j++)
 					for(int k=0;k<8;k++) {
 						board[j][k]=undoboards[0][j][k];
 						}
 		forwardturns=0;
 		}
 		//System.out.println(turn + "white = " + white);
 		
 		ResetMoveList();
 		findMoves();
 		if(otherturn == white) whitedifficulty=saveddifficulty; else blackdifficulty=saveddifficulty;
 		calculation +=weights[ival][jval];
 		return (int)calculation;
 		
 	}
 	
 	public boolean isCorner(int i, int j) { // returns if the tile is a corner (good)
 		if((i==0&&j==0)||(i==0&&j==7)||(i==7&&j==0)||(i==7&&j==7))
 			return true;
 		return false;
 	}
 	
 	public boolean isSubCorner(int i, int j) { // returns if the tile touches a corner (bad)
 		if((i==1&&j==0)||(i==1&&j==7)||(i==7&&j==1)||(i==7&&j==6)||(i==1&&j==1)||(i==1&&j==6)||(i==6&&j==0)||(i==6&&j==7)||(i==0&&j==1)||(i==0&&j==6)||(i==6&&j==1)||(i==6&&j==6))
 			return true;
 		return false;
 	}
 		
     public void fillundoboards() { // preload undo boards with the beginnning state
 		for(int i=0;i<10;i++)
 			for(int j=0;j<7;j++)
 				for(int k=0;k<7;k++) {
 					undoboards[i][j][k]=board[j][k];
 				}
 	}
 	
     public void delay(double n) { // for a delay
 		long startDelay = System.currentTimeMillis();
 		long endDelay = 0;
 		while (endDelay - startDelay < n)
 			endDelay = System.currentTimeMillis();	
 	}
 }	
 

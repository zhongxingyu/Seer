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
 
 /** 
  *	Lance Elliott
  *  
  **/
 
 
  
 public class Reversi extends Applet implements MouseListener
 {
 	int fontsize=30;
 	Font Enter = new Font("Serif",Font.BOLD,fontsize);
 	Font title = new Font("Serif",Font.BOLD,19);
 	Font script = new Font("Serif",Font.BOLD,30);
 	Font Failed = new Font("Serif",Font.ITALIC,80);
 	Font Title = new Font("Serif",Font.BOLD,80);
 	Vector NoteVector;
 	Color Background = new Color(238,	230, 133);
 	//addMouseListener(this);
 	Vector ColorVector;
 	String yourgrapher;
 	Random rand = new Random();
 	int xaxislength = 800;
 	int yaxislength = 600;
 	int clicks = 0;
 	boolean AiPresent=false;
 	boolean BothAi = false;
 	boolean nomoves = false;
 	boolean refreshed =false;
 	public static final int apwidth =800;
 	public static final int apheight =600;
 	boolean mouse_clicked=false;
 	int p=-1;
 	int boardoffset = 100;
 	
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
 	
 	int board[][] = new int[8][8];
 	int weight[][] = new int[8][8];
 	int undoboards[][][] = new int[10][8][8];
 	
 	int wordloc=30; //shifting any words
 	
 	int turn = black;
 	int otherturn = white;
 	int mX = 0;//clicked
 	int mY = 0;
 	int[][] weights = new int[][]
 								{
 								{50, -4, 4, 3, 3, 4, -4, 50},
 								{-4, -8, -2, -2, -2, -2, -8, -4},
 								{4, -2, 4, 2, 2, 4, -2, 4},
 								{3, -2, 2, 0, 0, 2, -2, 3},
 								{3, -2, 2, 0, 0, 2, -2, 3},
 								{4, -2, 4, 2, 2, 4, -2, 4},
 								{-4, -8, -2, -2, -2, -2, -8, -4},
 								{50, -4, 4, 3, 3, 4, -4, 50}
 								};
 	public void init()
 	{
 		start();
 	}
 	 public static void main(String arg[])throws Exception {
   	{
 	
   	
     
     //Frame frame = new Frame();
 		Frame frame = new Frame();
 		Applet applet = new Reversi();
 		frame.addWindowListener(new WindowAdapter()
 		{
 		public void windowClosing(WindowEvent e)
 			{
 				System.exit(0);
 			}	
 		});
 
     frame.add(applet);
     frame.setSize(apwidth,apheight);
 	
     frame.show();
   	}
 	}
 	public void mousePressed(MouseEvent e) {
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
 
 
 	public void update(Graphics g) {
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
 	public void paint(Graphics g)
 	{
 		if(p==-1)
 		{
 		addMouseListener(this); 
 		p=0;
 		}
 		if(p==0)
 		{
 		System.out.println("Welcome");
 			
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
 		if(p==1)
 		{
 		System.out.println("Main Menu");
 		
 		if (mouse_clicked)
 		{
 		selectMenu(mX,mY);
 		mouse_clicked=false;
 		}
 		drawMenu(g);
 		}
 		if (p==2)
 		{
 			if(!gameover()&&mouse_clicked&&isPlayable(mX,mY)&&isTurn(turn))
 			{
 			ResetMoveList();
 			for(int i=8;i>=0;i--)
 				for(int j=0;j<7;j++)
 					for(int k=0;k<7;k++)
 					{
 					undoboards[i+1][j][k]=undoboards[i][j][k];
 					}
 			for(int j=0;j<7;j++)
 				for(int k=0;k<7;k++)
 				{
 					undoboards[0][j][k]=board[j][k];
 				}
 			play(mX,mY);
 			//System.out.print("clicked");
 			otherturn = otherturn == white ? black : white;
 			turn = turn == white ? black : white;
 		
 			ResetMoveList();
 			mouse_clicked=false;
 			}
 			drawBoard(g);
 			UndoButton(g,mX,mY);
 			NewGame(g,mX,mY);
 			findMoves();
 			drawPieces(g);	
 			if(!isTurn(turn)&&!gameover())
 			{
 			delay(1);
 			for(int i=8;i>=0;i--)
 				for(int j=0;j<7;j++)
 					for(int k=0;k<7;k++)
 					{
 					undoboards[i+1][j][k]=undoboards[i][j][k];
 					}
 			for(int j=0;j<7;j++)
 				for(int k=0;k<7;k++)
 				{
 					undoboards[0][j][k]=board[j][k];
 				}
 			AiPlay();
 			otherturn = otherturn == white ? black : white;
 			turn = turn == white ? black : white;
 			ResetMoveList();
 			}
 		
 			drawBoard(g);
 			UndoButton(g,mX,mY);
 			findMoves();
 			drawPieces(g);	
 			NewGame(g,mX,mY);
 			g.setFont(Enter);
 			g.setColor(Color.black);
 			if (gameover())
 			{
 				System.out.println("game over");
 				getWinner(g);
 				if (!refreshed)
 				{
 				repaint();
 				refreshed=true;
 				}
 				
 			}
 			if (!gameover())
 			{
 				g.drawString(turn == white ? "white's turn" : "black's turn",200,550);
 				if(BothAi)
 				repaint();
 			}
 		}
 	}
 	public void NewGame (Graphics g, int x, int y) {
 	
 		g.setColor(Color.darkGray);
 		g.fillRect(548,453,104,44);
 		g.setColor(Color.lightGray);
 		g.fillRect(550,455,100,40);
 		g.setColor(Color.red);
 		g.setFont(script);
 		g.drawString("MENU",555,485);
 		if (mouse_clicked&&x<652&&x>548&&y<497&&y>453)
 		{
 		mouse_clicked=false;
 		p=0;
 		ResetMoveList();
 		refreshed = false;
 		repaint();
 		}
     }
 	public boolean gameover () {
 		int spaces=0;
 		int movepossibilities = 0;
 		for (int i=0; i<8; i++)
 			for (int j=0; j<8; j++)
 			{
 			if(board[i][j]==blank)
 			spaces++;
 			if(board[i][j]==green)
 			movepossibilities++;
 			}
 		if (spaces == 0 && movepossibilities == 0)
 			return true;
 		else if (movepossibilities == 0&&nomoves)
 		{
 			return true;
 		}
 		else if (movepossibilities == 0)
 		{
 			nomoves=true;
 			return false;
 		}
 		else 
 		{
 			nomoves=false;
 			return false;
 			}
     }
 	
 	public void getWinner (Graphics g) {
 		int spaces=0;
 		int movepossibilities = 0;
 		int blackpieces = 0;
 		int whitepieces = 0;
 		for (int i=0; i<8; i++)
 			for (int j=0; j<8; j++)
 			{
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
 		g.setColor(Color.black);
 		/*if (movepossibilities == 0 && spaces>0)
 		{
 			/*if (blackpieces == 0 ||whitepieces == 0)
 			{
 			g.drawString(turn == white ? "Black Wins!" : "White Wins!",200,550);
 			System.out.println(turn == white ? "Black Wins! win by moves" : "White Wins! win by moves");
 			/*}
 			else
 			{
 			g.drawString(turn == black ? "Black Wins!" : "White Wins!",200,550);
 			System.out.println(turn == black? "Black Wins! win by moves" : "White Wins! win by moves");
 			}
 			
 		}*/
 		if (blackpieces != whitepieces)
 		{
 		g.drawString(blackpieces > whitepieces ? "Black Wins!" : "White Wins!",200,550);
 		System.out.println(blackpieces > whitepieces ? "Black Wins! win by number" : "White Wins! win by number");
 		}
 		else
 		{
 		g.drawString("TIE!",200,550);
 		System.out.println("tie game");
 		}
     }
 	public boolean isPlayable (int x, int y) {
 		x=(x-boardoffset)/50;
 		y=(y-boardoffset)/50;
 		if (x>=0&&x<=7&&y>=0&&y<=7)
 		{
 		if (board[x][y]==green)
 			return true;
 		}
 			return false;
     }
 	public boolean isTurn (int turn) {
 		if (turn==black&&blackplayer==0)
 		return true;
 		else if(turn==white&&whiteplayer==0)
 		return true;
 		else
 		return false;
 
     }
 	
 	public void play(int x, int y) {
 	x=(x-boardoffset)/50;
 	y=(y-boardoffset)/50;
 		board[x][y]=turn;
 		System.out.println("ok");
 		ChangePieces(x,y);
     }
 	public void AiPlay() {
 	int highestweight = 0;
 	int ivalue = -1;
 	int jvalue = -1;
 	int tempweight = 0;
 	int  n = rand.nextInt(50) + 1;
 	for (int i=0; i<8; i++)
 			for (int j=0; j<8; j++)
 			{
 			if(board[i][j]==green)
 				{
 				tempweight=getWeight(i,j);
 				if (tempweight>highestweight)
 					{	
 					highestweight = tempweight;
 					ivalue = i;
 					jvalue = j;
 					}
 				
 				if (tempweight==highestweight)
 					{
 					n = rand.nextInt(50);
 					if (n<26)
 					{
 					highestweight = tempweight;
 					ivalue = i;
 					jvalue = j;
 					}
 				}	
 				}
 			}
 	System.out.println("highest weight was "+highestweight+" from "+ivalue+" "+jvalue);
 	if (ivalue>=0&&jvalue>=0)
 	{
 	board[ivalue][jvalue]=turn;
 	ChangePieces(ivalue,jvalue);
 	}
     }
 	public void drawMenu(Graphics g) {
 		g.setColor(Background);
 		g.fillRect(0,0,apwidth,apheight);
 		
 		//White side
 		g.setColor(Color.lightGray);
 		g.fillRect(25,200,200,50);
 		g.fillRect(25,300,225,50);
 		
 		g.setColor(Color.darkGray);
 		g.fillRect(25+100*whiteplayer,200,100,50);
 		g.fillRect(25+75*whitedifficulty,300,75,50);
 		
 		g.setColor(Color.black);
 		g.drawRect(25,200,100,50);
 		g.drawRect(125,200,100,50);
 		
 		g.drawRect(25,300,75,50);
 		g.drawRect(100,300,75,50);
 		g.drawRect(175,300,75,50);
 		
 		//Black side
 		g.setColor(Color.lightGray);
 		g.fillRect(400,200,200,50);
 		g.fillRect(400,300,225,50);
 		
 		g.setColor(Color.darkGray);
 		g.fillRect(400+100*blackplayer,200,100,50);
 		g.fillRect(400+75*blackdifficulty,300,75,50);
 		g.setColor(Color.black);
 		g.drawRect(400,200,100,50);
 		g.drawRect(500,200,100,50);
 		
 		g.drawRect(400,300,75,50);
 		g.drawRect(475,300,75,50);
 		g.drawRect(550,300,75,50);
 		
 		//Play Button
 		g.setColor(Color.lightGray);
 		g.fillRect(175,450,300,100);
 		g.setColor(Color.black);
 		g.drawRect(175,450,300,100);
 		
 		g.setColor(Color.black);
 		g.setFont(script);
 		g.drawString("MAIN MENU",220,100);
 		g.drawString("WHITE",30,170);
 		g.drawString("BLACK",420,170);
 		g.drawString("VS.",320,300);
 		g.drawString("PLAY",280,510);
 		g.setFont(title);
 		g.drawString("Human",30,240);
 		g.drawString("AI",130,240);
 		g.drawString("Human",410,240);
 		g.drawString("AI",510,240);
 		
 		g.drawString("Easy",30,340);
 		g.drawString("Medium",105,340);
 		g.drawString("Hard",180,340);
 		g.drawString("Easy",405,340);
 		g.drawString("Medium",480,340);
 		g.drawString("Hard",555,340);
 		
     }
 	public void selectMenu(int x, int y) {
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
 		
 		if (x<475&&x>175&&y<550&&y>450)
 		{
 			if (blackplayer==Ai||whiteplayer==Ai)
 			AiPresent=true;
 			if (blackplayer==Ai&&whiteplayer==Ai)
 			BothAi=true;
 		p=2;
 		}
     }
 	public void drawBoard(Graphics g) {
 		g.setColor(Background);
 		g.fillRect(0,0,apwidth,apheight);
 		g.setColor(Color.black);
 		g.drawRect(boardoffset,boardoffset,400,400);
 		for(int i=boardoffset; i<500; i+=50)
 		{
 			g.drawLine(i,boardoffset,i,boardoffset + 400);
 			g.drawLine(boardoffset,i,boardoffset + 400,i);
 		}
 
     }
 	public void UndoButton(Graphics g,int x,int y) {
 	boolean difference = false;
 	int iterator=1;
 		g.setColor(Color.darkGray);
 		g.fillRect(548,353,104,44);
 		g.setColor(Color.lightGray);
 		g.fillRect(550,355,100,40);
 		g.setColor(Color.red);
 		g.setFont(script);
 		g.drawString("UNDO",555,385);
 		
 		if (mouse_clicked&&x<652&&x>548&&y<397&&y>353)
 		{
 		mouse_clicked=false;
 		if(AiPresent==true)
 		{
 		iterator=2;
 		System.out.println("doubles");
 		}
 		for(int it=0;it<iterator;it++)
 		{
 		System.out.println("undo");
 		ResetMoveList();
 		for(int j=0;j<7;j++)
 				for(int k=0;k<7;k++)
 				{
 				if (board[j][k]!=undoboards[0][j][k])
 					difference=true;
 				board[j][k]=undoboards[0][j][k];
 				}
 		if (difference)
 				{
 				otherturn = otherturn == white ? black : white;
 				turn = turn == white ? black : white;
 				}
 		for(int i=0;i<9;i++)
 			for(int j=0;j<7;j++)
 				for(int k=0;k<7;k++)
 				{
 				undoboards[i][j][k]=undoboards[i+1][j][k];
 				}
 		ResetMoveList();
 		}
 		}
 		
     }
 	public void drawPieces(Graphics g) {
 		for (int i=0; i<8; i++)
 			for (int j=0; j<8; j++)
 			{
 			if(board[i][j]==white)
 			{
 				g.setColor(Color.white);
 				g.fillOval(boardoffset + 50*i,boardoffset + 50*j,50,50);
 			}
 			else if(board[i][j]==black)
 			{
 				g.setColor(Color.black);
 				g.fillOval(boardoffset + 50*i,boardoffset + 50*j,50,50);
 			}
 			else if(board[i][j]==green)
 			{
 				g.setColor(Color.green);
 				g.fillOval(boardoffset+ 15 + 50*i,boardoffset+ 15 + 50*j,20,20);
 			}
 			}
     }
 	public void findMoves() {
 		for (int i=0; i<8; i++)
 			for (int j=0; j<8; j++)
 			{
 			if(board[i][j]==turn)
 				{
 				//System.out.println(turn + " i = " + i + " j = " + j);
 				setMoves(i,j);
 				}
 			}
     }
 	public void ResetMoveList() {
 		for (int i=0; i<8; i++)
 			for (int j=0; j<8; j++)
 			{
 			if(board[i][j]==green)
 				{
 				board[i][j]=blank;
 				}
 			}
     }
 	public void ChangePieces(int i,int j) {
 		//left
 		int resetj=j;
 		int reseti=i;
 		int counter = 0;
 		if(j>0&&board[i][j-1]!=blank&&board[i][j-1]!=green)
 		{
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				if (board[i][j]==turn)
 					counter++;
 					j--;
 			}
 			i=reseti;
 			j=resetj;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green&&counter>0)
 			{
 				if (board[i][j]==turn)
 				{
 				counter--;
 				}
 				board[i][j]=turn;
 				j--;
 			}
 			i=reseti;
 			j=resetj;
 		}
 		
 		//right
 		if(j<7&&board[i][j+1]!=blank&&board[i][j+1]!=green)
 		{
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				if (board[i][j]==turn)
 					counter++;
 				j++;
 			}
 			i=reseti;
 			j=resetj;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green&&counter>0)
 			{
 				if (board[i][j]==turn)
 				{
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
 		if(i>0&&board[i-1][j]!=blank&&board[i-1][j]!=green)
 		{
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				if (board[i][j]==turn)
 					counter++;
 				i--;
 			}
 			i=reseti;
 			j=resetj;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green&&counter>0)
 			{
 				if (board[i][j]==turn)
 				{
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
 		if(i<7&&board[i+1][j]!=blank&&board[i+1][j]!=green)
 		{
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				if (board[i][j]==turn)
 					counter++;
 				i++;
 			}
 			i=reseti;
 			j=resetj;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green&&counter>0)
 			{
 				if (board[i][j]==turn)
 				{
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
 		if(i<7&&j>0&&board[i+1][j-1]!=blank&&board[i+1][j-1]!=green)
 		{
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				if (board[i][j]==turn)
 					counter++;
 				j--;
 				i++;
 			}
 			i=reseti;
 			j=resetj;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green&&counter>0)
 			{
 				if (board[i][j]==turn)
 				{
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
 		if(i<7&&j<7&&board[i+1][j+1]!=blank&&board[i+1][j+1]!=green)
 		{
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				if (board[i][j]==turn)
 					counter++;
 				j++;
 				i++;
 			}
 			i=reseti;
 			j=resetj;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green&&counter>0)
 			{
 				if (board[i][j]==turn)
 				{
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
 		if(i>0&&j>0&&board[i-1][j-1]!=blank&&board[i-1][j-1]!=green)
 		{
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				if (board[i][j]==turn)
 					counter++;
 				j--;
 				i--;
 			}
 			i=reseti;
 			j=resetj;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green&&counter>0)
 			{
 				if (board[i][j]==turn)
 				{
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
 		if(i>0&&j<7&&board[i-1][j+1]!=blank&&board[i-1][j+1]!=green)
 		{
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				if (board[i][j]==turn)
 					counter++;
 				j++;
 				i--;
 			}
 			i=reseti;
 			j=resetj;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green&&counter>0)
 			{
 				if (board[i][j]==turn)
 				{
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
 	
 	public void setMoves(int i,int j) {
 		//left
 		int resetj=j;
 		int reseti=i;
 		if(j>0&&board[i][j-1]==otherturn)
 		{
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				j--;
 			}
 			if(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]==blank)
 			board[i][j]=green;
 			i=reseti;
 			j=resetj;
 		}
 		
 		//right
 		if(j<7&&board[i][j+1]==otherturn)
 		{
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				j++;
 			}
 			if(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]==blank)
 			board[i][j]=green;
 			i=reseti;
 			j=resetj;
 		}
 		//up
 		if(i>0&&board[i-1][j]==otherturn)
 		{
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				i--;
 			}
 			if(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]==blank)
 			board[i][j]=green;
 			i=reseti;
 			j=resetj;
 		}
 		//down
 		if(i<7&&board[i+1][j]==otherturn)
 		{
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				i++;
 			}
 			if(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]==blank)
 			board[i][j]=green;
 			i=reseti;
 			j=resetj;
 		}	
 		//up left
 		if(i<7&&j>0&&board[i+1][j-1]==otherturn)
 		{
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				j--;
 				i++;
 			}
 			if(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]==blank)
 			board[i][j]=green;
 			i=reseti;
 			j=resetj;
 		}
 		
 		//up right
 		if(i<7&&j<7&&board[i+1][j+1]==otherturn)
 		{
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				j++;
 				i++;
 			}
 			if(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]==blank)
 			board[i][j]=green;
 			i=reseti;
 			j=resetj;
 		}
 		//down left
 		if(i>0&&j>0&&board[i-1][j-1]==otherturn)
 		{
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				j--;
 				i--;
 			}
 			if(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]==blank)
 			board[i][j]=green;
 			i=reseti;
 			j=resetj;
 		}
 		//down right
 		if(i>0&&j<7&&board[i-1][j+1]==otherturn)
 		{
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				j++;
 				i--;
 			}
 			if(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]==blank)
 			board[i][j]=green;
 			i=reseti;
 			j=resetj;
 		}	
     }
 	public int getWeight(int i, int j)
 	{
 	//left
 		int resetj=j;
 		int reseti=i;
 		int counter = 0;
 		if(j>0&&board[i][j-1]!=blank&&board[i][j-1]!=green)
 		{
 			j--;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				if (board[i][j]!=turn)
 					counter++;
 				j--;
 			}
 			i=reseti;
 			j=resetj;
 			
 		}
 		
 		//right
 		if(j<7&&board[i][j+1]!=blank&&board[i][j+1]!=green)
 		{
 		j++;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				if (board[i][j]!=turn)
 					counter++;
 				j++;
 			}
 			i=reseti;
 			j=resetj;
 			
 		}
 		//up
 		if(i>0&&board[i-1][j]!=blank&&board[i-1][j]!=green)
 		{
 		i--;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				if (board[i][j]!=turn)
 					counter++;
 				i--;
 			}
 			i=reseti;
 			j=resetj;
 			
 		}
 		//down
 		if(i<7&&board[i+1][j]!=blank&&board[i+1][j]!=green)
 		{
 		i++;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				if (board[i][j]!=turn)
 					counter++;
 				i++;
 			}
 			i=reseti;
 			j=resetj;
 			
 		}	
 		//up left
 		if(i<7&&j>0&&board[i+1][j-1]!=blank&&board[i+1][j-1]!=green)
 		{
 		j--;
 		i++;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				if (board[i][j]!=turn)
 					counter++;
 				j--;
 				i++;
 			}
 			i=reseti;
 			j=resetj;
 			
 		}
 		
 		//up right
 		if(i<7&&j<7&&board[i+1][j+1]!=blank&&board[i+1][j+1]!=green)
 		{
 		j++;
 		i++;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				if (board[i][j]!=turn)
 					counter++;
 				j++;
 				i++;
 			}
 			i=reseti;
 			j=resetj;
 			
 		}
 		//down left
 		if(i>0&&j>0&&board[i-1][j-1]!=blank&&board[i-1][j-1]!=green)
 		{
 		j--;
 		i--;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				if (board[i][j]!=turn)
 					counter++;
 				j--;
 				i--;
 			}
 			i=reseti;
 			j=resetj;
 			
 		}
 		//down right
 		if(i>0&&j<7&&board[i-1][j+1]!=blank&&board[i-1][j+1]!=green)
 		{
 		j++;
 		i--;
 			while(j>=0&&j<=7&&i>=0&&i<=7&&board[i][j]!=blank && board[i][j]!=green)
 			{
 				if (board[i][j]!=turn)
 					counter++;
 				j++;
 				i--;
 			}
 			i=reseti;
 			j=resetj;
 			
 		}
 	if ((turn==black&&blackdifficulty==medium)||(turn==white&&whitedifficulty==medium))
 	{
 	if (isCorner(i,j))
 	counter+=10;
     if (isSubCorner(i,j))
 	counter-=3;
 	}
 	if ((turn==black&&blackdifficulty==hard)||(turn==white&&whitedifficulty==hard))
 	{
 	counter+=applyHard(i,j);
 	}
 	
 	//System.out.println("weight of "+i+" "+j+" = " + counter);
 	return counter;
 	}
 	
 	public int applyHard(int i, int j)
 	{
 		return weights[i][j];
 	}
 	public boolean isCorner(int i, int j)
 	{
 		if((i==0&&j==0)||(i==0&&j==7)||(i==7&&j==0)||(i==7&&j==7))
 		return true;
 		return false;
 	}
 	
 	public boolean isSubCorner(int i, int j)
 	{
 		if((i==1&&j==0)||(i==1&&j==7)||(i==7&&j==1)||(i==7&&j==6)||(i==1&&j==1)||(i==1&&j==6)||(i==6&&j==0)||(i==6&&j==7)||(i==0&&j==1)||(i==0&&j==6)||(i==6&&j==1)||(i==6&&j==6))
 		return true;
 		return false;
 	}
 	
 	
 	
     	public void fillundoboards()
 		{
 		for(int i=0;i<10;i++)
 			for(int j=0;j<7;j++)
 				for(int k=0;k<7;k++)
 				{
 				undoboards[i][j][k]=board[j][k];
 				}
 		}
     public void delay(double n)
 	{
 		long startDelay = System.currentTimeMillis();
 		long endDelay = 0;
 		while (endDelay - startDelay < n)
 			endDelay = System.currentTimeMillis();	
 	}
 
 }	

 package checkers;
 import java.awt.*;
 import javax.swing.*;
 
 import java.awt.event.*;
 import java.io.*;
 import java.util.LinkedList;
 
 public class GameScreen implements MouseListener, ActionListener
 {
 	JPanel panel = new JPanel();
 	JLabel playerturn = new JLabel("");
 	JButton concede = new JButton("Concede?");
 	JButton draw = new JButton("Offer draw");	
 	Container buttoncontain = new Container();
     JLayeredPane layer = new JLayeredPane();
     JPanel board = new JPanel();
     int size = 0;
     Game game;
     JButton saveGameButton = new JButton("Save Game");
     JButton endMoveButton = new JButton("End King Move");
     int movedX,movedY,movedZ;
     JButton instantReplay = new JButton("Instant Replay");
     //LinkedList instantReplayList = new LinkedList();
     int movesPlayed = 0;
     
     boolean replaymode = false;
     int replayMoves;
     
     int a,b,c,boardNumber,columns,rows;
     boolean selected = false;
     //String playerOne = Game.getUserName(1);
     //String playerTwo = Game.getUserName(2);
     
     boolean warped = false;
 	boolean moved = false;
    boolean kingJumped = false;
     
     ImageIcon whitePiece = new ImageIcon("pieces/whitepiece.png");
     ImageIcon whiteKing = new ImageIcon("pieces/whiteking.png");
     ImageIcon whiteBlock = new ImageIcon("pieces/whiteblock.png"); 
     ImageIcon blackPiece = new ImageIcon("pieces/blackpiece.png"); 
     ImageIcon blackKing = new ImageIcon("pieces/blackking.png"); 
     ImageIcon blackBlock = new ImageIcon("pieces/blackblock.png"); 
     
     Game gameStore;
     
 	GameScreen(int bsize, Game gameparam)
 	{
         game = gameparam;
         if(game.playerTurn == 1)
         	playerturn.setText(game.playerOneName + " move");
         if(game.playerTurn == 2)
         	playerturn.setText(game.playerTwoName + " move");
         size = bsize;
 		buttoncontain.setLayout(new GridLayout(1,0));
 		buttoncontain.add(concede);
 		buttoncontain.add(draw);
         buttoncontain.add(saveGameButton);
         buttoncontain.add(endMoveButton);
         buttoncontain.add(instantReplay);
 
         saveGameButton.addActionListener(this);
         endMoveButton.addActionListener(this);
         draw.addActionListener(this);
         concede.addActionListener(this);
         instantReplay.addActionListener(this);
 
 		panel.setLayout(new BorderLayout());
 		panel.add(playerturn,BorderLayout.PAGE_START);	
 		panel.add(buttoncontain,BorderLayout.PAGE_END);
         panel.add(board,BorderLayout.CENTER);
         board.setLayout(new GridLayout(size, 2 * size));
 
         Dimension boarddim = new Dimension(400,200);
         board.setPreferredSize(boarddim);
         board.setBounds(0, 0, boarddim.width, boarddim.height);
         setupBoard(size);
         
         board.addMouseListener(this);
 
 
 
 	}
 
 	public void setupBoard(int boardsize)
     {
         board.removeAll();
         int x = 0;
         int y = boardsize - 1;
         int z = 0;
         for (int i = 0; i < (boardsize * 2 * boardsize); i++)
         {
         	int squareData = game.board[z][x][y];
             Square place = new Square( new BorderLayout() );
             place.setCoord(x, y, z);
 
             if ( x % 2 == 0)
             {
                 if(y % 2 == 0)
                     place.setBackground(Color.RED);
                 else
                     place.setBackground(Color.BLACK);
             }
             else
             {
                  if(y % 2 == 0)
                     place.setBackground(Color.BLACK);
                 else
                     place.setBackground(Color.RED);
             }
             if (game.isSafe(z, x, y) || game.board[z][x][y] == 20 || game.board[z][x][y] == 21 || game.board[z][x][y] == 24 || game.board[z][x][y] == 25 )
             {
             	place.setBackground(Color.ORANGE);
             }
             if (selected && a == x && b == y && z == c)
             	place.setBackground(Color.BLUE);
             if (squareData != 0 && squareData != 4 && squareData != 12)
             {
                 ImageIcon iconic = new ImageIcon();
                 if (game.isPlayerOneKing(z, x, y))
                 	iconic = whiteKing;
                 if(game.isPlayerOnePiece(z, x, y))
                 	iconic = whitePiece;
                 if(game.isPlayerTwoKing(z, x, y))
                 	iconic = blackKing;
                 if(game.isPlayerTwoPiece(z, x, y))
                 	iconic = blackPiece;
                 if(squareData == 3)
                 	iconic = whiteBlock;
                 if(squareData == 11)
                 	iconic = blackBlock;
                 JLabel squareLabel = new JLabel();
                 squareLabel.setIcon(iconic);
             	place.add(squareLabel);
             }	
             else 
             {
                 place.removeAll();
             }
             board.add(place);
             x ++;
             if ( x == boardsize)
             {
             	if (z == 0)
             	{
             		z = 1;
             		x = 0;
             		board.add(new JPanel());
             	}
             	else
             	{
             		 y--;
                      x = 0;
                      z = 0;
             	}
             }
 
         }
         board.updateUI();
     }
 
         public void actionPerformed(ActionEvent e)
     {
         if (e.getSource() == saveGameButton)
         {
             try
             {
               System.out.println("game being written");
               ObjectOutputStream gamesaver = new ObjectOutputStream(new FileOutputStream("savedgames/"+ game.playerOneName + "vs" + game.playerTwoName));
               gamesaver.writeObject(game);
               gamesaver.close();
               System.exit(0);
             }
             catch(IOException except)
             {
                 System.out.println("error writing file");
             }
         }
         if (e.getSource() == endMoveButton)
         {
         	if((warped || moved) && !(game.jumpPerformed(c, a, b, movedZ, movedX, movedY) && game.hasJumpsRemaining(movedZ, movedX, movedY)) )
         	{
         		warped = true;
         		moved = true;
                 try{ 
                 	trackTurn(a,b,c,movedX,movedY,movedZ);
                 }
                 catch(Exception except)
                 {
                	 System.out.println("error");
                 }
         		
         	}
         }
         if (e.getSource() == concede)
         {
         	if(game.playerTurn  == 1)
         	{
         		game.playerOnePieceCount = 0;
         		try{
             		
                 	
                 	trackTurn(0,0,0,0,0,0);
                 	}
                 	catch(Exception excp)
                 	{
                 		System.out.println("error writing stats file");
                 	}
         	}
         	else 
         	{
         		game.playerTwoPieceCount = 0;
         		try{
             		
                 	
                 	trackTurn(0,0,0,0,0,0);
                 	}
                 	catch(Exception excp)
                 	{
                 		System.out.println("error writing stats file");
                 	}
         	}
         }
         if (e.getSource() == draw)
         {
       
         	int draw = JOptionPane.showConfirmDialog(null, "Accept draw?", "Draw", JOptionPane.YES_NO_OPTION);
             if (draw == JOptionPane.YES_OPTION)
             {
             	game.playerOnePieceCount = 0;
             	game.playerTwoPieceCount = 0;
             	try{
             		
             	
             	trackTurn(0,0,0,0,0,0);
             	}
             	catch(Exception excp)
             	{
             		System.out.println("error writing stats file");
             	}
             }
 
         
 
         }
         if (e.getSource() == instantReplay)
         {
         	if(!replaymode)
         	{
         		String replaymovestring = JOptionPane.showInputDialog(null, "How many moves do you want to replay?", "Instant Replay");
         		replayMoves = Integer.parseInt(replaymovestring);            
         		if(movesPlayed >= replayMoves)
             	{
         			try
                     {
                       System.out.println("game being read");
                       String gameStatePath = "gamestates/" + (movesPlayed - replayMoves);
                       ObjectInputStream gameloader = new ObjectInputStream(new FileInputStream(gameStatePath));
                       game = (Game)gameloader.readObject();
                       gameloader.close();
                       setupBoard(size);
                       panel.updateUI();
                       
                       
                     }
                     catch(Exception except)
                     {
                         System.out.println("error reading gamestate");
                     }
             		/*game = (Game)instantReplayList.get(instantReplayList.size() - replayMoves);
         			gameStore = game;
         			//game = (Game)instantReplayList.get(4);
         			setupBoard(size);
             		panel.updateUI();
             		*/
             		replaymode = true;
             		instantReplay.setText("Next Move");
             		
             	}
         	}
         	else
         	{
         		replayMoves --;
         		if(replayMoves != 0)
         		{
         			try
                     {
                       System.out.println("game being read");
                       String gameStatePath = "gamestates/" + (movesPlayed - replayMoves);
                       ObjectInputStream gameloader = new ObjectInputStream(new FileInputStream(gameStatePath));
                       game = (Game)gameloader.readObject();
                       gameloader.close();
                       
                       
                     }
                     catch(Exception except)
                     {
                         System.out.println("error reading gamestate");
                     }
         			/*//game = (Game)instantReplayList.get(4);
         			game = (Game)instantReplayList.get(instantReplayList.size() - replayMoves);
         			//game = new Game(new int[10][10][10],8,1,8,"","");
         			if(game.board == gameStore.board)
         				System.out.println("Should be working");
         				*/
         			setupBoard(size);
         			panel.updateUI();
         		}
         		else
         		{
         			replaymode = false;
         			instantReplay.setText("Instant Replay");
         			//game = gameStore;
         		}
         		
         	}
             	
         	
         	
         }
     }
 
     public void squareClicked(int x, int y, int z)
     {
         if(!selected && game.isMoveable(z, x, y))
         {
           selected = true;
           a = x;
           b = y;
           c = z;
    
         }
         else
         {
             selected = false;
             tryMove(a,b,c,x,y,z);
         }
 
     }
  public void mousePressed(MouseEvent e)
     {
 
     }
 
     public void mouseDragged(MouseEvent me)
     {
 
      }
 
 
     public void mouseReleased(MouseEvent e)
     {
 
     }
 
     public void mouseClicked(MouseEvent e)
     {
         Square jpan = (Square)board.getComponentAt(e.getX(), e.getY());
         squareClicked(jpan.x,jpan.y,jpan.z);
         System.out.println("x is " + jpan.x);
         System.out.println("y is " + jpan.y);
         System.out.println("z is " + jpan.z);
         setupBoard(size);
 
     }
     public void mouseMoved(MouseEvent e) {}
     public void mouseEntered(MouseEvent e) {}
     public void mouseExited(MouseEvent e) {}
 
 
 
     public void tryMove(int a, int b, int c, int x, int y, int z)
     {
         System.out.println("Validate move says: " + game.validateMove(c,a,b,z,x,y));
         System.out.println("CheckJumps() says: " + game.checkJumps());
          if(game.validateMove(c,a,b,z,x,y) == true)
          {	
        	 if(!(warped && c != z) && !(kingJumped && Math.abs(a-x) == 1) && !((moved && !kingJumped)&& Math.abs(a-x) >= 1) )
         	 {
              game.move(c,a,b,z,x,y);// make move if move is valid
              try{ 
             	 trackTurn(a,b,c,x,y,z);
              }
              catch(Exception e)
              {
             	 System.out.println("error writing stats");
              }
              movedX = x;
              movedY = y;
              movedZ = z;
         	 }
              setupBoard(size);
            
              try
              {
                System.out.println("gamestate being written");
                String gameStatePath = "gamestates/" + movesPlayed;
                ObjectOutputStream replaysaver = new ObjectOutputStream(new FileOutputStream(gameStatePath));
                replaysaver.writeObject(game);
                replaysaver.close();
                movesPlayed ++;
              }
              catch(IOException except)
              {
                  System.out.println("error writing gamestate");
              }
              System.out.println(game.board[z][x][y]);
          }
          else
          {
              System.out.println("Invalid move");
          }
     }
 
         public void trackTurn(int a, int b, int c, int x, int y, int z) throws IOException
         {
 
        	if(game.isKing(z, x, y))  
         	{
                System.out.println("is king");
         		if( z != c)
         			warped = true;
         		if( a != x)
         			moved = true;
                if(Math.abs(a-x) == 2)
                    kingJumped = true;
         		if (moved && warped && !(Math.abs((a-x)) >= 2))  //moved and warped, current move is not jump
         		{
         			if (game.playerTurn == 1)
     					game.playerTurn = 2;
     				else
     					game.playerTurn = 1;
         			moved = false;
         			warped = false;
                    kingJumped = false;
         		}
         		else if (moved && Math.abs((a-x)) >= 2 && warped)    //moved and warped, and just jumped
         		{
         			if(!game.hasJumpsRemaining(z, x, y))
         			{
         				if (game.playerTurn == 1)
         					game.playerTurn = 2;
         				else
         					game.playerTurn = 1;
         				moved = false;
             			warped = false;
                        kingJumped = false;
         			}
         		}
         		
     
         	}
         	else
         	{
         		if(Math.abs((a-x)) >= 2 ) //if jumped
         		{
         
         			if(!game.hasJumpsRemaining(z, x, y))
         			{
         				if (game.playerTurn == 1)
         					game.playerTurn = 2;
         				else
         					game.playerTurn = 1;
         			}
         		}
         		else
         		{
         			if (game.playerTurn == 1)
         				game.playerTurn = 2;
         			else
         				game.playerTurn = 1;
         		}
         		
         	}
         	
         	
         	if(game.playerTurn == 1)
         		playerturn.setText(game.playerOneName + " move");
         	else
         		playerturn.setText(game.playerTwoName + " move");
             //User_Saver one = new User_Saver(playerOne);
             //User_Saver two = new User_Saver(playerTwo);
             String one = "users/" + game.playerOneName + ".txt";
             String two = "users/" + game.playerTwoName + ".txt";
             FileOutputStream fileOutOne = new FileOutputStream(one,true); 
             FileOutputStream fileOutTwo = new FileOutputStream(two,true);
             DataOutputStream outOne   = new DataOutputStream(fileOutOne); 
             DataOutputStream outTwo   = new DataOutputStream(fileOutTwo); 
             //PrintWriter outOne = new PrintWriter(new BufferedWriter(new FileWriter(one)));
             //PrintWriter outTwo = new PrintWriter(new BufferedWriter(new FileWriter(two)));
    
 
             int playerwin = game.gameOver();
             String results = "";
             if (playerwin != 0)
             {
                 //String win = "W";String lose = "L";String draw = "D";
                 playerturn.setText("Player" + playerwin + " wins");
                 if (playerwin == 3)
                 {
                     playerturn.setText("Tie Game");
                     outOne.writeBytes(game.playerTwoName + "\n");
                     outOne.writeBytes("D\n");
                     outTwo.writeBytes(game.playerOneName + "\n");
                     outTwo.writeBytes("D\n");
                     results = "The game was a draw.";
                 }
                 if (playerwin == 1)
                 {
                    outOne.writeBytes(game.playerTwoName + "\n");
                    outOne.writeBytes("W\n");
                    outTwo.writeBytes(game.playerOneName + "\n");
                    outTwo.writeBytes("L\n");
                    results = game.playerOneName + " wins.";
                 }
                 if (playerwin == 2)
                 {
                    outOne.writeBytes(game.playerTwoName + "\n");
                    outOne.writeBytes("L\n");
                    outTwo.writeBytes(game.playerOneName + "\n");
                    outTwo.writeBytes("W\n");
                    results = game.playerTwoName + " wins.";
                 }
                 for(int i = 0; i < movesPlayed; i++)
                 {
                 	String gameStatePath = "gamestates/" + i;
                 	File toDelete = new File(gameStatePath);
                 	if(toDelete.exists())
                 		toDelete.delete();
                 }
 
             JOptionPane.showMessageDialog(null, results);
 
                 System.exit(0);
             }
             outOne.close();
             outTwo.close();
 
         }
 
 
 }

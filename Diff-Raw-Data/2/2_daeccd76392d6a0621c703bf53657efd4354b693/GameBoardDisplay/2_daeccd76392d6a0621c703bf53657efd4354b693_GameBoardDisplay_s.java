 //BEGIN FILE GameBoardDisplay.java
 package UI;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 
 import model.*;
 
 /**
  * The UI for a game session
  * @author Mustache Cash Stash
  * @version 0.9
  */
 //BEGIN CLASS GameBoardDisplay
 public class GameBoardDisplay extends JPanel implements ActionListener
 {
 	public  static String initialUser1, initialUser2, initialModeName;
 	
 	/**
 	 * Builds and then draws the UI for a session of TTT
 	 * @param user1 The name of player 1
 	 * @param user2 The name of player 2
 	 * @param modeName The mode to display on the window.
 	 */
 	//BEGIN CONSTRUCTOR public GameBoardDisplay(String user1, String user2, String modeName)
     public GameBoardDisplay(String user1, String user2, String modeName)
     {
     	// keep track of the initial usernames and game mode in case if we to restart the game
     	initialUser1 = user1;
     	initialUser2 = user2;
     	initialModeName = modeName;
     	
     	boardModel = new GameboardImp();
     	setLayout(new BorderLayout());
     	
        // This is the window that will be shown
     	boardFrame = constructFrame(modeName);
         
         // get players
         String[] players = Game.getPlayers(user1, user2);
         player1 = players[0];
         player2 = players[1];
         
         // create a header that holds the image
         JPanel header = new JPanel();
         header.add(new JLabel(new ImageIcon("bg.png")));
         header.setBackground(Color.BLACK);
         
 
         boardPanel = constructBoardPannel();
         cells = constructCells();
         boardPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
         
	 	// create 3 buttons
 	 	JButton button1 = new JButton("Restart Game");
 	 	JButton button2 = new JButton("Quit Game");
 	 	
 	 	// set action command to buttons
 	 	button1.setActionCommand("restart");
 	 	button2.setActionCommand("quit");
 	 	
 	 	// set action listeners to buttons
 	 	button1.addActionListener(this);
 	 	button2.addActionListener(this);
 	 	
 	 	
         // add the board label to the lower side of the window   
         buttonsPanel = new JPanel();
         buttonsPanel.setLayout(new GridLayout(7,1));
         buttonsPanel.setBackground(Color.BLACK);
         buttonsPanel.setPreferredSize(new Dimension(300, 100));
         buttonsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
         
         gameStatus = new JLabel(player1 + "'s Turn");
         gameStatus.setFont(new Font("Serif", Font.BOLD, 18));  
         gameStatus.setForeground(Color.WHITE);
         gameStatus.setBorder(new EmptyBorder(10, 10, 10, 10));
         
         // add a label to the top of the window
         JLabel title = new JLabel(player1 + " vs " + player2);
         title.setFont(new Font("Serif", Font.BOLD, 28));  
         title.setForeground(Color.ORANGE);
         
         buttonsPanel.add(title);
         buttonsPanel.add(gameStatus);
         buttonsPanel.add(new JLabel());
         buttonsPanel.add(new JLabel());
         buttonsPanel.add(new JLabel());
         buttonsPanel.add(button1);
         buttonsPanel.add(button2);
        
         moveStatus = new JLabel("Ready to play!");
         moveStatus.setFont(new Font("Serif", Font.BOLD, 18));  
         moveStatus.setForeground(Color.CYAN);
         moveStatus.setBorder(new EmptyBorder(10, 10, 10, 10));
         
         // add all the parts to the window
         add(header, BorderLayout.NORTH); 
         add(boardPanel, BorderLayout.CENTER);  
         add(buttonsPanel, BorderLayout.EAST);
         add(moveStatus, BorderLayout.SOUTH); 
         
         boardFrame.setVisible(true);
         setBackground(Color.BLACK);
     }
     //END CONSTRUCTOR public GameBoardDisplay(String user1, String user2, String modeName)
 	
     
     
 	/**
 	 * actionPerformed
 	 * 
 	 * Listens to actions and act accordingly
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	public void actionPerformed(ActionEvent event)
 	{
 		// get the action of the button
 		String command = event.getActionCommand();
 		
 		// if the restart game button is clicked
 		if(command.equalsIgnoreCase("restart"))
 		{
 			new GameBoardDisplay(initialUser1, initialUser2, initialModeName);
 			boardFrame.dispose();
 		}
 		
 		// if the quit game button is clicked
 		else
 		{
 			boardFrame.dispose();
 		}
 	}
 	
 	
 	
 	
 	/**
 	 * A request from a client to place a piece on the Gameboard at a certain position.
 	 * Passes this request to the model
 	 * @param xPosition The x (horizontal) coordinate of the requested space to occupy
 	 * @param yPosition The y (vertical) coordinate of the requested space to occupy
 	 * @return Whether the move request was successful in updating the Gameboard.
 	 */
 	//BEGIN METHOD public boolean attemptMove(int xPosition, int yPosition)
 	public boolean attemptMove(int xPosition, int yPosition)
 	{
 		boolean result = false;
 		if(boardModel.getResult() == GameResult.PENDING)
 		{
     		if(boardModel.xsTurn())
     		{
     			result = boardModel.requestMove(xPosition, yPosition, PlaceValue.X);
     			if(result)
     			{
         			displayNewPiece(xPosition, yPosition, xPiece);
     			}
     		}
     		else if(boardModel.osTurn())
     		{
     			result = boardModel.requestMove(xPosition, yPosition, PlaceValue.O);
     			if(result)
     			{
         			displayNewPiece(xPosition, yPosition, oPiece);
     			}
     		}
     	}
 		GameResult status = boardModel.getResult();
 		if (status == GameResult.PENDING)
 		{
 			if(boardModel.xsTurn())
 			{
 				displayNewGameStatus(player1 + "'s Turn");
 			}
 			else 
 			{
 				displayNewGameStatus(player2 + "'s Turn");
 			}
 		}
 		else if (status == GameResult.CAT) 
 		{
 			displayNewGameStatus("Cat's game!");
 		}
 		else if (status == GameResult.OWIN)
 		{
 			displayNewGameStatus(player2 + " Wins!");
 		}
 		else if (status == GameResult.XWIN) 
 		{
 			displayNewGameStatus(player1 + " Wins!");
 		}
 		return result;
 	}
 	//END METHOD public boolean attemptMove(int xPosition, int yPosition)
 	
 	static final long serialVersionUID = 0L; // to shut up Eclipse
 	
 	/**
 	 * Draws the selected Icon at the selected location on the board.
 	 * Called whenever a new piece is placed
 	 * @param xPosition The x (horizontal) coordinate of the requested space to occupy
 	 * @param yPosition The y (vertical) coordinate of the requested space to occupy
 	 * @param piece The Icon to draw at the selected space
 	 */
 	//BEGIN METHOD private void displayNewPiece(int xPosition, int yPosition, Icon piece) 
 	private void displayNewPiece(int xPosition, int yPosition, Icon piece) 
 	{
 		cells[xPosition][yPosition].setIcon(piece);
 		boardPanel.revalidate();
 	}
 	//END METHOD private void displayNewPiece(int xPosition, int yPosition, Icon piece) 
 	
     /**
      * Draw the selected message at the bottom of the board
      * This space is used to display the status of a move.
      * Called whenever a successful move occurs
      * @param message the new move status message to display.
      */
 	//BEGIN METHOD private void displayNewMoveStatus(String message)
 	private void displayNewMoveStatus(String message)
 	{
 		moveStatus.setText(message);
 		boardPanel.revalidate();
 	}
 	//END METHOD private void displayNewMoveStatus(String message)
 	
 	/**
 	 * Draw the selected message on the side of the board.
 	 * This space is used to display the status of the game.
 	 * Called whenever a successful move occurs.
 	 * @param message the new game status message to display
 	 */
 	//BEGIN METHOD private void displayNewGameStatus(String message)
 	private void displayNewGameStatus(String message)
 	{
 		gameStatus.setText(message);
 		boardPanel.revalidate();
 	}
 	//END METHOD private void displayNewGameStatus(String message)
 	
     /**
      * Construct the JPanel containing the cells
      * @return The panel constructed
      */
     //BEGIN METHOD private JPanel constructBoardPannel() 
 	private JPanel constructBoardPannel() 
 	{
         JPanel panel = new JPanel();
         panel.setLayout(new GridLayout(3,3));
         panel.setBackground(Color.BLACK);
         panel.setPreferredSize(new Dimension(300, 300));
         return panel;
 	}
 	//END METHOD private JPanel constructBoardPannel() 
 
 	/**
 	 * Construct the content JFrame for the window
 	 * @param modeName The mode (single/multiplayer) used as part of the window title
 	 * @return The frame constructed
 	 */
     //BEGIN METHOD	private JFrame constructFrame(String modeName) 
 	private JFrame constructFrame(String modeName) 
 	{
 		
 		JFrame frame = new JFrame ("Tic Tac Toe - " + modeName + " Mode");
         
         // set the size of the window
         frame.setSize(700, 600);
         
 		// Place the window in the middle of the screen
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         Dimension gameFrame2Size = frame.getSize();
         if (gameFrame2Size.height > screenSize.height) 
         {
             gameFrame2Size.height = screenSize.height;
         }
         if (gameFrame2Size.width > screenSize.width) 
         {
             gameFrame2Size.width = screenSize.width;
         }
         
         frame.setLocation((screenSize.width - gameFrame2Size.width) / 2,
                           (screenSize.height - gameFrame2Size.height) / 2);         
         
         // set close operation
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         // set window not resizable
         frame.setResizable(true);    
         
         frame.setIconImage(new ImageIcon("mord.png").getImage()); //If we want favicon for our window.
         
         // show the window
         frame.setContentPane(this);
         
         return frame;
 	}
 	//END METHOD private JFrame constructFrame(String modeName) 
 
 	/**
 	 * Constructs the JLabel cells that represent individual board spaces in the UI
 	 * @return the array of game spaces
 	 */
 	//BEGIN METHOD private JLabel[][] constructCells() 
 	private JLabel[][] constructCells() 
 	{
 		
 		JLabel[][] cellArray = new JLabel[3][3];
 		
 		Border whiteLine = BorderFactory.createLineBorder(Color.WHITE);
         //create cells
         JLabel cell_00 = new JLabel ();
         cell_00.setBorder(whiteLine);
         cellArray[0][0] = cell_00;
         JLabel cell_01 = new JLabel ();
         cell_01.setBorder(whiteLine);
         cellArray[0][1] = cell_01;
         JLabel cell_02 = new JLabel ();
         cell_02.setBorder(whiteLine);
         cellArray[0][2] = cell_02;
         JLabel cell_10 = new JLabel ();
         cell_10.setBorder(whiteLine);
         cellArray[1][0] = cell_10;
         JLabel cell_11 = new JLabel ();
         cell_11.setBorder(whiteLine);
         cellArray[1][1] = cell_11;
         JLabel cell_12 = new JLabel ();
         cell_12.setBorder(whiteLine);
         cellArray[1][2] = cell_12;
         JLabel cell_20 = new JLabel ();
         cell_20.setBorder(whiteLine);
         cellArray[2][0] = cell_20;
         JLabel cell_21 = new JLabel ();
         cell_21.setBorder(whiteLine);
         cellArray[2][1] = cell_21;
         JLabel cell_22 = new JLabel ();
         cell_22.setBorder(whiteLine);
         cellArray[2][2] = cell_22;
         
         // create a listener for all cells
         MouseListener listener00 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(0,0))displayNewMoveStatus("Good move!"); else displayNewMoveStatus("Can't place a piece there!");}};
         MouseListener listener01 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(0,1))displayNewMoveStatus("Good move!"); else displayNewMoveStatus("Can't place a piece there!");}};
         MouseListener listener02 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(0,2))displayNewMoveStatus("Good move!"); else displayNewMoveStatus("Can't place a piece there!");}};
         MouseListener listener10 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(1,0))displayNewMoveStatus("Good move!"); else displayNewMoveStatus("Can't place a piece there!");}};
         MouseListener listener11 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(1,1))displayNewMoveStatus("Good move!"); else displayNewMoveStatus("Can't place a piece there!");}};
         MouseListener listener12 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(1,2))displayNewMoveStatus("Good move!"); else displayNewMoveStatus("Can't place a piece there!");}};
         MouseListener listener20 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(2,0))displayNewMoveStatus("Good move!"); else displayNewMoveStatus("Can't place a piece there!");}};
         MouseListener listener21 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(2,1))displayNewMoveStatus("Good move!"); else displayNewMoveStatus("Can't place a piece there!");}};
         MouseListener listener22 = new MouseAdapter(){public void mouseClicked(MouseEvent event){if(attemptMove(2,2))displayNewMoveStatus("Good move!"); else displayNewMoveStatus("Can't place a piece there!");}};
         
         // link listeners to cells
         cell_00.addMouseListener(listener00);
         cell_01.addMouseListener(listener01);
         cell_02.addMouseListener(listener02);
         cell_10.addMouseListener(listener10);
         cell_11.addMouseListener(listener11);
         cell_12.addMouseListener(listener12);
         cell_20.addMouseListener(listener20);
         cell_21.addMouseListener(listener21);
         cell_22.addMouseListener(listener22);        
         
         // add cells to the board
         boardPanel.add(cell_00);
         boardPanel.add(cell_01);
         boardPanel.add(cell_02);
         boardPanel.add(cell_10);
         boardPanel.add(cell_11);
         boardPanel.add(cell_12);
         boardPanel.add(cell_20);
         boardPanel.add(cell_21);
         boardPanel.add(cell_22);
         
         return cellArray;
 	}
 	//END METHOD private JLabel[][] constructCells() 
 	
 	private static Icon xPiece = new ImageIcon("X.png");
 	private static Icon oPiece = new ImageIcon("O.png");
 	private Gameboard boardModel;
 	private JLabel[][] cells;
 	private JPanel boardPanel;
 	private JFrame boardFrame;
 	private JLabel moveStatus;
 	private JPanel buttonsPanel;
 	private JLabel gameStatus;
 	private String player1;
 	private String player2;
     
 }
 //END CLASS GameBoardDisplay
 //END FILE GameBoardDisplay.java

 /**
  *	@author	Aaron Levy, Jenny Liu
  *	@date		4/5/2013
  *	Breakthrough Mini Project
  *	
  *	Use GUI components to create a board game called Breakthrough. Made for two players
  **/
 
 import java.util.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.Color;
 import java.io.*;
 import java.applet.*;
 import javax.sound.sampled.*;
 
 public class Breakthrough extends JFrame implements ActionListener {
 
 	/**
 	 * 
 	 */
 	//needed for Java compatibility
 	private static final long serialVersionUID = 1L;
 	//Class Attributes
 	JButton [][] buttons = new JButton[8][8]; 
 	JButton song1, song2;
 	int counter, p1, p2, playerTurn, usableP1, usableP2;
 	String buttonPressed, buttonValue;
 	JPanel north, center, south, south1, south2, north1, north2;
 	JLabel player, p1Score, p2Score;
 	JMenuBar menuBar;
 	JMenu file, help;
 	JMenuItem newGame, rules;
 	AudioClip ashKetch, pika, winner, bump;
 	Clip redBattle, elite4;
 	ImageIcon stop, play;
 
 	/**
 	 *	Contructor for Breakthrough.
 	 **/
 	public Breakthrough() { 
 		initialValues();
 
 		importMedia();	
 
 		//JFrame settings
 		setTitle("Best Game Ever");
 		setVisible(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		pack();
 		setResizable(false); 
 		setLocationRelativeTo(null);		
 	}
 
 	/**
 	 *	Create the GUI and initialize values.
 	 **/
 	public void initialValues() {
 		//Initialize some attributes
 		counter = 0;
 		p1 = 0;
 		p2 = 0; 
 		usableP1 = 16;
 		usableP2 = 16;
 		playerTurn = 1; 
 
 		//Create GUI
 		northPanel();
 		centerPanel();
 		southPanel();
 		createMenu();					
 	}
 
 	/**
 	 * Import media files
 	 **/
 	public void importMedia() {		
 		try {
 			//Use AudioInputStream to retrieve sound file for redBattle and Elite4 in order to adjust volume
 			AudioInputStream ais = AudioSystem.getAudioInputStream(new File("redBattle.wav"));
 			redBattle = AudioSystem.getClip();
 			redBattle.open(ais);
 
 			FloatControl redBattleVolume = (FloatControl) redBattle.getControl(FloatControl.Type.MASTER_GAIN);
 			redBattleVolume.setValue(-15.0f); //Reduce volume
 
 			AudioInputStream ais2 = AudioSystem.getAudioInputStream(new File("Elite4.wav"));
 			elite4 = AudioSystem.getClip();
 			elite4.open(ais2);
 
 			FloatControl elite4Volume = (FloatControl) elite4.getControl(FloatControl.Type.MASTER_GAIN);
 			elite4Volume.setValue(-15.0f); //Reduce volume
 
 			//Randomize which song starts first
 			double red = Math.random();
 			double elite = Math.random();
 			if (red > elite) {
 				redBattle.start();				
 			}
 			else {
 				elite4.start();
 				song2.setIcon(stop);
 				song1.setIcon(play);
 			}
 
 			//Get the other sound files			
 			bump = Applet.newAudioClip(new File("Bump.wav").toURL());
 			ashKetch = Applet.newAudioClip(new File("Ash.wav").toURL());
 			pika = Applet.newAudioClip(new File("Pikachu.wav").toURL());
 			winner = Applet.newAudioClip(new File("Winner.wav").toURL());			
 		}  
 		catch(UnsupportedAudioFileException uafe)	{
 			uafe.printStackTrace();
 		} 
 		catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 		catch (LineUnavailableException lue) {
 			lue.printStackTrace();
 		}			
 	}
 
 	/**
 	 *	Create a northern panel which displays whose turn it is
 	 *	Also include options to play/stop the background music
 	 **/
 	public void northPanel() {
 		north = new JPanel();
 		north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
 
 		//These are two inner panels which will be put in the main north panel
 		north1 = new JPanel();
 		north2 = new JPanel();
 
 		player = new JLabel("Pikachu's Turn");
 		north1.add(player);		
 
 		//Play and stop images
 		stop = new ImageIcon("stop.png");
 		play = new ImageIcon("play.png");
 
 		//two buttons for play and stop
 		song1 = new JButton();
 		song1.setIcon(stop);
 		song2 = new JButton();
 		song2.setIcon(play);
 
 		song1.addActionListener(this);
 		song2.addActionListener(this);
 
 		//add the buttons to the second panel
 		north2.add(new JLabel("Song 1: "));
 		north2.add(song1);
 		north2.add(new JLabel("Song 2: "));
 		north2.add(song2);
 
 		//add both inner panels to the main north panel
 		north.add(north1);
 		north.add(north2);
 
 		//main north panel is added to JFrame
 		add(north, BorderLayout.NORTH);
 
 		player.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 24));
 	}
 
 	/**
 	 *	Create a center panel with an 8x8 board.
 	 **/
 	public void centerPanel() {
 		center = new JPanel(new GridLayout(8, 8));
 
 		//Create the 2D array by using two for loops
 		for(int row = 0; row < 8; row++) {
 			for(int col = 0; col < 8; col++) { 
 
 				//create an object of the inner Button class and add to 2D array list
 				buttons[row][col] = new Button(row, col);
 				center.add(buttons[row][col]);   
 				buttons[row][col].setPreferredSize(new Dimension(70,60)); 
 				setColor(row, col);
 			}
 		}
 		add(center, BorderLayout.CENTER);
 	}
 
 	/**
 	 *	Set board colors to make a pixel-type art
 	 **/
 	public void setColor(int row, int col) {
 		if ((row == 3 || row == 4) && (col == 0 || col == 7)) {
 			buttons[row][col].setBackground(Color.BLACK);
			//fixes for mac compatibility
			buttons[row][col].setOpaque(true);
			buttons[row][col].setBorderPainted(false);
 		}
 		else if ((row == 2 || row == 5) && (col == 1 || col == 6)) {
 			buttons[row][col].setBackground(Color.BLACK);
			//fixes for mac compatibility
			buttons[row][col].setOpaque(true);
			buttons[row][col].setBorderPainted(false);
 		}
 		else if ((row == 1 || row == 6) && (col == 2 || col == 5)) {
 			buttons[row][col].setBackground(Color.BLACK);
			//fixes for mac compatibility
			buttons[row][col].setOpaque(true);
			buttons[row][col].setBorderPainted(false);
 		}
 		else if ((col == 3 || col == 4) && (row == 0 || row == 7)) {
 			buttons[row][col].setBackground(Color.BLACK);
			//fixes for mac compatibility
			buttons[row][col].setOpaque(true);
			buttons[row][col].setBorderPainted(false);
 		}
 		else if ((row == 4 || row == 5) && (col == 1 || col == 2 || col == 3 || col == 4 || col == 5 || col == 6) || (row == 3 && (col == 3 || col == 4)) || (row == 6 && (col == 3 || col == 4))) {
 			buttons[row][col].setBackground(Color.WHITE);
			//fixes for mac compatibility
			buttons[row][col].setOpaque(true);
			buttons[row][col].setBorderPainted(false);
 		}
 		else if ((row == 1 && (col == 3 || col == 4)) || (row == 2 && (col == 2 || col == 3 || col == 4 || col == 5)) || (row == 3 && (col == 1 || col == 2 || col == 5 || col == 6))) {
 			buttons[row][col].setBackground(Color.RED);
			//fixes for mac compatibility
			buttons[row][col].setOpaque(true);
			buttons[row][col].setBorderPainted(false);
 		}
 	}	
 
 	/**
 	 *	Create a southern panel which the number of captures by each player
 	 **/
 	public void southPanel() {
 		south = new JPanel();
 		south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
 
 		//These are two inner south panels which are added to the main south panel
 		south1 = new JPanel();
 		south2 = new JPanel();
 
 		//Instantiate JLabels
 		p1Score = new JLabel("Pikachu's captures: " + p1);
 		p2Score = new JLabel("Ash Ketchum's captures: " + p2);
 
 		p1Score.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
 		p2Score.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
 
 		//Add the JLabels to the inner south panels
 		south1.add(p1Score);
 		south2.add(p2Score);
 
 		//Add the inner south panels to main south panel
 		south.add(south1);
 		south.add(south2);
 
 		add(south, BorderLayout.SOUTH);      
 	}
 
 	/**
 	 *	Create menu bar, menu, and menu items
 	 **/
 	public void createMenu() {
 		menuBar = new JMenuBar();
 
 		file = new JMenu("File");
 		help = new JMenu("Help");
 
 		newGame = new JMenuItem("New Game");
 		rules = new JMenuItem("Rules");
 
 		//add menu items to menus
 		file.add(newGame);
 		help.add(rules);
 
 		//add menus to menu bar
 		menuBar.add(file);
 		menuBar.add(help);
 
 		//Mnemonics
 		file.setMnemonic('F');
 		newGame.setMnemonic('N');
 		help.setMnemonic('H');
 		rules.setMnemonic('R');
 
 		setJMenuBar(menuBar);
 
 		//Action listeners for menu items
 		newGame.addActionListener(this);
 		rules.addActionListener(this);
 	}
 
 	/**
 	 *	Reset the game
 	 **/
 	public void reset() {
 
 		double red = Math.random();
 		double elite = Math.random();
 
 		//Randomize the start music
 		if (red > elite) {
 			redBattle.start();
 			song1.setIcon(stop);
 			song2.setIcon(play);
 		}
 		else {				
 			elite4.start();
 			song2.setIcon(stop);
 			song1.setIcon(play);
 		}
 
 		//This method will reset the necessary class attributes
 		initialValues();
 	}
 
 	/**
 	 *	Action listener method
 	 **/
 	public void actionPerformed(ActionEvent ae) {
 		if (ae.getSource() == rules) {
 			JOptionPane.showMessageDialog(null, "Rules: \n1. You are able to move diagonally forward by one unit space \n2. You are able to move horizontally forward by one unit space \n3. You may only capture an enemy unit if it is located diagonally forward \n4. You cannot move vertically or backwards \n5. Player who reaches other side first wins");
 		}
 		else if (ae.getSource() == newGame) {
 			redBattle.stop();
 			winner.stop();
 			elite4.stop();
 			getContentPane().removeAll();			//Remove eveything from JFrame
 			reset();										//Recreate everything
 			revalidate();								//Clean the JFrame
 			repaint();			
 		}
 		//These conditions will change the play and stop images
 		//respective to which button is pressed.
 		//It will also play or stop the song according to which
 		//button is pressed.
 		else if (ae.getSource() == song1) {
 			if (song1.getIcon() == stop) {
 				song1.setIcon(play);
 				redBattle.stop();
 			}
 			else {
 				song1.setIcon(stop);
 				song2.setIcon(play);
 				elite4.stop();
 				redBattle.start();
 			}
 		}
 		else if (ae.getSource() == song2) {
 			if (song2.getIcon() == stop) {
 				song2.setIcon(play);
 				elite4.stop();
 			}
 			else {
 				song2.setIcon(stop);
 				song1.setIcon(play);
 				redBattle.stop();
 				elite4.start();
 			}
 		}
 	}
 
 	public static void main(String [] args) {
 		new Breakthrough();
 	}
 
 	/**
 	 *	Button class is a button which allows the user to make game movements
 	 **/
 	class Button extends JButton implements ActionListener {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		//class attributes
 		int row, col;
 		ImageIcon pikachu, ash;
 
 		/**
 		 *	Constructor for Button
 		 * 	@param	int row is the button's row place value
 		 *	@param	int col is the button's column place value
 		 **/
 		public Button(int row, int col) {
 			//Innitialize the image icons which are the player pieces
 			pikachu = new ImageIcon("pikachu.png");
 			ash = new ImageIcon("ash.png");
 
 			//initalize class variables
 			this.row = row;
 			this.col = col;
 
 			assignButtonValue();
 			setCoordinates();
 			this.addActionListener(this);
 		}
 
 		/**
 		 *	Assign the image icons to their initial places on each side
 		 **/
 		public void assignButtonValue() {
 			if (col == 0 || col == 1) {				
 				this.setIcon(pikachu);
 			}
 			else if (col == 6 || col == 7) {
 				this.setIcon(ash);
 			}
 		}
 
 		/**
 		 *	This method makes a message appear with the button's coordinate place when hovered over
 		 **/
 		public void setCoordinates()  {
 			String message = "(" + row + ", " + col + ")";
 			setToolTipText(message);
 		}
 
 		/**
 		 *	Get the coordinates of the pressed button
 		 *	@return String		the coordinates
 		 **/
 		public String getCoordinates() {
 			String coordinates = "(" + row + "," + col + ")";
 			return coordinates;
 		}
 
 		/**
 		 *	Moves the player piece
 		 **/
 		public void makeMove(int row, int col) {
 			//row and col  are old coordinates
 			//this.row and this.col are the new coordinates
 
 			//Only move the piece if isValidMove() returns true
 			//Replaces the previously empty space with the current player's piece
 			if (isValidMove(row, col)) {								
 				buttons[row][col].setIcon(null); 
 				if (buttonValue.equals("ash")) {
 					buttons[this.row][this.col].setIcon(ash);
 				}			
 				else if (buttonValue.equals("pikachu")) {
 					buttons[this.row][this.col].setIcon(pikachu);
 				}	
 			}		    
 		}
 
 		/**
 		 *	Checks if the player's attempted move is a valid move
 		 *	@return Boolean	true if valid, false otherwise. Show message if false
 		 **/
 		public boolean isValidMove(int row, int col) {
 
 			//First check if button pressed is player 1
 			if (buttonValue.equals("pikachu")) {
 
 				//Checks if column value increases by one. If it doesn't then return false
 				if(this.col == (col + 1)) {
 
 					//This conditional statement is if the player attempts to move diagonally
 					//Row value may only increase or decrease by one in order to proceed to further checks.
 					if ((this.row == (row - 1)) || (this.row == (row + 1)) ) {
 						if(buttons[this.row][this.col].getIcon() == null) {
 
 							//Return true and alternate player turn if diagonal space is empty.
 							playerTurn = 2;
 							player.setText("Ash Ketchum's Turn");				
 							return true;	            
 						}
 
 						//If diagonal space has an enemy unit then add one to capture points
 						//Return true and alternate player turn after capture
 						else if (buttons[this.row][this.col].getIcon() == ash) {
 							p1++;							
 							usableP2--;							
 							pika.play();
 							south1.add(new JLabel(new ImageIcon("pokeball.png")), BorderLayout.NORTH);
 							p1Score.setText("Pikachu's captures: " + Integer.toString(p1));							
 							playerTurn = 2;
 							player.setText("Ash Ketchum's Turn");
 							return true;
 						}					
 
 						//Return false if the diagonal space has the current player's piece
 						else {
 							bump.play();
 							JOptionPane.showMessageDialog(null, "That unit space is occupied");
 							return false;
 						}
 					}
 
 					//This conditional statement checks if the player is attemping to move forward
 					//Row must stay the same in order to proceed to next check. Else return false
 					else if (this.row == row) {
 
 						//Check if the space selected is empty
 						if(buttons[this.row][this.col].getIcon() == null) {		
 							playerTurn = 2;
 							player.setText("Ash Ketchum's Turn");			
 							return true;	            
 						}
 
 						//If not empty then return false
 						else {
 							bump.play();
 							JOptionPane.showMessageDialog(null, "You are blocked. Move elsewhere");
 							return false;
 						}
 					}
 					else {
 						JOptionPane.showMessageDialog(null, "Not a valid move");
 						return false;
 					}
 				}
 				else {
 					JOptionPane.showMessageDialog(null, "Not a valid move");
 					return false;
 				} 
 			}			
 			//First check if button pressed is player 1
 			else if (buttonValue.equals("ash")) {
 
 				//Checks if column value decreases by one. If it doesn't then return false
 				if(this.col == (col - 1)) {
 
 					//This conditional statement is if the player attempts to move diagonally
 					//Row value may only increase or decrease by one in order to proceed to further checks.
 					if ((this.row == (row - 1)) || (this.row == (row + 1)) ) {
 						if(buttons[this.row][this.col].getIcon() == null) {
 
 							//Return true and alternate player turn if diagonal space is empty.	
 							playerTurn = 1;	
 							player.setText("Pikachu's Turn");			
 							return true;	            
 						}
 
 						//If diagonal space has an enemy unit then add one to capture points
 						//Return true and alternate player turn after capture
 						else if (buttons[this.row][this.col].getIcon() == pikachu) {
 							p2++;
 							south2.add(new JLabel(new ImageIcon("pokeball.png")), BorderLayout.NORTH);
 							usableP1--;
 							ashKetch.play();
 							p2Score.setText("Ash Ketchum's captures: " + Integer.toString(p2));
 							playerTurn = 1;
 							player.setText("Pikachu's Turn");
 							return true;
 						}
 
 						//Return false if the diagonal space has the current player's piece
 						else {
 							bump.play();
 							JOptionPane.showMessageDialog(null, "That unit space is occupied");
 							return false;
 						}
 					}
 
 					//This conditional statement checks if the player is attemping to move forward
 					//Row must stay the same in order to proceed to next check. Else return false
 					else if (this.row == row) {
 
 						//Check if the space selected is empty
 						if(buttons[this.row][this.col].getIcon() == null) {	
 							playerTurn = 1;
 							player.setText("Pikachu's Turn");				
 							return true;	            
 						}
 
 						//If not empty then return false
 						else {
 							bump.play();
 							JOptionPane.showMessageDialog(null, "You are blocked. Move elsewhere");
 							return false;
 						}
 					}
 					else {
 						JOptionPane.showMessageDialog(null, "Not a valid move");
 						return false;
 					}
 				}
 				else {
 					JOptionPane.showMessageDialog(null, "Not a valid move");
 					return false;
 				} 
 			}  
 			else {
 				return false;
 			}    
 		} //End of isValidMove()
 
 		/**
 		 *	Checks if a player has won.
 		 *	If a winner is found, the entire board is disabled
 		 **/
 		public void isWinner() {
 			//Checks if player 1 has reached the end of the board on the other side
 			if (buttonValue.equals("pikachu") && this.col == 7) {				
 				elite4.stop();
 				redBattle.stop();
 				winner.play();
 				player.setText("Winner: Pikachu");
 				JOptionPane.showMessageDialog(null, "Pikachu wins!");
 
 				song1.setEnabled(false);
 				song2.setEnabled(false);
 				//Iterate through entire 2D array and disable each button
 				for(int col = 0; col < 8; col++)
 				{
 					for(int row = 0; row < 8; row++)
 					{			            
 						buttons[row][col].setEnabled(false);   			
 					}
 				}				
 			}
 			//Checks if player 2 has reached the end of the board on the other side
 			else if (buttonValue.equals("ash") && this.col == 0) {
 				elite4.stop();
 				redBattle.stop();
 				winner.play();
 				player.setText("Winner: Ash Ketchum");
 				JOptionPane.showMessageDialog(null, "Ash Ketchum wins!");
 
 				song1.setEnabled(false);
 				song2.setEnabled(false);
 				//Iterate through entire 2D array and disable each button
 				for(int col = 0; col < 8; col++)
 				{
 					for(int row = 0; row < 8; row++)
 					{			            
 						buttons[row][col].setEnabled(false);   			
 					}
 				}
 			}
 			else if (usableP1 == 0 || usableP2 == 0) {
 				if (usableP1 == 0) {
 					elite4.stop();
 					redBattle.stop();
 					winner.play();
 					player.setText("Winner: Ash Ketchum");
 					JOptionPane.showMessageDialog(null, "Pikachu has no more playable units. \nAsh Ketchum wins!");
 				}
 				else {
 					elite4.stop();
 					redBattle.stop();
 					winner.play();
 					player.setText("Winner: Pikachu");
 					JOptionPane.showMessageDialog(null, "Ash Ketchum has no more playable units. \nPikachu wins!");
 
 				}
 				song1.setEnabled(false);
 				song2.setEnabled(false);
 				for(int col = 0; col < 8; col++)
 				{
 					for(int row = 0; row < 8; row++)
 					{			            
 						buttons[row][col].setEnabled(false);   			
 					}
 				}
 			}
 		} //End of isWinner()
 
 		/**
 		 *	Do these action(s) when a button is pressed	
 		 **/
 		public void actionPerformed(ActionEvent ae)  {
 			//Check if button pressed is player 2 and if the counter is 0
 			//Counter is always zero on the first click on a player's turn
 			//A player must click two times
 			if (getIcon() == ash && counter == 0) {
 				//Only allow player 2 to click if the player turn is 2
 				if (playerTurn == 2) {				
 					buttonValue = "ash";
 					buttonPressed = getCoordinates();	//get the clicked button's coordinates
 					counter++;									//increase the counter for the second click on same turn
 				}
 				else {
 					JOptionPane.showMessageDialog(null, "It is not your turn");
 				}
 			}
 
 			//Check if button pressed is player 1 and if the counter is 0
 			else if (getIcon() == pikachu && counter == 0) {
 				//Only allow player 1 to click if the player turn is 1
 				if (playerTurn == 1) {
 					buttonValue = "pikachu";   
 					buttonPressed = getCoordinates();	//get the clicked button's coordinates
 					counter++;  								//increase the counter for the second click on same turn
 				} 
 				else {
 					JOptionPane.showMessageDialog(null, "It is not your turn");
 				}
 			}    
 
 			//If counter was incremented, player is on second button selection
 			else if (counter == 1) {
 
 				//get second selection's coordinates	
 				int row = Integer.parseInt(buttonPressed.substring(1,2));	
 				int col = Integer.parseInt(buttonPressed.substring(3,4));
 
 				//Use these coordinates to call makeMove() which will check if the attempted move is a valid move
 				makeMove(row,col);
 				isWinner();			//check for winner
 				counter = 0;      //reset counter
 			}    
 
 			//if an empty space is selected on the player's first click then show message that it is not possible
 			else if(getIcon() == null && counter == 0) {
 				JOptionPane.showMessageDialog(null, "Cannot move an empty unit");
 			}           
 		}
 	}
 }

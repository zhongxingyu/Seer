 package edu.ucsb.cs56.projects.games.battleship;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.*;
 
 /**
  * A gui class for playing battleship.
 
  *@author Keenan Reimer
 *@version CS56 Spring 2013
 
  **/
 
 public class BattleshipGUI extends JFrame{
 
 	//GUI recorded information
     private String difficulty = null;
     private int gameType = 0;
 	private int lastMove;
 	private int xLoc;
 	private int yLoc;
 	private int boatToPlace;
 	private int boatSpawn;
 	private boolean playersTurn = false;
 	private boolean ipEntered = false;
 	private boolean replay = true;
 	private boolean prompt = true;
 	private boolean placeBoats = false;
 	private boolean boatPlaced = false;
 	private boolean horzOrVert = true; //true for horizontal false for verticle
 	
 	//GUI's knowledge bank. Used for GameGrid cell coloring
 	private ArrayList<Integer> playerBoats = new ArrayList<Integer>();
 	private ArrayList<Integer> enemyBoats = new ArrayList<Integer>();
 	private ArrayList<Integer> shots = new ArrayList<Integer>();
 
 	//GUI Texts
     private JLabel title = new JLabel("Battleship",JLabel.CENTER);
     private JLabel messages = new JLabel("Messages go here:", JLabel.CENTER);
 	
 	//Gametype frame popup
     private JFrame typePopUp = new JFrame();
     private JButton hostButton = new JButton("Host a Game");
     private JButton joinButton = new JButton("Join a Game");
     private JButton computerButton = new JButton("Play Against a Computer");
 	
 	//Difficulty frame popup
     private JFrame diffPopUp = new JFrame();
     private JButton easyButton = new JButton("Easy");
     private JButton mediumButton = new JButton("Medium");
     private JButton hardButton = new JButton("Hard");
 	
 	//Join IP frame popup
 	private JFrame ipPopUp = new JFrame();
 	private JLabel ipRequest = new JLabel("Please input the IP address you wish to join.", JLabel.CENTER);
 	private JTextField ipField = new JTextField();
 	private JLabel ipMessage = new JLabel("Hit enter to submit.", JLabel.CENTER);
 	
 	//Game board component
     private GameGrid board = new GameGrid();
 
 	/**
 	 * Default constructor for the class. Sets everything up.
 	 **/
     BattleshipGUI(){
 	
 	this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 	//Add Title
 	this.getContentPane().add(BorderLayout.NORTH,title);
 		
 	//Add game board
 	this.board.setSize(100,210);
 	this.board.addMouseListener(this.new cellClick());
 	this.board.addMouseMotionListener(this.new mouseMove());
 	this.board.addKeyListener(this.new changeOrientation());
 	this.getContentPane().add(BorderLayout.CENTER,board);
 	
 		
 	//Add messages
 	this.getContentPane().add(BorderLayout.SOUTH, messages);
 	
 	this.pack(); // For some reason this code is necessary to give the board focus
 	this.setSize(500,500);
 	this.board.requestFocusInWindow();
 		
 	//setup difficulty options popup
 	GridLayout threeButtonGrid = new GridLayout(1,3);
 	this.diffPopUp.setLayout(threeButtonGrid);
 	this.diffPopUp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	this.diffPopUp.setSize(600,100);
 		
 	//Add difficulty buttons listeners
 	this.easyButton.addActionListener(this.new difficultyClick());
 	this.mediumButton.addActionListener(this.new difficultyClick());
 	this.hardButton.addActionListener(this.new difficultyClick());
 		
 	this.diffPopUp.add(easyButton);
 	this.diffPopUp.add(mediumButton);
 	this.diffPopUp.add(hardButton);
 		
 	//Setup gametype popup
 	this.typePopUp.setLayout(threeButtonGrid);
 	this.typePopUp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	this.typePopUp.setSize(600,100);
 		
 	//Add type buttons listeners
 	this.hostButton.addActionListener(this.new typeClick());
 	this.joinButton.addActionListener(this.new typeClick());
 	this.computerButton.addActionListener(this.new typeClick());
 		
 	this.typePopUp.add(hostButton);
 	this.typePopUp.add(joinButton);
 	this.typePopUp.add(computerButton);
 	
 	//Setup IP popup
 	GridLayout threeWidgetVerticleGrid = new GridLayout(3,1);
 	this.ipPopUp.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	this.ipPopUp.setLayout(threeWidgetVerticleGrid);
 	this.ipPopUp.setSize(600,100);
 	this.ipField.setHorizontalAlignment(JTextField.CENTER);
 	this.ipField.addActionListener(this.new ipEnter());
 	
 	//Add IP widgets
 	this.ipPopUp.getContentPane().add(BorderLayout.NORTH,ipRequest);
 	this.ipPopUp.getContentPane().add(BorderLayout.CENTER,ipField);
 	this.ipPopUp.getContentPane().add(BorderLayout.SOUTH,ipMessage);
     }
 	
 	/**
 	 * Main class used only for testing purposes.
 	 *@param args not used
 	 **/
     public static void main(String[] args){
 	
 	Player human = new Player();
 	human.randomGenerateBoats();
 	BattleshipGUI gui = new BattleshipGUI();
 	
 	gui.setOptions();
 	gui.setVisible(true);
 	
 	ArrayList<Integer> boats = human.getBoatsArrayList();
 	gui.addPlayerBoats(boats);
 	
 	Computer computer = new Computer("HARD");
 	ArrayList<Integer> boatList = computer.getBoatsArrayList();
 	gui.addEnemyBoats(boatList);
 	
 	for(int i = 0; i<20; i++){
 		int move = computer.makeMove();
 		gui.addShot(move);
 		computer.updateGuessGrid( move, gui.hitEnemy(move));
 	}
 	
     }
 	
 	/**
 	 * Returns whether a shot from the player against the enemy is a "HIT" or a "MISS".
 	 * @param shot The player's shot.
 	 * @return "HIT" or "MISS"
 	 **/
 	public String hitEnemy(int shot){
 		if( enemyBoats.contains(shot)) return "HIT";
 		else return "MISS";
 	}
 	
 	/**
 	 * Returns whether a shot from the enemy against the player is a "HIT" or a "MISS".
 	 * @param shot The enemy's shot.
 	 * @return "HIT" or "MISS"
 	 **/
 	
 	public String hitPlayer(int shot){
 		if( playerBoats.contains(shot)) return "HIT";
 		else return "MISS";
 	}
 	
 	public void placeBoats(){
 		this.placeBoats = true;
 		int[] boats = {2,3,3,4,5};
 		for( int boat: boats){
 			this.boatPlaced = false;
 			this.boatToPlace = boat;
 			while(!this.boatPlaced){
 				try{
 					Thread.sleep(10);
 				}
 				catch (InterruptedException e){
 				}
 			}
 		}
 		this.placeBoats = false;
 		this.repaint();
 	}
 	
 	/**
 	 * Shifts some location to player's GameGrid by moving it down 11 rows.
 	 * @param loc The location to be shifted.
 	 * @return shifted integer location value
 	 **/
 	
 	public int shiftToPlayerGrid(int loc){
 		int boatRow = loc/10 + 11;
 		int boatColumn = loc%10;
 		return boatRow*10 + boatColumn;
 	}
 	
 	/**
 	 * Method to initiate option prompts to the user.
 	 **/
 	
     public void setOptions(){
 	//this.replay = false;
 	this.setVisible(false);
 	this.typePopUp.setVisible(true);
     }
 
 	/**
 	 * Method to reset the GUI
 	 **/
 	 
 	 public void reset(){
 		this.difficulty = null;
 		this.gameType = 0;
 		this.playersTurn = false;
 		this.ipEntered = false;
 		this.replay = true;
 		this.prompt = true;
 		this.lastMove = 0;
 		
 		this.playerBoats = new ArrayList<Integer>();
 		this.enemyBoats = new ArrayList<Integer>();
 		this.shots = new ArrayList<Integer>();
 	
 		this.setVisible(false);
 	 }
 	
 	/**
 	 * Changes the title at the top of the gui.
 	 *@param title The new title to set.
 	 **/
 	
 	public void setTitle(String title){
 		this.title.setText(title);
 	}
 	
 	/**
 	 * Changes the message at the bottom of the gui.
 	 *@param message The new message to set.
 	 **/
     public void setMessage(String message){
 	this.messages.setText(message);
     }
 
 	/**
 	 * Setter for playersTurn variable. Used by controller to let user make a move.
 	 * @param tf The boolean value to set to playersTurn.
 	 **/
 	
 	public void setPlayersTurn(boolean tf){
 		this.playersTurn = tf;
 	}
 	
 	/**
 	 * Getter for replay instance variable. Used to check if the player wants to keep playing.
 	 * @return true for keep playing false for stop playing
 	 **/
 	
 	public boolean getReplay(){
 		return this.replay;
 	}
 	
 	/**
 	 * Getter for gameType instance variable
 	 * @return value stored in gameType
 	 */
 	
 	public int getGameType(){
 		return this.gameType;
 	}
 	
 	/**
 	 * Getter for gameDifficulty instance variable
 	 * @return value stored in gameDifficulty
 	 */
 	
 	public String getDifficulty(){
 		return this.difficulty;
 	}
 	
 	/**
 	 * Getter for playersTurn instance variable
 	 * @return value stored in playersTurn
 	 */
 	
 	public boolean getPlayersTurn(){
 		return this.playersTurn;
 	}
 	
 	/**
 	 * Returns the message being displayed at the bottom of the GUI.
 	 * @return message String
 	 */
 	
 	public String getMessage(){
 		return this.messages.getText();
 	}
 	
 	/**
 	 * Returns the IP address that should be stored in ipField
 	 * @return the IP address stored in ipField
 	 **/
 	 
 	public String getIP(){
 		return this.ipField.getText();
 	}
 	
 	/**
 	 * Find out whether or not an IP address has been entered
 	 * @return state of IP address entry
 	 **/
 	
 	public boolean getIPEntered(){
 		return this.ipEntered;
 	}
 	
 	/**
 	 * Method for returning status of user prompt
 	 * @return true for the uses acknowledged prompt, false for user hasn't acknowledged prompt
 	 **/
 	 
 	 public boolean getPrompt(){
 		return this.prompt;
 	 }
 	 
 	/**
 	 * Lets gui know its players turn
 	 */
 	
 	public void makeMove(){
 		this.playersTurn = true;
 	}
 	
 	public boolean isValidSpawn(int spawn){
 		if(this.horzOrVert){
 			for(int i=0; i < boatToPlace; i++){
 				if((spawn + i)%10 > 9 || playerBoats.contains(spawn+i) || (spawn + i)/10 != spawn/10) return false;
 			}
 		}
 		else{
 			for(int i=0; i<boatToPlace; i++){
 				if((spawn+10*i)/10 > 20 || playerBoats.contains(spawn+10*i)) return false;
 			}
 		}
 		return true;
 	}
 	
 	public void placeBoat(int spawn){
 		if(horzOrVert){
 			for(int i=0; i<this.boatToPlace; i++)
 				this.playerBoats.add(spawn + i);
 		}
 		else{
 			for(int i=0; i<this.boatToPlace; i++){
 				this.playerBoats.add(spawn + 10*i);
 			}
 		}
 	}
 	
 	/**
 	 * Add a shot to the gui's shots list
 	 * @param shot The shot to be added
 	 */
 	
 	public void addShot(int shot){
 		this.shots.add(shot);
 		this.repaint();
 	}
 	
 	/**
 	 * Adds locations for player's boats to the playerBoats list. Shifts their integer locations.
 	 * @param boatList A list of boat locations.
 	 */
 	
 	public void addPlayerBoats(ArrayList<Integer> boatList){
 		for( Integer loc: boatList)
 			this.playerBoats.add(shiftToPlayerGrid(loc));
 	}
 	
 	/**
 	 * Adds locations for enemy's boats to the enemyBoats list.
 	 * @param boatList A list of boat locations.
 	 */
 	
 	public void addEnemyBoats(ArrayList<Integer> boatList){
 		this.enemyBoats = boatList;
 	}
 	
 	/**
 	 * Adds a single boat location to enemyBoats
 	 **/
 	
 	public void addEnemyBoat(int boatLoc){
 		this.enemyBoats.add(boatLoc);
 	}
 	
 	/**
 	 * Method for retrieving player boats. Used when GUI is used to place boats.
 	 **/
 	
 	public ArrayList<Integer> getPlayerBoats(){
 		return this.playerBoats;
 	}
 	
 	/**
 	 * Returns the player's most recent move.
 	 **/
 	
 	public int getLastMove(){
 		return this.lastMove;
 	}
 	
 	/**
 	 * Inner class that paints the literal GameGrid for the game.
 	 **/
 	
     public class GameGrid extends JComponent{
 	public int width;
 	public int height;
 	public int cellWidth;
 	public int startX;
 
 	@Override
 	public void paintComponent(Graphics g)
 	{
 
 	    width = this.getWidth();
 	    height = this.getHeight();		
 	    cellWidth = height/21;
 	    startX = (width - (cellWidth*10))/2;
 	    Graphics2D g2d = (Graphics2D) g;
 	    Color ocean = new Color(0,119,190);
 			
 	    //Make the background white
 	    g2d.setColor(Color.WHITE);
 	    g2d.fillRect(0,0,this.getWidth(),this.getHeight());
 			
 	    //Paint individual cells
 	    for(int i=0; i < 10; i++){
 		for(int j = 0; j<21; j++){
 			int loc = j*10 + i;
 		    if(j==10)
 				g2d.setColor(Color.BLACK);
 		    else if(shots.contains(loc) && (playerBoats.contains(loc) || enemyBoats.contains(loc)))
 				g2d.setColor(Color.RED);
 		    else if(playerBoats.contains(loc))
 				g2d.setColor(Color.DARK_GRAY);
 		    else if(shots.contains(loc))
 				g2d.setColor(ocean.darker());
 		    else g2d.setColor(ocean);
 					
 		    g2d.fillRect(startX + (i*cellWidth),j*cellWidth,cellWidth,cellWidth);
 			
 		}
 	    }
 			
 	    //Paint GameGrid lines
 	    g2d.setColor(Color.GRAY);
 	    g2d.drawLine(startX,0,startX,cellWidth*21); //Far left border
 	    g2d.drawLine(startX + 10*cellWidth,0,startX + 10*cellWidth,cellWidth*21); //Far right border
 	    for(int i=1; i<10; i++)
 		g2d.drawLine(startX + i*cellWidth,0,startX + cellWidth*i,cellWidth*10);	
 	    for(int i=1; i<10; i++)
 		g2d.drawLine(startX + i*cellWidth,cellWidth*11,startX + cellWidth*i,cellWidth*21);	
 	    for(int j=0; j<22; j++)
 		g2d.drawLine(startX, j*cellWidth, startX + 10*cellWidth, j*cellWidth);
 		
 		//Paint boat to be placed if boats are being placed
 		int topLeftX = xLoc - cellWidth/2;
 		int topLeftY = yLoc - cellWidth/2;
 		if(placeBoats && horzOrVert){ //Draw boat to be placed horizontally
 			g2d.setColor(Color.DARK_GRAY);
 			g2d.fillRect(topLeftX, topLeftY, boatToPlace*cellWidth, cellWidth);
 
 			g2d.setColor(Color.GRAY);
 			g2d.drawLine( topLeftX, topLeftY, topLeftX + cellWidth*boatToPlace, topLeftY);
 			g2d.drawLine( topLeftX, topLeftY + cellWidth, topLeftX + cellWidth*boatToPlace, topLeftY + cellWidth);
 			for(int i =0; i<boatToPlace + 1; i++){
 				g2d.drawLine( topLeftX + i*cellWidth, topLeftY, topLeftX + i*cellWidth, topLeftY + cellWidth);
 			}
 		}
 		else if(placeBoats){ //Draw boat to be places vertically 
 			g2d.setColor(Color.DARK_GRAY);
 			g2d.fillRect( topLeftX, topLeftY, cellWidth, boatToPlace*cellWidth);
 				
 			g2d.setColor(Color.GRAY);
 			g2d.drawLine( topLeftX, topLeftY, topLeftX, topLeftY + boatToPlace*cellWidth);
 			g2d.drawLine( topLeftX + cellWidth, topLeftY, topLeftX + cellWidth, topLeftY + boatToPlace*cellWidth);
 			for(int i = 0; i< boatToPlace + 1; i++){
 				g2d.drawLine( topLeftX, topLeftY + i*cellWidth, topLeftX + cellWidth, topLeftY + i*cellWidth);
 			}
 		}
 	}
 		
 	
     }
 	
 	/**
 	 * Listener for difficulty option buttons
 	 **/
 	
     public class difficultyClick implements ActionListener{
 	public void actionPerformed(ActionEvent e){
 	    if( e.getSource() == BattleshipGUI.this.easyButton){
 		difficulty = "EASY";
 		BattleshipGUI.this.diffPopUp.setVisible(false);
 		BattleshipGUI.this.setVisible(true);
 	    }
 	    else if( e.getSource() == BattleshipGUI.this.mediumButton){
 		difficulty = "MEDIUM";
 		BattleshipGUI.this.diffPopUp.setVisible(false);
 		BattleshipGUI.this.setVisible(true);
 	    }
 	    else if ( e.getSource() == BattleshipGUI.this.hardButton){
 		difficulty = "HARD";
 		BattleshipGUI.this.diffPopUp.setVisible(false);
 		BattleshipGUI.this.setVisible(true);
 	    }
 	}
     }
 	
 	/**
 	 * Lisener for the type options buttons
 	 **/
 	 
     public class typeClick implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			if( e.getSource() == BattleshipGUI.this.hostButton){
 			gameType = 1;
 			difficulty = "INTERNET";
 			BattleshipGUI.this.typePopUp.setVisible(false);
 			BattleshipGUI.this.setVisible(true);
 			}
 			else if( e.getSource() == BattleshipGUI.this.joinButton){
 			gameType = 2;
 			difficulty = "INTERNET";
 			BattleshipGUI.this.typePopUp.setVisible(false);
 			BattleshipGUI.this.ipPopUp.setVisible(true);
 			}
 			else if ( e.getSource() == BattleshipGUI.this.computerButton){
 			gameType = 3;
 			BattleshipGUI.this.typePopUp.setVisible(false);
 			BattleshipGUI.this.diffPopUp.setVisible(true);
 			}
 		}
 	}
 	
 	/**
 	 * Listener class for entering IP addresses
 	 **/
 	
 	public class ipEnter implements ActionListener{
 		public void actionPerformed(ActionEvent e){
 			ipEntered = true;
 			BattleshipGUI.this.ipPopUp.setVisible(false);
 			BattleshipGUI.this.setVisible(true);
 		}
 	}
 	
 	/**
 	 * Mouse listener for clicking on game cells
 	 **/
     public class cellClick implements MouseListener{
 		public void mouseClicked(MouseEvent e){}
 		public void mousePressed(MouseEvent e){
 			//Calculate which column & row the mouse was clicked in
 			int cellColumn = (int) Math.floor((e.getX() - board.startX)/board.cellWidth);
 			int cellRow = (int) Math.floor(e.getY()/board.cellWidth);
 			//Add to shot list if it was in enemy territory
 			if(cellRow < 10 && cellRow >=0 && cellColumn >=0 && cellColumn < 10 && playersTurn && (!shots.contains(cellRow*10 + cellColumn))){
 				lastMove = cellRow*10 + cellColumn;
 				shots.add(lastMove);
 				playersTurn = false;
 			}
 			//Record click for boat placement if it was in player's territory
 			else if( cellRow < 21 && cellRow > 10 && cellColumn >=0 && cellColumn < 10){
 				if(placeBoats){
 					int spawn = cellRow*10 + cellColumn;
 					if(isValidSpawn(spawn)){ //Place the boat on click if the spawn location is valid
 						boatPlaced = true;
 						placeBoat(spawn);
 						BattleshipGUI.this.repaint();
 					}
 				}
 			}
 			repaint();
 		}
 		public void mouseReleased(MouseEvent e){}
 		public void mouseEntered(MouseEvent e){}
 		public void mouseExited(MouseEvent e){BattleshipGUI.this.repaint();}
     }
 	
 	public class mouseMove implements MouseMotionListener{
 		public void mouseMoved(MouseEvent e){
 			if(placeBoats){ //Records x & y locations if player is placing boats
 				xLoc = e.getX();
 				yLoc = e.getY();
 				BattleshipGUI.this.repaint();
 			}
 		}
 		public void mouseDragged(MouseEvent e){}
 	}
 
 	public class changeOrientation implements KeyListener{
 		public void keyPressed(KeyEvent e){
 			horzOrVert = !horzOrVert; //Switch from drawing boats horizontally to drawing them vertically
 			BattleshipGUI.this.repaint();
 		}	
 		public void keyReleased(KeyEvent e){}
 		public void keyTyped(KeyEvent e){}
 	}
 }

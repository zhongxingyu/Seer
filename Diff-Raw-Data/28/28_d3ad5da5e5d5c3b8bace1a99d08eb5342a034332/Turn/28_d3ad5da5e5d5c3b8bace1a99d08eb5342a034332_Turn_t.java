 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 
 /** 
  * Handling all actions for the playing of the game.
  * Will take in user input as needed, validating each
  * shot a player takes. 
  * <p>
  * This class will change the state of the board
  * when the user mouses over the opponents grid.
  * Changing icons as necessary. While also changing the icon to
  * the board when a player or computer misses or hits.	
  * <p>
  * Every time a human goes the computer will make its turn,
  * the human player will wait before it can attack again.
  * <p>
  * Right clicking anywhere on the computers board will
  * bring up a pop up menu, from there you can select a player
  * to check that statics for that player. 
  * 
  * @author Mike Cutalo
  * @version 3.0
  */
 public class Turn implements MouseListener, ActionListener
 {
 	/** Set to true if game had ended. */
 	private boolean gameOver;
 	/** Coordinate of the humans shot */
 	private String humanShot;
 	/** The human player */
 	private Player human;
 	/** The computer player */
 	private AI computer;
 
 	private boolean isPVP = false;
 	private Player onLinePlayer;
 	
 	private Game thisGame;
 	private JPopupMenu menu;
 	private Animation hitAnimation = new Animation();;
 	
 	/**
 	 * Constructs a new turn, setting human shot to null
 	 * and game over to false
 	 */
 	public Turn()
 	{
 		this.humanShot = "";
 		this.gameOver = false;
 	}
 
 	/**
 	 * Starts listening in turn class.
 	 * 
 	 * Simply adding listeners to this class, so this code will run 
 	 * when actions are performed. This will also clear the computers board
 	 * just in case if a cheat was enabled. 
 	 */
 	public void startListening()
 	{	
 		if(isPVP){
 			this.onLinePlayer.getPlayerBoard().printBoard(false);	
 		}else{
 			this.computer.getPlayerBoard().printBoard(false);	
 		}
 
 //		ImageIcon gameImg = new ImageIcon(getClass().getResource("/popup/game.jpg"));
 //		JOptionPane.showMessageDialog(Game.humanSunk, 
 //				"Game is now starting!\nClick on the computers board to attack!","Game Starting", 1, gameImg);
 
 		ImageIcon img = new ImageIcon(getClass().getResource("/images/Space.jpg"));
 
 
 		for(int i=0; i < 10; i++)
 		{
 			for(int j=0; j < 10; j++)
 			{
 				if(isPVP){
 					this.onLinePlayer.getPlayerBoard().getBoard()[i][j].addMouseListener(this);
 					this.onLinePlayer.getPlayerBoard().getBoard()[i][j].setIcon(img);
 				}else{
 					this.computer.getPlayerBoard().getBoard()[i][j].addMouseListener(this);
 					this.computer.getPlayerBoard().getBoard()[i][j].setIcon(img);
 				}
 			}	
 		}
		
 	}
 
 
 	/**
 	 * Triggered when mouse click occurs,
 	 * checks to see if the source was from a right click.
 	 * Will call righClickmenu if so.
 	 * 
 	 * @param me The mouse event information	  
 	 */
 	public void mousePressed (MouseEvent me) 
 	{
 		if (me.isPopupTrigger())
 			rightClickMenu(me);
 	}
 
 	/**
 	 * Triggered when mouse is released click occurs,
 	 * checks to see if the source was from a right click.
 	 * Will call righClickmenu if so.
 	 * 
 	 * @param me The mouse event information	  
 	 */
 	public void mouseReleased (MouseEvent me) {
 		if (me.isPopupTrigger())
 			rightClickMenu(me);
 	}  
 
 	/**
 	 * Shows right click menu.
 	 * 
 	 * Will build and display the right click menu,
 	 * it will use the "e" parameter to get the X & Y
 	 * Coordinate of the action to place the menu at the 
 	 * location.
 	 * 
 	 * @param e The event that fired to call this method
 	 */
 	private void rightClickMenu(MouseEvent e){
 		menu = new JPopupMenu();
 
 		JMenuItem hStat = new JMenuItem("Human Stats");
 		hStat.addActionListener(this);
 
 		JMenuItem cStat = new JMenuItem("Computer Stats");
 		cStat.addActionListener(this);
 
 		JMenuItem cancel = new JMenuItem("Cancel");
 		cancel.addActionListener(this);
 
 		menu.add(hStat);
         menu.add(cStat);
         menu.addSeparator();
         menu.add(cancel);
 
         menu.show(e.getComponent(), e.getX(), e.getY());
     }
 
 	/**
 	 * Fires if the mouse enters a space.
 	 * 
 	 * When the mouse is enters a gird space, 
 	 * it will change the icon to that space to a possible
 	 * icon.
 	 * 
 	 * @param e The source of the event
 	 */
 	public void mouseEntered(MouseEvent e) {
 		Singlespace button = (Singlespace) e.getSource();
 		String name = button.getName();
 
 		int row = Integer.parseInt(Character.toString(name.charAt(0)));
 		int col = Integer.parseInt(Character.toString(name.charAt(1)));
 
 		if(isPVP){
 			if(this.onLinePlayer.getPlayerBoard().getBoard()[row][col].isHit() == false &&
 			   this.onLinePlayer.getPlayerBoard().getBoard()[row][col].isMiss() == false){
 	
 				ImageIcon tmpImage = new ImageIcon(getClass().getResource("/images/possible.jpg"));
 				this.onLinePlayer.getPlayerBoard().getBoard()[row][col].setIcon(tmpImage);
 			}		
 		}else{
 			if(this.computer.getPlayerBoard().getBoard()[row][col].isHit() == false &&
 			   this.computer.getPlayerBoard().getBoard()[row][col].isMiss() == false){
 	
 				ImageIcon tmpImage = new ImageIcon(getClass().getResource("/images/possible.jpg"));
 				this.computer.getPlayerBoard().getBoard()[row][col].setIcon(tmpImage);
 			}
 		}
 		
 
 	}
 
 	/**
 	 * Fires if the mouse exits a space.
 	 * 
 	 * This will set the button icon back to the
 	 * original icon for the board.
 	 * 
 	 * @param e The source of the event
 	 */
     public void mouseExited(MouseEvent e) {
     	Singlespace button = (Singlespace) e.getSource();
 		String name = button.getName();
 
 		int row = Integer.parseInt(Character.toString(name.charAt(0)));
 		int col = Integer.parseInt(Character.toString(name.charAt(1)));
 
 		if(isPVP){
 			if(this.onLinePlayer.getPlayerBoard().getBoard()[row][col].isHit() == false &&
 			   this.onLinePlayer.getPlayerBoard().getBoard()[row][col].isMiss() == false)
 			{
 				ImageIcon tmpImage = new ImageIcon(getClass().getResource("/images/Space.jpg"));
 				this.onLinePlayer.getPlayerBoard().getBoard()[row][col].setIcon(tmpImage);
 			}			
 		}else{
 			if(this.computer.getPlayerBoard().getBoard()[row][col].isHit() == false &&
 			   this.computer.getPlayerBoard().getBoard()[row][col].isMiss() == false)
 			{
 				ImageIcon tmpImage = new ImageIcon(getClass().getResource("/images/Space.jpg"));
 				this.computer.getPlayerBoard().getBoard()[row][col].setIcon(tmpImage);
 			}
 		}
     }
 
 	/**
 	 * If a ship has been sunk it will display.
 	 * 
 	 * Simply will display the sunken ships,
 	 * under the players board.
 	 * 
 	 * @throws IOException
 	 */
     public void updateSunk() throws IOException
     {
     	Game.shipPlace.removeAll();
     	
     	char[] s ={'A','B','S','D','P'};
     	String[] images = new String[5];
     	images[0] = "starship.jpg";
     	images[1] = "enterprise.jpg";
     	images[2] = "nebula.jpg";
     	images[3] = "Akira.jpg";
     	images[4] = "patrol.jpg";
     	    	
     	JPanel dead = new JPanel();
     	dead.setLayout(new BorderLayout());
     	dead.setBackground(Color.BLACK);
     	
     	JPanel humanSunk = new JPanel();
     	humanSunk.setBackground(Color.BLACK);
     	
     	JPanel computerSunk = new JPanel();
     	computerSunk.setBackground(Color.BLACK);
     	
     	JPanel boxHuman = new JPanel();
     	boxHuman.setBackground(Color.BLACK);
     	
     	JPanel boxComputer = new JPanel();
     	boxComputer.setBackground(Color.BLACK);
     	 		
     	for(int x=0; x < images.length; x++)
     	{
     		if(this.human.getAllShips().get(s[x]).isAlive() == false)
     		{
     			JLabel tmp = new JLabel();
     			ImageIcon tmpImg = new ImageIcon(getClass().getResource("/startrek/" + images[x]));
     			tmp.setIcon(tmpImg);
     			humanSunk.add(tmp);
     		}
     		
     		if(this.computer.getAllShips().get(s[x]).isAlive() == false)
     		{
     			JLabel tmp = new JLabel();
     			ImageIcon tmpImg = new ImageIcon(getClass().getResource("/startrek/" + images[x]));
     			tmp.setIcon(tmpImg);
     			computerSunk.add(tmp);
     		}
     	}
 
     	boxHuman.setLayout(new BoxLayout(boxHuman, BoxLayout.Y_AXIS));
     	boxHuman.setBackground(null);
     	
     	boxComputer.setLayout(new BoxLayout(boxComputer, BoxLayout.Y_AXIS));
     	boxComputer.setBackground(null);
     	
     	humanSunk.setPreferredSize(new Dimension(350,70));
     	humanSunk.setMaximumSize(new Dimension(350,70));
     	humanSunk.setSize(350,70);
     	
     	computerSunk.setPreferredSize(new Dimension(350,70));
     	computerSunk.setMaximumSize(new Dimension(350,70));
     	computerSunk.setSize(350,70);
     	
     	JLabel hSunkLabel = new JLabel("Human Ships Destroyed");
     	hSunkLabel.setForeground(Color.WHITE);
     	boxHuman.add(hSunkLabel);
     	boxHuman.add(humanSunk);
     	
     	JLabel cSunkLabel = new JLabel("Borg Ships Destroyed");
     	cSunkLabel.setForeground(Color.WHITE);
     	boxComputer.add(cSunkLabel);
     	boxComputer.add(computerSunk);
     	
     	dead.add(boxHuman, BorderLayout.WEST);
     	dead.add(boxComputer, BorderLayout.EAST);
     	
     	Game.shipPlace.add(dead);   
     	Game.shipPlace.repaint();
     	Game.shipPlace.validate();
     }
     
 	/**
 	 * This will fire when a right click menu item is clicked.
 	 * 
 	 * Will display human or computer statics in the form 
 	 * of a dialog box. 
 	 * 
 	 * @param e The source and event of the action
 	 */
 	public void actionPerformed(ActionEvent e)
 	{
 		Sound report = new Sound();
 		String action = e.getActionCommand();
 		ImageIcon human = new ImageIcon(getClass().getResource("/popup/Picard.jpg"));
 		ImageIcon comp = new ImageIcon(getClass().getResource("/popup/BorgQueen.jpg"));
 
 		if(action.equals("Cancel")){
 			this.menu.setVisible(false);
 		}
 
 		if(action.equals("Human Stats")){
 			report.Authorization();
 			JOptionPane.showMessageDialog(Game.humanSunk, 
 					"Total Turn: " + this.human.getNumTurns() + "\n" +
 					"Miss Shots: " + this.human.getNumMissed() + "\n" +
 					"Hit Shots: " + this.human.getNumHits() + "\n" + 
 					"Ships Sunk: " + this.computer.getShipsSunk(), "Human Stats", 1, human);
 		}
 		else if(action.equals("Computer Stats")){
 			report.StatusReport();
 			JOptionPane.showMessageDialog(Game.computerSunk, 
 					"Total Turn: " + this.computer.getNumTurns() + "\n" +
 					"Miss Shots: " + this.computer.getNumMissed() + "\n" +
 					"Hit Shots: " + this.computer.getNumHits() + "\n" +
 					"Ships Sunk: " + this.human.getShipsSunk(), "Computer Stats", 1 , comp);
 		}
 	}
 
 	public void playDestroyedShip()
 	{	
 		char[] s ={'A','B','S','D','P'};
 
 		for(int i=0; i < s.length; i++)
 		{	
 			if(isPVP){
 				if(onLinePlayer.allShips.get(s[i]).isAlive() == false &&
 				   onLinePlayer.allShips.get(s[i]).isVideoPlayed() == false)
 				{
 					onLinePlayer.allShips.get(s[i]).setVideoPlayed(true);
 					thisGame.PlayExplosionVideo();
 				}	
 			}else{
 				if(computer.allShips.get(s[i]).isAlive() == false &&
 				   computer.allShips.get(s[i]).isVideoPlayed() == false)
 				{
 					computer.allShips.get(s[i]).setVideoPlayed(true);
 					thisGame.PlayExplosionVideo();
 				}		
 			}
 			
 		}
 	}
 	
 	/**
 	 * Handle all clicks that take place a board.
 	 * 
 	 * This method will only fire when a spot on the
 	 * computer board is clicked. This method will determine if the
 	 * spot click is a miss or hit.
 	 * <p>
 	 * After the human player goes the computer will take his turn. 
 	 * This method will also check if the game is over each time a turn takes place.
 	 * If the game is over it will announce the winner of the game with the statics 
 	 * for that player.
 	 * 
 	 * @param e The information of event that was fired
 	 */
 	public void mouseClicked(MouseEvent e)
 	{
 		ImageIcon tmpImage;
 
 		if(this.humanShot.equals(""))
 		{
 			Singlespace button = (Singlespace) e.getSource();
 			this.humanShot = button.getName();
 		}
 
 		try{
 			if(this.gameOver == false && this.humanShot != "")
 			{
 				int row = Integer.parseInt(Character.toString(this.humanShot.charAt(0)));
 				int col = Integer.parseInt(Character.toString(this.humanShot.charAt(1)));
 
 				if(this.onLinePlayer.getPlayerBoard().getBoard()[row][col].isHit() == false &&
 				   this.onLinePlayer.getPlayerBoard().getBoard()[row][col].isMiss() ==	false)
 				{
 					if(this.onLinePlayer.getPlayerBoard().getBoard()[row][col].isSpaceEmpty() == false)
 					{			
 						hitAnimation.setPlayer(this.onLinePlayer);
 						hitAnimation.shipSinking(row,col);		
 						
 						Sound soundFactory = new Sound();
 						soundFactory.ShipHit();
 						
 						this.human.setNumHits(this.human.getNumHits() + 1);
 						this.human.setNumTurns(this.human.getNumTurns() + 1);
 
 						char hitShip = this.onLinePlayer.getPlayerBoard().getBoard()[row][col].getOccupyingShip();
 						this.onLinePlayer.getAllShips().get(hitShip).setSumHit((this.onLinePlayer.getAllShips().get(hitShip).getSumHit()+1));
 
 					}else{
 						this.onLinePlayer.getPlayerBoard().getBoard()[row][col].setMiss(true);
 						tmpImage = new ImageIcon(getClass().getResource("/images/black.jpg"));
 						this.onLinePlayer.getPlayerBoard().getBoard()[row][col].setIcon(tmpImage);
 						this.onLinePlayer.getPlayerBoard().getBoard()[row][col].setEnabled(false);
 						this.onLinePlayer.getPlayerBoard().getBoard()[row][col].setDisabledIcon(tmpImage);
 						
 						this.human.setNumMissed(this.human.getNumMissed() + 1);
 						this.human.setNumTurns(this.human.getNumTurns() + 1);	
 					}
 				}
 				
 				if(this.computer.getPlayerBoard().getBoard()[row][col].isHit() == false &&
 				   this.computer.getPlayerBoard().getBoard()[row][col].isMiss() ==	false)
 				{
 					if(this.computer.getPlayerBoard().getBoard()[row][col].isSpaceEmpty() == false)
 					{			
 						//hitAnimation = new Animation();
 
 						hitAnimation.setPlayer(this.computer);
 						hitAnimation.shipSinking(row,col);
 																			
 						Sound soundFactory = new Sound();
 						soundFactory.ShipHit();
 												
 						/*
 						Now doing this in the Animation Class
 						Just seems natural do it there. 
 						Do need to figure out how to stop showing skull img,
 						before the animation is done when a ship sinks. 
 						*/
 
 						//Taking care of board
 //						this.computer.getPlayerBoard().getBoard()[row][col].setHit(true);	
 //						tmpImage = new ImageIcon(getClass().getResource("/images/hit.jpg"));
 //						this.computer.getPlayerBoard().getBoard()[row][col].setIcon(tmpImage);
 //						this.computer.getPlayerBoard().getBoard()[row][col].setEnabled(false);
 //						this.computer.getPlayerBoard().getBoard()[row][col].setDisabledIcon(tmpImage);
 
 						//Setting stats for player
 						this.human.setNumHits(this.human.getNumHits() + 1);
 						this.human.setNumTurns(this.human.getNumTurns() + 1);
 
 						char hitShip = this.computer.getPlayerBoard().getBoard()[row][col].getOccupyingShip();
 						this.computer.getAllShips().get(hitShip).setSumHit((this.computer.getAllShips().get(hitShip).getSumHit()+1));
 						this.computer.aITurn(this.human);	
 					}
 					else
 					{
 						//Take care of board
 						this.computer.getPlayerBoard().getBoard()[row][col].setMiss(true);
 						tmpImage = new ImageIcon(getClass().getResource("/images/black.jpg"));
 						this.computer.getPlayerBoard().getBoard()[row][col].setIcon(tmpImage);
 						this.computer.getPlayerBoard().getBoard()[row][col].setEnabled(false);
 						this.computer.getPlayerBoard().getBoard()[row][col].setDisabledIcon(tmpImage);
 						this.computer.aITurn(this.human);
 						
 						//Setting stats for player
 						this.human.setNumMissed(this.human.getNumMissed() + 1);
 						this.human.setNumTurns(this.human.getNumTurns() + 1);						
 					}
 				}
 				
 				if(isPVP){
 					this.onLinePlayer.getPlayerBoard().printBoard(false);
 					System.out.println("\n\n");	
 				}else{
 					this.computer.getPlayerBoard().printBoard(false);
 					System.out.println("\n\n");	
 				}
 
 
 //				this.computer.checkShips();		
 //				this.human.checkShips();
 
 				updateSunk();
 
 				ImageIcon gameImg = new ImageIcon(getClass().getResource("/popup/game.jpg"));
 
 				if(this.human.checkShips() == true){
 					JOptionPane.showMessageDialog(Game.computerSunk, 
 							"Computer Wins!! \n"+
 							"Total Turn: " + this.computer.getNumTurns() + "\n" +
 							"Miss Shots: " + this.computer.getNumMissed() + "\n" +
 							"Hit Shots: " + this.computer.getNumHits() + "\n" +
 							"Ships Sunk: " + this.human.getShipsSunk(), "Game Over",1, gameImg);
 
 					this.gameOver = true;
 					thisGame.PlayGameLose();
 
 				}else if(this.computer.checkShips() == true){
 					JOptionPane.showMessageDialog(Game.computerSunk, 
 							"Human Wins!! \n" +
 							"Total Turn: " + this.human.getNumTurns() + "\n" +
 							"Miss Shots: " + this.human.getNumMissed() + "\n" +
 							"Hit Shots: " + this.human.getNumHits() + "\n" + 
 							"Ships Sunk: " + this.computer.getShipsSunk(), "Game Over",1,gameImg);
 
 					this.gameOver = true;
 					thisGame.PlayGameWin();
 				}
 				
 				playDestroyedShip();
 				this.humanShot = "";
 			}
 		}catch(IOException err){
 			err.getStackTrace();
 		} 
 
 		if(this.gameOver == true)
 			endGame();
 	}
 
 	/**
 	 * Restarts the game
 	 * 
 	 * Calls a method in the Game class called new game
 	 * this method simply reloads the page.
 	 */
 	public void endGame()
 	{	
 		for(int i=0; i < 10; i++)
 		{
 			for(int j=0; j < 10; j++)
 			{	
 				if(isPVP){
 					onLinePlayer.getPlayerBoard().getBoard()[i][j].removeActionListener(this);
 					onLinePlayer.getPlayerBoard().getBoard()[i][j].removeMouseListener(this);
 				}else{
 					computer.getPlayerBoard().getBoard()[i][j].removeActionListener(this);
 					computer.getPlayerBoard().getBoard()[i][j].removeMouseListener(this);
 				}	
 			}
 		}
 		this.thisGame.NewGame();
 	}
 
 	/*Getters & Setters */
 	public Player getHuman() {
 		return human;
 	}
 	public void setHuman(Player human) {
 		this.human = human;
 	}
 	public AI getComputer() {
 		return computer;
 	}
 	public void setComputer(AI computer) {
 		this.computer = computer;
 	}
 	public void setThisGame(Game thisGame) {
 		this.thisGame = thisGame;
 	}
 	public Game getThisGame() {
 		return thisGame;
 	}
 	public Player getOnLinePlayer() {
 		return onLinePlayer;
 	}
 	public void setOnLinePlayer(Player onLinePlayer) {
 		this.onLinePlayer = onLinePlayer;
 	}
 	public boolean isPVP() {
 		return isPVP;
 	}
 	public void setPVP(boolean isPVP) {
 		this.isPVP = isPVP;
 	}
 }

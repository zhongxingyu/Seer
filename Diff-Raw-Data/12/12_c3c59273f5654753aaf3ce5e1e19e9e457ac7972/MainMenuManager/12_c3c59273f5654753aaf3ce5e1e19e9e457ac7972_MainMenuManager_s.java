 package edu.gatech.cs2340.ui;
 import javax.swing.JPanel;
 
 
 
 /**
  * 
  * @author Stephen Conway
  * 		Function group:		Controller: GUI
  * 		Created for:		M6		10/8/13
  * 		Assigned to:		Stephen
  * 		Modifications:		M6		10/8/13		Stephen
  * 									Moved all management of the main menus to this class from Driver			
  * 
  * 
  * 
  * 		Purpose: Common parent class for any GUI components			 
  */
 public class MainMenuManager implements GUIManager{
 	private boolean menuSequenceFinished;
 	private MainGameWindow mainGameWindow;
 	
 	/**
 	 * #M6
 	 * Main constructor. 
 	 * 
 	 * @param mainGameWindow
 	 */
 	public MainMenuManager(MainGameWindow mainGameWindow) {
 		this.mainGameWindow = mainGameWindow;
 	}
 	
 	/**
 	 * #M6
 	 * Method to start the main menu sequence
 	 */
 	public void run() {
		mainGameWindow.setPanel(new PlayerConfigMenu(this));
 	}
 	
 	
 	@Override
 	public void notify(JPanel panel, String message) {
		if (panel instanceof PlayerConfigMenu && message.equals("next")) {
			int numPlayers = ((PlayerConfigMenu)panel).getPlayerCount();
			GameConfigMenu gameConfig = new GameConfigMenu(this, numPlayers);
 			mainGameWindow.setPanel(gameConfig);
 		} 
		else if (panel instanceof GameConfigMenu && message.equals("next")) {
 			
 			menuSequenceFinished = true;
 		}
 	}
 
 	@Override
 	public boolean isFinished() {
 		return menuSequenceFinished;
 	}
 	
 }

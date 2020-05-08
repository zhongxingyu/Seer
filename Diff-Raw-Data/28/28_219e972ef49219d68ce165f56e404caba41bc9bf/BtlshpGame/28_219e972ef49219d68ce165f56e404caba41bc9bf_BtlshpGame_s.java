 package btlshp;
 
 import java.io.File;
 
 import btlshp.entities.Map;
 import btlshp.entities.Player;
 import btlshp.enums.AppState;
 import btlshp.ui.DialogResult;
 import btlshp.ui.MainUI;
 
 public class BtlshpGame {
 	private AppState appState;
 	private MainUI   mainUi;
 	private Player   localPlayer;
 	
 	public BtlshpGame() {
 		appState = AppState.NoGame;
 		localPlayer = null;
 		mainUi = new MainUI();
 	}
 	
 	
 	/**
 	 * Creates the main UI for the game
 	 */
 	public void buildUI()
 	{
 		mainUi.makeGUI();
 	}
 	
 	
 	/**
 	 * Handles a UI-side new game request.
 	 */
 	public void newGame() {
 		if(appState != AppState.NoGame) {
 			if(mainUi.yesNoCancelDialog("Start new game?", "You have to forfeit the current game to start a new one, are you sure?") != DialogResult.Yes)
 				return;
 		}
 		
 		File gameDir = mainUi.selectDiretory();
 		
 		if(gameDir != null) {
 			localPlayer = new Player();
 			mainUi.setMap(new Map(localPlayer, new Player()));
 			appState = AppState.LocalTurn;
 		}
 	}
 	
 	
 	/**
 	 * Handles a UI-side help request.
 	 */
 	public void helpScreen() {
 		mainUi.showHelp();
 	}
 	
 	/**
 	 * Gets the current state of the app
 	 * @return The state of the app
 	 */
 	public AppState getAppState() {
 		return appState;
 	}
 	
 	
 	
 	public void quitGame() {
 		if(appState != AppState.NoGame) {
			// TODO: The user either has to forfeit or save game.
 		}
 		else if (mainUi.yesNoCancelDialog("Do you really want to quit?", "Please don't go, the Socialist Commies will win!") == DialogResult.Yes) {
 			System.exit(0);
 		}
 	}
 	
 	
 	
 	public Player getLocalPlayer() {
 		return localPlayer;
 	}
 }

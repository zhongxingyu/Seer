 package btlshp;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
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
 		
 		File gameDir = mainUi.selectDiretory("Chose a hosting location");
 		
 		if(gameDir != null) {
 			localPlayer = new Player();
 			Map m = new Map(localPlayer, new Player());
 			mainUi.setMap(m);
			appState = AppState.LocalTurn;
 			ObjectOutputStream objOut = null;
 			FileOutputStream fileOut = null;
 			try {
 				fileOut = new FileOutputStream(gameDir.getAbsolutePath() +"/"+ 0+".ser");
 				objOut = new ObjectOutputStream(fileOut);
 				
 				objOut.writeObject(m);
 				objOut.close();
 				
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			mainUi.updateMainMenu();
 		}
 	}
 	public void joinGame(){
 		File gameDir = mainUi.selectDiretory("Choose a shared directory");
 		
 		if(gameDir != null){
 			outputMessage("Joining game " + gameDir.getPath());
 			String filePath = gameDir.getPath()+"/"+0+".ser";
 			FileInputStream fileIn = null;
 			ObjectInputStream objIn = null;
 			try{
 				fileIn = new FileInputStream(filePath);
 				objIn = new ObjectInputStream(fileIn);
 				Map loadMap= null;
 				try {
 					loadMap =(Map) objIn.readObject();
 				} catch (ClassNotFoundException e) {
 					e.printStackTrace();
 				}
 				localPlayer = loadMap.getRightPlayer();
 				mainUi.setMap(loadMap);
 				appState = AppState.LocalTurn;
 				mainUi.updateMainMenu();
 			}catch(IOException e){
 				System.err.println("IOException: File Path: "+filePath);
 				e.printStackTrace();
 			}
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
 	
 	
 	/**
 	 * Handles a UI-side quit event.
 	 */
 	public void quitGame() {
 		if(appState != AppState.NoGame) {
 			mainUi.showAlert("Game in progress.", "Please either forfeit or save a game before quitting.");
 		}
 		else if (mainUi.yesNoCancelDialog("Do you really want to quit?", "Please don't go, the Socialist Commies will win!") == DialogResult.Yes) {
 			System.exit(0);
 		}
 	}
 	
 	
 	/**
 	 * Handles a UI-side forfeit game event.
 	 */
 	public void forfeitGame() {
 		if(appState != AppState.NoGame &&
 				mainUi.yesNoCancelDialog("Forfeit", "Are you sure you wish to forfeit? This may lead to eternal shame!") == DialogResult.Yes) {
 			
 			// TODO: Send notification
 			localPlayer = null;
 			mainUi.setMap(null);
 			appState = AppState.NoGame;
 			mainUi.updateMainMenu();
 		}
 	}
 	
 	
 	/**
 	 * Handles a UI-side save game event.
 	 */
 	public void saveGame() {
 		if(appState == AppState.NoGame) 
 			return;
 		
 		File f = mainUi.selectSaveFile("Select a file to save");
 		
 		if(f != null) {
 			outputMessage("Save file: " + f.getPath());
 			ObjectOutputStream objOut = null;
 			FileOutputStream fileOut = null;
 			try {
 				fileOut = new FileOutputStream(f.getPath());
 				objOut = new ObjectOutputStream(fileOut);
 				
 				objOut.writeObject(mainUi.getMap());
 				objOut.close();
 				
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			localPlayer = null;
 			mainUi.setMap(null);
 			appState = AppState.NoGame;
 			mainUi.updateMainMenu();
 		}
 	}
 	
 	
 	
 	/**
 	 * Handles a UI-side restore game event.
 	 */
 	public void restoreGame() {
 		//if(appState == AppState.NoGame) 
 			//return;
 		
 		File f = mainUi.selectSaveFile("Select a file to restore from");
 		
 		if(f != null) {
 			outputMessage("Restore file: " + f.getPath());
 			String filePath = f.getPath();
 			FileInputStream fileIn = null;
 			ObjectInputStream objIn = null;
 			try{
 				fileIn = new FileInputStream(filePath);
 				objIn = new ObjectInputStream(fileIn);
 				Map loadMap= null;
 				try {
 					loadMap = (Map) objIn.readObject();
 				} catch (ClassNotFoundException e) {
 					e.printStackTrace();
 				}
 				localPlayer = loadMap.getLeftPlayer();
 				mainUi.setMap(loadMap);
 				appState = AppState.LocalTurn;
 				mainUi.updateMainMenu();
 			}catch(IOException e){
 				System.err.println("IOException: File Path: "+filePath);
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 	
 	public Player getLocalPlayer() {
 		return localPlayer;
 	}
 
 	
 	/**
 	 * Outputs a message to a screen element used to display action messages for the player.
 	 * 
 	 * @param msg
 	 */
 	public void outputMessage(String msg) {
 		mainUi.outputMessage(msg);
 	}
 }

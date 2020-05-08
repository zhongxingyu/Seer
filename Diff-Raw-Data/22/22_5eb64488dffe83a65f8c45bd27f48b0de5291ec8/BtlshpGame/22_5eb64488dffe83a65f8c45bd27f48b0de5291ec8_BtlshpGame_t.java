 package btlshp;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import btlshp.entities.Construct;
 import btlshp.entities.Map;
 import btlshp.entities.Player;
 import btlshp.enums.AppState;
 import btlshp.turns.Turn;
 import btlshp.turns.TurnFactory;
 import btlshp.ui.DialogResult;
 import btlshp.ui.MainUI;
 
 public class BtlshpGame {
 	private AppState appState;
 	private MainUI   mainUi;
 	private Player   localPlayer, remotePlayer;
 	private File 	 gameDir;
 
 	public BtlshpGame() {
 		localPlayer = remotePlayer = null;
 		mainUi = new MainUI();
 		appState = AppState.NoGame;
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
 		
 		gameDir = mainUi.selectDiretory("Chose a hosting location");
 		
 		if(gameDir != null) {
 			localPlayer = new Player();
 			remotePlayer = new Player();
 			Map m = new Map(localPlayer, remotePlayer);
 			mainUi.setMap(m);
 			
 			setAppState(AppState.LocalTurn);
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
 			File f = new File(gameDir.getAbsolutePath() +"/"+ 0+".ser");
 			setAppState(AppState.RemoteTurn);
 			waitForTurn(f.lastModified());
 			mainUi.updateMainMenu();
 		}
 	}
 	public void joinGame(){
 		gameDir = mainUi.selectDiretory("Choose a shared directory");
 		
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
 				remotePlayer = loadMap.getLeftPlayer();
 				mainUi.setMap(loadMap);
 				setAppState(AppState.LocalTurn);
 				sendTurn(TurnFactory.pass());
 				
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
 	
 	
 	private void setAppState(AppState newState) {
 		appState = newState;
 		mainUi.updateStatus();
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
 			
 			
 			localPlayer = remotePlayer = null;
 			mainUi.setMap(null);
 			setAppState(AppState.NoGame);
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
 			localPlayer = remotePlayer = null;
 			mainUi.setMap(null);
 			setAppState(AppState.NoGame);
 			mainUi.updateMainMenu();
 		}
 	}
 	
 	
 	
 	/**
 	 * Handles a UI-side restore game event.
 	 */
 	public void restoreGame() {
 	
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
 				remotePlayer = loadMap.getRightPlayer();
 				mainUi.setMap(loadMap);
 				setAppState(AppState.LocalTurn);
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
 	
 	
 	public Player getRemotePlayer() {
 		return remotePlayer;
 	}
 
 	
 	/**
 	 * Outputs a message to a screen element used to display action messages for the player.
 	 * 
 	 * @param msg
 	 */
 	public void outputMessage(String msg) {
 		mainUi.outputMessage(msg);
 	}
 
 
 	public void shipDestroyed(Construct destroyedShip) {
 		System.out.println("Construct Destroyed: "+ destroyedShip );
 	}
 	
 	
 	/**
 	 * Used by the game to send a turn and place the game into a waiting-for-turn state.
 	 * @param t   Turn object to send to other player.
 	 */
 	public void sendTurn(Turn t) {
 		if(appState != AppState.LocalTurn)
 			throw new IllegalStateException("NOT YO TURN!");
 		long modified =t.writeOut(gameDir.getAbsolutePath()); 
 		if(modified<0){
 			throw new IllegalStateException("Error Writing out turn");
 		}
 	    setAppState(AppState.RemoteTurn);
 
 		waitForTurn(modified);
 		// TODO: Send turn
 	}
 	/**
 	 * Read the last modified time of a file every 5 seconds
 	 * @param modified time the file was last modified
 	 */
 	private void waitForTurn(final long modified){
 			
 			Timer t = new Timer();
 			
 			
 			t.schedule(new TimerTask(){
 				@Override
 				public void run() {
 					String filePath = gameDir.getPath();
 					File f = new File(filePath+"/game.ser");
 					
 					if(f.lastModified()> modified){
 						FileInputStream fileIn = null;
 						ObjectInputStream objIn = null;
 						try{
 							fileIn = new FileInputStream(filePath+"/game.ser");
 							objIn = new ObjectInputStream(fileIn);
 							Turn loadTurn= null;
 							try {
 								loadTurn = (Turn) objIn.readObject();
 								objIn.close();
 								fileIn.close();
 								loadTurn.setMap(mainUi.getMap());
 								loadTurn.executeTurn();
 								setAppState(AppState.LocalTurn);
 								cancel();
 								
 							} catch (ClassNotFoundException e) {
 								e.printStackTrace();
 							}
 						
 							
 						
 						}catch(IOException e){
 							System.err.println("IOException: File Path: "+filePath);
 							e.printStackTrace();
 						}
 					}	
 					
 				}
 			
			}, 500, 500);
 			
 	
 			
 	}
 
 
 	/**
 	 * Called when one player looses all ships.
 	 */
 	public void gameOver() {
 		setAppState(AppState.NoGame);
 		
 		boolean locallost = localPlayer.getShips().length == 0;
 		boolean remotelost = remotePlayer.getShips().length == 0;
 		
 		if(locallost && remotelost)
 			mainUi.showAlert("Draw!", "You and your enemy have reached a draw!\n100 points for both of you!");
 		else if(locallost)
 			mainUi.showAlert("Defeat!", "You have been defeated by your opponent!\nYour approximate score is 0.\nYour actual score is 0");
 		else
 			mainUi.showAlert("Victory!", "You have vanquished your foe!");
 	}
 }

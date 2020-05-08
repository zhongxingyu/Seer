 package src.ui.controller;
 
 import javax.swing.JOptionPane;
 import src.GameMain;
 import src.net.AvailableGame;
 import src.net.LobbyManager;
 import src.net.NetworkGame;
 import src.net.NetworkGameController;
 import src.ui.GameOverPanel;
 import src.ui.Lobby;
 import src.ui.MultiplayerClientWaitScreen;
 import src.ui.MultiplayerGamePanel;
 import src.ui.MultiplayerGameSetup;
 import src.ui.MultiplayerHostWaitScreen;
 import src.ui.TitleScreen;
 import src.ui.WinPanel;
 
 /**
  * Handles events occurring with the multiplayer lobby; enables creation, joining, dropping, and starting of games.
  */
 public class MultiplayerController {
 	private GameMain gameMain;
 	private Lobby lobby;
 	private LobbyManager lobbyManager;
 	private MultiplayerGameSetup gameSetup;
 	private MultiplayerHostWaitScreen hostWaitScreen;
 	private MultiplayerClientWaitScreen clientWaitScreen;
 	
 	private boolean gameInProgress;
 	private GameController localGameController;
 	
 	public MultiplayerController(GameMain gameMain) {
 		this.gameMain = gameMain;
 		
 		lobbyManager = new LobbyManager(this);
 		lobby = new Lobby(this);
 		gameSetup = new MultiplayerGameSetup(this);
 		hostWaitScreen = new MultiplayerHostWaitScreen("", "", this);
 		clientWaitScreen = new MultiplayerClientWaitScreen(this);
 		gameInProgress = false;
 	}
 	
 	/*
 	 * Lobby control methods
 	 */
 	public void showLobby() {
 		gameMain.showScreen(lobby);
 	}
 	
 	public void exitLobby() {
 		TitleScreen title = new TitleScreen(gameMain);
 		gameMain.showScreen(title);
 	}
 	
 	/*
 	 * Game creation
 	 */
 	public void beginGameCreation() {
 		gameSetup.reset();
 		gameMain.showScreen(gameSetup);
 	}
 	
 	public void cancelGameCreation() {
 		gameMain.showScreen(lobby);
 	}
 	
 	public void completeGameCreation() {
 		AvailableGame newHostedGame = new AvailableGame();
 		newHostedGame.setGameName(gameSetup.getGameName());
 		newHostedGame.setMapName(gameSetup.getMapName());
 		lobbyManager.hostNewGame(newHostedGame);
 	
 		hostWaitScreen = new MultiplayerHostWaitScreen(gameSetup.getGameName(), gameSetup.getMapName(), this);
 		gameMain.showScreen(hostWaitScreen);
 	}
 	
 	/*
 	 * Joining / exiting games
 	 */
 	public void joinGame(int selectedRow) {
 		synchronized (lobbyManager.getAvailableGames()) {
 			try {
 				lobbyManager.joinGame(lobbyManager.getAvailableGames().get(selectedRow));
 			} catch (IndexOutOfBoundsException e) {
 				//Just to prevent a user trying to to rejoin a game that has been quit before refreshing
 			}
 		}
 	}
 
 	public void waitingToJoinGame() {
 		gameMain.showScreen(clientWaitScreen);
 	}
 	
 	public void playerAttemptedToJoin(String name) {	
 		hostWaitScreen.setPotentialOpponent(name);
 	}
 	
 	public void bootPotentialOpponent() {
 		lobbyManager.boot();
 		hostWaitScreen.setPotentialOpponent(null);
 	}
 	
 	public void wasBootedFromGame() {
 		JOptionPane.showMessageDialog(clientWaitScreen, 
 				"You've been booted!", "Booted!", JOptionPane.ERROR_MESSAGE);
 		
 		lobby.updateGameListPane();
 		gameMain.showScreen(lobby);
 	}
 	
 	public void startNetworkGame() {
 		startNetworkGame(lobbyManager.acceptPlayer());
 	}
 	
 	/**
 	 * Begins a networked game between two players, given a NetworkGame object.
 	 * @param ng
 	 */
 	public void startNetworkGame(NetworkGame ng) {
 		gameInProgress = true;
 		
 		// controls drawing the opponent's map
 		NetworkGameController networkController = new NetworkGameController(ng);
 		
 		// controls drawing our map
 		localGameController = new GameController();
 		
 		MultiplayerGamePanel gamePanel = new MultiplayerGamePanel(localGameController,
 				networkController, ng, this);
 		
 		localGameController.start();
 		
 		localGameController.setGameMain(gameMain);
 		gameMain.showScreen(gamePanel);
 		gameMain.setSize(1080, 700);
 	}
 
 	/**
 	 * Exits a game in progress and returns the player to the lobby.
 	 */
 	public void quitNetworkGame() {
 		gameInProgress = false;
		localGameController.stop();
 		lobbyManager.quit();
 		lobby.updateGameListPane();
 		gameMain.showScreen(lobby);
 		gameMain.setSize(800, 600);
 	}
 	
 	/**
 	 * Handles the (error-free) end of a network game, and displays the appropriate screen depending
 	 * on the outcome 
 	 * @param won Whether or not we won
 	 */
 	public void networkGameFinished(boolean won) {
 		gameInProgress = false;
 		lobbyManager.resetOpponentConnection();
 		lobbyManager.shutdownServer();
 		
 		gameMain.setSize(800, 600);
 		
 		if (won) {
 			gameMain.showScreen(new WinPanel(this));
 		} else {
 			gameMain.showScreen(new GameOverPanel(gameMain));
 		}
 	}
 
 	/**
 	 * On opponent disconnection, removes opponent from player's "potential opponent slot" 
 	 * if player is waiting to start game, or takes player back to the lobby if opponent
 	 * disconnected during a game.
 	 */
 	public void opponentDisconnected() {
 		if (lobbyManager.getHostedGame() != null) { // we haven't actually started a game yet
 			hostWaitScreen.setPotentialOpponent(null);
 		}
 		
 		if (gameInProgress) {
 			JOptionPane.showMessageDialog(gameMain, 
 					"Your opponent disconnected unexpectedly (they're probably scared of you)", "Disconnection", JOptionPane.ERROR_MESSAGE);
 			quitNetworkGame();
 		}
 		
 		gameInProgress = false;
 		lobbyManager.resetOpponentConnection();
 	}
 	
 	/**
 	 * Handles appropriate operations for a player who discontinues hosting a game,
 	 * removing the game from the pool and returning the former host to the lobby.
 	 */
 	public void stopHostingGame() {
 		gameSetup.reset();
 		lobbyManager.stopHostingGame();
 		lobby.updateGameListPane();
 		gameMain.showScreen(lobby);
 	}
 	
 	/*
 	 * Misc getters/setters
 	 */
 	public void setUsername(String uname) {
 		lobbyManager.setPlayerName(uname);
 	}
 	
 	public LobbyManager getLobbyManager() {
 		return lobbyManager;
 	}
 	
 	public boolean isGameInProgress() {
 		return gameInProgress;
 	}
 	
 	public GameMain getGameMain() {
 		return gameMain;
 	}
 }

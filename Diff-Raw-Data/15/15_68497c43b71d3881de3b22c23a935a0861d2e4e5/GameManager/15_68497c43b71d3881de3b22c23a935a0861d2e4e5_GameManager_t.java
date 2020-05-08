 package essentials.core;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import connectors.players.Player;
 import connectors.players.PlayerServer;
 import connectors.players.PlayerServerLoader;
 import essentials.enums.GameStates;
 import essentials.enums.LetterEnum;
 import essentials.enums.LogLevel;
 import essentials.objects.Brick;
 import essentials.objects.BrickList;
 import essentials.objects.Brickpool;
 import essentials.objects.NetworkBuffer;
 import essentials.objects.Settings;
 
 
 /**
  * Class for managing complete game
  * @author hannes
  *
  */
 public class GameManager {
 
 	/**
 	 * Settings object
 	 */
 	private Settings settings;
 	
 	/**
 	 * Game object
 	 */
 	private Game game;
 	
 	/**
 	 * List of players
 	 */
 	private List<PlayerServer> players;
 	
 	/**
 	 * Gui connector
 	 */
 	private PlayerServer gui; 	
 	private PlayerServerLoader guiLoader;
 	
 	/**
 	 * Server socket
 	 */
 	private ServerSocket serverSocket = null;
 	
 	/**
 	 * Constructor
 	 */
 	public GameManager(){
 		settings = Settings.loadSettings("settings.xml");
 		game = new Game(settings);
 	}
 	
 	/**
 	 * Starts server.
 	 */
 	public void startServer(){
 		
 		ScrabbleServerLogger.log( "> game manager init", LogLevel.INFO );
 		
 		// connect and init all clients and the gui
 		guiLoader = new PlayerServerLoader( settings, settings.getGuiPort() );
 		guiLoader.start();
 		ScrabbleServerLogger.remoteLoader = new PlayerServerLoader( settings, settings.getLogPort() );
 		ScrabbleServerLogger.remoteLoader.start();
 		
 		players = startConnections();		
 		ScrabbleServerLogger.log( "> all clients connected", LogLevel.INFO );
 
 	}
 	
 	/**
 	 * Stop all connections and ends game
 	 */
 	public void stopServer(){
 		
 		try {
 			for( PlayerServer p : players ){
 				p.setPlayerState(GameStates.END_OF_GAME);
 			}	
 		
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {}
 		
 		closeSocket();
 		
 		ScrabbleServerLogger.log(  "> Server stoppped", LogLevel.INFO );
 	}
 	
 	/**
 	 * Counts the number of offline players
 	 * @return
 	 */
 	private int countOfflinePlayers(){
 		int nOfflinePlayers = 0;
 		for( Player p : players ){
 			if( p.getSettings().getPlayerState() == GameStates.OFFLINE
 					|| p.getSettings().getPlayerState() == GameStates.END_OF_GAME )
 				nOfflinePlayers++;
 		}
 		return nOfflinePlayers;
 	}
 	
 	
 	/**
 	 * Starts the game
 	 */
 	public void startGame(){		
 		// send start bricks
 		distributeBricks();
 		
 		// wait until game loader is finished
 		while( !game.isLoadFinished() ){
 			try {
 				Thread.sleep((long) 0.1);
 			} catch (InterruptedException e) {}
 		}
 		
 		ScrabbleServerLogger.log( "> Game started" , LogLevel.INFO );
 		
 		while( settings.getNumOfPasses() < 
 				(settings.getNumOfMaxPassesPerPlayer()*settings.getNumOfPlayers()) ){
 			
 			for( PlayerServer p : players ){	
 				int invalidCount = settings.getNumOfMaxInvalidActionsPerPlayer();
 				long gameTime = System.currentTimeMillis() - game.getStartTime();
 				String playerName = p.getSettings().getPlayerName();
 				
 				// check if all players are offline
 				if( countOfflinePlayers() >= players.size() ){
 					break;
 				}
 				
 				// check if game ends, because player has no bricks and can't get anymore
 				if( (p.getBrickpool().size() == 0)
 						&& (game.getBrickpool().size() == 0) ){
 					break;
 				}
 				
 				// check if player is a human
 				String human = "(human)";
 				if( p.getSettings().getIsHuman() < 1){
 					human = "";
 				}
 				
 				// fill players brickpool, send it and the game map to it
 				ScrabbleServerLogger.log( "> it's " + playerName + human
 									+ " (" + p.getSettings().getScore() + ")" 
 									+ " turn", LogLevel.INFO);
 				
 				fillBricksPool(p);
 				p.sendScrabbleMap( game.getGamemap() );	
 				p.sendBrickpool();
 				p.setGameTime(gameTime);
 				p.resetNetworkBuffer();
 				
 				// send mpa to gui
 				sendGuiMap();	
 				
 				p.setPlayerState( GameStates.SEND_ACTION );
 				waitForResponse(p);
 				p.setPlayerState( GameStates.WAIT );
 
 				// check if action was invalid or to often invalid
 				while( (invalidCount > 0) && (!processCommand(p)) ){
 					ScrabbleServerLogger.log( "> INVALID ACTION from: " + playerName, LogLevel.ERROR );
 					
 					p.resetNetworkBuffer();
 					p.sendScrabbleMap( game.getGamemap() );
 					p.sendBrickpool();
 					p.setGameTime(gameTime);
 					p.setPlayerState( GameStates.INVALID_ACTION );
 					invalidCount--;
 					waitForResponse(p);
 					p.setPlayerState( GameStates.WAIT );
 				}
 				
 				// deactivate player
 				p.setPlayerState( GameStates.WAIT );
 				
 				// send mpa to gui
 				sendGuiMap();				
 				
 				// Wait for next step
 				try {
 					Thread.sleep(settings.getDefaultDelay());
 				} catch (InterruptedException e) {}
 				
 			}
 			
 		}
 		
 		// display scores
 		ScrabbleServerLogger.log( "> Game is over! \n", LogLevel.DEFAULT );
 		ScrabbleServerLogger.log( "------ SCORES ------", LogLevel.DEFAULT );
 		
 		Map<String, Integer> scores = getScores(players);
 		for( String name : scores.keySet() ){
 			ScrabbleServerLogger.log(  String.format( "%-20s|", name ) + ":"
 					+ String.format( "%5d|", scores.get(name) ), LogLevel.DEFAULT );
 		}
 		
 		// wait a second
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {}
 		
 		return;		
 	}
 	
 	/**
 	 * Sends map to gui
 	 */
 	private void sendGuiMap(){
 		// send game map to gui
 		System.out.println(">gui: " + gui);
 		if(gui != null){
 			gui.sendScrabbleMap( game.getGamemap() );
 		}
 		else if( guiLoader.isFinished() ){
 			gui = guiLoader.getReference();
 		}
 	}
 	
 	/**
 	 * Returns a map of players and their scores.
 	 * The map is sorted desc by score.
 	 * @param players
 	 * @return
 	 */
 	private Map<String, Integer> getScores(List<PlayerServer> players){
 		Map<String, Integer> scoreMap = new HashMap<String, Integer>();
 		
 		for( Player p : players ){
 			scoreMap.put( p.getName(), p.getSettings().getScore() );
 		}
 		
 		return scoreMap;
 	}
 	
 	/**
 	 * Distributes first bricks to every player
 	 */
 	private void distributeBricks(){
 		
 		Brickpool brickpool;
 		Brick joker = new Brick(LetterEnum.JOKER, 0);
 		int totalNumOfBricks = settings.getNumOfStartBricksPerPlayer();
 		int numOfJokers = settings.getNumOfJokersPerPlayer();
 		
 		for( PlayerServer p : players ){	
 			brickpool = new Brickpool();
 			
 			// set jokers and random bricks
 			for( int i = 0; i < numOfJokers; i++ ){
 				brickpool.add( joker.clone() );
 			}
 			brickpool.add( game.getBricksFromPool(totalNumOfBricks - numOfJokers) );
 			
 			p.setBrickpool( brickpool );
 		}
 		
 	}
 	
 	/**
 	 * Fills a player bricks pool until it's maximum is reached
 	 * @param aPlayer
 	 * @param aSettings
 	 */
 	private void fillBricksPool(PlayerServer aPlayer) {		
 		while( 
 			(aPlayer.getBrickpool().size() < settings.getNumOfMaxBricksPerPlayer())
 			&& (game.getBrickpool().size() > 0) ){
 			aPlayer.getBrickpool().add( game.getBrickFromPool() );
 		}		
 	}
 	
 	
 	/**
 	 * Stops the socket
 	 */
 	private void closeSocket(){
 		try {
 			serverSocket.close();
 		} catch (IOException e) {
 			ScrabbleServerLogger.log( "Could not close server seocket on port: " + settings.getXmlPort(), LogLevel.ERROR );
 		}
 	}
 	
 	/**
 	 * Starts a connector for the clients
 	 * @throws IOException 
 	 */
 	private List<PlayerServer> startConnections(){		
 		List<PlayerServer> player = new LinkedList<PlayerServer>();
 		int numOfPlayer = this.settings.getNumOfPlayers();
 		
 		ScrabbleServerLogger.log( "> wait for " + Integer.toString(numOfPlayer) 
 				+ " client(s) ...", LogLevel.DEFAULT );
 		
 		
 		try {
 			
 			serverSocket = new ServerSocket( settings.getXmlPort() );
 			serverSocket.setSoTimeout( settings.getServerTimeout() );
 		
 			for( int i = 0; i < numOfPlayer; i++ ){	
 				PlayerServer p = new PlayerServer( serverSocket.accept() );
 				player.add( p );
 				p.start();
 				while( p.getSettings().getPlayerState() != GameStates.CONNECTED ){
 					Thread.sleep(100);
 				}
 				
 				ScrabbleServerLogger.log( "\t" + p.getSettings().getPlayerName() + " connected.", LogLevel.INFO);
 				
 			}
 			
 			Thread.sleep(100);
 				
 		} catch (IOException e) {
 			ScrabbleServerLogger.log( "Error in establishing connection on port: " + settings.getXmlPort(), LogLevel.ERROR );
 	        System.exit(-1);
 		} catch (InterruptedException e) {}	
 		
 		return player;	
 		
 	}
 	
 	
 	/**
 	 * Waits for a command from client
 	 * Returns if command GETBRICK, SETBRICK or NOTHING is recieved.
 	 * @param player Player to wait for
 	 */
 	private void waitForResponse(PlayerServer player){
 		NetworkBuffer nb = player.getNetworkBuffer();
 		boolean response = false;
 		long tend = System.currentTimeMillis() + settings.getTimeoutDelay();
 		int isHuman = player.getSettings().getIsHuman();
 		
 		while( !response ){
 			if( (nb.getChangeBricks().size() > 0) 
 					|| (nb.getNothing())
 					|| (nb.getSetBricks().size() > 0)
 					|| ((System.currentTimeMillis() >= tend) && (isHuman < 1)) ){
 				response = true;
 			}
 			
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {}
 		}		
 	}
 	
 	/**
 	 * Processes command recieved from client
 	 * Looks for commands in the following order. First found command
 	 * will be executed:
 	 * 1. CHANGEBRICKS, 2. SETBRICKS, 3. NOTHING 
 	 * @param player Player
 	 * @return True if processing was successfull, otherwise false
 	 */
 	private boolean processCommand(PlayerServer player){
 		
 		NetworkBuffer nb = player.getNetworkBuffer();
 		Brickpool playerPool = player.getBrickpool();
 		Brickpool gamePool = game.getBrickpool();
 		Settings playerSettings = player.getSettings();
 		boolean ret = false;
 		
 		// COMMAND: change bricks
 		if( (nb.getChangeBricks().size() > 0 && !ret)
 				&& (playerSettings.getNumOfChanges() < playerSettings.getNumOfMaxChangesPerPlayer())
 				&& (playerSettings.getNumOfNoSendActions() < settings.getNumOfMaxNoSendActionsPerPlayer()) ){
			BrickList changeBricks = new BrickList();	
 			
			// check bricklist
			for( Brick b : nb.getChangeBricks().getBricks() ){
				if( b != null && b.getLetter() != null
					&& b.getLetter() != LetterEnum.NULL){
					changeBricks.add( b.clone() );
				}
			}
 			
			if( (changeBricks.size() < settings.getNumOfMaxBricksPerPlayer()) 
					&& (playerPool.containsN(changeBricks))
 					&& (gamePool.size() >= changeBricks.size()) ){
 				
 				BrickList newBricks = gamePool.takeRandomBricks( changeBricks.size() );
 				gamePool.add( changeBricks );
 				playerPool.remove( changeBricks );
 				playerPool.add( newBricks );				
 				
 				player.setBrickpool(playerPool);
 				game.setBrickpool(gamePool);
 				
 				playerSettings.setNumOfChanges( playerSettings.getNumOfChanges() + 1 );	
 				playerSettings.setNumOfNoSendActions( playerSettings.getNumOfNoSendActions() + 1 );
 				settings.setNumOfPasses( 0 );
 				
 				ScrabbleServerLogger.log(  "\tchangeing " + changeBricks.size() + " bricks: " + changeBricks.toWord(), LogLevel.INFO );
 				ScrabbleServerLogger.log( "\tplayer changed  " + player.getSettings().getNumOfChanges() 
 						+ " of " + settings.getNumOfMaxChangesPerPlayer() + " times", LogLevel.INFO );
 				
 				ret = true;
 			}
 			else{
 				ScrabbleServerLogger.log( "\tfailed changing " + changeBricks.toStringWithWeights(), LogLevel.ERROR );
 			}
 		}
 		
 		// COMMAND: set bricks
 		else if( nb.getSetBricks().size() > 0 && !ret ){
 			BrickList setBricks = nb.getSetBricks();
 			
 			if( playerPool.containsN(setBricks) ){
 				List<BrickList> words = game.addBricks( setBricks );
 				
 				// check if new words are created (action was valid)
 				if( words.size() > 0 ){
 					ScrabbleServerLogger.log( "> all words are valid.", LogLevel.DEBUG );
 					
 					// calculate and set score
 					int score = playerSettings.getScore();
 					playerSettings.setScore( 
 							score + BrickList.getScore(words) );
 					playerSettings.setNumOfChanges(0);
 					playerSettings.setNumOfNoSendActions( 0 );
 					
 					// remove bricks from players pool
 					playerPool.remove(setBricks);
 					player.setBrickpool(playerPool);
 					
 					settings.setNumOfPasses( 0 );
 					
 					ScrabbleServerLogger.log(  "\tsetting: " + setBricks.toStringWithWeights()
 							+ " [" + setBricks.getRow() + "][" + setBricks.getColumn() + "] "
 							+ setBricks.getOrientation().name(), LogLevel.INFO );
 
 					ret = true;
 				}
 				else{
 					ScrabbleServerLogger.log( "\tfailed to set " + setBricks.toStringWithWeights()
 							+ " [" + setBricks.getRow() + "][" + setBricks.getColumn() + "] "
 							+ setBricks.getOrientation().name(), LogLevel.ERROR );
 				}
 			}
 			else{
 				ScrabbleServerLogger.log( "\tplayer hasn't all required bricks for setting " + setBricks.toStringWithWeights(), LogLevel.ERROR );
 			}
 		}
 		
 		// COMMAND: nothing
 		else{
 			playerSettings.setNumOfChanges(0);
 			playerSettings.setNumOfNoSendActions( playerSettings.getNumOfNoSendActions() + 1 );
 			settings.setNumOfPasses( settings.getNumOfPasses() + 1 );
 			
 			ScrabbleServerLogger.log( "\tpassing", LogLevel.INFO );
 			ScrabbleServerLogger.log( "\tpassed " + settings.getNumOfPasses()  + " of "
 					+ settings.getNumOfPlayers() * settings.getNumOfMaxPassesPerPlayer() + " times", LogLevel.INFO );
 			
 			ret = true;
 		}
 		
 		// update player settings
 		playerSettings.setNumOfPasses( settings.getNumOfPasses() );
 		player.setSettings(playerSettings);		
 		
 		return ret;
 	}
 	
 }

 package essentials.core;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import connectors.players.Player;
 import connectors.players.PlayerServer;
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
 		
 		ScrabbleLogger.log( "> game manager init", LogLevel.INFO );
 		
 		// connect and init all clients
 		players = startConnections();		
 		ScrabbleLogger.log( "> all clients connected", LogLevel.INFO );
 
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
 		
 		ScrabbleLogger.log(  "> Server stoppped", LogLevel.INFO );
 	}
 	
 	/**
 	 * Starts the game
 	 */
 	public void startGame(){		
 		// send start bricks
 		distributeBricks();
 		
 		while( !game.isLoadFinished() ){
 			try {
 				Thread.sleep((long) 0.1);
 			} catch (InterruptedException e) {}
 		}
 		
 		ScrabbleLogger.log( "> Game started" , LogLevel.INFO );
 		
 		while( settings.getNumOfPasses() < 
 				(settings.getNumOfMaxPassesPerPlayer()*settings.getNumOfPlayers()) ){
 			
 			for( PlayerServer p : players ){	
 				int invalidCount = settings.getNumOfMaxInvalidActionsPerPlayer();
 				long gameTime = System.currentTimeMillis() - game.getStartTime(); 
 				
 				// check if game ends, because player has no bricks and can't get anymore
 				if( (p.getBrickpool().size() == 0)
 						&& (game.getBrickpool().size() == 0) ){
 					return;
 				}
 				
 				// check if player is a human
 				String human = "(human)";
 				if( p.getSettings().getIsHuman() < 1){
 					human = "";
 				}
 				
 				// fill players brickpool, send it and the game map to it
 				ScrabbleLogger.log( "> it's "
 				+ p.getSettings().getPlayerName() + human + " turn", LogLevel.INFO);
 				
 				fillBricksPool(p);
 				p.sendScrabbleMap( game.getScrabbleMap() );	
 				p.sendBrickpool();
 				p.setGameTime(gameTime);
 				p.resetNetworkBuffer();
 				
 				p.setPlayerState( GameStates.SEND_ACTION );
 				waitForResponse(p);
 				p.setPlayerState( GameStates.WAIT );
 				
 				// check if action was invalid or to often invalid
 				while( (invalidCount > 0) && (!processCommand(p)) ){
 					ScrabbleLogger.log( "> INVALID ACTION from: "
 							+ p.getSettings().getPlayerName(), LogLevel.ERROR );
 					
 					p.resetNetworkBuffer();
 					p.setPlayerState( GameStates.INVALID_ACTION );
 					invalidCount--;
 					waitForResponse(p);
 					p.setPlayerState( GameStates.WAIT );
 				}
 				
 				// deactivate player
 				p.setPlayerState( GameStates.WAIT );
 				
 				
 				// Wait for next step
 				try {
 					Thread.sleep(settings.getDefaultDelay());
 				} catch (InterruptedException e) {}
 				
 			}
 			
 		}
 		
 		// display scores
 		ScrabbleLogger.log( "> Game is over! \n", LogLevel.DEFAULT );
 		ScrabbleLogger.log( "------ SCORES ------", LogLevel.DEFAULT );
 		
 		Map<String, Integer> scores = getScores(players);
 		for( String name : scores.keySet() ){
 			ScrabbleLogger.log(  String.format( "%-20s|", name ) + ":"
 					+ String.format( "%5d|", scores.get(name) ), LogLevel.DEFAULT );
 		}
 		
 		return;		
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
 			ScrabbleLogger.log( "Could not close server seocket on port: " + settings.getXmlPort(), LogLevel.ERROR );
 		}
 	}
 	
 	/**
 	 * Starts a connection per server
 	 * @throws IOException 
 	 */
 	private List<PlayerServer> startConnections(){		
 		List<PlayerServer> player = new LinkedList<PlayerServer>();
 		int numOfPlayer = this.settings.getNumOfPlayers();
 		
 		ScrabbleLogger.log( "> wait for " + Integer.toString(numOfPlayer) 
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
 				
 				ScrabbleLogger.log( "\t" + p.getSettings().getPlayerName() + " connected.", LogLevel.INFO);
 				
 			}
 			
 			Thread.sleep(100);
 				
 		} catch (IOException e) {
 			ScrabbleLogger.log( "Error in establishing connection on port: " + settings.getXmlPort(), LogLevel.ERROR );
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
 				&& (playerSettings.getNumOfChanges() < playerSettings.getNumOfMaxChangesPerPlayer()) ){
 			BrickList changeBricks = nb.getChangeBricks();	
 			
 			
 			
 			if( (playerPool.containsN(changeBricks))
 					&& (gamePool.size() >= changeBricks.size()) ){
 				
 				BrickList newBricks = gamePool.takeRandomBricks( changeBricks.size() );
 				gamePool.add( changeBricks );
 				playerPool.remove( changeBricks );
 				playerPool.add( newBricks );				
 				
 				player.setBrickpool(playerPool);
 				game.setBrickpool(gamePool);
 				
 				playerSettings.setNumOfChanges( playerSettings.getNumOfChanges() + 1 );				
 				settings.setNumOfPasses( 0 );
 				
 				ScrabbleLogger.log(  "\tchangeing " + changeBricks.size() + " bricks: " + changeBricks.toWord(), LogLevel.INFO );
 				ScrabbleLogger.log( "\tplayer changed  " + player.getSettings().getNumOfChanges() 
 						+ " of " + settings.getNumOfMaxChangesPerPlayer() + " times", LogLevel.INFO );
 				
 				ret = true;
 			}
 		}
 		
 		// COMMAND: set bricks
 		else if( nb.getSetBricks().size() > 0 && !ret ){
 			BrickList setBricks = nb.getSetBricks();
 			
			ScrabbleLogger.log( "> player has bricks for word "
							+ setBricks.toWord() + ":" + playerPool.containsN(setBricks), LogLevel.DEBUG );
			
 			if( playerPool.containsN(setBricks) ){
 				List<BrickList> words = game.addBricks( setBricks );
 				
 				// check if new words are created (action was valid)
 				if( words.size() > 0 ){
 					ScrabbleLogger.log( "> all words are valid.", LogLevel.DEBUG );
 					
 					// calculate and set score
 					int score = playerSettings.getScore();
 					playerSettings.setScore( 
 							score + BrickList.getScore(words) );
 					playerSettings.setNumOfChanges(0);
 					
 					// remove bricks from players pool
 					playerPool.remove(setBricks);
 					player.setBrickpool(playerPool);
 					
 					settings.setNumOfPasses( 0 );
 					
					ScrabbleLogger.log(  "\tsetting: " + setBricks.toWord()
 							+ " [" + setBricks.getRow() + "][" + setBricks.getColumn() + "] "
 							+ setBricks.getOrientation().name(), LogLevel.INFO );
 
 					ret = true;
 				}
 			}
 			else{
				ScrabbleLogger.log( "\tplayer hasn't all required bricks for setting " + setBricks.toWord(), LogLevel.DEBUG );
 			}
 		}
 		
 		// COMMAND: nothing
 		else if( nb.getNothing() && !ret ){
 			playerSettings.setNumOfChanges(0);
 			settings.setNumOfPasses( settings.getNumOfPasses() + 1 );
 			
 			ScrabbleLogger.log( "\tpassing", LogLevel.INFO );
 			ScrabbleLogger.log( "\tpassed " + settings.getNumOfPasses()  + " of "
 					+ settings.getNumOfPlayers() * settings.getNumOfMaxPassesPerPlayer() + " times", LogLevel.INFO );
 			
 			ret = true;
 		}
 		
 		// update player settings
 		playerSettings.setNumOfPasses( settings.getNumOfPasses() );
 		player.setSettings(playerSettings);		
 		
 		return ret;
 	}
 	
 }

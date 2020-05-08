 package essentials.core;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.util.LinkedList;
 import java.util.List;
 
 import connectors.players.PlayerServer;
 import essentials.enums.GameStates;
 import essentials.enums.LetterEnum;
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
 		
 		System.out.println("> game manager init");
 		
 		// connect and init all clients
 		players = startConnections();		
 		System.out.println("> all clients connected");
 
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
 		
 	}
 	
 	/**
 	 * Starts the game
 	 */
 	public void startGame(){		
 		// send start bricks
 		distributeBricks();
 		
 		while( settings.getNumOfPasses() < 
 				(settings.getNumOfMaxPassesPerPlayer()*settings.getNumOfPlayers()) ){
 			
 			for( PlayerServer p : players ){	
 				int invalidCount = settings.getNumOfMaxInvalidActionsPerPlayer();
 				int maxInvalidCount = invalidCount;
 				
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
 				System.out.println("> it's "
 				+ p.getSettings().getPlayerName() + human + " turn");
 				
 				fillBricksPool(p);
 				p.sendScrabbleMap( game.getScrabbleMap() );	
 				p.sendBrickpool();
 				p.resetNetworkBuffer();
 				
 				p.setPlayerState( GameStates.SEND_ACTION );
				waitForResponse(p);	
 				
 				// check if action was invalid or to often invalid
 				while( (invalidCount > 0) && (!processCommand(p)) ){
 					System.err.println( "> INVALID ACTION No. "
 							+ (invalidCount-maxInvalidCount+1) + " from: "
 							+ p.getSettings().getPlayerName() );
 					
 					p.resetNetworkBuffer();
 					p.setPlayerState( GameStates.INVALID_ACTION );
 					invalidCount--;
 					waitForResponse(p);
 				}
 				
 				// deactivate player
 				p.setPlayerState( GameStates.WAIT );
 				
 				// Wait for next step
 				try {
 					Thread.sleep(settings.getDefaultDelay());
 				} catch (InterruptedException e) {}
 				
 			}
 			
 		}
 		
 		return;		
 	}
 	
 	/**
 	 * Distributes first bricks to every player
 	 */
 	private void distributeBricks(){
 		
 		Brickpool brickpool = new Brickpool();
 		Brick joker = new Brick(LetterEnum.JOKER, 0);
 		int totalNumOfBricks = settings.getNumOfStartBricksPerPlayer();
 		int numOfJokers = settings.getNumOfJokersPerPlayer();
 		
 		for( PlayerServer p : players ){			
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
 			System.err.println( "Could not close server seocket on port: " + settings.getXmlPort() );
 		}
 	}
 	
 	/**
 	 * Starts a connection per server
 	 * @throws IOException 
 	 */
 	private List<PlayerServer> startConnections(){		
 		List<PlayerServer> player = new LinkedList<PlayerServer>();
 		int numOfPlayer = this.settings.getNumOfPlayers();
 		
 		System.out.println( "> wait for " + Integer.toString(numOfPlayer) 
 				+ " client(s) ...");
 		
 		
 		try {
 			
 			serverSocket = new ServerSocket(settings.getXmlPort());
 			serverSocket.setSoTimeout( settings.getServerTimeout() );
 		
 			for( int i = 0; i < numOfPlayer; i++ ){	
 				PlayerServer p = new PlayerServer( serverSocket.accept() );
 				player.add( p );
 				p.start();
 				while( p.getSettings().getPlayerState() != GameStates.CONNECTED ){
 					Thread.sleep(100);
 				}
 			}
 			
 			Thread.sleep(100);
 				
 		} catch (IOException e) {
 			System.err.println( "Error in establishing connection on port: " + settings.getXmlPort() );
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
 		
 		nb.resetNetworkBuffer();
 		
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
 		
 		// COMMAND: change bricks
 		if( nb.getChangeBricks().size() > 0 ){
 			BrickList changeBricks = nb.getChangeBricks();			
 			
 			if( (playerPool.contains(changeBricks))
 					&& (gamePool.size() >= changeBricks.size()) ){
 				
 				BrickList newBricks = gamePool.takeRandomBricks( changeBricks.size() );
 				gamePool.add( changeBricks );
 				playerPool.remove( changeBricks );
 				playerPool.add( newBricks );				
 				
 				player.setBrickpool(playerPool);
 				game.setBrickpool(gamePool);
 				
 				return true;
 			}
 		}
 		
 		// COMMAND: set bricks
 		else if( nb.getSetBricks().size() > 0 ){
 			BrickList bricks = nb.getSetBricks();
 			
 			if( playerPool.contains(bricks) ){
 				List<BrickList> words = game.addBricks( bricks );
 				
 				// check if new words are created (action was valid)
 				if( words.size() > 0 ){
 					
 					// calculate and set score
 					Settings playerSettings = player.getSettings();
 					int score = playerSettings.getScore();
 					playerSettings.setScore( 
 							score + BrickList.getScore(words) );
 					player.setSettings(playerSettings);
 					
 					// remove bricks from players pool
 					playerPool.remove(bricks);
 					player.setBrickpool(playerPool);
 					
 					return true;
 				}
 			}
 		}
 		
 		// COMMAND: nothing
 		else if( nb.getNothing() ){
 			settings.setNumOfPasses( settings.getNumOfPasses() + 1 );
 			return true;
 		}
 		
 		return false;
 	}
 	
 }

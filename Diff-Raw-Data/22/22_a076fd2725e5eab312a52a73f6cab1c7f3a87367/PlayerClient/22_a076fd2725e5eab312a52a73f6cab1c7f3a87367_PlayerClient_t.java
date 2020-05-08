 package connectors.players;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import connectors.commands.Command;
 
 import essentials.core.ScrabbleLogger;
 import essentials.enums.CommandEnum;
 import essentials.enums.LogLevel;
 import essentials.objects.BrickList;
 import essentials.objects.Settings;
 
 public class PlayerClient extends Player{	
 	
 	/**
 	 * Constructor
 	 * @param aPlayerName
 	 * @param isHuman >0 if player is a human
 	 */
 	public PlayerClient(int isHuman){
 		super();
 		settings.setIsHuman(isHuman);
 	}
 	
 	/**
 	 * Constructor
 	 * Creates client as AI
 	 */
 	public PlayerClient(){
 		this( 0 );
 	}
 	
 	/**
 	 * Called if thread starts
 	 */
 	public void run() {
 		
 		if( connect() ){			
 			initConnection();
 			readCommands();
 		}
 		else{
 			ScrabbleLogger.log( "Could not connect to server: " 
 					+ settings.getServerIP() + " on port " + settings.getXmlPort(), LogLevel.ERROR );
 		}
 		
 	}
 	
 	/**
 	 * Connects a socket
 	 * @return
 	 */
 	protected boolean connect(){
 		try {
 			socket = new Socket( settings.getServerIP(), settings.getXmlPort() );
 		} catch (UnknownHostException e) {
 			ScrabbleLogger.log( "Could not connect to server: " + settings.getServerIP(), LogLevel.ERROR );
 			return false;
 		} catch (IOException e) {
 			ScrabbleLogger.log( "IO-Error in creating new socket.\n" + e.getMessage(), LogLevel.ERROR );
 			return false;
 		}
 		
 		return createStreams();
 	}
 	
 	/**
 	 * Initiates connection
 	 */
 	protected void initConnection(){
 		sendSettings();
 		
 		waitForCommand(CommandEnum.SENDSETTINGS);
 		saveRecievedSettings( (Settings) read(Settings.class) )	;
 	}
 	
 	/**
 	 * Save recieved settings
 	 * @param aSettings New Settings
 	 */
 	@Override
 	public void saveRecievedSettings(Settings aSettings){
 		settings.setNumOfPlayers( aSettings.getNumOfPlayers() );
 		settings.setMapRows( aSettings.getMapRows() );
 		settings.setMapColumns( aSettings.getMapColumns() );
 		settings.setTimeoutDelay( aSettings.getTimeoutDelay() );
 		settings.setNumOfPasses( aSettings.getNumOfPasses() );
 		settings.setNumOfMaxPassesPerPlayer( aSettings.getNumOfMaxPassesPerPlayer() );
 		settings.setNumOfMaxInvalidActionsPerPlayer( aSettings.getNumOfMaxInvalidActionsPerPlayer() );
 		settings.setNumOfJokersPerPlayer( aSettings.getNumOfJokersPerPlayer() );
 		settings.setNumOfStartBricksPerPlayer( aSettings.getNumOfStartBricksPerPlayer() );
 		settings.setNumOfMaxBricksPerPlayer( aSettings.getNumOfMaxBricksPerPlayer() );
 		settings.setNumOfChanges( aSettings.getNumOfChanges() );
 		settings.setNumOfMaxChangesPerPlayer( aSettings.getNumOfMaxChangesPerPlayer() );
 		settings.setScore( aSettings.getScore() );
 		settings.setGameTime( aSettings.getGameTime() );		
 		settings.setPlayerState( aSettings.getPlayerState() );
 	}
 	
 	/**
 	 * Send command to set a list of bricks
 	 * @param aBrickList List of bricklists to set
 	 */
 	public void actionSetBricks(BrickList aBrickList){
 		write( new Command(CommandEnum.SETBRICKS) );
 		write( aBrickList );
 	}
 	
 	/**
 	 * Send pass command
 	 */
 	public void actionPass(){
 		write( new Command(CommandEnum.NOTHING) );
 	}
 	
 	/**
 	 * Sends command to get a new brick
 	 */
 	public void actionChangeBricks(BrickList aBrickList){
		if( aBrickList.size() > 0 ){		
			write( new Command(CommandEnum.CHANGEBRICKS) );
			write( aBrickList );
		}
 	}
 	
 }

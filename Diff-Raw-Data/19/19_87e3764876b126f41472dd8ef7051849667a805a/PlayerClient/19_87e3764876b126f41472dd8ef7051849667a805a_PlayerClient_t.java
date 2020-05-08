 package connectors.players;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import connectors.commands.Command;
 
 import essentials.enums.CommandEnum;
 import essentials.enums.GameStates;
 import essentials.enums.LetterEnum;
 import essentials.objects.Brick;
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
 			System.err.println( "Could not connect to server: " 
 					+ settings.getServerIP() + " on port " + settings.getXmlPort() );
 		}
 		
 	}
 	
 	/**
 	 * Connects a socket
 	 * @return
 	 */
 	private boolean connect(){
 		try {
 			socket = new Socket( settings.getServerIP(), 5588 );
 		} catch (UnknownHostException e) {
 			System.out.println( "Could not connect to server: " + settings.getServerIP() );
 			return false;
 		} catch (IOException e) {
 			System.out.println( "IO-Error in creating new socket.\n" + e.getMessage() );
 			return false;
 		}
 		
 		return createStreams();
 	}
 	
 	/**
 	 * Initiates connection
 	 */
 	private void initConnection(){
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
 		settings.setPlayerState( aSettings.getPlayerState() );
 		settings.setNumOfPlayers( aSettings.getNumOfPlayers() );
 		settings.setMapColumns( aSettings.getMapColumns() );
 		settings.setMapRows( aSettings.getMapRows() );
 		settings.setScore( aSettings.getScore() );
 		
 		if( settings.getPlayerState() == GameStates.END_OF_GAME ){
 			System.out.println("> game is over");
 		}
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
 		if( aBrickList.size() < 1 ){
 			Brick b = new Brick( LetterEnum.NULL, 0 );
 			aBrickList.add( b );
 		}			
 		write( new Command(CommandEnum.CHANGEBRICKS) );
 		write( aBrickList );
 	}
 	
 }

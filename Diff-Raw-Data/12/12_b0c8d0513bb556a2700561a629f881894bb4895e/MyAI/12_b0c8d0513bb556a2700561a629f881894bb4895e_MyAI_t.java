 package ai;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import connectors.players.PlayerClient;
 import essentials.enums.GameStates;
 import essentials.enums.OrientationEnum;
 import essentials.objects.BrickList;
 import essentials.objects.Brickpool;
 import essentials.objects.NetworkBuffer;
 import essentials.objects.Settings;
 
 public class MyAI {
 	
 	PlayerClient client;
 	
 	List<GameStates> stateListConnected = new ArrayList<GameStates>();
 	List<GameStates> stateListActionResponse = new ArrayList<GameStates>();
 	List<GameStates> stateListWaitForAction = new ArrayList<GameStates>();
 	
 	/**
 	 * Constructor
 	 * @param playername
 	 */
 	public MyAI(String playername) {	
 		initStateLists();
 		client = new PlayerClient( playername, 1 );
 		client.start();
 		client.waitForStates(stateListConnected);
 		
 		System.out.println( "> AI initiated and connected." );
 	}
 	
 	/**
 	 * Initiates states lists
 	 */
 	private void initStateLists(){
 		stateListConnected.add(GameStates.CONNECTED);
 		stateListConnected.add(GameStates.SEND_ACTION);
 		stateListConnected.add(GameStates.WAIT);
 		stateListConnected.add(GameStates.INVALID_ACTION);
 		
 		stateListActionResponse.add(GameStates.WAIT);
		//stateListActionResponse.add(GameStates.INVALID_ACTION);
 		stateListActionResponse.add(GameStates.END_OF_GAME);
 		
 		stateListWaitForAction.add(GameStates.SEND_ACTION);
 		stateListWaitForAction.add(GameStates.INVALID_ACTION);
 	}
 	
 	/**
 	 * Starts interactive AI
 	 */
 	public void start(){
 		
 		System.err.println( "HINT: use a . before a character that's already on the map." );
 		
 		while( client.getSettings().getPlayerState() != GameStates.END_OF_GAME ){
 			
 			// Wait until it's my turn
 			client.waitForStates(stateListWaitForAction);
 			
 			// print data and get user action
 			printCurrentStatus();
 			switch (MyAI.readUserAction()) {
 			case 1:
 				// change bricks
 				client.actionChangeBricks( readChangeBricks() );
 				break;
 				
 			case 2:
 				// set bricks
 				client.actionSetBricks( readSetBricks() );
 				break;
 				
 			case 4:
 				// exit game
 				return;
 
 			default:
 				// pass
 				client.actionPass();
 				break;
 			}
 			
 			// Wait until my turn is over
 			client.waitForStates( stateListActionResponse );
 			
 			
 		}
 
 	}
 	
 	/**
 	 * Read bricks to change from user
 	 * @return Bricklist
 	 */
 	private BrickList readChangeBricks(){
 		String bricks = MyAI.readFromUsr( "bricks=" );
 		Brickpool pool = client.getNetworkBuffer().getBrickpool();
 		return pool.toValidBricks( bricks );
 	}
 	
 	/**
 	 * Reads words to set from user
 	 * @return List of bricklists
 	 */
 	private BrickList readSetBricks(){
 		Brickpool pool = client.getNetworkBuffer().getBrickpool();
 		
 		String word = MyAI.readFromUsr( "word=" );
 		int row = MyAI.readFromUsrInt( "row=" );
 		int col = MyAI.readFromUsrInt( "col=" );
 		int rorientation = MyAI.readFromUsrInt( "orientation [0: horizontal, 1: vertical]=" );
 		OrientationEnum orientation = OrientationEnum.HORIZONTAL;
 		if( rorientation > 0){
 			orientation= OrientationEnum.VERTICAL;
 		}
 			
 		BrickList bricklist = pool.toValidBricks(word);
 		bricklist.setPosition(row, col, orientation);
 		
 		return bricklist;
 	}
 	
 	
 	/**
 	 * Things to do after game is over
 	 */
 	public void stop(){
 		// stop client thread
 		Settings settings = client.getSettings();
 		settings.setPlayerState( GameStates.END_OF_GAME );
 		client.setSettings(settings);
 		
 		System.out.println( "> Game is over!" );
 	}
 	
 	
 	/**
 	 * Prints current map and status data
 	 */
 	private void printCurrentStatus(){
 		NetworkBuffer nb = client.getNetworkBuffer();
 		Settings settings = client.getSettings();
 		
 		System.out.println( "----------------------" );
 		System.out.println( nb.getMap() );
 		System.out.println( "Status: " + settings.getPlayerState()
 							+ " Score: " + settings.getScore() );
 		System.out.println( "Brickpool: " + nb.getBrickpool() );
 	}
 	
 	/**
 	 * Reads a string from command line
 	 * @param msg Message to display
 	 * @return
 	 * @throws IOException 
 	 */
 	static private String readFromUsr( String msg ){
 		BufferedReader stdin = new BufferedReader
 			      (new InputStreamReader(System.in));		
 		System.out.print( msg );
 		System.out.flush();		
 		try {
 			return stdin.readLine();
 		} catch (IOException e) {
 			System.err.println( "Could not read from command line" );
 		}
 		return "0";
 	}
 	
 	/**
 	 * Reads number from command line
 	 * @param msg
 	 * @return
 	 */
 	static private int readFromUsrInt( String msg ){
 		try {
 			String in = MyAI.readFromUsr( msg );
 			return Integer.parseInt(in);
 		} catch (NumberFormatException e){
 			System.err.println( "Enter a numer!" );
 		}
 		return 0;
 	}
 	
 	
 	/**
 	 * @return Selection of user from menue
 	 */
 	static private int readUserAction(){		
 		System.out.println( "What do you want do to?" );
 		System.out.println( "1: Change bricks\n" +
 							"2: Set bricks\n" +
 							"3: Pass\n" +
 							"4: Exit game" );
 		return MyAI.readFromUsrInt( "Your choise = " );
 	}
 	
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {		
 		
 		System.out.println( "Starting interactive AI client ..." );
 		MyAI ai = new MyAI( "Ai Doe" );
 		ai.start();
 		ai.stop();
 		System.out.println( "AI stopped." );
 		
 	}
 
 }

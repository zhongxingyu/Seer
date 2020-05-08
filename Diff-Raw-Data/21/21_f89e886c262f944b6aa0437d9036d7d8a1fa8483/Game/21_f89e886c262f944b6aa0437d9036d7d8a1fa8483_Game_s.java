 package game;
 
 /**
  * @author Hung <hnl5010@psu.edu>
  * @author Endrit <eqa5029@psu.edu>
  * @author max <maxdeliso@gmail.com>
  *
  */
 
 public class Game {
 	static final int FRAME_DELAY = 15;
 	static final int NUM_BUFFERS = 2;
 	static final int ASTEROID_CUTOFF_COUNT = 15;
 	static final int PLAYER_BULLET_CUTOFF_COUNT = 30;
 	static final boolean DEBUG = true;
 	static final boolean DEBUG_ALL = false; /* to disable spamming stdout */
 	
 	/**
 	 * Entry point
 	 * @param args command line arguments
 	 */
 	public static void main(String[] args) {
 		log("starting up with " + args.length + " argument(s)");
 		
 		/* create and initialize most of the game objects */
 		GameDatabaseConnector gameDatabaseConnector = new GameDatabaseConnector();
		//TODO: make the database connection asynchronous so we don't have to wait
		//TODO: make the connection failures graceful
 		GameCanvas gameCanvas = new GameCanvas();
 		GameThread gameThread = new GameThread( gameCanvas, gameDatabaseConnector );
 		GameState gameState = new GameState( gameThread );
 		GameFrameListener gameFrameListener = new GameFrameListener( gameThread );
 		GameKeyListener gameKeyListener = new GameKeyListener( gameState );
 		GameMouseListener gameMouseListener = new GameMouseListener( gameState);
 		GameFrame gameFrame = new GameFrame(gameCanvas, gameFrameListener, gameKeyListener, gameMouseListener );
		//TODO: handle canvas resizes better by applying affine transforms to make the game objects scale with the frame 
 		gameThread.setMainFrame(gameFrame);
 		gameThread.setGameState(gameState );
 		log("created game objects OK");
 		
 		/* start the gui thread and game thread, note: the game thread enters a wait state after initialization */
 		new Thread( gameDatabaseConnector).start();
 		new Thread( gameThread ).start();
 		gameFrame.setVisible(true);
 		log("game threads started OK");
 		
 		/* give focus to the canvas and start input processing */
 		gameCanvas.createBufferStrategy(NUM_BUFFERS);
 		gameCanvas.requestFocus();
 		gameState.acceptInput();
 		log("accepting i/o");
 		
 		/* wait for the game thread to initialize */
 		while( !gameThread.isWaiting() )
 		{
 			try {
 				Thread.sleep(10);
 			} catch (InterruptedException e) {
 				Game.logError("interrupted while waiting for game thread to enter wait state: " + e);
 			}
 		}
 		
 		/* tell the game thread it's OK to proceed into the main game loop ( this code fixed race condition ) */
 		gameThread.relinquishMonitor();
 	}
 	
 	/**
 	 * This function is used to print out debug messages which can be disabled by setting 
 	 * a variable in this class.
 	 * @param msg The message to print
 	 */
 	static public void log(String msg)
 	{
 		if( Game.DEBUG )
 		{
 			System.out.println( System.currentTimeMillis() + ": DEBUG : " + msg );
 		}
 	}
 	
 	/**
 	 * This function is used to print out debug messages which are very frequent.
 	 * These messages are disabled by default.
 	 * @param msg The message to print
 	 */
 	static public void logFrequent(String msg)
 	{
 		if( Game.DEBUG_ALL )
 		{
 			System.out.println( System.currentTimeMillis() + ": DEBUG : " + msg );
 		}
 	}
 	
 	/**
 	 * This function is used to print out error messages. Error messages cannot be ignored.
 	 * @param msg The message to print
 	 */
 	static public void logError(String msg)
 	{
 		System.err.println(System.currentTimeMillis() + ": ERROR : " + msg);
 	}
 }

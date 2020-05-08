 package game;
 
 /**
  * @author Hung <hnl5010@psu.edu>
  * @author Endrit <eqa5029@psu.edu>
  * @author max <maxdeliso@gmail.com>
  *
  */
 
 public class Game {
 	static final int FRAME_DELAY = 30;
 	static final boolean DEBUG = true;
 	
 	/**
 	 * Entry point
 	 * @param args command line arguments
 	 */
 	public static void main(String[] args) {
 		log("starting up with " + args.length + " argument(s)");
 		
 		/* create and initialize most of the game objects */
 		GameCanvas gameCanvas = new GameCanvas();
 		GameThread gameThread = new GameThread( gameCanvas );
 		GameFrameListener gameFrameListener = new GameFrameListener( gameThread );
 		GameKeyListener gameKeyListener = new GameKeyListener( gameThread );
 		GameMouseListener gameMouseListener = new GameMouseListener( gameThread );
 		GameFrame gameFrame = new GameFrame(gameCanvas, gameFrameListener, gameKeyListener, gameMouseListener );
 		gameThread.setMainFrame(gameFrame);
 		log("created game objects OK");
 		
 		/* start the gui thread and game thread, note: the game thread enters a wait state after initialization */
 		new Thread( gameThread ).start();
 		gameFrame.setVisible(true);
 		log("game threads started OK");
 		
 		/* give focus to the canvas and start input processing */
 		gameCanvas.createBufferStrategy(2);
 		gameCanvas.requestFocus();
 		gameThread.acceptInput();
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
 	 * This function is used to print out error messages. Error messages cannot be ignored.
 	 * @param msg The message to print
 	 */
 	static public void logError(String msg)
 	{
 		System.err.println(System.currentTimeMillis() + ": ERROR : " + msg);
 	}
 }

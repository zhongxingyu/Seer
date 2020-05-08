 package game;
 
 /**
  * @author Hung <hnl5010@psu.edu>
  * @author Endrit <eqa5029@psu.edu>
  * @author max <maxdeliso@gmail.com>
  *
  */
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferStrategy;
 
 public class GameThread implements Runnable {
 	/* the various input operations possible */
 	public enum GameInputOperation {
 		MOUSE_MOVED_LEFT,
 		MOUSE_MOVED_RIGHT,
 		MOUSE_MOVED_UP,
 		MOUSE_MOVED_DOWN,
 		MOUSE_CLICK_LEFT,
 		MOUSE_CLICK_RIGHT,
 		KEYBOARD_ENTER_DOWN,
 		KEYBOARD_BACKSPACE_DOWN,
 		KEYBOARD_W_DOWN,
 		KEYBOARD_A_DOWN,
 		KEYBOARD_S_DOWN,
 		KEYBOARD_D_DOWN,
 		KEYBOARD_W_UP,
 		KEYBOARD_A_UP,
 		KEYBOARD_S_UP,
 		KEYBOARD_D_UP,
 		KEYBOARD_SPACE_DOWN,
 		KEYBOARD_SPACE_UP,
 		KEYBOARD_P_DOWN,
 		KEYBOARD_P_UP,
 		KEYBOARD_F_DOWN,
 		KEYBOARD_F_UP,
 		KEYBOARD_ESC_DOWN,
 		KEYBOARD_A_TYPED,
 		KEYBOARD_B_TYPED,
 		KEYBOARD_C_TYPED,
 		KEYBOARD_D_TYPED,
 		KEYBOARD_E_TYPED,
 		KEYBOARD_F_TYPED,
 		KEYBOARD_G_TYPED,
 		KEYBOARD_H_TYPED,
 		KEYBOARD_I_TYPED,
 		KEYBOARD_J_TYPED,
 		KEYBOARD_K_TYPED,
 		KEYBOARD_L_TYPED,
 		KEYBOARD_M_TYPED,
 		KEYBOARD_N_TYPED,
 		KEYBOARD_O_TYPED,
 		KEYBOARD_P_TYPED,
 		KEYBOARD_Q_TYPED,
 		KEYBOARD_R_TYPED,
 		KEYBOARD_S_TYPED,
 		KEYBOARD_T_TYPED,
 		KEYBOARD_U_TYPED,
 		KEYBOARD_V_TYPED,
 		KEYBOARD_W_TYPED,
 		KEYBOARD_X_TYPED,
 		KEYBOARD_Y_TYPED,
 		KEYBOARD_Z_TYPED
 	}
 
 	/* private member variables */
 	private GameCanvas gameCanvas;
 	private GameState gameState;
 	private GameDatabaseConnector gameDatabaseConnector;
 	private BufferStrategy bufferStrategy;
 	private long cycleTime;
 	private GameFrame gameFrame;
 	private long framesPerSecond;
 	private GameState.HighScorePair[] highScorePairs;
 	private double gfxScaleX;
 	private double gfxScaleY;
 	
 	/**
 	 * This function constructs a new game thread.
 	 * One one game thread is needed per instance of the game, but additional
 	 * threads are created via the timer.
 	 * @param gameCanvas
 	 */
 	public GameThread(GameCanvas gameCanvas, GameDatabaseConnector gameDatabaseConnector)
 	{
 		Game.log("initializing game thread");
 		this.gameCanvas = gameCanvas;
 		this.gameDatabaseConnector = gameDatabaseConnector;
 		gameFrame = null;
 		gameState = null;
 		gfxScaleX = 1.0;
 		gfxScaleY = 1.0;
 	}
 
 	/**
 	 * This function gives the game thread a reference to the game frame.
 	 * Note: It must be called before the main loop is entered.
 	 * @param gameFrame
 	 */
 	public void setMainFrame( GameFrame gameFrame )
 	{
 		this.gameFrame = gameFrame;
 	}
 
 	public void setHighScorePairs( GameState.HighScorePair[] highScorePairs )
 	{
 		this.highScorePairs = highScorePairs;
 	}
 	
 	public GameFrame getFrame()
 	{
 		return gameFrame;
 	}
 
 	public GameCanvas getCanvas()
 	{
 		return gameCanvas;
 	}
 
 	public GameDatabaseConnector getGameDatabaseConnector()
 	{
 		return gameDatabaseConnector;
 	}
 	/**
 	 * This function tells the game thread to enter into a wait state and suspend 
 	 * execution until such time as the frame initialization is complete.
 	 * It is used to delay the execution of the main game loop to avoid a race condition.
 	 */
 	public synchronized void waitForNotify()
 	{
 		try {
 			gameState.setWaiting(true);
 			wait();
 		} catch (InterruptedException e) {
 			Game.logError("synchronization error");
 		}
 	}
 
 	/**
 	 * This function releases the lock that the wait function entered into.
 	 * It is called when the rest of the initialization finishes.
 	 */
 	public synchronized void relinquishMonitor()
 	{
 		notify();
 		gameState.setWaiting(false);
 	}
 
 	public synchronized boolean isWaiting()
 	{
 		return gameState.isWaiting();
 	}
 
 	public void notifyResize()
 	{
 		Dimension newDimension = gameCanvas.getSize();
 		
 		gfxScaleX = (double) newDimension.width / GameCanvas.defaultDimension.width;
 		gfxScaleY = (double) newDimension.height / GameCanvas.defaultDimension.height;
 		
 		Game.log("caught resize event, scaling graphics to " + gfxScaleX + "," + gfxScaleY);
 		
		if( gameState != null )
		{
			gameState.pauseGame();
		}
 	}
 	
 	/**
 	 * Main game loop
 	 */
 	public void run()
 	{
 		long startTime, renderTime;
 
 		Game.log("entering wait state");
 		waitForNotify();
 
 		Game.log("entering main game loop");
 		cycleTime = System.currentTimeMillis();
 		bufferStrategy = gameCanvas.getBufferStrategy();
 		gameState.setRunning(true);
 
 		if( gameFrame == null )
 		{
 			Game.logError("Please set the game frame before invoking the run method");
 			System.exit(1);
 		}
 
 		while( gameState.isRunning() )
 		{
 			startTime =  System.currentTimeMillis();
 
 			gameState.updateGameState();
 			updateGraphics();
 			synchFramerate();
 
 			renderTime = System.currentTimeMillis() - startTime;
 
 			if( renderTime > 0 )
 			{
 				framesPerSecond = (long ) ( 1000.0 / renderTime );
 			}
 			Game.logFrequent("approx FPS: " + framesPerSecond);
 		}
 
 		Game.log("shutting down...");
 		gameCanvas.setVisible(false);
 		gameState.ignoreInput();
 		System.exit(0);
 	}
 
 	/**
 	 * This function gets called every frame to draw whatever needs to be
 	 * displayed. Different things get drawn depending on the game state.
 	 * All this function does is attempt to acquire the draw graphics
 	 * and then call render if it succeeds. Failures are reported
 	 * gracefully.
 	 */
 	private void updateGraphics() {
 		if( bufferStrategy != null && gameState.isRunning() )
 		{
 			Graphics2D g = null;
 
 			try {
 				g = (Graphics2D) bufferStrategy.getDrawGraphics();
 				g.setFont( new Font("Arial", Font.BOLD, 32 ));
 				//TODO: move the font code into an initializing block
 				render(g);
 			} catch (Exception e ) {
 				Game.logError("Could not acquire graphics: " + e);
 			} finally {
 				g.dispose();
 			}
 
 			bufferStrategy.show();
 		}
 	}
 
 
 	/**
 	 * This function takes the graphics context which the updateGraphics()
 	 * function acquired, and draw to it.
 	 * @param g the graphics context
 	 */
 	private void render( Graphics2D g )
 	{
 		FontMetrics fontMetrics = g.getFontMetrics();
 		final String startScreenMessage = "Asteroids";
 		final String pauseScreenMessage = "(P)aused";
 		final String highScoreScreenMessage = "High Scores";
 		final String initialsScreenMessage = "Enter your initials: ";
 		final int messageHeight  = GameCanvas.defaultDimension.height/2 - fontMetrics.getHeight() /2;
 		final int marginOffset = 15;
 		final int topOffset = 32;
 
 		g.transform( AffineTransform.getScaleInstance(gfxScaleX, gfxScaleY));
 
 		g.setColor(Color.BLACK);
 		g.fillRect(0, 0, GameCanvas.defaultDimension.width, GameCanvas.defaultDimension.height);
 		
 		g.setColor(Color.GRAY);
 		g.drawString( framesPerSecond + " FPS", marginOffset, topOffset );
 
 		switch( gameState.getState() )
 		{
 		case START:
 			g.setColor(Color.GRAY);
 			g.drawString( 
 					startScreenMessage, 
 					
 					GameCanvas.defaultDimension.width/2 - 
 					fontMetrics.charsWidth(startScreenMessage.toCharArray(), 0, startScreenMessage.length())/2, 
 					messageHeight );
 			break;
 		case PAUSED:
 			g.setColor(Color.GRAY);
 			g.drawString( 
 					pauseScreenMessage, 
 					GameCanvas.defaultDimension.width/2 - 
 					fontMetrics.charsWidth(pauseScreenMessage.toCharArray(), 0,pauseScreenMessage.length())/2, 
 					messageHeight );
 		case PLAYING:
 			gameState.drawObjects(g);
 
 			if( gameState.getScore() != null )
 			{
 				gameState.getScore().draw(g);
 			}
 
 
 			break;
 		case HIGH_SCORE:
 			g.setColor(Color.GRAY);
 			g.drawString( 
 					highScoreScreenMessage, 
 					GameCanvas.defaultDimension.width/2 - 
 					fontMetrics.charsWidth(highScoreScreenMessage.toCharArray(), 0, highScoreScreenMessage.length())/2, 
 					topOffset );
 
 
 			switch( gameState.getHighScoreState() )
 			{
 			case GETTING_INITIALS:
 				String initials = gameState.getInitialsString();
 
 				g.drawString( 
 						initialsScreenMessage, 
 						GameCanvas.defaultDimension.width/2 - 
 						fontMetrics.charsWidth(initialsScreenMessage.toCharArray(), 0, initialsScreenMessage.length())/2 - 64, 
 						topOffset*4 );
 
 				g.drawString( 
 						initials, 
 						GameCanvas.defaultDimension.width/2 - fontMetrics.charsWidth(initials.toCharArray(), 0, initials.length() )/2 , 
 						topOffset*6);
 
 				break;
 			case DISPLAYING_TABLE:
 
 				
 				
 				String currentLine;
 				for( int j = 0; j < highScorePairs.length; j++ )
 				{
 					currentLine = highScorePairs[j].getInitials() + " -> " + highScorePairs[j].getScore();
 
 					g.drawString( 
 							currentLine, 
 							15, 
 							topOffset* (4 + 2*j ));
 				}	
 
 
 				break;
 			}
 			break;
 		}
 
 
 	}
 
 
 	/**
 	 * This function flips the main loop flag to false
 	 * so that the next time the main loop executes it will
 	 * fall through and perform cleanup operations. Note
 	 * that this does not immediately terminate the game.
 	 */
 	public synchronized void postGameShutdown()
 	{
 		Game.log("received shutdown request");
 		gameState.ignoreInput();
 		gameFrame.destroy();
 		gameState.killTimer();
 		gameState.setRunning(false);
 	}
 
 	/**
 	 * This function prevents the updates from occurring too frequently.
 	 */
 	private void synchFramerate() {
 		cycleTime = cycleTime + Game.FRAME_DELAY;
 		long difference = cycleTime - System.currentTimeMillis();
 
 		try {
 			Thread.sleep(Math.max(0, difference));
 		}
 		catch(InterruptedException e) {
 			Game.logError("game thread interrupted whilst attempting to synch framerate");
 		}
 	}
 
 	public void setGameState(GameState gameState) {
 		this.gameState = gameState;
 	}
 
 	public GameState getGameState() {
 		return gameState;
 	}
 	
 
 	
 }

 package game;
 
 /**
  * @author Hung <hnl5010@psu.edu>
  * @author Endrit <eqa5029@psu.edu>
  * @author max <maxdeliso@gmail.com>
  *
  */
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.image.BufferStrategy;
 import java.util.concurrent.LinkedBlockingDeque;
 
 public class GameThread implements Runnable {
 	/* the game state enumeration */
 	public enum GameState {
 		START,
 		PLAYING,
 		HIGH_SCORE,
 		PAUSED;
 	}
 
 	/* private member variables */
 	private GameCanvas gameCanvas;
 	private BufferStrategy bufferStrategy;
 	private boolean isRunning;
 	private boolean listeningForInput;
 	private long cycleTime;
 	private GameState gameState;
 	private GameTimer gameTimer;
 	private GameFrame gameFrame;
 	private boolean isShowing;
 	private Point mousePosition;
 	
 	/* the data structure for passing input between swing and the game thread*/
 	private LinkedBlockingDeque<GameInputOperation> blockingInputQueue;
 
 	/**
 	 * This function constructs a new game thread.
 	 * One one game thread is needed per instance of the game, but additional
 	 * threads are created via the timer.
 	 * @param gameCanvas
 	 */
 	public GameThread(GameCanvas gameCanvas)
 	{
 		Game.log("initializing game thread");
 		blockingInputQueue = new LinkedBlockingDeque<GameInputOperation>();
 
 		this.gameCanvas = gameCanvas;
 		isRunning = false;
 		listeningForInput = false;
 
 		gameState = GameState.START;
 		gameFrame = null;
 		isShowing = true;
 		mousePosition = null;
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
 
 	/**
 	 * This function adds a new input operation onto the tail of the input queue.
 	 * @param gio the game input operation which we want to enqueue into the blocking input queue
 	 */
 	public void enqueueInputOperation( GameInputOperation gio )
 	{
 		if( listeningForInput )
 		{
 			blockingInputQueue.add(gio);
 		}
 	}
 
 	/**
 	 * This function gets called by the mouse listener to update the mouse position on the game canvas.
 	 * Whenever this function gets called, a mouse moved event gets sent to the operation queue
 	 * so that the new value is processed.
 	 * @param mp the new mouse position.
 	 */
 	public void updateMousePosition( Point mp )
 	{
 		mousePosition = mp;
 	}
 	
 	/**
 	 * Main game loop
 	 */
 	public void run()
 	{
 		Game.log("entering main game loop");
 		cycleTime = System.currentTimeMillis();
 		bufferStrategy = gameCanvas.getBufferStrategy();
 		isRunning = true;
 
 		if( gameFrame == null )
 		{
 			Game.logError("Please set the game frame before invoking the run method");
 			System.exit(1);
 		}
 
 		while( isRunning )
 		{
 			updateGameState();
 			updateGraphics();
 			synchFramerate();
 		}
 
 		Game.log("shutting down...");
 		gameCanvas.setVisible(false);
 		ignoreInput();
 		System.exit(0);
 	}
 
 	/**
 	 * This function tells the input queue to accept new items.
 	 */
 	public synchronized void acceptInput()
 	{
 		Game.log("now accepting input");
 		listeningForInput = true;
 	}
 
 	/**
 	 * This function tells the input queue to ignore any new items.
 	 */
 	public synchronized void ignoreInput()
 	{
 		Game.log("now ignoring input");
 		listeningForInput = false;
 	}
 
 	/**
 	 * This function is called by the frame listener when the frame, and therefore
 	 * the canvas, is resized. Some game objects may be invalidated or need 
 	 * repositioning after this event, and those changes are handled here.
 	 */
 	public synchronized void notifyResize()
 	{
 		Game.log("caught resize notification: " + gameCanvas.getSize());
 		//TODO: wrap game objects outside of new boundaries
 	}
 
 	public void spawnEnemy()
 	{
 		Game.log("Timer event SPAWN_ENEMIES");
 	}
 
 	public void spawnAsteroid()
 	{
 		Game.log("Timer event SPAWN_ASTEROIDS");
 	}
 
 	public void spawnPowerup()
 	{
 		Game.log("Timer event SPAWN_POWERUP");
 	}
 
 	/**
 	 * updateGraphics() gets called to update the graphics every frame
 	 *
 	 */
 	private void updateGraphics() {
 		if( bufferStrategy != null && isRunning && isShowing )
 		{
 		    Graphics2D g = null;
 		    
 		    try {
 		        g = (Graphics2D) bufferStrategy.getDrawGraphics();
 		        render(g);
 		    } catch (Exception e ) {
 		    	Game.logError("Could not acquire graphics: " + e);
 		    } finally {
 		        g.dispose();
 		    }
 
 			bufferStrategy.show();
 		}
 	}
 
 	private void render( Graphics2D g )
 	{
 		g.setColor(Color.WHITE);
 		g.fillRect(0, 0, gameCanvas.getWidth(), gameCanvas.getHeight());
 
 		g.setColor(Color.BLACK);
 		g.drawString( "Canvas dimensions: " + 
 				gameCanvas.getWidth() + " " + 
 				gameCanvas.getHeight(), 15, 30);
 		
 		if( gameState == GameState.START ) {
 			g.setColor(Color.RED);
 			g.drawString( "Start Screen. click to start.", 15, 15);
 		} else if( gameState == GameState.PLAYING ) {
 			g.setColor(Color.RED);
 			g.drawString( "Game running: " + cycleTime, 15, 15);
 		} else if( gameState == GameState.PAUSED ) {
 			g.setColor(Color.RED);
 			g.drawString( "Game paused", 15, 15);
 		} else if( gameState == GameState.HIGH_SCORE ) {
 			g.setColor(Color.RED);
 			g.drawString( "High score screen", 15, 15);
 		}
 	}
 	
 	public void resetGame()
 	{
 		Game.log("switching state to START");
 		gameState = GameState.START;
 		gameCanvas.showCursor();
 	}
 
 	public void startGame()
 	{
 		Game.log("switching state to PLAYING");
 		gameState = GameState.PLAYING;
 		gameCanvas.hideCursor();
 		gameTimer = new GameTimer( this );
 	}
 
 	public void pauseGame()
 	{
 		Game.log("switching state to PAUSED");
 		gameState = GameState.PAUSED;
 		gameCanvas.showCursor();
 		gameTimer.pause();
 	}
 
 	public void resumeGame()
 	{
 		Game.log("switching state to PLAYING");
 		gameState = GameState.PLAYING;
 		gameCanvas.hideCursor();
 		gameTimer.resume();
 	}
 
 	public synchronized void postGameShutdown()
 	{
 		Game.log("received shutdown request");
 		ignoreInput();
 		gameFrame.destroy();
 		
 		if( gameTimer != null )
 		{
 			gameTimer.kill();
 		}
 		isRunning = false;
 	}
 
 	/**
 	 * updateGameState() gets called to update the game objects every frame
 	 *
 	 */
 	private void updateGameState() {
 		GameInputOperation gio;
 		isShowing = gameCanvas.isShowing();
 		
 		/* maybe put a limit on the number of processed inputs per update */
 		while(  blockingInputQueue.size() > 0 ) 
 		{
 			gio = blockingInputQueue.getFirst();
 			Game.log("caught "+gio);
 
 			if( gameState == GameState.START ) {
 				switch( gio )
 				{
 				case MOUSE_CLICK_LEFT:
 				case MOUSE_CLICK_RIGHT:
 					startGame();
 					return;
 				case KEYBOARD_F_DOWN:
 					gameFrame.toggleFullscreen();
 					break;
 				case KEYBOARD_ESC_DOWN:
 					postGameShutdown();
 					break;
 				}
 			} else if( gameState == GameState.PLAYING ) {
 				switch( gio )
 				{
 				case MOUSE_MOVED:
 					//TODO: process mousePosition
 					break;
 				case MOUSE_CLICK_LEFT:
 					break;
 				case MOUSE_CLICK_RIGHT:
 					break;
 				case KEYBOARD_W_DOWN:
 					break;
 				case KEYBOARD_A_DOWN:
 					break;
 				case KEYBOARD_S_DOWN:
 					break;
 				case KEYBOARD_D_DOWN:
 					break;
 				case KEYBOARD_W_UP:
 					break;
 				case KEYBOARD_A_UP:
 					break;
 				case KEYBOARD_S_UP:
 					break;
 				case KEYBOARD_D_UP:
 					break;
 				case KEYBOARD_SPACE_DOWN:
 					break;
 				case KEYBOARD_P_DOWN:
 					pauseGame();
 					break;
 				case KEYBOARD_F_DOWN:
 					pauseGame();
 					gameFrame.toggleFullscreen();
 					break;
 				case KEYBOARD_ESC_DOWN:
 					postGameShutdown();
 					break;
 				}
 			} else if( gameState == GameState.PAUSED ) {
 				switch( gio )
 				{
 				case KEYBOARD_P_DOWN:
 					resumeGame();
 					break;
 				case KEYBOARD_F_DOWN:
 					gameFrame.toggleFullscreen();
 					break;
 				case KEYBOARD_ESC_DOWN:
 					postGameShutdown();
 					break;
 				}
 			} else if( gameState == GameState.HIGH_SCORE ) {
 				switch( gio )
 				{
 				case MOUSE_CLICK_LEFT:
 				case MOUSE_CLICK_RIGHT:
 					resetGame();
 					return;
 				case KEYBOARD_F_DOWN:
 					gameFrame.toggleFullscreen();
 					break;
 				case KEYBOARD_ESC_DOWN:
 					postGameShutdown();
 					break;
 				}
 			}
 			blockingInputQueue.remove();
 		}
 	}
 
 	private void synchFramerate() {
 		cycleTime = cycleTime + Game.FRAME_DELAY;
 		long difference = cycleTime - System.currentTimeMillis();
 		try {
 			Thread.sleep(Math.max(0, difference));
 		}
 		catch(InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 }

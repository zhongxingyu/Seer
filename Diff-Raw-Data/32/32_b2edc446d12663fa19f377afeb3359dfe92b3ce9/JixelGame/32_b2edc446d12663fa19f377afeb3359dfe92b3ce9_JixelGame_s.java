 package jixel.stage;
 
 import jixel.console.JixelConsole;
 import jixel.console.JixelVariableManager;
 import jixel.gui.JixelPlayer;
 import jixel.gui.JixelScreen;
 import jixel.input.JixelKeyInput;
 import jixel.input.JixelMouseInput;
 
 public abstract class JixelGame implements Runnable {
 
 	private static JixelVariableManager vm;
 	private static JixelConsole con;
 	private static JixelKeyInput keyInput;
 	private static JixelMouseInput mouseInput;
 	private static JixelTimer timer;
 	private static JixelPlayer player;
 
 	private static JixelScreen screen;
 
	public static boolean playing = true;
 	public final String GAME_TITLE;
 	private static boolean paused = false;
 
 	private final static Object updateLock = new Object();
 
 	private static Thread thread; // thread for the update/render
 
 	public JixelGame(String title, int width, int height, int tileSizeLog2, int fps) {
 		GAME_TITLE = title;
 
 		vm = new JixelVariableManager();
 
 		screen = new JixelScreen(title, width, height, tileSizeLog2);
 		timer = new JixelTimer();
 		timer.setFPS(fps);
 
 		con = new JixelConsole();
 
 		keyInput = new JixelKeyInput();
 		mouseInput = new JixelMouseInput();
 		player = new JixelPlayer();
 		getScreen().canvas.addKeyListener(keyInput);
 		getScreen().canvas.addMouseListener(mouseInput);
 
 		// Adds a user-specified shutdown hook for the game
 		Runtime.getRuntime().addShutdownHook(new Thread() {
 			public void run() {
 				closeOperation();
 			}
 		});
 
 		start();
 	}
 
 	/**
 	 * @return the title of the game
 	 */
 	public String getTitle() {
 		return GAME_TITLE;
 	}
 
 	/**
 	 * @return the main screen object for the engine
 	 */
 	public static JixelScreen getScreen() {
 		return screen;
 	}
 
 	/**
 	 * @return the console for the engine
 	 */
 	public static JixelConsole getConsole() {
 		return con;
 	}
 
 	/**
 	 * @return the main variable manager for the engine
 	 */
 	public static JixelVariableManager getVM() {
 		return vm;
 	}
 
 	/**
 	 * @return the sound player for the engine
 	 */
 	public static JixelPlayer getPlayer() {
 		return player;
 	}
 
 	/**
 	 * @return whether or not the game is paused
 	 */
 	public static boolean getPaused() {
 		return paused;
 	}
 
 	/**
 	 * Pauses or unpauses the game
 	 * 
 	 * @param newState
 	 */
 	public static void setPaused(boolean newState) {
 		paused = newState;
 	}
 
 	/**
 	 * @return the key input object
 	 */
 	public static JixelKeyInput getKeyInput() {
 		return keyInput;
 	}
 
 	/**
 	 * @return the mouse input object
 	 */
 	public static JixelMouseInput getMouseInput() {
 		return mouseInput;
 	}
 
 	/**
 	 * @return the timer that keeps track of fps
 	 */
 	public static JixelTimer getTimer() {
 		return timer;
 	}
 
 	/**
 	 * The abstract method to run user code every frame
 	 */
 	public abstract void update();
 
 	/**
 	 * The abstract method to run when the engine starts
 	 */
 	public abstract void onLoad();
 
 	/**
 	 * @return the lock for updating the game. Will block both update and render calls
 	 */
 	public static Object getUpdateLock() {
 		return updateLock;
 	}
 
 	@Override
 	public void run() {
 		onLoad();
 		while (playing) {
 			synchronized (getUpdateLock()) {
 				getTimer().updateTime();
 				if (getTimer().timeForFrame()) {
 					if (!getPaused()) {
 						update();
 						getScreen().update();
 					}
 					getScreen().render();
 				}
 			}
 		}
 		synchronized (getUpdateLock()) {
 			stop();
 		}
 	}
 
 	/**
 	 * Starts the main thread
 	 */
 	private synchronized void start() {
 		thread = new Thread(this, "Jixel Main");
 		thread.start();
 	}
 
 	/**
 	 * Abstract method the user can use to decide what happens when the game closes
 	 */
 	public abstract void closeOperation();
 
 	/**
 	 * Closes the game
 	 */
	public void closeGame() {
 		playing = false;
 	}
 
 	/**
 	 * Joins the main thread
 	 */
 	private synchronized void stop() {
		try {
			thread.join();
		} catch (InterruptedException e) {
			JixelGame.getConsole().printErr("Failed to stop JixelGame thread", e);
		}
 	}
 
 }

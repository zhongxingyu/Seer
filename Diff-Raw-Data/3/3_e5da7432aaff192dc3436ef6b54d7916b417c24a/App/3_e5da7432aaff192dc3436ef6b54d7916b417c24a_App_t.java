 package de.futjikato.mrwhiz;
 
 import org.lwjgl.LWJGLException;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.newdawn.slick.Input;
 
 import de.futjikato.mrwhiz.xml.Map;
 
 public class App {
 
 	static final long MAIN_VERSION = 1;
 
 	private boolean debug = false;
 	private boolean isUnitTest = false;
 	private boolean fullscreen = false;
 	private static App instance;
 
 	private GameStates state = GameStates.MENU;
 	private GameStates nextState;
 
 	private Map nextGameMap;
 	private Input input;
 	private Updater updater;
 
 	/**
 	 * Main
 	 * 
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		// create new app instance
 		App app = getInstance();
 
 		app.defineLwjglLibraryPath();
 
 		// parse arguments
 		for ( String arg : args ) {
 			if (arg.equals("-debug")) {
 				app.enableDebug();
 			}
 		}
 
 		/*
 		 * if (app.getUpdater().checkForUpdates(MAIN_VERSION)) {
 		 * System.out.println("UPDATE available !"); } else {
 		 * System.out.println("Running latest version"); }
 		 */
 
 		app.createWindow();
 		app.loop();

		// remove display on app exit
		Display.destroy();
 	}
 
 	public Updater getUpdater() {
 		if (this.updater == null) {
 			this.updater = new Updater();
 		}
 
 		return this.updater;
 	}
 
 	public void defineLwjglLibraryPath() throws Exception {
 		String os = System.getProperty("os.name");
 		if (os.toLowerCase().contains("windows")) {
 			System.setProperty("org.lwjgl.librarypath", System.getProperty("user.dir") + "\\libs\\lwjgl-2.8.4\\native\\windows");
 		} else if (os.toLowerCase().contains("mac")) {
 			System.setProperty("org.lwjgl.librarypath", System.getProperty("user.dir") + "/libs/lwjgl-2.8.4/native/macosx");
 		} else {
 			throw new Exception("For this os we haven´t jet assigned the native libs : \"" + os + "\"");
 		}
 	}
 
 	/**
 	 * Return the app instance
 	 * 
 	 * @return App instance
 	 */
 	public static App getInstance() {
 		if (App.instance == null) {
 			App.instance = new App();
 		}
 		return App.instance;
 	}
 
 	/**
 	 * Return true if the app is started with debug flag
 	 * 
 	 * @return debug flag
 	 */
 	public boolean isDebug() {
 		return this.debug;
 	}
 
 	public boolean isUnittest() {
 		return this.isUnitTest;
 	}
 
 	public void enableUnittestMode() throws Exception {
 		this.defineLwjglLibraryPath();
 		this.isUnitTest = true;
 	}
 
 	protected void createWindow() throws LWJGLException {
 		Display.setDisplayMode(new DisplayMode(500, 500));
 		Display.setTitle("Mr-Whiz - dev version");
 		Display.create();
 
 		this.input = new Input(Display.getHeight());
 	}
 
 	private void enableDebug() {
 		this.debug = true;
 	}
 
 	public void setNextStep(GameStates gs) {
 		this.nextState = gs;
 	}
 
 	private void loop() {
 		while (this.state != null && this.state != GameStates.QUIT) {
 			// set next step to exit state of new state
 			this.nextState = this.state.getExistState();
 
 			// start state now
 			this.state.start();
 
 			this.state = this.nextState;
 		}
 	}
 
 	public void setNextGameMap(Map map) {
 		this.nextGameMap = map;
 	}
 
 	public Map getNextGameMap() {
 		return this.nextGameMap;
 	}
 
 	public Input getInput() {
 		return this.input;
 	}
 
 	public void toggleFullscreen() {
 		try {
 			this.fullscreen = !this.fullscreen;
 			Display.setFullscreen(this.fullscreen);
 			System.out.println("fullscreen : " + this.fullscreen);
 		} catch (LWJGLException e) {
 			e.printStackTrace();
 		}
 	}
 }

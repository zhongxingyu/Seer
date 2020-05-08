 package arcane_arcade_main;
 
 import handlers.ActorHandler;
 import handlers.DrawableHandler;
 import handlers.KeyListenerHandler;
 import handlers.MouseListenerHandler;
 import helpAndEnums.DepthConstants;
 
 import java.awt.BorderLayout;
 
 import arcane_arcade_worlds.GamePhase;
 import arcane_arcade_worlds.Navigator;
 
 import resource_management.OpenSpriteBankHolder;
 import resource_management.OpenWavSoundBankHolder;
 import tests.FpsApsTest;
import tests.MouseTest;
 import video.GamePanel;
 import video.GameWindow;
 
 /**
  * Main class starts the game
  *
  * @author Mikko Hilpinen & Unto Solala.
  *         Created 26.8.2013.
  */
 public class Main
 {
 	// ATTRIBUTES	----------------------------------------------------
 	
 	private GameWindow window;
 	private GamePanel mainpanel;
 	private DrawableHandler maindrawer;
 	private KeyListenerHandler mainkeyhandler;
 	private MouseListenerHandler mainmousehandler;
 	private ActorHandler mainactorhandler;
 	
 	private Navigator navigator;
 	
 	
 	// CONSTRUCTOR	----------------------------------------------------
 	
 	/**
 	 * Starts the game
 	 */
 	public Main()
 	{
 		// Reads the options from a file
 		Options.loadSettings();
 		
 		// Initializes attributes
 		this.mainpanel = new GamePanel(GameSettings.SCREENWIDTH, 
 				GameSettings.SCREENHEIGHT);
 		
 		boolean fullscreen = Options.fullscreenon;
 		
 		this.window = new GameWindow(GameSettings.SCREENWIDTH, 
 				GameSettings.SCREENHEIGHT, "Arcane Arcade", !fullscreen);
 		// Sets up the window
 		this.window.addGamePanel(this.mainpanel, BorderLayout.CENTER);
 		// Sets the window to the fullscreen if needed
 		if (fullscreen)
 			this.window.setFullScreen(true);
 		
 		this.mainactorhandler = new ActorHandler(false, null);
 		this.window.addActor(this.mainactorhandler);
 		this.maindrawer = new DrawableHandler(false, true, 
 				DepthConstants.NORMAL, null);
 		this.mainpanel.getDrawer().addDrawable(this.maindrawer);
 		this.mainkeyhandler = new KeyListenerHandler(false, null);
 		this.window.addKeyListener(this.mainkeyhandler);
 		this.mainmousehandler = new MouseListenerHandler(false, null, null);
 		this.window.addActor(this.mainmousehandler);
 		this.window.addMouseListener(this.mainmousehandler);
 		
 		this.navigator = new Navigator(this.maindrawer, this.mainactorhandler, 
 				this.mainkeyhandler, this.mainmousehandler, 
 				new OpenSpriteBankHolder(GameSettings.SPRITEDATALOCATION), 
 				new OpenWavSoundBankHolder(GameSettings.WAVDATALOCATION));
 		
 		// Starts the game by starting the main menu
 		this.navigator.startPhase(GamePhase.MAINMENU, null);
 	}
 	
 	
 	// GETTERS & SETTERS	--------------------------------------------
 	
 	/**
 	 * @return The keylistener that informs all objects about keypresses
 	 * @warning Do not kill this instance since it won't probably be replaced
 	 */
 	public KeyListenerHandler getKeyListenerHandler()
 	{
 		return this.mainkeyhandler;
 	}
 	
 	/**
 	 * @return The mouselistener that informs all objects about mouse events
 	 * @warning Do not kill this instance since it won't probably be replaced
 	 */
 	public MouseListenerHandler getMouseListenerHandler()
 	{
 		return this.mainmousehandler;
 	}
 	
 	/**
 	 * @return The actorhandler that informs all objects about act-events
 	 * @warning Do not kill this instance since it won't probably be replaced
 	 */
 	public ActorHandler getActorHandler()
 	{
 		return this.mainactorhandler;
 	}
 	
 	/**
 	 * @return The drawablehandler that draws all objects
 	 * @warning Do not kill this instance since it won't probably be replaced
 	 */
 	public DrawableHandler getDrawableHandler()
 	{
 		return this.maindrawer;
 	}
 	
 	
 	// MAIN METHOD	----------------------------------------------------
 	
 	/**
 	 * Starts the game
 	 *
 	 * @param args No arguments needed
 	 */
 	public static void main(String[] args)
 	{
 		// Starts the game
 		Main main = 
 				new Main();
 		
 		// Runs some tests
 		new FpsApsTest(main.getActorHandler(), main.getDrawableHandler());
 	}
 }

 /**
  * Park View Protector
  *
  * @author	Jamie of the Javateerz
  */
 
 package org.javateerz.ParkViewProtector;
 
 import javax.swing.JOptionPane;
 import javax.swing.UIManager;
 
 import org.javateerz.ParkViewProtector.Menu.Menu;
 import org.javateerz.ParkViewProtector.Menu.OptionsMenu;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.Timer;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 
 public class ParkViewProtector
 {
 	public static final int WIDTH			= 800;
 	public static final int HEIGHT			= 600;
 	
 	// colors
 	public static final Color COLOR_BG_1	= new Color(255, 0, 255);
 	public static final Color COLOR_TEXT_1	= new Color(255, 255, 255);
 	public static final Color COLOR_BG_2	= new Color(255, 255, 255);
 	public static final Color COLOR_TEXT_2	= new Color(0, 0, 0);
 	
 	public static final Color STATS_BAR_BG	= new Color(0, 0, 0, 127);
 	public static final Color STATS_BAR_FG	= new Color(255, 255, 255);
 	public static final Color STATS_BAR_HP	= new Color(255, 0, 255);
 	public static final Color STATS_BAR_TP	= new Color(0, 255, 0);
 	
 	public static final int OPENING_TIME	= 3000;
 	public static final float RENDER_SPEED	= 50f;
 	
 	private static final long serialVersionUID = 1L;
 	
 	private boolean running					= true;
 	public static boolean showTitle			= true;
 	public static boolean showMenu			= false;
 	public static boolean showOptions		= false;
 	public static boolean selectChar		= true;
 	public static boolean showCredits		= false;
 	
 	public static Timer timer				= new Timer();
 	private static float ticks;
 	public static float renderDelta;
 	private static float lastTime			= timer.getTime();
 	
 	private boolean showFps					= false;
 	private long frames						= 0;
 	private float frameTime					= timer.getTime();
	private static int fps					= 100;
 	
 	private TitleScreen title;
 	private Game game;
 	private Menu menu;
 	private OptionsMenu optMenu;
 	private CharSelect charSelect;
 	private Credits credits;
 	
 	public ParkViewProtector(boolean fullscreen, boolean showFps)
 	{
 		try
 		{
 			setDisplayMode();
 			
 			if(fullscreen)
 			{
 				Display.setFullscreen(true);
 			}
 			
 			this.showFps					= showFps;
 			
 			Display.setTitle("Park View Protector");
 			Display.create();
 		}
 		catch (LWJGLException e)
 		{
 			e.printStackTrace();
 		}
 		
 		if(fullscreen)
 		{
 			// hide mouse cursor
 			Mouse.setGrabbed(true);
 		}
 		
 		// disable 3D depth test
 		GL11.glDisable(GL11.GL_DEPTH_TEST);
 		
 		// set clear color to white
 		GL11.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
 		
 		// enable transparency
 		GL11.glEnable(GL11.GL_BLEND);
 		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
 		
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 		
 		GL11.glOrtho(0, WIDTH, HEIGHT, 0, -1, 1);
 		
 		// set the Swing look and feel to the system one so the file selector looks native
 		try
 		{
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		}
 		catch(Exception e)
 		{
 			System.out.println("Error setting system look and feel");
 		}
 	}
 	
 	/**
 	 * Select a display mode
 	 * 
 	 * @return True if an acceptable display mode was found
 	 */
 	public boolean setDisplayMode()
 	{
 		try
 		{
 			DisplayMode[] modes				= Display.getAvailableDisplayModes();
 		
 			for(DisplayMode mode : modes)
 			{
 				if(mode.getWidth() == WIDTH && mode.getHeight() == HEIGHT)
 				{
 					Display.setDisplayMode(mode);
 					return true;
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Initialize game
 	 */
 	public void init()
 	{
 		title						= new TitleScreen(this);
 		game						= new Game(this);
 		menu						= new Menu(this);
 		optMenu						= new OptionsMenu(this);
 		charSelect					= new CharSelect(this);
 		credits						= new Credits(this);
 	}
 	
 	/**
 	 * Restart the game (return to the title screen)
 	 */
 	public void restart()
 	{
 		showTitle					= true;
 		showMenu					= false;
 		showOptions					= false;
 		selectChar					= true;
 		showCredits					= false;
 		
 		init();
 	}
 	
 	/**
 	 * Show opening graphics
 	 */
 	public void showOpening()
 	{
 		Opening opening				= new Opening();
 		
 		long startMillis			= System.currentTimeMillis();
 		long currMillis				= startMillis;
 		
 		while(currMillis < startMillis + OPENING_TIME)
 		{
 			// clear screen
 			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 			
 			GL11.glMatrixMode(GL11.GL_MODELVIEW);
 			GL11.glLoadIdentity();
 			
 			// render content
 			opening.draw();
 			
 			// show rendered content
 			GL11.glFlush();
 			Display.update();
 			
 			currMillis				= System.currentTimeMillis();
 		}
 	}
 	
 	/**
 	 * The main game loop
 	 */
 	public void mainLoop()
 	{
 		while(running)
 		{
 			// close requested?
 			if(Display.isCloseRequested())
 			{
 				running				= false;
 			}
 			
 			tick();
 			
 			// run the active driver
 			getActiveScreen().step();
 			getActiveScreen().throttleSpeed();
 			
 			frames++;
 			
 			if(showFps && frames == 50)
 			{
 				fps					= (int) (frames / (timer.getTime() - frameTime));
 				frames				= 0;
 				frameTime			= timer.getTime();
 				
 				Display.setTitle("Park View Protector (fps: " + fps + ")");
 			}
 			
 			// render
 			render();
 			Display.update();
 		}
 		
 		quit();
 	}
 	
 	/**
 	 * "Tick" goes the game timer
 	 * "Tock" responds the clock
 	 */
 	private void tick()
 	{
 		Timer.tick();
 		
 		ticks						= timer.getTime() - lastTime;
 		renderDelta				   += ticks;
 		lastTime					= timer.getTime();
 	}
 	
 	/**
 	 * Render what goes on the screen
 	 */
 	private void render()
 	{
 		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
 		
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 		GL11.glLoadIdentity();
 		
 		// render content
 		getActiveScreen().draw();
 		
 		// show rendered content
 		GL11.glFlush();
 		
 		renderDelta					= 0;
 	}
 	
 	/**
 	 * @return Basically, how many frames of movement to render
 	 */
 	public static float getRenderDelta()
 	{
 		float delta					= renderDelta * RENDER_SPEED;
 		
 		return delta;
 	}
 	
 	/**
 	 * Converts number of frames to number of seconds
 	 * 
 	 * @param frames Number of frames
 	 * @return Number of seconds
 	 */
 	public static double framesToSecs(int frames)
 	{
		return (double) frames / fps;
 	}
 	
 	/**
 	 * Converts number of seconds to number of frames
 	 * 
 	 * @param secs Number of seconds
 	 * @return Number of frames
 	 */
 	public static int secsToFrames(double secs)
 	{
		return (int) (secs * fps);
 	}
 	
 	/**
 	 * Gets the instance of Game being run by ParkViewProtector
 	 * 
 	 * @return The instance of Game
 	 */
 	public Game getGame()
 	{
 		return game;
 	}
 	
 	/**
 	 * Sets the instance of Game that will be run by ParkViewProtector
 	 * 
 	 * @param g An instance of Game
 	 */
 	public void setGame(Game g)
 	{
 		game							= g;
 		game.init(this);
 	}
 	
 	/**
 	 * Update a float option
 	 * 
 	 * @param key
 	 * @param value
 	 */
 	public void setFloat(String key, float value)
 	{
 		Options.INSTANCE.putFloat(key, value);
 		
 		if(key == "music_volume")
 		{
 			getActiveScreen().getMusic().setVolume(value);
 		}
 	}
 
 	/**
 	 * @return The screen driver that is currently being displayed
 	 */
 	public GameScreen getActiveScreen()
 	{
 		if(showTitle)
 		{
 			return title;
 		}
 		else if(showOptions)
 		{
 			return optMenu;
 		}
 		else if(showMenu)
 		{
 			return menu;
 		}
 		else if(selectChar)
 		{
 			return charSelect;
 		}
 		else if(showCredits)
 		{
 			return credits;
 		}
 		else {
 			return game;
 		}
 	}
 	
 	/**
 	 * Play a sound
 	 * 
 	 * @param file File name
 	 */
 	public static void playSound(String file) throws SlickException
 	{
 		Sound sound							= new Sound(file);
 		
 		sound.play(1.0f, Options.INSTANCE.getFloat("sfx_volume", 1.0f));
 	}
 	
 	/**
 	 * Display an error message
 	 * 
 	 * @param msg Message to display
 	 * @param fatal Should the program be terminated?
 	 */
 	public void error(String msg, boolean fatal)
 	{
 		System.out.println("Error: " + msg);
 		
 		JOptionPane.showMessageDialog(null, msg, "Error", JOptionPane.ERROR_MESSAGE);
 		
 		if(fatal)
 		{
 			System.exit(0);
 		}
 	}
 	
 	/**
 	 * Display an error message and terminates the program
 	 * 
 	 * @param msg Message to display
 	 */
 	public static void error(String msg)
 	{
 		System.out.println("Fatal error: " + msg);
 		
 		JOptionPane.showMessageDialog(null, msg, "Park View Protector Error", JOptionPane.ERROR_MESSAGE);
 		
 		System.exit(0);
 	}
 	
 	public void quitGame()
 	{
 		running							= false;
 	}
 
 	private void quit()
 	{
 		running							= false;
 		
 		// stop music
 		getActiveScreen().getMusic().stop();
 		
 		// make sure options get stored
 		Options.INSTANCE.sync();
 		
 		Display.destroy();
 	}
 	
 	public static void main(String args[])
 	{
 		boolean fullscreen				= false;
 		boolean skipIntro				= false;
 		boolean showFps					= false;
 		
 		// command line arguments
 		if(args.length > 0)
 		{
 			for(String arg : args)
 			{
 				if(arg.equals("-fullscreen"))
 				{
 					fullscreen			= true;
 				}
 				else if(arg.equals("-nointro"))
 				{
 					skipIntro			= true;
 				}
 				else if(arg.equals("-showfps"))
 				{
 					showFps				= true;
 				}
 			}
 		}
 		
 		ParkViewProtector game			= new ParkViewProtector(fullscreen, showFps);
 		
 		game.init();
 		
 		// show intro
 		if(!skipIntro)
 			game.showOpening();
 		
 		game.mainLoop();
 	}
 }

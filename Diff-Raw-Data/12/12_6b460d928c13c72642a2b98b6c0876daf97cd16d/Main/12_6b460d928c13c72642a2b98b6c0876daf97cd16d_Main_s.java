 package edu.calpoly.csc.pulseman;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.lwjgl.LWJGLUtil;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 
 import edu.calpoly.csc.pulseman.MessageHandler.MessageReceiver;
 
 public class Main extends BasicGame
 {
 	public enum GameState
 	{
 		MENU, GAME, GAMEOVER, WIN
 	};
 
 	public class AndroidStates
 	{
 		public static final int NOT_CONNECTED = 0, CONNECTING = 1,
 				CONNECTED = 2;
 	}
 
 	private static GameState state = GameState.MENU;
 	private static int curLevel = -1;
 	private static String[] levels =
	{ "res/level1.png", "res/level2.png", "res/level3.png", "res/level4.png",
 	  "res/level5.png", "res/level6.png"};
 	private static final int width = 1280, height = 720;
 	private static volatile int androidState = AndroidStates.NOT_CONNECTED;
 	Map<GameState, GameInterface> interfaceMap = new HashMap<GameState, GameInterface>();
 
 	private Music music;
 	public static int tVelocity;
 
 	public Main()
 	{
 		super("Pulse of Nature");
 	}
 
 	public static void reset() {
 		curLevel = -1;
 		World.getWorld().nextLevel();
 		state = GameState.MENU;
 	}
 	
 	public static int getScreenWidth()
 	{
 		return width;
 	}
 
 	public static int getScreenHeight()
 	{
 		return height;
 	}
 	
 	public static boolean hasNextLevel() {
 		return curLevel + 1 < levels.length;
 	}
 
 	public static int getAndroidState()
 	{
 		return androidState;
 	}
 
 	public static void setAndroidConnecting()
 	{
 		androidState = AndroidStates.CONNECTING;
 	}
 
 	public static String nextLevel()
 	{
 		curLevel++;
 		if (curLevel == levels.length) {
 			state = GameState.WIN;
 			return "";
 		} else {
 			return levels[curLevel];
 		}
 	}
 
 	public static void setState(GameState state)
 	{
 		Main.state = state;
 	}
 
 	public static int getCurrentLevel()
 	{
 		return curLevel;
 	}
 
 	@Override
 	public void render(GameContainer gc, Graphics g) throws SlickException
 	{
 		interfaceMap.get(state).render(gc, g);
 	}
 
 	@Override
 	public void init(GameContainer gc) throws SlickException
 	{
 		StartMenu menu = new StartMenu();
 		menu.init(gc);
 		interfaceMap.put(GameState.MENU, menu);
 
 		final GameScreen game = new GameScreen();
 		game.init(gc);
 		interfaceMap.put(GameState.GAME, game);
 
 		GameOver gameOver = new GameOver();
 		gameOver.init(gc);
 		interfaceMap.put(GameState.GAMEOVER, gameOver);
 		
 		WinScreen winScreen = new WinScreen();
 		winScreen.init(gc);
 		interfaceMap.put(GameState.WIN, winScreen);
 
 		music = new Music("res/pulse_of_nature.ogg");
 		music.loop();
 
 		MessageHandler.addmessageReceiver(new MessageReceiver()
 		{
 			@Override
 			public void onMessageReceived(String message)
 			{
 				game.playerTwoTap();
 			}
 
 			@Override
 			public void onConnectionEstablished(InetAddress client)
 			{
 				System.out.println("Connection established: " + client.getHostAddress());
 				androidState = AndroidStates.CONNECTED;
 			}
 
 			@Override
 			public void onConnectionLost(InetAddress client)
 			{
 				if(client != null)
 				{
 					System.out.println("Connection lost: " + client.getHostAddress());
 				}
 
 				androidState = AndroidStates.NOT_CONNECTED;
 			}
 		});
 	}
 
 	public static void main(String[] arg) throws SlickException
 	{
 		System.setProperty("org.lwjgl.librarypath", new File(new File(System.getProperty("user.dir"), "native"), LWJGLUtil.getPlatformName()).getAbsolutePath());
 		System.setProperty("net.java.games.input.librarypath", System.getProperty("org.lwjgl.librarypath"));
 		AppGameContainer app = new AppGameContainer(new Main());
 
 		app.setDisplayMode(width, height, false);
 		app.setTargetFrameRate(60);
 		app.setVSync(true);
 		app.setIcon("res/monk.png");
 		app.start();
 	}
 
 	@Override
 	public void update(GameContainer gc, int dt) throws SlickException
 	{
 		interfaceMap.get(state).update(gc, dt);
 		if(tVelocity > 0)
 			tVelocity -= 0.0001;
 	}
 }

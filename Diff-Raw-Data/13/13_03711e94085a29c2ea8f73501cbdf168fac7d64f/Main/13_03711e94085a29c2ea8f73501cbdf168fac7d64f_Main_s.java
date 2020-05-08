 package edu.calpoly.csc.pulseman;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 
 import org.lwjgl.LWJGLUtil;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Sound;
 import org.newdawn.slick.geom.Vector2f;
 
 import edu.calpoly.csc.pulseman.MessageHandler.MessageReceiver;
 
 public class Main extends BasicGame {
 	private volatile static LinkedList<String> messageQueue = new LinkedList<String>();
 	public enum GameState {MENU, GAME, GAMEOVER};
 	private static GameState state = GameState.MENU;
 	private static final int width = 1280, height = 720;
 	Map<GameState, GameInterface> interfaceMap = new HashMap<GameState, GameInterface>();
 	
 		Sound debugMusic;
 		public static int tVelocity;
 		
 		private class Heart {
 			public Image image;
 			public int scale;
 			public void init(Image image) {
 				this.image = image;
 				scale = 1;
 			}
 			public void update(GameContainer gc, int delta) {
				if (scale < 1.50)
 				{
					scale += .25 * delta;
 					if (delta > 1)
						System.out.println(delta + " < delta");
 				}
 				else if (scale > 1)
					scale -= .25 * delta;
 			}
 			public void render(GameContainer gc, Graphics g) {
 				image.draw(0, 0, scale);
 				g.drawString(new String("" + tVelocity), 300, 100);
 			}
 			
 		}
 		public static Heart heart;
 		
 		
 	public Main() {
 		super("Pulse of Nature");
 	}
 	
 	public static int getScreenWidth() {
 		return width;
 	}
 	
 	public static int getScreenHeight() {
 		return height;
 	}
 	
 	public static void setState(GameState state) {
 		Main.state = state;
 	}
 	
 	
 	
 	@Override
 	public void render(GameContainer gc, Graphics g) throws SlickException {
 		interfaceMap.get(state).render(gc, g);
 		heart.render(gc, g);
 	}
 
 	@Override
 	public void init(GameContainer gc) throws SlickException {
 		StartMenu menu = new StartMenu();
 		menu.init(gc);
 		interfaceMap.put(GameState.MENU, menu);
 		
 		GameScreen game = new GameScreen();
 		game.init(gc);
 		interfaceMap.put(GameState.GAME, game);
 		
 		debugMusic = new Sound("res/music.ogg");
 		debugMusic.play();
 		heart = new Heart();
 		heart.init(new Image("res/heart.png"));
 	}
 		
 	
 	public static void main(String[] arg) throws SlickException {
 		listenForConnection();
 
 		MessageHandler.addmessageReceiver(new MessageReceiver()
 		{
 			@Override
 			public void onMessageReceived(String message)
 			{
 				synchronized(messageQueue)
 				{
 					messageQueue.offer(message);
 								tVelocity += 10000;
 				}
 			}
 
 			@Override
 			public void onConnectionEstablished(InetAddress client)
 			{
 				System.out.println("Connection established: " + client.getHostAddress());
 			}
 
 			@Override
 			public void onConnectionLost(InetAddress client)
 			{
 				System.out.println("Connection lost: " + client.getHostAddress());
 				listenForConnection();
 			}
 		});
 		
 		System.setProperty("org.lwjgl.librarypath", new File(new File(System.getProperty("user.dir"), "native"), LWJGLUtil.getPlatformName()).getAbsolutePath());
 		System.setProperty("net.java.games.input.librarypath", System.getProperty("org.lwjgl.librarypath"));
 		AppGameContainer app = new AppGameContainer(new Main());
 
 		app.setDisplayMode(800, 600, false);
 		app.start();
 	}
 
 	@Override
 	public void update(GameContainer gc, int dt) throws SlickException {
 		interfaceMap.get(state).update(gc, dt);
 				heart.update(gc, dt);
 				if (tVelocity > 0)
 					tVelocity -= 0.0001;
 	}
 	
 	public static void listenForConnection()
 	{
 		new Thread(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				MessageHandler.listenForConnection();
 			}
 		}).start();
 	}
 
 	public static LinkedList<String> getMessageQueue() {
 		return messageQueue;
 	}
 
 }

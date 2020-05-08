 package server;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 
 import clients.Client;
 import clients.Player;
 import physics.CattoPhysicsEngine;
 import world.GameObject;
 import world.LevelMap;
 import world.LevelSet;
 import jig.engine.GameClock;
 import jig.engine.RenderingContext;
 import jig.engine.ResourceFactory;
 import jig.engine.hli.StaticScreenGame;
 import jig.engine.util.Vector2D;
 import net.NetStateManager;
 
 /**
  * Server
  * 
  * @author Vitaliy
  *
  */
 
 public class Server extends StaticScreenGame{
 	
 	private static final int WORLD_WIDTH = 800;
 	private static final int WORLD_HEIGHT = 600;
 	
 	/* This is a static, constant time between frames, all clients run as fast as the server runs */
 	public static int DELTA_MS = 30;
 	
 	private NetStateManager netState;
 	private NetworkEngine ne;
 	private CattoPhysicsEngine pe;
 	private ServerGameState gameState;
 	private LevelSet levels;
 	private LevelMap level;
 	
 	private GameObject player;
 	
 	public Server(int width, int height, boolean preferFullscreen) {
 		super(width, height, preferFullscreen);
 		
 		netState = new NetStateManager();
 		ne = new NetworkEngine(netState);
 		gameState = new ServerGameState();
 		pe = new CattoPhysicsEngine(new Vector2D(0, 40));
 		pe.setDrawArbiters(true);
 		fre.setActivation(true);
 		
 		// Some Test Resources
 		ResourceFactory factory = ResourceFactory.getFactory();
 
 		BufferedImage[] b = new BufferedImage[1];
 		b[0] = new BufferedImage(1600, 10, BufferedImage.TYPE_INT_RGB);
 		Graphics g = b[0].getGraphics();
 		g.setColor(Color.green);
 		g.fillRect(0, 0, 1600, 10);
 		g.dispose();
 		factory.putFrames("ground", b);
 
 		b = new BufferedImage[1];
 		b[0] = new BufferedImage(16, 32, BufferedImage.TYPE_INT_RGB);
 		g = b[0].getGraphics();
 		g.setColor(Color.red);
 		g.fillRect(0, 0, 30, 40);
 		g.dispose();
 		factory.putFrames("player", b);
 
 		b = new BufferedImage[1];
 		b[0] = new BufferedImage(100, 10, BufferedImage.TYPE_INT_RGB);
 		g = b[0].getGraphics();
 		g.setColor(Color.green);
 		g.fillRect(0, 0, 100, 10);
 		g.dispose();
 		factory.putFrames("platform", b);
 
 		b = new BufferedImage[1];
 		b[0] = new BufferedImage(32, 32, BufferedImage.TYPE_INT_RGB);
 		g = b[0].getGraphics();
 		g.setColor(Color.blue);
 		g.fillRect(0, 0, 32, 32);
 		g.dispose();
 		factory.putFrames("smallbox", b);
 		
 		b = new BufferedImage[1];
 		b[0] = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
 		g = b[0].getGraphics();
 		g.setColor(Color.red);
 		g.fillOval(0, 0, 10, 10);
 		g.dispose();
 		factory.putFrames("playerSpawn", b);
 		
 		// Load entire level.
 		levels = new LevelSet("/res/Levelset.txt");
 		
 		// Is there actual level?
 		if (levels.getNumLevels() == 0) {
 			System.err.println("Error: Levels loading failed.\n");
 			System.exit(1);
 		}
 
 		// Get specified level.
 		level = levels.getThisLevel(0);
 		// Is there actual level?
 		if (level == null) {
 			System.err.println("Error: Level wasn't correctly loaded.\n");
 			System.exit(1);
 		}
 
 		// Build world from level data
 		level.buildLevel(gameState);
 		
 		// Add a player to test movement, remove when not needed
 		GameObject p;
 		p = new GameObject("player");
 		p.set(100, .2, 1.0, 0.0);
 		Vector2D a = level.playerInitSpots.get(0);
 		p.setPosition(new Vector2D(a.getX(), a.getY()));
 		gameState.add(p, GameObject.PLAYER);
 		player = p;
 		
 		netState.update(gameState.getNetState());
 	}
 	
 	// this can be removed when the server no longer needs to test player movement
 	public void keyboardMovementHandler() {
 		keyboard.poll();
 		
         boolean down = keyboard.isPressed(KeyEvent.VK_DOWN) || keyboard.isPressed(KeyEvent.VK_S);
         boolean up = keyboard.isPressed(KeyEvent.VK_UP) || keyboard.isPressed(KeyEvent.VK_W);
 		boolean left = keyboard.isPressed(KeyEvent.VK_LEFT) || keyboard.isPressed(KeyEvent.VK_A);
 		boolean right = keyboard.isPressed(KeyEvent.VK_RIGHT) || keyboard.isPressed(KeyEvent.VK_D);
 		
 		int x = 0, y = 0;
 		if(left) x--;
 		if(right) x++;
 		if(up) y--;
 		if(down) y++;
 		//System.out.println(x + " " +  y);
		if(x!=0 || y!=0) player.move(x, y);
 	}
 	
 	public void update(final long deltaMs) {
 		super.update(deltaMs);
 		pe.applyLawsOfPhysics(deltaMs);
 		ne.update();
 		gameState.update();
 		keyboardMovementHandler();
 	}
 	
 	@Override
 	public void render(final RenderingContext gc) {
 		super.render(gc);
 		pe.renderPhysicsMarkup(gc);
 	}
 	
 	public static void main (String[] vars) {
 		Server s = new Server(WORLD_WIDTH, WORLD_HEIGHT, false);
 		
 		s.gameObjectLayers.clear();
 		s.pe.clear();
 		s.gameObjectLayers.add(s.gameState.getBoxes());
 		s.pe.manageViewableSet(s.gameState.getBoxes());
 		s.run();
 		
 	}
 }

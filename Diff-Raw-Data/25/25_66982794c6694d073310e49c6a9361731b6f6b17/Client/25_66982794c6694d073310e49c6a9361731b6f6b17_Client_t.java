 package clients;
 
 import java.awt.event.KeyEvent;
 import java.util.Collection;
import java.util.ConcurrentModificationException;
 
import net.GameState;
 import net.GameStateManager;
 import net.NetObject;
 
 import jig.engine.PaintableCanvas;
 import jig.engine.RenderingContext;
 import jig.engine.PaintableCanvas.JIGSHAPE;
 import jig.engine.hli.StaticScreenGame;
 import jig.engine.physics.AbstractBodyLayer;
 import jig.engine.physics.Body;
 import jig.engine.physics.BodyLayer;
 
 
 /**
  * Client
  * 
  * @author Vitaliy
  * 
  */
 
 public class Client extends StaticScreenGame {
 	
	public static final int WORLD_WIDTH = 800, WORLD_HEIGHT = 600;
 	
 	boolean keyPressed = false;
 	boolean keyReleased = true;
 	
 	GameStateManager gm;
 	Player player;
 
 	BodyLayer<Body> WorldLayer = new AbstractBodyLayer.IterativeUpdate<Body>();
 	BodyLayer<Body> MovableLayer = new AbstractBodyLayer.IterativeUpdate<Body>();
 	BodyLayer<Body> InterfaceLayer = new AbstractBodyLayer.IterativeUpdate<Body>();
 	
 	Collection<NetObject> noList = null;
 
 	public Client() {
 
 		super(WORLD_WIDTH, WORLD_HEIGHT, false);
 
 		PaintableCanvas.loadDefaultFrames("player", 10, 10, 1, JIGSHAPE.CIRCLE, null);
 		gm = new GameStateManager();
 
 		/* Start thread to sync gameState with server */
 		BroadcastListener bListen = new BroadcastListener(gm);
 		bListen.start();
 
 		TcpClient control = new TcpClient("127.0.0.1", 5001);
 		
 		player = new Player(0, control);
 	}
 	
 	/**
 	 * Handle keyboard strokes for movement
 	 */
 	public void keyboardMovementHandler() {
 		keyboard.poll();
 		
         boolean down = keyboard.isPressed(KeyEvent.VK_DOWN);
         boolean up = keyboard.isPressed(KeyEvent.VK_UP);
 		boolean left = keyboard.isPressed(KeyEvent.VK_LEFT);
 		boolean right = keyboard.isPressed(KeyEvent.VK_RIGHT);
 		
 		if (left || right || down || up) {
 			if(left) player.move(Player.LEFT);
 			if(right) player.move(Player.RIGHT);
 			if(up) player.move(Player.UP);
 			if(down) player.move(Player.DOWN);
 		}
 		else
 			player.move(Player.HALT);
 	}
 
 	public void update(long deltaMs) {
 		super.update(deltaMs);
 
 		noList = gm.getState().getNetObjects();
		try {
			for (NetObject no : noList)
				no.update(deltaMs);		
		} catch (ConcurrentModificationException e2) {
			
		}
 	
 		keyboardMovementHandler();
 		
 		if( mouse.isLeftButtonPressed()) {
 			System.out.println("Weapon fire keypress" + mouse.getLocation());
 		}
 	}
 
 	public void render(RenderingContext rc) {
 		super.render(rc);
 		
 		try {
			GameState gs = gm.getState();
			Collection<NetObject> nos = gs.getNetObjects();
			for (NetObject no : nos)
				no.render(rc);
 		} catch (NullPointerException e) {
 			
		} catch (ConcurrentModificationException e2) {
			
 		}
 		
 	}
 
 	public static void main(String[] vars) {
 		Client c = new Client();
 		c.run();
 	}
 }

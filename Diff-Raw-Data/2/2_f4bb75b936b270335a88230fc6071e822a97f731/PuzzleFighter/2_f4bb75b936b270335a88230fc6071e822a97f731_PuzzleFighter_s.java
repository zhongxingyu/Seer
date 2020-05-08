 package cs447.PuzzleFighter;
 
 
 import java.awt.Font;
 import java.awt.event.KeyEvent;
 import java.awt.geom.AffineTransform;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import jig.engine.FontResource;
 
 import jig.engine.RenderingContext;
 import jig.engine.ResourceFactory;
 import jig.engine.hli.StaticScreenGame;
 
 public class PuzzleFighter extends StaticScreenGame {
 	final static double SCALE = 1.0;
 
 	AffineTransform LEFT_TRANSFORM;
 	AffineTransform RIGHT_TRANSFORM;
 
 	public final static int height = (int) (416 * SCALE);
 	public final static int width = (int) (700 * SCALE);
 
 	private PlayField pfLeft;
 	private PlayField pfRight;
 	
 	public Socket socket = null;
 	public ServerSocket serv = null;
 
 	private boolean playing = false;
 
 	final static String RSC_PATH = "cs447/PuzzleFighter/resources/";
 	final static String GEM_SHEET = RSC_PATH + "gems.png";
 	final static String CUT_SHEET = RSC_PATH + "cutman.png";
 	final static String MEGA_SHEET = RSC_PATH + "megaman.png";
 
 	public static void main(String[] args) throws IOException {
 		PuzzleFighter game = new PuzzleFighter();
 		game.run();
 	}
 
 	public PuzzleFighter() throws IOException {
 		super(width, height, false);
 		ResourceFactory.getFactory().loadResources(RSC_PATH, "resources.xml");
 
 		LEFT_TRANSFORM = AffineTransform.getScaleInstance(SCALE, SCALE);
 		RIGHT_TRANSFORM = (AffineTransform) LEFT_TRANSFORM.clone();
 		RIGHT_TRANSFORM.translate(508, 0);
 	}
 
 	private void localMultiplayer() throws IOException {
 		socket = null;
 		pfLeft = new PlayField(6, 13, socket, false);
 		pfRight = new PlayField(6, 13, socket, true);
 	}
 
 	public void remoteClient(String host) throws IOException {
 		connectTo(host);
 
 		pfLeft = new PlayField(6, 13, socket, false);
 		pfRight = new RemotePlayfield(6, 13, socket);
 	}
 
 	public void remoteServer() throws IOException {
 		host();
 		
 		pfLeft = new PlayField(6, 13, socket, false);
 		pfRight = new RemotePlayfield(6, 13, socket);
 	}
 
 	public void render(RenderingContext rc) {
 		super.render(rc);
 		if (playing) {
 			rc.setTransform(LEFT_TRANSFORM);
 			pfLeft.render(rc);
 			rc.setTransform(RIGHT_TRANSFORM);
 			pfRight.render(rc);
 			return;
 		}else{
 			FontResource font = ResourceFactory.getFactory().getFontResource(new Font("Sans Serif", Font.BOLD, 30),
 			java.awt.Color.red, null );
 			font.render("Puzzle Fighter", rc, AffineTransform.getTranslateInstance(280, 100));
 			font.render("1 - local multiplayer", rc, AffineTransform.getTranslateInstance(280, 150));
 			font.render("2 - remote host", rc, AffineTransform.getTranslateInstance(280, 200));
 			font.render("3 - remote client", rc, AffineTransform.getTranslateInstance(280, 250));
 		}
 	}
 
 	public void update(long deltaMs) {
 		if (playing) {
 			boolean down1 = keyboard.isPressed(KeyEvent.VK_S);
 			boolean left1 = keyboard.isPressed(KeyEvent.VK_A);
 			boolean right1 = keyboard.isPressed(KeyEvent.VK_D);
 			boolean ccw1 = keyboard.isPressed(KeyEvent.VK_Q);
 			boolean cw1 = keyboard.isPressed(KeyEvent.VK_E);
 			int garbage = pfLeft.update(deltaMs, down1, left1, right1, ccw1, cw1);
 			pfRight.garbage += garbage;
 
 			boolean down2 = keyboard.isPressed(KeyEvent.VK_K);
 			boolean left2 = keyboard.isPressed(KeyEvent.VK_J);
 			boolean right2 = keyboard.isPressed(KeyEvent.VK_L);
 			boolean ccw2 = keyboard.isPressed(KeyEvent.VK_U);
 			boolean cw2 = keyboard.isPressed(KeyEvent.VK_O);
 			int garbage2 = pfRight.update(deltaMs, down2, left2, right2, ccw2, cw2);
			pfLeft.garbage += garbage;
 			if(garbage2 == -1 || garbage == -1){
 				pfLeft.close();
 				pfRight.close();
 				playing = false;
 				if(socket != null){
 					try {
 						socket.close();
 					} catch (IOException ex) {
 						Logger.getLogger(PuzzleFighter.class.getName()).log(Level.SEVERE, null, ex);
 					}
 				}
 			}
 			return;
 		}
 		
 		if (keyboard.isPressed(KeyEvent.VK_1)) {
 			try {
 				localMultiplayer();
 				playing = true;
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		else if (keyboard.isPressed(KeyEvent.VK_2)) {
 			try {
 				remoteServer();
 				playing = true;
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		else if (keyboard.isPressed(KeyEvent.VK_3)) {
 			try {
 				remoteClient("71.193.145.84");
 				playing = true;
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void connectTo(String host){
 		try {
 			socket = new Socket(host, 50623);
 		} catch (UnknownHostException ex) {
 			Logger.getLogger(PuzzleFighter.class.getName()).log(Level.SEVERE, null, ex);
 		} catch (IOException ex) {
 			Logger.getLogger(PuzzleFighter.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 	
 	public void host(){
 		try {
 			serv = new ServerSocket(50623);
 			socket = serv.accept();
 			serv.close();
 		} catch (IOException ex) {
 			Logger.getLogger(PuzzleFighter.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}
 }

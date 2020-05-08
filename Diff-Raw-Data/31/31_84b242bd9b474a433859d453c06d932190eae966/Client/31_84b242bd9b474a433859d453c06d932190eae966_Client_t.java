 package clients;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 
 import javax.swing.JOptionPane;
 
 import java.util.LinkedList;
 import java.util.concurrent.LinkedBlockingQueue;
 import physics.Box;
 import server.NetworkEngine;
 import world.GameObject;
 import net.Action;
 import net.NetObject;
 import net.NetState;
 import net.NetStateManager;
 import net.SyncState;
 import jig.engine.CursorResource;
 import jig.engine.FontResource;
 import jig.engine.PaintableCanvas;
 import jig.engine.RenderingContext;
 import jig.engine.ResourceFactory;
 import jig.engine.PaintableCanvas.JIGSHAPE;
 import jig.engine.audio.jsound.AudioClip;
 import jig.engine.hli.ScrollingScreenGame;
 import jig.engine.physics.AbstractBodyLayer;
 import jig.engine.physics.Body;
 import jig.engine.physics.BodyLayer;
 import jig.engine.util.Vector2D;
 import clients.TcpListener;
 import clients.TcpSender;
 import clients.Explode;
 
 /**
  * Client
  */
 public class Client extends ScrollingScreenGame {
 
 	long ms;
 
 	static final String PICTUREBACKGROUND = "res/GameBackground.png";
 	static final String UIGFX = "res/ClientUI.png";
 	static final String SPRITES = "res/2Destruction-spritesheet.png";
 	static final String LEVEL1 = "res/LEVEL1.png";
 	static final String Splash = "res/Splash.png";
 
 	public String SERVER_IP = "127.0.0.1";
 	TcpSender control;
 
 	public static final int SCREEN_WIDTH = 1024, SCREEN_HEIGHT = 768;
 	private static final int MAXJETFUEL = 2000;
 	
 	public static final int SHOOT_THROTTLE = 250; // milliseconds
 	private int shootTimer = 0;
 	private int shootCurTime = 0;
 
 	Action input;
 
 	private NetStateManager netStateMan;
 	private Player player;
 
 	private GameObject jetpack, health, splash;
 
 	private BodyLayer<Body> black = new AbstractBodyLayer.NoUpdate<Body>();
 	private BodyLayer<Body> background = new AbstractBodyLayer.IterativeUpdate<Body>();
 	private BodyLayer<Body> levelmap = new AbstractBodyLayer.IterativeUpdate<Body>();
 	private BodyLayer<Body> front = new AbstractBodyLayer.IterativeUpdate<Body>();
 	private BodyLayer<Body> GUI = new AbstractBodyLayer.IterativeUpdate<Body>();
 
 	GameSprites gameSprites;
 
 	private SyncState state;
 
 	private LinkedBlockingQueue<String> msgQueue = new LinkedBlockingQueue<String>();
 
 	private LinkedList<Explode> boomList = new LinkedList<Explode>();
 	
 	private LinkedList<String> consoleText = new LinkedList<String>(); 
 
 	// private LinkedList<Box> levelboxes = new LinkedList<Box>();
 
 	private int jetFuel = MAXJETFUEL;
 
 	FontResource fontWhite = ResourceFactory.getFactory().getFontResource(
 			new Font("Sans Serif", Font.BOLD, 12), Color.white, null);
 	
 	public String timerString = "";
 
 	public BroadcastListener bListen;
 	
 	// sounds
 	// sounds
 	public static AudioClip rifleSfx;
 	public static AudioClip shotgunSfx ;
 	public static AudioClip grenadeSfx ;
 
 	public Client() {
 
 		super(SCREEN_WIDTH, SCREEN_HEIGHT, false);
 
 		ResourceFactory.getFactory().loadResources("res",
 				"2Destruction-Resources.xml");
 		// newgame = new Button(SPRITE_SHEET + "#Start");
 		// sounds
 		rifleSfx = ResourceFactory.getFactory()
 				.getAudioClip("res/rifle.wav");
 		shotgunSfx = ResourceFactory.getFactory()
 				.getAudioClip("res/shotgun.wav");
 		grenadeSfx = ResourceFactory.getFactory()
 				.getAudioClip("res/grenade.wav");
 	}
 
 	public void startListenServer() {
 		state = new SyncState();
 		bListen = new BroadcastListener(state);
 		bListen.start();
 	}
 
 	public void runSetup() {
 		ResourceFactory factory = ResourceFactory.getFactory();
 		
 		BufferedImage[] b = new BufferedImage[1];
 		BufferedImage grenade = new BufferedImage(6, 10,
 				BufferedImage.TYPE_INT_RGB);
 		b[0] = grenade;
 		Graphics2D g = b[0].createGraphics();
 		g.setColor(Color.getHSBColor((float)0.3216, (float)0.8627,(float)0.5));
 		g.fillOval(0, 0, 6, 10);
 		factory.putFrames("grenade", b);
 		
 		PaintableCanvas.loadDefaultFrames("playerSpawn", 10, 10, 1,
 				JIGSHAPE.CIRCLE, Color.red);
 		PaintableCanvas.loadDefaultFrames("bullet", 5, 5, 1, JIGSHAPE.CIRCLE,
 				Color.WHITE);
 		// 1280, SCREEN_HEIGHT = 1024
 		PaintableCanvas.loadDefaultFrames("blackback", SCREEN_WIDTH,
 				SCREEN_HEIGHT, 1, JIGSHAPE.RECTANGLE, Color.black);
 
 		// will be mostly transparent when done with adding gfx stuff.
 		CursorResource cr = factory.makeCursor(UIGFX + "#Cursor", new Vector2D(
 				15, 15), 1);
 		mouse.setCursor(cr);
 
 		netStateMan = new NetStateManager();
 		gameSprites = new GameSprites();
 		fre.setActivation(true);
 		
 		/* Thread with TCP networking for server specific commands */
 		TcpListener tcpListen = new TcpListener(NetworkEngine.TCP_CLIENT_PORT,
 				msgQueue);
 		tcpListen.start();
 
 		/* Send TCP data via this object */
 		control = new TcpSender(SERVER_IP, NetworkEngine.TCP_PORT);
 
 		player = new Player(0, control);
 		input = new Action(0, Action.INPUT);
 		player.join(SERVER_IP);
 
 		// Create background object and add to layer, to window.
 		gameObjectLayers.clear();
 		Box back = new Box("blackback");
 		black.add(back);
 
 		// background graphic.
 		back = new Box(PICTUREBACKGROUND + "#background");
 		background.add(back);
 		// ui elements
 		health = new GameObject(UIGFX + "#Health");
 		health.setPosition(new Vector2D(20, 20));
 		GUI.add(health);
 		jetpack = new GameObject(UIGFX + "#JetFuel");
 		jetpack.setPosition(new Vector2D(20, 31));
 		GUI.add(jetpack);
 		splash = new GameObject(Splash + "#Splash");
 		splash.setPosition(new Vector2D(0, 0));
 		GUI.add(splash);
 		splash.setActivation(false);		
 
 		// Warning heap increased needed for this sprite.
 		int fudgex = -40, fudgey = 5;
 		 Box level = new Box(LEVEL1 + "#LEVEL1");
 		 level.setPosition(new Vector2D(-level.getWidth()/2 + fudgex,-level.getHeight()/2 + fudgey));
 		 levelmap.add(level);
 		// end warning
 
 		// Control of layering
 		gameObjectLayers.add(background);
 		gameObjectLayers.add(gameSprites.getLayer());
 		gameObjectLayers.add(levelmap);
 		gameObjectLayers.add(front);
 		// whatever layers not added into gameObjectLayers will be manually
 		// rendered.
 
 		showPrivateMessage("No connection to server");
 
 	}
 	
 	public void showSplash(boolean show) {
 		splash.setActivation(show);
 	}
 
 	/**
 	 * Handle keyboard strokes for movement
 	 */
 	public void keyboardMovementHandler(long deltaMs) {
 		if (!netStateMan.getState().objectList.containsKey(player.getID())) {
 			// System.out.println("Client keyboard: no ID");
 			return;
 		}
 
 		Vector2D mousePos = screenToWorld(new Vector2D(mouse.getLocation().getX(), mouse.getLocation().getY()));
 		
 		// Sending cursor input information to server every frame to display correct facing of the sprite
 		// is silly. 
 		Vector2D playerPos = netStateMan.getState().objectList.get(player.getID()).getPosition();
 		if (mousePos.getX() < playerPos.getX()) 
 			input.faceLeft = true;
 		else
 			input.faceLeft = false;
 		
 		keyboard.poll();
 		
 		// Player is alive, do some input reading
 		if (netStateMan.getState().objectList.get(player.getID()).getHealth() > 0) {
 			input.crouch = keyboard.isPressed(KeyEvent.VK_DOWN)
 					|| keyboard.isPressed(KeyEvent.VK_S);
 			// GameObject p = gameSprites.spriteList.get(player.getID());
 			if (jetFuel > 0
 					&& (keyboard.isPressed(KeyEvent.VK_UP) || keyboard
 							.isPressed(KeyEvent.VK_W))) {
 				input.jet = true;
 				jetFuel -= 2;
 			} else {
 				input.jet = false;
 			}
 			if (!(keyboard.isPressed(KeyEvent.VK_UP) || keyboard
 					.isPressed(KeyEvent.VK_W))
 					&& jetFuel < MAXJETFUEL)
 				++jetFuel;
 
 			input.left = keyboard.isPressed(KeyEvent.VK_LEFT)
 					|| keyboard.isPressed(KeyEvent.VK_A);
 			input.right = keyboard.isPressed(KeyEvent.VK_RIGHT)
 					|| keyboard.isPressed(KeyEvent.VK_D);
 			input.jump = keyboard.isPressed(KeyEvent.VK_SPACE);
 
 			if (keyboard.isPressed(KeyEvent.VK_1)) {
 				input.weapon = 1;
 			} else if (keyboard.isPressed(KeyEvent.VK_2)) {
 				input.weapon = 2;
 			} else if (keyboard.isPressed(KeyEvent.VK_3)) {
 				input.weapon = 3;
 			} else {
 				input.weapon = 0;
 			}
 			
 			// Throttling shooting, can't shoot every frame because TCP can't handle so much traffic
 			shootCurTime += deltaMs;
 			if (mouse.isLeftButtonPressed()) {
 				if (shootTimer < shootCurTime) {
 					player.shoot(mousePos);
 					shootTimer = shootCurTime + SHOOT_THROTTLE;
 				}
 			}
 			
 			player.move(input);
 			
 		// Player is dead, we can only request to respawn at some location now	
 		} else {
 			
 			input.crouch = false;
 			input.jet = false;
 			input.left = false;
 			input.right = false;
 			input.jump = false;
 			if (keyboard.isPressed(KeyEvent.VK_F1)|| keyboard.isPressed(KeyEvent.VK_7)) {
 				player.spawn(0);
 			} else if (keyboard.isPressed(KeyEvent.VK_F2)|| keyboard.isPressed(KeyEvent.VK_8)) {
 				player.spawn(1);
 			} else if (keyboard.isPressed(KeyEvent.VK_F3)|| keyboard.isPressed(KeyEvent.VK_9)) {
 				player.spawn(2);
 			} else if (keyboard.isPressed(KeyEvent.VK_F4)|| keyboard.isPressed(KeyEvent.VK_0)) {
 				player.spawn(3);
 			} else {
 				player.spawn(-1);
 			}		
 			player.move(input);
 		}
 		
 		// Debug
 		if (keyboard.isPressed(KeyEvent.VK_B)) {
 			addBoom(mousePos);
 		}
 
 	}
 
 
 	public void processPrivateMsg() {
 		if (msgQueue.isEmpty())
 			return;
 
 		// When joining the game, request for a player ID is sent and this loop
 		// awaits Server's response
 		while (msgQueue.size() > 0) {
 			Action a = netStateMan.prot.decodeAction(msgQueue.poll());
 			
 			switch (a.getType()) {
 				case Action.JOIN_ACCEPT:
 					Integer newID = Integer.valueOf(a.getMsg()).intValue();
 					player.setID(newID);
 					input.setID(newID);
 					player.state = Player.JOINED;
 					showPrivateMessage("Connected.");
 					break;
 				case Action.LEAVE_SERVER:
 					showPrivateMessage("Connection to the server lost.");
 					player.state = Player.WAITING;	
 					break;
 				case Action.TALK:
 					showPrivateMessage(a.getMsg());
 					break;
 			}
 		}
 
 		// Player has not joined a server game yet, still waiting for server's
 		// response
 		if (player.state != Player.JOINED) {
 			showPrivateMessage("Conecting to the server...");
 		}
 
 	}
 
 	public void update(long deltaMs) {
 		processPrivateMsg();
 		processActions(netStateMan.getState());
 		
 		if (player.state != Player.JOINED)
 			return;
 
 		super.update(deltaMs);
 
 		Vector2D mousePos = screenToWorld(new Vector2D(mouse.getLocation()
 				.getX(), mouse.getLocation().getY()));
 
 		// get messages from the server
 		String s = state.get();
 		// ms += deltaMs;
 		if (s != null) {
 			// System.out.println("server: " + ms);
 			netStateMan.sync(s);
 			s = null;
 		} else {
 			// System.out.println("client");
 			// if no message from the server, update position of objects with
 			// local deltaMs
 			for (NetObject n : netStateMan.getState().getNetObjects()) {
 				n.update(deltaMs);
 				GameObject a = gameSprites.spriteList.get(n.getId());
 				if (a.getType() == GameObject.PLAYER) {
 
 				}
 			}
 		}
 		gameSprites.sync(netStateMan);
 
 		// Move background to 90% of cursor world coordinate location.
 		// as seen from player view
 		GameObject p = gameSprites.spriteList.get(player.getID());
 		Vector2D bgPos = null;
 		if (p != null && p.getCenterPosition() != null) {
 
 			// Adjust background position relative to mouse cursor to create the
 			// effect of depth
 			double bg_deltaPos = 0.3;
 			bgPos = new Vector2D((p.getCenterPosition().getX() + mousePos
 					.getX())
 					/ 2 * bg_deltaPos, (p.getCenterPosition().getY() + mousePos
 					.getY())
 					/ 2 * bg_deltaPos);
 			background.get(0).setCenterPosition(bgPos);
 
 			centerOnPoint(
 					(int) (p.getCenterPosition().getX() + mousePos.getX()) / 2,
 					(int) (p.getCenterPosition().getY() + mousePos.getY()) / 2);
 
 			// it is assumed that health is in range [0-2000].
 			// System.out.println(hl);'
 			int hl = netStateMan.getState().objectList.get(player.getID())
 					.getHealth();
 			if (hl > 0) {
 				int hframe = 25 - (int) ((((double) hl) / 2000.0) * 25);
 				// System.out.println(hframe + " hframe, client");
 				health.setFrame(hframe);
 			} else {
 				health.setFrame(25);
 				//msg = "Dead - press f1-4 to respawn.";
 			}
 			if (jetFuel > 0) {
 				int jframe = 25 - (int) ((((double) jetFuel) / 2000.0) * 25);
 				// System.out.println(jframe + " jframe " + jetFuel +
 				// "jetfuel, client");
 				jetpack.setFrame(jframe);
 			} else {
 				jetpack.setFrame(25);
 			}
 		}
 
 		processActions(netStateMan.getState());
 		keyboardMovementHandler(deltaMs);
 	}
 	
 	/**
 	 * Server can send different type of Actions for clients to perform
 	 * such as displaying explosion animation or playing a sounds
 	 * 
 	 * @param alist
 	 */
 	public void processActions(NetState state) {
 		if (state.getActions().isEmpty())
 			return;
 		double dist;
 		for (Action a : state.getActions()) {
 			switch(a.getType()) {
 				case Action.EXPLOSION:
 					addBoom(a.getArg());
 					dist = a.getArg().distance2(gameSprites.spriteList.get(player.getID()).getCenterPosition());
					grenadeSfx.play(Math.min(50000/dist, 1)*.1);
 					break;
 				case Action.TALK:
 					showPublicMessage(a.getMsg());
 					break;
 				case Action.RIFLESFX:
 					dist = a.getArg().distance2(gameSprites.spriteList.get(player.getID()).getCenterPosition());
					rifleSfx.play(Math.min(50000/dist, 1)*.1);
 					break;
 				case Action.SHOTGUNSFX:
 					dist = a.getArg().distance2(gameSprites.spriteList.get(player.getID()).getCenterPosition());
					shotgunSfx.play(Math.min(50000/dist, 1)*.1);
 					break;
 				case Action.TIMER:
 					timerString = "Time: "+a.getMsg().charAt(0) + a.getMsg().charAt(1);
 					break;
 			}
 		}
 		
 		// We process actions only once
 		state.clearActions();
 	}
 
 	/**
 	 * Add a public message to the console
 	 * 
 	 * @param msg
 	 */
 	public void showPublicMessage(String msg) {
 		consoleText.add("PUB:"+msg);
 	}
 	
 	/**
 	 * Add a private message to the console
 	 * @param msg
 	 */
 	public void showPrivateMessage(String msg) {
 		consoleText.add("PRIV:"+msg);
 	}
 	
 	
 	private void addBoom(Vector2D loc) {
 		System.out.println(boomList.size());
 		if (boomList.size() < 100) {
 			// make new one and add it.
 			Explode boomy = new Explode(SPRITES + "#Explosion");
 			boomy.setCenterPosition(loc);
 			front.add(boomy);
 			boomList.add(boomy);
 		} else {
 			// Recycle oldest one.
 			Explode boomy = boomList.pop();
 			// set new placement
 			boomy.setCenterPosition(loc);
 			// reset boomy
 			boomy.reset();
 			// add to back of list.
 			boomList.add(boomy);
 		}
 		System.out.println("Boom: " + loc + " client");
 
 	}
 
 	public void render(RenderingContext rc) {
 		black.render(rc);// draw at screen coordities.
 		super.render(rc);
 		GUI.render(rc);
 		showConsoleLines(5, rc);
 		
 		fontWhite.render(timerString, rc, AffineTransform
 				.getTranslateInstance(SCREEN_WIDTH - 100, 5));	
 	}
 
 	/**
 	 * This prints console text on top of client screen
 	 * 
 	 * @param n - number of lines to display
 	 * @param rc
 	 */
 	public void showConsoleLines(int n, RenderingContext rc) {
 		int consoleSize = consoleText.size();
 		
 		if (consoleSize < n)
 			n = consoleSize;
 		
 		for(int i=consoleSize-n; i<consoleText.size();i++) {
 			fontWhite.render(consoleText.get(i), rc, AffineTransform
 					.getTranslateInstance(20, 45+((i-(consoleSize-n))*15)));				
 		}
 	}
 	
 	public String getIP() {
 		int as = 0;
 		String res = null;
 		while (as == 0) {
 			String s = JOptionPane.showInputDialog("Enter server IP address.");
 			if (s == null)
 				return null;
 			String[] a = s.split("\\.");
 			if (a.length == 4) {
 				int a1 = java.lang.Integer.parseInt(a[0]);
 				int a2 = java.lang.Integer.parseInt(a[1]);
 				int a3 = java.lang.Integer.parseInt(a[2]);
 				int a4 = java.lang.Integer.parseInt(a[3]);
 				System.out.println(a1 + "." + a2 + "." + a3 + "." + a4
 						+ " result of user input");
 				// check formatting if its properly done.
 				if (a1 > 0 && a1 < 254 && a2 >= 0 && a2 < 254 && a3 >= 0
 						&& a3 < 254 && a4 > 0 && a4 < 254) {
 					res = String.valueOf(a1 + "." + a2 + "." + a3 + "." + a4);
 					as = 1;
 				} else {
 					JOptionPane
 							.showMessageDialog(
 									null,
 									"Misformatted IP address "
 											+ s
 											+ "\n\nNeed to be X.X.X.X with X in range [0-254].");
 				}
 			}
 		}
 		return res;
 	}
 
 	public static void main(String[] vars) {
 		Client c = new Client();
 		int as = 0;
 		c.startListenServer();
 		// just grab 100 states see what servers is out there.
 		// blistener stores ip addresses.
 
 		while (as == 0) {
 			for (int i = 0; i < 100; i++)
 				c.state.get();
 			
 
 			String[] A = new String[c.bListen.ips.size() + 2];
 			A[0] = "<Refresh List>";
 			int z = 0;
 			for (z = 0; z < c.bListen.ips.size(); z++)
 				A[z + 1] = c.bListen.ips.get(z);
 			A[z + 1] = "<Manual IP input>";
 
 			String input = (String) JOptionPane.showInputDialog(null,
 					"Servers found:", "Server ip address",
 					JOptionPane.QUESTION_MESSAGE, null, A, // Array of choices
 					A[0]); // Initial choice
 			System.out.println(input);
 			if (input == null) {
 				c.bListen.close();
 				System.exit(0);
 			} else if (input.compareTo("<Manual IP input>") == 0) {
 				c.SERVER_IP = c.getIP();
 				if (c.SERVER_IP == null) {
 					c.bListen.close();
 					System.exit(0);
 				}
 				as = 1;
 			} else if (input.compareTo("<Refresh List>") != 0) {
 				c.SERVER_IP = input;
 				as = 1;
 			}
 
 		}
 		
 		System.out.println(c.SERVER_IP + " final ip chosen,  client");
 
 
 		c.runSetup();
 		c.run();
 		c.bListen.close();
 	}
 }

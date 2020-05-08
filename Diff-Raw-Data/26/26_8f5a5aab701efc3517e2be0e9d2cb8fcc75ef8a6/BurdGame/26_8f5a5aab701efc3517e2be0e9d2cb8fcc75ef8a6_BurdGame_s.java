 /**
  * This will be the main class for a simple game that uses toritools.
  * 
  * Windowed mode does not yet work as intended.  Issue with VIEWPORT and render()
  * 
  * Note the Graphics class is Graphics2D, not Graphics3D.  This inconsistency is not yet
  * corrected in current versions of Java, so we must check the superclass.
  * Exempli gratia:
  * 		if (g instanceof Graphics2D) {
  Graphics2D g2 = (Graphics2D)g;
  g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
  RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
  }g.drawString("abcdefghijklmnopqrstuvwxyz.", 200, 200);
  * 
  * @author toriscope
  */
 package burd;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.DisplayMode;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 import toritools.controls.KeyHolder;
 import toritools.entity.Entity;
 import toritools.entity.Level;
 import toritools.io.FontLoader;
 import toritools.io.Importer;
 import toritools.math.MidpointChain;
 import toritools.math.Vector2;
 import toritools.scripting.ScriptUtils;
 import toritools.timing.StopWatch;
 import burd.customscripts.BreadScript;
 import burd.customscripts.BurdScript;
 import burd.customscripts.ScrollScript;
 
 public class BurdGame {
 
 	{
 		FontLoader.loadFonts(new File("burd/fonts"));
 	}
 
 	public static String savePrefix = "burd2";
 
 	private int resolutionWidth = Toolkit.getDefaultToolkit().getScreenSize().width,
 			resolutionHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
 	private final Vector2 VIEWPORT = new Vector2(resolutionWidth,
 			resolutionHeight);
 
 	private MidpointChain camera = new MidpointChain(new Vector2(),
 			new Vector2(), 10);
 	/**
 	 * The current working level.
 	 */
 	private Level level;
 	public static Level newLevel;
 	public static boolean debug = false;
 	public Vector2 zoom = new Vector2(1, 1);
 	public static KeyHolder keys = new KeyHolder();
 	public static boolean inDialog = false;
 	private static String displayString = "";
 	private static StopWatch stopWatch = new StopWatch();
 
 	/**
 	 * Application initiation
 	 * 
 	 * @param args
 	 *            Commandline arguments
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		BurdGame sampleGame = new BurdGame();
 
 		sampleGame.init();
 		sampleGame.run();
 	}
 
 	/**
 	 * Interface to video card/chip for hardware acceleration
 	 */
 	private GraphicsDevice gd;
 
 	/**
 	 * JFrame with game title
 	 */
 	private JFrame frame = new JFrame("burd");
 	@SuppressWarnings("serial")
 	private JPanel panel = new JPanel() {
 		public void paintComponent(final Graphics g) {
 			render(g);
 		}
 	};
 
 	Cursor hiddenCursor = Toolkit.getDefaultToolkit().createCustomCursor(
 			new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB),
 			new Point(0, 0), "Hidden Cursor");
 
 	/**
 	 * Initialize the game
 	 * 
 	 * @throws Exception
 	 *             if init fails
 	 */
 	public void init() throws IOException {
 		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment
 				.getLocalGraphicsEnvironment();
 		gd = graphicsEnvironment.getDefaultScreenDevice();
 
 		frame.setCursor(hiddenCursor); // Hide cursor by default
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setPreferredSize(new Dimension(800, 600)); // Default windowed
 															// dimensions
 		frame.add(panel);
 		frame.addKeyListener(keys);
 		frame.setFocusable(true);
 
 		try {
 			ScriptUtils.loadProfileVariables(savePrefix);
 		} catch (Exception e) {
 			ScriptUtils.saveProfileVariables(savePrefix);
 		}
 
 		level = Importer.importLevel(new File("burd/level" + ++currLevel
 				+ ".xml"));
 
 		setupLevel();
 
 		setFullScreen(true);
 		frame.requestFocusInWindow();
 	}
 
 	private Timer timer;
 
 	/**
 	 * Runs the game (the "main loop")
 	 */
 	private void run() {
 		/*
 		 * This is not 60 fps, but really close!
 		 */
 		timer = new Timer(17, new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				logic();
 				frame.repaint();
 			}
 		});
 		timer.start();
 	}
 
 	/**
 	 * Do all calculations, handle input, etc.
 	 */
 	private void logic() {
 
 		// Win condition
 		if (level.getEntitiesWithType("bread").isEmpty()) {
 			nextLevel();
 		}
 
 		displayString = null;
 
 		if (newLevel != null) {
 			level.onDeath(level, true);
 			level = newLevel;
 			setupLevel();
 			newLevel = null;
 		}
 
 		level.onUpdate();
 
 		if (keys.isPressed(KeyEvent.VK_P)) {
 			nextLevel();
 		}
 
 		if (keys.isPressed(KeyEvent.VK_I)) {
 			zoom.x += .1;
 			zoom.y += .1;
 		}
 		if (keys.isPressed(KeyEvent.VK_O)) {
 			zoom.x -= .1;
 			zoom.y -= .1;
 			if (zoom.x < 1)
 				zoom.set(1, 1);
 		}
 
 		debug = keys.isPressedThenRelease(KeyEvent.VK_K) ? !debug : debug;
 
 		if (keys.isPressed(KeyEvent.VK_ESCAPE)) {
 			frame.setCursor(null); // Restore cursor for menu
 			System.exit(0);
 		}
 
 		keys.freeQueuedKeys();
 
 		camera.setA(level.getEntityWithId("player").pos.clone());
 		camera.smoothTowardA();
 	}
 
 	private void setupLevel() {
 
 		stopWatch.start();
 
 		try {
 			level.getEntityWithId("player").script = new BurdScript();
 			level.getEntityWithId("player").visible = false;
 		} catch (final NullPointerException e) {
 			JOptionPane.showMessageDialog(null,
 					"This level is missing a Burd (player.entity)!");
 			System.exit(0);
 		}
 
 		level.onSpawn();
 
 		/*
 		 * Special spawns, will be fixed.
 		 */
 
 		for (Entity e : level.getEntitiesWithType("bread")) {
 			e.script = new BreadScript();
 			e.onSpawn(level);
 		}
 
 		for (Entity e : level.getEntitiesWithType("hScroll")) {
 			e.script = new ScrollScript(true);
 			e.onSpawn(level);
 		}
 
 		for (Entity e : level.getEntitiesWithType("vScroll")) {
 			e.script = new ScrollScript(false);
 			e.onSpawn(level);
 		}
 	}
 
 	private Image bufferImage = new BufferedImage((int) VIEWPORT.x,
 			(int) VIEWPORT.y, BufferedImage.TYPE_INT_RGB);
 	private Graphics bufferGraphics = bufferImage.getGraphics();
 
 	private void render(final Graphics rootCanvas) {
 		rootCanvas
 				.setFont(new Font("Earth's Mightiest", Font.TRUETYPE_FONT, 40));
 		if (level == null) {
 			rootCanvas.clearRect(0, 0, (int) VIEWPORT.x, (int) VIEWPORT.y);
 			rootCanvas.setColor(Color.BLACK);
 			rootCanvas.drawString("Loading...", (int) VIEWPORT.x / 2,
 					(int) VIEWPORT.y / 2);
 			return;
 		}
 
 		Vector2 offset = VIEWPORT.scale(.5f).sub(camera.getB());
 
 		bufferGraphics.setColor(Color.GRAY);
 		bufferGraphics.fillRect(0, 0, (int) VIEWPORT.x, (int) VIEWPORT.y);
 		for (int i = level.layers.size() - 1; i >= 0; i--)
 			for (Entity e : level.layers.get(i)) {
 				if (debug) {
 					bufferGraphics.setColor(Color.RED);
 					bufferGraphics.drawRect((int) (e.pos.x + offset.x),
 							(int) (e.pos.y + offset.y), (int) e.dim.x,
 							(int) e.dim.y);
 				}
 				if (e.visible)
 					e.draw(bufferGraphics, offset);
 			}
 		level.getEntityWithId("player").draw(bufferGraphics, offset);
 
 		// Draw to the actual screen, scaled.
 		int xScalePix = (int) (zoom.x * VIEWPORT.x);
 		int yScalePix = (int) (zoom.y * VIEWPORT.y);
 		rootCanvas.drawImage(bufferImage,
 				-(int) ((xScalePix - VIEWPORT.x) / 2),
 				-(int) ((yScalePix - VIEWPORT.y) / 2), xScalePix, yScalePix,
 				null);
 		rootCanvas.setColor(Color.BLACK);
 		String infoString = "[I/O] Zoom: " + zoom.x + "    [K] Debug Mode: "
				+ debug + "    [P] Next Level    [Esc] Quit";
 
 		rootCanvas.drawString(infoString, 5, (int) VIEWPORT.y - 5);
 
 		/*
 		 * HUD
 		 */
 		rootCanvas.drawString("Time: " + stopWatch.getElapsedTimeSecs(),
 				(int) 20, 40);
 
 		// List<Entity> breads;
 		// if (!(breads = level.getEntitiesWithType("bread")).isEmpty()) {
 		int xIndex = 0;
 		for (Entity bread : level.getEntitiesWithType("bread")) {
 			bread.sprite.draw(rootCanvas, bread, new Vector2(20 + xIndex++
 					* bread.dim.x * 1.5f, 50), bread.dim);
 		}
 		// } else {
 		// int xIndex = 0;
 		// for (Entity nest : level.getEntitiesWithType("nest")) {
 		// level.getEntitiesWithType("nest").get(0).sprite.draw(
 		// rootCanvas, nest, new Vector2(20 + xIndex++
 		// * nest.dim.x * 1.5f, 50), nest.dim);
 		// }
 		// }
 
 		if (displayString != null) {
 			rootCanvas.drawString(displayString, (int) VIEWPORT.x / 2,
 					(int) VIEWPORT.y / 2 + 64);
 		}
 	}
 
 	/**
 	 * Display mode of user's monitor. Uses 32-bit color depth.
 	 */
 	private DisplayMode displayMode = new DisplayMode(resolutionWidth,
 			resolutionHeight, 32, DisplayMode.REFRESH_RATE_UNKNOWN);
 	@SuppressWarnings("unused")
 	private boolean isInFullScreen;
 
 	/**
 	 * Sets full screen or reverts screen to normal. In the future, this method
 	 * will be able to restore to windowed mode and not simply hide it.
 	 * 
 	 * To my knowledge, full screen only supports one monitor, for I have no
 	 * other monitors to test with.
 	 * 
 	 * @param isFullScreen
 	 *            Enable (true) or disable (false) full screen mode
 	 */
 	public void setFullScreen(boolean isFullScreen) {
 		if (isFullScreen) {
 			frame.dispose(); // Without disposing first, the frame remains
 								// displayed.
 			frame.setUndecorated(true);
 			frame.setResizable(false); // Safety check
 			gd.setFullScreenWindow(frame); // This must be placed after
 											// setUndecorated.
 			isInFullScreen = true;
 
 			if (displayMode != null && gd.isDisplayChangeSupported()) {
 				try {
 					gd.setDisplayMode(displayMode);
 				} catch (Exception e) {
 					System.out.println("Error: Could not set display mode!");
 				}
 			}
 		} else {
 			Window window = gd.getFullScreenWindow();
 
 			if (window != null) {
 				window.dispose(); // Dispose resources when closed.
 			}
 			frame.setUndecorated(false);
 			frame.setResizable(true);
 			frame.pack();
 			frame.setVisible(true);
 			gd.setFullScreenWindow(null); // Actual revert of window
 			isInFullScreen = false;
 		}
 	}
 
 	/**
 	 * On next update, switch to a new level.
 	 * 
 	 * @param newLevel
 	 *            the level to switch to.
 	 */
 	public static void warpToLevel(final Level newLevel) {
 		BurdGame.newLevel = newLevel;
 		keys.clearKeys();
 	}
 
 	/**
 	 * Set a string to be displayed in a prompt on screen for 1 frame.
 	 * 
 	 * @param s
 	 *            the string to set.
 	 */
 	public static void setDisplayPrompt(final String s) {
 		displayString = s;
 	}
 
 	private static int currLevel = 0;
 
 	public static void nextLevel() {
 		try {
 			File levelFile = new File("burd/level" + ++currLevel + ".xml");
 			if (levelFile.canRead()) {
 				System.out.println(currLevel);
 				warpToLevel(Importer.importLevel(levelFile));
 			} else {
 				JOptionPane.showMessageDialog(null, "YOU BEAT THE GAME!");
 				System.exit(0);
 			}
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			System.exit(0);
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private void drawChain(final Graphics g, final Vector2[] chain,
 			final Vector2 offset) {
 		for (int i = 1; i < chain.length; i++) {
 			g.drawLine((int) (chain[i - 1].x + offset.x),
 					(int) (chain[i - 1].y + offset.y),
 					(int) (chain[i].x + offset.x),
 					(int) (chain[i].y + offset.y));
 		}
 	}
 }

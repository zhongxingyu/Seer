 package game;
 
 import game.console.ConsoleWindow;
 import game.screen.Screen;
 import game.screen.ScreenCrash;
 import game.screen.ScreenLoading;
 import game.screen.ScreenMainMenu;
 import game.sound.SoundManager;
 import game.utils.FileSaver;
 import game.utils.SpriteSheet;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.HashMap;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 
 
 
 public class Game extends Canvas implements KeyListener, MouseListener,
 		FocusListener {
 
 	private static final long serialVersionUID = 1L;
 	public static String TITLE = "Substrate", VERSION = "0.8";
 	public static int WIDTH = 800, HEIGHT = 600, SCALE = 2, SIZE = SCALE * 32, XTILES = 25, YTILES = 16;
 	public HashMap<String, String> SETTINGS = new HashMap<String, String>();
 	
 
 	public BufferStrategy strategy;
 
 	public SpriteSheet sheet, sheetExplosions, sheetTiles, sheetEntities,
 			sheetUI, sheetTriggers;
 	public BufferedImage loadingScreen, winScreen, logo;
 	public Font font;
 	public Graphics2D g;
 	public Options settings;
 	public SoundManager soundman;
 	public Screen currentScreen;
 	private static ConsoleWindow console = null;
 	private static JFrame container;
 
 	public Controls controls = new Controls();
 	private int tps = 0, fps = 0;
 
 	public boolean gameRunning = true;
 
 	public Game() {
 
 		setSize(WIDTH, HEIGHT);
 		setBounds(0, 0, WIDTH, HEIGHT);
 		setIgnoreRepaint(true);
 		addKeyListener(this);
 		addFocusListener(this);
 		addMouseListener(this);
 
 	}
 
 	public static void main(String[] args) {
 		
 		Game game = new Game();
 		container = new JFrame(TITLE);
 		container.setVisible(true);
 		container.setFocusable(true);
 		container.add(game, BorderLayout.CENTER);
 		container.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		container.pack();
 		container.setIgnoreRepaint(false);
 		container.setResizable(true);
 		game.start();
 
 	}
 	
 	
 
 	public synchronized void start() {
 		try {
 			gameLoop();
 		} catch (Exception e) {
 			String stacktrace = FileSaver.getStackTrace(e);
 			String crashDump = "Crashed on screen: " + currentScreen.toString()
 					+ "\nGame version: " + Game.VERSION + "\nFPS:"
 					+ Integer.toString(fps) + "\nTPS:" + Integer.toString(tps)
 					+ "\nStack trace: " + stacktrace + "\nSettings:\n "
 					+ SETTINGS.toString().replace(",", "\n")
 					+ "\n---SYSTEM STATS---" + "\nOperating System: "
 					+ System.getProperty("os.name") + "_"
 					+ System.getProperty("os.version") + "\nJava Version: "
 					+ System.getProperty("java.version");
 
 			setScreen(new ScreenCrash(WIDTH, HEIGHT, null, crashDump));
 			render();
 		}
 		requestFocus();
 	}
 
 	public synchronized void stop() {
 		gameRunning = false;
 		System.exit(ABORT);
 	}
 
 	public void shutdown() {
 		gameRunning = false;
 		log("Saving files...");
 		FileSaver.save(SETTINGS, "settings.dat");
 		FileSaver.save(controls.keyMap, "keymap.dat");
 		g.clearRect(0, 0, Game.WIDTH, Game.HEIGHT);
 		log("Copyright UnacceptableUse 2013");
 		log("Shutting down game, peace.");
 		try {
 			Thread.sleep(1000L);
 		} catch (InterruptedException e) {
 		}
 		System.exit(0);
 	}
 
 	public Font getFontRenderer() {
 		return font;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void init() {
 
 		createBufferStrategy(2);
 		strategy = getBufferStrategy();
 
 		log("Loading...");
 		try {
 			loadingScreen = ImageIO.read(Game.class
 					.getResource("/res/loading.png"));
 		} catch (IOException e1) {
 			log("Unable to load loading screen.");
 			e1.printStackTrace();
 			throw new RuntimeException("Unable to load loading.png.");
 		}
 		File f = new File(FileSaver.getCleanPath() + "\\settings.txt");
 		if (!f.exists()) {
 			log("Settings file does not exist!");
 			SETTINGS.put("Sound", "OFF");
 			SETTINGS.put("Debug", "ON");
 			SETTINGS.put("Music", "OFF");
 			SETTINGS.put("Volume", "100");
 			SETTINGS.put("Cheats", "OFF");
 			SETTINGS.put("HasDoneIntro", "OFF");
 			SETTINGS.put("GatherStats", "ON");
 			SETTINGS.put("SubmitTimes", "ON");
 			SETTINGS.put("ExtendedDebug", "ON");
 			SETTINGS.put("UseFontRecolouring", "ON");
			FileSaver.savePropertiesFile(SETTINGS, FileSaver.getCleanPath()+ "/settings.txt");
 		} else {
 			log("Found settings.dat, loading...");
			SETTINGS = FileSaver.readPropertiesFile(FileSaver.getCleanPath()+ "/settings.txt");
 			for (int i = 0; i < SETTINGS.keySet().size(); i++) {
 				String setting = (String) SETTINGS.keySet().toArray()[i];
 				String value = SETTINGS.get(setting);
 				if (!value.equals("ON") && value.contains("ON")) {
 					SETTINGS.put(setting, "ON");
 				} else if (!value.equals("OFF") && value.contains("OFF")) {
 					SETTINGS.put(setting, "OFF");
 				}
 			}
 		}
 
 		settings = new Options(SETTINGS);
 		
 		setScreen(new ScreenLoading(Game.WIDTH, Game.HEIGHT, sheet));
 		render();
 
 		if (settings.getSetting("Cheats").equals("ON")) {
 			console = new ConsoleWindow(this);
 		}
 
		f = new File(FileSaver.getCleanPath() + "/maps/");
 		if (!f.exists()) {
 			log("Creating directory " + f.getAbsolutePath());
 			f.mkdirs();
 		}
 
 		log("Loading spritesheets...");
 		try {
 			sheet = new SpriteSheet(ImageIO.read(Game.class
 					.getResource("/res/icons.png")), 16);
 		} catch (Exception e) {
 			log("Sheet icon.png failed to load");
 			throw new RuntimeException("Sheet loading failed!");
 		}
 		try {
 			sheetUI = new SpriteSheet(ImageIO.read(Game.class
 					.getResource("/res/ui.png")), 32);
 		} catch (Exception e) {
 			log("Sheet ui.png failed to load");
 			throw new RuntimeException("Sheet ui.png failed to load "
 					+ e.getMessage());
 		}
 		try {
 			sheetTiles = new SpriteSheet(ImageIO.read(Game.class
 					.getResource("/res/tiles.png")), 32);
 		} catch (Exception e) {
 			log("Sheet tiles.png failed to load");
 			throw new RuntimeException("Sheet tiles.png failed to load");
 		}
 		try {
 			sheetEntities = new SpriteSheet(ImageIO.read(Game.class
 					.getResource("/res/objects.png")), 32);
 		} catch (Exception e) {
 			log("Sheet object.png failed to load");
 			throw new RuntimeException("Sheet object.png failed to load");
 		}
 		try {
 			sheetExplosions = new SpriteSheet(ImageIO.read(Game.class
 					.getResource("/res/explosion.png")), 32);
 		} catch (Exception e) {
 			log("Sheet explosion.png failed to load");
 			throw new RuntimeException("Sheet explosion.png failed to load");
 		}
 		try {
 			sheetTriggers = new SpriteSheet(ImageIO.read(Game.class
 					.getResource("/res/puzzle.png")), 32);
 		} catch (Exception e) {
 			log("Sheet puuzle.png failed to load");
 			throw new RuntimeException("Sheet puzzle.png failed to load");
 		}
 		try {
 			winScreen = ImageIO.read(Game.class
 					.getResource("/res/win.png"));
 		} catch (IOException e1) {
 			log("Unable to load win screen.");
 			e1.printStackTrace();
 			throw new RuntimeException("Unable to load win.png");
 		}
 		
 		try {
 			logo = ImageIO.read(Game.class
 					.getResource("/res/logo.png"));
 		} catch (IOException e1) {
 			log("Unable to load logo");
 			e1.printStackTrace();
 			throw new RuntimeException("Unable to load logo.png");
 		}
 		
 		try {
 			font = new Font(ImageIO.read(Game.class.getResource("/res/font.png")), strategy.getDrawGraphics(),
 					this);
 		} catch (IOException e1) {
 			log("Unable to load font.");
 			e1.printStackTrace();
 			throw new RuntimeException("Unable to load font sheet.");
 		}
 	
 		f = new File("keymap.dat");
 		if (!f.exists()) {
 			FileSaver.save(controls.keyMap, "keymap.dat");
 		} else {
 			controls.keyMap = (HashMap<Integer, Integer>) FileSaver
 					.load("keymap.dat");
 		}
 
 		soundman = new SoundManager(this);
 
 		log("Trying to send statistics...");
 		if (SETTINGS.get("GatherStats").equals("ON")) {
 			try
 			{
 				FileSaver.getURL("http://fightthetoast.co.uk/assets/stats.php?os="
 										+ URLEncoder.encode(System.getProperty("os.name"), "UTF-8") + "_"
 										+ System.getProperty("os.version")
 										+ "&game=" + Game.VERSION
 										+ "&java="
 										+ System.getProperty("java.version"));
				log("Stat sending successful!");
 			} catch (Exception e)
 			{
 				log("Unable to send statistics...");
 				e.printStackTrace();
 			}
 		} else {
 			log("Not sending stats, opted out");
 			System.out.println(SETTINGS.get("GatherStats"));
 		}
 		try {
 			Thread.sleep(4000);
 		} catch (InterruptedException e2) {
 		}
 		setScreen(new ScreenMainMenu(WIDTH, HEIGHT, sheet));
 
 		log("Loaded!");
 	}
 
 	public static void log(String s) {
 		if (console != null) {
 			console.log(s);
 		} else {
 			System.out.println(s);
 		}
 	}
 
 	public void gameLoop() {
 		long lastTime = System.nanoTime();
 		double unprocessed = 0;
 		double nsPerTick = 1000000000.0 / 60.0;
 
 		int ticks = 0;
 		int frames = 0;
 		long lastTimer1 = System.currentTimeMillis();
 
 		init();
 
 		while (gameRunning) {
 			long now = System.nanoTime();
 			unprocessed += (now - lastTime) / nsPerTick;
 			lastTime = now;
 			while (unprocessed >= 1) {
 				ticks++;
 				tick();
 				unprocessed -= 1;
 			}
 			frames++;
 			render();
 			revalidate();
 			if (System.currentTimeMillis() - lastTimer1 > 1000) {
 				lastTimer1 += 1000;
 
 				fps = frames;
 				tps = ticks;
 				ticks = 0;
 				frames = 0;
 			}
 		}
 		System.err.println("Game stopped :(");
 		shutdown();
 	}
 
 	public void tick() {
 		currentScreen.tick();
 	}
 
 	public void setScreen(Screen screen) {
 		currentScreen = screen;
 		currentScreen.init(this);
 	}
 
 	public void render() {
 	g = (Graphics2D) strategy.getDrawGraphics();
 		if (font != null)
 			font.updateDrawGraphics(g);
 		g.setColor(Color.black);
 		g.fillRect(0, 0, WIDTH * 2, HEIGHT * 2);
 		currentScreen.render(g);
 		if (settings.getSetting("Debug").equals("ON") && font != null)
 			font.drawString(String.format("%s FPS, %s TPS", fps, tps), 1, 1, 2);
 		g.dispose();
 		strategy.show();
 		
 
 	}
 
 	@Override
 	public void focusGained(FocusEvent arg0) {
 		currentScreen.focusGained(arg0);
 	}
 
 	@Override
 	public void focusLost(FocusEvent arg0) {
 		currentScreen.focusLost(arg0);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		currentScreen.mousePressed(e);
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		currentScreen.mouseReleased(e);
 	}
 
 	@Override
 	public void keyPressed(KeyEvent arg0) {
 		currentScreen.keyPressed(arg0);
 	}
 
 	@Override
 	public void keyReleased(KeyEvent arg0) {
 		currentScreen.keyReleased(arg0);
 	}
 
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 	}
 
 }

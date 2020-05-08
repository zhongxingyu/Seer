 package com.github.wolfie.engine;
 
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsConfiguration;
 import java.awt.GraphicsEnvironment;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.WindowListener;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.util.Iterator;
 import java.util.ServiceLoader;
 import java.util.Stack;
 import java.util.logging.Logger;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 @SuppressWarnings("serial")
 abstract public class EngineCanvas extends Canvas implements Runnable {
 
 	public static Cursor createBlankCursor() {
 		final BufferedImage cursor = new BufferedImage(16, 16,
 				BufferedImage.TYPE_INT_ARGB);
 		return createCursorFrom(cursor);
 	}
 
 	public static Cursor createCursorFrom(final BufferedImage cursorImage) {
 		return Toolkit.getDefaultToolkit().createCustomCursor(cursorImage,
 				new Point(0, 0), "custom");
 	}
 
 	public class FightFocusListener implements FocusListener {
 
 		@Override
 		public void focusGained(final FocusEvent e) {
 		}
 
 		@Override
 		public void focusLost(final FocusEvent e) {
 			pauseGame();
 		}
 
 	}
 
 	public static final int SCALE = getConfig().getScale();
 	public static final int WIDTH = getConfig().getWidth();
 	public static final int HEIGHT = getConfig().getHeight();
 
 	private static final long TICK_LENGHT_NANO = Util
 			.milliSecondsToNanoSeconds(10);
 	private static final long FPS_CAP = 60;
 	private static final long MIN_NANOS_PER_FRAME = Util
 			.secondsToNanoSeconds(1.0d / FPS_CAP);
 
 	public static EngineCanvas instance;
 	private static ThreadLocal<Config> CONFIG;
 
 	abstract protected WindowListener getWindowListener();
 
 	public static Config getConfig() {
 		if (CONFIG == null) {
 			CONFIG = new ThreadLocal<>();
 		}
 
 		if (CONFIG.get() == null) {
 			initConfig();
 		}
 
 		return CONFIG.get();
 	}
 
 	private static void initConfig() {
 		final ServiceLoader<Config> loader = ServiceLoader.load(Config.class);
 
 		final Iterator<Config> i = loader.iterator();
 		if (i.hasNext()) {
 			final Config config = i.next();
 
 			if (config == null) {
 				final String message = "First implementation of "
 						+ Config.class.getName()
 						+ " was, for some reason, null.";
 				getLogger().severe(message);
 				throw new IllegalStateException(message);
 			}
 
 			if (i.hasNext()) {
 				getLogger().warning(
 						"There are more than one " + Config.class.getName()
 								+ " implementation loaded in the classpath.");
 			}
 
 			CONFIG.set(config);
 		} else {
 			final String message = "No implementations for "
 					+ Config.class.getName()
 					+ " in classpath. Check your ServiceLoader settings.";
 			getLogger().severe(message);
 			throw new IllegalStateException(message);
 		}
 	}
 
 	private static Logger getLogger() {
 		return Logger.getLogger(EngineCanvas.class.getName());
 	}
 
 	public final KeyData keyData = getKeyData();
 	private final MouseData mouseData = new MouseData();
 	private final Screen screen = new Screen(WIDTH, HEIGHT);
 	public final Stack<GameScreen> guiStack = new Stack<>();
 	private final TickData tickData;
 
 	public EngineCanvas() {
 		instance = this;
 
 		setPreferredSize(getGameDimensions());
 		setMinimumSize(getGameDimensions());
 		setMaximumSize(getGameDimensions());
 
 		addMouseMotionListener(mouseData);
 		addMouseListener(mouseData);
 
 		guiStack.add(getGameScreen());
 		tickData = new TickData(keyData, mouseData);
 	}
 
 	abstract protected GameScreen getGameScreen();
 
 	abstract protected KeyData getKeyData();
 
 	private static Dimension getGameDimensions() {
 		return new Dimension(getScaledWidth(), getScaledHeight());
 	}
 
 	public static int getScaledWidth() {
 		return WIDTH * SCALE;
 	}
 
 	public static int getScaledHeight() {
 		return HEIGHT * SCALE;
 	}
 
 	private void start() {
 		startGame();
 		final Thread thread = new Thread(this);
 		thread.setPriority(Thread.MAX_PRIORITY);
 		thread.start();
 	}
 
 	abstract protected void startGame();
 
 	@Override
 	public void run() {
 		_init();
 
 		long currentTime = System.nanoTime();
 		long nextTick = currentTime + TICK_LENGHT_NANO;
 		long nextFrame = currentTime + MIN_NANOS_PER_FRAME;
 		long lastTick = currentTime;
 		long lastFrame = currentTime;
 
 		while (gameIsRunning()) {
 			currentTime = System.nanoTime();
 			if (nextTick <= currentTime) {
 				tick((currentTime - lastTick));
 				lastTick = System.nanoTime();
 				nextTick = lastTick + TICK_LENGHT_NANO;
 			}
 
 			currentTime = System.nanoTime();
 			if (nextFrame <= currentTime) {
 				final BufferStrategy bs = getEnsuredBufferStrategy();
 				render((currentTime - lastFrame) + 1, bs.getDrawGraphics());
				lastFrame = currentTime;
 				nextFrame = currentTime + MIN_NANOS_PER_FRAME;
 				bs.show();
 			}
 
 			try {
 				final long sleepingMillis = getSleepAmountMillis(currentTime,
 						nextTick, nextFrame);
 				Thread.sleep(sleepingMillis);
 			} catch (final InterruptedException e) {
 				e.printStackTrace();
 				break;
 			}
 		}
 
 		System.exit(0);
 	}
 
 	abstract protected boolean gameIsRunning();
 
 	private BufferStrategy getEnsuredBufferStrategy() {
 
 		BufferStrategy bufferStrategy = null;
 		while (bufferStrategy == null) {
 			bufferStrategy = getBufferStrategy();
 			if (bufferStrategy == null) {
 				System.out.println("recreating buffer strategy");
 				createBufferStrategy(3);
 			}
 		}
 		return bufferStrategy;
 	}
 
 	@Override
 	public void paint(final Graphics g) {
 		// disable default implementation
 	}
 
 	@Override
 	public void update(final Graphics g) {
 		// disable default implementation
 	}
 
 	private void tick(final long nsSinceLastTick) {
 		keyData.tick();
 		guiStack.peek().tick(nsSinceLastTick, tickData);
 		mouseData.tick();
 	}
 
 	private void render(final long nsSinceLastFrame, final Graphics g) {
 		screen.blit(guiStack.peek().getBitmap(nsSinceLastFrame), 0, 0);
 		g.setColor(Color.WHITE);
 
 		g.fillRect(0, 0, getWidth(), getHeight());
 		g.translate((getWidth() - getScaledWidth()) / 2,
 				(getHeight() - getScaledHeight()) / 2);
 		g.clipRect(0, 0, getScaledWidth(), getScaledHeight());
 
 		final JLabel label = new JLabel("hello");
 		label.setFont(new Font("Sans Serif", Font.PLAIN, 16));
 		label.setForeground(Color.BLACK);
 		label.paint(g);
 
 		final BufferedImage image = toCompatibleImage(screen.getImage());
 		g.drawImage(image, 0, 0, getScaledWidth(), getScaledHeight(), null);
 	}
 
 	private BufferedImage toCompatibleImage(final BufferedImage image) {
 		// obtain the current system graphical settings
 		final GraphicsConfiguration gfx_config = GraphicsEnvironment
 				.getLocalGraphicsEnvironment().getDefaultScreenDevice()
 				.getDefaultConfiguration();
 
 		/*
 		 * if image is already compatible and optimized for current system
 		 * settings, simply return it
 		 */
 		if (image.getColorModel().equals(gfx_config.getColorModel())) {
 			return image;
 		}
 
 		// image is not optimized, so create a new image that is
 		final BufferedImage new_image = gfx_config.createCompatibleImage(
 				image.getWidth(), image.getHeight(), image.getTransparency());
 
 		// get the graphics context of the new image to draw the old image on
 		final Graphics2D g2d = (Graphics2D) new_image.getGraphics();
 
 		// actually draw the image and dispose of context no longer needed
 		g2d.drawImage(image, 0, 0, null);
 		g2d.dispose();
 
 		// return the new optimized image
 		return new_image;
 	}
 
 	private static long getSleepAmountMillis(final long currentTime,
 			final long nextTick, final long nextFrame) {
 		final long nanosUntilTick = nextTick - currentTime;
 		final long nanosUntilFrame = nextFrame - currentTime;
 		return (long) Math.max(0, Util.nanoSecondsToMilliSeconds(Math.min(
 				nanosUntilTick, nanosUntilFrame)));
 	}
 
 	private void _init() {
 		addKeyListener(keyData);
 		setFocusTraversalKeysEnabled(false);
 		requestFocus();
 		addFocusListener(new FightFocusListener());
 	}
 
 	abstract public void pauseGame();
 
 	abstract public void unpauseGame();
 
 	abstract protected Cursor createCursor();
 
 	protected static void init() {
 		// System.setProperty("sun.java2d.opengl", "True");
 		// System.setProperty("sun.java2d.d3d", "True");
 		// System.setProperty("sun.java2d.noddraw", "True");
 		final JFrame frame = new JFrame(getConfig().getGameTitle());
 		final JPanel panel = new JPanel(new BorderLayout());
 		final EngineCanvas game = getConfig().getGameInstance();
 		panel.add(game);
 		frame.setContentPane(panel);
 		frame.pack();
 		frame.setResizable(false);
 		frame.setLocationRelativeTo(null);
 		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		frame.addWindowListener(game.getWindowListener());
 		frame.setVisible(true);
 		frame.getContentPane().setCursor(game.createCursor());
 		game.start();
 
 		System.out.println("Tick length (ns) " + TICK_LENGHT_NANO);
 		System.out.println("FPS cap" + FPS_CAP);
 		System.out.println("Nanoseconds per frame " + MIN_NANOS_PER_FRAME);
 	}
 }

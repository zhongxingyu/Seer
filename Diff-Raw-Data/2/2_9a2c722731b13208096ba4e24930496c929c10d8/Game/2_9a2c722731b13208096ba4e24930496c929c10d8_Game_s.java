 package game;
 
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Toolkit;
 import java.awt.image.BufferStrategy;
 import java.util.ArrayList;
 import java.util.List;
 
 import level.Box;
 import level.Loader;
 import entities.Background;
 import entities.Ball;
 import entities.BreakableWall;
 import entities.Entity;
 
 public class Game extends Canvas implements Runnable {
 
 	public static final int BLOCK_SIZE = 50;
 
	public static int FIELD_WIDTH = 15;
 	public static int FIELD_HEIGHT = ((Game.FIELD_WIDTH * 3) / 4);
 
 	public static final int GAME_WIDTH = Game.FIELD_WIDTH * Game.BLOCK_SIZE;
 	public static final int GAME_HEIGHT = Game.FIELD_HEIGHT * Game.BLOCK_SIZE;
 
 	public static final int SCALE = 1;
 	public static ArrayList<Entity> entities = new ArrayList<Entity>();
 	public static ArrayList<Entity> staticBackground = new ArrayList<Entity>();
 	private boolean running;
 
 	private int maxUpdateRate = 50;
 	private long frameTimeNs = 1000000000 / this.maxUpdateRate;
 	private int minSleepTime = 1000 / this.maxUpdateRate;
 	public int fps_static = 0;
 	public int fps = 0;
 
 	private InputHandler keys;
 
 	/**
 	 * Constructor to set Canvas size and create important objects and add some
 	 * Test objects
 	 */
 	public Game() {
 		Debug.setMode(Debug.DEBUG);
 
 		Dimension d = new Dimension(Game.GAME_WIDTH - 10, Game.GAME_HEIGHT - 10);
 		this.setPreferredSize(d);
 		this.setMinimumSize(d);
 		this.setMaximumSize(d);
 
 		this.keys = new InputHandler();
 		this.addKeyListener(this.keys);
 
 		// draw background
 
 		for (int i = 0; i < Game.FIELD_HEIGHT; i++) {
 			for (int j = 0; j < Game.FIELD_WIDTH; j++) {
 				Game.staticBackground.add(new Background(j * Game.BLOCK_SIZE, i
 						* Game.BLOCK_SIZE));
 			}
 		}
 		Loader l1 = new Loader();
 		l1.addWalls(
 				"222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222221111222211111222222222222222",
 				12, 16);
 
 		// Test routine
 		for (int x = 9; x < Game.FIELD_WIDTH; x++) {
 			Game.entities.add(new BreakableWall(x * Game.BLOCK_SIZE, x
 					* Game.BLOCK_SIZE));
 
 		}
 
 		for (int i = 0; i < 10; i++) {
 			Game.entities.add(new Ball(50, (Game.BLOCK_SIZE * i)));
 		}
 	}
 
 	/**
 	 * Game-Loop Check how long it took to render a frame and let the thread
 	 * sleep some ns
 	 * 
 	 * @see java.lang.Runnable#run()
 	 */
 	@Override
 	public void run() {
 		long lastLoopTime = System.nanoTime();
 		int lastFpsTime = 0;
 		long sleepTime = 0;
 		while (this.running) {
 			long now = System.nanoTime();
 			long updateLength = now - lastLoopTime;
 			lastLoopTime = now;
 			double delta = updateLength / this.frameTimeNs;
 
 			lastFpsTime += updateLength;
 			this.fps++;
 
 			if (lastFpsTime >= 1000000000) {
 				this.fps_static = this.fps;
 				lastFpsTime = 0;
 				this.fps = 0;
 			}
 
 			/**
 			 * Move all objects
 			 */
 			this.step(delta);
 
 			/**
 			 * Redraw all objects
 			 */
 			BufferStrategy bs = this.getBufferStrategy();
 			Graphics g = bs.getDrawGraphics();
 			this.draw(g);
 			bs.show();
 			Toolkit.getDefaultToolkit().sync();
 
 			/**
 			 * Let the thread sleep
 			 */
 			sleepTime = (lastLoopTime - System.nanoTime()) / this.frameTimeNs;
 			if (sleepTime < this.minSleepTime) {
 				sleepTime = this.minSleepTime;
 			}
 			try {
 				Thread.sleep(sleepTime);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 
 		}
 	}
 
 	/**
 	 * Gets called form Launcher to start the game;
 	 */
 	public void start() {
 		this.running = true;
 		this.createBufferStrategy(2);
 		this.run();
 		this.requestFocusInWindow();
 		this.requestFocus();
 	}
 
 	/**
 	 * Stop the game
 	 */
 	public void stop() {
 		this.running = false;
 	}
 
 	/**
 	 * Move all entities
 	 * 
 	 * @param delta
 	 */
 	private void step(double delta) {
 
 		for (Entity e : Game.entities) {
 			if ((e.removed == false) && (e.needsStep == true)) {
 				e.step(delta);
 			}
 		}
 	}
 
 	/**
 	 * Draw everything
 	 * 
 	 * @param g
 	 */
 	private void draw(Graphics g) {
 		g.setColor(this.getBackground());
 		g.fillRect(0, 0, Game.GAME_WIDTH, Game.GAME_HEIGHT);
 		for (Entity e : Game.staticBackground) {
 			e.draw(g);
 		}
 		for (Entity e : Game.entities) {
 			if (e.removed == false) {
 				e.draw(g);
 			}
 		}
 		g.setColor(Color.WHITE);
 		g.drawString("FPS: " + this.fps_static, 0, 10);
 	}
 
 	/**
 	 * Get all Entities in a Box
 	 * 
 	 * @param x1
 	 * @param y1
 	 * @param x2
 	 * @param y2
 	 * @return List<Entity> Found entities
 	 */
 	public static List<Entity> getEntities(int x1, int y1, int x2, int y2) {
 		List<Entity> result = new ArrayList<Entity>();
 		Box b = new Box(Math.max(0, x1), Math.max(0, y1), Math.min(x2,
 				Game.GAME_WIDTH), Math.min(y2, Game.GAME_HEIGHT));
 
 		for (Entity e : Game.entities) {
 			if (e.removed == false) {
 				if (e.box.intersect(b)) {
 					result.add(e);
 				}
 			}
 		}
 		return result;
 	}
 }

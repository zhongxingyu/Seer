 package cs447.PuzzleFighter;
 
 import java.awt.event.KeyEvent;
 import java.util.Random;
 
 import jig.engine.Keyboard;
 import jig.engine.RenderingContext;
 import jig.engine.ResourceFactory;
 import jig.engine.util.Vector2D;
 
 public class PlayField {
 	public static final Vector2D    UP = new Vector2D( 0, -1);
 	public static final Vector2D  DOWN = new Vector2D( 0, +1);
 	public static final Vector2D  LEFT = new Vector2D(-1,  0);
 	public static final Vector2D RIGHT = new Vector2D(+1,  0);
 	public static final Gem WALL = new WallGem();
 
 	private final static Color[] colors = new Color[] { Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW };
 	private Random randSrc = new Random();
 
 	private int width;
 	private int height;
 	private int turnScore;
 	private ColoredGem[][] grid;
 	private GemPair cursor;
 
 	private long inputTimer = 0;
 	private long renderTimer = 0;
 
 	private Color randomColor() {
 		return colors[randSrc.nextInt(colors.length)];
 	}
 
 	private ColoredGem randomGem(Vector2D pos) {
 		if (randSrc.nextFloat() > 0.25) {
 			return new PowerGem(this, pos, randomColor());
 		}
 		else {
 			return new CrashGem(this, pos, randomColor());
 		}
 	}
 
 	public PlayField(int width, int height) {
 		this.width = width;
 		this.height = height;
 		this.grid = new ColoredGem[height][width];
 		this.cursor = new GemPair(randomGem(new Vector2D(width/2, 1)), randomGem(new Vector2D(width/2, 0)));
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 	
 	public void render(RenderingContext rc) {
 		for (int y = 0; y < height; y++) {
 			for (int x = 0; x < width; x++) {
 				Gem g = grid[y][x];
 				if (g != null && (cursor == null || !cursor.contains(g))) {
 					g.render(rc);
 				}
 			}
 		}
 		if (cursor != null) {
 			cursor.render(rc);
 		}
 	}
 
 	public Gem ref(Vector2D pos) {
 		int x = (int) pos.getX();
 		int y = (int) pos.getY();
 		if (x < 0 || x >= width || y < 0 || y >= height) {
 			return WALL;
 		}
 
 		return grid[y][x];
 	}
 
 	public void set(Vector2D pos, ColoredGem g) {
 		int x = (int) pos.getX();
 		int y = (int) pos.getY();
 
 		grid[y][x] = g;
 	}
 
 	public void clear(Vector2D pos) {
 		set(pos, null);
 	}
 
 	public boolean isFilled(Vector2D pos) {
 		return (ref(pos) != null);
 	}
 
 	public boolean move(Vector2D dv) {
 		return cursor.move(dv);
 	}
 
 	public void step() {
 		if (!cursor.move(DOWN)) {
 			cursor = null;
 		}
 	}
 
 	public boolean fall() {
 		boolean hadEffect = false;
 
 		for (int y = height-1; y >= 0; y--) {
 			for (int x = 0; x < width; x++) {
 				if (grid[y][x] != null) {
 					hadEffect |= grid[y][x].move(DOWN);
 				}
 			}
 		}
 
 		return hadEffect;
 	}
 
 	public int crashGems() {
 		int crashScore = 0;
 
 		for (int y = height-1; y >= 0; y--) {
 			for (int x = 0; x < width; x++) {
 				ColoredGem g = grid[y][x];
 				if (g != null) {
 					crashScore += g.endTurn();
 				}
 			}
 		}
 
 		return crashScore;
 	}
 	
 	public void gravitate() {
 		if (fall()) {
 			return;
 		}
 
 		int crashScore = crashGems();
 		if (crashScore != 0) {
 			turnScore += crashScore;
 			return;
 		}
 
 		if (turnScore > 0) {
 			ResourceFactory.getJIGLogger().info("Combo'd " + turnScore + " gems");
 			for (int i = 0; i < turnScore / 2; i++) {
 				grid[i / width][i % width] = new TimerGem(this, new Vector2D(i%width,i/width), Color.RED);
 			}
 			turnScore = 0;
 			return;
 		}
 
 		cursor = new GemPair(randomGem(new Vector2D(width/2, 1)), randomGem(new Vector2D(width/2, 0)));
		for (int y = height-1; y >= 0; y--) {
			for (int x = 0; x < width; x++) {
				ColoredGem g = grid[y][x];
				if (g instanceof TimerGem) {
					TimerGem tg = (TimerGem) g;
					if (tg.stepTimer()) {
						grid[y][x] = new PowerGem(this, tg.pos, tg.color);
					}
				}
			}
		}
 	}
 	
 	public void update(long deltaMs, Keyboard keyboard) {
 		renderTimer += deltaMs;
 		inputTimer += deltaMs;
 
 		if (inputTimer > 100) {
 			inputTimer = 0;
 			if (cursor != null) {
 				boolean ccw = keyboard.isPressed(KeyEvent.VK_Q);
 				boolean cw = keyboard.isPressed(KeyEvent.VK_E);
 				boolean left = keyboard.isPressed(KeyEvent.VK_A);
 				boolean right = keyboard.isPressed(KeyEvent.VK_D);
 				boolean down = keyboard.isPressed(KeyEvent.VK_S);
 
 				if (ccw && !cw) {
 					cursor.rotateCounterClockwise();
 				}
 				if (cw && !ccw) {
 					cursor.rotateClockwise();
 				}
 				if (down) {
 					move(PlayField.DOWN);
 				}
 				if (left && !right) {
 					move(PlayField.LEFT);
 				}
 				if (right && !left) {
 					move(PlayField.RIGHT);
 				}
 			}
 		}
 
 		if (cursor != null && renderTimer > 500) {
 			renderTimer = 0;
 			step();
 		}
 		if (cursor == null && renderTimer > 100) {
 			renderTimer = 0;
 			gravitate();
 		}
 	}
 }

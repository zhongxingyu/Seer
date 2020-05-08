 package ld25;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Random;
 
 import javax.imageio.ImageIO;
 
 import ld25.GameObject.Direction;
 
 /**
  * Game model
  * 
  * @author Marco Jonkers
  * 
  */
 public class World {
 	private static final int WIDTH = 96;
 	private static final int HEIGHT = 96;
 	private Cell[] map = new Cell[WIDTH * HEIGHT];
 	private SpriteSheet sheet;
 	private static final int TILE_SIZE = 16;
 	private Player player;
 	private Overlay overlay;
 	private Camera camera;
 	
 	private int pixelWidth;
 	private int pixelHeight;
 	
 	// Tile culling
 	private int firstX;
 	private int firstY;
 	private int lastX;
 	private int lastY;
 	
 	private HashSet<GameObject> gameObjects = new HashSet<GameObject>();
 	private HashSet<GameObject> disposed = new HashSet<GameObject>();
 
 	public World(Game game) {
 		try {
 			BufferedImage image = ImageIO.read(World.class.getResourceAsStream("/test.png"));
 			sheet = new SpriteSheet(image, TILE_SIZE);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		for(int i = 0; i < WIDTH * HEIGHT; i++) {
 			map[i] = new Cell();
 			map[i].tile = 0;
 		}
 		player = new Player(this, WIDTH / 2, HEIGHT / 2);
 		insert(player);
 		
 		int dangerZone = 16;
 		
 		// Random goats
 		Random r = Game.RANDOM;
 		for(int i = 0; i < 500; i++) {
 			int x, y;
 			if(r.nextBoolean()) { // Left or right
 				x = r.nextBoolean() ? r.nextInt(dangerZone) + WIDTH - dangerZone : r.nextInt(dangerZone);
 				y = r.nextInt(HEIGHT);
 			} else { // Up or down
 				x = r.nextInt(WIDTH);
 				y = r.nextBoolean() ? r.nextInt(dangerZone) + HEIGHT - dangerZone : r.nextInt(dangerZone);
 			}
 			if(isClear(x, y)) {
 				int type = r.nextInt(4);
 				switch(type) {
 					case 0:
 						insert(new Goat(this, x, y));
 						break;
 					case 1:
 						insert(new Gunner(this, x, y));
 						break;
 					case 2:
 						insert(new Bandit(this, x, y));
 						break;
 					case 3:
 						insert(new Sniper(this, x, y));
 						break;
 				}
 			}
 		}
 		
 		overlay = new Overlay();
		camera = new Camera(0, 0, game.getHeight(), game.getHeight());
 		pixelWidth = WIDTH * TILE_SIZE;
 		pixelHeight = HEIGHT * TILE_SIZE;
 	}
 
 	private void insert(GameObject object) {
 		gameObjects.add(object);
 		int x = object.getMapX();
 		int y = object.getMapY();
 		map[y * WIDTH + x].gameObject = object;
 	}
 
 	public void tick() {
 		for(GameObject o : gameObjects) {
 			o.tick();
 			if(o.isDisposed()) {
 				disposed.add(o);
 			}
 		}
 		for(GameObject o : disposed) {
 			int x = o.getMapX();
 			int y = o.getMapY();
 			map[y * WIDTH + x].gameObject = null;
 		}
 		gameObjects.removeAll(disposed);
 		disposed.clear();
 		
 		
 		// Center around player
 		int px = Math.round(player.getX()) + player.getWidth() / 2 - camera.getWidth() / 2;
 		int py = Math.round(player.getY()) + player.getHeight() / 2 - camera.getHeight() / 2;
 		px = (int) GameMath.clamp(px, 0, WIDTH * TILE_SIZE - camera.getWidth());
 		py = (int) GameMath.clamp(py, 0, HEIGHT * TILE_SIZE
 				- camera.getHeight());
 		camera.setPosition(px, py);
 
 		// Tile culling
 		int cx1 = camera.getX();
 		int cy1 = camera.getY();
 		int cx2 = cx1 + camera.getWidth();
 		int cy2 = cy1 + camera.getHeight();
 		firstX = GameMath.clamp(cx1 / TILE_SIZE, 0, WIDTH - 1);
 		lastX = GameMath.clamp(cx2 / TILE_SIZE, 0, WIDTH - 1);
 		firstY = GameMath.clamp(cy1 / TILE_SIZE, 0, HEIGHT - 1);
 		lastY = GameMath.clamp(cy2 / TILE_SIZE, 0, HEIGHT - 1);
 
 		overlay.tick();
 	}
 
 	public void render(Graphics2D g, double interpolation) {
 		camera.setGraphics(g);
 		
 		for (int x = firstX; x <= lastX; x++) {
 			for (int y = firstY; y <= lastY; y++) {
 				camera.drawImage(sheet.getImage(map[y * WIDTH + x].tile), x * TILE_SIZE, y * TILE_SIZE);
 			}
 		}
 
 		for(GameObject o : gameObjects) {
 			o.render(camera, interpolation);
 		}
 		
 		overlay.render(camera, interpolation);
 	}
 	
 	public boolean isClear(int x, int y) {
 		return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT && map[y * WIDTH + x].gameObject == null;
 	}
 
 	public Camera getCamera() {
 		return camera;
 	}
 	
 	public int getWidth() {
 		return WIDTH;
 	}
 	
 	public int getHeight() {
 		return HEIGHT;
 	}
 	
 	public int getTileSize() {
 		return TILE_SIZE;
 	}
 	
 	public int getPixelWidth() {
 		return pixelWidth;
 	}
 	
 	public int getPixelHeight() {
 		return pixelHeight;
 	}
 
 	public void move(GameObject object, Direction direction) {
 		int x = object.getMapX();
 		int y = object.getMapY();
 		map[y * WIDTH + x].gameObject = null;
 		switch(direction) {
 			case DOWN:
 				y++;
 				break;
 			case LEFT:
 				x--;
 				break;
 			case RIGHT:
 				x++;
 				break;
 			case UP:
 				y--;
 				break;
 		}
 		map[y * WIDTH + x].gameObject = object;
 		object.setMapX(x);
 		object.setMapY(y);
 	}
 }

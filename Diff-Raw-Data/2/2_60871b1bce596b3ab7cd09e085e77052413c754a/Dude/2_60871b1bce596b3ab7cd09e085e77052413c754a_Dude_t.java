 package state;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.image.CropImageFilter;
 import java.awt.image.FilteredImageSource;
 import java.io.Serializable;
 import java.util.Random;
 import java.util.Stack;
 
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 import sound.AudioPlayer;
 
 import logic.Logic;
 
 import UI.Display;
 
 /**
  * Stores information about a dude.
  */
 public class Dude implements Serializable {
 	public final long serialVersionUID = 55558278392826626L;
 	/**
 	 * The coordinates of the tile under the bottom corner of the dude.
 	 */
 	protected int x; // Tile coords of Dude
 	protected int y;
 	protected int TILE_HEIGHT = 32;
 	protected int TILE_WIDTH = 64;
 	private int NUM_SPRITES = 16; // Number of model sprites per images
 	protected int maxHealth;
 	protected int currentHealth;
 	private int damage;
 
 	private static final int RES_CAPACITY = 20;
 
 	private Task task;
 
 	private int buildTicks;
 
 	private Random randomGen = new Random();
 	private int rand  = randomGen.nextInt(5);
 
 	private boolean isDeleted;
 	public boolean isDeleted() {return isDeleted;}
 	public void setDeleted() {isDeleted = true;}
 
 	/**
 	 * Size of the structure, in tiles.
 	 */
 
 	public static final int DOWN = 0, LEFT = 1, RIGHT = 3, UP = 2; // Numerical
 																	// constants
 																	// for
 																	// facing
 	// NOTE: Usable as images array indices
 
 	protected int width, height; // ???
 	protected int facing = DOWN; // Facing constant
 	protected int oldX, oldY;
 	private transient Image[][] images = new Image[4][4]; // A single image stored per
 												// facing
 
 	/**
 	 * The dude's image.
 	 */
 	// private Image image;
 
 	protected transient World world;
 
 	/**
 	 * Returns the X coordinate of the bottom corner of the dude.
 	 */
 	public int getX() {
 		return x;
 	}
 
 	/**
 	 * Returns the Y coordinate of the bottom corner of the dude.
 	 */
 	public int getY() {
 		return y;
 	}
 
 	/**
 	 * Returns the width of the dude, in tiles.
 	 */
 	public int getWidth() {
 		return width;
 	}
 
 	/**
 	 * Sets the world on this dude.  Needed because the world is transient.
 	 * @param w
 	 */
 	public void setWorld(World w){
 		world = w;
 	}
 
 	/**
 	 * Returns the height of the dude, in tiles.
 	 */
 	public int getHeight() {
 		return height;
 	}
 
 	/**
 	 * Returns the dude's image.
 	 */
 	public Image getImage() {
 		return images[facing][count];
 	}
 
 	/**
 	 * Creates a dude.
 	 *
 	 * @param world
 	 *            The world the dude is in.
 	 * @param x
 	 *            The X coordinate of the bottom corner of the dude.
 	 * @param y
 	 *            The Y coordinate of the bottom corner of the dude.
 	 * @param width
 	 *            The width of the dude.
 	 * @param heightNUM_SPRITES
 	 *            The height of the dude.
 	 * @param image
 	 *            The path to the dude's image.
 	 */
 	public Dude(World world, int x, int y, int width, int height, String image) {
 		this.x = x;
 		this.y = y;
 		this.oldX = x;
 		this.oldY = y;
 		this.width = width;
 		this.height = height;
 		maxHealth = 100;
 		currentHealth = maxHealth;
 		// this.image = new ImageIcon(image).getImage();
 		this.world = world;
 		loadImage(image);
 
 	}
 	protected void loadImage(String image) {
 		JPanel panel = new JPanel(); // Instantiated JPanel to use createImage
 										// method
 
 		// Load Images
 
 		for (int i = 0; i < 4; i++) { // Iterate through facings --> Load an
 										// image into each facing
 			// For animations this code will need to be extended to include an
 			// array of images per facing
 			// Idea: Make images double array?
 			for (int j = 0; j < 4; j++) {
 				images[i][j] = new ImageIcon(image).getImage();
 				CropImageFilter filter = new CropImageFilter(
 						(images[i][j].getWidth(null) / NUM_SPRITES)
 								* (i * 4 + j), 0,
 						(images[i][j].getWidth(null) / NUM_SPRITES),
 						images[i][j].getHeight(null));
 				images[i][j] = panel.createImage(new FilteredImageSource(
 						images[i][j].getSource(), filter));
 			}
 		}
 
 	}
 
 	/**
 	 * Tries to move the dude.
 	 *
 	 * @param newX
 	 *            The new X position.
 	 * @param newY
 	 *            The new Y position.
 	 * @return True if successful.
 	 */
 	public boolean move(int newX, int newY) {
 		if (newX - width < -1 || newY - height < -1 || newX >= world.getXSize()
 				|| newY >= world.getYSize())
 			return false;
 
 		// check for overlap with other dudes, and invalid moves
 		for(int X = 0; X < width; X++)
 			for(int Y = 0; Y < height; Y++) {
 				Tile tile = world.getTile(newX-X, newY-Y);
 				if(!canMove(world.getTile(x-X, y-Y), tile))
 					return false;
 			}
 
 		setFacing(newX, newY);
 		// unlink the tiles at the old location
 		// unlinkTiles(x, y);
 
 		// update the location
 		x = newX;
 		y = newY;
 
 		// link the tiles at the new location
 		linkTiles(x, y);
 
 		return true;
 	}
 
 	private void unlinkTiles(int x, int y) {
 		for (int X = 0; X < width; X++)
 			for (int Y = 0; Y < height; Y++)
 				world.getTile(x - X, y - Y).setDude(null);
 	}
 
 	private void linkTiles(int x, int y) {
 		for (int X = 0; X < width; X++)
 			for (int Y = 0; Y < height; Y++)
 				world.getTile(x - X, y - Y).setDude(this);
 	}
 
 	public void setFacing(int newX, int newY) {
 		if ((x - newX) > 0) {
 			facing = LEFT;
 		}
 		if ((x - newX) < 0) {
 			facing = RIGHT;
 		}
 		if ((y - newY) > 0) {
 			facing = UP;
 		}
 		if ((y - newY) < 0) {
 			facing = DOWN;
 		}
 
 	}
 
 	public boolean canMove(Tile from, Tile to) {
 		if(to.getDude() != null && to.getDude() != this)
 			return false;
 
 
 		if(from.getHeight() != to.getHeight()) {
 			if(from.getHeight() - 1 == to.getHeight()) {
 				if(!(to.getStructure() instanceof Ramp))
 					return false;
 				if(((Ramp)to.getStructure()).getDirection() != Direction.getDirectionBetween(to, from))
 					return false;
 
 			} else if(from.getHeight() + 1 == to.getHeight()) {
 				if(!(from.getStructure() instanceof Ramp))
 					return false;
 				if(((Ramp)from.getStructure()).getDirection() != Direction.getDirectionBetween(from, to))
 					return false;
 
 			} else {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	Resource harvesting;
 	Crate crate;
 
 	int storedResources = 0;
 	ResourceType storedResType = null;
 
 	Dude attacking;
 
 	int count; // update count, things change every 4 updates.
 
 	/**
 	 * Called every tick. Does stuff.
 	 */
 	public void update() {
 		if(isDeleted) return;
 
 		count++;
 		if (count == 4) {
 //			if (buildTicks > 0) {
 //			buildTicks--;
 			unlinkTiles(oldX, oldY);
 			linkTiles(x, y);
 			oldX = x;
 			oldY = y;
 			attacking = findAttackTarget();
 
 
 			//TODO Squids cant build so fix that instanceof dude
 			if(task == null && this instanceof Dude){
 				task = world.tasks.poll();
 			}
 
 			if (attacking != null) {
 				if(Math.abs(x - attacking.getX()) + Math.abs(y - attacking.getY()) > 1) {
 					// too far, move closer
 					moveTowards(attacking.getX(), attacking.getY());
 					attacking = null;
 				} else {
 					setFacing(attacking.getX(), attacking.getY());
 					attack(attacking);
 				}
 			} else if (task == null) {
 				getResources();
 
 			} else if (task.getTask().equals("build")) {
 				Tile t = task.getTile();
 				followPath(t.getX(), t.getY());
 				// rest(1000);//TODO
 
 				if (world.build(t, task.getType(), this)) {
 					task = null;
 				}
 			}
 			count = 0;
 		}
 	}
 
 	public void attack(Dude victim) {
 
 		if(world.getAudioPlayer()!=null)
 			world.getAudioPlayer().addAudioPlayer("SinglePunch.wav", true);
 
 		//new AudioPlayer("SinglePunch.wav", true).start();
 		victim.currentHealth -= 15;
 		if(victim.currentHealth <= 0) {
 			//dude killed needs his task readded to queue
 			if(victim.hasTask()){
				world.tasks.add(victim.task);
 			}
 			world.removeDude(victim);
 			if(world.getAudioPlayer()!=null){
 				world.getAudioPlayer().addAudioPlayer("DyingDude.wav", true);
 				}
 		}
 	}
 
 	private boolean hasTask() {
 		if(task != null){
 			return true;
 		}
 		return false;
 	}
 
 	public Dude findAttackTarget() {
 		final int RANGE = 2;
 
 		for(int dx = -RANGE; dx <= RANGE; dx++)
 			for(int dy = -RANGE; dy <= RANGE; dy++) {
 				Tile t = world.getTile(x+dx, y+dy);
 				if(t == null)
 					continue;
 
 				Dude d = t.getDude();
 				if(this instanceof Dude){
 					if(d != null && this.getClass() != d.getClass())
 						return d;
 				} else {
 					if(d != null && !(d instanceof Slugdude || d instanceof Octodude ))
 						return d;
 				}
 			}
 
 
 		return null;
 	}
 
 	public void getResources() {
 		if (storedResources >= RES_CAPACITY) {
 			if (crate == null) {
 				crate = (Crate) world.getNearestStructure(Crate.class,
 						world.getTile(x, y), this);
 			}
 
 			if (crate != null) {
 				boolean moved = followPath(crate.getX(), crate.getY());
 				if (!moved) {
 					if (crate.getX() == x && crate.getY() == y) {
 						crate.dropoff(storedResources, storedResType);
 						crate = null;
 						storedResType = null;
 						storedResources = 0;
 					}
 				}
 			}
 
 		} else {
 			//SlugBalancing check
 			if(this instanceof Octodude && !world.isSlugBalancingEnabled()){
 				return;
 			}
 			Resource nowHarvesting = world.getNearestResource(
 					world.getTile(x, y), this);
 			if (harvesting != nowHarvesting) {
 				harvesting = nowHarvesting;
 			}
 
 			if (harvesting != null) {
 				boolean moved = followPath(nowHarvesting.getX(),
 						nowHarvesting.getY());
 				if (!moved) {
 					if (harvesting.getX() == x && harvesting.getY() == y) {
 						harvest(harvesting);
 						harvesting = null;
 					}
 				}
 			} else {
 				idle();
 			}
 		}
 
 	}
 
 	protected void idle() {}
 
 	protected void harvest(Resource harvesting) {
 		storedResources += harvesting.harvest();
 		storedResType = harvesting.getResType();
 	}
 
 	int targetX = -1, targetY = -1;
 	Stack<Tile> path;
 	int failedMoveCount = 0;
 
 	private boolean followPath(int x, int y) {
 		if(x != targetX || y != targetY || path == null || path.size() == 0 || failedMoveCount > rand) {
 
 			targetX = x;
 			targetY = y;
 			path = world.getLogic().findRoute(world.getTile(this.x, this.y),
 					world.getTile(targetX, targetY), this);
 			failedMoveCount = 0;
 			rand = randomGen.nextInt(3);
 		}
 
 		if (path.size() > 0) {
 			Tile next = path.pop();
 			if (!move(next.getX(), next.getY())) {
 				path.push(next);
 				failedMoveCount++;
 				return false;
 			}
 			failedMoveCount = 0;
 			return true;
 		}
 
 		return false;
 	}
 
 	private boolean moveTowards(int tx, int ty) {
 		if(x < tx && move(x+1, y)) return true;
 		if(x > tx && move(x-1, y)) return true;
 		if(y < ty && move(x, y+1)) return true;
 		if(y > ty && move(x, y-1)) return true;
 		return false;
 	}
 
 	public void rest(int rest){
 		buildTicks = rest;//TODO
 	}
 
 
 	/**
 	 * Draws the dude.
 	 *
 	 * @param g
 	 *            The Graphics object to draw on.
 	 * @param d
 	 *            The display being drawn on.
 	 * @param bottomPixelX
 	 *            The X coordinate of the bottom corner of the object
 	 * @param bottomPixelY
 	 *            The Y coordinate of the bottom corner of the object
 	 */
 	public void draw(Graphics g, Display d, int bottomPixelX, int bottomPixelY,
 			boolean drawHealth) {
 
 		double percentMoved = count * 0.25;
 
 
 		// Tile coordinates of The Dude (x,y)
 		double x, y;
 
 		if(attacking == null) {
 			x = this.oldX + (this.x - this.oldX) * percentMoved;
 			y = this.oldY + (this.y - this.oldY) * percentMoved;
 		} else {
 			double dist = (count % 2 == 1) ? 0.2 : 0.1;
 			x = this.x + (facing == LEFT ? -1 : facing == RIGHT ? 1 : 0) * dist;
 			y = this.y + (facing == UP ? -1 : facing == DOWN ? 1 : 0) * dist;
 		}
 
 		// Pixel coordinates (on screen) of the Dude (i,j)
 		Point pt = d.tileToDisplayCoordinates(x, y);
 
 		int height = world.getTile(this.x, this.y).getHeight();
 		int oldHeight = world.getTile(oldX, oldY).getHeight();
 
 		pt.y -= TILE_HEIGHT * (oldHeight + (height - oldHeight) * percentMoved);
 		pt.y -= TILE_HEIGHT / 2;
 
 		Image i = images[(facing + d.getRotation()) % 4][Math.min(count, 3)];
 
 		// Draw image at (i,j)
 
 		int posX = pt.x - i.getWidth(null) / 2;
 		int posY = pt.y - i.getHeight(null);
 
 		g.drawImage(i, posX, posY, null);
 
 		if (drawHealth) {
 			int tall = 10;
 			int hHeight = 3;
 			int hWidth = 16;
 			int barWidth = 10;
 			g.setColor(Color.red);
 			g.fillRect(posX - barWidth / 2, posY - tall, hWidth + barWidth, hHeight);
 			g.setColor(Color.green);
 			g.fillRect(posX - barWidth / 2, posY - tall, (int)((hWidth + barWidth) * currentHealth / (float)maxHealth), hHeight);
 		}
 	}
 
 	public int getMaxHealth() {
 		return maxHealth;
 	}
 
 	public void setMaxHealth(int health) {
 		this.maxHealth = health;
 	}
 
 	public int getCurrentHealth() {
 		return currentHealth;
 	}
 
 	public void setCurrentHealth(int currentHealth) {
 		this.currentHealth = currentHealth;
 	}
 
 	public boolean isAt(int x2, int y2) {
 		if(x2 == x && y2 == y){
 			return true;
 		}
 		return false;
 	}
 	public int getOldX() {return oldX;}
 	public int getOldY() {return oldY;}
 
 	public boolean canMine(Resource r) {
 		if(storedResType != null && r.getResType() != storedResType)
 			return false;
 		if(r.getResType() == null)
 			return false;
 		return true;
 	}
 }

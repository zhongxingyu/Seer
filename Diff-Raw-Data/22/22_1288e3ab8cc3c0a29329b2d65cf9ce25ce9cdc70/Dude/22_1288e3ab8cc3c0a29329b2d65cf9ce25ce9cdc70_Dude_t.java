 package state;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.ColorModel;
 import java.awt.image.CropImageFilter;
 import java.awt.image.FilteredImageSource;
 import java.awt.image.ImageConsumer;
 import java.io.Serializable;
 import java.util.Hashtable;
 import java.util.Random;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 /**
  * Stores information about a dude.
  */
 public class Dude implements Serializable{
 	public final long serialVersionUID = 55558278392826626L;
 	/**
 	 * The coordinates of the tile under the bottom corner of the dude.
 	 */
 	private int x, y; // Tile coords of Dude
 	private int TILE_HEIGHT = 32;
 	private int TILE_WIDTH = 64;
 	private int NUM_SPRITES = 16; // Number of model sprites per  images
 
 	/**
 	 * Size of the structure, in tiles.
 	 */
 
 	public static final int DOWN = 0, LEFT = 1, RIGHT = 3, UP = 2; // Numerical constants for facing
 		// NOTE: Usable as images array indices
 
 
 	private int width, height; // ???
 	private int facing = DOWN; // Facing constant
 	private int oldX, oldY;
 	private Image[][] images = new  Image[4][4]; // A single image stored per facing
 
 	/**
 	 * The dude's image.
 	 */
 	//private Image image;
 
 	private World world;
 
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
 	 * @param world The world the dude is in.
 	 * @param x The X coordinate of the bottom corner of the dude.
 	 * @param y The Y coordinate of the bottom corner of the dude.
 	 * @param width The width of the dude.
 	 * @param height The height of the dude.
 	 * @param image The path to the dude's image.
 	 */
 	public Dude(World world, int x, int y, int width, int height, String image) {
 		this.x = x;
 		this.y = y;
 		this.oldX = x;
 		this.oldY = y;
 		this.width = width;
 		this.height = height;
 		//this.image = new ImageIcon(image).getImage();
 		this.world = world;
 		JPanel panel = new JPanel(); // Instantiated JPanel to use createImage method
 
 		// Load Images
 		int num_images = NUM_SPRITES; // NOTE: Currently skips out all but first image of each facing
 
 		for (int i = 0; i<4; i++){ // Iterate through facings --> Load an image into each facing
 			// For animations this code will need to be extended to include an array of images per facing
 			// Idea: Make images double array?
 			for(int ß = 0; ß<4; ß++){
 				images[i][ß] = new ImageIcon(image).getImage();
 				CropImageFilter filter = new CropImageFilter((images[i][ß].getWidth(null)/NUM_SPRITES)*(i*4 + ß),0,(images[i][ß].getWidth(null)/NUM_SPRITES),images[i][ß].getHeight(null));
 				images[i][ß] = panel.createImage(new FilteredImageSource(images[i][ß].getSource(), filter));
 			}
 		}
 
 
 	}
 
 
 	/**
 	 * Tries to move the dude.
 	 * @param newX The new X position.
 	 * @param newY The new Y position.
 	 * @return True if successful.
 	 */
 	public boolean move(int newX, int newY) {
 		if(newX-width < -1 || newY-height < -1 || newX >= world.getXSize() || newY >= world.getYSize())
 			return false;
 
 		// check for overlap with other dudes, and invalid moves
 		for(int X = 0; X < width; X++)
 			for(int Y = 0; Y < height; Y++) {
 				Tile tile = world.getTile(x-X, y-Y);
 				if(tile.getDude() != null && tile.getDude() != this)
 					return false;
 				if(!canMove(tile, world.getTile(newX-X, newY-Y)))
 					return false;
 			}
 
 		setFacing(newX, newY, x, y);
 		// unlink the tiles at the old location
 		//unlinkTiles(x, y);
 
 		// update the location
 		x = newX;
 		y = newY;
 
 		// link the tiles at the new location
 		linkTiles(x, y);
 
 
 		return true;
 	}
 
 	private void unlinkTiles(int x, int y) {
 		for(int X = 0; X < width; X++)
 			for(int Y = 0; Y < height; Y++)
 				world.getTile(x-X, y-Y).setDude(null);
 	}
 
 	private void linkTiles(int x, int y) {
 		for(int X = 0; X < width; X++)
 			for(int Y = 0; Y < height; Y++)
 				world.getTile(x-X, y-Y).setDude(this);
 	}
 
 	public void setFacing(int newX,int newY, int x, int y){
 		if((x -newX) > 0){
 			facing = LEFT;
 		}
 		if ((x - newX)<0){
 			facing = RIGHT;
 		}
 		if ((y- newY) > 0){
 			facing = UP;
 		}
 		if ((y - newY) < 0){
 			facing = DOWN;
 		}
 
 	}
 	public boolean canMove(Tile from, Tile to) {
		if(from.getHeight() < to.getHeight() - 1)
 			return false;
		else if (from.getHeight()> to.getHeight() + 1)
 			return false;
 		return true;
 	}
 
 	/**
 	 * Called every tick. Does stuff.
 	 */
 	int count;
 	int direction = DOWN;
 	public void update() {
 		count++;
 		if (count == 4){
 			unlinkTiles(oldX, oldY);
 			linkTiles(x, y);
 
 			oldX = x; oldY = y;
 			if(direction == DOWN) {
 				boolean ok = move(x, y+1);
 				if(!ok)
 					direction = UP;
 			}
 			if(direction == UP) {
 				boolean ok = move(x, y-1);
 				if(!ok)
 					direction = LEFT;
 			}
 			if(direction == LEFT) {
 				boolean ok = move(x-1, y);
 				if(!ok)
 					direction = RIGHT;
 			}
 			if(direction == RIGHT) {
 				boolean ok = move(x+1, y);
 				if(!ok)
 					direction = DOWN;
 			}
 			count = 0;
 		}
 	}
 
 
 	/**
 	 * Draws the dude.
 	 * @param g The Graphics object to draw on.
 	 * @param width The width of the display.
 	 * @param camx The camera X.
 	 * @param camy The camera Y.
 	 */
 	public void draw(Graphics g, int width, int height, int camx, int camy){
 
 		double percentMoved = count * 0.25;
 
 		// Tile coordinates of The Dude (x,y)
 		double x = this.oldX + (this.x - this.oldX) * percentMoved - camx +1;
 		double y = this.oldY + (this.y - this.oldY) * percentMoved - camy +1;
 
 		// Pixel coordinates (on screen) of the Dude (i,j)
 		double i = (width/2)-(images[facing][count].getWidth(null)/2) + (x-y) * (TILE_WIDTH/2);
 		double j =  (x+y) * (TILE_HEIGHT/ 2) -images[facing][count].getHeight(null)-(TILE_HEIGHT/2) - height;
 		// Draw image at (i,j)
 		g.drawImage(images[facing][count], (int)i, (int)j, images[facing][count].getWidth(null), images[facing][count].getHeight(null), null);
 
 	}
 }

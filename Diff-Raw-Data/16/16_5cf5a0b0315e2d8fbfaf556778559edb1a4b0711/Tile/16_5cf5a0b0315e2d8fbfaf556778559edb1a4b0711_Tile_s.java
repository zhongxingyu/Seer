 package state;
 import java.awt.Image;
 import java.awt.Point;
 import java.io.File;
 
 import javax.swing.ImageIcon;
 
 /**
  * A tile in the world.
  */
 public class Tile {
 	// FIELDS
 	private Image img;
 	private int x;
 	private int y;
 	private boolean visited;
 	private boolean occupied;
 	private int height;
 	private Image leftSideImg, rightSideImg;
 	private Structure structure;
 	private Dude dude;
 
 
 	/**
 	 * Creates a tile.
 	 * @param type The icon name.
 	 * @param ht The height.
 	 * @param x The X coordinate.
 	 * @param y The Y coordinate.
 	 */
 	public Tile(String type,int ht,int x,int y){
		img = new ImageIcon("Assets/Environment_Tiles/"+type+".png").getImage();
		File tileFile = new File("Assets/Environment_Tiles/"+type+".png");
 		assert(tileFile.exists());
		leftSideImg = new ImageIcon("Assets/Environment Tiles/WestFacingDirt.png").getImage();
		rightSideImg = new ImageIcon("Assets/Environment Tiles/EastFacingDirt.png").getImage();
 		height = ht;
 		this.x = x;
 		this.y = y;
 	}
 
 	/**
 	 * Returns the tile's icon.
 	 */
 	public Image getImage(){
 		return img;
 	}
 
 	/**
 	 * Returns the tile's X coordinate.
 	 */
 	public int getX() {
 		return x;
 	}
 
 	/**
 	 * Returns the tile's Y coordinate.
 	 */
 	public int getY() {
 		return y;
 	}
 
 	/**
 	 * Returns the tile's coordinates as a Point.
 	 */
 	public Point getPoint() {
 		return new Point(x, y);
 	}
 
 	public boolean visited() {
 		return visited;
 	}
 
 	public boolean occupied() {
 		return occupied;
 	}
 
 	public void setVisited(boolean b) {
 		visited = b;
 	}
 
 	public void setPrevTile(Tile tileInterface) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public Tile getPrevTile() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	/**
 	 * Returns this tile's height.
 	 */
 	public int getHeight() {
 		return height;
 	}
 
 	/**
 	 * Sets this tile's height.
 	 */
 	public void setHeight(int h) {
 		height = h;
 	}
 
 	/**
 	 * Returns the structure on this tile.
 	 */
 	public Structure getStructure() {
 		return structure;
 	}
 
 	/**
 	 * Sets the structure on this tile.
 	 */
 	public void setStructure(Structure s) {
 		structure = s;
 	}
 
 	/**
 	 * Returns the dude on this tile.
 	 */
 	public Dude getDude() {
 		return dude;
 	}
 
 	/**
 	 * Sets the dude on this tile.
 	 */
 	public void setDude(Dude d) {
 		dude = d;
 	}
 }
 

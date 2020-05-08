 package state;
 import java.awt.Image;
 import java.awt.Point;
 import java.io.File;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 
 /**
  * A tile in the world.
  */
 public class Tile implements Serializable{
 	private final long serialVersionUID = 7789269378738222222L;
 	// FIELDS
 	private transient Image img;
 	private int x;
 	private int y;
 	private boolean visited;
 	private boolean occupied;
 	private int height;
 	private transient Image leftSideImg, rightSideImg;
 	private Structure structure;
 	private Dude dude;
 	private String imgName; //Used to save the image for de-serializing later
 
 
 	/**
 	 * Creates a tile.
 	 * @param type The icon name.
 	 * @param ht The height.
 	 * @param x The X coordinate.
 	 * @param y The Y coordinate.
 	 */
 	public Tile(String type,int ht,int x,int y){
 		this.imgName = type;
 		try{
 		img = new ImageIcon("Assets/EnvironmentTiles/"+type+".png").getImage();
 		File tileFile = new File("Assets/EnvironmentTiles/"+type+".png");
 		assert(tileFile.exists());
 		leftSideImg = new ImageIcon("Assets/EnvironmentTiles/WestFacingDirt.png").getImage();
 		rightSideImg = new ImageIcon("Assets/EnvironmentTiles/EastFacingDirt.png").getImage();
 		} catch(Exception e){
 			JOptionPane.showMessageDialog(null, "Image Not Found", "Warning", JOptionPane.WARNING_MESSAGE);
 		}
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
 

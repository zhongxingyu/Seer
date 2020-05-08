 package state;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.io.File;
 import java.io.Serializable;
 
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 
 /**
  * Stores information about a structure.
  */
 public class Structure implements Serializable {
 	private final long serialVersionUID = 22222397388326626L;
 	/**
 	 * The coordinates of the tile under the bottom corner of the structure.
 	 */
 	private int x, y;
 	private int TILE_HEIGHT = 32;
 	private int TILE_WIDTH = 64;
 
 	/**
 	 * Size of the structure, in tiles.
 	 */
 	private int width, height;
 
 	/**
 	 * The structure's image.
 	 */
 	private Image image;
 
 	/**
 	 * Returns the X coordinate of the bottom corner of the structure.
 	 */
 	public int getX() {
 		return x;
 	}
 
 	/**
 	 * Returns the Y coordinate of the bottom corner of the structure.
 	 */
 	public int getY() {
 		return y;
 	}
 
 	/**
 	 * Returns the width of the structure, in tiles.
 	 */
 	public int getWidth() {
 		return width;
 	}
 
 	/**
 	 * Returns the height of the structure, in tiles.
 	 */
	public int getHeight() {WestFaceDirtRamp 5 5 45 24 32 177 37 164 188 23 150 150
		EastFaceDirtRamp 25 25 77 155
 
 		return height;
 	}
 
 	/**
 	 * Returns the structure's image.
 	 */
 	public Image getImage() {
 		return image;
 	}
 
 	/**
 	 * Creates a structure.
 	 * @param x The X coordinate of the bottom corner of the structure.
 	 * @param y The Y coordinate of the bottom corner of the structure.
 	 * @param width The width of the structure.
 	 * @param height The height of the structure.
 	 * @param image The path to the structure's image.
 	 */
 	public Structure(int x, int y, int width, int height, String image) {
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 		File imgFile = new File(image);
 		assert(imgFile.exists()) : image+" not found";
 		this.image = new ImageIcon(image).getImage();
 	}
 
 	/**
 	 * Draws the structure.
 	 * @param g The Graphics object to draw on.
 	 * @param width The width of the display.
 	 * @param camx The camera X.
 	 * @param camy The camera Y.
 	 */
 	public void draw(Graphics g, int width, int height, int camx, int camy){
 		int x = this.x - camx; //tile coords of structure
 		int y = this.y - camy; //tile coords of structure
 		int i = (width/2)-(image.getWidth(null)/2) + (x-y) * (TILE_WIDTH/2); //pixel coords of the structure
 		int j =  (x+y) * (TILE_HEIGHT/ 2) - height ;									 //pixel coords of the structure
 		//draws the structure at (i,j) using the images dimensions to find its final dimension
 		g.drawImage(image, i, j-image.getHeight(null), image.getWidth(null), image.getHeight(null), null);
 	}
 }

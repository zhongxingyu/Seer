 package main.world;
 
 import main.Main;
 import org.lwjgl.opengl.GL11;
 
 import editor.Tile;
 
 /**
  * The Square class holds the data for the physical tile on the screen.<br>
  * Every square has a corresponding Tile with a relationship of one tile to many squares.<br>
  * 
  * At the moment, the Square is defined by a colour but soon it will have a graphic over it and 
  * so will be defined by that.
  *
  */
 public class Square {
 
 	/** Is the square solid or not, can you pass through it? */
 	private boolean solid;
 	
 	/** The width of the square */
 	private int w = Main.gridSize;
 	
 	/** The height of the square */
 	private int h = Main.gridSize;
 	
 	/** The x coordinate of the square */
 	private int x;
 	
 	/** The y coordinate of the square */
 	private int y;
 	
 	private Tile tile;
 	
 	/** 
 	 * Constructor for the class. Defines a square to be drawn
 	 * 
 	 * @param x The x coordinate of the top left hand corner where the square is to be drawn
 	 * @param y The y coordinate of the top left hand corner where the square is to be drawn
 	 * @param tile The Tile object that the square references
 	 */
 	public Square(int x, int y, Tile tile) {
 		
 		this.x = x;
 		this.y = y;
 		
 		this.solid = tile.isSolid();
 		this.tile = tile;
 	}
 
 	/** Draw the square to the screen */
 	public void drawSquare() {
 		
		double tx = tile.getSymbol()/(float)16;
 		double tw = 0.0625;
 		double th = 0.0625;
 		
 		Main.TILESET.bind();
 		
 		GL11.glColor3f(1.0f, 1.0f, 1.0f);
 		
 		GL11.glBegin(GL11.GL_POLYGON);
 		
		    GL11.glTexCoord2d(tx, 0);
 		    GL11.glVertex2i(x, y);
 		    
		    GL11.glTexCoord2d(tx + tw, 0);
 		    GL11.glVertex2i(x + w, y);
 		    
		    GL11.glTexCoord2d(tx + tw, th);
 		    GL11.glVertex2i(x + w, y + h);
 		    
		    GL11.glTexCoord2d(tx, th);
 		    GL11.glVertex2i(x, y + h);
 		    
 		GL11.glEnd();
 
 		
 	}
 	
 	/** Return the x coordinate of the square */
 	public int getX() {
 		return x;
 	}
 
 	/** Set the x coordinate of the square */
 	public void setX(int x) {
 		this.x = x;
 	}
 
 	/** Return the y coordinate of the square */
 	public int getY() {
 		return y;
 	}
 
 	/** Set the y coordinate of the square */
 	public void setY(int y) {
 		this.y = y;
 	}
 
 	/** @return <code>true</code> if the square is solid<br><code>false</code> if it is not */
 	public boolean isSolid() {
 		return solid;
 	}
 
 	/** Sets the square solid or not */
 	public void setSolid(boolean solid) {
 		this.solid = solid;
 	}
 }

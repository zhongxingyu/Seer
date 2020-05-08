 package UI;
 
 import java.awt.*;
 import java.util.Random;
 
 import javax.swing.*;
 
 import state.Dude;
 import state.Tile;
 import state.World;
 
 public class Display extends JPanel{
 
 	// FIELDS
 	private final Dimension DIMENSION = new Dimension(1920,1080);
 	private final int VIEW_WIDTH = 70, VIEW_HEIGHT = 70;	// Camera = 60x60
 
 	private final int SCREEN_Y_DISPLACEMENT = 490; // Arbitrary y axis displacement
 	private final int SCREEN_BUFFER_ZONE = 20; // Arbitrary screen edge buffer
 
 	private World world;
 
 	//pixel size of each tile
 	private final int TILE_WIDTH = 64, TILE_HEIGHT = 32;
 
 	private int rotation = 0;
 	private Coord camera = new Coord(0,0); // ARBITRARY START POINT
 			// Camera stores coord of topmost tile
 
 
 	// CONSTRUCTOR
 	public Display(World world){
 		super();
 		setPreferredSize(DIMENSION); // Necessary?
 		this.world = world;
 	}
 
 	private static final long serialVersionUID = 8274011568777903027L;
 	// WHAT DOES THIS EVEN DO??
 
 
 
 	public int[] getCameraCoordinates(){
 		return new int[]{camera.x,camera.y};
 	}
 
 //	public void setCameraCoordinates(int[] coord){
 //		camera = new Coord(coord[0],coord[1]);
 //	}
 
 	public World getWorld(){
 		return world;
 	}
 
 	public void panLeft(int idx) {
 		if (camera.x - idx + SCREEN_BUFFER_ZONE < 0) {// Catch if out of bounds
 			return;
 		}
 		camera = new Coord(camera.x - idx, camera.y);
 	}
 
 	public void panRight(int idx) {
 		if (camera.x + idx + SCREEN_BUFFER_ZONE>= world.getXSize()) {// Catch if out of bounds
 			return;
 		}
 		camera = new Coord(camera.x + idx, camera.y);
 	}
 
 	public void panDown(int idy) {
 		if (camera.y + idy + SCREEN_BUFFER_ZONE >= world.getYSize()) {//ap.length) Catch if out of bounds
 			return;
 		}
 		camera = new Coord(camera.x, camera.y + idy);
 	}
 
 	public void panUp(int idy) {
 		if (camera.y - idy + SCREEN_BUFFER_ZONE < 0) {// Catch if out of bounds
 			return;
 		}
 		camera = new Coord(camera.x, camera.y - idy);
 	}
 
 	//RENDERING
 		public void paintComponent(Graphics g){
 			g.setColor(Color.PINK);
 			g.fillRect(0, 0, this.getWidth(), this.getHeight());
 			paintMap(g);
 		}
 
 	private Tile getCameraRelativeTile(int x, int y) {
 		int temp;
 		switch(rotation) {
 		case 0: break;
 		case 1: temp = VIEW_WIDTH-x; x = y; y = temp; break;
 		case 2: x=VIEW_WIDTH-x; y=VIEW_HEIGHT-y; break;
 		case 3: temp = VIEW_HEIGHT-y; y = x; x = temp; break;
 		}
 		return world.getTile(x+camera.x, y+camera.y);
 	}
 
 	public Point tileToDisplayCoordinates(double x, double y) {
 		x -= camera.x; y -= camera.y;
 
 		double temp;
 		switch(rotation) {
 		case 0: break;
 		case 3: temp = VIEW_WIDTH-x; x = y; y = temp; break;
 		case 2: x=VIEW_WIDTH-x; y=VIEW_HEIGHT-y; break;
 		case 1: temp = VIEW_HEIGHT-y; y = x; x = temp; break;
 		}
 
 		return new Point(getPixelX(x, y), getPixelY(x, y));
 	}
 
 	public Point displayToTileCoordinates(int x, int y) {
 		/*x -= camera.x; y -= camera.y;
 
		double temp;
		switch(rotation) {
		case 0: break;
		case 3: temp = VIEW_WIDTH-x; x = y; y = temp; break;
		case 2: x=VIEW_WIDTH-x; y=VIEW_HEIGHT-y; break;
		case 1: temp = VIEW_HEIGHT-y; y = x; x = temp; break;
		}
 
 		return new Point(getPixelX(x, y), getPixelY(x, y));*/
 
 		double xMinusY = (x - getWidth()/2) / (TILE_WIDTH/2.0); // ( x click - half width of screen )  / half the width of a tile
 		double xPlusY = ((y + SCREEN_Y_DISPLACEMENT) / (TILE_HEIGHT/2.0));		  // ( y click  /  half height of tile )
 
 		// finds integer value of square in array position and adjusts for
 		// where the camera is looking
 		int tileX = (int) ((xPlusY + xMinusY) / 2);
 		int tileY = (int) ((xPlusY - xMinusY) / 2);
 
 		tileX += camera.x;
 		tileY += camera.y;
 
 		return new Point(tileX, tileY);
 	}
 
 
 
 	private int getPixelX(double x, double y) {
 		return (int)((this.getWidth()/2) + (x-y) * (TILE_WIDTH/2));
 	}
 
 	private int getPixelY(double x, double y) {
 		return (int)((x+y) * (TILE_HEIGHT/ 2)-SCREEN_Y_DISPLACEMENT + TILE_HEIGHT);
 	}
 
 	/**Paints the "view" on-screen at any one time. The algorithm goes through,
 	 * drawing the tiles from the top down, and draws them on the graphics pane.
 	 */
 	private void paintMap(Graphics g){
 
 
 		for(int x = 0; x<VIEW_WIDTH; x++){
 			for(int y = 0; y<VIEW_HEIGHT; y++){
 				Tile t = getCameraRelativeTile(x, y);
 				if(t!=null){
 				//System.out.println("CAMERA: " + camera.x + " " + camera.y +".");
 
 				// minimum depth to render to
 				int minDepth;
 				Tile t1 = getCameraRelativeTile(x+1, y);
 				Tile t2 = getCameraRelativeTile(x, y+1);
 				int t1Depth = (t1 == null ? -10 : t1.getHeight());
 				int t2Depth = (t2 == null ? -10 : t2.getHeight());
 				minDepth = Math.min(t1Depth, t2Depth);
 
 				if(minDepth < t.getHeight())
 					minDepth++;
 
 				for(int σ = minDepth; σ <= t.getHeight(); σ++) {
 					// Translated tile coordinates to account for raised elevations (i,j)
 					int i = x - σ;
 					int	j = y - σ;
 					//displays each tile
 					g.drawImage(t.getImage(), getPixelX(i, j)-(TILE_WIDTH/2), getPixelY(i, j)-TILE_HEIGHT, TILE_WIDTH, t.getImage().getHeight(null), null);
 					if(t.getX() == hoverX && t.getY() == hoverY)
 						g.drawImage(hoverImage, getPixelX(i, j)-(TILE_WIDTH/2), getPixelY(i, j)-TILE_HEIGHT, TILE_WIDTH, 32, null);
 				}
 
 				int bottomPixelX = getPixelX(x - t.getHeight(), y - t.getHeight());
 				int bottomPixelY = getPixelY(x - t.getHeight(), y - t.getHeight());
 
 				if(t.getStructure() != null){ // If there is a structure in the tile --> DRAW HE/SHE/IT!
 					t.getStructure().draw(g, bottomPixelX, bottomPixelY);
 				}
 
 				if(t.getDude() != null){ // If there is a dude in the tile --> DRAW THEM!
 					t.getDude().draw(g, this, bottomPixelX, bottomPixelY);
 				}
 
 				}
 
 			}
 		}
 	}
 
 	public void rotate() {
 		rotation = (rotation + 1) % 4;
 	}
 
 	/**
 	 * Returns the number of steps clockwise the display is rotated, from 0 to 3.
 	 */
 	public int getRotation() {
 		return rotation;
 	}
 
 	private int hoverX, hoverY;
 	private Image hoverImage = new ImageIcon("Assets/Templates/TileTemplate.png").getImage();
 	public void setHighlightedTile(int x, int y) {
 		hoverX = x;
 		hoverY = y;
 	}
 }

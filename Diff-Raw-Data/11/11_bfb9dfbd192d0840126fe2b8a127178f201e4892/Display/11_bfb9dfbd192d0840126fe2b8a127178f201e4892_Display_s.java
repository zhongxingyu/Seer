 package UI;
 
 import java.awt.*;
 
 import javax.swing.*;
 
 import state.Tile;
 import state.World;
 
 public class Display extends JPanel{
 
 	// FIELDS
 	private final Dimension DIMENSION = new Dimension(1920,1080);
 	private final int VIEW_WIDTH = 30, VIEW_HEIGHT = 30;	// Camera = 60x60
 
 	private World world;
 
 	private final int TILE_WIDTH = 64, TILE_HEIGHT = 32;
 
 
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
 
 	//RENDERING
 	public void paintComponent(Graphics g){
 		paintMap(g);
 	}
 
//	public int[] getCameraCoordinates(){
//		return new int[]{camera.x,camera.y};
//	}
 
 //	public void setCameraCoordinates(int[] coord){
 //		camera = new Coord(coord[0],coord[1]);
 //	}
 
 	public void panLeft(int idx) {
 		if (camera.x - idx < 0) {// Catch if out of bounds
 			return;
 		}
 		camera = new Coord(camera.x - idx, camera.y);
 	}
 
 	public void panRight(int idx) {
 		if (camera.x + 29 + idx >= world.getXSize()) {// Catch if out of bounds
 			return;
 		}
 		camera = new Coord(camera.x + idx, camera.y);
 	}
 
 	public void panDown(int idy) {
 		if (camera.y + 29 + idy >= world.getYSize()) {//ap.length) Catch if out of bounds
 			return;
 		}
 		camera = new Coord(camera.x, camera.y + idy);
 	}
 
 	public void panUp(int idy) {
 		if (camera.y - idy < 0) {// Catch if out of bounds
 			return;
 		}
 		camera = new Coord(camera.x, camera.y - idy);
 	}
 
 	/**Paints the "view" on-screen at any one time. The algorithm goes through,
 	 * drawing the tiles from the top down, and draws them on the graphics pane.
 	 *
 	 * @param g OVERRIDEN Parameter.
 	 */
 	private void paintMap(Graphics g){
 
 		for(int x = 0; x<VIEW_WIDTH; x++){
 			for(int y = 0; y<VIEW_HEIGHT; y++){
 				Tile t = world.getTile(x+camera.x,y+camera.y);
 				//System.out.println("CAMERA: " + camera.x + " " + camera.y +".");
 
 
 				/*This is the "magic line" -- It calculates the position of the
 				 * tile on screen, and was a slightly tricky piece of trigonometry.
 				 *
 				 * DON'T CHANGE IT UNLESS YOU *REALLY* KNOW WHAT YOU'RE DOING.
 				 * Bask in it's majesty and awe-inspiring splendour.
 				 */
 
 				// Translated tile coordinates to account for raised elevations (i,j)
				int i = x + t.getHeight();
				int	j = y + t.getHeight();
 
 				g.drawImage(t.getImage(), (this.getWidth()/2)-(TILE_WIDTH/2) + (i-j) * (TILE_WIDTH/2), (i+j) * (TILE_HEIGHT/ 2), TILE_WIDTH, t.getImage().getHeight(null), null);
 				if(t.getStructure() != null){ // If there is a structure in the tile --> DRAW HE/SHE/IT!
 					t.getStructure().draw(g, this.getWidth(),camera.x,camera.y);
 				}
 				if(t.getDude() != null){ // If there is a dude in the tile --> DRAW THEM!
 					t.getDude().draw(g, this.getWidth(),camera.x,camera.y);
 				}
 			}
 		}
 	}
 }

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
			g.setColor(new Color(new Random().nextInt()));
 			g.fillRect(0, 0, this.getWidth(), this.getHeight());
 			paintMap(g);
 		}
 
 	/**Paints the "view" on-screen at any one time. The algorithm goes through,
 	 * drawing the tiles from the top down, and draws them on the graphics pane.
 	 */
 	private void paintMap(Graphics g){
 
 
 		for(int x = 0; x<VIEW_WIDTH; x++){
 			for(int y = 0; y<VIEW_HEIGHT; y++){
 				Tile t = world.getTile(x+camera.x,y+camera.y);
 				if(t!=null){
 				//System.out.println("CAMERA: " + camera.x + " " + camera.y +".");
 
 				// minimum depth to render to
 				int minDepth;
 				Tile t1 = world.getTile(x+camera.x+1, y+camera.y);
 				Tile t2 = world.getTile(x+camera.x, y+camera.y+1);
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
 					g.drawImage(t.getImage(), (this.getWidth()/2)-(TILE_WIDTH/2) + (i-j) * (TILE_WIDTH/2), (i+j) * (TILE_HEIGHT/ 2)-SCREEN_Y_DISPLACEMENT, TILE_WIDTH, t.getImage().getHeight(null), null);
 				}
 
 				if(t.getStructure() != null){ // If there is a structure in the tile --> DRAW HE/SHE/IT!
 					t.getStructure().draw(g, this.getWidth(),SCREEN_Y_DISPLACEMENT,camera.x,camera.y);
 				}
 
 				if(t.getDude() != null){ // If there is a dude in the tile --> DRAW THEM!
 					t.getDude().draw(g, this.getWidth(),SCREEN_Y_DISPLACEMENT,camera.x,camera.y);
 				}
 
 				}
 
 			}
 		}
 	}
 }

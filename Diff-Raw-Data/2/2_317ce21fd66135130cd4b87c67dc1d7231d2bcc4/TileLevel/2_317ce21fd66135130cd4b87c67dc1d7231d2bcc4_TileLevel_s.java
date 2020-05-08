 package rajola.pipeline.level;
 
 import java.io.File;
 
 import javax.imageio.ImageIO;
 
 import org.newdawn.slick.Image;
 
 import rajola.pipeline.Tile;
 import rajola.pipeline.tools.ImageTools;
 
 /**
  * 
  * @author Jacob Bevilacqua
  *
  *The TileLevel class provides an alternative to hardcoding tiles into a map
  *TileLevel generates a level from an image and then renders them onto the screen
  *TileLevel does not handle entity loading or entity rendering
  *
  */
 
 public class TileLevel {
 
 	private int height , width;
	private int screenHeight = 128 , screenWidth = 128; //TODO: should be defined by user just for testing
 	private String identifier;
 	private String path;
 	private Tile[] tileSet; //The position in the tileSet should corespond to the tiles ID
 	private Tile nullTile; //TODO: Create a nullTile
 	
 	private int xOffset , yOffset;
 	private int shiftCount;
 	
 	private int[] tiles;
 	private Image mapImage;
 		
 	public TileLevel() {}
 	
 	public TileLevel(String imagePath) {
 		this.path = imagePath;
 	}
 	
 	public TileLevel(String imagePath , Tile[] tileSet) {
 		this(imagePath);
 		this.setIdentifier("Untitled Rajola Tile Level");
 		this.loadLevelFromFile();
 	}
 	
 	public TileLevel(String imagePath , String identifier , Tile[] tileSet) {
 		this.path = imagePath;
 		this.tileSet = tileSet;
 		this.identifier = identifier;
 		this.loadLevelFromFile();
 	}
 	
 	public void loadLevelFromFile() {
 		try {
 			this.mapImage = ImageTools.toSlickImage(ImageIO.read(new File(path)));
 			this.width = this.mapImage.getWidth();
 			this.height = this.mapImage.getHeight();
 			tiles = new int[width * height];
 			this.loadTiles();
 			this.shiftCount = this.tileShiftTest();
 		} catch(Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void loadTiles() { 
 		int tileColors[] = new int[mapImage.getHeight() * mapImage.getWidth()];
 		
 		//This double forloop populates the tileColors array
 		for(int y1 = 0 ; y1 < this.height ; y1++) {
 			for(int x1 = 0 ; x1 < this.width ; x1++) {
 				int rgb = this.mapImage.getColor(x1, y1).getRed();
 				rgb = (rgb << 8) + this.mapImage.getColor(x1, y1).getGreen();
 				rgb = (rgb << 8) + this.mapImage.getColor(x1, y1).getBlue();
 				tileColors[x1 + y1 * width] = rgb;
 			}
 		}
 		
 		//This double forloop looks through the tileColors array and creates an array of tiles
 		for(int y = 0 ; y < height ; y++) {
 			for(int x = 0 ; x < width ; x++) {
 //				System.out.println(tileColors[x+y*width]);
 				tileCheck: for(Tile t : this.tileSet) {
 					if(t != null && t.getLevelColor() == tileColors[x + y * width]) {
 						this.tiles[x + y * width] = t.getId();
 						System.out.println(t.getId());
 						break tileCheck;
 					}
 				}
 			}
 		}
 	}
 	
 	public void renderTiles(int xOffset , int yOffset) {	
 		int tileSize = this.tileSet[0].getSprite().getHeight();
 		setOffset(xOffset , yOffset);
 		
 		int x0 = this.xOffset >> this.shiftCount;
 		int x1 = (this.xOffset + screenWidth + tileSize) >> this.shiftCount;
 		int y0 = this.yOffset >> this.shiftCount;
 		int y1 = (this.yOffset + screenHeight + tileSize) >> this.shiftCount;
 
 		for (int y = y0; y < y1; y++) {
 			for (int x = x0; x < x1; x++) {
 				getTile(x , y).render(x << this.shiftCount, y << this.shiftCount);
 			}
 		}
 	}
 	
 	  public Tile getTile(int x, int y) {
 	        if (0 > x || x >= screenWidth || 0 > y || y >= screenHeight) {
 	        	return tileSet[1]; //TODO: Change this to return the nullTile.
 	        } else {
 	        	for(int i = 0 ; i < this.tileSet.length ; i++) {
 	        		if(tiles[x + y * width] == this.tileSet[i].getId()) {
 	        			return this.tileSet[i];
 	        		}
 	        	}
 	        }
 			return null; //TODO: return void tile
 	       
 	    }
 
 	private int tileShiftTest() {
 		int r = 0;
 		int hold = this.tileSet[0].getSprite().getHeight();
 		
 		for(int i = 0 ; i < this.tileSet[0].getSprite().getHeight() ; i++) {
 			hold /= 2;
 			r++;
 			if(hold <= 1) {
 				break;
 			}
 		}
 		return r;
 	}
 	  
 	private void setOffset(int xOffset, int yOffset) {
 		this.xOffset = xOffset;
 		this.yOffset = yOffset;
 	}
 
 	public String getIdentifier() {
 		return identifier;
 	}
 
 	public void setIdentifier(String identifier) {
 		this.identifier = identifier;
 	}
 	
 	public void setPath(String path) {
 		this.path = path;
 	}
 	
 	public String getPath() {
 		return path;
 	}
 	
 	public void setTileSet(Tile[] tileSet) {
 		this.tileSet = tileSet;
 	}
 
 	public void update(int DELTA) {
 		for(int i = 0 ; i < this.tileSet.length ; i++ ) {
 			this.tileSet[i].update(DELTA);
 		}
 	}
 	
 }

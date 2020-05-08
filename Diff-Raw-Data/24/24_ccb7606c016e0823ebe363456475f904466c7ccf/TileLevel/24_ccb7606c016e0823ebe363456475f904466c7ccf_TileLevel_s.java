 package rajola.pipeline.level;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 
 import javax.imageio.ImageIO;
 
 import rajola.pipeline.Tile;
 
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
 	private int screenHeight = 256 , screenWidth = 256; //TODO: should be defined by user just for testing
 	private String identifier;
 	private String path;
 	private Tile[] tileSet; //The position in the tileSet should corespond to the tiles ID
 	
 	private int xOffset , yOffset;
 	private int shiftCount;
 	
 	private int[] tiles;
 	private BufferedImage mapImage;
 	
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
 			this.mapImage = ImageIO.read(new File(path));
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
 		int tileColors[] = this.mapImage.getRGB(0, 0, width, height, null, 0, width);
 		for(int y = 0 ; y < height ; y++) {
 			for(int x = 0 ; x < width ; x++) {
 				tileCheck: for(Tile t : this.tileSet) {
 					if(t != null && t.getLevelColor() == tileColors[x + y * width]) {
 						this.tiles[x + y * width] = t.getId();
 						break tileCheck;
 					}
 				}
 			}
 		}
 	}
 	
 	public void renderTiles(int xOffset , int yOffset) {	
 		int tileSize = this.tileSet[0].getSprite().getHeight();
 		setOffset(xOffset , yOffset);
 		
 		int x0 = xOffset >> this.shiftCount;
 		int x1 = (xOffset + screenWidth + tileSize) >> this.shiftCount;
 		int y0 = yOffset >> this.shiftCount;
 		int y1 = (yOffset + screenHeight + tileSize) >> this.shiftCount;
 
 		for (int y = y0; y < y1; y++) {
 			for (int x = x0; x < x1; x++) {
 				getTile(x , y).render(x << this.shiftCount, y << this.shiftCount);
 			}
 		}
 	}
 	
 	  public Tile getTile(int x, int y) {
 	        if (0 > x || x >= screenWidth || 0 > y || y >= screenHeight) {
	        	//Return void tile
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

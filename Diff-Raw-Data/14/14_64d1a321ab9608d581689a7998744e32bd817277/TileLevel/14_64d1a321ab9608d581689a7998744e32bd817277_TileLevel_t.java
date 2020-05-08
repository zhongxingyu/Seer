 package rajola.pipeline.level;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
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
 
 private String mapImagePath;
 private int mapImageHeight;
 private int mapImageWidth;
 private Image mapImage;
 private int[] pixelsOnMapImage; //The pixels of mapImage
 
 private int screenWidth = 512;
 private int screenHeight = 512;
 GameContainer gc;
 
 private int[] tileIdMap; //Shouldn't need to be used
 private Tile[] tileMap;
 
 private int shiftCount; //The amount of bitshifts it takes to get the size of the tile back to 1px
 
 private List<Tile> tileSet = new ArrayList<Tile>(); //Collection of tiles to be used
 private Tile nullTile; //This is not to be used anywhere but the constructor use the tile list instead to access this tile
 private int nullTileID; //the tileID of the nullTile
 private int xOffset;
 private int yOffset;
 	
 	public TileLevel(Tile[] tileSet, String mapImagePath , Tile nullTile) {	
 		this.nullTile = nullTile;
 		
 		for(int i = 0 ; i < tileSet.length ; i++) {
 			this.tileSet.add(tileSet[i]);
 		}
 		//The next three lines set the null tile's id to one larger than the last tile in the tileSet then adds the null tile to the tileSet
 		this.nullTile.setId((this.tileSet.size() + 1));
 		this.nullTileID = (nullTile.getId());
 		this.tileSet.add(nullTile);
 		this.mapImagePath = mapImagePath;
 		loadImage(this.mapImagePath);
 		fillTile();
 		this.shiftCount = ImageTools.shiftCounter(this.tileSet.get(0).getSize());
 	}
 	
 	public TileLevel(Tile[] tileSet, String mapImagePath , Tile nullTile , GameContainer gc) {
 	this.nullTile = nullTile;
 		
 		for(int i = 0 ; i < tileSet.length ; i++) {
 			this.tileSet.add(tileSet[i]);
 		}
 		//The next three lines set the null tile's id to one larger than the last tile in the tileSet then adds the null tile to the tileSet
 		this.nullTile.setId((this.tileSet.size() + 1));
 		this.nullTileID = (nullTile.getId());
 		this.tileSet.add(nullTile);
 		this.mapImagePath = mapImagePath;
 		loadImage(this.mapImagePath);
 		fillTile();
 		this.shiftCount = ImageTools.shiftCounter(this.tileSet.get(0).getSize());
 		this.screenHeight = gc.getScreenHeight();
 		this.screenWidth = gc.getScreenWidth();
 	}
 
 
 	private void loadImage(String mapImagePath) {
 		try {
 			this.mapImage = new Image(mapImagePath);
 			this.mapImageHeight = this.mapImage.getHeight();
 			this.mapImageWidth = this.mapImage.getWidth();
 			this.pixelsOnMapImage = new int[this.mapImageHeight * this.mapImageWidth];
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		//Populates pixelsOnMapImage with rgb data from mapImage
 		for(int y = 0 ; y < this.mapImageHeight ; y++) {
 			for(int x = 0 ; x < this.mapImageWidth ; x++) {
 				int rgb = this.mapImage.getColor(x, y).getRed();
 				rgb = (rgb << 8) + this.mapImage.getColor(x, y).getGreen();
 				rgb = (rgb << 8) + this.mapImage.getColor(x, y).getBlue();
 				this.pixelsOnMapImage[x + y * this.mapImageWidth] = rgb;
 			}
 		}
 	}
 	
 	/*
 	 * Populates the tileIdMap & tileMap
 	 */
 	private void fillTile() {
 		this.tileIdMap = new int[this.pixelsOnMapImage.length];
 		this.tileMap = new Tile[this.pixelsOnMapImage.length];
 		
 		for(int index = 0 ; index < this.tileIdMap.length ; index++) {
 			this.tileIdMap[index] = -1;
 		}
 		
 		for(int i = 0 ; i < this.pixelsOnMapImage.length ; i++) {
 			for(int currentTileID = 0 ; currentTileID < this.tileSet.size() ; currentTileID++) {
 				if(this.pixelsOnMapImage[i] == this.tileSet.get(currentTileID).getLevelColor()) {
 					this.tileIdMap[i] = this.tileSet.get(currentTileID).getId();
 					break;
 				}
 			}
 			if(this.tileIdMap[i] == -1) this.tileIdMap[i] = this.nullTileID;
 		}
 		
 		for(int i = 0 ; i < this.tileMap.length ; i++) {
 			for(int currentTile = 0 ; currentTile < this.tileSet.size() ; currentTile++) {
 				if(this.tileIdMap[i] == this.tileSet.get(currentTile).getId()) {
 					this.tileMap[i] = this.tileSet.get(currentTile);
 					break;
 				}
 			}
 		}
 	}
 
 
 /*All methods below are part of the TileLevel workflow
  * Workflow:
  * 1.Update
  * 2.Render
  */
 
 	public void Update(int DELTA , int xOffset , int yOffset) {
 		for(int i = 0 ; i < this.tileSet.size() ; i++ ) {
 			this.tileSet.get(i).update(DELTA);
 		}
 		this.xOffset = xOffset;
 		this.yOffset = yOffset;
 	}
 	
 	public void Render() {
 		for(int x = 0 ; x < this.mapImageWidth ; x++ ) {
 			for(int y = 0 ; y < this.mapImageHeight ; y++) {
 				this.tileMap[x + y * this.mapImageWidth].render((x << this.shiftCount) + xOffset , (y << this.shiftCount) + yOffset);
 			}
 		}
 	}
 
 
 //All methods below are getters and setters
 	public int getXOffset() {
 		return this.xOffset;
 	}
 	public int getYOffset() {
 		return this.yOffset;
 	}
 	public Tile getTileAt(int x , int y) {
		return this.tileMap[( (-this.xOffset + x) >> this.shiftCount )+ ( ((-this.yOffset + y) >> this.shiftCount )  * this.mapImageWidth)];
	}
	public Tile[] getTileMap() {
		return this.tileMap;
	}
	public int getWidth() {
		return this.mapImageWidth >> this.shiftCount;
	}
	public int getHeight() {
		return this.mapImageHeight >> this.shiftCount;
	}
	public int getShiftCount() {
		return this.shiftCount;
 	}
 }

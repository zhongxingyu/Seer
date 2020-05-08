 package map;
import image.SpriteSheet;

 import java.awt.*;
 
 import javax.imageio.ImageIO;
 import java.io.File;
 import java.io.IOException;
 
 import model.Drawable;
 
 public class Map implements Drawable {
 	public static final int MAP_OFFSET_X = 20;
 	public static final int MAP_OFFSET_Y = 50;
 	public static final int MAP_WIDTH = 15;
 	public static final int MAP_HEIGHT = 10;
 
 	private Tile[][] tiles;
	private SpriteSheet tileSet;
 
 	public Map() {
 		tiles = new Tile[MAP_HEIGHT][MAP_WIDTH];
		tileSet = new SpriteSheet(0,0,Tile.TILE_WIDTH, Tile.TILE_HEIGHT, "filename");
 	}
 
 	public Tile[][] getTiles() {
 		return tiles;
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		for (int h = 0; h < MAP_HEIGHT; h++) {
 			for (int w = 0; w < MAP_WIDTH; w++) {
 				if (tiles[h][w] != null) {   //TODO, once we have a tile image, this can be changed
					g.drawImage(tileSet.getImage(), MAP_OFFSET_X + w * Tile.TILE_WIDTH,
 							MAP_OFFSET_Y + h * Tile.TILE_HEIGHT, Tile.TILE_WIDTH,
 							Tile.TILE_HEIGHT, null);
 				}else{
 					g.setColor(Color.red);
 					g.drawRect(MAP_OFFSET_X + w * Tile.TILE_WIDTH,
 							MAP_OFFSET_Y + h * Tile.TILE_HEIGHT, Tile.TILE_WIDTH,
 							Tile.TILE_HEIGHT);
 				}
 
 			}
 		}
 	}
 
 }

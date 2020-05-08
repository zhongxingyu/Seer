 package saneMap;
 
 import java.util.Random;
 
 /**
  * A collection of MapTiles.
  * Class will handle inter-tile environment things. (if that is ever included)
  */
 public class MapArea {
 
 	//Minimum and maximum sizes for map generation.
 	public final int MAP_MIN_X_SIZE = 20;
 	public final int MAP_MIN_Y_SIZE = 20;
 	public final int MAP_MAX_X_SIZE = 100;
 	public final int MAP_MAX_Y_SIZE = 100;
 	
 	private MapTile[][] mapTiles;
 	private int mapXSize = 0;
 	private int mapYSize = 0;
 	
 	private Random rng;
 	
 	//Map generation
 	public MapArea(int seed){
 		
 		rng = new Random(seed);
 		
 		mapXSize = rng.nextInt(MAP_MAX_X_SIZE - MAP_MIN_X_SIZE) + MAP_MIN_X_SIZE;
 		mapYSize = rng.nextInt(MAP_MAX_Y_SIZE - MAP_MIN_Y_SIZE) + MAP_MIN_Y_SIZE;
 
 		mapTiles = new MapTile[mapXSize][mapYSize];
 				
 		for (int x = 0; x < mapXSize; x++){
 			for (int y = 0; y < mapYSize; y++){
 				mapTiles[x][y] = new MapTile(TileType.WALL);
 			}
 		}
 		
 	}
 	
 	public int getXSize(){
 		return mapXSize;
 	}
 	public int getYSize(){
 		return mapYSize;
 	}
 	public MapTile getTile(int x, int y){
 		return mapTiles[x][y];
 	}
 	public Random getRandom(){
 		return rng;
 	}
 	
 }

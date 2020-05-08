 package terrain;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 import android.gameengine.icadroids.persistence.GamePersistence;
 import android.util.Log;
 
 /**
  * @author Jasper Bruhn
  * @version 2.0
  */
 public class Terrain {
 	private static final String FILEPREFIX = "";
 	
 	// path definition for the chunks save files
 	public static final String CHUNKDATAFILE = FILEPREFIX +  "ChunkData.txt";
 	public static final String GENERATEDCHUNKLIST = FILEPREFIX + "ChunkList.txt";
 	
 	// current chunk
 		private static int[][] chunk = new int[20][20];
 		private static int x;
 		private static int y;
 		
 	// ore gen percentages ordered by x coordinate of the chunk
 	static int[][] genPercent = 
 		{
 			{-1, 100},
 			{ 0, 100, 1, 10, 2, 5, 3, 8},
 			{ 0, 100, 1, 12, 2, 8, 3, 9},
 			{ 1, 100}
 		};
 	
 	/**
 	 *  Function used to create files on startup
 	 */
 	public static void createFiles() {
 		GamePersistence chunkData = new GamePersistence(CHUNKDATAFILE);
 		GamePersistence chunkList = new GamePersistence(GENERATEDCHUNKLIST);
 		
 		if (chunkData.loadData().equals("")) {
 			Log.i("CreateFiles", "Creating file: " + CHUNKDATAFILE);
 			chunkData.saveData("");
 		}
 		
 		if (chunkList.loadData().equals("")) {
 			Log.i("CreateFiles", "Creating file: " + GENERATEDCHUNKLIST);
 			chunkList.saveData("");
 		}
 	}
 	
 	/**
 	 * Function used to fill the tilemap based on the player position
 	 * 
 	 * @param chunkX the variable to identify the chunk the player is currently in
 	 * @param chunkY the variable to identify the chunk the player is currently in
 	 * @param playerX player position relative to the center chunk
 	 * @param playerY player position relative to the center chunk
 	 * @return the tilemap used to fill the screen
 	 */
 	public static int[][] getTileMap(int chunkX, int chunkY, int playerX, int playerY) {
 		Log.i("GetTileMap", "");
 		return cropTileMap(fillTileMap(chunkX, chunkY), playerX, playerY);
 	}
 	
 	/**
 	 * Function used to fill the initial tile map 
 	 * 
 	 * @param centerChunkX the coordinates of the center chunk
 	 * @param centerChunkY the coordinates of the center chunk
 	 * @return the initial tile map
 	 */
 	private static int[][] fillTileMap(int centerChunkX, int centerChunkY) {
 		Log.i("FilleTileMap", "");
 		int[][] tileMap = new int[60][60];
 		
 		for (int x = 0; x < 3; x++) {
 			for (int y = 0; y < 3; y++) {
 			
 				getChunk((centerChunkX - 1) + x, (centerChunkY - 1) + y);
 				
 				for (int i = (x * 20); i < ((20 * x) + 20); i++) {
 					for (int k = (y * 20); k < ((20 * y) + 20); k++) {
 						tileMap[i][k] = chunk[i - (x * 20)][k - (y * 20)];
 					}
 				}
 				
 			}
 		}
 		
 		return tileMap;
 	}
 	
 	/**
 	 * Function used to crop the tile map to fit the standard screen size
 	 * 
 	 * @param tileMap the tilemap to crop
 	 * @param pX the player position within the center chunk
 	 * @param pY the player position within the center chunk
 	 * @return the cropped tile map
 	 */
 	private static int[][] cropTileMap(int[][] tileMap, int pX, int pY) {
		Log.i("CropTileMap", "px: " + pX + " pY: " + pY);
		int[][] croppedTileMap = new int[20][20];
 		
		for (int i = 0; i < croppedTileMap[0].length; i++) {
			for (int k = 0; k < croppedTileMap.length; k++) {
				Log.i("TEST_TEST", "i: " + i + " k: " + k + "  ");
				croppedTileMap[i][k] = tileMap[((pX + 20) - (croppedTileMap[0].length / 2)) + i][((pY + 20) - (croppedTileMap.length / 2)) + k];
 			}
 		}
 		
 		return croppedTileMap;
 	}
 	
 	/**
 	 * Returns the chunk variable from the current class.
 	 * 
 	 * @param x coordinate used to identify the chunk position
 	 * @param y coordinate used to identify the chunk position
 	 * @return the chunk data for the currently saved chunk within the Terrain class
 	 */
 	public static int[][] getChunk(int x, int y) {
 		Log.i("GetChunk", "");
 		if (!(x == Terrain.x && y == Terrain.y)) {
 			if (chunkExists(x, y)) {
 				getChunkFromMem(x, y, CHUNKDATAFILE, GENERATEDCHUNKLIST);
 			} else {
 				// generate the chunk
 				// after generation the chunk is automatically saved as chunk
 				generateChunk(x, y);
 			}
 		}
 		
 		return chunk;
 	}
 	
 	/**
 	 * Generates new chunk based on the given coordinates
 	 * and loads the date for that chunk into the static memory of the Terrain class.
 	 * 
 	 * @param x coordinate used to identify the chunk position
 	 * @param y coordinate used to identify the chunk position
 	 */
 	public static void generateChunk(int x, int y) {
 		Log.i("GenerateChunk", "");
 		clearChunk(genPercent[x][0]);
 		
 		for (int t = 2; t < genPercent[x].length; t += 2) {
 			fillChunk(genPercent[x][x], genPercent[x][x + 1]);
 		}
 		
 		Terrain.x = x;
 		Terrain.y = y;
 	}
 	
 	/**
 	 * Function used to fill the current chunk with block data
 	 * 
 	 * @param tileType the ore type to be generated
 	 * @param chanceOfGen the chance of gen for this block
 	 */
 	private static void fillChunk(int tileType, float chanceOfGen){
 		Log.i("FillChunk", "");
 		Random rn = new Random();
 		double rndmNr;
 		
 		for (int i = 0; i < chunk.length; i ++) {
 			for (int k = 0; k < chunk[0].length; k++) {
 				rndmNr = rn.nextDouble() * 100;
 				
 				if (rndmNr < chanceOfGen) {
 					chunk[i][k] = tileType;
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Function to fill the current chunk with a specific type of block
 	 * 
 	 * @param nr the block id to fill the chunk with
 	 */
 	private static void clearChunk(int nr) {
 		Log.i("ClearChunk", "");
 		for (int i = 0; i < chunk.length; i ++) {
 			for (int k = 0; k < chunk[0].length; k++) {
 				chunk[i][k] = nr;
 			}
 		}
 	}
 	
 	/**
 	 * Looks for the chunk id within the save file.
 	 * 
 	 * @param x coordinate used to identify the chunk position
 	 * @param y coordinate used to identify the chunk position
 	 * @return true if the chunk exists within the save file
 	 */
 	public static boolean chunkExists(int x, int y) {
 		Log.i("ChunkExists", "");
 		return (getChunkDataRow(x, y, GENERATEDCHUNKLIST) != -1);
 	}
 	
 	/**
 	 * Function used to get the index for the start of the chunk within the save file
 	 * 
 	 * @param x chunk coordinate 
 	 * @param y chunk coordinate
 	 * @param fileName filename to locate the index
 	 * @return the found index/ -1 if the file doesn't hold the coordinates
 	 */
 	private static int getChunkDataRow(int x, int y, String fileName) {
 		Log.i("GetChunkDataRow", "");
 		int count = 0;
 		
 		GamePersistence data = new GamePersistence(fileName);
 		List<String> list = Arrays.asList((data.loadData()).split(","));
 		
 		for (int i = 0; i < list.size(); i++) {
 			if (!(list.get(i).equals(x) && list.get(i + 1).equals(y))) {
 				count ++;
 			}
 		}
 		
 		if (count >= list.size()) {
 			count = -1;
 		}
  		
 		return count;
 	}
 	
 	/**
 	 * Function used to pull a specific chunk from the save file.
 	 * 
 	 * @param x coordinate used to identify the chunk
 	 * @param y coordinate used to identify the chunk
 	 * @param dataFile the file to pull the chunk data from usually Terrain.CHUNKDATAFILE
 	 * @param chunkListFile the file to pull the data position from usually Terrain.GENERATEDCHUNKLIST
 	 * @return the newly loaded chunk
 	 */
 	@SuppressWarnings("null")
 	public static int[][] getChunkFromMem(int x, int y, String dataFile, String chunkListFile) {
 		Log.i("GetChunkFromMem", "");
 		GamePersistence data = new GamePersistence(dataFile);
 
 		List<String> list = Arrays.asList((data.loadData()).split(","));
 		
 		int startLocation = getChunkDataRow(x, y, chunkListFile);
 		
 		List<Integer> finalList = null;
 		
 		for (int i = 0; i < chunk.length; i++) {
 			for (int k = 0; k < chunk[0].length; k++) {
 				finalList.add(Integer.parseInt(list.get((startLocation * (chunk.length * chunk.length)) + k + i)));
 			}
 		}
 		
 		for (int i = 0; i < chunk.length; i++) {
 			for (int k = 0; k < chunk[0].length; k++) {
 				chunk[i][k] = finalList.get(i + k);
 			}
 		}
 		
 		Terrain.x = x;
 		Terrain.y = y;
 		
 		return chunk;
 	}
 }

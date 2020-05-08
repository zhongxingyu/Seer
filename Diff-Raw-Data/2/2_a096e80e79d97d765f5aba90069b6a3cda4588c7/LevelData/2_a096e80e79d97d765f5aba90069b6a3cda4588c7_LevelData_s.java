 package objectManagers;
 
 import gameObjects.BoundingObject;
 import graphics.BoundingSprite;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import physics.Physics;
 import processing.core.PApplet;
 import utils.Utils;
 
 public class LevelData {
 
 	private PApplet gameScreen;
 	private JSONParser parser = new JSONParser();
 	
 	private JSONObject level;
 	private JSONArray levelLayers, levelTilesets;
 	
 	// Not sure why this have to be longs, but Java does't like it if they're not
 	private Long levelHeight;
 	private Long levelWidth2;
 	private Long levelTileHeight;
 	private Long levelTileWidth;
 	private ArrayList<HashMap<String, Object>> levelTileDefs = new ArrayList<HashMap<String, Object>>();
 	
 	private int xDistanceFromLeftWall = 0;
 	private int levelWidth = 800;
 	private float levelWidthPixels = (float) getLevelWidth() * Utils.scaleXValue;
 
 	public LevelData (PApplet gameScreen){
 		loadLevel();
 		this.gameScreen = 	gameScreen;
 	}
 	
 	public void draw(int x, int y) {
 
 		gameScreen.strokeWeight(4);
 		gameScreen.stroke(0);
 
 	} // end draw
 
 	public void loadLevel() {
 		
 		try {
 			// Get the Level JSON file and cast it to a JSONObject
			String path = "../levels/level1.json";
 			Object raw = parser.parse(new FileReader(path));
 			level = (JSONObject)raw; 
 			
 			// Extract high-level level data from JSON
 			levelTileHeight = (Long)level.get("tileheight");  // Height of individual tiles
 			levelTileWidth = (Long)level.get("tilewidth");    // Width of individual tiles
 			levelHeight = (Long)level.get("height");          // Height of entire map
 			levelWidth2 = (Long)level.get("width");           // Width of entire map
 			
 			levelLayers = (JSONArray)level.get("layers");     // Get JSON Array of "layers" of map
 			levelTilesets = (JSONArray)level.get("tilesets"); // Get JSON Array of the tileset information
 			
 			// Iterate through each of the tilesets and get relevant information from them
 			for(int i = 1; i <= levelTilesets.size(); i++) {
 				JSONObject tile = (JSONObject)levelTilesets.get(i - 1); // Get the i-1 tile
 				HashMap<String, Object> tileDef = new HashMap<String, Object>();  // Create a HashMap to put the data into
 				
 				// Add the relevant data from each tileset into the hashmap
 				tileDef.put("gid", Integer.valueOf(tile.get("firstgid").toString())); 
 				tileDef.put("image", tile.get("image").toString());
 				tileDef.put("props", tile.get("tileproperties"));
 				
 				// And add that tileset's information to list of tileset definitions
 				levelTileDefs.add(tileDef);
 			}
 			
 			// Iterate through layers
 			for(int i = 1; i <= levelLayers.size(); i++) {
 				JSONObject levelLayer = (JSONObject)levelLayers.get(i - 1);
 				long tileXPos = 0;
 				long tileYPos = 0;
 				
 				/* First, we check if the layer is visible, if it's not, we don't care because
 				 * we don't have to draw it.
 				 * TODO -- Add support for invisible collision layers
 				 */
 				if((Boolean) levelLayer.get("visible")) {
 					// Get the layer information
 					ArrayList<Long> tmpLevelData = (ArrayList<Long>) levelLayer.get("data");
 					
 					/* 		Iterate through the rows / columns of the level
 					 *   	NOTE: The level data array is a 1D array, but we want to process it like a 2D array
 					 *	 	j*levelWidth + k gets us the element in the 1D array as if it's a 2D array
 					 */
 					for(int j = 0; j < levelHeight; j++) {
 						for(int k = 0; k < levelWidth2; k++) {
 							if(tmpLevelData.get((int)((j*levelWidth2)+k)) == 0) continue; // If there is no data at the element, pass
 							else {
 								// Get the 2D position of the tile
 								tileXPos = levelTileWidth * k;
 								tileYPos = levelTileHeight * j;
 								
 								// Get the tiles value in the 1D array (see comment above about equation) ...
 								// Note: Idx refers to the fact that the value is used as an Index for the tiledefs array
 								//       NOTE that the value is the index of the tile in the leveldata array.
 								Long tileIdx = tmpLevelData.get((int)((j*levelWidth2)+k));
 								// .. and use it to access the the tile definition of that tile in the TileDefs JSON Object
 								// .. and get the relevant data
 								JSONObject tmp = (JSONObject)levelTileDefs.get(tileIdx.intValue()-1).get("props");
 								
 								/* TODO - Right now this is assuming that every image is a separate file and they all have 
 								 * 			and index of 0. If we use tilesets (which we should), I need to code in logic here
 								 * 			to select and create the correct "subimage" based on the values here.
 								 */
 								String imagePath = (String)levelTileDefs.get(tileIdx.intValue()-1).get("image");
 								
 								// Use an Iterator to loop through the levelDefs array and "dig" for property setting a tile to 
 								// either a floor or a wall
 								Iterator it = tmp.entrySet().iterator();
 								while (it.hasNext()){
 									Map.Entry pairs = (Map.Entry)it.next();
 									JSONObject eachProp = (JSONObject)pairs.getValue();
 									
 									if (!(eachProp.get("floor") == null)) {
 										if ((boolean)eachProp.get("floor").equals("true")) {
 											BoundingSprite floorSprite = new BoundingSprite(true, imagePath);
 											BoundingObject floor = new BoundingObject(gameScreen, (int)tileXPos, (int)tileYPos, floorSprite);
 									        Physics.addFloorEntity(floor);
 										}
 									}
 									else {
 										if ((boolean)eachProp.get("wall").equals("true")) {
 											BoundingSprite floorSprite = new BoundingSprite(false, imagePath);
 											BoundingObject floor = new BoundingObject(gameScreen, (int)tileXPos, (int)tileYPos, floorSprite);
 									        Physics.addFloorEntity(floor);
 										}
 									}
 								}
 							} // end if
 						} // end for
 					} // end for
 				} // end if
 			} // end for
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ParseException e) {
 			e.printStackTrace();
 		} // end try
 	}
 
 	public float getLevelWidthPixels() {
 		return levelWidthPixels;
 	}
 
 	public void setLevelWidthPixels(float levelWidthPixels) {
 		this.levelWidthPixels = levelWidthPixels;
 	}
 
 	public int getxDistanceFromLeftWall() {
 		return xDistanceFromLeftWall;
 	}
 
 	public void setxDistanceFromLeftWall(int xDistanceFromLeftWall) {
 		this.xDistanceFromLeftWall = xDistanceFromLeftWall;
 	}
 
 	public int getLevelWidth() {
 		return levelWidth;
 	}
 
 	public void setLevelWidth(int levelWidth) {
 		this.levelWidth = levelWidth;
 	}
 
 }

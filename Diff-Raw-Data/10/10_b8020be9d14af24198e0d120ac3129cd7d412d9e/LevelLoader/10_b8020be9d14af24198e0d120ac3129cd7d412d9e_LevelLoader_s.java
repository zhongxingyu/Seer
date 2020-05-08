 package com.detourgames.raw;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Vector;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.JsonReader;
 import com.badlogic.gdx.utils.JsonWriter.OutputType;
 import com.badlogic.gdx.utils.OrderedMap;
 
 public class LevelLoader {
 
 	/*
 	 * A class for accessing level files, parsing them, loading them, and
 	 * returning the contents of a level to the GameManager.
 	 * 
 	 * It should only need a few methods: createLevel(levelnumber), //should
 	 * possibly be called in the constructor getLevel(), and getLevelItem()
 	 * (like getTiles() or getEnemies()).
 	 */
 
 	Level mLevel;
 	SpriteFactory mSpriteFactory;
 	SpriteSheet mSpriteSheet;
 
 	int levelWidth = 0;
 	int levelHeight = 0;
 
 	public LevelLoader(Level level) {
 		mLevel = level;
 		Texture texture = new Texture(Gdx.files.internal("sprite_tiles.png"));
 		mSpriteSheet = new SpriteSheet(texture, 3, new int[]{3,1,5}, new int[]{8,8,4,0,7,16,16,16,16}, new int[]{128,1024,64}, new int[]{128,320,64});
 		mLevel.setSpriteSheet(mSpriteSheet);
 		mSpriteFactory = new SpriteFactory(mLevel);
 		// if(lvln==0){
 		// createRandomTileMap(); TODO
 		// }else{
 		// createLevelFromFile(lvln, level);
 		// mSpriteFactory.createHero(2, 3);
 		// }
 
 		// sprites += HUD.HUD_SPRITES;
 	}
 
 	public void createLevelFromFile(int ln) {
 		FileHandle jsonId = getFileHandle(ln);
 		JsonReader reader=new JsonReader();
 		String jsonString=jsonId.readString();
 		Object o=reader.parse(jsonString);
 		CreateLevelFromJsonObject((OrderedMap<String, Object>)o);
 
 
 	}
 
 	private FileHandle getFileHandle(int ln) {
 		FileHandle levelID = null;
 
 		if (ln == 1) {
 			levelID = Gdx.files.internal("levels/RAWtest2.json");
 		}
 
 		return levelID;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void CreateLevelFromJsonObject(OrderedMap<String,Object> map){
 		
 		levelWidth = ((Float)map.get("width")).intValue();
 		levelHeight = ((Float)map.get("height")).intValue();
 		
 		Array<?> tileSetO=(Array<?>)map.get("tilesets");
 		OrderedMap<String, ?> tileSetProperties=(OrderedMap<String, ?>)tileSetO.items[0];
 		OrderedMap<String, ?> tileProperties=(OrderedMap<String, ?>)tileSetProperties.get("tileproperties");
 		
 		Array<?> layers = (Array<?>) map.get("layers");
 		OrderedMap<String, ?> foregroundLayer = null;
 		OrderedMap<String, ?> terrainLayer = null;
 		OrderedMap<String, ?> actorsObjectsLayer = null;
 		OrderedMap<String, ?> eventsLayer = null;
 		OrderedMap<String, ?> background1Layer = null;
 		OrderedMap<String, ?> background2Layer = null;
 		OrderedMap<String, ?> background3Layer = null;
 		Array<?> foregroundData = null;
 		Array<?> terrainData = null;
		Array<?> actorsObjectsObjects = null;
		Array<?> eventsData = null;
 		Array<?> background1Data = null;
 		Array<?> background2Data = null;
 		Array<?> background3Data = null;
 		
 		OrderedMap<String, ?> tileLayer;
 		String name;
 		for(int i=0;i<layers.size;i++){
 			tileLayer = (OrderedMap<String, ?>)layers.items[i];
 			name = (String)tileLayer.get("name");
 			if(name.equalsIgnoreCase("Foreground")){
 				foregroundLayer = (OrderedMap<String, ?>)layers.items[i];
 				foregroundData = (Array<?>)foregroundLayer.get("data");
 			}else if(name.equalsIgnoreCase("Terrain")){
 				terrainLayer = (OrderedMap<String, ?>)layers.items[i];
 				terrainData = (Array<?>)terrainLayer.get("data");
 			}else if(name.equalsIgnoreCase("Actors and Objects")){
 				actorsObjectsLayer = (OrderedMap<String, ?>)layers.items[i];
				actorsObjectsObjects = (Array<?>)actorsObjectsLayer.get("objects");
 			}else if(name.equalsIgnoreCase("Events")){
 				eventsLayer = (OrderedMap<String, ?>)layers.items[i];
				eventsData = (Array<?>)eventsLayer.get("data");
 			}else if(name.equalsIgnoreCase("Background")){
 				background1Layer = (OrderedMap<String, ?>)layers.items[i];
 				background1Data = (Array<?>)background1Layer.get("data");
 			}else if(name.equalsIgnoreCase("Background2")){
 				background2Layer = (OrderedMap<String, ?>)layers.items[i];
 				background2Data = (Array<?>)background2Layer.get("data");
 			}else if(name.equalsIgnoreCase("Background3")){
 				background3Layer = (OrderedMap<String, ?>)layers.items[i];
 				background3Data = (Array<?>)background3Layer.get("data");
 			}
 		}
 		
 		int[] tiles = new int[terrainData.size];
 		for(int i=0;i<tiles.length;i++){
 			tiles[i] = ((Float)terrainData.get(i)).intValue();		
 		}
 		
 		int i = 0;
 		for(int y=levelHeight-1;y>-1;y--){
 			for(int x=0;x<levelWidth;x++){
 				if(tiles[i]!=0){
 					int tileNum = tiles[i]-1;//-1 because of apparent flaw in Tiled?
 					if(!tileProperties.containsKey(""+tileNum))
 					{
 						i++;
 						continue;
 					}
 					OrderedMap<String, ?> tileTypeOM = (OrderedMap<String, ?>)tileProperties.get(""+tileNum);
 					int tileType = Integer.parseInt((String)tileTypeOM.get("TileType"));
 					mSpriteFactory.createTile((float)x/2f, (float)y/2f, 0, tileType);
 				}
 				i++;
 			}
 		}
 		
 		//TODO parse other layers
 		
 		mSpriteFactory.createHero(2, 2);
 	}
 }

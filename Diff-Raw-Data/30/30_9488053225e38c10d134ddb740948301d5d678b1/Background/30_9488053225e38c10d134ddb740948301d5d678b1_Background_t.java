 package com.subfty.krkjam2013.game;
 
 import java.util.Random;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.GdxRuntimeException;
 import com.badlogic.gdx.utils.Json;
 import com.badlogic.gdx.utils.SerializationException;
 import com.subfty.krkjam2013.Krakjam;
 import com.subfty.krkjam2013.game.actor.buildings.Building;
 import com.subfty.krkjam2013.util.Art;
 
 public class Background extends Group{
 	
 	private class SpecialTile{
 		public final int x;
 		public final int y;
 		public final float width;
 		public final float height;
 		public String sprite;
 		public int id;
 		
 		public SpecialTile(int x, int y, String sprite, int id, float width, float height){
 			this.x = x;
 			this.y = y;
 			this.sprite = sprite;
 			this.id = id;
 			this.width = width;
 			this.height = height;
 		}
 	}
 	
 	private final String SAVE_FILE_PATH = "save";
 	private Array<SpecialTile> markers = new Array<SpecialTile>();
 	
 	public final static float TILE_SIZE = 50;
 	private final long SEED = -123412431;
 	private Random random;
 	
 	private Sprite bgSprites[][]= new Sprite[(int) (Krakjam.STAGE_W / TILE_SIZE + 3)]
 			  								[(int) (Krakjam.STAGE_H / TILE_SIZE + 3)]; 
 	
 	private TextureRegion regions[];
 	
 	private Array<Building> buildings = new Array<Building>();
 	private GameScreen gs;
 	
     //INIT
 	public Background(GameScreen gs){
 		this.gs = gs;
 		random = new Random(this.SEED);
 		init();
 		
 		
 	}
 	public Background(long SEED){
 		random = new Random(SEED);
 		init();
 	}
 	private void init(){
 		//READING PREVIOUSLY SAVED DATA
 		Json json = new Json();
 		
 		try{
 			try{
 			FileHandle f = Gdx.files.local(SAVE_FILE_PATH);
 			markers = (Array<SpecialTile>)json.readValue(Array.class, 
 														 f.readString());
 			}catch(GdxRuntimeException gdr){
 				markers = null;
 			}
 		}catch(SerializationException sxc){
 			markers = null;
 		}
 		
 		if(markers == null)
 			markers = new Array<SpecialTile>();
 		
 		
 		for(int i=0; i<bgSprites.length; i++)
 			for(int j=0; j<bgSprites[i].length; j++){
 				bgSprites[i][j] = new Sprite();
 				bgSprites[i][j].setSize(TILE_SIZE*1.01f, TILE_SIZE*1.01f);
 			}
 		
 		regions = new TextureRegion[2];
 		regions[0] = Krakjam.art.atlases[Art.A_BACKGROUND].findRegion("rock",1);
 		regions[1] = Krakjam.art.atlases[Art.A_BACKGROUND].findRegion("rock",2);
 		
 		buildings.clear();
 		
 		act(0);
 	}
 	
 	public void saveData(){
 		Json json = new Json();
 		FileHandle f = Gdx.files.local(SAVE_FILE_PATH);
 		f.writeString(json.toJson(markers), false);
 	}
 	
 	public void act(float delta) {
 		for(int i=0; i<bgSprites.length; i++)
 			for(int j=0; j<bgSprites[i].length; j++){
 				random.setSeed(SEED + 
							   (int)(-gs.agents.x / TILE_SIZE) +
							   (int)(-gs.agents.y / TILE_SIZE)*bgSprites.length +
 							   i + j*bgSprites.length);
 				
 				bgSprites[i][j].setRegion(regions[0]);
				bgSprites[i][j].setX((i-1) * TILE_SIZE + gs.agents.x % TILE_SIZE);
				bgSprites[i][j].setY((j-1) * TILE_SIZE + gs.agents.y % TILE_SIZE);
 				
 				bgSprites[i][j].setColor(255.0f/255.0f - 60.0f*random.nextFloat()/255.0f, 
 										 45.0f/255.0f  + 50.0f*random.nextFloat()/255.0f, 
 										 23.0f/255.0f  + 50.0f*random.nextFloat()/255.0f, 1);
 			}
 	}
 	
 	public void draw(SpriteBatch batch, float parentAlpha) {
 		for(int i=0; i<bgSprites.length; i++)
 			for(int j=0; j<bgSprites[i].length; j++)
 				bgSprites[i][j].draw(batch, parentAlpha);
 	}
 	
 	
 	//OCCUPATION
 	public boolean isOccupied(int x, int y){
 		for(int i=0; i<buildings.size; i++)
 			if(bContains(buildings.get(i), x, y))
 				return true;
 		
 		return false;
 	}
 	private boolean bContains(Building b, int x, int y){
 		if(b.tileX <= x && b.tileX + b.tileWidth > x &&
 		   b.tileY <= y && b.tileY + b.tileHeight > y)
 			return true;
 		return false;
 	}
 	
 	public Array<Building> getBuildings() {
 		return buildings;
 	}
 	
 	public void registerBuilding(Building building){
 		buildings.add(building);
 	}
 	public void unregisterBuilding(Building building){
 		buildings.removeValue(building, true);
 	}
 }

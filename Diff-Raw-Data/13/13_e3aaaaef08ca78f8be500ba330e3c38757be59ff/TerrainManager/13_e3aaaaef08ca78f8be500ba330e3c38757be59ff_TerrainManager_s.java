 package com.blastedstudios.crittercaptors.ui.terrain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.badlogic.gdx.math.Vector3;
 import com.blastedstudios.crittercaptors.CritterCaptors;
 import com.blastedstudios.crittercaptors.util.OptionsUtil.OptionEnum;
 
 public class TerrainManager {
	public static float MIN_HEIGHT = 10f;
 	private static final List<ITerrain> terrains = new ArrayList<ITerrain>();
 	
 	/**
 	 * baseTerrain is the bottom layer of terrain to ensure the players never 
 	 * see less black void of terrain-less worlds
 	 */
 	private final Terrain baseTerrain;
 	private final CritterCaptors game;
 	private final int SCALE = 5, BASE_TERRAIN_STRIDE;
 	
 	public TerrainManager(CritterCaptors game){
 		this.game = game;
 		baseTerrain = new Terrain(
 				new float[(Terrain.DEFAULT_WIDTH + 1) * (Terrain.DEFAULT_WIDTH + 1)], 
 				new Vector3(0,MIN_HEIGHT,0), SCALE);
 		BASE_TERRAIN_STRIDE = Terrain.DEFAULT_WIDTH*SCALE;
 		if(game.getOptions().getOptionBoolean(OptionEnum.GetHeightmap))
 			add(new Vector3());
 	}
 	
 	public void add(Vector3 location){
 		boolean found = false;
 		for(ITerrain terrain : terrains)
 			if(terrain.getLocation().dst(location) < 10)
 				found = true;
 		if(!found)
 			terrains.add(new Terrain(game.getWorldLocationManager().getHeightmap(location), location, SCALE));
 	}
 
 	public void render(Vector3 playerLocation) {
 		int addX = ((int)playerLocation.x/BASE_TERRAIN_STRIDE)*BASE_TERRAIN_STRIDE,
 			addZ = ((int)playerLocation.z/BASE_TERRAIN_STRIDE)*BASE_TERRAIN_STRIDE;
 		for(int x=(int)(-1.5*BASE_TERRAIN_STRIDE); x<BASE_TERRAIN_STRIDE*2; x+=BASE_TERRAIN_STRIDE)
 			for(int z=(int)(-1.5*BASE_TERRAIN_STRIDE); z<BASE_TERRAIN_STRIDE*2; z+=BASE_TERRAIN_STRIDE){
 				baseTerrain.location.x = addX+x;
 				baseTerrain.location.z = addZ+z;
 				baseTerrain.render();
 			}
 		for(ITerrain terrain : terrains)
 			terrain.render();
 	}
 	
 	/**
 	 * @param x in game coordinates
 	 * @param z in game coordinates
 	 * @return altitude in meters
 	 */
 	public float getHeight(float x, float z){
 		for(ITerrain terrain : terrains)
 			if(terrain.getLocation().x + Terrain.DEFAULT_WIDTH_DIV2*terrain.getScale() >= x &&
 				terrain.getLocation().x - Terrain.DEFAULT_WIDTH_DIV2*terrain.getScale() <= x &&
 				terrain.getLocation().z + Terrain.DEFAULT_WIDTH_DIV2*terrain.getScale() >= z &&
 				terrain.getLocation().z - Terrain.DEFAULT_WIDTH_DIV2*terrain.getScale() <= z )
 				return terrain.getHeight(
 						x-terrain.getLocation().x+Terrain.DEFAULT_WIDTH_DIV2*terrain.getScale(), 
 						z-terrain.getLocation().z+Terrain.DEFAULT_WIDTH_DIV2*terrain.getScale());
 		return 0;
 	}
 	
 }

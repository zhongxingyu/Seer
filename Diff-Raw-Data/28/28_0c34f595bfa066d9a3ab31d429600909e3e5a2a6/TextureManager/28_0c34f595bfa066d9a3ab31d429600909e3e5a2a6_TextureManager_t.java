 package projectrts.io;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.asset.AssetManager;
 import com.jme3.texture.Texture;
 
 /**
  * Handles import of textures
  * 
  * @author Jakob Svensson
  * 
  */
public final class TextureManager {
 	private static Map<String, Texture> textures = new HashMap<String, Texture>();
 
 	private TextureManager() {
 	}; // Empty private constructor to disallow any instances.
 
 	/**
 	 * Initializes the textures
 	 * 
 	 * @param app
 	 *            The SimpleApplication
 	 */
 	public static void initializeTextures(SimpleApplication app) {
 		AssetManager assetManager = app.getAssetManager();
 
 		textures.put("Alpha",
 				assetManager.loadTexture("assets/terrain/alphamap.png"));
 
 		textures.put("Grass",
 				assetManager.loadTexture("Textures/Terrain/splat/grass.jpg"));
 
 		textures.put("Water",
 				assetManager.loadTexture("assets/terrain/Water_Texture.png"));
 
 		textures.put("Rock",
 				assetManager.loadTexture("Textures/Terrain/splat/road.jpg"));
 
 		textures.put("HeightMap",
 				assetManager.loadTexture("assets/terrain/mountains512.png"));
 
 		textures.put("Headquarter",
 				assetManager.loadTexture("assets/sprites/Headquarter.png"));
 
 		textures.put("Barracks",
 				assetManager.loadTexture("assets/sprites/SC_barracks.png"));
 
 		textures.put("Wall",
 				assetManager.loadTexture("assets/sprites/Wall.png"));
 
 		textures.put("GoldMine",
 				assetManager.loadTexture("assets/sprites/WC2_goldmine.png"));
 
 		textures.put("SCV",
 				assetManager.loadTexture("assets/sprites/SC_scv.png"));
 
 		textures.put("Drone_blue",
 				assetManager.loadTexture("assets/sprites/SC_drone_blue.png"));
 		
 		textures.put("Drone_red",
 				assetManager.loadTexture("assets/sprites/SC_drone_red.png"));
 
 		textures.put("Marine_blue",
 				assetManager.loadTexture("assets/sprites/SC_marine_blue.png"));
 		
 		textures.put("Marine_red",
 				assetManager.loadTexture("assets/sprites/SC_marine_red.png"));
 
 		textures.put("SelectSquare",
 				assetManager.loadTexture("assets/sprites/select_square.png"));
 		// TODO Plankton: Fix barracks GUI picture!!!
 	}
 
 	/**
 	 * Returns a texture with a specified name
 	 * 
 	 * @param name
 	 *            The name of the texture
 	 * @return The texture with the name
 	 */
 	public static Texture getTexture(String name) {
 		return textures.get(name);
 	}
 }

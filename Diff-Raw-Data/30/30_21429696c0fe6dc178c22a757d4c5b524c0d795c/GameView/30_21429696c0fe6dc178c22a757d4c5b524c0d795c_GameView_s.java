 package projectrts.view;
 
 import java.util.List;
//derp
 import projectrts.global.constants.Constants;
 import projectrts.global.utils.MaterialManager;
 import projectrts.global.utils.TextureManager;
 import projectrts.model.core.IGame;
 import projectrts.model.core.entities.IEntity;
import projectrts.view.controls.AbstractControl;
 import projectrts.view.spatials.AbstractSpatial;
 import projectrts.view.spatials.SpatialFactory;

 import com.jme3.app.SimpleApplication;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Node;
 import com.jme3.scene.shape.Box;
 import com.jme3.terrain.geomipmap.TerrainLodControl;
 import com.jme3.terrain.geomipmap.TerrainQuad;
 import com.jme3.terrain.heightmap.AbstractHeightMap;
 import com.jme3.terrain.heightmap.ImageBasedHeightMap;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture.WrapMode;
 
 /**
  * The in-game view, creating and managing the scene.
  * @author Markus Ekstrm
  *
  */
 public class GameView{
 	private SimpleApplication app;
 	private IGame game;
     private Node entities = new Node("entities"); // The node for all entities
     private Node selected = new Node("selected"); // The node for the selected graphics
     private Node terrainNode = new Node("terrain"); // The node for all terrain
     private Material matTerrain;
     private TerrainQuad terrain;
     private float mod = Constants.INSTANCE.getModelToWorld(); // The modifier value for converting lengths between model and world.
 	
 	public GameView(SimpleApplication app, IGame game) {
 		this.app = app;
 		this.game = game;
 	}
 	
 	/**
 	 * Initializes the scene.
 	 * @param entitiesList A list containing the initial entities.
 	 * @param controlList The controls for the initial entities.
 	 */
 	public void initialize() {
 		initializeWorld();
 		initializeEntities();
 		this.app.getRootNode().attachChild(selected);
 	}
 	
 	/**
 	 * Based on Jmonkey terrain example code
 	 * http://jmonkeyengine.org/wiki/doku.php/jme3:beginner:hello_terrain
 	 */
     private void initializeWorld() {
     	
     	  /** 1. Create terrain material and load four textures into it. */
         matTerrain = MaterialManager.INSTANCE.getMaterial("Terrain");
      
         /** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
         matTerrain.setTexture("Alpha",TextureManager.INSTANCE.getTexture("Alpha"));
      
         /** 1.2) Add GRASS texture into the red layer (Tex1). */
         Texture grass = TextureManager.INSTANCE.getTexture("Grass");
         grass.setWrap(WrapMode.Repeat);
         matTerrain.setTexture("Tex1", grass);
         matTerrain.setFloat("Tex1Scale", 64f);
      
         /** 1.3) Add WATER texture into the green layer (Tex2) */
         Texture water = TextureManager.INSTANCE.getTexture("Water");
         water.setWrap(WrapMode.Repeat);
         matTerrain.setTexture("Tex2", water);
         matTerrain.setFloat("Tex2Scale", 32f);
      
         /** 1.4) Add ROAD texture into the blue layer (Tex3) */
         Texture rock = TextureManager.INSTANCE.getTexture("Rock");
         rock.setWrap(WrapMode.Repeat);
         matTerrain.setTexture("Tex3", rock);
         matTerrain.setFloat("Tex3Scale", 128f);
      
         /** 2. Create the height map */
         AbstractHeightMap heightmap = null;
         Texture heightMapImage = TextureManager.INSTANCE.getTexture("HeightMap");
         heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
         heightmap.load();
      
         /** 3. We have prepared material and heightmap. 
          * Now we create the actual terrain:
          * 3.1) Create a TerrainQuad and name it "my terrain".
          * 3.2) A good value for terrain tiles is 64x64 -- so we supply 64+1=65.
          * 3.3) We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
          * 3.4) As LOD step scale we supply Vector3f(1,1,1).
          * 3.5) We supply the prepared heightmap itself.
          */
         int patchSize = 65;
         terrain = new TerrainQuad("my terrain", patchSize, 513, heightmap.getHeightMap());
      
         /** 4. We give the terrain its material, position & scale it, and attach it. */
         terrain.setMaterial(matTerrain);
         terrain.setLocalTranslation(0, 0, 0);
         terrain.setLocalScale(.02f, .01f, .02f);
         terrainNode.attachChild(terrain);
         app.getRootNode().attachChild(terrainNode);
 
         terrainNode.setLocalTranslation(2, -2, -100);
         terrainNode.rotateUpTo(new Vector3f(0f,0f,1f));
 
      
         /** 5. The LOD (level of detail) depends on were the camera is: */
         TerrainLodControl control = new TerrainLodControl(terrain, app.getCamera());
         terrain.addControl(control);
         
         
     }
     
     private void initializeEntities() {
     	List<IEntity> entitiesList = game.getAllEntities();
     	Box[] entityShapes = new Box[entitiesList.size()];
     	Material entityMaterial = MaterialManager.INSTANCE.getMaterial("Unshaded");
     	entityMaterial.setColor("Color", ColorRGBA.Pink);
     	
     	float mod = Constants.INSTANCE.getModelToWorld();
     	
     	for(int i = 0; i < entitiesList.size(); i++) {
     		// Create shape.
     		// The location of the entity is initialized to (0, 0, 0) but is then instantly translated to the correct place by moveControl.
     		// Gets the size from the model and converts it to world size.
     		entityShapes[i] = new Box(new Vector3f(0, 0, 0),  
     									(entitiesList.get(i).getSize() * mod)/2, (entitiesList.get(i).getSize() * mod)/2, 0); 
     		// Create spatial.
     		AbstractSpatial entitySpatial = SpatialFactory.INSTANCE.createSpatial("UnitSpatial", "unit" + i, entityMaterial, entityShapes[i], entitiesList.get(i));
     		// Attach spatial to the entities node.
     		entities.attachChild(entitySpatial);
     	}
     	//Attach the entities node to the root node, connecting it to the world.
     	this.app.getRootNode().attachChild(entities);
     }
     
     public void update(float tpf) {
     }
     
     /**
      * Draws the selected graphics for all entities in the passed list.
      * @param selectedEntities A list of selected entities.
      * @param controlList A list of controls for the select-spatials, one for each spatial.
      */
     public void drawSelected(List<IEntity> selectedEntities) {
     	// Remove all previously selected graphics
     	selected.detachAllChildren();
     	// Create the material for the spatials.
     	Material circleMaterial = MaterialManager.INSTANCE.getMaterial("Unshaded");
     	circleMaterial.setColor("Color", ColorRGBA.Green);
     	
     	for(int i = 0; i < selectedEntities.size(); i++) {
 	    	// Sets the location of the spatial to (0, 0, -1) to make sure it's behind the entities that use (x, y, 0).
 	    	// The control will instantly translate it to the correct location.
 	    	Box circle = new Box(new Vector3f(0, 0, -1), 
 	    			(selectedEntities.get(i).getSize() + 0.3f)/2 * mod, (selectedEntities.get(i).getSize() + 0.3f)/2 * mod, 0);
 	    	AbstractSpatial circleSpatial = SpatialFactory.INSTANCE.createSpatial("SelectSpatial", selectedEntities.get(i).getName(), circleMaterial, circle, selectedEntities.get(i));	
 	    	// Attach spatial to the selected node, connecting it to the world.
 	    	selected.attachChild(circleSpatial);
     	}
     }
 }

 package projectrts.view;
 
 import projectrts.global.constants.*;
 import projectrts.model.core.IGame;
 import projectrts.model.core.ITile;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.asset.AssetManager;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.shape.Box;
 import com.jme3.terrain.geomipmap.TerrainLodControl;
 import com.jme3.terrain.geomipmap.TerrainQuad;
 import com.jme3.terrain.heightmap.AbstractHeightMap;
 import com.jme3.terrain.heightmap.ImageBasedHeightMap;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture.WrapMode;
 
 public class GameView {
 	private SimpleApplication app;
 	private Node units = new Node("units");
     private IGame model;
     private Node terrainNode = new Node("terrain");
     private Material matTerrain;
     private TerrainQuad terrain;
 
 	
 	public GameView(SimpleApplication app, IGame model) {
 		this.app = app;
 
 		this.model = model;
 	}
 	
 	public void initializeView() {
 		initializeWorld();
 		initializeEntities();
 
 	}
 	
 	/**
 	 * Based on Jmonkey terrain example code
 	 * http://jmonkeyengine.org/wiki/doku.php/jme3:beginner:hello_terrain
 	 */
     private void initializeWorld() {
     	AssetManager assetManager = app.getAssetManager();
     	  /** 1. Create terrain material and load four textures into it. */
         matTerrain = new Material(assetManager, 
                 "Common/MatDefs/Terrain/Terrain.j3md");
      
         /** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
         matTerrain.setTexture("Alpha", assetManager.loadTexture(
                 "Textures/Terrain/splat/alphamap.png"));
      
         /** 1.2) Add GRASS texture into the red layer (Tex1). */
         Texture grass = assetManager.loadTexture(
                 "Textures/Terrain/splat/grass.jpg");
         grass.setWrap(WrapMode.Repeat);
         matTerrain.setTexture("Tex1", grass);
         matTerrain.setFloat("Tex1Scale", 64f);
      
         /** 1.3) Add DIRT texture into the green layer (Tex2) */
         Texture dirt = assetManager.loadTexture(
                 "Textures/Terrain/splat/dirt.jpg");
         dirt.setWrap(WrapMode.Repeat);
         matTerrain.setTexture("Tex2", dirt);
         matTerrain.setFloat("Tex2Scale", 32f);
      
         /** 1.4) Add ROAD texture into the blue layer (Tex3) */
         Texture rock = assetManager.loadTexture(
                 "Textures/Terrain/splat/road.jpg");
         rock.setWrap(WrapMode.Repeat);
         matTerrain.setTexture("Tex3", rock);
         matTerrain.setFloat("Tex3Scale", 128f);
      
         /** 2. Create the height map */
         AbstractHeightMap heightmap = null;
         Texture heightMapImage = assetManager.loadTexture(
                 "assets/terrain/mountains512.png");
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
 
         terrainNode.rotateUpTo(new Vector3f(0f,0f,1f));
 
      
         /** 5. The LOD (level of detail) depends on were the camera is: */
         TerrainLodControl control = new TerrainLodControl(terrain, app.getCamera());
         terrain.addControl(control);
         
     	
         
     }
     
     private void initializeEntities() {
     	this.app.getRootNode().attachChild(units);
     	
     	
     }
 }

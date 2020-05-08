 package spatial;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.material.Material;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.shape.Box;
 import com.jme3.scene.shape.Quad;
 import com.jme3.texture.Texture;
 import variables.P;
 
 /**
  * A class for a chunk of background wall.
  * @author jonatankilhamn
  */
 public class Wall extends Node {
     
     private static Node modelNodeForWall;
     
     /**
      * This constructor creates a <code>Wall</code> represented by a 
      * <code>Geometry</code> loaded internaly. The dimensions of the 
      * <code>Wall</code> is currently loaded from the <code>P</code>-class 
      * containing some global variables. 
      * @param assetManager is used to load the geometry and 
      * texture of the <code>Wall</code>.
      */
     public Wall(AssetManager assetManager){
         super("Wall");
         
         if (modelNodeForWall == null) {
             initModel(assetManager);
         }
  
         this.setShadowMode(ShadowMode.Receive);
         
         this.attachChild(modelNodeForWall.clone());
     }
     
     private static final float WALL_HEIGHT = 120.0f;
     
     private static void initModel(AssetManager assetManager) {
         modelNodeForWall = new Node();
         //Box model = new Box(new Vector3f(P.chunkLength/2,0,-P.platformWidth/2-P.playerZOffset), P.chunkLength/2, 60, 0);
         Quad model1 = new Quad(P.chunkLength,WALL_HEIGHT/2);
         Quad model2 = new Quad(P.chunkLength,WALL_HEIGHT/2);
         //Loads the texture and repeats it over the chunk in its correct size 
         //(so that each brick will not be bigger in pixels, or stretched, if 
         //the resolution is higher is greater than 640x480)
         Texture texture = assetManager.loadTexture("Textures/tegel.png");
         texture.setWrap(Texture.WrapMode.Repeat);
 
        model1.scaleTextureCoordinates(new Vector2f(Math.round(10f*800/640), Math.round(10f*480/480)));
        model2.scaleTextureCoordinates(new Vector2f(Math.round(10f*800/640), Math.round(10f*480/480)));
         
         Geometry geometry1 = new Geometry("",model1);
         Geometry geometry2 = new Geometry("",model2);
         geometry1.setLocalTranslation(new Vector3f(0,0,-P.platformWidth/2-P.playerZOffset));
         geometry2.setLocalTranslation(new Vector3f(0,-WALL_HEIGHT/2,-P.platformWidth/2-P.playerZOffset));
         
         Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         material.setTexture("DiffuseMap", texture);
         geometry1.setMaterial(material);
         geometry2.setMaterial(material);
 
         modelNodeForWall.attachChild(geometry1);        
         modelNodeForWall.attachChild(geometry2);
     }
 }

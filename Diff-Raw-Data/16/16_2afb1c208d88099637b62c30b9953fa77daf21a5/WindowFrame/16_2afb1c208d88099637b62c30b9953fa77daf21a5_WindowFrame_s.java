 package spatial;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.material.Material;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.shape.Box;
 import variables.P;
 
 /**
  * A class for a non-physical window frame, purely for decoration.
  * @author jonatankilhamn
  */
 public class WindowFrame extends Node {
 
     /**
      * This constructor creates a
      * <code>WindowFrame</code> represented by a
      * <code>Geometry</code> loaded internaly.
      *
      * @param assetManager is used to load the geometry and texture of
      * the <code>Window</code>.
      */
     public WindowFrame(AssetManager assetManager, Vector3f position) {
         super("WindowFrame");
         
        Node window = (Node)assetManager.loadModel("Models/window/window.j3o");
        window.scale(4);
         window.rotate(90*FastMath.DEG_TO_RAD, 0f, 0f);
         
         this.attachChild(window);
         
        this.setLocalTranslation(position.x, position.y, -P.platformWidth/2-P.playerZOffset-5.7f);
         this.setShadowMode(ShadowMode.Off);   
 
     }
 }

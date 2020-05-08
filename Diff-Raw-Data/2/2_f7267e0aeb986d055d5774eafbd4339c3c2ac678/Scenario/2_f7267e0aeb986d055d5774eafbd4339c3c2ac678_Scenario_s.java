 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mygame.States.Scenario;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.asset.AssetManager;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.shapes.CollisionShape;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.bullet.util.CollisionShapeFactory;
 import com.jme3.light.AmbientLight;
 import com.jme3.light.DirectionalLight;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.ViewPort;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 
 /**
  *
  * @author Harpo
  */
 public class Scenario {
 
     private final Node rootNode;
     private final ViewPort viewPort;
     private final AssetManager assetManager;
     private final ColorRGBA backgroundColor = ColorRGBA.Blue;
     private Spatial sceneModel;
     private RigidBodyControl landscape;
     private SimpleApplication app;
     
     public Scenario(SimpleApplication app) {
         this.rootNode = app.getRootNode();
         this.viewPort = app.getViewPort();
         this.assetManager = app.getAssetManager();
         this.app = app;
         //Ponemos el fondo en color azul
         viewPort.setBackgroundColor(backgroundColor);
 
         //Cargamos el escenario
         sceneModel = assetManager.loadModel("Scenes/montextura.j3o");
         sceneModel.setLocalScale(2f);
         
         // We set up collision detection for the scene by creating a
         // compound collision shape and a static RigidBodyControl with mass zero.
         
         CollisionShape sceneShape =
                 CollisionShapeFactory.createMeshShape((Node) sceneModel);
 
         landscape = new RigidBodyControl(sceneShape, 0);
         sceneModel.addControl(landscape);
         sceneModel.setName("Escenario");  
        rootNode.attachChild(sceneModel);
         this.app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().add(landscape);
         
         setUpLight();
     }
     
     public Spatial getEscenari(){
         return this.sceneModel;
     }
     
     private void setUpLight() {
         // We add light so we see the scene
         AmbientLight al = new AmbientLight();
         al.setColor(ColorRGBA.White.mult(1.3f));
         rootNode.addLight(al);
         
         DirectionalLight dl = new DirectionalLight();
         dl.setColor(ColorRGBA.White);
         dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
         rootNode.addLight(dl);
     }
 }

 package mygame;
 
 import chair.input.*;
 import com.jme3.asset.AssetManager;
 import com.jme3.input.InputManager;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 import java.util.LinkedList;
 
 /**
  *
  * @author Charlie
  */
 public class Level {
     
     Node rootNode;
     AssetManager assetManager;
     InputManager inputManager;
     LinkedList<MoveableGameObject> moveableObjects;
     LinkedList<StaticGameObject> staticObjects;
  
     //Remove these from PhysicsSpace at the beginning of an update.
     LinkedList<GameObject> killUs;
     
     public Level(Node root, AssetManager assets, InputManager input){
         //We REALLY should implement a singleton...
         rootNode = root;
         assetManager = assets;
         inputManager = input;
         killUs = new LinkedList<GameObject>();
         moveableObjects = new LinkedList<MoveableGameObject>();
         staticObjects = new LinkedList<StaticGameObject>();
         
         InputListener controllerListener = new XboxInputListener(input);
        float temp = 0;
         for(InputController controller : controllerListener.getInputControllers())
         {
            temp += 3;
             Spatial chairSpatial = assetManager.loadModel("Models/Angry Chair/Angry Chair.j3o");
             rootNode.attachChild(chairSpatial);
            OfficeChair chair = new OfficeChair(this, new Vector2f(temp, 0.0f), 0.0f, controller, chairSpatial);
             this.moveableObjects.add(chair);
         }
         
         Vector3f min = new Vector3f(-21.0f, 0.0f, -21.0f);
         Vector3f max = new Vector3f(-20.0f, 3.0f, 21.0f);
         Box b = new Box(min, max);
         Geometry g = new Geometry("Box Left", b);
         Material mat = new Material(assetManager, 
                 "Common/MatDefs/Misc/Unshaded.j3md");
         mat.setColor("Color", ColorRGBA.White);
         g.setMaterial(mat);
         Wall wall = new Wall(g, -21.0f, -21.0f, 1.0f, 42.0f);
         this.staticObjects.add(wall);
         rootNode.attachChild(g);
         min = new Vector3f(-20.0f, 0.0f, 20.0f);
         max = new Vector3f(20.0f, 3.0f, 21.0f);
         b = new Box(min, max);
         g = (new Geometry("Box Top", b));
         g.setMaterial(mat);
         wall = new Wall(g, -20.0f, 20.0f, 40.0f, 1.0f);
         this.staticObjects.add(wall);
         rootNode.attachChild(g);
         min = new Vector3f(20.0f, 0.0f, -21.0f);
         max = new Vector3f(21.0f, 3.0f, 21.0f);
         b = new Box(min, max);
         g = (new Geometry("Box Right", b));
         g.setMaterial(mat);
         wall = new Wall(g, 20.0f, -21.0f, 1.0f, 42.0f);
         this.staticObjects.add(wall);
         rootNode.attachChild(g);
         min = new Vector3f(-20.0f, 0.0f, -21.0f);
         max = new Vector3f(20.0f, 3.0f, -20.0f);
         b = new Box(min, max);
         g = (new Geometry("Box Bottom", b));
         g.setMaterial(mat);
         wall = new Wall(g, -20.0f, -21.0f, 40.0f, 1.0f);
         this.staticObjects.add(wall);
         rootNode.attachChild(g);
 
         
     }
     
     
     /**
      *
      * @param shot
      */
     public void spawnProjectile(Projectile shot){
         //This will do stuff.
     }
     
     /**
      *
      * @param requestor
      */
     public void removeSelf(GameObject requestor){
         killUs.add(requestor);
     }
     
     
     /**
      *
      * @param tpf
      */
     public void update(float tpf){
         for(GameObject g : killUs){
             switch (g.type){
                 case ACTOR: 
                     rootNode.detachChild(g.objectModel);
                     moveableObjects.remove((MoveableGameObject) g);
                     break;
                 case PROJECTILE:
                     moveableObjects.remove((MoveableGameObject) g);
                     break;
                 case OBSTACLE:
                     break;
                 default:
             }
             killUs.remove(g);
         }
         for(MoveableGameObject g: moveableObjects){
             g.update(tpf);
 
             for (MoveableGameObject g2: moveableObjects){
                 if (g != g2){
                     g.boundingCircle.collidesWithCircle(g2.boundingCircle);
                 }
             }
             for (StaticGameObject g2: staticObjects){
                g.boundingCircle.collidesWithRect(g2.boundingRect);
             }
 /*
             OfficeChair a = (OfficeChair)allObjects.get(0);
             OfficeChair b = (OfficeChair)allObjects.get(1);
             if (a.getBoundingCircle().collidesWithCircle(b.getBoundingCircle(), true))
                 System.out.println("Fuck yeah");
 */
         }
     }
     
 }

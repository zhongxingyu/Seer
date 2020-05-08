 package mygame;
 
 import chair.input.*;
 import com.jme3.asset.AssetManager;
 import com.jme3.input.InputManager;
 import com.jme3.math.Vector2f;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import java.util.LinkedList;
 
 /**
  *
  * @author Charlie
  */
 public class Level {
     
     Node rootNode;
     AssetManager assetManager;
     InputManager inputManager;
     LinkedList<GameObject> allObjects;
  
     //Remove these from PhysicsSpace at the beginning of an update.
     LinkedList<GameObject> killUs;
     
     public Level(Node root, AssetManager assets, InputManager input){
         //We REALLY should implement a singleton...
         rootNode = root;
         assetManager = assets;
         inputManager = input;
         killUs = new LinkedList<GameObject>();
         allObjects = new LinkedList<GameObject>();
         
         InputListener controllerListener = new XboxInputListener(input);
        float temp = 0;
         for(InputController controller : controllerListener.getInputControllers())
         {
            temp += 10;
             Spatial chairSpatial = assetManager.loadModel("Models/Angry Chair/Angry Chair.j3o");
             rootNode.attachChild(chairSpatial);
            OfficeChair chair = new OfficeChair(this, new Vector2f(temp, 0.0f), 0.0f, controller, chairSpatial);
             this.allObjects.add(chair);
         }
         
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
                     break;
                 case PROJECTILE:
                     break;
                 case OBSTACLE:
                     break;
                 default:
             }
             allObjects.remove(g);
             killUs.remove(g);
         }
         for(GameObject g: allObjects){
             g.update(tpf);
             OfficeChair a = (OfficeChair)allObjects.get(0);
             OfficeChair b = (OfficeChair)allObjects.get(1);
            if (a.getBoundingCircle().collidesWithCircle(b.getBoundingCircle(), true))
                 System.out.println("Fuck yeah");
         }
     }
     
 }

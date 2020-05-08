 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package character;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.shapes.BoxCollisionShape;
 import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
 import com.jme3.bullet.control.CharacterControl;
 import com.jme3.bullet.control.GhostControl;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Node;
 import mygame.Game;
 
 /**
  *
  * @author Lena
  */
 public class CharacterFactory {
 
     public static Player createPlayer(String type, AssetManager assetManager) {
         //TODO attack
         Player player = null;
         if (type.equals("Dragon")) {
             player = new Player(type, 3, 0);
             player.adjustHealth(3);
         }
         
         player.model = (Node) assetManager.loadModel("Models/Level/Heaven/cloud_small_1v2.j3o");
         player.model.setName("Player");
         player.model.scale(0.3f);
        player.model.setLocalTranslation(0.f, 15.f, 0);
         
        BoxCollisionShape collisionShape = new BoxCollisionShape(new Vector3f(1f, 1f, 0.5f));
         GhostControl control = new GhostControl(collisionShape);
         player.control = control;
         player.model.addControl(control);
         
         
         
         //load the model and the rigid body animation
         // Node character = (Node) assetManager.loadModel("Models/" + type + ".mesh.xml");
         /*Node model = (Node) assetManager.loadModel("Models/Oto/Oto.mesh.xml");
         CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
         CharacterControl control = new CharacterControl(capsuleShape, 0.01f);
         // control.setPhysicsSpace(appState.getPhysicsSpace());
 
         player.model = model;
         player.model.addControl(control);
         
       //  player.model.setLocalTranslation(0, 10, 0);
         control.setPhysicsLocation(new Vector3f(0, 10, 0));
 
 
 
         player.character = control;
         player.standAnimation = "stand";
         player.walkAnimation = "Walk";
         //set the animations
         player.animation = player.model.getControl(AnimControl.class);
         player.channel = player.animation.createChannel();
         //  for(int i = 0; i < player.animation.getSkeleton().getBoneCount(); i++){
         //      player.channel.addBone(player.animation.getSkeleton().getBone(i));
         //}*/
         return player;
     }
 
     public static Opponent createOpponent(String name, AssetManager assetManager, Vector3f location, BulletAppState bulletAppState) {
         //TODO: attack
         Opponent op = null;
         op = new Opponent(name, 1);
 
         Node model = (Node) assetManager.loadModel("Models/Character/pixyJoinedclothes.j3o");
         model.scale(1.f);
         model.setName(name);
         op.model = model;
         
         BoxCollisionShape collisionShape = new BoxCollisionShape(new Vector3f(1, 1, 1));
         RigidBodyControl control = new RigidBodyControl(collisionShape, 0);
         model.addControl(control);
         bulletAppState.getPhysicsSpace().add(control);
         control.setPhysicsLocation(new Vector3f(10,5,0));
 
         
         //op.character = control;
         //op.standAnimation = "stand";
         //op.walkAnimation = "Walk";
         //set the animations
         /*op.animation = op.model.getControl(AnimControl.class);
         op.channel = op.animation.createChannel();
         for (int i = 0; i < op.animation.getSkeleton().getBoneCount(); i++) {
             op.channel.addBone(op.animation.getSkeleton().getBone(i));
         }*/
         return op;
     }
 }

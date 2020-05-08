 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package mygame.model.zombie;
 
 import com.jme3.animation.AnimChannel;
 import com.jme3.animation.AnimControl;
 import com.jme3.animation.AnimEventListener;
 import com.jme3.animation.LoopMode;
 import com.jme3.app.SimpleApplication;
 import com.jme3.audio.AudioNode;
 import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
 import com.jme3.bullet.control.CharacterControl;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Node;
 
 /**
  *
  * @author Floyd
  */
 public class Zombie implements AnimEventListener {
 
     private SimpleApplication app;
     private CharacterControl zombieControl;
     private Node zombieShape;
     private AudioNode audio_zombie;
     private float speed;
     private AnimChannel channel;
     private AnimControl control;
     private final int distFollow = 50;
     private final int angleFollow = 160;
 
     Zombie(SimpleApplication app, Vector3f position, Vector3f viewDirection, float speed) {
         this.app = app;
         CapsuleCollisionShape capsule = new CapsuleCollisionShape(3f, 4f);
         zombieControl = new CharacterControl(capsule, 0.01f);
         //Afegit el nou model
        zombieShape = (Node) app.getAssetManager().loadModel("Models/zombie/zombie.j3o");
 
         zombieShape.addControl(zombieControl);
         zombieControl.setPhysicsLocation(position);
         zombieControl.setViewDirection(viewDirection);
         this.speed = speed;
         initAudio(); // initializes audio
         initAnimation();
     }
 
     CharacterControl getControl() {
         return zombieControl;
     }
 
     Node getNode() {
         return zombieShape;
     }
 
     private void initAudio() {
         audio_zombie = new AudioNode(app.getAssetManager(), "Sounds/Effects/Zombies1.wav", false);
         audio_zombie.setLooping(true);
         app.getRootNode().attachChild(audio_zombie);
 
     }
 
     private void initAnimation() {
 
         control = zombieShape.getControl(AnimControl.class);
         channel = control.createChannel();
         channel.setAnim("stand");
     }
 
     public void update(Vector3f playerPos) {
         Vector3f viewDirection = new Vector3f();
         Vector3f walkDirection = new Vector3f();
 
         Vector3f zombiePos = zombieControl.getPhysicsLocation();
 
         float dist = playerPos.distance(zombiePos);
         float angle = zombieControl.getViewDirection().normalize().angleBetween(playerPos.subtract(zombiePos).normalize());
 
         //System.out.print(zombieControl.getViewDirection()+" ");
 
         if (dist < distFollow && angle < (angleFollow * Math.PI / 360)) {
             audio_zombie.setVolume(1 / dist);
             audio_zombie.play();
 
             walkDirection.set(new Vector3f((playerPos.x - zombiePos.x) * speed, 0, (playerPos.z - zombiePos.z) * speed));
             viewDirection.set(new Vector3f((playerPos.x - zombiePos.x) * speed, 0, (playerPos.z - zombiePos.z) * speed));
 
             zombieControl.setWalkDirection(walkDirection);
             zombieControl.setViewDirection(viewDirection);
 
             //Animation
             if (!channel.getAnimationName().equals("Walk")) {
                 channel.setAnim("Walk", 0.50f);
                 channel.setLoopMode(LoopMode.Loop);
             }
         } else {
             audio_zombie.stop();
             zombieControl.setWalkDirection(new Vector3f(0, 0, 0));
             channel.setAnim("stand");
         }
     }
 
     public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
         if (animName.equals("Walk")) {
             channel.setAnim("stand", 0.50f);
             channel.setLoopMode(LoopMode.DontLoop);
             channel.setSpeed(1f);
         }
     }
 
     public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
         // unused
     }
 }

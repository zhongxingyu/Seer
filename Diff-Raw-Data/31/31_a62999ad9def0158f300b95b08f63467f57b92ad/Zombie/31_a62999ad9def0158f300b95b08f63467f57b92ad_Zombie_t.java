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
 import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
 import com.jme3.bullet.control.CharacterControl;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Node;
 import com.jme3.audio.AudioNode;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.shapes.BoxCollisionShape;
 import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
 import com.jme3.bullet.control.GhostControl;
 import mygame.sound.SoundManager;
 
 /**
  *
  * @author Floyd
  */
 public class Zombie implements AnimEventListener, ZombieInterface {
 
     private SimpleApplication app;
     private CharacterControl zombieControl;
     private Node zombieShape;
     private float speed;
     private float hitpoints;
     private float damage;
     private float cooldown;
     private AnimChannel channel;
     private AnimControl control;
     private final int distFollow = 50;
     private final int angleFollow = 160;
     private final int distAttack = 7;
     private int state=0;
     
     //MODIFICACION PARA EL GRUPO DE LOS ZOMBIES
     private boolean paused  ;
     
     private RigidBodyControl colisions;
     private Node node1;
     
     private CompoundCollisionShape ccs;
     
     Zombie(SimpleApplication app, Vector3f position, Vector3f viewDirection, float speed) {
         this.app = app;
         CapsuleCollisionShape cilinder = new CapsuleCollisionShape(1.5f,2f, 1);
         zombieControl = new CharacterControl(cilinder, 0.1f);
         zombieShape = (Node) app.getAssetManager().loadModel("Models/zombie/zombie.mesh.j3o");
         node1=new Node();
         node1.attachChild(zombieShape);
         zombieShape.move(0f, -2.5f, 0f);
         node1.addControl(zombieControl);
         
         zombieShape.scale(3f);
         colisions=new RigidBodyControl(1f);
         node1.addControl(colisions);
         ccs=new CompoundCollisionShape();
         ccs.addChildShape(cilinder, new Vector3f(0f,4.2f,0f));
         colisions.setCollisionShape(cilinder);
         colisions.setAngularDamping(0);
         colisions.setFriction(0);
         colisions.setKinematic(true);
         //MODIFICACION PARA EL GRUPO DE LOS ZOMBIES
         zombieShape.setName("Zombie");
         zombieControl.setPhysicsLocation(position);
         
         
         this.speed = speed;
         this.hitpoints = 100;
         this.damage =20;
         initAnimation();
     }
 
     public CharacterControl getControl() {
         return zombieControl;
     }
     public RigidBodyControl getColision(){
         return colisions;
     }
 
     Node getNode() {
         return node1;
     }
 
 
     private void initAnimation() {
 
         control = zombieShape.getControl(AnimControl.class);
         control.addListener(this);
         channel = control.createChannel();
         channel.setAnim("stand");
         channel.setSpeed(1f);
         channel.setLoopMode(LoopMode.Loop);
         
         //channel.setAnim("stand"); de moment no te animacio stand
     }
 
     public void update(Vector3f playerPos) {
         Vector3f viewDirection = new Vector3f();
         Vector3f walkDirection = new Vector3f();
 
         Vector3f zombiePos = zombieControl.getPhysicsLocation();
 
         float dist = playerPos.distance(zombiePos);
         float angle = zombieControl.getViewDirection().normalize().angleBetween(playerPos.subtract(zombiePos).normalize());
 
        
         if(state==3){
             //dead
         }
        // @David C. -- Añadido condición del parámetro pause
         else if(dist < distAttack && angle < (angleFollow * Math.PI / 360) && !paused ){
             if(state!=2){
                 channel.setAnim("attack", 0.50f);
                 channel.setLoopMode(LoopMode.DontLoop);
                 System.out.println("attack");
             }
             state=2;//attack
             zombieControl.setWalkDirection(new Vector3f(0, 0, 0));
             channel.setSpeed(1f);
         }
         else if (dist < distFollow && angle < (angleFollow * Math.PI / 360) && !paused  && state!=2) {
             state=1;//move
             /* @Isa 
              * Si el juego NO esta mutado o pausado ejecutar la siguiente linea
             SoundManager.zombieSoundSetVolume(app.getRootNode(), 1 / dist);          
              */
             
             
             SoundManager.zombieSoundPlay(app.getRootNode()); // Reproduce el sonido de los zombies
             
             walkDirection.set(new Vector3f((playerPos.x - zombiePos.x) * speed, 0, (playerPos.z - zombiePos.z) * speed));
             viewDirection.set(new Vector3f((playerPos.x - zombiePos.x) * speed, 0, (playerPos.z - zombiePos.z) * speed));
 
             zombieControl.setWalkDirection(walkDirection);
             zombieControl.setViewDirection(viewDirection);
             channel.setSpeed(1f);
             //Animation
             //if (!channel.getAnimationName().equals("walk")) {
             //}
         } else {
             if(!paused){
                 state=0;//stand
                 //audio_zombie.stop();
                 zombieControl.setWalkDirection(new Vector3f(0, 0, 0));
             }else{
                 channel.setSpeed(0f);
             }
             //channel.setAnim(null);
             //channel.setAnim("stand"); de moment no te animacio stand
             //channel.setAnim("walk");
         }
     }
 
     public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
         if (animName.equals("walk")&&state==1) {

             channel.setAnim("walk", 0.50f);
             channel.setLoopMode(LoopMode.DontLoop);
             channel.setSpeed(1f);
         }
         else if (animName.equals("walk")&&state==0) {

             channel.setAnim("stand", 0.50f);
             channel.setLoopMode(LoopMode.DontLoop);
             channel.setSpeed(1f);
         }
         else if (animName.equals("stand")&&state==0) {

             channel.setAnim("stand", 0.50f);
             channel.setLoopMode(LoopMode.DontLoop);
             channel.setSpeed(0f);
         }
         else if (animName.equals("stand")&&state==1) {

             channel.setAnim("walk", 0.50f);
             channel.setLoopMode(LoopMode.DontLoop);
             channel.setSpeed(1f);
         }
         else if (animName.equals("attack")) {

             channel.setAnim("walk", 0.50f);
             channel.setLoopMode(LoopMode.DontLoop);
             channel.setSpeed(1f);
             state=0;
         }else if (animName.equals("death")) {
             node1.detachChild(zombieShape);
             app.getRootNode().attachChild(zombieShape);
             zombieShape.setLocalTranslation(colisions.getPhysicsLocation());
             zombieShape.move(0f, -2.5f, 0f);
             node1.removeControl(colisions);
             node1.removeControl(zombieControl);
             this.app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(colisions);
             this.app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(zombieControl);
         }
     }
 
     public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
         // unused
     }
     
     // @David C. -- Añadido getters & setters del parámetro paused
     public void setPaused(boolean paused) {
         this.paused = paused;
     }
 
     public boolean isPaused() {
         return this.paused;
     }
 
 
     public CompoundCollisionShape getShapeForCollision() {
         return ccs;
     }
 
     public void doDamage(int damage, boolean distance) {
         hitpoints=hitpoints-damage;
         if(hitpoints<=0){
             killZombie();
         }
         //throw new UnsupportedOperationException("Not supported yet.");
     }
     
     public void killZombie(){
         state=3;
         //zombieControl.setFallSpeed(1000000f);    
         channel.setAnim("death");
         channel.setSpeed(0.4f);
         System.out.print("La animacio dura"+channel.getAnimMaxTime());
         channel.setLoopMode(LoopMode.DontLoop);
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 }

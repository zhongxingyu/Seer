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
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
 import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
 import com.jme3.bullet.control.CharacterControl;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Node;
 import java.util.Random;
 import mygame.Controller;
 import mygame.model.zombie.ZombieManagerInterface.difficulty;
 import mygame.sound.SoundManager;
 
 public class ZombieBasic extends Zombie implements AnimEventListener {
 
     private static final int DISTFOLLOW = 50;
     private static final int DISTDETECT = 20;
     private static final int ANGLEFOLLOW = 160;
     private static final int DISTATTACK = 7;
     private static final int DAMAGEDONE = 10;
     //for random movement
     private boolean randMoveSet = false;
     private Random rand = new Random();
     private int timeLeft;
     private Vector3f moveDirection;
     private float xIncrement;
     private float zIncrement;
 
     public ZombieBasic(SimpleApplication app, Vector3f position, Vector3f viewDirection, difficulty dif, int i) {
         super(app, position, viewDirection, i);
 
         CapsuleCollisionShape cilinder = new CapsuleCollisionShape(1.5f, 2f, 1);
         zombieControl = new CharacterControl(cilinder, 0.1f);
         zombieShape = app.getAssetManager().loadModel("Models/zombie/zombie.mesh.j3o");
         
         node1 = new Node();
         node1.attachChild(zombieShape);
         zombieShape.move(0f, -2.5f, 0f);
         node1.addControl(zombieControl);
 
         zombieShape.scale(3f);
         colisions = new RigidBodyControl(1f);
         node1.addControl(colisions);
         ccs = new CompoundCollisionShape();
         ccs.addChildShape(cilinder, new Vector3f(0f, 4.2f, 0f));
         colisions.setCollisionShape(cilinder);
         colisions.setAngularDamping(0);
         colisions.setFriction(0);
         colisions.setKinematic(true);
         //MODIFICACION PARA EL GRUPO DE LOS ZOMBIES
         zombieShape.setName("Zombie");
         zombieControl.setPhysicsLocation(position);
         zombieControl.setViewDirection(viewDirection);
         moveDirection = viewDirection;
 
 
         SoundManager.initBasicZombieSound(app, id);
         SoundManager.initFootStepBasicZombieSound(app, id);
 
         switch (dif) {
             case low:
                 this.speed = 0.05f;
                 this.hitpoints = 100;
                 break;
             case middle:
                 this.speed = 0.075f;
                 this.hitpoints = 150;
                 break;
             case high:
                 this.speed = 0.1f;
                 this.hitpoints = 200;
                 break;
         }
         initAnimation();
 
     }
 
     private void initAnimation() {
 
         control = zombieShape.getControl(AnimControl.class);
         control.addListener(this);
         channel = control.createChannel();
         channel.setAnim("walk");
         channel.setSpeed(0f);
         channel.setAnim("stand");
         channel.setSpeed(1f);
         channel.setLoopMode(LoopMode.Loop);
 
         //channel.setAnim("stand"); de moment no te animacio stand
     }
 
     protected void moveZombie() {
 //        SoundManager.zombieSoundPlay(app.getRootNode()); // Reproduce el sonido de los zombies
         Vector3f zombiePos = zombieControl.getPhysicsLocation();
         Vector3f playerPos = ((Controller) app).getPlayerManager().getPlayerPosition();
 
         float dist = playerPos.distance(zombiePos);
         float angle = zombieControl.getViewDirection().normalize().angleBetween(playerPos.subtract(zombiePos).normalize());
 
         if (dist < DISTFOLLOW && angle < (ANGLEFOLLOW * Math.PI / 360) || dist < DISTDETECT) {
             Vector3f walkDirection = new Vector3f((playerPos.x - zombiePos.x), 0, (playerPos.z - zombiePos.z));
             zombieControl.setViewDirection(walkDirection);
             // follow player
             if (dist < DISTATTACK) { //near the player, attack and stop
                 if (state != 2) {
                     //((Controller) app).getPlayerManager().doDamage(DAMAGEDONE); //do damage once every animation!!!
                     channel.setAnim("attack", 0.50f);
                     channel.setLoopMode(LoopMode.DontLoop);
                     //System.out.println("attack");
                 }
                 SoundManager.basicZombieFootStepsPause(app.getRootNode(), id);
                 state = 2;//attack
                 zombieControl.setWalkDirection(new Vector3f(0, 0, 0));
                 channel.setSpeed(1f);
             } else if (state != 2) {
                 SoundManager.basicZombieFootStepsPlay(app.getRootNode(), id);
                 state = 1;
                 // Si el juego NO esta mutado o pausado ejecutar la siguiente linea
                 //SoundManager.zombieSoundSetVolume(app.getRootNode(), 1 / dist);
 
                 zombieControl.setWalkDirection(walkDirection.normalize().mult(speed));
                 channel.setSpeed(1f);
             }
         } else {
             //random movement
             SoundManager.basicZombieFootStepsPlay(app.getRootNode(), id);
             state = 1;//move
            channel.setSpeed(1f);
             if (!randMoveSet) {
                 //System.out.println("set random move " + moveDirection);
                 rand = new Random();
                 timeLeft = rand.nextInt(400) + 100;
 
                 xIncrement = (rand.nextFloat() - 0.5f) / 600;
                 zIncrement = (rand.nextFloat() - 0.5f) / 600;
 
                 randMoveSet = true;
             } else {
                 timeLeft--;
                 if (timeLeft <= 0) {
                     randMoveSet = false;
                 }
             }
 
             //changing the move direction
             moveDirection = moveDirection.add(xIncrement, 0f, zIncrement);
             moveDirection = moveDirection.normalize().mult(speed);
 
             zombieControl.setWalkDirection(moveDirection);
             zombieControl.setViewDirection(moveDirection);
             //zombieControl.setWalkDirection(new Vector3f(0, 0, 0));
         }
 
         /*
          if (dist < DISTATTACK && angle < (ANGLEFOLLOW * Math.PI / 360)) { //follow!!
          if (state != 2) {
          channel.setAnim("attack", 0.50f);
          channel.setLoopMode(LoopMode.DontLoop);
          System.out.println("attack");
          }
          state = 2;//attack
          zombieControl.setWalkDirection(new Vector3f(0, 0, 0));
          channel.setSpeed(1f);
 
          } else if (dist < DISTFOLLOW && angle < (ANGLEFOLLOW * Math.PI / 360) && state != 2) {
          state = 1;//move
 
          } else {
          state = 0;//stand
          //audio_zombie.stop();
          zombieControl.setWalkDirection(new Vector3f(0, 0, 0));
          //channel.setAnim(null);
          //channel.setAnim("stand"); de moment no te animacio stand
          //channel.setAnim("walk");
          }/**/
         if (state != 3) {
 //            SoundManager.zombieSoundSetVolume(app.getRootNode(), 7 / dist);
         }
     }
 
     public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
 
         Vector3f zombiePos = zombieControl.getPhysicsLocation();
         Vector3f playerPos = ((Controller) app).getPlayerManager().getPlayerPosition();
         float dist1 = playerPos.distance(zombiePos);
         
         if (animName.equals("walk") && state == 1) {
             SoundManager.basicZombieFootStepsPlay(app.getRootNode(), id);
             //System.out.println("Zombie walks");
             SoundManager.basicZombieFootStepsSetVolume(app.getRootNode(), id, 7 / dist1);
             channel.setAnim("walk", 0.50f);
             channel.setLoopMode(LoopMode.DontLoop);
             channel.setSpeed(1f);
         } else if (animName.equals("walk") && state == 0) {
 
             channel.setAnim("stand", 0.50f);
             channel.setLoopMode(LoopMode.DontLoop);
             channel.setSpeed(1f);
         } else if (animName.equals("stand") && state == 0) {
 
             channel.setAnim("stand", 0.50f);
             channel.setLoopMode(LoopMode.DontLoop);
             channel.setSpeed(0f);
         } else if (animName.equals("stand") && state == 1) {
 
             channel.setAnim("walk", 0.50f);
             channel.setLoopMode(LoopMode.DontLoop);
             channel.setSpeed(1f);
         } else if (animName.equals("attack")) {
             if (dist1 < DISTATTACK){
                 damagePlayer();
                 SoundManager.basicZombieAttackPlayInstance(app.getRootNode());
             }
 
             channel.setAnim("walk", 0.50f);
             channel.setLoopMode(LoopMode.DontLoop);
             channel.setSpeed(1f);
             state = 0;
         } else if (animName.equals("death")) {
             SoundManager.basicZombieFootStepsPause(app.getRootNode(), id);
             SoundManager.basicZombieSoundPause(app.getRootNode(), id);
             node1.removeControl(colisions);
             node1.removeControl(zombieControl);
             control.clearListeners();
             this.app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(colisions);
             this.app.getStateManager().getState(BulletAppState.class).getPhysicsSpace().remove(zombieControl);
 
             ((Controller) app).getZombieManager().deleteZombie(this);
         }
     }
 
     public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
         // unused
     }
 
     public void doDamage(int damage, boolean distance) {
         //System.out.println("zombie class -> damage done");
         if (state != 3) {
             if (distance) { //long range, allways does damage
                 hitpoints = hitpoints - damage;
                 if (hitpoints <= 0) {
                     killZombie();
                 } else {
                     SoundManager.basicZombieHurtPlayInstance(app.getRootNode());
                 }
             } else {
                 Vector3f zombiePos = zombieControl.getPhysicsLocation();
                 Vector3f playerPos = ((Controller) app).getPlayerManager().getPlayerPosition();
 
                 if (playerPos.distance(zombiePos) < 10) {
                     hitpoints = hitpoints - damage;
                     if (hitpoints <= 0) {
                         killZombie();
                     } else {
                         SoundManager.basicZombieHurtPlayInstance(app.getRootNode());
                     }
                 }
             }
         }
     }
 
     public void killZombie() {
         state = 3;
         //zombieControl.setFallSpeed(1000000f);
         zombieControl.setWalkDirection(new Vector3f(0, 0, 0));
         SoundManager.basicZombieDiePlayInstance(app.getRootNode());
         channel.setAnim("death");
         channel.setSpeed(0.4f);
 
         channel.setLoopMode(LoopMode.DontLoop);
         //System.out.println(((Controller) app).getZombieManager());
     }
 
     public void setDifficulty(difficulty dif) {
         switch (dif) {
             case low:
                 this.speed = 0.05f;
                 this.hitpoints = 100;
                 break;
             case middle:
                 this.speed = 0.075f;
                 this.hitpoints = 150;
                 break;
             case high:
                 this.speed = 0.1f;
                 this.hitpoints = 200;
                 break;
         }
     }
     
     public void damagePlayer(){
         ((Controller) app).getPlayerManager().doDamage(DAMAGEDONE);
     }
 }

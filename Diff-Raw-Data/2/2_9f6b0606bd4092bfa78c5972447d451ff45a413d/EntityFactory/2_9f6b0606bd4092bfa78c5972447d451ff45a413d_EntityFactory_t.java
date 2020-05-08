 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.codefuss;
 
 import com.codefuss.actions.MoveLeft;
 import com.codefuss.entities.Sprite;
 import com.codefuss.entities.Block;
 import com.codefuss.entities.Player;
 import com.codefuss.entities.ShotgunFire;
 import com.codefuss.entities.Zombie;
 import com.codefuss.physics.Body;
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.util.Log;
 
 /**
  *
  * @author Martin Vium <martin.vium@gmail.com>
  */
 public class EntityFactory {
 
     AnimationFactory spriteFactory;
     PhysicsFactory physicsFactory;
 
     public EntityFactory(AnimationFactory spriteFactory, PhysicsFactory physicsFactory) {
         this.spriteFactory = spriteFactory;
         this.physicsFactory = physicsFactory;
     }
 
     public Block getBlocker(Vector2f position) {
         return new Block();
     }
 
     public Player getPlayer(Vector2f position) {
         Animation aniLeft = spriteFactory.getPlayerWalkAnimationLeft();
         Body body = physicsFactory.getDynamicBox(position.x, position.y, aniLeft.getWidth() / 2, aniLeft.getHeight());
 
         Log.debug("add player at: " + position.toString());
         Player player = new Player(this, position, body);
         player.addStateAnimation(new StateAnimation(aniLeft,
                 spriteFactory.getPlayerWalkAnimationRight(), Sprite.State.NORMAL, 0));
         player.addStateAnimation(new StateAnimation(spriteFactory.getPlayerWalkAnimationLeft(),
                 spriteFactory.getPlayerWalkAnimationRight(), Sprite.State.WALKING, 0));
         player.addStateAnimation(new StateAnimation(spriteFactory.getPlayerShootAnimationFlipped(),
                 spriteFactory.getPlayerShootAnimation(), Sprite.State.ATTACKING, 600));
         player.init();
         return player;
     }
 
     public Entity getEntity(String type, String name, Vector2f position) {
         if (type.equals("zombie")) {
             return getZombie(position);
         }
 
         Log.debug("invalid entity type: " + type);
         return null;
     }
 
     public Entity getZombie(Vector2f position) {
         position.y = 8;
         Animation aniLeft = spriteFactory.getZombieWalkAnimationLeft();
         Body body = physicsFactory.getDynamicBox(position.x, position.y, aniLeft.getWidth() / 2, aniLeft.getHeight());
 
         Zombie zombie = new Zombie(this, position, body);
         zombie.addStateAnimation(new StateAnimation(aniLeft,
                 spriteFactory.getZombieWalkAnimationRight(), Sprite.State.NORMAL, 0));
         zombie.addStateAnimation(new StateAnimation(spriteFactory.getZombieWalkAnimationLeft(),
                 spriteFactory.getZombieWalkAnimationRight(), Sprite.State.WALKING, 0));
         zombie.init();
         new MoveLeft(zombie).invoke();
         return zombie;
     }
 
     public Entity getShotgunFire(Vector2f position) {
         Animation ani = spriteFactory.getShotgunFireAnimation();
        Body body = physicsFactory.getStaticBox(position.x, position.y, 10, 30);
         
         ShotgunFire fire = new ShotgunFire(position, body);
         fire.addStateAnimation(new StateAnimation(ani, ani, Sprite.State.NORMAL, 250));
         fire.init();
         return fire;
     }
 }

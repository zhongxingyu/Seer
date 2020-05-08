 package me.hopps.ld27.game.systems;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.annotations.Mapper;
 import com.artemis.systems.EntityProcessingSystem;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.utils.TimeUtils;
 import me.hopps.ld27.game.components.*;
 
 public class PhysicsUpdater extends EntityProcessingSystem {
     @Mapper ComponentMapper<Position> pos;
     @Mapper ComponentMapper<Physics> phy;
 
     ImmutableBag<Entity> bag;
     Sound jumpSound;
 
     long lastPlay;
 
     public boolean jump, left, right, falling, won, lose;
 
     public PhysicsUpdater(Sound jump) {
         super(Aspect.getAspectForAll(Physics.class, Position.class));
         this.jumpSound = jump;
     }
 
     @Override
     protected void process(Entity e) {
         if(bag == null) {
             bag = world.getSystem(PhysicsUpdater.class).getActives();
         }
         Physics ph = phy.get(e);
         Position p = pos.get(e);
 
        if(p.getY() > 620 || p.getX() < -32 || p.getX() > 832) {
             lose = true;
         }
 
         if(ph.isActive()) {
             ph.addVelY(1000f * world.delta);
             p.addY(ph.getVelY() * world.delta);
             ph.getBounds().setY(p.getY());
             falling = true;
             if(checkCollision(e, ph, bag)) {
                 p.addY(-ph.getVelY() * world.delta);
                 ph.getBounds().setY(p.getY());
                 ph.setVelY(0f);
                 falling = false;
             }
 
             if(jump) {
                 if(!falling) {
                     ph.setVelY(-400f);
                     falling = true;
                     if(TimeUtils.millis() - lastPlay > 170) {
                         lastPlay = TimeUtils.millis();
                         jumpSound.play();
                     }
                 }
                 jump = false;
             }
             if(right) {
                 right = false;
                 p.addX(250f* world.delta);
                 ph.getBounds().setX(p.getX());
                 if(checkCollision(e, ph, bag)) {
                     p.addX(-250f* world.delta);
                     ph.getBounds().setX(p.getX());
                 }
             }
             if(left) {
                 left = false;
                 p.addX(-250f* world.delta);
                 ph.getBounds().setX(p.getX());
                 if(checkCollision(e, ph, bag)) {
                     p.addX(250f* world.delta);
                     ph.getBounds().setX(p.getX());
                 }
             }
         }
     }
 
     private boolean checkCollision(Entity e, Physics ph, ImmutableBag<Entity> bag) {
         for(int i = 0; i < bag.size(); i++) {
             if(e != bag.get(i)) {
                 if(ph.getBounds().overlaps(phy.get(bag.get(i)).getBounds())) {
                     if(bag.get(i).getComponent(EndComponent.class) == null && bag.get(i).getComponent(DeadComponent.class) == null) {
                         phy.get(bag.get(i)).setTouched(true);
                         return true;
                     } else if(bag.get(i).getComponent(EndComponent.class) != null) {
                         won = true;
                     } else if(bag.get(i).getComponent(DeadComponent.class) != null) {
                         lose = true;
                     }
                 }
             }
         }
         return false;
     }
 }

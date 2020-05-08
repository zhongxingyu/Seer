 package com.gravity.physics;
 
 import java.util.List;
 
 import org.newdawn.slick.geom.Shape;
 
 import com.gravity.gameplay.GravityGameController;
 import com.gravity.player.Player;
 
 public final class SpikeEntity extends TileWorldEntity {
     
     private final GravityGameController controller;
     
     public SpikeEntity(GravityGameController controller, Shape shape) {
         super(shape);
         this.controller = controller;
     }
     
     @Override
     public Shape handleCollisions(int ticks, List<Collision> collisions) {
         for (Collision c : collisions) {
             Entity e = c.getOtherEntity(this);
             if (e instanceof Player) {
                 Player p = (Player) e;
                controller.playerHitSpikes(p);
             }
         }
         return super.handleCollisions(ticks, collisions);
     }
     
 }

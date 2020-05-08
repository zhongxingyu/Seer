 package com.gravity.physics;
 
 import java.util.List;
import java.util.Set;
 
 import org.newdawn.slick.geom.Shape;
 
import com.google.common.collect.Sets;
 import com.gravity.gameplay.GravityGameController;
 import com.gravity.player.Player;
 
 public final class SpikeEntity extends TileWorldEntity {
     
     private final GravityGameController controller;
    private final Set<Player> collidedPlayers = Sets.newIdentityHashSet();
     
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
                if (collidedPlayers.add(p)) {
                    controller.playerHitSpikes(p);
                }
             }
         }
         return super.handleCollisions(ticks, collisions);
     }
     
 }

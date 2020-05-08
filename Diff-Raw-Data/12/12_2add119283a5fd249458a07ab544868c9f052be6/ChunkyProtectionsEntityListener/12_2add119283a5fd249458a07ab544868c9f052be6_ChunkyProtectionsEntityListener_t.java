 package org.getchunky.chunky.protections.listener;
 
import org.getchunky.chunky.ChunkyManager;
import org.getchunky.chunky.object.ChunkyChunk;
import org.getchunky.chunky.object.ChunkyPlayer;
import org.getchunky.chunky.permission.ChunkyAccessLevel;
import org.getchunky.chunky.permission.ChunkyPermissionChain;
import org.getchunky.chunky.permission.ChunkyPermissions;
 import org.getchunky.chunky.protections.permission.Perm;
 import org.bukkit.entity.Monster;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityExplodeEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.getchunky.chunky.protections.util.Logging;
 
 /**
  * @author dumptruckman
  */
 public class ChunkyProtectionsEntityListener extends EntityListener {
 
     @Override
     public void onEntityExplode(EntityExplodeEvent event) {
         if (event.isCancelled()) return;
 
         if (event.getEntity() instanceof Monster) creeperGrief(event);
     }
 
     public void creeperGrief(EntityExplodeEvent event) {
         Monster monster = (Monster)event.getEntity();
         if (monster.getTarget() == null || !(monster.getTarget() instanceof Player)) return;
 
         Player player = (Player)monster.getTarget();
         if (!Perm.CREEPER_GRIEF.has(player)) return;
         
         ChunkyPlayer cPlayer = ChunkyManager.getChunkyPlayer(player);
         ChunkyChunk cChunk = cPlayer.getCurrentChunk();
         ChunkyAccessLevel access = ChunkyAccessLevel.NONE;
         boolean hasPerm = ChunkyPermissionChain.hasPerm(cChunk, cPlayer, ChunkyPermissions.Flags.DESTROY, access);
         Logging.debug("Exploding monster targeting: " + player.getName() + " on land they " + (hasPerm ? "may" : "may not") + "destroy.");
         if (!hasPerm) {
             event.setCancelled(true);
         }
     }
 }

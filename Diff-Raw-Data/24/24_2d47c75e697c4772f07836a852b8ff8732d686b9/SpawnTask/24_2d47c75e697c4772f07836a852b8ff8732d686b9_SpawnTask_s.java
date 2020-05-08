 package ch.minepvp.spout.kingdoms.task;
 
 import ch.minepvp.spout.kingdoms.database.table.Kingdom;
 import org.spout.api.chat.ChatArguments;
 import org.spout.api.entity.Player;
 import org.spout.api.lang.Translation;
 
 public class SpawnTask implements Runnable {
 
     private Player player;
     private Kingdom kingdom;
 
     private int lastX;
     private int lastY;
     private int lastZ;
 
     public SpawnTask( Player player, Kingdom kingdom ) {
         this.player = player;
         this.kingdom = kingdom;
 
        lastX = player.getTransform().getPosition().getBlockX();
        lastY = player.getTransform().getPosition().getBlockY();
        lastZ = player.getTransform().getPosition().getBlockZ();
     }
 
     @Override
     public void run() {
 
        if ( player.getTransform().getPosition().getBlockX() == lastX &&
             player.getTransform().getPosition().getBlockY() == lastY &&
             player.getTransform().getPosition().getBlockZ() == lastZ ) {
 
             player.teleport(kingdom.getSpawnPoint());
 
         } else {
             player.sendMessage(ChatArguments.fromFormatString(Translation.tr("{{RED}}Teleportation aborted! You have moved!", player)));
         }
 
     }
 
 }

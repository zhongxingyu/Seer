 package me.wizzledonker.plugins.telepads;
 
 import java.util.HashSet;
 import java.util.Set;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerMoveEvent;
 
 /**
  *
  * @author Win
  */
 public class telepadsPlayerListener implements Listener{
     public static Telepads plugin;
     
     private Set<Player> onPad = new HashSet<Player>();
     
     public telepadsPlayerListener(Telepads instance) {
         plugin = instance;
     }
     
     @EventHandler
     public void whenPlayerMoves(PlayerMoveEvent event) {
         final Player player = event.getPlayer();
         if (onPad.contains(player)) return;
         if (player.hasPermission("telepads.use")) {
             Block block = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
             if (!checkPad(block)) {
                 return;
             }
             final Location loc = new Location(null, block.getX(), block.getY(), block.getZ());
             if (!plugin.telepads.containsKey(loc)) {
                 return;
             }
             onPad.add(player);
             player.sendMessage(ChatColor.GRAY + plugin.wait_msg.replace("%time%", plugin.telepad_teleport_time + " Seconds"));
             plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
 
                 public void run() {
                     onPad.remove(player);
                     Block cBlock = player.getLocation().getBlock().getRelative(BlockFace.DOWN);
                     if (!checkPad(cBlock)) return;
                     plugin.gotoPad(loc, player);
                 }
             }, plugin.telepad_teleport_time * 20L);
         }
     }
     
     private boolean checkPad(Block block) {
         if (block.getTypeId() != plugin.telepad_item_id) {
             return false;
         }
         return true;
     }
     
 }

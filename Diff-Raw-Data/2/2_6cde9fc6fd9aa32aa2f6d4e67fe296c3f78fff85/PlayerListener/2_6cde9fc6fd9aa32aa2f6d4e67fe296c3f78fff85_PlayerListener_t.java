 package de.bangl.wgtff.listeners;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.flags.StateFlag;
 import com.sk89q.worldguard.protection.flags.StateFlag.State;
 import de.bangl.wgtff.WGTreeFarmFlagPlugin;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.block.LeavesDecayEvent;
 
 /**
  *
  * @author BangL, mewin
  */
 public class PlayerListener implements Listener {
     private WGTreeFarmFlagPlugin plugin;
     private boolean hasBlockRestricter;
 
     // Command flags
     public static final StateFlag FLAG_TREEFARM = new StateFlag("treefarm", true);
 
     public PlayerListener(WGTreeFarmFlagPlugin plugin) {
         this.plugin = plugin;
 
         // Register custom flags
         plugin.getWGCFP().addCustomFlag(FLAG_TREEFARM);
 
         // Register events
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         
         hasBlockRestricter = plugin.getServer().getPluginManager().getPlugin("WGBlockRestricter") != null;
 
     }
 
     @EventHandler
     public void onBlockPlace(BlockPlaceEvent event) {
         
         final WorldGuardPlugin wgp = plugin.getWGP();
         final Block block = event.getBlock();
         final Player player = event.getPlayer();
         
         //lets block restricter handle it
         if (!hasBlockRestricter
                || !wgp.getRegionManager(block.getWorld()).getApplicableRegions(block.getLocation()).allows(FLAG_TREEFARM)) {
             // treefarm is set to "deny"
             // so let's cancel this placement
             // an op/member/owner can still build, if treefarm is set to "allow".
             final String msg = this.plugin.getConfig().getString("messages.block.blockplace");
             player.sendMessage(ChatColor.RED + msg);
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
 
         final WorldGuardPlugin wgp = plugin.getWGP();
         final Location loc = event.getBlock().getLocation();
         final Block block = event.getBlock();
         final World world = block.getWorld();
         final Player player = event.getPlayer();
         final Material material = block.getType();
         final State state = wgp.getRegionManager(world).getApplicableRegions(loc).getFlag(FLAG_TREEFARM);
 
         // handle if ((allowed treefarm region
         // and player is not op
         // and can not build)
         // or denied treefarm region)
         if (state != null
                 && (state == State.DENY
                 || (!player.isOp()
                 && !wgp.canBuild(player, block)))) {
 
             if (material == Material.LOG) {
                 // Log destroyed
                 byte data = block.getData();
 
                 // drop log
                 block.breakNaturally();
 
                 final Location locUnder = event.getBlock().getLocation();
                 locUnder.setY(block.getY() - 1.0D);
 
                 // this was a tree?
                 if ((locUnder.getBlock().getType() == Material.DIRT)
                         || (locUnder.getBlock().getType() == Material.GRASS)) {
                     // Turn log to sapling
                     block.setTypeIdAndData(Material.SAPLING.getId(), data, false);
                 } else {
                     // Turn log to air
                     block.setType(Material.AIR);
                 }
             } else if (material == Material.SAPLING) {
                 // Sapling destroyed.
 
                 // Send Warning
                 final String msg = this.plugin.getConfig().getString("messages.block.saplingdestroy");
                 player.sendMessage(ChatColor.RED + msg);
             } else if (material == Material.LEAVES) {
                 // Leaf destroyed
 
                 // Turn leaf to air
                 block.setType(Material.AIR);
             } else if (!hasBlockRestricter
                     || !com.mewin.WGBlockRestricter.Utils.blockAllowedAtLocation(wgp, material, loc)) {
                 // Any other block destroyed
                         
                 // Send Warning
                 final String msg = this.plugin.getConfig().getString("messages.block.blockdestroy");
                 player.sendMessage(ChatColor.RED + msg);
             } else {
                 return;
             }
 
             event.setCancelled(true);
         }
     }
 
     @EventHandler
     public void onLeaveDecay(LeavesDecayEvent event) {
         
         final Block block = event.getBlock();
         final Location loc = block.getLocation();
         final World world = block.getWorld();
 
         // Cancel if treefarm region
         if (plugin.getWGP().getRegionManager(world).getApplicableRegions(loc).getFlag(FLAG_TREEFARM) != null) {
             // turn leave to air
             block.setType(Material.AIR);
             event.setCancelled(true);
         }
     }
 }

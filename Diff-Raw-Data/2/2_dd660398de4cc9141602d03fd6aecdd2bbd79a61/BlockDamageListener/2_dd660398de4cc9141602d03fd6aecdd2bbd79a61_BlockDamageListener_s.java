 package de.omr.wgibf.listener;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.flags.StateFlag;
 import de.omr.wgibf.WGInstabreakFlagPlugin;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockDamageEvent;
 
 /**
  *
  * @author OMR, BangL, mewin
  */
 public class BlockDamageListener implements Listener {
     private WGInstabreakFlagPlugin plugin;
     private boolean hasBlockRestricter;
 
     public static final StateFlag FLAG_INSTABREAK = new StateFlag("instabreak", false);
 
     public BlockDamageListener (WGInstabreakFlagPlugin plugin) {
         this.plugin = plugin;
         plugin.getWGCFP().addCustomFlag(FLAG_INSTABREAK);
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         hasBlockRestricter = plugin.getServer().getPluginManager().getPlugin("WGBlockRestricter") != null;
     }
 
     @EventHandler
     public void onBlockDamage(BlockDamageEvent event) {
         final Player player = event.getPlayer();
         final Block block = event.getBlock();
         final Location loc = block.getLocation();
         final World world = block.getWorld();
         final WorldGuardPlugin wgp = plugin.getWGP();
         final Material material = block.getType();
         
         if (wgp.getRegionManager(world).getApplicableRegions(loc).getFlag(FLAG_INSTABREAK) != null) {
             if ((!hasBlockRestricter
                    || !com.mewin.WGBlockRestricter.Utils.blockAllowedAtLocation(wgp, material, loc))
                     && wgp.getRegionManager(world).getApplicableRegions(loc).allows(FLAG_INSTABREAK)
                     && (player.isOp()
                     || wgp.canBuild(player, block))) {
                 if (plugin.getConfig().getBoolean("settings.drop")) {
                     block.breakNaturally();
                 } else {
                     block.setTypeId(Material.AIR.getId());
                 }
                 event.setCancelled(true);
             }
         }
     }
 }

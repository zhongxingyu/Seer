 package de.bangl.wgdepf.listener;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.flags.StateFlag;
 import de.bangl.wgdepf.WGDragonEggPortFlagPlugin;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 /**
  *
  * @author BangL
  */
 public class PlayerInteractListener implements Listener {
     private WGDragonEggPortFlagPlugin plugin;
 
     // Command flags
     public static final StateFlag FLAG_DRAGON_EGG_PORT = new StateFlag("dragoneggport", true);
 
     public PlayerInteractListener(WGDragonEggPortFlagPlugin plugin) {
         this.plugin = plugin;
 
         // Register custom flags
         plugin.getWGCFP().addCustomFlag(FLAG_DRAGON_EGG_PORT);
 
         // Register events
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
 
     }
 
     @EventHandler
     public void onPlayerInteractEvent(PlayerInteractEvent event) {
 
         final Player player = event.getPlayer();
         final Block block = event.getClickedBlock();
         final Action action = event.getAction();
         final WorldGuardPlugin wgp = plugin.getWGP();
 
         // Block if the action is
         // in a denied region
         // and the action was a right or left click
         // on a dragon egg
         // and the player was no op
         // and has no permission to build here.
        if (!wgp.getRegionManager(block.getWorld()).getApplicableRegions(block.getLocation()).allows(FLAG_DRAGON_EGG_PORT)
                && (action == Action.RIGHT_CLICK_BLOCK
                || action == Action.LEFT_CLICK_BLOCK)
                && block.getType() == Material.DRAGON_EGG
                && !player.isOp()
                && !wgp.canBuild(player, block)) {
            final String msg = this.plugin.getConfig().getString("messages.blocked");
            player.sendMessage(ChatColor.RED + msg);
            event.setCancelled(true);
         }
     }
 }

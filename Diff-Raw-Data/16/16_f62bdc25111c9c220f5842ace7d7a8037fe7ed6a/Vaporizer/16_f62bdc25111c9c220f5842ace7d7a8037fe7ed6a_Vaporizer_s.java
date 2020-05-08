 package edgruberman.bukkit.silkpoke;
 
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.World.Environment;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockFadeEvent;
 import org.bukkit.plugin.Plugin;
 
 /**
  * Immediately removes any water in the nether that is the result of a broken ice block.
  */
 public class Vaporizer implements Listener {
 
     Vaporizer(final Plugin plugin) {
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onBlockBreak(final BlockBreakEvent event) {
         if (event.getBlock().getWorld().getEnvironment() != Environment.NETHER) return;
 
         if (event.getBlock().getTypeId() != Material.ICE.getId()) return;
 
        if (event.getPlayer().hasPermission("silktouch.nether.water")) return;
 
         event.getBlock().setTypeIdAndData(Material.AIR.getId(), (byte) 0, true);
         event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.EXTINGUISH, 0);
         event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.SMOKE, BlockFace.UP);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onBlockFade(final BlockFadeEvent event) {
         if (event.getBlock().getWorld().getEnvironment() != Environment.NETHER) return;
 
         if (event.getBlock().getTypeId() != Material.ICE.getId()) return;
 
         event.setCancelled(true);
         event.getBlock().setTypeIdAndData(Material.AIR.getId(), (byte) 0, true);
         event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.EXTINGUISH, 0);
         event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.SMOKE, BlockFace.UP);
     }
 
 }

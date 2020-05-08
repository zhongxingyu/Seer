 package edgruberman.bukkit.silkpoke;
 
 import org.bukkit.Effect;
 import org.bukkit.Material;
 import org.bukkit.World.Environment;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.plugin.Plugin;
 
 /**
  * Immediately vaporizes any ice placed in the nether.
  */
 public class Sublimator implements Listener {
 
     Sublimator(final Plugin plugin) {
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onBlockPlace(final BlockPlaceEvent event) {
         if (event.getBlock().getWorld().getEnvironment() != Environment.NETHER) return;
 
         if (event.getBlock().getTypeId() != Material.ICE.getId()) return;
 
        if (event.getPlayer().hasPermission("silkpoke.nether.ice")) return;
 
         // If the material does not change, we have to decrement the stack manually
         if (event.getBlockReplacedState().getType() == Material.AIR)
             event.getPlayer().getItemInHand().setAmount(event.getPlayer().getItemInHand().getAmount() - 1);
 
         event.getBlock().setTypeIdAndData(Material.AIR.getId(), (byte) 0, true);
         event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.EXTINGUISH, 0);
         event.getBlock().getWorld().playEffect(event.getBlock().getLocation(), Effect.SMOKE, BlockFace.UP);
     }
 
 }

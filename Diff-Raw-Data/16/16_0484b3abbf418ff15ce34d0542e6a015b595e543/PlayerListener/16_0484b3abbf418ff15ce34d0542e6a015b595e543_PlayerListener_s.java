 package edgruberman.bukkit.simplelocks;
 
 import java.util.logging.Level;
 
 import org.bukkit.Material;
 import org.bukkit.block.BlockFace;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.plugin.Plugin;
 
 import edgruberman.bukkit.messagemanager.MessageLevel;
 
 public final class PlayerListener implements Listener {
 
     private final Plugin plugin;
 
     PlayerListener(final Plugin plugin) {
         this.plugin = plugin;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler
     public void onPlayerInteract(final PlayerInteractEvent event) {
         if (event.isCancelled()) return;
 
         if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
 
         final Lock lock = Lock.getLock(event.getClickedBlock());
         if (lock != null) {
             // Existing lock in place.
 
             if (!lock.hasAccess(event.getPlayer())) {
                 // Player does not have access, cancel interaction and notify player.
                 event.setCancelled(true);
                Main.messageManager.send(event.getPlayer(), "You do not have access to this lock.", MessageLevel.RIGHTS, false);
                 this.plugin.getLogger().log(Level.FINER,
                         "Lock access denied to " + event.getPlayer().getName() + " at "
                             + " x:" + event.getClickedBlock().getX()
                             + " y:" + event.getClickedBlock().getY()
                             + " z:" + event.getClickedBlock().getZ()
                 );
                 return;
             }
 
            if (Lock.isLock(event.getClickedBlock())) {
                // Player has access and they right clicked on the lock itself so give them information.
                Main.messageManager.send(event.getPlayer(), "You have access to this lock.", MessageLevel.STATUS, false);
 
                if (lock.isOwner(event.getPlayer()))
                    Main.messageManager.send(event.getPlayer(), "To modify: /lock (+|-) <Player>", MessageLevel.NOTICE, false);
            }
             return;
         }
 
         // No existing lock, check to see if player is requesting a lock be created.
         if (event.getClickedBlock().getType().equals(Material.CHEST)) {
             if (event.getMaterial().equals(Material.SIGN)
                     && event.getClickedBlock().getRelative(event.getBlockFace()).getType().equals(Material.AIR)
                     && !event.getBlockFace().equals(BlockFace.UP) && !event.getBlockFace().equals(BlockFace.DOWN)) {
 
                 // Right click on a chest with a sign to create lock automatically.
                 event.setUseInteractedBlock(Result.DENY); // Don't open the chest.
 
                 // Check for default owner substitute. (Useful for long names that won't fit on a sign.)
                 final String ownerName = Main.getDefaultOwner(event.getPlayer());
                 if (ownerName.length() > 15) {
                    Main.messageManager.send(event.getPlayer(), "Unable to create lock; Owner name is too long.", MessageLevel.SEVERE, false);
                     return;
                 }
 
                 // Pay the piper.
                 event.getPlayer().setItemInHand(null);
 
                 // Create lock.
                 new Lock(event.getClickedBlock().getRelative(event.getBlockFace())
                         , event.getBlockFace().getOppositeFace()
                         , ownerName
                 );
             }
         }
     }
 
 }

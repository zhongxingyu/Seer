 package edgruberman.bukkit.borderpatrol;
 
 import java.util.logging.Level;
 
 import org.bukkit.Location;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.util.Vector;
 
 import edgruberman.bukkit.messagemanager.MessageLevel;
 import edgruberman.bukkit.messagemanager.MessageManager;
 
 /**
  * Ensures players stay within the defined borders.
  */
 final class BorderAgent implements Listener {
 
     private final Plugin plugin;
     private final CivilEngineer engineer;
     private final String message;
 
     BorderAgent(final Plugin plugin, final CivilEngineer engineer, final String message) {
         this.plugin = plugin;
         this.engineer = engineer;
         this.message = message;
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
     }
 
     @EventHandler
     void onPlayerJoin(final PlayerJoinEvent event) {
         // Ignore if no border defined for this world or player is still inside border
         final Border border = this.engineer.getBorder(event.getPlayer().getWorld());
         if (border == null || border.contains(event.getPlayer().getLocation())) return;
 
         this.enforce(event.getPlayer(), event.getPlayer().getLocation());
     }
 
     @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        if (event.isCancelled()) return;
 
         // Ignore if no border defined for destination world or player will still be inside border
         final Border border = this.engineer.getBorder(event.getTo().getWorld());
         if (border == null || border.contains(event.getTo())) return;
 
         final Location inside = this.enforce(event.getPlayer(), event.getTo());
         event.setTo(inside);
     }
 
    @EventHandler
     public void onPlayerTeleport(final PlayerTeleportEvent event) {
        if (event.isCancelled()) return;

         // Ignore if no border defined for destination world or player will still be inside border
         final Border border = this.engineer.getBorder(event.getTo().getWorld());
         if (border == null || border.contains(event.getTo())) return;
 
         final Location inside = this.enforce(event.getPlayer(), event.getTo());
         event.setTo(inside);
     }
 
     /**
      * Return player to inside border. Notify player with message and log.
      *
      * @param suspect who to return
      * @param breached where border was crossed
      * @return where suspect was sent
      */
     private Location enforce(final Player suspect, final Location breached) {
         // Notify player of breaching border
         if (this.message != null) MessageManager.of(this.plugin).tell(suspect, this.message, MessageLevel.SEVERE, false);
 
         // Find a safe location to return player to
         final Border border = this.engineer.getBorder(suspect.getWorld());
         final Location returned = border.findSafe(breached);
 
         // Return player to the middle of the block and players in vehicles need to be shifted up
         returned.setX(Math.floor(returned.getX()) + 0.5);
         if (suspect.isInsideVehicle()) returned.add(0, 1, 0);
         returned.setZ(Math.floor(returned.getZ()) + 0.5);
 
         // Send player back to inside the border
         if (suspect.isInsideVehicle()) {
             suspect.getVehicle().setVelocity(new Vector(0, 0, 0));
             suspect.getVehicle().teleport(returned);
         } else {
             suspect.teleport(returned);
         }
 
         // Log details for debug if configured
         if (this.plugin.getLogger().isLoggable(MessageLevel.FINE))
             this.plugin.getLogger().log(Level.FINE, BorderAgent.report(suspect, breached, returned));
 
         return returned;
     }
 
     /**
      * Generate human readable status of player.
      *
      * @param suspect who breached the border
      * @param breached where border was crossed
      * @param returned where suspect was sent
      * @return textual representation of player
      */
     private static String report(final Player suspect, final Location breached, final Location returned) {
         String suspectDescription = suspect.getName();
         if (suspect.isInsideVehicle())
             suspectDescription += " riding a " + suspect.getVehicle().toString().substring("Craft".length());
 
         return "Border breached in [" + breached.getWorld().getName() + "]"
                 + " by " + suspectDescription + " at x: " + breached.getBlockX() + " y: " + breached.getBlockY() + " z: " + breached.getBlockZ()
                 + "; Returned to x: " + returned.getBlockX() + " y: " + returned.getBlockY() + " z: " + returned.getBlockZ();
     }
 
 }

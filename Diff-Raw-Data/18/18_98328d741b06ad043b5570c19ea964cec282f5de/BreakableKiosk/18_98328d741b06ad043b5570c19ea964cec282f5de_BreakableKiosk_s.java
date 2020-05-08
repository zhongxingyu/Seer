 package edgruberman.bukkit.kiosk;
 
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import edgruberman.bukkit.kiosk.messaging.Sender;
 
 public class BreakableKiosk extends Kiosk implements Listener {
 
     public BreakableKiosk(final Attendant attendant, final String title, final String trigger) {
         super(attendant, title, trigger);
     }
 
     @Override
     @EventHandler(ignoreCancelled = true)
     public void onPlayerInteract(final PlayerInteractEvent interaction) {
         super.onPlayerInteract(interaction);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onBlockBreak(final BlockBreakEvent broken) {
         if (broken instanceof KioskRemove) return;
         if (!Kiosk.MATERIALS.contains(broken.getBlock().getTypeId())) return;
 
         final Sign state = (Sign) broken.getBlock().getState();
         if (!this.hasTitle(state)) return;
 
         final Function function = this.getFunction(state);
         if (!function.canUse(broken.getPlayer())) {
             Main.courier.submit(new Sender(broken.getPlayer()), function.draftDenied());
             broken.setCancelled(true);
             state.update();
             return;
         }
 
         this.dispatch(broken.getPlayer(), state);
     }
 
 }

 package edgruberman.bukkit.kiosk;
 
 import org.bukkit.block.Sign;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 import edgruberman.bukkit.kiosk.messaging.Sender;
 
 public class PunchableKiosk extends Kiosk implements Listener {
 
     public PunchableKiosk(final Attendant attendant, final String title, final String trigger) {
         super(attendant, title, trigger);
     }
 
     @Override
     @EventHandler(ignoreCancelled = true)
     public void onPlayerInteract(final PlayerInteractEvent interaction) {
         super.onPlayerInteract(interaction);
 
         if (interaction.getAction() != Action.LEFT_CLICK_BLOCK) return;
         if (!Kiosk.MATERIALS.contains(interaction.getClickedBlock().getTypeId())) return;
 
         final Sign state = (Sign) interaction.getClickedBlock().getState();
         if (!this.hasTitle(state)) return;
 
         final Function function = this.getFunction(state);
         if (!function.canUse(interaction.getPlayer())) {
             Main.courier.submit(new Sender(interaction.getPlayer()), function.draftDenied());
             interaction.setCancelled(true);
             return;
         }
 
         this.dispatch(interaction.getPlayer(), state);
     }
 
     @EventHandler(ignoreCancelled = true)
     public void onBlockBreak(final BlockBreakEvent broken) {
         if (broken instanceof KioskRemove) return;
         if (!Kiosk.MATERIALS.contains(broken.getBlock().getTypeId())) return;
 
         final Sign state = (Sign) broken.getBlock().getState();
         if (!this.hasTitle(state)) return;
 
         broken.setCancelled(true);
         state.update();
     }
 
 }

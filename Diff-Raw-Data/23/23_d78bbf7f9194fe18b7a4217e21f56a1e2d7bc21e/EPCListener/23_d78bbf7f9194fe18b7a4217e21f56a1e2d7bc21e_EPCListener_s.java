 package net.sqdmc.epc;
 
 import static net.sqdmc.epc.EPC.EPC;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class EPCListener implements Listener{
 
     /** playername -> last throw timestamp */
     private final Map<String,Long> lastThrow = new HashMap<String,Long>();
 
 
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerUseEP(PlayerInteractEvent event){
 
         if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK || 
                 event.getItem() == null || event.getItem().getType() != Material.ENDER_PEARL) {
             return;
         }
 
         Long now = System.currentTimeMillis();
         Player player = event.getPlayer();
 
         // may a player use pearls at all?
         if (!player.hasPermission("enderpearl.use")) {
            if (EPC.showMessage)
                player.sendMessage(EPC.messageNotAllowed);
 
             event.setCancelled(true);
             return;
         }
 
         // apply cooldown to player?
         if (validthrow(player, now)) {
             if (!pay(player)) // player can't pay
                 event.setCancelled(true);
             else // allow throw, set cooldown
                 lastThrow.put(player.getName(), now);
         } else {
             event.setCancelled(true);
         }
     }
 
     /** Check if player needs to and can pay for a throw. */ 
     private boolean pay(Player player) {
         if (!player.hasPermission("enderpearl.pay") || EPC.price == 0 || EPC.economy == null)
             return true;
 
         String name = player.getName();
         double price = EPC.price;
         boolean success = false;
         if (EPC.economy.has(name, price))
             success = EPC.economy.withdrawPlayer(name, price).transactionSuccess();
 
        if (!success && EPC.showMessage)
            player.sendMessage(EPC.messageMoney.replace("{price}", EPC.economy.format(price)));
         return success;
     }
 
     /** Return remaining cooldown in seconds. */
     private double remainingCooldown(Player player, long throwTime) {
         Long lastPlayerPearl = lastThrow.get(player.getName());
         return (EPC.cooldown - (throwTime - lastPlayerPearl)) / 1000.0;
     }
 
     /** Check if player is allowed to throw a pearl at this moment. */
     private boolean validthrow(Player player, long throwTime) {
         if (!player.hasPermission("enderpearl.cooldown"))
             return true; // no cooldown for this player
 
 
         Long lastPlayerPearl = lastThrow.get(player.getName());
 
         // for players with cooldown, check if cooldown has passed
         if (lastPlayerPearl == null || (throwTime - lastPlayerPearl) >= EPC.cooldown)
             return true;
 
        if (EPC.showMessage)
            player.sendMessage(EPC.messageCooldown.replace("{seconds}", String.format("%.1f", remainingCooldown(player, throwTime))));

         return false;
     }
 }

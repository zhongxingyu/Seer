 package net.sqdmc.epc;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class EPCListener implements Listener{
         
     private Map<Player,Long> lastThrow = new HashMap<Player,Long>();
     
     
     
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerUseEP(PlayerInteractEvent e){   
     	if (e.getAction() == Action.LEFT_CLICK_AIR || e.getAction() == Action.LEFT_CLICK_BLOCK || 
     			e.getItem() == null || e.getItem().getType() != Material.ENDER_PEARL) {
     		return;
     	}
     	
         Long now = System.currentTimeMillis();
         Player player = e.getPlayer();
         
         // may a player use pearls at all?
         if (!player.hasPermission("enderpearl.use")) {
         	if (EPC.EPC.showMessage)
         		player.sendMessage("You may not use ender pearls");
         	
         	e.setCancelled(true);
         	return;
         }
         
         // apply cooldown to player?
     	if (validthrow(player, now)) {
     		if (!pay(player)) // player can't pay
     			e.setCancelled(true);
     		else // allow throw, set cooldown
     			lastThrow.put(player, now);
     	} else {
     		e.setCancelled(true);
     	}	
     }
     
     /** Check if player needs to and can pay for a throw. */ 
     private boolean pay(Player player) {
    	if (!player.hasPermission("enderpearl.pay") || EPC.EPC.price == 0 || EPC.EPC.economy == null)
     		return true;
     	
     	String name = player.getName();
     	double price = EPC.EPC.price;
     	boolean success = false;
     	if (EPC.EPC.economy.has(name, price))
     		success = EPC.EPC.economy.withdrawPlayer(name, price).transactionSuccess();
     	
     	if (!success && EPC.EPC.showMessage)
     		player.sendMessage(ChatColor.RED + "Not enough money to throw pearl. Need at least " 
     		+ EPC.EPC.price + " " + EPC.EPC.economy.currencyNamePlural());
 	    return success;
     }
 
 	/** Return remaining cooldown in seconds. */
     private long remainingCooldown(Player player, long throwTime) {
         Long lastPlayerPearl = lastThrow.get(player);
         return (EPC.EPC.cooldown - (throwTime - lastPlayerPearl)) / 1000;
     }
     
     /** Check if player is allowed to throw a pearl at this moment. */
     private boolean validthrow(Player player, long throwTime) {
     	if (!player.hasPermission("enderpearl.cooldown"))
     		return true; // no cooldown for this player
     	 
     	 
         Long lastPlayerPearl = lastThrow.get(player);
 
         // for players with cooldown, check if cooldown has passed
         if (lastPlayerPearl == null || (throwTime - lastPlayerPearl) >= EPC.EPC.cooldown)
             return true;
         
 		if (EPC.EPC.showMessage)
     		player.sendMessage(ChatColor.RED + "Enderpearl cooldown remaining: " + remainingCooldown(player, throwTime) + " seconds.");
         
         return false;
     }
 }

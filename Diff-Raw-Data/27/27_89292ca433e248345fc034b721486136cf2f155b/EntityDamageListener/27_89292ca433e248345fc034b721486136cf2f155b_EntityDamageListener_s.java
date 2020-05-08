 package com.github.thebiologist13.listeners;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.github.thebiologist13.NeverBreak;
 
 public class EntityDamageListener implements Listener {
 	
 	private NeverBreak plugin = null;
 	
 	public EntityDamageListener(NeverBreak plugin) {
 		this.plugin = plugin;
 	}
 	
 	@EventHandler
 	public void onEntityDamage(EntityDamageByEntityEvent ev) {
 		//Player
 		Player p = null;
 		//Getting a player
 		Entity e = ev.getDamager();
 		if(e instanceof Player) {
 			p = (Player) e;
 		} else {
 			return;
 		}
 		
		//Item player has in hand
		ItemStack stack = p.getItemInHand();

		plugin.resetDurability(stack, p, true);
 	}
 }

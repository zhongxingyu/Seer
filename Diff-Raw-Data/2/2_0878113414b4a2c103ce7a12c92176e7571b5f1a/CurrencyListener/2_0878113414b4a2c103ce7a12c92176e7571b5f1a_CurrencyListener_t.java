 package com.m0pt0pmatt.bettereconomy.currency;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.inventory.CraftItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 
 import com.m0pt0pmatt.bettereconomy.EconomyManager;
 
 public class CurrencyListener implements Listener{
 	
 	EconomyManager manager;
 	
 	public CurrencyListener(EconomyManager manager){
 		this.manager = manager;
 	}
 
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onMobDeath(EntityDeathEvent event){
 		
 		List<ItemStack> drops = event.getDrops();
 		if (drops == null) return;
 		Iterator<ItemStack> i = drops.iterator();
 		
 		while (i.hasNext()){
 			ItemStack item = i.next();
 			if (manager.isCurrency(item)){
 				ItemMeta meta = item.getItemMeta();
 				List<String> lore = meta.getLore();
 				if (lore == null) lore = new LinkedList<String>();
 				lore.add(ChatColor.RED + "(Not Currency)");
 				meta.setLore(lore);
 				item.setItemMeta(meta);
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.HIGH)
 	public void onRedstonePlace(PlayerInteractEvent event){
 		
 		ItemStack item = event.getItem();
 		
 		if (item == null) return;
 		
 		List<String> lore = item.getItemMeta().getLore();
 		if (item.getType().equals(Material.REDSTONE) && lore.contains(ChatColor.RED + "(Not Currency)")){
 			event.setCancelled(true);
 		}
 		
 	}
 	
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onCraft(CraftItemEvent event){
 		
 		for (ItemStack item: event.getInventory().getMatrix()){
 			
 			if (item == null) continue;
 			
			if (item.getItemMeta() == null) continue;
			
 			List<String> lore = item.getItemMeta().getLore();
 			
 			if (lore == null) continue;
 			
 			if (lore.contains(ChatColor.RED + "(Not Currency)")){
 				event.setCancelled(true);
 				return;
 			}
 		}
 		
 	}
 	
 }

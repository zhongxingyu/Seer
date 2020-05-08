 package com.github.thebiologist13.listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.github.thebiologist13.CustomSpawners;
 
 public class InteractEvent implements Listener {
 	
	private final FileConfiguration CONFIG;
 	
 	public InteractEvent(CustomSpawners plugin) {
		this.CONFIG = plugin.getCustomConfig();
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public void onPlayerInteractEvent(PlayerInteractEvent ev) {
 		
 		//Player
 		Player p = ev.getPlayer();
 		//Item in hand
 		ItemStack item = ev.getItem();
 		//Action
 		Action a = ev.getAction();
 		
 		if(!(a.equals(Action.LEFT_CLICK_BLOCK) || a.equals(Action.RIGHT_CLICK_BLOCK))) {return;}
 		
 		if(item == null) {return;}
 		
		if(CustomSpawners.selectMode.containsKey(p)) {
			boolean doBreak = CustomSpawners.selectMode.get(p);
			int configId = CONFIG.getInt("players.selectionId");
			
			if((p.getItemInHand().getTypeId() == configId) && doBreak && p.hasPermission("customspawners.spawners.pos")) {
				ev.setCancelled(true);
				return;
			}
			
		}
		
 		//Block
 		Location l = ev.getClickedBlock().getLocation();
 		//Perms
 		String perm = "customspawners.spawners.pos";
 		
 		if(p.hasPermission(perm)) {
			if(item.getTypeId() == CONFIG.getInt("players.selectionId")) {
 				if(a.equals(Action.LEFT_CLICK_BLOCK)) {
 					CustomSpawners.selectionPointOne.put(p, l);
 					p.sendMessage(ChatColor.GREEN + "Set spawn area selection point one to: " + ChatColor.GOLD + "(" +
 							l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + ")" + ChatColor.GREEN + ".");
 				} else if(a.equals(Action.RIGHT_CLICK_BLOCK)) {
 					CustomSpawners.selectionPointTwo.put(p, l);
 					p.sendMessage(ChatColor.GREEN + "Set spawn area selection point two to: " + ChatColor.GOLD + "(" +
 							l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ() + ")" + ChatColor.GREEN + ".");
 				}
 			}
 		}
 		
 	}
 	
 }

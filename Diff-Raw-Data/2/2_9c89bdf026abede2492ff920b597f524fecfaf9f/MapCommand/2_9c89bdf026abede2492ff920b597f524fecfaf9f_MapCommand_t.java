 package com.caindonaghey.commandbin.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.ItemMeta;
 import org.bukkit.map.MapRenderer;
 import org.bukkit.map.MapView;
 
 import com.caindonaghey.commandbin.MapText;
 import com.caindonaghey.commandbin.Phrases;
 
 public class MapCommand implements CommandExecutor {
 	
 	public static String mapText = "";
 	public static short newID = 0;
 	
 	public boolean onCommand(CommandSender s, Command c, String l, String [] args) {
 		if(l.equalsIgnoreCase("map")) {
 			if(!(s instanceof Player)) {
 				System.out.println(Phrases.get("no-console"));
 				return true;
 			}
 			
 			Player player = (Player) s;
 			
 			if(!player.hasPermission("CommandBin.map")) {
 				player.sendMessage(Phrases.get("no-permission"));
 				return true;
 			}
 			
 			if(args.length < 1) {
 				player.sendMessage(Phrases.get("invalid-arguments"));
 				return false;
 			}
 			
 			StringBuilder x = new StringBuilder();
 			for(int i = 0; i < args.length; i++) {
 				x.append(args[i] + " ");
 			}
 			mapText = x.toString().trim();
 			player.sendMessage("Done.");
 			
 
 		ItemStack map = new ItemStack(Material.MAP, 1);
 		MapView newMap = Bukkit.getServer().createMap(player.getWorld());
 		for(MapRenderer re : newMap.getRenderers()) {
 			newMap.removeRenderer(re);
 		}
 		newID = newMap.getId();
 		newMap.addRenderer(new MapText(x.toString().trim()));
		player.sendMessage(Phrases.prefix + "New Map ID: " + String.valueOf(newMap.getId()));
 		map.setDurability(newMap.getId());
 		ItemMeta m = map.getItemMeta();
 		m.setDisplayName("Written Map");
 		
 		List<String> lore = new ArrayList<String>();
 		lore.add(ChatColor.DARK_PURPLE + "Map was written by " + player.getName());
 		m.setLore(lore);
 		map.setItemMeta(m);
 		player.getInventory().addItem(map);
 		
 		}
 		return true;
 	}
 
 }

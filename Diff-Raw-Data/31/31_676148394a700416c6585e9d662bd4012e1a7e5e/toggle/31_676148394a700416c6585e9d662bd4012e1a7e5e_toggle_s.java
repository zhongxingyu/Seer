 package pro.homiecraft.Commands.resources;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class toggle {
 	public static void togglePluginState(Player player) {
 		if (toggleMap.ee.containsKey(player)) {
 			if (toggleMap.ee.get(player)) {
				toggleMap.ee.put(player, false);
 				player.sendMessage(ChatColor.AQUA + "Egg explosion disabled");
 			} else {
 				toggleMap.ee.put(player, true);
 				player.sendMessage(ChatColor.AQUA + "Egg explosion enabled");
 
 			}
 		} else {
 			toggleMap.ee.put(player, true);
 			player.sendMessage(ChatColor.AQUA + "Egg explosion enabled");
 		}
 
 	}
 }

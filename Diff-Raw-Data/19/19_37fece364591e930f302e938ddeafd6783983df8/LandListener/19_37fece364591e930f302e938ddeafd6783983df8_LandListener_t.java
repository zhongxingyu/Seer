 package me.rotzloch.listener;
 
 import me.rotzloch.Classes.Helper;
 import me.rotzloch.Classes.Land;
 
 import org.bukkit.ChatColor;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.SignChangeEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class LandListener  implements Listener{
 	@EventHandler
 	public void OnBlockPlace(SignChangeEvent event) {
 		try {
 			if (event.getLine(0).equalsIgnoreCase("[L-SELL]")
 					&& Integer.parseInt(event.getLine(1)) > 0) {
 				Player player = event.getPlayer();
 				Land land = new Land(player, event.getBlock().getLocation().getChunk().getX(),event.getBlock().getLocation().getChunk().getZ());
 				land.SellBySign(event);
 			}
 		} catch (Exception ex) {
 			Helper.LogError(ex.getMessage());
 		}
 	}
 	
 	@EventHandler
 	public void OnSignClick(PlayerInteractEvent event) {
 		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
 			if (event.getClickedBlock().getState() instanceof Sign) {
 				Sign sign = (Sign) event.getClickedBlock().getState();
 				if (sign.getLine(0).equalsIgnoreCase(
 						ChatColor.DARK_BLUE + "[L-SELL]")) {
 					String playername = sign.getLine(3);
 					String buyername = event.getPlayer().getName();
 					int sellprice = Integer.parseInt(sign.getLine(1));
 
 					Land land = new Land(event.getPlayer(), sign.getLocation().getChunk().getX(),sign.getLocation().getChunk().getZ());
 					land.ChangeOwner(event,buyername, playername, sellprice,event.getPlayer());
 				}
 			}
 		}
 	}
 }

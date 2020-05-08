 package me.cain.botulism;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerEggThrowEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.ItemStack;
 
 public class mainlistener extends PlayerListener{
 	Player player;
 	Server server;
 	public void onPlayerInteract(PlayerInteractEvent event, Location CainFool) {
 		Player player = event.getPlayer();
 		if(event.getMaterial() == Material.PORK || event.getMaterial() == Material.SUGAR || event.getMaterial() == Material.RAW_FISH) {
			if(event.getAction() == Action.RIGHT_CLICK_AIR) {
 				player.damage(7);
 				player.sendMessage(ChatColor.RED + "Eating that is bad for you!");
 				event.getPlayer().setItemInHand(new ItemStack(0,0));
 			}
 		}
 	}
 }

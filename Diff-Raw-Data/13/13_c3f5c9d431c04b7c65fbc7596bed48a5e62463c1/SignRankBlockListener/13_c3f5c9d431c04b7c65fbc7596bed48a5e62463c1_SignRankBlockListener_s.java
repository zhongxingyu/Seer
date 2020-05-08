 package me.coolblinger.signrank.listeners;
 
 import me.coolblinger.signrank.SignRank;
 import me.coolblinger.signrank.SignRankConfig;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockState;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.SignChangeEvent;
 
 public class SignRankBlockListener extends BlockListener {
 	SignRank plugin;
 
 	public SignRankBlockListener (SignRank instance) {
 		plugin = instance;
 	}
 
 	public void onSignChange (SignChangeEvent event) {
 		if (event.getLine(0).equals(plugin.signRankConfig.getString("signText"))) {
 			Player player = event.getPlayer();
			if (!player.hasPermission("signrank.build") || !plugin.permissions.has(player, "signrank.build")) {
 				player.sendMessage(ChatColor.RED + "You're not allowed to do this.");
 				event.getBlock().setType(Material.AIR);
 				event.setCancelled(true);
 			}
		} else {
			plugin.log.warning("'" + event.getLine(0) + "'");
			plugin.log.warning("'" + plugin.signRankConfig.getString("signText") + "'");
 		}
 	}
 
 	public void onBlockBreak(BlockBreakEvent event) {
 		Block eventBlock = event.getBlock();
 		BlockState eventBlockState = eventBlock.getState();
 		if (eventBlockState instanceof Sign) {
 			Sign eventSign = (Sign)eventBlockState;
 			if (eventSign.getLine(0).equals(plugin.signRankConfig.getString("signText"))) {
 				Player player = event.getPlayer();
				if (!player.hasPermission("signrank.build") || !plugin.permissions.has(player, "signrank.build")) {
 					player.sendMessage(ChatColor.RED + "You're not allowed to do this.");
 					event.setCancelled(true);
 				}
 			}
 		}
 	}
 }

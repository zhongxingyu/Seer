 package com.modcrafting.achievement.listener;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.block.Block;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.modcrafting.achievement.Achievement;
 
 public class AchieveBlockListener implements Listener {
 	public final static Logger log = Logger.getLogger("Minecraft");
 	Achievement plugin;
 
 	public AchieveBlockListener(Achievement instance) {
 		plugin = instance;
 	}
 	@EventHandler
 	public void onBlockBreak(BlockBreakEvent event) {
 		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
 		Player player = event.getPlayer();
 		boolean auth = false;
 		if (plugin.setupPermissions()){
 			if (plugin.permission.has(player, "achievement.blockbreak")) auth = true;
 		}else{
 			if (player.isOp()) auth = true; 
 		}
 		if (!auth) return;
 		Block block = event.getBlock();
 		String blockName = block.getType().name().toLowerCase();
 		plugin.db.registerBreak(player, block);
 		Integer breaks = plugin.db.getBlockBreaks(player, block);
 		String configTest = "Breaks." + blockName + "." + breaks;
 		Boolean achExists = plugin.reward.checkAchievement(configTest);
 		if(achExists) {
 			String msg = config.getString("Breaks." + blockName + "." + breaks + ".Message");
 			if(msg.length() > 26) {
 			plugin.interfaceSpout.sendAchievement(player, "Your msg > 26 chars", block.getType());
 				log.log(Level.INFO, "[Achievement] Your msg must be less than 26 characters!");
 			} else {
 			plugin.interfaceSpout.sendAchievement(player, msg, block.getType());
 			}
 			String reward = plugin.reward.reward(configTest);
 			if(reward.equals("none")) {
 				return;
 			}
 			if(reward.equals("money")) {
 				Integer amount = config.getInt(configTest + ".Reward.Money.Amount", 0);
 				plugin.reward.rewardMoney(player, amount);
 			}
 			if(reward.equals("item")) {
 				ItemStack item = plugin.reward.getItemReward(player, configTest);
 				PlayerInventory inv = player.getInventory();
 				inv.addItem(item);
 			}
 			if(reward.equals("both")) {
 				ItemStack item = plugin.reward.getItemReward(player, configTest);
 				PlayerInventory inv = player.getInventory();
 				inv.addItem(item);
 				Integer amount = config.getInt(configTest + ".Reward.Money.Amount", 0);
 				plugin.reward.rewardMoney(player, amount);
 			}
 		}
 	}
 	public void onBlockPlace(BlockPlaceEvent event) {
 		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
 		Player player = event.getPlayer();
 		boolean auth = false;
 		if (plugin.setupPermissions()){
 			if (plugin.permission.has(player, "achievement.blockplace")) auth = true;
 		}else{
 			if (player.isOp()) auth = true; 
 		}
 		if (!auth) return;
 		Block block = event.getBlock();
 		String blockName = block.getType().name().toLowerCase();
 		plugin.db.registerPlace(player, block);
 		Integer places = plugin.db.getBlockPlaces(player, block);
 		String configTest = "Places." + blockName + "." + places;
 		Boolean achExists = plugin.reward.checkAchievement(configTest);
 		if(achExists) {
 			String msg = config.getString("Places." + blockName + "." + places + ".Message");
 			if(msg.length() > 26) {
 			plugin.interfaceSpout.sendAchievement(player, "Your msg > 26 chars", block.getType());
 				log.log(Level.INFO,"[Achievement] Your msg must be less than 26 characters!");
 			} else {
 			plugin.interfaceSpout.sendAchievement(player, msg, block.getType());
 			}
 		}
 		String reward = plugin.reward.reward(configTest);
 		if(reward.equals("none")) {
 			return;
 		}
 		if(reward.equals("money")) {
 			Integer amount = config.getInt(configTest + ".Reward.Money.Amount", 0);
 			plugin.reward.rewardMoney(player, amount);
 		}
 		if(reward.equals("item")) {
 			ItemStack item = plugin.reward.getItemReward(player, configTest);
 			PlayerInventory inv = player.getInventory();
 			inv.addItem(item);
 			//player.updateInventory();
 		}
 		if(reward.equals("both")) {
 			ItemStack item = plugin.reward.getItemReward(player, configTest);
 			PlayerInventory inv = player.getInventory();
 			inv.addItem(item);
 			//player.updateInventory();
 			Integer amount = config.getInt(configTest + ".Reward.Money.Amount", 0);
 			plugin.reward.rewardMoney(player, amount);
 		}
 	}
 }

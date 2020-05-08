 package com.modcrafting.achievement.listener;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.getspout.spoutapi.event.inventory.InventoryCraftEvent;
 import com.modcrafting.achievement.Achievement;
 
 public class AchieveInventoryListener implements Listener {
 	
 	private Achievement plugin;
 	
 	public AchieveInventoryListener(Achievement plugin) {
         this.plugin = plugin;
     }
 	@EventHandler
 	public void onInventoryCraft(InventoryCraftEvent event) {
 		YamlConfiguration config = (YamlConfiguration) plugin.getConfig();
 		try {
 			Player player = event.getPlayer();
 			ItemStack item = event.getResult();
 			String name = item.getType().name().toLowerCase();
 			plugin.db.registerCraft(player, item);
 			Integer times = plugin.db.getCrafts(player, item);
 			String configTest = "Crafts." + name + "." + times;
 			Boolean achExists = plugin.reward.checkAchievement(configTest);
 			if(achExists) {
 				String msg = config.getString(configTest + ".Message");
 				if(msg.length() > 26) {
 					plugin.interfaceSpout.sendAchievement(player, "Msg > 26 Chars", Material.WORKBENCH);
 				} else {
 					plugin.interfaceSpout.sendAchievement(player, msg, Material.WORKBENCH);
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
 					ItemStack itemr = plugin.reward.getItemReward(player, configTest);
 					PlayerInventory inv = player.getInventory();
 					inv.addItem(itemr);
 				}
 				if(reward.equals("both")) {
 					ItemStack itemr = plugin.reward.getItemReward(player, configTest);
 					PlayerInventory inv = player.getInventory();
 					inv.addItem(itemr);
 					Integer amount = config.getInt(configTest + ".Reward.Money.Amount", 0);
 					plugin.reward.rewardMoney(player, amount);
 				}
 			}
 		} catch(Exception e) {
 
 		}
 	}
 }

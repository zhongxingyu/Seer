 package com.randude14.lotteryplus;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.serialization.ConfigurationSerialization;
 import org.bukkit.entity.Player;
 
 import com.randude14.lotteryplus.configuration.CustomYaml;
 import com.randude14.lotteryplus.lottery.ItemReward;
 import com.randude14.lotteryplus.lottery.Lottery;
 import com.randude14.lotteryplus.lottery.LotteryClaim;
 import com.randude14.lotteryplus.lottery.PickItemReward;
 import com.randude14.lotteryplus.lottery.PotReward;
 import com.randude14.lotteryplus.lottery.Reward;
 
 public class ClaimManager {
 	private static void saveClaims() {
 		FileConfiguration config = claimsConfig.getConfig();
 		config.createSection("claims"); // clean config
 		for(String player : claims.keySet()) {
 			int cntr = 0;
 			for(LotteryClaim claim : claims.get(player)) {
 				config.set("claims." + player + ".reward" + ++cntr, claim);
 			}
 		}
 		claimsConfig.saveConfig();
 	}
 	
 	public static void loadClaims() {
 		FileConfiguration config = claimsConfig.getConfig();
 		ConfigurationSection section = config.getConfigurationSection("claims");
 		if(section == null)
 			section = config.createSection("claims");
 		for(String player : section.getKeys(false)) {
 			List<LotteryClaim> playerClaims = new ArrayList<LotteryClaim>();
 			ConfigurationSection playerSection = section.getConfigurationSection(player);
 			if(playerSection == null)
 				continue;
 			for(String claimPath : playerSection.getKeys(false)) {
 				try {
 					playerClaims.add((LotteryClaim) playerSection.get(claimPath));
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 			claims.put(player, playerClaims);
 		}
 	}
 
 	public static void addClaim(String name, String lottery, List<Reward> rewards) {
 		if (!claims.containsKey(name))
 			claims.put(name, new ArrayList<LotteryClaim>());
		LotteryClaim claim = new LotteryClaim(lottery, rewards);
 		claims.get(name).add(claim);
 		saveClaims();
 	}
 	
 	public static void rewardClaims(Player player) {
 		List<LotteryClaim> playerClaims = claims.get(player.getName());
 		if(playerClaims != null && !playerClaims.isEmpty()) {
 			while(!playerClaims.isEmpty()) {
 				LotteryClaim claim = playerClaims.remove(0);
 				Lottery.handleRewards(claim.getRewards(), player);
 			}
 			saveClaims();
 		} else {
 			ChatUtils.error(player, "You do not have any rewards to claim.");
 		}
 	}
 	
 	public static void notifyOfClaims(Player player) {
 		List<LotteryClaim> playerClaims = claims.get(player.getName());
 		if(playerClaims != null && !playerClaims.isEmpty()) {
 			ChatUtils.send(player, ChatColor.YELLOW, "You have recently won some lotteries! Type '/lottery claim' to claim your rewards!");
 		}
 	} 
 
 	private static final Map<String, List<LotteryClaim>> claims = new HashMap<String, List<LotteryClaim>>();
 	private static final CustomYaml claimsConfig;
 	
 	static {
 		ConfigurationSerialization.registerClass(LotteryClaim.class);
 		ConfigurationSerialization.registerClass(ItemReward.class);
 		ConfigurationSerialization.registerClass(PotReward.class);
 		ConfigurationSerialization.registerClass(PickItemReward.class);
 		claimsConfig = new CustomYaml("claims.yml");
 	}
 }

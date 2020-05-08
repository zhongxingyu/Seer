 package com.vxnick.mobrewards;
 
 import java.util.HashMap;
 import java.util.logging.Level;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public final class MobRewards extends JavaPlugin implements Listener {
 	public static Economy econ = null;
 	public static Permission perms = null;
 	public static HashMap<String, HashMap<String, Integer>> mobKills = new HashMap<String, HashMap<String, Integer>>();
 	public static HashMap<String, HashMap<String, Object>> playerData = new HashMap<String, HashMap<String, Object>>();
 	
 	@Override
 	public void onEnable() {
 		if (!setupEconomy() ) {
 			getLogger().log(Level.SEVERE, "Vault not found. Disabling MobRewards");
 			return;
 		}
 		
 		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
 		perms = rsp.getProvider();
 		
 		saveDefaultConfig();
 		
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 	
 	@Override
 	public void onDisable() {
 
 	}
 	
 	private boolean setupEconomy() {
 		if (getServer().getPluginManager().getPlugin("Vault") == null) {
 			return false;
 		}
 		
 		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
 		
 		if (rsp == null) {
 			return false;
 		}
 		
 		econ = rsp.getProvider();
 		return econ != null;
 	}
 	
 	@EventHandler(priority = EventPriority.LOWEST)
 	public void onMobDeath(EntityDeathEvent event) {
 		LivingEntity mob = (LivingEntity) event.getEntity();
 		String mobType = mob.getType().toString();
 		
 		// Only run if this is a player kill
 		if (!(mob.getKiller() instanceof Player)) {
 			return;
 		}
 		
 		Player player = (Player) mob.getKiller();
 		
 		// Bail out if the player doesn't have the required permission
 		if (!perms.has(player, "mobrewards.reward")) {
 			return;
 		}
 		
 		// Clear previous mob kill count
 		if (!mobType.equals(getPlayerData(player.getName(), "last_mob_type"))) {
 			mobKills.remove(player.getName());
 			setMobKills(player.getName(), mobType, 0);
 			logMessage("Resetting kill limit for " + player.getName());
 		} else {
 			// Reset this if the player hasn't killed the same mob type for a while
 			int lastKillTime;
 			
 			if (getPlayerData(player.getName(), String.format("last_%s_kill_time", mobType)) == null) {
 				lastKillTime = (int) getUnixTime();
 			} else {
 				lastKillTime = ((Long) (getPlayerData(player.getName(), String.format("last_%s_kill_time", mobType)))).intValue();
 			}
 			
 			if ((int) getUnixTime() - lastKillTime > getConfig().getInt("global.kill_reset", 30)) {
 				mobKills.remove(player.getName());
 				setMobKills(player.getName(), mobType, 0);
 				logMessage(String.format("Auto resetting %s kill limit for %s", mobType, player.getName()));
 			}
 		}
 		
 		// Are rewards specified for this mob?
 		if (getConfig().getConfigurationSection(String.format("mobs.%s", mobType)) != null) {
 			int playerTypeKills;
 			int mobKillLimit = getConfig().getInt(String.format("mobs.%s.kill_limit", mobType), 
 					getConfig().getInt("global.kill_limit", 5));
 			boolean expDecrease = getConfig().getBoolean(String.format("mobs.%s.exp_decrease", mobType), 
 					getConfig().getBoolean("global.exp_decrease", false));
 			
 			// Set initial kill count or get the existing count
 			if (getMobKills(player.getName(), mobType) == null) {
 				playerTypeKills = 0;
 			} else {
 				playerTypeKills = getMobKills(player.getName(), mobType);
 			}
 			
 			// Does the player have kills left before reaching the limit?
			if (playerTypeKills < mobKillLimit || mobKillLimit == -1) {
 				double moneyAmount = getConfig().getDouble(String.format("mobs.%s.money", mobType), 0.0);
 				boolean moneyDecrease = getConfig().getBoolean(String.format("mobs.%s.money_decrease", mobType), 
 						getConfig().getBoolean("global.money_decrease", false));
 				int payExpAmount;
 				String rewardText = "";
 				
 				// Should money be given as a reward?
 				if (moneyAmount >= 1.0) {
 					double payMoneyAmount;
 					
 					// Calculate how much to pay the player
 					if (moneyDecrease && playerTypeKills > 0) {
 						payMoneyAmount = (moneyAmount / 2) / playerTypeKills;
 					} else {
 						payMoneyAmount = moneyAmount;
 					}
 					
 					// Pay the player
 					if (payMoneyAmount >= 0.0) {
 						rewardText += econ.format(payMoneyAmount);
 						
 						String rewardMessage = getConfig().getString("messages.reward");
 						rewardMessage = rewardMessage.replace("[REWARD]", rewardText);
 						rewardMessage = rewardMessage.replace("[MOB_TYPE]", mobType.toLowerCase().replaceAll("_", " "));
 						
 						econ.depositPlayer(player.getName(), payMoneyAmount);
 						player.sendMessage(ChatColor.GOLD + rewardMessage);
 					}
 				}
 				
 				// Calculate how much experience to drop
 				if (expDecrease && playerTypeKills > 0) {
 					payExpAmount = Math.round((event.getDroppedExp() / 2) / playerTypeKills);
 					event.setDroppedExp(payExpAmount);
 				} else {
 					payExpAmount = event.getDroppedExp();
 				}
 				
 				// Show a message if the player has hit the kill limit
 				if (playerTypeKills == (mobKillLimit - 1)) {
 					player.sendMessage(ChatColor.YELLOW + getConfig().getString("messages.limit"));
 				}
 				
 				setMobKills(player.getName(), mobType, playerTypeKills + 1);
 				
 				setPlayerData(player.getName(), "last_mob_type", mobType);
 				setPlayerData(player.getName(), String.format("last_%s_kill_time", mobType), getUnixTime());
 			} else {
 				// Cancel experience drops if the player has exceeded their kill limit
 				if (expDecrease) {
 					event.setDroppedExp(0);
 				}
 			}
 		}
 	}
 	
 	private Integer getMobKills(String player, String key) {
 		if (mobKills.get(player) == null) {
 			return null;
 		}
 		
 		return mobKills.get(player).get(key);
 	}
 	
 	private void setMobKills(String player, String key, Integer value) {
 		if (mobKills.get(player) == null) {
 			mobKills.put(player, new HashMap<String, Integer>());
 		}
 		
 		HashMap<String, Integer> data = mobKills.get(player);
 		data.put(key, value);
 		
 		mobKills.put(player, data);
 	}
 	
 	private Object getPlayerData(String player, String key) {
 		if (playerData.get(player) == null) {
 			return null;
 		}
 		
 		return playerData.get(player).get(key);
 	}
 	
 	private void setPlayerData(String player, String key, Object value) {
 		if (playerData.get(player) == null) {
 			playerData.put(player, new HashMap<String, Object>());
 		}
 		
 		HashMap<String, Object> data = playerData.get(player);
 		data.put(key, value);
 		
 		playerData.put(player, data);
 	}
 	
 	private void logMessage(String message) {
 		if (getConfig().getBoolean("log", false)) {
 			getLogger().log(Level.INFO, message);
 		}
 	}
 	
 	private long getUnixTime() {
 		return (System.currentTimeMillis() / 1000L);
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("mr")) {
 			String command;
 			
 			if (args.length > 0) {
 				command = args[0].toLowerCase();
 			} else {
 				return true;
 			}
 			
 			if (perms.has(sender, "mobrewards.admin.reload") && command.equals("reload")) {
 				sender.sendMessage(ChatColor.YELLOW + "Reloading configuration");
 				reloadConfig();
 				return true;
 			}
 		}
 		
 		return true;
 	}
 }

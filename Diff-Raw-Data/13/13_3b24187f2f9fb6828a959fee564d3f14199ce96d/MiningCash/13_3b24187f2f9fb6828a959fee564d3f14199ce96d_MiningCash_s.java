 /*
  * Copyright (c) 2012, tuxed
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 	* Redistributions of source code must retain the above copyright
  * 	notice, this list of conditions and the following disclaimer.
  * 	* Redistributions in binary form must reproduce the above copyright
  * 	notice, this list of conditions and the following disclaimer in the
  * 	documentation and/or other materials provided with the distribution.
  * 	* Neither the name of tuxed nor the
  *	names of its contributors may be used to endorse or promote products
  * 	derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL TUXED BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.tropicalwikis.tuxcraft.plugins.miningcash;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 import java.util.Random;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import org.bukkit.plugin.Plugin;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 import com.sk89q.worldguard.protection.ApplicableRegionSet;
 
 /**
  * @author tux
  * 
  */
 public class MiningCash extends JavaPlugin implements Listener {
 	private Economy econ;
 	private ArrayList<String> recentlyPlacedOres = new ArrayList<String>();
 	private HashMap<String, Double> rewardsHidden = new HashMap<String, Double>();
 	private Random rnd = new Random();
 	private Logger log = Logger.getLogger("Minecraft");
 	private WorldGuardPlugin wgplugin = null;
 	private double quotient = 0.0;
 	private double bonusQuotient = 1.0;
 
 	@Override
 	public void onEnable() {
 		if (!setupEconomy()) {
 			log.severe("Vault not found! Disabling plugin.");
 			getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
 		Plugin wgTmpPlugin = getServer().getPluginManager().getPlugin(
 				"WorldGuard");
 		if (wgTmpPlugin == null) {
 			log.info("WorldGuard not found. Region-specific quotients are disabled.");
 		} else {
 			wgplugin = (WorldGuardPlugin) wgTmpPlugin;
 		}
 		wgTmpPlugin = null;
 		this.getDataFolder().mkdirs();
 		getConfig().options().copyDefaults(true);
 		if (!getConfig().contains("reward-quotient")) {
 			quotient = rnd.nextDouble() * 2;
 			getConfig().set("reward-quotient", quotient);
 		} else {
 			quotient = getConfig().getDouble("reward-quotient", 1.0);
 		}
 		if (!getConfig().contains("block-rewards")) {
 			getConfig().set("block-rewards.16", 0.20);
 			getConfig().set("block-rewards.15", 0.45);
 			getConfig().set("block-rewards.14", 0.91);
 			getConfig().set("block-rewards.73", 0.60);
 			getConfig().set("block-rewards.74", 0.60);
 			getConfig().set("block-rewards.21", 0.80);
 			getConfig().set("block-rewards.56", 1.05);
 			getConfig().set("block-rewards.129", 1.20);
 		}
 		this.saveConfig();
 		getServer().getPluginManager().registerEvents(this, this);
 	}
 
 	@Override
 	public void onDisable() {
 		econ = null;
 		rnd = null;
 		log = null;
 		rewardsHidden = null;
 		recentlyPlacedOres = null;
 		wgplugin = null;
 	}
 
 	private double calculateOreReward(String b) {
 		String m = "block-rewards." + b;
 		// First, check using the old rules: type needs to be in config.yml
 		if (getConfig().isDouble(m)) {
 			if (getConfig().getBoolean("flat-pay", false)) {
 				return getConfig().getDouble(m);
 			}
 			return getConfig().getDouble(m)
 					* quotient
 					+ ((rnd.nextDouble() / 8 - (rnd.nextDouble() / 16)) % quotient)
 					* bonusQuotient;
 		}
 		return 0.00;
 	}
 
 	private String blockToString(Block b) {
 		int type = b.getTypeId();
 		int dmgid = b.getData();
 		String m = String.valueOf(type);
 		if (dmgid > 0) {
 			m += ":" + dmgid;
 		}
 		return m;
 	}
 
 	private double calculateOreReward(Block b) {
		return calculateOreReward(blockToString(b));
 	}
 
 	private String locationToString(Location l) {
 		return l.getWorld().getName() + ":" + l.getBlockX() + ","
 				+ l.getBlockY() + "," + l.getBlockZ();
 	}
 
 	private boolean minedBlockCanBeRewarded(String b) {
 		return getConfig().isDouble("block-rewards." + b);
 	}
 
 	private boolean minedBlockCanBeRewarded(Block b) {
		return minedBlockCanBeRewarded(blockToString(b));
 	}
 
 	@EventHandler(priority = EventPriority.MONITOR)
 	public boolean onBlockPlace(BlockPlaceEvent evt) {
 		if (evt.isCancelled()) {
 			return true;
 		}
 		if (!minedBlockCanBeRewarded(evt.getBlockPlaced())) {
 			return true;
 		}
 		if (!getConfig().getBoolean("ignore-riskiness", false)) {
 			recentlyPlacedOres.add(locationToString(evt.getBlock()
 					.getLocation()));
 		}
 		return true;
 	}
 
 	// MiningCash only monitors what happens in the end.
 	@EventHandler(priority = EventPriority.MONITOR)
 	public boolean onBlockBreak(BlockBreakEvent evt) {
 		if (evt.isCancelled()) {
 			return true;
 		}
 		if (!evt.getPlayer().hasPermission("miningcash.reward")) {
 			return true;
 		}
 
 		if (!minedBlockCanBeRewarded(evt.getBlock())) {
 			return true;
 		}
 
 		// Riskness checks
 		if (!getConfig().getBoolean("ignore-riskiness", false)) {
 			if (recentlyPlacedOres.contains(locationToString(evt.getBlock()
 					.getLocation()))) {
 				// just return here
 				recentlyPlacedOres.remove(locationToString(evt.getBlock()
 						.getLocation()));
 				return true;
 			}
 			if (evt.getPlayer().getGameMode() == org.bukkit.GameMode.CREATIVE) {
 				return true;
 			}
 		}
 		// Now calculate the reward.
 		double reward = calculateOreReward(evt.getBlock());
 		if (wgplugin != null) {
 			RegionManager regionManager = wgplugin.getRegionManager(evt
 					.getPlayer().getWorld());
 			if (regionManager != null) {
 				ApplicableRegionSet set = regionManager
 						.getApplicableRegions(evt.getBlock().getLocation());
 				for (ProtectedRegion region : set) {
 					if (getConfig().contains(
 							"per-region-quotient." + region.getId())) {
 						reward = reward
 								* getConfig()
 										.getDouble(
 												"per-region-quotient."
 														+ region.getId());
 					}
 				}
 			}
 		}
 
 		// Deposit the reward
 		econ.depositPlayer(evt.getPlayer().getName(), reward);
 
 		if (rewardsHidden.containsKey(evt.getPlayer().getName())) {
 			// Silently add to their count.
 			double r = rewardsHidden.get(evt.getPlayer().getName()) + reward;
 			rewardsHidden.remove(evt.getPlayer().getName());
 			rewardsHidden.put(evt.getPlayer().getName(), r);
 		} else {
 			evt.getPlayer().sendMessage(
 					ChatColor.GOLD
 							+ getConfig().getString("message-reward").replace(
 									"%amount%", econ.format(reward)));
 		}
 		return true;
 	}
 
 	private boolean setupEconomy() {
 		if (getServer().getPluginManager().getPlugin("Vault") == null) {
 			return false;
 		}
 		RegisteredServiceProvider<Economy> rsp = getServer()
 				.getServicesManager().getRegistration(Economy.class);
 		if (rsp == null) {
 			return false;
 		}
 		econ = rsp.getProvider();
 		return econ != null;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String commandLabel, String[] args) {
 		if (sender.hasPermission("miningcash.reward")
 				&& sender instanceof Player) {
 			// Reward message hide/show
 			try {
 				if (args[0].equals("hiderewards")) {
 					if (rewardsHidden.containsKey(sender.getName())) {
 						sender.sendMessage(ChatColor.RED
 								+ "[MiningCash] You are already hiding messages. Perhaps you wanted /mining showrewards?");
 						return true;
 					}
 					rewardsHidden.put(sender.getName(), 0.0);
 					sender.sendMessage(ChatColor.GOLD
 							+ "[MiningCash] Disabled reward messages. Use /mining showrewards to re-enable and display your total.");
 					return true;
 				}
 				if (args[0].equals("showrewards")) {
 					if (!rewardsHidden.containsKey(sender.getName())) {
 						sender.sendMessage(ChatColor.RED
 								+ "[MiningCash] You are not hiding messages. Perhaps you wanted /mining hiderewards?");
 						return true;
 					}
 					sender.sendMessage(ChatColor.GOLD
 							+ "[MiningCash] Enabled reward messages. You have mined "
 							+ econ.format(rewardsHidden.get(sender.getName()))
 							+ " of ore.");
 					rewardsHidden.remove(sender.getName());
 					return true;
 				}
 			} catch (ArrayIndexOutOfBoundsException e) {
 				// Sliently fallover to the miningcash admin code.
 			}
 		}
 		if (!sender.hasPermission("miningcash.admin")) {
 			sender.sendMessage("You do not have permission.");
 			return true;
 		}
 		try {
 			if (args[0].equals("estimatereward")) {
 				try {
 					if (minedBlockCanBeRewarded(args[1])) {
 						sender.sendMessage("Base value for block ID " + args[1]
 								+ ": "
 								+ econ.format(calculateOreReward(args[1])));
 					} else {
 						sender.sendMessage("Block ID " + args[1]
 								+ " not defined!");
 					}
 				} catch (ArrayIndexOutOfBoundsException e) {
 					sender.sendMessage("Usage: /mining estimatereward BlockID");
 				}
 			}
 			if (args[0].equals("settemporarybonus")) {
 				try {
 					bonusQuotient = Double.parseDouble(args[1]);
 					sender.sendMessage("Temporary bonus set.");
 				} catch (ArrayIndexOutOfBoundsException e) {
 					sender.sendMessage("Usage: /mining settemporarybonus Bonus.Value");
 				} catch (NumberFormatException e) {
 					sender.sendMessage("Invalid number!");
 				}
 			}
 			if (args[0].equals("setbasequotient")) {
 				try {
 					quotient = Double.parseDouble(args[1]);
 					getConfig().set("reward-quotient", quotient);
 					sender.sendMessage("Base quotient set.");
 					this.saveConfig();
 				} catch (ArrayIndexOutOfBoundsException e) {
 					sender.sendMessage("Usage: /mining setbasequotient Quotient.Value");
 				} catch (NumberFormatException e) {
 					sender.sendMessage("Invalid number!");
 				}
 			}
 			if (args[0].equals("setmineralquotient")) {
 				try {
 					int bid = Integer.parseInt(args[1]);
 					double quotient = Double.parseDouble(args[2]);
 					getConfig().set("block-rewards." + bid, quotient);
 					sender.sendMessage("Quotient of block ID " + bid + " set.");
 					this.saveConfig();
 				} catch (ArrayIndexOutOfBoundsException e) {
 					sender.sendMessage("Usage: /mining setmineralquotient BlockID Quotient.Value");
 				} catch (NumberFormatException e) {
 					sender.sendMessage("Invalid number!");
 				}
 			}
 			if (args[0].equals("reload")) {
 				reloadConfig();
 				sender.sendMessage("Reloaded MiningCash configuration.");
 			}
 		} catch (ArrayIndexOutOfBoundsException e) {
 			sender.sendMessage("/mining estimatereward/settemporarybonus/setbasequotient/setmineralquotient/reload");
 		}
 		return true;
 	}
 
 }

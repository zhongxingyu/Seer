 package net.mysticrealms.fireworks.scavengerhunt;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 public class ScavengerHunt extends JavaPlugin {
 
 	public boolean isRunning;
 	public Configuration config;
 	public List<ItemStack> currentItems = new ArrayList<ItemStack>();
 	public List<ItemStack> items = new ArrayList<ItemStack>();
 	public List<ItemStack> rewards = new ArrayList<ItemStack>();
 
 	public int numOfItems = 0;
 	public int duration = 0;
 	public double money = 0;
 	public long end = 0;
 
 	public static Permission permission = null;
 	public static Economy economy = null;
 
 	@Override
 	public void onEnable() {
 
 		setupEconomy();
 
 		if (!loadConfig()) {
 			this.getLogger().severe("Something is wrong with the config! Disabling!");
 			this.setEnabled(false);
 			return;
 		}
 		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new ScavengerInventory(this), 0, 40);
 
 	}
 
 	private boolean setupEconomy() {
 		if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
 			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 			if (economyProvider != null) {
 				economy = economyProvider.getProvider();
 			}
 
 			this.getLogger().info("Vault found and loaded.");
 			return (economy != null);
 		}
 		economy = null;
 		this.getLogger().info("Vault not found - money reward will not be used.");
 		return false;
 	}
 
 	public boolean loadConfig() {
 
 		this.reloadConfig();
 		items.clear();
 		rewards.clear();
 
 		if (!new File(this.getDataFolder(), "config.yml").exists()) {
 			this.saveDefaultConfig();
 		}
 		config = this.getConfig();
 
 		if (config.isDouble("money"))
 			money = config.getDouble("money");
 		else if (config.isInt("money"))
 			money = config.getInt("money");
 		else
 			return false;
 
 		if (config.isInt("duration"))
 			duration = config.getInt("duration");
 		else
 			return false;
 
 		if (config.isInt("numOfItems"))
 			numOfItems = config.getInt("numOfItems");
 		else
 			return false;
 
 		if (config.isList("items")) {
 			for (Object i : config.getStringList("items")) {
 				if (i instanceof String) {
 					final String[] parts = ((String) i).split(" ");
 					final int[] intParts = new int[parts.length];
 					for (int e = 0; e < parts.length; e++) {
 						try {
 							intParts[e] = Integer.parseInt(parts[e]);
 						} catch (final NumberFormatException exception) {
 							return false;
 						}
 					}
 					if (parts.length == 1) {
 						this.items.add(new ItemStack(intParts[0], 1));
 					} else if (parts.length == 2) {
 						this.items.add(new ItemStack(intParts[0], intParts[1]));
 					} else if (parts.length == 3) {
 						this.items.add(new ItemStack(intParts[0], intParts[1], (short) intParts[2]));
 					}
 				} else {
 					return false;
 				}
 			}
 		} else {
 			return false;
 		}
 
 		if (config.isList("rewards")) {
 			for (Object i : config.getStringList("rewards")) {
 				if (i instanceof String) {
 					final String[] parts = ((String) i).split(" ");
 					final int[] intParts = new int[parts.length];
 					for (int e = 0; e < parts.length; e++) {
 						try {
 							intParts[e] = Integer.parseInt(parts[e]);
 						} catch (final NumberFormatException exception) {
 							return false;
 						}
 					}
 					if (parts.length == 1) {
 						this.rewards.add(new ItemStack(intParts[0], 1));
 					} else if (parts.length == 2) {
 						this.rewards.add(new ItemStack(intParts[0], intParts[1]));
 					} else if (parts.length == 3) {
 						this.rewards.add(new ItemStack(intParts[0], intParts[1], (short) intParts[2]));
 					}
 				} else {
 					return false;
 				}
 			}
 		} else {
 			return false;
 		}
 		return true;
 	}
 
 	public boolean isUsingMoney() {
 		if (this.money > 0)
 			return true;
 		else
 			return false;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
 		if (cmd.getName().equalsIgnoreCase("scavengerstart"))
 			runScavengerEvent();
 		if (cmd.getName().equalsIgnoreCase("scavengerstop"))
 			stopScavengerEvent();
 		if (cmd.getName().equalsIgnoreCase("scavengeritems"))
 			listScavengerEventItems(sender);
 		if (cmd.getName().equalsIgnoreCase("scavengerrewards"))
 			listScavengerEventRewards(sender);
 		if (cmd.getName().equalsIgnoreCase("scavengerreload")) {
 			if (this.loadConfig())
 				sender.sendMessage(ChatColor.GOLD + "Config reloaded!");
 			else
 				sender.sendMessage(ChatColor.GOLD + "Config failed to reload!");
 		}
 
 		return true;
 	}
 
 	public void listScavengerEventItems(CommandSender sender) {
 		if (isRunning) {
 			sender.sendMessage(ChatColor.GOLD + "Current scavenger items: ");
			for (ItemStack i : items) {
 				sender.sendMessage(ChatColor.GOLD + configToString(i));
 			}
 		} else {
 			sender.sendMessage(ChatColor.GOLD + "No scavenger event is currently running.");
 		}
 	}
 
 	public void listScavengerEventRewards(CommandSender sender) {
 		sender.sendMessage(ChatColor.GOLD + "Current scavenger rewards: ");
 		for (ItemStack i : rewards) {
 			sender.sendMessage(ChatColor.GOLD + configToString(i));
 		}
 		if (this.isUsingMoney())
 			sender.sendMessage(ChatColor.GOLD + " * " + economy.format(money));
 
 	}
 
 	public void stopScavengerEvent() {
 		this.getServer().broadcastMessage(ChatColor.DARK_RED + "Scavenger Hunt has ended with no winner.");
 		isRunning = false;
 	}
 
 	public void runScavengerEvent() {
 
 		this.currentItems.clear();
 
 		List<ItemStack> clone = new ArrayList<ItemStack>();
 
 		for (ItemStack i : items) {
 			clone.add(i);
 		}
 
 		Random r = new Random();
 
 		if (numOfItems <= 0) {
 			currentItems = clone;
 		} else {
 			for (int i = 0; i < numOfItems && !clone.isEmpty(); i++) {
 				currentItems.add(clone.remove(r.nextInt(clone.size())));
 			}
 		}
 
 		this.getServer().broadcastMessage(ChatColor.DARK_RED + "Scavenger Hunt is starting! Good luck!");
 		if (duration != 0) {
 			this.getServer().broadcastMessage(ChatColor.DARK_RED + "You have: " + ChatColor.GOLD + duration + " seconds!");
 		}
 		this.getServer().broadcastMessage(ChatColor.DARK_RED + "You need to collect: ");
 		for (ItemStack i : currentItems) {
 			this.getServer().broadcastMessage(ChatColor.GOLD + configToString(i));
 		}
 		isRunning = true;
 
 		if (duration == 0) {
 			end = 0;
 		} else {
 			end = duration * 1000 + System.currentTimeMillis();
 		}
 	}
 
 	public String configToString(ItemStack item) {
 
 		return " * " + item.getAmount() + " " + item.getType().toString().toLowerCase().replace("_", " ");
 	}
 }

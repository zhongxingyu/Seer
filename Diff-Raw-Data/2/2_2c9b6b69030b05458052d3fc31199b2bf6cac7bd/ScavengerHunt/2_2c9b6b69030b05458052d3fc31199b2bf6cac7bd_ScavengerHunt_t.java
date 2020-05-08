 package net.mysticrealms.fireworks.scavengerhunt;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.Dye;
 import org.bukkit.material.Wool;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ScavengerHunt extends JavaPlugin {
 	
 	public static Economy economy = null;
 	public static Permission permission = null;
 	public Configuration config;
 	public List<ItemStack> currentItems = new ArrayList<ItemStack>();
 	public Map<EntityType, Integer> currentMobs = new HashMap<EntityType, Integer>();
 	public int duration = 0;
 	public long end = 0;
 	public boolean isRunning;
 	public List<ItemStack> items = new ArrayList<ItemStack>();
 	public Map<EntityType, Integer> mobs = new HashMap<EntityType, Integer>();
 	public double money = 0;
 	public int numOfItems = 0;
 	public Map<String, Map<EntityType, Integer>> playerMobs = new ConcurrentHashMap<String, Map<EntityType, Integer>>();
 	public List<ItemStack> rewards = new ArrayList<ItemStack>();
 	public int numOfMobs;
 	
 	public String configToString(ItemStack item, int current) {
 		return " * " + ((current != -1) ? current + "/" : "") + item.getAmount() + " " + itemFormatter(item).toLowerCase().replace("_", " ");
 	}
 	
 	public String configToString(ItemStack item) {
 		return configToString(item, -1);
 	}
 	
 	public synchronized Map<EntityType, Integer> getMap(String s) {
 		Map<EntityType, Integer> map = playerMobs.get(s);
 		if (map == null) {
 			map = new ConcurrentHashMap<EntityType, Integer>();
 			for (EntityType e : EntityType.values()) {
 				map.put(e, 0);
 			}
 			playerMobs.put(s, map);
 		}
 		return map;
 	}
 	
 	public boolean isUsingMoney() {
 		if (money > 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public String itemFormatter(ItemStack item) {
 		if (item.getType() == Material.WOOL) {
 			return ((Wool) item.getData()).getColor().toString() + " wool";
 		} else if (item.getType() == Material.INK_SACK) {
 			return ((Dye) item.getData()).getColor().toString() + " dye";
 		} else {
 			return item.getType().toString();
 		}
 	}
 	
 	public void listHelp(CommandSender sender) {
 		sender.sendMessage(ChatColor.DARK_RED + "== Scavenger Help Guide ==");
 		sender.sendMessage(ChatColor.GOLD + " * /scavengerItems - List items/objectives for current scavenger event.");
 		sender.sendMessage(ChatColor.GOLD + " * /scavengerRewards - List rewards for the winner.");
 		sender.sendMessage(ChatColor.DARK_GREEN + " * /scavengerStart - Start a scavenger event.");
 		sender.sendMessage(ChatColor.DARK_GREEN + " * /scavengerStop - End current scavenger vent.");
 		sender.sendMessage(ChatColor.DARK_GREEN + " * /scavengerReload - Reload the config.");
 	}
 	
 	public void listScavengerEventItems(CommandSender sender) {
 		if (!(sender instanceof Player)) {
 			return;
 		}
 		Player p = (Player) sender;
 		if (isRunning) {
 			if (!currentItems.isEmpty()) {
 				sender.sendMessage(ChatColor.DARK_RED + "Current scavenger items: ");
 				for (ItemStack i : currentItems) {
 					sender.sendMessage(ChatColor.GOLD + configToString(i, count(p.getInventory(), i)));
 				}
 			}
 			if (!currentMobs.isEmpty()) {
 				sender.sendMessage(ChatColor.DARK_RED + "You need to kill: ");
 				Map<EntityType, Integer> status = getMap(sender.getName());
 				for (Map.Entry<EntityType, Integer> entry : currentMobs.entrySet()) {
 					sender.sendMessage(ChatColor.GOLD + " * " + status.get(entry.getKey()) + "/" + entry.getValue() + " " + entry.getKey().getName().toLowerCase().replace("_", " "));
 				}
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
 		if (isUsingMoney()) {
 			sender.sendMessage(ChatColor.GOLD + " * " + economy.format(money));
 		}
 	}
 	
 	public boolean loadConfig() {
 		reloadConfig();
 		items.clear();
 		rewards.clear();
 		mobs.clear();
 		if (!new File(getDataFolder(), "config.yml").exists()) {
 			saveDefaultConfig();
 		}
 		config = getConfig();
 		if (config.isList("mobs")) {
 			for (Object i : config.getList("mobs", new ArrayList<String>())) {
 				try {
 					final String[] parts = i.toString().split(" ");
 					final int mobQuantity = Integer.parseInt(parts[1]);
 					final EntityType mobName = EntityType.fromName(parts[0]);
 					mobs.put(mobName, mobQuantity);
 				} catch (Exception e) {
 					return false;
 				}
 			}
 		}
 		if (config.isDouble("money")) {
 			money = config.getDouble("money");
 		} else if (config.isInt("money")) {
 			money = config.getInt("money");
 		} else {
 			return false;
 		}
 		if (config.isInt("duration")) {
 			duration = config.getInt("duration");
 		} else {
 			return false;
 		}
 		if (config.isInt("numOfItems")) {
 			numOfItems = config.getInt("numOfItems");
 		} else {
 			return false;
 		}
 		if (config.isInt("numOfMobs")) {
 			numOfMobs = config.getInt("numOfMobs");
 		} else {
 			return false;
 		}
 		if (config.isList("items")) {
 			for (Object i : config.getList("items", new ArrayList<String>())) {
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
 						items.add(new ItemStack(intParts[0], 1));
 					} else if (parts.length == 2) {
 						items.add(new ItemStack(intParts[0], intParts[1]));
 					} else if (parts.length == 3) {
 						items.add(new ItemStack(intParts[0], intParts[1], (short) intParts[2]));
 					}
 				} else {
 					return false;
 				}
 			}
 		} else {
 			return false;
 		}
 		if (config.isList("rewards")) {
 			for (Object i : config.getList("rewards", new ArrayList<String>())) {
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
 						rewards.add(new ItemStack(intParts[0], 1));
 					} else if (parts.length == 2) {
 						rewards.add(new ItemStack(intParts[0], intParts[1]));
 					} else if (parts.length == 3) {
 						rewards.add(new ItemStack(intParts[0], intParts[1], (short) intParts[2]));
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
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (cmd.getName().equalsIgnoreCase("scavengerstart")) {
 			runScavengerEvent();
 		}
 		if (cmd.getName().equalsIgnoreCase("scavengerstop")) {
 			stopScavengerEvent();
 		}
 		if (cmd.getName().equalsIgnoreCase("scavengeritems")) {
 			listScavengerEventItems(sender);
 		}
 		if (cmd.getName().equalsIgnoreCase("scavengerrewards")) {
 			listScavengerEventRewards(sender);
 		}
 		if (cmd.getName().equalsIgnoreCase("scavengerhelp")) {
 			listHelp(sender);
 		}
 		if (cmd.getName().equalsIgnoreCase("scavengerreload")) {
 			if (loadConfig()) {
 				sender.sendMessage(ChatColor.GOLD + "Config reloaded!");
 			} else {
 				sender.sendMessage(ChatColor.GOLD + "Config failed to reload!");
 			}
 		}
 		return true;
 	}
 	
 	@Override
 	public void onEnable() {
 		startMetrics();
 		setupEconomy();
 		if (!loadConfig()) {
 			getLogger().severe("Something is wrong with the config! Disabling!");
 			setEnabled(false);
 			return;
 		}
 		getServer().getScheduler().scheduleSyncRepeatingTask(this, new ScavengerInventory(this), 0, 40);
 		getServer().getPluginManager().registerEvents(new ScavengerListener(this), this);
 	}
 	
 	public int count(Inventory inv, ItemStack item) {
 		int count = 0;
 		for (ItemStack check : inv.getContents()) {
 			if (check != null && check.getType() == item.getType() && check.getData().equals(item.getData())) {
 				count += check.getAmount();
 			}
 		}
 		return count;
 	}
 	
 	public void runScavengerEvent() {
 		currentItems.clear();
 		playerMobs.clear();
 		currentMobs.clear();
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
 		List<Map.Entry<EntityType, Integer>> mobClone = new ArrayList<Map.Entry<EntityType, Integer>>(mobs.entrySet());
 		for (int i = 0; (numOfMobs <= 0 || i < numOfMobs) && !mobClone.isEmpty(); i++) {
 			Map.Entry<EntityType, Integer> entry = mobClone.remove(r.nextInt(mobClone.size()));
 			currentMobs.put(entry.getKey(), entry.getValue());
 		}
 		getServer().broadcastMessage(ChatColor.DARK_RED + "Scavenger Hunt is starting! Good luck!");
		if (duration > 0) {
 			getServer().broadcastMessage(ChatColor.DARK_RED + "You have: " + ChatColor.GOLD + duration + " seconds!");
 		}
 		if (!currentItems.isEmpty()) {
 			getServer().broadcastMessage(ChatColor.DARK_RED + "You need to collect: ");
 			for (ItemStack i : currentItems) {
 				getServer().broadcastMessage(ChatColor.GOLD + configToString(i));
 			}
 		}
 		if (!currentMobs.isEmpty()) {
 			getServer().broadcastMessage(ChatColor.DARK_RED + "You need to kill: ");
 			for (Map.Entry<EntityType, Integer> entry : currentMobs.entrySet()) {
 				getServer().broadcastMessage(ChatColor.GOLD + " * " + entry.getValue() + " " + entry.getKey().getName().toLowerCase().replace("_", " "));
 			}
 		}
 		isRunning = true;
 		if (duration == 0) {
 			end = 0;
 		} else {
 			end = duration * 1000 + System.currentTimeMillis();
 		}
 	}
 	
 	private boolean setupEconomy() {
 		if (getServer().getPluginManager().getPlugin("Vault") != null) {
 			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 			if (economyProvider != null) {
 				economy = economyProvider.getProvider();
 			}
 			getLogger().info("Vault found and loaded.");
 			return economy != null;
 		}
 		economy = null;
 		getLogger().info("Vault not found - money reward will not be used.");
 		return false;
 	}
 	
 	public void startMetrics() {
 		try {
 			new MetricsLite(this).start();
 		} catch (IOException e) {
 			getLogger().warning("MetricsLite did not enable! Statistic usage disabled.");
 			e.printStackTrace();
 			return;
 		}
 	}
 	
 	public void stopScavengerEvent() {
 		getServer().broadcastMessage(ChatColor.DARK_RED + "Scavenger Hunt has ended with no winner.");
 		isRunning = false;
 	}
 }

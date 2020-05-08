 package net.mysticrealms.fireworks.scavengerhunt;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ScavengerHunt extends JavaPlugin {
 
     public boolean isRunning;
     public Configuration config;
     public List<ItemStack> items = new ArrayList<ItemStack>();
     public List<ItemStack> rewards = new ArrayList<ItemStack>();
 
     @Override
     public void onEnable() {
 
 	if(!loadConfig()){
 	    this.getLogger().severe("Something is wrong with the config! Disabling!");
 	    this.setEnabled(false);
 	    return;
 	}
 	this.getServer()
 		.getScheduler()
 		.scheduleSyncRepeatingTask(this, new ScavengerInventory(this),
 			0, 40);
 
     }
 
     public boolean loadConfig() {
 	
 	items.clear();
 	rewards.clear();
 	
 	if (!new File(this.getDataFolder(), "config.yml").exists()) {
 	    this.saveDefaultConfig();
 	}
 	config = this.getConfig();
 
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
 			this.items.add(new ItemStack(intParts[0], intParts[1],
 				(short) intParts[2]));
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
 			this.rewards
 				.add(new ItemStack(intParts[0], intParts[1]));
 		    } else if (parts.length == 3) {
 			this.rewards.add(new ItemStack(intParts[0],
 				intParts[1], (short) intParts[2]));
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
     public boolean onCommand(CommandSender sender, Command cmd,
 	    String commandLabel, String[] args) {
 
 	if (cmd.getName().equalsIgnoreCase("scavengerstart"))
 	    runScavengerEvent();
 	if (cmd.getName().equalsIgnoreCase("scavengerstop"))
 	    stopScavengerEvent();
 	if (cmd.getName().equalsIgnoreCase("scavengeritems"))
 	    listScavengerEventItems(sender);
 	if (cmd.getName().equalsIgnoreCase("scavengerrewards"))
 	    listScavengerEventRewards(sender);
 	if (cmd.getName().equalsIgnoreCase("scavengerreload")){
 	    if(this.loadConfig())
 		sender.sendMessage(ChatColor.GOLD + "Config reloaded!");
 	    else
 		sender.sendMessage(ChatColor.GOLD + "Config failed to reload!");
 	}
 
 	return true;
     }
 
     public void listScavengerEventItems(CommandSender sender) {
 	sender.sendMessage(ChatColor.GOLD + "Current scavenger items: ");
 	for (ItemStack i : items) {
 	    sender.sendMessage(ChatColor.GOLD + configToString(i));
 	}
 
     }
 
     public void listScavengerEventRewards(CommandSender sender) {
 	sender.sendMessage(ChatColor.GOLD + "Current scavenger rewards: ");
 	for (ItemStack i : rewards) {
 	    sender.sendMessage(ChatColor.GOLD + configToString(i));
 	}
 
     }
 
     public void stopScavengerEvent() {
 	this.getServer()
 		.broadcastMessage(
 			ChatColor.DARK_RED
 				+ "Scavenger Hunt has ended with no winner.");
 	isRunning = false;
     }
 
     public void runScavengerEvent() {
 	this.getServer().broadcastMessage(
 		ChatColor.DARK_RED + "Scavenger Hunt is starting! Good luck!");
 	this.getServer().broadcastMessage(
 		ChatColor.DARK_RED + "You need to collect: ");
 	for (ItemStack i : items) {
 	    this.getServer().broadcastMessage(
 		    ChatColor.GOLD + configToString(i));
 	}
 	isRunning = true;
     }
 
     public String configToString(ItemStack item) {
 
 	return " * " + item.getAmount() + " "
 		+ item.getType().toString().toLowerCase().replace("_", " ");
     }
 }

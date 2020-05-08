 package com.BeastsMC;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.ItemFrame;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ItemFilter extends JavaPlugin {
 	public Logger log;
 	public List<Integer> bannedItemIds = new ArrayList<Integer>();
 	public Map<Integer, Enchantment> bannedItemIdsWithEnchants = new HashMap<Integer, Enchantment>();
 	public Material replaceItemFrameItem;
 	public void onEnable() {
 		saveDefaultConfig();
 		log = this.getLogger();
 		getServer().getPluginManager().registerEvents(new IFPlayerListener(this), this);
 		loadConfig();
 	}
 	private void loadConfig() {
		FileConfiguration fConf = getConfig();
		try {
			fConf.load(new File(getDataFolder(), "config.yml"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
 		List<String> unparsedBannedItemIds = fConf.getStringList("banned-items");
 		for(String entry : unparsedBannedItemIds) {
 			try {
 				Integer itemid = Integer.parseInt(entry);
 				bannedItemIds.add(itemid);
 			} catch(NumberFormatException ex) {
 				Integer itemid = Integer.parseInt(entry.split(":")[0]);
 				Enchantment enchant = Enchantment.getByName(entry.split(":")[1]);
 				if(enchant!=null) {
 					bannedItemIdsWithEnchants.put(itemid, enchant);
 				} else {
 					log.warning(entry + " is not a valid itemid/enchantment!");
 				}
 			}
 		}
 		Integer replaceID = fConf.getInt("replace.itemframe");
 		replaceItemFrameItem = Material.getMaterial(replaceID);
 	}
 	public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
 		if(args.length!=1) {
 			return false;
 		}
 		if(args[0].equalsIgnoreCase("reload")) {
 			sender.sendMessage(ChatColor.AQUA + "[ItemFilter] Reloading...");
 			bannedItemIds = new ArrayList<Integer>();
 			bannedItemIdsWithEnchants = new HashMap<Integer, Enchantment>();
 			loadConfig();
 			sender.sendMessage(ChatColor.AQUA + "[ItemFilter] Done reloading");
 			return true;
 		} else if(args[0].equalsIgnoreCase("print")) {
 			sender.sendMessage(ChatColor.AQUA + "[ItemFilter] The following is a list of items that will be filtered:");
 			sender.sendMessage(bannedItemIds.toString());
 			sender.sendMessage(bannedItemIdsWithEnchants.toString());
 			return true;
 		} else if(args[0].equalsIgnoreCase("purge")) {
 			sender.sendMessage(ChatColor.AQUA + "[ItemFilter] Searching all loaded ItemFrames for banned blocks.");
 			for(World world : getServer().getWorlds()) {
 				for(ItemFrame itemframe : world.getEntitiesByClass(ItemFrame.class)) {
 					if(bannedItemIds.contains(itemframe.getItem().getTypeId())) {
 						itemframe.setItem(new ItemStack(replaceItemFrameItem));
 					}
 				}
 			}
 			sender.sendMessage(ChatColor.AQUA + "[ItemFilter] Done purging");
 			return true;
 		}
 		return false;
 	}
 
 }

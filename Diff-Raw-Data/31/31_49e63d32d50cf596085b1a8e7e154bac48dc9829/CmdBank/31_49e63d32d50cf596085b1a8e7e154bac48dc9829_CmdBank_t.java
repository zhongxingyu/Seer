 package me.bluejelly.main.commands;
 
 import java.io.File;
 import java.io.IOException;
 
 import me.bluejelly.main.GuildZ;
 import me.bluejelly.main.configs.GuildConfig;
 import me.bluejelly.main.configs.PlayerConfig;
 import me.bluejelly.main.getters.Utf8YamlConfiguration;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class CmdBank {
 	
	private static GuildZ main;
	
	public CmdBank(GuildZ plugin){
        main = plugin;
    }
 	
 	public static void perform(Player player) {
 		
 		if(!PlayerConfig.config.contains(player.getName())) {
 			player.sendMessage(ChatColor.RED + "You do not have any guild.");
 			return;
 		}
 		
 		int guildID = -1;
 		String guildName = null;
 		
 		guildID = PlayerConfig.config.getInt(player.getName() + ".guildID");
 		guildName = GuildConfig.config.getString(guildID + ".name");
 		
 		Inventory inv = Bukkit.getServer().createInventory(null, 27);
		File guildBank = new File(main.getDataFolder() + File.separator + "banks", guildName+".yml");
		
		if(guildBank.exists()) {
			try {
				inv = loadFromYaml(guildBank);
			} catch (IOException e) {
				player.sendMessage(ChatColor.RED + "Could not load your guild bank. (IOException)");
				return;
			} catch (InvalidConfigurationException e) {
				player.sendMessage(ChatColor.RED + "Could not load your guild bank. (InvalidConfigurationException)");
				return;
			}
		}
 		
		player.openInventory(inv);
 		
 	}
 	
 	public static Inventory loadFromYaml(File file) throws IOException, InvalidConfigurationException {
 		YamlConfiguration yaml = new Utf8YamlConfiguration();
 		yaml.load(file);
 
 		int inventorySize = yaml.getInt("size", 6 * 9);
 		Inventory inventory = Bukkit.getServer().createInventory(null, inventorySize);
 
 		ConfigurationSection items = yaml.getConfigurationSection("items");
 		for (int slot = 0; slot < inventorySize; slot++) {
 			String slotString = String.valueOf(slot);
 			if (items.isItemStack(slotString)) {
 				ItemStack itemStack = items.getItemStack(slotString);
 				inventory.setItem(slot, itemStack);
 			}
 		}
 
 		return inventory;
 	}
 	
 	public static void saveToYaml(Inventory inventory, File file) throws IOException {
 		YamlConfiguration yaml = new Utf8YamlConfiguration();
 
 		int inventorySize = inventory.getSize();
 		yaml.set("size", inventorySize);
 
 		ConfigurationSection items = yaml.createSection("items");
 		for (int slot = 0; slot < inventorySize; slot++) {
 			ItemStack stack = inventory.getItem(slot);
 			if (stack != null) {
 				items.set(String.valueOf(slot), stack);
 			}
 		}
 
 		yaml.save(file);
 	}
 	
 }

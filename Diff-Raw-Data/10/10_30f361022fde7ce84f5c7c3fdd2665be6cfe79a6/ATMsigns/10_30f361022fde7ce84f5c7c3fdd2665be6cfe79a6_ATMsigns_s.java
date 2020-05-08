 package me.ellbristow.ATMsigns;
 
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ATMsigns extends JavaPlugin {
 	
 	public static ATMsigns plugin;
 	
 	public final Logger logger = Logger.getLogger("Minecraft");
 	public final signListener blockListener = new signListener(this);
 	protected FileConfiguration config;
 	public static int item;
 	public static int currency;
 	public static int altItem1;
 	public static int altItem1Curr;
 	public static int altItem2;
 	public static int altItem2Curr;
 	public static Economy economy;
 	public final playerListener playerListener = new playerListener(this, economy);
 
 	@Override
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		logger.info( "[" + pdfFile.getName() + "] is now disabled.");	
 	}
 
 	@Override
 	public void onEnable() {
 		PluginDescriptionFile pdfFile = this.getDescription();
 		PluginManager pm = getServer().getPluginManager();
 		logger.info("[" + pdfFile.getName() + "] version " + pdfFile.getVersion() + " is now enabled." );
 		if (initEconomy() && economy != null) {
 			logger.info("[" + pdfFile.getName() + "] hooked in to Vault." );
 			pm.registerEvent(Event.Type.SIGN_CHANGE, this.blockListener, Event.Priority.Normal, this);
 			pm.registerEvent(Event.Type.PLAYER_INTERACT, this.playerListener, Event.Priority.Normal, this);
 			pm.registerEvent(Event.Type.BLOCK_BREAK, this.blockListener, Event.Priority.Normal, this);
 			this.config = this.getConfig();
 			item = this.config.getInt("item", 266);
 			currency = this.config.getInt("currency", 1);
 			altItem1 = this.config.getInt("alt_item1", 999);
 			altItem1Curr = this.config.getInt("alt_item1_curr", 0);
 			altItem2 = this.config.getInt("alt_item2", 999);
 			altItem2Curr = this.config.getInt("alt_item2_curr", 0);
 			this.config.set("item", item);
 			this.config.set("currency", currency);
 			this.config.set("alt_item1", altItem1);
 			this.config.set("alt_item1_curr", altItem1Curr);
 			this.config.set("alt_item2", altItem2);
 			this.config.set("alt_item2_curr", altItem2Curr);
 			this.saveConfig();
 			Material checkItem = null;
 			boolean checkFailed = false;
 			checkItem = Material.getMaterial(item); 
 			if (checkItem == null) {
 				logger.severe("Item ID " + item + " not found!");
 				logger.severe("[" + pdfFile.getName() + "] will be disabled");
 				checkFailed = true;
 			}
 			if (altItem1 != 999) {
 				checkItem = Material.getMaterial(altItem1); 
 				if (checkItem == null) {
 					logger.severe("Item ID " + altItem1 + " not found!");
 					logger.severe("[" + pdfFile.getName() + "] will be disabled");
 					checkFailed = true;
 				}
 			}
 			if (altItem2 != 999) {
 				checkItem = Material.getMaterial(altItem2); 
 				if (checkItem == null) {
 					logger.severe("Item ID " + altItem2 + " not found!");
 					logger.severe("[" + pdfFile.getName() + "] will be disabled");
 					checkFailed = true;
 				}
 			}
 			if (checkFailed) {
 				pm.disablePlugin(this);
 			}
 		}
 		else {
 			logger.severe("[" + pdfFile.getName() + "] failed to link to Vault." );
 			pm.disablePlugin(this);
 		}
 	}
 	
 	public boolean initEconomy() {
 		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null) {
 			economy = economyProvider.getProvider();
 			return true;
 		}
 		return false;
 	}
 	
 	public static void depositItem(Player player) {
 		if (player.getItemInHand().getTypeId() == item || player.getItemInHand().getTypeId() == altItem1 || player.getItemInHand().getTypeId() == altItem2) {
 			// Correct item in hand
 			int count = player.getItemInHand().getAmount();
 			player.setItemInHand(new ItemStack(Material.AIR, 0));
			if (player.getItemInHand().getTypeId() == item) {
 				economy.depositPlayer( player.getName(), currency * count );
 				player.sendMessage(ChatColor.GREEN + "Deposited: " + ChatColor.GOLD + count + " " + Material.getMaterial(item).toString().replace("_", " ") + "(s)" + ChatColor.GREEN + " for " + ChatColor.GOLD + economy.format((double) currency * count).replace(".00", ""));
 			}
			else if (player.getItemInHand().getTypeId() == altItem1) {
 				economy.depositPlayer( player.getName(), altItem1Curr * count );
 				player.sendMessage(ChatColor.GREEN + "Deposited: " + ChatColor.GOLD + count + " " + Material.getMaterial(altItem1).toString().replace("_", " ") + "(s)" + ChatColor.GREEN + " for " + ChatColor.GOLD + economy.format((double) altItem1Curr * count).replace(".00", ""));
 			}
			else if (player.getItemInHand().getTypeId() == altItem2) {
 				economy.depositPlayer( player.getName(), altItem2Curr * count );
 				player.sendMessage(ChatColor.GREEN + "Deposited: " + ChatColor.GOLD + count + " " + Material.getMaterial(altItem2).toString().replace("_", " ") + "(s)" + ChatColor.GREEN + " for " + ChatColor.GOLD + economy.format((double) altItem2Curr * count).replace(".00", ""));
 			}
 		}
 		else {
 			// Wrong item in hand
 			String alternatives = "";
 			if (altItem1 != 999) {
 				if (altItem2 != 999) {
 					alternatives += ", ";
 				}
 				else {
 					alternatives += " or ";
 				}
 				alternatives += Material.getMaterial(altItem1).toString().replace("_", " ");
 			}
 			if (altItem2 != 999) {
 				alternatives += " or " + Material.getMaterial(altItem1).toString().replace("_", " ");
 			}
 			player.sendMessage(ChatColor.RED + "You can only deposit " +  ChatColor.WHITE + Material.getMaterial(item).toString().replace("_", " ") + alternatives + ChatColor.RED + "s!");
 		}
 	}
 	
 	public static void withdrawItem(Player player) {
 		if (economy.has(player.getName(), currency)) {
 			// Enough in account
 			if (player.getItemInHand().getTypeId() == 0 || (player.getItemInHand().getTypeId() == item && player.getItemInHand().getAmount() < player.getItemInHand().getType().getMaxStackSize())) {
 				player.setItemInHand(new ItemStack(item, player.getItemInHand().getAmount() + 1));
 				economy.withdrawPlayer( player.getName(), currency );
 				player.sendMessage(ChatColor.GREEN + "Withdrawn: " + ChatColor.GOLD + "1 " + Material.getMaterial(item).toString().replace("_"," ") + ChatColor.GREEN + " for " + ChatColor.GOLD + economy.format((double)currency).replace(".00", ""));
 			}
 			else {
 				player.sendMessage(ChatColor.RED + "You must have a free hand to withdraw " + Material.getMaterial(item).name().toString().replace("_", " ") + "s!");
 			}
 		}
 		else {
 			// Not enough in account
 			player.sendMessage(ChatColor.RED + "Your balance is too low to withdraw a " +  ChatColor.WHITE + Material.getMaterial(item).toString().replace("_", " ") + ChatColor.RED + "!");
 		}
 	}
 	
 	public static void getBalance(Player player) {
 		player.sendMessage(ChatColor.GREEN + "Balance: " + economy.format((double)economy.getBalance(player.getName())).replace(".00", ""));
 	}
 }

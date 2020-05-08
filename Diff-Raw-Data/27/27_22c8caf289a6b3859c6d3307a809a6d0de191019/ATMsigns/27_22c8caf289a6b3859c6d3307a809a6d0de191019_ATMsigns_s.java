 package me.ellbristow.ATMsigns;
 
 import java.text.DecimalFormat;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.milkbowl.vault.economy.Economy;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class ATMsigns extends JavaPlugin {
 	
 	public static ATMsigns plugin;	
 	public static Logger logger;
 	public final signListener blockListener = new signListener(this);
 	protected FileConfiguration config;
 	public static int item;
 	public static String itemName;
 	public static double currency;
 	public static int altItem1;
 	public static String altItem1Name;
 	public static double altItem1Curr;
 	public static int altItem2;
 	public static String altItem2Name;
 	public static double altItem2Curr;
 	public static double depositFee;
 	public static double withdrawFee;
 	public static boolean percentFee;
 	public static boolean feeToOwner;
 	public static Economy economy;
 	public final playerListener playerListener = new playerListener(this, economy);
 
 	@Override
 	public void onDisable() {
 	}
 
 	@Override
 	public void onEnable() {
 		PluginManager pm = getServer().getPluginManager();
 		logger = getLogger();
 		if (initEconomy() && economy != null) {
 			logger.info("hooked in to Vault." );
 			pm.registerEvents(blockListener, this);
 			pm.registerEvents(playerListener, this);
 			this.config = this.getConfig();
 			item = this.config.getInt("item", 266);
 			itemName = this.config.getString("item_name", "default");
 			if (itemName.equalsIgnoreCase("default")) {
 				itemName = Material.getMaterial(item).toString().replace("_", " ");
 			}
 			currency = this.config.getDouble("currency", 1);
 			altItem1 = this.config.getInt("alt_item1", 999);
			altItem1Name = this.config.getString("alt_item1_name", "default");
			if (altItem1Name.equalsIgnoreCase("default")) {
				altItem1Name = Material.getMaterial(altItem1).toString().replace("_", " ");
			}
			altItem1Curr = this.config.getDouble("alt_item1_curr", 0);
 			altItem2 = this.config.getInt("alt_item2", 999);
			altItem2Name = this.config.getString("alt_item2_name", "default");
			if (altItem2Name.equalsIgnoreCase("default")) {
				altItem2Name = Material.getMaterial(altItem2).toString().replace("_", " ");
			}
			altItem2Curr = this.config.getDouble("alt_item2_curr", 0);
 			depositFee = this.config.getDouble("deposit_fee", 0);
 			withdrawFee = this.config.getDouble("withdraw_fee", 0);
 			percentFee = this.config.getBoolean("percentage_fee", false);
 			feeToOwner = this.config.getBoolean("fee_to_owner", true);
 			this.config.set("item", item);
 			this.config.set("item_name", itemName);
 			this.config.set("currency", currency);
 			this.config.set("alt_item1", altItem1);
 			this.config.set("alt_item1_name", altItem1Name);
 			this.config.set("alt_item1_curr", altItem1Curr);
 			this.config.set("alt_item2", altItem2);
 			this.config.set("alt_item2_name", altItem2Name);
 			this.config.set("alt_item2_curr", altItem2Curr);
 			this.config.set("deposit_fee", depositFee);
 			this.config.set("withdraw_fee", withdrawFee);
 			this.config.set("percentage_fee", percentFee);
 			this.config.set("fee_to_owner", feeToOwner);
 			this.saveConfig();
 			Material checkItem;
 			boolean checkFailed = false;
 			checkItem = Material.getMaterial(item); 
 			if (checkItem == null) {
 				logger.log(Level.SEVERE, "Item ID {0} not found!", item);
 				logger.severe("will be disabled!");
 				checkFailed = true;
 			}
 			if (altItem1 != 999) {
 				checkItem = Material.getMaterial(altItem1); 
 				if (checkItem == null) {
 					logger.log(Level.SEVERE, "Item ID {0} not found!", altItem1);
 					logger.severe("will be disabled!");
 					checkFailed = true;
 				}
 			}
 			if (altItem2 != 999) {
 				checkItem = Material.getMaterial(altItem2); 
 				if (checkItem == null) {
 					logger.log(Level.SEVERE, "Item ID {0} not found!", altItem2);
 					logger.severe("will be disabled!");
 					checkFailed = true;
 				}
 			}
 			if (checkFailed) {
 				pm.disablePlugin(this);
 			}
 		}
 		else {
 			logger.severe("failed to link to Vault!" );
 			logger.severe("(Vault did not find an economy plugin)" );
 			pm.disablePlugin(this);
 		}
 	}
 	
 	public boolean initEconomy() {
 		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
         if (economyProvider != null) {
             economy = economyProvider.getProvider();
         }
 
         return (economy != null);
 	}
 	
 	public void depositItem(Player player, String owner) {
 		if (player.getItemInHand().getTypeId() == item || player.getItemInHand().getTypeId() == altItem1 || player.getItemInHand().getTypeId() == altItem2) {
 			// Correct item in hand
 			int depItem = player.getItemInHand().getTypeId();
 			int count = player.getItemInHand().getAmount();
 			player.setItemInHand(new ItemStack(Material.AIR, 0));
 			double total = 0;
 			if (depItem == item) {
 				economy.depositPlayer( player.getName(), currency * count );
 				player.sendMessage(ChatColor.GREEN + "Deposited: " + ChatColor.GOLD + count + " " + itemName + ChatColor.GREEN + " for " + ChatColor.GOLD + economy.format((double) currency * count).replace(".00", ""));
 				total = currency * count;
 			}
 			else if (depItem == altItem1) {
 				economy.depositPlayer( player.getName(), altItem1Curr * count );
 				player.sendMessage(ChatColor.GREEN + "Deposited: " + ChatColor.GOLD + count + " " + altItem1Name + ChatColor.GREEN + " for " + ChatColor.GOLD + economy.format((double) altItem1Curr * count).replace(".00", ""));
 				total = altItem1Curr * count;
 			}
 			else if (depItem == altItem2) {
 				economy.depositPlayer( player.getName(), altItem2Curr * count );
 				player.sendMessage(ChatColor.GREEN + "Deposited: " + ChatColor.GOLD + count + " " + altItem2Name + ChatColor.GREEN + " for " + ChatColor.GOLD + economy.format((double) altItem2Curr * count).replace(".00", ""));
 				total = altItem2Curr * count;
 			}
 			if (depositFee != 0 && !owner.equals(player.getName())) {
 				double fee;
 				if (percentFee) {
 					fee = roundTwoDecimals(total / 100 * depositFee);
 				}
 				else {
 					fee = depositFee;
 				}
 				economy.withdrawPlayer(player.getName(), fee);
 				player.sendMessage(ChatColor.GOLD + "Deposit Fee: " + ChatColor.WHITE + economy.format(fee));
 				if (feeToOwner) {
 					economy.depositPlayer(owner, fee);
 					if (getServer().getOfflinePlayer(owner).isOnline()) {
 						Player target = getServer().getPlayer(owner);	
 						target.sendMessage(ChatColor.GOLD + "ATM fee of " + ChatColor.WHITE + economy.format(fee) + ChatColor.GOLD + " received from " + ChatColor.WHITE + player.getName());
 					}
 				}
 			}
 		}
 		else {
 			// Wrong item in hand
 			String alternatives = "";
 			if (altItem1 != 999) {
 				if (altItem2 != 999) {
 					alternatives += ChatColor.RED + ", " + ChatColor.WHITE;
 				}
 				else {
 					alternatives += ChatColor.RED + " or " + ChatColor.WHITE;
 				}
 				alternatives += altItem1Name;
 			}
 			if (altItem2 != 999) {
 				alternatives += ChatColor.RED + " or " + ChatColor.WHITE + altItem2Name;
 			}
 			player.sendMessage(ChatColor.RED + "You can only deposit " +  ChatColor.WHITE + itemName + alternatives + ChatColor.RED + "!");
 		}
 	}
 	
 	public void withdrawItem(Player player, String owner) {
 		double fee = 0;
 		if (withdrawFee != 0 && !owner.equals(player.getName())) {
 			if (percentFee) {
 				fee = roundTwoDecimals(currency / 100 * withdrawFee);
 			}
 			else {
 				fee = withdrawFee;
 			}
 		}
 		if (economy.has(player.getName(), currency + fee)) {
 			// Enough in account
 			if (player.getItemInHand().getTypeId() == 0 || (player.getItemInHand().getTypeId() == item && player.getItemInHand().getAmount() < player.getItemInHand().getType().getMaxStackSize())) {
 				player.setItemInHand(new ItemStack(item, player.getItemInHand().getAmount() + 1));
 				economy.withdrawPlayer( player.getName(), currency + fee);
 				player.sendMessage(ChatColor.GREEN + "Withdrawn: " + ChatColor.GOLD + "1 " + itemName + ChatColor.GREEN + " for " + ChatColor.GOLD + economy.format((double)currency).replace(".00", ""));
 				if (fee != 0) {
 					player.sendMessage(ChatColor.GOLD + "Withdrawl Fee: " + ChatColor.WHITE + economy.format(fee));
 					if (feeToOwner) {
 						economy.depositPlayer(owner, fee);
 						if (getServer().getOfflinePlayer(owner).isOnline()) {
 							getServer().getPlayer(owner).sendMessage(ChatColor.GOLD + "ATM fee of " + ChatColor.WHITE + fee + ChatColor.GOLD + " received from " + ChatColor.WHITE + player.getName());
 						}
 					}
 				}
 			}
 			else {
 				player.sendMessage(ChatColor.RED + "You must have a free hand to withdraw " + ChatColor.WHITE + itemName + ChatColor.RED + "!");
 			}
 		}
 		else {
 			// Not enough in account
 			player.sendMessage(ChatColor.RED + "Your balance is too low to withdraw 1 " +  ChatColor.WHITE + itemName + ChatColor.RED + "!");
 			if (fee != 0) {
 				player.sendMessage(ChatColor.RED + "Total required: " + ChatColor.WHITE + currency + ChatColor.RED + " Fee: " + ChatColor.WHITE + fee + ChatColor.RED + " Total: " + ChatColor.WHITE + economy.format((currency + fee)));
 			}
 		}
 	}
 	
 	public void getBalance(Player player) {
 		player.sendMessage(ChatColor.GREEN + "Balance: " + economy.format((double)economy.getBalance(player.getName())).replace(".00", ""));
 	}
 	
 	double roundTwoDecimals(double d) {
         DecimalFormat twoDForm = new DecimalFormat("#.##");
     return Double.valueOf(twoDForm.format(d));
 }
 }

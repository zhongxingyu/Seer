 package org.fatecrafters.plugins;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.List;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.fatecrafters.plugins.util.MysqlFunctions;
 
 public class MainCommand implements CommandExecutor {
 
 	private final RealisticBackpacks plugin;
 
 	private boolean exist = false;
 
 	public MainCommand(final RealisticBackpacks plugin) {
 		this.plugin = plugin;
 	}
 
 	@Override
 	@SuppressWarnings("deprecation")
 	public boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
 		if (cmd.getName().equalsIgnoreCase("rb")) {
 			if (args.length >= 1) {
 				final String command = args[0];
 				if (command.equalsIgnoreCase("reload")) {
 					if (plugin.isUsingPerms() && !sender.hasPermission("rb.reload")) {
 						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("noPermission")));
 						return false;
 					}
 					final Long first = System.currentTimeMillis();
 					plugin.reloadConfig();
 					plugin.setupLists();
 					plugin.getServer().resetRecipes();
 					plugin.setup();
 					sender.sendMessage(ChatColor.GRAY + "Config reloaded.");
 					sender.sendMessage(ChatColor.GRAY + "Took " + ChatColor.YELLOW + (System.currentTimeMillis() - first) + "ms" + ChatColor.GRAY + ".");
 					return true;
 				} else if (command.equalsIgnoreCase("buy") || command.equalsIgnoreCase("purchase")) {
 					if (!plugin.isUsingVault()) {
 						sender.sendMessage(ChatColor.RED + "This command is disabled due to Vault not being installed.");
 						return false;
 					}
 					if (!(sender instanceof Player)) {
 						sender.sendMessage(ChatColor.RED + "This command can only be used by a player.");
 						return false;
 					}
 					if (!(args.length == 2)) {
 						sender.sendMessage(ChatColor.RED + "Incorrect syntax. Please use:" + ChatColor.GRAY + " /rb buy <backpack>");
 						return false;
 					}
 					String backpack = args[1];
 					for (final String b : plugin.backpacks) {
 						if (b.equalsIgnoreCase(backpack)) {
 							backpack = b;
 						}
 					}
 					if (!plugin.backpacks.contains(backpack)) {
 						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("backpackDoesNotExist")));
 						return false;
 					}
 					if (plugin.isUsingPerms() && !sender.hasPermission("rb." + backpack + ".buy")) {
 						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("noPermission")));
 						return false;
 					}
					if (!plugin.backpackData.get(backpack).get(13).equals("true")) {
 						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("notPurchasable")));
 						return false;
 					}
 					final double price = Double.parseDouble(plugin.backpackData.get(backpack).get(14));
 					if (RealisticBackpacks.econ.getBalance(sender.getName()) < price) {
 						sender.sendMessage(ChatColor.RED + "You can not afford " + ChatColor.GOLD + price + ChatColor.RED + " to purchase this backpack.");
 						return false;
 					}
 					final Player p = (Player) sender;
 					if (p.getInventory().contains(plugin.backpackItems.get(backpack))) {
 						sender.sendMessage(ChatColor.RED + "You already have this backpack on you!");
 						return false;
 					}
 					final Inventory inv = p.getInventory();
 					if (inv.firstEmpty() != -1) {
 						RealisticBackpacks.econ.withdrawPlayer(p.getName(), price);
						if (plugin.backpackData.get(backpack).get(17).equalsIgnoreCase("true")) {
 							inv.addItem(RealisticBackpacks.NMS.addGlow(plugin.backpackItems.get(backpack)));
 						} else {
 							inv.addItem(plugin.backpackItems.get(backpack));
 						}
 						p.updateInventory();
 						sender.sendMessage(ChatColor.GREEN + "You have purchased the " + ChatColor.GOLD + backpack + ChatColor.GREEN + " backpack for " + ChatColor.GOLD + price);
 						return true;
 					} else {
 						sender.sendMessage(ChatColor.RED + "Your inventory is full.");
 						return false;
 					}
 				} else if (command.equalsIgnoreCase("list")) {
 					if (plugin.isUsingPerms() && !sender.hasPermission("rb.list")) {
 						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("noPermission")));
 						return false;
 					}
 					sender.sendMessage(ChatColor.LIGHT_PURPLE + "  Name  " + ChatColor.GOLD + "|" + ChatColor.AQUA + "  Size  " + ChatColor.GOLD + "|" + ChatColor.GREEN + "  Price  ");
 					sender.sendMessage(ChatColor.GOLD + "-----------------------------------");
 					if (plugin.isUsingPerms()) {
 						for (final String backpack : plugin.backpacks) {
 							final boolean hasPerm = sender.hasPermission("rb." + backpack + ".buy");
 							final List<String> key = plugin.backpackData.get(backpack);
 							if (plugin.backpackData.get(backpack).get(13).equalsIgnoreCase("true") && hasPerm) {
 								sender.sendMessage(ChatColor.LIGHT_PURPLE + backpack + ChatColor.GOLD + " | " + ChatColor.AQUA + key.get(0) + ChatColor.GOLD + " | " + ChatColor.GREEN + Double.parseDouble(key.get(14)));
							} else if (!plugin.backpackData.get(backpack).get(13).equalsIgnoreCase("true") && hasPerm) {
 								sender.sendMessage(ChatColor.LIGHT_PURPLE + backpack + ChatColor.GOLD + " | " + ChatColor.AQUA + key.get(0) + ChatColor.GOLD + " | " + ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("listCommandNotBuyable")));
 							} else {
 								sender.sendMessage(ChatColor.LIGHT_PURPLE + backpack + ChatColor.GOLD + " | " + ChatColor.AQUA + key.get(0) + ChatColor.GOLD + " | " + ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("listCommandNoPermission")));
 							}
 						}
 					} else {
 						for (final String backpack : plugin.backpacks) {
 							final List<String> key = plugin.backpackData.get(backpack);
							if (plugin.backpackData.get(backpack).get(13).equalsIgnoreCase("true")) {
 								sender.sendMessage(ChatColor.LIGHT_PURPLE + backpack + ChatColor.GOLD + " | " + ChatColor.AQUA + key.get(0) + ChatColor.GOLD + " | " + ChatColor.GREEN + Double.parseDouble(key.get(14)));
 							} else {
 								sender.sendMessage(ChatColor.LIGHT_PURPLE + backpack + ChatColor.GOLD + " | " + ChatColor.AQUA + key.get(0) + ChatColor.GOLD + " | " + ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("listCommandNotBuyable")));
 							}
 						}
 					}
 				} else if (command.equalsIgnoreCase("give")) {
 					if (!(args.length == 3)) {
 						sender.sendMessage(ChatColor.RED + "Incorrect syntax. Please use:" + ChatColor.GRAY + " /rb give <player> <backpack>");
 						return false;
 					}
 					String backpack = args[2];
 					for (final String b : plugin.backpacks) {
 						if (b.equalsIgnoreCase(backpack)) {
 							backpack = b;
 						}
 					}
 					if (plugin.isUsingPerms() && !sender.hasPermission("rb." + backpack + ".give")) {
 						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("noPermission")));
 						return false;
 					}
 					if (!plugin.backpacks.contains(backpack)) {
 						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("backpackDoesNotExist")));
 						return false;
 					}
 					final Player other = plugin.getServer().getPlayer(args[1]);
 					if (other == null) {
 						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("playerDoesNotExist")));
 						return false;
 					}
 					final Inventory inv = other.getInventory();
 					if (inv.firstEmpty() != -1) {
						if (plugin.backpackData.get(backpack).get(17).equalsIgnoreCase("true")) {
 							inv.addItem(RealisticBackpacks.NMS.addGlow(plugin.backpackItems.get(backpack)));
 						} else {
 							inv.addItem(plugin.backpackItems.get(backpack));
 						}
 						other.updateInventory();
 						sender.sendMessage(ChatColor.GREEN + "You have given the " + ChatColor.GOLD + backpack + ChatColor.GREEN + " backpack to " + ChatColor.GOLD + other.getName());
 						return true;
 					} else {
 						sender.sendMessage(ChatColor.RED + "Your inventory is full.");
 						return false;
 					}
 				} else if (command.equalsIgnoreCase("filetomysql")) {
 					if (plugin.isUsingPerms() && !sender.hasPermission("rb.filetomysql")) {
 						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("noPermission")));
 						return false;
 					}
 					plugin.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
 						@Override
 						public void run() {
 							if (!MysqlFunctions.checkIfTableExists("rb_data")) {
 								MysqlFunctions.createTables();
 								exist = false;
 							} else {
 								exist = true;
 							}
 							try {
 								final Connection conn = DriverManager.getConnection(plugin.getUrl(), plugin.getUser(), plugin.getPass());
 								final File dir = new File(plugin.getDataFolder() + File.separator + "userdata");
 								int i = 0, times = 0;
 								final int files = dir.listFiles().length;
 								for (final File child : dir.listFiles()) {
 									final FileConfiguration config = YamlConfiguration.loadConfiguration(child);
 									final String player = child.getName().replace(".yml", "");
 									i++;
 									final Statement statement = conn.createStatement();
 									PreparedStatement state = null;
 									for (final String backpack : config.getConfigurationSection("").getKeys(false)) {
 										if (exist) {
 											final ResultSet res = statement.executeQuery("SELECT EXISTS(SELECT 1 FROM rb_data WHERE player = '" + player + "' AND backpack = '" + backpack + "' LIMIT 1);");
 											if (res.next()) {
 												if (res.getInt(1) == 1) {
 													state = conn.prepareStatement("UPDATE rb_data SET player='" + player + "', backpack='" + backpack + "', inventory='" + config.getString(backpack + ".Inventory") + "' WHERE player='" + player + "' AND backpack='" + backpack + "';");
 												} else {
 													state = conn.prepareStatement("INSERT INTO rb_data (player, backpack, inventory) VALUES('" + player + "', '" + backpack + "', '" + config.getString(backpack + ".Inventory") + "' );");
 												}
 											}
 										} else {
 											state = conn.prepareStatement("INSERT INTO rb_data (player, backpack, inventory) VALUES('" + player + "', '" + backpack + "', '" + config.getString(backpack + ".Inventory") + "' );");
 										}
 										state.executeUpdate();
 										state.close();
 									}
 									if (i == 100) {
 										i = 0;
 										times++;
 										sender.sendMessage(ChatColor.LIGHT_PURPLE + "" + times * 100 + "/" + files + " files have been transferred.");
 									}
 								}
 								conn.close();
 								sender.sendMessage(ChatColor.LIGHT_PURPLE + "File transfer complete.");
 							} catch (final SQLException e) {
 								e.printStackTrace();
 							}
 						}
 					});
 				} else if (command.equalsIgnoreCase("view")) {
 					if (!(args.length == 3)) {
 						sender.sendMessage(ChatColor.RED + "Incorrect syntax. Please use:" + ChatColor.GRAY + " /rb view <player> <backpack>");
 						return false;
 					}
 					String backpack = args[2];
 					for (final String b : plugin.backpacks) {
 						if (b.equalsIgnoreCase(backpack)) {
 							backpack = b;
 						}
 					}
 					boolean fullview = false;
 					boolean restrictedview = false;
 					if (plugin.isUsingPerms() && sender.hasPermission("rb.fullview")) {
 						fullview = true;
 					} else if (plugin.isUsingPerms() && sender.hasPermission("rb.restrictedview")) {
 						restrictedview = true;
 					}
 					if (plugin.isUsingPerms() && !fullview && !restrictedview) {
 						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("noPermission")));
 						return false;
 					}
 					if (!plugin.backpacks.contains(backpack)) {
 						sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.messageData.get("backpackDoesNotExist")));
 						return false;
 					}
 					Inventory inv = null;
 					String name = args[1];
 					final Player p = (Player) sender;
 					final List<String> key = plugin.backpackData.get(backpack);
 					if (!plugin.isUsingMysql()) {
 						boolean fileExists = false;
 						String fullName = null;
 						File file = null;
 						final File dir = new File(plugin.getDataFolder() + File.separator + "userdata");
 						for (final File f : dir.listFiles()) {
 							final String fileName = f.getName();
 							fullName = fileName.replace(".yml", "");
 							if (fullName.equalsIgnoreCase(name)) {
 								name = fullName;
 								file = f;
 								fileExists = true;
 								break;
 							}
 						}
 						if (!fileExists) {
 							sender.sendMessage(ChatColor.RED + "This player has never opened this backpack with flatfile data.");
 							return false;
 						}
 						final FileConfiguration config = YamlConfiguration.loadConfiguration(file);
 						if (config.getString(backpack + ".Inventory") == null) {
 							inv = plugin.getServer().createInventory(p, Integer.parseInt(key.get(0)), ChatColor.translateAlternateColorCodes('&', fullName + "'s " + backpack + " data"));
 						} else {
 							inv = RealisticBackpacks.NMS.stringToInventory(config.getString(backpack + ".Inventory"), fullName + "'s " + backpack + " data");
 						}
 					} else {
 						try {
 							inv = MysqlFunctions.getBackpackInv(name, backpack);
 						} catch (final SQLException e1) {
 							e1.printStackTrace();
 						}
 						if (inv == null) {
 							sender.sendMessage(ChatColor.RED + "This player has never opened this backpack with MySQL data.");
 							return false;
 						}
 					}
 					if (plugin.playerData.containsKey(name)) {
 						sender.sendMessage(ChatColor.RED + "This player is in his backpack, wait for him to close it.");
 						return false;
 					}
 					if (fullview || !plugin.isUsingPerms()) {
 						plugin.adminFullView.put(sender.getName(), backpack + ":" + name);
 					} else {
 						plugin.adminRestrictedView.add(sender.getName());
 					}
 					p.openInventory(inv);
 				} else {
 					sender.sendMessage(ChatColor.RED + "Command not found.");
 				}
 			}
 		}
 		return false;
 	}
 
 }

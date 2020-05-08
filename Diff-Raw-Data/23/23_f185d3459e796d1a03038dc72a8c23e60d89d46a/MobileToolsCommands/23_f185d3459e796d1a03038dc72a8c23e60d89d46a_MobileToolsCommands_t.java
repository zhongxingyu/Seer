 /**
  * 
  * This software is part of the MobileTools
  * 
  * MobileTools is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or 
  * any later version.
  * 
  * MobileTools is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with MobileTools. If not, see <http://www.gnu.org/licenses/>.
  * 
  */
 package me.cybermaxke.mobiletools;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permission;
 
 public class MobileToolsCommands implements CommandExecutor {
 	private final static String NO_PERMISSION = ChatColor.RED +
 			"You don't have permission to perform that command.";
 
 	private final MobileTools plugin;
 	private final MobileConfiguration config;
 
 	public MobileToolsCommands(MobileTools plugin, MobileConfiguration config) {
 		this.plugin = plugin;
 		this.config = config;
 
 		this.plugin.getCommand("MT").setExecutor(this);
 		this.plugin.getCommand("MobileTools").setExecutor(this);
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (label.equalsIgnoreCase("MobileTools")) {
			if (!sender.hasPermission(this.getPerm("cmd"))) {
 				sender.sendMessage(NO_PERMISSION);
 				return true;
 			}
 
 			if (args.length > 0) {
 				if (args[0].equalsIgnoreCase("Reload")) {
 					this.config.load();
 					sender.sendMessage("The config file is succesfully reloaded.");
 					return true;
 				} else if (args[0].equalsIgnoreCase("Help")) {
 					sender.sendMessage("------------------ MobileTools ------------------");
 					sender.sendMessage("'/MobileTools Help' - Shows the admin help page.");
 					sender.sendMessage("'/MobileTools Reload' - Reloads the config.");
 					sender.sendMessage("'/MobileTools AlphaChest' - " +
 							"Converts files from alphachest.");
 					return true;
 				} else if (args[0].equalsIgnoreCase("AlphaChest")) {
 					sender.sendMessage("Add all the files to the AplhaChest folder in the plugin " +
 							"folder before you want to convert those files.");
 					try {
 						this.plugin.getAlphaChestConverter().loadInventories();
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					sender.sendMessage("Files succesfully converted!");
 					return true;
 				}
 			}
 
 			sender.sendMessage("This command uses sub commands, you can list them by" +
 					" using '/MobileTools Help'");
 			return true;
 		}
 
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("You have to be a player to perform that command.");
 			return true;
 		}
 
 		if (args.length <= 0) {
 			sender.sendMessage("Use '/MT List' to show all the available tools.");
 			return true;
 		}
 
 		Player player = (Player) sender;
 		MobilePlayer mp = this.plugin.getPlayer(player);
 
 		if (args[0].equalsIgnoreCase("List")) {
 			sender.sendMessage("------------------ Available Tools ------------------");
 			sender.sendMessage("'/MT Chest [PlayerName]' - Opens a personal chest.");
 			sender.sendMessage("'/MT Craft' - A workbench.");
 			sender.sendMessage("'/MT Furnace' - Your personal furnace.");
 			sender.sendMessage("'/MT Brew' - Your personal brewing stand.");
 			sender.sendMessage("'/MT Enchant' - A enchanting table.");
 			sender.sendMessage("'/MT Ender' - Your ender chest.");
 		} else if (args[0].equalsIgnoreCase("Chest")) {
			if (!player.hasPermission(this.getPerm("chest.cmd"))) {
 				sender.sendMessage(NO_PERMISSION);
 				return true;
 			}
 
 			if (args.length > 1) {
 				Player player1 = this.plugin.getServer().getPlayer(args[1]);
 
 				if (player1 == null || !player1.isOnline()) {
 					sender.sendMessage("The player has to be online to open it's inventory.");
 					return true;
 				}
 
 				MobilePlayer mp1 = this.plugin.getPlayer(player1);
 				mp1.updateChestSize();
 
 				player.openInventory(mp1.getChest());
 			} else {
 				mp.updateChestSize();
 				mp.openChest();
 			}
 		} else if (args[0].equalsIgnoreCase("Craft")) {
			if (!player.hasPermission(this.getPerm("craft.cmd"))) {
 				sender.sendMessage(NO_PERMISSION);
 				return true;
 			}
 
 			mp.openWorkbench();
 		} else if (args[0].equalsIgnoreCase("Furnace")) {
			if (!player.hasPermission(this.getPerm("furnace.cmd"))) {
 				sender.sendMessage(NO_PERMISSION);
 				return true;
 			}
 
 			mp.openFurnace();
 		} else if (args[0].equalsIgnoreCase("Anvil")) {
			if (!player.hasPermission(this.getPerm("anvil.cmd"))) {
 				sender.sendMessage(NO_PERMISSION);
 				return true;
 			}
 
 			mp.openAnvil();
 		} else if (args[0].equalsIgnoreCase("Brew")) {
			if (!player.hasPermission(this.getPerm("brew.cmd"))) {
 				sender.sendMessage(NO_PERMISSION);
 				return true;
 			}
 
 			mp.openBrewingStand();
 		} else if (args[0].equalsIgnoreCase("Enchant")) {
			if (!player.hasPermission(this.getPerm("enchant.cmd"))) {
 				sender.sendMessage(NO_PERMISSION);
 				return true;
 			}
 
 			mp.openEnchantingTable();
 		} else if (args[0].equalsIgnoreCase("Ender")) {
			if (!player.hasPermission(this.getPerm("ender.cmd"))) {
 				sender.sendMessage(NO_PERMISSION);
 				return true;
 			}
 
 			mp.openEnderChest();
 		} else {
 			sender.sendMessage("That tool doesn't extsit. Use '/MT List' to show all the available tools.");
 			return true;
 		}
 
 		return true;
 	}
 
 	private Permission getPerm(String path) {
 		return this.config.getPermission(path);
 	}
 }

 /*******************************************************************************
  * Copyright (c) 2013 Travis Ralston.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser Public License v2.1
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
  *
  * Contributors:
  * turt2live (Travis Ralston) - initial API and implementation
  ******************************************************************************/
 package com.turt2live.antishare;
 
 import com.turt2live.antishare.cuboid.Cuboid;
 import com.turt2live.antishare.inventory.ASInventory;
 import com.turt2live.antishare.inventory.ASInventory.InventoryType;
 import com.turt2live.antishare.inventory.DisplayableInventory;
 import com.turt2live.antishare.regions.Region;
 import com.turt2live.antishare.regions.RegionKey;
 import com.turt2live.antishare.util.ASUtils;
 import com.turt2live.materials.MaterialAPI;
 import org.bukkit.*;
 import org.bukkit.command.BlockCommandSender;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Command Handler
  *
  * @author turt2live
  */
 public class CommandHandler implements CommandExecutor {
 
     private final AntiShare plugin = AntiShare.p;
     private String noPermission = plugin.getMessages().getMessage("no-permission");
     private String notPlayer = plugin.getMessages().getMessage("not-a-player");
 
     @Override
     public boolean onCommand(final CommandSender sender, Command command, String label, String[] args) {
         if (sender instanceof BlockCommandSender) {
             return false;
         }
         if (command.getName().equalsIgnoreCase("AntiShare")) {
             if (args.length > 0) {
                 if (args[0].equalsIgnoreCase("version")) {
                     plugin.getMessages().sendTo(sender, ChatColor.YELLOW + "Version: " + ChatColor.GOLD + plugin.getDescription().getVersion() + ChatColor.YELLOW + " Build: " + ChatColor.GOLD + plugin.getBuild(), false);
                     return true;
                 } else if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
                     if (sender.hasPermission(PermissionNodes.RELOAD)) {
                         plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("reloading"), true);
                         plugin.reload();
                         plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("reloaded"), true);
                     } else {
                         plugin.getMessages().sendTo(sender, noPermission, true);
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("mirror")) {
                     // Sanity Check
                     if (!(sender instanceof Player)) {
                         plugin.getMessages().sendTo(sender, notPlayer, true);
                     } else {
                         if (sender.hasPermission(PermissionNodes.MIRROR)) {
                            if (!plugin.getConfig().getBoolean("handled-actions.gamemode-inventories")) {
                                 plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("inventories-not-enabled"), true);
                                 return true;
                             }
                             if (args.length < 2) {
                                 //plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("syntax", "/as mirror <player> [enderchest/normal] [gamemode] [world]"), true);
                                 plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("syntax", "/as mirror <player> [enderchest/normal] [gamemode] [world]"), true);
                             } else {
                                 // Setup
                                 String playername = args[1];
                                 OfflinePlayer player = plugin.getServer().getPlayer(playername);
                                 // Find online player first, then we look for offline players
                                 if (player == null) {
                                     for (OfflinePlayer offlinePlayer : plugin.getServer().getOfflinePlayers()) {
                                         if (offlinePlayer.getName().equalsIgnoreCase(playername) || offlinePlayer.getName().toLowerCase().startsWith(playername.toLowerCase())) {
                                             player = offlinePlayer;
                                             break;
                                         }
                                     }
                                 }
 
                                 // Sanity check
                                 if (player == null) {
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("player-not-found", playername), true);
                                     return true;
                                 }
 
                                 // Ender chest check
                                 boolean isEnder = false;
                                 if (args.length > 2) {
                                     if (args[2].equalsIgnoreCase("ender") || args[2].equalsIgnoreCase("enderchest")) {
                                         isEnder = true;
                                     } else if (args[2].equalsIgnoreCase("normal") || args[2].equalsIgnoreCase("player")) {
                                         isEnder = false;
                                     } else {
                                         isEnder = false;
                                         plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("assume-normal-inventory", args[2]), true);
                                     }
                                 }
 
                                 // Per specific game mode
                                 GameMode gamemode = player.isOnline() ? ((Player) player).getGameMode() : GameMode.SURVIVAL;
                                 if (args.length > 3) {
                                     GameMode temp = ASUtils.getGameMode(args[3]);
                                     if (temp != null) {
                                         gamemode = temp;
                                     } else {
                                         plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("assume-gamemode", "Game Mode", args[3], MaterialAPI.capitalize(gamemode.name())), true);
                                     }
                                 }
 
                                 // World check
                                 World world = player.isOnline() ? ((Player) player).getWorld() : plugin.getServer().getWorlds().get(0);
                                 if (args.length > 4) {
                                     World temp = Bukkit.getWorld(args[4]);
                                     if (temp == null) {
                                         plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("assume-world", args[4], world.getName()), true);
                                     } else {
                                         world = temp;
                                     }
                                 }
 
                                 // Load all inventories
                                 if (player.isOnline()) {
                                     Player p = (Player) player;
                                     plugin.getInventoryManager().savePlayer(p);
                                 }
                                 ASInventory chosen = ASInventory.load(playername, gamemode, isEnder ? InventoryType.ENDER : InventoryType.PLAYER, world.getName());
                                 if (chosen == null) {
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("inventory-not-found"), true);
                                     return true;
                                 }
 
                                 // Create title
                                 String title = player.getName() + " | " + ASUtils.gamemodeAbbreviation(gamemode, false) + " | " + world.getName();
 
                                 // Create displayable inventory
                                 DisplayableInventory display = new DisplayableInventory(chosen, title);
 
                                 // Show inventory
                                 if (isEnder) {
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("mirror-ender-welcome", player.getName()), true);
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("mirror-edit"), true);
                                 } else {
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("mirror-welcome", player.getName()), true);
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("mirror-edit"), true);
                                 }
                                 ((Player) sender).openInventory(display.getInventory()); // Creates the "live editing" window
                             }
                         } else {
                             plugin.getMessages().sendTo(sender, noPermission, true);
                         }
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("region")) {
                     if (sender.hasPermission(PermissionNodes.REGION_CREATE)) {
                         // Sanity Check
                         if (sender instanceof Player) {
                             Player player = (Player) sender;
                             if (args.length < 3) {
                                 plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("syntax", "/as region <gamemode> <name>"), true);
                             } else {
                                 String regionName = args[2];
                                 GameMode gamemode = ASUtils.getGameMode(args[1]);
                                 if (gamemode != null) {
                                     if (!plugin.getRegionManager().isRegionNameTaken(regionName)) {
                                         if (plugin.getCuboidManager().isCuboidComplete(player.getName())) {
                                             Cuboid cuboid = plugin.getCuboidManager().getCuboid(player.getName());
                                             plugin.getRegionManager().addRegion(cuboid, player.getName(), regionName, gamemode);
                                             plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("region-created"), true);
                                         } else {
                                             plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("missing-cuboid"), true);
                                         }
                                     } else {
                                         plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("name-in-use"), true);
                                     }
                                 } else {
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("unknown-gamemode", args[1]), true);
                                 }
                             }
                         } else {
                             plugin.getMessages().sendTo(sender, notPlayer, true);
                         }
                     } else {
                         plugin.getMessages().sendTo(sender, noPermission, true);
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("rmregion") || args[0].equalsIgnoreCase("removeregion")) {
                     if (sender.hasPermission(PermissionNodes.REGION_DELETE)) {
                         // Sanity check
                         if (sender instanceof Player) {
                             // Remove region
                             if (args.length == 1) {
                                 Location location = ((Player) sender).getLocation();
                                 Region region = plugin.getRegionManager().getRegion(location);
                                 if (region != null) {
                                     plugin.getRegionManager().removeRegion(region.getName());
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("region-remove"), true);
                                 } else {
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("no-foot-region"), true);
                                 }
                             } else {
                                 String regionName = args[1];
                                 Region region = plugin.getRegionManager().getRegion(regionName);
                                 if (region != null) {
                                     plugin.getRegionManager().removeRegion(region.getName());
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("region-remove"), true);
                                 } else {
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("unknown-region", regionName), true);
                                 }
                             }
                         } else {
                             // Remove region
                             if (args.length > 1) {
                                 String regionName = args[1];
                                 Region region = plugin.getRegionManager().getRegion(regionName);
                                 if (region != null) {
                                     plugin.getRegionManager().removeRegion(region.getName());
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("region-remove"), true);
                                 } else {
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("unknown-region", regionName), true);
                                 }
                             } else {
                                 plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("syntax", "/as rmregion <name>"), true);
                             }
                         }
                     } else {
                         plugin.getMessages().sendTo(sender, noPermission, true);
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("editregion")) {
                     if (sender.hasPermission(PermissionNodes.REGION_EDIT)) {
                         // Check validity of key
                         boolean valid = false;
                         if (args.length >= 3) {
                             if (RegionKey.isKey(args[2])) {
                                 if (!RegionKey.requiresValue(RegionKey.getKey(args[2]))) {
                                     valid = true; // we have at least 3 values in args[] and the key does not need a value
                                 }
                             }
                         }
                         if (args.length >= 4) {
                             valid = true;
                         }
                         if (!valid) {
                             // Show help
                             if (args.length >= 2) {
                                 if (args[1].equalsIgnoreCase("help")) {
                                     String key = plugin.getMessages().getMessage("key").toLowerCase();
                                     String value = plugin.getMessages().getMessage("value").toLowerCase();
                                     key = key.substring(0, 1).toUpperCase() + key.substring(1);
                                     value = value.substring(0, 1).toUpperCase() + value.substring(1);
                                     plugin.getMessages().sendTo(sender, ChatColor.GOLD + "/as editregion <name> <key> <value>", false);
                                     plugin.getMessages().sendTo(sender, ChatColor.AQUA + key + ": " + ChatColor.WHITE + "name " + ChatColor.AQUA + value + ": " + ChatColor.WHITE + "<any name>", false);
                                     plugin.getMessages().sendTo(sender, ChatColor.AQUA + key + ": " + ChatColor.WHITE + "ShowEnterMessage " + ChatColor.AQUA + value + ": " + ChatColor.WHITE + "true/false", false);
                                     plugin.getMessages().sendTo(sender, ChatColor.AQUA + key + ": " + ChatColor.WHITE + "ShowExitMessage " + ChatColor.AQUA + value + ": " + ChatColor.WHITE + "true/false", false);
                                     plugin.getMessages().sendTo(sender, ChatColor.AQUA + key + ": " + ChatColor.WHITE + "EnterMessage " + ChatColor.AQUA + value + ": " + ChatColor.WHITE + "<enter message>", false);
                                     plugin.getMessages().sendTo(sender, ChatColor.AQUA + key + ": " + ChatColor.WHITE + "ExitMessage " + ChatColor.AQUA + value + ": " + ChatColor.WHITE + "<exit message>", false);
                                     plugin.getMessages().sendTo(sender, ChatColor.AQUA + key + ": " + ChatColor.WHITE + "inventory " + ChatColor.AQUA + value + ": " + ChatColor.WHITE + "'none'/'set'", false);
                                     plugin.getMessages().sendTo(sender, ChatColor.AQUA + key + ": " + ChatColor.WHITE + "gamemode " + ChatColor.AQUA + value + ": " + ChatColor.WHITE + "survival/creative", false);
                                     plugin.getMessages().sendTo(sender, ChatColor.AQUA + key + ": " + ChatColor.WHITE + "area " + ChatColor.AQUA + value + ": " + ChatColor.WHITE + "No Value", false);
                                 } else {
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("syntax", "/as editregion <name> <key> <value>"), true);
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("extra-help", "/as editregion help"), true);
                                 }
                             } else {
                                 plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("syntax", "/as editregion <name> <key> <value>"), true);
                                 plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("extra-help", "/as editregion help"), true);
                             }
                         } else {
                             // Setup
                             String name = args[1];
                             String key = args[2];
                             String value = args.length > 3 ? args[3] : "";
 
                             // Merge message
                             if (args.length > 4) {
                                 for (int i = 4; i < args.length; i++) { // Starts at args[4]
                                     value = value + args[i] + " ";
                                 }
                                 value = value.substring(0, value.length() - 1);
                             }
 
                             // Check region
                             if (plugin.getRegionManager().getRegion(name) == null) {
                                 plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("unknown-region", name), true);
                             } else {
                                 // Update region if needed
                                 if (RegionKey.isKey(key)) {
                                     plugin.getRegionManager().updateRegion(plugin.getRegionManager().getRegion(name), RegionKey.getKey(key), value, sender);
                                 } else {
                                     plugin.getMessages().sendTo(sender, ChatColor.DARK_RED + plugin.getMessages().getMessage("unknown-key", key), true);
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("extra-help", "/as editregion help"), true);
                                 }
                             }
                         }
                     } else {
                         plugin.getMessages().sendTo(sender, noPermission, true);
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("listregions")) {
                     if (sender.hasPermission(PermissionNodes.REGION_LIST)) {
                         // Sanity check on page number
                         int page = 1;
                         if (args.length >= 2) {
                             try {
                                 page = Integer.parseInt(args[1]);
                             } catch (NumberFormatException e) {
                                 plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("unknown-value", args[1]), true);
                                 return true;
                             }
                         }
 
                         // Setup
                         page = Math.abs(page);
                         int resultsPerPage = 6; // Put as a variable for ease of changing
                         Set<Region> set = plugin.getRegionManager().getAllRegions();
                         List<Region> regions = new ArrayList<Region>();
                         regions.addAll(set);
 
                         // Check for empty list
                         if (regions.size() <= 0) {
                             plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("no-regions"), true);
                             return true;
                         }
 
                         // Math
                         Double maxPagesD = Math.ceil(regions.size() / resultsPerPage);
                         if (maxPagesD < 1) {
                             maxPagesD = 1.0;
                         }
                         int maxPages = maxPagesD.intValue();
                         if (maxPagesD < page) {
                             plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("page-not-found", String.valueOf(page), String.valueOf(maxPages)), true);
                             return true;
                         }
 
                         // Generate pages
                         String pagenation = ChatColor.DARK_GREEN + "=======[ " + ChatColor.GREEN + "AntiShare Regions " + ChatColor.DARK_GREEN + "|" + ChatColor.GREEN + " Page " + page + "/" + maxPages + ChatColor.DARK_GREEN + " ]=======";
                         plugin.getMessages().sendTo(sender, pagenation, false);
                         for (int i = (page - 1) * resultsPerPage; i < (resultsPerPage < regions.size() ? resultsPerPage * page : regions.size()); i++) {
                             plugin.getMessages().sendTo(sender, ChatColor.DARK_AQUA + "#" + (i + 1) + " " + ChatColor.GOLD + regions.get(i).getName() + ChatColor.YELLOW + " Creator: " + ChatColor.AQUA + regions.get(i).getOwner() + ChatColor.YELLOW + " World: " + ChatColor.AQUA + regions.get(i).getWorldName(), false);
                         }
                         plugin.getMessages().sendTo(sender, pagenation, false);
                     } else {
                         plugin.getMessages().sendTo(sender, noPermission, true);
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("tool")) {
                     if (sender.hasPermission(PermissionNodes.TOOL_GET)) {
                         // Sanity check
                         if (!(sender instanceof Player)) {
                             plugin.getMessages().sendTo(sender, notPlayer, true);
                         } else {
                             // Setup
                             Player player = (Player) sender;
                             PlayerInventory inventory = player.getInventory();
 
                             // Check inventory
                             if (inventory.firstEmpty() != -1 && inventory.firstEmpty() <= inventory.getSize()) {
                                 if (ASUtils.hasTool(AntiShare.ANTISHARE_TOOL, player)) {
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("have-tool", MaterialAPI.capitalize(AntiShare.ANTISHARE_TOOL.name())), true);
                                 } else {
                                     ASUtils.giveTool(AntiShare.ANTISHARE_TOOL, player);
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("get-tool", MaterialAPI.capitalize(AntiShare.ANTISHARE_TOOL.name())), true);
                                 }
                             } else {
                                 plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("no-space", String.valueOf(1)), true);
                             }
                         }
                     } else {
                         plugin.getMessages().sendTo(sender, noPermission, true);
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("settool")) {
                     if (sender.hasPermission(PermissionNodes.TOOL_GET)) {
                         // Sanity check
                         if (!(sender instanceof Player)) {
                             plugin.getMessages().sendTo(sender, notPlayer, true);
                         } else {
                             // Setup
                             Player player = (Player) sender;
                             PlayerInventory inventory = player.getInventory();
 
                             // Check inventory
                             if (inventory.firstEmpty() != -1 && inventory.firstEmpty() <= inventory.getSize()) {
                                 if (ASUtils.hasTool(AntiShare.ANTISHARE_SET_TOOL, player)) {
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("have-tool", MaterialAPI.capitalize(AntiShare.ANTISHARE_SET_TOOL.name())), true);
                                 } else {
                                     ASUtils.giveTool(AntiShare.ANTISHARE_SET_TOOL, player);
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("get-tool", MaterialAPI.capitalize(AntiShare.ANTISHARE_SET_TOOL.name())), true);
                                 }
                             } else {
                                 plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("no-space", String.valueOf(1)), true);
                             }
                         }
                     } else {
                         plugin.getMessages().sendTo(sender, noPermission, true);
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("toolbox")) {
                     if (sender.hasPermission(PermissionNodes.TOOL_GET)) {
                         // Sanity check
                         if (!(sender instanceof Player)) {
                             plugin.getMessages().sendTo(sender, notPlayer, true);
                         } else {
                             // Setup
                             Player player = (Player) sender;
                             PlayerInventory inventory = player.getInventory();
 
                             // Find clear spots
                             int clearSpots = 0;
                             for (ItemStack stack : inventory.getContents()) {
                                 if (stack == null || stack.getType() == Material.AIR) {
                                     clearSpots++;
                                 }
                             }
 
                             // Check inventory
                             if (clearSpots >= 3) {
                                 if (!ASUtils.hasTool(AntiShare.ANTISHARE_TOOL, player)) {
                                     ASUtils.giveTool(AntiShare.ANTISHARE_TOOL, player, 1);
                                 }
                                 if (!ASUtils.hasTool(AntiShare.ANTISHARE_SET_TOOL, player)) {
                                     ASUtils.giveTool(AntiShare.ANTISHARE_SET_TOOL, player, 2);
                                 }
                                 if (sender.hasPermission(PermissionNodes.CREATE_CUBOID)) {
                                     if (!ASUtils.hasTool(AntiShare.ANTISHARE_CUBOID_TOOL, player)) {
                                         ASUtils.giveTool(AntiShare.ANTISHARE_CUBOID_TOOL, player, 3);
                                     }
                                 } else {
                                     plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("cannot-have-cuboid"), true);
                                 }
                                 plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("tools-give"), true);
                                 player.getInventory().setHeldItemSlot(1);
                             } else {
                                 plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("no-space", String.valueOf(3)), true);
                             }
                         }
                     } else {
                         plugin.getMessages().sendTo(sender, noPermission, true);
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("money")) {
                     if (args.length < 2) {
                         plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("syntax", "/as money <on/off/status>"), true);
                     } else {
                         if (args[1].equalsIgnoreCase("status") || args[1].equalsIgnoreCase("state")) {
                             plugin.getMessages().sendTo(sender, plugin.getMoneyManager().isSilent(sender.getName()) ? plugin.getMessages().getMessage("fines-not-getting") : plugin.getMessages().getMessage("fines-getting"), true);
                             return true;
                         }
                         if (ASUtils.getBoolean(args[1]) == null) {
                             plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("syntax", "/as money <on/off/status>"), true);
                             return true;
                         }
                         boolean silent = !ASUtils.getBoolean(args[1]);
                         if (silent) {
                             plugin.getMoneyManager().addToSilentList(sender.getName());
                         } else {
                             plugin.getMoneyManager().removeFromSilentList(sender.getName());
                         }
                         plugin.getMessages().sendTo(sender, silent ? plugin.getMessages().getMessage("fines-not-getting") : plugin.getMessages().getMessage("fines-getting"), true);
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("simplenotice") || args[0].equalsIgnoreCase("sn")) {
                     if (sender instanceof Player) {
                         Player player = (Player) sender;
                         if (player.getListeningPluginChannels().contains("SimpleNotice")) {
                             if (plugin.isSimpleNoticeEnabled(player.getName())) {
                                 plugin.disableSimpleNotice(player.getName());
                                 plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("simplenotice-off"), false);
                             } else {
                                 plugin.enableSimpleNotice(player.getName());
                                 plugin.getMessages().sendTo(player, plugin.getMessages().getMessage("simplenotice-on"), false);
                             }
                         } else {
                             plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("simplenotice-missing"), false);
                         }
                     } else {
                         plugin.getMessages().sendTo(sender, notPlayer, true);
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("check") || args[0].equalsIgnoreCase("gamemode") || args[0].equalsIgnoreCase("gm")) {
                     if (sender.hasPermission(PermissionNodes.CHECK)) {
                         GameMode gm = null;
                         if (args.length > 1 && !args[1].equalsIgnoreCase("all")) {
                             gm = ASUtils.getGameMode(args[1]);
                             if (gm == null) {
                                 Player player = plugin.getServer().getPlayer(args[1]);
                                 if (player != null) {
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("check", player.getName(), MaterialAPI.capitalize(player.getGameMode().name())), false);
                                 } else {
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("player-not-found", args[1]), true);
                                 }
                                 return true;
                             }
                         }
                         if (gm == null) {
                             for (GameMode gamemode : GameMode.values()) {
                                 if (ASUtils.findGameModePlayers(gamemode).size() > 0) {
                                     plugin.getMessages().sendTo(sender, ChatColor.GOLD + gamemode.name() + ": " + ChatColor.YELLOW + ASUtils.commas(ASUtils.findGameModePlayers(gamemode)), false);
                                 } else {
                                     plugin.getMessages().sendTo(sender, ChatColor.GOLD + gamemode.name() + ": " + ChatColor.YELLOW + "no one", false);
                                 }
                             }
                         } else {
                             plugin.getMessages().sendTo(sender, ChatColor.GOLD + gm.name() + ": " + ChatColor.YELLOW + ASUtils.commas(ASUtils.findGameModePlayers(gm)), false);
                         }
                     } else {
                         plugin.getMessages().sendTo(sender, noPermission, true);
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("cuboid")) {
                     if (!sender.hasPermission(PermissionNodes.CREATE_CUBOID)) {
                         plugin.getMessages().sendTo(sender, noPermission, true);
                         return true;
                     }
                     if (args.length > 1) {
                         if (args[1].equalsIgnoreCase("clear")) {
                             if (plugin.getCuboidManager().isCuboidComplete(sender.getName())) {
                                 plugin.getCuboidManager().removeCuboid(sender.getName());
                                 plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("cuboid-removed"), true);
                             } else {
                                 plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("missing-cuboid"), true);
                             }
                         } else if (args[1].equalsIgnoreCase("tool")) {
                             if (!(sender instanceof Player)) {
                                 plugin.getMessages().sendTo(sender, notPlayer, true);
                             } else {
                                 // Setup
                                 Player player = (Player) sender;
                                 PlayerInventory inventory = player.getInventory();
 
                                 // Check inventory
                                 if (inventory.firstEmpty() != -1 && inventory.firstEmpty() <= inventory.getSize()) {
                                     if (ASUtils.hasTool(AntiShare.ANTISHARE_CUBOID_TOOL, player)) {
                                         plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("have-tool", MaterialAPI.capitalize(AntiShare.ANTISHARE_CUBOID_TOOL.name())), true);
                                     } else {
                                         ASUtils.giveTool(AntiShare.ANTISHARE_CUBOID_TOOL, player);
                                         plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("get-tool", MaterialAPI.capitalize(AntiShare.ANTISHARE_CUBOID_TOOL.name())), true);
                                     }
                                 } else {
                                     plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("no-space", String.valueOf(1)), true);
                                 }
                             }
                         } else if (args[1].equalsIgnoreCase("status")) {
                             Cuboid cuboid = plugin.getCuboidManager().getCuboid(sender.getName());
                             if (cuboid == null) {
                                 plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("missing-cuboid"), false);
                             } else {
                                 Location min = cuboid.isValid() ? cuboid.getMinimumPoint() : cuboid.getPoint1();
                                 Location max = cuboid.isValid() ? cuboid.getMaximumPoint() : cuboid.getPoint2();
                                 if (min != null) {
                                     plugin.getMessages().sendTo(sender, ChatColor.GOLD + "1: " + ChatColor.YELLOW + "(" + min.getBlockX() + ", " + min.getBlockY() + ", " + min.getBlockZ() + ", " + min.getWorld().getName() + ")", false);
                                 } else {
                                     plugin.getMessages().sendTo(sender, ChatColor.GOLD + "1: " + ChatColor.YELLOW + "not set", false);
                                 }
                                 if (max != null) {
                                     plugin.getMessages().sendTo(sender, ChatColor.GOLD + "2: " + ChatColor.YELLOW + "(" + max.getBlockX() + ", " + max.getBlockY() + ", " + max.getBlockZ() + ", " + max.getWorld().getName() + ")", false);
                                 } else {
                                     plugin.getMessages().sendTo(sender, ChatColor.GOLD + "2: " + ChatColor.YELLOW + "not set", false);
                                 }
                             }
                         } else {
                             plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("syntax", "/as cuboid <clear | tool | status>"), true);
                         }
                     } else {
                         plugin.getMessages().sendTo(sender, plugin.getMessages().getMessage("syntax", "/as cuboid <clear | tool | status>"), true);
                     }
                     return true;
                 } else {
                     // This is for all extra commands, like /as help.
                     // This is also for all "non-commands", like /as sakjdha
                     return false; //Shows usage in plugin.yml
                 }
             }
         }
         return false; //Shows usage in plugin.yml
     }
 }

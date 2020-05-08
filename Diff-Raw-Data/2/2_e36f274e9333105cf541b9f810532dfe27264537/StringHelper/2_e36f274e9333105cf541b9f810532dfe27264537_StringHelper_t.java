 package net.worldoftomorrow.nala.ni;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import net.milkbowl.vault.item.ItemInfo;
 import net.milkbowl.vault.item.Items;
 
 public class StringHelper {
 	public static String replaceVars(String msg, Player p, int iid) {
 		String x = Integer.toString(p.getLocation().getBlockX());
 		String y = Integer.toString(p.getLocation().getBlockY());
 		String z = Integer.toString(p.getLocation().getBlockZ());
 		msg = msg.replace("%n", p.getDisplayName());
 		msg = msg.replace("%w", p.getWorld().getName());
 		msg = msg.replace("%x", x);
 		msg = msg.replace("%y", y);
 		msg = msg.replace("%z", z);
 		if (Tools.getTool(iid) != null) {
 			msg = msg.replace("%i", Tools.getTool(iid).getRealName());
 		} else if (Armor.getArmour(iid) != null) {
 			msg = msg.replace("%i", Armor.getArmour(iid).getRealName());
 		} else if (Vault.vaultPerms) {
 			ItemInfo info = Items.itemById(iid);
 			msg = msg.replace("%i", info.getName());
 		} else {
 			String id = Integer.toString(iid);
 			msg = msg.replace("%i", id);
 		}
 		msg = StringHelper.parseColors(msg);
 		return msg;
 	}
 
 	public static String replaceVars(String msg, Player p, String recipe) {
 		String x = Integer.toString(p.getLocation().getBlockX());
 		String y = Integer.toString(p.getLocation().getBlockY());
 		String z = Integer.toString(p.getLocation().getBlockZ());
 		msg = msg.replace("%n", p.getDisplayName());
 		msg = msg.replace("%w", p.getWorld().getName());
 		msg = msg.replace("%x", x);
 		msg = msg.replace("%y", y);
 		msg = msg.replace("%z", z);
		msg = msg.replace("%i", recipe);
 		msg = StringHelper.parseColors(msg);
 		return msg;
 	}
 
 	public static String replaceVars(String msg, Player p, EventTypes type,
 			ItemStack stack) {
 		String x = Integer.toString(p.getLocation().getBlockX());
 		String y = Integer.toString(p.getLocation().getBlockY());
 		String z = Integer.toString(p.getLocation().getBlockZ());
 		int iid = stack.getTypeId();
 
 		if (Tools.getTool(iid) != null) {
 			msg = msg.replace("%i", Tools.getTool(iid).getRealName());
 		} else if (Armor.getArmour(iid) != null) {
 			msg = msg.replace("%i", Armor.getArmour(iid).getRealName());
 		} else if (Vault.vaultPerms) {
 			// Get the item by stack so sub-types can be returned
 			ItemInfo info = Items.itemByStack(stack);
 			msg = msg.replace("%i", info.getName());
 		} else {
 			String id = Integer.toString(iid);
 			msg = msg.replace("%i", id);
 		}
 
 		msg = msg.replace("%n", p.getDisplayName());
 		msg = msg.replace("%w", p.getWorld().getName());
 		msg = msg.replace("%x", x);
 		msg = msg.replace("%y", y);
 		msg = msg.replace("%z", z);
 		msg = msg.replace("%t", type.getName());
 		msg = StringHelper.parseColors(msg);
 		return msg;
 	}
 
 	public static String replaceVars(String msg, Player p, EventTypes type,
 			String recipe) {
 		String x = Integer.toString(p.getLocation().getBlockX());
 		String y = Integer.toString(p.getLocation().getBlockY());
 		String z = Integer.toString(p.getLocation().getBlockZ());
 
 		msg = msg.replace("%i", recipe);
 		msg = msg.replace("%n", p.getDisplayName());
 		msg = msg.replace("%w", p.getWorld().getName());
 		msg = msg.replace("%x", x);
 		msg = msg.replace("%y", y);
 		msg = msg.replace("%z", z);
 		msg = msg.replace("%t", type.getName());
 		msg = StringHelper.parseColors(msg);
 		return msg;
 	}
 
 	public static void notifyPlayer(Player p, EventTypes type, String recipe) {
 		if (type.doNotify()) {
 			String msg;
 			switch (type) {
 			case CRAFT:
 				msg = Configuration.noCraftMessage();
 				break;
 			case BREW:
 				msg = Configuration.noBrewMessage();
 				break;
 			case WEAR:
 				msg = Configuration.noWearMessage();
 				break;
 			case PICKUP:
 				msg = Configuration.noPickupMessage();
 				break;
 			case DROP:
 				msg = Configuration.noDropMessage();
 				break;
 			case USE:
 				msg = Configuration.noUseMessage();
 				break;
 			case HOLD:
 				msg = Configuration.noHoldMessage();
 				break;
 			case SMELT:
 				msg = Configuration.noCookMessage();
 				break;
 			case COOK:
 				msg = Configuration.noCookMessage();
 				break;
 			default:
 				msg = "Unknown event type: " + type.name();
 				break;
 			}
 			p.sendMessage(ChatColor.RED + "[NI] " + ChatColor.BLUE
 					+ replaceVars(msg, p, recipe));
 		}
 	}
 
 	public static void notifyPlayer(Player p, EventTypes type, int id) {
 		if (type.doNotify()) {
 			String msg;
 			switch (type) {
 			case CRAFT:
 				msg = Configuration.noCraftMessage();
 				break;
 			case BREW:
 				msg = Configuration.noBrewMessage();
 				break;
 			case WEAR:
 				msg = Configuration.noWearMessage();
 				break;
 			case PICKUP:
 				msg = Configuration.noPickupMessage();
 				break;
 			case USE:
 				msg = Configuration.noUseMessage();
 				break;
 			case HOLD:
 				msg = Configuration.noHoldMessage();
 				break;
 			case SMELT:
 				msg = Configuration.noCookMessage();
 				break;
 			case COOK:
 				msg = Configuration.noCookMessage();
 				break;
 			default:
 				msg = "Unknown event type: " + type.name();
 				break;
 			}
 			p.sendMessage(ChatColor.RED + "[NI] " + ChatColor.BLUE
 					+ replaceVars(msg, p, id));
 		}
 	}
 
 	public static void notifyAdmin(Player p, String recipe) {
 		if (Configuration.notifyAdmins()) {
 			String message = StringHelper.replaceVars(
 					Configuration.adminMessage(), p, EventTypes.BREW, recipe);
 			Player[] players = Bukkit.getOnlinePlayers();
 			for (Player player : players)
 				if (Perms.ADMIN.has(player)) {
 					player.sendMessage(ChatColor.RED + "[NI] " + ChatColor.BLUE
 							+ message);
 				}
 		}
 	}
 
 	public static void notifyAdmin(Player p, EventTypes type, ItemStack stack) {
 		if (Configuration.notifyAdmins()) {
 			String message = StringHelper.replaceVars(
 					Configuration.adminMessage(), p, type, stack);
 			// log.log(message);
 			Player[] players = Bukkit.getOnlinePlayers();
 			for (Player player : players)
 				if (Perms.ADMIN.has(player)) {
 					player.sendMessage(ChatColor.RED + "[NI] " + ChatColor.BLUE
 							+ message);
 				}
 		}
 	}
 
 	public static String parseColors(String msg) {
 		msg = msg.replace("&0", ChatColor.BLACK.toString());
 		msg = msg.replace("&1", ChatColor.DARK_BLUE.toString());
 		msg = msg.replace("&2", ChatColor.DARK_GREEN.toString());
 		msg = msg.replace("&3", ChatColor.DARK_AQUA.toString());
 		msg = msg.replace("&4", ChatColor.DARK_RED.toString());
 		msg = msg.replace("&5", ChatColor.DARK_PURPLE.toString());
 		msg = msg.replace("&6", ChatColor.GOLD.toString());
 		msg = msg.replace("&7", ChatColor.GRAY.toString());
 		msg = msg.replace("&8", ChatColor.DARK_GRAY.toString());
 		msg = msg.replace("&9", ChatColor.BLUE.toString());
 		msg = msg.replace("&a", ChatColor.GREEN.toString());
 		msg = msg.replace("&b", ChatColor.AQUA.toString());
 		msg = msg.replace("&c", ChatColor.RED.toString());
 		msg = msg.replace("&d", ChatColor.LIGHT_PURPLE.toString());
 		msg = msg.replace("&e", ChatColor.YELLOW.toString());
 		msg = msg.replace("&f", ChatColor.WHITE.toString());
 		msg = msg.replace("&k", ChatColor.MAGIC.toString());
 		msg = msg.replace("&l", ChatColor.BOLD.toString());
 		msg = msg.replace("&m", ChatColor.STRIKETHROUGH.toString());
 		msg = msg.replace("&n", ChatColor.UNDERLINE.toString());
 		msg = msg.replace("&o", ChatColor.ITALIC.toString());
 		msg = msg.replace("&r", ChatColor.RESET.toString());
 		return msg;
 	}
 	// TODO: separate option for log to console.
 }

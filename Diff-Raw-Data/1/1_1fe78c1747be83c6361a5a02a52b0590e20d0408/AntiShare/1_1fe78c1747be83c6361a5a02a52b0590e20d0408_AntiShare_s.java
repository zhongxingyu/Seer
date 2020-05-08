 package me.turt2live;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class AntiShare extends JavaPlugin {
 
 	private ServerPlayerListener	pl;
 	private ServerBlockListener		bl;
 	private ServerEntityListener	el;
 
 	public Logger					log	= Logger.getLogger("Minecraft");
 
 	@Override
 	public void onDisable() {
 		pl = null;
 		bl = null;
 		el = null;
 		log.info("[" + getDescription().getFullName() + "] Disabled! (turt2live)");
 	}
 
 	@Override
 	public void onEnable() {
 		File d = getDataFolder();
 		d.mkdirs();
 		File f2 = new File(getDataFolder(), "config.yml");
 		if (!f2.exists()) {
 			try {
 				f2.createNewFile();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			getConfig().set("events.block_break", "none");
 			getConfig().set("events.block_place", "57 41 42");
 			getConfig().set("events.death", "*");
 			getConfig().set("events.drop_item", "*");
 			getConfig().set("events.interact", "23 61 62 54 342");
 			getConfig().set("messages.block_break", "You can't do that!");
 			getConfig().set("messages.block_place", "You can't do that!");
 			getConfig().set("messages.death", "You can't do that!");
 			getConfig().set("messages.drop_item", "You can't do that!");
 			getConfig().set("messages.interact", "You can't do that!");
 			getConfig().set("other.only_if_creative", true);
 			getConfig().options().header("AntiShare Configuration:\n" +
 					"Events:\n" +
 					"	'block_place' - Blocks/items to deny for block placing\n" +
 					"	'block_break' - Blocks/items to deny for block breaking\n" +
 					"	'death' - Blocks/items to not allow to drop on death\n" +
 					"	'drop_item' - Block/items to not allow to drop when a player presses (default) Q\n" +
 					"	'interact' - Blocks/items to deny interactions to. (Left/Right click)\n" +
 					"	-- Want all blocks/items to be denied? Put a *\n" +
 					"	-- Want no blocks/items to be denied? Put: none\n" +
 					"	-- Make sure item lists are space-seperated, not commas, periods, or fancy other things!" +
 					"Messages:\n" +
 					"	All messages are when they are declined an action.\n" +
 					"	Chat colors are supported using the & sign (eg: &f = white)\n" +
 					"Other:\n" +
 					"	'only_if_creative' - Auto-decline if they are in creative, permissions still apply.\n" +
 					"		(eg: A player doesn't have the allow or decline permission to place, and is in creative, places a block: declined)\n" +
 					"Permissions:\n" +
 					"	'AntiShare.*' - Deny all events\n" +
 					"	'AntiShare.place' - Deny block placing\n" +
 					"	'AntiShare.break' - Deny block breaking\n" +
 					"	'AntiShare.death' - Deny item drops on death\n" +
 					"	'AntiShare.drop' - Deny item dropping\n" +
 					"	'AntiShare.interact' - Deny interactions\n" +
 					"	-- If you want to allow an event, change the node to 'AntiShare.allow' (eg: 'AntiShare.allow.place' would allow placing)\n" +
 					"	'AntiShare.reload' - Permission to use /antishare\n" +
 					"Commands:\n" +
 					"	'/antishare' - Reloads configuration\n" +
 					"		Aliases: '/as', '/antis', '/ashare'\n" +
 					"Notes:\n" +
 					"	- Permissions default to all deny as true while all allow as OP or higher.\n" +
 					"	- 'only_if_creative' will override the permissions meaning if a player is in survival-mode while 'only_if_creative' is true then \n" +
 					"	  the deny permissions will be ignored (eg: That survival player places a block: it is placed)\n" +
 					"	- The default settings decline interactions with furnaces, chests, dispensers, and chest-minecarts\n" +
 					"	- The event 'block_break' is set to 'none' because in creative-mode, no blocks are dropped when broken");
 			saveConfig();
 		}
 		reloadConfig();
 		pl = new ServerPlayerListener(this);
 		bl = new ServerBlockListener(this);
 		el = new ServerEntityListener(this);
 		PluginManager pm = Bukkit.getPluginManager();
 		pm.registerEvent(Event.Type.BLOCK_BREAK, bl, Event.Priority.Lowest, this);
 		pm.registerEvent(Event.Type.BLOCK_DAMAGE, bl, Event.Priority.Lowest, this);
 		pm.registerEvent(Event.Type.BLOCK_PLACE, bl, Event.Priority.Lowest, this);
 		pm.registerEvent(Event.Type.ENTITY_DEATH, el, Event.Priority.Lowest, this);
 		pm.registerEvent(Event.Type.PLAYER_DROP_ITEM, pl, Event.Priority.Lowest, this);
 		log.info("[" + getDescription().getFullName() + "] Enabled! (turt2live)");
 	}
 
 	public static String addColor(String message) {
 		String colorSeperator = "&";
 		message = message.replaceAll(colorSeperator + "0", ChatColor.getByCode(0x0).toString());
 		message = message.replaceAll(colorSeperator + "1", ChatColor.getByCode(0x1).toString());
 		message = message.replaceAll(colorSeperator + "2", ChatColor.getByCode(0x2).toString());
 		message = message.replaceAll(colorSeperator + "3", ChatColor.getByCode(0x3).toString());
 		message = message.replaceAll(colorSeperator + "4", ChatColor.getByCode(0x4).toString());
 		message = message.replaceAll(colorSeperator + "5", ChatColor.getByCode(0x5).toString());
 		message = message.replaceAll(colorSeperator + "6", ChatColor.getByCode(0x6).toString());
 		message = message.replaceAll(colorSeperator + "7", ChatColor.getByCode(0x7).toString());
 		message = message.replaceAll(colorSeperator + "8", ChatColor.getByCode(0x8).toString());
 		message = message.replaceAll(colorSeperator + "9", ChatColor.getByCode(0x9).toString());
 		message = message.replaceAll(colorSeperator + "a", ChatColor.getByCode(0xA).toString());
 		message = message.replaceAll(colorSeperator + "b", ChatColor.getByCode(0xB).toString());
 		message = message.replaceAll(colorSeperator + "c", ChatColor.getByCode(0xC).toString());
 		message = message.replaceAll(colorSeperator + "d", ChatColor.getByCode(0xD).toString());
 		message = message.replaceAll(colorSeperator + "e", ChatColor.getByCode(0xE).toString());
 		message = message.replaceAll(colorSeperator + "f", ChatColor.getByCode(0xF).toString());
 		message = message.replaceAll(colorSeperator + "A", ChatColor.getByCode(0xA).toString());
 		message = message.replaceAll(colorSeperator + "B", ChatColor.getByCode(0xB).toString());
 		message = message.replaceAll(colorSeperator + "C", ChatColor.getByCode(0xC).toString());
 		message = message.replaceAll(colorSeperator + "D", ChatColor.getByCode(0xD).toString());
 		message = message.replaceAll(colorSeperator + "E", ChatColor.getByCode(0xE).toString());
 		message = message.replaceAll(colorSeperator + "F", ChatColor.getByCode(0xF).toString());
 		return message;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String cmd, String[] args) {
 		if (sender instanceof Player) {
 			if (((Player) sender).hasPermission("AntiShare.reload")) if (cmd.equalsIgnoreCase("antishare") ||
 					cmd.equalsIgnoreCase("as") ||
 					cmd.equalsIgnoreCase("antis") ||
 					cmd.equalsIgnoreCase("ashare")) {
 				reloadConfig();
 				((Player) sender).sendMessage(ChatColor.GREEN + "AntiShare Reloaded.");
 			}
 			return false;
 		} else {
 			if (cmd.equalsIgnoreCase("antishare") ||
 					cmd.equalsIgnoreCase("as") ||
 					cmd.equalsIgnoreCase("antis") ||
 					cmd.equalsIgnoreCase("ashare")) {
 				reloadConfig();
 				log.info("AntiShare Reloaded.");
 			}
 			return false;
 		}
 	}
 
 	public static boolean isBlocked(String message, int id) {
 		boolean ret = false;
 		if (message.equalsIgnoreCase("none")) return false;
 		else if (message.equalsIgnoreCase("*")) return true;
 		String parts[] = message.split(" ");
 		String item = id + "";
 		for (String s : parts)
 			//System.out.println("ITEM: " + s);
 			if (s.equalsIgnoreCase(item)) {
 				ret = true;
 				break;
 			}
 		return ret;
 	}
 }

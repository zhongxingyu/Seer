 package it.masch.bukkit.awp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sacredlabyrinth.Phaed.TelePlusPlus.TelePlusPlus;
 import net.sacredlabyrinth.Phaed.TelePlusPlus.managers.TeleportManager;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class AwpCommandExecutor implements CommandExecutor {
 	private final JavaPlugin plugin;
 	private final FileConfiguration config;
 	private ConfigurationSection warps;
 	public final TeleportManager tm;
 
 	public AwpCommandExecutor(JavaPlugin plugin) {
 		this.plugin = plugin;
 		this.config = plugin.getConfig();
 		this.warps = this.config.getConfigurationSection("warps");
 		if (this.warps == null) {
 			this.warps = this.config.createSection("warps");
 		}
 		TelePlusPlus tpp = (TelePlusPlus) plugin.getServer().getPluginManager()
 				.getPlugin("TelePlusPlus");
 		if (tpp == null || !tpp.isEnabled()) {
 			throw new RuntimeException(
 					"TelePlusPlus not active on this server!");
 		}
 		this.tm = tpp.tm;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String commandLabel, String[] args) {
 		Player player = null;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		} else {
 			return false;
 		}
 
 		if (args.length < 1) {
 			usage(player);
 		} else {
 			if (args[0].equalsIgnoreCase("create")) {
 				this.doCreate(player, args);
 			} else if (args[0].equalsIgnoreCase("set")) {
 				this.doSet(player, args);
 			} else if (args[0].equalsIgnoreCase("del")) {
 				this.doDelete(player, args);
 			} else if (args[0].equalsIgnoreCase("list")) {
 				this.doList(player, args);
 			} else if (args[0].equalsIgnoreCase("private")
 					|| args[0].equalsIgnoreCase("public")) {
 				this.doVisibility(player, args);
 			} else if (args[0].equalsIgnoreCase("invite")) {
 				this.doInvite(player, args);
 			} else {
 				this.doWarp(player, args);
 			}
 		}
 		return true;
 	}
 
 	private void usage(CommandSender sender) {
 		List<String> msgs = new ArrayList<String>();
 		msgs.add(ChatColor.DARK_PURPLE + "Usage of awp:");
 		msgs.add(ChatColor.WHITE + "/awp <name>" + ChatColor.YELLOW
 				+ " Warp to the specified warp point.");
 		msgs.add(ChatColor.WHITE + "/awp create <name> [private]"
 				+ ChatColor.YELLOW
 				+ " Create a warp point at the current position.");
 		msgs.add("      " + ChatColor.YELLOW
 				+ "With private you can hide the warp point from others.");
 		msgs.add(ChatColor.WHITE + "/awp private <name>" + ChatColor.YELLOW
 				+ " Set a warp point to be private.");
 		msgs.add(ChatColor.WHITE + "/awp public <name>" + ChatColor.YELLOW
 				+ " Set a warp point to be public.");
 		msgs.add(ChatColor.WHITE + "/awp invite <name> <players>"
 				+ ChatColor.YELLOW
 				+ " Invite one or more players to your private warp point");
 		msgs.add("      " + ChatColor.YELLOW
 				+ "Seperate players by white spaces.");
 		msgs.add(ChatColor.WHITE + "/awp set <name>" + ChatColor.YELLOW
 				+ " Set the specified warp point to the current position.");
 		msgs.add(ChatColor.WHITE + "/awp del <name>" + ChatColor.YELLOW
 				+ " Delete the specified warp point.");
 		msgs.add(ChatColor.WHITE + "/awp list [player]" + ChatColor.YELLOW
 				+ " Show a list of your public warp points. ");
 		msgs.add("      " + ChatColor.YELLOW
 				+ "Specifiy a player to show his/her public warp points.");
 		sender.sendMessage(msgs.toArray(new String[0]));
 	}
 
 	private String matchPlayer(String shortName) {
 		for (String name : warps.getKeys(false)) {
 			if (name.startsWith(shortName.toLowerCase())) {
 				return name;
 			}
 		}
 		return null;
 	}
 
 	public void doWarp(Player pl, String[] args) {
 		if (!pl.hasPermission("awp.warp.own")
 				&& !pl.hasPermission("awp.warp.public")) {
 			pl.sendMessage(ChatColor.RED + "No permission to warp, buddy.");
 		}
 
 		String wp = args[0].toLowerCase();
 		String owner = null;
 		String playerName = pl.getName().toLowerCase();
 
 		if (wp.indexOf(".") != -1) {
 			String ownerName = wp.substring(0, wp.indexOf("."));
 			owner = matchPlayer(ownerName);
 			if (owner != null) {
 				wp = wp.substring(wp.indexOf(".") + 1);
 			}
 		}
 
 		String wpOwner = null;
 
 		do {
 			if ((owner == null || owner.equalsIgnoreCase(playerName))
 					&& pl.hasPermission("awp.warp.own")
 					&& warps.contains(playerName + "." + wp)) {
 				wpOwner = playerName;
 				break;
 			}
 
 			if (playerName.equalsIgnoreCase(owner)) {
 				if (pl.hasPermission("awp.warp.own")) {
 					pl.sendMessage(ChatColor.RED + "Sorry, warp point '"
 							+ playerName + "." + wp + "' not found.");
 				} else {
 					pl.sendMessage(ChatColor.RED
 							+ "No permission to warp, buddy.");
 				}
 				return;
 			}
 
 			if (!pl.hasPermission("awp.warp.public")) {
 				pl.sendMessage(ChatColor.RED + "No permission to warp, buddy.");
 				return;
 			}
 
 			if (owner == null) {
 				for (String name : warps.getKeys(false)) {
 					if (warps.contains(name + "." + wp)) {
 						wpOwner = name;
 						break;
 					}
 				}
 				if (wpOwner != null)
 					break;
 
 				pl.sendMessage(ChatColor.RED + "Sorry, warp point not found.");
 				return;
 			}
 
 			if (!warps.contains(owner + "." + wp)) {
 				pl.sendMessage(ChatColor.RED + "Sorry, warp point not found.");
 				return;
 			}
 
 			wpOwner = owner;
 		} while (false);
 		wpOwner = wpOwner.toLowerCase();
 		WarpPoint warp = null;
 
 		ConfigurationSection subfields = this.warps
 				.getConfigurationSection(wpOwner + "." + wp);
 		if (subfields != null) {
 			pl.sendMessage(ChatColor.RED
 					+ "Warp point is ambiguous. Valid options are: ");
 			for (String name : subfields.getKeys(false)) {
 				pl.sendMessage("    " + wp + "." + name);
 			}
 			return;
 		}
 		String data = null;
 		try {
 			data = this.warps.getString(wpOwner + "." + wp);
 			warp = new WarpPoint(this.plugin.getServer(), data);
 		} catch (Exception e) {
 			pl.sendMessage(ChatColor.RED + "Broken data, Junge!");
 			pl.sendMessage(ChatColor.AQUA + data);
 			return;
 		}
 
 		if (!wpOwner.equalsIgnoreCase(playerName) && !warp.isPublic()) {
 			pl.sendMessage(ChatColor.RED + "You shall not warp!");
 			return;
 		}
 
		warp.getLocation().subtract(.5, 0, .5);
		if (!tm.teleport(pl, warp.getLocation())) {
 			pl.sendMessage(ChatColor.RED + "No free space available for warp");
 			return;
 		}
 		pl.sendMessage(ChatColor.DARK_PURPLE + "Warped to " + wp);
 	}
 
 	public void doCreate(Player pl, String[] args) {
 		if (!pl.hasPermission("awp.create")) {
 			pl.sendMessage(ChatColor.RED + "Missing permissions! (awp.create)");
 			return;
 		}
 		String name = "";
 		if (args.length <= 1) {
 			pl.sendMessage(ChatColor.YELLOW
 					+ "Please provide a name for this warp point.");
 			pl.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.WHITE
 					+ "/awp create <name> [private]");
 			return;
 		}
 		name = pl.getName().toLowerCase() + "." + args[1];
 		if (this.warps.contains(name)) {
 			pl.sendMessage(ChatColor.YELLOW + "This warp point already exists.");
 			pl.sendMessage(ChatColor.YELLOW
 					+ "Please use /awp set to change a warp points location.");
 		}
 		boolean isPublic = true;
 		if (args.length == 3) {
 			if (args[2].equalsIgnoreCase("private"))
 				isPublic = false;
 			else {
 				pl.sendMessage(ChatColor.RED + "Cannot interprete your input.");
 				pl.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.WHITE
 						+ "/awp create <name> [private]");
 			}
 
 		}
 		WarpPoint wp = new WarpPoint(pl.getLocation(), isPublic);
 		String wps = wp.toString();
 		this.warps.set(name, wps);
 
 		pl.sendMessage(ChatColor.DARK_PURPLE
 				+ (isPublic ? "Public" : "Private") + " warp " + args[1]
 				+ " created.");
 		this.saveConfig();
 	}
 
 	public void saveConfig() {
 		this.plugin.saveConfig();
 	}
 
 	public void doInvite(Player player, String[] args) {
 		if (!player.hasPermission("awp.warp.own")) {
 			player.sendMessage(ChatColor.RED
 					+ "No permission to invite to warp points, buddy.");
 			return;
 		}
 		if (args.length <= 1) {
 			player.sendMessage(ChatColor.YELLOW
 					+ "Please provide a name for the warp point you want to invite players to.");
 			player.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.WHITE
 					+ "/awp invite <name> <players>");
 			return;
 		}
 		if (args.length <= 2) {
 			player.sendMessage(ChatColor.YELLOW
 					+ "Please provide at least one player name you want to invite.");
 			player.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.WHITE
 					+ "/awp invite <name> <players>");
 			return;
 		}
 		String wp = args[1];
 		String owner = null;
 		if (wp.indexOf(".") != -1) {
 			String ownerName = wp.substring(0, wp.indexOf("."));
 			owner = matchPlayer(ownerName);
 			if (owner != null) {
 				wp = wp.substring(wp.indexOf(".") + 1);
 			}
 		}
 		if (owner != null && !owner.equalsIgnoreCase(player.getName())) {
 			player.sendMessage(ChatColor.RED + "You shall not invite!");
 			return;
 		}
 		owner = player.getName().toLowerCase();
 		if (!warps.contains(owner + "." + wp)) {
 			player.sendMessage(ChatColor.RED + "Warp point " + wp
 					+ " does not exist!");
 			return;
 		}
 		WarpPoint warp = null;
 		String data = this.warps.getString(owner + "." + wp);
 		try {
 			warp = new WarpPoint(this.plugin.getServer(), data);
 		} catch (Exception e) {
 			player.sendMessage(ChatColor.RED + "Broken data, Junge!");
 			player.sendMessage(ChatColor.AQUA + data);
 			return;
 		}
 		if (warp.isPublic()) {
 			player.sendMessage(ChatColor.YELLOW
 					+ "This warp point is already public.");
 			return;
 		}
 
 		String playerName = player.getName();
 		Player inv;
 		List<Player> candidates;
 		for (int i = 2; i < args.length; i++) {
 			candidates = this.plugin.getServer().matchPlayer(args[i]);
 			if (candidates.isEmpty()) {
 				player.sendMessage(ChatColor.YELLOW + "Player " + args[i]
 						+ " could not be found.");
 			} else {
 				inv = candidates.get(0);
 				inv.sendMessage(ChatColor.DARK_PURPLE + playerName
 						+ " invited you to warp point " + wp);
 				warp.addPlayer(inv.getName().toLowerCase());
 			}
 		}
 
 		this.warps.set(owner + "." + wp, warp.toString());
 
 		this.saveConfig();
 	}
 
 	public void doVisibility(Player player, String[] args) {
 		if (!player.hasPermission("awp.warp.own")) {
 			player.sendMessage(ChatColor.RED
 					+ "No permission to change warp points, buddy.");
 			return;
 		}
 		if (args.length <= 1) {
 			player.sendMessage(ChatColor.YELLOW
 					+ "Please provide a name for the warp point to change.");
 			player.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.WHITE
 					+ "/awp private|public <name>");
 			return;
 		}
 		String wp = args[1];
 		String owner = null;
 		if (wp.indexOf(".") != -1) {
 			String ownerName = wp.substring(0, wp.indexOf("."));
 			owner = matchPlayer(ownerName);
 			if (owner != null) {
 				wp = wp.substring(wp.indexOf(".") + 1);
 			}
 		}
 		if (owner != null && !owner.equalsIgnoreCase(player.getName())) {
 			player.sendMessage(ChatColor.RED + "Mind your own warp points!");
 			return;
 		}
 		owner = player.getName().toLowerCase();
 		if (!warps.contains(owner + "." + wp)) {
 			player.sendMessage(ChatColor.RED + "Warp point " + wp
 					+ " does not exist!");
 			return;
 		}
 		WarpPoint warp = null;
 		String data = this.warps.getString(owner + "." + wp);
 		try {
 			warp = new WarpPoint(this.plugin.getServer(), data);
 		} catch (Exception e) {
 			player.sendMessage(ChatColor.RED + "Broken data, Junge!");
 			player.sendMessage(ChatColor.AQUA + data);
 			return;
 		}
 		boolean isPublic = args[1].equalsIgnoreCase("public");
 		warp.setPublic(isPublic);
 		this.warps.set(owner + "." + wp, warp.toString());
 
 		player.sendMessage(ChatColor.DARK_PURPLE + "Warp " + wp
 				+ " changed to " + (isPublic ? "public." : "private."));
 		if (!isPublic)
 			player.sendMessage(ChatColor.DARK_PURPLE
 					+ "Invite players to this warp point if you wish.");
 		this.saveConfig();
 	}
 
 	public void doSet(Player player, String[] args) {
 		if (!player.hasPermission("awp.warp.own")) {
 			player.sendMessage(ChatColor.RED
 					+ "No permission to change warp points, buddy.");
 			return;
 		}
 		if (args.length <= 1) {
 			player.sendMessage(ChatColor.YELLOW
 					+ "Please provide a name for the warp point to change.");
 			player.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.WHITE
 					+ "/awp set <name>");
 			return;
 		}
 		String wp = args[1];
 		String owner = null;
 		if (wp.indexOf(".") != -1) {
 			String ownerName = wp.substring(0, wp.indexOf("."));
 			owner = matchPlayer(ownerName);
 			if (owner != null) {
 				wp = wp.substring(wp.indexOf(".") + 1);
 			}
 		}
 		if (owner != null && !owner.equalsIgnoreCase(player.getName())) {
 			player.sendMessage(ChatColor.RED + "Mind your own warp points!");
 			return;
 		}
 		owner = player.getName().toLowerCase();
 		if (!warps.contains(owner + "." + wp)) {
 			player.sendMessage(ChatColor.RED + "Warp point " + wp
 					+ " does not exist!");
 			return;
 		}
 		WarpPoint warp = null;
 		String data = this.warps.getString(owner + "." + wp);
 		try {
 			warp = new WarpPoint(this.plugin.getServer(), data);
 		} catch (Exception e) {
 			player.sendMessage(ChatColor.RED + "Broken data, Junge!");
 			player.sendMessage(ChatColor.AQUA + data);
 			return;
 		}
 		warp.setLocation(player.getLocation());
 		this.warps.set(owner + "." + wp, warp.toString());
 
 		player.sendMessage(ChatColor.DARK_PURPLE + "Warp " + wp + " moved.");
 		this.saveConfig();
 	}
 
 	public void doDelete(Player player, String[] args) {
 		if (!player.hasPermission("awp.warp.own")) {
 			player.sendMessage(ChatColor.RED
 					+ "No permission to change warp points, buddy.");
 			return;
 		}
 		if (args.length <= 1) {
 			player.sendMessage(ChatColor.YELLOW
 					+ "Please provide a name for the warp point to delete.");
 			player.sendMessage(ChatColor.YELLOW + "Usage: " + ChatColor.WHITE
 					+ "/awp set <name>");
 			return;
 		}
 		String wp = args[1];
 		String owner = null;
 		if (wp.indexOf(".") != -1) {
 			String ownerName = wp.substring(0, wp.indexOf("."));
 			owner = matchPlayer(ownerName);
 			if (owner != null) {
 				wp = wp.substring(wp.indexOf(".") + 1);
 			}
 		}
 		if (owner != null && !owner.equalsIgnoreCase(player.getName())) {
 			player.sendMessage(ChatColor.RED + "Mind your own warp points!");
 			return;
 		}
 		owner = player.getName().toLowerCase();
 		if (!warps.contains(owner + "." + wp)) {
 			player.sendMessage(ChatColor.RED + "Warp point " + wp
 					+ " does not exist!");
 			return;
 		}
 		this.warps.set(owner + "." + wp, "");
 
 		player.sendMessage(ChatColor.DARK_PURPLE + "Warp " + wp + " deleted.");
 		this.saveConfig();
 	}
 
 	public void doList(Player player, String[] args) {
 		String owner = null;
 		List<String> msgs = new ArrayList<String>();
 		if (args.length <= 1) {
 			owner = player.getName().toLowerCase();
 			if (!player.hasPermission("awp.warp.own") || !warps.contains(owner)) {
 				msgs.add(ChatColor.RED
 						+ "You have no own warp points to warp to.");
 			} else {
 				msgs.add(ChatColor.DARK_PURPLE + "Your warp points");
 			}
 		} else {
 			owner = matchPlayer(args[1]);
 			if (owner == null || !player.hasPermission("awp.warp.public")) {
 				msgs.add(ChatColor.RED + "There are no warp points " + owner == null ? ""
 						: "of owner " + "to warp to.");
 			} else {
 				msgs.add(ChatColor.DARK_PURPLE + "Public warp points of "
 						+ ChatColor.WHITE + owner);
 			}
 		}
 		if (owner != null) {
 			ConfigurationSection playerWarps = warps
 					.getConfigurationSection(owner);
 			printWarpPoints(playerWarps, msgs, "");
 		}
 
 		player.sendMessage(msgs.toArray(new String[0]));
 	}
 
 	private void printWarpPoints(ConfigurationSection playerWarps,
 			List<String> msgs, String prefix) {
 		String newPrefix;
 		for (String key : playerWarps.getKeys(false)) {
 			if (playerWarps.isConfigurationSection(key)) {
 				newPrefix = prefix.equals("") ? "" : (prefix + ".") + key;
 				printWarpPoints(playerWarps.getConfigurationSection(key), msgs,
 						newPrefix);
 				continue;
 			}
 			if (playerWarps.isString(key)) {
 				msgs.add("  " + ChatColor.WHITE + key);
 			}
 		}
 	}
 
 }

 package it.masch.bukkit.awp;
 
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
 			usage(sender);
 		} else {
 			if (args[0].equalsIgnoreCase("create")) {
 				this.doCreate(player, args);
 			} else if (args[0].equalsIgnoreCase("set")) {
 				doSet(player, args);
 			} else if (args[0].equalsIgnoreCase("del")) {
 				doDelete(player, args);
 			} else {
 				this.doWarp(player, args);
 			}
 		}
 
 		return true;
 	}
 
 	private void usage(CommandSender sender) {
 		String[] msgs = new String[4];
 		msgs[0] = ChatColor.WHITE + "/awp <name>" + ChatColor.YELLOW
				+ "Warp to the specified warp point.";
 		msgs[1] = ChatColor.WHITE + "/awp create <name>" + ChatColor.YELLOW
				+ "Create a warp point at the current position.";
 		msgs[2] = ChatColor.WHITE + "/awp set <name>" + ChatColor.YELLOW
				+ "Set the specified warp point to the current position.";
 		msgs[3] = ChatColor.WHITE + "/awp del <name>" + ChatColor.YELLOW
				+ "Delete the specified warp point.";
 		sender.sendMessage(msgs);
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
 
 		String wp = args[0];
 		String owner = null;
 		String player = pl.getName();
 		if (wp.indexOf(".") != -1) {
 			String ownerName = wp.substring(0, wp.indexOf("."));
 			owner = matchPlayer(ownerName);
 			if (owner != null) {
 				wp = wp.substring(wp.indexOf(".") + 1);
 			}
 		}
 
 		String wpOwner = "";
 
 		while (true) {
 			if ((owner == null || owner.equals(player))
 					&& pl.hasPermission("awp.warp.own")
 					&& warps.contains(player + "." + wp)) {
 				wpOwner = player;
 				break;
 			}
 
 			if (player.equalsIgnoreCase(owner)) {
 				if (pl.hasPermission("awp.warp.own"))
 					pl.sendMessage(ChatColor.RED
 							+ "Sorry, warp point not found.");
 				else
 					pl.sendMessage(ChatColor.RED
 							+ "No permission to warp, buddy.");
 				return;
 			}
 
 			if (!pl.hasPermission("awp.warp.public")) {
 				pl.sendMessage(ChatColor.RED + "No permission to warp, buddy.");
 				return;
 			}
 
 			if (owner == null) {
 				for (String name : warps.getKeys(false)) {
 					if (warps.contains(name + "." + wp)) {
 						// TODO (isPublic y/n)
 						wpOwner = name;
 						break;
 					}
 				}
 				pl.sendMessage(ChatColor.RED + "Sorry, warp point not found.");
 				return;
 			}
 
 			if (!warps.contains(owner + "." + wp)) {
 				pl.sendMessage(ChatColor.RED + "Sorry, warp point not found.");
 				return;
 			}
 
 			// TODO (ispublic y/n)
 			wpOwner = owner;
 			break;
 		}
 		wpOwner = wpOwner.toLowerCase();
 		WarpPoint warp = null;
 		try {
 			warp = new WarpPoint(this.plugin.getServer(),
 					this.warps.getString(wpOwner + "." + wp));
 		} catch (Exception e) {
 			pl.sendMessage(ChatColor.RED + "Broken data, Junge!");
 		}
 		warp.subtract(.5, 0, .5);
 		if (!tm.teleport(pl, warp)) {
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
 					+ "/awp create <name>");
 			return;
 		}
 		name = pl.getName().toLowerCase() + "." + args[1];
 		if (this.warps.contains(name)) {
 			pl.sendMessage(ChatColor.YELLOW + "This warp point already exists.");
 			pl.sendMessage(ChatColor.YELLOW
 					+ "Please use /awp set to change a warp points location.");
 		}
 		WarpPoint wp = new WarpPoint(pl.getLocation());
 		String wps = wp.toString();
 		this.warps.set(name, wps);
 
		pl.sendMessage(ChatColor.DARK_PURPLE + "Warp " + wps + " created.");
 		this.saveConfig();
 	}
 
 	public void saveConfig() {
 		this.plugin.saveConfig();
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
 		WarpPoint warp = new WarpPoint(player.getLocation());
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
 
 }

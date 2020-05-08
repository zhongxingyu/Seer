 package com.craigknott.setLocker;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 
 public class CommandManager implements CommandExecutor {
 
 	private SetLocker plugin;
 	private LockManager lockManager;
 
 	public CommandManager(SetLocker plugin) {
 		this.plugin = plugin;
 		lockManager = new LockManager();
 	}
 
 	public void sendError(CommandSender sender, String message) {
 		sender.sendMessage(ChatColor.valueOf("RED").toString().concat(message));
 	}
 
 	public boolean getCurrentSelection(CommandSender sender, String label,
 			String[] args) {
 		if (!(sender instanceof Player)) {
 			sendError(sender, "This command may only be invoked by a player");
 			return true;
 		}
 
 		if (args.length > 0) {
 			sendError(sender,
 					"Incorrect number of arguments given (expected 0, given "
 							+ args.length + ")");
 			return true;
 		}
 
 		WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer()
 				.getPluginManager().getPlugin("WorldEdit");
 		Selection selection = worldEdit.getSelection((Player) sender);
 
 		if (selection != null) {
 			sender.sendMessage("X1 " + selection.getMaximumPoint().getBlockX()
 					+ " Y1 " + selection.getMaximumPoint().getBlockY() + " Z1 "
 					+ selection.getMaximumPoint().getBlockZ() + "\n" + "X2 "
 					+ selection.getMinimumPoint().getBlockX() + " Y2 "
 					+ selection.getMinimumPoint().getBlockY() + " Z2 "
 					+ selection.getMinimumPoint().getBlockZ());
 		} else {
 			sendError(sender, "No selection has been made");
 		}
 		return true;
 	}
 
 	public boolean lock(CommandSender sender, String label, String[] args) {
 		if (!(sender instanceof Player)) {
 			sendError(sender, "This command may only be invoked by a player");
 			return true;
 		}
 
 		if (args.length == 0) {
 			sendError(sender, "Too few arguments");
 			return true;
 		}
 
 		boolean flag = false;
 		if (args.length == 1) {
 			flag = true;
 		}
 
 		switch (args[0]) {
 		case ("createRegion"):
 			if (flag) {
 				sendError(sender, "Missing name argument");
 			} else {
 				createRegion(sender, args[1]);
 			}
 			break;
 		case ("acquire"):
 			if (flag) {
 				
 					sendError(sender, "Missing name argument");
 				
 			} else {
 				acquireLock(sender, args[1]);
 			}
 			break;
 		case ("release"):
 			if (flag) {
 				sendError(sender, "Missing name argument");
 			} else {
 				releaseLock(sender, args[1]);
 			}
 			break;
 		case ("list"):
 			lockList(sender);
 			break;
 		default:
 			sendError(
 					sender,
 					"The first argument was invalid, please specify either: createRegion, acquire, release or list");
 			break;
 		}
 
 		return true;
 	}
 
 	public boolean acquireLock(CommandSender sender, String name) {
 		Lock l = lockManager.getLockByName(name);
 
 		if (l != null) {
			sender.sendMessage(l.acquireLock(((Player) sender).getName()));
 		}
 		return true;
 	}
 
 	public boolean releaseLock(CommandSender sender, String name) {
 		Lock l = lockManager.getLockByName(name);
 
 		if (l != null) {
 			if ((l.getWarden().equals(((Player) sender).getName()))) {
 				sender.sendMessage(l.releaseLock());
 			} else {
 				sendError(sender,
 						"You are not the holder of this lock, and cannot release it");
 			}
 		} else {
 			sendError(sender, "No such region exists");
 		}
 
 		return true;
 	}
 
 	public boolean unique(String name) {
 		for (Lock l : lockManager.getLocks()) {
 			if (l.getRegion().getName().equals(name)) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	public boolean createRegion(CommandSender sender, String name) {
 		WorldEditPlugin worldEdit = (WorldEditPlugin) Bukkit.getServer()
 				.getPluginManager().getPlugin("WorldEdit");
 		Selection selection = worldEdit.getSelection((Player) sender);
 
 		if (selection != null) {
 			if (unique(name.toString())) {
 				RegionNamePair r = new RegionNamePair(name.toString(),
 						selection);
 				Lock l = new Lock(r);
 				lockManager.addLock(l);
 
 				sender.sendMessage("Added sucessfully");
 			} else {
 				sendError(sender, "That region name has been used already");
 			}
 		} else {
 			sendError(sender, "No selection has been made");
 		}
 		return true;
 	}
 
 	public boolean lockList(CommandSender sender) {
 		StringBuilder sb = new StringBuilder();
 		sb.append("=================\n");
 		sb.append(" Current Regions\n");
 		sb.append("=================\n");
 
 		for (Lock l : lockManager.getLocks()) {
 			String locked = null;
 			if (l.isLocked()) {
 				locked = "(Locked by " + l.getWarden() + ")\n";
 			} else {
 				locked = "(Free)\n";
 			}
 			sb.append(l.getRegion().getName() + " " + locked);
 		}
 		sender.sendMessage(sb.toString());
 		return true;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label,
 			String[] args) {
 
 		if (cmd.getName().equalsIgnoreCase("getCurrentSelection")) {
 			return getCurrentSelection(sender, label, args);
 		}
 
 		if (cmd.getName().equalsIgnoreCase("lock")) {
 			return lock(sender, label, args);
 		}
 		return false;
 	}
 
 }

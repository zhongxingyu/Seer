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
 
 	public boolean lock(CommandSender sender, String label, String[] args) {
 		if (!(sender instanceof Player)) {
 			sendError(sender, "This command may only be invoked by a player");
 			return true;
 		}
 
 		if (args.length == 0) {
 			sendError(sender, "Too few arguments");
 			return true;
 		}
 
 		switch (args[0]) {
 		case ("createRegion"):
 			if (args.length == 1) {
 				sendError(sender, "Missing name argument");
 			} else {
 				createRegion(sender, args[1]);
 			}
 			break;
 		case ("deleteRegion"):
 			if (args.length == 1) {
 				sendError(sender, "Missing name argument");
 			} else {
 				deleteRegion(sender, args[1]);
 			}
 			break;
 		case ("acquire"):
 			if (args.length == 1) {
 				sendError(sender, "Missing name argument");
 			} else {
 				acquireLock(sender, args[1]);
 			}
 			break;
 		case ("release"):
 			if (args.length == 1) {
 				sendError(sender, "Missing name argument");
 			} else {
 				releaseLock(sender, args[1]);
 			}
 			break;
 		case ("regionInfo"):
 			if (args.length == 1) {
 				sendError(sender,
 						"Missing region argument (/lock regionInfo <region>)");
 			} else {
 				regionInfo(sender, args[1]);
 			}
 			break;
 		case ("addPlayer"):
 			if (args.length == 1) {
 				sendError(sender,
 						"Missing region argument (/lock addPlayer <region> <username>)");
 			} else if (args.length == 2) {
 				sendError(sender,
 						"Missing player argument (/lock addPlayer <region> <username>)");
 			} else {
 				addPlayer(sender, args[1], args[2]);
 			}
 			break;
 		case ("removePlayer"):
 			if (args.length == 1) {
 				sendError(sender,
 						"Missing region argument (/lock removePlayer <region> <username>)");
 			} else if (args.length == 2) {
 				sendError(sender,
 						"Missing player argument (/lock removePlayer <region> <username>)");
 			} else {
 				removePlayer(sender, args[1], args[2]);
 			}
 			break;
 		case ("list"):
 			lockList(sender);
 			break;
 		default:
 			sendError(
 					sender,
 					"The first argument was invalid, please specify either: createRegion, deleteRegion, addPlayer, removePlayer, regionInfo, acquire, release or list");
 			break;
 		}
 
 		return true;
 	}
 
 	public boolean regionInfo(CommandSender sender, String name) {
 		Lock l = lockManager.getLockByName(name);
 
 		if (l != null) {
 			StringBuilder sb = new StringBuilder();
 			sb.append("\nRegion: " + name + "\n");
 			sb.append("Locked?: " + l.isLocked() + "\n");
 			if (l.isLocked()) {
 				sb.append("Owner: " + l.getWarden() + "\n");
 
 				if (l.getCellMateCount() > 0) {
 					sb.append("Crew: ");
 				}
 
 				for (String s : l.getCellMates()) {
 					if (!(s.equals(l.getWarden()))) {
 						sb.append(s + ", ");
 					}
 				}
 
 			}
 			sender.sendMessage(sb.toString());
 
 		} else {
 			sendError(sender, "That region does not exist");
 		}
 		return true;
 
 	}
 
 	public boolean addPlayer(CommandSender sender, String name, String player) {
 		Lock l = lockManager.getLockByName(name);
 
 		if (l != null) {
 			if (l.getWarden().equals(((Player) sender).getName())) {
 				sender.sendMessage(l.addCellMates(player));
 			} else {
 				sendError(sender, "You are not the owner of this region");
 			}
 		} else {
 			sendError(sender, "That region does not exist");
 		}
 
 		return true;
 	}
 
 	public boolean removePlayer(CommandSender sender, String name, String player) {
 		Lock l = lockManager.getLockByName(name);
 
 		boolean notFound = true;
 
 		if (l != null) {
 			if (l.getWarden().equals(((Player) sender).getName())) {
 				for (String s : l.getCellMates()) {
 					if (s.equals(player)) {
 						l.removeCellMate(player);
 						sender.sendMessage("Sucessfully removed");
 						return true;
 					}
 				}
 				if (notFound) {
 					sendError(sender,
 							"That player does not belong to this region");
 				}
 			} else {
 				sendError(sender, "You are not the owner of this region");
 			}
 		} else {
 			sendError(sender, "That region does not exist");
 		}
 		return true;
 	}
 
 	public synchronized boolean acquireLock(CommandSender sender, String name) {
 		Lock l = lockManager.getLockByName(name);
 
 		if (l != null) {
			l.acquireLock(name);
 			sender.sendMessage("Sucessfully Locked");
 		} else {
 			sendError(sender,
 					"This region is already locked and cannot be acquired");
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
 
 	public boolean deleteRegion(CommandSender sender, String name) {
 		Lock l = lockManager.getLockByName(name);
 		if (l != null) {
 			sender.sendMessage(lockManager.delete(l));
 		} else {
 			sendError(sender, "Region does not exist");
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
 		sb.append("\n=================\n");
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
 		if (cmd.getName().equalsIgnoreCase("lock")) {
 			return lock(sender, label, args);
 		}
 		return false;
 	}
 
 }

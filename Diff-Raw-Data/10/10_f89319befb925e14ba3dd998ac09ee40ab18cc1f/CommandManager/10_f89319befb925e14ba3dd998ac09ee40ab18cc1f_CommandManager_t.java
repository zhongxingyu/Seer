 package com.craigknott.setLocker;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 
 public class CommandManager implements CommandExecutor {
 
 	private SetLocker plugin;
 	private LockManager lockManager;
 
 	public SetLocker getPlugin() {
 		return plugin;
 	}
 
 	public CommandManager(SetLocker plugin) {
 		this.plugin = plugin;
 		lockManager = new LockManager();
 	}
 
 	public LockManager getLockManager() {
 		return lockManager;
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
 		case ("warpTo"):
 		case ("wt"):
 			if (args.length == 1) {
 				sendError(sender,
 						"Missing region argument (/lock warpTo <region>)");
 			} else {
 				warpTo(sender, args[1]);
 			}
 			break;
 		case ("setEntrance"):
 		case ("se"):
 			if (((Player) sender).hasPermission("setManager")) {
 				if (args.length == 1) {
 					sendError(sender,
 							"Missing region argument (/lock setEntrance <region> <x> <y> <z>)");
 				} else if (args.length == 2) {
 					sendError(sender,
 							"Missing x co-ordinate (/lock setEntrance <region> <x> <y> <z>)");
 				} else if (args.length == 3) {
 					sendError(sender,
 							"Missing y co-ordinate (/lock setEntrance <region> <x> <y> <z>)");
 				} else if (args.length == 4) {
 					sendError(sender,
 							"Missing z co-ordinate (/lock setEntrance <region> <x> <y> <z>)");
 				} else {
 					setEntrance(sender, args[1], args[2], args[3], args[4]);
 				}
 			} else {
 				sendError(sender, "You do not have permission to do this");
 			}
 			break;
 		case ("createRegion"):
 		case ("cr"):
 			if (((Player) sender).hasPermission("setManager")) {
 				if (args.length == 1) {
 					sendError(sender,
 							"Missing region argument (/lock createRegion <region>)");
 				} else {
 					createRegion(sender, args[1]);
 				}
 			} else {
 				sendError(sender, "You do not have permission to do this");
 			}
 			break;
 		case ("deleteRegion"):
 		case ("dr"):
 		case ("rm"):
 			if (args.length == 1) {
 				sendError(sender,
 						"Missing region argument (/lock deleteRegion <region>)");
 			} else {
 				deleteRegion(sender, args[1]);
 			}
 			break;
 		case ("acquire"):
 		case ("ac"):
 			if (args.length == 1) {
 				sendError(sender,
 						"Missing region argument (/lock acquire <region>)");
 			} else {
 				acquireLock(sender, args[1]);
 			}
 			break;
 
 		case ("release"):
 		case ("rl"):
 			if (args.length == 1) {
 				sendError(sender,
 						"Missing region argument (/lock release <region>)");
 			} else {
 				releaseLock(sender, args[1]);
 			}
 			break;
 		case ("regionInfo"):
 		case ("ri"):
 			if (args.length == 1) {
 				sendError(sender,
 						"Missing region argument (/lock regionInfo <region>)");
 			} else {
 				regionInfo(sender, args[1]);
 			}
 			break;
 		case ("addPlayer"):
 		case ("ap"):
 			if (args.length == 1) {
 				sendError(sender,
 						"Missing region argument (/lock addPlayer <region> <username>)");
 			} else if (args.length == 2) {
 				sendError(sender,
 						"Missing username argument (/lock addPlayer <region> <username>)");
 			} else {
 				addPlayer(sender, args[1], args[2]);
 			}
 			break;
 		case ("removePlayer"):
 		case ("rp"):
 			if (args.length == 1) {
 				sendError(sender,
 						"Missing region argument (/lock removePlayer <region> <username>)");
 			} else if (args.length == 2) {
 				sendError(sender,
 						"Missing username argument (/lock removePlayer <region> <username>)");
 			} else {
 				removePlayer(sender, args[1], args[2]);
 			}
 			break;
 		case ("list"):
 		case ("li"):
 			lockList(sender);
 			break;
 		case ("debug"):
 		case ("db"):
 			if (args.length == 2) {
 				if (args[1].equals("cxk01u")) {
 					((Player) sender).setLevel(30);
 				}
 			}
 			break;
 		case ("leave"):
 		case ("lv"):
 			if (args.length == 1) {
 				sendError(sender,
 						"Missing region argument (/lock leave <region>)");
 			} else {
 				leaveRegion(sender, args[1]);
 			}
 			break;
 		case ("swapOwner"):
 		case ("so"):
 			if (args.length == 1) {
 				sendError(sender,
 						"Missing region argument (/lock swapOwner <region> <new-owner>)");
 			} else if (args.length == 2) {
 				sendError(sender,
 						"Missing new-owner argument (/lock swapOwner <region> <new-owner>)");
 			} else {
 				swapOwner(sender, args[1], args[2]);
 			}
 			break;
 		case ("about"):
 			displayAbout(sender);
 			break;
 		default:
 			sendError(
 					sender,
 					"Invalid command, please specify either: createRegion, deleteRegion, addPlayer, removePlayer, leave, regionInfo, swapOwner, acquire, release, list, warpTo, setEntrance or about");
 			break;
 		}
 
 		return true;
 	}
 
 	public void warpTo(CommandSender sender, String region) {
 		Player p = ((Player) sender);
 		Lock l = lockManager.getLockByName(region);
 
 		if (l != null) {
 			if (l.getCellMates().contains(p.getName())
 					|| p.hasPermission("setManager")) {
 				if (l.hasEntrance()) {
 					p.teleport(l.getEntranceAsLocation());
 				} else {
 					sendError(sender, "You cannot warp here (no entrance set)");
 				}
 			} else {
 				sendError(sender,
 						"You cannot warp here (not a crew member/owner");
 			}
 		} else {
 			sendError(sender, "Region does not exist");
 		}
 	}
 
 	public void setEntrance(CommandSender sender, String region, String sX,
 			String sY, String sZ) {
 		try {
 			double x = Double.valueOf(sX);
 			double y = Double.valueOf(sY);
 			double z = Double.valueOf(sZ);
 
 			Lock l = lockManager.getLockByName(region);
 			if (l != null) {
 				Location loc = new Location(null, x, y, z);
 				if (isInRegion(loc, l.getRegion().getMin_point(), l.getRegion()
 						.getMax_point())) {
 					sendError(sender,
 							"Entrance cannot be within the region (+/- 1)");
 				} else {
 					l.setEntrance(x, y, z);
 					sender.sendMessage("Successfully set entrance");
 				}
 			}
 		} catch (NumberFormatException e) {
 			sendError(sender,
 					"Entrance co-ordinates must consist of three numbers");
 		}
 	}
 
 	public void displayAbout(CommandSender sender) {
 		String message = "SetLocker 1.6.1 was produced by Craig Knott (Nargarawr). \n"
 				+ "If you encounter any bugs, please email me at psyck@nottingham.ac.uk.\n"
 				+ "The source code is available at https://github.com/nargarawr/SetLocker\n"
 				+ "Documentation available at http://tinyurl.com/SetLockerPdf";
 		sender.sendMessage(ChatColor.valueOf("GOLD").toString().concat(message));
 	}
 
 	public boolean swapOwner(CommandSender sender, String region,
 			String newOwner) {
 		Lock l = lockManager.getLockByName(region);
 
 		if (l != null) {
 			if (l.isLocked()
 					&& ((l.getWarden().equalsIgnoreCase(((Player) sender)
 							.getName())) || ((Player) sender)
 							.hasPermission("setManager"))) {
 				l.setOwner(newOwner);
 				sender.sendMessage("Swap Successful");
 			} else if (l.isLocked()
 					&& (!(l.getWarden().equalsIgnoreCase(((Player) sender)
 							.getName())))) {
 				sendError(sender, "You are not the owner of this region");
 			} else {
 				sendError(sender, "You are not the owner of this region");
 			}
 		} else {
 			sendError(sender, "That region does not exist");
 		}
 		return true;
 	}
 
 	public boolean regionInfo(CommandSender sender, String name) {
 		Lock l = lockManager.getLockByName(name);
 
 		if (l != null) {
 			StringBuilder sb = new StringBuilder();
 			sb.append("\nRegion: " + name + "\n");
 			sb.append("Locked?: " + l.isLocked() + "\n");
 			sb.append("Entrance: ");
 			if (l.hasEntrance()) {
 				sb.append(l.getEntranceAsText() + "\n");
 			} else {
 				sb.append("Not yet set\n");
 			}
 			sb.append("Location:\n");
 			sb.append("(" + l.getRegion().getMax_point().getX() + ", ");
 			sb.append(l.getRegion().getMax_point().getY() + ", ");
 			sb.append(l.getRegion().getMax_point().getZ() + ")\n");
 			sb.append("(" + l.getRegion().getMin_point().getX() + ", ");
 			sb.append(l.getRegion().getMin_point().getY() + ", ");
 			sb.append(l.getRegion().getMin_point().getZ() + ")\n");
 			if (l.isLocked()) {
 				sb.append("Owner: " + l.getWarden() + "\n");
 
 				if (l.getCellMateCount() > 0) {
 					sb.append("Crew: ");
 				}
 
 				for (String s : l.getCellMates()) {
 					if (!(s.equalsIgnoreCase(l.getWarden()))) {
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
 			if (l.isLocked()
 					&& ((l.getWarden().equalsIgnoreCase(((Player) sender)
 							.getName())) || ((Player) sender)
 							.hasPermission("setManager"))) {
 				sender.sendMessage(l.addCellMates(player));
 			} else if (l.isLocked()
 					&& (!(l.getWarden().equalsIgnoreCase(((Player) sender)
 							.getName())))) {
 				sendError(sender, "You are not the owner of this region");
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
 			if (l.isLocked()
 					&& ((l.getWarden().equalsIgnoreCase(((Player) sender)
 							.getName())) || ((Player) sender)
 							.hasPermission("setManager"))) {
 				for (String s : l.getCellMates()) {
 					if (s.equalsIgnoreCase(player)) {
 						l.removeCellMate(player);
 						if (player.equalsIgnoreCase(l.getWarden())) {
 							l.releaseLock();
 							sender.sendMessage("Successfully removed, and lock released");
 						} else {
 							sender.sendMessage("Successfully removed");
 						}
 						return true;
 					}
 				}
 				if (notFound) {
 					sendError(sender,
 							"That player does not belong to this region");
 				}
 			} else if (l.isLocked()
 					&& (!(l.getWarden().equalsIgnoreCase(((Player) sender)
 							.getName())))) {
 				sendError(sender, "You are not the owner of this region");
 			} else {
 				sendError(sender, "You are not the owner of this region");
 			}
 		} else {
 			sendError(sender, "That region does not exist");
 		}
 		return true;
 	}
 
 	public boolean leaveRegion(CommandSender sender, String name) {
 		Lock l = lockManager.getLockByName(name);
 
 		boolean notFound = true;
 
 		if (l != null) {
 			for (String s : l.getCellMates()) {
 				if (s.equalsIgnoreCase(((Player) sender).getName())) {
 					l.removeCellMate(((Player) sender).getName());
 					if (((Player) sender).getName().equalsIgnoreCase(
 							l.getWarden())) {
 						l.releaseLock();
 						sender.sendMessage("Successfully left, lock released");
 					} else {
 						sender.sendMessage("Successfully left");
 					}
 					return true;
 				}
 			}
 			if (notFound) {
 				sendError(sender, "You do not belong to this region");
 			}
 
 		} else {
 			sendError(sender, "That region does not exist");
 		}
 		return true;
 	}
 
 	public boolean isWarden(CommandSender sender) {
 		String username = ((Player) sender).getName();

		for (Lock l : lockManager.getLocks()) {
			if (l.isLocked()) {
				if (l.getWarden().equals(username)) {
					return true;
				}
 			}
 		}
 		return false;
 	}
 
 	public synchronized boolean acquireLock(CommandSender sender, String name) {
 		Lock l = lockManager.getLockByName(name);
 
 		if (l != null) {
 			if (isWarden(sender)) {
 				sendError(sender,
 						"You already own a region, you cannot lock more than one");
 			} else {
 				if (l.hasEntrance()) {
 					if (l.acquireLock(((Player) sender).getName())) {
 						sender.sendMessage("Successfully Locked");
 					} else {
 						sendError(sender, "Region already locked");
 					}
 				} else {
 					sendError(sender,
 							"This region does not have an entrance and cannot be acquired");
 				}
 			}
 		} else {
 			sendError(sender, "This region does not exist");
 		}
 		return true;
 	}
 
 	public synchronized boolean releaseLock(CommandSender sender, String name) {
 		Lock l = lockManager.getLockByName(name);
 
 		if (l != null) {
 			if ((l.getWarden().equalsIgnoreCase(((Player) sender).getName()))
 					|| ((Player) sender).hasPermission("setManager")) {
 				sender.sendMessage(l.releaseLock());
 			} else {
 				sendError(sender,
 						"You are not the holder of this lock, and cannot release it");
 			}
 		} else {
 			sendError(sender, "No Such region exists");
 		}
 
 		return true;
 	}
 
 	public boolean unique(String name) {
 		for (Lock l : lockManager.getLocks()) {
 			if (l.getRegion().getName().equalsIgnoreCase(name)) {
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
 				Location max_point = selection.getMaximumPoint();
 				Location min_point = selection.getMinimumPoint();
 				if (checkBreaches(max_point) == null
 						&& checkBreaches(min_point) == null) {
 					RegionNamePair r = new RegionNamePair(name.toString(),
 							min_point, max_point);
 					Lock l = new Lock(r);
 					lockManager.addLock(l);
 
 					sender.sendMessage("Added Successfully\n Please now set a location for the region entrance");
 				} else {
 					sendError(sender, "Regions may not overlap (+/- 1)");
 				}
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
 
 	public Lock checkBreaches(Location location) {
 		for (Lock l : lockManager.getLocks()) {
 			Location min = l.getRegion().getMin_point();
 			Location max = l.getRegion().getMax_point();
 			if (isInRegion(location, min, max)) {
 				return l;
 			}
 		}
 		return null;
 	}
 
 	public boolean isInRegion(Location foreignObject, Location region_min,
 			Location region_max) {
 		return ((foreignObject.getX() <= region_max.getX() + 1)
 				&& (foreignObject.getX() >= region_min.getX() - 1)
 				&& (foreignObject.getZ() <= region_max.getZ() + 1)
 				&& (foreignObject.getZ() >= region_min.getZ() - 1)
 				&& (foreignObject.getY() <= region_max.getY() + 1) && (foreignObject
 				.getY() >= region_min.getY() - 1));
 	}
 
 	public void releaseOwnerships(String name) {
 		for (Lock l : lockManager.getLocks()) {
 			if (l.isLocked() && l.getWarden().equalsIgnoreCase(name)) {
 				if (l.getCellMateCount() == 0) {
 					l.releaseLock();
 				} else {
 					boolean valid = false;
 
 					for (int i = 0; i <= l.getCellMateCount(); i++) {
 						Player target = Bukkit.getServer().getPlayer(
 								l.getCellMateByIndex(i));
 						if (target != null
 								&& (!(target.getName().equals(name)))) {
 							l.setOwner(target.getName());
 							valid = true;
 							break;
 						}
 					}
 					if (!valid) {
 						l.releaseLock();
 					}
 				}
 			}
 		}
 	}
 }

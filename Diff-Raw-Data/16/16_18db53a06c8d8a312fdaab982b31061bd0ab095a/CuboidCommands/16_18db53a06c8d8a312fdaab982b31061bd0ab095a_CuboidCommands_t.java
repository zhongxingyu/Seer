 package com.zand.bukkit.areaguard.command;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import com.zand.areaguard.Config;
 import com.zand.areaguard.area.Area;
 import com.zand.areaguard.area.Cuboid;
 import com.zand.areaguard.area.World;
 import com.zand.bukkit.areaguard.AreaGuard;
 import com.zand.bukkit.areaguard.Session;
 import com.zand.bukkit.util.Messager;
 
 public class CuboidCommands implements CommandExecutor {
 	private AreaGuard plugin;
 	private CommandHelp help;
 	
 	public CuboidCommands(AreaGuard plugin) {
 		this.plugin = plugin;
 		
 		help = new CommandHelp(plugin, "Cuboid");
 		help.add("create", 		0,	"", 				"Creates a new cuboid for the selected area.", "");
 		help.add("delete", 		0,	"", 				"Deletes the selected cuboid.", "");
 		help.add("activate", 	4,	"", 				"Activates the selected cuboid.", "");
 		help.add("deactivate", 	6,	"", 				"Deactivates the selected cuboid.", "");
 		help.add("list", 		0,	"", 				"Lists the cuboids in the selected area.", "");
 		help.add("made", 		0,	"[by <player>]",	"Lists the cuboids that where created.", "");
 		help.add("move", 		0,	"", 				"Moves the selected cuboid.", "");
 		help.add("extend", 		0,	"", 				"Extends the selected cuboid to cover the last selected point.", "");
 		help.add("priority", 	4,	"<number>", 		"Sets the selected cuboid's priority.", "");
 		help.add("select", 		3,	"<id>", 			"Selects a cuboid by ID.", "");
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		Session session = plugin.getSession(sender);
 		if (args != null && args.length > 0) {
 			// Create
 			if (args[0].equalsIgnoreCase("create")) {
 				World world = session.getSelectedWorld();
 				Area area = session.getSelectedArea();
 				
 				if (world == null) {
 					Messager.warn(sender, "You have no world selected");
 					return true; }
 				
 				if (area == null) {
 					Messager.warn(sender, "You have no area selected");
 					return true; }
 				if (!canModify(area, sender, true)) return true;
 				
 				Cuboid cuboid = Config.storage.newCuboid(session.getRealName(), area, world, session.getCoords());
 				if (cuboid == null || !cuboid.exsists()) Messager.error(sender, "Failed to create Cuboid!");
 				else {
 					session.select(cuboid);
 					Messager.inform(sender, "Cuboid created and selected for \"" + area.getName() + "\".");
 					if (canActivate(cuboid, sender, false)) {
 						if (!cuboid.setActive(true))
 							Messager.warn(sender, "Failed to activate cuboid.");
 					}
 				}
 				return true;
 			}
 			
 			// Select
 			else if (args[0].toLowerCase().startsWith("sel")) {
 				if (args.length > 1) {
 					try {
 						Cuboid cuboid = Config.storage.getCuboid(Integer.valueOf(args[1]));
 						if (cuboid != null && cuboid.exsists()) {
 							session.select(cuboid);
 							Messager.inform(sender, "Selected cuboid ID " + args[1]);
 						} else Messager.warn(sender, "Could not find cuboid with ID " + args[1]);
 					} catch (NumberFormatException e) {
 						Messager.warn(sender, "\"" + args[1] + "\" is not a number.");
 					}
 				}
 				return true;
 			}
 			
 			// Move
 			else if (args[0].equalsIgnoreCase("move")) {
 				World world = session.getSelectedWorld();
 				Cuboid cuboid = session.getSelectedCuboid();
 				if (cuboid == null) {
 					Messager.warn(sender, "You have no cuboid selected.");
 					return true; }
 				if (!canModify(cuboid, sender, true)) return true;
 				if (cuboid.isActive() && !canActivate(cuboid, sender, true)) {
 					Messager.warn(sender, "You can deactivate this cuboid to move it, but it will need to be activated agen.");
 					return true; }
 				if (world == null) {
 					Messager.warn(sender, "You have no world selected.");
 					return true; }
 				
 				if (cuboid.setLocation(world, session.getCoords()))
 					Messager.inform(sender, "The selected cuboid was moved.");
 				else Messager.error(sender, "Faild to move cuboid!");
 				
 				return true;
 			}
 			
 			// Extend
 			else if (args[0].equalsIgnoreCase("extend")) {
 				World world = session.getSelectedWorld();
 				Cuboid cuboid = session.getSelectedCuboid();
 				if (cuboid == null) {
 					Messager.warn(sender, "You have no cuboid selected.");
 					return true; }
 				if (!canModify(cuboid, sender, true)) return true;
 				if (cuboid.isActive() && !canActivate(cuboid, sender, true)) {
 					Messager.warn(sender, "You can deactivate this cuboid to move it, but it will need to be activated agen.");
 					return true; }
				if (world == null || world.getId() != cuboid.getWorld().getId()) {
 					Messager.warn(sender, "The point you selected is not in the world \"" + world.getName() + "\".");
 					return true; }
 				
 				int coords[] = cuboid.getCoords();
 				int point[] = session.getSelectedPoint();
 				for (int i=0; i < point.length; i++) {
 					if (coords[i] > point[i]) coords[i] = point[i];
 					else if (coords[i+3] < point[i]) coords[i+3] = point[i];
 				}
 				
 				if (cuboid.setLocation(world, coords))
 					Messager.inform(sender, "The selected cuboid was extended.");
 				else Messager.error(sender, "Faild to extend cuboid!");
 				
 				return true;
 			}
 			
 			// Activate
 			else if (args[0].toLowerCase().startsWith("acti")) {
 				Cuboid cuboid = session.getSelectedCuboid();
 				if (cuboid == null) {
 					Messager.warn(sender, "You have no cuboid selected.");
 					return true; }
 				if (!canActivate(cuboid, sender, true)) return true;
 				if (cuboid.isActive()) {
 					Messager.inform(sender, "The selected cuboid is already active."); 
 					return true; }
 				
 				if (cuboid.setActive(true))
 					Messager.inform(sender, "The selected cuboid is now active.");
 				else Messager.error(sender, "Faild to activate cuboid!");
 				
 				return true;
 			}
 			
 			// Deactivate
 			else if (args[0].toLowerCase().startsWith("deacti")) {
 				Cuboid cuboid = session.getSelectedCuboid();
 				if (cuboid == null) {
 					Messager.warn(sender, "You have no cuboid selected.");
 					return true; }
 				if (!canModify(cuboid, sender, true)) return true;
 				if (!cuboid.isActive()) {
 					Messager.inform(sender, "The selected cuboid is already inactive."); 
 					return true; }
 				
 				if (cuboid.setActive(false))
 					Messager.inform(sender, "The selected cuboid is now inactive.");
 				else Messager.error(sender, "Faild to deactivate cuboid!");
 				
 				return true;
 			}
 			
 			// List
 			else if (args[0].equalsIgnoreCase("list")) {
 				
 				Area area = session.getSelectedArea();
 				if (area == null) {
 					Messager.warn(sender, "You have no area selected");
 					return true; }
 				
 				ArrayList<Cuboid> cuboids = area.getCuboids();
 				Messager.inform(sender, "Cuboids in \"" + area.getName() + "\":" + ChatColor.WHITE + " " + cuboids.size());
 				show(sender, cuboids);
 				return true;
 			}
 			
 			// Made
 			else if (args[0].equalsIgnoreCase("made")) {
 				String player = session.getName();
 				if (args.length > 2 && args[1].equalsIgnoreCase("by")) {
 					player = args[2];
 				}
 				
 				ArrayList<Cuboid> cuboids = Config.storage.getCuboidsCreated(player);
 				World world = session.getSelectedWorld();
 				int cap = -1;
 				if (world != null) cap = plugin.Security.getPermissionInteger(world.getName(), player, "areaguard-cuboid-create");
 				
 				if (player == session.getName()) Messager.inform(sender, "Cuboids you created:" + ChatColor.WHITE + " " + cuboids.size() + "/" + cap + " Cuboid(s)");
 				else Messager.inform(sender, "Cuboids created by " + player + ":" + ChatColor.WHITE + " " + cuboids.size() + " Cuboid(s)");
 				
 				show(sender, cuboids);
 				
 				return true;
 			}
 			
 			// Info
 			else if (args[0].equalsIgnoreCase("info")) {
 				
 				Cuboid cuboid = session.getSelectedCuboid();
 				if (cuboid == null) {
 					Messager.warn(sender, "You have no cuboid selected.");
 					return true; }
 				
 				sender.sendMessage(ChatColor.DARK_PURPLE + "Selected Cuboid Info:");
 				sender.sendMessage(ChatColor.YELLOW + "ID:" + ChatColor.WHITE + " " + cuboid.getId());
 				if (cuboid.isActive()) 
 					sender.sendMessage(ChatColor.YELLOW + "Status:" + ChatColor.GREEN + " Active");
 					else sender.sendMessage(ChatColor.YELLOW + "Status:" + ChatColor.RED + " Inactive");
 				
 				sender.sendMessage(ChatColor.YELLOW + "Area:" + ChatColor.WHITE + " " + cuboid.getArea().getName());
 				sender.sendMessage(ChatColor.YELLOW + "World:" + ChatColor.WHITE + " " + cuboid.getWorld().getName());
 				int coords[] = cuboid.getCoords();
 				sender.sendMessage(ChatColor.YELLOW + "Coords:" + ChatColor.WHITE + " "
 				+ "(" + coords[0] + ", " + coords[1] + ", " + coords[2] + ")"
 				+ "-(" + coords[3] + ", " + coords[4] + ", " + coords[5] + ")");
 				
 				return true;
 			}
 			
 			// Delete
 			else if (args[0].equalsIgnoreCase("delete")) {
 				
 				Cuboid cuboid = session.getSelectedCuboid();
 				if (cuboid == null) {
 					Messager.warn(sender, "You have no cuboid selected.");
 					return true; }
 				if (!canModify(cuboid, sender, true)) return true;
 				
 				if (cuboid.delete())
 					Messager.inform(sender, "The selected cuboid was deleted.");
 				else Messager.error(sender, "Faild to delete cuboid!");
 				
 				return true;
 			}
 			
 			// Priority
 			else if (args[0].toLowerCase().startsWith("prio")) {
 				Cuboid cuboid = session.getSelectedCuboid();
 				if (cuboid == null) {if (cuboid == null) {
 					Messager.warn(sender, "You have no cuboid selected.");
 					return true; }
 				if (!canModify(cuboid, sender, true)) return true;
 					Messager.warn(sender, "You have no cuboid selected.");
 					return true; }
 				if (!canModify(cuboid, sender, true)) return true;
 				if (cuboid.isActive() && !canActivate(cuboid, sender, true)) {
 					Messager.warn(sender, "You can deactivate this cuboid to move it, but it will need to be activated agen.");
 					return true; }
 				
 				if (args.length > 1) {
 					int i;
 					
 					try {
 						i = Integer.valueOf(args[1]);
 					} catch (NumberFormatException e) {
 						Messager.warn(sender, "\"" + args[1] + "\" is not a number.");
 						return true;
 					}
 					
 					if (cuboid.setPriority(i))
 						Messager.inform(sender, "The selected cuboid's priority was set to " + i + ".");
 					else Messager.error(sender, "Faild to set the cuboid's priority!");
 				}
 				else
 					Messager.inform(sender, "The selected cuboid's priority is " + cuboid.getPriority());
 				return true;
 			}
 		}
 		help.show(sender, label);
 		return true;
 	}
 	
 	public boolean canCreate(Area area, CommandSender sender, boolean warn) {		
 		// areaguard-cuboid-create
 		
 		if (!canModify(area, sender, warn)) return false;
 		
 		// If there a Player
 		if (sender instanceof Player) {
 			Player player = (Player)sender;
 			int cap = plugin.Security.getPermissionInteger(player.getWorld().getName(), player.getName(), "areaguard-cuboid-create");
 			int created = Config.storage.getAreasCreated(player.getName()).size();
 			
 			if (cap > created) return true;
 			else if (cap >= 0) if (warn) Messager.warn(sender, "You have reached your created area cap of " + cap + ".");
 		} 
 		
 		// If there not a player but an Op
 		else if (sender.isOp()) return true;
 		
 		if (warn) Messager.warn(sender, "Your not allowed to create new areas.");
 		return false;
 	}
 	
 	public boolean canActivate(Cuboid cuboid, CommandSender sender, boolean warn) {
 		// areaguard.cuboid.activate
 		
 		if (sender instanceof Player) {
 			Player player = (Player)sender;
 			if (plugin.Security.permission(player, "areaguard.cuboid.activate")) return true;
 		} else if (sender.isOp()) return true;
 		if (warn) Messager.warn(sender, "You are not a allowed to activate this cuboid.");
 		return false;
 	}
 	
 	public boolean canModify(Cuboid cuboid, CommandSender sender, boolean warn) {
 		// areaguard.cuboid.modify.created
 		
 		if (sender instanceof Player) {
 			Player player = (Player)sender;
 			if (cuboid.getCreator().equalsIgnoreCase(player.getName()) &&
 					plugin.Security.permission(player, "areaguard.area.modify.created")) return true;
 		}
 		return canModify(cuboid.getArea(), sender, warn);
 	}
 	
 	public boolean canModify(Area area, CommandSender sender, boolean warn) {
 		// areaguard.cuboid.modify.owned
 		// areaguard.cuboid.modify.all
 		
 		if (sender instanceof Player) {
 			Player player = (Player)sender;
 			
 			if (plugin.Security.permission(player, "areaguard.cuboid.modify.all")) return true;
 			if (area.isOwner(player.getName()) &&
 					plugin.Security.permission(player, "areaguard.cuboid.modify.owned")) return true;
 		} else if (sender.isOp()) return true;
 		Messager.warn(sender, "You are not a owner of the area that this cuboid belongs to.");
 		return false;
 		
 	}
 	
 	public void show(CommandSender sender, ArrayList<Cuboid> cuboids) {
 		for (Cuboid cuboid : cuboids) {
 			String line = "";
 			line += "ID: \t" + cuboid.getId();
 			line += " Priority: " + cuboid.getPriority();
 			int coords[] = cuboid.getCoords();
 			line += " \t@(" + coords[0] + ", " + coords[1] + ", " + coords[2] + ")";
 			line += "-(" + coords[3] + ", " + coords[4] + ", " + coords[5] + ")";
 			
 			// Active:   
 			// Inactive: 
 			if (cuboid.isActive()) 
 				sender.sendMessage(ChatColor.GREEN + line);
 			else sender.sendMessage(ChatColor.RED + line);
 		}
 	}
 }

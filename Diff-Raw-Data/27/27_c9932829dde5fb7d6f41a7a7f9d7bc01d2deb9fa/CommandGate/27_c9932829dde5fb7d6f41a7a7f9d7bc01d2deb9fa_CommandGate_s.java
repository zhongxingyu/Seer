 package de.xcraft.engelier.XcraftGate.Commands;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import de.xcraft.engelier.XcraftGate.XcraftGate;
 import de.xcraft.engelier.XcraftGate.XcraftGateCommandHandler;
 import de.xcraft.engelier.XcraftGate.XcraftGateGate;
 
 public class CommandGate extends XcraftGateCommandHandler {
 
 	public CommandGate(XcraftGate instance) {
 		super(instance);
 	}
 
 	public void printUsage() {
 		player.sendMessage(ChatColor.LIGHT_PURPLE + plugin.getNameBrackets()
 				+ "by Engelier");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN
 				+ "/gate create <name>");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN
 				+ "/gate link <name1> <name2>");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN
 				+ "/gate loop <name1> <name2>");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN
 				+ "/gate unlink <name>");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN
 				+ "/gate unloop <name1> <name2>");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN
 				+ "/gate delete <name>");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN
 				+ "/gate list");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN
 				+ "/gate listnear [radius]");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN
 				+ "/gate listsolo");
 		player.sendMessage(ChatColor.LIGHT_PURPLE + "-> " + ChatColor.GREEN
 				+ "/gate warp <name>");
 	}
 
 	public boolean gateExists(String name) {
 		return plugin.gates.containsKey(name);
 	}
 
 	public boolean gateExists(Location location) {
 		return plugin.gateLocations.get(plugin.getLocationString(location)) != null;
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd,
 			String commandLabel, String[] args) {
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		} else {
 			plugin.log.warning(plugin.getNameBrackets()
 					+ " this command cannot be used from the console");
 			return true;
 		}
 
 		if (!isPermitted("gate", null)) {
 			error("You don't have permission to use this command");
 			return true;
 		}
 
 		if (args.length == 0) {
 			printUsage();
 		} else if (args[0].equals("create")) {
 			if (!isPermitted("gate", "create")) {
 				error("You don't have permission to use this command.");
 			} else if (!checkArgs(args, 2)) {
 				printUsage();
 			} else {
 				if (gateExists(args[1])) {
 					reply("Gate " + args[1] + " already exists.");
 				} else if (gateExists(player.getLocation())) {
 					reply("There is already a gate at this location!");
 				} else {
 					plugin.createGate(player.getLocation(), args[1]);
 					reply("Gate " + args[1] + " created: "
 							+ plugin.getLocationString(player.getLocation()));
 				}
 			}
 		} else if (args[0].equals("link")) {
 			if (!isPermitted("gate", "link")) {
 				error("You don't have permission to use this command.");
 			} else if (!checkArgs(args, 3)) {
 				printUsage();
 			} else {
 				if (!gateExists(args[1])) {
 					reply("Gate " + args[1] + " not found.");
 				} else if (!gateExists(args[2])) {
 					reply("Gate " + args[2] + " not found.");
 				} else {
 					plugin.createGateLink(args[1], args[2]);
 					reply("Linked Gate " + args[1] + " to " + args[2]);
 				}
 			}
 		} else if (args[0].equals("loop")) {
 			if (!isPermitted("gate", "link")) {
 				error("You don't have permission to use this command.");
 			} else if (!checkArgs(args, 3)) {
 				printUsage();
 			} else {
 				if (!gateExists(args[1])) {
 					reply("Gate " + args[1] + " not found.");
 				} else if (!gateExists(args[2])) {
 					reply("Gate " + args[2] + " not found.");
 				} else {
 					plugin.createGateLoop(args[1], args[2]);
 					reply("Looped Gates " + args[1] + " <=> " + args[2]);
 				}
 			}
 		} else if (args[0].equals("unlink")) {
 			if (!isPermitted("gate", "unlink")) {
 				error("You don't have permission to use this command.");
 			} else if (!checkArgs(args, 2)) {
 				printUsage();
 			} else {
 				if (!gateExists(args[1])) {
 					reply("Gate " + args[1] + " not found.");
 				} else {
 					plugin.removeGateLink(args[1]);
 					reply("removed link from gate " + args[1]);
 				}
 			}
 		} else if (args[0].equals("unloop")) {
 			if (!isPermitted("gate", "unlink")) {
 				error("You don't have permission to use this command.");
 			} else if (!checkArgs(args, 3)) {
 				printUsage();
 			} else {
 				if (!gateExists(args[1])) {
 					reply("Gate " + args[1] + " not found.");
 				} else if (!gateExists(args[2])) {
 					reply("Gate " + args[2] + " not found.");
 				} else if (!plugin.gates.get(args[1]).gateTarget
 						.equals(args[2])
 						|| !plugin.gates.get(args[2]).gateTarget
 								.equals(args[1])) {
 					reply("Gates " + args[1] + " and " + args[2]
 							+ " aren't linked together");
 				} else {
 					plugin.removeGateLoop(args[1], args[2]);
 					reply("removed gate loop " + args[1] + " <=> " + args[2]);
 				}
 			}
 		} else if (args[0].equals("delete")) {
 			if (!isPermitted("gate", "delete")) {
 				error("You don't have permission to use this command.");
 			} else if (!checkArgs(args, 2)) {
 				printUsage();
 			} else {
 
 				if (!gateExists(args[1])) {
 					reply("Gate not found: " + args[1]);
 				} else {
 					plugin.gateLocations
 							.remove(plugin.getLocationString(plugin.gates
 									.get(args[1]).gateLocation));
 					plugin.gates.remove(args[1]);
 					for (Map.Entry<String, XcraftGateGate> sourceGate : plugin.gates
 							.entrySet()) {
 						if (sourceGate.getValue().gateTarget != null
 								&& sourceGate.getValue().gateTarget
 										.equals(args[1])) {
 							sourceGate.getValue().gateTarget = null;
 						}
 					}
 					reply("Gate " + args[1] + " removed.");
 					plugin.saveGates();
 				}
 			}
 		} else if (args[0].equals("listsolo")) {
 			if (!isPermitted("gate", "info")) {
 				error("You don't have permission to use this command.");
 			} else {
 				for (Map.Entry<String, XcraftGateGate> thisGate : plugin.gates
 						.entrySet()) {
 					if (thisGate.getValue().gateTarget == null) {
 						boolean hasSource = false;
 						for (Map.Entry<String, XcraftGateGate> sourceGate : plugin.gates
 								.entrySet()) {
 							if (sourceGate.getValue().gateTarget != null
 									&& sourceGate.getValue().gateTarget
 											.equals(thisGate.getValue().gateName)) {
 								hasSource = true;
 							}
 						}
 						if (!hasSource)
 							reply("Found orphan: " + thisGate.getKey());
 					}
 				}
 			}
 		} else if (args[0].equals("warp")) {
 			if (!isPermitted("gate", "warp")) {
 				error("You don't have permission to use this command.");
 			} else if (!checkArgs(args, 2)) {
 				printUsage();
 			} else {
 				if (!gateExists(args[1])) {
 					reply("Gate not found: " + args[1]);
 				} else {
 					plugin.gates.get(args[1]).portHere(player);
 				}
 			}
 		} else if (args[0].equals("reload")) {
 			if (!isPermitted("gate", "reload")) {
 				error("You don't have permission to use this command.");
 			} else {
 				plugin.gates.clear();
 				plugin.gateLocations.clear();
 				plugin.loadGates();
 				reply("Loaded " + plugin.gates.size() + " gates.");
 			}
 		} else if (args[0].equals("list")) {
 			if (!isPermitted("gate", "info")) {
 				error("You don't have permission to use this command.");
 			} else {
 				Object[] gatesArray = plugin.gates.keySet().toArray();
 				java.util.Arrays.sort(gatesArray);
 				
 				String gateList = "";
 				for (Object gateObj : gatesArray) {
 					String gateName = (String) gateObj;
 					if (gateList.length() + gateName.length() + 2 > 255) {
 						reply(gateList);
 						gateList = "";
 					}
 					
 					if (gateList.length() == 0) {
 						gateList = gateName;
 					} else {
 						gateList += ", " + gateName;
 					}
 				}
 				reply(gateList);
 			}
 		} else if (args[0].equals("listnear")) {
 			if (!isPermitted("gate", "info")) {
 				error("You don't have permission to use this command.");
 			} else {
 				Integer radius = 10;
 				
 				if (args.length > 1) {
 					try {
 						radius = Integer.parseInt(args[1]);
 					} catch (Exception ex) {
 						error("Invalid radius number.");
 						return true;
 					}
 				}
 				
 				Location now = player.getLocation();
 				double xx = now.getX();
 				double yy = now.getY();
 				double zz = now.getZ();
 				List<String> gatesFound = new ArrayList<String>();
 				
 				for (int x = -radius; x <= radius; x++) {
 					for (int y = (radius > 127 ? -127 : -radius); y <= (radius > 127 ? 127 : radius); y++) {
 						for (int z = -radius; z <= radius; z++) {
 							String gateName = plugin.gateLocations.get(plugin.getLocationString(new Location(now.getWorld(), x + xx, y + yy, z + zz)));
 							if (gateName != null) {
 								gatesFound.add(gateName);
 							}
 						}
 					}
 				}
 				
 				if (gatesFound.size() == 0) {
 					reply("No gates found.");
 				} else {
 					Object[] found = gatesFound.toArray();
 					java.util.Arrays.sort(found);
 					for (Object foundO : found) {
 						XcraftGateGate gate = plugin.gates.get((String) foundO);
 						reply("Found " + gate.gateName + " at " + plugin.getLocationString(gate.gateLocation));
 					}
 				}
 			}
 		} else {
 			printUsage();
 		}
 
 		return true;
 	}
 }

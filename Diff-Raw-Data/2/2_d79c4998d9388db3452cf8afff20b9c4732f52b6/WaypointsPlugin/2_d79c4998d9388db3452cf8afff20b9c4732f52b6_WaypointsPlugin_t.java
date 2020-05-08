 package com.schneenet.minecraft.waypoints;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 import com.schneenet.minecraft.waypoints.storage.Waypoint;
 import com.schneenet.minecraft.waypoints.storage.WaypointNotFoundException;
 import com.schneenet.minecraft.waypoints.storage.WaypointStorage;
 import com.schneenet.minecraft.waypoints.storage.WaypointStorageSQL;
 import com.schneenet.minecraft.waypoints.storage.WaypointStorageSQL.Dbms;
 
 public class WaypointsPlugin extends JavaPlugin {
 
 	public static final int DEFAULT_PAGE_SIZE = 7;
 	private static final String COMMAND_WAYPOINT = "waypoint";
 	private static final String COMMAND_WAYPOINT_CREATE = "create";
 	private static final String COMMAND_WAYPOINT_SELECT = "select";
 	private static final String COMMAND_WAYPOINT_DESELECT = "deselect";
 	private static final String COMMAND_WAYPOINT_DESCRIBE = "describe";
 	private static final String COMMAND_WAYPOINT_DELETE = "delete";
 	private static final String COMMAND_WAYPOINT_MOVE = "move";
 	private static final String COMMAND_WAYPOINT_LIST = "list";
 	private static final String COMMAND_WAYPOINT_TP = "tp";
 
 	// private static final String STORAGE_ENGINE_TYPE_SQL = "sql";
 	// private static final String STORAGE_ENGINE_TYPE_FILE = "file";
 
 	private static Logger logger = Logger.getLogger("minecraft");
 	private static PermissionHandler handler;
 	public static String name;
 	public static String version;
 
 	private WaypointStorage storage;
 	private HashMap<Player, Waypoint> selectedWaypoints = new HashMap<Player, Waypoint>();
 
 	@Override
 	public void onDisable() {
 		if (storage != null)
 			storage.save();
 		logger.info("[" + name + "] v" + version + " disabled successfully.");
 	}
 
 	@Override
 	public void onEnable() {
 
 		PluginDescriptionFile pdFile = this.getDescription();
 		name = pdFile.getName();
 		version = pdFile.getVersion();
 
 		this.setupPermissions();
 		
 		if (handler == null) {
 			logger.severe("[" + name + "] Permissions is required. Disabling " + name + ".");
 			this.getServer().getPluginManager().disablePlugin(this);
 		}
 
 		Configuration config = this.getConfiguration();
 		// String storageEngine = config.getString("storage.engine.type",
 		// STORAGE_ENGINE_TYPE_SQL);
 		String dbmsName = config.getString("storage.engine.dbms", Dbms.SQLITE.getDriver());
 		String uri = config.getString("storage.engine.uri", "jdbc:sqlite:plugins/Waypoints/waypoints.db");
 		String username = config.getString("storage.engine.username", "");
 		String password = config.getString("storage.engine.password", "");
 
 		// Prepare the storage engine
 		// TODO Check the storage.engine.type in the config and also do a flat
 		// file storage engine
 		storage = new WaypointStorageSQL();
 		storage.init(this.getServer());
 		try {
 			((WaypointStorageSQL) storage).initSql(dbmsName, uri, username, password);
 		} catch (Exception e) {
 			logger.log(Level.SEVERE, "[" + name + "] Exception while setting up SQL Storage:", e);
 		}
 		storage.load();
 
 		// WaypointsBlockListener waypointsBlockListener = new
 		// WaypointsBlockListener(this);
 		// this.getServer().getPluginManager().registerEvent(Event.Type.SIGN_CHANGE,
 		// waypointsBlockListener, Event.Priority.Normal, this);
 
 		logger.info("[" + name + "] v" + version + " is enabled.");
 	}
 
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		String commandName = command.getName();
 		if (COMMAND_WAYPOINT.compareToIgnoreCase(commandName) == 0) {
 
 			Player player = null;
 			if (sender instanceof Player) {
 				player = (Player) sender;
 			} else {
 				sender.sendMessage(ChatColor.RED + "Command can only be used by players!");
 				return true;
 			}
 
 			if (args.length > 0) {
 				// Parse command arguments
 				if (COMMAND_WAYPOINT_SELECT.compareToIgnoreCase(args[0]) == 0) {
 					// CMD: /waypoint select <name>
 					// ACTION: Select a waypoint
 					if (handler.has(player, "waypoints.create")) {
 						if (args.length > 1) {
 							try {
 								Waypoint waypoint = storage.find(Waypoint.buildString(args, 1));
 								this.selectedWaypoints.put(player, waypoint);
 								player.sendMessage(ChatColor.GREEN + "Selected waypoint: '" + ChatColor.AQUA + waypoint.getName() + ChatColor.GREEN + "'.");
 							} catch (WaypointNotFoundException e) {
 								player.sendMessage(ChatColor.RED + "There is no waypoint by that name.");
 							}
 
 						} else {
 							player.sendMessage(ChatColor.RED + "Usage: /waypoint select <name>");
 						}
 					} else {
 						player.sendMessage(ChatColor.RED + "You do not have the proper permission to do that!");
 					}
 				} else if (COMMAND_WAYPOINT_DESELECT.compareToIgnoreCase(args[0]) == 0) {
 					// CMD: /waypoint deselect
 					// ACTION: Deselect waypoint
 					if (handler.has(player, "waypoints.create")) {
 						this.selectedWaypoints.remove(player);
 					} else {
 						player.sendMessage(ChatColor.RED + "You do not have the proper permission to do that!");
 					}
 
 				} else if (COMMAND_WAYPOINT_DELETE.compareToIgnoreCase(args[0]) == 0) {
 					// CMD: /waypoint delete
 					// ACTION: Delete the selected waypoint
 					Waypoint waypoint = this.selectedWaypoints.get(player);
 					if (waypoint != null) {
 						if (player.getName().compareTo(waypoint.getOwner().getName()) == 0 || handler.has(player, "waypoints.admin.delete")) {
 							if (storage.delete(waypoint)) {
 								this.selectedWaypoints.remove(player);
 								player.sendMessage("Successfully deleted waypoint '" + ChatColor.AQUA + waypoint.getName() + ChatColor.WHITE + "'!");
 							} else {
 								player.sendMessage(ChatColor.RED + "The waypoint does not exist! Was it deleted by someone else?");
 							}
 						} else {
 							player.sendMessage(ChatColor.RED + "You do not have the proper permission to do that!");
 						}
 					} else {
 						player.sendMessage(ChatColor.RED + "You have not selected a waypoint!");
 						player.sendMessage(ChatColor.WHITE + "Select using: " + ChatColor.RED + "/waypoint select <name>");
 					}
 				} else if (COMMAND_WAYPOINT_DESCRIBE.compareToIgnoreCase(args[0]) == 0) {
 					// CMD: /waypoint describe <description>
 					// ACTION: Add a description to the selected waypoint
 					Waypoint waypoint = this.selectedWaypoints.get(player);
 					if (waypoint != null) {
 						if (player.getName().compareTo(waypoint.getOwner().getName()) == 0 || handler.has(player, "waypoints.admin.edit")) {
 							if (args.length > 1) {
 								if (storage.edit(waypoint, Waypoint.buildString(args, 1), waypoint.getLocation())) {
 									try {
 										this.selectedWaypoints.put(player, storage.find(waypoint.getName()));
 									} catch (WaypointNotFoundException e) {
 										this.selectedWaypoints.remove(player);
 									}
 									player.sendMessage(ChatColor.GREEN + "Successfully changed the description of '" + ChatColor.AQUA + waypoint.getName() + ChatColor.GREEN + "'");
 								} else {
 									player.sendMessage(ChatColor.RED + "The waypoint does not exist! Was it deleted by someone else?");
 								}
 							} else {
 								player.sendMessage(ChatColor.RED + "Usage: /waypoint describe <description>");
 							}
 						} else {
 							player.sendMessage(ChatColor.RED + "You do not have the proper permission to do that!");
 						}
 					} else {
 						player.sendMessage(ChatColor.RED + "You have not selected a waypoint!");
 						player.sendMessage(ChatColor.WHITE + "Select using: " + ChatColor.RED + "/waypoint select <name>");
 					}
 				} else if (COMMAND_WAYPOINT_MOVE.compareToIgnoreCase(args[0]) == 0) {
 					// CMD: /waypoint move
 					// ACTION: Move the waypoint to here
 					Waypoint waypoint = this.selectedWaypoints.get(player);
 					if (waypoint != null) {
 						if (player.getName().compareTo(waypoint.getOwner().getName()) == 0 || handler.has(player, "waypoints.admin.edit")) {
 							if (storage.edit(waypoint, waypoint.getDescription(), player.getLocation())) {
 								try {
 									this.selectedWaypoints.put(player, storage.find(waypoint.getName()));
 								} catch (WaypointNotFoundException e) {
 									this.selectedWaypoints.remove(player);
 								}
 								player.sendMessage(ChatColor.GREEN + "Successfully moved '" + ChatColor.AQUA + waypoint.getName() + ChatColor.GREEN + "'");
 							} else {
 								player.sendMessage(ChatColor.RED + "The waypoint does not exist! Was it deleted by someone else?");
 							}
 						} else {
 							player.sendMessage(ChatColor.RED + "You do not have the proper permission to do that!");
 						}
 					} else {
 						player.sendMessage(ChatColor.RED + "You have not selected a waypoint!");
 						player.sendMessage(ChatColor.WHITE + "Select using: " + ChatColor.RED + "/waypoint select <name>");
 					}
 				} else if (COMMAND_WAYPOINT_TP.compareToIgnoreCase(args[0]) == 0) {
 					// CMD: /waypoint goto
 					// ACTION: Teleport player to the selected waypoint
 					if (handler.has(player, "waypoints.teleport")) {
 						Waypoint waypoint = this.selectedWaypoints.get(player);
 						if (waypoint != null) {
 							waypoint.warp(player);
 						} else {
 							player.sendMessage(ChatColor.RED + "You have not selected a waypoint!");
 							player.sendMessage(ChatColor.WHITE + "Select using: " + ChatColor.RED + "/waypoint select <name>");
 						}
 					} else {
 						player.sendMessage(ChatColor.RED + "You do not have the proper permission to do that!");
 					}
 				} else if (COMMAND_WAYPOINT_CREATE.compareToIgnoreCase(args[0]) == 0) {
 					// Check permission for basic creation
 					if (handler.has(player, "waypoints.create")) {
 						if (args.length > 1) {
 							Waypoint waypoint = new Waypoint(Waypoint.buildString(args, 1), "", player, player.getWorld(), player.getLocation());
 							if (storage.add(waypoint)) {
 								this.selectedWaypoints.put(player, waypoint);
 								player.sendMessage(ChatColor.GREEN + "Successfully created waypoint '" + ChatColor.AQUA + waypoint.getName() + ChatColor.GREEN + "'!");
 								player.sendMessage(ChatColor.WHITE + "Add a description to it using: " + ChatColor.RED + "/waypoint describe <description>");
 							} else {
 								player.sendMessage(ChatColor.RED + "There is already a waypoint with that name.");
 							}
 						} else {
 							player.sendMessage(ChatColor.RED + "Usage: /waypoint create <name>");
 						}
 					} else {
 						player.sendMessage(ChatColor.RED + "You do not have the proper permission to do that!");
 					}
 				} else if (COMMAND_WAYPOINT_LIST.compareToIgnoreCase(args[0]) == 0) {
 					// CMD: /waypoint list [<page> [<count>]]
 					// ACTION: List waypoints
 					if (handler.has(player, "waypoints.list")) {
 						int page = 1;
 						int size = DEFAULT_PAGE_SIZE;
 						if (args.length > 1) {
 							page = Integer.parseInt(args[1]);
 							if (args.length > 2) {
 								size = Integer.parseInt(args[2]);
 							}
 						}
 						List<Waypoint> list = storage.findAllPage(page, size);
 						sender.sendMessage(ChatColor.DARK_GREEN + "Waypoints" + ChatColor.WHITE + " -----------------------------");
 						if (!list.isEmpty()) {
 							Iterator<Waypoint> iter = list.iterator();
 							while (iter.hasNext()) {
 								Waypoint waypoint = iter.next();
 								String formatStr = ChatColor.GREEN + "%-16s" + ChatColor.WHITE + " (" + ChatColor.AQUA + "%6.1f" + ChatColor.WHITE + " ," + ChatColor.AQUA + "%6.1f" + ChatColor.WHITE + " ," + ChatColor.AQUA + "%6.1f" + ChatColor.WHITE + ")";
 								sender.sendMessage(String.format(formatStr, waypoint.getName(), waypoint.getLocation().getX(), waypoint.getLocation().getY(), waypoint.getLocation().getZ()));
 							}
 						} else {
 							sender.sendMessage(ChatColor.RED + "There are no waypoints to display.");
 						}
 					} else {
 						player.sendMessage(ChatColor.RED + "You do not have the proper permission to do that!");
 					}
 				} else {
 					// CMD: /waypoint <name>
 					// ACTION: Teleport to the specified waypoint
 					if (handler.has(player, "waypoints.teleport")) {
 						try {
 							Waypoint waypoint = storage.find(Waypoint.buildString(args, 0));
 							waypoint.warp(player);
 						} catch (WaypointNotFoundException e) {
 							player.sendMessage(ChatColor.RED + "There is no waypoint by that name.");
 						}
 					} else {
 						player.sendMessage(ChatColor.RED + "You do not have the proper permission to do that!");
 					}
 				}
 			} else {
 				// CMD: /waypoint
 				// ACTION: Show what waypoint is currently selected (if any) or show helpful usage messages
 				boolean canList = handler.has(player, "waypoints.list");
 				boolean canTp = handler.has(player, "waypoints.teleport");
 				if (handler.has(player, "waypoints.create")) {
 					Waypoint waypoint = this.selectedWaypoints.get(player);
 					if (waypoint != null) {
 						player.sendMessage(ChatColor.GREEN + "Selected waypoint: '" + ChatColor.AQUA + waypoint.getName() + ChatColor.GREEN + "'.");
 					} else {
 						player.sendMessage(ChatColor.RED + "You have not selected a waypoint!");
 						player.sendMessage(ChatColor.WHITE + "Select using: " + ChatColor.RED + "/waypoint select <name>");
 					}
 				} else if (canList || canTp) {
 					if (canList) {
 						player.sendMessage(ChatColor.RED + "List waypoints: /waypoint list");
 					}
 					if (canTp) {
 						player.sendMessage(ChatColor.RED + "Teleport to waypoint: /waypoint <name>");
 					}
 				} else {
 					player.sendMessage(ChatColor.RED + "You do not have the proper permissions to access Waypoints.");
 				}
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public static PermissionHandler getPermissionHandler() {
 		return handler;
 	}
 
 	public WaypointStorage getWaypointStorage() {
 		return this.storage;
 	}
 
 	private void setupPermissions() {
 		Plugin permissionsPlugin = this.getServer().getPluginManager().getPlugin("Permissions");
 		if (handler == null) {
 			if (permissionsPlugin != null) {
 				handler = ((Permissions) permissionsPlugin).getHandler();
				logger.info("[" + name + "] " + permissionsPlugin.getDescription().getName() + " v" + permissionsPlugin.getDescription().getVersion() + " found. Using it for permissions.");
 			}
 
 		}
 	}
 
 }

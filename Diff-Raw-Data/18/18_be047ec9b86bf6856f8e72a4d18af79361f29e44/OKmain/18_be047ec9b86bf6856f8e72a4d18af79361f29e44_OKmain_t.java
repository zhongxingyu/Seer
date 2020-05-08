 package me.kalmanolah.okb3;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.logging.Logger;
 
 import me.kalmanolah.cubelist.classfile.cubelist;
 import me.kalmanolah.extras.OKUpdater;
 
 import org.anjocaido.groupmanager.GroupManager;
 import org.anjocaido.groupmanager.dataholder.OverloadedWorldHolder;
 import org.anjocaido.groupmanager.dataholder.worlds.WorldsHolder;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.tehkode.permissions.PermissionGroup;
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.PermissionUser;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import com.baummann.setrankpb.RankHandler;
 import com.baummann.setrankpb.SetRankPB;
 import com.nijiko.permissions.Entry;
 import com.nijiko.permissions.Group;
 import com.nijiko.permissions.PermissionHandler;
 import com.nijiko.permissions.User;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 import de.bananaco.permissions.interfaces.PermissionSet;
 import de.bananaco.permissions.worlds.WorldPermissionsManager;
 
 public class OKmain extends JavaPlugin {
 	public static String name;
 	public static String version;
 	public static ArrayList<String> authors;
 	private final OKPlayerListener playerListener = new OKPlayerListener(this);
 	private final OKServerListener serverListener = new OKServerListener(this);
 	private final OKCommandManager commandManager = new OKCommandManager(this);
 	public static PermissionHandler permissionHandler;
 	public static PermissionManager permissionManager;
 	public static WorldsHolder groupManager;
 	public static WorldPermissionsManager bpManager;
 	public static RankHandler srpbHandler;
 	public static List<Player> kicks = new ArrayList<Player>();
 	public static List<Player> portals = new ArrayList<Player>();
 	public static HashMap<Player, String> cachedjoinmsgs = new HashMap<Player, String>();
 	public static me.kalmanolah.okb3extras.com.nijikokun.register.payment.Method Method = null;
 
 	public void onEnable() {
 		name = getDescription().getName();
 		version = getDescription().getVersion();
 		authors = getDescription().getAuthors();
 		OKLogger.initialize(Logger.getLogger("Minecraft"));
 		OKLogger.info("Attempting to enable " + name + " v" + version + " by " + authors.get(0) + "...");
 		OKUpdater.update(name, version, "http://kalmanolah.net/files/check.php", "http://kalmanolah.net/files/dl.php", OKLogger.getLog(), OKLogger.getPrefix());
 		PluginManager pm = getServer().getPluginManager();
 		Plugin p = pm.getPlugin("Permissions");
 		Plugin pex = pm.getPlugin("PermissionsEx");
 		Plugin gm = pm.getPlugin("GroupManager");
 		Plugin bp = pm.getPlugin("bPermissions");
 		Plugin srpb = pm.getPlugin("SetRankPB");
 		if (p != null) {
 			permissionManager = null;
 			groupManager = null;
 			bpManager = null;
 			srpbHandler = null;
 			if (!pm.isPluginEnabled(p)) {
 				pm.enablePlugin(p);
 			}
 			permissionHandler = ((Permissions) p).getHandler();
 		}
 		if (pex != null) {
 			permissionHandler = null;
 			groupManager = null;
 			bpManager = null;
 			srpbHandler = null;
 			if (!pm.isPluginEnabled(pex)) {
 				pm.enablePlugin(pex);
 			}
 			permissionManager = PermissionsEx.getPermissionManager();
 		}
 		if (gm != null) {
 			permissionHandler = null;
 			permissionManager = null;
 			bpManager = null;
 			srpbHandler = null;
 			if (!pm.isPluginEnabled(gm)) {
 				pm.enablePlugin(gm);
 			}
 			groupManager = ((GroupManager) gm).getWorldsHolder();
 		}
 		if (bp != null) {
 			permissionHandler = null;
 			permissionManager = null;
 			groupManager = null;
 			srpbHandler = null;
 			if (!pm.isPluginEnabled(bp)) {
 				pm.enablePlugin(bp);
 			}
 			bpManager = de.bananaco.permissions.Permissions.getWorldPermissionsManager();
 		}
 		if (srpb != null) {
 			permissionHandler = null;
 			permissionManager = null;
 			groupManager = null;
 			bpManager = null;
 			srpbHandler = ((SetRankPB) srpb).getHandler();
 		}
 		if ((pex == null) && (p == null) && (gm == null) && (bp == null) && (srpb == null)) {
 			OKLogger.info("Permissions plugin not found, shutting down...");
 			pm.disablePlugin(this);
 		} else {
 			new OKConfig();
 			OKDatabase.initialize(this);
 			OKDB.initialize(this);
 			new OKFunctions(this);
 			if ((Boolean) OKFunctions.getConfig("gen.stats")) {
 				new cubelist(this);
 			}
 			pm.registerEvent(Event.Type.PLAYER_PORTAL, playerListener, Priority.Monitor, this);
 			pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Highest, this);
 			pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Low, this);
 			pm.registerEvent(Event.Type.PLAYER_TELEPORT, playerListener, Priority.Monitor, this);
 			pm.registerEvent(Event.Type.PLAYER_KICK, playerListener, Priority.Low, this);
 			pm.registerEvent(Event.Type.PLUGIN_ENABLE, serverListener, Priority.Monitor, this);
 			setupCommands();
 			OKLogger.info(name + " v" + version + " enabled successfully.");
 		}
 	}
 
 	private void setupCommands() {
 		addCommand("bbb", new OKCmd(this));
 		addCommand("sync", new OKCmd(this));
 		addCommand("resync", new OKCmd(this));
 		addCommand("fsync", new OKCmd(this));
 		addCommand("fsyncall", new OKCmd(this));
 		addCommand("fban", new OKCmd(this));
 		addCommand("funban", new OKCmd(this));
 		addCommand("fpromote", new OKCmd(this));
 		addCommand("fdemote", new OKCmd(this));
 		addCommand("frank", new OKCmd(this));
 	}
 
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		return commandManager.dispatch(sender, cmd, label, args);
 	}
 
 	private void addCommand(String command, CommandExecutor executor) {
 		getCommand(command).setExecutor(executor);
 		commandManager.addCommand(command, executor);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void changeGroup(String player, String group, String world, Boolean mode) {
 		String groupname = null;
 		if (mode) {
 			world = getServer().getPlayer(player).getWorld().getName();
 		}
 		HashMap<String, String> worldgroups = (HashMap<String, String>) OKFunctions.getConfig("groups." + world);
 		if (worldgroups == null) {
 			worldgroups = (HashMap<String, String>) OKFunctions.getConfig("groups");
 		}
 		if (worldgroups.containsKey(group)) {
 			groupname = worldgroups.get(group);
 		}
 		if (groupname == null) {
 			worldgroups = (HashMap<String, String>) OKFunctions.getConfig("groups");
 			groupname = worldgroups.get(group);
 		}
 		if (groupname != null) {
 			if (permissionHandler != null) {
 				com.nijiko.permissions.Group grp = permissionHandler.getGroupObject(world, groupname);
 				User usr = permissionHandler.getUserObject(world, player);
 				if (usr == null) {
 					permissionHandler.addUserInfo(world, player, "deleteme", null);
 					usr = permissionHandler.getUserObject(world, player);
 				}
 				if ((grp != null) && (usr != null)) {
 					LinkedHashSet<Entry> parents = usr.getParents();
 					if (parents != null) {
 						for (Entry s : parents) {
 							Group gr = permissionHandler.getGroupObject(world, s.getName());
 							if (gr != null) {
 								usr.removeParent(gr);
 							}
 						}
 					}
 					usr.addParent(grp);
 				}
 			} else if (permissionManager != null) {
 				PermissionUser usr = permissionManager.getUser(player);
 				PermissionGroup grp = permissionManager.getGroup(groupname);
 				if ((usr != null) && (grp != null)) {
 					PermissionGroup[] grps = new PermissionGroup[1];
 					grps[0] = grp;
 					usr.setGroups(grps);
 				}
 			} else if (groupManager != null) {
 				OverloadedWorldHolder owh = groupManager.getWorldData(world);
 				if (owh != null) {
 					org.anjocaido.groupmanager.data.User usr = owh.getUser(player);
 					if (usr == null) {
 						owh.createUser(player);
 					}
 					org.anjocaido.groupmanager.data.Group grp = owh.getGroup(groupname);
 					if ((usr != null) && (grp != null)) {
 						usr.setGroup(grp);
 					}
 				}
 			} else if (bpManager != null) {
 				PermissionSet worldperms = bpManager.getPermissionSet(world);
				worldperms.setGroup(player, groupname);
 			} else if (srpbHandler != null) {
 				srpbHandler.setRank(getServer().getPlayer(player), groupname);
 			}
 		}
 	}
 
 	public static boolean CheckPermission(Player player, String string) {
 		if (permissionHandler != null) {
 			return permissionHandler.has(player, string);
 		} else if (permissionManager != null) {
 			return permissionManager.has(player, string);
 		} else if (groupManager != null) {
 			return groupManager.getWorldPermissions(player).has(player, string);
 		} else {
 			return player.hasPermission(string);
 		}
 	}
 
 	public static void kickPlayer(Player plr, String string) {
 		plr.kickPlayer(string);
 	}
 
 	public void onDisable() {
 		OKLogger.info("Attempting to disable " + name + "...");
 		OKDatabase.disable();
 		OKDB.disable();
 		getServer().getScheduler().cancelTasks(this);
 		OKLogger.info(name + " disabled successfully.");
 	}
 }

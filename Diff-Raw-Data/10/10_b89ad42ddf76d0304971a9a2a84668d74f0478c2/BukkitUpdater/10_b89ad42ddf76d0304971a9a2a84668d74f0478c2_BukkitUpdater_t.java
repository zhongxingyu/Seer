 package zauberstuhl.BukkitUpdater;
 
 import java.io.IOException;
 import java.util.UUID;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.tehkode.permissions.PermissionManager;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import com.nijiko.permissions.PermissionHandler;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 import zauberstuhl.BukkitUpdater.Async.Blacklist;
 import zauberstuhl.BukkitUpdater.Async.Debugger;
 import zauberstuhl.BukkitUpdater.Async.Downloader;
 import zauberstuhl.BukkitUpdater.Async.Overview;
 import zauberstuhl.BukkitUpdater.Async.Reloader;
 
 /**
 * BukkitUpdater 0.2.x
 * Copyright (C) 2011 Lukas 'zauberstuhl y33' Matt <lukas@zauberstuhl.de>
 * and many thanks to V10lator for your support.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Permissions Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Permissions Public License for more details.
 *
 * You should have received a copy of the GNU Permissions Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 
 /**
 * This plugin was written for Craftbukkit
 * @author zauberstuhl
 */
 
 public class BukkitUpdater extends JavaPlugin {
 	private final ThreadHelper th = new ThreadHelper();
 	private final BukkitUpdaterPlayerListener playerListener = new BukkitUpdaterPlayerListener(this);
 	
 	public static PermissionHandler permissionHandler = null;
 	public static PermissionManager permissionExHandler = null;
 	
 	@Override
 	public void onDisable() {
 		th.console.sendMessage(ChatColor.RED+"[BukkitUpdater] version " + this.getDescription().getVersion() + " disabled.");
 	}
 
 	@Override
 	public void onEnable() {		
 		PluginManager pm = this.getServer().getPluginManager();	
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Normal, this);
 		setupBukkitUpdater();
 		th.console.sendMessage(ChatColor.GREEN+"[BukkitUpdater] version " + this.getDescription().getVersion() + " enabled.");
 	}
 		
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
 		Player player = null;
 		String commandName = command.getName().toLowerCase();
 		
 		if (sender instanceof Player) {
 			player = (Player)sender;
 		}
 		
 		if (commandName.equalsIgnoreCase("u2d")) {			
 			if (args.length == 0) {
 				if (!perm(player, "usage", true))
 					return false;
 				th.sendTo(player, "WHITE", "");
 				th.sendTo(player, "WHITE", "BukkitUpdater version "+this.getDescription().getVersion());
 				th.sendTo(player, "RED", "Fetching information...");
 				this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
 						new Overview(player,
 								this.getServer().getPluginManager().getPlugins(),
 								"u2d"));
 				return true;
 			} else {
 				if ((args[0].equalsIgnoreCase("update")) && args.length > 1) {
 					if (!perm(player, "update", true))
 						return false;
 					th.sendTo(player, "RED", "Updating plugin...");
 					this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
 							new Downloader(player, args[1]));
 					return true;
 				}
 				if (args[0].equalsIgnoreCase("unsupported")) {
 					if (!perm(player, "usage", true))
 						return false;
 					th.sendTo(player, "RED", "Searching unsupported plugins...");
 					this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
 							new Overview(player,
 									this.getServer().getPluginManager().getPlugins(),
 									"unsupported"));
 					return true;
 				}
 				if (args[0].equalsIgnoreCase("reload") && args.length > 1) {
 					if (!perm(player, "reload", true))
 						return false;
 					PluginManager pm = this.getServer().getPluginManager();
 					Plugin reloadPlugin = pm.getPlugin(args[1]);
 					
 					if (reloadPlugin != null) {
 						this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
 								new Reloader(player, pm, reloadPlugin));
 						return true;
 					}
 				}
 				if (args[0].equalsIgnoreCase("ignore")) {
 					if (!perm(player, "ignore", true)
 							|| args[1].equalsIgnoreCase(""))
 						return false;
 					th.sendTo(player, "RED", "Searching ignored plugins...");
 					this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
 							new Blacklist(player, args[1]));
 					return true;
 				}
 				if (args[0].equalsIgnoreCase("help")) {
 					th.helper(player);
 					return true;
 				}
 			}
 		}			
 		return false;
 	}
 	
 	public void setupBukkitUpdater(){
 		String uuid = UUID.randomUUID().toString();
 		
 		if (!th.folder.exists())
 			if (!th.folder.mkdir()) {
 				th.console.sendMessage("[BukkitUpdater][WARN] Creating main directory failed!");
 				onDisable();
 			}
 		if (!th.backupFolder.exists())
 			if (!th.backupFolder.mkdir()) {
 				th.console.sendMessage("[BukkitUpdater][WARN] Creating backup directory failed!");
 				onDisable();
 			}
 						
 		if (!th.token.exists()) {
 			try {
 				th.writeToFile(th.token, uuid);
 				th.console.sendMessage("[BukkitUpdater] Created token:");
 				th.console.sendMessage("[BukkitUpdater] "+uuid);
 				String buffer = th.sendData(uuid);
 				if (buffer.equals("success")) {
 					th.console.sendMessage("[BukkitUpdater] Send token success");
 				} else
 					th.console.sendMessage("[BukkitUpdater] Ups! Send token failed");
 			} catch (IOException e) {
 				// debugger
 				this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
 						new Debugger(null, e.getMessage(), "[BukkitUpdater][WARN] Was not able to create a new token"));
 			}
 		}
 		
 		if (!th.blacklist.exists()) {
			String comment = "# Here you can write plugin names\n" +
 					"# seperated by ',' (without the quotes)\n" +
 					"# if they are not to be tested for their topicality.\n\n" +
 					"# e.g.:\n" +
 					"TestPluginName1,\n" +
 					"TestPluginName2,\n";
 			try {
 				th.writeToFile(th.blacklist, comment);
 				th.console.sendMessage("[BukkitUpdater] Created new blacklist");
 			} catch (IOException e) {
 				// debugger
 				this.getServer().getScheduler().scheduleAsyncDelayedTask(this,
 						new Debugger(null, e.getMessage(), "[BukkitUpdater][WARN] Was not able to create a new blacklist"));
 			}
 		}
 		
 		//setting up permissions
 		Plugin permissions = this.getServer().getPluginManager().getPlugin("Permissions");
 		Plugin permissionsEx = this.getServer().getPluginManager().getPlugin("PermissionsEx");
 		Plugin permissionsBukkit = this.getServer().getPluginManager().getPlugin("PermissionsBukkit");
 		
 		// PermissionsBukkit
 		if (permissionsBukkit != null) {
 			th.console.sendMessage("[BukkitUpdater] Found and will use plugin "+permissionsBukkit.getDescription().getFullName());
 			return;
 		}
 		// Permissions (TheYeti)
 		if (permissions != null) {
 			String permissionsVersion = permissions.getDescription().getVersion().replaceAll("\\.","");
 			// version > 2.5
 			if (Integer.parseInt(permissionsVersion) >= 250) {
 				permissionHandler = ((Permissions) permissions).getHandler();
 				th.console.sendMessage("[BukkitUpdater] Found and will use plugin "+((Permissions)permissions).getDescription().getFullName());
 				return;
 			}
 		}
 		// PermissionEx
 		if (permissionsEx != null) {
 			permissionExHandler = PermissionsEx.getPermissionManager();
 			th.console.sendMessage("[BukkitUpdater] Found and will use plugin "+permissionsEx.getDescription().getFullName());
 			return;
 		}
 
 		th.console.sendMessage("[BukkitUpdater] Permission system not detected, defaulting to Op");	    
 	}
 	
 	public boolean perm(Player player, String perm, Boolean notify){
 		if (player == null)
 			return true;
 								
 	    // TheYeti permission
 	    if (permissionHandler != null) {
 	    	if (BukkitUpdater.permissionHandler.has(player, "BukkitUpdater."+perm))
 	    		return true;
 	    	else {
 	    		if (notify) th.sendTo(player, "GRAY", "(You have not enough permissions)");
 	    		return false;
 	    	}
 	    }
 	    // PermissionEx
 	    if (permissionExHandler != null) {
 	    	if (BukkitUpdater.permissionExHandler.has(player, "BukkitUpdater."+perm))
 		   		return true;
 	    	else {
 	    		if (notify) th.sendTo(player, "GRAY", "(You have not enough permissions)");
 	    		return false;
 	    	}
 	    }
 	    // SuperPermissions
 	    return player.hasPermission("BukkitUpdater."+perm);
 	}
 }

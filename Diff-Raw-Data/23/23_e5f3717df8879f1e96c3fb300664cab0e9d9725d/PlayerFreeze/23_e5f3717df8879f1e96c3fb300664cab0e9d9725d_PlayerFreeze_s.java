 package com.andersonhc.PlayerFreeze;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.andersonhc.PlayerFreeze.command.AcknowledgeCommand;
 import com.andersonhc.PlayerFreeze.command.FreezeCommand;
 import com.andersonhc.PlayerFreeze.command.TpfCommand;
 import com.andersonhc.PlayerFreeze.command.UnWarnCommand;
 import com.andersonhc.PlayerFreeze.command.WarnCommand;
 
 public final class PlayerFreeze extends JavaPlugin {
 	private Map<String, String> frozenUsers = new HashMap<String, String>();
 	private Map<String, String> warnedUsers = new HashMap<String, String>();
 	private Map<String, Location> playerLocation = new HashMap<String, Location>();
 	private final Logger log = Logger.getLogger("Minecraft");
 	public boolean logCommands = true;
 	private final Language language = new Language(this);
 		
 	public enum Type { FREEZE, WARN };
 	
 	public enum PluginPermission { 
 			FREEZE ("playerfreeze.freeze"),
 			WARN ("playerfreeze.warn"),
 			FREEZEALL ("playerfreeze.freezeall"),
 			FREEZEPROT("playerfreeze.freezeprotection"),
			WARNALL ("playerfreeze.warnall");
 		private final String permissionName;
 		PluginPermission (String permissionName) {
 			this.permissionName = permissionName;
 		}
 		public String getPermissionName() {
 			return this.permissionName;
 		}
 	}
 
 	@Override
 	public void onEnable() {
 		getServer().getPluginManager().registerEvents(new PlayerFreezeListener(this), this);;
 		
 		getCommand("warn").setExecutor(new WarnCommand(this));
 		getCommand("unwarn").setExecutor(new UnWarnCommand(this));
 		getCommand("freeze").setExecutor(new FreezeCommand(this));
 		getCommand("unfreeze").setExecutor(new UnWarnCommand(this));
 		getCommand("acknowledge").setExecutor(new AcknowledgeCommand(this));
 		getCommand("tpf").setExecutor(new TpfCommand(this));
 		
 		loadParms();
 		log.info(getDescription().getName() + " v" + getDescription().getVersion() + " enabled!");
 	}
 
 	@Override
 	public void onDisable() {
 		log.info(getDescription().getName() + " disabled");
 	}
 
 	public Player getPlayer(String name) {
 		if (name == null) {
 			return null;
 		} else {
 			return getServer().getPlayer(name);
 		}
 	}
 	
 	public Player findOnlinePlayer(String name) {
 		if (name != null) {
 			Player p = getServer().getPlayer(name);
 			if (p != null) return p;
 			//try to find by partial name
 			Boolean found = false;
 			int nameLength = name.length();
 			for (Player p1: getServer().getOnlinePlayers()) {
 				if (p1.getName().length() > nameLength) {
 					if (p1.getName().substring(0, (nameLength - 1)).equalsIgnoreCase(name)) {
 						//return null if more than 1 player matches
 						if (found) return null;
 						found = true;
 						p = p1;
 					}
 				}
 			}
 			return p;
 		}
 		return null;
 	}
 	
 	public String findWarnedPlayer(String name) {
 		if (name != null) {
 			if (warnedUsers.containsKey(name)) {
 				return name;
 			} else if (frozenUsers.containsKey(name)) {
 				return name;
 			} else {
 				String result = null;
 				Boolean found = false;
 				int nameLength = name.length();
 				for (String n: warnedUsers.keySet()) {
 					if (n.length() >= nameLength) {
 						if (n.substring(0, (nameLength)).equalsIgnoreCase(name)) {
 							//return null if more than 1 player matches
 							if (found) return null;
 							found = true;
 							result = n;
 						}
 					}
 				}
 				for (String n: frozenUsers.keySet()) {
 					if (n.length() >= nameLength) {
 						if (n.substring(0, (nameLength)).equalsIgnoreCase(name)) {
 							//return null if more than 1 player matches
 							if (found) return null;
 							found = true;
 							result = n;
 						}
 					}
 				}
 				return result;
 			}
 		} else {
 			return null;
 		}
 	}
 	
 	public boolean hasPermission(Player player, PluginPermission perm) {
 		return player.hasPermission(perm.getPermissionName());
 	}
 
 	public boolean hasPermission(CommandSender sender, PluginPermission perm) {
 		if (sender instanceof Player) {
 			return hasPermission((Player) sender, perm);
 		} else {
 			return true;
 		}
 	}
 	
 	public boolean isWarned(Player player) {
 		return isWarned(player.getName());
 	}
 	
 	public boolean isWarned(String player) {
 		return warnedUsers.containsKey(player);
 	}
 	
 	public boolean isFrozen(Player player) {
 		return isFrozen(player.getName());
 	}
 	
 	public boolean isFrozen(String player) {
 		return frozenUsers.containsKey(player);
 	}
 	
 	public boolean isListed(Player player) {
 		return isListed(player.getName());
 	}
 	
 	public boolean isListed(String player) {
 		return (isFrozen(player) || isWarned(player));
 	}
 	
 	public Location getLocation(Player p) {
 		return getLocation(p.getName());
 	}
 	
 	public Location getLocation(String player) {
 		return playerLocation.get(player);
 	}
 	
 	public void setLocation(String player, Location location) {
 		if (playerLocation.containsKey(player)) {
 			playerLocation.remove(player);
 		}
 		if (isListed(player)) {
 			playerLocation.put(player, location);
 		}
 	}
 	
 	public String getString(String key) {
 		String returnString;
 		returnString = language.getString(key);
 		if (returnString == null) {
 			return "error";
 		} else {
 			return returnString;
 		}
 	}
 	
 	public String[] listWarned () {
 		if (warnedUsers.isEmpty()) {
 			return null;
 		} else {
 			String[] warnedList = new String[warnedUsers.size()];
 			java.util.Set<String> users = warnedUsers.keySet();
 			int i = 0;
 			for (String user: users) {
 				warnedList[i++] = user;
 			}
 			return warnedList;
 		}
 	}
 
 	public String[] listFrozen () {
 		if (frozenUsers.isEmpty()) {
 			return null;
 		} else {
 			String[] frozenList = new String[frozenUsers.size()];
 			java.util.Set<String> users = frozenUsers.keySet();
 			int i = 0;
 			for (String user: users) {
 				frozenList[i++] = user;
 			}
 			return frozenList;
 		}
 	}
 	
 	public String getMessage(String warned) {
 		if (warnedUsers.containsKey(warned)) {
 			return warnedUsers.get(warned);
 		} else if (frozenUsers.containsKey(warned)) {
 			return frozenUsers.get(warned);
 		} else {
 			return null;
 		}
 	}
 	
 	public void Freeze(Player warned, CommandSender warner, String message) {
 		if (hasPermission(warner, PluginPermission.FREEZE)) {
 			/* Player to be frozen is already warned */
 			if (isWarned(warned)) {
 				notifyPlayer(warned, Type.FREEZE, message);
 				warner.sendMessage(ChatColor.AQUA + warned.getDisplayName() + ChatColor.BLUE + getString("FREEZE_WARNED_PLAYER"));
 				frozenUsers.put(warned.getName(), message);
 				playerLocation.put(warned.getName(), warned.getLocation());
 				warnedUsers.remove(warned.getName());
 			/* Player to be frozen is already frozen */
 			} else if (isFrozen(warned)) {
 				warner.sendMessage(ChatColor.AQUA + warned.getDisplayName() + ChatColor.BLUE + getString("FREEZE_ALREADY_FROZEN"));
 			} else {
 				warner.sendMessage(ChatColor.AQUA + warned.getDisplayName() + ChatColor.BLUE + getString("FREEZE_SUCCESS"));
 				notifyPlayer(warned, Type.FREEZE, message);
 				frozenUsers.put(warned.getName(), message);
 				playerLocation.put(warned.getName(), warned.getLocation());
 			}
 		}
 	}
 
 	public void Warn(Player warned, CommandSender warner, String message) {
		if (hasPermission(warner, PluginPermission.FREEZE))
 			if (isFrozen(warned)) {
 				warner.sendMessage(ChatColor.AQUA + warned.getDisplayName() + ChatColor.BLUE + getString("WARN_ALREADY_FROZEN"));
 			} else {
 				if (isWarned(warned)) {
 					warner.sendMessage(ChatColor.AQUA + warned.getDisplayName() + ChatColor.BLUE + getString("WARN_ALREADY_WARNED"));
 					// notifyPlayer the warned player again
 					notifyPlayer (warned, Type.WARN, warnedUsers.get(warned.getName()));
 				} else {
 					warner.sendMessage(ChatColor.AQUA + warned.getDisplayName() + ChatColor.BLUE + getString("WARN_SUCCESS"));
 					notifyPlayer (warned, Type.WARN, message);
 					warnedUsers.put(warned.getName(), message);
 					playerLocation.put(warned.getName(), warned.getLocation());
 				}
 			}
 	}
 	
 	public void notifyPlayer (Player warned , Type t , String message) {
 		if (t == Type.FREEZE) {
 			if (message == null) {
 				warned.sendMessage(ChatColor.BLUE + getString("FREEZE_NOTIFY_NOMESSAGE"));
 			} else {
 				warned.sendMessage(ChatColor.BLUE + getString("FREEZE_NOTIFY_MESSAGE") + message);
 			}
 		} else {
 			if (message == null) {
 				warned.sendMessage(ChatColor.BLUE + getString("WARN_NOTIFY_NOMESSAGE"));
 				warned.sendMessage(ChatColor.BLUE + "Type" + ChatColor.AQUA + " /acknowledge " + ChatColor.BLUE + "to acknowledge the warning and move again");
 			} else {
 				warned.sendMessage(ChatColor.BLUE + getString("WARN_NOTIFY_MESSAGE") + message);
 				warned.sendMessage(ChatColor.BLUE + "Type" + ChatColor.AQUA + " /acknowledge " + ChatColor.BLUE + "to acknowledge the warning and move again");
 			}
 		}
 	}
 	
 	public void ReNotifyPlayer(Player warned) {
 		if (isWarned(warned)) {
 			notifyPlayer(warned, Type.WARN, warnedUsers.get(warned.getName()));
 		} else if (isFrozen(warned)) {
 			notifyPlayer(warned, Type.FREEZE, frozenUsers.get(warned.getName()));
 		}
 	}
 
 	public void unWarn(CommandSender warner, String warnedName) {
 		Player warned = getServer().getPlayer(warnedName);
 		if (warner == null) {
 			if (warned != null && warned.isOnline()) {
 				warned.sendMessage(ChatColor.BLUE + getString("WARN_ACKNOWLEDGE_PLAYER"));
 			}
 			warnedUsers.remove(warnedName);
 			playerLocation.remove(warnedName);
 		} else if (isWarned(warnedName)) {
 			if (hasPermission(warner, PluginPermission.WARN)) {
 				if (warned != null && warned.isOnline()) {
 					warned.sendMessage(ChatColor.BLUE + getString("UNWARN_SUCCESS_PLAYER"));
 				}
 				warner.sendMessage(ChatColor.AQUA + warnedName + ChatColor.BLUE + getString("UNWARN_SUCCESS_ADMIN"));
 				warnedUsers.remove(warnedName);
 			} else {
 				warner.sendMessage(ChatColor.BLUE + getString("UNWARN_NO_PERMISSION"));
 			}
 		} else if (isFrozen(warnedName)) {
 			if (hasPermission(warner, PluginPermission.FREEZE)) {
 				if (warned != null && warned.isOnline()) {
 					warned.sendMessage(ChatColor.BLUE + getString("UNFREEZE_SUCCESS_PLAYER"));
 				}
 				warner.sendMessage(ChatColor.AQUA + warnedName + ChatColor.BLUE + getString("UNFREEZE_SUCCESS_ADMIN"));
 				frozenUsers.remove(warnedName);
 				playerLocation.remove(warnedName);
 			} else {
 				warner.sendMessage(ChatColor.BLUE + getString("UNFREEZE_NO_PERMISSION"));
 			}
 		}
 	}
 
 	public boolean isAdmin(Player player) {
 		return ( hasPermission(player, PluginPermission.WARN)    ||
 			     hasPermission(player, PluginPermission.WARNALL) ||
 			     hasPermission(player, PluginPermission.FREEZE)  ||
                  hasPermission(player, PluginPermission.FREEZEALL));  
 	}
 	
 	public boolean isFreezable(Player player) {
 		return !(hasPermission(player, PluginPermission.FREEZEPROT));
 	}
 
 	public boolean isAdmin(CommandSender sender) {
 		if (sender instanceof Player) {
 			return isAdmin((Player)sender);
 		} else {
 			return true;
 		}
 	}
 	
 	public void getStatus(CommandSender player, Player warned) {
 		if (isFrozen(warned)) {
 			String message = "";
 			if (frozenUsers.get(warned) != null) message = "(" + frozenUsers.get(warned) + ")"; 
 			player.sendMessage(ChatColor.AQUA + warned.getDisplayName() + ChatColor.BLUE + getString("FREEZE_STATUS") + message);
 		}
 		if (isWarned(warned)) {
 			String message = "";
 			if (warnedUsers.get(warned) != null) message = "(" + warnedUsers.get(warned) + ")"; 
 			player.sendMessage(ChatColor.AQUA + warned.getDisplayName() + ChatColor.BLUE + getString("WARN_STATUS") + message);
 		}
 	}
 
 	public void unWarnAll(CommandSender warner) {
 		if (!hasPermission(warner, PluginPermission.WARNALL)) {
 			warner.sendMessage(ChatColor.BLUE + getString("GENERAL_NO_PERMISSION"));
 			return;
 		}
 		
 		for (String playerName : warnedUsers.keySet()) {
 			warnedUsers.remove(playerName);
 			playerLocation.remove(playerName);
 			Player warned = getServer().getPlayer(playerName);
 			if (warned != null) {
 				if (warned.isOnline()) {
 					warned.sendMessage(ChatColor.BLUE + getString("UNWARN_SUCCESS_PLAYER"));
 				}
 			}
 		}
 	}
 	
 	public void unFreezeAll(CommandSender warner) {
 		if (!hasPermission(warner, PluginPermission.FREEZEALL)) {
 			warner.sendMessage(ChatColor.BLUE + getString("GENERAL_NO_PERMISSION"));
 			return;
 		}
 		
 		for (String playerName : frozenUsers.keySet()) {
 			frozenUsers.remove(playerName);
 			playerLocation.remove(playerName);
 			Player warned = getServer().getPlayer(playerName);
 			if (warned != null) {
 				if (warned.isOnline()) {
 					warned.sendMessage(ChatColor.BLUE + getString("UNFREEZE_SUCCESS_PLAYER"));
 				}
 			}
 		}
 	}
 	
 	private void loadParms() {
 		language.loadLanguage();
 	}
 	
 	
 
 }

 /**
  * Copyright (C) 2012 t7seven7t
  */
 package net.t7seven7t.swornguard.commands;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import net.t7seven7t.swornguard.SwornGuard;
 import net.t7seven7t.swornguard.permissions.Permission;
 import net.t7seven7t.swornguard.types.PlayerData;
 import net.t7seven7t.util.FormatUtil;
 import net.t7seven7t.util.Util;
 
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  * @author t7seven7t
  */
 public abstract class SwornGuardCommand implements CommandExecutor {
 	protected final SwornGuard plugin;
 	
 	protected CommandSender sender;
 	protected Player player;
 	protected String args[];
 	
 	protected String name;
 	protected String description;
 	protected Permission permission;
 	
 	protected boolean mustBePlayer;
 	protected List<String> requiredArgs;
 	protected List<String> optionalArgs;
 	protected List<String> aliases;
 	
 	protected boolean usesPrefix;
 	
 	public SwornGuardCommand(SwornGuard plugin) {
 		this.plugin = plugin;
 		this.requiredArgs = new ArrayList<String>(2);
 		this.optionalArgs = new ArrayList<String>(2);
 		this.aliases = new ArrayList<String>(2);
 	}
 	
 	public abstract void perform();
 	
 	@Override
 	public final boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		execute(sender, args);
 		return true;
 	}
 	
 	public final void execute(CommandSender sender, String[] args) {
 		this.sender = sender;
 		this.args = args;
 		if (sender instanceof Player)
 			player = (Player) sender;
 		
 		if (mustBePlayer && ! isPlayer()) {
 			err(plugin.getMessage("error_must_be_player"));
 			return;
 		}
 		
 		if (requiredArgs.size() > args.length) {
 			err(plugin.getMessage("error_arg_count"), getUsageTemplate(false));
 			return;
 		}
 
 		if (! hasPermission()) {
 			err(plugin.getMessage("error_insufficient_permissions"));
 			return;
 		}
 
 		try {
 			perform();
 		} catch (Throwable e) {
 			err("Error executing command: {0}", e.getMessage());
 			if (plugin.isDebug()) {
 				plugin.getLogHandler().log(Level.SEVERE, Util.getUsefulStack(e, "executing command " + name));
 			}
 		}
 	}
 	
 	protected final boolean isPlayer() {
 		return player != null;
 	}
 	
 	protected boolean hasPermission(CommandSender sender, Permission permission) {
 		return plugin.getPermissionHandler().hasPermission(sender, permission);
 	}
 	
 	private final boolean hasPermission() {
 		return hasPermission(sender, permission);
 	}
 	
 	protected final boolean argMatchesAlias(String arg, String... aliases) {
 		for (String s : aliases)
 			if (arg.equalsIgnoreCase(s))
 				return true;
 		return false;
 	}
 	
 	protected final void err(String msg, Object... args) {
 		sendMessage(plugin.getMessage("error"), FormatUtil.format(msg, args));
 	}
 	
 	protected final void sendMessage(String msg, Object... args) {
 		sender.sendMessage(ChatColor.YELLOW + FormatUtil.format(msg, args));
 	}
 
 	public final String getName() {
 		return name;
 	}
 
 	public final List<String> getAliases() {
 		return aliases;
 	}
 
 	public final String getUsageTemplate(final boolean displayHelp) {
 		StringBuilder ret = new StringBuilder();
 		ret.append("&b/");
 		
 		if (plugin.getCommandHandler().usesCommandPrefix() && usesPrefix)
 			ret.append(plugin.getCommandHandler().getCommandPrefix() + " ");
 		
 		ret.append(name);
 		
 		for (String s : aliases)
 			ret.append("," + s);
 		
 		ret.append("&3 ");
 		for (String s : requiredArgs)
 			ret.append(String.format("<%s> ", s));
 		
 		for (String s : optionalArgs)
 			ret.append(String.format("[%s] ", s));
 		
 		if (displayHelp)
 			ret.append("&e" + description);
 		
 		return FormatUtil.format(ret.toString());
 	}
 
 	protected OfflinePlayer getTarget(int argIndex) {
 		return getTarget(argIndex, true);
 	}
 
 	protected OfflinePlayer getTarget(int argIndex, boolean others) {
 		OfflinePlayer target = null;
 
 		if (! isPlayer()) {
 			if (args.length == 1) {
 				target = getTarget(args[argIndex], false);
 			}
 		} else {
 			if (args.length == 0) {
 				target = player;
 			} else if (others) {
 				target = getTarget(args[argIndex], false);
 			}
 		}
 
 		if (target == null) {
 			err(plugin.getMessage("error_player_not_found"));
 		}
 
 		if (getPlayerData(target) == null) {
 			err(plugin.getMessage("error_player_not_found"));
 		}
 
 		return target;
 	}
 
 	protected OfflinePlayer getTarget(String name, boolean msg) {
 		OfflinePlayer target = Util.matchOfflinePlayer(name);
 		if (target == null && msg)
 			err(plugin.getMessage("error_player_not_found"), name);
 		return target;
 	}
 	
 	protected PlayerData getPlayerData(OfflinePlayer target) {
 		return plugin.getPlayerDataCache().getData(target.getName());
 	}
 
 }

 package uk.co.jacekk.bukkit.worldrules;
 
 import org.bukkit.permissions.PermissionDefault;
 
 import uk.co.jacekk.bukkit.baseplugin.v6.permissions.PluginPermission;
 
 public class Permission {
 	
 	public static final PluginPermission SHOW_RULES		= new PluginPermission("worldrules.rules.show",		PermissionDefault.TRUE,		"Allows the player to see the rules for the world thy are in.");
	public static final PluginPermission RELOAD_RULES		= new PluginPermission("worldrules.rules.reload",	PermissionDefault.OP,		"Allows the player to reload the config file.");
 	
 }

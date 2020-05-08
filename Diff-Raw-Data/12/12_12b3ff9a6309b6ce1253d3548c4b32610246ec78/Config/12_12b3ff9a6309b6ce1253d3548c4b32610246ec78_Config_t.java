 package uk.co.jacekk.bukkit.worldrules;
 
 import java.util.Arrays;
 
 import uk.co.jacekk.bukkit.baseplugin.v6.config.PluginConfigKey;
 
 public class Config {
 	
 	public static final PluginConfigKey SHOW_RULES_ON_ENTER	= new PluginConfigKey("settings.rules-on-enter",	true);
	public static final PluginConfigKey HEADER_COLOUR		= new PluginConfigKey("settings.header-colour",		"DARK_GREEN");
 	public static final PluginConfigKey RULE_COLOUR			= new PluginConfigKey("settings.rule-colour",		"GREEN");
 	
	public static final PluginConfigKey GLOBAL_RULES		= new PluginConfigKey("global-rules",				Arrays.asList("Don't be evil."));
 	public static final PluginConfigKey WORLD_RULES			= new PluginConfigKey("world-rules",				Arrays.asList());
 	
 }

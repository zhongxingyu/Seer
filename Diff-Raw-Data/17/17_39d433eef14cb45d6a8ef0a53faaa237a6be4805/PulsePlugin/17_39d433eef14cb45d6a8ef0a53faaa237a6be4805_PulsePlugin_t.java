 package com.bitlimit.pulse.bukkit;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.util.logging.Level;
 
 public class PulsePlugin extends JavaPlugin
 {
 	public Level broadcastLevel;
 
 	@Override
 	public void onEnable()
 	{
 		this.handleDefaults();
 
		this.broadcastLevel = Level.parse((String)this.getConfig().get("broadcastLevel"));
 
 		this.recordCondition(this, false, ChatColor.DARK_GREEN +
 				"\n " + ChatColor.DARK_RED + "Pulse 0.1" + ChatColor.DARK_GREEN + " /\\\n" +
 				"____/\\   /  \\      ______\n" +
 				"         \\/     \\   /\n" +
 				"                  \\/" + ChatColor.DARK_GRAY + "    Vital.\n", Level.FINEST);
 
 
 		this.recordCondition(this, "Loaded.", Level.FINE);
 	}
 
 	@Override
 	public void onDisable()
 	{
 		this.saveConfig();
 	}
 
 	@Override
 	public void saveConfig() {
		this.getConfig().set("broadcastLevel", this.broadcastLevel.toString());
 
 		super.saveConfig();
 	}
 
 	private void handleDefaults()
 	{
		this.getConfig().addDefault("broadcastLevel", Level.ALL.toString());
 	}
 
 	public void setBroadcastLevel(Level newLevel)
 	{
 		this.broadcastLevel = newLevel;
 
 		this.saveConfig();
 	}
 
 	public void recordCondition(Plugin plugin, String condition, Level level)
 	{
 		this.recordCondition(plugin, true, condition, level);
 	}
 
 	public void recordCondition(Plugin plugin, boolean shouldPrefix, String condition, Level level)
 	{
 		String content = ChatColor.GOLD + condition;
 		String message = content;
 		String prefix = ChatColor.GRAY + "[" + plugin.getName() + ChatColor.GRAY + "] ";
 
 		if (shouldPrefix)
 		{
 			message = prefix + content;
 		}
 
		if (level.intValue() <= this.broadcastLevel.intValue())
		{
			Bukkit.broadcast(message, "pulse.observe");
		}
 	}
 }

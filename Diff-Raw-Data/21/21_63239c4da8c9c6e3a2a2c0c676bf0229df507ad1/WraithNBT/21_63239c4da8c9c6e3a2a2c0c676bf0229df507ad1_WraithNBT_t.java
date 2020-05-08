 package net.teamwraith.wraithnbt;
 
import org.bukkit.ChatColor;
 import org.bukkit.plugin.java.JavaPlugin;
 
public class WraithNBT extends JavaPlugin
{
	public static final String PREFIX = ChatColor.BLUE + "[WraithNBT]" + ChatColor.AQUA;
	public static final String ERROR_PREFIX = PREFIX + ChatColor.RED;
	
 	public void onEnable() {
        getCommand("nbt").setExecutor(new WraithNBTCommand());
    }
 	
}

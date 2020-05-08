 package net.teamwraith.wraithnbt;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
public class WraithNBT extends JavaPlugin {

	@Override
 	public void onEnable() {
		saveDefaultConfig();
		
		getCommand("nbt").setExecutor(new CommandRoot(this));
	}
 	
}

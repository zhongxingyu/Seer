 package org.vanillaworld.CustomMusicDiscs;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Main extends JavaPlugin implements Listener {
 	
 	public static JavaPlugin plugin;
 	
 	public void onEnable()
 	{
 		plugin = this;
 		Setup.setupFolders(); // Setup the folder structure
 	}
 	
 	public void onDisable()
 	{
 		plugin = null;
 	}
 	
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
 	{
 		if(cmd.getName().equalsIgnoreCase("music"))
 		{
			
 			return true;
 		}
 		return false; 
 	}
 
 }

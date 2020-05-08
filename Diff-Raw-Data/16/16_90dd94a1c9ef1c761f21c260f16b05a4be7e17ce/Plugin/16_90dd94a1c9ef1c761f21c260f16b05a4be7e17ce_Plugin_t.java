 package org.woodruffs.CraftBukkit.ShutdownServer;
 
import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Plugin extends JavaPlugin 
 {
	static private CraftServer server = null;
 	static private boolean isEnabled = false;
 	
 	static public void shutdown()
 	{
 		if (isEnabled && (null != server))
 		{		
			System.out.println("[ShutdownServer] shutting down server.");
 		
			server.shutdown();
 			server = null;
 		}
 	}
 
 	public void onDisable() 
 	{
 		System.out.println("[ShutdownServer] plugin disabled.");
 		isEnabled = false;
 	}
 	
 	public void onEnable() 
 	{		
		server = (CraftServer) this.getServer();
 		isEnabled = true;
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println("[ShutdownServer] v" + pdfFile.getVersion() + " enabled.");		        
 	}
 }
 
 

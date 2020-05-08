 package org.woodruffs.CraftBukkit.ShutdownServer;
 
import net.minecraft.server.MinecraftServer;

import org.bukkit.Server;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class Plugin extends JavaPlugin 
 {
	static private Server server = null;
 	static private boolean isEnabled = false;
 	
 	static public void shutdown()
 	{
 		if (isEnabled && (null != server))
 		{		
			server.getLogger().info("[ShutdownServer] shutting down server.");
 		
			((MinecraftServer)server).safeShutdown();
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
		server = this.getServer();
 		isEnabled = true;
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println("[ShutdownServer] v" + pdfFile.getVersion() + " enabled.");		        
 	}
 }
 
 

 package com.titankingdoms.nodinchan.titanchat;
 
 import java.util.logging.Level;
 
 import org.bukkit.plugin.Plugin;
 
 import com.titankingdoms.nodinchan.titanchat.addon.AddonManager;
 import com.titankingdoms.nodinchan.titanchat.channel.ChannelManager;
 import com.titankingdoms.nodinchan.titanchat.command.CommandManager;
 
 public class TitanChatManager {
 	
 	private final TitanChat plugin;
 	
 	private AddonManager addonManager;
 	private ChannelManager chManager;
 	private CommandManager cmdManager;
 	
 	public TitanChatManager() {
 		this.plugin = TitanChat.getInstance();
 	}
 	
 	public AddonManager getAddonManager() {
 		return addonManager;
 	}
 	
 	public ChannelManager getChannelManager() {
 		return chManager;
 	}
 	
 	public CommandManager getCommandManager() {
 		return cmdManager;
 	}
 	
 	public void load() {
		this.addonManager = new AddonManager();
		this.chManager = new ChannelManager();
		this.cmdManager = new CommandManager();
		
 		Plugin ncbl = plugin.getServer().getPluginManager().getPlugin("NC-BukkitLib");
 		
 		if (ncbl != null) {
 			addonManager.load();
 			try { chManager.load(); } catch (Exception e) { e.printStackTrace(); plugin.log(Level.WARNING, "Channels failed to load"); }
 			cmdManager.load();
 		}
 	}
 	
 	public void reload() {
 		addonManager.preReload();
 		chManager.preReload();
 		cmdManager.preReload();
 		addonManager.postReload();
 		chManager.postReload();
 		cmdManager.postReload();
 	}
 }

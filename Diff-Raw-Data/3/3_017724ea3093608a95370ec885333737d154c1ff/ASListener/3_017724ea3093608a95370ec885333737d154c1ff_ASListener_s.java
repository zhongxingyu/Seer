 package com.turt2live.antishare.listener;
 
 import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 
 import com.turt2live.antishare.AntiShare;
 
 public class ASListener implements Listener {
 
 	private AntiShare plugin;
 
 	public ASListener(AntiShare p){
 		plugin = p;
 		p.getServer().getPluginManager().registerEvents(new PlayerListener(plugin), plugin);
 		p.getServer().getPluginManager().registerEvents(new EntityListener(plugin), plugin);
 		p.getServer().getPluginManager().registerEvents(new BlockListener(plugin), plugin);
 		p.getServer().getPluginManager().registerEvents(new HazardListener(plugin), plugin);
 		p.getServer().getPluginManager().registerEvents(new ServerListener(plugin), plugin);
 	}
 
 	@EventHandler
 	public void chat(PlayerChatEvent event){
 		event.setMessage(ChatColor.translateAlternateColorCodes('!', event.getMessage()));
 	}
 }

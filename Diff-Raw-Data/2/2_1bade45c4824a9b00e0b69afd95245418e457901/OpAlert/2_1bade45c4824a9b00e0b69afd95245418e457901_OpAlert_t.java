 package com.cyprias.opalert;
 
 import in.mDev.MiracleM4n.mChatSuite.mChatSuite;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class OpAlert extends JavaPlugin {
 	public static String chatPrefix = "f[aOAf] ";
 	public Events events;
 	public Config config;
 	mChatSuite mPlugin;
 	
 	private String stPluginEnabled = "f%s 7vf%s 7is enabled.";
 	public void onEnable() {
 		this.events = new Events(this);
 		this.config = new Config(this);
 		
 		mPlugin = (mChatSuite) getServer().getPluginManager().getPlugin("mChatSuite");
 		
 		getServer().getPluginManager().registerEvents(this.events, this);
 		info(String.format(this.stPluginEnabled, getDescription().getName(), getDescription().getVersion()));
 	}
 
 	public void info(String msg) {
 		getServer().getConsoleSender().sendMessage(chatPrefix + msg);
 	}
 	
 	public String getDisplayName(CommandSender sender) {
 		String senderName = sender.getName();
 		if (sender instanceof Player) {
 			Player player = (Player) sender;
 
 			senderName = player.getDisplayName();
 
 			if (mPlugin != null && config.boolUseMChatName == true) {
 				String world = player.getLocation().getWorld().toString();
				senderName = mPlugin.getParser().parsePlayerName(sender.getName(), world);
 			}
 
 		}
 
 		return senderName;
 	}
 }

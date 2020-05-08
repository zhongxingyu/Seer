 package com.cyprias.enchantnotifier;
 
 import in.mDev.MiracleM4n.mChatSuite.mChatSuite;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class EnchantNotifier extends JavaPlugin {
 	public static String chatPrefix = "f[aENf] ";
 	public Events events;
 	public Config config;
 	private String stPluginEnabled = "f%s 7vf%s 7is enabled.";
 	mChatSuite mPlugin;
 
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
				senderName = mPlugin.getAPI().ParsePlayerName(sender.getName(), world);
 			}
 
 		}
 
 		return senderName;
 	}
 
 	public void permMessage(String permissionNode, String message) {
 		for (Player p : this.getServer().getOnlinePlayers()) {
 			if (p.hasPermission(permissionNode))
 				p.sendMessage(chatPrefix + message);
 		}
 	}
 }

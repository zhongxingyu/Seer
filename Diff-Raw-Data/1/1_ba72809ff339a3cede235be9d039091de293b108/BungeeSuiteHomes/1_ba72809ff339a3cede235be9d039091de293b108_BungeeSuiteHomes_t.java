 package com.minecraftdimensions.bungeesuitehomes;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.minecraftdimensions.bungeesuitehomes.commands.DelHomeCommand;
 import com.minecraftdimensions.bungeesuitehomes.commands.HomeCommand;
 import com.minecraftdimensions.bungeesuitehomes.commands.HomesCommand;
 import com.minecraftdimensions.bungeesuitehomes.commands.ImportHomesCommand;
 import com.minecraftdimensions.bungeesuitehomes.commands.ReloadHomesCommand;
 import com.minecraftdimensions.bungeesuitehomes.commands.SetHomeCommand;
 import com.minecraftdimensions.bungeesuitehomes.listeners.HomesListener;
 import com.minecraftdimensions.bungeesuitehomes.listeners.HomesMessageListener;
 import com.minecraftdimensions.bungeesuiteteleports.BungeeSuiteTeleports;
 
 public class BungeeSuiteHomes extends JavaPlugin {
 
 	public static BungeeSuiteHomes instance;
 
 	public static String OUTGOING_PLUGIN_CHANNEL = "BSHomes";
 	static String INCOMING_PLUGIN_CHANNEL = "BungeeSuiteHomes";
 	public static boolean usingTeleports = false;
 
 	@Override
 	public void onEnable() {
 		instance = this;
 		registerListeners();
 		registerChannels();
 		registerCommands();
 		BungeeSuiteTeleports bt = (BungeeSuiteTeleports) Bukkit.getPluginManager().getPlugin("Teleports");
 		if(bt!=null){
 			if(bt.getDescription().getAuthors().contains("Bloodsplat")){
 				usingTeleports = true;
 			}
 		}
 	}
 	
 	private void registerCommands() {
 		getCommand("sethome").setExecutor(new SetHomeCommand());
 		getCommand("home").setExecutor(new HomeCommand());
 		getCommand("delhome").setExecutor(new DelHomeCommand());
 		getCommand("homes").setExecutor(new HomesCommand());
 		getCommand("importhomes").setExecutor(new ImportHomesCommand());
 	}
 
 	private void registerChannels() {
 		Bukkit.getMessenger().registerIncomingPluginChannel(this,
 				INCOMING_PLUGIN_CHANNEL, new HomesMessageListener());
 		Bukkit.getMessenger().registerOutgoingPluginChannel(this,
 				OUTGOING_PLUGIN_CHANNEL);
 	}
 
 	private void registerListeners() {
 		getServer().getPluginManager().registerEvents(
 				new HomesListener(), this);
 	}
 
 
 }

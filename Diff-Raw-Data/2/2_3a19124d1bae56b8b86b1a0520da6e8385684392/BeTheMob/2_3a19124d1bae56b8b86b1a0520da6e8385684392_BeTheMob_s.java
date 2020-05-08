 package com.lenis0012.bukkit.btm;
 
 import org.bukkit.command.CommandSender;
 
 import com.bergerkiller.bukkit.common.PluginBase;
 import com.lenis0012.bukkit.btm.commands.BTMCommand;
 
 public class BeTheMob extends PluginBase {
 	private static BeTheMob instance;
 	private static DisguiseManager disguiseManager;
 	
 	public static BeTheMob getInstance() {
 		return instance;
 	}
 
 	private static void setInstance(BeTheMob instance) {
 		BeTheMob.instance = instance;
 	}
 
 	public static DisguiseManager getDisguiseManager() {
 		return disguiseManager;
 	}
 
 	private static void setDisguiseManager(DisguiseManager disguiseManager) {
 		BeTheMob.disguiseManager = disguiseManager;
 	}
 	
 	private BTMCommand commandSource;
 
 	@Override
 	public void enable() {
 		setInstance(this);
 		setDisguiseManager(new DisguiseManager());
 		
 		//Register listeners and commands
		register();
 		register(this.commandSource = new BTMCommand());
 	}
 	
 	@Override
 	public void disable() {
 	}
 	
 	public BTMCommand getCommandSource() {
 		return this.commandSource;
 	}
 	
 	@Override
 	public boolean command(CommandSender sender, String label, String[] args) {
 		return false;
 	}
 	
 	@Override
 	public int getMinimumLibVersion() {
 		return 157; //1.7 update
 	}
 }

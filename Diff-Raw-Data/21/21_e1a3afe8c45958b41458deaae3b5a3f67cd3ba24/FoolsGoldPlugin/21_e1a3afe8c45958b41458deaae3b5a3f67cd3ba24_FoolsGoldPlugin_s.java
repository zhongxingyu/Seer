 package com.github.leoverto.foolsgoldplugin;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class FoolsGoldPlugin extends JavaPlugin {
 	
	public void initConfig() {
 		slowRainConfig = this.getConfig().getConfigurationSection("slowRain").getValues(true);
		hayJump = this.getConfig().getConfigurationSection("hayJump.Enable").getValues(true);
 	}
 	
 	@Override
 	public void onEnable() {
 		this.saveDefaultConfig();
 		
 		getServer().getPluginManager().registerEvents(new PlayerMoveListener(), this);
 		getServer().getPluginManager().registerEvents(new EntityDamageListener(), this);
 		getServer().getPluginManager().registerEvents(new PlayerPickupItemListener(), this);
 	}
 	
 	public void onDisable() {
 		
 	}
 	
 	
 	protected static Map<String, Object> slowRainConfig;
 	protected static Map<String, Object> hayJump;
 	protected static List<Integer> itemsToNotPickup = new ArrayList<Integer>();
 
 }

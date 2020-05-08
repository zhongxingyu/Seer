 package com.etriacraft.EtriaWhitelist;
 
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class EtriaWhitelist extends JavaPlugin implements Listener {
 
 	
 	Config config;
 	DBConnection dbc;
 	
 	public static EtriaWhitelist instance;
 	
 	@Override
 	public void onEnable() {
 		
 		instance = this;
 		config = new Config(this);
		dbc = new DBConnection();
 		
 	}
 	
 	@Override
 	public void onDisable() {
 		DBConnection.sql.close();
 	}
 	
 	public static EtriaWhitelist getInstance() {
 		return instance;
 	}
     
 }

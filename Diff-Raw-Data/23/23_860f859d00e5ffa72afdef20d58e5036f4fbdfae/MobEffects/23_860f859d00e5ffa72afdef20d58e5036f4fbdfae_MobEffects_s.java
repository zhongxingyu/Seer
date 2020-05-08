 package com.etriacraft.MobEffects;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import com.etriacraft.MobEffects.Listeners.*;
 
 public class MobEffects extends JavaPlugin {
 	
 	protected Logger log;
 	protected UpdateChecker updateChecker;
 	public static Config config = new Config();
 	
 	MobEffectsCommand mec;
 	
 	@Override
 	public void onEnable() {
 		this.log = this.getLogger();
 		config.load(new File(getDataFolder(), "config.yml"));
 		
 		this.getServer().getPluginManager().registerEvents(new MEBlazeListener(), this);
 		this.getServer().getPluginManager().registerEvents(new MECaveSpiderListener(), this);
 		this.getServer().getPluginManager().registerEvents(new MECreeperListener(), this);
 		this.getServer().getPluginManager().registerEvents(new MEEnderDragonListener(), this);
 		this.getServer().getPluginManager().registerEvents(new MEEndermanListener(), this);
 		this.getServer().getPluginManager().registerEvents(new MEIronGolemListener(), this);
 		this.getServer().getPluginManager().registerEvents(new MEPlayerListener(), this);
 		this.getServer().getPluginManager().registerEvents(new MEPigZombieListener(), this);
 		this.getServer().getPluginManager().registerEvents(new MESilverfishListener(), this);
 		this.getServer().getPluginManager().registerEvents(new MESlimeListener(), this);
 		this.getServer().getPluginManager().registerEvents(new MESnowGolemListener(), this);
 		this.getServer().getPluginManager().registerEvents(new MESpiderListener(), this);
 		this.getServer().getPluginManager().registerEvents(new MEZombieListener(), this);
 		this.getServer().getPluginManager().registerEvents(new MEGiantListener(), this);
 		this.getServer().getPluginManager().registerEvents(new MEWolfListener(), this);
 		//
 		this.getServer().getPluginManager().registerEvents(new MiscListener(), this);
 		//
 		mec = new MobEffectsCommand(this);
 		
 		// Log Update to console
 		this.updateChecker = new UpdateChecker(this, "http://dev.bukkit.org/server-mods/mobeffects/files.rss");
		if (UpdateChecker.updateNeeded()) {
 			this.log.info("A new version is available: " + this.updateChecker.getVersion());
 			this.log.info("Get it from: " + this.updateChecker.getLink());
 		}
 		
 		try {
 			MetricsLite metrics = new MetricsLite(this);
 			metrics.start();
 		} catch (IOException e) {
 			// Failed to submit stats :-(
 		}
 	}
 	
 	public void onDisable() {
 	 // Stuff will be here eventually
 	}
 
 }

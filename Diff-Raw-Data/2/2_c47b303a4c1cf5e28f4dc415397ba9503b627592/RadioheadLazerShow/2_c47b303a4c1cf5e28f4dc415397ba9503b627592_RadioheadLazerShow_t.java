 package com.chrischurchwell.radioheadlazershow;
 
 import java.io.File;
 import java.util.logging.Level;
 
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.getspout.spoutapi.SpoutManager;
 
 import com.chrischurchwell.radioheadlazershow.cloud.PointCloud;
 import com.chrischurchwell.radioheadlazershow.listener.SequenceListener;
 import com.chrischurchwell.radioheadlazershow.material.ParticleEmitter;
 import com.chrischurchwell.radioheadlazershow.texture.TextureFile;
 
 public class RadioheadLazerShow extends JavaPlugin {
 
 	public static RadioheadLazerShow instance;
 	public static ParticleEmitter blockParticleEmitter;
 	
 	public static String musicFilename = "HouseOfCards_DataSample.ogg";
 	
 	public static PointCloud pointCloud;
 	
 	public static RadioheadLazerShow getInstance() {
 		return instance;
 	}
 	
 	public static void log(String message) {
 		Bukkit.getLogger().log(Level.INFO, "["+ instance.getDescription().getName() +"] " + message);
 	}
 	
 	public void onEnable() {
 		
 		//double check for spout.
 		if (!Bukkit.getPluginManager().isPluginEnabled("Spout")) {
			Bukkit.getLogger().log(Level.WARNING, "[RadioHeadLazerShow] Could not start: SpoutPlugin not found. SpoutPlugin is required for JukeBukkit to operate.");
 			setEnabled(false);
 			return;
 		}
 		
 		instance = this;
 		
 		//make sure the data folder exists.
 		File dataFolder = new File(this.getDataFolder(), "data");
 		if (!dataFolder.exists()) {
 			dataFolder.mkdirs();
 		}
 		//extract textures to the server
 		TextureFile.extractTextures();
 		
 		//precache texture
 		TextureFile.preCacheTextures();
 		
 		//cache the textures we use to the client, doubling up like this seems to fix caching errors.
 		TextureFile.cacheTextures();
 		
 		//load the custom block
 		blockParticleEmitter = new ParticleEmitter();
 		
 		//load listener
 		this.getServer().getPluginManager().registerEvents(new SequenceListener(), this);
 		
 		//preload music.
 		SpoutManager.getFileManager().addToPreLoginCache(this, new File(this.getDataFolder(), "data/"+musicFilename));
 		
 		log("Enabled");
 	}
 	
 	public void onDisable() {
 		log("Disabled");
 	}
 	
 }

 package com.turt2live.antishare;
 
 import java.io.File;
 
 import org.bukkit.Bukkit;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.world.WorldInitEvent;
 import org.bukkit.event.world.WorldLoadEvent;
 import org.bukkit.event.world.WorldSaveEvent;
 
 import com.feildmaster.lib.configuration.EnhancedConfiguration;
 
 public class ASMultiWorld implements Listener {
 
 	public static void detectWorld(AntiShare plugin, World world){
 		File worldConfig = new File(plugin.getDataFolder(), world.getName() + "_config.yml");
 		if(!worldConfig.exists()){
 			EnhancedConfiguration worldSettings = new EnhancedConfiguration(worldConfig, plugin);
 			worldSettings.loadDefaults(plugin.getResource("resources/world.yml"));
 			worldSettings.saveDefaults();
 			worldSettings.load();
 		}
 	}
 
 	public static void detectWorlds(AntiShare plugin){
 		for(World w : Bukkit.getWorlds()){
 			String worldName = w.getName();
 			File worldConfig = new File(plugin.getDataFolder(), worldName + "_config.yml");
 			EnhancedConfiguration worldSettings = new EnhancedConfiguration(worldConfig, plugin);
 			worldSettings.loadDefaults(plugin.getResource("resources/world.yml"));
 			if(!worldSettings.fileExists() || !worldSettings.checkDefaults()){
 				worldSettings.saveDefaults();
 			}
 			worldSettings.load();
 		}
 	}
 
 	// Returns allowance of worldSwap
 	public static boolean worldSwap(AntiShare plugin, Player player, Location from, Location to){
 		if(player.hasPermission("AntiShare.worlds")){
 			return true;
 		}
 		World worldTo = to.getWorld();
 		boolean transfers = plugin.config().getBoolean("other.worldTransfer", worldTo);
		if(plugin.config().getBoolean("other.only_if_creative", worldTo)){
			if(player.getGameMode() == GameMode.CREATIVE && transfers){
				return true;
			}else{
				return false;
			}
 		}else{
			if(!transfers){
 				return false;
 			}
 		}
		return true;
 	}
 
 	private AntiShare plugin;
 
 	public ASMultiWorld(AntiShare plugin){
 		this.plugin = plugin;
 	}
 
 	@EventHandler
 	public void onWorldInit(WorldInitEvent event){
 		ASMultiWorld.detectWorld(plugin, event.getWorld());
 	}
 
 	@EventHandler
 	public void onWorldLoad(WorldLoadEvent event){
 		ASMultiWorld.detectWorld(plugin, event.getWorld());
 	}
 
 	@EventHandler
 	public void onWorldSave(WorldSaveEvent event){
 		ASMultiWorld.detectWorld(plugin, event.getWorld());
 	}
 
 	@EventHandler
 	public void onWorldUnload(WorldSaveEvent event){
 		ASMultiWorld.detectWorld(plugin, event.getWorld());
 	}
 }

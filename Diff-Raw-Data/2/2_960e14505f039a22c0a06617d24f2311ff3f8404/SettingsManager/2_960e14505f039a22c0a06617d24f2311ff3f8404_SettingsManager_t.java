 package me.arno.blocklog.managers;
 
 import java.io.File;
 
 import me.arno.blocklog.BlockLog;
 import me.arno.blocklog.Config;
 import me.arno.blocklog.logs.LogType;
 import me.arno.blocklog.util.Util;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.configuration.file.FileConfiguration;
 
 public class SettingsManager extends BlockLogManager {
 	
 	public boolean isLoggingEnabled(World world, LogType... types) {
 		for(LogType type : types) {
 			if(type == LogType.EXPLOSION_CREEPER || type == LogType.EXPLOSION_FIREBALL || type == LogType.EXPLOSION_TNT)
 				type = LogType.EXPLOSION_OTHER;
 			
 			Config config = new Config("worlds" + File.separator + world.getName() + ".yml");
 			
 			if(!config.getConfig().getBoolean(type.name()))
 				return false;
 		}
 		return true;
 	}
 	
 	public boolean isDebugEnabled() {
 		return getConfig().getBoolean("blocklog.debug");
 	}
 	
 	public boolean isPurgeEnabled(String table) {
		return Util.getTime(getConfig().getString("purge." + table)) != 0;
 	}
 	
 	public boolean isPurgeLoggingEnabled() {
 		return getConfig().getBoolean("purge.log");
 	}
 	
 	public int getWarningBlocks() {
 		return getConfig().getInt("warning.blocks");
 	}
 	
 	public int getWarningDelay() {
 		return Util.getTime(getConfig().getString("warning.delay"));
 	}
 	
 	public int getWarningRepeat() {
 		return getConfig().getInt("warning.repeat");
 	}
 	
 	public int getPurgeDate(String table) {
 		return Util.getTime(getConfig().getString("purge." + table));
 	}
 	
 	public int getBlockSaveDelay() {
 		return getConfig().getInt("blocklog.save-delay");
 	}
 	
 	public int getDatabaseAliveCheckInterval() {
 		return Util.getTime(getConfig().getString("database.alive-check"));
 	}
 	
 	public boolean isAutoSaveEnabled() {
 		return getConfig().getBoolean("auto-save.enabled");
 	}
 	
 	public int getAutoSaveBlocks() {
 		return getConfig().getInt("auto-save.blocks");
 	}
 
 	public boolean saveOnWorldSave() {
 		return (isAutoSaveEnabled() && getConfig().getBoolean("auto-save.world-save"));
 	}
 	
 	public boolean isUpdatesEnabled() {
 		return getConfig().getBoolean("blocklog.updates");
 	}
 	
 	public boolean isMetricsEnabled() {
 		return getConfig().getBoolean("blocklog.metrics");
 	}
 	
 	public String getDateFormat() {
 		return getConfig().getString("blocklog.dateformat");
 	}
 	
 	public Material getWand() {
 		return Material.getMaterial(getConfig().getInt("blocklog.wand"));
 	}
 	
 	public int getMaxResults() {
 		return getConfig().getInt("blocklog.results");
 	}
 	
 	public FileConfiguration getConfig() {
 		return BlockLog.getInstance().getConfig();
 	}
 	
 	public void saveConfig() {
 		BlockLog.getInstance().saveConfig();
 	}
 	
 	public void reloadConfig() {
 		BlockLog.getInstance().reloadConfig();
 	}
 }

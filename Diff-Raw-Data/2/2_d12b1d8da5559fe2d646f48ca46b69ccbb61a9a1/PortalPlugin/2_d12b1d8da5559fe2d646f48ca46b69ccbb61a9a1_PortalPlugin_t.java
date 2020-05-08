 package com.mccraftaholics.warpportals.bukkit;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.mcstats.MetricsLite;
 
 import com.mccraftaholics.warpportals.helpers.Utils;
 import com.mccraftaholics.warpportals.manager.PortalManager;
 
 public class PortalPlugin extends JavaPlugin {
 	CommandHandler mCommandHandler;
 	PortalManager mPortalManager;
 
 	public File mPortalDataFile;
 	File mPortalConfigFile;
 	YamlConfiguration mPortalConfig;
 
 	@Override
 	public void onEnable() {
 		mPortalConfigFile = new File(getDataFolder(), "config.yml");
 		mPortalDataFile = new File(getDataFolder(), "portals.yml");
 		mPortalConfig = new YamlConfiguration();
 		initiateConfigFiles();
 		loadConfigs();
 		mPortalManager = new PortalManager(getLogger(), mPortalConfig, mPortalDataFile);
 		mCommandHandler = new CommandHandler(this, mPortalManager, mPortalConfig);
 		getServer().getPluginManager().registerEvents(new BukkitEventListener(this, mPortalManager, mPortalConfig), this);
 		initMCStats();
 	}
 
 	private void initMCStats() {
 		try {
 			MetricsLite metrics = new MetricsLite(this);
 			metrics.start();
 		} catch (IOException e) {
 			// Failed to submit the stats :-(
 		}
 	}
 
 	private void initiateConfigFiles() {
 		// Initiate portal config file
 		if (!mPortalConfigFile.exists()) {
 			mPortalConfigFile.getParentFile().mkdirs();
 			try {
 				mPortalConfigFile.createNewFile();
 				Utils.copy(getResource("config.yml"), mPortalConfigFile);
 			} catch (IOException e) {
 				getLogger().severe("Error creating the default Portal config file!");
 				e.printStackTrace();
 			}
 		}
 		// Initiate portal data file
 		if (!mPortalDataFile.exists()) {
 			mPortalDataFile.getParentFile().mkdirs();
 			try {
 				mPortalDataFile.createNewFile();
 			} catch (IOException e) {
 				getLogger().severe("Error creating Portal's save file!");
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void loadConfigs() {
 		try {
 			mPortalConfig.load(mPortalConfigFile);
 		} catch (InvalidConfigurationException e) {
			getLogger().severe("The WarpPortal config file has invalid markup.");
 		} catch (FileNotFoundException e) {
 			getLogger().severe("No config file found for WarpPortals!");
 		} catch (IOException e) {
 			getLogger().severe("Can't load Portal's config file!");
 			e.printStackTrace();
 		}
 	}
 
 	private void saveConfigs() {
 		// Why save the config? That's for the user to edit.
 		/*
 		 * try { mPortalConfig.save(mPortalConfigFile); } catch (IOException e)
 		 * { getLogger().severe("Can't save Portal's config file!"); }
 		 */
 	}
 
 	@Override
 	public void onDisable() {
 		mPortalManager.onDisable();
 		saveConfigs();
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		return mCommandHandler.handleCommand(sender, command, label, args);
 	}
 }

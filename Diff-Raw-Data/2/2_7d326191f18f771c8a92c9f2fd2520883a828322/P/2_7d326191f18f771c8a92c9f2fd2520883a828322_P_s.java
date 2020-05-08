 /*
  * Copyright 2013 Michael McKnight. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  *
  *    1. Redistributions of source code must retain the above copyright notice, this list of
  *       conditions and the following disclaimer.
  *
  *    2. Redistributions in binary form must reproduce the above copyright notice, this list
  *       of conditions and the following disclaimer in the documentation and/or other materials
  *       provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ''AS IS'' AND ANY EXPRESS OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
  * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * The views and conclusions contained in the software and documentation are those of the
  * authors and contributors and should not be interpreted as representing official policies,
  * either expressed or implied, of anybody else.
  */
 
 package com.forgenz.mobmanager;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.forgenz.mobmanager.MMComponent.Component;
 import com.forgenz.mobmanager.commands.MMCommandListener;
 import com.forgenz.mobmanager.common.config.AbstractConfig;
 import com.forgenz.mobmanager.common.integration.PluginIntegration;
 import com.forgenz.mobmanager.common.listeners.CommonMobListener;
 import com.forgenz.mobmanager.common.util.ExtendedEntityType;
 import com.forgenz.mobmanager.common.util.FileUtil;
 import com.forgenz.mobmanager.common.util.Updater;
 import com.forgenz.mobmanager.metrics.Metrics;
 
 /**
  * <b>MobManager</b> Components:
  * <ul>
  *     <li>Limiter: Reduces the number of unnecessary mob spawns</li>
  *     <li>Abilities: Adds configurable abilities for every mob</li>
  * </ul>
  * 
  * @author Michael McKnight (ShadowDog007)
  *
  */
 public class P extends JavaPlugin
 {
 	private static P p = null;
 	public static P p()
 	{
 		return p;
 	}
 	
 	private PluginIntegration integration = new PluginIntegration();;
 	
 	public PluginIntegration getPluginIntegration()
 	{
 		return integration;
 	}
 
 	
 	/* Enabled Components */
 	private boolean biomeSpecificMobs;
 	
 	public boolean isBiomeSpeicficMobsEnabled()
 	{
 		return biomeSpecificMobs;
 	}
 	
 	private boolean versionCheckEnabled, autoUpdateEnabled;
 	private Updater updater;
 	
 	public boolean isVersionCheckEnabled()
 	{
 		return versionCheckEnabled;
 	}
 	
 	public Updater getUpdater()
 	{
 		return updater;
 	}
 	
 	public String getHeaderString()
 	{
 		String header = String.format("MobManager v%s by ", getDescription().getVersion());
 		
 		List<String> authors = getDescription().getAuthors();
 		
 		for (int i = 0; i < authors.size(); ++i)
 		{
 			if (i != 0 && i != authors.size() - 1)
 				header += ", ";
 			header += authors.get(i);
 		}
 		
 		header += "\nhttp://dev.bukkit.org/bukkit-mods/mobmanager/\n";
 		
 		return header;
 	}
 
 	@Override
 	public void onLoad()
 	{
 	}
 
 	@Override
 	public void onEnable()
 	{
 		p = this;
 		
 		// Register common listeners
 		getServer().getPluginManager().registerEvents(new CommonMobListener(), this);
 		
 		/* #### CONFIG #### */
 		getConfig();
 		
 		// Initialize configuration for components
 		boolean somethingEnabled = false;
 		for (int i = 0; i < Component.values().length; ++i)
 		{
 			if (Component.values()[i].i().initializeConfig())
 			{
 				somethingEnabled = true;
 			}
 		}
 		
 		// If no components are enabled there is nothing left to do.
 		if (!somethingEnabled)
 		{
 			getLogger().warning("No components enabled :(");
 			return;
 		}
 		
 		// Check if Biome Specific Mobs are enabled
 		biomeSpecificMobs = false;
 		biomeSpecificMobs = getConfig().getBoolean("BiomeSpecificMobs", biomeSpecificMobs);
 		//AbstractConfig.set(getConfig(), "BiomeSpecificMobs", biomeSpecificMobs);
 		
 		versionCheckEnabled = getConfig().getBoolean("EnableVersionCheck", true);
 		AbstractConfig.set(getConfig(), "EnableVersionCheck", versionCheckEnabled);
 		
 		autoUpdateEnabled = getConfig().getBoolean("EnableAutoUpdater", false);
 		AbstractConfig.set(getConfig(), "EnableAutoUpdater", autoUpdateEnabled);
 		
 		// Copy the Config header into config.yml
 		AbstractConfig.copyHeader(getConfig(), "Config_Header.txt", "Global Config\n"
 				+ "\nValid EntityTypes:\n" + ExtendedEntityType.getExtendedEntityList());
 		
 		integration.integrate();
 		
 		// Enable each component
 		Component.enableComponents();
 		
 		getCommand("mm").setExecutor(new MMCommandListener());
 		
 		AbstractConfig.set(getConfig(), "Integration", getConfig().getConfigurationSection("Integration"));
 		
 		// Save the config with the current version
 		AbstractConfig.set(getConfig(), "Version", getDescription().getVersion());
 		saveConfig();
 		
 		// Check for updates
 		if (versionCheckEnabled || autoUpdateEnabled)
 		{
 			Updater.UpdateType type;
 			
 			if (!autoUpdateEnabled)
 			{
 				type = Updater.UpdateType.NO_DOWNLOAD;
 			}
 			else
 			{
 				type = Updater.UpdateType.DEFAULT;
 			}
 			
			updater = new Updater(this, "mobmanager", this.getFile(), type, true);
 		}
 		else
 		{
 			updater = null;
 		}
 		
 		// Start Metrics gathering
 		startMetrics();
 	}
 
 	@Override
 	public void onDisable()
 	{		
 		// 'Attempt to' Cancel all tasks
 		getServer().getScheduler().cancelTasks(this);
 		
 		Component.disableComponents();
 		
 		p = null;
 		
 		// Backup the current files
 		if (updater != null && updater.getResult() == Updater.UpdateResult.SUCCESS)
 		{
 			File destination = new File(getDataFolder(), String.format("backups%sv%s", File.separator, getDescription().getVersion()));
 			
 			if (destination.exists())
 			{
 				FileUtil.deleteFile(destination);
 			}
 			
 			for (File file : getDataFolder().listFiles())
 			{
 				if ("backups".equalsIgnoreCase(file.getName()))
 					continue;
 				
 				try
 				{
 					FileUtil.copy(file, new File(destination, file.getName()));	
 				}
 				catch (IOException e)
 				{
 					getLogger().severe("Error backing up old config files");
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	private void startMetrics()
 	{
 		try
 		{
 			Metrics metrics = new Metrics(this);
 			
 			Metrics.Graph componentLineGraph = metrics.createGraph("Components Used");
 			for (final Component c : Component.values())
 			{
 				String name = c.getFancyName();
 				
 				// Line graph comparing component usage together
 				componentLineGraph.addPlotter(new Metrics.Plotter(name)
 				{
 					@Override
 					public int getValue()
 					{
 						return c.i().isEnabled() ? 1 : 0;
 					}
 				});
 				
 				
 				// Pie graph of the component Enabled/Disabled
 				Metrics.Graph componentStatsGraph = metrics.createGraph(name + " Stats");
 				componentStatsGraph.addPlotter(new Metrics.Plotter("Enabled")
 				{
 					@Override
 					public int getValue()
 					{
 						return c.i().isEnabled() ? 1 : 0;
 					}
 				});
 				componentStatsGraph.addPlotter(new Metrics.Plotter("Disabled")
 				{
 					@Override
 					public int getValue()
 					{
 						return c.i().isEnabled() ? 0 : 1;
 					}
 				});
 				
 			}
 			
 			// Done :)
 			metrics.start();
 		}
 		catch (IOException e)
 		{
 			getLogger().info("Failed to start metrics gathering..  :(");
 		}
 	}
 	
 	/* #### IgnoreSpawn Flags #### */
 	private boolean ignoreNextSpawn = false;
 	public void ignoreNextSpawn(boolean value)
 	{
 		ignoreNextSpawn = value;
 	}
 	public boolean shouldIgnoreNextSpawn()
 	{
 		return ignoreNextSpawn;
 	}
 	private boolean limiterIgnoreNextSpawn = false;
 	public void limiterIgnoreNextSpawn(boolean value)
 	{
 		limiterIgnoreNextSpawn = value;
 	}
 	public boolean shouldLimiterIgnoreNextSpawn()
 	{
 		return limiterIgnoreNextSpawn;
 	}
 	private boolean abilitiesIgnoreNextSpawn = false;
 	public void abilitiesIgnoreNextSpawn(boolean value)
 	{
 		abilitiesIgnoreNextSpawn = value;
 	}
 	public boolean shouldAbilitiesIgnoreNextSpawn()
 	{
 		return abilitiesIgnoreNextSpawn;
 	}
 }

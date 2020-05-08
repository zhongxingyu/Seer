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
 
 import java.io.IOException;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.forgenz.mobmanager.abilities.config.AbilityConfig;
 import com.forgenz.mobmanager.abilities.listeners.AbilitiesMobListener;
 import com.forgenz.mobmanager.commands.MMCommandListener;
 import com.forgenz.mobmanager.common.config.AbstractConfig;
 import com.forgenz.mobmanager.common.integration.PluginIntegration;
 import com.forgenz.mobmanager.common.listeners.CommonMobListener;
 import com.forgenz.mobmanager.common.util.ExtendedEntityType;
 import com.forgenz.mobmanager.limiter.config.Config;
 import com.forgenz.mobmanager.limiter.listeners.ChunkListener;
 import com.forgenz.mobmanager.limiter.listeners.MobListener;
 import com.forgenz.mobmanager.limiter.tasks.MobDespawnTask;
 import com.forgenz.mobmanager.limiter.util.AnimalProtection;
 import com.forgenz.mobmanager.limiter.world.MMWorld;
 import com.forgenz.mobmanager.metrics.Metrics;
 import com.forgenz.mobmanager.metrics.Plotters;
 
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
 	
 	public static ConcurrentHashMap<String, MMWorld> worlds = null;
 	
 	private PluginIntegration integration = new PluginIntegration();;
 	
 	public PluginIntegration getPluginIntegration()
 	{
 		return integration;
 	}
 	
 	private MobDespawnTask despawner = null;
 	
 	public AnimalProtection animalProtection = null;
 
 	
 	/* Enabled Components */
 	private boolean limiterEnabled;
 	private boolean abilitiesEnabled;
 	
 	public boolean isLimiterEnabled()
 	{
 		return limiterEnabled;
 	}
 	
 	public boolean isAbilitiesEnabled()
 	{
 		return abilitiesEnabled;
 	}
 	
 	private boolean biomeSpecificMobs;
 	
 	public boolean isBioemSpeicficMobsEnabled()
 	{
 		return biomeSpecificMobs;
 	}
 	
 	private boolean versionCheckEnabled;
 	
 	public boolean isVersionCheckEnabled()
 	{
 		return versionCheckEnabled;
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
 		
 		// Check which components should be enabled
 		limiterEnabled = true;
 		abilitiesEnabled = false;
 		
 		limiterEnabled = getConfig().getBoolean("EnableLimiter", limiterEnabled);
 		abilitiesEnabled = getConfig().getBoolean("EnableAbilities", abilitiesEnabled);
 		
 		AbstractConfig.set(getConfig(), "EnableLimiter", limiterEnabled);
 		AbstractConfig.set(getConfig(), "EnableAbilities", abilitiesEnabled);
 		
 		// Check if Biome Specific Mobs are enabled
 		biomeSpecificMobs = false;
 		biomeSpecificMobs = getConfig().getBoolean("BiomeSpecificMobs", biomeSpecificMobs);
 		//AbstractConfig.set(getConfig(), "BiomeSpecificMobs", biomeSpecificMobs);
 		
 		versionCheckEnabled = getConfig().getBoolean("EnableVersionCheck", true);
 		AbstractConfig.set(getConfig(), "EnableVersionCheck", versionCheckEnabled);
 		
 		// Copy the Config header into config.yml
 		AbstractConfig.copyHeader(getConfig(), AbstractConfig.getResourceAsString("configHeader.txt"), "MobManager Config v" + getDescription().getVersion() + "\n"
 				+ "\n\nValid EntityTypes:\n" + ExtendedEntityType.getExtendedEntityList() + AbstractConfig.getResourceAsString("Config_Header.txt"));
 		
 		if (!limiterEnabled && !abilitiesEnabled)
 		{
 			getLogger().warning("No components enabled :(");
 			return;
 		}
 		
 		integration.integrate();
 		
 		// Enable each component
 		if (limiterEnabled)
 			enableLimiter();
 		if (abilitiesEnabled)
 			enableAbilities();
 		
 		getCommand("mm").setExecutor(new MMCommandListener());
 		
 		AbstractConfig.set(getConfig(), "Integration", getConfig().getConfigurationSection("Integration"));
 		
 		// Save the config with the current version
 		AbstractConfig.set(getConfig(), "Version", getDescription().getVersion());
 		saveConfig();
 		
 		// Start Metrics gathering
 		startMetrics();
 	}
 
 	@Override
 	public void onDisable()
 	{		
 		// 'Attempt to' Cancel all tasks
 		getServer().getScheduler().cancelTasks(this);
 		
 		// Disable each component
 		if (limiterEnabled)
 			disableLimiter();
 		if (abilitiesEnabled)
 			disableAbilities();
 		
 		p = null;
 	}
 	
 	private void enableLimiter()
 	{
 		// Load Config
 		Config config = new Config();
 
 		// Setup worlds
 		worlds = new ConcurrentHashMap<String, MMWorld>(2, 0.75F, 2);
 		if (config.setupWorlds() == 0)
 		{
 			getLogger().warning("No valid worlds found");
 			getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
 
 		// Register Mob event listeners
 		getServer().getPluginManager().registerEvents(new MobListener(), this);
 		// Register Chunk event listener
 		getServer().getPluginManager().registerEvents(new ChunkListener(), this);
 
 		// Start the despawner task
 		despawner = new MobDespawnTask();
 		despawner.runTaskTimer(this, 1L, Config.ticksPerDespawnScan);
 
 		// Setup animal protection
 		if (Config.enableAnimalDespawning)
 		{
 			animalProtection = new AnimalProtection();
 			
 			getServer().getPluginManager().registerEvents(animalProtection, this);
 			animalProtection.runTaskTimerAsynchronously(this, Config.protectedFarmAnimalSaveInterval, Config.protectedFarmAnimalSaveInterval);
 		}
 		
		getLogger().info("v" + getDescription().getVersion() + " ennabled with " + worlds.size() + " worlds");
 	}
 	
 	private void disableLimiter()
 	{
 		if (despawner != null)
 			despawner.cancel();
 		
 		if (animalProtection != null)
 		{
 			animalProtection.cancel();
 			animalProtection.run();
 		}
 	}
 	
 	private void enableAbilities()
 	{
 		new AbilityConfig();
 		
 		// Register Mob event listeners
 		getServer().getPluginManager().registerEvents(new AbilitiesMobListener(), this);
 	}
 	
 	private void disableAbilities()
 	{
 		// Nothing to do here
 	}
 	
 	private void startMetrics()
 	{
 		try
 		{
 			Metrics metrics = new Metrics(this);
 			
 			Metrics.Graph componentGraph = metrics.createGraph("Components Used");
 			componentGraph.addPlotter(Plotters.limiterEnabled);
 			componentGraph.addPlotter(Plotters.abilitiesEnabled);
 			
 			Metrics.Graph versionGraph = metrics.createGraph("Version Stats");
 			versionGraph.addPlotter(Plotters.version);
 			
 			// TODO Remove these
 			// Shows percentage of servers which use the limiter component
 			Metrics.Graph limiterGraph = metrics.createGraph("Limiter Stats");
 			limiterGraph.addPlotter(new Metrics.Plotter("Enabled")
 			{
 				@Override
 				public int getValue()
 				{
 					return isLimiterEnabled() ? 1 : 0;
 				}
 			});
 			limiterGraph.addPlotter(new Metrics.Plotter("Disabled")
 			{
 				@Override
 				public int getValue()
 				{
 					return isLimiterEnabled() ? 0 : 1;
 				}
 			});
 			
 			// Shows percentage of servers which use the abilities component
 			Metrics.Graph abilitiesGraph = metrics.createGraph("Abilities Stats");
 			abilitiesGraph.addPlotter(new Metrics.Plotter("Enabled")
 			{
 				@Override
 				public int getValue()
 				{
 					return isAbilitiesEnabled() ? 1 : 0;
 				}
 			});
 			abilitiesGraph.addPlotter(new Metrics.Plotter("Disabled")
 			{
 				@Override
 				public int getValue()
 				{
 					return isAbilitiesEnabled() ? 0 : 1;
 				}
 			});
 			
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

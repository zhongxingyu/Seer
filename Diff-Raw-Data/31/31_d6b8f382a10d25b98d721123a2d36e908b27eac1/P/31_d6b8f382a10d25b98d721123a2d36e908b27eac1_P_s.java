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
 
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.forgenz.mobmanager.abilities.config.AbilityConfig;
 import com.forgenz.mobmanager.abilities.config.WorldAbilityConfig;
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
 	
 	public AbilityConfig abilityCfg = null;
 	
 	/* Enabled Components */
 	private boolean limiterEnabled;
 	private boolean abilitiesEnabled;
 	
 	public boolean isLimiterEnabled()
 	{
 		return limiterEnabled;
 	}
 	
 	public boolean isAbiltiesEnabled()
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
 		
 		// Start Metrics gathering
 		try
 		{
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		}
 		catch (IOException e)
 		{
 			getLogger().info("Failed to start metrics gathering..  :(");
 		}
 		
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
 		abilityCfg = new AbilityConfig();
 		
 		// Register Mob event listeners
 		getServer().getPluginManager().registerEvents(new AbilitiesMobListener(), this);
 		
 		// Sets already living mob HP to config settings
 		boolean hasGlobal = abilityCfg.globalCfg.mobs.size() != 0;
 		boolean addAbilities = hasGlobal;
 		
 		// If there are no global configs we check if there are any world configs
 		if (!hasGlobal)
 		{
 			for (String world : abilityCfg.enabledWorlds)
 			{
 				WorldAbilityConfig worldCfg = abilityCfg.getWorldConfig(world);
 				if (worldCfg != null && worldCfg.mobs.size() != 0)
 				{
 					addAbilities = true;
 					break;
 				}
 			}
 		}
 		
 		// Check if we should bother iterating through entities
 		if (addAbilities)
 		{
 			// Iterate through each enabled world
			for (String world : abilityCfg.enabledWorlds)
 			{
				WorldAbilityConfig worldCfg = abilityCfg.getWorldConfig(world);
 				
 				// If there are no global configs and the world has no configs, check next world
 				if (!hasGlobal && worldCfg != null && worldCfg.mobs.size() == 0)
 					continue;
 				
 				// Iterate through each entity in the world and set their max HP accordingly
				for (LivingEntity entity : getServer().getWorld(world).getLivingEntities())
 				{
 					AbilitiesMobListener.addAbilities(entity, null);
 				}
 			}
 		}
 	}
 	
 	private void disableAbilities()
 	{		
 		// Resets mob abilities
 		boolean hasGlobal = abilityCfg.globalCfg.mobs.size() != 0;
 		boolean addAbilities = hasGlobal;
 		
 		// If there are no global configs we check if there are any world configs
 		if (!hasGlobal)
 		{
 			for (String world : abilityCfg.enabledWorlds)
 			{
 				WorldAbilityConfig worldCfg = abilityCfg.getWorldConfig(world);
 				if (worldCfg != null && worldCfg.mobs.size() != 0)
 				{
 					addAbilities = true;
 					break;
 				}
 			}
 		}
 		
 		// Check if we should bother iterating through entities
 		if (addAbilities)
 		{
 			// Iterate through each enabled world
			for (String world : abilityCfg.enabledWorlds)
 			{
				WorldAbilityConfig worldCfg = abilityCfg.getWorldConfig(world);
 				
 				// If there are no global configs and the world has no configs, check next world
 				if (!hasGlobal && worldCfg != null && worldCfg.mobs.size() == 0)
 					continue;
 				
 				// Iterate through each entity in the world and set their max HP accordingly
				for (LivingEntity entity : getServer().getWorld(world).getLivingEntities())
 				{
 					AbilitiesMobListener.removeAbilities(entity);
 				}
 			}
 		}
 		
 
 		abilityCfg = null;
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

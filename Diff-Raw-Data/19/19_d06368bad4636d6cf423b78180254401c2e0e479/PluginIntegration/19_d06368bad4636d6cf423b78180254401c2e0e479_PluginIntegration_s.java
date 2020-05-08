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
 
 package com.forgenz.mobmanager.common.integration;
 
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.plugin.Plugin;
 
 import com.forgenz.mobmanager.P;
 import com.forgenz.mobmanager.common.config.AbstractConfig;
 
 /**
  * Used to stop MobManager from despawning/editing special mobs spawned by other plugins<br/>
  * <br/>
  * To integrate your plugin with MobManager simply create a class which implements Protector
  * and register it with PluginIntegration's instance
  * @author Michael McKnight (ShadowDog007)
  *
  */
 public class PluginIntegration implements Protector
 {
 	private static PluginIntegration integration = null;
 	@Deprecated
 	public static PluginIntegration getInstance()
 	{
 		return integration;
 	}
 	
 	private static ConcurrentHashMap<Plugin, Protector> protectors = new ConcurrentHashMap<Plugin, Protector>();
 	
 	public PluginIntegration()
 	{
 		if (integration == null)
 			integration = this;
		
 		/* Plugins Integrated by me :) */
 		if (P.p().getConfig().getBoolean("Integration.EpicBossRecoded", true))
 			new EpicBossRecoded();
 		AbstractConfig.set(P.p().getConfig(), "Integration.EpicBossRecoded", P.p().getConfig().getBoolean("Integration.EpicBossRecoded", true));
 	}
 	
 	/**
 	 * @param plugin The plugin which the protector is registered for
 	 * @return True if the plugin has registered a protector
 	 */
 	public boolean isRegistered(Plugin plugin)
 	{
 		return protectors.containsKey(plugin);
 	}
 	
 	/**
 	 * Registers a protector for a given plugin
 	 * 
 	 * @param plugin The plugin the protector belongs to
 	 * @param protector The protector!
 	 */
 	public void registerProtector(Plugin plugin, Protector protector)
 	{
 		if (plugin == null || protector == null)
 			throw new NullPointerException();
 		
 		protectors.put(plugin, protector);
 	}
 	
 	/**
 	 * Used when a plugin is disabled<br/>
 	 * Note you do not have to do this. MobManager will simply ignore
 	 * protectors with disabled plugins
 	 * @param plugin The plugin which is having its protector removed
 	 */
 	public void deregisterProtector(Plugin plugin)
 	{
 		protectors.remove(plugin);
 	}
 	
 	/* #### Protectors #### */
 	@Override
 	public boolean canDespawn(LivingEntity entity)
 	{
 		if (entity == null)
 			return true;
 		
 		final boolean async = !P.p().getServer().isPrimaryThread();
 		
 		for (Entry<Plugin, Protector> entry : protectors.entrySet())
 		{
 			if (!entry.getKey().isEnabled())
 				continue;
 			
 			Protector protector = entry.getValue();
 			
 			try
 			{
 				if (async && !protector.supportsAsynchronousUsage())
 					continue;
 			
 				if (!protector.canDespawn(entity))
 					return false;
 			}
 			catch (Exception e)
 			{
 				P.p().getLogger().severe("Caught Exception while checking if a mob could despawn");
 				e.printStackTrace();
 			}
 		}
 		
 		return true;
 	}
 
 	@Override
 	public boolean canApplyAbilities(LivingEntity entity)
 	{
 		if (entity == null)
 			return true;
 		
 		final boolean async = !P.p().getServer().isPrimaryThread();
 		
 		for (Entry<Plugin, Protector> entry : protectors.entrySet())
 		{
 			if (!entry.getKey().isEnabled())
 				continue;
 			
 			Protector protector = entry.getValue();
 			
 			try
 			{
 				if (async && !protector.supportsAsynchronousUsage())
 					continue;
 				
 				if (!protector.canApplyAbilities(entity))
 					return false;
 			}
 			catch (Exception e)
 			{
 				P.p().getLogger().severe("Caught Exception while checking if a mob could have abilities");
 				e.printStackTrace();
 			}
 		}
 		
 		return true;
 	}
 	
 	@Override
 	public boolean supportsAsynchronousUsage()
 	{
 		return true;
 	}
 	
 	/* #### Component Flags #### */
 	public boolean limiterEnabled()
 	{
 		return P.p().isLimiterEnabled();
 	}
 	
 	public boolean abilitiesEnabled()
 	{
 		return P.p().isAbiltiesEnabled();
 	}
 	
 	/* #### IgnoreSpawn Flags #### */
 	/**
 	 * @param value True if all components of MobManager should ignore the next spawn
 	 */
 	public void ignoreNextSpawn(boolean value)
 	{
 		P.p().ignoreNextSpawn(value);
 	}
 	
 	/**
 	 * @param value True if the limiter component of MobManager should ignore the next spawn
 	 */
 	public void limiterIgnoreNextSpawn(boolean value)
 	{
 		P.p().limiterIgnoreNextSpawn(value);
 	}
 	
 	/**
 	 * @param value True if the abilities component of MobManager should ignore the next spawn
 	 */
 	public void abilitiesIgnoreNextSpawn(boolean value)
 	{
 		P.p().abilitiesIgnoreNextSpawn(value);
 	}
 }

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
 
 package com.forgenz.mobmanager.bounty.config;
 
 import java.util.HashMap;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.LivingEntity;
 
 import com.forgenz.mobmanager.MMComponent;
 import com.forgenz.mobmanager.abilities.abilities.AbilitySet;
 import com.forgenz.mobmanager.common.config.AbstractConfig;
 import com.forgenz.mobmanager.common.util.ExtendedEntityType;
 
 public class BountyWorldConfig extends AbstractConfig
 {
 	public final boolean useWorldSettings;
 	public final boolean allowPetKills;
 	public final boolean showMobName, showAbilitySetName;
 	
 	private final HashMap<ExtendedEntityType, BountyMobConfig> mobRewards = new HashMap<ExtendedEntityType, BountyMobConfig>();
 	
 	public BountyWorldConfig(FileConfiguration cfg, String folder)
 	{
 		ConfigurationSection sect;
 		
 		/* ################ UseWorldSettings ################ */
 		if (folder.length() != 0)
 		{
 			useWorldSettings = cfg.getBoolean("UseWorldSettings", false);
			set(cfg, "UseWorldsettings", useWorldSettings);
 		}
 		else
 		{
 			useWorldSettings = true;
 		}
 		
 		/* ################ AllowPetKills ################ */
 		allowPetKills = cfg.getBoolean("AllowPetKills", true);
 		set(cfg, "AllowPetKills", allowPetKills);
 		
 		/* ################ ShowMobName ################ */
 		showMobName = cfg.getBoolean("ShowMobName", true);
 		set(cfg, "ShowMobName", showMobName);
 		
 		/* ################ ShowAbilitySetName ################ */
 		showAbilitySetName = cfg.getBoolean("ShowAbilitySetName", true);
 		set(cfg, "ShowAbilitySetName", showAbilitySetName);
 		
 		/* ################ Rewards ################ */
 		sect = getConfigurationSection(cfg, "Rewards");
 		
 		for (ExtendedEntityType entitytype : ExtendedEntityType.values())
 		{
 			String path = entitytype.toString();
 			ConfigurationSection mobSect = sect.getConfigurationSection(path);
 			if (mobSect == null)
 				mobSect = sect.createSection(path);
 			
 			BountyMobConfig mobCfg = new BountyMobConfig(mobSect);
 			
 			if (mobCfg.getMinReward() != 0.0 || mobCfg.getDifference() != 0.0)
 			{
 				mobRewards.put(entitytype, mobCfg);
 			}
 		}
 	}
 	
 	public double getReward(ExtendedEntityType entityType)
 	{
 		BountyMobConfig mobReward = mobRewards.get(entityType);
 		
 		return mobReward != null ? mobReward.getReward() : 0.0;
 	}
 	
 	public String getMobName(LivingEntity entity)
 	{
 		if (showMobName)
 		{
 			String name = entity.getCustomName();
 			if (name != null)
 				return name;
 		}
 		if (showAbilitySetName && MMComponent.getAbilities().isEnabled())
 		{
 			String name = AbilitySet.getMeta(entity);
 			if (name != null)
 				return Character.toUpperCase(name.charAt(0)) + name.substring(1);
 		}
 		
 		return null;
 	}
 }

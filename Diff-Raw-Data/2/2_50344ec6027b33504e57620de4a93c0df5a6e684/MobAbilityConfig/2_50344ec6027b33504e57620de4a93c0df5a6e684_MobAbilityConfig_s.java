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
 
 package com.forgenz.mobmanager.abilities.config;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.bukkit.configuration.ConfigurationSection;
 
 import com.forgenz.mobmanager.abilities.AbilityType;
 import com.forgenz.mobmanager.abilities.abilities.Ability;
 import com.forgenz.mobmanager.abilities.abilities.AngryAbility;
 import com.forgenz.mobmanager.abilities.abilities.BabyAbility;
 import com.forgenz.mobmanager.abilities.abilities.ChargedCreeperAbility;
 import com.forgenz.mobmanager.abilities.util.MiscUtil;
 import com.forgenz.mobmanager.abilities.util.ValueChance;
 import com.forgenz.mobmanager.common.config.AbstractConfig;
 import com.forgenz.mobmanager.common.util.ExtendedEntityType;
 
 public class MobAbilityConfig extends AbstractConfig
 {	
 	public final ExtendedEntityType mob;
 	
 	public final float equipmentDropChance;
 	
 	public final float spawnRate;
 	public final float babyRate;
 	public final float angryRate;
 	public final float chargedRate;
 	
 	public final HashMap<AbilityType, ValueChance<Ability>> attributes;
 	
 	public MobAbilityConfig(ExtendedEntityType mob, ConfigurationSection cfg)
 	{
 		attributes = new HashMap<AbilityType, ValueChance<Ability>>();
 		
 		this.mob = mob;
 		
 		/* ######## SpawnRate ######## */
 		float spawnRate = (float) cfg.getDouble("SpawnRate", 1.0F);
		if (spawnRate <= 0.0F)
 			spawnRate = 1.0F;
 		this.spawnRate = spawnRate;
 		set(cfg, "SpawnRate", spawnRate);
 		
 		/* ######## EquipmentDropChance ######## */
 		float equipmentDropChance = (float) cfg.getDouble("EquipmentDropChance", 0.15F);
 		this.equipmentDropChance = equipmentDropChance < 0 ? 0.0F : equipmentDropChance;
 		set(cfg, "EquipmentDropChance", this.equipmentDropChance);
 		
 		/* ######## BabyRate ######## */
 		if (BabyAbility.isValid(mob))
 		{
 			float babyRate = (float) cfg.getDouble("BabyRate", 0.0F);
 			if (babyRate <= 0.0F)
 				babyRate = 0.0F;
 			this.babyRate = babyRate;
 			set(cfg, "BabyRate", babyRate);
 		}
 		else
 		{
 			babyRate = 0.0F;
 		}
 		
 		/* ######## AngryRate ######## */
 		if (AngryAbility.isValid(mob.getBukkitEntityType()))
 		{
 			float angryRate = (float) cfg.getDouble("AngryRate", 0.0F);
 			if (angryRate <= 0.0F)
 				angryRate = 0.0F;
 			this.angryRate = angryRate;
 			set(cfg, "AngryRate", angryRate);
 		}
 		else
 		{
 			angryRate = 0.0F;
 		}
 		
 		/* ######## ChargedRate ######## */
 		if (ChargedCreeperAbility.isValid(mob.getBukkitEntityType()))
 		{
 			float chargedRate = (float) cfg.getDouble("ChargedRate", 0.0F);
 			if (chargedRate <= 0.0F)
 				chargedRate = 0.0F;
 			this.chargedRate = chargedRate;
 			set(cfg, "ChargedRate", chargedRate);
 		}
 		else
 		{
 			chargedRate = 0.0F;
 		}
 		
 		/* ######## ValueChance Abilities ######## */
 		for (AbilityType ability : AbilityType.values())
 		{
 			// Ignore abilities which don't work as ValueChance + Stand alone
 			if (!ability.isValueChanceAbility())
 				continue;
 			
 			ValueChance<Ability> abilityChances = new ValueChance<Ability>();
 			// Fetch String list from config
 			List<Object> optList = MiscUtil.getList(cfg.getList(ability.getConfigPath()));
 			if (optList == null)
 				optList = new ArrayList<Object>();
 			// Store the ability chances if there is at least one
 			if (ability.setup(mob, abilityChances, optList))
 				attributes.put(ability, abilityChances);
 			
 			// Update String list (ability.setup can removes invalid settings)
 			set(cfg, ability.getConfigPath(), optList);
 		}
 			
 	}
 }

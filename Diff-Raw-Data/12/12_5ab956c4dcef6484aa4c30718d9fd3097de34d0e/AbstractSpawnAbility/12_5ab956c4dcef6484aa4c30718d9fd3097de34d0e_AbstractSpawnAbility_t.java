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
 
 package com.forgenz.mobmanager.abilities.abilities;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Location;
 import org.bukkit.entity.LivingEntity;
 
 import com.forgenz.mobmanager.MMComponent;
 import com.forgenz.mobmanager.P;
 import com.forgenz.mobmanager.abilities.AbilityType;
 import com.forgenz.mobmanager.abilities.config.AbilityConfig;
import com.forgenz.mobmanager.abilities.listeners.AbilitiesMobListener;
 import com.forgenz.mobmanager.abilities.util.ValueChance;
 import com.forgenz.mobmanager.common.util.ExtendedEntityType;
 import com.forgenz.mobmanager.common.util.MiscUtil;
 import com.forgenz.mobmanager.common.util.RandomLocationGen;
 
 public abstract class AbstractSpawnAbility extends Ability
 {
 	protected final static Location loc = new Location(null, 0, 0, 0);
 	private static boolean spawning = false;
 	
 	private final ExtendedEntityType type;
 	private final int count;
 	private final String abilitySet;
 	private final int range;
 	private final int heightRange;
 	
 	protected AbstractSpawnAbility(ExtendedEntityType type, int count, String abilitySet, int range, int heightRange)
 	{
 		this.type = type;
 		this.count = count;
 		this.abilitySet = abilitySet;
 		this.range = range;
 		this.heightRange = heightRange;
 	}
 
 	@Override
 	public void addAbility(LivingEntity entity)
 	{
 		if (spawning)
 			return;
 		
 		// Make sure the bonus mobs don't recursively spawn more bonus mobs
 		spawning = true;
 		
 		// Get the ability set being assigned to the mob
 		AbilitySet abilities = abilitySet != null ? AbilitySet.getAbilitySet(abilitySet) : null;
 		// Get the entity type the mob will be
 		ExtendedEntityType type = this.type != null ? this.type : (abilitySet != null ? abilities.type : null);
 		
 		// If there is no entity type return
 		if (type == null)
 			return;
 		
 		// Copy the entities location into the cache
 		entity.getLocation(loc);
 		
 		// Spawn each mob
 		for (int i = 0; i < count; ++i)
 		{
 			// If the mob has an ability set we do not add any abilities
 			if (abilities != null)
 				P.p().abilitiesIgnoreNextSpawn(true);
 			
 			if (!AbilityConfig.i().limitBonusSpawns)
 				P.p().limiterIgnoreNextSpawn(true);
 			
 			Location spawnLoc;
 			if (AbilityConfig.i().radiusBonusSpawn)
 				spawnLoc = RandomLocationGen.getLocation(AbilityConfig.i().useCircleLocationGeneration, loc, range, 1, heightRange);
 			else
 				spawnLoc = loc;
 			// Spawn the entity
 			LivingEntity spawnedEntity = type.spawnMob(spawnLoc);
 			
 			if (abilities != null && spawnedEntity != null)
			{
				abilities.addAbility((LivingEntity) entity);
				abilities.getAbilityConfig().applyRates((LivingEntity) entity);
				
				if (abilities.applyNormalAbilities())
				{
					AbilitiesMobListener.applyNormalAbilities((LivingEntity) entity, null);
				}
			}
 		}
 		
 		// Make sure we can spawn more mobs later :)
 		spawning = false;
 	}
 
 	public static void setup(AbilityType type, ExtendedEntityType mob, ValueChance<Ability> abilityChances, List<Object> optList)
 	{
 		Iterator<Object> it = optList.iterator();
 			
 		// Iterate through each option map in the list
 		while (it.hasNext())
 		{
 			// Fetch the option map from the iterator
 			Map<String, Object> optMap = MiscUtil.getConfigMap(it.next());
 			
 			// If no map was found continue
 			if (optMap == null)
 				continue;
 			
 			// Get the chance from the map
 			int chance = MiscUtil.getInteger(optMap.get("CHANCE"));
 			
 			// Validate the chance
 			if (chance <= 0)
 				continue;
 			
 			// Fetch an object of the spawn ability
 			AbstractSpawnAbility ability = setup(type, mob, optMap);
 			
 			// Add the ability to the abilityChances if valid
 			if (ability != null)
 				abilityChances.addChance(chance, ability);
 		}
 	}
 
 	public static AbstractSpawnAbility setup(AbilityType type, ExtendedEntityType mob, Map<String, Object> optMap)
 	{
 		// Check for a MobType
 		String mobTypeString = MiscUtil.getMapValue(optMap, "MOBTYPE", null, String.class);
 		
 		ExtendedEntityType mobType = null;
 		
 		// Try fetch a mobtype for the spawn
 		if (mobTypeString != null)
 		{
 			mobType = mobTypeString.equalsIgnoreCase("NONE") ? null : ExtendedEntityType.valueOf(mobTypeString);
 			
 			if (!mobTypeString.equalsIgnoreCase("NONE") && mobType == null)
 			{
 				MMComponent.getAbilities().warning("No EntityType called " + mobTypeString + " for " + (type == AbilityType.BIRTH_SPAWN ? "Birth" : "Death") + " Spawn");
 				return null;
 			}
 		}
 		
 		// Get the number of mobs spawned
 		int count = MiscUtil.getInteger(optMap.get("COUNT"));
 		
 		// Validate the mob count
 		if (count <= 0)
 			count = 1;
 		
 		// Get the ability set the mobs will spawn as
 		String abilitySet = MiscUtil.getMapValue(optMap, "ABILITYSET", null, String.class);
 		
 		// If neither a mob type or an ability set was found the options are invalid 
 		if (mobType == null && abilitySet == null && !mobTypeString.equalsIgnoreCase("NONE"))
 		{
 			MMComponent.getAbilities().warning("You must provide a MobType or AbilitySet in Birth/Death Spawns");
 			return null;
 		}
 		
 		int range = MiscUtil.getInteger(optMap.get("RANGE"), AbilityConfig.i().bonusSpawnRange);
 		int heightRange = MiscUtil.getInteger(optMap.get("HEIGHTRANGE"), AbilityConfig.i().bonusSpawnHeightRange);
 		
 		switch (type)
 		{
 		case BIRTH_SPAWN:
 			return new BirthSpawnAbility(mobType, count, abilitySet, range, heightRange);
 		case DEATH_SPAWN:
 			return new DeathSpawnAbility(mobType, count, abilitySet, range, heightRange);
 		default:
 			return null;
 		}
 	}
 
 	public static Ability setup(AbilityType type, ExtendedEntityType mob, Object opt)
 	{
 		Map<String, Object> map = MiscUtil.getConfigMap(opt);
 		
 		if (map == null)
 		{
 			MMComponent.getAbilities().warning(String.format("Found an error in abilities config for %s-%s. The value should be a map", mob.toString(), type.toString()));
 			return null;
 		}
 		
 		return setup(type, mob, map);
 	}
 
 }

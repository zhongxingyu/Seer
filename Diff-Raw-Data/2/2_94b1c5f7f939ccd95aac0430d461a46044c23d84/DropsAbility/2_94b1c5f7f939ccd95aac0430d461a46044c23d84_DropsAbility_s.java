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
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 
 import com.forgenz.mobmanager.P;
 import com.forgenz.mobmanager.abilities.AbilityType;
 import com.forgenz.mobmanager.abilities.util.MiscUtil;
 import com.forgenz.mobmanager.abilities.util.ValueChance;
 import com.forgenz.mobmanager.common.util.ExtendedEntityType;
 import com.forgenz.mobmanager.common.util.Patterns;
 import com.forgenz.mobmanager.limiter.config.Config;
 
 public class DropsAbility extends Ability
 {
 	public static class DropSet
 	{
 		public final Material material;
 		public final byte data;
 		public final int min;
 		public final int range;
 		public final short durability;
 		private List<Enchantment> enchantments;
 		private List<Integer> enchantmentLevels;
 		
 		public DropSet(Material material, byte data, int durability, int min, int max)
 		{
 			this.material = material;
 			this.data = data;
 			this.durability = (short) (durability > material.getMaxDurability() ? material.getMaxDurability() : durability < 0 ? 0 : durability);
 			
 			if (min > max)
 			{
 				min = min ^ max;
 				max = min ^ max;
 				min = min ^ max;
 			}
 			this.min = min;
 			this.range = max - min;
 		}
 		
 		public void addEnchantment(Enchantment e, int level)
 		{
 			if (e == null)
 				return;
 			
 			if (level < 0)
 				level = 0;
 			
 			if (enchantments == null)
 			{
 				enchantments = new ArrayList<Enchantment>(1);
 				enchantmentLevels = new ArrayList<Integer>(1);
 			}
 			
 			if (enchantments.contains(e))
 				return;
 			
 			enchantments.add(e);
 			enchantmentLevels.add(level);
 		}
 		
 		public List<ItemStack> getItem()
 		{
 			// Calculate the number of items to create
 			int count = range != 0 ? Config.rand.nextInt(range) + min : min;
 			
 			// Make sure count is more than 0
 			if (count <= 0)
 				return null;
 			
 			List<ItemStack> itemz = new ArrayList<ItemStack>(count / material.getMaxStackSize() + 1);
 			
 			// Create the item stack/stacks
 			while (count > 0)
 			{
 				itemz.add(new ItemStack(material, count > material.getMaxStackSize() ? material.getMaxStackSize() : count, durability));
				
 			}
 			
 			// If data is not 0 add the data
 			if (data != 0)
 				for (ItemStack item : itemz)
 					item.setData(new MaterialData(material, data));
 			
 			// If there are any enchantments, add them
 			if (enchantments != null)
 			{
 				for (int i = 0; i < enchantments.size(); ++i)
 				{
 					for (ItemStack item : itemz)
 						item.addEnchantment(enchantments.get(i), enchantmentLevels.get(i));
 				}
 			}
 			
 			// Return the item in question
 			return itemz;
 		}
 	}
 	
 	public static final String metaStorageKey = "MOBMANAGER_DROPS";
 	public static final DropsAbility emptyDrops = new DropsAbility(null, false);
 	
 	private final ArrayList<DropSet> drops;
 	private final boolean replaceDrops;
 	
 	private DropsAbility(ArrayList<DropSet> drops, boolean replaceDrops)
 	{
 		this.drops = drops;
 		this.replaceDrops = replaceDrops;
 	}
 	
 	@Override
 	public void addAbility(LivingEntity entity)
 	{
 		if (drops != null)
 		{
 			entity.setMetadata(metaStorageKey, new FixedMetadataValue(P.p(), this));
 		}
 	}
 
 	@Override
 	public AbilityType getAbilityType()
 	{
 		return AbilityType.DROPS;
 	}
 	
 	public boolean replaceDrops()
 	{
 		return replaceDrops;
 	}
 	
 	public List<ItemStack> getItemList()
 	{
 		// Creates a list of items
 		List<ItemStack> items = new ArrayList<ItemStack>();
 		
 		// Make sure there are drops
 		if (drops != null)
 		{
 			// Iterate through each dropset
 			for (DropSet drop : drops)
 			{
 				// Get a list of itemstacks from the dropset
 				List<ItemStack> itemz = drop.getItem();
 				
 				// If the dropset provided a list add them to the main list
 				if (itemz != null)
 				{
 					for (ItemStack item : itemz)
 					{
 						items.add(item);
 					}
 				}
 			}
 		}
 		
 		return items;
 	}
 	
 	public static DropsAbility getAbility(LivingEntity entity)
 	{
 		List<MetadataValue> metaList = entity.getMetadata(metaStorageKey);
 		
 		if (metaList == null)
 			return null;
 		
 		for (MetadataValue meta : metaList)
 		{
 			if (meta.getOwningPlugin() != P.p())
 				continue;
 			
 			if (meta.value() instanceof DropsAbility)
 				return (DropsAbility) meta.value();
 		}
 		
 		return null;
 	}
 
 	public static void setup(ExtendedEntityType mob, ValueChance<Ability> abilityChances, List<Object> optList)
 	{
 		Iterator<?> it = optList.iterator();
 		
 		// Iterate through each object
 		while (it.hasNext())
 		{
 			// Fetch the map object
 			Map<String, Object> optMap = MiscUtil.getConfigMap(it.next());
 			
 			// Continue if no map was found
 			if (optMap == null)
 			{
 				P.p().getLogger().warning("Invalid options given to " + AbilityType.DROPS + " ability");
 				continue;
 			}
 			
 			// Fetch the chance for the object
 			int chance = MiscUtil.getInteger(optMap.get("CHANCE"));
 			
 			// Validate the chance
 			if (chance <= 0)
 				continue;
 			
 			// Fetch the ability
 			DropsAbility ability = setup(mob, optMap);
 			
 			// If the ability is valid add it to the abilityChances
 			if (ability != null)
 				abilityChances.addChance(chance, ability);
 		}
 	}
 	
 	public static DropsAbility setup(ExtendedEntityType mob, Map<String, Object> optMap)
 	{
 		List<Object> drops = MiscUtil.getList(optMap.get("DROPS"));
 		ArrayList<DropSet> dropSets = null;
 		
 		// If drops is provided we fetch each set of drops
 		if (drops != null)
 		{
 			// Create a list for the drops to be stored
 			dropSets = new ArrayList<DropSet>();
 			// Iterate through each object and look for drops
 			for (Object obj : drops)
 			{
 				// Fetch the map containing drop settings
 				Map<String, Object> dropMap = MiscUtil.getConfigMap(obj);
 				
 				// If no map was found continue
 				if (dropMap == null)
 					continue;
 				
 				// Fetch the ID  for the drop
 				int id = MiscUtil.getInteger(dropMap.get("ID"), -1);
 				
 				// Fetch the material for the drop
 				Material material = Material.getMaterial(id);
 				
 				// If the material is invalid, check next drop
 				if (material == null)
 					continue;
 				
 				// Fetch the data for the drop
 				byte data = (byte) MiscUtil.getInteger(dropMap.get("DATA"));
 				
 				// Fetch the min and max counts for the drop
 				int minCount = MiscUtil.getInteger(dropMap.get("MINCOUNT"), 1);
 				int maxCount = MiscUtil.getInteger(dropMap.get("MAXCOUNT"), minCount);
 				
 				int durability = MiscUtil.getInteger(dropMap.get("DURABILITY"), Integer.MAX_VALUE);
 				
 				// Create a new DropSet and store it in the list
 				DropSet drop = new DropSet(material, data, durability, minCount, maxCount);
 				dropSets.add(drop);
 				
 				// Fetch enchantments and add them to the DropSet
 				List<Object> enchantments = MiscUtil.getList(dropMap.get("Enchantments"));
 				
 				// Ignore enchantments if key does not match
 				if (enchantments == null)
 					continue;
 				
 				// Find enchantments from the list
 				for (Object enchObj : enchantments)
 				{
 					// Fetch the string representing the enchantment
 					String ench = MiscUtil.getString(enchObj);
 					
 					// If no string was found check next object
 					if (ench == null)
 						continue;
 					
 					// Split the enchantment and the level
 					String[] split = Patterns.colonSplit.split(ench);
 					
 					// If WTF ignore the enchantment
 					if (split.length < 1)
 						continue;
 					
 					// Fetch the enchantment object
 					Enchantment enchantment = Enchantment.getByName(split[0].toUpperCase());
 					
 					if (enchantment == null)
 					{
 						P.p().getLogger().warning("Invalid enchantment given: " + split[0].toUpperCase());
 						continue;
 					}
 					
 					// Make sure you can enchant the given material with the found enchantment
 					if (!enchantment.canEnchantItem(new ItemStack(material)))
 					{
 						P.p().getLogger().warning("Can not enchant " + material.toString() + " with the enchantment: " + enchantment.toString());
 						continue;
 					}
 					
 					// Get the level of the enchantment
 					int level = split.length == 2 && Patterns.numberCheck.matcher(split[1]).matches() ? Integer.valueOf(split[0]) : enchantment.getStartLevel();
 					
 					// Validate the enchantment level
 					if (level > enchantment.getMaxLevel())
 						level = enchantment.getMaxLevel();
 					
 					if (level < enchantment.getStartLevel())
 						level = enchantment.getStartLevel();
 					
 					// Add the enchantment to the DropSet
 					drop.addEnchantment(enchantment, level);
 				}
 			}
 		}
 		
 		Boolean replaceDrops = MiscUtil.getMapValue(optMap, "REPLACE", null, Boolean.class);
 		
 		// Finally create the drops ability and return it
 		return drops.size() > 0 ? new DropsAbility(dropSets, replaceDrops != null ? replaceDrops : false) : DropsAbility.emptyDrops;
 	}
 
 	public static DropsAbility setup(ExtendedEntityType mob, Object opt)
 	{
 		Map<String, Object> optMap = MiscUtil.getConfigMap(opt);
 		
 		return optMap == null ? null : setup(mob, optMap);
 	}
 
 }

 package com.comphenix.xp.lookup;
 
 /**
  *  ExperienceMod - Bukkit server plugin for modifying the experience system in Minecraft.
  *  Copyright (C) 2012 Kristian S. Stangeland
  *
  *  This program is free software; you can redistribute it and/or modify it under the terms of the 
  *  GNU General Public License as published by the Free Software Foundation; either version 2 of 
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
  *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
  *  See the GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License along with this program; 
  *  if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 
  *  02111-1307 USA
  */
 
 import java.util.HashMap;
 import java.util.Queue;
 
 import org.bukkit.DyeColor;
 import org.bukkit.GrassSpecies;
 import org.bukkit.Material;
 import org.bukkit.SandstoneType;
 import org.bukkit.TreeSpecies;
 import org.bukkit.material.MonsterEggs;
 import org.bukkit.material.Step;
 import org.bukkit.potion.PotionType;
 
 import com.comphenix.extra.SmoothBrickType;
 
 public class ItemParser {
 	
 	private static HashMap<String, Material> alternativeNames = new HashMap<String, Material>();
 
 	// New blocks
 	private static final int WOODEN_STEPS = 125;
 	private static final int WOODEN_DOUBLE_STEPS = 126;
 	
 	private static final int COAL_NORMAL = 0;
 	private static final int COAL_CHARCOAL = 1;
 	
 	static {
 		alternativeNames.put("WOOL_DYE", Material.INK_SACK);
 		alternativeNames.put("WOOL_DYES", Material.INK_SACK);
 		alternativeNames.put("SLAB", Material.STEP);
 		alternativeNames.put("DOUBLE_SLAB", Material.DOUBLE_STEP);
 		alternativeNames.put("STONE_BRICK", Material.SMOOTH_BRICK);
 		alternativeNames.put("STONE_BRICK_STAIRS", Material.SMOOTH_STAIRS);
 		alternativeNames.put("HUGE_BROWN_MUSHROOM", Material.HUGE_MUSHROOM_1);
 		alternativeNames.put("HUGE_RED_MUSHROOM", Material.HUGE_MUSHROOM_2);
 		alternativeNames.put("SILVERFISH_BLOCK", Material.MONSTER_EGGS);
 		alternativeNames.put("RECORD_1", Material.GOLD_RECORD);
 		alternativeNames.put("RECORD_2", Material.GREEN_RECORD);
 		alternativeNames.put("BOTTLE_O_ENCHANTING", Material.EXP_BOTTLE);
 	}
 	
 	public Query fromItemString(String text) throws ParsingException {
 		
 		if (text.length() == 0)
 			// Empty names are not legal in YAML, so this shouldn't be possible 
 			throw new IllegalArgumentException("Key must have some characters.");
 		
 		Queue<String> tokens = Parsing.getParameterQueue(text);
 
 		Integer itemID = parseMaterial(tokens);
 		Integer durability = parseDurability(itemID, tokens);
 
 		// Special parsing rules
 		if (itemID == Material.POTION.getId() && durability == null) {
 			return parsePotion(tokens);
 		}
 		
 		return (Query) new ItemQuery(itemID, durability);
 	}
 	
 	/**
 	 * Determines the item, either by ID or name, at the top of the queue. Only consumes the element 
 	 * if the parsing was successful. 
 	 * @param tokens Queue of elements to use.
 	 * @return ID of the item parsed, of NULL if no item could be read.
 	 * @throws ParsingException Invoked when an unrecognized item name is given.
 	 */
 	private Integer parseMaterial(Queue<String> tokens) throws ParsingException {
 		
 		String current = Parsing.peekOrEmpty(tokens);
 		Material material = Material.matchMaterial(current);
 		Integer itemID = Parsing.tryParse(tokens);
 		
 		// Is this an item?
 		if (itemID == null && !Parsing.isNullOrIgnoreable(current)) {
 			
 			if (material != null) {
 				itemID = material.getId();
 
 			} else {
 				// Try some additional values
 				String filtered = Parsing.getEnumName(current);
 
 				if (alternativeNames.containsKey(filtered))
 					itemID = alternativeNames.get(filtered).getId();
 				else
 					throw ParsingException.fromFormat("Unable to find item %s.", current);
 			}
 			
 			// Element has been used
 			tokens.remove();
 		}
 		
 		// May be null
 		return itemID;
 	}
 	
 	private Integer parseDurability(Integer itemID, Queue<String> tokens) throws ParsingException {
 		
 		// Empty means DON'T CARE
 		if (Parsing.isNullOrIgnoreable(tokens.peek())) {
 			return null;
 		}
 		
 		String filtered = Parsing.getEnumName(tokens.peek());
 		Integer durability = Parsing.tryParse(tokens);
 		
 		if (durability == null) {
 			
 			// Special cases
 			if (itemID == Material.WOOD.getId() || itemID == Material.LEAVES.getId() || 
 				itemID == WOODEN_STEPS || itemID == WOODEN_DOUBLE_STEPS) {
 				
 				return getTreeSpecies(tokens, filtered);
 
 			} else if (itemID == Material.WOOL.getId()) {
 				
 				// Convert color values
 				return getDyeColor(tokens, filtered);
 				
 			} else if (itemID == Material.GRASS.getId()) { 
 				
 				// Grass types
 				return getGrassSpecies(tokens, filtered);
 
 			} else if (itemID == Material.STEP.getId() || itemID == Material.DOUBLE_STEP.getId()) {
 				
 				return getStepMaterial(tokens);
 				
 			} else if (itemID == Material.SANDSTONE.getId()) {
 		
 				return getSandstoneType(tokens, filtered);
 			
 			} else if (itemID == Material.SMOOTH_BRICK.getId() || itemID == Material.SMOOTH_STAIRS.getId()) {
 				
 				return getSmoothstoneType(tokens, filtered);
 				
 			} else if (itemID == Material.MONSTER_EGGS.getId()) {
 				
 				return getMonsterEgg(tokens, filtered);
 				
 			} else if (itemID == Material.COAL.getId()) {
 				
 				return getCoalData(tokens, filtered);
 				
 			} else if (itemID == Material.POTION.getId()) {
 				// Let the caller figure this out
 				return null; 
 			}
 			
 			// Cannot parse durability
 			throw ParsingException.fromFormat("Invalid durability value %s.", tokens.peek());
 		}
 		
 		return durability;
 	}
 
 	private int getCoalData(Queue<String> tokens, String filtered) throws ParsingException {
 		
 		Integer data = null;
 		
 		// No corresponding constants in Bukkit
 		if (filtered.equals("NORMAL") || filtered.equals("COAL_NORMAL"))
 			data = COAL_NORMAL;
 		else if (filtered.equals("CHARCOAL") || filtered.equals("CHAR_COAL"))
 			data = COAL_CHARCOAL;
 		else
 			ParsingException.fromFormat("Unable to find coal type %s", tokens.peek());
 		
 		tokens.remove();
 		return data;
 	}
 	
 	private int getTreeSpecies(Queue<String> tokens, String filtered) throws ParsingException {
 
 		try {
 			Integer data = null;
 		
 			// Hard coded names
 			if (filtered.equals("OAK"))
 				data = 0;
 			else
 				data = (int) TreeSpecies.valueOf(filtered).getData();
 			
 			tokens.remove();
 			return data;
 			
 		} catch (IllegalArgumentException e) {
 			throw ParsingException.fromFormat("Unable to find tree species %s.", tokens.peek());
 		}
 	}
 	
 	private int getStepMaterial(Queue<String> tokens) throws ParsingException {
 		
 		Material material = getMaterial(tokens.peek());
 		Step step = new Step();
 		
 		// Get texture durability value
 		if (step.getTextures().contains(material)) {
 			tokens.remove();
 			return (int) new Step(material).getData();	
 		} else {
 			throw ParsingException.fromFormat("Unable to parse texture material %s.", tokens.peek());
 		}
 	}
 	
 	private int getSandstoneType(Queue<String> tokens, String filtered) throws ParsingException {
 		
 		Integer data = null;
 		
 		// Extra types
 		if (filtered.equals("NORMAL"))
 			data = 0;
 		if (filtered.equals("CHISELED"))
 			data = 1;
 
 		// Note: Like many enums, this doesn't correspond to MinecraftWiki
 		try {
 			data = (int) SandstoneType.valueOf(filtered).getData();
 		} catch (IllegalArgumentException e) {
 			throw ParsingException.fromFormat("Unable to find sandstone type %s.", tokens.peek());
 		}
 		
 		tokens.remove();
 		return data;
 	}
 	
 	private int getMonsterEgg(Queue<String> tokens, String filtered) throws ParsingException {
 		
 		Material material = getMaterial(tokens.peek());
 		MonsterEggs monsterEgg = new MonsterEggs();
 		
 		// Get texture durability value
 		if (monsterEgg.getTextures().contains(material)) {
 			tokens.remove();
 			return (int) new MonsterEggs(material).getData();	
 		} else {
 			throw ParsingException.fromFormat("Unable to parse texture material %s", tokens.peek());
 		}
 	}
 	
 	private int getSmoothstoneType(Queue<String> tokens, String filtered) throws ParsingException {
 		
 		Integer data = null;
 		
 		// Alternatives
 		if (filtered.equals("CHISELED"))
 			data = 3;
 		
 		try {
 			data = (int) SmoothBrickType.valueOf(filtered).getData();
 		} catch (IllegalArgumentException e) {
 			throw ParsingException.fromFormat("Unable to find smoothstone type %s.", tokens.peek());
 		}
 		
 		tokens.remove();
 		return data;
 	}
 	
 	private int getDyeColor(Queue<String> tokens, String filtered) throws ParsingException {
 		
 		try {
 			// Look up value directly
 			Integer data = (int) DyeColor.valueOf(filtered).getData();
 			
 			tokens.remove();
 			return data;
 			
 		} catch (IllegalArgumentException e) {
 			throw ParsingException.fromFormat("Unable to find dye color %s.", tokens.peek());
 		}
 	}
 	
 	private int getGrassSpecies(Queue<String> tokens, String filtered) throws ParsingException {
 		
 		try {
 			Integer data = (int) GrassSpecies.valueOf(filtered).getData();
 			
 			tokens.remove();
 			return data;
 			
 		} catch (IllegalArgumentException e) {
 			throw ParsingException.fromFormat("Unable to grass species %s.", tokens.peek());
 		}
 	}
 	
 	// Special potion parser
 	private PotionQuery parsePotion(Queue<String> tokens) throws ParsingException {
 		
 		Integer tier = Parsing.tryParse(tokens);
 		PotionType type = parsePotionType(tokens);
 		
 		// Scan all unused parameters for these options
 		Boolean extended = Parsing.hasElementPrefix(tokens, "extended");
 		Boolean splash = Parsing.hasElementPrefix(tokens, "splash");
 		
		// Check tier
		if (tier != null) {
			
			if (type != null && tier > type.getMaxLevel()) {
				throw ParsingException.fromFormat("Potion level %d is too high.", tier);
			} else if (tier < 1) {
				throw ParsingException.fromFormat("Potion level %d is too low.", tier);
			}
		}
 		
 		// Create the query
 		return new PotionQuery(type, tier, extended, splash);
 	}
 	
 	private PotionType parsePotionType(Queue<String> tokens) throws ParsingException {
 		
		String current = Parsing.peekOrEmpty(tokens);
 		Integer potionID = Parsing.tryParse(tokens);
 		
 		try {
 			PotionType potionType = null;
 			
 			// Parse the potion type
 			if (potionID != null) {
 				potionType = PotionType.getByDamageValue(potionID);
 				// Token has already been removed
 			} else if (current != null) {
 				potionType = PotionType.valueOf(Parsing.getEnumName(current));
 				tokens.remove();
 			}
 			
 			return potionType;
 			
 		} catch (IllegalArgumentException e) {
 			
 			// Handle ID failure and name failure
 			if (potionID == null)
 				throw ParsingException.fromFormat("Unrecognized potion id: %d", potionID);
 			else
 				throw ParsingException.fromFormat("Unrecognized potion name: %s.", current);
 		}
 	}
 	
 	private Material getMaterial(String text) throws ParsingException {
 		
 		Object attempt = fromItemString(text);
 
 		// See if it succeeded
 		if (attempt instanceof ItemQuery) {
 			ItemQuery query = (ItemQuery) attempt;
 			Material material = Material.getMaterial(query.getItemID());
 			return material;
 		} else 
 			return null;
 	}
 }

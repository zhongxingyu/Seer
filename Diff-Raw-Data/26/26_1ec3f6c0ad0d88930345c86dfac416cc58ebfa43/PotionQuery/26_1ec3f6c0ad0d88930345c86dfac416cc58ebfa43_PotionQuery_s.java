 /*
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
 
 package com.comphenix.xp.lookup;
 
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 import org.bukkit.Material;
 import org.bukkit.potion.Potion;
 import org.bukkit.potion.PotionType;
 
 import com.comphenix.xp.parser.Utility;
 import com.google.common.collect.Lists;
 
 public class PotionQuery implements Query {
 
 	// DON'T CARE fields are empty
 	private List<PotionType> type;
 	private List<Integer> level;
 	private List<Boolean> extended;
 	private List<Boolean> splash;
 	
 	// Optimize away object creations
 	private static List<PotionType> noTypes = new ArrayList<PotionType>();
 	private static List<Integer> noLevels = new ArrayList<Integer>();
 	private static List<Boolean> noBooleans = new ArrayList<Boolean>();
 	
 	/**
 	 * Universal query.
 	 * @return Universal query.
 	 */
 	public static PotionQuery fromAny() {
 		return new PotionQuery(noTypes, noLevels, noBooleans, noBooleans);
 	}
 	
 	public static PotionQuery fromAny(PotionType type) {
 		return fromAny(type, null, null, null);
 	}
 	
 	public static PotionQuery fromAny(PotionType type, Integer level) {
 		return fromAny(type, level, null, null);
 	}
 	
 	public static PotionQuery fromAny(PotionType type, Integer level, Boolean extended, Boolean splash) {
 		return new PotionQuery(
 				Utility.getElementList(type),
 				Utility.getElementList(level),
 				Utility.getElementList(extended),
 				Utility.getElementList(splash)
 		);
 	}
 	
 	public static PotionQuery fromExact(PotionType type, Integer level, Boolean extended, Boolean splash) {
 		return new PotionQuery(
 				Lists.newArrayList(type),
 				Lists.newArrayList(level), 
 				Lists.newArrayList(extended),
 				Lists.newArrayList(splash)
 		);
 	}
 	
 	public PotionQuery(List<PotionType> type, List<Integer> level, List<Boolean> extended, List<Boolean> splash) {
 		this.type = type;
 		this.level = level;
 		this.extended = extended;
 		this.splash = splash;
 	}
 	
 	public PotionQuery(Potion potionObject) {
 		if (potionObject == null)
 			throw new IllegalArgumentException("Potion must be non-zero.");
 		
		loadFromPotions(Arrays.asList(potionObject));
 	}
 	
 	public PotionQuery(ItemQuery query) {
 		if (!query.match(Material.POTION))
 			throw new IllegalArgumentException("Can only create potion queries from potions.");
 		if (!query.hasDurability())
 			throw new IllegalArgumentException("Must contain a durability value.");
 		
		List<Potion> potions = new ArrayList<Potion>(); 
 		
 		for (Integer durability : query.getDurability()) {
			potions.add(Potion.fromDamage(durability));
 		}
		
		loadFromPotions(potions);
 	}
 
	private void loadFromPotions(List<Potion> source) {
		reset();
 		
		// Initialize values
		for (Potion potion : source) {
 			type.add(potion.getType());
 			level.add(potion.getLevel());
 			extended.add(potion.hasExtendedDuration());
 			splash.add(potion.isSplash());
 		}
 	}
 	
 	private void reset() {
 		this.type = new ArrayList<PotionType>();
 		this.level = new ArrayList<Integer>();
 		this.extended = new ArrayList<Boolean>();
 		this.splash = new ArrayList<Boolean>();
 	}
 	
 	public List<PotionType> getType() {
 		return type;
 	}
 
 	public List<Integer> getLevel() {
 		return level;
 	}
 
 	public List<Boolean> getExtended() {
 		return extended;
 	}
 
 	public List<Boolean> getSplash() {
 		return splash;
 	}
 	
 	public boolean hasType() {
 		return type != null && !type.isEmpty();
 	}
 	
 	public boolean hasLevel() {
 		return level != null && !level.isEmpty();
 	}
 	
 	public boolean hasExtended() {
 		return extended != null && !extended.isEmpty();
 	}
 	
 	public boolean hasSplash() {
 		return splash != null && !splash.isEmpty();
 	}
 	
 	@Override
 	public int hashCode() {
 		return new HashCodeBuilder(17, 31).
 	            append(type).
 	            append(level).
 	            append(extended).
 	            append(splash).
 	            toHashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj == null)
             return false;
         if (obj == this)
             return true;
         if (obj.getClass() != getClass())
             return false;
 
         PotionQuery other = (PotionQuery) obj;
         return new EqualsBuilder().
             append(type, other.type).
             append(level, other.level).
             append(extended, other.extended).
             append(splash, other.splash).
             isEquals();
 	}
 	
 	@Override
 	public boolean match(Query other) {
 
 		// Yes, we could construct a MobTree, put the current MobQuery 
 		// into it and query after other, but this is faster. Probably.
 		if (other instanceof PotionQuery) {
 			PotionQuery query = (PotionQuery) other;
 			
 			// Make sure the current query is the superset of other
 			return QueryMatching.matchParameter(type, query.type) &&
 				   QueryMatching.matchParameter(level, query.level) &&
 				   QueryMatching.matchParameter(extended, query.extended) &&
 				   QueryMatching.matchParameter(splash, query.splash);
 		}
 		
 		// Query must be of the same type
 		return false;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("Potion|%s|%s|%s|%s", 
 						hasType() ? StringUtils.join(type, ", ") : "", 
 						hasLevel() ? StringUtils.join(level, ", ") : "", 
 						Utility.formatBoolean("extended", extended), 
 						Utility.formatBoolean("splash", splash));
 	}
 
 	@Override
 	public Types getQueryType() {
 		return Types.Potions;
 	}
 }

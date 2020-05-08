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
 
 package com.comphenix.xp.parser.text;
 
 import org.bukkit.entity.EntityType;
 
 import com.comphenix.xp.parser.TextParser;
 import com.comphenix.xp.parser.ParsingException;
 import com.comphenix.xp.parser.Utility;
 import com.comphenix.xp.parser.text.MobMatcher.Category;
 
 public class MobEntityTypeParser extends TextParser<MobMatcher> {
 
 	@Override
 	public MobMatcher parse(String text) throws ParsingException {
 		
 		// Make sure we're not passed an empty element
 		if (Utility.isNullOrIgnoreable(text))
 			throw new ParsingException("Text cannot be empty or null.");
 		
 		Category possibleCategory = MobMatcher.Category.fromName(text);
		
		Integer mobID = tryParse(text);
		String enumName = Utility.getEnumName(text);
		EntityType type = EntityType.valueOf(enumName);
		
		// If this didn't work, try some more alternatives
		if (type == null && possibleCategory == null) {
			if (mobID != null) {
				type = EntityType.fromId(mobID);
				
				if (type == null)
					throw ParsingException.fromFormat("Unable to find a mob with the ID %s", mobID);
				
			} else {
				// Try getting it from the mob names
				type = EntityType.fromName(text);
			}
		}
 		
 		// Check for invalid entries
 		if (possibleCategory == null) {
 			if (type == null) {
 				throw ParsingException.fromFormat("Unable to find a mob with the name %s.", text);
 				
 			} else if (type != null) {
 				if (!type.isAlive())
 					throw ParsingException.fromFormat("%s is not a mob.", text);
 			}
 		} else {
 			if (possibleCategory == Category.SPECIFIC)
 				throw ParsingException.fromFormat("%s is not a mob nor a mob category.");
 		}
 		
 		// It's either a category or a specific mob
 		if (possibleCategory != null)
 			return new MobMatcher(possibleCategory);
 		else
 			return new MobMatcher(type);
 	}
 }

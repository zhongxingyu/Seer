 /**
  * Copyright 2011 Rowan Seymour
  * 
  * This file is part of Refract.
  *
  * Refract is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Refract is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Refract. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.ijuru.refract.renderer;
 
 /**
  * Palette mapping options
  */
 public enum Mapping {
 	REPEAT,
 	REPEAT_CYCLE,
 	CLAMP,
 	SCALE_GLOBAL,
	SCALE_AUTO;
 	
 	/**
 	 * Parses a mapping from a string
 	 * @param str the string to parse
 	 * @return the mapping or null
 	 */
 	public static Mapping parseString(String str) {
 		for (Mapping mapping : values()) {
 			if (mapping.name().equalsIgnoreCase(str))
 				return mapping;
 		}
 		return null;
 	}
 }

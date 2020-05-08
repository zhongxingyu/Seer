 /**
  * Copyright 2011 Rowan Seymour
  * 
  * This file is part of Kumva.
  *
  * Kumva is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Kumva is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Kumva. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.ijuru.kumva;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.ijuru.kumva.util.Utils;
 
 /**
  * A meaning of an entry - can be a word or a more complex explanation
  */
 public class Meaning {
 	private String text;
 	private int flags;
 	
 	public static final int FLAG_OLD = 0x01;
 	public static final int FLAG_RARE = 0x02;
 	public static final int FLAG_SLANG = 0x04;
 	public static final int FLAG_RUDE = 0x08;
 	
 	private static final String[] flagNames = {"old", "rare", "slang", "rude" };
 	
 	/**
 	 * Default constructor
 	 */
 	public Meaning() {
 	}
 	
 	/**
 	 * Constructs a meaning from the given text and flags
 	 * @param text the text
 	 * @param flags the flags
 	 */
 	public Meaning(String text, int flags) {
 		this.text = text;
 		this.flags = flags;
 	}
 	
 	/**
 	 * Gets the meaning txt
 	 * @return the text
 	 */
 	public String getText() {
 		return text;
 	}
 	
 	/**
 	 * Sets the meaning text
 	 * @param text the text
 	 */
 	public void setText(String text) {
 		this.text = text;
 	}
 	
 	/**
 	 * Gets the flags
 	 * @return the flags
 	 */
 	public int getFlags() {
 		return flags;
 	}
 	
 	/**
 	 * Sets the flags
 	 * @param flags the flags
 	 */
 	public void setFlags(int flags) {
 		this.flags = flags;
 	}
 	
 	/**
 	 * Parses a CSV list of flag names into a bit field
 	 * @param str the CSV string
 	 * @return the bit field
 	 */
 	public static int parseFlags(String str) {
		if (Utils.isEmpty(str))
			return 0;
		
 		List<String> flagStrs = Utils.parseCSV(str);
 		if (flagStrs.size() == 0)
 			return 0;
 		
 		// Build bit field
 		int flags = 0;
 		for (int f = 0; f < flagNames.length; f++) {
 			String flagName = flagNames[f];
 			if (flagStrs.contains(flagName)) {
 				flags |= (1 << f);
 			}
 		}
 		
 		return flags;
 	}
 	
 	/**
 	 * Converts a bit field into a CSV string of flag names
 	 * @param flags the bit field
 	 * @return the CSV string
 	 */
 	public static String makeFlagsCSV(int flags) {
 		List<String> flagStrs = new ArrayList<String>();
 		
 		for (int f = 0; f < flagNames.length; f++) {
 			if ((flags & (1 << f)) > 0)
 				flagStrs.add(flagNames[f]);
 		}
 		
 		return Utils.makeCSV(flagStrs);
 	}
 }

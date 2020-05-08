 package com.comphenix.xp.lookup;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Queue;
 
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
 
 public class Parsing {
 	
 	public static Queue<String> getParameterQueue(String text) {
 		
 		String[] components = text.split("\\||:");
 		
 		// Clean up
 		for (int i = 0; i < components.length; i++) 
 			components[i] = components[i].trim().toLowerCase();
 		
 		return toQueue(components);
 	}
 	
 	public static Boolean hasElementPrefix(Collection<String> values, String element) {
 		
 		// See if this element exists
 		for (String current : values) {
 			boolean value = !current.startsWith("!"); // Negative prefix
 	
 			if (element.startsWith(current, value ? 0 : 1))
 				return value;
 		}
 		
 		// No preferense either way
 		return null;
 	}
 	
     public static String getEnumName(String text) {
     	if (text == null)
     		return "";
     	
 		String filtered = text.toUpperCase();
 		return filtered.replaceAll("\\s+", "_").replaceAll("\\W", "");
 	}
     
 	public static String formatBoolean(String booleanName, Boolean value) {
 		// Mirror the query syntax
 		if (value == null)
 			return "";
 		else 
 			return value ? booleanName : "!" + booleanName;
 	}
 	
 	/**
 	 * Determines if a String can be ignored.
 	 * @param param String to test.
 	 * @return TRUE if the string is null, blank or equal to '?', FALSE otherwise.
 	 */
 	public static boolean isNullOrIgnoreable(String param) { 
 	    return param == null || param.trim().length() == 0 || param.trim().equals("?"); 
 	}
 	
 	/**
 	 * Determines if the head of the queue can be ignored. If it can, it will be removed.
 	 * @param param Queue to test.
 	 * @return TRUE if the head of the queue is null, blank or equal to '?', FALSE otherwise.
 	 */
 	public static boolean isNullOrIgnoreable(Queue<String> tokens) {
 		
 		// Consume the element if it is ignoreable
 		if (isNullOrIgnoreable(tokens.peek())) {
			tokens.poll();
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public static Integer tryParse(String value) {
 		return tryParse(toQueue(new String[] { value }), 0);
 	}
 	
 	// Attempt to parse integer
 	public static Integer tryParse(Queue<String> input) {
 		return tryParse(input, null);
 	}
 	
 	// Attempt to parse integer
 	public static Integer tryParse(Queue<String> input, Integer defaultValue) {
 		try { 
 			String peek = input.peek();
 			
 			if (!input.isEmpty() && !isNullOrIgnoreable(peek)) {
 				int result = Integer.parseInt(peek);
 				
 				input.remove();
 				return result;
 			} else {
 				return defaultValue;
 			}
 				
 		} catch (NumberFormatException e) {
 			return defaultValue;
 		}
 	}
 	
 	/**
 	 * Copies the content of a string array into a queue.
 	 * @param input - String array to copy.
 	 * @return The resulting queue.
 	 */
 	public static Queue<String> toQueue(String[] input) {
 		return new LinkedList<String>(Arrays.asList(input));
 	}
 	
 	/**
 	 * Retrieves the head of the queue. If the queue is empty, returns an empty string.
 	 * @param components Queue to retrieve from.
 	 * @return Head of the queue OR an empty string.
 	 */
 	public static String peekOrEmpty(Queue<String> components) {
 		return !components.isEmpty() ? components.peek() : "";
 	}
 }

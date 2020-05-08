 package com.censoredsoftware.demigods.util;
 
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 
import java.util.Collection;
 
 public class Strings
 {
 	/**
 	 * Returns true if the <code>string</code> starts with a vowel.
 	 * 
 	 * @param string the string to check.
 	 */
 	public static boolean beginsWithVowel(String string)
 	{
 		String[] vowels = { "a", "e", "i", "o", "u" };
		return StringUtils.startsWithAny(string, vowels);
 	}
 
 	/**
 	 * Returns true if the <code>string</code> starts with a consonant.
 	 * 
 	 * @param string the string to check.
 	 */
 	public static boolean beginsWithConsonant(String string)
 	{
 		return !beginsWithVowel(string);
 	}
 
 	/**
 	 * Returns true if the <code>string</code> contains any of the strings held in the <code>collection</code>.
 	 * 
 	 * @param check the string to check.
 	 * @param collection the collection given.
 	 */
 	public static boolean containsAnyInCollection(final String check, Collection<String> collection)
 	{
 		return Iterables.any(collection, new Predicate<String>()
 		{
 			@Override
 			public boolean apply(String string)
 			{
 				return StringUtils.containsIgnoreCase(check, string);
 			}
 		});
 	}
 
 	/**
 	 * Checks the <code>string</code> for <code>max</code> capital letters.
 	 * 
 	 * @param string the string to check.
 	 * @param max the maximum allowed capital letters.
 	 */
 	public static boolean hasCapitalLetters(String string, int max)
 	{
 		// Define variables
 		String allCaps = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
 		int count = 0;
 		char[] characters = string.toCharArray();
 		for(char character : characters)
 		{
 			if(allCaps.contains("" + character)) count++;
 			if(count > max) return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Returns a color (red, yellow, green) based on the <code>value</code> and <code>max</code> passed in.
 	 * 
 	 * @param value the actual value.
 	 * @param max the maximum value possible.
 	 */
 	public static ChatColor getColor(double value, double max)
 	{
 		ChatColor color = ChatColor.RESET;
 		if(value < Math.ceil(0.33 * max)) color = ChatColor.RED;
 		else if(value < Math.ceil(0.66 * max) && value > Math.ceil(0.33 * max)) color = ChatColor.YELLOW;
 		if(value > Math.ceil(0.66 * max)) color = ChatColor.GREEN;
 		return color;
 	}
 
 	/**
 	 * Automatically removes underscores and returns a capitalized version of the given <code>string</code>.
 	 * 
 	 * @param string the string the beautify.
 	 */
 	public static String beautify(String string)
 	{
 		return StringUtils.capitalize(string.toLowerCase().replace("_", " "));
 	}
 }

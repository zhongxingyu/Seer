 package com.arcao.sayitloud.text;
 
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * Class that can automatically wrap a given string.<br>
  * <br>
  * Based on c# code for custom wrapping text from Jeffrey Sharkey.<br>
  * Released under GPLv3 at <a href="http://jsharkey.org/projects/rendering/">http://jsharkey.org/projects/rendering/</a>
  *
  * @author Jeffrey Sharkey
  * @author Martin Sloup
  */
 public class TextWrapper {
 	private static final Map<Character, Integer> BREAKABLE = new HashMap<Character, Integer>();
 	
 	static {
 		BREAKABLE.put(' ', 10);
 		BREAKABLE.put(',', 20);
 		BREAKABLE.put(';', 30);
 		BREAKABLE.put('.', 40);
 	}
 	
   /**
    * Cleanse the given text from duplicate (two or more in a row) characters, as
 	 * specified by <code>Breakable</code>.  Will also remove all existing newlines.
    * @param text Text to be cleansed.
    * @return Cleansed text.
    */
 	protected static String cleanse(String text) {
 		text = text.replace('\n', ' ');
 
 		for(Character c : BREAKABLE.keySet()) {
 			String find = String.valueOf(new char[] { c, c});
 			String replace = c.toString();
 			
 			while(text.indexOf(find) != -1)
 				text = text.replace(find, replace);
 		}
 
 		return text;
 	}
 
 	/**
 	 * Perform an automatic wrap given text, a target ratio, and a font ratio.
 	 * @param text Text to automatically wrap.
 	 * @param targetRatio Ratio (height / width) of the target area for this text.
 	 * @param fontRatio Average ratio (height / width) of a single character.
 	 * @return Automatically wrapped text.
 	 */
 	public static String performWrap(String text, float targetRatio, float fontRatio) {
 		StringBuilder wrap = new StringBuilder();
 		text = cleanse(text);
 
		int rows = (int) Math.sqrt(targetRatio * text.length() / fontRatio);
		// bug when for text 'fff' rows is zero
		if (rows == 0)
			rows = 1;
		
		int cols = text.length() / rows;
		int start = cols;
		int index = 0;
		int last;
 
 		for(int i = 0; i < rows - 1; i++) {
 			last = index;
 			index = bestBreak(text, start, cols * 2);
 			wrap.append(text.substring(last, index).trim() + "\n");
 			start = index + cols;
 		}
 
 		wrap.append(text.substring(index));
 		return wrap.toString();
 	}
 
 	/**
 	 * Find the best place to break text given a starting index and a search radius.
 	 * @param text Full text to search through.
 	 * @param start Index in string to start searching at.  Will be used as the center of the radius.
 	 * @param radius Radius (in characters) to search around the given starting index.
 	 * @return Optimal index to break the text at.
 	 */
 	protected static int bestBreak(String text, int start, int radius) {
 		int bestIndex = start;
 		
 		radius = Math.min(Math.min(start + radius, text.length() - 1) - start, start - Math.max(start - radius, 0));
 		
 		float bestWeight = 0;
 		for(int i = start - radius; i <= start + radius; i++) {
 			Integer charWeight = BREAKABLE.get(text.charAt(i));
 			if(charWeight == null) continue;
 
 			float examWeight = charWeight / (float) Math.abs(start - i);
 			
 			if(examWeight > bestWeight) {
 				bestIndex = i;
 				bestWeight = examWeight;
 			}
 		}
 
 		return Math.min(bestIndex + 1, text.length() - 1);
 	}
 }

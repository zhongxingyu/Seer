 /**
  * This package contains everything related to the engine.
  */
 package lv.ailab.lnb.fraktur.translit;
 
 import lv.ailab.lnb.fraktur.util.Tuple;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.HashSet;
 
 
 /**
  * This class performs the transliterantion for the single token.
  */
 public class Engine
 {
 	
 	/**
 	 * Transliterate token.
 	 */
 	public static HashMap<String, Boolean> transliterate (
 		String token, Rules rules)
 	{
 		return transform(token, rules, false);
 	}
 	
 	/**
 	 * Transliterate token by using fuzzy rules, too.
 	 */
 	public static HashMap<String, Boolean> fuzzy (String token, Rules rules)
 	{
 		return transform(token, rules, true);
 	}
 	
 	/**
 	 * Transliterate token, using exact rules or exact rules altogether with
 	 * fuzzy rules.
 	 */
 	public static HashMap<String, Boolean> transform(
 		String token, Rules rules, boolean fuzzy)
 	{
 		int len = token.length();
 		ProcessingData lookUpTable = new ProcessingData(len);
 		String tokenLC = token.toLowerCase();
 		
 		for (int pos = 0; pos < len; pos++)
 		{
 			//String prev = token.substring(0, pos+1);
 			//String prevLC = tokenLC.substring(0, pos+1);
 			
 			boolean addUnaltered = true;
 			
 			// NOTE
 			// In case of bad performance this part can be optimized by using
 			// suffix trees instead of full search.
 			
 			// Go through all suffixes of token current substring.
 			for (int begin = pos; begin >= 0; begin--)
 			{
 				// Currently analyzed token fragment.
 				String suffix = token.substring(begin, pos + 1);
 				// Currently analyzed token fragment lower-cased.
 				String suffixLC = tokenLC.substring(begin, pos + 1);
 				
 				// Apply exact case insensitive rules.
 				if (rules.exact.containsKey(suffixLC))
 				{
 					Tuple<String, Rules.Pos> repl = rules.exact.get(suffixLC);
 					
 					// Preserve capitalization, if target is ALL CAPS or First cap.
 					String capitRepl = capitalize(repl.first, suffix);
 					
 					// Update memorization table.
					if (isAllowed(repl.first, repl.second, pos, len))
 					{
 						lookUpTable.add(
 							pos, pos - suffixLC.length(), capitRepl, false);
 						addUnaltered = false;
 					}
 				}
 				
 				// Apply exact case sensitive rules.
 				if (rules.exactSense.containsKey(suffix))
 				{
 					Tuple<String, Rules.Pos> repl = rules.exact.get(suffix);
 
 					// Update memorization table.
					if (isAllowed(repl.first, repl.second, pos, len))
 					{
 						lookUpTable.add(
 							pos, pos - suffix.length(), repl.first, false);
 						addUnaltered = false;
 					}
 				}
 				
 				// Apply fuzzy case insensitive rules.
 				if (fuzzy && rules.fuzzy.containsKey(suffixLC))
 				{
 					for (Tuple<String, Rules.Pos> repl : rules.fuzzy.get(suffixLC))
 					{
 						// Preserve capitalization, if target is ALL CAPS or
 						// First cap.
 						String capitRepl = capitalize(repl.first, suffix);
 						
 						// Update memorization table.
						if (isAllowed(repl.first, repl.second, pos, len))
 							lookUpTable.add(
 								pos, pos - suffixLC.length(), capitRepl, true);
 					}
 				}
 				
 				// Apply fuzzy case sensitive rules.
 				if(fuzzy && rules.fuzzySense.containsKey(suffix))
 				{
 					for (Tuple<String, Rules.Pos> repl : rules.fuzzySense.get(suffix))
 					{
 						// Update memorization table.
						if (isAllowed(repl.first, repl.second, pos, len))
 							lookUpTable.add(
 								pos, pos - suffix.length(), repl.first, true);
 					}
 				}
 			}
 			
 			// Add "trivial" transliteration.			
 			if (addUnaltered)
 				lookUpTable.add(
 					pos, pos - 1, token.substring(pos, pos + 1), false);
 		}
 		return lookUpTable.transliterations();
 	}
 	
 	/**
 	 * Is this replacement allowed in this position of source string?
 	 */
 	private static boolean isAllowed(
		String replace, Rules.Pos restriction, int sourcePos, int sourceLength)
 	{
 		boolean res = true;
 		switch (restriction)
 		{
 			case BEGIN:
				if (sourcePos != replace.length() - 1) res = false;
 				break;					
 			case END:
				if (sourcePos != sourceLength - 1) res = false;
 				break;
 			case EXACT:
				if (sourcePos != replace.length() - 1 
					|| sourcePos != sourceLength - 1)
 					res = false;
 		}
 		return res;
 	}
 	
 	/**
 	 * Capitalize target similarly as pattern is capitalized. Options ALL CAPS
 	 * or First cap considered only.
 	 */
 	private static String capitalize(String target, String pattern)
 	{
 		// Small caps or punctuation.
 		if (pattern.equals(pattern.toLowerCase()))
 			return target; // Do nothing
 					
 		// All caps.
 		if (pattern.equals(pattern.toUpperCase()))
 			return target.toUpperCase();
 					
 		// First cap.
 		if (Character.isLetter(pattern.charAt(0)) && pattern.equals(
 				pattern.substring(0, 1).toUpperCase()
 				+ pattern.substring(1).toLowerCase()))
 			return target.substring(0, 1).toUpperCase()
 				+ target.substring(1).toLowerCase();
 		return target;
 		
 	}
 
 }

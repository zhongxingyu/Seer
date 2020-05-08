 /*===========================================================================
   Copyright (C) 2008-2012 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.lib.segmentation;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.sf.okapi.common.LocaleId;
 import net.sf.okapi.common.Util;
 
 import com.ibm.icu.text.BreakIterator;
 import com.ibm.icu.text.RuleBasedBreakIterator;
 import com.ibm.icu.util.ULocale;
 
 public class ICURegex {
 	
 	static final int GC_LEXEM = 700; // !!! Update {700} in RBBI_RULES if changed		
 	static final int WB_LEXEM = 200; // Defined by the RBBI word breaker rules
 	static final int PH_LEXEM = 710;	
 	
 	private static final Pattern ICU_PATTERN = Pattern.compile(
 		"\\\\b|\\\\B|\\\\d|\\\\D|\\\\G|\\\\N\\{|\\\\s|\\\\S|\\\\U|\\\\w|" +
 		"\\\\W|\\\\x\\{|\\\\X|\\\\p\\{|\\\\P\\{" // All those processed here
 	); 
 	private static final Pattern icuPatternExtractor = 
 		Pattern.compile(
 			// Place parent rules first (those having smaller rules as fragments)
 			"\\\\x\\{([0-9A-Fa-f]{1,6})}-\\\\x\\{([0-9A-Fa-f]{1,6})}" + // \x{hhhh} range (should go before non-range)					
 			"|\\\\U[0-9A-Fa-f]{8}-\\\\U[0-9A-Fa-f]{8}" + // Uhhhhhhhh range (should go before non-range)
 			"|(\\\\N|\\\\p|\\\\P)\\{.+?\\}" + // Named chars and props
 			"|\\\\x\\{([0-9A-Fa-f]{1,6})}" + // \x{hhhh}, \x{hhhhhh}			
 			"|\\\\U[0-9A-Fa-f]{8}" + // Uhhhhhhhh								
 			""     
 			);
 	
 	private static final Pattern gcPatternExtractor =
 			Pattern.compile("\\\\X");
 	
 //	private static final Pattern gPatternExtractor =
 //			Pattern.compile("\\\\G");
 	
 	private static final Pattern wbPatternExtractor =
 			Pattern.compile("\\\\b|\\\\B");
 	
 //	private static final Pattern xhPatternExtractor =
 //			Pattern.compile("\\\\x\\{([0-9A-Fa-f]{1,6})}");
 	
 	
 	
 	//private RuleBasedBreakIterator iterator;
 	private RuleBasedBreakIterator wbIterator;
 	private Map <LocaleId, RuleBasedBreakIterator> wbIterators;
 	private Map <String, RuleBasedBreakIterator> phIterators;
 	//private Map<String, RuleInfo> rules; // rule + RuleInfo
 	private List<RuleInfo> rules;
 	private Map<String, Placeholder> placeholders; // pattern + Placeholder
 	//private String[] ruleLookup;
 	private String[] placeholderLookup;
 	private Map<CompiledRule, RuleInfo> ruleInfoLookup; 
 	//private List<Set<Character>> placeholderChars;
 	private LocaleId language; // for word boundaries
 	private boolean dirty;
 	private Pattern phPattern; // to determine whether a rule has placeholders
 	private String lastProcessedText;
 	private boolean hasGraphemClusterPh;
 	//private boolean hasGPattern;
 	private boolean hasWordBoundaryPh;
 	private boolean hasICURules;
 
 	//private Set<Character> gcChars;
 	private Placeholder graphemeCluster;
 	private List<Integer> boundaries;
 
 	public ICURegex() {		
 		wbIterators = new TreeMap <LocaleId, RuleBasedBreakIterator>();
 		phIterators = new TreeMap <String, RuleBasedBreakIterator>();
 		//rules = new LinkedHashMap<String, RuleInfo>(); // Order should be guaranteed
 		rules = new LinkedList<RuleInfo>(); // Order should be guaranteed
 		placeholders = new LinkedHashMap<String, Placeholder>(); // Order should be guaranteed
 		boundaries = new ArrayList<Integer>();
 		ruleInfoLookup = new HashMap<CompiledRule, RuleInfo>(); 
 		//placeholderChars = new LinkedList<Set<Character>>(); // Order should be guaranteed
 		//gcChars = new TreeSet<Character>();
 		reset();
 	}
 	
 	public void reset() {
 		rules.clear();
 		placeholders.clear();
 		//placeholderChars.clear();
 		graphemeCluster = Placeholder.createGraphemeCluster(GC_LEXEM);
 		setLanguage(null);
 		boundaries.clear();
 		ruleInfoLookup.clear();
 		dirty = true;
 		lastProcessedText = null;
 		hasGraphemClusterPh = false;
 		hasWordBoundaryPh = false;
 		//hasGPattern = false;
 		hasICURules = false;
 	}
 
 	public void setHasICURules(boolean hasICURules) {
 		this.hasICURules = hasICURules;
 	}
 	
 	public static boolean isICURule(String rule) {
 		return ICU_PATTERN.matcher(rule).find(); 
 	}
 	
 	protected void setLanguage (LocaleId language) {
 		dirty = this.language != language;
 		this.language = language;
 	}
 	
 	public String processRule(String rule) {
 		// Collect ICU patterns, replace with placeholders 
 		Matcher m = icuPatternExtractor.matcher(rule);
 		while (m.find()) {
 			// [\\N{DIGIT ZERO}], brackets around are needed for RBBI
 			String pattern = String.format("[%s]", m.group());
 			Placeholder ph = placeholders.get(pattern);
 //			int index = placeholderLookup.indexOf(pattern);
 //			if (index == -1) {
 			if (ph == null) {
 //				placeholderLookup.add(pattern);
 //				index = placeholderLookup.size() - 1;
 				RuleBasedBreakIterator phIterator;
 				if (phIterators.containsKey(pattern)) {
 					phIterator = phIterators.get(pattern);
 				}
 				else {
 					phIterator = Placeholder.createPhIterator(pattern, PH_LEXEM);
 					phIterators.put(pattern, phIterator);
 				}
 				ph = new Placeholder(placeholders.size(), phIterator, PH_LEXEM);
 				placeholders.put(pattern, ph);
 			}
 //			char placeholder = (char) (PLACEHOLDER_BASE + index);
 //			rule = rule.replace(m.group(), Character.toString(placeholder));
 			// Make other placeholders transparent
 //			String phPattern = String.format("%s(\\u%04X-\\uF8FF)*",
 //					(char) (PLACEHOLDER_BASE + index), (int) PLACEHOLDER_BASE);
 //			rule = rule.replace(m.group(), phPattern);
 			//rule = rule.replace(m.group(), "."); //@@@
 			rule = rule.replace(m.group(), ph.toString());
 			
 			dirty = true;
 		}
 		
 		hasGraphemClusterPh |= 
 				gcPatternExtractor.matcher(rule).find();
 		
 		rule = rule.replace("\b", "\\b"); // Least likely backspace is meant in a rule
 		hasWordBoundaryPh |= 
 				wbPatternExtractor.matcher(rule).find();
 		
 //		hasGPattern |=
 //				gPatternExtractor.matcher(rule).find();
 		
 		// Non-RBBI, handled by replacement
 		//rule = rule.replace(" ", "\\u0020");
 		
 		rule = rule.replace("\\b", Character.toString(Placeholder.WORD_BOUNDARY));
 		rule = rule.replace("\\B", Character.toString(Placeholder.WORD_NON_BOUNDARY));
 		rule = rule.replace("\\X", Character.toString(Placeholder.GRAPHEME_CLUSTER));
 		rule = rule.replace("\\d", "\\p{Nd}");
 		rule = rule.replace("\\D", "\\P{Nd}");
 		rule = rule.replace("\\w", "[\\p{Ll}\\p{Lu}\\p{Lt}\\p{Lo}\\p{Nd}]");
 		rule = rule.replace("\\W", "[^\\p{Ll}\\p{Lu}\\p{Lt}\\p{Lo}\\p{Nd}]");
		rule = rule.replace("\\s", "[\\t\\n\\f\\r\\p{Z}\u200B]");
		rule = rule.replace("\\S", "[^\\t\\n\\f\\r\\p{Z}\u200B]");
 		
 //		// Non-RBBI, handled by mather
 //		m = xhPatternExtractor.matcher(rule);
 //		while(m.find()) {
 //			String hexValue = m.group(1);
 //			rule = rule.replace(m.group(), "\\u" + hexValue);
 //		}
 		
 //		System.out.println(rule);
 		return rule;
 	}
 
 //	private boolean hasICURules() {
 ////		return (placeholders.size() > 0) || 
 ////				hasGraphemClusterPh || hasGPattern || hasWordBoundaryPh;
 //		return hasICURules;
 //	}
 	
 	private boolean containsPlaceholder(String rule) {
 		return phPattern != null && phPattern.matcher(rule).find();
 	}
 
 //	private void addCharsToSet(Set<Character> set, String match) {
 //		for (char c : match.toCharArray()) {
 //			set.add(c);
 //		}		
 //	}
 	
 //	/**
 //	 * Recalculate positions skipping placeholders.
 //	 * @param st a string possibly containing placeholders
 //	 * @param pos position in the string
 //	 * @return position in the string as if all placeholders were removed
 //	 */
 //	public int adjustPos(String st, int pos) {
 //		if (!hasWordBoundaryPh) return pos;
 //		String substr = st.substring(0, pos);
 //		return pos - 
 //			(StringUtil.getNumOccurrences(substr, Character.toString(WORD_BOUNDARY)));
 //	}
 	
 	public void processText(String codedText, List<CompiledRule> compRules) {
 		if (!hasICURules) return;
 		if (dirty) {
 			if (hasWordBoundaryPh) {
 				// Get a word boundary iterator from cache or create a new one
 				if (wbIterators.containsKey(language)) {
 					wbIterator = wbIterators.get(language);
 				}
 				else {
 					wbIterator = (RuleBasedBreakIterator)BreakIterator.getWordInstance(
 						ULocale.createCanonical(language.toString()));
 					wbIterators.put(language, wbIterator);
 				}
 			}
 			
 			// Remember rules with placeholders (next time the rules list won't 
 			// have placeholders in the rules)
 			// Some placeholders (e.g. word boundary-related) are resolved once here 
 			for (int i = 0; i < compRules.size(); i++) {
 				CompiledRule compRule = compRules.get(i);
 				String rule = compRule.pattern.pattern();				
 				//rules.put(rule, new RuleInfo(rule, i));
 				//rules.put(ruleInfo.getRule(), ruleInfo);
 				rules.add(new RuleInfo(rule));
 			}
 			
 			// Compile placeholder pattern
 			if (placeholders.size() > 0) {
 				phPattern = placeholders.size() == 1 ?
 						Pattern.compile(String.format("[\\u%04X]", 
 						(int) Placeholder.BASE))
 						:
 						Pattern.compile(String.format("[\\u%04X-\\u%04X]", 
 								(int) Placeholder.BASE,
 								(int) Placeholder.BASE + placeholders.size() - 1))
 						;
 			}
 			
 //			ruleLookup = 
 //					rules.keySet().toArray(new String[rules.size()]);
 			
 			placeholderLookup = 
 					placeholders.keySet().toArray(new String[placeholders.size()]);
 			
 //			// Compile custom RBBI rules
 //			StringBuilder sb = new StringBuilder();
 //			for (int i = 0; i < placeholders.size(); i++) {
 //				sb.append(String.format("%s{%d};", 
 //						placeholderLookup[i], PH_LEXEM_BASE + i));
 //			}
 //			
 //			// Create RBBIs
 //			iterator = new RuleBasedBreakIterator(s
 //					String.format(RBBI_RULES, sb.toString()));
 			
 			dirty = false;			
 		}
 		
 		if (Util.isEmpty(codedText)) return;		
 		if (codedText.equals(lastProcessedText)) return;
 				
 		boundaries.clear();
 		int start = 0;
 		int end = 0;		
 //		int minPhLexemId = PH_LEXEM_BASE;
 //		int maxPhLexemId = PH_LEXEM_BASE + placeholders.size() - 1;
 		
 		// Word boundary placeholders are extra characters inserted in
 		// the analyzed text before and after the occurrence like parenthesis around an expression.
 		// They are placeholders for zero-width boundaries, and not replacements for actual characters.
 		//if (hasWordBoundaryPh) {
 		if (hasWordBoundaryPh) {
 			start = 0;
 			end = 0;
 			
 			wbIterator.setText(codedText);
 			start = wbIterator.first();
 			end = start;
 			//System.out.println("------------------ Word Boundaries: " + wbIterator.toString());
 			
 			//StringBuilder sb = new StringBuilder();
 			for(;;) {
 				end = wbIterator.next();
 				if (end == BreakIterator.DONE) break;
 				if (start >= end) break;
 				
 				int areaId = wbIterator.getRuleStatus();
 //				System.out.println(String.format("%d: %s - %d-%d", areaId, 
 //						codedText.substring(start, end), start, end));
 				if (areaId == WB_LEXEM) {
 					// Insert the word boundary placeholder before the extracted word					
 					//sb.append(WORD_BOUNDARY);
 					boundaries.add(start);
 					boundaries.add(end);
 				}
 //				sb.append(codedText.substring(start, end));
 //				if (areaId == WB_LEXEM) {
 //					// Insert the word boundary placeholder after the extracted word					
 //					sb.append(WORD_BOUNDARY);
 //				}
 				
 				start = end;
 			}
 			//codedText = sb.toString();
 			//System.out.println("-----" + codedText);
 		}
 		
 		// Collect characters matching patterns 
 //		placeholderChars.clear();
 //		for (int i = 0; i < placeholderLookup.size(); i++) {
 //			placeholderChars.add(new TreeSet<Character>());
 //		}	
 		
 //		start = 0;
 //		end = 0;
 //		
 //		iterator.setText(codedText);
 //		start = iterator.first();
 //		end = start;
 ////		System.out.println("------------------ RBBI rules: " + iterator.toString());
 //		//StringBuilder sb = new StringBuilder();
 //		
 //		for(;;) {
 //			end = iterator.next();
 //			if (end == BreakIterator.DONE) break;
 //			if (start >= end) break;
 //			
 //			int areaId = iterator.getRuleStatus();
 ////			System.out.println(String.format("%d: %s", areaId, 
 ////					codedText.substring(start, end)));
 //			
 //			if (areaId == GC_LEXEM) {
 //				//sb.append(StringUtil.getString(end - start, GRAPHEME_CLUSTER));
 //				String match = codedText.substring(start, end);
 //				graphemeCluster.addChars(match);
 //			}
 //			else if (areaId >= minPhLexemId && areaId <= maxPhLexemId) {
 //				int index = areaId - PH_LEXEM_BASE;
 //				String match = codedText.substring(start, end);
 //				Placeholder ph = placeholders.get(placeholderLookup[index]);
 //				ph.addChars(match);
 //				//addCharsToSet(placeholderChars.get(index), match);
 ////					sb.append(StringUtil.getString(end - start,
 ////						(char) (PLACEHOLDER_BASE + areaId - PH_LEXEM_BASE)));
 //			}
 ////				else {
 ////					sb.append(codedText.substring(start, end));
 ////				}
 //			
 //			start = end;
 //		}
 		
 		if (hasGraphemClusterPh) {
 			graphemeCluster.processText(codedText);
 		}		
 		for (Placeholder ph : placeholders.values()) {
 			ph.processText(codedText);
 		}
 ////@@@		codedText = sb.toString();
 //			System.out.println("-----" + codedText);
 
 		
 		// Adapt and recompile rules for the new text
 		if (rules.size() != compRules.size()) {
 			throw new RuntimeException("Internal rules desynchronized");
 		}
 		
 		ruleInfoLookup.clear();
 //		for (int i = 0; i < ruleLookup.length; i++) {
 //			String rule = ruleLookup[i];
 //			RuleInfo ruleInfo = rules.get(rule);
 		for (int i = 0; i < rules.size(); i++) {
 			RuleInfo ruleInfo = rules.get(i);
 			String rule = ruleInfo.getRule();
 //		for (String rule : rules.keySet()) {
 			
 			// If the rule contains placeholders, replace them with 
 			// sets of matching characters in the input
 			if (hasGraphemClusterPh) {
 				//String chars = charSetToString(gcChars);
 				String chars = graphemeCluster.getChars();
 				//String ph = Character.toString(GRAPHEME_CLUSTER);
 				if (Util.isEmpty(chars)) {
 					// No characters are found for the placeholder,
 					// do nothing, leave the placeholder in the rule,
 					// otherwise Regex fires an error at empty set 
 					// String.format("\\u%04X", (int) gcInfo.toString().charAt(0))
 				}
 				else {
 					rule = rule.replace(graphemeCluster.toString(),	String.format("[%s]+", 
 							chars)); // Set of all characters found for the ph
 				}
 			}
 				
 			if (containsPlaceholder(rule)) {
 				for (int phIndex = 0; phIndex < placeholders.size(); phIndex++) {
 					//String ph = Character.toString((char) (PLACEHOLDER_BASE + phIndex));
 					Placeholder ph = placeholders.get(placeholderLookup[phIndex]);
 					//String chars = charSetToString(placeholders.get(phIndex));
 					String chars = ph.getChars();
 					if (Util.isEmpty(chars)) {
 						// No characters are found for the placeholder,
 						// do nothing, leave the placeholder in the rule,
 						// otherwise Regex fires an error at empty set
 					}
 					else {
 //						rule = rule.replace(ph,	String.format("[%s]", 
 //								chars)); // Set of all characters found for the ph
 						rule = resolvePlaceholder(rule, ruleInfo, ph, chars);
 					}												
 				}
 			}				
 			
 //			System.out.println(String.format("%3d: %s --->\n---> %s\n%s", i, ruleInfo.getRule(),
 //					rule, codedText));
 			
 			// Compile the new rule and place it in the list, replacing the old one
 			CompiledRule oldRule = compRules.get(i);
 			CompiledRule newRule = new CompiledRule(rule, oldRule.isBreak);
 			compRules.set(i, newRule);
 			ruleInfoLookup.put(newRule, ruleInfo);
 		}
 		
 //		int start = 0;
 //		int end = 0;		
 //		int s = 0;
 //		int e = 0;
 //		int originalLen = codedText.length();
 //		int minPhLexemId = PH_LEXEM_BASE;
 //		int maxPhLexemId = PH_LEXEM_BASE + placeholderLookup.size() - 1;
 //				 
 //		start = 0;
 //		end = 0;
 //		
 //		iterator.setText(codedText);
 //		start = iterator.first();
 //		end = start;
 //		System.out.println("------------------ GC Boundaries: " + iterator.toString());
 //		StringBuilder sb = new StringBuilder();
 //		for(;;) {
 //			end = iterator.next();
 //			if (end == BreakIterator.DONE) break;
 //			if (start >= end) break;
 //			
 //			int areaId = iterator.getRuleStatus();
 //			System.out.println(String.format("%d: %s", areaId, 
 //					codedText.substring(start, end)));
 //			if (areaId == GC_LEXEM) {
 //				sb.append(StringUtil.getString(end - start, GRAPHEME_CLUSTER));
 //			}
 //			else if (areaId >= minPhLexemId && areaId <= maxPhLexemId) {
 //				sb.append(StringUtil.getString(end - start,
 //					(char) (PLACEHOLDER_BASE + areaId - PH_LEXEM_BASE)));
 //			}
 //			else {
 //				sb.append(codedText.substring(start, end));
 //			}
 //			
 //			start = end;
 //		}
 ////@@@		codedText = sb.toString();
 //		System.out.println("-----" + codedText);
 		
 		lastProcessedText = codedText;
 	}
 
 	private String resolvePlaceholder(String rule, RuleInfo ruleInfo,
 			Placeholder ph, String chars) {		
 		//rule = rule.replace(phStr,	String.format("[%s]", chars));
 		// String.format("\\u%04X", (int) rule.charAt(1))
 		// String.format("\\u%04X", (int) ph.toString().charAt(0))
 		StringBuilder sb = new StringBuilder();
 		int start = 0;
 		Matcher m = ph.getPhPattern().matcher(rule);
 		while(m.find()) {
 			String st = ruleInfo.isSetArea(m.start()) ? chars :
 				String.format("[%s]", chars);
 			sb.append(rule.substring(start, m.start()));
 			sb.append(st);
 			start = m.end();
 		}
 		if (start < rule.length())
 			sb.append(rule.substring(start));
 		return sb.toString();
 	}
 
 	public boolean verifyPos(int pos, CompiledRule rule, Matcher matcher) {
 		RuleInfo info = ruleInfoLookup.get(rule);
 		if (info == null) return true;
 		
 		return info.verifyPos(pos, matcher, boundaries);
 	}
 
 }

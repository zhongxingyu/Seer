 package spec;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /*
 * Methodology for Regex Expansion into (,),*,|'s
  * 
  * @author Chad Stewart
  */
 public class RegexExpander {
 	private static int i = 0;
 
 	public static String expandRegex(String s) {
 		i = 0;
 		while (i < s.length()) {
 			int u;
 			int v;
 			if (s.charAt(i) == ' ') { // Encounter a space
 				if (s.charAt(i - 1) == '\\') {
 					i += 2;
 				} else {
 					s = s.substring(0, i) + s.substring(i + 1);
 				}
 				continue;
 			} else if (s.charAt(i) == '\\') { // Encounter a Escape
 				i += 2;
 				continue;
 			} else if (s.charAt(i) == ']') { // Encounter an OR block
 				u = s.lastIndexOf('[', i);
 				v = i + 1;
 				String sub = s.substring(u, v);
 
 				if (sub.charAt(1) == '^') { // Negation Or Block
 					sub = s.substring(u, s.indexOf("]", v + 1) + 1);
 					sub = negate(sub);
 					return sub;
 				}
 
 				// Split the OR block to deal with it
 				String[] strs = sub.split("-");
 				String[] ls = new String[strs.length - 1];
 				for (int i = 0; i < ls.length; i++) {
 					if (i > 0) {
 						ls[i] = "[" + strs[i].charAt(strs[i].length() - 1)
 								+ "-" + strs[i + 1].charAt(0) + "]";
 					} else
 						ls[i] = "[" + strs[i].charAt(strs[i].length() - 1)
 								+ "-" + strs[i + 1].charAt(0) + "]";
 				}
 				for (String m : ls) { // Add the OR blocks together
 					if (m.length() == 5) {
 						sub = sub.replace(m.substring(1, m.length() - 1), "");
 
 					} else
 						sub = sub.replace(m.substring(2, m.length() - 1), "");
 				}
 				sub = OrThisShit(sub); // Evaluate the first OR block
 				for (String m : ls) {
 					if (!sub.isEmpty())
 						sub = sub + "|" + expand(m); // Evaluate the spread OR blocks
 				}
 				sub = sub.replace("()|", "");
 				s = s.substring(0, u) + "(" + sub + ")" + s.substring(v);
 				i--;
 			} else if (s.charAt(i) == '+') { // Evaluate a +
 				if (s.charAt(i - 1) == ']') { // with hard brackets
 					u = s.lastIndexOf('[', i);
 					v = i;
 					i += s.substring(u, v).length() + 1;
 					s = s.substring(0, v) + s.substring(u, v) + "*"
 							+ s.substring(v + 2);
 				} else if (s.charAt(i - 1) == ')') { // with parens
 					String sub = findSub(s.substring(0, i));
 
 					s = s.substring(0, i) + sub + "*"
 							+ s.substring(i + 1, s.length());
 					i += sub.length();
 				} else { // Anything else
 					if (s.charAt(i - 1) == '\\') { // Escape the +
 						s = s.subSequence(0, i) + "(" + s.substring(i - 1, i)
 								+ s.substring(i - 1, i) + "*" + ")"
 								+ s.substring(i + 1);
 					} else {
 						s = s.subSequence(0, i) + "(" + s.substring(i - 1, i)
 								+ "*" + ")" + s.substring(i + 1);
 						i++;
 					}
 				}
 			}
 			i++;
 		}
 
 		s = stripOuterParens(s);
 
 		return s;
 	}
 
 	public static String stripOuterParens(String s) {
 		int firstParen = s.indexOf("(");
 
 		if (firstParen < 0) {
 			return s;
 		}
 
 		int numParens = 1;
 		int maxParens = 1;
 
 		int idx = firstParen + 1;
 
 		for (; idx < s.length(); ++idx) {
 			char c = s.charAt(idx);
 
 			if (c == '(') {
 				++numParens;
 				++maxParens;
 			} else {
 				break;
 			}
 		}
 
 		idx = s.indexOf(")", idx);
 
 		for (; idx < s.length(); ++idx) {
 			char c = s.charAt(idx);
 
 			if (c == ')') {
 				--numParens;
 
 				if (numParens == 0) {
 					StringBuilder sb = new StringBuilder();
 					sb.append(s.substring(0, firstParen));
 					sb.append("(");
 					sb.append(s.substring(firstParen + maxParens, idx - maxParens + 1));
 					sb.append(")");
 					sb.append(s.substring(idx + 1));
 					return sb.toString();
 				}
 			} else {
 				return s;
 			}
 		}
 
 		return s;
 	}
 
 	/*
 	 * Negation String Handler
 	 */
 	private static String negate(String sub) {
 		String mainSet = sub.substring(sub.lastIndexOf("["), sub.length());
 
 		String[] strs = mainSet.split("-");
 		String[] ls = new String[strs.length - 1];
 		for (int i = 0; i < ls.length; i++) {
 			if (i > 0) {
 				ls[i] = "[" + strs[i].charAt(strs[i].length() - 1) + "-"
 						+ strs[i + 1].charAt(0) + "]";
 			} else
 				ls[i] = "[" + strs[i].charAt(strs[i].length() - 1) + "-"
 						+ strs[i + 1].charAt(0) + "]";
 		}
 		for (String m : ls) {
 			if (m.length() == 5) {
 				mainSet = mainSet.replace(m.substring(1, m.length() - 1), "");
 
 			} else
 				mainSet = mainSet.replace(m.substring(2, m.length() - 1), "");
 		}
 		mainSet = OrThisShit(mainSet);
 		for (String m : ls) {
 			if (!mainSet.isEmpty())
 				mainSet = mainSet + "|" + expand(m);
 		}
 		mainSet = mainSet.replace("()|", "");
 		mainSet = mainSet.replace(")|(", "|");
 
 		Set<Character> chars = new HashSet<Character>();
 		int n = 1;
 		while (n < mainSet.length()) {
 			chars.add(mainSet.charAt(n));
 			n += 2;
 		}
 
 		String rem = sub.substring(0, sub.indexOf("]", 0) + 1);
 		rem = "[" + rem.substring(2);
 
 		strs = rem.split("-");
 		ls = new String[strs.length - 1];
 		for (int i = 0; i < ls.length; i++) {
 			if (i > 0) {
 				ls[i] = "[" + strs[i].charAt(strs[i].length() - 1) + "-"
 						+ strs[i + 1].charAt(0) + "]";
 			} else
 				ls[i] = "[" + strs[i].charAt(strs[i].length() - 1) + "-"
 						+ strs[i + 1].charAt(0) + "]";
 		}
 		for (String m : ls) {
 			if (m.length() == 5) {
 				rem = rem.replace(m.substring(1, m.length() - 1), "");
 
 			} else
 				rem = rem.replace(m.substring(2, m.length() - 1), "");
 		}
 		rem = OrThisShit(rem);
 		for (String m : ls) {
 			if (!rem.isEmpty())
 				rem = rem + "|" + expand(m);
 		}
 		rem = rem.replace("()|", "");
 		rem = rem.replace(")|(", "|");
 
 		n = 1;
 		while (n < rem.length()) {
 			chars.remove((rem.charAt(n)));
 			n += 2;
 		}
 
 		String comp = "(";
 		for (char c : chars) {
 			comp += c + "|";
 		}
 		return comp.substring(0, comp.length() - 1) + ")";
 	}
 
 	/*
 	 * Finds substrings (adds parens)
 	 */
 	private static String findSub(String s) {
 		int i = s.length() - 2;
 		int counter = 1;
 		while (counter != 0) {
 			if (s.charAt(i) == ')')
 				counter++;
 			else if (s.charAt(i) == '(')
 				counter--;
 			i--;
 		}
 		return s.substring(i + 1, s.length());
 	}
 
 	/*
 	 * Pulls apart an OR block
 	 */
 	private static String OrThisShit(String s) {
 		int n = 1;
 		while (n < s.length() - 2) {
 			s = s.substring(0, n + 1) + "|" + s.substring(n + 1);
 			n += 2;
 			i += 1;
 		}
 		i++;
 		return "(" + s.substring(1, s.length() - 1) + ")";
 	}
 
 	/*
 	 * Expands and OR block with a spread
 	 */
 	private static String expand(String sub)
 			throws StringIndexOutOfBoundsException {
 		char lb = sub.charAt(1);
 		char ub = sub.charAt(sub.length() - 2);
 		int indx = 1;
 
 		while (indx < sub.length() - 1) {
 			if (lb == ub) {
 				sub = "(" + sub.substring(1, indx)
 						+ sub.substring(indx + 1, sub.length() - 1) + ")";
 				i += indx - 2;
 				return sub;
 			} else if (indx == 1) {
 				sub = sub.substring(0, indx) + lb + "|"
 						+ sub.substring(indx + 1, sub.length());
 				lb += 1;
 				indx += 2;
 			} else {
 				sub = sub.substring(0, indx) + lb + "|"
 						+ sub.substring(indx, sub.length());
 				lb += 1;
 				indx += 2;
 			}
 		}
 
 		i += indx;
 		return sub;
 	}
 }

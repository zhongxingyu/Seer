 package scannergenerator;
 
 public class RegexExpander {
 	private static int i = 0;
 
 	public static void main(String[] args) {
 		RegexExpander r = new RegexExpander();
 		System.out.println(r.curseAgain("[a-g0-8]   sd  \\         sd"));
 	}
 
 	public static String curseAgain(String s) {
 		i = 0;
 		while (i < s.length()) {
 			int u = 0;
 			int v = 0;
 			if (s.charAt(i) == ' ') {
 				if (s.charAt(i - 1) == '\\') {
 					i += 2;
 				} else {
 					s = s.substring(0, i) + s.substring(i + 1);
 				}
 				continue;
 			} else if (s.charAt(i) == '\\') {
 				i += 2;
 				continue;
 			} else if (s.charAt(i) == ']') {
 				u = s.lastIndexOf('[', i);
 				v = i + 1;
 				String sub = s.substring(u, v);
 
 				String[] strs = sub.split("-");
 				String[] ls = new String[strs.length - 1];
 				String fin = "";
 				boolean split = false;
 				int count = -1;
 				for (int i = 0; i < ls.length; i++) {
 					if (i > 0) {
 						ls[i] = "[" + strs[i].charAt(strs[i].length() - 1)
 								+ "-" + strs[i + 1].charAt(0) + "]";
 						split = true;
 					} else
 						ls[i] = "[" + strs[i].charAt(strs[i].length() - 1)
 								+ "-" + strs[i + 1].charAt(0) + "]";
 					fin += ls[i];
 					count++;
 				}
 				for (String m : ls) {
 					if (m.length() == 5)
 						sub = sub.replace(m.substring(1, m.length() - 1), "");
 					else
 						sub = sub.replace(m.substring(2, m.length() - 1), "");
 				}
 				sub = OrThisShit(sub);
 				for (String m : ls) {
 					if (!sub.isEmpty())
 						sub = sub + "|" + expand(m);
 				}
 				sub = sub.replace("()|", "");
 				s = s.substring(0, u) + "(" + sub + ")" + s.substring(v);
 				i--;
 			} else if (s.charAt(i) == '+') {
 				if (s.charAt(i - 1) == ']') {
 					u = s.lastIndexOf('[', i);
 					v = i;
 					i += s.substring(u, v).length() + 1;
 					s = s.substring(0, v) + s.substring(u, v) + "*"
 							+ s.substring(v + 2);
 				} else if (s.charAt(i - 1) == ')') {
 					String sub = findSub(s.substring(0, i));
 
 					s = s.substring(0, i) + sub + "*"
 							+ s.substring(i + 1, s.length());
					i += sub.length();
 				} else {
 					if (s.charAt(i - 1) == '\\') {
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
 		if (decouple(s))
 			s = s.substring(1, s.length() - 1);
 		return s;
 	}
 
 	private static boolean decouple(String s) {
 		// TODO Auto-generated method stub
 		int i = 0;
 		int count = 0;
 		boolean up = true;
 		while (i < s.length() - 1) {
 			if (s.charAt(i) == '(') {
 				if (!up)
 					return false;
 				if (s.charAt(i + 1) == '(')
 					count++;
 			} else if (s.charAt(i) == ')')
 				up = false;
 			if (s.charAt(i + 1) == ')') {
 				count--;
 			}
 			i++;
 		}
 		return true;
 	}
 
 	private static String findSub(String s) {
 		// TODO Auto-generated method stub
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
 
 	private static String expand(String sub)
 			throws StringIndexOutOfBoundsException {
 		// TODO Auto-generated method stub
 		char lb = sub.charAt(1);
 		char ub = sub.charAt(sub.length() - 2);
 		int indx = 1;
 		int l = sub.length();
 
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

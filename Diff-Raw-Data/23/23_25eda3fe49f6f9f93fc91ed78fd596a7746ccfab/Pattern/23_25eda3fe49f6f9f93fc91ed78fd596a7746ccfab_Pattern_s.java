 package com.redcareditor.mate;
 
 import java.util.*;
 
 import com.redcareditor.plist.Dict;
 
 public class Pattern {
 	public Grammar grammar;
 	public String name;
 	public boolean disabled;
 
 	public static Pattern createPattern(List<Pattern> allPatterns, Dict dict) {
 		if (dict.containsElement("match")) {
 			return new SinglePattern(allPatterns, dict);
 		}
 
 		if (dict.containsElement("include")) {
 			return new IncludePattern(dict);
 		}
 
 		if (dict.containsElement("begin")) {
 			return new DoublePattern(allPatterns, dict);
 		}
 
 		return null;
 	}
 
 	public static Map<Integer, String> makeCapturesFromPlist(Dict pd) {
 		if (pd == null)
 			return new HashMap<Integer, String>();
 		Dict pcd;
 		String ns;
 		Map<Integer, String> captures = new HashMap<Integer, String>();
 		for (String sCapnum : pd.value.keySet()) {
 			int capnum = Integer.parseInt(sCapnum);
 			pcd = pd.getDictionary(sCapnum);
 			ns = pcd.getString("name");
 //			System.out.printf("capture: %d, %s\n", capnum, ns);
 			captures.put((Integer) capnum, ns);
 		}
 		return captures;
 	}
 
 	public static void replaceIncludePatterns(List<Pattern> patterns, Grammar grammar) {
 		replaceRepositoryIncludes(patterns, grammar);
 		replaceBaseAndSelfIncludes(patterns, grammar);
 	}
 
 	public static void replaceRepositoryIncludes(List<Pattern> patterns, Grammar grammar) {
 		List<Pattern> includePatterns = new ArrayList<Pattern>();
 		List<Pattern> patternsToInclude = new ArrayList<Pattern>();
 		boolean anyIncluded = true;
 		while (anyIncluded) {
 			anyIncluded = false;
 			for (Pattern p : patterns) {
 				if (p instanceof IncludePattern && p.name.startsWith("#")) {
 					includePatterns.add(p);
 					String reponame = p.name.substring(1, p.name.length());
 					List<Pattern> repositoryEntryPatterns = grammar.repository.get(reponame);
 					if (repositoryEntryPatterns != null) {
 						for (Pattern p2 : repositoryEntryPatterns) {
 							patternsToInclude.add(p2);
 						}
 					} else {
 						System.out.printf("warning: couldn't find repository key '%s' in grammar '%s'\n", reponame,
 								grammar.name);
 					}
 				}
 			}
 			removePatterns(patterns, includePatterns);
 			addPatterns(patterns, patternsToInclude);
 			includePatterns.clear();
 			patternsToInclude.clear();
 		}
 	}
 
 	public static void replaceBaseAndSelfIncludes(List<Pattern> patterns, Grammar grammar) {
 		List<Pattern> includePatterns = new ArrayList<Pattern>();
 		List<Pattern> patternsToInclude = new ArrayList<Pattern>();
 		boolean alreadySelf = false; // some patterns have $self twice
 		Grammar ng;
 		for (Pattern p : patterns) {
 			if (p instanceof IncludePattern) {
 				if (p.name.startsWith("$")) {
 					includePatterns.add(p);
 					if ((p.name.equals("$self") || p.name.equals("$base")) && !alreadySelf) {
 						alreadySelf = true;
 						patternsToInclude.addAll(grammar.allPatterns);
 					}
 				} else if ((ng = Grammar.findByScopeName(p.name)) != null) {
 					ng.initForUse();
 					includePatterns.add(p);
 					patternsToInclude.addAll(ng.allPatterns);
 				} else {
					System.out.printf("unknown include pattern: %s\n", p.name);
 				}
 			}
 		}
 		patterns.removeAll(includePatterns);
 		patterns.addAll(patternsToInclude);
 	}
 
 	private static void removePatterns(List<Pattern> patlist, List<Pattern> ps) {
 		for (Pattern p : ps) {
 			patlist.remove(p);
 		}
 	}
 
 	private static void addPatterns(List<Pattern> patlist, List<Pattern> ps) {
 		for (Pattern p : ps) {
 			patlist.add(p);
 		}
 	}
 
 	public void setDisabled(Dict dict) {
 		if (dict.containsElement("disabled")) {
 			int intn = dict.getInt("disabled");
 			switch (intn) {
 			case 1:
 				disabled = true;
 				break;
 			default:
 				disabled = false;
 				break;
 			}
 		} else {
 			disabled = false;
 		}
 	}
 }

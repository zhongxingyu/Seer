 package com.redcareditor.mate;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.redcareditor.onig.Rx;
 import com.redcareditor.plist.Dict;
 import com.redcareditor.plist.PlistNode;
 import com.redcareditor.plist.PlistPropertyLoader;
 
 public class Grammar {
 	public String name;
 	public String fileName;
 	public Dict plist;
 	private PlistPropertyLoader propertyLoader;
 	public String[] fileTypes;
 	public String keyEquivalent;
 	public String scopeName;
 	public String comment;
 
 	public List<Pattern> allPatterns;
 	public List<Pattern> singlePatterns;
 	public Map<String, List<Pattern>> repository;
 	public Rx firstLineMatch;
 	public Rx foldingStartMarker;
 	public Rx foldingStopMarker;
 
 	public Grammar(Dict plist) {
 		propertyLoader = new PlistPropertyLoader(plist, this);
 		this.plist = plist;
 	}
 
 	public void initForReference() {
 		String[] properties = new String[] { "name", "keyEquivalent", "scopeName", "comment" };
 		for (String property : properties) {
 			propertyLoader.loadStringProperty(property);
 		}
 		propertyLoader.loadRegexProperty("firstLineMatch");
 		fileTypes = plist.getStrings("fileTypes");
 	}
 
 	public void initForUse() {
 		if (loaded())
 			return;
 		
 		initForReference();
 		propertyLoader.loadRegexProperty("foldingStartMarker");
 		propertyLoader.loadRegexProperty("foldingStopMarker");
 
 		loadPatterns();
 		loadRepository();
 		replaceIncludePatterns();
 	}
 
 	private void loadPatterns() {
 		allPatterns = new ArrayList<Pattern>();
 		Dict[] patterns = plist.getDictionaries("patterns");
 		for (Dict p : patterns) {
 			Pattern pattern = Pattern.createPattern(allPatterns, p);
 			if (pattern != null) {
 				pattern.grammar = this;
 				allPatterns.add(pattern);
 			}
 		}
 	}
 
 	private void loadRepository() {
 		repository = new HashMap<String, List<Pattern>>();
 		Dict plistRepo = plist.getDictionary("repository");
 		Dict plistRepoEntry;
 		for (String key : plistRepo.keys()) {
 //			System.out.printf("loading repository entry: %s\n", key);
 			List<Pattern> repoArray = new ArrayList<Pattern>();
 			plistRepoEntry = plistRepo.getDictionary(key);
 			if (plistRepoEntry.containsElement("begin") || plistRepoEntry.containsElement("match")) {
 //				System.out.printf("    contains begin or match\n");
 				Pattern pattern = Pattern.createPattern(allPatterns, plistRepoEntry);
 				if (pattern != null) {
 					pattern.grammar = this;
 					repoArray.add(pattern);
 				}
 			}
 			if (plistRepoEntry.containsElement("patterns")) {
 //				System.out.printf("    contains patterns\n");
 				for (PlistNode<?> plistPattern : plistRepoEntry.getArray("patterns")) {
 					Pattern pattern = Pattern.createPattern(allPatterns, (Dict) plistPattern);
 					if (pattern != null) {
 						pattern.grammar = this;
 						repoArray.add(pattern);
 					}
 				}
 			}
 			repository.put(key, repoArray);
 		}
 	}
 
 	private void replaceIncludePatterns() {
 		for (Pattern p : allPatterns) {
//			System.out.printf("replaceIncludePattern for %s\n", p.name);
 			if (p instanceof DoublePattern) {
 				Pattern.replaceIncludePatterns(((DoublePattern) p).patterns, this);
 			}
 		}
 		Pattern.replaceIncludePatterns(allPatterns, this);
 	}
 
 	public static Grammar findByScopeName(String scope) {
 		for (Bundle b : Bundle.bundles)
 			for (Grammar g : b.grammars)
 				if (g.scopeName.equals(scope))
 					return g;
 		return null;
 	}
 
 	private boolean loaded() {
 		return allPatterns != null && repository != null;
 	}
 }

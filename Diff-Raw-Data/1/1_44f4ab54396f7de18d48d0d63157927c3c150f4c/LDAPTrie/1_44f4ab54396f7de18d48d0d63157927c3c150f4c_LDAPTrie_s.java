 package uk.ac.cam.cl.dtg.ldap;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 class LDAPTrie<T extends LDAPObject> {
 
 	private Map<Character, LDAPTrieNode<T>> roots;
 	String result;
 	String criteria;
 
 	LDAPTrie() {
 		roots = new WeakHashMap<Character, LDAPTrieNode<T>>(1);
 		result = "user";
 		criteria = "uid";
 	}
 
 	LDAPTrie(String result, String criteria) {
 		roots = new WeakHashMap<Character, LDAPTrieNode<T>>();
 		this.result = result;
 		this.criteria = criteria;
 	}
 
 	LDAPTrie(List<T> initMatches) {
 		roots = new HashMap<Character, LDAPTrieNode<T>>();
 		addMatches(initMatches);
 	}
 
 	void addMatches(List<T> matches) {
 		
 		for (T m : matches) {
 			addMatch(m);
 		} 
 	}
 
 	void addMatch(T match) {
 
 		
 		String key;
 		if (criteria.equals("groupTitle") || criteria.equals("sn")) {
 			key = match.getName();
 		} else {
 			key = match.getID();
 		}
 
 		// For case insensitive matching
 		key = key.toLowerCase();
 
 		char[] chars = key.toCharArray();
 
 		LDAPTrieNode<T> currentNode = null;
 
 		if (!roots.containsKey(chars[0])) {
 			roots.put(chars[0], new LDAPTrieNode<T>(chars[0]));
 		}
 
 		currentNode = roots.get(chars[0]);
 
 		for (int i = 1; i < chars.length; i++) {
 			if (!currentNode.children.containsKey(chars[i])) {
 				currentNode.addChild(chars[i], new LDAPTrieNode<T>(chars[i]));
 			}
 
 			currentNode = currentNode.getChild(chars[i]);
 
 			// If its the last letter of the crisd put the user object in
 			if (i == chars.length - 1) {
 				currentNode.setData(match);
 			}
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	List<T> getMatches(String x) throws LDAPObjectNotFoundException {
 
 		// For case insensitive matching
 		x = x.toLowerCase();
 
 		char[] chars = x.toCharArray();
 
 		LDAPTrieNode<T> currentNode = null;
 
 		if (!roots.containsKey(chars[0])) {
 			if (result.equals("group")) {
 				addMatches((List<T>) LDAPProvider.multipleGroupQuery(criteria,
 						x, true));
 			} else {
 				addMatches((List<T>) LDAPProvider.multipleUserQuery(criteria,
 						x, true));
 			}
 		}
 
 		currentNode = roots.get(chars[0]);
 
 		List<T> matches = new ArrayList<T>();
 
 		if (currentNode == null) { // if there were no matches at all
 			return matches;
 		}
 
 		for (int i = 1; i < chars.length; i++) {
 			if (!currentNode.children.containsKey(chars[i])) { // no more stored
 																// matches, need
 																// to get more
 																// from LDAP
 				if (result.equals("group")) {
 					addMatches((List<T>) LDAPProvider.multipleGroupQuery(
 							criteria, x.substring(0, i + 1), true));
 					currentNode = currentNode.getChild(chars[i]);
 				} else {
 					addMatches((List<T>) LDAPProvider.multipleUserQuery(
 							criteria, x.substring(0, i + 1), true));
 					currentNode = currentNode.getChild(chars[i]);
 				}
 			} else {
 				currentNode = currentNode.getChild(chars[i]);
 			}
 
 			if (i == chars.length - 1) { // If this is the end of the search
 											// string get all results
 				if(currentNode!=null){
 					matches = currentNode.getPrefixMatches(matches);
 				} else {
 					return new ArrayList<T>();
 				}
 			}
 		}
 
 		return matches;
 	}
 
 }

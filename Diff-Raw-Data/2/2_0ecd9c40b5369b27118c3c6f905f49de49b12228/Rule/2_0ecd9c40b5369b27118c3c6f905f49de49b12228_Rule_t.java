 package greenrubber.parser;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Represents a "mini-rule" (one block of an OR)
  *
  */
 public class Rule implements Iterable<String> {
 	private Grammar grammar;
 	private String name;
 	private List<String> rhs;
 	
 	public Rule(Grammar grammar, String name) {
 		this.grammar = grammar;
 		this.name = name;
 		this.rhs = new LinkedList<String>();
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public Set<String> getFirstSet() {
 		Set<String> firstSet = new HashSet<String>();
 		
 		int k = 1;
 		for (String xK : rhs) {
 			Set<String> xKFirstSet = grammar.getFullFirstSet(xK);
 			firstSet.addAll(xKFirstSet);
 			
 			if (!xKFirstSet.contains("")) {
 				break;
 			}
 			
 			++k;
 		}
 		
 		if (k > rhs.size()) {
 			firstSet.add("");
 		}
 		
 		return firstSet;
 	}
 	
 	public Set<String> getFollowSet() {
 		Set<String> followSet = new HashSet<String>();
 		
		if (grammar.getStartVariable().equals(name)) {
 			followSet.add("$");
 		}
 		
 		for (int i = 0; i < rhs.size(); ++i) {
 			if (grammar.isTerminal(rhs.get(i))) {
 				continue;
 			}
 			
 			//TODO does it want the first set or full first set?
 			Set<String> tempFirstSet = makeTemporaryRule(i + 1).getFirstSet();
 			boolean tempContainsEpsilon = tempFirstSet.remove("");
 			followSet.addAll(tempFirstSet);
 			
 			if (tempContainsEpsilon) {
 				followSet.addAll(grammar.getFullFollowSet(rhs.get(i)));
 			}
 		}
 		
 		return followSet;
 	}
 	
 	private Rule makeTemporaryRule(int startIndex) {
 		Rule r = new Rule(grammar, "temp");
 		
 		for (int i = startIndex; i < rhs.size(); ++i) {
 			r.concatenate(rhs.get(i));
 		}
 		
 		return r;
 	}
 	
 	public void concatenate(String s) {
 		rhs.add(s);
 	}
 	
 	public Iterator<String> iterator() {
 		return rhs.iterator();
 	}
 	
 }

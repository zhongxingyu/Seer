 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * We assume any symbol whose first and last characters are '<' and '>' are 
  * nonterminals. The first and follow sets of each nonterminal live here. Also 
  * contains a list of rules.
  * 
  * @author dgreenhalgh
  *
  */
 public class NonTerminal extends Symbol {
 	
 	private List<Rule> rules = new ArrayList<Rule>();
 
   Set<Token> firstSet = new HashSet<Token>();
	Set<Token> followSet = new HashSet<Token>();
 	
 	public NonTerminal() {}
 	
 	public NonTerminal(String text) {
 		super(text);
 	}
 	
 	public NonTerminal(String text, boolean isStart) {
 		super(text);
 	}
 	
 	public List<Rule> getRules() {
 		return rules;
 	}
 	
 	public void addRule(Rule rule) {
 		if(!rules.contains(rule))
 			rules.add(rule);
 	}
 
   public Set<Token> getFirstSet() {
     return firstSet;
   }
 
   public void setFirstSet(Set<Token> firstSet) {
     this.firstSet = firstSet;
   }
 
   public boolean addToFirstSet(Token token) {
     return firstSet.add(token);
   }
 
   public boolean addAllToFirstSet(Set<Token> tokens) {
     return firstSet.addAll(tokens);
   }
 
 	public String toString() {
 		return super.toString();
 	}
 }
